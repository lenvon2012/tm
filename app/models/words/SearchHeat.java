package models.words;

import java.io.Serializable;

public class SearchHeat implements Serializable{

	private static final long serialVersionUID = -1L;

	public String date;

	public Long score;

	public int sold;

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Long getScore() {
		return score;
	}
	public void setScore(Long score) {
		this.score = score;
	}
	public int getSold() {
		return sold;
	}
	public void setSold(int sold) {
		this.sold = sold;
	}
}
