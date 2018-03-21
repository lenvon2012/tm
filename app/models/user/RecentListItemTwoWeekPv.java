package models.user;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;

@Entity(name = RecentListItemTwoWeekPv.TABLE_NAME)
public class RecentListItemTwoWeekPv extends Model {

    public final static Logger log = LoggerFactory.getLogger(RecentListItemTwoWeekPv.class);

    public final static String TABLE_NAME = "recent_list_item_two_week_pv";

    @Index(name = "userId")
    public Long userId;

    @Index(name = "numIid")
    public Long numIid;

    public int day1;
    public int day2;
    public int day3;
    public int day4;
    public int day5;
    public int day6;
    public int day7;
    public int day8;
    public int day9;
    public int day10;
    public int day11;
    public int day12;
    public int day13;
    public int day14;
    public int day15;
    public int day16;
    public int day17;
    public int day18;
    public int day19;
    public int day20;
    public int day21;
    
    public RecentListItemTwoWeekPv(Long userId, Long numIid) {
		super();
		this.userId = userId;
		this.numIid = numIid;
	}
    
    public RecentListItemTwoWeekPv(Long userId, Long numIid, int day1,
			int day2, int day3, int day4, int day5, int day6, int day7,
			int day8, int day9, int day10, int day11, int day12, int day13,
			int day14, int day15, int day16, int day17, int day18, int day19, int day20, int day21) {
		super();
		this.userId = userId;
		this.numIid = numIid;
		this.day1 = day1;
		this.day2 = day2;
		this.day3 = day3;
		this.day4 = day4;
		this.day5 = day5;
		this.day6 = day6;
		this.day7 = day7;
		this.day8 = day8;
		this.day9 = day9;
		this.day10 = day10;
		this.day11 = day11;
		this.day12 = day12;
		this.day13 = day13;
		this.day14 = day14;
		this.day15 = day15;
		this.day16 = day16;
		this.day17 = day17;
		this.day18 = day18;
		this.day19 = day19;
		this.day20 = day20;
		this.day21 = day21;
	}

	@Override
    public void _save() {
        jdbcSave();
    }

	@Transient
    static String EXIST_ID_QUERY = "select numIid from " + TABLE_NAME
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

    static String insertSQL = "insert into `recent_list_item_two_week_pv`(`userId`,`numIid`,`day1`,`day2`,`day3`,`day4`,`day5`" +
    		",`day6`,`day7`,`day8`,`day9`,`day10`,`day11`,`day12`,`day13`,`day14`,`day15`,`day16`,`day17`,`day18`,`day19`,`day20`,`day21`" +
    		") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.numIid, this.day1, this.day2, this.day3, this.day4
        		, this.day5, this.day6, this.day7, this.day8, this.day9, this.day10, this.day11, this.day12, this.day13, this.day14,
        		this.day15, this.day16, this.day17, this.day18, this.day19, this.day20, this.day21);

        log.info("[Insert RecentListItemTwoWeekPv Id:]" + id);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert RecentListItemTwoWeekPv Fails.....");
            return false;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
