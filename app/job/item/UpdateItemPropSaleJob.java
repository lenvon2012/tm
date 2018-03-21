/**
 * 
 */

package job.item;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import job.shop.CrawlSellerDSRJob;
import models.item.ItemCatPlay;
import models.item.ItemPropSale;
import models.item.NoPropsItemCat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchKeywordsUpdater;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import bustbapi.BusAPI;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;

import configs.TMConfigs;

/**
 * @author navins
 * @date: Mar 6, 2014 7:40:06 PM
 */
@On("0 0 1 * * ?")
public class UpdateItemPropSaleJob extends Job {

    private final static Logger log = LoggerFactory.getLogger(CrawlSellerDSRJob.class);

    @Override
    public void doJob() {
        if (!TMConfigs.Is_Update_ItemPropSale) {
            return;
        }

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != 1) {
            return;
        }

        // 删除历史数据
        ItemPropSale.dp.update("truncate table item_prop_sale");

        int ps = 50;
        int pn = 1;
        int totalCount = (int) ItemCatPlay.countCatAll();
        PageOffset po = new PageOffset(pn, ps);

        Map<Long, String> escapeCidMap = NoPropsItemCat.fetchCidsMap();
        if (CommonUtils.isEmpty(escapeCidMap)) {
            escapeCidMap = new HashMap<Long, String>();
        }

        while (po.getOffset() <= totalCount) {
            log.info("[UpdateItemPropSaleJob] doing pn: " + pn + " / total:" + totalCount);
            List<ItemCatPlay> catList = ItemCatPlay.findEachLevel2Cat(po);
            po = new PageOffset(++pn, ps);
            if (CommonUtils.isEmpty(catList)) {
                continue;
            }
            for (ItemCatPlay itemCat : catList) {
                if (escapeCidMap.containsKey(itemCat.getCid())) {
                    continue;
                }
                try {
                    updateEachCat(itemCat);
                } catch (ClientException e) {
                    log.warn(e.getMessage(), e);

                }
            }
        }
    }

    public static void updateEachCat(ItemCatPlay itemCat) throws ClientException {
        String[] catArr = itemCat.getName().split("/");
        if (catArr != null && catArr.length <= 0) {
            return;
        }

        HashMap<Long, MainSearchItemRank> itemMap = new HashMap<Long, MainSearchItemRank>();
        for (int i = 0; i < catArr.length; i++) {
            MainSearchParams params = new MainSearchParams(catArr[i], 10, "sale-desc");
            HashMap<Long, MainSearchItemRank> catItemMap = MainSearchKeywordsUpdater.doSearch(params);
            if (!CommonUtils.isEmpty(catItemMap)) {
                itemMap.putAll(catItemMap);
            }
        }

        final long cid = itemCat.getCid();
        List<Item> items = new ArrayList<Item>(new BusAPI.MultiItemApi(itemMap.keySet()).execute().values());
        final Map<Long, Item> rawItemMap = new HashMap<Long, Item>();
        for (Item item : items) {
            rawItemMap.put(item.getNumIid(), item);
        }

        new MapIterator<Long, MainSearchItemRank>(itemMap) {
            @Override
            public void execute(Entry<Long, MainSearchItemRank> entry) {
                MainSearchItemRank itemRank = entry.getValue();
                long sale = itemRank.getSalesCount();

//                Item item = new ItemApi.ItemPropsGet(itemRank.getNumIid()).call();
                Item itemWithProp = rawItemMap.get(entry.getKey());
                if (itemWithProp == null) {
                    return;
                }
                String propsName = itemWithProp.getPropsName();
                if (propsName == null) {
                    return;
                }
                String[] propsArr = propsName.split(";");
                if (propsArr == null || propsArr.length <= 0) {
                    return;
                }

                for (int i = 0; i < propsArr.length; i++) {
                    String prop = propsArr[i];
                    String[] arr = prop.split(":");
                    if (arr.length < 4) {
                        continue;
                    }
                    long pid = Long.valueOf(arr[0]);
                    long vid = Long.valueOf(arr[1]);
                    String pname = arr[2];
                    String vname = arr[3];
                    //modify by uttp
                    long totalPrice = itemRank.getPrice();
                    ItemPropSale.createOrAddCidPropSale(cid, pid, vid, pname, vname, sale, totalPrice);
                }
            }
        }.call();
    }

    /**
     * @param itemCat
     */
    public void genSpiderUrl(ItemCatPlay itemCat) {

    }
}
