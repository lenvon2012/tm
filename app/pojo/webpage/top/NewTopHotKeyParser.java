package pojo.webpage.top;

import codegen.CodeGenerator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import job.word.NewTopKeyNavSpider;
import models.word.top.TopKey;
import models.word.top.TopURLBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hao on 15-4-30.
 */
public class NewTopHotKeyParser {
    private static final NewTopHotKeyParser HOT_KEY_PARSER = new NewTopHotKeyParser();
    public static String TOPURL = "http://top.taobao.com/";
    private static Logger LOGGER = LoggerFactory.getLogger(NewTopKeyNavSpider.class);

    /**
     * 获取所有的标签
     */
    public void getUrls() {

        JSONObject mods = getMods(TOPURL);
        if (mods == null) {
            return;
        }
        JSONObject tab = mods.getJSONObject("tab");
        JSONObject data = tab.getJSONObject("data");
        JSONArray tabs = data.getJSONArray("tabs");
        if (tabs == null) {
            return;
        }
        for (int i = 1; i < tabs.size(); i++) {
            JSONObject tabJson = JSON.parseObject(tabs.get(i).toString());
            String topId = tabJson.getString("id");
            String href = tabJson.getString("href").substring(2);
            String name = tabJson.getString("text");
            TopURLBase topURLLevel1 = new TopURLBase(1, href, "", topId, name).save();

            getNavs(TOPURL + href, topURLLevel1.getId());
        }
    }

    /**
     * 获取页面导航
     * @param url
     */
    public void getNavs(String url, Long parentBaseId) {

        JSONObject mods = getMods(url);
        if (mods == null) {
            return;
        }
        JSONObject nav = mods.getJSONObject("nav");
        JSONObject data = nav.getJSONObject("data");
        JSONArray common = data.getJSONArray("common");
        if (common == null) {
            return;
        }

        for (Object comm : common) {
            JSONObject commJson = JSON.parseObject(comm.toString());
            String commTitle = commJson.getString("text");
            TopURLBase topURLLevel2 = new TopURLBase(2, "", commTitle);
            topURLLevel2.setParentBaseId(parentBaseId);
            topURLLevel2.save();

            JSONArray subs = commJson.getJSONArray("sub");
            for (Object subObj : subs) {
                JSONObject subJson = JSON.parseObject(subObj.toString());
                String subTitle = subJson.getString("text");
                String value = subJson.getString("value");
                String suburl = subJson.getString("url").substring(2);
                TopURLBase topURLLevel3 = new TopURLBase(3, suburl, value, subTitle);
                topURLLevel3.type = TopURLBase.Type.IS_EXACT_CID;
                topURLLevel3.setParentBaseId(topURLLevel2.getId());
                topURLLevel3.save();
            }
        }
    }

    /**
     * 爬到的top词数据
     * @param url
     * @param subNavId
     */
    public void getTopKeys(String url, Long subNavId) {
        JSONObject mods = getMods(url);
        if (mods == null) {
            return;
        }
        JSONObject wbang = mods.getJSONObject("wbang");
        JSONObject data = wbang.getJSONObject("data");
        JSONArray list = data.getJSONArray("list");
        if (list == null) {
            return;
        }

        for (Object atomObj : list) {
            JSONObject atomJson = JSON.parseObject(atomObj.toString());
            JSONObject col1 = atomJson.getJSONObject("col1");
            Integer order = col1.getInteger("text");
            JSONObject col2 = atomJson.getJSONObject("col2");
            String name = col2.getString("text");
            JSONObject col4 = atomJson.getJSONObject("col4");
            Double attention = col4.getDouble("num");
            JSONObject col5 = atomJson.getJSONObject("col5");
            Integer updownCount = col5.getInteger("text");
            int upOrDown = col5.getIntValue("upOrDown");
            if(upOrDown == 0) {
                updownCount = - updownCount;
            }
            JSONObject col6 = atomJson.getJSONObject("col6");
            double rateChange = 0;
            if (col6 != null) {
                String updownrange = col6.getString("text");
                rateChange = Double.parseDouble(updownrange.substring(0, updownrange.length() - 1));
                if (upOrDown == 0) rateChange = -rateChange;
            }

            TopKey topKey = new TopKey();
            topKey.setSearchRank(order);
            topKey.setText(name);
            topKey.setFocusIndex(attention.intValue());
            topKey.setRankChange(updownCount);
            topKey.setRateChange(rateChange);
            topKey.setTopUrlBaseId(subNavId);

            topKey.insert();
            //save2Top20w(name, subNavId);

        }
    }

    /**
     * 数据载体
     * @param url
     * @return
     */
    public JSONObject getMods(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).timeout(3000).get();
            Element head = document.head();
            Element scriptTag = head.getElementsByTag("script").last();
            String scriptHtml = scriptTag.html();
            int startIndex = scriptHtml.indexOf("{");
            int endIndex = scriptHtml.lastIndexOf("}");
//            取得的json数据体
            String mainJsonStr = scriptHtml.substring(startIndex, endIndex + 1);

            JSONObject mainJsonObject = JSON.parseObject(mainJsonStr);
            JSONObject mods = mainJsonObject.getJSONObject("mods");
            return mods;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save2Top20w(String name, Long topUrlBaseId) {
        String insertSql = "INSERT INTO top20w (name, catlevel1, catlevel2, catlevel3) VALUES (?, ?, ?, ?)";
        String existSql = "SELECT 1 FROM top20w WHERE name = ? LIMIT 1";
        int exist = JDBCBuilder.singleIntQuery(DBBuilder.DataSrc.BASIC, existSql, name);
        if(exist == 1) return;
        TopURLBase level3TopUrl = TopURLBase.findById(topUrlBaseId);
        TopURLBase level2TopUrl = TopURLBase.findById(level3TopUrl.getParentBaseId());
        TopURLBase level1TopUrl = TopURLBase.findById(level2TopUrl.getParentBaseId());
        JDBCBuilder.insert(insertSql, name, level1TopUrl.getTag(), level2TopUrl.getTag(), level3TopUrl.getTag());

    }

    private NewTopHotKeyParser() {}

    public static NewTopHotKeyParser getInstance() {
        return HOT_KEY_PARSER;
    }
}
