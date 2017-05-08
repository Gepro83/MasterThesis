package ac.at.wu.conceptfinder.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.DatasetSearchMask;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

/*
 * This class manages a list of datasets which can be loaded from the database.
 * Furthermore, it is responsible for the categorization algorithm for datasets.
 * The algorithm is based on the categories of the concepts (synsets) that were
 * discovered in the dataset.  
 * Parameters of the algorithm are stored in a Configuration object.
 * The class also provides some statistics about the managed datasets and categories.
 */
public class Categorizer {
	
	/*
	 * A class that encapsules the features a (type of) concept can have
	 */
	public class ConceptFeatures{
		private String m_Name;
		private float m_Frequency;
		private BabelDomain m_Category;
		private float m_CatConf;
		private float m_AvgRelScore;
		private float m_AvgCohScore;
		private int m_NonMCSCount;
		private float m_Weight;
		
		ConceptFeatures(){
			m_Name = "";
			m_Frequency = 0;
			m_Category = null;
			m_CatConf = 0;
			m_AvgRelScore = 0;
			m_AvgCohScore = 0;
			m_NonMCSCount = 0;
			m_Weight = 0;
		}
		
		public String Name() { return m_Name; }
		public float Frequency() { return m_Frequency; }
		public BabelDomain Category() { return m_Category; }
		public float CatConf() { return m_CatConf; }
		public float AvgRelScore() { return m_AvgRelScore; }
		public float AvgCohScore() { return m_AvgCohScore; }
		public int NonMCSCount() { return m_NonMCSCount; }
		public float Weight() { return m_Weight; }
		
		public void setName(String n) { m_Name = n; }
		public void setFrequency(float f){ 
			if(f >= 0.0f && f <= 1.0f){
				m_Frequency = f; 
			}
			else if (f > 1.0f){
				m_Frequency = 1.0f;
			}
			else if (f < 0.0f){
				m_Frequency = 0.0f;
			}
		}
		public void setCategory(BabelDomain c){ m_Category = c; }
		public void setCatConf(float c){ if(c >= 0.0f && c <= 1.0f) m_CatConf = c; }
		public void setAvgRelScore(float s){ m_AvgRelScore = s; }
		public void setAvgCohScore(float s){ m_AvgCohScore = s; }
		public void setNonMCSCount(int c) { m_NonMCSCount = c; }
		public void setWeight(float w){ if(w >= 0.0f && w <= 1.0f) m_Weight = w; }
	}

	public Categorizer(Database database){
		m_Database = database;
		m_Configuration = new Configuration();
		m_Datasets = new DatasetManager();
		m_CategoryToFrequency = new EnumMap<BabelDomain, Float>(BabelDomain.class);
		m_ConceptIDsToFeatures = new HashMap<ConceptID, ConceptFeatures>();
	}
	
	public Categorizer(Database database, Configuration conf){
		m_Database = database;
		m_Configuration = conf;
		m_Datasets = new DatasetManager();
		m_CategoryToFrequency = new EnumMap<BabelDomain, Float>(BabelDomain.class);
		m_ConceptIDsToFeatures = new HashMap<ConceptID, ConceptFeatures>();
	}
	
	/*
	 * The set of currently active datasets
	 */
	public Set<Dataset> Datasets(){ return Collections.unmodifiableSet(m_Datasets.Datasets()); }
	//The current Configuration of this Categorizer 
	public Configuration Configuration(){ return m_Configuration; }
	public Map<ConceptID, ConceptFeatures> ConceptIDsToFeatures(){ return Collections.unmodifiableMap(m_ConceptIDsToFeatures); }
	/*
	 * This is a map that maps all occurring categories of the active datasets to their corresponding frequencies.
	 * The frequency of a category corresponds to the probability of a single dataset to belong to this category.
	 */
	public Map<BabelDomain, Float> CategoriesToFrequency(){ return Collections.unmodifiableMap(m_CategoryToFrequency); }
	
	/*
	 * Adds all datasets of the portal to this Categorizer.
	 * Datasets that were already loaded (with identical IDs) will be ignored
	 */
	public void loadPortal(String portal) throws StorageException{
		m_Datasets.addDatasets(m_Database.getDatasets(new DatasetSearchMask(null, new String[]{portal})));
		calcConceptIDsToFeatures();
	}
	
	/*
	 * Removes all datasets belonging to the given portal from this Categorizer
	 * returns the set of removed datasets
	 */
	public Set<Dataset> unloadPortal(String portal){
		HashSet<Dataset> remove = new HashSet<Dataset>();
		for(Dataset dataset : m_Datasets)
			if(dataset.Portal().equals(portal)) remove.add(dataset);
		for(Dataset dataset : remove)
			m_Datasets.removeDataset(dataset);
		updateCategoryFrequencies();
		calcConceptIDsToFeatures();
		return remove;
	}
	
	/*
	 * Runs the categorization algorithm on all active datasets.
	 * Overwrites the previous categories of the datasets.
	 * Updates the frequencies of categories. 
	 */
	public void categorize(){
		//Discover the categories with the current settings and attach them to the datasets
		for(Dataset dataset : m_Datasets){
			dataset.clearCategories();
			EnumMap<BabelDomain, Float> newCats = selectCategories(dataset);
			for(BabelDomain cat : newCats.keySet())
				dataset.addCategory(cat, newCats.get(cat));
		}
		updateCategoryFrequencies();
	}
	

	
	/*
	 * Calculates the average number of concepts per dataset in this object
	 */
	public float AverageConceptCount(){
		int totalConcepts = 0;
		for(Dataset dataset : m_Datasets)
			totalConcepts += dataset.Concepts().size();
		return (float) totalConcepts / m_Datasets.DatasetCount();
	}
	
	/*
	 * Calculates the number of distinct concepts (distinct conceptIDs to be exact) in the datasets
	 */
	public int DistinctConceptsCount(){
		HashSet<ConceptID> occurringConcepts = new HashSet<ConceptID>();
		for(Dataset dataset : m_Datasets)
			for(Concept concept : dataset.Concepts())
				occurringConcepts.add(concept.ID());
		return occurringConcepts.size();
	}
	
	/*
	 * Calculates the map that contains all distinct concepts (IDs) that occur in the currently active datasets
	 * together with their features.
	 * (Re)initializes the weights of all concepts to 1.
	 */
	private void calcConceptIDsToFeatures(){
		m_ConceptIDsToFeatures.clear();
		//Go through all active datasets
		for(Dataset dataset : m_Datasets){
			//Keep track of concepts that have already occurred in the dataset
			HashSet<ConceptID> occurredConcepts = new HashSet<ConceptID>();
			//For every concept of each datasets
			for(Concept concept : dataset.Concepts()){
				//If the concept id is not yet in the map
				if(!m_ConceptIDsToFeatures.containsKey(concept.ID())){
					//Set the appropriate concept features 
					ConceptFeatures features = new ConceptFeatures();
					features.setName(concept.Name());
					features.setFrequency(1.0f / m_Datasets.DatasetCount());
					//Only count and compute average score for the concepts that were not found by the MCS heuristic
					if(concept.Scores().CoherenceScore() != 0 || concept.Scores().RelevanceScore() != 0){
						features.setAvgCohScore(concept.Scores().CoherenceScore());
						features.setAvgRelScore(concept.Scores().RelevanceScore());
						features.setNonMCSCount(1);
					}
					features.setCategory(concept.Category());
					features.setCatConf(concept.CatConfidence());
					//All concepts are initially weighted evenly
					features.setWeight(1);
					//add it to the map
					m_ConceptIDsToFeatures.put(concept.ID(), features);
					//This concept has now occurred in this dataset
					occurredConcepts.add(concept.ID());
				}
				//If the concept already exists in the map
				else{
					//Adjust the frequency, count and average scores
					ConceptFeatures features = m_ConceptIDsToFeatures.get(concept.ID());
					//The frequency is only increased the first time a concept occurred within a dataset		
					if(!occurredConcepts.contains(concept.ID())){
						features.setFrequency(features.Frequency() + 1.0f / m_Datasets.DatasetCount());
						occurredConcepts.add(concept.ID());
					}
					//Do not take concepts into the score average if they were found by the MCS heuristic (indicated by a score of 0)
					if(concept.Scores().CoherenceScore() != 0 || concept.Scores().RelevanceScore() != 0){
						features.setNonMCSCount(features.NonMCSCount() + 1);
						features.setAvgCohScore(
							(features.AvgCohScore() * (features.NonMCSCount()-1)  + concept.Scores().CoherenceScore()) 
							/ (float) features.NonMCSCount()
							);
						features.setAvgRelScore(
							(features.AvgRelScore() * (features.NonMCSCount()-1)  + concept.Scores().RelevanceScore()) 
							/ (float) features.NonMCSCount()
							);
					}
				}
			}
		}
	}
	
	
	/*
	 * Derives the categories of a dataset based on the categories and scores of its concepts
	 * Assumes that concepts have been scored, marked as a keyword concept and
	 * the map of concept ids to features has been filled.
	 * 
	 * @return a map with the derived categories and a confidence score
	 */
  	private EnumMap<BabelDomain, Float> selectCategories(Dataset dataset){
		//Keep a map of potential top categories and their scores
		EnumMap<BabelDomain, Float> potentialTopCategories = new EnumMap<BabelDomain, Float>(BabelDomain.class);
		//Keep track of how many concepts contributed to the score of each category
		EnumMap<BabelDomain, Integer> numContributeConcepts = new EnumMap<BabelDomain, Integer>(BabelDomain.class);
		//First go through keyword concepts
		//Keep a list of conceptIDs that appeared in the dataset
		HashSet<ConceptID> appearedConcepts = new HashSet<ConceptID>();
		for(Concept concept : dataset.Concepts()){
			//Work with the concept categories stored in the ConceptFeatures object, meaning they can be altered by the user
			BabelDomain conceptCategory = m_ConceptIDsToFeatures.get(concept.ID()).Category();
			//Concepts marked with a 1 are keyword concepts
			if(!concept.Mark().startsWith("1")) continue;
			//Ignore concepts with no category
			if(conceptCategory == null) continue;
			//If all scores are 0 the concept was found by the most common sense heuristic
			boolean MCS = (concept.Scores().DisambiguationScore() == 0 &&
						concept.Scores().RelevanceScore() == 0 &&
						concept.Scores().CoherenceScore() == 0) ? true : false;
			//If the category does exist in the map of potential top categories
			if(potentialTopCategories.containsKey(conceptCategory)){
				//Calculate the base score of the category
				float score = calcCatScore(concept, MCS, true);
				//If this concept has appeared in this dataset already weight it with the respective parameter
				if(appearedConcepts.contains(concept.ID()))
					score *= m_Configuration.getRepeatedConceptWeight();
				//Increase the current score of this category by the calculated base score 
				score += potentialTopCategories.get(conceptCategory); 
				//Replace the old score with the new score 
				potentialTopCategories.put(conceptCategory, score);
				//Increase the counter for contributing concepts to the category
				int i = numContributeConcepts.get(conceptCategory);
				numContributeConcepts.put(conceptCategory, ++i);
			}
			//If the category does not exist in the map of potential top categories
			else{
				//Calculate the score of the category
				float score = calcCatScore(concept, MCS, true);
				//Add the category to map of potential top categories
				potentialTopCategories.put(conceptCategory, score);
				//Start counting the contributing concepts for the category
				numContributeConcepts.put(conceptCategory, 1);
			}
			//Add the conceptID to the list of IDs that appeared in this dataset
			appearedConcepts.add(concept.ID());
		}
		//Now go through non-keyword concepts
		for(Concept concept : dataset.Concepts()){
			//Work with the concept categories stored in the ConceptFeatures object, meaning they can be altered by the user
			BabelDomain conceptCategory = m_ConceptIDsToFeatures.get(concept.ID()).Category();
			//Concepts marked with a 1 are keyword concepts
			if(concept.Mark().startsWith("1")) continue;
			//Ignore concepts with no category
			if(conceptCategory == null) continue;
			//If all scores are 0 the concept was found by the most common sense heuristic
			boolean MCS = (concept.Scores().DisambiguationScore() == 0 &&
						concept.Scores().RelevanceScore() == 0 &&
						concept.Scores().CoherenceScore() == 0) ? true : false;
			//If the category does exist in the map of potential top categories
			if(potentialTopCategories.containsKey(conceptCategory)){
				//Calculate the base score of the category
				float score = calcCatScore(concept, MCS, true);
				//If this concept has appeared in this dataset already weight it with the respective parameter
				if(appearedConcepts.contains(concept.ID()))
					score *= m_Configuration.getRepeatedConceptWeight();
				
				//Increase the current score of this category by the calculated base score 
				score += potentialTopCategories.get(conceptCategory); 
				//Replace the old score with the new score 
				potentialTopCategories.put(conceptCategory, score);
				//Increase the counter for contributing concepts to the category
				int i = numContributeConcepts.get(conceptCategory);
				numContributeConcepts.put(conceptCategory, ++i);
			}
			//If the category does not exist in the map of potential top categories
			else{
				//Calculate the score of the category
				float score = calcCatScore(concept, MCS, true);
				//Add the category to map of potential top categories
				potentialTopCategories.put(conceptCategory, score);
				//Start counting the contributing concepts for the category
				numContributeConcepts.put(conceptCategory, 1);
			}
			//Add the conceptID to the list of IDs that appeared in this dataset
			appearedConcepts.add(concept.ID());
		}
		//If no categories were found return the empty set
		if(potentialTopCategories.isEmpty()) return potentialTopCategories;
		//Normalize the scores of the categories
		for(Map.Entry<BabelDomain, Float> catWithScore : potentialTopCategories.entrySet()){
			float score = catWithScore.getValue();
			score /= numContributeConcepts.get(catWithScore.getKey());
			catWithScore.setValue(score);
		}
		//Create a list of all the potential top categories 
		List<BabelDomain> potentialTopCatsList = new ArrayList<BabelDomain>(potentialTopCategories.keySet());
		//Sort the list by their scores in the map of potential top categories
		Collections.sort(potentialTopCatsList, new Comparator<BabelDomain>() {
			public int compare(BabelDomain o1, BabelDomain o2) {
				if(potentialTopCategories.get(o1) > potentialTopCategories.get(o2)) return -1;
				if(potentialTopCategories.get(o1) < potentialTopCategories.get(o2)) return 1;
				return 0;
			}
		});
		//Keep as many categories as specified in the parameters of this object as a resulting map
		//lowest scored categories are discarded first
		int counter = 0;
		for(BabelDomain category : potentialTopCatsList){
			counter++;
			if(counter > m_Configuration.getMaxOutputCategories())
				potentialTopCategories.remove(category);
		}
		//Discard categories with a score below the threshold unless the minimum number of categories is reached
		counter = 0;
		for(BabelDomain category : potentialTopCatsList){
			if(!potentialTopCategories.containsKey(category)) continue;
			counter++;
			float score = potentialTopCategories.get(category);
			if(score < m_Configuration.getMinScore())
				if(counter > m_Configuration.getMinOutputCategories()) 
					potentialTopCategories.remove(category);
		}
		//Return the resulting map
		return potentialTopCategories;
	}
	
	/*
	 * Calculates the score of the category of a concept
	 */
	private float calcCatScore(Concept concept, boolean MCS, boolean keywordConcept){
		//If the concept was found by the most common sense heuristic
		//give it the score defined by this objects parameter (must be in interval [0,1])
		float score = m_Configuration.getMCSScore();
		//If it was found by babelfy
			//give it a weighted score between relevance and coherence score
			//weights are defined as parameters of this object (sum of the weights is ensured to be 1.0)
		if(!MCS)
			score = concept.Scores().RelevanceScore() * m_Configuration.getRelevanceWeight() + concept.Scores().CoherenceScore() * m_Configuration.getCoherenceWeight();
		//If it is a keyword Concept weight it as defined in the parameters
		if(keywordConcept)
			score *= m_Configuration.getKeywordsWeight();
		//The score gets weighted by the confidence of the concept belonging to the category
		//also this weight is defined in the parameters
		score = score * ( 1 + m_Configuration.getCategoryConfidenceWeight() * m_ConceptIDsToFeatures.get(concept.ID()).CatConf());
		//Normalize the score
		score /= m_Configuration.getKeywordsWeight() * (1 + m_Configuration.getCategoryConfidenceWeight());
		//Finally, the score gets weighted by the weight of this concept type, which is determined by the concept id. It must be between 0 and 1
		score *= m_ConceptIDsToFeatures.get(concept.ID()).Weight();
		//Return the score
		return score;
	}
	
	/*
	 * Updates the frequencies of categories
	 */
	private void updateCategoryFrequencies(){
		//Count all occurring categories for all datasets
		Map<BabelDomain, Integer> CategoryToCount = new EnumMap<BabelDomain, Integer>(BabelDomain.class);
		for(Dataset dataset : m_Datasets){
			for(BabelDomain dsCategory : dataset.Categories().keySet()){
				if(!CategoryToCount.containsKey(dsCategory)){
					CategoryToCount.put(dsCategory, 1);
				}else{
					int currentCount = CategoryToCount.get(dsCategory);
					currentCount++;
					CategoryToCount.replace(dsCategory, currentCount);
				}
			}
		}
		//The frequency of a category corresponds to the probability of a single dataset to belong to this category.
		//Calculate the frequencies of each category by dividing the count by the total number of datasets
		int datasetsCount = m_Datasets.DatasetCount();
		//Frequencies are computed from scratch after every call of categorize()
		m_CategoryToFrequency.clear();
		for(BabelDomain category : CategoryToCount.keySet())
			m_CategoryToFrequency.put(category, (float) CategoryToCount.get(category) / datasetsCount);
	}
	
	private Configuration m_Configuration;
	private final Database m_Database;
	private final DatasetManager m_Datasets;
	/*
	 * A map that stores all categories that result of the current categorization 
	 * and their frequencies within the datasets. This is updated after each call
	 * of the categorization method.
	 */
	private EnumMap<BabelDomain, Float> m_CategoryToFrequency;
	/*
	 * A map that contains all distinct concepts (IDs) that occur in the currently active datasets
	 * together with their features
	 */
	private Map<ConceptID, ConceptFeatures> m_ConceptIDsToFeatures;
}
