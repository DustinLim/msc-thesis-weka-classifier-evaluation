package methodLevelBugPrediction.similarityComputation;

import java.util.regex.Pattern;

public class JaccardIndex {

	public static double computeAsimmetricJaccardIndex(String pDocumentOne, String pDocumentTwo) {
		Pattern space = Pattern.compile(" ");
	
		String[] docOneWords = space.split(pDocumentOne);
		String[] docTwoWords = space.split(pDocumentTwo);
		
		Double overlap = JaccardIndex.overlapWords(docOneWords, docTwoWords);
		
		Double result = 0.0;
		
		if(docTwoWords.length < docOneWords.length)
			result = (overlap/docTwoWords.length);
		else result = (overlap/docOneWords.length);
		
		if(result.doubleValue()>1.0) 
			return 1.0;
		
		if(result.isInfinite()) 
			return 0.0;
		
		if(result.toString().contains("E"))
			return 0.0;
		
		return result;
	}
	
	public static double computeSimmetricJaccardIndex(String pDocumentOne, String pDocumentTwo) {
		Pattern space = Pattern.compile(" ");
		
		String[] docOneWords = space.split(pDocumentOne);
		String[] docTwoWords = space.split(pDocumentTwo);
		
		double overlap = JaccardIndex.overlapWords(docOneWords, docTwoWords);
		double totalWords = docOneWords.length + docTwoWords.length;
		
		return (overlap/(totalWords-1));
	}
	
	private static double overlapWords(String[] pDocOne, String[] pDocTwo) {
		double counter=0.0;
		
		for(String word: pDocOne) {
			for(String wordTwo: pDocTwo) {
				if(word.equals(wordTwo))
					counter++;
			}
		}
		
		return counter;
	}
}
