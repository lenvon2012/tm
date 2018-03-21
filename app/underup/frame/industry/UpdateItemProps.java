package underup.frame.industry;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPropSale;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.CatTopSaleItemSQL.HotWordInfo;

import org.codehaus.jackson.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.BusAPI;

import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;

import play.jobs.Job;
import play.jobs.OnApplicationStart;

//@OnApplicationStart
public class UpdateItemProps extends Job {
    public static final Logger log = LoggerFactory.getLogger(UpdateItemProps.class);

    @Override
    public void doJob() {
        long t2, t3;
        t2 = System.currentTimeMillis();
        List<HotWordInfo> catHotItems = CatTopSaleItemSQL.getCidInfos(); 
        t3 = System.currentTimeMillis();
        log.info("----------------------------------------------------------------------------------get cid information "
                + (t3 - t2));                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
        if (catHotItems == null) {
            return;
        }
        for (HotWordInfo hotWordInfo : catHotItems) {
            Map<String, ItemProps> itemPropsMap = new HashMap<String, ItemProps>();
            List<CatTopSaleItemSQL> catTopSaleItems = CatTopSaleItemSQL.findByCid(hotWordInfo.getCid(),
                    hotWordInfo.getYear(), hotWordInfo.getMonth()); // 根据backcid得到item的信息
            if (catTopSaleItems == null) {
                log.error("get backcid " + hotWordInfo.getCid() + "from cat_top_sale_item failed ......... ");
                continue;
            }
            long t, t1;
            t = System.currentTimeMillis();
            for (CatTopSaleItemSQL catTopSaleItem : catTopSaleItems) {
                String propsUtil = catTopSaleItem.getProps(); // 得到每一个item的属性信息
                if (propsUtil == null) {
                    log.error("the props of item is null");
                    continue;
                }
                List<PropUnit> propNodes = splitProps1(propsUtil); // 对item的属性信息进行分析,得到每个item的属性和对应的值
                if (propNodes == null) {
                    continue;
                }
                for (PropUnit propNode : propNodes) {
                    String pName = propNode.getKey();
                    String vName = propNode.getValue();
                    long pid = propNode.getPid();
                    long vid = propNode.getVid();
                    long year = catTopSaleItem.getYear();
                    long month = catTopSaleItem.getMonth();
                    String key = "" + catTopSaleItem.getCid() + pName.replace(" ", "") + vName.replace(" ", "") + year + month;
                    ItemProps model = itemPropsMap.get(key);
                    if (model == null) {
                        model = new ItemProps();
                        model.setCid(catTopSaleItem.getCid());
                        model.setPid(pid);
                        model.setVid(vid);
                        model.setVname(vName);
                        model.setPName(pName);
                        model.setYear(year);
                        model.setMonth(month);
                        itemPropsMap.put(key, model);
                    }
                    model.setTotalPrice(model.getTotalPrice() + (long) catTopSaleItem.getPrice());
                    model.setSale(model.getSale() + catTopSaleItem.getTradeNum());
                }
            }
            t1 = System.currentTimeMillis();
            log.info("------------------------------------------------------------------------------切取" + catTopSaleItems.size() + "所需要的时间为" + (t1 - t));
            ItemProps.insertPatch(itemPropsMap);
            t = System.currentTimeMillis();
            log.info("------------------------------------------------------------------------------属性" + "存入数据库所需要的时间为：" + (t - t1));
        }
        //触发hotword的job
        new HotSearchWord().doJob();
    }

    public static List<PropUnit> splitProps1(String props) {
        if (StringUtils.isEmpty(props)) {
            return null;
        }
        List<PropUnit> propUtils = new ArrayList<PropUnit>();
        try {
            JSONArray jsonArray = new JSONArray(props);
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject json = new JSONObject(jsonArray.getJSONObject(i).toString());
                String pname = json.getString("key").trim();
                String vname = json.getString("value").trim();
                long vid = json.getLong("vid");
                long pid = json.getLong("pid");
                PropUnit propUtil = new PropUnit(pid, vid, pname, vname);
                propUtils.add(propUtil);
            }
        } catch (JSONException e) {
            log.info(e.getMessage());
        }

        return propUtils;
    }

}
