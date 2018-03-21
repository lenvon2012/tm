
package models.vgouitem;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import utils.TMCatUtil;
import utils.TaobaoUtil;
import codegen.CodeGenerator.PolicySQLGenerator;
import configs.TMConfigs;

@Entity(name = VGouItem.TABLE_NAME)
public class VGouItem extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(VGouItem.class);

    @Transient
    public static final String TABLE_NAME = "vg_items";

    @Column(columnDefinition = "smallint(4) default NULL")
    private int cid;

    @Column(columnDefinition = "varchar(50) default NULL")
    private String item_key;

    @Column(columnDefinition = "varchar(255) default NULL")
    private String title;

    @Column(columnDefinition = "varchar(255) default NULL")
    private String img;

    @Column(columnDefinition = "varchar(255) default NULL")
    private String simg;

    @Column(columnDefinition = "varchar(255) default NULL")
    private String bimg;

    @Column(columnDefinition = "decimal(10,2) DEFAULT NULL")
    private double price;

    @Column(columnDefinition = "varchar(1000) DEFAULT NULL")
    private String url;

    @Column(columnDefinition = "smallint(4) NOT NULL DEFAULT '0'")
    private int sid = 0;

    @Column(columnDefinition = "int(10) NOT NULL DEFAULT '0'")
    private int hits = 0;

    @Column(columnDefinition = "int(10) NOT NULL DEFAULT '0'")
    private int likes = 0;

    @Column(columnDefinition = "int(10) NOT NULL")
    private int browse_num = 0;

    @Column(columnDefinition = "int(10) NOT NULL DEFAULT '0'")
    private int haves = 0;

    @Column(columnDefinition = "int(10) NOT NULL DEFAULT '0'")
    private int comments = 0;

    @Column(columnDefinition = "text")
    private String comments_last;

    @Column(columnDefinition = "tinyint(1) NOT NULL DEFAULT '0'")
    private int is_index = 0;

    @Column(columnDefinition = "tinyint(1) NOT NULL DEFAULT '1'")
    private int status = 1;

    @Column(columnDefinition = "int(10) NOT NULL DEFAULT '0'")
    private int add_time = 0;

    @Column(columnDefinition = "bigint(20) DEFAULT '0'")
    private Long uid = 0l;

    @Column(columnDefinition = "varchar(255) DEFAULT NULL")
    private String seo_title;

    @Column(columnDefinition = "varchar(255) DEFAULT NULL")
    private String seo_keys;

    @Column(columnDefinition = "int(10) DEFAULT '0'")
    private int sort_order = 0;

    @Column(columnDefinition = "text")
    private String sort_desc;

    @Column(columnDefinition = "varchar(40) NOT NULL")
    private String cash_back_rate = StringUtils.EMPTY;

    @Column(columnDefinition = "varchar(100) NOT NULL")
    private String seller_name = StringUtils.EMPTY;

    @Column(columnDefinition = "varchar(50) DEFAULT NULL")
    private String remark;

    @Column(columnDefinition = "tinyint(6) DEFAULT '1'")
    private int remark_status = 1;

    @Column(columnDefinition = "int(1) DEFAULT '0'")
    private int id_collect_comments = 0;

    @Column(columnDefinition = "bigint(20) DEFAULT '0'")
    private Long numIid = 0l;

    @Column(columnDefinition = "decimal(10,2) DEFAULT 0")
    private double skuMinPrice = 0;//打折价

    public VGouItem() {
        super();
    }

    public VGouItem(Long cid, String item_key, String title, String img, double price, String url, Long uid, int sid,
            Long numIid) {
        this.cid = TMCatUtil.getCidInt(cid);
        this.item_key = item_key;
        this.title = title;
        this.img = img;
        this.price = price;
        this.url = url;
        this.uid = uid;
        this.sid = sid;
        this.numIid = numIid;
        setTBInfo();
    }

    public VGouItem(Long uid, Long numIid, String title, String img, double price, Long cid) {
        this.uid = uid;
        this.numIid = numIid;
        this.title = title;
        this.img = img;
        this.price = price;
//        this.cid = TMCatUtil.getCidInt(cid);
        setTBItemCid(cid);
        setTBInfo();
    }

    private void setTBInfo() {
        this.url = "item.taobao.com/item.htm?id=" + numIid;
        this.seller_name = "淘宝网";
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getItem_key() {
        return item_key;
    }

    public void setItem_key(String item_key) {
        this.item_key = item_key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getSimg() {
        return simg;
    }

    public void setSimg(String simg) {
        this.simg = simg;
    }

    public String getBimg() {
        return bimg;
    }

    public void setBimg(String bimg) {
        this.bimg = bimg;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getBrowse_num() {
        return browse_num;
    }

    public void setBrowse_num(int browse_num) {
        this.browse_num = browse_num;
    }

    public int getHaves() {
        return haves;
    }

    public void setHaves(int haves) {
        this.haves = haves;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public String getComments_last() {
        return comments_last;
    }

    public void setComments_last(String comments_last) {
        this.comments_last = comments_last;
    }

    public int getIs_index() {
        return is_index;
    }

    public void setIs_index(int is_index) {
        this.is_index = is_index;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAdd_time() {
        return add_time;
    }

    public void setAdd_time(int add_time) {
        this.add_time = add_time;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getSeo_title() {
        return seo_title;
    }

    public void setSeo_title(String seo_title) {
        this.seo_title = seo_title;
    }

    public String getSeo_keys() {
        return seo_keys;
    }

    public void setSeo_keys(String seo_keys) {
        this.seo_keys = seo_keys;
    }

    public int getSort_order() {
        return sort_order;
    }

    public void setSort_order(int sort_order) {
        this.sort_order = sort_order;
    }

    public String getSort_desc() {
        return sort_desc;
    }

    public void setSort_desc(String sort_desc) {
        this.sort_desc = sort_desc;
    }

    public String getCash_back_rate() {
        return cash_back_rate;
    }

    public void setCash_back_rate(String cash_back_rate) {
        this.cash_back_rate = cash_back_rate;
    }

    public String getSeller_name() {
        return seller_name;
    }

    public void setSeller_name(String seller_name) {
        this.seller_name = seller_name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getRemark_status() {
        return remark_status;
    }

    public void setRemark_status(int remark_status) {
        this.remark_status = remark_status;
    }

    public int getId_collect_cooments() {
        return id_collect_comments;
    }

    public void setId_collect_cooments(int id_collect_comments) {
        this.id_collect_comments = id_collect_comments;
    }

    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public double getSkuMinPrice() {
        return skuMinPrice;
    }

    public void setSkuMinPrice(double skuMinPrice) {
        this.skuMinPrice = skuMinPrice;
    }

    //每次更新时都重置一下skuMinPrice
    public void resetSkuMinPrice() {
        this.skuMinPrice = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableHashKey()
     */
    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableName()
     */
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Transient
    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where uid = ? and numIid = ?";

    private static long findExistId(Long userId, Long numIid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.uid, this.numIid);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `vg_items`(`cid`,`item_key`,`title`,`img`,`simg`,`bimg`,`price`,`url`,`sid`,`hits`, `likes`,`browse_num`,`haves`,`comments`,`comments_last`,`is_index`,`status`,`add_time`,`uid`,`seo_title`,`seo_keys`,`sort_order`,`sort_desc`,`cash_back_rate`,`seller_name`,`remark`,`remark_status`,`id_collect_comments`,`numIid`,`skuMinPrice`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.cid, this.item_key, this.title, this.img, this.simg, this.bimg, this.price, this.url,
                        this.sid, this.hits, this.likes, this.browse_num, this.haves, this.comments,
                        this.comments_last, this.is_index, this.status, this.add_time, this.uid, this.seo_title,
                        this.seo_keys, this.sort_order, this.sort_desc, this.cash_back_rate, this.seller_name,
                        this.remark, this.remark_status, this.id_collect_comments, this.numIid, this.skuMinPrice);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.uid + "[numIid : ]" + this.numIid);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder
                .insert("update `vg_items` set  `cid` = ?, `item_key` = ?, `title` = ?, `img` = ?, `simg` = ?, `bimg` = ? , `price` = ?, `url` = ?, `sid` = ?,`hits` = ? ,`likes` = ?,`browse_num` = ?,`haves` = ?,`comments` = ?,`comments_last` = ?,`is_index` = ?,`status` = ?,`add_time` = ?,`uid` = ?,`seo_title` = ?,`seo_keys` = ?,`sort_order` = ?,`sort_desc` = ?,`cash_back_rate` = ?,`seller_name` = ?,`remark` = ?,`remark_status` = ?,`id_collect_comments` = ?,`numIid` = ?,`skuMinPrice` = ? where `id` = ? ",
                        this.cid, this.item_key, this.title, this.img, this.simg, this.bimg, this.price, this.url,
                        this.sid, this.hits, this.likes, this.browse_num, this.haves, this.comments,
                        this.comments_last, this.is_index, this.status, this.add_time, this.uid, this.seo_title,
                        this.seo_keys, this.sort_order, this.sort_desc, this.cash_back_rate, this.seller_name,
                        this.remark, this.remark_status, this.id_collect_comments, this.numIid, this.skuMinPrice,
                        this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.uid + "[numIid : ]" + this.numIid);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    /**
     * 
     * @param cid
     */
    public void setTBItemCid(Long cid) {
        //this.cid = 2;
        Long firstLevel = TaobaoUtil.getFirstLevel(cid);
        if (firstLevel == null) {
            this.cid = (int) SuiyiTaoCatId;
            return;
        }
        String catname = TMConfigs.PopularizeConfig.bigCidNameMap.get(firstLevel);
        if (StringUtils.isEmpty(catname)) {
            this.cid = (int) SuiyiTaoCatId;
            return;
        }
        Long vgCid = VGCatNameIdMap.get(catname);
        if (vgCid == null) {
            this.cid = (int) SuiyiTaoCatId;
            return;
        }
        this.cid = (int) (vgCid.longValue());
    }

    private static final Map<String, Long> VGCatNameIdMap = new HashMap<String, Long>();

    private static final long SuiyiTaoCatId = 788L;//随意淘的cid，原来宠物的cid

    /**
     * | id  | name   | keywords |
    +-----+--------+----------+
    |   2 | 鞋子   | 鞋子     |
    |    3 | 包包         | 包包         |
    |    4 | 配饰         | 配饰         |
    |    5 | 美容         | 美容         |
    |    6 | 家居         | 家居         |
    |  443 | 数码         | 数码         |
    |  493 | 家电         | 家电         |
    |  548 | 女装         | 女装         |
    |  590 | 男装         | 男装         |
    |  611 | 护理         | 护理         |
    |  656 | 美装         | 美装         |
    |  682 | 母婴         | 母婴         |
    |  733 | 美食         | 美食         |
    |  788 | 宠物         | 宠物         |
    |  853 | 运动         | 运动         |
    |  903 | 鲜花         | 鲜花         |
    |  928 | 汽车配件     | 汽车配件     |
    | 1007 | 奢侈品       | 奢侈品       |
     */
    static {
        VGCatNameIdMap.put("女装", 548L);
        VGCatNameIdMap.put("男装", 590L);
        VGCatNameIdMap.put("鞋子", 2L);
        VGCatNameIdMap.put("包包", 3L);
        VGCatNameIdMap.put("美容", 5L);
        VGCatNameIdMap.put("家居", 6L);
        VGCatNameIdMap.put("母婴", 682L);
        VGCatNameIdMap.put("数码", 443L);
        VGCatNameIdMap.put("美食", 733L);

        //随意淘，特殊处理
        VGCatNameIdMap.put("随意淘", SuiyiTaoCatId);
    }

}
