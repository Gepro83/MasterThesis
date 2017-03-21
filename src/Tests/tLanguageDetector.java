package Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.cybozu.labs.langdetect.LangDetectException;

import ac.at.wu.conceptfinder.stringanalysis.Language;
import ac.at.wu.conceptfinder.stringanalysis.LanguageDetector;

public class tLanguageDetector {

	@Test
	public void testIdentifyLanguage() {
		String text = "This is obiously english. How are you?";
		
		try{
			LanguageDetector detector = new LanguageDetector();
			
			Language language = detector.identifyLanguage(text);
			assertEquals(language, Language.EN);
			
			text = "Jetzt Deutsch! Gehts? Hallo!";
			language = detector.identifyLanguage(text);
			assertEquals(language, Language.DE);
			
			text = "Parle vous francais?";
			language = detector.identifyLanguage(text);
			assertEquals(language, Language.FR);

			text = "Cómo estás tan?";
			language = detector.identifyLanguage(text);
			assertEquals(language, Language.ES);
			
			text = "come stai così?";
			language = detector.identifyLanguage(text);
			assertEquals(language, Language.IT);
			
		} catch(LangDetectException e){
			fail("Exception thrown: " + e.getMessage());
		}
		
	}

}
