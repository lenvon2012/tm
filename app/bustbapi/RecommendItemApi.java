package bustbapi;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.request.ItemrecommendItemsGetRequest;
import com.taobao.api.response.ItemrecommendItemsGetResponse;

import controllers.newAutoTitle;

import dao.item.ItemDao;

public class RecommendItemApi {
    
    private static final Logger log = LoggerFactory.getLogger(RecommendItemApi.class);
    
    public static class getRecommend extends TBApi<ItemrecommendItemsGetRequest, ItemrecommendItemsGetResponse, List<ItemPlay>>{
        
        private Long numIid;
        
        private User user;
        
        private Long count;
        
        public getRecommend(User user,Long numIid,Long count){
            super(user.getSessionKey());
            this.user = user;
            this.numIid = numIid;
            this.count = count;
        }
        
        @Override
        public ItemrecommendItemsGetRequest prepareRequest() {
            ItemrecommendItemsGetRequest req = new ItemrecommendItemsGetRequest();
            req.setItemId(numIid);
            req.setRecommendType(3L);
            req.setCount(count);
            req.setExt("");
            return req;
        }

        @Override
        public List<ItemPlay> validResponse(ItemrecommendItemsGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            List<ItemPlay> recommendList = new ArrayList<ItemPlay>();
            
            if(resp.isSuccess()){
                try {
                    JSONObject obj = new JSONObject(resp.getBody()).getJSONObject("itemrecommend_items_get_response")
                            .getJSONObject("values");
                    if (!"{}".equals(obj.toString())) {
                        JSONArray favs = (JSONArray) obj.getJSONArray("favorite_item");
                        for (int i = 0; i < favs.length(); i++) {
                            JSONObject fav = favs.getJSONObject(i);
                            String track_iid = fav.getString("track_iid");
                            Long favId = Long.parseLong(track_iid.substring(0, track_iid.indexOf("_track_")));
                            recommendList.add(ItemDao.findByNumIid(user.getId(), favId));
                        }
                    }
                    if (recommendList.size() < count.intValue()) {
                        recommendList.addAll(ItemDao.findByUserIdOnsaleDesc(user.getId(), count.intValue() - recommendList.size()));
                    }
                } catch (JSONException e) {
                    log.warn(e.getMessage(),e);
                }
            }
            return recommendList;
        }

        @Override
        public List<ItemPlay> applyResult(List<ItemPlay> res) {
            return res;
        }
    }
}
