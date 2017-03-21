package ac.at.wu.conceptfinder.dataset;

import java.io.Serializable;

/*
 * Stores a (file) format for a dataset
 * Usually contains the extension for the fileformat
 * can also store synonyms for the same fileformat
 *  */
public class DatasetFormat implements Serializable{
	
	private static final long serialVersionUID = 6395549957680593343L;

	public DatasetFormat(String format){
		m_format = format.toLowerCase();
		m_synonyms = new String[0];
	}
	
	public DatasetFormat(String[] synonyms){
		if(synonyms.length > 0){
			m_format = synonyms[0].toLowerCase();
			m_synonyms = new String[(synonyms.length - 1)];
			
			for(int i = 1; i < synonyms.length; i++){
				m_synonyms[(i - 1)] = synonyms[i].toLowerCase();
			}
		}else{
			m_format = "";
			m_synonyms = new String[]{""};
		}
	}
	
	public String Format(){
		return m_format;
	}
	
	/*
	 * all Synonyms including the main format name
	 */
	public String[] allSynonyms(){
		String[] all = new String[(m_synonyms.length + 1)];
		all[0] = m_format;
		
		for(int index = 1; index < all.length; index++){
			all[index] = m_synonyms[(index - 1)];
		}
		return all;
	}
	
	/*
	 * only exact matches are of a formats synonyms are considered equal DatasetFormats
	 */
	public boolean compare(DatasetFormat in){

		
		for(String format : in.allSynonyms()){
			if(format.equals(m_format)) return true;
			
			for(String syn : m_synonyms)
				if(syn.equals(format)) return true;
		}
		
		return false;
	}

	private final String m_format;
	private final String[]  m_synonyms;
}
