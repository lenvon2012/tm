
package actions;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.relation.RelationStaticModel;
import models.relation.RelationedItems;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import actions.DiagAction.BatchResultMsg;
import bustbapi.ItemApi;
import bustbapi.RecommendItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.HtmlUtil;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

public class RelationAction {
    private static final Logger log = LoggerFactory.getLogger(RelationAction.class);

    public static final String TAG = "RelationAction";

    private static final String RELATION_TITLE = "_tbtRel";

    private static final String RELATION_PATH = "//*[@title='" + RELATION_TITLE + "']";

    public static BatchResultMsg insertRelationItems(User user, Long numIid, int index) {
        try {
            if (user == null || numIid == null) {
                return null;
            }

            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
            if (itemPlay == null) {
                return null;
            }

            RelationedItems relationed = RelationedItems.findByNumIid(user.getId(), numIid);
            // 如果已经关联了
            if (relationed != null) {
                return null;
            }
            String desc = getItemDesc(user, numIid);
            if (desc == null) {
                return null;
            }

//            boolean flag = HtmlUtil.isNeedToAddImg(desc, RELATION_PATH);
//            if (flag == false) {
//            	itemPlay.setRelated();
//                itemPlay.jdbcSave();
//                return null;
//            }

            //List<ItemPlay> relationList = chooseRelationList(user.getId(), numIid);
            List<ItemPlay> relationList = recommendItems(user, numIid);
            String html = RelationHtml.generateHtml(relationList, index);
            String newHtml = html + desc;
//            newHtml = desc = HtmlUtil.deleteBlank(newHtml);log.info("333333333333333333333333333333333333333");    
            DiagAction.BatchResultMsg resultMsg = TemplateAction.updateItem(user, numIid, newHtml);
            if (resultMsg.isOk()) {
                // 不应该使用ItemPlay来标志宝贝是否关联过
                /*itemPlay.setRelated();
                itemPlay.jdbcSave();*/
                new RelationedItems(user.getId(), numIid).jdbcSave();
                return null;
            }
            else {
                return resultMsg;
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static BatchResultMsg removeRelationItems(User user, Long numIid) {
        try {
            if (user == null || numIid == null) {
                return null;
            }

            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
            //if (itemPlay == null || itemPlay.isRelated() == false)
            //    return;
            String desc = getItemDesc(user, numIid);
            if (desc == null) {
                return null;
            }

//            boolean flag = HtmlUtil.isNeedToAddImg(desc, RELATION_PATH);
//            if (flag == true) {
//            	itemPlay.setUnRelated();
//                itemPlay.jdbcSave();
//            	return null;
//            }

            String newHtml = deleteModel(desc, false);
            DiagAction.BatchResultMsg resultMsg = TemplateAction.updateItem(user, numIid, newHtml);
            if (resultMsg.isOk()) {
                // 不应该使用ItemPlay来标志是否推广, 使用RelationedItems
                //itemPlay.setUnRelated();
                //itemPlay.jdbcSave();
                RelationedItems.remove(user, numIid);
                return null;
            }
            else {
                return resultMsg;
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }

        return null;
    }

    //要不要考虑上架和下架的问题
    private static List<ItemPlay> chooseRelationList(Long userId, Long numIid) {
        List<ItemPlay> itemPlayList = ItemDao.findByUserId(userId);

        List<ItemPlay> relationList = new ArrayList<ItemPlay>();
        if (itemPlayList.size() == 0)
            return relationList;
        while (relationList.size() < 6) {
            ItemPlay relation = null;
            while (relation == null || numIid.equals(relation.getNumIid())) {
                int rand = (int) (Math.random() * 100);
                int index = rand % itemPlayList.size();
                relation = itemPlayList.get(index);
            }
            relationList.add(relation);
        }

        return relationList;
    }

    /**
     * This is very important...
     * @param userId
     * @param numIid
     * @param Long count 获取的数量
     * @return
     */
    public static List<ItemPlay> recommendItems(User user,Long numIid){
        log.info(format("recommendItems:userId, numIid".replaceAll(", ", "=%s, ") + "=%s", user.getId(), numIid));

        Long count = 8L;
        
        List<ItemPlay> redommendList = null;
        redommendList = (List<ItemPlay>) Cache.get(TAG + "recommendItems" + numIid);

        if (redommendList != null) {
            return redommendList;
        }

        redommendList = new ArrayList<ItemPlay>();
        
        RecommendItemApi.getRecommend api = new RecommendItemApi.getRecommend(user, numIid,count);
        
        if(api.isApiSuccess()){
            redommendList = api.call();
        }
        return redommendList;
    }
    
//    public static List<ItemPlay> recommendItems(Long userId, Long numIid) {
//
//        log.info(format("recommendItems:userId, numIid".replaceAll(", ", "=%s, ") + "=%s", userId, numIid));
//
//        List<ItemPlay> relationList = null;
//        relationList = (List<ItemPlay>) Cache.get(TAG + "recommendItems" + numIid);
//
//        if (relationList != null) {
//            return relationList;
//        }
//
//        relationList = new ArrayList<ItemPlay>();
//        TaobaoClient client = new DefaultTaobaoClient(TMConfigs.App.API_TAOBAO_URL, TMConfigs.App.APP_KEY,
//                TMConfigs.App.APP_SECRET);
//        
//        /*
//         * TODO 怎么还有这种裸掉 淘宝接口的代码, 全部封装成 tbapi的形式
//         */
//        ItemrecommendItemsGetRequest req = new ItemrecommendItemsGetRequest();
//        req.setItemId(numIid);
//        req.setRecommendType(3L);
//        req.setCount(8L);
//        req.setExt("");
//        try {
//            ItemrecommendItemsGetResponse response = client.execute(req);
//            if (response.isSuccess()) {
//                JSONObject obj = new JSONObject(response.getBody()).getJSONObject("itemrecommend_items_get_response")
//                        .getJSONObject("values");
//                if (!"{}".equals(obj.toString())) {
//                    JSONArray favs = (JSONArray) obj.getJSONArray("favorite_item");
//                    for (int i = 0; i < favs.length(); i++) {
//                        JSONObject fav = favs.getJSONObject(i);
//                        String track_iid = fav.getString("track_iid");
//                        Long favId = Long.parseLong(track_iid.substring(0, track_iid.indexOf("_track_")));
//                        relationList.add(ItemDao.findByNumIid(userId, favId));
//                    }
//                }
//                if (relationList.size() < 8) {
//                    relationList.addAll(ItemDao.findByUserIdOnsale(userId, 8 - relationList.size()));
//                }
//            }
//        } catch (ApiException e) {
//            log.warn(e.getMessage(), e);
//        } catch (JSONException e) {
//            log.warn(e.getMessage(), e);
//        }
//
//        if (!CommonUtils.isEmpty(relationList)) {
//            Cache.set(TAG + "recommendItems" + numIid, relationList, "1h");
//        }
//        return relationList;
//    }

    public static String getItemDesc(User user, Long id) {
        try {
            Item item = new ItemApi.ItemDescGet(user, id).call();
            if (item == null)
                return null;
            return item.getDesc();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    //生成关联的html
    public static class RelationHtml {
        public static String generateHtml(List<ItemPlay> relationList, int index) {
            if (CommonUtils.isEmpty(relationList)) {
                return StringUtils.EMPTY;
            }

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<div class='" + RELATION_TITLE + "' style='width:100%;background:#f9f9f9;'>");
            htmlBuilder
                    .append("<div style='color:#333;font-weight:bold;font-size:16px;padding:10px 0px 5px 10px;;margin-bottom:10px;border-bottom:3px solid #fe7e01;'>您可能还会喜欢：</div>");
            htmlBuilder
                    .append("<table style='width:100%;border-spacing:0;border-collapse:collapse;text-align:center'><tbody>");

//            htmlBuilder.append("<tr>");
            String itemTable = "<tr>";
            for (int i = 0; i < 4; i++) {
                itemTable += "<td>";
                if (i < relationList.size()) {
                    itemTable += appendOneItem(relationList.get(i), index);
                }
                itemTable += "</td>";
            }
//            htmlBuilder.append("</tr>");
            itemTable += "</tr><tr>";

//            htmlBuilder.append("<tr>");
            for (int i = 4; i < 8; i++) {
                itemTable += "<td>";
                if (i < relationList.size()) {
                    itemTable += appendOneItem(relationList.get(i), index);
                }
                itemTable += "</td>";
            }
            itemTable += "</tr>";
//            htmlBuilder.append("</tr>");
            htmlBuilder.append(itemTable);
            htmlBuilder.append("</tbody></table>");
            htmlBuilder.append("</div>");
            String html = htmlBuilder.toString();
            return html;
        }

        private static String appendOneItem(ItemPlay itemPlay, int index) {

            String price = Double.toString(itemPlay.getPrice());
            String numIid = Long.toString(itemPlay.getNumIid());
            String salesCount = Long.toString(itemPlay.getSalesCount());

            List<String> ModelList = RelationStaticModel.getModelList(numIid, itemPlay.getPicURL(),
                    itemPlay.getTitle(), price, salesCount, 1);

            return ModelList.get(index);

        }
    };
    
    public static BatchResultMsg removeRelationItemsAll(User user, Long numIid) {
        if (user == null || numIid == null) {
            return null;
        }
        String desc = getItemDesc(user, numIid);
        if (desc == null) {
            return null;
        }
        String newHtml = deleteModel(desc, true);
        DiagAction.BatchResultMsg resultMsg = TemplateAction.updateItem(user, numIid, newHtml);
        if (resultMsg.isOk()) {
            RelationedItems.remove(user, numIid);
            return resultMsg;
        }
        return null;
    }
    
    private static String deleteModel(String content, boolean isAll) {
        Document doc = Jsoup.parse(content);
        if(isAll){
            doc.select("div[style*=width:100.0%;background:#f9f9f9;]").remove();
        } else {
            doc.select("div[class=_tbtRel]").remove();
        }
        String model = doc.toString();
        model = model.replace("<html>", "");
        model = model.replace("<head>", "");
        model = model.replace("</head>", "");
        model = model.replace("<body>", "");
        model = model.replace("</body>", "");
        model = model.replace("</html>", "");
        return model;
    }

}
