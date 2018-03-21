package actions.CWordAction;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemCatPlay;
import models.item.ItemPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import configs.CWordsConfig;
import dao.item.ItemDao;


public class CWordAction {

	private final static Logger log = LoggerFactory.getLogger(CWordAction.class);
	
	public static List<String> getCWords(Long userId, Long numIid, String title, Long cid) {
		List<String> cWords = new ArrayList<String>();
		if(StringUtils.isEmpty(title)){
			ItemPlay item =  ItemDao.findByNumIid(userId, numIid);
			if(item != null) {
				title = item.getTitle();
			}
		}
		// 生成类目中心词
		String category = CWordAction.genCatName(userId, numIid, cid);
		if(StringUtils.isNotBlank(category)) {
			cWords.add(category);
		}

		List<String> catCWords = CWordsConfig.categoryMap.get(cid);
		if(!CommonUtils.isEmpty(catCWords)){
			if(!StringUtils.isEmpty(title)) {
				for(String word : catCWords){
					if(title.indexOf(word) >= 0){
						cWords.add(word);
					}
				}
			}
			
		}
		return cWords;
	}
	
	public static String genCatName(Long userId, Long numIid, Long cid) {
		if(cid == null || cid < 0){
			ItemPlay itemPlay = ItemDao.findByNumIid(userId, numIid);
			if(itemPlay == null){
				return StringUtils.EMPTY;
			} else {
				cid = itemPlay.getCid();
			}
		}
		
		ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
		if(itemCatPlay == null){
			return StringUtils.EMPTY;
		} else {
			return itemCatPlay.getName();
		}
	}
}
