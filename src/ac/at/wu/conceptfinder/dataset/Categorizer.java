package ac.at.wu.conceptfinder.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
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

	public Categorizer(Database database){
		m_Database = database;
		m_Configuration = new Configuration();
		m_Datasets = new DatasetManager();
		m_CategoryToFrequency = new EnumMap<BabelDomain, Float>(BabelDomain.class);
	}
	
	public Categorizer(Database database, Configuration conf){
		m_Database = database;
		m_Configuration = conf;
		m_Datasets = new DatasetManager();
		m_CategoryToFrequency = new EnumMap<BabelDomain, Float>(BabelDomain.class);
	}
	
	/*
	 * The set of currently active datasets
	 */
	public Set<Dataset> Datasets(){ return Collections.unmodifiableSet(m_Datasets.Datasets()); }
	//The current Configuration of this Categorizer 
	public Configuration Configuration(){ return m_Configuration; }
	
	/*
	 * Adds all datasets of the portal to this Categorizer.
	 * Datasets that were already loaded (with identical IDs) will be ignored
	 */
	public void loadPortal(String portal) throws StorageException{
		m_Datasets.addDatasets(m_Database.getDatasets(new DatasetSearchMask(null, new String[]{portal})));
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
		updateFrequencies();
		System.out.println(m_Datasets.Datasets().size());
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
		updateFrequencies();
	}
	
	/*
	 * This is a map that maps all occurring categories of the active datasets to their corresponding frequencies.
	 * The frequency of a category corresponds to the probability of a single dataset to belong to this category.
	 */
	public Map<BabelDomain, Float> CategoriesToFrequency(){ return Collections.unmodifiableMap(m_CategoryToFrequency); }
	
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
	 * Calculates the number of distinct concepts (distinct conceptIDs to be exact) in the datasetts
	 */
	public int DistinctConceptsCount(){
		HashSet<ConceptID> occurringConcepts = new HashSet<ConceptID>();
		for(Dataset dataset : m_Datasets)
			for(Concept concept : dataset.Concepts())
				occurringConcepts.add(concept.ID());
		return occurringConcepts.size();
	}
	
	
	/*
	 * Derives the categories of a dataset based on the categories and scores of its concepts
	 * assumes that concepts have been scored and marked as a keyword concept
	 * 
	 * @return a map with the derived categories and a confidence score
	 */
  	private EnumMap<BabelDomain, Float> selectCategories(Dataset dataset){
		//Keep a map of potential top categories and their scores
		EnumMap<BabelDomain, Float> potentialTopCategories = new EnumMap<BabelDomain, Float>(BabelDomain.class);
		//First go through keyword concepts
		for(Concept concept : dataset.Concepts()){
			//Concepts marked with a 1 are keyword concepts
			if(!concept.Mark().startsWith("1")) continue;
			//Ignore concepts with no category
			if(concept.Category() == null) continue;
			//If all scores are 0 the concept was found by the most common sense heuristic
			boolean MCS = (concept.Scores().DisambiguationScore() == 0 &&
						concept.Scores().RelevanceScore() == 0 &&
						concept.Scores().CoherenceScore() == 0) ? true : false;
			//If the category does exist in the map of potential top categories
			if(potentialTopCategories.containsKey(concept.Category())){
				//Calculate the base score of the category
				float score = calcCatScore(concept, MCS, true);
				//Increase the current score of this category by the weighted calculated base score 
				score = potentialTopCategories.get(concept.Category()) + score * m_Configuration.getRepeatedConceptWeight();
				//Replace the old score with the new score 
				potentialTopCategories.put(concept.Category(), score);
			}
			//If the category does not exist in the map of potential top categories
			else{
				//Calculate the score of the category
				float score = calcCatScore(concept, MCS, true);
				//Add the category to map of potential top categories
				potentialTopCategories.put(concept.Category(), score);
			}
		}
		//Now go through non-keyword concepts
		for(Concept concept : dataset.Concepts()){
			//Concepts marked with a 1 are keyword concepts
			if(concept.Mark().startsWith("1")) continue;
			//Ignore concepts with no category
			if(concept.Category() == null) continue;
			//If all scores are 0 the concept was found by the most common sense heuristic
			boolean MCS = (concept.Scores().DisambiguationScore() == 0 &&
						concept.Scores().RelevanceScore() == 0 &&
						concept.Scores().CoherenceScore() == 0) ? true : false;
			//If the category does exist in the map of potential top categories
			if(potentialTopCategories.containsKey(concept.Category())){
				//Calculate the base score of the category
				float score = calcCatScore(concept, MCS, false);
				//Increase the current score of this category by the weighted calculated base score 
				score = potentialTopCategories.get(concept.Category()) + score * m_Configuration.getRepeatedConceptWeight();
				//Replace the old score with the new score 
				potentialTopCategories.put(concept.Category(), score);
			}
			//If the category does not exist in the map of potential top categories
			else{
				//Calculate the score of the category
				float score = calcCatScore(concept, MCS, false);
				//Add the category to map of potential top categories
				potentialTopCategories.put(concept.Category(), score);
			}
		}
		//If no categories were found return the empty set
		if(potentialTopCategories.isEmpty()) return potentialTopCategories;
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
			if(counter > m_Configuration.getNumOutputCategories())
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
		//Finally, the score gets weighted by the confidence of the concept belonging to the category
		//also this weight is defined in the parameters
		score = score * ( 1 + m_Configuration.getCategoryConfidenceWeight() * concept.CatConfidence());
		//Return the score
		return score;
	}
	
	/*
	 * Updates the frequencies of categories
	 */
	private void updateFrequencies(){
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
}
