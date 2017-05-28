package ac.at.wu.conceptfinder.stringanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

public class BabelConcept implements Concept {

	private static final long serialVersionUID = -8193421919124250631L;

	public BabelConcept(ConceptId ID, String name, BabelDomain category, float catConf) throws IOException, InvalidBabelSynsetIDException {
		m_ConceptID = new ConceptId(ID.value());
		m_name = name;
		m_mark = "";
		m_scores = new ConceptScores();
		m_category = category;
		m_catConfidence = catConf;
	}

	@Override
	public ConceptId ID() {
		return new ConceptId(m_ConceptID.value());
	}

	@Override
	public String Name() {
		return m_name;
	}

	@Override
	public String Mark() {
		return m_mark;
	}

	@Override
	public void setMark(String mark) {
		m_mark = mark;
	}

	@Override
	public void setScores(ConceptScores scores) {
		m_scores = scores;
		
	}

	@Override
	public ConceptScores Scores() {
		return m_scores;
	}
	
	@Override
	public BabelDomain Category() {
		return m_category;
	}

	@Override
	public float CatConfidence() {
		return m_catConfidence;
	}
	
	private ConceptId m_ConceptID;
	private String m_name;
	private ConceptScores m_scores;
	private String m_mark;
	private BabelDomain m_category;
	private float m_catConfidence;
	private static HashMap<ConceptId, Object[]> m_conceptToDomain = null;
	

}
