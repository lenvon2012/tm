
package bustbapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.INCategoryChildTop;
import com.taobao.api.domain.INCategoryTop;
import com.taobao.api.domain.INWordCategory;
import com.taobao.api.request.SimbaInsightCatsforecastGetRequest;
import com.taobao.api.request.SimbaInsightWordscatsGetRequest;
import com.taobao.api.response.SimbaInsightCatsforecastGetResponse;
import com.taobao.api.response.SimbaInsightWordscatsGetResponse;

public class DirectBusApi {

    private static final Logger log = LoggerFactory.getLogger(DirectBusApi.class);

    public static final String TAG = "DirectBusApi";

//  static String TEMP_SID = "6100b2281aa7770f85b0f2d8b593da1926fad1e3723e13279742176";
//  static String TEMP_SID = "610161250cdaa1a3015b1a0a8a4fd31efa28054768bdc1479742176";
    /**
     * 比格希勃网络技术有限公司
     */
    static String TEMP_SID = "6201716f3133de34409c5451ZZ9a77135ad68bdf7e3b81b1132351118";

    /**
     * {"simba_insight_catsforecast_get_response":{"in_category_tops":{"i_n_category_top":[{"category_child_top_list":{"i_n_category_child_top":[{"category_desc":"女装\/女士精品>连衣裙","category_id":50010850,"category_name":"女装\/女士精品>连衣裙","category_properties_list":{}},{"category_desc":"女装\/女士精品","category_id":16,"category_name":"女装\/女士精品","category_properties_list":{}},{"category_desc":"童装\/童鞋\/亲子装","category_id":50008165,"category_name":"童装\/童鞋\/亲子装","category_properties_list":{}},{"category_desc":"女士内衣\/男士内衣\/家居服","category_id":1625,"category_name":"女士内衣\/男士内衣\/家居服","category_properties_list":{}}]}}]}}}
     * @param word
     * @return 
     */
    public static Map<String, Long> getCatPvMap(String word) {
        TaobaoClient client = TBApi.genBusClient();
        try {
            Map<String, Long> catPv = new HashMap<String, Long>();
            Map<Long, String> cidName = new HashMap<Long, String>();
            buildCatIdNameMap(word, client, cidName);
            buildCatPv(word, client, catPv, cidName);
            return catPv;
        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }
        return MapUtils.EMPTY_MAP;
    }

    static int MAX_WID_NUM = 200;

    static String urlBase = "http://chedao.taovgo.com/";

    public static Map<String, WordBaseBean> buildWordBase(Collection<String> colls, Long cid,
            Map<String, WordBaseBean> res) throws ApiException {
        TaobaoClient client = TBApi.genBusClient();
        SimbaInsightWordscatsGetRequest req = new SimbaInsightWordscatsGetRequest();
        List<String> wordCat = new ArrayList<String>();
        for (String string : colls) {
            wordCat.add(string + "^^" + cid);
        }
        req.setWordCategories(StringUtils.join(wordCat, ','));
        req.setFilter("COMPETITION,PV,CLICK");
        SimbaInsightWordscatsGetResponse response = client.execute(req, TEMP_SID);
        List<INWordCategory> list = response.getInWordCategories();
        if (CommonUtils.isEmpty(list)) {
            return MapUtils.EMPTY_MAP;
        }

//      Map<String, WordBaseBean> res = new Map<String, WordBaseBean>();
        for (INWordCategory catInfo : list) {
            Long pv = catInfo.getPv();
            if (NumberUtil.isNullOrZero(pv)) {
                continue;
            }
            WordBaseBean bean = new WordBaseBean();
            bean.setWord(catInfo.getWord());
            bean.setClick(catInfo.getClick().intValue());
            bean.setPv(catInfo.getPv().intValue());
            bean.setCompetition(catInfo.getCompetition().intValue());
            res.put(catInfo.getWord(), bean);
        }

        return res;
    }

    public static Map<String, Long> buildCatClick(Collection<String> colls, Long cid) throws ApiException {
        TaobaoClient client = TBApi.genBusClient();
        SimbaInsightWordscatsGetRequest req = new SimbaInsightWordscatsGetRequest();
        List<String> wordCat = new ArrayList<String>();
        for (String string : colls) {
            wordCat.add(string + "^^" + cid);
        }
        req.setWordCategories(StringUtils.join(wordCat, ','));
        req.setFilter("PV");
        SimbaInsightWordscatsGetResponse response = client.execute(req, TEMP_SID);
        List<INWordCategory> list = response.getInWordCategories();
        if (CommonUtils.isEmpty(list)) {
            return MapUtils.EMPTY_MAP;
        }

        Map<String, Long> res = new HashMap<String, Long>();
        for (INWordCategory catInfo : list) {
            Long pv = catInfo.getPv();
            if (NumberUtil.isNullOrZero(pv)) {
                continue;
            }
            res.put(catInfo.getWord(), pv);
        }

        return res;
    }

    private static void buildCatPv(String word, TaobaoClient client, Map<String, Long> catPv, Map<Long, String> cidName)
            throws ApiException {
        SimbaInsightWordscatsGetRequest req = new SimbaInsightWordscatsGetRequest();
        List<String> wordCat = new ArrayList<String>();
        Set<Long> cids = cidName.keySet();
        for (Long cid : cids) {
            wordCat.add(word + "^^" + cid);
        }

        req.setWordCategories(StringUtils.join(wordCat, ','));
        req.setFilter("COMPETITION,PV,CLICK");
        SimbaInsightWordscatsGetResponse response = client.execute(req, TEMP_SID);
        List<INWordCategory> list = response.getInWordCategories();
        if (CommonUtils.isEmpty(list)) {
            return;
        }

        for (INWordCategory catInfo : list) {
            Long cid = catInfo.getCategoryId();
            Long pv = catInfo.getPv();
            if (NumberUtil.isNullOrZero(pv)) {
                continue;
            }
            String cname = cidName.get(cid);
            if (cname == null) {
                continue;
            }

            catPv.put(cname, pv);
        }
    }

    private static void buildCatIdNameMap(String word, TaobaoClient client, Map<Long, String> map) throws ApiException {
        SimbaInsightCatsforecastGetRequest req = new SimbaInsightCatsforecastGetRequest();
        req.setWords(word);
        SimbaInsightCatsforecastGetResponse response = client.execute(req, TEMP_SID);
        List<INCategoryTop> tops = response.getInCategoryTops();
        if (CommonUtils.isEmpty(tops)) {
            return;
        }
        for (INCategoryTop top : tops) {
            map.put(top.getCategoryId(), top.getCategoryName());

            List<INCategoryChildTop> categoryChildTopList = top.getCategoryChildTopList();
            for (INCategoryChildTop child : categoryChildTopList) {
                map.put(child.getCategoryId(), child.getCategoryName());
            }
        }
    }

    public static Map<String, WordBaseBean> buildMultiCatClick(List<String> colls, Long cid) throws ApiException {
        if (CommonUtils.isEmpty(colls)) {
            return MapUtils.EMPTY_MAP;
        }

        int size = colls.size();
        Map<String, WordBaseBean> finalRes = new HashMap<String, WordBaseBean>();

        for (int i = 0; i < size; i += MAX_WID_NUM) {
            int end = i + MAX_WID_NUM;
            if (end > size) {
                end = size;
            }
            List<String> subList = colls.subList(i, end);
            buildWordBase(subList, cid, finalRes);
        }
        return finalRes;
    }

}
