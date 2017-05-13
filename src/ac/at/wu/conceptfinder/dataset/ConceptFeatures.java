package ac.at.wu.conceptfinder.dataset;

import java.io.Serializable;

import it.uniroma1.lcl.babelnet.data.BabelDomain;

/*
 * A class that encapsules the features a (type of) concept can have
 */
public class ConceptFeatures implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5490190097042640130L;

	
	public ConceptFeatures(){
		m_Name = "";
		m_Frequency = 0;
		m_Category = null;
		m_CatConf = 0;
		m_AvgRelScore = 0;
		m_AvgCohScore = 0;
		m_NonMCSCount = 0;
		m_Weight = 0;
		m_edited = false;
		
	}
	
	public String Name() { return m_Name; }
	public float Frequency() { return m_Frequency; }
	public BabelDomain Category() { return m_Category; }
	public float CatConf() { return m_CatConf; }
	public float AvgRelScore() { return m_AvgRelScore; }
	public float AvgCohScore() { return m_AvgCohScore; }
	public int NonMCSCount() { return m_NonMCSCount; }
	public float Weight() { return m_Weight; }
	public boolean getEdited(){ return m_edited; }
	
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
	public void setEdited(boolean b){ m_edited = b;}
		
	private String m_Name;
	private float m_Frequency;
	private BabelDomain m_Category;
	private float m_CatConf;
	private float m_AvgRelScore;
	private float m_AvgCohScore;
	private int m_NonMCSCount;
	private float m_Weight;
	private boolean m_edited;
}