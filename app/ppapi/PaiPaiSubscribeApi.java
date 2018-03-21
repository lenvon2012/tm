package ppapi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.paipai.PaiPaiUser;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiSubscribe;

import com.ciaosir.client.utils.JsonUtil;

public class PaiPaiSubscribeApi {

    public final static Logger log = LoggerFactory.getLogger(PaiPaiSubscribeApi.class);

    public static final int ITEM_PAGE_SIZE = 30;

    public static final int TRADE_PAGE_SIZE = 30;

    public static class PaiPaiSubscribeListApi extends PaiPaiApi<List<PaiPaiSubscribe>> {

        public PaiPaiUser user;

        public PaiPaiSubscribeListApi(PaiPaiUser user) {
            super(user);
            this.user = user;
        }

        @Override
        public String getApiPath() {
            return "/appstore/getSubscribeList.xhtml";
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            params.put("userUin", String.valueOf(user.getId()));
            return false;
        }

        @Override
        public List<PaiPaiSubscribe> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }

            JsonNode itemList = node.findValue("appUserSubscribeList");
            if (itemList == null || itemList.isArray() == false) {
                log.info("--itemList empty--" + resp);
            }

            List<PaiPaiSubscribe> list = new ArrayList<PaiPaiSubscribe>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            try {
                JSONArray json = new JSONArray(itemList.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    int chargeItemId = Integer.valueOf(obj.getString("chargeItemId"));
                    long deadLine = 0;
                    if (!StringUtils.isEmpty(obj.getString("deadline"))) {
                        deadLine = sdf.parse(obj.getString("deadline")).getTime();
                    }
                    PaiPaiSubscribe subscribe = new PaiPaiSubscribe(user.getId(), chargeItemId, deadLine);
                    list.add(subscribe);
                }
            } catch (JSONException e) {
                log.error(resp);
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(resp);
                log.error(e.getMessage(), e);
            }
            return list;
        }

        @Override
        public List<PaiPaiSubscribe> applyResult(List<PaiPaiSubscribe> res) {
            return res;
        }

    }

}
