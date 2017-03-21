package ac.at.wu.conceptfinder.stringanalysis;

enum ConceptDetectionError{ cannotConnect, missingLanguage, allreadyExistingConcepts, invalidID, tooManyCalls };

public class ConceptDetectionException extends Exception {
		
	public ConceptDetectionException(String message, ConceptDetectionError error) {
		super(message);
		m_errorCode = error;
	}
	
	public ConceptDetectionError getErrorCode(){
		return m_errorCode;
	}
	
	private ConceptDetectionError m_errorCode;

}
