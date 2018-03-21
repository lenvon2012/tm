/**
 * 
 */
package models.fenxiao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

/**
 * @author navins
 * @date: Nov 24, 2013 5:20:29 PM
 */
@Entity(name = ItemDescPlay.TABLE_NAME)
@JsonIgnoreProperties(value = { "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn",
        "propsName" })
public class ItemDescPlay extends GenericModel implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(ItemDescPlay.class);

    @Transient
    public static final String TABLE_NAME = "item_desc_";

    @Transient
    public static ItemDescPlay _instance = new ItemDescPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, _instance);
    
    public static final String LinkSeparator = "!@#";

    @Id
    private Long numIid;

    @Index(name = "user_id")
    private Long userId;

    private Long price;

    @Column(columnDefinition = "varchar(63) default '' not null")
    private String title;

    @Lob
    private String detail;

    private String picUrl;

    @Lob
    private String links;

    private Long ts;

    private Long status = ItemDescOpStatus.NotOperated;
    
    public static class ItemDescOpStatus {
        public static final Long NotOperated = 0L;
        public static final Long hasDeletedBefore = 1L;
    }

    @Transient
    private Map<String, Long> linkActionMap = new HashMap<String, Long>();

    public ItemDescPlay() {
        this.status = 0L;
    }

    public ItemDescPlay(Long userId, Long numIid) {
        super();
        this.userId = userId;
        this.numIid = numIid;
    }

    public ItemDescPlay(ResultSet rs) throws SQLException {
        this.numIid = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.price = rs.getLong(3);
        this.title = rs.getString(4);
        this.detail = rs.getString(5);
        this.picUrl = rs.getString(6);
        this.links = rs.getString(7);
        this.ts = rs.getLong(8);
        this.status = rs.getLong(9);
    }
    
    public void updateByTbItem(Item tbItem, Set<String> itemLinkSet) {
        
        if (tbItem == null) {
            return;
        }
        this.title = tbItem.getTitle();
        this.detail = tbItem.getDesc();
        this.picUrl = tbItem.getPicUrl();
        this.price = (long) NumberUtil.getIntFromPrice(tbItem.getPrice());
        
        this.links = StringUtils.join(itemLinkSet, LinkSeparator);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return detail;
    }

    public void setDesc(String detail) {
        this.detail = detail;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getLinks() {
        return links;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Map<String, Long> getLinkActionMap() {
        return linkActionMap;
    }

    @Override
    public String toString() {
        return "ItemDescPlay [numIid=" + numIid + ", userId=" + userId + ", price=" + price + ", title=" + title
                + ", detail=" + detail + ", picUrl=" + picUrl + ", links=" + links + ", ts=" + ts + ", status="
                + status + ", linkActionMap=" + linkActionMap + "]";
    }

    public Boolean checkLinkActionMap(HashMap<String, Long> linkAction) {
        if (StringUtils.isEmpty(this.links) || CommonUtils.isEmpty(linkAction)) {
            return Boolean.FALSE;
        }
        String[] linkArr = StringUtils.split(links, ItemDescPlay.LinkSeparator);
        this.linkActionMap = new HashMap<String, Long>();
        for (int i = 0; i < linkArr.length; i++) {
            this.linkActionMap.put(linkArr[i], linkAction.get(linkArr[i]));
        }

        return Boolean.TRUE;
    }

    @Override
    public Long getId() {
        return numIid;
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
    public String getIdName() {
        return "numIid";
    }

    @Override
    public String getIdColumn() {
        return "numIid";
    }

    @Override
    public void setId(Long id) {
        this.numIid = id;
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long userId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(userId)));
    }

    static String EXIST_ID_QUERY = "select numIid from item_desc_%s where numIid = ? ";

    public static long findExistId(Long userId, Long numIid) {
        String query = genShardQuery(EXIST_ID_QUERY, userId);
        return dp.singleLongQuery(query, numIid);
    }

    @Override
    public boolean jdbcSave() {
        // log.warn("write for :" + this);

        try {
            // log.info("[current userId :]"+this.userId);
            long existdId = findExistId(this.userId, this.numIid);

            // log.info("[exist ids :]" + existdId);
            if (existdId == 0L) {
                return rawInsert();
            } else {
                return rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    static String insertSQL = "insert into `item_desc_%s`(`numIid`,`userId`,`price`,`title`,`detail`,`picUrl`,`links`,`ts`, `status`) values(?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        
        this.ts = System.currentTimeMillis();
        
        long id = dp.insert(genShardQuery(insertSQL, userId), this.numIid, this.userId, this.price, this.title,
                this.detail, this.picUrl, this.links, this.ts, this.status);

        if (id >= 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    static String updateSQL = "update `item_desc_%s` set  `userId` = ?, `price` = ?, `title` = ?, `detail` = ?, `picUrl` = ?, `links` = ?, `ts` = ?, `status` = ? where `numIid` = ? ";

    public boolean rawUpdate() {

        this.ts = System.currentTimeMillis();
        
        long updateNum = dp.update(genShardQuery(updateSQL, userId), this.userId, this.price, this.title, this.detail,
                this.picUrl, this.links, this.ts, this.status, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }

    
    public boolean rawDelete() {
        
        String deleteSql = "delete from item_desc_%s where userId = ? and numIid = ? ";
        
        deleteSql = genShardQuery(deleteSql, userId);
        
        long deleteNum = dp.update(deleteSql, userId, numIid);
        
        return deleteNum > 0;
        
    }
}
