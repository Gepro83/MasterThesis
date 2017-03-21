package ac.at.wu.conceptfinder.stringanalysis;

public class InvalidConceptIDException extends Exception {

	public InvalidConceptIDException() {
		super("ConceptID invalid");
	}

	public InvalidConceptIDException(String message) {
		super(message + " this ConceptID is invalid");
	}

}
