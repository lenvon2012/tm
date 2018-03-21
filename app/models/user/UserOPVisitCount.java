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
 * 用户第二次进入弹窗标志
 *
 */
@Entity(name = UserOPVisitCount.TABLE_NAME)
public class UserOPVisitCount extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserOPVisitCount.class);

    @Transient
    public static final String TABLE_NAME = "user_op_visit_count";

    @Transient
    public static UserOPVisitCount EMPTY = new UserOPVisitCount();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    public Long userId;
    
    public int visitCount = 0;

    public UserOPVisitCount() {

    }

    public UserOPVisitCount(Long userId, int visitCount) {
        this.userId = userId;
        this.visitCount = visitCount;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? ";

    public static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    static String insertSQL = "insert into `user_op_visit_count`(`userId`,`visitCount`) values(?,?)";

    public boolean rawInsert() {

        long id = dp.insert(false, insertSQL, this.userId, this.visitCount);

        log.info("[Insert ReinUserOP userId:]" + userId  + ": ]" + this.userId);

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
    
    public static UserOPVisitCount findByUserId(Long userId) {

        String query = "select userId,visitCount,id from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<UserOPVisitCount>(dp, query, userId) {

            @Override
            public UserOPVisitCount doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	UserOPVisitCount op = new UserOPVisitCount(rs.getLong(1),rs.getInt(2));
                	op.setId(rs.getLong(3));
                	return op; 
                } else {
                    return null;
                }
            }

        }.call();
    }
    
    public boolean rawUpdate() {
        long updateNum = dp.insert(false, "update `user_op_visit_count` set `userId` = ?,`visitCount` = ? " +
        		"where `id` = ? ", this.userId, this.visitCount, this.id);

        if (updateNum == 1) {
            log.info("[Update ReInUserOP userId:]" + userId );

            return true;
        } else {
            log.error("update failed...for userId:" + this.userId );
            return false;
        }
    }

    public static long rawDelete(Long userId, Long expiredTs) {
        long deleteNum = dp.insert(false, "delete from `user_op_visit_count` where `userId` = ? ", userId);
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

    public int getVisitCount() {
		return visitCount;
	}

	public void setVisitCount(int visitCount) {
		this.visitCount = visitCount;
	}

	@Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
