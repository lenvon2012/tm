
package models.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.itemCopy.AlibabaCat;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JPATransactionManager;
import utils.ApiUtilFor1688;
import bustbapi.ItemCatApi;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.taobao.api.domain.ItemCat;

/**
 * 商品类目
 * 
 * @author STORM
 * 
 */
@Entity(name = ItemCatPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn",
})
public class ItemCatPlay extends GenericModel implements PolicySQLGenerator {

    @Transient
    public static final String TABLE_NAME = "item_cat";

    @Transient
    public static final Logger log = LoggerFactory.getLogger(ItemCatPlay.class);

    @Transient
    public static ItemCatPlay EMPTY = new ItemCatPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    // 商品所属类目ID
    @PolicySQLGenerator.CodeNoUpdate
    @Id
    public Long cid;

    public static String findNameByCid(Long cid) {
        return JDBCBuilder.singleStringQuery(DataSrc.BASIC, "select name from " + TABLE_NAME + " where cid = ?", cid);
    }

    // 父类目ID=0时，代表的是一级的类目
    public Long parentCid;

    // 类目名称
    public String name;

    // 该类目是否为父类目(即：该类目是否还有子类目)
    public boolean isParent;

    // 状态。可选值:normal(正常,0),deleted(删除,1)
    public int status;

    // 排列序号，表示同级类目的展现次序，如数值相等则按名称次序排列。取值范围:大于零的整数
    public Long sortOrder;

    public int level;

    public long topCate_1 = 0L;

    public long topCate_2 = 0L;

    public long topCate_3 = 0L;

    public long topCate_4 = 0L;

    public ItemCatPlay() {

    }

    public ItemCatPlay(ItemCat itemCat, ItemCatPlay parentItemCat, int level) {
        this.cid = itemCat.getCid();
        this.parentCid = itemCat.getParentCid();
        this.name = itemCat.getName();
        this.isParent = itemCat.getIsParent();
        if ("normal".equalsIgnoreCase(itemCat.getStatus())) {
            this.status = 0;
        } else {
            this.status = 1;
        }
        this.sortOrder = itemCat.getSortOrder();

        this.level = level;

        switch (level) {
            case 1:
                this.topCate_1 = this.cid;
                break;
            case 2:
                this.topCate_1 = parentItemCat.getTopCate_1();
                this.topCate_2 = this.cid;
                break;
            case 3:
                this.topCate_1 = parentItemCat.getTopCate_1();
                this.topCate_2 = parentItemCat.getTopCate_2();
                this.topCate_3 = this.cid;
                break;
            case 4:
                this.topCate_1 = parentItemCat.getTopCate_1();
                this.topCate_2 = parentItemCat.getTopCate_2();
                this.topCate_3 = parentItemCat.getTopCate_3();
                this.topCate_4 = this.cid;
                break;
        }

    }

    public ItemCatPlay(ItemCat itemCat) {
        this.cid = itemCat.getCid();
        this.parentCid = itemCat.getParentCid();
        this.name = itemCat.getName();
        this.isParent = itemCat.getIsParent();
        if ("normal".equalsIgnoreCase(itemCat.getStatus())) {
            this.status = 0;
        } else {
            this.status = 1;
        }
        this.sortOrder = itemCat.getSortOrder();

    }

    /*public static ItemCatPlay findByCid(Long cid) {
        return ItemCatPlay.find("cid = ? ", cid).first();

    }*/

    private static final String SelectAllProperties = " cid,parentCid,name,isParent,status,sortOrder,level,topCate_1,topCate_2,topCate_3,topCate_4 ";

    public static ItemCatPlay findByCid(Long cid) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where cid = ? ";

        return new JDBCBuilder.JDBCExecutor<ItemCatPlay>(dp, query, cid) {

            @Override
            public ItemCatPlay doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseItemCatPlay(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static String getWholeCatName(long catId){
		StringBuffer catStr=new StringBuffer();
		ItemCatPlay sonCat=findByCid(catId);
		//如果数据库中未找到该类目信息，则调用1688接口保存
//		if (sonCat==null) {
//			AlibabaCat cat=ApiUtilFor1688.getAliCatByCid(catId);
//			cat.rawInsert();
//			sonCat=cat;
//		}
		while (sonCat!=null) {
			catStr.append(sonCat.getName()+"--");
			sonCat=sonCat.getParentCat();
		}
		
		return catStr.toString();
	}

    public static ItemCatPlay findByNameLike(String name) {
        name = CommonUtils.escapeSQL(name);

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where name like  ?";

        return new JDBCBuilder.JDBCExecutor<ItemCatPlay>(dp, query, "%" + name + "%") {

            @Override
            public ItemCatPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {

                    return parseItemCatPlay(rs);

                } else {
                    return null;
                }
            }

        }.call();
    }
    
    public static ItemCatPlay findAccordingName(String name) {
        name = CommonUtils.escapeSQL(name);

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where name = ?";

        return new JDBCBuilder.JDBCExecutor<ItemCatPlay>(dp, query, name) {

            @Override
            public ItemCatPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {

                    return parseItemCatPlay(rs);

                } else {
                    return null;
                }
            }

        }.call();
    }

    public static List<ItemCatPlay> findByName(String name) {
        name = CommonUtils.escapeSQL(name);

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where name =  ?";

        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query, "%" + name + "%") {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCatPlay> cats = new ArrayList<ItemCatPlay>();
                while (rs.next()) {

                    cats.add(parseItemCatPlay(rs));

                }
                return cats;
            }

        }.call();
    }

    public static List<ItemCatPlay> findAllByNameLike(String name) {
        name = CommonUtils.escapeSQL(name);

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where  name like  '%" + name + "%'";

        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCatPlay> cats = new ArrayList<ItemCatPlay>();
                while (rs.next()) {

                    cats.add(parseItemCatPlay(rs));

                }
                return cats;
            }

        }.call();
    }

    public static List<ItemCatPlay> findCats(Collection<Long> ids) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where cid in (" + StringUtils.join(ids, ',') + ")";
        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCatPlay> list = new ArrayList<ItemCatPlay>();
                while (rs.next()) {

                    list.add(parseItemCatPlay(rs));

                }
                return list;
            }

        }.call();
    }

    public static List<ItemCatPlay> findAllFirstLevelCats() {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where level = 1 ";
        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCatPlay> catList = new ArrayList<ItemCatPlay>();
                while (rs.next()) {
                    ItemCatPlay catPlay = parseItemCatPlay(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }

                }
                return catList;
            }

        }.call();
    }

    public static List<ItemCatPlay> findByParentCid(Long parentCid) {

        if (parentCid == null || parentCid < 0L) {
            return new ArrayList<ItemCatPlay>();
        }

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where parentCid = ? ";
        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query, parentCid) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCatPlay> catList = new ArrayList<ItemCatPlay>();
                while (rs.next()) {
                    ItemCatPlay catPlay = parseItemCatPlay(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }

                }
                return catList;
            }

        }.call();
    }

    public static List<ItemCatPlay> findByLevel(int level) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " where level = ? ";

        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query, level) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCatPlay> catList = new ArrayList<ItemCatPlay>();
                while (rs.next()) {
                    ItemCatPlay catPlay = parseItemCatPlay(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }

                return catList;
            }

        }.call();
    }

    public static List<ItemCatPlay> findEachLevel2Cat(PageOffset po) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where level = 2 order by sale desc limit ? offset ?";

        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemCatPlay> catList = new ArrayList<ItemCatPlay>();
                while (rs.next()) {
                    ItemCatPlay catPlay = parseItemCatPlay(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }

                return catList;
            }

        }.call();
    }

    public static long countCatAll() {
        String query = "select count(*) from " + TABLE_NAME + "  where level = 2";
        return dp.singleLongQuery(query);
    }

    private static ItemCatPlay parseItemCatPlay(ResultSet rs) {
        try {
            ItemCatPlay listCfg = new ItemCatPlay();
            listCfg.setCid(rs.getLong(1));
            listCfg.setParentCid(rs.getLong(2));
            listCfg.setName(rs.getString(3));
            listCfg.setParent(rs.getBoolean(4));
            listCfg.setStatus(rs.getInt(5));
            listCfg.setSortOrder(rs.getLong(6));
            listCfg.setLevel(rs.getInt(7));
            listCfg.setTopCate_1(rs.getLong(8));
            listCfg.setTopCate_2(rs.getLong(9));
            listCfg.setTopCate_3(rs.getLong(10));
            listCfg.setTopCate_4(rs.getLong(11));
            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    @JsonIgnore
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    @JsonIgnore
    public String getIdColumn() {
        return "cid";
    }

    @Override
    public void setId(Long id) {
        this.cid = id;
    }

    @Override
    @JsonIgnore
    public String getIdName() {
        return "cid";
    }

    @Override
    public Long getId() {
        return cid;
    }

    @Transient
    static String EXIST_ID_QUERY = "select cid from " + ItemCatPlay.TABLE_NAME + " where cid = ? ";

    public static long findExistId(Long cid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, cid);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    @Transient
    static String insertSQL = "insert into `item_cat`(`cid`,`parentCid`,`name`,`isParent`,`status`,`sortOrder`,`level`,`topCate_1`,`topCate_2`,`topCate_3`,`topCate_4`) values(?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(false, insertSQL, this.cid, this.parentCid, this.name, this.isParent, this.status,
                this.sortOrder, this.level, this.topCate_1, this.topCate_2, this.topCate_3, this.topCate_4);

//        log.info("[Insert Item Id:]" + id);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }

    @Transient
    static String updateSQL = "update `item_cat` set  `parentCid` = ?, `name` = ?, `isParent` = ?, `status` = ?, `sortOrder` = ?, `level` = ?, `topCate_1` = ?, `topCate_2` = ?, `topCate_3` = ?, `topCate_4` = ? where `cid` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(false, updateSQL, this.parentCid, this.name, this.isParent, this.status,
                this.sortOrder, this.level, this.topCate_1, this.topCate_2, this.topCate_3, this.topCate_4,
                this.getId());

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getParentCid() {
        return parentCid;
    }

    public void setParentCid(Long parentCid) {
        this.parentCid = parentCid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isParent() {
        return isParent;
    }

    public void setParent(boolean isParent) {
        this.isParent = isParent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @JsonIgnore
    public long getTopCate_1() {
        return topCate_1;
    }

    public void setTopCate_1(long topCate_1) {
        this.topCate_1 = topCate_1;
    }

    public long getTopCate_2() {
        return topCate_2;
    }

    public void setTopCate_2(long topCate_2) {
        this.topCate_2 = topCate_2;
    }

    @JsonIgnore
    public long getTopCate_3() {
        return topCate_3;
    }

    public void setTopCate_3(long topCate_3) {
        this.topCate_3 = topCate_3;
    }

    @JsonIgnore
    public long getTopCate_4() {
        return topCate_4;
    }

    public void setTopCate_4(long topCate_4) {
        this.topCate_4 = topCate_4;
    }

    @Override
    public String toString() {
        return "ItemCatPlay [cid=" + cid + ", parentCid=" + parentCid + ", name=" + name + ", isParent=" + isParent
                + ", status=" + status + ", sortOrder=" + sortOrder + ", level=" + level + ", topCate_1=" + topCate_1
                + ", topCate_2=" + topCate_2 + ", topCate_3=" + topCate_3 + ", topCate_4=" + topCate_4 + "]";
    }

    public static ItemCatPlay tryMatch(String name, int level) {

        if (StringUtils.isBlank(name)) {
            return null;
        }

//        log.info(format("tryMatch:name, level".replaceAll(", ", "=%s, ") + "=%s", name, level));
        //ItemCatPlay first = ItemCatPlay.find("name like ? ", "%" + name + "%").first();
        ItemCatPlay first = ItemCatPlay.findByNameLike(name);
        if (first != null) {
            return first;
        }

        Long cid = busCatToCid.get(name);
        if (cid != null) {
            first = ItemCatPlay.findByCid(cid);
            if (first != null) {
                return first;
            }
        }

        for (String exclude : args) {
            if (name.contains(exclude)) {
                name = name.replaceAll(exclude, "");
            }
        }
        first = ItemCatPlay.findByNameLike(name);

        return first;
    }

    private static String[] args = new String[] {
            "（将删除）", "3C", "（待删除，勿发商品）", "（即将删除）", "（待删除）", "一级类目", "（不含相片墙）", "1月20日删除，新品请放新类目", "剃须?喱/", "（预定及配送）",
            "T-", "2点卡", "市场"
    };

    static Map<String, Long> busCatToCid = new HashMap<String, Long>();

    static {
        busCatToCid.put("床上用品/靠垫/毛巾/布艺", 50008163L);
        busCatToCid.put("靠垫/抱枕/坐垫", 213002L);
        busCatToCid.put("儿童玩具/早教/运动/学习", 25L);
        busCatToCid.put("玩具/娃娃/模型/动漫/桌游", 25L);
        busCatToCid.put("酒店客栈/景点门票/度假旅游", 50011949L);
        busCatToCid.put("居家日用/收纳/礼品", 21L);
        busCatToCid.put("粮油/蔬果/干货/速食/水产", 50016422L);
        busCatToCid.put("零食/坚果/茶叶/特产", 50002766L);
        busCatToCid.put("日化/清洁/护理", 50016348L);
        busCatToCid.put("网店/网络服务/个性定制/软件", 50014811L);
        busCatToCid.put("传统滋补品/其他保健营养品", 50020275L);
        busCatToCid.put("单反/单电专业相机配件", 50024097L);
        busCatToCid.put("苹果内置配件", 50018326L);
        busCatToCid.put("数码相机配件（将删除）", 140908L);
        busCatToCid.put("数码相机配件（将删除）", 140908L);
        busCatToCid.put("教育培训服务", 50014927L);
        busCatToCid.put("休闲裤", 3035L);
        busCatToCid.put("中老年服装", 50000852L);
    }

    @Override
    @JsonIgnore
    public String getTableHashKey() {
        return null;
    }

    @JsonIgnore
    public static boolean isBaoyou(Long cid) {
        return (cid == 50023725L || cid == 50023728L);
    }

    public static List<ItemCatPlay> findCidPath(long cid2) {

        List<ItemCatPlay> path = new ArrayList<ItemCatPlay>();

        ItemCatPlay cat = ItemCatPlay.findByCid(cid2);
        while (cat != null) {
            path.add(cat);
            if (cat.parentCid == null || cat.parentCid < 1L) {
                break;
            }
            cat = ItemCatPlay.findByCid(cat.parentCid);
        }

        Collections.reverse(path);
        return path;
    }

    public static Long findTopCid(Long cid) {
        if (cid == null) {
            return null;
        }

        ItemCatPlay cat = ItemCatPlay.findByCid(cid);
        if (cat == null) {
            return null;
        }
        while (cat != null) {
            if (cat.parentCid == null || cat.parentCid < 1L || cat.parentCid.longValue() == cat.cid.longValue()) {
                break;
            }

            cat = ItemCatPlay.findByCid(cat.parentCid);
        }
        return cat.getCid();
    }

    public static ItemCatPlay findParent(Long cid) {
        if (cid == null) {
            return null;
        }

        ItemCatPlay cat = ItemCatPlay.findByCid(cid);
        if (cat == null) {
            return null;
        }
        if (cat.parentCid == null || cat.parentCid < 1L || cat.parentCid.longValue() == cat.cid.longValue()) {
            return null;
        }

        return ItemCatPlay.findByCid(cat.parentCid);
    }

    /*public static List<ItemCatPlay> findCats(Collection<Long> ids) {
        if (CommonUtils.isEmpty(ids)) {
            return ListUtils.EMPTY_LIST;
        }
        StringBuilder sb = new StringBuilder(" cid in (");
        sb.append(StringUtils.join(ids, ','));
        sb.append(")");
        return ItemCatPlay.find(sb.toString()).fetch();
    }*/

    public static abstract class ItemCatPlayBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 500L;

        public ItemCatPlayBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public ItemCatPlayBatchOper(int limit) {
            this.limit = limit;
        }

        public List<ItemCatPlay> findNext() {
            return findByOffset(offset, limit);
        }

        public abstract void doForEachItemCat(ItemCatPlay itemCatPlay);

        @Override
        public Boolean call() {

            while (true) {

                List<ItemCatPlay> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (ItemCatPlay itemCatPlay : findList) {
                    offset++;
                    doForEachItemCat(itemCatPlay);
                }

                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);
            }

        }
    }

    public static List<ItemCatPlay> findByOffset(int offset, int limit) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " limit ?,?";

        return new JDBCBuilder.JDBCExecutor<List<ItemCatPlay>>(dp, query, offset, limit) {

            @Override
            public List<ItemCatPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemCatPlay> catList = new ArrayList<ItemCatPlay>();
                while (rs.next()) {
                    ItemCatPlay catPlay = parseItemCatPlay(rs);
                    if (catPlay != null) {
                        catList.add(catPlay);
                    }
                }

                return catList;
            }

        }.call();
    }

    @Transient
    ItemCatPlay parentCat = null;

    public ItemCatPlay getParentCat() {
        return findByCid(this.getParentCid());
    }

    public void setParentCat(ItemCatPlay parentCat) {
        this.parentCat = parentCat;
    }

    public static void ensureParentPath(List<ItemCatPlay> results) {
        for (ItemCatPlay itemCatPlay : results) {
            while (itemCatPlay.getLevel() > 1) {
                Long parentCid = itemCatPlay.getParentCid();
                ItemCatPlay tempParent = ItemCatPlay.findByCid(parentCid);
                itemCatPlay.setParentCat(tempParent);
                itemCatPlay = tempParent;
            }
        }

    }

    static Set<Long> fuShiXiangBaoCids = new HashSet<Long>();
    static {
        Long[] args = new Long[] {
                30L,// 男装
                50008165L, // 童装/童鞋/亲子装
                50011699L, // 运动服/运动包/颈环配件
                16L, // 女装/女士精品
                50006843L, // 女鞋
                50006842L, // 箱包皮具/热销女包/男包
                1625L, // 女士内衣/男士内衣/家居服
                50010404L, // 服饰配件/皮带/帽子/围巾
                50012029L, // 运动鞋new
                50011740L, // 流行男鞋
        };

        for (Long long1 : args) {
            fuShiXiangBaoCids.add(long1);
        }
    }

    public static boolean isFushixiangbaoLvel1(Long cid) {
        if (cid == null) {
            return false;
        }
        return fuShiXiangBaoCids.contains(cid);
    }

    public static boolean isNvZhuang(Long cid) {
        if (cid == null) {
            return false;
        }

        return cid.longValue() == 16L;
    }

    public static boolean IsYifu(Long cid) {
        if (cid == null) {
            return false;
        }

        return cid.longValue() == 16L || cid.longValue() == 30L;
    }
    
    public static ItemCatPlay updateNewCatByTaobaoCat(Long cid) {
    	if(cid == null || cid <= 0) {
    		return null;
    	}
    	ItemCatPlay existedCatPlay = ItemCatPlay.findByCid(cid);
    	if(existedCatPlay != null) {
    		return existedCatPlay;
    	}
    	// 如果不存在
    	List<ItemCat> cats = new ItemCatApi.ItemcatsGet(0L, cid).call();
		if(CommonUtils.isEmpty(cats)) {
			return null;
		}
		ItemCat itemCat = cats.get(0);
		ItemCatPlay play = null;
		if(itemCat.getParentCid() > 0) {
			ItemCatPlay parentCatPlay = updateNewCatByTaobaoCat(itemCat.getParentCid());
			play =  new ItemCatPlay(itemCat, parentCatPlay, parentCatPlay.getLevel() + 1);
			play.jdbcSave();
			return play;
		}
		play = new ItemCatPlay(itemCat, null, 1);
		play.jdbcSave();
		return play;
    }
    
    public static void delByCid(Long cid) {
    	ItemCatPlay icp = ItemCatPlay.findByCid(cid);
    	if (icp!=null) {
			icp.rawDelete(cid);
		}
		
	}
    
    public boolean rawDelete(Long cid) {
    	
		String sql = " delete from " + TABLE_NAME + " where cid = ? ";
		
		dp.update(sql, this.cid);
		
		return true;
    }
}
