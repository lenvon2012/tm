package actions;

import actions.DiagAction.BatchResultMsg;
import bustbapi.RecommendItemApi;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.HtmlUtil;
import com.ciaosir.client.utils.NumberUtil;
import configs.TMConfigs;
import dao.item.ItemDao;
import job.apiget.UpdatePopularizedJob;
import models.associate.AssociateModel;
import models.associate.AssociatePlan;
import models.associate.AssociatedItems;
import models.item.ItemPlay;
import models.user.User;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.Cache;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static actions.TemplateAction.getItemDesc;
import static java.lang.String.format;

/**
 * 关联模板
 * 
 * @author hyg 2014-4-10下午8:41:24
 */
public class AssociateAction {

    private static final Logger log = LoggerFactory.getLogger(AssociateAction.class);

    public static final String TAG = "ASSOCIATEACTION";
    
    public static final int ZERO = 0;
    
    public static final int TYPEONE = 1;
    
    public static final int TYPETWO = 2;
    
    public static final int TYPETHREE = 3;
    
    public static final int TYPEFOUR = 4;

    public static List<AssociateModel> listAssociateModel(User user, int width, int maxNum, int type, int offset,
            int limit) {
        List<AssociateModel> listModels = new ArrayList<AssociateModel>();
        if (user == null) {
            return null;
        }
        listModels = AssociateModel.findModels(width, maxNum, type, offset, limit);
        if (listModels.isEmpty()) {
            return null;
        }
        return listModels;
    }

    public static AssociateModel findAssociateModel(Long modelId) {
        if (NumberUtil.isNullOrZero(modelId)) {
            return null;
        }
        AssociateModel model = AssociateModel.findModelBymodelId(modelId);
        return model;
    }
    
    /**
     * recommend template
     */
    public static String selectModel(Long modelId){
        File file = new File(TMConfigs.templateDir,modelId + ".html");
        Document document = null;
        try {
            document = Jsoup.parse(file, "UTF-8");
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        
        Elements templates = document.select(".template");
        
        String html = templates.get(0).outerHtml();
        return html;
    }
    
    public static List<ItemPlay> listItems(User user, int offset, int limit, String search,Long sellerCat,int sort) {
        
        //all the shop
        List<ItemPlay> items = ItemDao.findItemBySellerCat(user.getId(), offset,limit,search,sellerCat,sort);
        
        List<ItemPlay> newItems = new ArrayList<ItemPlay>();
        //需要將原價改成折後價格
        for(ItemPlay item:items){
            ItemPlay newItem = getNewItem(item, user);
            newItems.add(newItem);
        }
        
//        List<ItemPlay> itemsNew = new ArrayList<ItemPlay>();
        //同时需要知道这个宝贝中有几个计划
//        int count = AssociateAction.ZERO;
        
//        for(ItemPlay item:items){
//            count = AssociatedItems.getPlanCountByNumIid(item.numIid, user.getId());
//            item.setPlanCount(count);
//            itemsNew.add(item);
//        }
        return newItems;
    }
    
     public static List<ItemPlay> listItemsNoMinPrice(User user, int offset, int limit, String search,Long sellerCat,int sort) {
         
            //all the shop
            List<ItemPlay> items = ItemDao.findItemBySellerCat(user.getId(), offset,limit,search,sellerCat,sort);
            
            List<ItemPlay> itemsNew = new ArrayList<ItemPlay>();
            
            //同时需要知道这个宝贝中有几个计划
            int count = AssociateAction.ZERO;
            
            for(ItemPlay item:items){
                if(item != null){
                    count = AssociatedItems.getPlanCountByNumIid(item.numIid, user.getId());
                    item.setPlanCount(count);
                    itemsNew.add(item);
                }
            }
            return itemsNew;
    }
    
    
    
    /**
     * update or save
     */
    public static Long saveAssociatePlan(String itemIds, Long modelId, String planName, User user, String borderColor,
            String activityTitle, double activityPrice, double counterPrice, String activityNameChinese,
            String activityNameEnglish, int planWidth, double originalPrice, String fontColor,Long planId,
            String backgroundColor,int days,int hours,int minutes) {
        
        if (StringUtils.isEmpty(itemIds) || NumberUtil.isNullOrZero(modelId)) {
            return null;
        }
        
        AssociatePlan plan = newAssociatePlan(itemIds, modelId, planName, user, borderColor, activityTitle,
                activityPrice, counterPrice, activityNameChinese, activityNameEnglish, originalPrice, fontColor,backgroundColor,
                planWidth,days,hours,minutes);
        if(!NumberUtil.isNullOrZero(planId)){
            plan.setId(planId);
            //如果计划类型是1(已投放) type类型不改变
            List<AssociatedItems> itemsList = AssociatedItems.findByPlanId(planId, user.getId());
            if(itemsList.size() > 0){
                plan.setType(TYPEONE);
            }
        }
        boolean flag = plan.jdbcSave();
        if (flag) {
            return plan.getId();
        } else {
            log.error("[update or save associatePlan failed: ] planId=" + plan.getId());
            return null;
        }
    }

    private static AssociatePlan newAssociatePlan(String itemIds, Long modelId, String planName, User user,
            String borderColor, String activityTitle, double activityPrice, double counterPrice,
            String activityNameChinese, String activityNameEnglish, double originalPrice, String fontColor,String backgroundColor,
            int planWidth,int days,int hours,int minutes) {
        AssociatePlan plan = new AssociatePlan();
        plan.setModelId(modelId);
        plan.setPlanName(planName);
        plan.setNumIids(itemIds);
        plan.setType(TYPETWO);
        plan.setUserId(user.getId());
        plan.setActivityNameChinese(activityNameChinese);
        plan.setActivityNameEnglish(activityNameEnglish);
        plan.setActivityPrice(activityPrice);
        plan.setActivityTitle(activityTitle);
        plan.setBorderColor(borderColor);
        plan.setCounterPrice(counterPrice);
        plan.setFontColor(fontColor);
        plan.setOriginalPrice(originalPrice);
        plan.setPlanWidth(planWidth);
        plan.setBackgroundColor(backgroundColor);
        plan.setDays(days);
        plan.setHours(hours);
        plan.setMinutes(minutes);
        return plan;
    }

    public static List<BatchResultMsg>  addAssociatedItems(User user, String numIids,Long planId,Long modelId) {
        String getTemplateHtml = null;
        List<BatchResultMsg> msgList = new ArrayList<BatchResultMsg>();
        try {
            if (user == null) {
                return msgList;
            }
            AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
            
            String itemIids[] = plan.getNumIids().split("!@#");
            
            List<ItemPlay> items = new ArrayList<ItemPlay>();

            for (String id : itemIids) {
                ItemPlay item = ItemDao.findByNumIid(user.getId(), Long.parseLong(id));
                item = getNewItem(item, user);
                items.add(item);
            }

            getTemplateHtml = getTemplateHtml(modelId, items,planId,user.getId());
            
            Document doc = Jsoup.parse(getTemplateHtml);
            Elements itemBoxes = doc.select(".tmp_td_item");
            for (Element elem : itemBoxes) {
                if (elem.select(".tmp_td_item_id").attr("value") == ""
                        || elem.select(".tmp_td_item_id").attr("value") == null) {
                    elem.remove();
                }
            }
            getTemplateHtml = doc.outerHtml();
            // 全店铺关联
            if (numIids == null) {
                Set<Long> itemsSet = ItemDao.findNumIidsByUserStatus(user.getId());
                for(Long id:itemsSet){
                    msgList.add(addOneAssociatedItem(id, getTemplateHtml, user, planId));
                }
            }
            // 选择性关联
            else {
                String[] ids = numIids.split("!@#");
                for (String id : ids) {
                    msgList.add(addOneAssociatedItem(Long.parseLong(id), getTemplateHtml, user, planId));
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return msgList;
    }
    
	public static BatchResultMsg addOneAssociatedItem(Long numIid, String getTemplateHtml, User user, Long planId) {
		DiagAction.BatchResultMsg resultMsg = null;
		String desc = getItemDesc(user, numIid);
		
		if (StringUtils.isEmpty(desc)) {
			return null;
		}
		String newHtml = "";
		//如果有的话 replace
		if(isPlanIn(desc, planId)){
			String newDesc = replaceOneTmp(desc, getTemplateHtml, planId);
			newHtml = HtmlUtil.deleteBlank(newDesc);
		}else{
			if(user.isTmall()) {
				// 使用Jsoup的xml解析器解析desc
				Document descXml = Jsoup.parse(desc, "", Parser.xmlParser());
				// 修改打印格式为精简打印
				descXml.outputSettings(descXml.outputSettings().prettyPrint(false));
				// 获取排序value最小字段
				Integer minOrder = Integer.MAX_VALUE;
				Element minOrderValue = null;
				Elements values = descXml.select("field[id$=_mod_order] value");
				if (values.isEmpty()) return null;
				for (Element value : values) {
					Integer order = Integer.valueOf(value.html());
					if (order < minOrder) {
						minOrder = order;
						minOrderValue = value;
					}
				}
				// 获取排序最前的字段 在content前添加html
				Element orderField = minOrderValue.parent();
				Element contentField = orderField.parent().select("field[id$=_mod_content]").first();
				Element contentFieldValue = contentField.child(0);
				String content = contentFieldValue.html();
				String newContent = getTemplateHtml + content;
				contentFieldValue.html(newContent);

				newHtml = descXml.outerHtml();
			} else {
				newHtml = getTemplateHtml + desc;
			}
			newHtml = HtmlUtil.deleteBlank(newHtml);
		}

		resultMsg = TemplateAction.updateItemDesc(user, numIid, newHtml);

		boolean isPlanIn = isPlanIn(newHtml, planId);
		
		if (resultMsg.isOk() && isPlanIn) {
			// save associated item
			new AssociatedItems(user.getId(), numIid, planId).jdbcSave();
			// change the type to 1
			AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
			plan.setType(TYPEONE);
			plan.setId(planId);
			plan.jdbcSave();
			return resultMsg;
		} else {
			log.error("associate item failed:  [numId =] " + numIid + "[planId =] " + planId);
			return resultMsg;
		}
	}

    /**
     * @param desc 宝贝描述
     * @param planHtml 计划html
     * @param planId
     * @return
     */
    public static String replaceOneTmp(String desc,String planHtml,Long planId){
        String newDesc =  null;
        
        Document document = Jsoup.parse(desc);
        
        Document planDoc = Jsoup.parse(planHtml);
        
        Elements planElems = planDoc.select(".template");
        
        String newPlanHtml = planElems.first().html();
        
        Elements elements = document.select("a");

        for (Element element : elements) {
            if (("tbt_relation" + planId).equals(element.attr("name"))) { 
                element.parent().empty();
                element.parent().append(newPlanHtml);
                newDesc = document.outerHtml();
                break;
            }
        }
        return newDesc;
    }
    
    public static boolean isPlanIn(String newHtml,Long planId){
        boolean flag = false;
        Document document = Jsoup.parse(newHtml);
        Elements elements = document.select("a");
        for(Element element:elements){  
            if(("tbt_relation" + planId).equals(element.attr("name"))){
                flag = true;
            }
        }
        return flag;
    }
    

    public static String getTemplateHtml(Long modelId, List<ItemPlay> items, Long planId, Long userId) {
        String getTemplateHtml = null;
        File file = new File(TMConfigs.templateDir, modelId + ".html");
        Document document = null;
        try {
            document = Jsoup.parse(file, "UTF-8");
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            log.error("[faile to change the template dir to html......]" + "[modelId=]" + modelId + " [userId=]" + userId + " [planId=]" + planId);
            return null;
        }
        
        Elements templates = document.select(".template");
        
        // bind planId
        templates.get(0).select(".tzg_tag_name").first().attr("name", "tbt_relation" + String.valueOf(planId));

        Elements itemBoxes = templates.get(0).select(".tmp_td_item");
        int i = ZERO;
        
        for (ItemPlay item : items) {
            
            itemBoxes.get(i).select(".tmp_td_item_id").attr("value", String.valueOf(item.getNumIid()));
             
            log.info("" + itemBoxes.get(i).select(".tmp_td_item_img").outerHtml().indexOf("<img"));
            
            if(itemBoxes.get(i).select(".tmp_td_item_img").outerHtml().indexOf("<img") ==  0){
                itemBoxes.get(i).select(".tmp_td_item_img").attr("src",item.getPicURL());
            }else{
                itemBoxes.get(i).select(".tmp_td_item_img").attr("background",item.getPicURL());
            }
            
            itemBoxes.get(i).select(".tmp_td_item_title").html(item.getTitle());
            itemBoxes.get(i).select(".tmp_td_item_price").html(String.valueOf(item.getMinPrice()));
            itemBoxes.get(i).select(".tmp_td_item_origPrice").html(String.valueOf(item.getPrice()));
            itemBoxes.get(i).select(".tmp_td_item_href")
                    .attr("href", "http://item.taobao.com/item.htm?id=" + String.valueOf(item.getNumIid()));
            itemBoxes.get(i).select(".tmp_td_item_href").attr("title",item.getTitle());
            i++;
        }
        
        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(userId, planId);
        String activityNameChinese = plan.getActivityNameChinese();
        String activityNameEnglish = plan.getActivityNameEnglish();
        double activityPrice = plan.getActivityPrice();
        String activityTitle = plan.getActivityTitle();
        String borderColor = plan.getBorderColor();
        double counterPrice = plan.getCounterPrice();
        String fontColor = plan.getFontColor();
        double originalPrice = plan.getOriginalPrice();
        int planWidth = plan.getPlanWidth();
        String backgroundColor = plan.getBackgroundColor();
        int days = plan.getDays();
        int hours = plan.getHours();
        int minutes = plan.getMinutes();
        // String numIids = plan.getNumIids();
        // String planName = plan.getPlanName();
        // int type = plan.getType();
        if(planWidth > 0){
            if (!StringUtils.isEmpty(activityNameChinese)) {
                templates.get(0).select(".tmp_td_act_nam_chi").html(activityNameChinese);
            }
            if (!StringUtils.isEmpty(activityNameEnglish)) {
                templates.get(0).select(".tmp_td_act_nam_eng").html(activityNameEnglish);
            }
            if (activityPrice > 0) {
                templates.get(0).select(".tmp_td_act_pri").attr("value", String.valueOf(activityPrice));
            }
            if (!StringUtils.isEmpty(activityTitle)) {    
                templates.get(0).select(".tmp_td_act_tit").html(activityTitle);
            }
            if (counterPrice > 0) {
                templates.get(0).select(".tmp_td_cou_pri").attr("value", String.valueOf(counterPrice));
            }
            if (originalPrice > 0) {
                templates.get(0).select(".tmp_td_ori_pri").attr("value", String.valueOf(originalPrice));
            }
            if (StringUtils.isNotEmpty(borderColor)) {
                Elements borderColors = templates.get(0).select(".tmp_td_bor_col");
                modifyStyle(borderColors, borderColor,"color");
            }
            if (StringUtils.isNotEmpty(fontColor)) {
                Elements fontColors = templates.get(0).select(".tmp_td_fon_col");
                modifyStyle(fontColors, fontColor,"color");
            }
            //change the background color and font color at the same time
            if(StringUtils.isNotEmpty(backgroundColor)){
                Elements backgoundColors = templates.get(0).select(".tmp_td_bac_col");
                modifyStyle(backgoundColors, backgroundColor,"background-color");
                Elements bac_fon_Colors = templates.get(0).select(".tmp_td_bac_col_fon");
                if(bac_fon_Colors.size() > 0){
                    modifyStyle(bac_fon_Colors, backgroundColor, "color");
                }
            }
            if(days > 0){
                templates.get(0).select(".tmp_td_act_day").attr("value",String.valueOf(days));
            }
            if(hours > 0){
                templates.get(0).select(".tmp_td_act_hou").attr("value",String.valueOf(hours));
            }
            if(minutes > 0){
                templates.get(0).select(".tmp_td_act_min").attr("value",String.valueOf(minutes));
            }
        }
        getTemplateHtml = templates.get(0).outerHtml();

        return getTemplateHtml;
    }

    //none display
    public static void modifyStyle(Elements elems, String newColor,String flag) {
        String style = null;
        int first;
        int last;
        String color = null;
        for (Element el : elems) {
            style = el.attr("style");
            if (style.indexOf(flag) < 0) {
                style = style + flag + ":" + newColor + ";";
            } else {
                first = style.indexOf(flag + ":") + flag.length() + 1;
                last = style.indexOf(";", first);
                color = style.substring(first, last);
                style = style.replace(color, newColor);
            }
            el.attr("style", style);
        }
    }

	
    public static List<Map<String, AssociatePlan>> getPlanHtmls(User user, int type, int offset, int limit) {
        List<Map<String, AssociatePlan>> list = new ArrayList<Map<String, AssociatePlan>>();

        if (user == null) {
            return null;
        }
        List<AssociatePlan> planList = AssociatePlan.nativeQuery("userId = ? and type = ? order by id desc limit ?,?", user.getId(), type,offset, limit);
                
        for (AssociatePlan associatePlan : planList) {
            
            if(associatePlan.getType() == TYPEONE){
                //獲取count
                List<Long> listCount  = getAssociatedCount(associatePlan.getId(), user);
                if(listCount == null){
                    //TODO
                    AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), associatePlan.getId());
                    plan.setType(AssociateAction.TYPETHREE);
                    plan.setId(associatePlan.getId());
                    plan.jdbcSave();
                    listCount = new ArrayList<Long>();
                }
                associatePlan.setCount(listCount.size());
            }
            
            String[] numIidArr = associatePlan.getNumIids().split("!@#");
            
            List<ItemPlay> items = new ArrayList<ItemPlay>();

            for (String numIid : numIidArr) {
                ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), Long.parseLong(numIid));
                if(itemPlay == null){
                    ItemPlay item = new ItemPlay();
                    item.setNumIid(000000L);
                    //应该传入一个固定的地址
                    item.setPicURL("http://img04.taobaocdn.com/imgextra/i4/79742176/TB2a2G2XVXXXXaAXpXXXXXXXXXX-79742176.png");
                    item.setTitle("宝贝已经删除");
                    item.setPrice(0);
                    itemPlay = item;
                }
                
                //add minPrice
                itemPlay = getNewItem(itemPlay, user);
                items.add(itemPlay);
            }
            
            String html = getTemplateHtml(associatePlan.getModelId(), items, associatePlan.getId(),user.getId());
            
            Document doc = Jsoup.parse(html);
            Elements itemBoxes = doc.select(".tmp_td_item");
            modifyTemplate(itemBoxes);
            
            html = HtmlUtil.deleteBlank(doc.outerHtml());
            
            Map<String, AssociatePlan> map = new HashMap<String, AssociatePlan>();
            map.put(html, associatePlan);
            list.add(map);
        }
        return list;
    }
    
    public static String getPlanHtml(User user, Long planId) {
        if (user == null || planId == null) {
            return null;
        }
        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        String[] numIidsArr = plan.getNumIids().split("!@#");

        List<ItemPlay> items = new ArrayList<ItemPlay>();

        //add minPrice
        for (String numIid : numIidsArr) {
            ItemPlay item = ItemDao.findByNumIid(user.getId(), Long.parseLong(numIid));
            if(item == null){
                item = new ItemPlay();
                item.setPicURL("http://img04.taobaocdn.com/imgextra/i4/79742176/TB2a2G2XVXXXXaAXpXXXXXXXXXX-79742176.png");
                item.setPrice(0L);
                item.setMinPrice(0L);
                item.setTitle("此宝贝已经删除");
                item.setNumIid(Long.parseLong(numIid));
            }else{
                item = getNewItem(item, user);
            }
            items.add(item);
        }

        Long modelId = plan.getModelId();
        String html = getTemplateHtml(modelId, items, planId, user.getId());
        Document doc = Jsoup.parse(html);
        Elements itemBoxes = doc.select(".tmp_td_item");
        modifyTemplate(itemBoxes);
        html = doc.outerHtml();
        return html;
    }
    
    public static ItemPlay getNewItem(ItemPlay item,User user){
        double minPrice = UpdatePopularizedJob.getItemSkuMinPriceWithCache(user, item);
        if(minPrice > 0){
//            item.setPrice(minPrice);
            item.setMinPrice(minPrice);
        }else{
            item.setMinPrice(item.getPrice());
        }
        return item;
    }
    
    public static void modifyTemplate(Elements elems){
        for (Element elem : elems) {
            if (elem.select(".tmp_td_item_id").attr("value") == ""
                    || elem.select(".tmp_td_item_id").attr("value") == null) {
                String style = null;
                String flag = "display";
                String newStyle = "none";
                int last;
                String color = null;
                style = elem.attr("style");
                if (style.indexOf(flag) < 0) {
                    style = style + flag + ":" + newStyle + ";";
                } else {
                    last = style.indexOf(";", style.indexOf(flag + ":") + flag.length() + 1);
                    color = style.substring(style.indexOf(flag + ":") + flag.length() + 1, last);
                    style = style.replace(color, newStyle);
                }
                elem.attr("style", style);
            }
        }
    }
    
    
    public static List<BatchResultMsg> stopPlanId(User user, Long planId) {

        List<BatchResultMsg> list = new ArrayList<BatchResultMsg>();
        
        if (user == null || NumberUtil.isNullOrZero(planId)) {
            return list;
        }
        
        List<AssociatedItems> itemsList = AssociatedItems.findByPlanId(planId, user.getId());
        
        int count = ZERO;
        
        DiagAction.BatchResultMsg resultMsg = null;
        
        for (AssociatedItems item : itemsList) {
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), item.getNumIid());
            if(itemPlay == null){
                AssociatedItems assoItem = new AssociatedItems();
                boolean flag = assoItem.rawDelete(user.getId(), item.getNumIid(), planId);
                if(flag){
                    resultMsg = new BatchResultMsg(true,null,null,null,null);
                }else{
                    resultMsg = new BatchResultMsg(false,null,null,"无效宝贝删除失败",null);
                }
            }else{
                resultMsg = delPlanIdInOneItem(item.getNumIid(), user, planId);
            }
            list.add(resultMsg);
            if(resultMsg.isOk()){
                count ++;
            }else{
                log.error("[delete planId in one item failed...]" + "[itemId=]" + item.getNumIid() + " [planId=]" + planId);
            }
        }
        if(count == itemsList.size()){
            // change the type to 3
            AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
            plan.setType(TYPETHREE);
            plan.setId(planId);
            plan.jdbcSave();
        }
        return list;
    }
    
    
    public static BatchResultMsg delPlanIdInOneItem(Long itemId,User user,Long planId){
        
        //TODO 如果itemId 不存在 宝贝被删除
        String itemDesc = getItemDesc(user, itemId);
        BatchResultMsg resultMsg = null;
        if(itemDesc == null){
            return new BatchResultMsg(false,null,null,"宝贝已被删除",null);
        }
        
        Document document = Jsoup.parse(itemDesc, "", Parser.xmlParser());
        document.outputSettings(document.outputSettings().prettyPrint(false));
        
        Elements elements = document.select("a");
        
        for(Element element:elements){  
            if(("tbt_relation" + planId).equals(element.attr("name"))){
                element.parent().remove();
                break;
            }
        }
        
        String html = HtmlUtil.deleteBlank(document.html());

        resultMsg = TemplateAction.updateItemDesc(user, itemId, html);
        
        if(resultMsg.isOk()){
            //delete associated item
            new AssociatedItems().rawDelete(user.getId(), itemId, planId);
        }
        else{
            log.error("[delete planId in one item failed......]" + "[itemId=]" + itemId + "  [planId=]" + planId);
        }
        return resultMsg;
    }
    
    
    public static List<BatchResultMsg> batchDelPlanId(String numIids,User user,Long planId){
        String itemIids[] = numIids.split("!@#");
        List<BatchResultMsg> resultList = new ArrayList<DiagAction.BatchResultMsg>();
        for(String itemId :itemIids){
            resultList.add(delPlanIdInOneItem(Long.valueOf(itemId), user, planId));
        }
        return resultList;
    }
    
    public static List<ItemPlay> getAssociatedItemsByPlanId(Long planId,User user){
        
        if(NumberUtil.isNullOrZero(planId) || user == null){
            return null;
        }
        
        List<AssociatedItems> itemList = AssociatedItems.findByPlanId(planId, user.getId());
        
        List<ItemPlay> list = new ArrayList<ItemPlay>();
        
        for(AssociatedItems item:itemList){
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), item.getNumIid());
            if(itemPlay != null){
                list.add(itemPlay);
            }
        }
        return list;
    }
    
    public static List<Long> getAssociatedCount(Long planId,User user){
        if(NumberUtil.isNullOrZero(planId) || user == null){
            return null;
        }
        
        List<AssociatedItems> itemList = AssociatedItems.findByPlanId(planId, user.getId());
        
        List<Long> list = new ArrayList<Long>();
        for(AssociatedItems item:itemList){
            if(item != null){
                list.add(item.getNumIid());
            }
        }
        List<Long> numList = null;
        
        if(list.size() == ZERO){
            return numList;
        }
        
        numList = ItemDao.associatedCount(user.getId(), list);
        return numList;
    }
    
    /**
     * save planId then toPut
     */
    public static List<BatchResultMsg> putPlanAll(Long planId, User user) {
            
        List<BatchResultMsg> resultMsgs = new ArrayList<BatchResultMsg>();
        
        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        
        Long modelId = null;
//        boolean flag = true;
        if(plan == null){
            return null;
        }
//        if(plan.getType() == TYPEONE){
//            flag = false;
//            //TODO
//            resultMsgs = stopPlanId(user, planId);
//            //if stopAll
//            if(AssociatePlan.findByUserAndPlanId(user.getId(), planId).getType() == TYPETHREE){
//                flag = true;
//            }
//        }
//        if(flag){
            modelId = plan.getModelId();
            resultMsgs = AssociateAction.addAssociatedItems(user, null, planId, modelId);
//        }
        return resultMsgs;

//        List<AssociatedItems> listItems = AssociatedItems.findByPlanId(planId, user.getId());
//        
//        
//        
//        //模板计划中直接投放，不需要修改
//        if(AssociateAction.ZERO == flag && !NumberUtil.isNullOrZero(planId)){
//            plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
//            if(plan == null){
//                return null;
//            }
//            else{
//                modelId = plan.getModelId();
//            }
//            resultMsgs = AssociateAction.addAssociatedItems(user, null, planId, modelId);
//        }
//        else{
//            if (CommonUtils.isEmpty(listItems)) {
//                resultMsgs = AssociateAction.addAssociatedItems(user, null, planId, modelId);
//            } 
//            else if (!CommonUtils.isEmpty(listItems)) {
//                AssociateAction.stopPlanId(user, planId);
//                resultMsgs = AssociateAction.addAssociatedItems(user, null, planId, modelId);
//            } 
//        }
//        return resultMsgs;
    }

    /**
     * @param AssociateIds  associatedItems
     */
    public static List<BatchResultMsg> putPlanCheched(String AssociateIds,Long planId, User user) {
            
        List<BatchResultMsg> resultMsgs = new ArrayList<BatchResultMsg>();

        AssociatePlan plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
        
        Long modelId = null;
        
//        boolean flag = true;
        
        if(plan == null || StringUtils.isEmpty(AssociateIds)){
            return resultMsgs;
        }
        //if planId-->type =1 stop delete first;
//        if(plan.getType() == TYPEONE){
//            //TODO
//            flag = false;
//            
//            resultMsgs = stopPlanId(user, planId);
//            
//            //if stopAll
//            if(AssociatePlan.findByUserAndPlanId(user.getId(), planId).getType() == TYPETHREE){
//                flag = true;
//            }
//        }
//        if(flag){
            modelId = plan.getModelId();
            resultMsgs = AssociateAction.addAssociatedItems(user, AssociateIds, planId, modelId);
//        }
        return resultMsgs;
    }
//      //模板计划中直接投放，不需要修改
//        if(AssociateAction.ZERO == flag && !NumberUtil.isNullOrZero(planId)){
//            plan = AssociatePlan.findByUserAndPlanId(user.getId(), planId);
//            if(plan == null){
//                return null;
//            }else{
//                modelId = plan.getModelId();
//            }
//            resultMsgs = AssociateAction.addAssociatedItems(user, AssociateIds, planId, modelId);
//        }
//        
//        //未在投放中
//        if (CommonUtils.isEmpty(listItems)) {
//            resultMsgs = AssociateAction.addAssociatedItems(user, AssociateIds, planId, modelId);
//        } 
//        //投放中 
//        else if (!CommonUtils.isEmpty(listItems)) {
//            AssociateAction.stopPlanId(user, planId);
//            resultMsgs = AssociateAction.addAssociatedItems(user, AssociateIds, planId, modelId);
//        }
//        return resultMsgs;
//    }
    
    /**
     *up to 12 salesCount Desc 
     */
    public static List<ItemPlay> getRecommend(Long numIid, User user) {

        log.info(format("recommendItems:userId, numIid".replaceAll(", ", "=%s, ") + "=%s", user.getId(), numIid));

        Long count = 12L;
        
        List<ItemPlay> recommendList = (List<ItemPlay>) Cache.get(TAG + "recommendItems" + numIid);
        
        if (recommendList != null) {
            return recommendList;
        }

        recommendList = new ArrayList<ItemPlay>();

        RecommendItemApi.getRecommend api = new RecommendItemApi.getRecommend(user, numIid, count);
        
        recommendList = api.call();
        
        if(!CommonUtils.isEmpty(recommendList)){
            List<ItemPlay> newItems = new ArrayList<ItemPlay>();
            for(ItemPlay item:recommendList){
                item = getNewItem(item, user);
                newItems.add(item);
            }
            recommendList = newItems;
            String key = TAG + "recommendItems" + numIid;
            Cache.set(key, recommendList, "20min");
        }else{
            recommendList = new ArrayList<ItemPlay>();
        }
        return recommendList;
    }    
    
    
    /**
     *  刪除店鋪內所有模板
     * (*补救)防止刪除模板出錯 谨慎使用   
     */
    public static List<BatchResultMsg> removeAllPlanInOneShop(User user){
        
        List<ItemPlay> items = ItemDao.findOnSaleByUserId(user.getId());
        
        List<BatchResultMsg> msgs = new ArrayList<DiagAction.BatchResultMsg>();
        for(ItemPlay item:items){
            BatchResultMsg resultMsg = null;
            String itemDesc = getItemDesc(user,item.getNumIid());
            
            if(itemDesc == null){
                resultMsg = new BatchResultMsg(false,null,null,"宝贝已被删除",null);
            }
            
            Document document = Jsoup.parse(itemDesc);
            
            Elements elements = document.select("a");
            
            for(Element element:elements){  
                if(element.attr("name").indexOf("tbt_relation") >= 0){
                    element.parent().remove();
                }
            }
            String html = HtmlUtil.deleteBlank(document.html());
            
            resultMsg = TemplateAction.updateItemDesc(user, item.getNumIid(), html);
            
            msgs.add(resultMsg);
        }
        return msgs;
    }
    
    /**
     *  刪除店鋪內某一個模板
     * (*补救)防止刪除模板出錯 谨慎使用   
     */
    public static List<BatchResultMsg> removeOnePlanAllShop(User user,Long planId){
        List<ItemPlay> items = ItemDao.findOnSaleByUserId(user.getId());
        
        List<BatchResultMsg> msgs = new ArrayList<DiagAction.BatchResultMsg>();
        for(ItemPlay item:items){
            BatchResultMsg resultMsg = null;
            String itemDesc = getItemDesc(user,item.getNumIid());
            
            if(itemDesc == null){
                resultMsg = new BatchResultMsg(false,null,null,"宝贝已被删除",null);
            }
            
            Document document = Jsoup.parse(itemDesc, "", Parser.xmlParser());
            
            Elements elements = document.select("a");
            
            for(Element element:elements){  
                if(("tbt_relation" + planId).equals(element.attr("name"))){
                    element.parent().remove();
                }
            }
            String html = HtmlUtil.deleteBlank(document.html());
            
            resultMsg = TemplateAction.updateItemDesc(user, item.getNumIid(), html);
            
            msgs.add(resultMsg);
        }
        return msgs;
    }
    
    /**
     *  刪除某個寶貝中某個模板
     * (*补救)防止刪除模板出錯 谨慎使用   
     */
    public static BatchResultMsg removeOnePlanOneItem(User user,Long planId,Long numIid){
        BatchResultMsg resultMsg = null;
        String itemDesc = getItemDesc(user,numIid);
        
        if(itemDesc == null){
            resultMsg = new BatchResultMsg(false,null,null,"宝贝已被删除",null);
        }
        
        Document document = Jsoup.parse(itemDesc);
        
        Elements elements = document.select("a");
        
        for(Element element:elements){  
            if(("tbt_relation" + planId).equals(element.attr("name"))){
                element.parent().remove();
            }
        }
        String html = HtmlUtil.deleteBlank(document.html());
        
        resultMsg = TemplateAction.updateItemDesc(user, numIid, html);
        
        return resultMsg;
    }
    
    
    public static BatchResultMsg removeOnePlanOneItemNoTag(User user,Long numIid){
        BatchResultMsg resultMsg = null;
        String itemDesc = getItemDesc(user,numIid);
        if(itemDesc == null){
            resultMsg = new BatchResultMsg(false,null,null,"宝贝已被删除",null);
        }
        Document document = Jsoup.parse(itemDesc);
        
        Elements elements = document.select(".template");
        
        for(Element element:elements){
            element.remove();
        }
        String html = HtmlUtil.deleteBlank(document.html());
        
        resultMsg = TemplateAction.updateItemDesc(user, numIid, html);
        
        return resultMsg;
    }
    
    
    public static List<BatchResultMsg> removeAllPlanAllItem(User user){
        
        List<ItemPlay> items = ItemDao.findOnSaleByUserId(user.getId());
        
        List<BatchResultMsg> msgs = new ArrayList<DiagAction.BatchResultMsg>();
        
        for(ItemPlay item:items){
            BatchResultMsg resultMsg = null;
            String itemDesc = getItemDesc(user,item.getNumIid());
            
            if(itemDesc == null){
                resultMsg = new BatchResultMsg(false,null,null,"宝贝已被删除",null);
            }
            
            Document document = Jsoup.parse(itemDesc, "", Parser.xmlParser());
            
            Elements elements = document.select(".template");
            
            for(Element element:elements){  
                element.remove();
            }
            
            String html = HtmlUtil.deleteBlank(document.html());
            
            resultMsg = TemplateAction.updateItemDesc(user, item.getNumIid(), html);
            
            msgs.add(resultMsg);
        }
        return msgs;
    }
    
}
