package Tests;

import static org.junit.Assert.*;

import java.awt.List;
import java.util.ArrayList;

import org.junit.Test;

import ac.at.wu.conceptfinder.stringanalysis.Babelfyer;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptDetectionException;
import ac.at.wu.conceptfinder.stringanalysis.ConceptText;


public class tBabelfyer {

	public tBabelfyer() throws ConceptDetectionException{
		m_babelfyer = new Babelfyer();
	}
	
	@Test
	public void testDiscoverConcepts() throws ConceptDetectionException{
		
		
			m_babelfyer = new Babelfyer();
			ConceptText text = new ConceptText("My son is the coolest kid~ in the world. I love him very much. My car is not so cool!");
			text.SetLanguage(ac.at.wu.conceptfinder.stringanalysis.Language.EN);
			m_babelfyer.discoverConcepts(text, "k", "~");
			ArrayList<Concept> concepts = new ArrayList<Concept>(text.Concepts());
			
			if(concepts.size() == 0) fail("no concepts found");
			
			for(Concept concept : concepts){
				if(concept.Scores().RelevanceScore() < 0) fail("RelevanceScore not set");
				if(concept.Scores().CoherenceScore() < 0) fail("CoherenceScore not set");
				if(concept.Scores().DisambiguationScore() < 0) fail("DisambigutationScore not set");
				System.out.println(concept.Name());
			}
			

	}

	private Babelfyer m_babelfyer;
}
