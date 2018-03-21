
package result;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.industry.IndustryDelistResultAction.DelistItemInfo;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

public interface IItemBase {

    static final Logger log = LoggerFactory.getLogger(IItemBase.class);

    public static final String TAG = "IItemBase";

    public Long getNumIid();

    public String getTitle();

    public String getPicURL();

    public double getPrice();

    public long getDeListTime();

    public int getSalesCount();

    public void setSalesCount(int salesCount);

    @JsonAutoDetect
    public static class ItemBaseBean implements IItemBase, Comparable<ItemBaseBean> {

        @JsonProperty
        Long numIid;

        @JsonProperty
        String title;

        @JsonProperty
        String picURL;

        @JsonProperty
        double price;

        @JsonProperty
        long deListTime;

        @JsonProperty
        int salesCount = 0;

        @JsonProperty
        String delistTimeStr;

        @JsonProperty
        boolean isMustRecommend;

        @JsonProperty
        boolean isSalesFirst;
        
        @Transient
        @JsonProperty
        String onShowWindowReason;
        
        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

		public boolean isSalesFirst() {
			return isSalesFirst;
		}

		public void setSalesFirst(boolean isSalesFirst) {
			this.isSalesFirst = isSalesFirst;
		}

		public String getOnShowWindowReason() {
			return onShowWindowReason;
		}

		public void setOnShowWindowReason(String onShowWindowReason) {
			this.onShowWindowReason = onShowWindowReason;
		}

		public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getPicURL() {
            return picURL;
        }

        public void setPicURL(String picURL) {
            this.picURL = picURL;
        }

        public long getDeListTime() {
            return deListTime;
        }

        public void setDeListTime(long deListTime) {
            this.deListTime = deListTime;
        }
        
        public String getLeftTimeStr() {
            long leftTime = deListTime - System.currentTimeMillis();
            
            return DelistItemInfo.getLeftTimeStrStatic(leftTime);
        }

        public ItemBaseBean(Item item) {
            this.picURL = item.getPicUrl();
            this.title = item.getTitle();
            this.price = NumberUtil.parserDouble(item.getPrice(), 0d);
            this.numIid = item.getNumIid();
            this.deListTime = item.getDelistTime().getTime();
            this.delistTimeStr = DateUtil.formDateForLog(deListTime);
           
        }

        public static java.util.List<ItemBaseBean> buildFromTBItem(List<Item> items) {
            if (CommonUtils.isEmpty(items)) {
                return ListUtils.EMPTY_LIST;
            }

            List<ItemBaseBean> list = new ArrayList<ItemBaseBean>();
            for (Item item : items) {
                list.add(new ItemBaseBean(item));
            }
            return list;
        }

        public static List<ItemBaseBean> buildFromTBItem(User user, List<Item> items, boolean ensureSale) {

//            log.info(format("buildFromTBItem:user, items, ensureSale".replaceAll(", ", "=%s, ") + "=%s", user, items,
//                    ensureSale));
            List<ItemBaseBean> buildFromTBItem = buildFromTBItem(items);
            if (CommonUtils.isEmpty(buildFromTBItem)) {
                return ListUtils.EMPTY_LIST;
            }
//            log.info("[raw item base :]" + buildFromTBItem);

            if (!ensureSale) {
                return buildFromTBItem;
            }

            List<Long> ids = new ArrayList<Long>();
            for (IItemBase iItemBase : buildFromTBItem) {
                ids.add(iItemBase.getNumIid());
            }

//            Map<Long, Integer> res = ItemDao.findSalesByUserIdAndNumIids(user.getId(), StringUtils.join(ids, ','));
//            Map<Long, Integer> res = new HashMap<Long, Integer>();

//            if (ShowWindowConfig.enableItemTradeCache) {
            List<ItemPlay> list = UserHasTradeItemCache.getByUser(user, 5000);
            log.error(" item sale :" + list.size());
//            log.error("[sale list:]" + list);
            if (!CommonUtils.isEmpty(list)) {
                for (ItemPlay itemPlay : list) {
//                    res.put(itemPlay.getNumIid(), itemPlay.getSalesCount());
                    for (IItemBase iItemBase : buildFromTBItem) {
                        if (itemPlay.getNumIid().longValue() == iItemBase.getNumIid().longValue()) {
                            iItemBase.setSalesCount(itemPlay.getSalesCount());
                        }
                    }
                }
            }

            return buildFromTBItem;
        }

        public int getSalesCount() {
            return salesCount;
        }

        public void setSalesCount(int salesCount) {
            this.salesCount = salesCount;
        }

        @Override
        public int compareTo(ItemBaseBean o) {
        	if(isSalesFirst) {
        		if (this.salesCount < o.salesCount) {
                    return 1;
                }
                if (this.salesCount > o.salesCount) {
                    return -1;
                }
                if (this.deListTime < o.deListTime) {
                    return -1;
                }
                if (this.deListTime > o.deListTime) {
                    return 1;
                }
                return 0;
        	} else {
                if (this.deListTime < o.deListTime) {
                    return -1;
                }
                if (this.deListTime > o.deListTime) {
                    return 1;
                }
                return 0;
        	}
            
        }

        @Override
        public String toString() {
            return "ItemBaseBean [numIid=" + numIid + ", price=" + price + ", salesCount=" + salesCount + "]";
        }

        public String getDelistTimeStr() {
            return delistTimeStr;
        }

        public void setDelistTimeStr(String delistTimeStr) {
            this.delistTimeStr = delistTimeStr;
        }

        public boolean isMustRecommend() {
            return isMustRecommend;
        }

        public void setMustRecommend(boolean isMustRecommend) {
            this.isMustRecommend = isMustRecommend;
        }

    }

}
