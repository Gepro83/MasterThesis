package Tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ tConceptText.class, tLanguageDetector.class, tBabelfyer.class, tDataset.class, tDatasetFormat.class, tDatabase.class, tDatasetManager.class })
public class AllTests {

}
