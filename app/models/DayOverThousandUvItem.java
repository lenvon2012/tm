package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.item.ItemPlay;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

@Entity(name = DayOverThousandUvItem.TABLE_NAME)
public class DayOverThousandUvItem extends GenericModel {

    private final static Logger log = LoggerFactory
            .getLogger(DayOverThousandUvItem.class);

    public final static String TABLE_NAME = "day_over_thousand_uv_item";

    @Id
    public long numIid;

    @Index(name = "userId")
    public long userId;

    @Index(name = "uv")
    public long uv;

    public String theDate;

    public String listTime;
    
    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getUv() {
		return uv;
	}

	public void setUv(long uv) {
		this.uv = uv;
	}

	public String getTheDate() {
		return theDate;
	}

	public void setTheDate(String theDate) {
		this.theDate = theDate;
	}

    public String getListTime() {
		return this.listTime;
	}

	public void setListTime(String listTime) {
		this.listTime = listTime;
	}

	public DayOverThousandUvItem(long numIid, long userId, long uv,
			String theDate, String listTime) {
		super();
		this.numIid = numIid;
		this.userId = userId;
		this.uv = uv;
		this.theDate = theDate;
		this.listTime = listTime;
	}

    public boolean rawInsert() {
        // TODO Auto-generated method stub
        long id = JDBCBuilder.insert("insert into `" + TABLE_NAME
                + "`(`numIid`,`userId`,`uv`,`theDate`,`listTime`) values(?,?,?,?,?)", this.numIid,
                this.userId, this.uv, this.theDate, this.listTime);

        if (id > 0L) {
            log.info("insert ts for the first time !" + numIid);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.numIid);

            return false;
        }
    }

    public static DayOverThousandUvItem findMostRecently(Long userId) {
        String sql = "select numIid, userId, uv, theDate, listTime from " + TABLE_NAME + " order by uv desc limit ?,? ";

        return new JDBCExecutor<DayOverThousandUvItem>(sql, userId) {
            @Override
            public DayOverThousandUvItem doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new DayOverThousandUvItem(rs.getLong(1), rs.getLong(2), rs.getLong(3),
                            rs.getString(4), rs.getString(5));
                } else
                    return null;
            }
        }.call();
    }

}
