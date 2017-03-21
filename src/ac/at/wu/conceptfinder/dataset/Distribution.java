package ac.at.wu.conceptfinder.dataset;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/*
 * stores a dcat:distribution of a dataset
 * some values may be null
 */
public class Distribution implements Serializable {

	private static final long serialVersionUID = 7636428562545082764L;

	public Distribution(RdfId id, String title, String description, Date issued, Date modified, String license, 
			float bytesize, DatasetFormat format, URL accessURL) throws MalformedURLException{
		
		m_id = new RdfId(id.value());
		m_title = title;
		m_description = description;
		m_license = license;
		m_bytesize = bytesize;
		
		if(format != null)
			m_format = new DatasetFormat(format.allSynonyms());
		
		if(accessURL != null)
			m_accessURL = new URL(accessURL.toExternalForm());
				
		if(issued == null) return;
		m_issued = (Date) issued.clone();
		
		if(modified == null){
			m_modified = (Date) m_issued.clone();
			return;
		}
		
		if(modified.before(m_issued)){
			m_modified = (Date) issued.clone();
		}else{
			m_modified = (Date) modified.clone();
		}
	}
	
	public Distribution(RdfId id){
		m_id = new RdfId(id.value());
		m_title = "";
		m_description = "";
		m_license = "";
		m_bytesize = 0;
	}
	
	public void setTitle(String title){
		m_title = title;
	}
	
	public void setDescription(String description){
		m_description = description;
	}
	
	public void setLicense(String license){
		m_license = license;
	}
	
	/*
	 *  Sets the date of issue for this distribution
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
	 * Sets the date of last modification for this distribution
	 * if the modification date preceds the date of issue
	 * the date of issue will remain the modification date
	 */
	public void setModified(Date modified){
		if(modified == null){
			m_modified = null;
			return;
		}
		
		if(m_issued == null)
			m_issued = (Date) modified.clone();
		
		if(modified.after(m_issued)){
			m_modified = (Date) modified.clone();
		}else {
			m_modified = (Date) m_issued.clone();
		}
		
	}
	
	public void setBytsize(float size){
		m_bytesize = size;
	}
	
	public void setAccessURL(URL url) throws MalformedURLException{
		m_accessURL = new URL(url.toExternalForm());
	}
	
	public void setFormat(DatasetFormat format){
		m_format = new DatasetFormat(format.allSynonyms());
	}
	
	public RdfId ID(){
		return m_id;
	}
	
	public String Title(){
		return m_title;
	}
	
	public String Description(){
		return m_description;
	}
	
	public Date Issued(){
		return m_issued;
	}
	
	public Date Modified(){
		return m_modified;
	}
	
	public String License(){
		return m_license;
	}
	
	public float Bytesize(){
		return m_bytesize;
	}
		
	public DatasetFormat Format(){
		return m_format;
	}
	
	public URL AccessURL(){
		return m_accessURL;
	}
	
	private RdfId m_id;
	private String m_title;
	private String m_description;
	private Date m_issued;
	private Date m_modified;
	private String m_license;
	private float m_bytesize;
	private DatasetFormat m_format;
	private URL m_accessURL;
}
