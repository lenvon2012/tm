
package models.showwindow;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import job.showwindow.ShowWindowExecutor;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = ShowWindowConfig.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn",
})
public class ShowWindowConfig extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(ShowWindowConfig.class);

    @Transient
    public static final String TABLE_NAME = "windowconfig";

    @Transient
    public static ShowWindowConfig EMPTY = new ShowWindowConfig();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

//    @Index(name = "userId")
    @Id
    private Long userId = NumberUtil.DEFAULT_LONG;

    @JsonProperty
    private int priorSaleNum = 0;

    @JsonProperty
    private boolean enableSaleNum = false;

    /**
     * 推荐要求:最低库存
     */
    @JsonProperty
    private int minInstockNum = 0;

    @JsonProperty
    private boolean enableInstockNum = false;

    private static String cacheKey = "_" + TABLE_NAME;

    public static String genKey(Long userId) {
        return cacheKey + userId;
    }

    public static void clearCache(Long userId) {
        String key = genKey(userId);
        Cache.safeDelete(key);
    }

    public boolean isEnableSaleNum() {
        return enableSaleNum;
    }

    public void setEnableSaleNum(boolean enableSaleNum) {
        this.enableSaleNum = enableSaleNum;
    }

    public ShowWindowConfig() {
        super();
        this.priorSaleNum = ShowWindowExecutor.MUST_RECOMMEND_BY_TRADE_ORDER_NUM;
        this.enableSaleNum = true;
    }

    public ShowWindowConfig(Long userId) {
        super();
        this.userId = userId;
        this.priorSaleNum = ShowWindowExecutor.MUST_RECOMMEND_BY_TRADE_ORDER_NUM;
        this.enableSaleNum = true;
        this.enableInstockNum = false;
        this.minInstockNum = 0;
    }

    public ShowWindowConfig(Long userId, int prior_num) {
        this.userId = userId;
        this.priorSaleNum = prior_num;
        this.enableSaleNum = true;
    }

    public ShowWindowConfig(Long userId, int prior_num, boolean enableSaleNum) {
        this.userId = userId;
        this.priorSaleNum = prior_num;
        this.enableSaleNum = enableSaleNum;
    }

    public ShowWindowConfig(Long userId, int priorSaleNum, boolean enableSaleNum, int minInstockNum,
            boolean enableInstockNum) {
        super();
        this.userId = userId;
        this.priorSaleNum = priorSaleNum;
        this.enableSaleNum = enableSaleNum;
        this.minInstockNum = minInstockNum;
        this.enableInstockNum = enableInstockNum;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public int getPriorSaleNum() {
        return priorSaleNum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "userId";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "userId";
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

    static String EXIST_ID_QUERY = "select userId from " + TABLE_NAME + " where userId = ? ";

    private static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    static String PRIOR_NUM_QUERY = "select userId,priorSaleNum,enableSaleNum,minInstockNum,enableInstockNum from "
            + TABLE_NAME
            + " where userId = ? ";

    private static ShowWindowConfig findByUserId(Long userId) {
        ShowWindowConfig config = new JDBCBuilder.JDBCExecutor<ShowWindowConfig>(dp, PRIOR_NUM_QUERY, userId) {
            @Override
            public ShowWindowConfig doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return new ShowWindowConfig(rs.getLong(1), rs.getInt(2), rs.getBoolean(3), rs.getInt(4),
                            rs.getBoolean(5));
                }
                return null;
            }
        }.call();

        return config;
    }

    static String NUM_QUERY = "select priorSaleNum from " + TABLE_NAME + " where userId = ? ";

    public static ShowWindowConfig findOrCreate(Long userId) {
        String key = genKey(userId);
        ShowWindowConfig config = (ShowWindowConfig) Cache.get(key);
        if (config != null) {
            return config;
        }

        config = findByUserId(userId);
        if (config != null) {
            Cache.set(key, config, (4 + (userId.longValue() / 64L % 4)) + "d");
            return config;
        }
        config = new ShowWindowConfig(userId);
        config.jdbcSave();
        Cache.set(key, config, (4 + (userId.longValue() / 64L % 4)) + "d");
        return config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {
        clearCache(userId);

        try {
            long existdId = findExistId(this.userId);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
//                this.userId= existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    boolean rawInsert() {
        long id = dp
                .insert(
                        "insert into `"
                                + TABLE_NAME
                                + "`(`userId`,`priorSaleNum`,`enableSaleNum`,`minInstockNum`,`enableInstockNum`) values(?,?,?,?,?)",
                        this.userId, this.priorSaleNum, this.enableSaleNum, this.minInstockNum, this.enableInstockNum);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    boolean rawUpdate() {
        long updateNum = dp
                .update(
                        "update `"
                                + TABLE_NAME
                                + "` set  `priorSaleNum` = ?, `enableSaleNum` = ?, `minInstockNum` = ?,`enableInstockNum` = ? where `userId` = ? ",
                        this.priorSaleNum, this.enableSaleNum, this.minInstockNum, this.enableInstockNum, this.userId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :[userId : ]" + this.userId);
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
        this.userId = id;
    }

//
//    public int getPriorSaleNum() {
//        return priorSaleNum;
//    }

    public void setPriorSaleNum(int priorSaleNum) {
        this.priorSaleNum = priorSaleNum;
    }

    @Override
    public String toString() {
        return "ShowWindowConfig [userId=" + userId + ", priorSaleNum=" + priorSaleNum + ", enableSaleNum="
                + enableSaleNum + ", minInstockNum=" + minInstockNum + ", enableInstockNum=" + enableInstockNum + "]";
    }

    public void setMinInstockNum(int minInstockNum) {
        this.minInstockNum = minInstockNum;
    }

    public boolean isEnableInstockNum() {
        return enableInstockNum;
    }

    public void setEnableInstockNum(boolean enableInstockNum) {
        this.enableInstockNum = enableInstockNum;
    }

    public int checkPrioSaleNum() {
        if (this.enableSaleNum) {
            return this.priorSaleNum;
        } else {
            return 0;
        }
    }

    public int checkMinStockNum() {
        if (this.enableInstockNum) {
            return this.minInstockNum;
        } else {
            return 0;
        }
    }

}
