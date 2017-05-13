package ac.at.wu.conceptfinder.stringanalysis;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;


//Can identify the language of a string
public class LanguageDetector {
	
	public LanguageDetector() throws LangDetectException {
		DetectorFactory.loadProfile("resources/profiles");
	}
	
	public ac.at.wu.conceptfinder.stringanalysis.Language identifyLanguage(String text) throws LangDetectException{
		
		m_detector = DetectorFactory.create();
		m_detector.append(text);
		String resultLanguage = m_detector.detect();
		ac.at.wu.conceptfinder.stringanalysis.Language result = convertToEnum(resultLanguage);
		if(result == Language.NULL) 
			throw new LangDetectException(null, "Cannot find Language for String: " + text);
		return result;
	}
	
	public static ac.at.wu.conceptfinder.stringanalysis.Language stringToEnum(String language){
		return convertToEnum(language.toLowerCase());
	}
	
	private static ac.at.wu.conceptfinder.stringanalysis.Language convertToEnum(String language){
		
		switch(language){
		
			case "de":
				return ac.at.wu.conceptfinder.stringanalysis.Language.DE;
				
			case "en":
				return ac.at.wu.conceptfinder.stringanalysis.Language.EN;
				
			case "es":
				return ac.at.wu.conceptfinder.stringanalysis.Language.ES;
				
			case "it":
				return ac.at.wu.conceptfinder.stringanalysis.Language.IT;
				
			case "fr":
				return ac.at.wu.conceptfinder.stringanalysis.Language.FR;
			
			//case "ro":
			//	return ac.at.wu.conceptfinder.stringanalysis.Language.RO;
			
			case "nl":
				return ac.at.wu.conceptfinder.stringanalysis.Language.NL;
				
			case "fi":
				return ac.at.wu.conceptfinder.stringanalysis.Language.FI;
				
			//case "ca":
			//	return ac.at.wu.conceptfinder.stringanalysis.Language.CA;
				
			case "af":
				return ac.at.wu.conceptfinder.stringanalysis.Language.AF;
			
			case "tl":
				return ac.at.wu.conceptfinder.stringanalysis.Language.TL;
				
			case "da":
				return ac.at.wu.conceptfinder.stringanalysis.Language.DA;
				
			case "ja":
				return ac.at.wu.conceptfinder.stringanalysis.Language.JA;
			
			case "el":
				return ac.at.wu.conceptfinder.stringanalysis.Language.EL;
				
			case "pt":
				return ac.at.wu.conceptfinder.stringanalysis.Language.PT;
				
			case "pl":
				return ac.at.wu.conceptfinder.stringanalysis.Language.PL;
				
			case "hr":
				return ac.at.wu.conceptfinder.stringanalysis.Language.HR;
				
			case "ru":
				return ac.at.wu.conceptfinder.stringanalysis.Language.RU;
				
			case "hu":
				return ac.at.wu.conceptfinder.stringanalysis.Language.HU;
				
			case "sv":
				return ac.at.wu.conceptfinder.stringanalysis.Language.SV;
			
			case "sk":
				return ac.at.wu.conceptfinder.stringanalysis.Language.SK;
		
			case "bg":
				return ac.at.wu.conceptfinder.stringanalysis.Language.BG;
			
			case "sw":
				return ac.at.wu.conceptfinder.stringanalysis.Language.SW;
		}
		
		return ac.at.wu.conceptfinder.stringanalysis.Language.NULL;
	}
	
	private Detector m_detector;

}
