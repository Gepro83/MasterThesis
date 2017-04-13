package Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.Dataset;
import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.dataset.DatasetManager;

public class tDatasetManager {

	@Test
	public void testAddDataset() {
		DatasetManager m = new DatasetManager();
		Dataset ds = new Dataset(new RdfId("asdf"));
		
		m.addDataset(ds);
		m.addDataset(ds);
		assertEquals(1, m.Datasets().size());
		
		ds = new Dataset(new RdfId("asdf"));
		m.addDataset(ds);
		assertEquals(1, m.Datasets().size());
		
		ds = new Dataset(new RdfId("asdf2"));
		m.addDataset(ds);
		assertEquals(2, m.Datasets().size());
	}

	@Test
	public void testRemoveDataset() {
		DatasetManager m = new DatasetManager();
		Dataset ds = new Dataset(new RdfId("asdf"));
		
		m.addDataset(ds);
		ds = new Dataset(new RdfId("asdf2"));
		m.addDataset(ds);
		//m.removeDataset(new RdfId("asdf2"));
		assertEquals(1, m.Datasets().size());
	}

	@Test
	public void testIterator() {
		DatasetManager m = new DatasetManager();
		Dataset ds1 = new Dataset(new RdfId("asdf"));
		Dataset ds2 = new Dataset(new RdfId("asdf3"));
		Dataset ds3 = new Dataset(new RdfId("asdf6"));
		
		m.addDataset(ds1);
		m.addDataset(ds2);
		m.addDataset(ds3);
		
		int i = 0;
		for(Dataset d : m){
			i++;
		}
		assertEquals(3, i);
	}

}
