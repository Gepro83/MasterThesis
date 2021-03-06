package ac.at.wu.conceptfinder.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.cybozu.labs.langdetect.LangDetectException;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.DatasetManager;
import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.DatasetSearchMask;
import ac.at.wu.conceptfinder.storage.JsonLdFile;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptCreator;
import ac.at.wu.conceptfinder.stringanalysis.ConceptDetectionException;
import ac.at.wu.conceptfinder.stringanalysis.ConceptDetector;
import ac.at.wu.conceptfinder.stringanalysis.ConceptScores;
import ac.at.wu.conceptfinder.stringanalysis.ConceptText;
import ac.at.wu.conceptfinder.stringanalysis.Language;
import ac.at.wu.conceptfinder.stringanalysis.LanguageDetector;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

public class ConceptFinder {
	
	public ConceptFinder(String databaseHost, String databaseUsername, String databasePassword, ConceptCreator cCreator, ConceptDetector cDetector) throws StorageException{
		m_conceptCreator = cCreator;
		m_conceptDetector = cDetector;
		m_database = new Database(databaseHost, databaseUsername, databasePassword, m_conceptCreator);
		m_datasetManager = new DatasetManager();
		m_datasetIdToConceptText = new HashMap<RdfId, ConceptText>();
	}
	
	public void loadDatasets(JsonLdFile file, String portal) throws StorageException{
		for(Dataset dataset : file.loadDatasets()){
			dataset.setPortal(portal);
			m_datasetManager.addDataset(dataset);
			ConceptText cText = new ConceptText(selectConceptText(dataset));
			m_datasetIdToConceptText.put(dataset.ID(), cText);
		}
	}
	
	public void clear(){
		m_datasetManager.clear();
	}
	
	public void loadDatasets(DatasetSearchMask mask) throws StorageException{
		System.out.println("Loading datasets from database...");
		Set<Dataset> loadedDatasets = m_database.getDatasets(mask);
		for(Dataset loadedSet : loadedDatasets){
			m_datasetManager.addDataset(loadedSet);
			ConceptText cText = new ConceptText(selectConceptText(loadedSet));
			m_datasetIdToConceptText.put(loadedSet.ID(), cText);
		}
		System.out.println("Done!");
	}
	
	public void createDatasetsInDatabase() throws StorageException{
			m_database.createDatasets(m_datasetManager.Datasets());			
	}

	public void discoverLanguages() throws LangDetectException {
		LanguageDetector langDetector = new LanguageDetector();
		Scanner reader = new Scanner(System.in);
		
		for(Dataset dataset : m_datasetManager){
			ConceptText currentConceptText = m_datasetIdToConceptText.get(dataset.ID());

			String langText = currentConceptText.Text();
			langText = langText.substring(langText.indexOf(Globals.KEYWORDS_MARKER) + Globals.KEYWORDS_MARKER.length());
			try{
				Language detectedLanguage = langDetector.identifyLanguage(langText);
				System.out.println("Language found for dataset: " + dataset.ID().value());
				System.out.println("Language: " + detectedLanguage);
				System.out.println("Based on: '" + langText + "'");
				dataset.setLanguage(detectedLanguage);
				currentConceptText.SetLanguage(detectedLanguage);
			}catch (LangDetectException e){
				boolean tryagain = true;

				while(tryagain){
					System.out.println("Could not find language. Please enter language for this text:");
					System.out.println(langText);
					System.out.println(dataset.ID().value());
					String input = reader.next();
					
					try{
						dataset.setLanguage(Language.valueOf(input.toUpperCase()));
						currentConceptText.SetLanguage(Language.valueOf(input.toUpperCase()));
						tryagain = false;
					}catch (Exception ex){
						System.out.println("Sorry this is no valid language!");
						tryagain = true;
					}
				}
				
			}
		}

	}
	
	//Sets the language for all active datasets
	public void setAllLanguages(Language lang){
		for(Dataset dataset : m_datasetManager){
			ConceptText conceptText = m_datasetIdToConceptText.get(dataset.ID());
			dataset.setLanguage(lang);
			conceptText.SetLanguage(lang);
		}
	}
		
	public void discoverConcepts() throws ConceptDetectionException{
		for(Dataset dataset : m_datasetManager){
			System.out.println("Discovering concepts for dataset: " + dataset.ID().value() + " lang: " + dataset.Language().toString());
			ConceptText currentText = m_datasetIdToConceptText.get(dataset.ID());
			currentText.SetLanguage(dataset.Language());
			m_conceptDetector.discoverConcepts(currentText, "1", Globals.KEYWORDS_MARKER);
			
			for(Concept concept : currentText.Concepts()){
				if(!concept.Mark().startsWith("1")) concept.setMark("0" + concept.Mark());
				dataset.addConcept(concept);
			}
		}
	}
	
	/*
	 * Calculates the total score for all concepts of all datasets
	 * will produce meaningless scores of all other scores have not been set before
	 * keywords mark should be set to improve quality
	 */
	public void calcTotalConceptScore() throws StorageException{
		System.out.println("Calculating total concept score...");
		//Go through all active datasets
		for(Dataset dataset : m_datasetManager){
			//Go through all concepts of each datasets
			for(Concept concept : dataset.Concepts()){
				//Compute a score based on disambigutation/relevance/coherence score and whether it is a keyword concept
				boolean keywordConcept = false;
				if(!concept.Mark().isEmpty())
					if(concept.Mark().startsWith("1"))
						keywordConcept = true;
				
				//Set this score as total score for the concept	
				//(This function sets the total score field of the concept)
				computeTotalScore(concept.Scores(), keywordConcept);
			}	
		}
		System.out.println("Done!");
	}
	
	/*
	 * Returns a set of all relevant concepts of all active datasets 
	 */
	public Set<Concept> selectConcepts() throws StorageException{
		//Keep a list of relevant concepts of active datasets
		HashSet<Concept> relevantConcepts = new HashSet<Concept>(10000);
		//Go through all active datasets and add relevant concepts to the list
		for(Dataset dataset : m_datasetManager)
			relevantConcepts.addAll(selectConcepts(dataset));
		
		return relevantConcepts;
	}
	
	/*
	 * Picks the relevant concepts out of all the concepts in a dataset
	 * assumes concepts of keywords are marked
	 */
	private Set<Concept> selectConcepts(Dataset dataset){
		//Copy the list of concepts from the given dataset as potential relevant concepts
		HashSet<Concept> relevantConcepts = new HashSet<Concept>(dataset.Concepts());
		//Keep a list of concepts that will be removed
		HashSet<Concept> remove = new HashSet<Concept>();
		//Go through all concepts of the dataset
		for(Concept current : relevantConcepts){
			//If the concept is marked as key concept keept it as relevant
			if(current.Mark().startsWith("1")) continue;
			//Discard concepts which have a combined relevance and coherence score of less than 0.3
			if(current.Scores().RelevanceScore() + current.Scores().CoherenceScore() < 0.3)
				remove.add(current);
		}
		//Remove the unrelevant concepts
		relevantConcepts.removeAll(remove);
		//Return the set of relevant concepts
		return relevantConcepts;
	}
	
	/*
	 * Saves/updates active datasets in the database
	 * @param flag if the concepts of the datasets need to be saved/updated
	 */
	public void saveDatasets(boolean saveConcepts) throws StorageException{
		System.out.println("Updating datasets in database");
		m_database.saveDatasets(m_datasetManager.Datasets(), saveConcepts);
		System.out.println("Done!");
	}
	
	/*
	 * Checks whether a concept is allready in a list of concepts and adds it if it is not
	 */
	private void addNewConcept(Set<Concept> concepts, Concept concept){
		for(Concept c : concepts)
			if(c.ID().equals(concept)) return;
		
		concepts.add(concept);
	}
	
	/*
	 * Sort a list of concepts by total score in descending order
	 */
	private void SortByTotalScore(List<Concept> concepts){
		
		//Sort by using a comparator for concepts using to total score for comparison
		Collections.sort(concepts, new Comparator<Concept>() {
			public int compare(Concept o1, Concept o2) {
				float score1 = o1.Scores().TotalScore();
				float score2 = o2.Scores().TotalScore();
				if(score1 == Float.NaN || score2 == Float.NaN) return 1;
				if(score1 > score2) return -1;
				if(score1 < score2) return 1;
				return 0;
			}
		});
	}
	
	public void showDatasetsWithConcepts(){
		int counter = 0;
		for(Dataset ds : m_datasetManager){
			counter++;
			if(counter > 20){
				System.out.println("Press any key for the next 20 datasets");
				try {
					System.in.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				counter = 0;
			}
			System.out.println("-------------------------------------------------------");
			System.out.println("Dataset: " + ds.ID().value());
			System.out.println("Title: " + ds.Title());
			System.out.println("Description: " + ds.Description());
			System.out.println("Keywords: " + concatWords(ds.Keywords()));
			System.out.println("Categories: ");
			for(BabelDomain cat : ds.Categories().keySet())
				System.out.print(cat + " ");
			System.out.println("Concepts: ");
			for(Concept c : ds.Concepts()){
				System.out.println("Name: " + c.Name() + 
						" Scores: " + c.Scores().TotalScore() + " "
						+ c.Scores().DisambiguationScore() + " "
						+ c.Scores().RelevanceScore() + " "
						+ c.Scores().CoherenceScore() 
						+ " (total/disam/rele/coher)");
				System.out.println("Mark: " + c.Mark());
				System.out.print("Concept category: ");
				System.out.print(c.Category() + ":" + c.CatConfidence() + " ");
				System.out.print("\n");
			}
		}
	}
	
	/*
	 * Computes and sets the total score field for a concept based on the 3 scores plus whether it is a keyword concept or not
	 */
	private void computeTotalScore(ConceptScores scores, boolean keywordConcept){
		float totalScore = 2.0f;
		//If all scores are 0 then the concept was found by most common sense heuristic
		if(scores.CoherenceScore() != 0.0f && 
				scores.DisambiguationScore() != 0.0f &&
				scores.RelevanceScore() != 0.0f)			
			totalScore = scores.CoherenceScore() * 0.27f 
				+ scores.DisambiguationScore() * 0.1f 
				+ scores.RelevanceScore() * 0.33f
				+ ((keywordConcept) ? 0.3f : 0 );
		
		scores.setTotalScore(totalScore);
	}
	
		
	
	/*
	 * selects the string that is used for language and concept discovery for each dataset
	 */
	private String selectConceptText(Dataset dataset){
		String cText = concatWords(dataset.Keywords()) +
				Globals.KEYWORDS_MARKER + dataset.Title() + " " + dataset.Description();
				
		return cText;
	}
	
	private String concatWords(Set<String> words){
		String ret = "";
		if(words.size() == 0) return ret;
		
		for(String word : words)
			ret += word + " ";
		
		return ret.substring(0, ret.length() - 1);
	}
	
	private Database m_database;
	private DatasetManager m_datasetManager;
	private ConceptCreator m_conceptCreator;
	private ConceptDetector m_conceptDetector;
	private HashMap<RdfId, ConceptText> m_datasetIdToConceptText;
}
