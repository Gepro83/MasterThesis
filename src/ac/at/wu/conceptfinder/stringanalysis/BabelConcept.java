package ac.at.wu.conceptfinder.stringanalysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

public class BabelConcept implements Concept {

	private static final long serialVersionUID = -8193421919124250631L;

	public BabelConcept(ConceptID ID, String name) throws IOException, InvalidBabelSynsetIDException {
		m_ConceptID = new ConceptID(ID.value());
		m_name = name;
		m_mark = "";
		m_scores = new ConceptScores();
		if(m_conceptToDomain == null)
			loadConceptToDomain();
		Object[] catAndConf = m_conceptToDomain.get(m_ConceptID);
		if(catAndConf == null){
			m_category = null;
			m_catConfidence = 0;
			return;
		}
		m_category = (BabelDomain) catAndConf[0];
		m_catConfidence = (float) catAndConf[1];
	}

	@Override
	public ConceptID ID() {
		return new ConceptID(m_ConceptID.value());
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
	
	private ConceptID m_ConceptID;
	private String m_name;
	private ConceptScores m_scores;
	private String m_mark;
	private BabelDomain m_category;
	private float m_catConfidence;
	private static HashMap<ConceptID, Object[]> m_conceptToDomain = null;
	
	//Loads the mapping of synsets to BabelDomains with confidence scores from the resource file
	private void loadConceptToDomain() throws IOException {
		System.out.println("Loading map for categories...");
		//Load the file where the domains are stored
		try (BufferedReader br = new BufferedReader(new FileReader("resources/babeldomains.txt"))) {
			//Initialise the map to hold 2,68 mio entries
			m_conceptToDomain = new HashMap<ConceptID, Object[]>(2680000, 1);
			//Go through the file line by line
			String line;
			while ((line = br.readLine()) != null) {
				//Each line consists of the synset ID the corresponding BabelDomain and a score for confidence
				//separated by a tab
				//Split the line at the tabs in the 3 parts mentioned above
				String[] parts = line.split("\t");
				//Create a ConceptID out of the first part
				ConceptID conceptID = new ConceptID(parts[0]);
				//Create a BabelDomain out of the second part 
				BabelDomain domain = BabelDomain.valueOfName(parts[1]);
				//Create a float out of the third part
				float confidence = Float.parseFloat(parts[2].startsWith("*") ? parts[2].substring(1) : parts[2]);
				//Add an entry to the mapping for concepts to BabelDomains
				m_conceptToDomain.put(conceptID, new Object[]{domain, confidence});
			}
		}
		System.out.println("Done!");
	}
}
