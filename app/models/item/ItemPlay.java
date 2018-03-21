
package models.item;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import job.showwindow.ShowWindowExecutor;
import models.defense.ItemBuyLimit;
import models.defense.ItemPass;
import models.showwindow.DropWindowTodayCache;
import models.showwindow.OnWindowItemCache;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import result.IItemBase;
import titleDiag.DiagResult;
import transaction.DBBuilder.DataSrc;
import actions.DiagAction;
import actions.industry.IndustryDelistGetAction;
import autotitle.AutoSplit;
import bustbapi.FenxiaoApi.FenxiaoProductBean;
import cache.UserHasTradeItemCache;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import codegen.TaobaoObjWrapper;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;

import configs.TMConfigs;
import configs.TMConfigs.App;
import controllers.APIConfig;
import dao.UserDao;
import dao.item.ItemDao;

/**
 * 最新的Item
 *
 *
 */
@Entity(name = ItemPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "userId", "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn", "propsName", "maxKeywordAllowPrice"
})
public class ItemPlay extends GenericModel implements TaobaoObjWrapper<Item>, PolicySQLGenerator, IItemBase,
        Serializable, Comparable<ItemPlay> {

    private static final long serialVersionUID = -4311305298240916427L;

    private static final Logger log = LoggerFactory.getLogger(ItemPlay.class);

    public static final String TABLE_NAME = "item";

    public static ItemPlay EMPTY = new ItemPlay();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Id
    @JsonProperty(value = "numIid")
    @PolicySQLGenerator.CodeNoUpdate
    public Long numIid;

    @Index(name = "user_id")
    @PolicySQLGenerator.CodeNoUpdate
    public Long userId;

    @Index(name = "ts")
    public Long ts;

    public String title;

    @Transient
    public String detailURL;

    @Transient
    public boolean isOptimised;
    
    @Transient
    public Long created;

    @Transient
    public Long lastOptimiseTs;
    
    @Transient
    public int planCount;
    
    @Transient
    public double minPrice;

    //    @JsonProperty(value = "pic")
    public String picURL;

    public Long cid;

    /**
     * let's store this with modified time...
     */
    public Long listTime = 0L;

    public long deListTime = 0L;

    public double price;

    public String sellerCids;

    public static class Status {
        public final static int INSTOCK = 0;

        public final static int ONSALE = 1;
    }

    public int status;

    public Integer type = 0;

    public int quantity;

    public int salesCount = 0;
    
    public int recentSalesCount = 0;

    public int score;

    @Transient
    public String WhyOnShowWindow;

    @Transient
    public ItemBuyLimit itemLimit;

    @Transient
    public ItemPass itemPass;

    @Column(columnDefinition = "text NOT NULL")
    public String propsName = StringUtils.EMPTY;

    @Transient
    String outerId;

    @Transient
    String toBackTitle;

    public static class Type {
        public static final int NN_PROMOTING = 0;

        public static final int PROMOTING = 1;

        public static final int RELATED = 2;

        public static final int POPULARIZED = 4;

        public static final int IS_FENXIAO = 8;

        public static final int IS_VIRTUAL = 16;
    }

    @Transient
    private long vgItemId;

    public long getVgItemId() {
        return vgItemId;
    }

    public void setVgItemId(long vgItemId) {
        this.vgItemId = vgItemId;
    }
    
    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }
    
    public int getPlanCount() {
        return planCount;
    }

    public void setPlanCount(int planCount) {
        this.planCount = planCount;
    }

    public Long getLastOptimiseTs() {
        return lastOptimiseTs;
    }

    public void setLastOptimiseTs(Long lastOptimiseTs) {
        this.lastOptimiseTs = lastOptimiseTs;
    }

    public String getWhyOnShowWindow() {
        return WhyOnShowWindow;
    }

    public void setWhyOnShowWindow(String whyOnShowWindow) {
        WhyOnShowWindow = whyOnShowWindow;
    }

    public String getToBackTitle() {
        return toBackTitle;
    }

    public void setToBackTitle(String toBackTitle) {
        this.toBackTitle = toBackTitle;
    }

    public ItemPlay() {
        super();
    }

    public ItemPlay(Long userId, Long numIid, Long cid, String picURL, double price, String title, int type,
            int quantity, int recentSalesCount, int salesCount, int status, String sellerCids) {
        this(userId, numIid, cid, picURL, price, title, type, quantity, recentSalesCount, salesCount, status, 80, sellerCids);
    }

    public ItemPlay(Long userId, Long numIid, Long cid, String picURL, double price, String title, int type,
            int quantity, int recentSalesCount, int salesCount, int status, int score, String sellerCids) {
        this.userId = userId;
        this.numIid = numIid;
        this.cid = cid;
        this.picURL = picURL;
        this.price = price;
        this.title = title;
        this.type = type;
        this.quantity = quantity;
        this.recentSalesCount = recentSalesCount;
        this.salesCount = salesCount;
        this.status = status;
        this.score = score;
        this.sellerCids = sellerCids;
    }

    public ItemPlay(Long userId, Long numIid, Long cid, String picURL, double price, String title, int type,
            int quantity, int recentSalesCount, int salesCount, int status, int score) {
        this.userId = userId;
        this.numIid = numIid;
        this.cid = cid;
        this.picURL = picURL;
        this.price = price;
        this.title = title;
        this.type = type;
        this.quantity = quantity;
        this.recentSalesCount = recentSalesCount;
        this.salesCount = salesCount;
        this.status = status;
        this.score = score;
    }

    public ItemPlay(Long userId, Item item) {
        this(userId, DateUtil.formCurrDate(), item);
    }

//    public ItemPlay(Long userId, Item item, int saleCount, long quantity) {
//        this(userId, DateUtil.formCurrDate(), item, saleCount, quantity);
//    }

    public ItemPlay(Long userId, Long ts, Item item) {
        this(userId, ts, item, item.getAfterSaleId() == null ? -1 : item.getAfterSaleId().intValue(), item.getVolume() == null ? -1 : item.getVolume().intValue(), 0);
    }

    public ItemPlay(Long userId, Long ts, Item item, int recentSaleCount, int saleCount, long quantity) {
        this.numIid = item.getNumIid();

        if (recentSaleCount >= 0) {
            this.recentSalesCount = recentSaleCount;
        }
        if (saleCount >= 0) {
            this.salesCount = saleCount;
        }
        this.quantity = (int) quantity;

        this.userId = userId;
        this.ts = ts;

        try {
            updateWrapper(item);
        } catch (Exception e) {
            log.error(" erro item :" + new Gson().toJson(item));
            log.warn(e.getMessage(), e);
        }

    }

    public int getMaxKeywordAllowPrice() {
        if (this.salesCount >= 100) {
            return 150;
        } else if (this.salesCount >= 50) {
            return 100;
        } else if (this.salesCount >= 30) {
            return 80;
        } else if (this.salesCount >= 10) {
            return 70;
        } else if (this.salesCount > 0) {
            return 60;
        } else {
            return 50;
        }
    }

    static String EXIST_ID_QUERY = "select numIid from item%s where numIid = ? ";

    public static long findExistId(Long userId, Long numIid) {
        String query = ItemDao.genShardQuery(EXIST_ID_QUERY, userId);
//        log.info("[query :]" + query);
        return dp.singleLongQuery(query, numIid);
//        log.info("[query : ]" + query);
    }

    @Override
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "numIid";
    }

    @Override
    public void setId(Long id) {
        this.numIid = id;
    }

    @Override
    public boolean jdbcSave() {
//        log.warn("write for :" + this);

        try {
//            log.info("[current userId :]"+this.userId);
            long existdId = findExistId(this.userId, this.numIid);

//            log.info("[exist ids :]" + existdId);
            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    @Override
    public String getIdName() {
        return "numIid";
    }

    @Override
    public Long getId() {
        return this.numIid;
    }

    static String insertSQL = "insert into `item%s`(`numIid`,`userId`,`ts`,`title`,`picURL`,`cid`,`listTime`,"
            + "`deListTime`,`price`,`sellerCids`,`type`,`recentSalesCount`,`salesCount`,`quantity`,`propsName`,`status`,`score`) "
            + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = dp.insert(true, ItemDao.genShardQuery(insertSQL, userId), this.numIid, this.userId, this.ts,
                this.title, this.picURL, this.cid, this.listTime, this.deListTime, this.price, this.sellerCids,
                this.type, this.recentSalesCount, this.salesCount, this.quantity, this.propsName, this.status, this.score);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    static String updateSQL = "update `item%s` set   `ts` = ?, `title` = ?, `picURL` = ?, `cid` = ?, `listTime` = ?, "
            + "`deListTime` = ?, `price` = ?, `sellerCids` = ?, `type` = ?, `propsName` = ?,`status` = ?,`score` = ?,`recentSalesCount` = ?,`salesCount` = ?,`userId` = ? where `numIid` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.update(ItemDao.genShardQuery(updateSQL, userId), this.ts, this.title, this.picURL,
                this.cid, this.listTime, this.deListTime, this.price, this.sellerCids, this.type, this.propsName,
                this.status, this.score, this.recentSalesCount, this.salesCount, this.userId, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }

    static String updateSalesCountSQL = "update `item%s` set  `type` = ?, `salesCount` = ?, `quantity` = ?  where `numIid` = ? ";

    public boolean rawSalesCountUpdate() {
        long updateNum = dp.update(ItemDao.genShardQuery(updateSalesCountSQL, userId), this.type, this.salesCount,
                this.quantity, this.getId());

        if (updateNum == 1) {

            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);

            return false;
        }
    }

    @JsonProperty
    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailURL() {
        return detailURL;
    }

    public void setDetailURL(String detailURL) {
        this.detailURL = detailURL;
    }

    public String getRecommendTitle() {
        return recommendTitle;
    }

    public boolean isOptimised() {
        return isOptimised;
    }

    public void setOptimised(boolean isOptimised) {
        this.isOptimised = isOptimised;
    }

    public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getListTime() {
        return listTime;
    }

    public void setListTime(Long listTime) {
        if (listTime == null) {
            this.listTime = -1L;
        } else {
            this.listTime = listTime;
        }
    }

    public void setDeListTime(long deListTime) {
        this.deListTime = deListTime;
    }

//    public void ssetDelistTime(long delistTime) {
//        if (delistTime <= 0L) {
//            this.deListTime = delistTime;
//        }
//        this.deListTime = delistTime % DateUtil.WEEK_MILLIS;
//    }

    public long getDeListTime() {
        if (this.deListTime < 0L) {
            return this.deListTime;
        }

        long weekMillis = DateUtil.WEEK_MILLIS;
        long curr = System.currentTimeMillis();
        long newDelistTime = curr / weekMillis * weekMillis + this.deListTime % weekMillis;
        if (newDelistTime < curr) {
            newDelistTime += weekMillis;
        }
        return newDelistTime;

    }

    public long ggetDelistTime() {
        if (this.deListTime < 0L) {
            return this.deListTime;
        }

        long weekMillis = DateUtil.WEEK_MILLIS;
        long curr = System.currentTimeMillis();
        long newDelistTime = curr / weekMillis * weekMillis + this.deListTime % weekMillis;
        if (newDelistTime < curr) {
            newDelistTime += weekMillis;
        }
        return newDelistTime;
    }

    public static long buildDelistFormTime(long time) {
        return time % DateUtil.WEEK_MILLIS;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItemSellerCids() {
        return sellerCids;
    }
    
    public String getSellerCids() {
        return sellerCids;
    }

    public void setSellerCids(String sellerCids) {
        this.sellerCids = sellerCids;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @JsonProperty
    public int getTradeItemNum() {
        return this.salesCount;
    }

    @JsonProperty
    public double tradeAmount() {
        return 0d;
    }

    @JsonProperty
    public int getRemain() {
        return this.quantity;
    }

    @JsonProperty
    public String getName() {
        return this.title;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRecentSalesCount() {
		return recentSalesCount;
	}

	public void setRecentSalesCount(int recentSalesCount) {
		this.recentSalesCount = recentSalesCount;
	}

	public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public ItemBuyLimit getItemLimit() {
        return itemLimit;
    }

    public void setItemLimit(ItemBuyLimit itemLimit) {
        this.itemLimit = itemLimit;
    }

    public ItemPass getItemPass() {
        return itemPass;
    }

    public void setItemPass(ItemPass itemPass) {
        this.itemPass = itemPass;
    }

    public boolean rawDelete() {
        return dp.update(ItemDao.genShardQuery("delete from item%s where `numIid` = ? ", userId), this.getNumIid()) > 0L;

    }

    @Override
    public boolean isSameEntity(Item t) {
        return false;
    }

    @Override
    public boolean isStatusChaned(Item t) {
        return false;
    }

//
//    public void updateWithTitleAndScore(Item item) {
//        if (item == null) {
//            return;
//        }
//
//        this.setTitle(item.getTitle());
//        if (APIConfig.get().isItemScoreRelated()) {
//            this.setScore(DiagAction.diag(item).getScore());
//        }
//        this.jdbcSave();
//    }

    public void updateWithTitleAndScore(Item item, String newTitle) {
        this.setTitle(newTitle);
        if (APIConfig.get().isItemScoreRelated()) {
            this.setScore(DiagAction.diag(item).getScore());
        }
        this.jdbcSave();
    }

    public boolean updateItemBaseInfo(Item item) {
        if (!StringUtils.equals(this.title, item.getTitle())) {
            if (APIConfig.get().isItemScoreRelated()) {
                buildScore(item);
            } else {
                this.title = item.getTitle();
            }
        }

        this.title = item.getTitle();
        this.detailURL = item.getDetailUrl() == null ? App.TAOBAO_ITEM_URL.concat(String.valueOf(numIid)) : item
                .getDetailUrl();
        this.picURL = item.getPicUrl();
        this.cid = item.getCid();

        setListTime(item.getModified() == null ? -1L : item.getModified().getTime());
        setDeListTime(item.getDelistTime() == null ? -1L : item.getDelistTime().getTime());

        this.price = CommonUtils.String2Double(item.getPrice());
        this.sellerCids = item.getSellerCids();
        if (item.getApproveStatus().equals("onsale")) {
            this.status = Status.ONSALE;
        } else {
            this.status = Status.INSTOCK;
        }
        this.type = buildItemType(item, type);

        this.quantity = item.getNum() == null ? 0 : item.getNum().intValue();

        return true;
    }

    public static Integer buildItemType(Item item, Integer type) {
        if (type == null) {
            type = 0;
        }
        if (item.getIsFenxiao() != null && item.getIsFenxiao().intValue() > 0) {
            type |= Type.IS_FENXIAO;
        }
        if (item.getIsVirtual() != null) {
            if (item.getIsVirtual()) {
                type |= Type.IS_VIRTUAL;
            } else {
                type &= ~Type.IS_VIRTUAL;
            }
        }

        return type;
    }

    private void buildScore(Item item) {
        User user = UserDao.findById(userId);
        if (user == null) {
            return;
        }

        try {
            DiagResult doDiag = DiagAction.doDiag(user, item, item.getTitle(), this.salesCount);
            if (doDiag == null) {
                return;
            }
            item.setScore(new Long(doDiag.getScore()));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public Item toItem() {
        Item item = new Item();
        item.setNumIid(this.numIid);
        item.setTitle(this.getTitle());
        item.setDetailUrl(this.getDetailURL());
        item.setPicUrl(this.getPicURL());
        item.setCid(this.getCid());
        if (getListTime() > 0L) {
            item.setModified(new Date(getListTime()));
        }

        if (getDeListTime() > 0L) {
            item.setDelistTime(new Date(getDeListTime()));
        }
        item.setPrice(NumberUtil.getConversionFormat(getPrice()));
        item.setSellerCids(getSellerCids());
        item.setApproveStatus(this.status == Status.ONSALE ? "onsale" : "instock");
        item.setNum(new Long(getQuantity()));
        User user = UserDao.findById(userId);
        if (user != null) {
            item.setNick(user.getUserNick());
        }

        return item;
    }

    @Override
    public boolean updateWrapper(Item item) {

        updateItemBaseInfo(item);

        if (!StringUtils.isBlank(item.getPropsName())) {
            this.propsName = item.getPropsName();
        }

        this.score = item.getScore() == null ? this.score : item.getScore().intValue();
        this.recentSalesCount = item.getAfterSaleId() == null ? 0 : item.getAfterSaleId().intValue();
        this.salesCount = item.getVolume() == null ? 0 : item.getVolume().intValue();

        return true;
    }

    public boolean isBaseInfoEqual(Item item) {
        // check listTime

        // check delistTime
        Long itemDeListTime = item.getDelistTime() == null ? -1L : item.getDelistTime().getTime();
//        log.info("local delist time : " + DateUtil.formDateForLog(this.getDeListTime()) + "for remote:"
//                + DateUtil.formDateForLog(itemDeListTime.longValue()));

        final User user = UserDao.findById(this.userId);

        if (this.getDeListTime() != itemDeListTime.longValue()) {
            if (user != null && user.isShowWindowOn()) {
                DropWindowTodayCache.updateDeslistTime(item.getNumIid(), itemDeListTime);
                OnWindowItemCache.get().checkRefresh(user, numIid);
            }

            return false;
        }

        // check title
        if (!StringUtils.equals(this.title, item.getTitle())) {
//            log.info("no title for local:[" + title + "] remote: [" + item.getTitle() + "]");
            return false;
        }

        if (!StringUtils.equals(this.picURL, item.getPicUrl())) {
//            log.info("no pic url for local:" + picURL + " remotes pic url :" + item.getPicUrl());
            return false;
        }

        // check cid
        if (this.cid != null && item.getCid() != null && this.cid.longValue() != item.getCid().longValue()) {
            log.info("no cid local :[" + cid + "] remote: " + item.getCid());
            return false;
        }

        // check price
        if (this.price != CommonUtils.String2Double(item.getPrice())) {
            log.info("no price :[" + price + "] remote[" + item.getPrice() + "]");
            return false;
        }

        // check status
        int item_status;
        if (item.getApproveStatus().equals("onsale")) {
            item_status = Status.ONSALE;
        } else {
            item_status = Status.INSTOCK;
        }

        if (this.status != item_status) {
//            log.info("no status local :[" + this.status + " with remote:" + item.getApproveStatus());
            OnWindowItemCache.get().checkRefresh(user, numIid);
//            UserHasTradeItemCache.removeForChange(user, this.numIid);
            UserHasTradeItemCache.clear(user);
            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                @Override
                public ItemPlay call() throws Exception {
                    new ShowWindowExecutor(user).doJob();
                    return null;
                }
            });

            return false;
        }

        int newQuantity = item.getNum() == null ? 0 : item.getNum().intValue();
        if (this.quantity != newQuantity) {
            return false;
        }

        // check sellerCids
        if (!StringUtils.equals(this.sellerCids, item.getSellerCids())) {
//            log.info("no sellercids:" + item.getSellerCids() + " to this:" + this.sellerCids);
            return false;
        }

        return true;
    }

    public boolean isEqualToItem(Item item) {
        if (!isBaseInfoEqual(item)) {
            return false;
        }
        long itemListTime = item.getModified() == null ? -1L : item.getModified().getTime();
        if (this.listTime != null && this.listTime.longValue() != itemListTime) {
            if (itemListTime <= 0L) {
                log.info("not numiid[" + numIid + "] modified time  [local]"
                        + DateUtil.formDateForLog(this.listTime == null ? 0L : this.listTime.longValue())
                        + "  remote :" + new Gson().toJson(item));
            }

//            log.info("not numiid[" + numIid + "] modified time  [local]"
//                    + DateUtil.formDateForLog(this.listTime == null ? 0L : this.listTime.longValue())
//                    + "  remote :" + DateUtil.formDateForLog(itemListTime));
            return false;
        }
        // check score
        int item_score = item.getScore() == null ? this.score : item.getScore().intValue();
        if (this.score != item_score) {
            log.info("no score");
            return false;
        }
        
        // check recentSalesCount
        int newRecentSaleCount = item.getAfterSaleId() == null ? 0 : item.getAfterSaleId().intValue();
        if (newRecentSaleCount != this.recentSalesCount) {
            return false;
        }

        // check salesCount
        int newSaleCount = item.getVolume() == null ? 0 : item.getVolume().intValue();
        if (newSaleCount != this.salesCount) {
            return false;
        }

        return true;
    }

    public String getPropsName() {
        return propsName;
    }

    public void setPropsName(String propsName) {
        this.propsName = propsName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ItemPlay [numIid=" + numIid + ", userId=" + userId + ", deListTime="
                + DateUtil.formDateForLog(getDeListTime()) + ", price=" + price + ", status=" + status + ", type="
                + type + ", salesCount="
                + salesCount + ", score=" + score + "]";
    }

    public String getACidKey() {
        String key = StringUtils.EMPTY;
        long aCid = getCid();
        if (aCid <= 0) {
            try {
                List<String> execute = new AutoSplit(getTitle(), ListUtils.EMPTY_LIST, false).execute();
                if (!CommonUtils.isEmpty(execute)) {
                    return key;
                }
                return execute.get(execute.size() - 1);

            } catch (ClientException e) {
                log.warn(e.getMessage(), e);
                return key;
            }

        }

        ItemCatPlay findByCid = ItemCatPlay.findByCid(aCid);
        if (findByCid == null) {
            return key;
        }

        String[] split = StringUtils.split(findByCid.name.replaceAll(" ", StringUtils.EMPTY), '/');
        if (ArrayUtils.isEmpty(split)) {
            return key;
        }

        String first = split[0];
        return first;
    }

    /**
     * 如果设置了关联，那么就该一下这个宝贝在数据库里的状态
     */
    public void setRelated() {
        this.type |= Type.RELATED;
    }

    public void setUnRelated() {
        this.type &= ~Type.RELATED;
    }

    public boolean isRelated() {
        return (this.type & Type.RELATED) > 0;
    }

    public void setPopularized() {
        this.type |= Type.POPULARIZED;
    }

    public void setUnPopularized() {
        this.type &= ~Type.POPULARIZED;
    }

    public boolean isPopularized() {
        return (this.type & Type.POPULARIZED) > 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Transient
    String recommendTitle = StringUtils.EMPTY;

    public void setRecommendTitle(String recommend) {
        this.recommendTitle = recommend;
    }

    public static class ItemPageBean {
        Long numIid;

        Long day;

        Long sellerId;

        int share;

        int pv;

        boolean isBShop;

        public boolean isBShop() {
            return isBShop;
        }

        public void setBShop(boolean isBShop) {
            this.isBShop = isBShop;
        }

        public int getSale() {
            return sale;
        }

        public void setSale(int sale) {
            this.sale = sale;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getShare() {
            return share;
        }

        public int getPv() {
            return pv;
        }

        public ItemPageBean(Long numIid, Long day) {
            super();
            this.numIid = numIid;
            this.day = day;
        }

        public ItemPageBean(Long numIid, Long day, Long sellerId) {
            super();
            this.numIid = numIid;
            this.day = day;
            this.sellerId = sellerId;
        }

        public ItemPageBean(ItemThumb itemThumb, Long sellerId2) {
            this.numIid = itemThumb.getId();
            this.sellerId = sellerId2;
            this.day = DateUtil.formCurrDate();
            this.sale = itemThumb.getTradeNum();
            this.price = itemThumb.getPrice();
        }

        public Long getSellerId() {
            return sellerId;
        }

        public void setSellerId(Long sellerId) {
            this.sellerId = sellerId;
        }

        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

        public Long getDay() {
            return day;
        }

        public void setDay(Long day) {
            this.day = day;
        }

        int sale;

        int price;

        public void setShare(int share) {
            this.share = share;
        }

        public void setPv(int pv) {
            this.pv = pv;
        }

        @Override
        public String toString() {
            return "ItemPageBean [numIid=" + numIid + ", day=" + day + ", sellerId=" + sellerId + ", share=" + share
                    + ", pv=" + pv + ", isBShop=" + isBShop + ", sale=" + sale + ", price=" + price + "]";
        }

    }

    @Override
    public int compareTo(ItemPlay o) {
        return (int) (o.deListTime - this.deListTime);
    }

    public String getDelistWeekDay() {

        if (deListTime < 0) {
            return "-";
        }
        return IndustryDelistGetAction.getDelistWeekDay(deListTime);

    }

    public String getDelistHHmmss() {
        if (deListTime < 0) {
            return "-";
        }
        return IndustryDelistGetAction.getDelistHHmmss(deListTime);

    }

    public boolean isFenxiao() {
        return (this.type & Type.IS_FENXIAO) > 0;
    }

    public void setFenxiaoProductBean(FenxiaoProductBean bean) {
        this.bean = bean;
    }

    @Transient
    @JsonProperty
    private FenxiaoProductBean bean;

    public FenxiaoProductBean getFenxiaoProductBean() {
        return bean;
    }

}
