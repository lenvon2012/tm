/**
 * 
 */

package ppapi.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.paipai.PaiNumIidToItemCode;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

/**
 * @author navins
 * @date 2013-7-9 下午12:14:17
 */
@Entity(name = PaiPaiItem.TABLE_NAME)
public class PaiPaiItem extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiItem.class);

    public static final String TAG = "PaiPaiItem";

    public static final String TABLE_NAME = "paipai_item_";

    public static PaiPaiItem _instance = new PaiPaiItem();

    public PaiPaiItem() {
    }

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, _instance);

    @Index(name = "sellerUin")
    private Long sellerUin;

    @Index(name = "itemCode")
    private String itemCode;

    private String itemName;

    private String itemState;

    private String picLink;

    private Long createTime;

    /**
     * 商品所属的拍拍类目id。每个商品只能从属于一个拍拍类目id，且必须从属于一个类目id。
     */
    private Long categoryId;

    private int itemPrice;

    private int visitCount;
    
    private int type;
    
    @Transient
    private Long longNumIid;
    
    @Transient
    public List<PaiPaiItemAttr> attr;
    
    public static class PaiPaiItemAttr {
        public int attrId;
        public String attrName;
        public List<Long> attrOptionId;
        public List<String> attrOptionName;
        
        public PaiPaiItemAttr(int attrId, String attrName, List<Long> attrOptionId, List<String> attrOptionName) {
            super();
            this.attrId = attrId;
            this.attrName = attrName;
            this.attrOptionId = attrOptionId;
            this.attrOptionName = attrOptionName;
        }

        public int getAttrId() {
			return attrId;
		}

		public void setAttrId(int attrId) {
			this.attrId = attrId;
		}

		public String getAttrName() {
			return attrName;
		}

		public void setAttrName(String attrName) {
			this.attrName = attrName;
		}

		public List<Long> getAttrOptionId() {
			return attrOptionId;
		}

		public void setAttrOptionId(List<Long> attrOptionId) {
			this.attrOptionId = attrOptionId;
		}

		public List<String> getAttrOptionName() {
			return attrOptionName;
		}

		public void setAttrOptionName(List<String> attrOptionName) {
			this.attrOptionName = attrOptionName;
		}

		@Override
        public String toString() {
            return "PaiPaiItemAttr [attrId=" + attrId + ", attrName=" + attrName + ", attrOptionId=" + attrOptionId
                    + ", attrOptionName=" + attrOptionName + "]";
        }
        
        public String toFormat() {
        	String props = StringUtils.EMPTY;
        	
        	return props;
        }
        
    }
    
    
    public static class Type {
        public static final int PROMOTING = 1;

        public static final int NN_PROMOTING = 0;

        public static final int RELATED = 2;

        public static final int POPULARIZED = 4;
        
        public static final int DISCOUNT=8;
    }

    public PaiPaiItem(Long sellerUin, String itemCode, String itemName, String itemState,
            String picLink, Long createTime, Long categoryId, int itemPrice, int visitCount) {
        super();
        this.sellerUin = sellerUin;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemState = itemState;
        this.categoryId = categoryId;
        this.picLink = picLink;
        this.createTime = createTime;
        this.itemPrice = itemPrice;
        this.visitCount = visitCount;
    }

    public PaiPaiItem(ResultSet rs) throws SQLException {
        this.sellerUin = rs.getLong(1);
        this.itemCode = rs.getString(2);
        this.itemName = rs.getString(3);
        this.itemState = rs.getString(4);
        this.picLink = rs.getString(5);
        this.createTime = rs.getLong(6);
        this.categoryId = rs.getLong(7);
        this.itemPrice = rs.getInt(8);
        this.visitCount = rs.getInt(9);
        this.id = rs.getLong(10);
        this.type = rs.getInt(11);
    }

    public Long getSellerUin() {
        return sellerUin;
    }

    public void setSellerUin(Long sellerUin) {
        this.sellerUin = sellerUin;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemState() {
        return itemState;
    }

    public void setItemState(String itemState) {
        this.itemState = itemState;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getPicLink() {
        return picLink;
    }

    public void setPicLink(String picLink) {
        this.picLink = picLink;
    }
    
    public String getPicURL() {
        return picLink;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
    
    public void setDiscount() {
        this.type |= Type.DISCOUNT;
    }

    public void setUnDiscount() {
        this.type &= ~Type.DISCOUNT;
    }

    public boolean isDiscount() {
        return (this.type & Type.DISCOUNT) > 0;
    }
    
    public List<PaiPaiItemAttr> getAttr() {
        return attr;
    }

    public void setAttr(List<PaiPaiItemAttr> attr) {
        this.attr = attr;
    }
    

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long sellerUin) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(sellerUin)));
    }

    @Transient
    static String EXIST_ID_QUERY = "select id from `paipai_item_%s` where sellerUin  = ? and itemCode = ?";

    public static long findExistId(Long sellerUin, String itemCode) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, sellerUin), sellerUin, itemCode);
    }

    @Transient
    static String insertSQL = "insert into `paipai_item_%s`(`sellerUin`,`itemCode`,`itemName`,`itemState`,`picLink`,`createTime`,`categoryId`,`itemPrice`,`visitCount`,`type`) values(?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(genShardQuery(insertSQL, sellerUin), this.sellerUin, this.itemCode, this.itemName,
                this.itemState, this.picLink, this.createTime, this.categoryId, this.itemPrice, this.visitCount, this.type);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }

    @Transient
    static final String updateSQL = "update `paipai_item_%s` set  `sellerUin` = ?, `itemCode` = ?, `itemName` = ?, `itemState` = ?, `categoryId` = ?, `itemPrice` = ?, `picLink` = ?, `createTime` = ?, `visitCount` = ?, `type` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.update(genShardQuery(updateSQL, this.sellerUin), this.sellerUin, this.itemCode,
                this.itemName, this.itemState, this.categoryId, this.itemPrice, this.picLink, this.createTime,
                this.visitCount, this.type, this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            log.warn("Update Fails... for :" + sellerUin + "   itemCode=" + itemCode);
            return false;
        }
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.sellerUin, this.itemCode);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                this.setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public String toString() {
        return "PaiPaiItem [sellerUin=" + sellerUin + ", itemCode=" + itemCode + ", itemName=" + itemName
                + ", itemState=" + itemState + ", categoryId=" + categoryId + ", itemPrice=" + itemPrice + ", picLink="
                + picLink + ", createTime=" + createTime + ", visitCount=" + visitCount + "]";
    }

    @JsonProperty
    public String getTitle() {
        return this.itemName;
    }

    @JsonProperty
    public String getPicPath() {
        return this.picLink;
    }

    public static class ListFetcher extends JDBCExecutor<List<PaiPaiItem>> {
        public ListFetcher(Long hashKeyId, String whereQuery, Object... params) {
            super(whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select sellerUin,itemCode,itemName,itemState,picLink,createTime,categoryId,itemPrice,visitCount,id,type from ");
            sb.append(TABLE_NAME + PaiPaiItem._instance.getTableHashKey(hashKeyId));
            sb.append(" where  sellerUin =  ");
            sb.append(hashKeyId);
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }
            this.src = dp.getDataSrc(hashKeyId);
            this.query = sb.toString();

        }

        @Override
        public List<PaiPaiItem> doWithResultSet(ResultSet rs) throws SQLException {
            List<PaiPaiItem> list = new ArrayList<PaiPaiItem>();
            while (rs.next()) {
                list.add(new PaiPaiItem(rs));
            }
            return list;
        }
    }

    public static int count(Long hashKeyId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");

        sb.append(TABLE_NAME + PaiPaiItem._instance.getTableHashKey(hashKeyId));
        sb.append(" where  sellerUin =  ");
        sb.append(hashKeyId);
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(dp.getDataSrc(hashKeyId), sb.toString(), params);

    }

    public long getTableHashKey(Long hashKeyId) {
        return DBBuilder.genUserIdHashKey(hashKeyId);
    }

    public static TMResult fetchTMResult(Long sellerUin, String itemName, PageOffset po) {
        StringBuilder sb = new StringBuilder(" 1 = 1 ");
        if (!StringUtils.isEmpty(itemName)) {
            sb.append(" and itemName like '");
            sb.append(CommonUtils.escapeSQL(itemName));
            sb.append("' ");
        }
//        sb.append(" limit ? offset ?");

        List<PaiPaiItem> list = new ListFetcher(sellerUin, sb.toString() + "  limit ? offset ?", po.getPs(),
                po.getOffset()).call();
        int count = count(sellerUin, sb.toString());
        return new TMResult(list, count, po);
    }

    @JsonProperty
    public String getNumIid() {
        return this.itemCode;
    }
    
    
    public Long initLongNumIid() {
        this.longNumIid = PaiNumIidToItemCode.ensureItemCode(itemCode);
        
        return longNumIid;
    }
    
    public Long getLongNumIid() {
        return longNumIid;
    }
}
