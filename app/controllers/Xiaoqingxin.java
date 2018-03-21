
package controllers;

import java.util.ArrayList;
import java.util.List;

import models.popularized.Popularized;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import result.TMResult;
import utils.TMCatUtil;
import actions.shopping.RandomShareAction;

import com.ciaosir.client.utils.JsonUtil;

import configs.TMConfigs.WebParams;
import dao.UserDao;
import dao.popularized.PopularizedDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;
import dao.popularized.ShoppingDao;

/**
 * loading的图片换成绿色的，我稍后叫我弟兄发过来
 *
 */
public class Xiaoqingxin extends Controller {

    private static final Logger log = LoggerFactory.getLogger(Xiaoqingxin.class);

    public static void index() {
        goIndex();
    }

    static void goIndex() {
        render("xiaoqingxin/index.html");
    }

    public static void detail() {
        render("xiaoqingxin/detail.html");
    }

    public static void catItem() {
        render("xiaoqingxin/catitem.html");
    }

    public static void allCats() {
        List<List<Popularized>> cats = new ArrayList<List<Popularized>>();
        for (long catId = 1l; catId <= 10l; catId++) {
            cats.add(randomUserItems(0l, catId, "", 1));
        }
        renderSuccess(cats);
    }

    public static List<Popularized> randomUserItems(Long numIid, Long topCatId, String title, int status) {
        User user = null;
        String sid = session.get(WebParams.SESSION_USER_KEY);
        if (StringUtils.isEmpty(sid)) {
            sid = params.get("sid");
        }

        if (!StringUtils.isEmpty(sid)) {
            user = UserDao.findBySessionKey(sid);
        }

        status = PopularizedStatusSqlUtil.checkStatus(status);
        
        Long userId = null;
        if (user != null) {
            userId = user.getId();
        }

        List<Popularized> items = RandomShareAction.randomWithUser(numIid, userId, topCatId, title, status);

        return items;

    }

    protected static void renderSuccess(Object res) {
        TMResult tmRes = new TMResult(true, "", res);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    private static final int EachReturnNum = 20;

    public static List<Popularized> randomWithUser(Long numIid, User user, Long topCatId, String title, int status) {
        List<Popularized> resultItemList = new ArrayList<Popularized>();

        try {
            String catName = TMCatUtil.getFirstLevelCatName(topCatId);

            //根据numIid搜索
            Popularized targetItem = null;
            if (numIid != null && numIid > 0) {
                targetItem = ShoppingDao.findByNumIid(numIid, catName, title, status);
            }
            if (targetItem != null)
                resultItemList.add(targetItem);

            //根据user搜索
            if (user != null) {
                List<Popularized> userItemList = ShoppingDao.findByUserAndTitle(user.getId(), catName, title, status);
                userItemList = PopularizedDao.randomItems(userItemList);
                if (targetItem != null && userItemList.contains(targetItem)) {
                    userItemList.remove(targetItem);
                }

                resultItemList.addAll(userItemList);
            }
            if (resultItemList.size() >= EachReturnNum) {
                resultItemList = resultItemList.subList(0, EachReturnNum);
                return resultItemList;
            } else {
                List<Popularized> itemList = RandomShareAction.randomWithTitleByCatName(title, catName, status);
                for (Popularized temp : itemList) {
                    if (resultItemList.contains(temp))
                        continue;
                    resultItemList.add(temp);
                    if (resultItemList.size() >= EachReturnNum)
                        break;
                }

                return resultItemList;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return resultItemList;

    }

}
