
package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import configs.Subscribe.Version;
import dao.UserDao;

@Entity(name = User.TABLE_NAME)
public class User extends GenericModel implements Comparable<User>, PolicySQLGenerator {

    private static final long serialVersionUID = 121241341377009029L;

    @Transient
    public static User EMPTY = new User();

    @Transient
    public static final String TABLE_NAME = "user";

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Transient
    private static final Logger log = LoggerFactory.getLogger(User.class);

    /**
     * 只在 addUser的时候会被设置成true
     */
    @Transient
    boolean isNew = false;

    /**
     * 只在 子账号登陆的时候为true
     */
    @Transient
    boolean isSub = false;
    
    @Id
    @PolicySQLGenerator.CodeNoUpdate
    public Long id;

    @Index(name = "name")
    public String userNick;

    @Index(name = "sessionKey")
    public String sessionKey;

    @Transient
    public boolean isVaild;

    public Long firstLoginTime;

    public long lastUpdateTime = System.currentTimeMillis();

    /**
     * @see Version
     */
    int version;

    @Transient
    public boolean hasShop;

    public int cid;

    public int level = 0;

    public static class Type {
        public static final int HASH_MODE = 1;

        public static final int HAS_ESERVICE = 2;

        public static final int IS_TMALL = 4;

        public static final int HAS_SHOP = 8;

        public static final int IS_VALID = 16;

        public static final int IS_SHOWWINDOW_ON = 32;

        public static final int IS_DELIST_ON = 64;

        public static final int IS_AUTOCOMMENT_ON = 128;

        public static final int IS_salesCount_On = 256;

        public static final int IS_AutoDefense_On = 512;

        public static final int IS_SendDefenseMsg_On = 1024;//发送短信

        public static final int IS_FENGXIAO = 2048;

        public static final int IS_GONGHUO = 4096;

        public static final int IS_POPULAR_AWARD = 8192;

        public static final int IS_POPULAR_OFF = 16384;

        public static final int IS_BlackListAutoDefense_On = 32768;

        public static final int IS_WhiteListAutoDefense_On = 65536;

        public static final int IS_AutoChapingBlackListOn = 131072;

        public static final int IS_AutoRefundBlackListOn = 262144;

        public static final int IS_BadCommentNoticeOff = 524288;//中差评短信提醒

        public static final int IS_BadCommentBuyerSms = 1048576;//中差评给买家短信

        public static final int IS_DefenseNoticeSmsOff = 2097152;//差评拦截通知关闭
        
        public static final int IS_ShowOldDiscount = 4194304;//旧版打折
    }
    
    public boolean isShowOldDiscount() {
        return (this.type & Type.IS_ShowOldDiscount) > 0;
    }

    public void setShowOldDiscount(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_ShowOldDiscount;
        } else {
            this.type &= (~Type.IS_ShowOldDiscount);
        }
        log.error(" current type:" + this.type);
    }

    public boolean isPopularOff() {
        return (this.type & Type.IS_POPULAR_OFF) > 0;
        //return false;
    }

    public boolean isSub() {
		return isSub;
	}

	public void setSub(boolean isSub) {
		this.isSub = isSub;
	}

	public void setPopularOff(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_POPULAR_OFF;
        } else {
            this.type &= (~Type.IS_POPULAR_OFF);
        }
        log.error(" current type:" + this.type);
    }

    public boolean isShowWindowOn() {
        return (this.type & Type.IS_SHOWWINDOW_ON) > 0;
    }

    public void setShowWindowOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_SHOWWINDOW_ON;
        } else {
            this.type &= (~Type.IS_SHOWWINDOW_ON);
        }
        log.error(" current type:" + this.type);
    }

    public void setFenxiaoOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_FENGXIAO;
        } else {
            this.type &= (~Type.IS_FENGXIAO);
        }
        log.error(" current type:" + this.type);
    }

    public boolean isMsgOn() {
        return isAutoCommentOn() || isSalesCountOn() || isBlackListAutoDefenseOn() || !isBadCommentNoticeOff()
                || isBadCommentBuyerSmsOn() || !isDefenseNoticeSmsOff();
    }

    public boolean isAutoCommentOn() {
        return (this.type & Type.IS_AUTOCOMMENT_ON) > 0;
    }

    public void setAutoCommentOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_AUTOCOMMENT_ON;
        } else {
            this.type &= (~Type.IS_AUTOCOMMENT_ON);
        }
    }

    public boolean isAutoDefenseOn() {
        return (this.type & Type.IS_AutoDefense_On) > 0;
    }

    public void setAutoDefenseOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_AutoDefense_On;
        } else {
            this.type &= (~Type.IS_AutoDefense_On);
        }
    }

    public boolean isBlackListAutoDefenseOn() {
        return (this.type & Type.IS_BlackListAutoDefense_On) > 0;
    }

    public void setBlackListAutoDefenseOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_BlackListAutoDefense_On;
        } else {
            this.type &= (~Type.IS_BlackListAutoDefense_On);
        }
    }

    public boolean isWhiteListAutoDefenseOn() {
        return (this.type & Type.IS_WhiteListAutoDefense_On) > 0;
    }

    public void setWhiteListAutoDefenseOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_WhiteListAutoDefense_On;
        } else {
            this.type &= (~Type.IS_WhiteListAutoDefense_On);
        }
    }

    public boolean isSendDefenseMsgOn() {
        return (this.type & Type.IS_SendDefenseMsg_On) > 0;
    }

    public void setSendDefenseMsgOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_SendDefenseMsg_On;
        } else {
            this.type &= (~Type.IS_SendDefenseMsg_On);
        }
    }

    public boolean isAutoChapingBlackListOn() {
        return (this.type & Type.IS_AutoChapingBlackListOn) > 0;
    }

    public void setAutoChapingBlackListOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_AutoChapingBlackListOn;
        } else {
            this.type &= (~Type.IS_AutoChapingBlackListOn);
        }
    }

    public boolean isAutoRefundBlackListOn() {
        return (this.type & Type.IS_AutoRefundBlackListOn) > 0;
    }

    public void setAutoRefundBlackListOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_AutoRefundBlackListOn;
        } else {
            this.type &= (~Type.IS_AutoRefundBlackListOn);
        }
    }

    public boolean isSalesCountOn() {
        return (this.type & Type.IS_salesCount_On) > 0;
    }

    public void setSalesCountOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_salesCount_On;
        } else {
            this.type &= (~Type.IS_salesCount_On);
        }
    }

    public boolean isAutoDelistOn() {
        return (this.type & Type.IS_DELIST_ON) > 0;
    }

    public void setAutoDelistOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_DELIST_ON;
        } else {
            this.type &= (~Type.IS_DELIST_ON);
        }
    }

    public boolean isBadCommentNoticeOff() {
        return (this.type & Type.IS_BadCommentNoticeOff) > 0;
    }

    public void setBadCommentNoticeOff(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_BadCommentNoticeOff;
        } else {
            this.type &= (~Type.IS_BadCommentNoticeOff);
        }
    }

    public boolean isBadCommentBuyerSmsOn() {
        return (this.type & Type.IS_BadCommentBuyerSms) > 0;
    }

    public void setBadCommentBuyerSmsOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_BadCommentBuyerSms;
        } else {
            this.type &= (~Type.IS_BadCommentBuyerSms);
        }
    }

    public boolean isDefenseNoticeSmsOff() {
        return (this.type & Type.IS_DefenseNoticeSmsOff) > 0;
    }

    public void setDefenseNoticeSmsOff(boolean toBeOff) {
        if (toBeOff) {
            this.type |= Type.IS_DefenseNoticeSmsOff;
        } else {
            this.type &= (~Type.IS_DefenseNoticeSmsOff);
        }
    }

    /**
     * 1 C; 2 B
     */
    public int type = 0;

    public User() {
    }

    public User(String userNick, Long userId, String sessionKey, boolean isVaild, int cid) {

        this.id = userId;
        this.userNick = userNick;
        this.sessionKey = sessionKey;
        // this.isVaild = isVaild;
        this.firstLoginTime = System.currentTimeMillis();

        this.cid = cid;

        if (isVaild) {
            this.type |= Type.IS_VALID;
        }

    }
    
	public User(String userNick, Long userId, String sessionKey, boolean isVaild) {
		this.id = userId;
		this.userNick = userNick;
		this.sessionKey = sessionKey;
		this.isVaild = isVaild;
	}

    public User(com.taobao.api.domain.User user, String sessionKey, int cid) {
        setByTBUser(user);
        this.sessionKey = sessionKey;
        this.firstLoginTime = System.currentTimeMillis();
        this.cid = cid;

    }

    public void setByTBUser(com.taobao.api.domain.User user) {
        this.id = user.getUserId();
        this.userNick = user.getNick();
        this.hasShop = user.getHasShop();
        this.level = (int) user.getSellerCredit().getLevel().longValue();
        log.error("Has Shop :" + user.getHasShop());

        if (hasShop) {
            this.type |= Type.HAS_SHOP;
        }

        if (user.getType().equals("B")) {
            log.warn("B shop:" + this.userNick + ", level : " + this.level);
            this.type |= Type.IS_TMALL;
        } else if (user.getType().equals("C")) {
            this.type &= ~Type.IS_TMALL;
        }

        if ("normal".equals(user.getStatus())) {
            this.isVaild = true;
            this.type |= Type.IS_VALID;
        } else {
            this.type &= (~Type.IS_VALID);
            this.isVaild = false;

        }

    }

    public boolean isVaild() {
        return ((this.type & Type.IS_VALID) > 0) && !StringUtils.isEmpty(sessionKey);
    }

    public Long getId() {
        return id;
    }

    @Transient
    @JsonIgnore
    private String idString = null;

    public String getIdString() {
        if (idString == null) {
            idString = String.valueOf(this.id);
        }
        return idString;
    }

    @Transient
    @JsonIgnore
    private long idlong;

    public long getIdlong() {
        if (idlong <= 0L) {
            idlong = id.longValue();
        }
        return idlong;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Long getFirstLoginTime() {
        return this.firstLoginTime;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getVersion() {

        if (this.getId() != null && this.getId().longValue() == 159328691L) {
            return 60;
        }

        return version;
    }

    public void setVersion(int version) {
    	log.error("setVersion version is " + version);
    	log.error(this.toString());
        if (this.getId().longValue() == 400887535L || this.getId().longValue() == 125453748L
                || this.getId().longValue() == 491116708L || this.getId().longValue() == 159328691L
                || this.getId().longValue() == 79742176L) {
            return;
        }

        this.version = version;
    }

    public void updateVersion(int ver) {
//        if (this.version >= Version.VIP) {
//            return;
//        }
//        if (this.version == Version.BLACK) {
//            return;
//        }
        if (this.getId().longValue() == 400887535L || this.getId().longValue() == 125453748L
                || this.getId().longValue() == 491116708L || this.getId().longValue() == 159328691L) {
            return;
        }

        this.version = ver;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", userNick=" + userNick + ", sessionKey=" + sessionKey + ", isVaild=" + isVaild
                + ", firstLoginTime=" + firstLoginTime + ", version=" + version + ", level=" + level + ", type=" + type
                + ", getId()= "+ getCid() + "]";
    }

    public String toIdNick() {
        return "User [id=" + id + ", userNick=" + userNick + "]";
    }

    public User update(com.taobao.api.domain.User taobaoUser) {
        if (taobaoUser == null) {
            setVaild(false);
            return this;
        }
        this.userNick = taobaoUser.getNick();
        this.level = (int) taobaoUser.getSellerCredit().getLevel().longValue();

        if (taobaoUser.getType().equals("B")) {
            log.warn("B shop:" + this.userNick + ", level : " + this.level);
            this.type |= Type.IS_TMALL;
        } else if (taobaoUser.getType().equals("C")) {
            // this.type = 1;
        }

        if (!"normal".equals(taobaoUser.getStatus())) {
            setVaild(false);
        } else {
            setVaild(true);
        }
        setHasShop(taobaoUser.getHasShop());

        return this;
    }

    public boolean updateIsVaild(boolean isVaild) {
        this.isVaild = isVaild;
        if (isVaild) {
            this.type |= Type.IS_VALID;
        } else {
            this.type &= (~Type.IS_VALID);
        }
        log.info("[current type :]" + type);
        this.jdbcSave();
        return this.isVaild;
    }

    public boolean hasValidShop() {
        // return this.hasShop && this.isVaild &&
        // !(StringUtils.isEmpty(this.sessionKey));
        return ((this.type & Type.HAS_SHOP) > 0) && isVaild();
    }

    @Override
    public void _save() {
        if (this.firstLoginTime == null || this.firstLoginTime <= 0L) {
            this.firstLoginTime = System.currentTimeMillis();
        }
        this.lastUpdateTime = System.currentTimeMillis();

        jdbcSave();
        // super._save();
    }

    @Override
    public int compareTo(User o) {
        return o.getLevel() - this.getLevel();

    }

    @Transient
    static String EXIST_ID_QUERY = "select id from " + User.TABLE_NAME + " where id = ? ";

    public static long findExistId(Long numIid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, numIid);
    }

    @Override
    public String getTableName() {
        return this.TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    // @Override
    // public void _save() {
    // this.jdbcSave();
    // }

    @Override
    public boolean jdbcSave() {

    	if(isSub()) {
    		log.info("user " + userNick + " is sub, do not insert!");
    		//return false;
    	}
        try {
            long existdId = findExistId(this.id);

            if (existdId <= 0L) {
                this.rawInsert();
            } else {
                this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

        UserDao.setUserCache(this);
        return true;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Transient
    static String FIND_BY_ID_QUERY = "select `id`,`userNick`,`sessionKey`,`firstLoginTime`,`version`,"
            + "`cid`,`level`,`type`,`lastUpdateTime` from " + TABLE_NAME + " where id = ? ";

    public static User findByUserId(Long userId) {
        return new JDBCExecutor<User>(dp, FIND_BY_ID_QUERY, userId) {
            @Override
            public User doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new User(rs);
                }
                return null;
            }

        }.call();
    }

    @Transient
    static String FIND_BY_USERNICK_QUERY = "select `id`,`userNick`,`sessionKey`,`firstLoginTime`,`version`,"
            + "`cid`,`level`,`type`,`lastUpdateTime` from " + TABLE_NAME + " where `userNick` = ? ";

    public static User findByUserNick(String userNick) {
        return new JDBCExecutor<User>(dp, FIND_BY_USERNICK_QUERY, userNick) {
            @Override
            public User doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new User(rs);
                }
                return null;
            }

        }.call();
    }

    @Transient
    static String FIND_BY_SessionKey_QUERY = "select `id`,`userNick`,`sessionKey`,`firstLoginTime`,`version`,"
            + "`cid`,`level`,`type`,`lastUpdateTime` from " + TABLE_NAME + " where sessionKey = ? ";

    public static User findBySessionKey(String sessionKey) {
        return new JDBCExecutor<User>(dp, FIND_BY_SessionKey_QUERY, sessionKey) {
            @Override
            public User doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new User(rs);
                }
                return null;
            }

        }.call();
    }

    @Transient
    static String insertSQL = "insert into `user`(`id`,`userNick`,`sessionKey`,`firstLoginTime`,`version`,"
            + "`cid`,`level`,`type`,`lastUpdateTime`,`refreshToken`) values(?,?,?,?,?,?,?,?,?,?)";

    boolean rawInsert() {
        long id = dp.insert(false, insertSQL, this.id, this.userNick, this.sessionKey, this.firstLoginTime,
                this.version, this.cid, this.level, this.type, this.lastUpdateTime, this.refreshToken);

        log.info("[Insert user Id:]" + id + "[userId : ]" + this.userNick);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userNick : ]" + this.userNick);
            return false;
        }

    }

    @Transient
    static String updateSQL = "update `user` set  `userNick` = ?, `sessionKey` = ?, `version` = ?,"
            +
            "`cid` = ?, `level` = ?, `type` = ?, `lastUpdateTime` = ?, `refreshToken` = ? where `id` = ? ";

    boolean rawUpdate() {

        long updateNum = dp.update(false, updateSQL, this.userNick, this.sessionKey, this.version, this.cid,
                this.level, this.type, this.lastUpdateTime, this.refreshToken, this.getId());

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId() + "[userNick : ]" + this.userNick);
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userNick : ]" + this.userNick);
            return false;
        }
    }

    // public boolean isVaild() {
    // return isVaild;
    // }

    public void setVaild(boolean isVaild) {
        this.isVaild = isVaild;
        if (isVaild) {
            this.type |= Type.IS_VALID;
        } else {
            this.type &= (~Type.IS_VALID);
        }
    }

    public boolean isHasShop() {
        return ((this.type & Type.HAS_SHOP) > 0);
    }

    public void setHasShop(boolean hasShop) {
        this.type |= Type.HAS_SHOP;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setFirstLoginTime(Long firstLoginTime) {
        this.firstLoginTime = firstLoginTime;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isTmall() {
        return (this.type & Type.IS_TMALL) == Type.IS_TMALL;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

//    static String FIND_BY_ID_QUERY = "select `id`,`userNick`,`sessionKey`,`firstLoginTime`,`version`,"
//            + "`cid`,`level`,`type`,`lastUpdateTime` from " + TABLE_NAME + " where id = ? ";
    public User(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.userNick = rs.getString(2);
        this.sessionKey = rs.getString(3);
        this.firstLoginTime = rs.getLong(4);
        this.version = rs.getInt(5);
        this.cid = rs.getInt(6);
        this.level = rs.getInt(7);
        this.type = rs.getInt(8);
        this.lastUpdateTime = rs.getLong(9);

    }

    String refreshToken = null;

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public boolean isFengxiao() {
        return (this.type & Type.IS_FENGXIAO) > 0;
    }

    public boolean isPopularAward() {
        return (this.type & Type.IS_POPULAR_AWARD) > 0;
    }

    public void setPopularAward(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_POPULAR_AWARD;
        } else {
            this.type &= (~Type.IS_POPULAR_AWARD);
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Transient
    Long ts;

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public boolean isBlackList() {
        if (this.userNick.indexOf("测试") >= 0) {
            return true;
        }
        return false;
    }

}
