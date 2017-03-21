package ac.at.wu.conceptfinder.stringanalysis;

/*
 * Represents all scores a concept can have
 */
public class ConceptScores {

	public ConceptScores(float disambiguation, float relevance, float coherence, float total){
		m_coherenceScore = coherence;
		m_disambiguationScore = disambiguation;
		m_relevanceScore = relevance;
		m_totalScore = total;
	}
	
	public ConceptScores(){
		m_coherenceScore = -1;
		m_disambiguationScore = -1;
		m_relevanceScore = -1;
		m_totalScore = -1;
	}
	
	public void setDisambiguationScore(float score) { m_disambiguationScore = score; }		
	public void setRelevanceScore(float score) { m_relevanceScore = score; }
	public void setCoherenceScore(float score) { m_coherenceScore = score; }
	public void setTotalScore(float score) { m_totalScore = score; } 
	
	public float DisambiguationScore() { return m_disambiguationScore; }
	public float RelevanceScore() {	return m_relevanceScore; }
	public float CoherenceScore() {	return m_coherenceScore; }
	public float TotalScore() { return m_totalScore; }
	
	private float m_coherenceScore;
	private float m_disambiguationScore;
	private float m_relevanceScore;
	private float m_totalScore;
}
