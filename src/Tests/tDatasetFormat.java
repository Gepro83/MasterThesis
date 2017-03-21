package Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.DatasetFormat;

public class tDatasetFormat {

	private static DatasetFormat single = new DatasetFormat("rdf");
	private DatasetFormat multi = new DatasetFormat(new String[]{"rdf", "jar", "csv"});
	private DatasetFormat syno = new DatasetFormat(new String[]{"b", "c", "d", "csv"});
	private DatasetFormat syno2 = new DatasetFormat(new String[]{"rdf", "c", "d", "x"});
	private DatasetFormat syno3 = new DatasetFormat(new String[]{"RDF"});
	
	@Test
	public void testFormat() {
		assertEquals("rdf", single.Format());
		assertEquals("rdf", multi.Format());
		
	}

	@Test
	public void testSynonyms() {
		assertEquals("jar", multi.allSynonyms()[1]);
		assertEquals("csv", multi.allSynonyms()[2]);
	}

	@Test
	public void testEqualsDatasetFormat() {
		assertEquals(true, single.compare(multi));
		assertEquals(true, multi.compare(syno));
		assertEquals(true, multi.compare(syno2));
		assertEquals(false, single.compare(syno));
		assertEquals(true, single.compare(syno3));
	}

}
