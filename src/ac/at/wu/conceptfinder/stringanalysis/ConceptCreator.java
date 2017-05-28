package ac.at.wu.conceptfinder.stringanalysis;


import it.uniroma1.lcl.babelnet.data.BabelDomain;

/*
 * Factory for concepts
 */
public interface ConceptCreator {
	public Concept createConcept(ConceptId ID, 
			String name, 
			ConceptScores scores,
			String mark,
			BabelDomain category,
			float catConf) 
					throws InvalidConceptIDException;
}
