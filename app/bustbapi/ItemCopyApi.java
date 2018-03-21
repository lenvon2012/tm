package bustbapi;

import java.util.List;

import models.carrierTask.CarrierItemPlay;
import models.carrierTask.ItemCarryCustom;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.PropImg;

public class ItemCopyApi extends ItemCopyApiBase{

	private static final Logger log = LoggerFactory.getLogger(ItemCopyApi.class);
	
	protected static String url = "http://gw.api.taobao.com/router/rest";
	
	protected static String appkey = "21255586";

	protected static String appSecret = "04eb2b1fa4687fbcdeff12a795f863d4";

	public ItemCopyApi(User user, Item item) {
		super(user, item);
	}

	public static TMResult<Item> itemCarrier(User user, Item item){
		ItemCopyApiBase itemCopyApi;
		ItemCarryCustom itemCarryCustom = ItemCarryCustom.findByUserId(user.getIdlong());
		if (itemCarryCustom == null) {
			itemCopyApi = new ItemCopyApi(user, item);
		} else {
			itemCopyApi = new ItemCopyApiForCustom(user, item, itemCarryCustom);
		}

		return itemCopyApi.itemCopy();
	}

	@Override
	protected void afterReqExecute() {
		if (resp == null) return;
		if (resp.isSuccess()) addPic();
	}
	
	@Override
	protected void beforeReturn() {
//		if(resp != null && resp.isSuccess() && "clorest510".equalsIgnoreCase(user.getUserNick())) {
//			new CarrierItemPlay(resp.getItem().getNumIid(), item.getNumIid(), user).jdbcSave();
//		}
	}

	// 添加副图
	private void addPic() {
		Long ignoreImgIndex = 1L;

		//添加副图
		Item itemNow = resp.getItem();
		List<ItemImg> imgs = item.getItemImgs();
		if (!CommonUtils.isEmpty(imgs)) {
			for (ItemImg itemImg : imgs) {
				if (itemImg.getPosition() != null && itemImg.getPosition().longValue() == ignoreImgIndex) continue;
				new ItemApi.ItemImgPictureAdd(user, itemNow.getNumIid(), itemImg).call();
			}
		}

		//添加属性图片
		List<PropImg> propImgs = item.getPropImgs();
		if (!CommonUtils.isEmpty(propImgs)) {
			for (PropImg propImg : propImgs) new ItemApi.ItemPropPictureAdd(user, itemNow.getNumIid(), propImg).call();
		}
	}
}