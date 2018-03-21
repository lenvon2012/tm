/**
 * 
 */
package ppapi;

import java.util.HashMap;

import models.paipai.PaiPaiUser;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiUserInfo;

import com.ciaosir.client.utils.JsonUtil;

/**
 * @author navins
 * @date 2013-7-8 下午6:20:08
 */
public class PaiPaiUserInfoApi extends PaiPaiApi<PaiPaiUserInfo> {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiUserInfoApi.class);

    public static String apiPath = "/user/getUserInfo.xhtml";

    public PaiPaiUserInfoApi(PaiPaiUser user) {
        super(user);
    }

    @Override
    public String getApiPath() {
        return apiPath;
    }

    @Override
    public boolean prepareRequest(HashMap<String, Object> params) {
        params.put("sellerUin", "" + uin);

        return false;
    }

    @Override
    public PaiPaiUserInfo validResponse(String resp) {
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

        long sellerUin = 0L;
        if (node.findValue("uin") == null) {
            sellerUin = uin;
        } else {
            sellerUin = node.findValue("uin").getLongValue();
        }
        String nick = node.findValue("nickName").getTextValue();
        int buyerCredit = node.findValue("buyerCredit").getIntValue();
        int sellerCredit = node.findValue("sellerCredit").getIntValue();
        PaiPaiUserInfo user = new PaiPaiUserInfo(sellerUin, nick, buyerCredit, sellerCredit);
        return user;
    }

    @Override
    public PaiPaiUserInfo applyResult(PaiPaiUserInfo res) {
        return res;
    }

}
