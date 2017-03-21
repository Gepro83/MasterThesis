package ac.at.wu.conceptfinder.stringanalysis;

/*
 * Factory for concepts
 */
public abstract class ConceptCreator {
	public abstract Concept createConcept(ConceptID ID, 
			String name, 
			ConceptScores scores,
			String mark) 
					throws InvalidConceptIDException;
}
