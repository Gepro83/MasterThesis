package ac.at.wu.conceptfinder.stringanalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import ac.at.wu.conceptfinder.application.Globals;

public class ConceptText implements Serializable{

	private static final long serialVersionUID = 7074366865743870506L;

	public ConceptText(String text) {
		m_lang = Language.NULL;
		m_concepts = new ArrayList<Concept>();
		m_text = text;
	}
	
	public String Text(){
		return m_text;
	}
	
	public Language Language(){
		return m_lang;
	}
	
	public void SetLanguage(Language language){
		m_lang = language;
	}
	
	public void SetConcepts(Collection<Concept> concepts){
		m_concepts = new ArrayList<Concept>(concepts);
	}
	
	public List<Concept> Concepts(){
		return Collections.unmodifiableList(m_concepts);
	}
	
	private String m_text;
	private Language m_lang;
	private ArrayList<Concept> m_concepts;

}
