package actions.carriertask;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.carrierTask.*;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.NewProxyTools;
import result.TMResult;
import actions.ItemGetAction;
import bustbapi.ItemApi;
import bustbapi.ItemCopyApi;
import bustbapi.ItemCopyApiForCustom;
import bustbapi.SchemaApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;
import com.taobao.api.domain.PropImg;
import com.taobao.top.schema.exception.TopSchemaException;
import com.taobao.top.schema.factory.SchemaWriter;
import com.taobao.top.schema.field.Field;

import configs.Subscribe.Version;
import controllers.ItemCarrier;
import dao.UserDao;

/**
 * Created by Administrator on 2016/3/8.
 */
public class CarrierTaskAction {

    private static final Logger log = LoggerFactory.getLogger(CarrierTaskAction.class);

    public static boolean checkParams(ShopBabyBean bean) {
        return bean != null && !(bean.getPn() == null || bean.getTaskId() == null || bean.getBabyCnt() == null || bean.getPublisher() == null);
    }

    public static TMResult<SubCarrierTask.SubTaskInfo> doCarry(SubCarrierTask.SubTaskInfo info) {
        Long numIid = null;
        String url = info.getUrl();
        String publisher = info.getPublisher();
        if (StringUtils.isBlank(url)) {
            return new TMResult<SubCarrierTask.SubTaskInfo>(false, "请先输入拷贝宝贝ID 或 宝贝网址！", info);
        }
        url = url.trim();
        if (NumberUtils.isDigits(url)) {
            numIid = NumberUtil.parserLong(url, 0L);
        } else {
            numIid = getNumId(url);
        }

        if (numIid == null || numIid <= 0) {
            return new TMResult<SubCarrierTask.SubTaskInfo>(false, "请输入有效的需要拷贝宝贝ID 或 宝贝网址！", info);
        }

        User user = UserDao.findByUserNick(publisher);

        Item itemOrigin = ItemGetAction.getItem(user.getSessionKey(), numIid);
        if (itemOrigin == null) {
            return new TMResult<SubCarrierTask.SubTaskInfo>(false, "拷贝失败，未查到要复制的宝贝相关信息！", info);
        }

        ItemApi.ItemFullCarrier carrier = new ItemApi.ItemFullCarrier(user, itemOrigin);

        Item itemNow = carrier.call();
        if (itemNow == null) {
            String msg = StringUtils.isEmpty(carrier.errorMsg) ? "返回结果为空！" : carrier.errorMsg;
            return new TMResult<SubCarrierTask.SubTaskInfo>(false, "<b>拷贝失败，请重试或联系客服反馈！</b><br>失败原因：" + msg, info);
        }

        List<ItemImg> imgs = itemOrigin.getItemImgs();
        if (!CommonUtils.isEmpty(imgs)) {
            for (ItemImg itemImg : imgs) {
                if (itemImg.getPosition() != null && itemImg.getPosition().longValue() == 0L) {
                    continue;
                }
                new ItemApi.ItemImgPictureAdd(user, itemNow.getNumIid(), itemImg).call();
            }
        }
        List<PropImg> propImgs = itemOrigin.getPropImgs();
        if (!CommonUtils.isEmpty(propImgs)) {
            for (PropImg propImg : propImgs) {
                new ItemApi.ItemPropPictureAdd(user, itemNow.getNumIid(), propImg).call();

            }
        }

        log.warn("success copy from [numIid: " + numIid + "] -> to [" + user.getUserNick() + "] id: "
                + itemNow.getNumIid());

        String itemUrl = "http://item.taobao.com/item.htm?id=" + itemNow.getNumIid();
        return new TMResult<SubCarrierTask.SubTaskInfo>(true, "拷贝成功，请在仓库中查看！新宝贝地址：<br><a href='" + itemUrl
                + "' target='_blank'>"
                + itemUrl + "</a>", info);
    }

	// 宝贝复制-批量复制
	public static boolean doCarry(long subId, long numIid, String wapData, String descData, String wirelessData, String publisher) {

		User user = UserDao.findByUserNick(publisher);
		if(user == null) {
			return SubCarrierTask.updateBySubId(subId, SubCarrierTask.SubCarrierTaskStatus.failure, "拷贝失败，用户【" + publisher + "】不存在！ ", null, null);
		}
		if(user.getVersion() == Version.LL) {
			Boolean success = CarrierLimitForDQ.checkUserLimit(user.getId());
			if(!success) {
				SubCarrierTask.updateBySubId(subId, SubCarrierTask.SubCarrierTaskStatus.failure, "已达到当月最大可复制宝贝数，如需继续复制请联系客服升级版本！", null, null);
			}
		}

		TMResult tmResult = null;
		if (user.isTmall()) {
			tmResult = doCarryForTmall(numIid, wapData, descData, user);
		} else {
			tmResult = doCarryForTaobao(subId, numIid, wapData, descData, wirelessData, user);
		}

		Boolean success = false;
		if (tmResult.isOk()) {
			if (user.isTmall()) {
				Map<String, String> itemMap = (Map<String, String>) tmResult.getRes();
				success = SubCarrierTask.updateBySubId(subId, SubCarrierTask.SubCarrierTaskStatus.success, tmResult.getMsg(), itemMap.get("title"), itemMap.get("item_image_0"));
			} else {
				Item item = (Item) tmResult.getRes();
				success =  SubCarrierTask.updateBySubId(subId, SubCarrierTask.SubCarrierTaskStatus.success, tmResult.getMsg(), item.getTitle(), item.getPicUrl());
			}
			if(user.getVersion() == Version.LL) {
				CarrierLimitForDQ.updateUseCountByUserId(user.getId());
			}
		} else {
			success = SubCarrierTask.updateBySubId(subId, SubCarrierTask.SubCarrierTaskStatus.failure, tmResult.getMsg(), null, null);
			checkExceptionMsg(subId, tmResult.getMsg());
		}

		return success;
		
	}

    // 子任务里出现了容量不足的错误或者宝贝发布数量上限 将主任务下的所有未操作的子任务设置为错误状态
    private static void checkExceptionMsg(Long subId, String msg) {
        String picExceptionMsg = "容量不足，请登录图片空间（tu.taobao.com）清理图片或订购存储功能包";
        String publishItemAmountExceptionMsg = "您今天发布的宝贝数量已超过了平台可支持单个账号宝贝发布数量";
        if (StringUtils.isNotEmpty(msg) && (msg.contains(picExceptionMsg) || msg.contains(publishItemAmountExceptionMsg))) {
            // 获取主任务id
            SubCarrierTask subCarrierTask = SubCarrierTask.findById(subId);
            long taskId = subCarrierTask.getTaskId();
            // 更新改主任务id下所有的未操作的子任务
            Long num = SubCarrierTask.finishFailSubTaskByTaskId(taskId, picExceptionMsg);
            // 主任务中的FinishCnt加上更新的数量
            CarrierTask.addFinishCnt(taskId, num);
        }
    }
	
	public static final List<String> VIP_USER_NICK = new ArrayList<String>();
	
	static {
		VIP_USER_NICK.add("hz易促");
		VIP_USER_NICK.add("clorest510");
		VIP_USER_NICK.add("julepcouture旗舰店");
		VIP_USER_NICK.add("madya服饰旗舰店");
		VIP_USER_NICK.add("dd幂群");
		VIP_USER_NICK.add("zzz牛牛咯");
		VIP_USER_NICK.add("腴雅旗舰店");
	}

    // 宝贝复制-天猫
    public static TMResult doCarryForTmall(long numIid, String wapData, String descData, User user) {
        Map<String, String> itemMap = ItemGetAction.getCopyItem(user, numIid, wapData, descData);
        if (itemMap == null || itemMap.isEmpty()) {
            return new TMResult(false, "复制任务添加成功，请到<a href='/kits/taskList'>任务中心</a>查看最新进程", null);
        }

        // 判断复制的是否是自己的店铺
        String targetNick = itemMap.get("nick");
        if(StringUtils.isEmpty(targetNick)) {
            return new TMResult(false, "拷贝失败，未查询到宝贝的店铺来源！", itemMap);
        }
        
        if(VIP_USER_NICK.contains(user.getUserNick())) {
            // 货号变更
            String num = itemMap.get("货号");
            num = "1003-t-" + numIid + "-" + num;
            itemMap.put("货号", num);
        }

        Long cid = Long.parseLong(itemMap.get("cid"));

        // 调用tmall.product.match.schema.get接口获取产品匹配的规则
        SchemaApi.tmallProductMatchSchemaGet pmsg = new SchemaApi.tmallProductMatchSchemaGet(user.getSessionKey(), cid);
        String productXmlString  = pmsg.call();
        if(StringUtils.isEmpty(productXmlString)) {
            if(StringUtils.isEmpty(pmsg.errorMsg)) {
                pmsg.errorMsg = "未匹配到相关产品规则,该类目商品不支持接口发布,请到商家后台发布";
            }
            return new TMResult(false, pmsg.errorMsg, itemMap);
        }
        log.info("productXmlStirng:" + productXmlString + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 根据规则生成产品匹配xml
        List<Field> newProductFieldList = ItemCarrier.getNewFieldList(productXmlString, itemMap, user);
        String propvalues = StringUtils.EMPTY;
        try {
            propvalues = SchemaWriter.writeParamXmlString(newProductFieldList);
            if("<itemParam/>".equalsIgnoreCase(propvalues)) {
                log.error("生成产品匹配xml异常");
                return new TMResult(false, "拷贝失败，生成产品匹配xml异常", itemMap);
            }
        } catch (TopSchemaException e) {
            e.printStackTrace();
        }

        // 调用tmall.product.schema.match进行产品匹配（返回product_id）
        SchemaApi.tmallProductSchemaMatch psm = new SchemaApi.tmallProductSchemaMatch(user.getSessionKey(), cid, propvalues);
        String productIdStr = psm.call();
        if(StringUtils.isEmpty(productIdStr)) {
            if(StringUtils.isEmpty(psm.errorMsg)) {
                // 调用tmall.product.add.schema.get接口获取产品发布涉及的规则
                SchemaApi.tmallProductAddSchemaGet pas = new SchemaApi.tmallProductAddSchemaGet(user.getSessionKey(), cid, null);
                String productAddString = pas.call();
                if(StringUtils.isEmpty(productAddString)) {
                    return new TMResult(false, pmsg.getSubErrorMsg(), itemMap);
                }
                log.info("productAddString:" + productAddString + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                // 根据规则生成产品发布xml
                List<Field> productAddFieldList = ItemCarrier.getNewFieldList(productAddString, itemMap, user);
                String xmlData = StringUtils.EMPTY;
                try {
                    xmlData = SchemaWriter.writeParamXmlString(productAddFieldList);
                    if("<itemParam/>".equalsIgnoreCase(xmlData)) {
                        log.error("生成产品发布xml异常");
                        return new TMResult(false, "拷贝失败，生成产品发布xml异常", itemMap);
                    }
                } catch (TopSchemaException e) {
                    e.printStackTrace();
                }

                // 调用tmall.product.schema.add发布产品
                SchemaApi.tmallProductSchemaAdd psa = new SchemaApi.tmallProductSchemaAdd(user.getSessionKey(), cid, null, xmlData);
                productIdStr = psa.call();
                if(StringUtils.isEmpty(productIdStr)) {
                    return new TMResult(false, "</br>您可能没有发布该类目或品牌产品的权限", itemMap);
                }
                // 重新调用tmall.product.schema.match进行产品匹配（返回product_id）
                try {
					Thread.sleep(500);
					productIdStr = new SchemaApi.tmallProductSchemaMatch(user.getSessionKey(), cid, propvalues).call();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            } else {
                return new TMResult(false, "</br>您可能没有发布该类目或品牌商品的权限", itemMap);
            }
        }
        Long productId = Long.parseLong(productIdStr);
        log.info("产品匹配的productId:" + productId + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 调用tmall.item.add.schema.get获取商品发布的规则
        SchemaApi.tmallItemAddSchemaGet iasg = new SchemaApi.tmallItemAddSchemaGet(user.getSessionKey(), cid, productId);
        String itemXmlString = iasg.call();
        if(StringUtils.isEmpty(itemXmlString)) {
            return new TMResult(false, iasg.errorMsg, itemMap);
        }
        log.info("itemXmlStirng:" + itemXmlString + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 根据规则生成商品上新xml
        List<Field> newItemFieldList = ItemCarrier.getNewFieldList(itemXmlString, itemMap, user);
        String addXml = StringUtils.EMPTY;
        try {
            addXml = SchemaWriter.writeParamXmlString(newItemFieldList);
            if(StringUtils.isEmpty(addXml)) {
                log.error("生成商品上新xml异常");
                return new TMResult(false, "拷贝失败，生成商品上新xml异常", itemMap);
            }
        } catch (TopSchemaException e) {
            e.printStackTrace();
        }
        log.info("addXml:" + addXml + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // 调用tmall.item.schema.add进行商品上新
        SchemaApi.tmallItemSchemaAdd isa = new SchemaApi.tmallItemSchemaAdd(user.getSessionKey(), cid, productId, addXml);
        String resule = isa.call();
        if(StringUtils.isEmpty(resule)) {
            return new TMResult(false, isa.errorMsg, itemMap);
        }
        log.info("商品上新的add_item_result:" + resule + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        String itemUrl = "http://item.taobao.com/item.htm?id=" + resule;

//        if("julepcouture旗舰店".equalsIgnoreCase(user.getUserNick())) {
//            new CarrierItemPlay(Long.valueOf(resule), numIid, user).jdbcSave();
//        }

        return new TMResult(true, itemUrl, itemMap);
    }
    public static TMResult doCarryForTmall(long numIid, User user) {
        return doCarryForTmall(numIid, null, null, user);
    }

    // 宝贝复制-淘宝
    public static TMResult doCarryForTaobao(Long subId, Long numIid, String wapData, String descData, String wirelessData, User user) {
        String sid = user.getSessionKey();

        // 宝贝的相关属性获取
        Item itemOrigin;
        try {
            itemOrigin = ItemGetAction.getItemExt(sid, numIid, wapData, descData, wirelessData);
        } catch (Exception e) {
            e.printStackTrace();
            return new TMResult(false, e.toString(), null);
        }
        if (itemOrigin == null) {
            return new TMResult(false, "复制任务添加成功，请到<a href='/kits/taskList'>任务中心</a>查看最新进程", null);
        }

        // 宝贝复制自定义设置
        ItemCarryCustom itemCarryCustom = getItemCarryCustom(subId, user);

        // 宝贝属性上传
        ItemCopyApi itemCopyApi;
        if (itemCarryCustom == null) itemCopyApi = new ItemCopyApi(user, itemOrigin);
        else itemCopyApi = new ItemCopyApiForCustom(user, itemOrigin, itemCarryCustom);

        TMResult<Item> result = itemCopyApi.itemCopy();
        if (!result.isOk) {
            String msg = StringUtils.isEmpty(result.msg) ? "返回结果为空！" : result.msg;
            return new TMResult(false, msg, itemOrigin);
        }

        Item itemNow = result.getRes();

        log.warn("success copy from [numIid: " + numIid + "] -> to [" + user.getUserNick() + "] id: " + itemNow.getNumIid());

        String itemUrl = "http://item.taobao.com/item.htm?id=" + itemNow.getNumIid();

        return new TMResult(true, itemUrl, itemOrigin);
    }
    public static TMResult doCarryForTaobao(long numIid, User user) {
        return doCarryForTaobao(null, numIid, null, null, null, user);
    }

    // 获取链接中的宝贝id
    public static Long getNumId(String url) {
        if (NumberUtils.isDigits(url)) {
            return NumberUtil.parserLong(url, 0l);
        }
    	
        Pattern pattern = Pattern.compile("[\\?&]id=(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String idParam = matcher.group(1);
            return NumberUtils.toLong(idParam);
        }

        return 0l;
    }

    public static void addOneFinishCnt(long taskId) {
        CarrierTask task = CarrierTask.findByTaskId(taskId);
        int finishCnt = task.getFinishCnt() + 1;
        task.setFinishCnt(finishCnt);
        if (task.getBabyCnt() <= finishCnt) {
            task.setStatus(CarrierTask.CarrierTaskStatus.finished);
        } else {
            task.setStatus(CarrierTask.CarrierTaskStatus.running);
        }
        log.error("addOneFinishCnt taskId " + task + " babyCnt: " + task.getBabyCnt() + " finishCnt: " + finishCnt);
        task.jdbcSave();
    }

    public static ItemCarryCustom getItemCarryCustom(Long subId, User user) {
        ItemCarryCustom itemCarryCustom;
        if (subId != null) {
            SubCarrierTask subCarrierTask = SubCarrierTask.findById(subId);
            if (subCarrierTask == null) return null;
            Long taskId = subCarrierTask.getTaskId();
            if (taskId == 0) return null;
            CarrierTask carrierTask = CarrierTask.findByTaskId(taskId);
            if (carrierTask == null) return null;
            long itemCarryCustomId = carrierTask.getItemCarryCustomId();
            if (itemCarryCustomId <= 0L) return null;
            itemCarryCustom = ItemCarryCustom.findById(carrierTask.getItemCarryCustomId());
        } else {
            itemCarryCustom = ItemCarryCustom.findByUserId(user.getIdlong());
        }

        return itemCarryCustom;
    }

    public static TMResult addBatchCarrierTask(User user, String content, String userNick, Long itemCarryCustomId) {

        List<BabyInfo> infos = getBabyInfoList(content);
        if (infos.size() >= 500) {
            return new TMResult(false, "单次复制任务不能超过500", null);
        }
        CarrierTask parentTask = new CarrierTask(userNick, CarrierTask.CarrierTaskType.batch, infos.size(), CarrierTask.CarrierTaskStatus.prepared, itemCarryCustomId);
        long id = parentTask.saveBatchTask();
        if (id <= 0L) {
            return new TMResult(false, "主任务添加失败", null);
        }

        SubCarrierTask.batchInsert(infos, id, 0, userNick, user.isTmall() ? SubCarrierTask.SubTaskType.天猫复制 : SubCarrierTask.SubTaskType.淘宝复制);

        return new TMResult(true, "添加成功", null);
    }
    
    public static TMResult addBatchCarrierTask1688(User user, String content, String userNick, Long itemCarryCustomId, Long cid, Long brand) {

        List<BabyInfo> infos = getBabyInfoList(content);
        if (infos.size() >= 500) {
            return new TMResult(false, "单次复制任务不能超过500", null);
        }
        CarrierTask parentTask = new CarrierTask(userNick, CarrierTask.CarrierTaskType.batch1688
        		, infos.size(), CarrierTask.CarrierTaskStatus.prepared, itemCarryCustomId);
        long id = parentTask.saveBatchTask();
        if (id <= 0L) {
            return new TMResult(false, "主任务添加失败", null);
        }

        SubCarrierTask.batchInsert(infos, id, 0, userNick, SubCarrierTask.SubTaskType.$1688复制,cid,brand);
//        BatchCarrierAction.doBactch();
        return new TMResult(true, "添加成功", null);
       
    }

    public static TMResult addShopBatchCarrierTask(User user, String ww, List items, String userNick, Long itemCarryCustomId) {
        // 对itemId去重
        Map itemsMap = new HashMap();
        if (items != null)
        for (Object object : items) {
            Map item = (Map) object;
            Object itemId = item.get("itemId");
            itemsMap.put(itemId, item);
        }
        if (itemsMap.isEmpty()) {
            return new TMResult(false, "未查询到可添加的宝贝", null);
        }
        // 获取当天提交主任务 获取当天店铺复制宝贝个数
        List<CarrierTask> taskShopCarryToday = CarrierTask.findTaskShopCarryToday(userNick);
        int countShopCarryToday = 0;
        for (CarrierTask carrierTask : taskShopCarryToday) {
            countShopCarryToday += carrierTask.getBabyCnt();
        }
        // 限制数量
        if (countShopCarryToday + itemsMap.size() > 500) {
            return new TMResult(false, "当天店铺复制宝贝个数不能超过500个 今日已添加复制个数：" + countShopCarryToday, null);
        }
        // 创建BabyInfo
        List<BabyInfo> babyInfos = new ArrayList<BabyInfo>();
        for (Object object : itemsMap.values()) {
            Map item = (Map) object;
            String itemId = (String) item.get("itemId");
            String babyTitle = (String) item.get("title");
            String picUrl = (String) item.get("image");
            babyInfos.add(new BabyInfo(itemId, babyTitle, picUrl));
        }
        // 添加主任务
        CarrierTask parentTask = new CarrierTask(ww, userNick, CarrierTask.CarrierTaskType.shop, babyInfos.size(), CarrierTask.CarrierTaskStatus.prepared, itemCarryCustomId);
        long id = parentTask.saveBatchTask();
        if (id <= 0L) {
            return new TMResult(false, "主任务添加失败", null);
        }
        // 添加子任务
        SubCarrierTask.batchInsert(babyInfos, id, 0, userNick, user.isTmall() ? SubCarrierTask.SubTaskType.天猫复制 : SubCarrierTask.SubTaskType.淘宝复制);

        return new TMResult(true, "添加成功", null);
    }

    public static List<BabyInfo> getBabyInfoList(String content) {
        List<BabyInfo> infos = new ArrayList<BabyInfo>();
        List<String> urls = Arrays.asList(content.split("\n"));
        for (String url : urls) {
            infos.add(new BabyInfo(url, null, null));
        }
        return infos;
    }

    public static void B2Bomber() {
        log.error("B2Bomber");
        log.error(NewProxyTools.proxyGet("http://vipqt3633.com/searchPun", "http://vipqt3633.com/"));
    }

    
    
}
