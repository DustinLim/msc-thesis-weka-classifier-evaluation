package methodLevelBugPrediction.similarityComputation;

public class LevenshteinDistance {
	
	public int computeDistance(String pFeatureOne, String pFeatureTwo) {
		pFeatureOne = pFeatureOne.toLowerCase();
		pFeatureTwo = pFeatureTwo.toLowerCase();

		int[] costs = new int[pFeatureTwo.length() + 1];
		for (int i = 0; i <= pFeatureOne.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= pFeatureTwo.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (pFeatureOne.charAt(i - 1) != pFeatureTwo.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[pFeatureTwo.length()] = lastValue;
		}
		return costs[pFeatureTwo.length()];
	}
}
