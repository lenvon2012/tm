
package controllers;

import static java.lang.String.format;

import java.util.List;

import models.helpcenter.HelpNavLevel1;
import models.helpcenter.TMHelpArticle;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

public class HelpCenter extends Controller {

    private static final Logger log = LoggerFactory.getLogger(HelpCenter.class);

    public static final String TAG = "HelpCenter";

//    @CacheFor("1d")
    /**
     * 自动上下架 还缺一些东西:
     * 如果找不到宝贝, 99%的可能是 已经被排除掉了,比如销量前十, 也可能是创建的计划本身是只针对部分类目的
     * 比如我现在创建了一键均匀计划,  排除了销量最高的十个宝贝,那么, 如果要单独调整其中的几个,就需要新建一个自定义计划,并手动选择要调整的宝贝
     * 对于新的  买家什么时候来CatAnalysis/catPayHour
     * 和
     * 行业数据分析 是新的功能, 还需要说明/home/catAnalysis
     *
     * @param id
     */
    public static void index(long id) {
        HelpNavLevel1.ensureBase();
        List<HelpNavLevel1> navs = HelpNavLevel1.allWithLevel2();
        render("helpcenter/tbthelpcenter.html", navs);
    }

    public static void article(long id) {

        log.info(format("article:id".replaceAll(", ", "=%s, ") + "=%s", id));
        TMHelpArticle article = TMHelpArticle.findById(id);
        log.info("[article :]" + article);
        renderJSON(JsonUtil.getJson(article));
    }

    public static void navs() {
        List<HelpNavLevel1> navs = HelpNavLevel1.allWithLevel2();
        log.info("[navs:]" + navs);
        renderText(navs);
    }

    public static void listLevel2Articles(long level2Id, int pn, int ps) {

        log.info(format("listLevel2Articles:level2Id, pn, ps".replaceAll(", ", "=%s, ") + "=%s", level2Id, pn, ps));
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult res = TMHelpArticle.findByLevel2(level2Id, po);
        renderJSON(JsonUtil.getJson(res));
    }

//  @CacheFor("1d")
    public static void listSearchArticles(String word, int pn, int ps) {

        log.info(format("listSearchArticles:word, pn, ps".replaceAll(", ", "=%s, ") + "=%s", word, pn, ps));
        word = StringUtils.trim(word);
        PageOffset po = new PageOffset(pn, ps);
        TMResult search = TMHelpArticle.search(word, po);
        renderJSON(JsonUtil.getJson(search));
    }

}
