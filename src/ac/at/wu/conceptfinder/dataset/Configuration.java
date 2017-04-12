package ac.at.wu.conceptfinder.dataset;

/*
 * Holds all configuration parameters for a categorization of datasets  
 */
public class Configuration{
	
	public Configuration(){
		//set default values for parameters
		m_w_keywords = 3.0f;
		m_w_relevancescore = 0.5f;
		m_w_coherencescore = 0.5f;
		m_w_repeatconcept = 0.5f;
		m_w_catconf = 0.5f;
		m_MCSscore = 0.5f;
		m_numOutputCats = 3;
	}
	
	public float getKeywordsWeight(){ return m_w_keywords; }
	public float getRelevanceWeight(){ return m_w_relevancescore; }
	public float getCoherenceWeight(){ return m_w_coherencescore; }
	public float getRepeatedConceptWeight(){ return m_w_repeatconcept; }
	public float getCategoryConfidenceWeight(){ return m_w_catconf; }
	public float getMCSScore(){ return m_MCSscore; }
	public int getNumOutputCategories(){ return m_numOutputCats; }
	
	public void setKeywordsWeight(float weight){ if(weight >= 0) m_w_keywords = weight; }
	public void setRepeatedConceptWeight(float weight){ if(weight >= 0) m_w_repeatconcept = weight; }
	public void setCategoryConfidenceWeight(float weight){ if(weight >= 0) m_w_catconf = weight; }
	public void setMCSScore(float score){ if(score >= 0 && score <= 1) m_MCSscore = score; }
	public void setNumOutputCategories(int number) { if(number > 0) m_numOutputCats = number; }
	
	/*
	 * Sets the weight of the relevance score for this categorizer
	 * also sets the weight of the coherence score accordingly
	 * since the sum of these scores must be 1.0 .
	 */
	public void setRelevanceWeight(float weight){
		if(weight >= 0 && weight <= 1){
			m_w_relevancescore = weight; 
			m_w_coherencescore = 1 - weight;
		}
	}
	
	/*
	 * Sets the weight of the relevance score for this categorizer
	 * also sets the weight of the coherence score accordingly
	 * since the sum of these scores must be 1.0 .
	 */
	public void setCoherenceWeight(float weight){
		if(weight >= 0 && weight <= 1){
			m_w_coherencescore = weight;
			m_w_relevancescore = 1 - weight;
		}
	}
	
	/*
	 * the weights of the parameters
	 */
	private float m_w_keywords;
	private float m_w_relevancescore;
	private float m_w_coherencescore;
	private float m_w_repeatconcept;
	private float m_w_catconf;
	
	//score of the MCS heuristic
	private float m_MCSscore;
	
	//number of maximum categories per dataset 
	private int m_numOutputCats;

}
