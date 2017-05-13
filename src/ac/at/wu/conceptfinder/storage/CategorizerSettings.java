package ac.at.wu.conceptfinder.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ac.at.wu.conceptfinder.dataset.ConceptFeatures;
import ac.at.wu.conceptfinder.dataset.Configuration;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;

/*
 * Encapsulates all settings of a categorizer. Handles saving and loading them to/from a file.
 */
public class CategorizerSettings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8243566972310672812L;
	
	public CategorizerSettings(Configuration conf){
		m_configuration = conf;
		if(conf == null)
			m_configuration = new Configuration();
		m_conceptFeatures = new HashMap<ConceptID, ConceptFeatures>();
	}
	
	public Configuration getConfiguration() { return m_configuration; }
	public Map<ConceptID, ConceptFeatures> getConceptFeatures() { return m_conceptFeatures; }
	
	/*
	 * Adds a concept type (id) with its features.
	 * If the concept id was already added the features will be replaced. 
	 */
	public void addConceptFeature(ConceptID cid, ConceptFeatures cfeature) { m_conceptFeatures.put(cid, cfeature); }
	
	/*
	 * Saves this CategorizerSettings object to the specified location.
	 */
	public void save(File file) throws IOException{
		FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.close();
	}
	
	/*
	 * Loads a CategorizerSettings object from a file.
	 */
	public static CategorizerSettings load(File file) throws IOException, ClassNotFoundException{
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        if(obj instanceof CategorizerSettings){
        	return (CategorizerSettings) obj;
        }else{
        	throw new IOException("This is no CategorizerSettings file.");
        }
	}
	
	//the configuration of the categorizer
	private Configuration m_configuration;
	//all concept ids with features that differ from the standard (meaning they have been altered by the user)
	private HashMap<ConceptID, ConceptFeatures> m_conceptFeatures;
	
}
