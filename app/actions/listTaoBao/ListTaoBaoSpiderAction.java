
package actions.listTaoBao;

import java.io.IOException;

import models.item.FrontCatPlay;
import models.item.ItemCatPlay;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

/**
 * 获取淘宝前端cid 以及name
 * @author hyg
 * 2014-4-8下午7:57:07
 */
//@Every("10s")
//@OnApplicationStart
public class ListTaoBaoSpiderAction extends Job {

    private static final Logger log = LoggerFactory.getLogger(ListTaoBaoSpiderAction.class);

    public static final String TAG = "ListTaoBaoSpider";

    @Override
    public void doJob() {
        new ListTaoBaoSpiderAction().fetchItem();
    }

    /**
     * 获取网页的数据，转换成document对象
     */
    public void fetchItem() {
        Document document = null;
        try {
            document = Jsoup.connect("http://list.taobao.com/browse/cat-0.htm").get();
            //log.info("--------------------------------------------http://list.taobao.com/browse/cat-0.htm-----------------"+document);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        Element content = document.getElementById("content");
        Elements main = content.select(".catlist");
        for (Element el : main) {

            // 获取每个大类下的所有li标签
            Elements firstLevel = el.select(".section");
            for (Element elem : firstLevel) {
                fetchEachLevel1(elem, firstLevel);
            }
        }
    }

    /**
     * 获取firstLevel
     * 
     * @param elem
     *            一级目录下li标签
     * @param firstLevel
     *            以及目录下的元素
     */
    private void fetchEachLevel1(Element elem, Elements firstLevel) {
        // String firstName = elem.getElementsByTag("h5").text();
        String firstName = null;
        String linkhref = elem.select("a").attr("href");
        Long firstCatNum = null;
        Integer level1 = 1;
        if (linkhref.indexOf("&cat=") < 0) {
            log.error("can't get the first level ...");
            return;
        } else {
            firstCatNum = Long.parseLong(subCatNum(linkhref, linkhref.indexOf("&cat=")));
        }

        // cid是否在map中
        if (ListTaoBaoSpiderMap.cidMap.containsKey(firstCatNum)) {
            firstCatNum = ListTaoBaoSpiderMap.cidMap.get(firstCatNum);

            log.info("[firstCatNum in map:] " + firstCatNum);
        }

        // cid在当前表中不存在
        if (FrontCatPlay.findById(firstCatNum) == null) {
            firstName = ItemCatPlay.findNameByCid(firstCatNum);
            FrontCatPlay firstModel = new FrontCatPlay(firstCatNum, null, null, firstName, Boolean.TRUE, level1);
            firstModel.save();
            log.info("[save firstModel:]" + firstModel);
        }

        // 获取最后一层，即h5后的下一个div 也是唯一一个
        Elements nextDivs = elem.getElementsByTag("div");
        // Element nextDiv = elem.nextElementSibling(); 兄弟div只能执行一半
        for (Element nextDiv : nextDivs) {
            Elements secondName = nextDiv.getElementsByTag("a");
            for (Element second : secondName) {
                fetchEachLevel2(firstName, firstCatNum, second);
            }
        }
    }

    /**
     * 获取secondLevel
     * 
     * @param firstName
     *            parentName
     * @param firstCatNum
     *            parentCid
     * @param second
     *            level2下的<a>标签
     */
    private void fetchEachLevel2(String firstName, Long firstCatNum, Element second) {
        String secondName = second.text();
        String linkhref = second.select("a").attr("href");
        Long secondCatNum = null;
        Integer level2 = 2;
        if (linkhref.indexOf("&cat=") < 0) {
            return;
        } else {
            secondCatNum = Long.parseLong(subCatNum(linkhref, linkhref.indexOf("&cat=")));
        }

        if (FrontCatPlay.findById(secondCatNum) == null) {

            FrontCatPlay secondModel = new FrontCatPlay(secondCatNum, firstCatNum, firstName, secondName,
                    Boolean.FALSE, level2);
            secondModel.save();
            log.info("[save secondModel]" + secondCatNum);
        }
    }

    /**
     * 截取cid
     * @param linkhref
     * @param index
     * @return
     */
    private String subCatNum(String linkhref, int index) {
        // 截取url中&cat= 到& 之间的字符串，并且判断是否为空
        index += "&cat=".length();
        int endCatIndex = linkhref.indexOf('&', index);
        return endCatIndex < 0 ? linkhref.substring(index) : linkhref.substring(index, endCatIndex);
    }
}
