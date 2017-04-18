package ac.at.wu.conceptfinder.dataset;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.Language;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

/*
 * represents a dcat:dataset
 */
public class Dataset implements Serializable{

	private static final long serialVersionUID = -986353595304050698L;

	/*
	 * Creates a new dataset
	 * ID should be the dct:identifier field of the dataset
	 */
	public Dataset(String ID){
		m_ID = new RdfId(ID);
		m_portal = "";
		m_distributions = new Vector<Distribution>(0, 1);
		m_title = "";
		m_keywords = new HashSet<String>();
		m_description = "";
		m_concepts = new Vector<Concept>();
		m_language = Language.NULL;
		m_categories = new EnumMap<BabelDomain, Float>(BabelDomain.class);
	}
	
	public Dataset(RdfId ID){
		m_ID = new RdfId(ID.value());
		m_portal = "";
		m_distributions = new Vector<Distribution>(0, 1);
		m_title = "";
		m_keywords = new HashSet<String>();
		m_description = "";
		m_concepts = new Vector<Concept>();
		m_language = Language.NULL;
		m_categories = new EnumMap<BabelDomain, Float>(BabelDomain.class);
	}
	
	@Override
	public final boolean equals(Object obj){
		if(obj == null) return false;
		if (!(obj instanceof Dataset))
	            return false;
	    if (obj == this)
	            return true;
	    Dataset dataset = (Dataset) obj;
	    return new EqualsBuilder().
	    		append(m_ID, dataset.ID()).
	    		isEquals();
	}
	
	@Override
	public final int hashCode(){
		return new HashCodeBuilder(17, 31).
				append(m_ID).				
				toHashCode();
	}
	
	public RdfId ID(){
		return new RdfId(m_ID.value());
	}
	
	public List<Distribution> Distributions(){
		return Collections.unmodifiableList(m_distributions);
	}
	
	public String Portal(){ return m_portal; }
	public String Title(){ return m_title; }
	public String Description(){ return m_description; }
	public Set<String> Keywords() { return m_keywords; }
	public Date Issued(){ return ( (m_issued == null) ? null : (Date) m_issued.clone() ); }
	public Date Modified(){ return ( (m_modified == null) ? null : (Date) m_modified.clone() ); }
	public Language Language(){ return m_language; }
	public List<Concept> Concepts(){ return Collections.unmodifiableList(m_concepts); }
	public Map<BabelDomain, Float> Categories(){ return Collections.unmodifiableMap(m_categories); }
	/*
	 * currently not used
	public URL URL(){
		if(m_URL == null) return null;
		try {
			return new URL(m_URL.toExternalForm());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
	}*/
	
	public void addDistribution(Distribution dist){
		if(!m_distributions.contains(dist))
			m_distributions.addElement(dist);
	}
	
	public void addConcept(Concept concept){
		if(!m_concepts.contains(concept))
			m_concepts.addElement(concept);
	}
	
	/*
	 * Adds a category to the dataset
	 * if the category all ready exists the confidence is overwritten
	 */
	public void addCategory(BabelDomain category, float confidence){
		m_categories.put(category, confidence);
	}
	
	public void setPortal(String portal){ m_portal = portal; }
	public void setTitle(String title){	m_title = title; }
	public void setDescription(String desc){ m_description = desc; }
	public void addKeyword(String keyword){ m_keywords.add(keyword); }
	public void setLanguage(Language lang){ m_language = lang; }
	
	/*
	 * Sets the date of issue for this dataset
	 * if the modified date precedes the new issue date 
	 * then the modify date is updated to the new issue date
	 */
	public void setIssued(Date issued){
		if(issued == null){
			m_issued = null;
			m_modified = null;
			return;
		}
		m_issued = (Date) issued.clone();
		
		if(m_modified == null){
			m_modified = (Date) issued.clone();
		}else if(m_modified.before(issued)){
			m_modified = (Date) issued.clone();
		}
 
	}
	
	/*
	 * Sets the date of last modification for this dataset
	 * if the modification date preceds the date of issue
	 * the date of issue will remain the modification date
	 */
	public void setModified(Date modified){
		if(modified == null){
			m_modified = null;
			return;
		}
		
		if(m_issued == null) m_issued = (Date) modified.clone();
		
		if(modified.after(m_issued)){
			m_modified = (Date) modified.clone();
		}else{
			m_modified = (Date) m_issued.clone();
		}
			
	}
	
	/* currently not used
	 * public void setURL(URL url) throws MalformedURLException{
		if(url == null){
			m_URL = null;
			return;
		}
		
		m_URL = new URL(url.toExternalForm());
	}*/
	
	public void clearCategories(){
		m_categories.clear();
	}
	
	private final RdfId m_ID;
	private String m_portal;
	private Vector<Distribution> m_distributions;
	private String m_title;
	private String m_description;
	private HashSet<String> m_keywords;
	private Date m_issued;
	private Date m_modified;
	//currently the ID is the URL of the dataset
	//private URL m_URL;
	private Language m_language;
	private Vector<Concept> m_concepts;
	private EnumMap<BabelDomain, Float> m_categories;

}
