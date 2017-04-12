package ac.at.wu.conceptfinder.stringanalysis;


import it.uniroma1.lcl.babelnet.data.BabelDomain;

/*
 * Factory for concepts
 */
public abstract class ConceptCreator {
	public abstract Concept createConcept(ConceptID ID, 
			String name, 
			ConceptScores scores,
			String mark,
			BabelDomain category,
			float catConf) 
					throws InvalidConceptIDException;
}
