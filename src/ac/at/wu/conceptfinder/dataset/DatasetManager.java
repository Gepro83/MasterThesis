package ac.at.wu.conceptfinder.dataset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ac.at.wu.conceptfinder.stringanalysis.Language;


/*
 * Manages a collection of datasets
 */
public class DatasetManager implements Iterable<Dataset> {

	public DatasetManager(){
		m_datasets = new HashSet<Dataset>();
	}
	
	/*
	 * Adds a dataset to the manager, duplicate datasets are not allowed
	 * neither duplicate objects, nor datasets with the same DatasetID
	 */
	public void addDataset(Dataset dataset){
		if(m_datasets.contains(dataset)) return;
		
		for(Dataset ds : m_datasets){
			if(ds.equals(dataset)) return;
			if(ds.ID().equals(dataset.ID())) return;
		}
		
		m_datasets.add(dataset);
	}
	
	/*
	 * Adds a set of datasets
	 */
	public void addDatasets(Set<Dataset> datasets){
		for(Dataset dataset : datasets)
			addDataset(dataset);
	}
	
	public void clear(){
		m_datasets.clear();
	}
	
	public void removeDataset(Dataset ds){
		m_datasets.remove(ds);

		return;
	}

	public Set<Dataset> Datasets(){
		return Collections.unmodifiableSet(m_datasets);
	}
	

	/*
	 * finds a dataset managed by this DatasetManager given an ID
	 * returns null if no match is found 
	 */
	public Dataset Dataset(Dataset ID){
		for(Dataset dataset : m_datasets){
			if(dataset.ID().equals(ID)) return dataset;
		}
		return null;
	}

	/*
	 * returns the set of datasets that match a given language
	 */
	public Set<Dataset> matchingDatasets(Language language){
		HashSet<Dataset> ret = new HashSet<Dataset>();
		
		for(Dataset dataset : m_datasets)
			if(dataset.Language().equals(language)) ret.add(dataset);
		
		return ret;
	}
	
	/*
	 * returns the number of managed datasets
	 */
	public int DatasetCount(){ return m_datasets.size(); }
	
	private HashSet<Dataset> m_datasets;
	
	@Override
	public Iterator<Dataset> iterator() {
		return m_datasets.iterator();
	}
}
