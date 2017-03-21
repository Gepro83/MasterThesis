package ac.at.wu.conceptfinder.storage;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import ac.at.wu.conceptfinder.stringanalysis.Language;

/*
 * A helper class that represents a searchmask for datasets
 */
public class DatasetSearchMask {
	
	/*
	 * creates a new searchmask
	 * parameters may be null if not needed
	 */
	public DatasetSearchMask(Language[] languages, String[] portals){
		m_portals = new HashSet<String>();
		m_languages = EnumSet.noneOf(Language.class);
		
		if(portals != null)
			for(String portal : portals)
				m_portals.add(portal);
		
		if(languages != null)
			for(Language lang : languages)
				m_languages.add(lang);
	}
	
	/*
	 * returns the set of portals that should be searched for
	 * if the set is empty portal is no search criterion
	 */
	public Set<String> Portals(){
		return Collections.unmodifiableSet(m_portals);
	}
	
	/*
	 * returns the set of languages that should be searched for
	 * if the set is empty language is no search criterion
	 */
	public Set<Language> Languages(){
		return Collections.unmodifiableSet(m_languages);
	}
	
	private HashSet<String> m_portals;
	private EnumSet<Language> m_languages;
}
