package Tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.Distribution;
import ac.at.wu.conceptfinder.storage.JsonLdFile;
import ac.at.wu.conceptfinder.storage.StorageException;

public class tJsonLdFile {

	@Test
	public void testLoadDataset() throws FileNotFoundException, StorageException {
		JsonLdFile json = new JsonLdFile("resources/opendataportal.jsonld");
		
		List<Dataset> datasets = json.loadDatasets();

		
		for(Dataset ds : datasets){
			System.out.println(ds.ID().value());
			System.out.println(ds.Title());
			//System.out.println(ds.Distributions().get(0).ID().value());
		//	System.out.println(ds.Distributions().get(0).Title());
			for(String s : ds.Keywords())
				System.out.println(s);
		}
			
		
		
	}

}
