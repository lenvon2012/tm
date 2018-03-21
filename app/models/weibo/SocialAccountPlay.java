package models.weibo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.user.User;
import models.user.User.Type;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

@Entity(name = SocialAccountPlay.TABLE_NAME)
public class SocialAccountPlay extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(SocialAccountPlay.class);
    
    @Transient
    public static final String TABLE_NAME = "social_account_play_";

    @Transient
    public static SocialAccountPlay EMPTY = new SocialAccountPlay();

    @Transient
    private static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Index(name = "userId")
    private Long userId;
    
    private String accountName;
    
    private String aliasName;
    
    private String token;
    
    @Index(name = "accountId")
    private String accountId;
    
    @Column(columnDefinition = "int default 0 ")
    private int accountType;
    
    public static class SocialAccountType {
        public static final int XinLangWeibo = 1;//新浪微博
    }
    
    @Column(columnDefinition = "int default 0 ")
    private int status;
    
    public static class SocialAccountStatus {
        public static final int Binding = 1;//已绑定
        public static final int UnBinding = 2;//未绑定
        
    }
    
    @Column(columnDefinition = "int default 0 ")
    private int function;
    
    public static class SocialAccountFunction {
        public static final int MainAccount = 1;//
        public static final int SlaveAccount = 2;//

        public static final int All = 4;
    }
    
    
    private String headImgUrl;
    
    private String userMainPage;//主页
    
    //粉丝数
    @Column(columnDefinition = "int default 0 ")
    private int fansNum;
    
    //积分
    private double contribution;
    
    //新增粉丝数
    @Column(columnDefinition = "int default 0 ")
    private int addFansNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int attentionNum;//关注数
    
    @Column(columnDefinition = "int default 0 ")
    private int newAttentionNum;//新增关注
    
    @Column(columnDefinition = "int default 0 ")
    private int newForwardNum;//新增转发
    
    private long checkAuthTs;//检查授权的ts
    
    private long expireSecond;//在checkAuthTs那一时刻的过期的秒数
    
    private long contributeTs;//最近获取积分的ts，用来排序
    
    private long createTs;
    
    private long updateTs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFunction() {
        return function;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public String getUserMainPage() {
        return userMainPage;
    }

    public void setUserMainPage(String userMainPage) {
        this.userMainPage = userMainPage;
    }

    public int getFansNum() {
        return fansNum;
    }

    public void setFansNum(int fansNum) {
        this.fansNum = fansNum;
    }

    public double getContribution() {
        return contribution;
    }

    public void setContribution(double contribution) {
        this.contribution = contribution;
    }

    public int getAddFansNum() {
        return addFansNum;
    }

    public void setAddFansNum(int addFansNum) {
        this.addFansNum = addFansNum;
    }

    public int getAttentionNum() {
        return attentionNum;
    }

    public void setAttentionNum(int attentionNum) {
        this.attentionNum = attentionNum;
    }
    
    public int getNewAttentionNum() {
        return newAttentionNum;
    }

    public void setNewAttentionNum(int newAttentionNum) {
        this.newAttentionNum = newAttentionNum;
    }

    public long getCheckAuthTs() {
        return checkAuthTs;
    }

    public void setCheckAuthTs(long checkAuthTs) {
        this.checkAuthTs = checkAuthTs;
    }

    public long getExpireSecond() {
        return expireSecond;
    }

    public void setExpireSecond(long expireSecond) {
        this.expireSecond = expireSecond;
    }

    public int getNewForwardNum() {
        return newForwardNum;
    }

    public void setNewForwardNum(int newForwardNum) {
        this.newForwardNum = newForwardNum;
    }

    public long getContributeTs() {
        return contributeTs;
    }

    public void setContributeTs(long contributeTs) {
        this.contributeTs = contributeTs;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }
    
    public boolean isMainAccount() {
        return function == SocialAccountFunction.MainAccount;
    }
    
    public boolean isBinding() {
        return status == SocialAccountStatus.Binding;
    }
    
    public boolean isOutOfDate() {
        long second = getNowExpireSecond();
        
        if (second <= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isSinaAccount() {
        return accountType == SocialAccountType.XinLangWeibo;
    }
    
    public long getNowExpireSecond() {
        long second = expireSecond - (System.currentTimeMillis() - checkAuthTs) / 1000;
        
        
        return second;
    }
    
    public String getExpireTimeStr() {
        
        long second = getNowExpireSecond();
        
        if (second <= 0) {
            return "已过期";
        }
        long dayNum = second * 1000 / DateUtil.DAY_MILLIS;
        if (dayNum > 10) {
            return "许多天后过期";
        }
        if (dayNum > 0) {
            return dayNum + "天后过期";
        }
        long hourNum = second * 1000 / DateUtil.ONE_HOUR;
        if (hourNum > 0) {
            return hourNum + "小时后过期";
        }
        long minuteNum = second * 1000 / DateUtil.ONE_MINUTE_MILLIS;
        if (minuteNum > 0) {
            return minuteNum + "分后过期";
        }
        
        return second + "秒后过期";
    }

    public SocialAccountPlay() {
        super();
    }

    public SocialAccountPlay(Long userId, String accountName, String aliasName,
            String token, String accountId, int accountType, int status, int function,
            long checkAuthTs, long expireSecond) {
        super();
        this.userId = userId;
        this.accountName = accountName;
        this.aliasName = aliasName;
        this.token = token;
        this.accountId = accountId;
        this.accountType = accountType;
        this.status = status;
        this.function = function;
        this.checkAuthTs = checkAuthTs;
        this.expireSecond = expireSecond;
    }
    
    public void updateAccount(String accountName, String aliasName,
            String token, String accountId, int status,
            long checkAuthTs, long expireSecond) {
        this.accountName = accountName;
        this.aliasName = aliasName;
        this.token = token;
        this.accountId = accountId;
        this.status = status;
        this.checkAuthTs = checkAuthTs;
        this.expireSecond = expireSecond;
    }
    
    public void updateSinaAccountBasic(weibo4j.model.User weiboUser) {
        this.headImgUrl = weiboUser.getProfileImageUrl();
        //this.userMainPage = weiboUser.getUserDomain();
        this.fansNum = weiboUser.getFollowersCount();
        this.attentionNum = weiboUser.getFriendsCount();
    }
    
    /*
    public void updateAccountBasic(String headImgUrl, String userMainPage, int fansNum, int attentionNum) {
        this.headImgUrl = headImgUrl;
        this.userMainPage = userMainPage;
        this.fansNum = fansNum;
        this.attentionNum = attentionNum;
    }
    */

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
    public String getIdName() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    
    
    public static long findExistId(Long userId, int accountType, int function) {
        
        String query = "select id from " + TABLE_NAME + " where userId = ? and accountType = ? " +
        		" and function = ? ";
        
        return dp.singleLongQuery(query, userId, accountType, function);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.accountType, this.function);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {

        String insertSQL = "insert into `" + TABLE_NAME + "`" +
                "(`userId`,`accountName`,`aliasName`,`token`,`accountId`," +
                "`accountType`,`status`,`function`,`headImgUrl`,`userMainPage`," +
                "`fansNum`,`contribution`,`addFansNum`,`attentionNum`,`newAttentionNum`," +
                "`newForwardNum`,`checkAuthTs`,`expireSecond`," +
                "`contributeTs`,`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.userId, this.accountName, this.aliasName, this.token, this.accountId,
                this.accountType, this.status, this.function, this.headImgUrl, this.userMainPage,
                this.fansNum, this.contribution, this.addFansNum, this.attentionNum, this.newAttentionNum,
                this.newForwardNum, this.checkAuthTs, this.expireSecond,
                this.contributeTs, this.createTs, this.updateTs);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }
    
    public boolean rawUpdate() {
        
        String updateSQL = "update `" + TABLE_NAME + "` set  " +
                " `accountName` = ?, `aliasName` = ?, `token` = ?, `accountId` = ?, `status` = ?, " +
                " `headImgUrl` = ?, `userMainPage` = ?, " +
                " `fansNum` = ?, `contribution` = ?, `addFansNum` = ?, `attentionNum` = ?, `newAttentionNum` = ?, " +
                " `newForwardNum` = ?, `checkAuthTs` = ?, `expireSecond` = ?, " +
                " `contributeTs` = ?, `updateTs` = ? " +
                " where userId = ? and accountType = ? and function = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.accountName, this.aliasName, this.token, this.accountId, this.status,
                this.headImgUrl, this.userMainPage,
                this.fansNum, this.contribution, this.addFansNum, this.attentionNum, this.newAttentionNum,
                this.newForwardNum, this.checkAuthTs, this.expireSecond,
                this.contributeTs, this.updateTs,
                this.userId, this.accountType, this.function);

        if (updateNum >= 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    public boolean rawDelete() {
        
        String sql = " delete from " + TABLE_NAME + " where userId = ? and accountType = ? and function = ? ";
        
        long deleteNum = dp.update(sql, userId, accountType, function);
        
        return true;
    }
    

    public static List<SocialAccountPlay> findByAccountType(Long userId, int accountType) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? " +
                " and accountType = ? ";
        
        return new JDBCBuilder.JDBCExecutor<List<SocialAccountPlay>>(dp, query, userId, accountType) {

            @Override
            public List<SocialAccountPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<SocialAccountPlay> accountList = new ArrayList<SocialAccountPlay>();
                
                while (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    if (account != null) {
                        accountList.add(account);
                    }
                } 
                
                return accountList;
            }
            
        }.call();
        
    }
    
    public static List<SocialAccountPlay> findByUserId(Long userId) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? ";
        
        return new JDBCBuilder.JDBCExecutor<List<SocialAccountPlay>>(dp, query, userId) {

            @Override
            public List<SocialAccountPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<SocialAccountPlay> accountList = new ArrayList<SocialAccountPlay>();
                
                while (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    if (account != null) {
                        accountList.add(account);
                    }
                } 
                
                return accountList;
            }
            
        }.call();
        
    }
    
    
    //如果id是long的，那么就不要加引号
    public static String joinForInSqlWithEscape(Set<String> idSet) {
        
        if (CommonUtils.isEmpty(idSet)) {
            return "";
        }
        
        List<String> sqlList = new ArrayList<String>();
        for (String idStr : idSet) {
            boolean isLong = false;
            try {
                Long.parseLong(idStr);
                isLong = true;
            } catch (Exception ex) {
                isLong = false;
            }
            
            if (isLong == true) {
                sqlList.add(idStr);
            } else {
                sqlList.add("'" + idStr + "'");
            }
        }
        
        String inSql = StringUtils.join(sqlList, ",");
        inSql = CommonUtils.escapeSQL(inSql);
        
        return inSql;
    }
    
    public static List<SocialAccountPlay> findBindMainAccountIds(Set<String> accountIdSet, int accountType) {
        
        if (CommonUtils.isEmpty(accountIdSet)) {
            return new ArrayList<SocialAccountPlay>();
        }
        
        
        String accountIds = joinForInSqlWithEscape(accountIdSet);
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where accountType = ? " +
        		" and status = ? and function = ? and accountId in (" + accountIds + ") ";
        
        return new JDBCBuilder.JDBCExecutor<List<SocialAccountPlay>>(dp, query, accountType,
                SocialAccountStatus.Binding, SocialAccountFunction.MainAccount) {

            @Override
            public List<SocialAccountPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<SocialAccountPlay> accountList = new ArrayList<SocialAccountPlay>();
                
                while (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    if (account != null) {
                        accountList.add(account);
                    }
                } 
                
                return accountList;
            }
            
        }.call();
        
    }
    
    
    public static SocialAccountPlay findByFunction(Long userId, int accountType, int function) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? " +
                " and accountType = ? and function = ? ";
        
        return new JDBCBuilder.JDBCExecutor<SocialAccountPlay>(dp, query, userId, accountType, function) {

            @Override
            public SocialAccountPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                if (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    return account;
                } else {
                    return null;
                }
            }
            
        }.call();
        
    }
    
    public static SocialAccountPlay findByAccountId(Long userId, Long accountId) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? " +
                " and id = ? ";
        
        return new JDBCBuilder.JDBCExecutor<SocialAccountPlay>(dp, query, userId, accountId) {

            @Override
            public SocialAccountPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                if (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    return account;
                } else {
                    return null;
                }
            }
            
        }.call();
        
    }
    
    
    public static List<SocialAccountPlay> findBindMainWithOffset(Long userId, int accountType, 
            String slaveAccountId, long offset, int ps) {
        
        List<Object> paramList = new ArrayList<Object>();
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + 
                " where userId != ? and accountType = ? and status = ? and function = ? " +
                " and userId in (select id from " + User.TABLE_NAME + " as u where u.type & " + 
                Type.IS_VALID + " > 0) ";
        
        paramList.add(userId);
        paramList.add(accountType);
        paramList.add(SocialAccountStatus.Binding);
        paramList.add(SocialAccountFunction.MainAccount);
        
        if (StringUtils.isEmpty(slaveAccountId) == false) {
            query += " and accountId != ? and accountId not in " +
                    " (select friendId from " + AccountFriendRecord.TABLE_NAME + 
                    " where myAccountId = ? and accountType = ?) ";
            paramList.add(slaveAccountId);
            paramList.add(slaveAccountId);
            paramList.add(accountType);
        }
        query += " limit ?, ?";
        
        paramList.add(offset);
        paramList.add(ps);
        
        log.info(query);
        
        Object[] paramArray = paramList.toArray();
        
        return new JDBCBuilder.JDBCExecutor<List<SocialAccountPlay>>(dp, query, paramArray) {

            @Override
            public List<SocialAccountPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<SocialAccountPlay> accountList = new ArrayList<SocialAccountPlay>();
                
                while (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    if (account != null) {
                        accountList.add(account);
                    }
                } 
                
                return accountList;
            }
            
        }.call(); 
        
    }
    
    
    public static List<SocialAccountPlay> findMostContribute(Long userId, int accountType, 
            String slaveAccountId, long offset, int ps) {
        
        List<Object> paramList = new ArrayList<Object>();
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + 
                " where userId != ? and accountType = ? and status = ? and function = ? " +
                " and userId in (select id from " + User.TABLE_NAME + " as u where u.type & " + 
                Type.IS_VALID + " > 0) and contribution >= 1 ";
        
        paramList.add(userId);
        paramList.add(accountType);
        paramList.add(SocialAccountStatus.Binding);
        paramList.add(SocialAccountFunction.MainAccount);
        
        if (StringUtils.isEmpty(slaveAccountId) == false) {
            query += " and accountId != ? and accountId not in " +
            		" (select friendId from " + AccountFriendRecord.TABLE_NAME + 
            		" where myAccountId = ? and accountType = ?) ";
            paramList.add(slaveAccountId);
            paramList.add(slaveAccountId);
            paramList.add(accountType);
        }
        query += " order by contribution desc, contributeTs asc limit ?, ?";
        
        paramList.add(offset);
        paramList.add(ps);
        
        log.info(query);
        
        Object[] paramArray = paramList.toArray();
        
        return new JDBCBuilder.JDBCExecutor<List<SocialAccountPlay>>(dp, query, paramArray) {

            @Override
            public List<SocialAccountPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                List<SocialAccountPlay> accountList = new ArrayList<SocialAccountPlay>();
                
                while (rs.next()) {
                    SocialAccountPlay account = parseSocialAccountPlay(rs);
                    if (account != null) {
                        accountList.add(account);
                    }
                } 
                
                return accountList;
            }
            
        }.call(); 
        
    }
    
    
    private static final String SelectAllProperty = " id,userId,accountName,aliasName,token,accountId," +
    		    "accountType,status,function,headImgUrl,userMainPage," +
                "fansNum,contribution,addFansNum,attentionNum,newAttentionNum," +
                "newForwardNum,checkAuthTs,expireSecond," +
                "contributeTs,createTs,updateTs ";
        
    private static SocialAccountPlay parseSocialAccountPlay(ResultSet rs) {
        
        try {
            SocialAccountPlay account = new SocialAccountPlay();
            
            account.setId(rs.getLong(1));
            account.setUserId(rs.getLong(2));
            account.setAccountName(rs.getString(3));
            account.setAliasName(rs.getString(4));
            account.setToken(rs.getString(5));
            account.setAccountId(rs.getString(6));
            account.setAccountType(rs.getInt(7));
            account.setStatus(rs.getInt(8));
            account.setFunction(rs.getInt(9));
            account.setHeadImgUrl(rs.getString(10));
            account.setUserMainPage(rs.getString(11));
            account.setFansNum(rs.getInt(12));
            account.setContribution(rs.getDouble(13));
            account.setAddFansNum(rs.getInt(14));
            account.setAttentionNum(rs.getInt(15));
            account.setNewAttentionNum(rs.getInt(16));
            account.setNewForwardNum(rs.getInt(17));
            account.setCheckAuthTs(rs.getLong(18));
            account.setExpireSecond(rs.getLong(19));
            account.setContributeTs(rs.getLong(20));
            account.setCreateTs(rs.getLong(21));
            account.setUpdateTs(rs.getLong(22));
            
            return account;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        
    }

    @Override
    public String toString() {
        return "SocialAccountPlay [userId=" + userId + ", accountName="
                + accountName + ", aliasName=" + aliasName + ", token=" + token
                + ", accountId=" + accountId + ", accountType=" + accountType
                + ", status=" + status + ", function=" + function
                + ", headImgUrl=" + headImgUrl + ", userMainPage="
                + userMainPage + ", fansNum=" + fansNum + ", contribution="
                + contribution + ", addFansNum=" + addFansNum
                + ", attentionNum=" + attentionNum + ", newAttentionNum="
                + newAttentionNum + ", newForwardNum=" + newForwardNum
                + ", checkAuthTs=" + checkAuthTs + ", expireSecond="
                + expireSecond + ", contributeTs=" + contributeTs
                + ", createTs=" + createTs + ", updateTs=" + updateTs + "]";
    }

    
    
    
    
    
}
