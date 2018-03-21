
package actions.industry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import models.item.ItemCatHotProps;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import autotitle.ItemPropAction;
import bustbapi.BusAPI;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.SplitUtils;
import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemProp;
import com.taobao.api.domain.PropValue;

public class RemoteIndustryGetAction {

    private static final Logger log = LoggerFactory.getLogger(RemoteIndustryGetAction.class);

    private static final int ItemGetThreadSize = 32;

    private static PYFutureTaskPool<List<Item>> itemGetPool = new PYFutureTaskPool<List<Item>>(ItemGetThreadSize);

    /**
     * 通过api获取类目属性
     * @param cid
     * @param propsBean
     * @param isNeedVNameBase
     * @return
     * @throws ApiException
     * @throws ClientException 
     */
    public static List<CatPNameResult> getPNameResultFromApi(Long cid,
            boolean isNeedVNameBase) throws ApiException, ClientException {

        long startTime = System.currentTimeMillis();

        if (cid == null || cid <= 0L) {
            return new ArrayList<CatPNameResult>();
        }

        List<ItemProp> propList = ItemCatHotProps.fetchItemProps(cid);
        if (CommonUtils.isEmpty(propList)) {
            return new ArrayList<CatPNameResult>();
        }

        List<CatPNameResult> pNameResList = new ArrayList<CatPNameResult>();

        for (ItemProp prop : propList) {
            String pname = prop.getName();

            if (ItemPropAction.isKeyExcluded(pname)) {
                continue;
            }

            Long pid = prop.getPid();
            if (pid.longValue() == 20000) {
                //考虑品牌....
                //continue;
            }

            CatPNameResult pNameRes = new CatPNameResult(pid, pname);
            pNameResList.add(pNameRes);
            List<PropValue> valueList = prop.getPropValues();
            if (CommonUtils.isEmpty(valueList)) {
                log.warn(" fuck no values :" + new Gson().toJson(prop));
                continue;
            }

            for (PropValue value : valueList) {
                String vName = value.getNameAlias();
                if (StringUtils.isEmpty(vName)) {
                    vName = value.getName();
                }
                if (StringUtils.isEmpty(vName)) {
                    continue;
                }
                vName = vName.replace("（", "(");
                int qIndex = vName.indexOf("(");
                if (qIndex > 0) {
                    vName = vName.substring(0, qIndex);
                }

                String[] vNameArray = vName.split("/");
                if (vNameArray == null || vNameArray.length <= 0) {
                    continue;
                }
                for (String partVName : vNameArray) {
                    if (StringUtils.isEmpty(partVName)) {
                        continue;
                    }
                    //partVName = partVName.replace(" ", StringUtils.EMPTY);
                    CatVNameBaseBean vNameBase = new CatVNameBaseBean(value.getVid(), partVName);
                    pNameRes.addVNameBase(vNameBase);
                }

            }
        }

        long endTime = System.currentTimeMillis();
        log.info("end fetchItemProps for cid: " + cid
                + ", used " + (endTime - startTime) + " ms---------------------------");

        if (isNeedVNameBase == true) {
            putVNameBaseInfo(cid, pNameResList);
        }

        return pNameResList;
    }

    //在CatVNameBaseBean中加入click,pv等数据
    protected static void putVNameBaseInfo(Long cid, List<CatPNameResult> pNameList) throws ApiException,
            ClientException {
        if (CommonUtils.isEmpty(pNameList)) {
            return;
        }

        Set<String> vNameSet = new HashSet<String>();

        for (CatPNameResult pNameRes : pNameList) {
            if (pNameRes == null) {
                continue;
            }
            List<CatVNameBaseBean> vNameBaseList = pNameRes.getvNameBaseList();
            if (CommonUtils.isEmpty(vNameBaseList)) {
                continue;
            }
            for (CatVNameBaseBean vNameBase : vNameBaseList) {
                if (vNameBase == null) {
                    continue;
                }
                String vName = vNameBase.getVname();
                if (StringUtils.isEmpty(vName)) {
                    continue;
                }
                vNameSet.add(vName);
            }
        }

        long startTime = System.currentTimeMillis();

        Map<String, WordBaseBean> wordBaseMap = BusAPI.wordPv(vNameSet);

        long endTime = System.currentTimeMillis();

        log.info("end load wordbase bean for cid: " + cid + ", size: " + vNameSet.size()
                + ", used " + (endTime - startTime) + " ms---------------------------");

        if (CommonUtils.isEmpty(wordBaseMap)) {
            return;
        }

        for (CatPNameResult pNameRes : pNameList) {
            if (pNameRes == null) {
                continue;
            }
            List<CatVNameBaseBean> vNameBaseList = pNameRes.getvNameBaseList();
            if (CommonUtils.isEmpty(vNameBaseList)) {
                continue;
            }
            for (CatVNameBaseBean vNameBase : vNameBaseList) {
                if (vNameBase == null) {
                    continue;
                }
                String vName = vNameBase.getVname();
                if (StringUtils.isEmpty(vName)) {
                    continue;
                }

                WordBaseBean wordBase = wordBaseMap.get(vName);
                if (wordBase == null) {
                    continue;
                }
                Integer click = wordBase.getClick();
                if (click == null) {
                    click = 0;
                }
                vNameBase.setClick(click);

                Integer pv = wordBase.getPv();
                if (pv == null) {
                    pv = 0;
                }
                vNameBase.setPv(pv);

            }
        }
    }

    /**
     * 根据淘宝接口，获取item，并且放入Cache
     * @param numIidList
     * @return
     */
    public static Map<Long, IndustryItemInfo> fetchTaobaoItemMap(Set<Long> numIidSet) {

        Map<Long, IndustryItemInfo> itemMap = new HashMap<Long, IndustryItemInfo>();

        if (CommonUtils.isEmpty(numIidSet)) {
            return itemMap;
        }

        List<Long> notHitNumIidList = new ArrayList<Long>();
        for (Long numIid : numIidSet) {
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            IndustryItemInfo cacheItem = IndustryItemInfoCache.getItemFromCache(numIid);
            if (cacheItem == null) {
                notHitNumIidList.add(numIid);
                continue;
            } else {
                itemMap.put(numIid, cacheItem);
                continue;
            }
        }

        if (CommonUtils.isEmpty(notHitNumIidList)) {
            return itemMap;
        }

        List<IndustryItemInfo> itemFromApiList = fetchTaobaoItemsByApi(notHitNumIidList);

        if (CommonUtils.isEmpty(itemFromApiList)) {
            return itemMap;
        }

        for (IndustryItemInfo industryItem : itemFromApiList) {
            if (industryItem == null) {
                continue;
            }
            itemMap.put(industryItem.getNumIid(), industryItem);
        }

        return itemMap;
    }

    private static List<IndustryItemInfo> fetchTaobaoItemsByApi(List<Long> numIidList) {

        if (CommonUtils.isEmpty(numIidList)) {
            return new ArrayList<IndustryItemInfo>();
        }

        //每个线程要获取的宝贝数
        int repeateApiTimes = 3;//一个线程中，调用3次api，获取多个宝贝
        int eachThreadItemSize = ItemApi.ItemsListGet.MAX_NUMIID_LENGTH * repeateApiTimes;

        List<List<Long>> splitNumIidsList = SplitUtils.splitToSubLongList(numIidList, eachThreadItemSize);

        if (CommonUtils.isEmpty(splitNumIidsList)) {
            return new ArrayList<IndustryItemInfo>();
        }

        List<FutureTask<List<Item>>> promises = new ArrayList<FutureTask<List<Item>>>();

        for (List<Long> splitNumIids : splitNumIidsList) {
            if (CommonUtils.isEmpty(splitNumIids)) {
                continue;
            }
            ItemApi.ItemsListGet itemGetApi = new ItemApi.ItemsListGet(splitNumIids, false);
            promises.add(itemGetPool.submit(itemGetApi));
        }

        List<Item> tbItemList = new ArrayList<Item>();

        for (FutureTask<List<Item>> promise : promises) {

            try {
                List<Item> tempList = promise.get();
                if (CommonUtils.isEmpty(tempList)) {
                    continue;
                } else {
                    tbItemList.addAll(tempList);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);
            }
        }

        List<IndustryItemInfo> resultList = new ArrayList<IndustryItemInfo>();

        for (Item item : tbItemList) {
            if (item == null) {
                continue;
            }
            IndustryItemInfo industryItem = toIndustryItemInfo(item);
            if (industryItem == null) {
                continue;
            }

            resultList.add(industryItem);
        }

        for (IndustryItemInfo industryItem : resultList) {
            IndustryItemInfoCache.putItemToCache(industryItem.getNumIid(), industryItem);
        }

        return resultList;

    }

    private static IndustryItemInfo toIndustryItemInfo(Item item) {
        if (item == null) {
            return null;
        }

        Long numIid = item.getNumIid();
        if (numIid == null || numIid <= 0L) {
            return null;
        }
        Date delistDate = item.getDelistTime();
        if (delistDate == null) {
            return null;
        }
        long delistTime = delistDate.getTime();
        if (delistTime <= 0) {
            return null;
        }
        //long createTime = item.getCreated() == null ? 0 : item.getCreated().getTime();
        
        IndustryItemInfo industryItem = new IndustryItemInfo(numIid, delistTime, 
                item.getTitle(), item.getPicUrl());

        return industryItem;
    }

    /**
     * 行业宝贝信息的cache
     * @author ying
     *
     */
    public static class IndustryItemInfoCache {

        private static final String Prefix = "IndustryItemInfo-Cahce-";

        private static String genKey(Long numIid) {
            return Prefix + numIid;
        }

        public static void putItemToCache(Long numIid, IndustryItemInfo industryItem) {
            if (numIid == null || numIid <= 0L) {
                return;
            }

            String cacheKey = genKey(numIid);

            try {
                Cache.set(cacheKey, industryItem, "90min");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public static IndustryItemInfo getItemFromCache(Long numIid) {
            if (numIid == null || numIid <= 0L) {
                return null;
            }

            String cacheKey = genKey(numIid);

            try {

                IndustryItemInfo industryItem = (IndustryItemInfo) Cache.get(cacheKey);

                return industryItem;

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }

    }

    public static class IndustryItemInfo implements Serializable {

        private static final long serialVersionUID = -1L;

        private Long numIid;

        private Long delistTime;

        private String title;

        private String picUrl;

        public IndustryItemInfo() {
            super();
        }

        public IndustryItemInfo(Long numIid, Long delistTime, String title,
                String picUrl) {
            super();
            this.numIid = numIid;
            this.delistTime = delistTime;
            this.title = title;
            this.picUrl = picUrl;
        }

        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

        public Long getDelistTime() {
            return delistTime;
        }

        public void setDelistTime(Long delistTime) {
            this.delistTime = delistTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        

    }

    public static class CatPNameResult implements Serializable {

        private static final long serialVersionUID = -1L;

        private Long pid;

        private String pname;

        private List<CatVNameBaseBean> vNameBaseList = new ArrayList<CatVNameBaseBean>();

        public CatPNameResult() {
            super();
        }

        public CatPNameResult(Long pid, String pname) {
            super();
            this.pid = pid;
            this.pname = pname;
        }

        public Long getPid() {
            return pid;
        }

        public void setPid(Long pid) {
            this.pid = pid;
        }

        public String getPname() {
            return pname;
        }

        public void setPname(String pname) {
            this.pname = pname;
        }

        public List<CatVNameBaseBean> getvNameBaseList() {
            return vNameBaseList;
        }

        public void setvNameBaseList(List<CatVNameBaseBean> vNameBaseList) {
            this.vNameBaseList = vNameBaseList;
        }

        public void addVNameBase(CatVNameBaseBean vNameBase) {
            vNameBaseList.add(vNameBase);
        }

    }

    public static class CatVNameBaseBean implements Serializable {

        private static final long serialVersionUID = -1L;

        private Long vid;

        private String vname;

        private int pv;

        private int click;

        public CatVNameBaseBean() {
            super();
        }

        public CatVNameBaseBean(Long vid, String vname) {
            super();
            this.vid = vid;
            this.vname = vname;
        }

        public Long getVid() {
            return vid;
        }

        public void setVid(Long vid) {
            this.vid = vid;
        }

        public String getVname() {
            return vname;
        }

        public void setVname(String vname) {
            this.vname = vname;
        }

        public int getPv() {
            return pv;
        }

        public void setPv(int pv) {
            this.pv = pv;
        }

        public int getClick() {
            return click;
        }

        public void setClick(int click) {
            this.click = click;
        }

        public void updateByIWordBase(IWordBase base) {
            if (base == null) {
                return;
            }
            this.pv = base.getPv();
            this.click = base.getClick();
        }
    }
}
