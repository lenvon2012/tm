/**
 * 
 */

package ppapi.models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

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
@Entity(name = PaiPaiTradeDisplay.TABLE_NAME)
public class PaiPaiTradeDisplay extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiTradeDisplay.class);

    public static final String TAG = "PaiPaiItem";

    public static final String TABLE_NAME = "paipai_trade_display_";

    public static final PaiPaiTradeDisplay EMPTY = new PaiPaiTradeDisplay();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Index(name = "sellerUin")
    private Long sellerUin;

    private long buyerUin;

    @Index(name = "dealCode")
    private String dealCode;

    private String buyerName;

    private String receiverAddress;

    private String receiverName;

    private String receiverMobile;

    private String receiverPhone;

    /**
     * 状态码参见：http://pop.paipai.com/bin/view/Main/dealState
     */
    private String dealState;

    private String dealRateState;

    private long createTime;

    private long payTime;

    private int totalCash;

    @Transient
    public List<PaiPaiTradeItem> itemList;

    public PaiPaiTradeDisplay() {

    }

    public PaiPaiTradeDisplay(Long sellerUin, long buyerUin, String dealCode, String buyerName, String receiverAddress,
            String receiverName, String receiverMobile, String receiverPhone, String dealState, String dealRateState,
            long createTime, long payTime, int totalCash) {
        super();
        this.sellerUin = sellerUin;
        this.buyerUin = buyerUin;
        this.dealCode = dealCode;
        this.buyerName = buyerName;
        this.receiverAddress = receiverAddress;
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.receiverPhone = receiverPhone;
        this.dealState = dealState;
        this.dealRateState = dealRateState;
        this.createTime = createTime;
        this.payTime = payTime;
        this.totalCash = totalCash;
    }
    
    public PaiPaiTradeDisplay(Long id,Long sellerUin, long buyerUin, String dealCode, String buyerName, String receiverAddress,
            String receiverName, String receiverMobile, String receiverPhone, String dealState, String dealRateState,
            long createTime, long payTime, int totalCash) {
        this.id=id;
        this.sellerUin = sellerUin;
        this.buyerUin = buyerUin;
        this.dealCode = dealCode;
        this.buyerName = buyerName;
        this.receiverAddress = receiverAddress;
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.receiverPhone = receiverPhone;
        this.dealState = dealState;
        this.dealRateState = dealRateState;
        this.createTime = createTime;
        this.payTime = payTime;
        this.totalCash = totalCash;
    }

    public Long getSellerUin() {
        return sellerUin;
    }

    public void setSellerUin(Long sellerUin) {
        this.sellerUin = sellerUin;
    }

    public long getBuyerUin() {
        return buyerUin;
    }

    public void setBuyerUin(long buyerUin) {
        this.buyerUin = buyerUin;
    }

    public String getDealCode() {
        return dealCode;
    }

    public void setDealCode(String dealCode) {
        this.dealCode = dealCode;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverMobile() {
        return receiverMobile;
    }

    public void setReceiverMobile(String receiverMobile) {
        this.receiverMobile = receiverMobile;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getDealState() {
        return dealState;
    }

    public void setDealState(String dealState) {
        this.dealState = dealState;
    }

    public String getDealRateState() {
        return dealRateState;
    }

    public void setDealRateState(String dealRateState) {
        this.dealRateState = dealRateState;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getPayTime() {
        return payTime;
    }

    public void setPayTime(long payTime) {
        this.payTime = payTime;
    }

    public int getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(int totalCash) {
        this.totalCash = totalCash;
    }

    public List<PaiPaiTradeItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<PaiPaiTradeItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public String toString() {
        return "PaiPaiTradeDisplay [sellerUin=" + sellerUin + ", buyerUin=" + buyerUin + ", dealCode=" + dealCode
                + ", buyerName=" + buyerName + ", receiverAddress=" + receiverAddress + ", receiverName="
                + receiverName + ", receiverMobile=" + receiverMobile + ", receiverPhone=" + receiverPhone
                + ", dealState=" + dealState + ", dealRateState=" + dealRateState + ", createTime=" + createTime
                + ", payTime=" + payTime + ", totalCash=" + totalCash + ", itemList=" + itemList + "]";
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
    static String EXIST_ID_QUERY = "select id from `paipai_trade_display_%s` where sellerUin = ? and dealCode = ?";

    public static long findExistId(Long sellerUin, String dealCode) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, sellerUin), sellerUin, dealCode);
    }

    @Transient
    static String insertSQL = "insert into `paipai_trade_display_%s`(`sellerUin`,`dealCode`,`buyerUin`,`buyerName`,`receiverAddress`,`receiverName`,`receiverMobile`,`receiverPhone`,`dealState`,`dealRateState`,`createTime`,`payTime`,`totalCash`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(genShardQuery(insertSQL, sellerUin), this.sellerUin, this.dealCode, this.buyerUin,
                this.buyerName, this.receiverAddress, this.receiverName, this.receiverMobile, this.receiverPhone,
                this.dealState, this.dealRateState, this.createTime, this.payTime, this.totalCash);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }

    @Transient
    static final String updateSQL = "update `paipai_trade_display_%s` set `sellerUin` = ?, `dealCode` = ?, `buyerUin` = ?, `buyerName` = ?, `receiverAddress` = ?, `receiverName` = ?, `receiverMobile` = ?, `receiverPhone` = ?, `dealState` = ?, `dealRateState`=?, `createTime` = ?, `payTime` = ?, `totalCash` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(updateSQL, this.sellerUin), this.sellerUin, this.dealCode,
                this.buyerUin, this.buyerName, this.receiverAddress, this.receiverName, this.receiverMobile,
                this.receiverPhone, this.dealState, this.dealRateState, this.createTime, this.payTime, this.totalCash,
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
            long existdId = findExistId(this.sellerUin, this.dealCode);

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

}
