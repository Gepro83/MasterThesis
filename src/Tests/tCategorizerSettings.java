package Tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.ConceptFeatures;
import ac.at.wu.conceptfinder.dataset.Configuration;
import ac.at.wu.conceptfinder.storage.CategorizerSettings;
import ac.at.wu.conceptfinder.stringanalysis.ConceptID;
import it.uniroma1.lcl.babelnet.data.BabelDomain;

public class tCategorizerSettings {

	public tCategorizerSettings(){
		m_file = "C:/Users/Arbeit/workspace/MasterThesis/resources/file.cat";
	}

	@Test
	public void testSave() throws IOException {
		Configuration conf = new Configuration();
		conf.setKeywordsWeight(0);
		ConceptFeatures feat1 = new ConceptFeatures();
		feat1.setName("concept1");
		feat1.setFrequency(0.4f);
		feat1.setWeight(0.4f);
		feat1.setCategory(BabelDomain.BIOLOGY);
		ConceptFeatures feat2 = new ConceptFeatures();
		feat2.setName("concept2");
		feat2.setFrequency(0.3f);
		feat2.setWeight(0.9f);
		feat2.setCategory(BabelDomain.ANIMALS);
		CategorizerSettings cs = new CategorizerSettings(conf);
		cs.addConceptFeature(new ConceptID("c1"), feat1);
		cs.addConceptFeature(new ConceptID("c2"), feat2);
		
		cs.save(new File(m_file));
	}

	@Test
	public void testLoad() throws ClassNotFoundException, IOException {
		testSave();
		CategorizerSettings cs = CategorizerSettings.load(new File(m_file));
		assertEquals(2, cs.getConceptFeatures().size());
		for(ConceptFeatures feature : cs.getConceptFeatures().values()){
			assertTrue(feature.Category() == BabelDomain.ANIMALS || feature.Category() == BabelDomain.BIOLOGY);
		}
	}
	
	private String m_file;

}
