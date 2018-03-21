
package models.shop;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.user.UserIdNick;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import transaction.JPATransactionManager;
import utils.PlayUtil;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.SimpleHttpApi;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.Paginger;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Shop;

@Entity(name = TBShopPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "persistent", "entityId"
})
public class TBShopPlay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TBShopPlay.class);

    public static final String TAG = "TBShopPlay_";

    public static final String TABLE_NAME = "tb_shop_";

    public static TBShopPlay _instance = new TBShopPlay();

    @Transient
    static DataSrc dataSrc = DataSrc.BASIC;

    public TBShopPlay() {
    }

    public Long getId() {
        return sellerId;
    }

    @Override
    public Object _key() {
        return getId();
    }

    @Id
    public Long sellerId;

    public String picPath;

    @Index(name = "title")
    @Column(columnDefinition = " varchar(255) default null")
    public String title;

    @Column(columnDefinition = " varchar(16382) default null")
    @JsonIgnore
    public String description;

    @Index(name = "wangwang")
    public String wangwang;

    public Long cid;

    int perCustAmount;

    @JsonProperty
    int itemNum = 0;

    @JsonProperty
    int tradeNum = 0;

    @JsonProperty
    long tradeAmount = 0;

    long updated = 0L;

    int type;

    int level;

    public TBShopPlay(String wangwang, long sellerId) {
        this.wangwang = wangwang;
        this.sellerId = sellerId;
//        this.updated = System.currentTimeMillis();
    }

    public TBShopPlay(String wangwang, Shop shop, long sellerId) {
        this.picPath = shop.getPicPath();
        this.wangwang = wangwang;
        this.title = shop.getTitle();
        this.description = shop.getBulletin();
        this.sellerId = sellerId;
        this.cid = shop.getCid();
        this.updated = System.currentTimeMillis();
    }

    public TBShopPlay(ResultSet rs) throws SQLException {
        this.sellerId = rs.getLong(1);
        this.picPath = rs.getString(2);
        this.title = rs.getString(3);
        this.description = rs.getString(4);
        this.wangwang = rs.getString(5);
        this.cid = rs.getLong(6);
        this.perCustAmount = rs.getInt(7);
        this.itemNum = rs.getInt(8);
        this.tradeNum = rs.getInt(9);
        this.tradeAmount = rs.getLong(10);
        this.updated = rs.getLong(11);
        this.status = rs.getInt(12);
        this.type = rs.getInt(13);
        this.level = rs.getInt(14);
    }

    public static class Type {
        public static final int IS_TMALL = 4;
    }

    public void updateShopInfoByWwangwang(String wangwang2) {
        try {
            ShopInfo shopInfo = new SimpleHttpApi.ShopInfoApi(wangwang2).execute();
            if (shopInfo == null) {
                log.error("No wangwang info :" + wangwang2);
                return;
            }

            this.setLevel(shopInfo.getLevel());
            this.setBShop(shopInfo.isBShop());
            log.info("After set :" + this);
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void setBShop(boolean bShop) {
        this.setTypeTmall();
    }

    @JsonProperty
    public boolean isTmall() {
        return (this.type & Type.IS_TMALL) > 0;
    }

    public void setTypeTmall() {
        this.type |= Type.IS_TMALL;
    }

    public static TBShopPlay findbySellerId(Long sellerId) {
        // return TBShopPlay.find("sellerId = ?", sellerId).first();
        return NumberUtil.first(new ListFetcher(null, "  sellerId = ?", sellerId).call());
    }

    public int status;

    public long getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(long tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public static Paginger searchWangwangPager(String wangwang, int pn, int ps) {
        String query = "%" + wangwang + "%";

        PageOffset pageOffset = new PageOffset(pn, ps);
        List<TBShopPlay> shops = PageOffset.appendQueryByPage(TBShopPlay.find("wangwang like ?", query),
                pageOffset.getOffset(), pageOffset.getPs());
        int count = (int) TBShopPlay.count("wangwang like ?", query);
        return new Paginger(pageOffset.getPn(), pageOffset.getPs(), count, shops);
    }

    /**
     * 
     * @return get some default page????
     */
    public String getPicPath() {
        if (StringUtils.isBlank(this.picPath)) {
            return "http://img01.taobaocdn.com/imgextra/i2/T1jGS6Xl8aXXXXXXXX-70-70.gif";
        } else {
            return picPath;
        }
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWangwang() {
        return wangwang;
    }

    public void setWangwang(String wangwang) {
        this.wangwang = wangwang;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static TBShopPlay findByWangwang(String wangwang) {
        if (wangwang == null) {
            return null;
        }
        // return TBShopPlay.find("wangwang = ?", wangwang).first();
        return NumberUtil.first(new ListFetcher(null, "wangwang = ?", wangwang).call());
    }

    static String insertSQL = "insert into `tb_shop_`(`sellerId`,`picPath`,`title`,`description`,`wangwang`,`cid`,`perCustAmount`,`itemNum`,`tradeNum`,`tradeAmount`,`updated`,`status`,`type`,`level`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        resetCaches(this.sellerId);
        long id = JDBCBuilder.insert(false, false, transaction.DBBuilder.DataSrc.BASIC, insertSQL, this.sellerId,
                this.picPath, this.title,
                this.description, this.wangwang, this.cid, this.perCustAmount, this.itemNum, this.tradeNum,
                this.tradeAmount, this.updated, this.status, this.type, this.level);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.sellerId);
            return false;
        }
    }

    static String updateSQL = "update `tb_shop_` set `sellerId` = ?, `picPath` = ?, `title` = ?, `description` = ?, `wangwang` = ?, `cid` = ?, `perCustAmount` = ?, `itemNum` = ?, `tradeNum` = ?, `tradeAmount` = ?, `updated` = ?, `status` = ?, `type` = ?, `level` = ? where `id` = ? ";

    public boolean rawUpdate() {
        resetCaches(this.sellerId);
        long updateNum = JDBCBuilder.update(false, DataSrc.BASIC, updateSQL, this.sellerId, this.picPath, this.title,
                this.description, this.wangwang, this.cid, this.perCustAmount, this.itemNum, this.tradeNum,
                this.tradeAmount, this.updated, this.status, this.type, this.level, this.getId());
        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    static String insertOnDupKeyUpdateSQL = "insert into `tb_shop_`(`sellerId`,`picPath`,`title`,`description`,`wangwang`,`cid`,`perCustAmount`,`itemNum`,`tradeNum`,`tradeAmount`,`updated`,`status`,`type`,`level`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?) on duplicate key update `sellerId` = ?, `picPath` = ?, `title` = ?, `description` = ?, `wangwang` = ?, `cid` = ?, `perCustAmount` = ?, `itemNum` = ?, `tradeNum` = ?, `tradeAmount` = ?, `updated` = ?, `status` = ?, `type` = ?, `level` = ? ";

    public boolean rawInsertOnDupKeyUpdate() {
        resetCaches(this.sellerId);
        long id = JDBCBuilder.insert(false, false, DataSrc.BASIC, insertOnDupKeyUpdateSQL, this.sellerId, this.picPath,
                this.title,
                this.description, this.wangwang, this.cid, this.perCustAmount, this.itemNum, this.tradeNum,
                this.tradeAmount, this.updated, this.status, this.type, this.level, this.sellerId, this.picPath,
                this.title, this.description, this.wangwang, this.cid, this.perCustAmount, this.itemNum, this.tradeNum,
                this.tradeAmount, this.updated, this.status, this.type, this.level);

        // log.info("[Insert Item Id:]" + id + "[userId : ]" + this.userId);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.sellerId);
            return false;
        }
    }

    static String EXIST_SELLERID_QUERY = "select sellerId from " + TABLE_NAME + " where sellerId = ? ";

    public static long findExistId(Long sellerId) {
        return JDBCBuilder.singleLongQuery(EXIST_SELLERID_QUERY, sellerId);
    }

    public boolean jdbcSave() {
//        long exist = findExistId(this.sellerId);
//        if (exist > 0L) {
//            this.rawUpdate();
//        } else {
//            this.rawInsert();
//        }
        return rawInsertOnDupKeyUpdate();
    }

    public static abstract class ShopBatchOper implements Callable<Boolean> {
        protected int offset = 0;

        protected int limit;

        protected int max = Integer.MAX_VALUE;

        protected long intervalTime = 50L;

        public ShopBatchOper(int limit) {
            this.limit = limit;
        }

        public ShopBatchOper(int limit, int max) {
            this.limit = limit;
            this.max = max;
        }

        public ShopBatchOper(int offset, int limit, int max) {
            super();
            this.offset = offset;
            this.limit = limit;
            this.max = max;
        }

        public List<TBShopPlay> findNext() {
            // return TBShopPlay.find(" 1 = 1").from(offset).fetch(limit);
            return new ListFetcher(null, "  1 =1 limit ? offset ? ", limit, offset).call();
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public abstract boolean doForEachShop(TBShopPlay shop);

        @Override
        public Boolean call() {

            while (true) {
                // if (offset > max) {
                // return Boolean.TRUE;
                // }

                List<TBShopPlay> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (TBShopPlay user : findList) {
                    offset++;
                    doForEachShop(user);
                }

                JPATransactionManager.clearEntities();
                PlayUtil.sleepQuietly(intervalTime);
            }

        }
    }

    public static class ShopBaseTradeBean {
        @Index(name = "sellerId")
        Long sellerId;

        int tradeNum;

        int itemNum;

        long amount = 0L;

        public Long getSellerId() {
            return sellerId;
        }

        public void setSellerId(Long sellerId) {
            this.sellerId = sellerId;
        }

        public int getTradeNum() {
            return tradeNum;
        }

        public void setTradeNum(int tradeNum) {
            this.tradeNum = tradeNum;
        }

        public int getItemNum() {
            return itemNum;
        }

        public void setItemNum(int itemNum) {
            this.itemNum = itemNum;
        }

        public ShopBaseTradeBean(Long sellerId, int tradeNum, int itemNum) {
            super();
            this.sellerId = sellerId;
            this.tradeNum = tradeNum;
            this.itemNum = itemNum;
        }

        public ShopBaseTradeBean(Long sellerId, int tradeNum, int itemNum, long amount) {
            super();
            this.sellerId = sellerId;
            this.tradeNum = tradeNum;
            this.itemNum = itemNum;
            this.amount = amount;
        }

        boolean isBShop;

        int level = 0;

        boolean updatePartial = true;

        public ShopBaseTradeBean(Long sellerId, int tradeNum, int itemNum, long amount, boolean isBShop, int level) {
            super();
            this.sellerId = sellerId;
            this.tradeNum = tradeNum;
            this.itemNum = itemNum;
            this.amount = amount;
            this.isBShop = isBShop;
            this.level = level;
            this.updatePartial = false;
        }

        public void rawUpdate() {
            if (updatePartial) {
                updatePartial();
            } else {
                updateAll();
            }
        }

        private boolean updateAll() {
            resetCaches(this.sellerId);
            long updateNum = JDBCBuilder.update(true, DataSrc.BASIC, "update `" + TABLE_NAME
                    + "` set  `itemNum` = ?, `tradeAmount` = ?, `tradeNum` = ? ,  `level` = ?, "
                    + (isBShop ? " `type` = `type` | " + Type.IS_TMALL : " `type` = `type` & " + (~Type.IS_TMALL))
                    + " where `sellerId` = ? ", this.itemNum, this.amount, this.tradeNum, this.level, this.sellerId);
            if (updateNum == 1) {
                log.info("update ok for :" + this.getSellerId());
                return true;
            } else {
                log.error("update failed...for :" + this.getSellerId());
                return false;
            }
        }

        private boolean updatePartial() {
            resetCaches(this.sellerId);
            long updateNum = JDBCBuilder.update(true, DataSrc.BASIC, "update `" + TABLE_NAME
                    + "` set  `itemNum` = ?, `tradeAmount` = ?, `tradeNum` = ? " + " where `sellerId` = ? ",
                    this.itemNum, this.amount, this.tradeNum, this.sellerId);
            if (updateNum == 1) {
                log.info("update ok for :" + this.getSellerId());
                return true;
            } else {
                log.error("update failed...for :" + this.getSellerId());
                return false;
            }
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

    }

    public static TBShopPlay findSellerIdCache(Long sellerId) {
        if (NumberUtil.isNullOrZero(sellerId)) {
            return null;
        }
        TBShopPlay shop = (TBShopPlay) Cache.get(TAG + sellerId);
        if (shop != null) {
            return shop;
        }

        shop = TBShopPlay.findbySellerId(sellerId);
        if (shop != null) {
            Cache.set(TAG + sellerId, shop);
        }
        return shop;
    }

    public static void resetCaches(Long sellerId) {
        Cache.safeDelete(TAG + sellerId);
    }

    @Override
    @JsonIgnore
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    @JsonIgnore
    public String getIdColumn() {
        return "id";
    }

    @Override
    @JsonIgnore
    public String getIdName() {
        return "id";
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public int getTradeNum() {
        return tradeNum;
    }

    public void setTradeNum(int tradeNum) {
        this.tradeNum = tradeNum;
    }

    public static String getUpdateSQL() {
        return updateSQL;
    }

    public static void setUpdateSQL(String updateSQL) {
        TBShopPlay.updateSQL = updateSQL;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public static class ListFetcher extends JDBCExecutor<List<TBShopPlay>> {
        public ListFetcher(Long hashKeyId, String whereQuery, Object... params) {
            super(false, whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select sellerId,picPath,title,description,wangwang,cid,perCustAmount,itemNum,tradeNum,tradeAmount,updated,status,type,level from tb_shop_");
            sb.append(" where  1 = 1 ");
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }
            this.src = _instance.getDataSrc();
            this.query = sb.toString();
            // log.info("[query :]" + query);
            // this.debug = true;
        }

        @Override
        public List<TBShopPlay> doWithResultSet(ResultSet rs) throws SQLException {
            List<TBShopPlay> list = new ArrayList<TBShopPlay>();
            while (rs.next()) {
                list.add(new TBShopPlay(rs));
            }
            return list;
        }
    }

    public static int count(Long hashKeyId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(TABLE_NAME);
        sb.append(" where  1 = 1  ");
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(_instance.getDataSrc(), sb.toString(), params);

    }

    @JsonProperty
    public int getPerCustAmount() {
        if (this.perCustAmount <= 0 && this.tradeAmount > 0 && this.tradeNum > 0) {
            this.perCustAmount = (int) (this.tradeAmount / this.tradeNum);
        }
        return perCustAmount;
    }

    public void setPerCustAmount(int perCustAmount) {
        this.perCustAmount = perCustAmount;
    }

    public static class ShopCatSaleTrade {
        long cid;

        long tradeNum;

        long tradeAmount;

        long itemNum;

        public long getCid() {
            return cid;
        }

        public void setCid(long cid) {
            this.cid = cid;
        }

        public long getTradeNum() {
            return tradeNum;
        }

        public void setTradeNum(long tradeNum) {
            this.tradeNum = tradeNum;
        }

        public long getTradeAmount() {
            return tradeAmount;
        }

        public void setTradeAmount(long tradeAmount) {
            this.tradeAmount = tradeAmount;
        }

        public long getItemNum() {
            return itemNum;
        }

        public void setItemNum(long itemNum) {
            this.itemNum = itemNum;
        }

        public ShopCatSaleTrade(long cid, long tradeNum, long tradeAmount, long itemNum) {
            super();
            this.cid = cid;
            this.tradeNum = tradeNum;
            this.tradeAmount = tradeAmount;
            this.itemNum = itemNum;
        }

        @Override
        public String toString() {
            return "ShopCatSaleTrade [cid=" + cid + ", tradeNum=" + tradeNum + ", tradeAmount=" + tradeAmount
                    + ", itemNum=" + itemNum + "]";
        }

    }

    public static List<ShopCatSaleTrade> querySum() {
        final List<ShopCatSaleTrade> list = new ArrayList<ShopCatSaleTrade>();
        new JDBCExecutor<ShopCatSaleTrade>("select cid, sum(tradeNum), sum(tradeAmount), sum(itemNum) from "
                + TABLE_NAME + " where cid > 0 group by cid ") {
            @Override
            public ShopCatSaleTrade doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    list.add(new ShopCatSaleTrade(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)));
                }
                return null;
            }
        }.call();
        log.info("[return list :]" + list);
        return list;
    }

    public static List<TBShopPlay> fetchCidTop(Long cid, PageOffset po) {
        if (NumberUtil.isNullOrZero(cid)) {
            return ListUtils.EMPTY_LIST;
        }
        List<TBShopPlay> call = new ListFetcher(null, " cid = ? order by tradeNum desc limit ? offset ?", cid,
                po.getPs(), po.getOffset()).call();
        return call;
    }

    public static int countCid(Long cid) {
        if (NumberUtil.isNullOrZero(cid)) {
            return 0;
        }
        long count = JDBCBuilder.singleLongQuery("select count(sellerId) from " + TABLE_NAME + " where cid = " + cid);
        return (int) count;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((sellerId == null) ? 0 : sellerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TBShopPlay other = (TBShopPlay) obj;
        if (sellerId == null) {
            if (other.sellerId != null)
                return false;
        } else if (!sellerId.equals(other.sellerId))
            return false;
        return true;
    }

    public ShopInfo toInfo() {
        ShopInfo info = new ShopInfo(this.getWangwang(), this.level, this.itemNum, this.tradeNum, this.isTmall());
        if (this.picPath != null && this.picPath.length() > 4) {
//            info.setPicPath("http://img01.taobaocdn.com/imgextra" + this.getPicPath());
            info.setPicPath(this.getPicPath());
        }

        return info;
    }

    @Transient
    boolean isAdded = false;

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public boolean isRecentUpdated() {
        return System.currentTimeMillis() - this.updated < DateUtil.TRIPPLE_DAY_MILLIS_SPAN;
    }

    public DataSrc getDataSrc() {
        return dataSrc;
    }

    public void setDataSrc(DataSrc dataSrc) {
        this.dataSrc = dataSrc;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public void setId(Long id) {
        this.sellerId = id;
    }

}
