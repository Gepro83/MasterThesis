package ac.at.wu.conceptfinder.stringanalysis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.uniroma1.lcl.babelnet.data.BabelDomain;

public interface Concept extends Serializable{
	public ConceptID ID();
	public String Name();
	public String Mark();
	public void setMark(String mark);
	public void setScores(ConceptScores scores);
	public ConceptScores Scores();
	//The category of the concept. Is NULL if the concept was just discovered.
	public BabelDomain Category();
	public float CatConfidence();
}
