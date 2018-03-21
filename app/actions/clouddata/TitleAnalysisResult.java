package actions.clouddata;

import models.user.User;

public class TitleAnalysisResult {
	
	public String name;
	
	public String word;
	
	public String hotSearchDegree;
	
	public String relevancy;
	
	public TitleAnalysisResult() {
		super();
	}
	
	public TitleAnalysisResult(String name, String word, String hotSearchDegree, String relevancy, User user) {
		super();
		this.name = name;
		this.word = word;
		this.hotSearchDegree = hotSearchDegree;
		this.relevancy = relevancy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getHotSearchDegree() {
		return hotSearchDegree;
	}

	public void setHotSearchDegree(String hotSearchDegree) {
		this.hotSearchDegree = hotSearchDegree;
	}

	public String getRelevancy() {
		return relevancy;
	}

	public void setRelevancy(String relevancy) {
		this.relevancy = relevancy;
	}
	
}
