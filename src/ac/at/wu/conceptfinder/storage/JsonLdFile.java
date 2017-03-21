package ac.at.wu.conceptfinder.storage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.github.jsonldjava.core.JsonLdApi;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.dataset.Distribution;

public class JsonLdFile {
	
	/*
	 * Creates a JsonLdFile by opening a connection to a file.
	 * 
	 * @param name the path and filename to the jsonld file 
	 */
	public JsonLdFile(String name) throws FileNotFoundException{
		m_inputStream = new FileInputStream(name);
		m_filename = name;
	}
	
	/*
	 * loads all dcat datasets that are found in the jsonld file
	 * expect the jsonld in expanded form
	 */
	public List<Dataset> loadDatasets() throws StorageException{
		
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		ArrayList<Distribution> distributions = new ArrayList<Distribution>();
		
		HashSet<RDFDataset.Quad> descriptions = new HashSet<RDFDataset.Quad>();
		HashSet<RDFDataset.Quad> titles = new HashSet<RDFDataset.Quad>();
		HashSet<RDFDataset.Quad> keywords = new HashSet<RDFDataset.Quad>();
		
		HashMap<RdfId, Integer> datasetMap = new HashMap<RdfId, Integer>();
		HashMap<RdfId, Integer> distMap = new HashMap<RdfId, Integer>();
		HashMap<RdfId, RdfId> distToDataset = new HashMap<RdfId, RdfId>();
		int datasetIndexCounter = 0;
		int distIndexCounter = 0;
			
		try {
			Object jsonObject = JsonUtils.fromInputStream(m_inputStream);
			JsonLdApi api = new JsonLdApi(jsonObject, new JsonLdOptions());
		
			RDFDataset rdf = api.toRDF();
			List<RDFDataset.Quad> quads = rdf.getQuads("@default");
			
			for(RDFDataset.Quad quad : quads){
				RDFDataset.Node subject = quad.getSubject();
				RDFDataset.Node predicate = quad.getPredicate();
				RDFDataset.Node object = quad.getObject();
				
				//a dataset is found
				if(predicate.getValue().toLowerCase().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
						&& object.getValue().toLowerCase().equals("http://www.w3.org/ns/dcat#dataset")){
					
					RdfId datasetId = new RdfId(subject.getValue());

					
					//do not allow duplicate datasets
					if(datasetMap.containsKey(datasetId)) continue;
					
					datasetMap.put(datasetId, datasetIndexCounter);
					
					Dataset dataset = new Dataset(datasetId);
					datasets.add(dataset);
					datasetIndexCounter++;
				}
								
				//a distribution is found
				if(predicate.getValue().toLowerCase().equals("http://www.w3.org/ns/dcat#distribution")){	

					RdfId id = new RdfId(object.getValue());

					//do not allow duplicate distributions
					if(distMap.containsKey(id)) continue;
					
					distMap.put(id, distIndexCounter);
					
					Distribution dist = new Distribution(id);
					distributions.add(dist);
					distIndexCounter++;
					
					distToDataset.put(id, new RdfId(subject.getValue()));
					
				}
				
				//a description is found
				if(predicate.getValue().toLowerCase().equals("http://purl.org/dc/terms/description"))
					descriptions.add(quad);
				
				//a title is found
				if(predicate.getValue().toLowerCase().equals("http://purl.org/dc/terms/title"))
					titles.add(quad);
				
				//a keyword is found
				if(predicate.getValue().toLowerCase().equals("http://www.w3.org/ns/dcat#keyword"))
					keywords.add(quad);
			}
			
			//add distributions to respective datasets
			for(Distribution dist : distributions){
				if(distToDataset.containsKey(dist.ID())){
					if(datasetMap.containsKey(distToDataset.get(dist.ID()))){
						datasets.get(datasetMap.get(distToDataset.get(dist.ID()))).addDistribution(dist);
					}
				}
				
			}
			
			//add descriptions to datasets/distributions
			for(RDFDataset.Quad quad : descriptions){
				RdfId descOf = new RdfId(quad.getSubject().getValue());
				
				if(datasetMap.containsKey(descOf)){
					int datasetIndex = datasetMap.get(descOf);
					datasets.get(datasetIndex).setDescription(quad.getObject().getValue());
				}else if(distMap.containsKey(descOf)){
					int discIndex = distMap.get(descOf);
					distributions.get(discIndex).setDescription(quad.getObject().getValue());
				}
			}
			
			//add titles do datasets/distributions
			for(RDFDataset.Quad quad : titles){
				RdfId titleOf = new RdfId(quad.getSubject().getValue());
				
				if(datasetMap.containsKey(titleOf)){
					int datasetIndex = datasetMap.get(titleOf);
					datasets.get(datasetIndex).setTitle(quad.getObject().getValue());
				}else if(distMap.containsKey(titleOf)){
					int discIndex = distMap.get(titleOf);
					distributions.get(discIndex).setTitle(quad.getObject().getValue());
				}
			}
			
			//add keywords to datasets
			for(RDFDataset.Quad quad : keywords){
				RdfId keywordOf = new RdfId(quad.getSubject().getValue());
				
				if(datasetMap.containsKey(keywordOf)){
					int datasetIndex = datasetMap.get(keywordOf);
					datasets.get(datasetIndex).addKeyword(quad.getObject().getValue());
				}
			}
		} catch (JsonLdError e) {
			throw new StorageException("Somethin wrong with the Json: " + e.getMessage(), 
					StorageError.jsonError);
		} catch (IOException e) {
			throw new StorageException("Cannot access file: " + m_filename, StorageError.cannotConnect);
		}

		return datasets;
	}
	
	String m_filename;
	InputStream m_inputStream;
}
