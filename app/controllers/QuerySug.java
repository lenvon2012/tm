/**
 * 
 */

package controllers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.Controller;
import result.PolicyResult;
import sug.api.QuerySugAPI;

import com.ciaosir.client.utils.JsonUtil;

/**
 * @author navins
 * @created 2012-10-19 下午8:07:57
 */
public class QuerySug extends Controller {

    public static boolean SUG_USE_PROXY = Boolean.valueOf(Play.configuration.getProperty(
            "sug.use_proxy", "false"));

    public static void sug(String word) {
        if (StringUtils.isEmpty(word)) {
            PolicyResult.badrequest("参数错误！param: word");
        }

        List<String> result = QuerySugAPI.getQuerySugList(word, SUG_USE_PROXY);

        renderJSON(JsonUtil.getJson(result));
    }
}
