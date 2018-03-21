
package models.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import jdp.ApiJdpAdapter;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.BusAPI;
import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemProp;
import com.taobao.api.domain.PropValue;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.response.ItempropsGetResponse;

@Entity(name = ItemCatHotProps.TABLE_NAME)
public class ItemCatHotProps extends GenericModel { //eg 属性分析----袖长、图案、材质

    private static final Logger log = LoggerFactory.getLogger(ItemCatHotProps.class);

    public static final String TAG = "ItemCatProps";

    public static final String TABLE_NAME = "item_cat_props";

    public ItemCatHotProps() {
        super();
    }

    public ItemCatHotProps(Long cid, long ts, String propJsonContent) {
        super();
        this.cid = cid;
        this.ts = ts;
        this.propJsonContent = propJsonContent;
    }

    @Id
    Long cid; //eg 女装---连衣裙

    long ts;

    @Lob
    String propJsonContent;

    @JsonAutoDetect
    static class PNameResult implements Comparable<PNameResult> {
        @JsonProperty
        Long pid = 0L; //eg 女装---连衣裙---材质

        @JsonProperty
        String pname = StringUtils.EMPTY; //属性名称比如材质

        @JsonProperty
        int pv = 1; //property view 属性查看总次数

        @JsonProperty
        int click = 0; //点击次数

        @JsonProperty
        List<VNameResult> list = new ArrayList<VNameResult>(); //属性具体名称分类---比如材质里面的雪纺、蕾丝、真丝、亚麻、纯棉等等

        public PNameResult(Long pid2, String name) {
            this.pid = pid2;
            this.pname = name;
        }

        @Override
        public int compareTo(PNameResult o) {
            return o.click - this.click;
        }

        public void addVNameRes(VNameResult res) {
            this.list.add(res);
        }

        public void buildQuotas(final Map<String, WordBaseBean> bases) {
            for (VNameResult vRes : this.list) {
                String vName = vRes.name;
                WordBaseBean base = bases.get(vName);
                vRes.updateByIWordBase(base);
//                log.info("[pv ]"+vRess.pv);
                PNameResult.this.pv += vRes.pv;
                PNameResult.this.click += vRes.click;
            }
            Collections.sort(this.list);
        }
    }

    @JsonAutoDetect
    static class VNameResult implements Comparable<VNameResult> {
        @JsonProperty
        Long vid = 0L; //属性具体分类名称id

        @JsonProperty
        String name = StringUtils.EMPTY; //属性具体分类名称name

        @JsonProperty
        int pv = 1; //具体属性查看次数

        @JsonProperty
        int click = 0; //具体属性点击次数

        public VNameResult(Long vid2, String nameAlias) {
            this.vid = vid2;
            this.name = nameAlias;
        }

        public void updateByIWordBase(IWordBase base) {
            if (base == null) {
                return;
            }
            this.pv = base.getPv();
            this.click = base.getClick();
        }

        @Override
        public int compareTo(VNameResult o) {
            return o.pv - this.pv;
        }

    }

    public static ItemCatHotProps _instance = new ItemCatHotProps();

    /**
     * TODO for dawei...
     * @param cid
     * @throws ApiException
     */
    public void brands(Long cid) throws ApiException {
        List<ItemProp> props = fetchItemProps(cid);
        for (ItemProp prop : props) {
            String name = prop.getName();
            if (ItemPropAction.isKeyExcluded(name)) {
                continue;
            }

            Long pid = prop.getPid();
            if (pid.longValue() == 20000) {
                prop.getPropValues();
            }
        }
    }

//    long testCid = 50012825L;
    public List<PNameResult> buildCatInfo(Long cid) { //建立类目信息
        Set<String> targetNames = new HashSet<String>();
        try {
            List<PNameResult> pResList = new ArrayList<PNameResult>();//建立PNameResult属性结果信息

            List<ItemProp> props = fetchItemProps(cid);
            if (CommonUtils.isEmpty(props)) {
                log.warn(" no prop list for cid :" + cid);
                return ListUtils.EMPTY_LIST;
            }

            for (ItemProp prop : props) {
                String name = prop.getName();
                if (ItemPropAction.isKeyExcluded(name)) {
                    continue;
                }

                Long pid = prop.getPid();
                if (pid.longValue() == 20000) {
                    //  不考虑品牌....
                    continue;
                }

                PNameResult pRes = new PNameResult(pid, name); //cat里面的属性eg.连衣裙----材质
                pResList.add(pRes);
                List<PropValue> values = prop.getPropValues(); //得到属性的值
                if (CommonUtils.isEmpty(values)) {
                    log.warn(" fuck no values :" + new Gson().toJson(prop));
                    continue;
                }

                for (PropValue value : values) { //属性的具体值
                    String vName = value.getNameAlias();
                    if (StringUtils.isEmpty(vName)) {
                        vName = value.getName();
                    }
                    int qIndex = vName.indexOf("（");
                    if (qIndex > 0) {
                        vName = vName.substring(0, qIndex);
                    }
                    qIndex = vName.indexOf("/");
                    if (qIndex > 0) {
                        vName = vName.substring(0, qIndex);
                    }

                    vName = vName.replace(" ", StringUtils.EMPTY);
                    VNameResult vRes = new VNameResult(value.getVid(), vName);
                    pRes.addVNameRes(vRes);
                    targetNames.add(vName);
                }
            }

            log.info("[target name ;]" + targetNames);
            List<String> list = new ArrayList<String>(targetNames);
//            Map<String, WordBaseBean> bases = BusAPI.buildMultiCatClick(list, cid);
//            Map<String, WordBaseBean> bases = BusAPI.buildMultiCatClick(list, cid);
            Map<String, WordBaseBean> bases = BusAPI.wordPv(list);

//            log.info("[return :bases]" + bases);
            for (PNameResult pNameResult : pResList) {
                pNameResult.buildQuotas(bases);
            }
            log.error(" panem list :" + pResList.size());
            Collections.sort(pResList);
            return pResList;

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);

        }
        return null;
    }

    /**
     * 获取某一个 类目cid所对应的所有属性
     * @param cid
     * @return
     * @throws ApiException
     */
    public static List<ItemProp> fetchItemProps(long cid) throws ApiException {
        TaobaoClient client = TBApi.genClient();
        ItempropsGetRequest req = new ItempropsGetRequest();
        req.setFields("pid,name,must,multi,prop_values");
        req.setCid(cid);
        ItempropsGetResponse response = client.execute(req);
        return response.getItemProps();
    }

    public static void getItemBrandProp(User user, Long numIid) {
        //ItemGet
//        ItemGet itemGet = ApiAdapter.get().findItem(user, numIid);
//        Item item = itemGet.call();
        Item item = ApiJdpAdapter.get(user).findItem(user, numIid);
        String propsName = item.getPropsName();
//      log.error(" prop names :" + propsName);
        String propAlias = item.getPropertyAlias();
        List<PropUnit> splitProp = ItemPropAction.mergePropAlis(propsName, propAlias);
        for (StringPair pair : splitProp) {
//            log.info("[for pair :]" + pair);
            if ("货号".equals(pair.getKey()) || "ISBN编号".equals(pair.getKey())) {
            } else if ("品牌".equals(pair.getKey())) {
                String brandName = pair.getValue();
                if (brandName.contains("/")) {
                    String[] splits = brandName.split("/");
                    for (String split : splits) {
//                        this.brandNameList.add(split.toLowerCase());
                    }
                } else {
//                    this.brandNameList.add(brandName.toLowerCase());
                }
            }
        }
    }

    public static String getRecent(Long cid) {
        ItemCatHotProps prop = ItemCatHotProps.findById(cid);
        long lower = DateUtil.formCurrDate() - DateUtil.TWO_DAY_MILLIS;
        if (prop != null && prop.ts > lower) {
            return prop.propJsonContent;
        }

        List<PNameResult> list = _instance.buildCatInfo(cid);
        if (list == null) {
            log.warn(" no pname result built...");
            return StringUtils.EMPTY;
        }

        String content = JsonUtil.getJson(list);
        if (prop == null) {
            prop = new ItemCatHotProps(cid, System.currentTimeMillis(), content);
        } else {
            prop.propJsonContent = content;
            prop.ts = System.currentTimeMillis();
        }
        prop.save();
        return prop.propJsonContent;
    }

    public static String getCachedRecent(Long cid) {
        String key = TAG + cid;
        /*String cached = (String) Cache.get(key);
        if (cached != null) {
            log.info("[caced for ]" + cid);
            return cached;
        }*/

//        log.info("try new cid:" + cid);
        String recent = getRecent(cid);
        if (recent == null) {
            ItemCatPlay cat = ItemCatPlay.findByCid(cid);
            Long parentCid = cat.getParentCid();
            recent = getRecent(parentCid);
            log.info("[go parent:]" + cid + " with parent cid :" + parentCid);
            if (recent == null) {
                log.warn("[go parent:]" + cid + " with parent cid :" + parentCid + " with res null");
                return null;
            }
        }

        //Cache.set(key, recent, "24h");
        return recent;
    }

}
