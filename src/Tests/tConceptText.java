package Tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import ac.at.wu.conceptfinder.dataset.RdfId;
import ac.at.wu.conceptfinder.stringanalysis.BabelConcept;
import ac.at.wu.conceptfinder.stringanalysis.Concept;
import ac.at.wu.conceptfinder.stringanalysis.ConceptId;
import ac.at.wu.conceptfinder.stringanalysis.ConceptText;
import ac.at.wu.conceptfinder.stringanalysis.InvalidConceptIDException;
import ac.at.wu.conceptfinder.stringanalysis.Language;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;
import nl.jqno.equalsverifier.EqualsVerifier;

public class tConceptText {
	
	ConceptText text = new ConceptText("This is a test!");
	Concept car, tree; 
	
	public tConceptText() throws IOException, InvalidConceptIDException, InvalidBabelSynsetIDException{
		car = new BabelConcept(new ConceptId("bn:00007309n"), "car", null, 0);
		tree = new BabelConcept(new ConceptId("bn:00078131n"), "tree", null, 0);
		text.SetLanguage(Language.EN);
	}
	
	@Test
	public void testText() {
		assertEquals("This is a test!", text.Text());
	}

	@Test
	public void testSetGetConcepts() {
		ArrayList<Concept> list = new ArrayList<Concept>();
		list.add(car);
		list.add(tree);
		
		text.SetConcepts(list);
		
		assertEquals(list, text.Concepts());
	}
	
	@Test
	public void testLanguage(){
		assertEquals(Language.EN, text.Language());
	}

	@Test
	public void testConceptIDequals(){
		EqualsVerifier.forClass(ConceptId.class).verify();
	}

}
