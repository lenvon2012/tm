/**
 * 
 */

package actions;

import models.user.TokenExpiresIn;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;

import utils.TaobaoUtil;

/**
 * @author navins
 * @date 2013-6-25 下午8:40:58
 */
public class GetTokenExpiresAction {

    public static TokenExpiresIn get(String sessionKey, String refreshToken) {
        if (StringUtils.isEmpty(sessionKey) || StringUtils.isEmpty(refreshToken)) {
            return TokenExpiresIn.EMPTY;
        }
        User user = new User();
        user.setSessionKey(sessionKey);
        user.setRefreshToken(refreshToken);

        try {
            JsonNode readJsonResult = TaobaoUtil.getRefreshJsonNode(user);

            String r2_expires_in = readJsonResult.get("r2_expires_in").getTextValue();
            String w2_expires_in = readJsonResult.get("w2_expires_in").getTextValue();

            TokenExpiresIn expire = new TokenExpiresIn(w2_expires_in, r2_expires_in);

            return expire;
        } catch (Exception e) {
            return TokenExpiresIn.EMPTY;
        }
    }

}
