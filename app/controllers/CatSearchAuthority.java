
package controllers;

import java.util.List;

import models.tmsearch.ItemAuthority;
import models.tmsearch.TmallSearchLog.TmallSearchType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.catunion.ItemAuthorityAction;
import actions.catunion.UserIdNickAction;

import com.ciaosir.client.CommonUtils;

public class CatSearchAuthority extends CatUnionBase {

    private static final Logger log = LoggerFactory.getLogger(CatSearchAuthority.class);

    public static void index() {

        String title = "卖家隐形降权查询";
        String keywords = MetaKeywords;
        String description = MetaDescription;
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.Authority);
        render("tmSearch/searchauthority.html", title, keywords, description, nickInfoList);

    }

    public static void userAuthority() {
        String userNick = queryNickByUserId(TmallSearchType.Authority);
        if (StringUtils.isEmpty(userNick))
            renderText("");
        //log.error(userNick);
        String title = "" + userNick + " 卖家隐形降权 - 天猫联盟";
        String keywords = userNick + " " + MetaKeywords;
        String description = "淘宝账号：" + userNick + " 卖家隐形降权。 " + MetaDescription;

        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.Authority);
        render("tmSearch/searchauthority.html", title, keywords, description, userNick, nickInfoList);
    }

    public static void doQueryAuthority(String userNick) {
        userNick = trimUserNick(userNick);

        try {
            List<ItemAuthority> itemList = ItemAuthorityAction.doFindItemAuthorityList(userNick);
            boolean isSuccess = true;
            if (CommonUtils.isEmpty(itemList))
                isSuccess = false;

            long userId = 0;
            if (isSuccess == true)
                userId = UserIdNickAction.findUserIdByNick(userNick);
            saveSearchLog(TmallSearchType.Authority, userId, userNick, isSuccess);
            renderResultJson(itemList);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            renderFailJson("系统出现一些异常，请稍后重试");
        }
    }
}
