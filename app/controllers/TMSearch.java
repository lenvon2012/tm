
package controllers;

import java.util.List;

import models.tmsearch.Grade;
import models.tmsearch.ItemAuthority;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.Required;
import play.mvc.Controller;
import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchApi.PriceRangeInfo;
import spider.mainsearch.MainSearchApi.TBSearchRes;
import actions.catunion.ItemAuthorityAction;

import com.ciaosir.client.utils.JsonUtil;

import configs.TMConfigs;

public class TMSearch extends Controller {

    private static final Logger log = LoggerFactory.getLogger(TMSearch.class);

    private static int searchID;

    public static void index() {
        render("tmSearch/tmSearch.html");
    }

    public static void search(@Required String username) {

        if (validation.hasErrors()) {
            flash.error("请填写淘宝帐号...");
            index();
        }
        else {
            if (searchID == 20) {
                try {
                    List<ItemAuthority> itemArray = ItemAuthorityAction.doFindItemAuthorityList(username);
                    render("tmSearch/tmResult.html", itemArray);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }

            }
            else if (searchID == 30) {
                try {
                    String gradeHTML = Grade.getGradeHtml(username);
                    System.out.println(gradeHTML);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
    }

    public static void comment() {
        searchID = 10;
        render("tmSearch/tmSearch.html");
    }

    public static void comment_rank() {
        searchID = 11;
        render("tmSearch/tmSearch.html");
    }

    public static void buyer_credit() {
        searchID = 12;
        render("tmSearch/tmSearch.html");
    }

    public static void seller_credit() {
        searchID = 13;
        render("tmSearch/tmSearch.html");
    }

    public static void credit() {
        searchID = 14;
        render("tmSearch/tmSearch.html");
    }

    public static void down() {
        searchID = 20;
        render("tmSearch/tmSearch.html");
    }

    public static void down_record() {
        searchID = 21;
        render("tmSearch/tmSearch.html");
    }

    public static void down_info() {
        searchID = 22;
        render("tmSearch/tmSearch.html");
    }

    public static void grade() {
        searchID = 30;
        render("tmSearch/tmSearch.html");
    }

    public static void grade_record() {
        searchID = 31;
        render("tmSearch/tmSearch.html");
    }

    public static void consignment() {
        searchID = 40;
        render("tmSearch/tmSearch.html");
    }

    public static void otherWeb() {
        searchID = 50;
        render("tmSearch/tmSearch.html");
    }

    public static void ems() {
        searchID = 51;
        render("tmSearch/tmSearch.html");
    }

    public static void buyAgent() {
        searchID = 52;
        render("tmSearch/tmSearch.html");
    }

    public static void bbs() {
        searchID = 53;
        render("tmSearch/tmSearch.html");
    }

    public static void comm_collection() {
        searchID = 54;
        render("tmSearch/tmSearch.html");
    }

    public static void priceRegion(String word) {
        if (TMConfigs.PARSE_PRICE_RANGE) {
            log.error("PParse Price Range Not open");
            return;
        }

        MainSearchParams params = new MainSearchParams(word, 1, null);
        TBSearchRes search = MainSearchApi.search(params);
        List<PriceRangeInfo> ranges = search.getRanges();
        if (ranges == null) {
            ranges = ListUtils.EMPTY_LIST;
        }
        renderJSON(JsonUtil.getJson(ranges));
    }

}
