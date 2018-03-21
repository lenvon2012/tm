package actions;

import actions.DiagAction.BatchResultMsg;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.HtmlUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.jd.open.api.sdk.internal.util.StringUtil;
import configs.TMConfigs;
import dao.item.ItemDao;
import job.apiget.UpdatePopularizedJob;
import job.message.UpdateTmplJob;
import models.group.*;
import models.item.ItemPlay;
import models.user.User;
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

public class GroupAction {

    private static Logger log = LoggerFactory.getLogger(GroupAction.class);

    private static final String TAG = "GROUPACTION";

    public static int ZERO = 0;

    public static int ONE = 1;

    public static int TWO = 2;

    public static int THREE = 3;

    public static int FOUR = 4;

    public static int FIVE = 5;

    public static int SIX = 6;

    public static String priceDesc = "priceDesc";

    public static String priceAsc = "priceAsc";

    public static String sellDesc = "sellDesc";

    public static String sellAsc = "sellAsc";

    public static String sortNormal = "sortNormal";

    // 直接展现的是html而不是图片 和关联不同
    public static List<Map<String, GroupModel>> listFavoriteModels(User user, int offSet, int limit) {

        List<Map<String, GroupModel>> favList = new ArrayList<Map<String, GroupModel>>();

        List<Long> favModelIds = FavoriteModel.getFavModels(user.getId(), offSet, limit);

        // modelId-->GroupModel modelId-->template
        for (Long modelId : favModelIds) {
            GroupModel groupModel = GroupModel.getSingleGroupModel(modelId);
            boolean inStore = FavoriteModel.inStore(user.getId(), modelId);
            groupModel.setFavorite(inStore);
            String html = GroupAction.templateHtml(modelId, "Default");
            Map<String, GroupModel> favMap = new HashMap<String, GroupModel>();
            favMap.put(html, groupModel);
            favList.add(favMap);
        }
        return favList;
    }

    private static String LIST_MODEL_CACHE_PRE = "LIST_MODEL_CACHE_PRE_";

    public static List<Map<String, GroupModel>> listModels(User user, int offSet, int limit) {

        List<Map<String, GroupModel>> modelList = (List<Map<String, GroupModel>>) Cache.get(LIST_MODEL_CACHE_PRE + offSet + "_" + limit);

        if (CommonUtils.isEmpty(modelList)) {

            modelList = new ArrayList<Map<String, GroupModel>>();

            List<GroupModel> models = GroupModel.getGroupModels(offSet, limit);

            for (GroupModel model : models) {

                String html = GroupAction.templateHtml(model.getModelId(), "Default");

                Map<String, GroupModel> modelMap = new HashMap<String, GroupModel>();

                modelMap.put(html, model);

                modelList.add(modelMap);
            }
            Cache.set(LIST_MODEL_CACHE_PRE + offSet + "_" + limit, modelList, "24h");
        }

        for (Map<String, GroupModel> map : modelList) {
            Set<Map.Entry<String, GroupModel>> entrySet = map.entrySet();
            Iterator<Map.Entry<String, GroupModel>> it = entrySet.iterator();
            GroupModel model = it.next().getValue();
            boolean inStore = FavoriteModel.inStore(user.getId(), model.getModelId());
            model.setFavorite(inStore);
        }
        return modelList;
    }

    /**
     * 只能选择颜色和宽度 而二者只有一个是可选的
     *
     * @param modelId
     * @param
     * @return
     */

    public static String templateHtml(Long modelId, String type) {

        GroupModel model = GroupModel.getSingleGroupModel(modelId);
        if (model == null) {
            return null;
        }
        File file = new File(TMConfigs.groupTemplateDir, modelId + ".html");
        Document document = null;
        try {
            document = Jsoup.parse(file, "UTF-8");
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            log.error("[faile to change the template dir to html......]" + "[modelId=]" + modelId);
            return null;
        }
        String templateHtml = null;

        if ("Default".equals(type)) {
            templateHtml = document.select(".template").first().outerHtml();
        } else {
            Elements elems = document.select(".template");
            for (Element elem : elems) {
                if (elem.attr("data").equals(type)) {
                    templateHtml = elem.outerHtml();
                }
            }

        }
        return templateHtml;
    }

    public static boolean doFavorite(Long modelId, User user) {
        boolean inStore = FavoriteModel.inStore(user.getId(), modelId);
        if (inStore) {
            return false;
        }
        FavoriteModel favModel = new FavoriteModel();
        favModel.setModelId(modelId);
        favModel.setUserId(user.getId());
        boolean flag = favModel.rawInsert();
        return flag;
    }

    public static boolean favoriteCancel(Long modelId, User user) {
        GroupModel model = GroupModel.getSingleGroupModel(modelId);
        if (model == null) {
            return false;
        }
        FavoriteModel favModel = new FavoriteModel();
        boolean flag = favModel.rawDelete(user.getId(), modelId);
        return flag;
    }

    public static Map<String, GroupModel> selectModelAction(Long modelId, String type) {
        Map<String, GroupModel> map = new HashMap<String, GroupModel>();
        String tmpHtml = templateHtml(modelId, type);

        // modify tmpHtml
        try {
            Document doc = Jsoup.parse(tmpHtml);
            Elements itemBoxes = doc.select(".group_item");
            for (Element elem : itemBoxes) {

                if (elem.select(".group_image").outerHtml().indexOf("<img") == 0) {
                    elem.select(".group_image").attr("src",
                            "http://img02.taobaocdn.com/imgextra/i2/79742176/TB2V4YNXVXXXXcgXXXXXXXXXXXX-79742176.png");
                } else {
                    elem.select(".group_image").attr("background",
                            "http://img02.taobaocdn.com/imgextra/i2/79742176/TB2V4YNXVXXXXcgXXXXXXXXXXXX-79742176.png");
                }

                elem.select(".group_image").attr("data", "");
                elem.select(".group_title").html("");
                elem.select(".group_price").html("");
                elem.select(".group_minPrice").html("");
                elem.select(".group_discount").html("");
                elem.select(".group_save").html("");
            }
            tmpHtml = HtmlUtil.deleteBlank(doc.outerHtml());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            tmpHtml = "";
        }

        GroupModel groupModel = GroupModel.getSingleGroupModel(modelId);
        map.put(tmpHtml, groupModel);
        return map;
    }

    public static ItemPlay getRecommendItem(User user) {
        List<ItemPlay> itemList = ItemDao.findByUserIdOnsaleDesc(user.getId(), GroupAction.ONE);
        if (itemList == null) {
            return null;
        }
        ItemPlay item = itemList.get(0);
        double minPrice = UpdatePopularizedJob.getItemSkuMinPrice(user, item);

        item.setMinPrice(minPrice);
        return item;
    }

    public static Long savePlan(Long planId, String planName, Long userId, Long modelId, String type,
                                String activityTitle, String label, String btnName, String originalPriceName, String currentPriceName, int days,
                                int hours, int minutes) {
        GroupPlan plan = new GroupPlan();
        plan.setPlanName(planName);
        plan.setUserId(userId);
        plan.setModelId(modelId);
        plan.setType(type);
        plan.setActivityTitle(activityTitle);
        plan.setLabel(label);
        plan.setBtnName(btnName);
        plan.setOriginalPriceName(originalPriceName);
        plan.setCurrentPriceName(currentPriceName);
        plan.setDays(days);
        plan.setHours(hours);
        plan.setMinutes(minutes);

        if (!NumberUtil.isNullOrZero(planId)) {
            plan.setId(planId);
        }
        boolean flag = plan.jdbcSave();
        if (flag) {
            return plan.getId();
        } else {
            log.error("[update or save groupPlan failed: ] planId=" + plan.getId() + "  usrId=" + userId);
            return null;
        }
    }

    public static List<ItemPlay> listItems(User user, int offset, int limit, String search, Long sellerCid, String sort) {
        int sortFlag = ZERO;

        if (priceDesc.equals(sort)) {
            sortFlag = TWO;
        } else if (priceAsc.endsWith(sort)) {
            sortFlag = THREE;
        } else if (sellDesc.equals(sort)) {
            sortFlag = ZERO;
        } else if (sellAsc.equals(sort)) {
            sortFlag = ONE;
        }

        List<ItemPlay> itemsList = ItemDao.findItemBySellerCat(user.getId(), offset, limit, search, sellerCid, sortFlag);
        //增加投放数量
        List<ItemPlay> items = new ArrayList<ItemPlay>();
        for (ItemPlay item : itemsList) {
            int count = GroupedItems.getPlanCountByNumIids(item.getNumIid(), user.getId());
            item.setPlanCount(count);
            items.add(item);

        }
        return items;
    }

    public static ItemPlay getItem(Long numIid, User user) {

        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);

        if (item == null) {
            item = new ItemPlay();
        } else {
            double minPrice = UpdatePopularizedJob.getItemSkuMinPriceWithCache(user, item);
            item.setMinPrice(minPrice);
        }
        return item;
    }

    public static Long saveItemProp(Long planId, User user, String itemString) {
        // 需要解析進來的json？ 传啥存啥 TODO
        Long id = null;
        GroupPlan plan = new GroupPlan();
        plan.setItemString(itemString);
        plan.setId(planId);
        plan.setUserId(user.getId());
        boolean flag = plan.itemPropUpdate();
        if (flag) {
            id = plan.getId();
        }
        return id;
    }

    public static boolean putAll(Long planId, User user) {
        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        if (plan == null) {
            return false;
        }
        Set<Long> numIids = ItemDao.findNumIidsByUserStatus(user.getId());
        //save first
        if (numIids == null) {
            return false;
        }
        boolean flag = put(plan, numIids, user);
        return flag;
    }

    public static List<BatchResultMsg> putAllNoQueue(Long planId, User user) {

        List<BatchResultMsg> resultMsgs = new ArrayList<DiagAction.BatchResultMsg>();

        Set<Long> numIids = ItemDao.findNumIidsByUserStatus(user.getId());

        boolean flag = saveAllItems(numIids, planId, user.getId());

        //get numIids from database
        List<Long> numIidsList = GroupedItems.findNumIidsByPlanIdAndStatus(user.getId(), planId, ONE);

        GroupQueue gq = new GroupQueue();
        gq.setNumIids(numIidsList);
        gq.setPlanId(planId);
        gq.setUser(user);

        resultMsgs = insertOnePlan(gq);

        return resultMsgs;
    }

    public static boolean putChecked(Long planId, String numIidsString, User user) {
        GroupPlan plan = new GroupPlan().getGroupPlan(planId, user);
        if (plan == null) {
            return false;
        }
        String[] numIidsStr = numIidsString.split("!@#");
        Set<Long> numIids = new HashSet<Long>();
        for (int i = 0; i < numIidsStr.length; i++) {
            ItemPlay item = ItemDao.findByNumIid(user.getId(), Long.valueOf(numIidsStr[i]));
            if (item != null) {
                numIids.add(Long.valueOf(numIidsStr[i]));
            }
        }
        boolean flag = put(plan, numIids, user);
        return flag;
    }

    public static List<BatchResultMsg> putCheckedNoQueue(Long planId, String numIidsString, User user) {
        List<BatchResultMsg> resultMsgs = new ArrayList<DiagAction.BatchResultMsg>();
        GroupPlan plan = new GroupPlan().getGroupPlan(planId, user);
        if (plan == null) {
            return null;
        }
        String[] numIidsStr = numIidsString.split("!@#");
        Set<Long> numIids = new HashSet<Long>();
        for (int i = 0; i < numIidsStr.length; i++) {
            ItemPlay item = ItemDao.findByNumIid(user.getId(), Long.valueOf(numIidsStr[i]));
            if (item != null) {
                numIids.add(Long.valueOf(numIidsStr[i]));
            }
        }

        boolean flag = saveAllItems(numIids, planId, user.getId());
        if (flag) {
            //get numIids from database;
            List<Long> numIidsList = GroupedItems.findNumIidsByPlanIdAndStatus(user.getId(), planId, ONE);
            GroupQueue gq = new GroupQueue();
            gq.setNumIids(numIidsList);
            gq.setPlanId(planId);
            gq.setUser(user);
            resultMsgs = insertOnePlan(gq);
        }
        return resultMsgs;
    }

    public static boolean put(GroupPlan plan, Set<Long> numIids, User user) {

        boolean flag = saveAllItems(numIids, plan.getId(), user.getId());

        //get numIids from database
        List<Long> numIidsList = GroupedItems.findNumIidsByPlanIdAndStatus(user.getId(), plan.getId(), ONE);
        GroupQueue gq = new GroupQueue();
        gq.setNumIids(numIidsList);
        gq.setPlanId(plan.getId());
        gq.setUser(user);
        gq.setType(ONE);
        UpdateTmplJob.addPlanId(gq);

        //將狀態改爲5 正在投放...
        plan.setStatus(FIVE);
        plan.statusUpdate();
        return flag;
    }

    /**
     * 保存所有將要投放的寶貝 status=1
     *
     * @return
     */
    public static boolean saveAllItems(Set<Long> numIids, Long planId, Long userId) {
        if (numIids == null) {
            return false;
        }
        for (Long numIid : numIids) {
            GroupedItems item = GroupedItems.findOneGroupedItemStatus(userId, planId, numIid);
            item.setPlanId(planId);
            item.setNumIid(numIid);
            item.setUserId(userId);
            item.setStatus(ONE);
            item.jdbcSave();
        }
        return true;
    }

    /**
     * 插入一個計劃  可包含多個numIids
     */
    public static List<BatchResultMsg> insertOnePlan(GroupQueue gq) {

        List<BatchResultMsg> resultMsgs = new ArrayList<DiagAction.BatchResultMsg>();

        GroupPlan plan = new GroupPlan().getGroupPlan(gq.getPlanId(), gq.getUser());

        String html = getPlanHtml(gq.getUser().getId(), plan);

        for (Long numIid : gq.getNumIids()) {
            BatchResultMsg result = insertOneTmp(numIid, html, gq.getUser(), gq.getPlanId());
            resultMsgs.add(result);
        }

        plan.setStatus(ONE);
        plan.statusUpdate();
//        for(BatchResultMsg result:resultMsgs){
//            if(result.isOk()){
//                plan.setStatus(ONE);
//                plan.statusUpdate();
//                break;
//            }
//        }
//        
//        plan.setStatus(TWO);
//        plan.statusUpdate();

        return resultMsgs;
    }

    /**
     * 插入一個寶貝  * 必须判断原本计划中是否有这个计划，如果有的话，取代原先的计划
     */
    public static BatchResultMsg insertOneTmp(Long numIid, String html, User user, Long planId) {

        BatchResultMsg resultMsg = null;
        String newHtml = null;
        boolean isPlan = false;

        GroupedItems groupedItem = new GroupedItems();

        String desc = getItemDesc(user, numIid);
        // 在原宝贝描述前添加一段html
        if (isPlanIn(desc, planId)) { // 原宝贝的描述里已经添加过html
            String newDesc = replaceOneTmp(desc, html, planId);
            newHtml = newDesc;
        } else { // // 原宝贝的描述里未添加过html
            if (user.isTmall()) { // 天猫用户

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
                String newContent = TemplateAction.escape(html) + content;
                contentFieldValue.html(newContent);

                newHtml = descXml.outerHtml();

            } else { // 淘宝用户
                newHtml = html + desc;
            }
        }

        isPlan = isPlanIn(newHtml, planId);

        resultMsg = TemplateAction.updateItemDesc(user, numIid, newHtml);

        groupedItem = GroupedItems.findOneGroupedItem(user.getId(), planId, numIid);

        if (resultMsg.isOk() && isPlan) {
            // change the status 2;
            groupedItem.setStatus(TWO);
            groupedItem.jdbcSave();

        } else {
            // change the status 3;
            groupedItem.setStatus(THREE);
            groupedItem.setErrorMsg(resultMsg.getMsg());
            groupedItem.jdbcSave();
            log.error("grouped item failed:  [numIid =] " + numIid + "[planId =] " + planId);
        }
        return resultMsg;
    }


    /**
     * @param desc     宝贝描述
     * @param planHtml 计划html
     * @param planId
     * @return
     */
    public static String replaceOneTmp(String desc, String planHtml, Long planId) {
        String newDesc = null;

        Document document = Jsoup.parse(desc);

        Document planDoc = Jsoup.parse(planHtml);

        Elements planElems = planDoc.select(".template");

        String newPlanHtml = planElems.first().html();

        Elements elements = document.select("a");

        for (Element element : elements) {
            if (("tbt_group_relation" + planId).equals(element.attr("name"))) {
                element.parent().empty();
                element.parent().append(newPlanHtml);
                newDesc = document.outerHtml();
                break;
            }
        }
        return newDesc;
    }

    /**
     * 点击暂停的事件 未刪除
     */
    public static boolean stopOnePlan(Long planId, User user) {

        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        if (plan == null) {
            return false;
        }
        //不一定是成功的吧，什么投放失败，插入失败的一旦点击取消 全给你干掉
//        List<Long> numIidsList = GroupedItems.findNumIidsSucceed(user.getId(), planId);
        List<Long> numIidsList = GroupedItems.findNumIidsByPlanId(user.getId(), planId);
        if (numIidsList == null) {
            return false;
        }
        GroupQueue gq = new GroupQueue();
        gq.setNumIids(numIidsList);
        gq.setPlanId(planId);
        gq.setUser(user);
        gq.setType(TWO);

        UpdateTmplJob.addPlanId(gq);

        plan.setStatus(SIX);
        plan.statusUpdate();

        return true;
    }

    public static List<BatchResultMsg> stopOnePlanNoQueue(Long planId, User user) {

        List<BatchResultMsg> results = new ArrayList<DiagAction.BatchResultMsg>();

        List<Long> numIidsList = GroupedItems.findNumIidsByPlanIdAndStatus(user.getId(), planId, TWO);

        GroupQueue gq = new GroupQueue();
        gq.setNumIids(numIidsList);
        gq.setPlanId(planId);
        gq.setUser(user);

        results = deleteOnePlan(gq);

        return results;

    }

    /**
     * 取消一個計劃，包含多個numIids
     */
    public static List<BatchResultMsg> deleteOnePlan(GroupQueue gq) {

        List<BatchResultMsg> results = new ArrayList<DiagAction.BatchResultMsg>();

        for (Long numIid : gq.getNumIids()) {
            BatchResultMsg resultMsg = deleteOneTmp(numIid, gq.getPlanId(), gq.getUser());
            results.add(resultMsg);
        }

        GroupPlan plan = GroupPlan.getGroupPlan(gq.getPlanId(), gq.getUser());
        List<Long> list = GroupedItems.findNumIidsByPlanId(gq.getUser().getId(), gq.getPlanId());
        if (list.size() == ZERO) {
            plan.setStatus(THREE);
            plan.statusUpdate();
        } else {
            plan.setStatus(ONE);
            plan.statusUpdate();
        }

        return results;
    }

    public static BatchResultMsg deleteOneTmp(Long numIid, Long planId, User user) {

        BatchResultMsg resultMsg = null;

        GroupedItems groupedItem = GroupedItems.findOneGroupedItem(user.getId(), planId, numIid);

        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);

        if (item == null) {
            new GroupedItems().rawDelete(user.getId(), numIid, planId);
            resultMsg = new BatchResultMsg(true, null, null, "宝贝已经删除", null);
        } else {
            String desc = getItemDesc(user, numIid);
            if (desc == null) {
                groupedItem.setStatus(FOUR);
                groupedItem.setErrorMsg("desc is null 请联系客服");
                groupedItem.jdbcSave();
                return new BatchResultMsg(false, null, null, "desc is null 请联系客服", null);
            }

            Document document = Jsoup.parse(desc, "", Parser.xmlParser());
            document.outputSettings(document.outputSettings().prettyPrint(false));

            Elements elements = document.select("a");

            for (Element element : elements) {
                if (("tbt_group_relation" + planId).equals(element.attr("name"))) {
                    element.parent().remove();
                }
            }

            String html = document.html();

            resultMsg = TemplateAction.updateItemDesc(user, numIid, html);

            //需要保證html中不存在tag標籤

            if (resultMsg.isOk()) {
                //delete
                new GroupedItems().rawDelete(user.getId(), numIid, planId);
            } else {
                groupedItem.setStatus(FOUR);
                groupedItem.setErrorMsg(resultMsg.getMsg());
                groupedItem.jdbcSave();
                log.error("[delete planId in one item failed......]" + "[numIid=]" + numIid + "  [planId=]" + planId);
            }
        }

        return resultMsg;
    }

    public static boolean isPlanIn(String newHtml, Long planId) {
        boolean flag = false;
        Document document = Jsoup.parse(newHtml);
        Elements elements = document.select("a");
        for (Element element : elements) {
            if (("tbt_group_relation" + planId).equals(element.attr("name"))) {
                flag = true;
            }
        }
        return flag;
    }

    public static String getPlanHtml(Long userId, GroupPlan plan) {
        String tmpHtml = templateHtml(plan.getModelId(), plan.getType());
        // 过滤敏感词汇
        tmpHtml = tmpHtml.replaceAll("原价", "价格");
        // modify tmpHtml
        try {
            Document doc = Jsoup.parse(tmpHtml, "", Parser.xmlParser());
            Elements template = doc.select(".template");
            template.get(0).select(".tzg_group_tag_name").first().attr("name", "tbt_group_relation" + String.valueOf(plan.getId()));
            template.get(0).select(".group_activityTitle").html(plan.getActivityTitle());
            template.get(0).select(".group_label").html(plan.getLabel());
            template.get(0).select(".group_days").html(String.valueOf(plan.getDays()));
            template.get(0).select(".group_hours").html(String.valueOf(plan.getHours()));
            template.get(0).select(".group_minutes").html(String.valueOf(plan.getMinutes()));

            parsingJson(plan, template.get(0), userId);

            tmpHtml = HtmlUtil.deleteBlank(doc.outerHtml());
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(), ex);
            tmpHtml = "";
        }
        return tmpHtml;
    }

    public static void parsingJson(GroupPlan plan, Element elem, Long userId) {
        if (StringUtil.isEmpty(plan.getItemString())) {
            plan.rawDelete(plan.getUserId(), plan.getId());
        }
        ItemProp[] itemArr = JsonUtil.toObject(plan.getItemString(), ItemProp[].class);

        Elements itemObjs = elem.select(".group_item");
        // itemString 中{id,title,price,mingPrice,count}

        for (int i = 0; i < itemObjs.size(); i++) {
            ItemPlay item = ItemDao.findByNumIid(userId, itemArr[i].getId());
            //宝贝不存在
            if (item == null) {
                item = new ItemPlay();
                item.setPicURL("http://img04.taobaocdn.com/imgextra/i4/79742176/TB2a2G2XVXXXXaAXpXXXXXXXXXX-79742176.png");
            }

            if (itemObjs.eq(i).select(".group_image").outerHtml().indexOf("<img") == 0) {
                itemObjs.eq(i).select(".group_image").attr("src", item.picURL);
            } else {
                itemObjs.eq(i).select(".group_image").attr("background", item.picURL);
            }

            itemObjs.eq(i).select(".group_href").attr("href", "http://item.taobao.com/item.htm?id=" + String.valueOf(itemArr[i].getId()));
            itemObjs.eq(i).select(".group_image").attr("data", String.valueOf(itemArr[i].getId()));
            itemObjs.eq(i).select(".group_title").html(String.valueOf(itemArr[i].getTitle()));
            itemObjs.eq(i).select(".group_price").html(String.valueOf(itemArr[i].getPrice()));
            itemObjs.eq(i).select(".group_minPrice").html(String.valueOf(itemArr[i].getMinPrice()));

            itemObjs.eq(i).select(".group_count").html(String.valueOf(itemArr[i].getCount()));
            //折扣 保留一位
            itemObjs.eq(i).select(".group_discount").html(String.format("%.1f", itemArr[i].getMinPrice() * 10 / itemArr[i].getPrice()));
            //差價 保留兩位
            itemObjs.eq(i).select(".group_save").html(String.format("%.2f", itemArr[i].getPrice() - itemArr[i].getMinPrice()));
        }
    }


    public static List<Map<String, GroupPlan>> showPlans(User user, int offset, int limit, int status) {

        List<GroupPlan> planList = GroupPlan.getGroupPlanByStatus(user, status, offset, limit);

        List<Map<String, GroupPlan>> planListMap = new ArrayList<Map<String, GroupPlan>>();

        for (GroupPlan plan : planList) {
            Map<String, GroupPlan> planMap = new HashMap<String, GroupPlan>();
            if (status == ONE) {
                List<Long> success = GroupedItems.findNumIidsSucceed(user.getId(), plan.getId());
                List<Long> fail = GroupedItems.findNumIidsFailed(user.getId(), plan.getId());
                //type 1 waiting for handle
                List<Long> waiting = GroupedItems.findNumIidsByPlanIdAndStatus(user.getId(), plan.getId(), ONE);

                plan.setSuccess(success.size());
                plan.setFail(fail.size());
                plan.setWait(waiting.size());

                int count = GroupedItems.getGrouedItemsCountByPlanId(plan.getId(), user.getId());
                if (count == ZERO) {
                    plan.setStatus(THREE);
                    plan.statusUpdate();
                    break;
                }
            }
            String html = getPlanHtml(user.getId(), plan);
            planMap.put(html, plan);
            planListMap.add(planMap);
        }
        return planListMap;
    }

    public static int getPlanCount(User user, int status) {
        int count = GroupPlan.getPlanCount(user.getId(), status);
        return count;
    }

    public static int getGroupedItemsCount(User user, int status, Long planId) {
        int count = GroupedItems.getItemCountByStatus(user.getId(), planId, status);
        return count;
    }

    public static List<GroupedItems> getGroupedItems(Long planId, User user, int status) {
        List<GroupedItems> list = GroupedItems.findGroupedItems(user.getId(), planId, status);
        List<GroupedItems> itemsList = new ArrayList<GroupedItems>();
        for (GroupedItems item : list) {
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), item.getNumIid());
            if (itemPlay == null) {
                new GroupedItems().rawDelete(user.getId(), item.getNumIid(), planId);
                itemPlay = new ItemPlay();
                itemPlay.setNumIid(item.getNumIid());
                itemPlay.setPicURL("http://img04.taobaocdn.com/imgextra/i4/79742176/TB2a2G2XVXXXXaAXpXXXXXXXXXX-79742176.png");
                itemPlay.setTitle("宝贝已经删除");
                itemPlay.setPrice(0L);
            }
            item.setPicURL(itemPlay.getPicURL());
            item.setTitle(itemPlay.getTitle());
            item.setPrice(itemPlay.getPrice());
            itemsList.add(item);
        }
        return itemsList;
    }

    //TODO
    public static List<GroupedItems> findGroupedItemsPage(Long planId, User user, int status, int offset, int limit) {
        List<GroupedItems> list = GroupedItems.findGroupedItemsPage(user.getId(), planId, status, offset, limit);
        List<GroupedItems> itemsList = new ArrayList<GroupedItems>();
        for (GroupedItems item : list) {
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), item.getNumIid());
            if (itemPlay == null) {
                new GroupedItems().rawDelete(user.getId(), item.getNumIid(), planId);
                itemPlay = new ItemPlay();
                itemPlay.setNumIid(item.getNumIid());
                itemPlay.setPicURL("http://img04.taobaocdn.com/imgextra/i4/79742176/TB2a2G2XVXXXXaAXpXXXXXXXXXX-79742176.png");
                itemPlay.setTitle("宝贝已经删除");
                itemPlay.setPrice(0L);
            }
            item.setPicURL(itemPlay.getPicURL());
            item.setTitle(itemPlay.getTitle());
            item.setPrice(itemPlay.getPrice());
            itemsList.add(item);
        }
        return itemsList;
    }


    public static boolean findPlanById(Long planId, User user) {
        boolean flag = false;

        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        if (plan != null) {
            flag = true;
        }
        return flag;
    }


    /**
     * 刪除计划的同時 需要到grouped_Items 中刪除 关联的寶貝
     */
    public static boolean deletPlan(Long planId, User user) {
        boolean flag = false;

        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        if (plan != null) {
            plan.setStatus(FOUR);
            plan.statusUpdate();
            flag = true;
        }

        List<Long> numIids = GroupedItems.findNumIidsByPlanId(user.getId(), planId, TWO);

        for (Long numIid : numIids) {
            new GroupedItems().rawDelete(user.getId(), numIid, planId);
        }
        return flag;
    }


    public static List<GroupPlan> getGroupPlans(User user, int status, int offset, int limit) {
        List<GroupPlan> plans = GroupPlan.getGroupPlanByStatus(user, status, offset, limit);
        return plans;
    }

    public static String showOnePlan(Long planId, User user) {
        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        String html = null;
        if (plan != null) {
            html = getPlanHtml(user.getId(), plan);
        }
        return html;
    }

    public static GroupPlan getPlanStatus(Long planId, User user) {
        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        if (plan == null) {
            return new GroupPlan();
        }

        int success = GroupedItems.getItemCountByStatus(user.getId(), planId, TWO);
        int wait = GroupedItems.getItemCountByStatus(user.getId(), planId, ONE);
        int fail = GroupedItems.getItemCountByStatus(user.getId(), planId, THREE) + GroupedItems.getItemCountByStatus(user.getId(), planId, FOUR);
        plan.setSuccess(success);
        plan.setWait(wait);
        plan.setFail(fail);
        return plan;
    }


    public static List<BatchResultMsg> stopPlanByNumIids(Long planId, String numIidsStr, User user) {
        List<BatchResultMsg> results = new ArrayList<BatchResultMsg>();
        String[] numIidsArr = numIidsStr.split("!@#");
        for (String numIid : numIidsArr) {
            BatchResultMsg msg = deleteOneTmp(Long.valueOf(numIid), planId, user);
            results.add(msg);
        }
        return results;
    }

    public static List<BatchResultMsg> dealByNumIids(Long planId, String numIidsStr, User user) {
        List<BatchResultMsg> results = new ArrayList<BatchResultMsg>();
        String[] numIidsArr = numIidsStr.split("!@#");
        for (String numIid : numIidsArr) {
            GroupedItems item = GroupedItems.findOneGroupedItem(user.getId(), planId, Long.valueOf(numIid));
            if (item == null) {
                results.add(new BatchResultMsg(false, "numIid非法", Long.valueOf(numIid)));
            } else {
                //3 投放失敗 4 退出投放失敗
                if (item.getStatus() == GroupAction.THREE) {
                    BatchResultMsg msg = addOneItem(planId, Long.valueOf(numIid), user);
                    results.add(msg);
                } else if (item.getStatus() == GroupAction.FOUR) {
                    BatchResultMsg msg = deleteOneTmp(Long.valueOf(numIid), planId, user);
                    results.add(msg);
                }
            }
        }
        return results;
    }


    public static BatchResultMsg addOneItem(Long planId, Long numIid, User user) {
        GroupPlan plan = GroupPlan.getGroupPlan(planId, user);
        BatchResultMsg msg = null;
        if (plan == null) {
            return new BatchResultMsg(false, null, null, "计划不存在 请联系客服", null);
        }
        String html = getPlanHtml(user.getId(), plan);
        msg = insertOneTmp(numIid, html, user, planId);
        return msg;
    }


    //删除null字符串
    public static BatchResultMsg deleteNULLByNumIid(Long numIid, User user) {
        String desc = getItemDesc(user, numIid);
        if (StringUtil.isEmpty(desc)) {
            BatchResultMsg result = new BatchResultMsg(false, null, null, "desc为空", null);
            return result;
        }
        String newDesc = desc.replace("null", "");
        if (StringUtil.isEmpty(newDesc)) {
            BatchResultMsg result = new BatchResultMsg(false, null, null, "newDesc为空", null);
            return result;
        }
        BatchResultMsg result = TemplateAction.updateItemDesc(user, numIid, newDesc);
        return result;
    }

    public static List<BatchResultMsg> deleteNULLAllShop(User user) {
        Set<Long> numIids = ItemDao.findNumIidsByUserStatus(user.getId());
        List<BatchResultMsg> results = new ArrayList<BatchResultMsg>();
        for (Long numIid : numIids) {
            BatchResultMsg msg = deleteNULLByNumIid(numIid, user);
            results.add(msg);
        }
        return results;
    }
}