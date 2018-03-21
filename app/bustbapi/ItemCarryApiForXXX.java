package bustbapi;

import com.taobao.api.domain.Item;
import models.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import result.TMResult;

import java.util.Map;

public class ItemCarryApiForXXX extends ItemCopyApiBase {

	private static final Logger log = LoggerFactory.getLogger(ItemCarryApiForXXX.class);

	private Map<String, String> imgMap;

	public ItemCarryApiForXXX(User user, Item item,  Map<String, String> imgMap) {
		super(user, item);
		this.imgMap = imgMap;
	}

	public static TMResult<Item> ItemCarrier(User user, Item item, Map<String, String> imgMap) {
		ItemCarryApiForXXX itemCarryApiForXXX = new ItemCarryApiForXXX(user, item, imgMap);
		TMResult<Item> result = itemCarryApiForXXX.itemCopy();

		return result;
	}

	@Override
	protected void reqSetPicPath() {
		// 设置主图
		String picUrl = imgMap.get(item.getPicUrl());
		req.setPicPath(picUrl);
	}

	@Override
	protected void reqSetDesc() {
		String desc = item.getDesc();
		for (String key : imgMap.keySet()) {
			String url = imgMap.get(key);
			desc = desc.replace(key, url);
		}
		desc += "<p style=\"height:0px;margin:0px;color:#ffffff;\">~~~~~</p>";
		req.setDesc(desc);
	}
}