
package models.jd;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = JDCrmMember.TABLE_NAME)
public class JDCrmMember extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(JDCrmMember.class);

    public static final String TABLE_NAME = "JDCrmMember_";

    private static JDCrmMember EMPTY = new JDCrmMember();

    public static final DBDispatcher CrmMemberDp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    //京东卖家id
    @Index(name = "sellerId")
    private long sellerId;

    //京东买家nick
    @Index(name = "buyerNick")
    private String buyerNick;

    //member的手机号
    private String telephone;

    //member客单价
    private double avgPrice;

    //最后一次交易订单号
    private long bizOrderId;

    //交易关闭的金额
    private double closeTradeAmount;

    //交易关闭的笔数
    private long closeTradeCount;

    //会员等级
    private long grade;

    //会员拥有的分组
    private String groupIds;

    //交易关闭的宝贝个数
    private long itemCloseCount;

    //购买宝贝的个数
    private long itemNum;

    //最后交易时间
    private long lastTradeTime;

    //买家地区
    private String area;

    /**
     * 关系来源，1交易成功，2未成交
     */
    private long relationSource;

    /**
     * 显示会员的状态，normal正常，delete被买家删除，blacklist黑名单
     */
    private int status;

    //交易成功的金额
    private double tradeAmount;

    //交易成功笔数
    private long tradeCount;

    //更新的时间戳
    private long updateTs;

    public static class CrmMemberStatus {
        public static final int Normal = 1;

        public static final int Delete = 2;

        public static final int BlackList = 4;
    }

    public static class CrmMemberGrade {
        public static final int Normal = 1;//普通会员

        public static final int Advance = 2;//高级会员

        public static final int VIP = 3;//VIP会员

        public static final int God = 4;//至尊VIP会员
    }

    public long getSellerId() {
        return sellerId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBuyerNick() {
        return buyerNick;
    }

    public void setBuyerNick(String buyerNick) {
        this.buyerNick = buyerNick;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public double getCloseTradeAmount() {
        return closeTradeAmount;
    }

    public void setCloseTradeAmount(double closeTradeAmount) {
        this.closeTradeAmount = closeTradeAmount;
    }

    public double getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(double tradeAmount) {
        this.tradeAmount = tradeAmount;
    }

    public long getBizOrderId() {
        return bizOrderId;
    }

    public void setBizOrderId(long bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public long getCloseTradeCount() {
        return closeTradeCount;
    }

    public void setCloseTradeCount(long closeTradeCount) {
        this.closeTradeCount = closeTradeCount;
    }

    public long getGrade() {
        return grade;
    }

    public void setGrade(long grade) {
        this.grade = grade;
    }

    public String getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public long getItemCloseCount() {
        return itemCloseCount;
    }

    public void setItemCloseCount(long itemCloseCount) {
        this.itemCloseCount = itemCloseCount;
    }

    public long getItemNum() {
        return itemNum;
    }

    public void setItemNum(long itemNum) {
        this.itemNum = itemNum;
    }

    public long getLastTradeTime() {
        return lastTradeTime;
    }

    public void setLastTradeTime(long lastTradeTime) {
        this.lastTradeTime = lastTradeTime;
    }

    public long getRelationSource() {
        return relationSource;
    }

    public void setRelationSource(long relationSource) {
        this.relationSource = relationSource;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public void setTradeCount(long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public long getTradeCount() {
        return tradeCount;
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

        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static String genShardQuery(String query, Long sellerId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(sellerId)));
    }

    private static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    private static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + "%s where sellerId = ? and buyerNick = ? ";

    private static long findExistId(long sellerId, String buyerNick) {

        String sql = genShardQuery(EXIST_ID_QUERY, sellerId);

        return CrmMemberDp.singleLongQuery(sql, sellerId, buyerNick);

    }

    public boolean rawInsert() {
        String insertSql = "insert into `" + TABLE_NAME + "%s`(`sellerId`,`buyerNick`," +
                "`telephone`,`avgPrice`,`bizOrderId`,`area`,`closeTradeAmount`,`closeTradeCount`," +
                "`grade`,`groupIds`,`itemCloseCount`,`itemNum`,`lastTradeTime`,`relationSource`," +
                "`status`,`tradeAmount`,`tradeCount`,`updateTs`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        insertSql = genShardQuery(insertSql, this.sellerId);

        long id = CrmMemberDp.insert(insertSql, this.sellerId, this.buyerNick,
                this.telephone, this.avgPrice, this.bizOrderId, this.area, this.closeTradeAmount, this.closeTradeCount,
                this.grade, this.groupIds, this.itemCloseCount, this.itemNum, this.lastTradeTime, this.relationSource,
                this.status, this.tradeAmount, this.tradeCount, this.updateTs);

        if (id > 0L) {
            return true;
        } else {
            log.error("insert crmMember error, buyerNick: " + buyerNick);
            return false;
        }

    }

    public boolean rawUpdate() {

        String updateSql = "update `" + TABLE_NAME + "%s` set " +
                "`telephone`=?,`avgPrice`=?,`bizOrderId`=?,`area`=?,`closeTradeAmount`=?,`closeTradeCount`=?," +
                "`grade`=?,`groupIds`=?,`itemCloseCount`=?,`itemNum`=?,`lastTradeTime`=?,`relationSource`=?," +
                "`status`=?,`tradeAmount`=?,`tradeCount`=?,`updateTs`=? where `sellerId`=? and `buyerNick`=? ";

        updateSql = genShardQuery(updateSql, this.sellerId);

        long updateNum = CrmMemberDp.insert(updateSql,
                this.telephone, this.avgPrice, this.bizOrderId, this.area, this.closeTradeAmount, this.closeTradeCount,
                this.grade, this.groupIds, this.itemCloseCount, this.itemNum, this.lastTradeTime, this.relationSource,
                this.status, this.tradeAmount, this.tradeCount, this.updateTs, this.sellerId, this.buyerNick);

        if (updateNum == 1L) {
            return true;
        } else {
            log.error("update crmMember error, buyerNick: " + buyerNick);
            return false;
        }

    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.sellerId, this.buyerNick);

            boolean status = false;
            if (existdId <= 0L) {
                status = rawInsert();
            } else {
                status = rawUpdate();
            }

            return status;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String getIdName() {

        return "id";
    }

}
