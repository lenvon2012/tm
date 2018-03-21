package models.showwindow;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = DoubleTwelveOpenUser.TABLE_NAME)
public class DoubleTwelveOpenUser extends GenericModel implements PolicySQLGenerator {
	@Transient
    private static final Logger log = LoggerFactory.getLogger(DoubleTwelveOpenUser.class);

    @Transient
    public static final String TABLE_NAME = "double_twelve_open_user";

    @Transient
    public static DoubleTwelveOpenUser EMPTY = new DoubleTwelveOpenUser();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Id
    private Long userId = NumberUtil.DEFAULT_LONG;
    
    private String userNick = StringUtils.EMPTY;
    
    public DoubleTwelveOpenUser() {
    	super();
    }
    
    public DoubleTwelveOpenUser(Long userId, String userNick) {
    	this.userId = userId;
    	this.userNick = userNick;
    }

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
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

    public static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }
    
    static String DELETE_BY_USERID = "delete from " + TABLE_NAME + " where userId = ?";
    public static boolean deleteByUserId(Long userId) {
    	return dp.insert(DELETE_BY_USERID, userId) > 0;
    }
    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

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

    public boolean rawInsert() {
        long id = dp
                .insert(
                        "insert into `" + TABLE_NAME + "`(`userId`,`userNick`) values(?,?)",
                        this.userId, this.userNick);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .update(
                        "update `" + TABLE_NAME + "` set  `userNick` = ? where `userId` = ? ",
                        this.userNick, this.userId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :[userId : ]" + this.userId);
            return false;
        }
    }

    static String FIND_BY_USERID = "select userId,userNick from "
            + TABLE_NAME
            + " where userId = ? ";

    public static DoubleTwelveOpenUser findByUserId(Long userId) {
        return new JDBCBuilder.JDBCExecutor<DoubleTwelveOpenUser>(dp, FIND_BY_USERID, userId) {
            @Override
            public DoubleTwelveOpenUser doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return new DoubleTwelveOpenUser(rs.getLong(1), rs.getString(2));
                }
                return null;
            }
        }.call();
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
}
