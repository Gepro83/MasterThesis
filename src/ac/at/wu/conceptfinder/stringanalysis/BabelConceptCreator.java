package ac.at.wu.conceptfinder.stringanalysis;

import java.io.IOException;

import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;

public class BabelConceptCreator extends ConceptCreator {

	@Override
	public Concept createConcept(ConceptID ID, 
			String name, 
			ConceptScores scores,
			String mark) 
					throws InvalidConceptIDException {
		
		BabelConcept concept;
		try {
			concept = new BabelConcept(ID, name);
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
