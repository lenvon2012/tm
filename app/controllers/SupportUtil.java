package controllers;

import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupportUtil extends TMController {
	
	private static final Logger log = LoggerFactory.getLogger(SupportUtil.class);
	
	public static void ItemCat(long parentCid, String search) {
		if (parentCid < 0L && StringUtils.isEmpty(search)) {
			renderError("请填写正确的Category Id或类目名");
		}
		if (StringUtils.isEmpty(search)) {
			renderResultJson(ItemCatPlay.findByParentCid(parentCid));
		} else {
			renderResultJson(ItemCatPlay.findAllByNameLike(search));
		}
	}
	
}
