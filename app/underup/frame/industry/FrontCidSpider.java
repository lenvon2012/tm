//package underup.frame.industry;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.apache.commons.collections.ListUtils;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.jsoup.nodes.Document;
//
//import play.jobs.Job;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.ArrayList;
//
//import models.item.FrontCatPlay;
//
//import com.ciaosir.client.CommonUtils;
//
//import play.jobs.OnApplicationStart;
//
////@OnApplicationStart
//public class FrontCidSpider extends Job {
//    private static final Logger log = LoggerFactory.getLogger(FrontCidSpider.class);
//  
//    @Override
//    public void doJob() {
//        
//        List<FrontCatShow> frontCatPlayLevel1 = getFrontCidLevel1();
//
//        if (CommonUtils.isEmpty(frontCatPlayLevel1)) {
//            log.info("get the level one cids fron front_cat_play failed...");
//            return;
//        }
//        for (FrontCatShow frontCatPlay : frontCatPlayLevel1) {
//            long frontCidLevel1 = frontCatPlay.getId();
//            getFrontCidLevel2(frontCidLevel1);
//        }
//        add();
//        
//    }
//    //Long frontCid, Long frontParentCid, String name, Boolean isParent, int level
//    public void add(){
//        new FrontCatShow(50006842L, null, "精品女包",false, 2).jdbcSave();
//        new FrontCatShow(50005700L, null, "品牌手表",false, 2).jdbcSave();
//        new FrontCatShow(1512L, null, "手机",false, 2).jdbcSave();
//        new FrontCatShow(50047310L, null, "平板电脑",false, 2).jdbcSave();
//        new FrontCatShow(1101L, null, "笔记本",false, 2).jdbcSave();
//        new FrontCatShow(50071436L, null, "美发护发",false, 2).jdbcSave();
//        new FrontCatShow(50007216L, null, "鲜花园艺",false, 2).jdbcSave();
//        new FrontCatShow(50008075L, null, "面包蛋糕",false, 2).jdbcSave();
//    }
//    public List<FrontCatShow> getFrontCidLevel1() {
//        List<FrontCatShow> frontCatLevel1 = new ArrayList<FrontCatShow>();
//        Document document = null;
//        try {
//            document = Jsoup.connect("http://list.taobao.com/browse/cat-0.htm").get();
//        } catch (IOException e) {
//            log.warn(e.getMessage(), e);
//        }
//        if (document == null) {
//            log.error("can't get the front cid level1");
//            return frontCatLevel1;
//        }
//
//        Element content = document.getElementById("content");
//        Elements main = content.select(".catlist");
//        for (Element el : main) {
//            Elements firstLevel = el.select(".section");
//            for (Element elem : firstLevel) {
//                String linkhref = elem.select("a").attr("href");
//                Long firstCatNum = null;
//                String name = null;
//                if (linkhref.indexOf("&cat=") < 0) {
//                    log.error("can't get the first level ..."+ linkhref);
//                    continue;
//                } else {
//                    firstCatNum = Long.parseLong(subCatNum(linkhref, linkhref.indexOf("&cat=")));
//                    name = elem.select("a").get(0).text().trim();
//                }
//                FrontCatShow frontCatShow = new FrontCatShow(firstCatNum, null, name, true, 1);
//                frontCatLevel1.add(frontCatShow);
//                frontCatShow.jdbcSave();
//            }
//        }
//        return frontCatLevel1;
//    }
//    public static int flag = 0;
//    public void getFrontCidLevel2(long frontCidLevel1) {
//        Document document = null;
//
//        StringBuilder sb = new StringBuilder(
//                "http://s.taobao.com/search?tab=all&style=list&cd=false&promote=0&sort=sale-desc&cps=yes&cat=");
//        sb.append(frontCidLevel1);
//        String url = sb.toString();
//        int retry = 0;
//        while (++retry < 6) {
//            try {
//                document = Jsoup.connect(url).get();
//            } catch (IOException e) {
//                log.info("cant't get info from remote url:" + url + " for time = " + retry + " and retry again");
//                log.error(e.getMessage(), e);
//            }
//        }
//
//        Elements level2Contents = document.getElementsByClass("nav-category");
//        if (level2Contents == null || level2Contents.size() == 0) {
//            log.error("can't get frontcid level2 infromation from the html, please check the frontcid level1 "
//                    + frontCidLevel1);
//            return;
//        }
//        Elements links = null;
//        for (Element element : level2Contents) {
//            if (element.getElementsByTag("h4").attr("title").trim().equals("相关分类")) {
//                Elements elems = element.getElementsByClass("nav-category-content");
//                if (elems == null) {
//                    log.error("get content failed by frontcid level1 " + frontCidLevel1);
//                    return;
//                }
//                links = elems.get(0).select("a");
//                log.info("get the front cid number " + ++flag);
//                if (links.size() == 0) {
//                    log.error(" can't find \"a\" elements please check by froncid level1 " + frontCidLevel1);
//                    return;
//                }
//                break;
//            }
//        }
//        if (links == null) {
//            log.error("can't find 相关分类, please check the front cid level1 " + frontCidLevel1);
//            return;
//        }
//        for (Element link : links) {
//            long frontCidLevel2 = Long.parseLong(link.attr("data-param-value"));
//            String name = link.attr("title").trim();
//            new FrontCatShow(frontCidLevel2, frontCidLevel1, name, false, 2).jdbcSave();
//        }
//
//    }
//   
//    private String subCatNum(String linkhref, int index) {
//        // 截取url中&cat= 到& 之间的字符串，并且判断是否为空
//        index += "&cat=".length();
//        int endCatIndex = linkhref.indexOf('&', index);
//        return endCatIndex < 0 ? linkhref.substring(index) : linkhref.substring(index, endCatIndex);
//    }
//    //Long frontCid, Long frontParentCid, String name, Boolean isParent, int level
//    static{
//        new FrontCatShow(50008907L, null, "移动/联通/电信网上营业厅", false, 2).jdbcSave();
//        new FrontCatShow(50004958L, null, "移动/联通/电信充值中心", false, 2).jdbcSave();
//        new FrontCatShow(50017708L, null, "网游装备/游戏币/帐号/代练", false, 2).jdbcSave();
//        new FrontCatShow(40L, null, "腾讯QQ专区", false, 2).jdbcSave();
//        new FrontCatShow(99L, null, "网络游戏点卡", false, 2).jdbcSave();
//        new FrontCatShow(50018963L, null, "度假线路/签证送关/旅游服务", false, 2).jdbcSave();
//        new FrontCatShow(50100088L, null, "特价酒店/特色客栈/公寓旅馆", false, 2).jdbcSave();
//        new FrontCatShow(52376017L, null, "投资保险", false, 2).jdbcSave();
//        new FrontCatShow(50101553L, null, "意外险", false, 2).jdbcSave();
//        new FrontCatShow(50100923L, null, "旅行保险", false, 2).jdbcSave();
//        new FrontCatShow(50101552L, null, "健康保险", false, 2).jdbcSave();
//        new FrontCatShow(50101547L, null, "少儿保险", false, 2).jdbcSave();
//        new FrontCatShow(50093869L, null, "财产险", false, 2).jdbcSave();
//        new FrontCatShow(50093856L, null, "车险", false, 2).jdbcSave();
//        new FrontCatShow(53150001L, null, "人寿保险", false, 2).jdbcSave();
//    }
//}
