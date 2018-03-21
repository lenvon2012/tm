/**
 * 
 */

package ppapi.models;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * @author navins
 * @date 2013-7-9 下午12:14:17
 */
@Entity(name = PaiPaiTradeItem.TABLE_NAME)
public class PaiPaiTradeItem extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiTradeItem.class);

    public static final String TAG = "PaiPaiItem";

    public static final String TABLE_NAME = "paipai_tradeitem_";

    public static final PaiPaiTradeItem EMPTY = new PaiPaiTradeItem();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Index(name = "sellerUin")
    private Long sellerUin;

    @Index(name = "dealCode")
    private String dealCode;

    @Index(name = "itemCode")
    private String itemCode;

    private String itemName;

    private String picLink;

    private long createTime;

    private int itemDealPrice;

    private int itemDealCount;

    public PaiPaiTradeItem() {

    }

    public PaiPaiTradeItem(Long sellerUin, String dealCode, String itemCode, String itemName, String picLink,
            long createTime, int itemDealPrice, int itemDealCount) {
        super();
        this.sellerUin = sellerUin;
        this.dealCode = dealCode;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.picLink = picLink;
        this.createTime = createTime;
        this.itemDealPrice = itemDealPrice;
        this.itemDealCount = itemDealCount;
    }
    
    public PaiPaiTradeItem(Long id,Long sellerUin, String dealCode, String itemCode, String itemName, String picLink,
            long createTime, int itemDealPrice, int itemDealCount) {
        this.id=id;
        this.sellerUin = sellerUin;
        this.dealCode = dealCode;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.picLink = picLink;
        this.createTime = createTime;
        this.itemDealPrice = itemDealPrice;
        this.itemDealCount = itemDealCount;
    }

    public Long getSellerUin() {
        return sellerUin;
    }

    public void setSellerUin(Long sellerUin) {
        this.sellerUin = sellerUin;
    }

    public String getDealCode() {
        return dealCode;
    }

    public void setDealCode(String dealCode) {
        this.dealCode = dealCode;
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

    public String getPicLink() {
        return picLink;
    }

    public void setPicLink(String picLink) {
        this.picLink = picLink;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getItemDealPrice() {
        return itemDealPrice;
    }

    public void setItemDealPrice(int itemDealPrice) {
        this.itemDealPrice = itemDealPrice;
    }

    public int getItemDealCount() {
        return itemDealCount;
    }

    public void setItemDealCount(int itemDealCount) {
        this.itemDealCount = itemDealCount;
    }

    @Override
    public String toString() {
        return "PaiPaiTradeItem [sellerUin=" + sellerUin + ", dealCode=" + dealCode + ", itemCode=" + itemCode
                + ", itemName=" + itemName + ", picLink=" + picLink + ", createTime=" + createTime + ", itemDealPrice="
                + itemDealPrice + ", itemDealCount=" + itemDealCount + "]";
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
    static String EXIST_ID_QUERY = "select id from `paipai_tradeitem_%s` where sellerUin  = ? and itemCode = ? and dealCode = ?";

    public static long findExistId(Long sellerUin, String itemCode,String dealCode) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, sellerUin), sellerUin, itemCode, dealCode);
    }

    @Transient
    static String insertSQL = "insert into `paipai_tradeitem_%s`(`sellerUin`,`dealCode`,`itemCode`,`itemName`,`picLink`,`createTime`,`itemDealPrice`,`itemDealCount`) values(?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(genShardQuery(insertSQL, sellerUin), this.sellerUin, this.dealCode, this.itemCode,
                this.itemName, this.picLink, this.createTime, this.itemDealPrice, this.itemDealCount);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }

    @Transient
    static final String updateSQL = "update `paipai_tradeitem_%s` set  `sellerUin` = ?, `dealCode` = ?, `itemCode` = ?, `itemName` = ?, `picLink` = ?, `createTime` = ?, `itemDealPrice` = ?, `itemDealCount` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(updateSQL, this.sellerUin), this.sellerUin, this.dealCode,
                this.itemCode, this.itemName, this.picLink, this.createTime, this.itemDealPrice, this.itemDealCount,
                this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.sellerUin, this.itemCode, this.dealCode);

            if (existdId == 0L) {
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

    @JsonProperty
    public String getTitle() {
        return this.itemName;
    }

    @JsonProperty
    public String getPicPath() {
        return this.picLink;
    }

}
