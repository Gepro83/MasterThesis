package Tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import ac.at.wu.conceptfinder.storage.DatasetSearchMask;
import ac.at.wu.conceptfinder.stringanalysis.Language;

public class tDatasetSearchMask {

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		DatasetSearchMask m = new DatasetSearchMask(null, null);
		
		assertEquals(true, m.Languages().isEmpty());
		
		DatasetSearchMask m2 = new DatasetSearchMask(new Language[]{Language.DE, Language.EN} , null);
		
		assertEquals(2, m2.Languages().size());
		
	}

}
