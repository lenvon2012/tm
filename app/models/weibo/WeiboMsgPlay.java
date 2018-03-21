package models.weibo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import weibo4j.model.Status;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;

@Entity(name = WeiboMsgPlay.TABLE_NAME)
public class WeiboMsgPlay extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(WeiboMsgPlay.class);
    
    private static final SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat monthSdf = new SimpleDateFormat("MM-dd HH:mm");
    private static final SimpleDateFormat hourSdf = new SimpleDateFormat("HH:mm");
    
    @Transient
    public static final String TABLE_NAME = "weibo_msg_play";

    @Transient
    public static WeiboMsgPlay EMPTY = new WeiboMsgPlay();

    @Transient
    private static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "userId")
    private Long userId;
    
    @Column(columnDefinition = "int default 0 ")
    private int accountType;
    
    
    @Index(name = "accountId")
    private String accountId;
    
    @Index(name = "weiboId")
    private String weiboId;

    private String accountName;
    
    @Column(columnDefinition = "varchar(500) default '' ")
    private String content;
    
    private String smallPicUrl;
    
    @Index(name = "publishTs")
    private long publishTs;
    
    @Column(columnDefinition = "int default 0 ")
    private int source;
    
    public static class WeiboMsgSource {
        
        public static final int FromMainAccount = 1;//微博大号
        
        public static final int FromFakeAccount = 2;//僵尸帐号
    }
    
    private long createTs;
    
    private long updateTs;
    
    
    @Transient
    private String headImgUrl;
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getWeiboId() {
        return weiboId;
    }

    public void setWeiboId(String weiboId) {
        this.weiboId = weiboId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSmallPicUrl() {
        return smallPicUrl;
    }

    public void setSmallPicUrl(String smallPicUrl) {
        this.smallPicUrl = smallPicUrl;
    }

    public long getPublishTs() {
        return publishTs;
    }

    public void setPublishTs(long publishTs) {
        this.publishTs = publishTs;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
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
    
    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }
    
    private static String formatContentHref(String content) {
        if (StringUtils.isEmpty(content)) {
            content = "";
        }
        
        int startIndex = 0;
        
        Set<String> linkSet = new HashSet<String>();
        
        while (true) {
            startIndex = content.indexOf("http://", startIndex);
            if (startIndex < 0) {
                break;
            }
            startIndex += "http://".length();
            String link = "http://";
            for (int i = startIndex; i < content.length(); i++) {
                char contentChar = content.charAt(i);
                if (contentChar == '.' || contentChar == '/' 
                        || contentChar == '?' || contentChar == '&' || contentChar == '=') {
                    link += contentChar;
                    continue;
                }
                if (contentChar >= 'a' && contentChar <= 'z') {
                    link += contentChar;
                    continue;
                }
                if (contentChar >= 'A' && contentChar <= 'Z') {
                    link += contentChar;
                    continue;
                }
                if (contentChar >= '0' && contentChar <= '9') {
                    link += contentChar;
                    continue;
                }
                break;
            }
            linkSet.add(link);
        }
        
        for (String link : linkSet) {
            content = content.replace(link, "<a href=\"" + link + "\" class=\"weibo-content-link\" target=\"_blank\">" + link + "</a>");
        }
        
        return content;
    }
    
    public String getContentHtml() {
        
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(formatContentHref(content));
        
        if (StringUtils.isEmpty(smallPicUrl) == false) {
            sb.append("<div style=\"padding-top: 10px;\">");
            sb.append("<img src=\"" + smallPicUrl + "\" />");
            sb.append("</div>");
        }
        
        return sb.toString();
    }

    
    public String getPublishTsStr() {
        
        //今天，昨天，前天，今年
        Calendar now = Calendar.getInstance();
        Calendar publish = Calendar.getInstance();
        publish.setTimeInMillis(publishTs);
        
        if (now.get(Calendar.YEAR) != publish.get(Calendar.YEAR)) {
            return yearSdf.format(new Date(publishTs));
        } else if (now.get(Calendar.MONTH) != publish.get(Calendar.MONTH)) {
            return monthSdf.format(new Date(publishTs));
        } else {
            int nowDay = now.get(Calendar.DAY_OF_MONTH);
            int publishDay = publish.get(Calendar.DAY_OF_MONTH);
            if (nowDay == publishDay) {
                return "今天 " + hourSdf.format(new Date(publishTs));
            } else if (nowDay - publishDay == 1) {
                return "昨天 " + hourSdf.format(new Date(publishTs));
            } else if (nowDay - publishDay == 2) {
                return "前天 " + hourSdf.format(new Date(publishTs));
            } else {
                return monthSdf.format(new Date(publishTs));
            }
        }
        
    }
    
    
    public WeiboMsgPlay() {
        super();
    }

    public WeiboMsgPlay(Long userId, int accountType, String accountId,
            String accountName, Status status, int source) {
        super();
        this.userId = userId;
        this.accountType = accountType;
        this.accountId = accountId;
        this.accountName = accountName;
        this.content = status.getText();
        this.publishTs = status.getCreatedAt() == null ? 0 : status.getCreatedAt().getTime();
        this.smallPicUrl = status.getThumbnailPic();
        this.weiboId = status.getId();
        this.source = source;
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
    public String getIdName() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static long findExistId(Long userId, String weiboId, int accountType) {
        
        String query = "select id from " + TABLE_NAME + " where userId = ? and weiboId = ? " +
                " and accountType = ? ";
        
        return dp.singleLongQuery(query, userId, weiboId, accountType);
    }

    public static boolean isExistByWeiboId(Long userId, String weiboId, int accountType) {
        long existId = findExistId(userId, weiboId, accountType);
        if (existId <= 0L) {
            return false;
        } else {
            return true;
        }
    }
    
    
    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.weiboId, this.accountType);

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
                "(`userId`,`accountType`,`accountId`,`weiboId`,`accountName`," +
                "`content`,`smallPicUrl`,`publishTs`,`source`," +
                "`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.userId, this.accountType, this.accountId, this.weiboId, this.accountName,
                this.content, this.smallPicUrl, this.publishTs, this.source,
                this.createTs, this.updateTs);

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
                " `accountId` = ?, `accountName` = ?, `content` = ?, `smallPicUrl` = ?, " +
                " `publishTs` = ?, `source` = ?, " +
                " `updateTs` = ? " +
                " where userId = ? and weiboId = ? and accountType = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.accountId, this.accountName, this.content, this.smallPicUrl, 
                this.publishTs, this.source,
                this.updateTs,
                this.userId, this.weiboId, this.accountType);

        if (updateNum >= 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    public boolean rawDelete() {
        String sql = "delete from " + TABLE_NAME + " where id = ?";
        
        long deleteNum = dp.update(sql, this.id);
        
        return true;
    }
    
    /*
    public static WeiboMsgPlay findByWeiboId(Long userId, String weiboId, int accountType) {
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? " +
                " and weiboId = ? and accountType = ? ";
        
        return new JDBCBuilder.JDBCExecutor<WeiboMsgPlay>(dp, query, userId, weiboId, accountType) {

            @Override
            public WeiboMsgPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                if (rs.next()) {
                    WeiboMsgPlay weibo = parseWeiboMsgPlay(rs);
                    return weibo;
                }
                
                return null;
            }
            
            
        }.call();
    }
    */
    
    public static List<WeiboMsgPlay> findByAccount(Long userId, String accountId, int accountType) {
        
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? " +
        		" and accountId = ? and accountType = ? ";
        
        return new JDBCBuilder.JDBCExecutor<List<WeiboMsgPlay>>(dp, query, userId, accountId, accountType) {

            @Override
            public List<WeiboMsgPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<WeiboMsgPlay> weiboList = new ArrayList<WeiboMsgPlay>();
                while (rs.next()) {
                    WeiboMsgPlay weibo = parseWeiboMsgPlay(rs);
                    if (weibo != null) {
                        weiboList.add(weibo);
                    }
                }
                
                return weiboList;
            }
            
            
        }.call();
        
    }
    
    
    //删除那些用户过期的微博，或没有绑定主帐号
    public static long deleteByUserIdAndAccountType(Long userId, int accountType) {
        
        String sql = " delete from " + TABLE_NAME + " where userId = ? and accountType = ? ";
        
        long deleteNum = dp.update(sql, userId, accountType);
        
        return deleteNum;
    }
    
    //删除那些不是主帐号发的微博
    public static long deleteNotAccountWeibo(Long userId, String accountId, int accountType) {
        String sql = " delete from " + TABLE_NAME + " where userId = ? and accountId != ? and accountType = ? ";
        long deleteNum = dp.update(sql, userId, accountId, accountType);
        
        return deleteNum;
    }
    
    //删除那些很早以前发的微博
    public static long deleteUserOldWeibo(Long userId, int accountType, int remainWeiboNum) {
        
        
        String query = "select id from " + TABLE_NAME + " where userId = ? and accountType = ? " +
                " order by publishTs desc limit ?,? ";
        
        Set<Long> idSet = new JDBCBuilder.JDBCLongSetExecutor(dp, query, userId, accountType, 
                0, remainWeiboNum).call();
        
        if (CommonUtils.isEmpty(idSet)) {
            return 0;
        }
        
        String inSql = StringUtils.join(idSet, ",");
        inSql = CommonUtils.escapeSQL(inSql);
        
        
        StringBuilder sb = new StringBuilder();
        sb.append(" delete from " + TABLE_NAME + " where userId = ? and accountType = ? and id not in (");
        sb.append(inSql);
        sb.append(") ");
        String sql = sb.toString();
        
        long deleteNum = dp.update(sql, userId, accountType);
        
        return deleteNum;
    }
    
    
    public static long deleteNoUserWeibos(int accountType, int limit) {
        
        String sql = " delete from " + TABLE_NAME + " where accountType = ? and source = ? " +
                " and userId not in (select id from " + User.TABLE_NAME + " where 1 = 1) limit ?";
        
        long deleteNum = dp.update(sql, accountType, WeiboMsgSource.FromMainAccount, limit);
        
        return deleteNum;
    }
    
    
    public static long deleteFakeOldWeibos(int accountType, long offset, int limit) {
        
        String query = "select id from " + TABLE_NAME + " where accountType = ? and source = ? " +
                " order by publishTs desc limit ?, ? ";
        
        Set<Long> idSet = new JDBCBuilder.JDBCLongSetExecutor(dp, query, accountType, 
                WeiboMsgSource.FromFakeAccount, offset, limit).call();
        
        if (CommonUtils.isEmpty(idSet)) {
            return 0;
        }
        
        String inSql = StringUtils.join(idSet, ",");
        inSql = CommonUtils.escapeSQL(inSql);
        
        
        StringBuilder sb = new StringBuilder();
        sb.append(" delete from " + TABLE_NAME + " where id in (");
        sb.append(inSql);
        sb.append(") ");
        String sql = sb.toString();
        
        long deleteNum = dp.update(sql);
        
        return deleteNum;
        
        
    }
    
    public static long countBySource(int accountType, int source) {
        String query = " select count(*) from " + TABLE_NAME + " where accountType = ? and source = ?";
        
        long count = dp.singleLongQuery(query, accountType, source);
        
        return count;
    }
    
    public static long countByUserIdAndAccountType(Long userId, int accountType) {
        String query = " select count(*) from " + TABLE_NAME + " where userId = ? and accountType = ? ";
        
        long count = dp.singleLongQuery(query, userId, accountType);
        
        return count;
    }
    
    
    public static List<WeiboMsgPlay> findUnForwardByAccountIds(Set<Long> userIdSet, Set<String> accountIdSet, 
            int accountType, String slaveAccountId) {
        
        if (CommonUtils.isEmpty(userIdSet) || CommonUtils.isEmpty(accountIdSet)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        List<Object> paramList = new ArrayList<Object>();
        
        String userIds = StringUtils.join(userIdSet, ",");
        userIds = CommonUtils.escapeSQL(userIds);
        String accountIds = SocialAccountPlay.joinForInSqlWithEscape(accountIdSet);
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " as w where w.accountType = ? " +
        		" and w.userId in (";
        
        paramList.add(accountType);
        
        StringBuilder sb = new StringBuilder();
        sb.append(query);
        sb.append(userIds);
        sb.append(") and w.accountId in (");
        sb.append(accountIds);
        sb.append(") ");
        
        if (StringUtils.isEmpty(slaveAccountId) == false) {
            sb.append(" and w.weiboId not in (select weiboId from " + ForwardMsgRecord.TABLE_NAME + " as f ");
            sb.append(" where f.myAccountId = ? and f.accountType = ?) ");
            
            paramList.add(slaveAccountId);
            paramList.add(accountType);
            
        }
        sb.append(" order by publishTs desc ");
        
        
        Object[] paramArray = paramList.toArray();
        
        query = sb.toString();
        
        return new JDBCBuilder.JDBCExecutor<List<WeiboMsgPlay>>(dp, query, paramArray) {

            @Override
            public List<WeiboMsgPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<WeiboMsgPlay> weiboList = new ArrayList<WeiboMsgPlay>();
                while (rs.next()) {
                    WeiboMsgPlay weibo = parseWeiboMsgPlay(rs);
                    if (weibo != null) {
                        weiboList.add(weibo);
                    }
                }
                
                return weiboList;
            }
            
            
        }.call();
        
    }
    
    
    
    public static List<WeiboMsgPlay> findByAccountType(int accountType, long offset, int ps) {
        
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + " where accountType = ? " +
        		" order by publishTs desc limit ?,? ";
        
        return new JDBCBuilder.JDBCExecutor<List<WeiboMsgPlay>>(dp, query, accountType, offset, ps) {

            @Override
            public List<WeiboMsgPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<WeiboMsgPlay> weiboList = new ArrayList<WeiboMsgPlay>();
                while (rs.next()) {
                    WeiboMsgPlay weibo = parseWeiboMsgPlay(rs);
                    if (weibo != null) {
                        weiboList.add(weibo);
                    }
                }
                
                return weiboList;
            }
            
            
        }.call(); 
        
    }
    

    private static final String SelectAllProperty = " id,userId,accountType,accountId,weiboId,accountName," +
                "content,smallPicUrl,publishTs,source," +
                "createTs,updateTs ";
    
    private static WeiboMsgPlay parseWeiboMsgPlay(ResultSet rs) {
        try {
            WeiboMsgPlay weibo = new WeiboMsgPlay();
            
            weibo.setId(rs.getLong(1));
            weibo.setUserId(rs.getLong(2));
            weibo.setAccountType(rs.getInt(3));
            weibo.setAccountId(rs.getString(4));
            weibo.setWeiboId(rs.getString(5));
            weibo.setAccountName(rs.getString(6));
            weibo.setContent(rs.getString(7));
            weibo.setSmallPicUrl(rs.getString(8));
            weibo.setPublishTs(rs.getLong(9));
            weibo.setSource(rs.getInt(10));
            weibo.setCreateTs(rs.getLong(11));
            weibo.setUpdateTs(rs.getLong(12));
            
            return weibo;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
}
