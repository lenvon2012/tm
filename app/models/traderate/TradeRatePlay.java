package models.traderate;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
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

import com.taobao.api.domain.TradeRate;

@JsonIgnoreProperties(value = { "tableHashKey", "persistent", "tableName", "idName", "idColumn", "entityId" })
@Entity(name = TradeRatePlay.TABLE_NAME)
public class TradeRatePlay extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(TradeRatePlay.class);

    @Transient
    public static final String TABLE_NAME = "trade_rate_";

    @Transient
    public static TradeRatePlay EMPTY = new TradeRatePlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Index(name = "numIid")
    private long numIid;

    private long tid;

    @Id
    private long oid;

    @Index(name = "userId")
    private Long userId;

    // 买家评价时间
    private long created;

    // 买家再次评价时间
    private long updated;
    
    // 卖家评价时间
    private long sellerTs;

    // 评价 good: 1, neutral: 2, bad: 3, 如果有改评价，低2位为当前评价，3-4位为之前评价
    // 当3-4位存在时，低2位为：0，表示删除评价
    private int rate;
    
    // 卖家评价 good: 1, neutral: 2, bad: 3, 如果有改评价，低2位为当前评价，3-4位为之前评价
    private int sellerRate;
    
    // 角色类型：卖家：1， 买家：0   @Depreciated 该字段没意义：买家评价，卖家评价放一条记录
    private int roleType;

    private double price;

    private Boolean validScore;

    /**
     * 不管谁评价的，这里都是买家名
     */
    @Index(name = "buyernick")
    private String nick;

    /**
     * 不管谁评价的，这里都是卖家名
     */
    @Transient
    private String ratedNick;

    // 评价结果,可选值:good(好评),neutral(中评),bad(差评)
    @Transient
    private String result;

    @Transient
    private String itemPrice;

    private String itemTitle;

    /**
     * 买家评价
     */
    private String content;

    /**
     * 卖家评价
     */
    private String reply;

    // 评价者角色.可选值:seller(卖家),buyer(买家)
    @Transient
    private String role;

    /**
     * 买家二次评价
     */
    private String reverse;
    
    private String remark;
    
    private Long dispatchId;

    @Transient
    private String groupName;
    
    @Transient
    private String tidStr;
    
    @Transient
    private String oidStr;

    public TradeRatePlay() {

    }

    public TradeRatePlay(long numIid, long tid, long oid, Long userId, long created, Boolean validScore, String nick,
            String ratedNick, String result, String itemPrice, String itemTitle, String content, String reply,
            String role) {
        super();
        this.numIid = numIid;
        this.tid = tid;
        this.oid = oid;
        this.userId = userId;
        this.validScore = validScore;
        this.result = result;
        this.itemPrice = itemPrice;
        this.itemTitle = itemTitle;

        if ("buyer".equals(role)) {
            this.nick = nick;
            this.ratedNick = ratedNick;

            this.content = content;
            if (this.content != null && this.content.length() >= 127) {
                this.content = this.content.substring(0, 127);
            }
            
            this.created = created;
            this.updated = this.created;
            
            this.rate = parseRate(result);
        } else {
            this.nick = ratedNick;
            this.ratedNick = nick;

            this.reply = reply;
            if (this.reply != null && this.reply.length() >= 127) {
                this.reply = this.reply.substring(0, 127);
            }
            
            this.created = 0;
            this.updated = 0;
            this.sellerTs = created;
            
            this.sellerRate = parseRate(result);
        }
        this.role = role;

        this.price = parsePrice(itemPrice);
        this.roleType = parseRole(role);
    }

    public TradeRatePlay(TradeRate tradeRate, Long userId) {
        super();
        this.userId = userId;
        this.numIid = tradeRate.getNumIid();
        this.tid = tradeRate.getTid();
        this.oid = tradeRate.getOid();
        this.result = tradeRate.getResult();
        this.itemPrice = tradeRate.getItemPrice();
        this.itemTitle = tradeRate.getItemTitle();

        if ("buyer".equals(tradeRate.getRole())) {
            this.nick = tradeRate.getNick();
            this.ratedNick = tradeRate.getRatedNick();

            this.content = shortComment(tradeRate.getContent());
            this.created = tradeRate.getCreated() == null ? System.currentTimeMillis() : tradeRate.getCreated().getTime();
            this.updated = this.created;
            this.sellerTs = 0;
            this.rate = parseRate(result);
        } else {
            this.nick = tradeRate.getRatedNick();
            this.ratedNick = tradeRate.getNick();

            this.reply = shortComment(tradeRate.getContent());
            this.created = 0;
            this.updated = 0;
            this.sellerTs = tradeRate.getCreated() == null ? System.currentTimeMillis() : tradeRate.getCreated().getTime();
            this.sellerRate = parseRate(result);
        }

        this.role = tradeRate.getRole();

        this.price = parsePrice(itemPrice);
        this.roleType = parseRole(role);
    }

    public TradeRatePlay(ResultSet rs) throws SQLException {
        this.numIid = rs.getLong(1);
        this.tid = rs.getLong(2);
        this.oid = rs.getLong(3);
        this.userId = rs.getLong(4);
        this.created = rs.getLong(5);
        this.rate = rs.getInt(6);
        this.roleType = rs.getInt(7);
        this.price = rs.getDouble(8);
        this.validScore = rs.getBoolean(9);
        this.nick = rs.getString(10);
        this.itemTitle = rs.getString(11);
        this.content = rs.getString(12);
        this.reply = rs.getString(13);
        this.reverse = rs.getString(14);
        this.updated = rs.getLong(15);
        this.sellerTs = rs.getLong(16);
        this.sellerRate = rs.getInt(17);
        this.remark = rs.getString(18);
        this.dispatchId = rs.getLong(19);
    }

    public static int parseRate(String result) {
        // 评价结果,可选值:good(好评),neutral(中评),bad(差评)
        if (StringUtils.isEmpty(result)) {
            return 1;
        }
        if ("good".equals(result)) {
            return 1;
        }
        if ("neutral".equals(result)) {
            return 2;
        }
        if ("bad".equals(result)) {
            return 3;
        }
        return 1;
    }

    public int getRecentRate() {
        return this.rate & 3;
    }

    public static double parsePrice(String itemPrice) {
        if (StringUtils.isEmpty(itemPrice)) {
            return 0;
        }
        return Double.valueOf(itemPrice.trim());
    }

    public static int parseRole(String role) {
        if (StringUtils.isEmpty(role)) {
            return 0;
        }
        if ("seller".equals(role)) {
            return 1;
        }
        return 0;
    }

    public void setNewRate(int rate) {
        this.rate = (this.rate << 2) + rate;
    }

    public int getLatestRate() {
        return this.rate & 3;
    }
    
    public int getPreviousRate() {
        return ((this.rate >> 2) & 3);
    }

    /**
     * 保存seller的评价信息，不保存seller的评分
     * 
     * @param tradeRate
     */
    public Boolean putTradeRate(TradeRate tradeRate) {
        if ("seller".equals(tradeRate.getRole())) {
            if (this.sellerRate > 0) {
                return Boolean.FALSE;
            }
            this.sellerRate = parseRate(tradeRate.getResult());
            this.sellerTs = tradeRate.getCreated() == null ? System.currentTimeMillis() : tradeRate.getCreated().getTime();
            this.reply = shortComment(tradeRate.getContent());
            return Boolean.TRUE;
        } else {
            int newRate = parseRate(tradeRate.getResult());
            String comment = shortComment(tradeRate.getContent());
            // 只关注评价改变的，不关注追评
            if (this.getLatestRate() != newRate) {  //|| (!comment.startsWith(this.content) && comment.startsWith(this.reverse))
                if (this.getLatestRate() == 0) {
//                    this.rate = newRate;
                    this.content = comment;
                    if (this.getPreviousRate() == newRate) {
                        this.rate = (this.rate >> 2);
                    } else {
                        this.rate += newRate;
                    }
                } else {
                    this.setNewRate(newRate);
                    this.reverse = comment;
                    this.updated = System.currentTimeMillis();
                }
                
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
    }
    
    public String shortComment(String comment) {
        if (StringUtils.isEmpty(comment)) {
            return StringUtils.EMPTY; 
        }
        if (comment.length() > 250) {
            comment = comment.substring(0, 250);
        }
        return comment;
    }
    
    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getSellerTs() {
        return sellerTs;
    }

    public void setSellerTs(long sellerTs) {
        this.sellerTs = sellerTs;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getSellerRate() {
        return sellerRate;
    }

    public void setSellerRate(int sellerRate) {
        this.sellerRate = sellerRate;
    }

    public int getRoleType() {
        return roleType;
    }

    public void setRoleType(int roleType) {
        this.roleType = roleType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Boolean getValidScore() {
        return validScore;
    }

    public void setValidScore(Boolean validScore) {
        this.validScore = validScore;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getRatedNick() {
        return ratedNick;
    }

    public void setRatedNick(String ratedNick) {
        this.ratedNick = ratedNick;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getReverse() {
        return reverse;
    }

    public void setReverse(String reverse) {
        this.reverse = reverse;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(Long dispatchId) {
        this.dispatchId = dispatchId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTidStr() {
		return tidStr;
	}

	public void setTidStr(String tidStr) {
		this.tidStr = tidStr;
	}

	public String getOidStr() {
		return oidStr;
	}

	public void setOidStr(String oidStr) {
		this.oidStr = oidStr;
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
        // TODO Auto-generated method stub
        return "oid";
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return oid;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "oid";
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.oid = id;
    }

    @Override
    public String toString() {
        return "TradeRatePlay [numIid=" + numIid + ", tid=" + tid + ", oid=" + oid + ", userId=" + userId
                + ", created=" + created + ", rate=" + rate + ", roleType=" + roleType + ", price=" + price
                + ", validScore=" + validScore + ", nick=" + nick + ", ratedNick=" + ratedNick + ", result=" + result
                + ", itemPrice=" + itemPrice + ", itemTitle=" + itemTitle + ", content=" + content + ", reply=" + reply
                + ", role=" + role + ", reverse=" + reverse + ", remark=" + remark + "]";
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.oid);

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

    static String EXIST_ID_QUERY = "select oid from `trade_rate_%s` where oid  = ?";

    public static long findExistId(Long userId, Long oid) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, userId), oid);
    }

    static String insertSQL = "insert into `trade_rate_%s`(`numIid`,`tid`,`oid`,`userId`,`created`,`rate`,`roleType`,`price`,`validScore`,`nick`,`itemTitle`,`content`,`reply`,`reverse`, `updated`, `sellerTs`, `sellerRate`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = dp.insert(genShardQuery(insertSQL, this.userId), this.numIid, this.tid, this.oid, this.userId,
                this.created, this.rate, this.roleType, this.price, this.validScore, this.nick, this.itemTitle,
                this.content, this.reply, this.reverse, this.updated, this.sellerTs, this.sellerRate);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }

    public static final String updateSQL = "update `trade_rate_%s` set  `numIid` = ?, `tid` = ?, `userId` = ?, `created` = ?, `rate` = ?, `roleType` = ?, `price` = ?, `validScore` = ?, `nick` = ?, `itemTitle` = ?, `content` = ?, `reply` = ?, `reverse` = ?, `updated` = ?, `sellerTs` = ?, `sellerRate` = ? where `oid` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(updateSQL, this.userId), this.numIid, this.tid, this.userId,
                this.created, this.rate, this.roleType, this.price, this.validScore, this.nick, this.itemTitle,
                this.content, this.reply, this.reverse, this.updated, this.sellerTs, this.sellerRate, this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }

}
