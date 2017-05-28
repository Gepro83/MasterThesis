package ac.at.wu.conceptfinder.stringanalysis;

import java.io.IOException;
import java.util.HashMap;

import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

public class BabelConceptCreator implements ConceptCreator {

	@Override
	public Concept createConcept(ConceptId ID, 
			String name, 
			ConceptScores scores,
			String mark,
			BabelDomain category,
			float catConf) 
					throws InvalidConceptIDException {
		
		BabelConcept concept;
		try {
			concept = new BabelConcept(ID, name, category, catConf);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InvalidConceptIDException();
		} catch (InvalidBabelSynsetIDException e) {
			e.printStackTrace();
			throw new InvalidConceptIDException();
		}

		concept.setScores(scores);
		concept.setMark(mark);
			
		return concept;
		
	}
}
