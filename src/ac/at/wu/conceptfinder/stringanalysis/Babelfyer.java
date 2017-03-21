package ac.at.wu.conceptfinder.stringanalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ac.at.wu.conceptfinder.application.Globals;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.*;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetType;
import it.uniroma1.lcl.babelnet.InvalidBabelSynsetIDException;

/*
 * Uses babelfy to discover concepts in a text
 */
public class Babelfyer implements ConceptDetector {

	private static final long serialVersionUID = 7990419236707076556L;

	public Babelfyer() throws ConceptDetectionException{
		m_babelfyParams = new BabelfyParameters();
		m_bfyCalls = 0;
		
		//find word senses as well as named entities
		m_babelfyParams.setAnnotationType(SemanticAnnotationType.ALL);
		
		/**
		 * enable densest subgraph heuristic
		 * The main idea here is that the most suitable meanings of
		 * each text fragment will belong to the densest area of
		 * the disambiguation graph
		 */
		m_babelfyParams.setDensestSubgraph(true);
		
		//only exact matches are considered for disambiguation
		m_babelfyParams.setMatchingType(MatchingType.EXACT_MATCHING);
		
		//interpret all adjectives as nouns
		m_babelfyParams.setPoStaggingOptions(PosTaggingOptions.NOMINALIZE_ADJECTIVES);
		
		//annotate with BabelNet synsets
		m_babelfyParams.setAnnotationResource(SemanticAnnotationResource.BN);
		
		//use only top ranked candidates for a fragment
		m_babelfyParams.setScoredCandidates(ScoredCandidates.TOP);
		
		//use Most Common Sense heuristic 
		m_babelfyParams.setMCS(MCS.ON);
		
		m_babelNet = BabelNet.getInstance();
	}
	
	/*
	 * Uses babelfy to discover the concepts and scores in a text
	 * fills the input ConceptTexts concept and score fields 
	 */
	@Override
	public void discoverConcepts(ConceptText text, String mark, String delimiter) throws ConceptDetectionException{
		
		validateConceptText(text);
		String inputText = text.Text();
		int delimiterPos = inputText.length();
		
		if(!delimiter.isEmpty())
			delimiterPos = inputText.indexOf(delimiter);
		
		if(inputText.isEmpty()) return;
		if(text.Concepts().size() > 0) return;
		
		Babelfy bfy = new Babelfy(m_babelfyParams);
		
		try{
			List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(inputText, convertLangEnum(text.Language()));
			m_bfyCalls++;
			
			ArrayList<Concept> concepts = new ArrayList<Concept>();

			for(SemanticAnnotation annotation : bfyAnnotations){
				
				BabelSynset synset = m_babelNet.getSynset(new BabelSynsetID(annotation.getBabelSynsetID()));
				BabelConcept currentConcept = new BabelConcept(new ConceptID(annotation.getBabelSynsetID()), synset.toString());
				ConceptScores scores = new ConceptScores();
				
				scores.setCoherenceScore((float) annotation.getCoherenceScore());
				scores.setDisambiguationScore((float) annotation.getScore());
				scores.setRelevanceScore((float) annotation.getGlobalScore());
				
				currentConcept.setScores(scores);
	
				int endCharIndex = annotation.getCharOffsetFragment().getEnd();
				if(endCharIndex < delimiterPos)
					currentConcept.setMark(mark);
				
				if(synset.getSynsetType() == BabelSynsetType.NAMED_ENTITY)
					currentConcept.setMark(currentConcept.Mark() + "n");

				if(synset.isKeyConcept())
					currentConcept.setMark(currentConcept.Mark() + "k");
				
				concepts.add(currentConcept);

			}
			
			text.SetConcepts(concepts);
		}
		catch (java.lang.RuntimeException e){
			throw new ConceptDetectionException("Cannot connect to detection service!", ConceptDetectionError.cannotConnect);
		} catch (IOException e) {
			throw new ConceptDetectionException("Cannot connect to detection service!", ConceptDetectionError.cannotConnect);
		} catch (InvalidBabelSynsetIDException e) {
			throw new ConceptDetectionException("Invalid concept ID!", ConceptDetectionError.invalidID);
		}
		

	}
	
	/*
	 * the numver of calls to babelfy that this object has made
	 */
	public int babelfyCalls(){ return m_bfyCalls; }
	
	private void validateConceptText(ConceptText text) throws ConceptDetectionException{
		
		if(text.Language() == ac.at.wu.conceptfinder.stringanalysis.Language.NULL){
			throw new ConceptDetectionException("Language missing for text: " + text.Text(), 
					ConceptDetectionError.missingLanguage);
		}
		
		if(text.Concepts().size() > 0){
			throw new ConceptDetectionException("Concepts allready exist for text: " + text.Text(), 
					ConceptDetectionError.allreadyExistingConcepts);
		}
	}
	
	private it.uniroma1.lcl.jlt.util.Language convertLangEnum(ac.at.wu.conceptfinder.stringanalysis.Language lang){
		
		switch(lang){
		
			case DE:
				return it.uniroma1.lcl.jlt.util.Language.DE;
				
			case EN:
				return it.uniroma1.lcl.jlt.util.Language.EN;
				
			case ES:
				return it.uniroma1.lcl.jlt.util.Language.ES;
				
			case FR:
				return it.uniroma1.lcl.jlt.util.Language.FR;
				
			case IT:
				return it.uniroma1.lcl.jlt.util.Language.IT;
				
			case NULL:
				return null;
		
		}
		
		return null;
	}

	private BabelfyParameters m_babelfyParams;
	private int m_bfyCalls;
	private BabelNet m_babelNet;

}
