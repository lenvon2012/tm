package dto.eslexicon;

import java.util.List;

import org.elasticsearch.search.SearchHit;

/**
 * @author:Young
 * @date:2016年7月14日下午2:57:33
 * @Description:
 */
public class ESSearchResult extends ESResult{
	
	boolean success;
	
	float max_score;
	/**
	 * @Description:The total number of hits that matches the search request.
	 */
	long total;
	/**
	 * @Description:
	 */
	long took;
	
	/**
	 * @Description:How long the search took in milliseconds.
	 */
	String msg;
	
	List<WordHit> wordHits;

	/**
	 * @Title:SearchResult
	 * @Description:
	 * @param max_score
	 * @param total:The total number of hits that matches the search request.
	 * @param took:How long the search took in milliseconds.
	 */
	public ESSearchResult(float max_score, long total, long took) {
		this.success = true;
		this.max_score = max_score;
		this.total = total;
		this.took = took;
	}

	public ESSearchResult(boolean success) {
		this.success = success;
	}
	
	public ESSearchResult(boolean success, String msg) {
		this.success = success;
		this.msg = msg;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		if (success) {
			result.append("查询用时").append(took).append("毫秒，命中").append(total).append("条结果，最高分").append(String.format("% 1.3f", max_score)).append("\n");
			if (wordHits!=null) {
				for (WordHit wordHit : wordHits) {
					result.append("命中记录：").append(wordHit.getName()).append("(").append(String.format("% 1.3f", wordHit.getScore())).append(")").append("\n");
				}
			}
		}else {
			result.append("查询失败，失败原因：").append(msg);
		}
		return result.toString();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public float getMax_score() {
		return max_score;
	}

	public void setMax_score(float max_score) {
		this.max_score = max_score;
	}

	/**
	 * @Description: The total number of hits that matches the search request.
	 * @return: long
	 */
	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * @Description: How long the search took in milliseconds.
	 * @return: long
	 */
	public long getTook() {
		return took;
	}

	public void setTook(long took) {
		this.took = took;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<WordHit> getWordHits() {
		return wordHits;
	}

	public void setWordHits(List<WordHit> wordHits) {
		this.wordHits = wordHits;
	}
		
}
