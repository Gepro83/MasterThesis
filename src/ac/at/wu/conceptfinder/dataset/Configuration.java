package ac.at.wu.conceptfinder.dataset;

import java.io.Serializable;

/*
 * Holds all configuration parameters for a categorization of datasets  
 */
public class Configuration implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6452960684529199291L;

	public Configuration(){
		//set default values for parameters
		m_w_keywords = 3.0f;
		m_w_relevancescore = 0.5f;
		m_w_coherencescore = 0.5f;
		m_w_repeatconcept = 0.5f;
		m_w_catconf = 0.5f;
		m_MCSscore = 0.5f;
		m_MaxOutputCats = 3;
		m_MinOutputCats = 1;
		m_MinScore = 0.02f;
	}
	
	public float getKeywordsWeight(){ return m_w_keywords; }
	public float getRelevanceWeight(){ return m_w_relevancescore; }
	public float getCoherenceWeight(){ return m_w_coherencescore; }
	public float getRepeatedConceptWeight(){ return m_w_repeatconcept; }
	public float getCategoryConfidenceWeight(){ return m_w_catconf; }
	public float getMCSScore(){ return m_MCSscore; }
	public int getMaxOutputCategories(){ return m_MaxOutputCats; }
	public int getMinOutputCategories(){ return m_MinOutputCats; }
	public float getMinScore() { return m_MinScore; }
	
	public void setKeywordsWeight(float weight){ if(weight >= 0) m_w_keywords = weight; }
	public void setRepeatedConceptWeight(float weight){ if(weight >= 0) m_w_repeatconcept = weight; }
	public void setCategoryConfidenceWeight(float weight){ if(weight >= 0) m_w_catconf = weight; }
	public void setMCSScore(float score){ if(score >= 0 && score <= 1) m_MCSscore = score; }
	public void setMaxOutputCategories(int number) { if(number > 0 && number >= m_MinOutputCats) m_MaxOutputCats = number; }
	public void setMinOutputCategories(int number) { if(number >= 0 && number <= m_MaxOutputCats) m_MinOutputCats = number; }
	public void setMinScore(float score){ if(score >= 0 && score <= 1) m_MinScore = score; }
	
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
	
	//number of maximum/minimum categories per dataset 
	private int m_MaxOutputCats;
	private int m_MinOutputCats;
	
	//minimum score for category (m_MinOutputCats supersedes this)
	private float m_MinScore;

}
