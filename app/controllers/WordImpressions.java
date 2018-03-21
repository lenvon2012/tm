package controllers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;
import models.item.NewItemCatPlay;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;

public class WordImpressions extends Controller {

    public static void getWordList() {
    	List<NewItemCatPlay> wordList = NewItemCatPlay.findNeedSearchList();
    	ResultData data = new ResultData();
    	data.data = wordList;
    	renderJSON(JsonUtil.getJson(data));
    }
    
    public static void wordListResult(Long id, String word, Long impressions) {
    	if(id == null) {
    		return;
    	}
    	if(StringUtils.isEmpty(word)) {
    		return;
    	}
    	if(impressions == null) {
    		return;
    	}
    	NewItemCatPlay result = NewItemCatPlay.findById(id);
    	if(result == null) {
    		return;
    	}
    	result.setWord(word);
    	result.setImpressions(impressions);
    	boolean isSuccess = result.jdbcSave();
    	if(!isSuccess) {
    		return;
    	}
    }
	
    public static class ResultData {
    	public boolean status = true;
        public String msg = "Success !";
        public Object data;
        public int ps = 20;
        public int pn = 0;
    }
}