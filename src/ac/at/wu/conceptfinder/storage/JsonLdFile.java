package ac.at.wu.conceptfinder.storage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.github.jsonldjava.core.JsonLdApi;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.Distribution;
import ac.at.wu.conceptfinder.dataset.RdfId;

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
	 */
	public List<Dataset> loadDatasets(){
		
		//Start a list of datasets
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		//Read the file, it must consist of a json array holding all datasets
		JsonReader reader = Json.createReader(m_inputStream);
		JsonArray allDatasets = reader.readArray();
		
		//Go through the array and create a dataset for each entry
		for(int i = 0; i < allDatasets.size(); i++){
			//Each datasets is contained in another array, otherwise it is an error json object
			if(allDatasets.get(i).getValueType() == JsonValue.ValueType.ARRAY){
				//Go through the array to find the actual dataset object
				JsonArray datasetArray = allDatasets.getJsonArray(i);
				for(int j = 0; j < datasetArray.size(); j++){
					//Check if this entry is a dataset
					JsonObject datasetObject = datasetArray.getJsonObject(j);
					JsonArray objectType = datasetObject.getJsonArray("@type");
					boolean isDataset = false;
										for(int k = 0; k < objectType.size(); k++)
						if(objectType.getString(k).toLowerCase().equals("http://www.w3.org/ns/dcat#dataset"))
							isDataset = true;
					if(!isDataset) continue;
					
					//Create a new dataset from the respective fields
					String ID = datasetObject.getString("@id");
					//If there is no ID then skip this entry
					if(ID == null) continue;
					Dataset dataset = new Dataset(new RdfId(ID));
					
					//Set title, description and keywords, check if they exist first
					JsonArray title = datasetObject.getJsonArray("http://purl.org/dc/terms/title");
					if(title != null){
						dataset.setTitle(title.getJsonObject(0).getString("@value"));
					}else{
						dataset.setTitle("");
					}
					
					JsonArray description = datasetObject.getJsonArray("http://purl.org/dc/terms/description");
					if(description != null){
						dataset.setDescription(description.getJsonObject(0).getString("@value"));
					}else{
						dataset.setDescription("");
					}
					
					JsonArray keywords = datasetObject.getJsonArray("http://www.w3.org/ns/dcat#keyword");
					if(keywords != null)
						for(JsonObject keywordObject : keywords.getValuesAs(JsonObject.class))
							dataset.addKeyword(keywordObject.getString("@value"));
					
					//Add the datasets to the resultlist
					datasets.add(dataset);
				}
			}
		}
		
		return datasets;
		/*
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
			
			
			
			JsonLdApi api = new JsonLdApi(allDatasetsBuilder.build(), new JsonLdOptions());
		
			
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
		}

		return datasets;*/
		
	}
	
	String m_filename;
	InputStream m_inputStream;
}
