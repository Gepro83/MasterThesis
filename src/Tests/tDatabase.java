package Tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.DatasetFormat;
import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.dataset.Distribution;
import ac.at.wu.conceptfinder.storage.Database;
import ac.at.wu.conceptfinder.storage.DatasetSearchMask;
import ac.at.wu.conceptfinder.storage.StorageException;
import ac.at.wu.conceptfinder.stringanalysis.BabelConcept;
import ac.at.wu.conceptfinder.stringanalysis.BabelConceptCreator;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptCreator;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
import ac.at.wu.conceptfinder.stringanalysis.InvalidConceptIDException;
import ac.at.wu.conceptfinder.stringanalysis.Language;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;

public class tDatabase {
	
	private ConceptCreator m_ccreator;
	
	public tDatabase(){
		m_ccreator = new BabelConceptCreator();
	}

	@Test
	public void testDatabase() throws StorageException {
		Database db;
		db = new Database("jdbc:postgresql://localhost:5432/ConceptFinder", "Georg", "georg", m_ccreator);
		
	}
	
	@Test
	public void testcreate_deleteDataset() throws StorageException, IOException, InvalidConceptIDException, InvalidBabelSynsetIDException{
		Database db;
		db = new Database("jdbc:postgresql://localhost:5432/ConceptFinder", "Georg", "georg", m_ccreator);
		
		db.deleteDataset(new RdfId("ac"));
		Dataset ds = new Dataset(new RdfId("ac"));
		HashSet<Dataset> dss = new HashSet<Dataset>();
		dss.add(ds);
		
		db.createDatasets(dss);		
		
		db.deleteDataset(new RdfId("abc"));
		ds = new Dataset(new RdfId("abc"));
		
		ds.setDescription("asdfasdf asdf asdfasdf 2");
		ds.setTitle("asdf");
		ds.setLanguage(Language.DE);
		
		Date issued = new Date(112345L);
		Date modif = new Date(13223454433523L);
		ds.setIssued(issued);
		ds.setModified(modif);
		ds.addKeyword("this");
		ds.addKeyword("ss");
		ds.addKeyword("me");
		
		Concept car = new BabelConcept(new ConceptID("bn:00007309n"), "car");
		Concept car2 = new BabelConcept(new ConceptID("bn:00007309n"), "car");
		Concept tree = new BabelConcept(new ConceptID("bn:00078131n"), "tree");
		car.Scores().setCoherenceScore(1);
		car.Scores().setDisambiguationScore(0.5f);
		car.Scores().setRelevanceScore(32);
		
		tree.Scores().setCoherenceScore(21.53f);
		tree.Scores().setDisambiguationScore(2);
		tree.Scores().setDisambiguationScore(5);
		
		ds.addConcept(car);
		ds.addConcept(car2);
		ds.addConcept(tree);
		
		Distribution dist = new Distribution(new RdfId("disttest3"), "ja", "ba", issued, modif, "lice", 43245, new DatasetFormat(new String[]{"rdf", "bfd"}), new URL("http://www.example.com"));
		ds.addDistribution(dist);
		
		dss.clear();
		dss.add(ds);
		db.createDatasets(dss);
		

	}
	
		
	@Test
	public void testGetDataset() throws StorageException, IOException, InvalidConceptIDException, InvalidBabelSynsetIDException{
		Database db;
		db = new Database("jdbc:postgresql://localhost:5432/ConceptFinder", "Georg", "georg", m_ccreator);

		db.deleteDataset(new RdfId("GET"));
		Dataset ds = new Dataset(new RdfId("GET"));
		HashSet<Dataset> dss = new HashSet<Dataset>();
		dss.add(ds);
		
		Concept car = new BabelConcept(new ConceptID("bn:00007309n"), "car");
		Distribution dist = new Distribution(new RdfId("testgetdataset"));
		
		dist.setAccessURL(new URL("http://www.ge.org"));
		dist.setBytsize(312389f);
		dist.setIssued(new Date(12309L));
		ds.addDistribution(dist);
		
		ds.addConcept(car);
		ds.setDescription("asdfasdf asdf asdfasdf 2");
		ds.setTitle("asdf");
		ds.setLanguage(Language.DE);
		ds.setIssued(new Date(1234785L));
		db.createDatasets(dss);
		
		Dataset get = db.getDataset(new RdfId("GET"));
		
		assertEquals(Language.DE, get.Language());
		
		Date comp = new Date(1234785L);
		assertEquals(comp.getTime(), get.Issued().getTime());
		
		assertEquals("bn:00007309n", get.Concepts().get(0).ID().value());	
		
		assertEquals((new Date(12309L).getTime()), get.Distributions().get(0).Issued().getTime());
	}
	
	@Test
	public void testUpdateDataset() throws StorageException, IOException, InvalidConceptIDException, InvalidBabelSynsetIDException{
		Database db;
		db = new Database("jdbc:postgresql://localhost:5432/ConceptFinder", "Georg", "georg", m_ccreator);
		
		db.deleteDataset(new RdfId("updateTEST"));
		Dataset ds = new Dataset(new RdfId("updateTEST"));
		HashSet<Dataset> dss = new HashSet<Dataset>();
		dss.add(ds);
		
		Distribution dist1 = new Distribution(new RdfId("firstInsert"));
		ds.addDistribution(dist1);
		
		db.createDatasets(dss);
		
		Distribution dist2 = new Distribution(new RdfId("sencondInser"));
		dist2.setDescription("DESCR");
		ds.setDescription("datasetdescription");
		ds.addDistribution(dist2);
		
		db.updateDatasets(dss, true);
		
		Dataset get = db.getDataset(new RdfId("updateTEST"));
		
		assertEquals("DESCR", get.Distributions().get(1).Description());
		
		Concept car = new BabelConcept(new ConceptID("bn:00007309n"), "car");
		
		ds.addConcept(car);
		dss = new HashSet<Dataset>();
		dss.add(ds);
		
		db.updateDatasets(dss, true);
		
		get = db.getDataset(new RdfId("updateTEST"));
		
		assertEquals("bn:00007309n", get.Concepts().get(0).ID().value());
		
		ds = new Dataset(new RdfId("updateTEST"));
		dss = new HashSet<Dataset>();
		dss.add(ds);
		
		db.updateDatasets(dss, true);
		get = db.getDataset(new RdfId("updateTEST"));
		
		assertEquals(0, get.Distributions().size());
		assertEquals(0, get.Concepts().size());
		
		db.updateDatasets(dss, true);

	}
	
	@Test
	public void testSaveDatasets() throws StorageException, IOException, InvalidBabelSynsetIDException{
		Database db;
		db = new Database("jdbc:postgresql://localhost:5432/ConceptFinder", "Georg", "georg", m_ccreator);
		
		db.deleteDataset(new RdfId("savedatasettest1"));
		db.deleteDataset(new RdfId("savedatasettest2"));
		
		Dataset ds1 = new Dataset(new RdfId("savedatasettest1"));
		ds1.addDistribution(new Distribution(new RdfId("saveDist1")));
		
		
		Dataset ds2 = new Dataset(new RdfId("savedatasettest2"));
		ds2.addDistribution(new Distribution(new RdfId("saveDist2")));
		ds2.addConcept(new BabelConcept(new ConceptID("saveCpt2"), "name"));
		
		HashSet<Dataset> dss = new HashSet<Dataset>();
		dss.add(ds1);
		
		db.saveDatasets(dss, true);
		
		Dataset get = db.getDataset(new RdfId("savedatasettest1"));
		
		assertEquals("saveDist1", get.Distributions().get(0).ID().value());
		
		dss.clear();
		ds1.addConcept(new BabelConcept(new ConceptID("saveCpt1"), "name"));
		dss.add(ds1);
		dss.add(ds2);
		
		db.saveDatasets(dss, true);
		
		get = db.getDataset(new RdfId("savedatasettest1"));
		
		assertEquals("saveCpt1", get.Concepts().get(0).ID().value());
		
		get = db.getDataset(new RdfId("savedatasettest2"));
		
		assertEquals("saveDist2", get.Distributions().get(0).ID().value());
		
	}
	
	@Test
	public void tGetDatasets() throws StorageException{
		DatasetSearchMask mask = new DatasetSearchMask(new Language[]{Language.EN}, null);
		Database db = new Database("jdbc:postgresql://localhost:5432/ConceptFinder", "Georg", "georg", m_ccreator);
		
		Set<Dataset> dss = db.getDatasets(mask);
		
		for(Dataset ds : dss){
			System.out.println(ds.ID().value());
			assertEquals(Language.EN, ds.Language());
		}
		
		mask = new DatasetSearchMask(new Language[]{Language.DE}, new String[]{"http://data.opendataportal.at"});
				
		dss = db.getDatasets(mask);
		
		for(Dataset ds : dss){
			System.out.println(ds.ID().value());
			assertEquals(Language.DE, ds.Language());
		}
		
		dss = db.getDatasets(null);
		
		System.out.println(dss.size());
	}
}
