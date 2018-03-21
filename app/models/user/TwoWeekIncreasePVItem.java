package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

@Entity(name = TwoWeekIncreasePVItem.TABLE_NAME)
public class TwoWeekIncreasePVItem extends GenericModel {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(TwoWeekIncreasePVItem.class);

    @Transient
    public static final String TABLE_NAME = "two_week_increase_pv_item";

    @Transient
    public static TwoWeekIncreasePVItem EMPTY = new TwoWeekIncreasePVItem();

    @Id
    public Long userId;

    @Index(name = "numIid")
    public Long numIid;
    
    public TwoWeekIncreasePVItem() {
    }

    public TwoWeekIncreasePVItem(Long userId, Long numIid) {
		super();
		this.userId = userId;
		this.numIid = numIid;
	}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Transient
    static String EXIST_ID_QUERY = "select userId from " + TABLE_NAME
            + " where userId = ? and numIid = ? ";

    public static long findExistId(Long userId, Long numIid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
    }

    public boolean jdbcSave() {
    	 long existdId = findExistId(this.userId, this.numIid);
         if (existdId > 0L) {
             return false;
         } else {
             return this.rawInsert();
         }
    }

    @Transient
    static String insertSQL = "insert into `two_week_increase_pv_item`(`userId`,`numIid`) values(?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.numIid);

        log.info("[Insert TwoWeekIncreasePVItem Id:]" + id);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert TwoWeekIncreasePVItem Fails.....");
            return false;
        }
    }

    @Override
    public String toString() {
        return "TwoWeekIncreasePVItem [userId=" + userId + ", numIid=" + numIid 
                + "]";
    }
}
