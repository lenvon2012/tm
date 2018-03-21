package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * 
 * @author lzl
 * 用户弹窗标志
 *
 */
@Entity(name = UserOP.TABLE_NAME)
public class UserOP extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserOP.class);

    @Transient
    public static final String TABLE_NAME = "user_op";

    @Transient
    public static UserOP EMPTY = new UserOP();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    public Long userId;

    public boolean isshowed;
    
    public int showed = 0;
    
    /**
	 * 好评助手备注是否同步到淘宝
	 */
    public boolean updateRemarkToTB;
    
    public static class Type {
        public static final int OLD_LINK = 1;

        public static final int FIVE_YUAN_XUFEI = 2;
        
        public static final int FIVE_XING_HAO_PING = 4;
        
        public static final int ONE_YUAN = 8;
        
        public static final int THREE_YUAN_XUFEI = 16;
        
        public static final int OLD_THREE_YUAN_XUFEI = 32;
        
        public static final int PEIXUN = 64;
        
        public static final int FREE_ONE_MONTH = 128;
        
        public static final int DAZHE = 256;
    }

    public UserOP() {

    }
    
    public UserOP(Long userId, boolean isshowed) {
        this.userId = userId;
        this.isshowed = isshowed;
    }
    
    public UserOP(Long userId, boolean isshowed, int showed) {
        this.userId = userId;
        this.isshowed = isshowed;
        this.showed = showed;
    }
    
	public UserOP(Long userId, boolean isshowed, int showed, boolean updateRemarkToTB) {
		this.userId = userId;
		this.isshowed = isshowed;
		this.showed = showed;
		this.updateRemarkToTB = updateRemarkToTB;
	}
    
    public boolean isFreeOneMonthShowed(){
        return (this.showed & Type.FREE_ONE_MONTH) > 0;
    }

    public void setFreeOneMonthShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.FREE_ONE_MONTH;
        } else {
            this.showed &= (~Type.FREE_ONE_MONTH);
        }
    }
    
    public boolean is5yuanShowed(){
        return (this.showed & Type.FIVE_YUAN_XUFEI) > 0;
    }

    public void set5YuanXufeiShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.FIVE_YUAN_XUFEI;
        } else {
            this.showed &= (~Type.FIVE_YUAN_XUFEI);
        }
    }
    
    public boolean is3yuanShowed(){
        return (this.showed & Type.THREE_YUAN_XUFEI) > 0;
    }

    public void set3YuanXufeiShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.THREE_YUAN_XUFEI;
        } else {
            this.showed &= (~Type.THREE_YUAN_XUFEI);
        }
    }
    
    public boolean isPeixunShowed(){
        return (this.showed & Type.PEIXUN) > 0;
    }

    public void setPeixunShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.PEIXUN;
        } else {
            this.showed &= (~Type.PEIXUN);
        }
    }
    
    public boolean isDazheShowed(){
        return (this.showed & Type.DAZHE) > 0;
    }

    public void setDazheShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.DAZHE;
        } else {
            this.showed &= (~Type.DAZHE);
        }
    }
    
    public boolean isold3yuanShowed(){
        return (this.showed & Type.OLD_THREE_YUAN_XUFEI) > 0;
    }

    public void setold3YuanXufeiShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.OLD_THREE_YUAN_XUFEI;
        } else {
            this.showed &= (~Type.OLD_THREE_YUAN_XUFEI);
        }
    }
    
    public boolean isHaoPingShowed(){
        return (this.showed & Type.FIVE_XING_HAO_PING) > 0;
    }

    public void setHaoPingShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.FIVE_XING_HAO_PING;
        } else {
            this.showed &= (~Type.FIVE_XING_HAO_PING);
        }
    }
    
    public boolean is1yuanShowed(){
        return (this.showed & Type.ONE_YUAN) > 0;
    }

    public void set1YuanShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.ONE_YUAN;
        } else {
            this.showed &= (~Type.ONE_YUAN);
        }
    }
    
    public void setOldLinkShowed(boolean toBeOn){
        if (toBeOn) {
            this.showed |= Type.OLD_LINK;
        } else {
            this.showed &= (~Type.OLD_LINK);
        }
    }
    
    public boolean isOldLinkShowed(){
        return (this.showed & Type.OLD_LINK) > 0;
    }
    
    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId);

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

    static String EXIST_ID_QUERY = "select id from " + UserOP.TABLE_NAME + " where userId = ? ";

    public static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    static String insertSQL = "insert into `user_op`(`userId`,`isshowed`,`showed`,`updateRemarkToTB`) values(?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(false, insertSQL, this.userId, this.isshowed, this.showed, this.updateRemarkToTB);

        log.info("[Insert UserOP userId:]" + userId  + ": ]" + this.userId);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId );
            return false;
        }

    }
    
   /* public static UserOP findByUserId(Long userId){
        return UserOP.find("userId = ?", userId).first();
    } */
    
    public static UserOP findByUserId(Long userId) {

        String query = "select userId,isshowed,showed,updateRemarkToTB,id from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<UserOP>(dp, query, userId) {

            @Override
            public UserOP doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	UserOP op = new UserOP(rs.getLong(1),rs.getBoolean(2),rs.getInt(3),rs.getBoolean(4));
                	op.setId(rs.getLong(5));
                	return op; 
                } else {
                    return null;
                }
            }

        }.call();
    }
    
    public boolean rawUpdate() {
        long updateNum = dp.insert(false, "update `user_op` set `userId` = ?,`isshowed` = ?, `showed` = ?, `updateRemarkToTB` = ? where `id` = ? ", this.userId, this.isshowed, this.showed,
                this.updateRemarkToTB, this.id);

        if (updateNum == 1) {
            log.info("[Update UserOP userId:]" + userId );

            return true;
        } else {
            log.error("update failed...for userId:" + this.userId );
            return false;
        }
    }

    public static long rawDelete(Long userId, Long expiredTs) {
        long deleteNum = dp.insert(false, "delete from `user_op` where `userId` = ? ", userId);
        log.error("Delete userId for " + userId );
        return deleteNum;

    }

    @Override
    public String getIdName() {
        return "id";
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isIsshowed() {
        return isshowed;
    }

    public void setIsshowed(boolean isshowed) {
        this.isshowed = isshowed;
    }
    
    public int getShowed() {
        return showed;
    }

    public void setShowed(int showed) {
        this.showed = showed;
    }

	public boolean isUpdateRemarkToTB() {
		return updateRemarkToTB;
	}

	public void setUpdateRemarkToTB(boolean updateRemarkToTB) {
		this.updateRemarkToTB = updateRemarkToTB;
	}

	@Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
