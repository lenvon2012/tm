package actions;

import java.util.List;

import models.mysql.word.WordBase;

public class KeyWordAction {
	public static List<WordBase> searchword(String[] conditions , int pn, int ps){
		String baohanall = conditions[0];
		pn = 1;
		ps = 20;
		String query = "word like ? and word in (word like ? , word like ? , word like ?) and NOT in (word like ?,word like ?,word like ?) limit ?,?";
		List<WordBase> words = WordBase.find(query, baohanall).from(pn).fetch(ps);
		return words;
	}
}
