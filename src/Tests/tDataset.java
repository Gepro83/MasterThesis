package Tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.DatasetFormat;
import ac.at.wu.conceptfinder.dataset.Distribution;
import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.stringanalysis.BabelConcept;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptId;
import ac.at.wu.conceptfinder.stringanalysis.InvalidConceptIDException;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import nl.jqno.equalsverifier.EqualsVerifier;

public class tDataset {

	public tDataset(){
		
	}
	
	@Test
	public void testDatasetString() {
		Dataset ds = new Dataset("asdf");
		assertEquals("asdf", ds.ID().value());
	}

	@Test
	public void testDatasetDatasetID() {
		RdfId id = new RdfId("abc");
		Dataset ds = new Dataset(id);
		assertEquals(id, ds.ID());
	}

	@Test
	public void testDistribitions() throws MalformedURLException{
		Distribution dist = new Distribution(new RdfId("testdist"));
		dist.setAccessURL(new URL("https://example.org"));
		dist.setDescription("desc");
		dist.setIssued(new Date(1235490453));
	}
	
	@Test 
	public void testIssuedModified(){
		Dataset ds = new Dataset(new RdfId("asdf"));
		Date now = new Date();
		Date before = new Date();
		before.setTime(0);
		
		ds.setIssued(now);
		ds.setModified(before);
		
		assertEquals(now.getTime(), ds.Modified().getTime());
		
		ds.setIssued(before);
		assertEquals(now.getTime(), ds.Modified().getTime());

	}
	
	@Test
	public void testConcepts() throws IOException, InvalidConceptIDException, InvalidBabelSynsetIDException{
		Concept car = new BabelConcept(new ConceptId("bn:00007309n"), "car", null, 0);
		Concept tree = new BabelConcept(new ConceptId("bn:00078131n"), "tree", null, 0);
		Dataset ds = new Dataset(new RdfId("asdf"));
		
		assertEquals(0, ds.Concepts().size());

		ds.addConcept(car);
		ds.addConcept(tree);
				
		assertEquals(2, ds.Concepts().size());
	}
	
	@Test
	public void testEquals(){

		Dataset ds = new Dataset(new RdfId("asdf"));
		Dataset ds2 = new Dataset(new RdfId("asdf"));
		
		assertEquals(true, ds.equals(ds2));
		
		HashSet<Dataset> set = new HashSet<Dataset>();
		set.add(ds);
		
		assertEquals(false, set.add(ds2));
		
		set.remove(ds2);
		
		assertEquals(0, set.size());
	}

}
