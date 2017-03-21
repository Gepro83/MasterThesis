package ac.at.wu.conceptfinder.stringanalysis;

import java.io.Serializable;

public interface ConceptDetector extends Serializable {
	
	/*
	 * fills empty concepts table of a given ConceptText with matching concepts
	 * also fills the scores table of the ConceptText
	 * sets the mark of concepts that are found before a delimiter symbol
	 * works only on empty concept tables
	 * @param text		concepts will be marked with this mark if they are discovered 
	 * 					before the delimiter. can be null or empty if no marking
	 * 					is necessary
	 * @param delimiter	a delimiter symbol. If it does not appear in the text all concepts
	 * 					will be marked. Can be null or empty if no marking is necessary  
	 */
	public void discoverConcepts(ConceptText text, String mark, String delimiter) throws ConceptDetectionException;

}
