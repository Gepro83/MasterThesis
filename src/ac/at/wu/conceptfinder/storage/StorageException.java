package ac.at.wu.conceptfinder.storage;

enum StorageError { allreadyExists, cannotConnect, SQLError, jsonError }

public class StorageException extends Exception {
	
	public StorageException(String message, StorageError error) {
		super(message);
		m_errorCode = error;
	}
	
	public StorageError getErrorCode(){
		return m_errorCode;
	}
	
	private StorageError m_errorCode;
}

