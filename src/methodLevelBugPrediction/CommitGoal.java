package methodLevelBugPrediction;

import methodLevelBugPrediction.beans.Commit;

public class CommitGoal {

	public static void tagCommit(Commit pCommit) {
		
		if(CommitGoal.isEnhancement(pCommit)) {
			pCommit.getTags().put("COMMIT-GOAL", "ENHANCEMENT");
		} else if(CommitGoal.isNewFeature(pCommit)) {
			pCommit.getTags().put("COMMIT-GOAL", "NEW-FEATURE");
		} else if(CommitGoal.isBugFixing(pCommit)) {
			pCommit.getTags().put("COMMIT-GOAL", "BUG-FIXING");
		} else if(CommitGoal.isRefactoring(pCommit)) {
			pCommit.getTags().put("COMMIT-GOAL", "REFACTORING");
		} else if(CommitGoal.isMerge(pCommit)) {
			pCommit.getTags().put("COMMIT-GOAL", "MERGE-BRANCH");
		} else if(CommitGoal.isPorting(pCommit)) {
			pCommit.getTags().put("COMMIT-GOAL", "PORTING-GITHUB");
		} else {
			pCommit.getTags().put("COMMIT-GOAL", "ENHANCEMENT");
		}
	}

	private static boolean isEnhancement(Commit pCommit) {
		String commitMessage = pCommit.getSubject() + " " +pCommit.getBody();

		if( (commitMessage.toLowerCase().contains("updat")) || (commitMessage.toLowerCase().contains("modif"))
				|| (commitMessage.toLowerCase().contains("upgrad")) || (commitMessage.toLowerCase().contains("export"))
				|| (commitMessage.toLowerCase().contains("remov")) || (commitMessage.toLowerCase().contains("integrat"))
				|| (commitMessage.toLowerCase().contains("support")) || (commitMessage.toLowerCase().contains("enhancement"))
				|| (commitMessage.toLowerCase().contains("replac")) || (commitMessage.toLowerCase().contains("includ"))
				|| (commitMessage.toLowerCase().contains("expos")) || (commitMessage.toLowerCase().contains("better"))
				|| (commitMessage.toLowerCase().contains("svn")) || (commitMessage.toLowerCase().contains("generate"))) { 
			return true;
		}
		
		return false;
	}

	private static boolean isNewFeature(Commit pCommit) {
		String commitMessage = pCommit.getSubject() + " " +pCommit.getBody();

		if( (commitMessage.toLowerCase().contains("new")) || (commitMessage.toLowerCase().contains("feature"))
				|| (commitMessage.toLowerCase().contains("add")) || (commitMessage.toLowerCase().contains("create"))
				|| (commitMessage.toLowerCase().contains("introduc")) || (commitMessage.toLowerCase().contains("migrat"))) {
			return true;
		}

		return false;
	}

	private static boolean isBugFixing(Commit pCommit) {
		String commitMessage = pCommit.getSubject() + " " +pCommit.getBody();

		if( (commitMessage.toLowerCase().contains("fix")) || (commitMessage.toLowerCase().contains("repair"))
				|| (commitMessage.toLowerCase().contains("error")) || (commitMessage.toLowerCase().contains("avoid"))
				|| (commitMessage.toLowerCase().contains("can ")) || (commitMessage.toLowerCase().contains("bug "))
				|| (commitMessage.toLowerCase().contains("issue ")) || (commitMessage.toLowerCase().contains("#"))
				|| (commitMessage.toLowerCase().contains("exception"))) {
			return true;
		}

		return false;
	}

	private static boolean isRefactoring(Commit pCommit) {
		String commitMessage = pCommit.getSubject() + " " +pCommit.getBody();

		if( (commitMessage.toLowerCase().contains("renam")) || (commitMessage.toLowerCase().contains("reorganiz"))
				|| (commitMessage.toLowerCase().contains("refactor")) || (commitMessage.toLowerCase().contains("clean"))
				|| (commitMessage.toLowerCase().contains("polish")) || (commitMessage.toLowerCase().contains("typo"))
				|| (commitMessage.toLowerCase().contains("move")) || (commitMessage.toLowerCase().contains("extract"))
				|| (commitMessage.toLowerCase().contains("reorder")) || (commitMessage.toLowerCase().contains("re-order"))) {
			return true;
		}

		return false;
	}
	
	private static boolean isMerge(Commit pCommit) {
		String commitMessage = pCommit.getSubject() + " " +pCommit.getBody();

		if( (commitMessage.toLowerCase().contains("merge"))) {
			return true;
		}

		return false;
	}
	
	private static boolean isPorting(Commit pCommit) {
		String commitMessage = pCommit.getSubject() + " " +pCommit.getBody();

		if( (commitMessage.toLowerCase().contains("initial "))) {
			return true;
		}

		return false;
	}
}