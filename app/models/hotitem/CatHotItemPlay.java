
package models.hotitem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;

@Entity(name = CatHotItemPlay.TABLE_NAME)
public class CatHotItemPlay extends GenericModel implements PolicySQLGenerator {

    //手机等数码类关键词，宝贝排列方式不一样的
    //酒店等关键词，宝贝页面不一样的，甚至itemCount都取不到，所以要加上itemCount > 0的条件

    private static final Logger log = LoggerFactory.getLogger(CatHotItemPlay.class);

    @Transient
    public static final String TABLE_NAME = "cat_hot_item_play";

    @Transient
    public static CatHotItemPlay EMPTY = new CatHotItemPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @PolicySQLGenerator.CodeNoUpdate
    @Id
    private Long numIid;            //通过item得到id

    @Index(name = "cid")
    private Long cid;           //得到后台cid

    private Long userId;        //卖家id

    private String userNick;    //旺旺名

    private String title;           //得到title;

    private String picURL;          //得到图片url

    private double price;       //得到价格

    private int salesCount; //得到销量

    @Column(columnDefinition = "text")
    private String props;                               //通过item得到一些属性

    private long delistTime;                    //得到下架时间

    //通过关键词搜索到的宝贝，如果不是属于指定类目的，那么就把这个宝贝缓存下来，但要标记一下
    @Column(columnDefinition = "int default 0 ")
    private int status;

    public static class CatHotItemStatus {
        public static final int CatItem = 1;//

        public static final int ForCache = 2;
    }

    private long updateTs;

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }

    public long getDelistTime() {
        return delistTime;
    }

    public void setDelistTime(long delistTime) {
        this.delistTime = delistTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isHitCatItem() {
        return (status & CatHotItemStatus.CatItem) > 0;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public CatHotItemPlay() {
        super();
    }

    public CatHotItemPlay(Item tbItem, MainSearchItemRank itemRank) {
        super();
        updateByItem(tbItem, itemRank);
    }

    private void updateByItem(Item tbItem, MainSearchItemRank itemRank) {

        this.numIid = tbItem.getNumIid();
        this.cid = tbItem.getCid();
        this.delistTime = tbItem.getDelistTime() == null ? 0L : tbItem.getDelistTime().getTime();
        this.picURL = tbItem.getPicUrl();
        this.price = CommonUtils.String2Double(tbItem.getPrice());
        this.props = parseItemProps(tbItem);
        this.salesCount = itemRank.getSalesCount();
        //this.status = 
        this.title = tbItem.getTitle();
        this.userId = itemRank.getSellerId();
        this.userNick = itemRank.getWangwangId();

        //最近更新
        this.updateTs = System.currentTimeMillis();

    }

    private static String parseItemProps(Item tbItem) {
        String propsName = tbItem.getPropsName();
        String propAlias = tbItem.getPropertyAlias();
        List<PropUnit> splitProp = ItemPropAction.mergePropAlis(propsName, propAlias);
        String json = JsonUtil.getJson(splitProp);
        return json;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "numIid";
    }

    @Override
    public String getIdName() {
        return "numIid";
    }

    @Override
    public Long getId() {
        return numIid;
    }

    @Override
    public void setId(Long id) {
        this.numIid = id;
    }

    private static String genShardQuery(String query, Long cid) {

        String key = String.valueOf(DBBuilder.genUserIdHashKey(cid));

        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    private static String getSqlTableName() {
        return TABLE_NAME + "%s";
    }

    public static long findExistId(Long cid, Long numIid) {

        String query = "select numIid from " + getSqlTableName() + " where numIid = ? ";
        query = genShardQuery(query, cid);

        return dp.singleLongQuery(query, numIid);
    }

    @Override
    public boolean jdbcSave() {
        try {

            long existdId = findExistId(this.cid, this.numIid);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {

        String insertSQL = "insert into `" + getSqlTableName() + "`" +
                "(`numIid`,`cid`,`userId`,`userNick`,`title`," +
                "`picURL`,`price`,`salesCount`,`props`,`delistTime`," +
                "`status`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?,?)";

        insertSQL = genShardQuery(insertSQL, cid);

        //updateTs = System.currentTimeMillis();

        long id = dp.insert(true, insertSQL,
                this.numIid, this.cid, this.userId, this.userNick, this.title,
                this.picURL, this.price, this.salesCount, this.props, this.delistTime,
                this.status, this.updateTs);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + getSqlTableName() + "` set  " +
                " `cid` = ?, `userId` = ?, `userNick` = ?, `title` = ?, " +
                " `picURL` = ?, `price` = ?, `salesCount` = ?, `props` = ?, `delistTime` = ?, " +
                " `status` = ?, `updateTs` = ? " +
                " where `numIid` = ? ";

        updateSQL = genShardQuery(updateSQL, cid);

        //updateTs = System.currentTimeMillis();

        long updateNum = dp.update(false, updateSQL,
                this.cid, this.userId, this.userNick, this.title,
                this.picURL, this.price, this.salesCount, this.props, this.delistTime,
                this.status, this.updateTs,
                this.numIid);

        if (updateNum == 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    public static List<CatHotItemPlay> findByCid(Long cid) {

        String query = "select " + SelectAllProperty + " from " + getSqlTableName()
                + " where cid = ? ";

        query = genShardQuery(query, cid);

        StringBuilder sb = new StringBuilder();
        sb.append(query);

        query = sb.toString();

        return findListByJDBC(query, cid);
    }

    public static long countCatItemByCid(Long cid) {
        String query = "select count(*) from " + getSqlTableName()
                + " where cid = ? and status = ?";

        query = genShardQuery(query, cid);

        return dp.singleLongQuery(query, cid, CatHotItemStatus.CatItem);
    }

    /*
    public static List<CatHotItemPlay> findByCid(Long cid) {
        
        String query = "select " + SelectAllProperty + " from " + getSqlTableName() 
                + " where cid = ? ";
        
        query = genShardQuery(query, cid);
        
        
        return findListByJDBC(query, cid);
    }
    */

    public static List<CatHotItemPlay> findByCidAndNumIids(Long cid, Set<Long> numIidSet) {

        if (CommonUtils.isEmpty(numIidSet)) {
            return new ArrayList<CatHotItemPlay>();
        }

        String query = "select " + SelectAllProperty + " from " + getSqlTableName()
                + " where cid = ? ";

        query = genShardQuery(query, cid);

        StringBuilder sb = new StringBuilder();
        sb.append(query);
        sb.append(" and numIid in (");
        sb.append(StringUtils.join(numIidSet, ","));
        sb.append(")");

        query = sb.toString();

        return findListByJDBC(query, cid);
    }

    public static CatHotItemPlay findByNumIid(Long numIid) {
        for (int i = 0; i < 16; i++) {

            String query = "select " + SelectAllProperty + " from " + getSqlTableName()
                    + " where numIid = ? ";

            query = genShardQuery(query, (long) i);

            CatHotItemPlay catItem = findByJDBC(query, numIid);

            if (catItem != null) {
                return catItem;
            }
        }

        return null;
    }

    private static List<CatHotItemPlay> findListByJDBC(String query, Object... params) {

        return new JDBCBuilder.JDBCExecutor<List<CatHotItemPlay>>(dp, query, params) {

            @Override
            public List<CatHotItemPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<CatHotItemPlay> catItemList = new ArrayList<CatHotItemPlay>();

                while (rs.next()) {
                    CatHotItemPlay catItem = parseCatHotItemPlay(rs);
                    if (catItem != null) {
                        catItemList.add(catItem);
                    }
                }

                return catItemList;

            }

        }.call();

    }

    private static CatHotItemPlay findByJDBC(String query, Object... params) {

        return new JDBCBuilder.JDBCExecutor<CatHotItemPlay>(dp, query, params) {

            @Override
            public CatHotItemPlay doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                    return parseCatHotItemPlay(rs);
                } else {
                    return null;
                }

            }

        }.call();

    }

    private static final String SelectAllProperty = " numIid,cid,userId,userNick,title," +
            "picURL,price,salesCount,props,delistTime," +
            "status,updateTs ";

    private static CatHotItemPlay parseCatHotItemPlay(ResultSet rs) {
        try {
            CatHotItemPlay item = new CatHotItemPlay();

            item.setNumIid(rs.getLong(1));
            item.setCid(rs.getLong(2));
            item.setUserId(rs.getLong(3));
            item.setUserNick(rs.getString(4));
            item.setTitle(rs.getString(5));
            item.setPicURL(rs.getString(6));
            item.setPrice(rs.getDouble(7));
            item.setSalesCount(rs.getInt(8));
            item.setProps(rs.getString(9));
            item.setDelistTime(rs.getLong(10));
            item.setStatus(rs.getInt(11));
            item.setUpdateTs(rs.getLong(12));

            return item;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

}
