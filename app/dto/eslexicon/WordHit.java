package dto.eslexicon;

/**
 * @author:Young
 * @date:2016年7月14日下午2:40:30
 * @Description:Map for the ES-hotword-keyword-word search result model
 */
public class WordHit {
	
	String id;
	
	float score;
	
	String name;
	
	long impressions;
	
	String word;
	
	public WordHit(String id, float score,String name,long impressions, String word) {
		this.id = id;
//		this.score = String.format("% 1.3f", score);
		this.score = score;
		this.name = name;
		this.impressions = impressions;
		this.word = word;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getImpressions() {
		return impressions;
	}
	public void setImpressions(long impressions) {
		this.impressions = impressions;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
}
