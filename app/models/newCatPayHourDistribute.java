package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.user.User;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import play.jobs.Job;
import result.TMResult;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JPATransactionManager;
import bustbapi.MBPApi;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.QueryRow;

import dao.UserDao;

@Entity(name = newCatPayHourDistribute.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn", "id"
})
public class newCatPayHourDistribute extends GenericModel implements
		PolicySQLGenerator {

	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(newCatPayHourDistribute.class);

	@Transient
	public static final String TABLE_NAME = "new_cat_payhour_distribute";

	@Transient
	public static newCatPayHourDistribute EMPTY = new newCatPayHourDistribute();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Id
	@Index(name = "cid")
	public Long cid;

	// 24小时对应的销量分布
	public int clock0 = 0;
	
	public int clock1 = 0;
	
	public int clock2 = 0;
	
	public int clock3 = 0;
	
	public int clock4 = 0;
	
	public int clock5 = 0;
	
	public int clock6 = 0;
	
	public int clock7 = 0;
	
	public int clock8 = 0;
	
	public int clock9 = 0;
	
	public int clock10 = 0;
	
	public int clock11 = 0;
	
	public int clock12 = 0;
	
	public int clock13 = 0;
	
	public int clock14 = 0;
	
	public int clock15 = 0;
	
	public int clock16 = 0;
	
	public int clock17 = 0;
	
	public int clock18 = 0;
	
	public int clock19 = 0;
	
	public int clock20 = 0;
	
	public int clock21 = 0;
	
	public int clock22 = 0;
	
	public int clock23 = 0;

	public newCatPayHourDistribute(Long cid) {
		super();
		this.cid = cid;
	}
	
	public newCatPayHourDistribute(int[] hourArray, Long cid) {
		this.cid = cid;
		if(hourArray.length > 0) {
			this.clock0 = hourArray[0];
			this.clock1 = hourArray[1];
			this.clock2 = hourArray[2];
			this.clock3 = hourArray[3];
			this.clock4 = hourArray[4];
			this.clock5 = hourArray[5];
			this.clock6 = hourArray[6];
			this.clock7 = hourArray[7];
			this.clock8 = hourArray[8];
			this.clock9 = hourArray[9];
			this.clock10 = hourArray[10];
			this.clock11 = hourArray[11];
			this.clock12 = hourArray[12];
			this.clock13 = hourArray[13];
			this.clock14 = hourArray[14];
			this.clock15 = hourArray[15];
			this.clock16 = hourArray[16];
			this.clock17 = hourArray[17];
			this.clock18 = hourArray[18];
			this.clock19 = hourArray[19];
			this.clock20 = hourArray[20];
			this.clock21 = hourArray[21];
			this.clock22 = hourArray[22];
			this.clock23 = hourArray[23];
		}
		
		
	}
	
	public newCatPayHourDistribute(Long cid, int clock0, int clock1, int clock2,
			int clock3, int clock4, int clock5, int clock6, int clock7,
			int clock8, int clock9, int clock10, int clock11, int clock12,
			int clock13, int clock14, int clock15, int clock16, int clock17,
			int clock18, int clock19, int clock20, int clock21, int clock22,
			int clock23) {
		super();
		this.cid = cid;
		this.clock0 = clock0;
		this.clock1 = clock1;
		this.clock2 = clock2;
		this.clock3 = clock3;
		this.clock4 = clock4;
		this.clock5 = clock5;
		this.clock6 = clock6;
		this.clock7 = clock7;
		this.clock8 = clock8;
		this.clock9 = clock9;
		this.clock10 = clock10;
		this.clock11 = clock11;
		this.clock12 = clock12;
		this.clock13 = clock13;
		this.clock14 = clock14;
		this.clock15 = clock15;
		this.clock16 = clock16;
		this.clock17 = clock17;
		this.clock18 = clock18;
		this.clock19 = clock19;
		this.clock20 = clock20;
		this.clock21 = clock21;
		this.clock22 = clock22;
		this.clock23 = clock23;
	}

	public int getByHour(Integer hour) {
		if(hour == null) {
			return -1;
		}
		if(hour < 0 || hour > 23) {
			return -1;
		}
		switch (hour) {
		case 0:
			return clock0;
		case 1:
			return clock1;
		case 2:
			return clock2;
		case 3:
			return clock3;
		case 4:
			return clock4;
		case 5:
			return clock5;
		case 6:
			return clock6;
		case 7:
			return clock7;
		case 8:
			return clock8;
		case 9:
			return clock9;
		case 10:
			return clock10;
		case 11:
			return clock11;
		case 12:
			return clock12;
		case 13:
			return clock13;
		case 14:
			return clock14;
		case 15:
			return clock15;
		case 16:
			return clock16;
		case 17:
			return clock17;
		case 18:
			return clock18;
		case 19:
			return clock19;
		case 20:
			return clock20;
		case 21:
			return clock21;
		case 22:
			return clock22;
		case 23:
			return clock23;
		default:
			return -1;
		}
	}
	
	public Integer getTotalTradeCount() {
		Integer count = 0;
		for(int i = 0; i <= 23; i++) {
			count += getByHour(i);
		}
		return count;
	}
	
	public void updateByHoueArray(int[] hourArray) {
		if(hourArray.length <= 0) {
			return;
		}
		this.clock0 = hourArray[0];
		this.clock1 = hourArray[1];
		this.clock2 = hourArray[2];
		this.clock3 = hourArray[3];
		this.clock4 = hourArray[4];
		this.clock5 = hourArray[5];
		this.clock6 = hourArray[6];
		this.clock7 = hourArray[7];
		this.clock8 = hourArray[8];
		this.clock9 = hourArray[9];
		this.clock10 = hourArray[10];
		this.clock11 = hourArray[11];
		this.clock12 = hourArray[12];
		this.clock13 = hourArray[13];
		this.clock14 = hourArray[14];
		this.clock15 = hourArray[15];
		this.clock16 = hourArray[16];
		this.clock17 = hourArray[17];
		this.clock18 = hourArray[18];
		this.clock19 = hourArray[19];
		this.clock20 = hourArray[20];
		this.clock21 = hourArray[21];
		this.clock22 = hourArray[22];
		this.clock23 = hourArray[23];
		
	}
	
	public int addByHour(Integer hour) {
		if(hour == null) {
			return -1;
		}
		if(hour < 0 || hour > 23) {
			return -1;
		}
		switch (hour) {
		case 0:
			return ++clock0;
		case 1:
			return ++clock1;
		case 2:
			return ++clock2;
		case 3:
			return ++clock3;
		case 4:
			return ++clock4;
		case 5:
			return ++clock5;
		case 6:
			return ++clock6;
		case 7:
			return ++clock7;
		case 8:
			return ++clock8;
		case 9:
			return ++clock9;
		case 10:
			return ++clock10;
		case 11:
			return ++clock11;
		case 12:
			return ++clock12;
		case 13:
			return ++clock13;
		case 14:
			return ++clock14;
		case 15:
			return ++clock15;
		case 16:
			return ++clock16;
		case 17:
			return ++clock17;
		case 18:
			return ++clock18;
		case 19:
			return ++clock19;
		case 20:
			return ++clock20;
		case 21:
			return ++clock21;
		case 22:
			return ++clock22;
		case 23:
			return ++clock23;
		default:
			return -1;
		}
	}
	
	public newCatPayHourDistribute() {
		super();
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public int getClock0() {
		return clock0;
	}

	public void setClock0(int clock0) {
		this.clock0 = clock0;
	}

	public int getClock1() {
		return clock1;
	}

	public void setClock1(int clock1) {
		this.clock1 = clock1;
	}

	public int getClock2() {
		return clock2;
	}

	public void setClock2(int clock2) {
		this.clock2 = clock2;
	}

	public int getClock3() {
		return clock3;
	}

	public void setClock3(int clock3) {
		this.clock3 = clock3;
	}

	public int getClock4() {
		return clock4;
	}

	public void setClock4(int clock4) {
		this.clock4 = clock4;
	}

	public int getClock5() {
		return clock5;
	}

	public void setClock5(int clock5) {
		this.clock5 = clock5;
	}

	public int getClock6() {
		return clock6;
	}

	public void setClock6(int clock6) {
		this.clock6 = clock6;
	}

	public int getClock7() {
		return clock7;
	}

	public void setClock7(int clock7) {
		this.clock7 = clock7;
	}

	public int getClock8() {
		return clock8;
	}

	public void setClock8(int clock8) {
		this.clock8 = clock8;
	}

	public int getClock9() {
		return clock9;
	}

	public void setClock9(int clock9) {
		this.clock9 = clock9;
	}

	public int getClock10() {
		return clock10;
	}

	public void setClock10(int clock10) {
		this.clock10 = clock10;
	}

	public int getClock11() {
		return clock11;
	}

	public void setClock11(int clock11) {
		this.clock11 = clock11;
	}

	public int getClock12() {
		return clock12;
	}

	public void setClock12(int clock12) {
		this.clock12 = clock12;
	}

	public int getClock13() {
		return clock13;
	}

	public void setClock13(int clock13) {
		this.clock13 = clock13;
	}

	public int getClock14() {
		return clock14;
	}

	public void setClock14(int clock14) {
		this.clock14 = clock14;
	}

	public int getClock15() {
		return clock15;
	}

	public void setClock15(int clock15) {
		this.clock15 = clock15;
	}

	public int getClock16() {
		return clock16;
	}

	public void setClock16(int clock16) {
		this.clock16 = clock16;
	}

	public int getClock17() {
		return clock17;
	}

	public void setClock17(int clock17) {
		this.clock17 = clock17;
	}

	public int getClock18() {
		return clock18;
	}

	public void setClock18(int clock18) {
		this.clock18 = clock18;
	}

	public int getClock19() {
		return clock19;
	}

	public void setClock19(int clock19) {
		this.clock19 = clock19;
	}

	public int getClock20() {
		return clock20;
	}

	public void setClock20(int clock20) {
		this.clock20 = clock20;
	}

	public int getClock21() {
		return clock21;
	}

	public void setClock21(int clock21) {
		this.clock21 = clock21;
	}

	public int getClock22() {
		return clock22;
	}

	public void setClock22(int clock22) {
		this.clock22 = clock22;
	}

	public int getClock23() {
		return clock23;
	}

	public void setClock23(int clock23) {
		this.clock23 = clock23;
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
		return "cid";
	}

	@Override
	public Long getId() {
		return cid;
	}

	@Override
	public void setId(Long cid) {
		this.cid = cid;
	}

	public static long findExistId(Long cid) {

		String query = "select cid from " + TABLE_NAME + " where cid = ? ";

		return dp.singleLongQuery(query, cid);
	}

	@Override
	public boolean jdbcSave() {
		try {

			long existId = findExistId(this.cid);

			if (existId <= 0) {
				return this.rawInsert();
			} else {
				return this.rawUpdate();
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public String getIdName() {
		return "id";
	}

	public boolean rawInsert() {
		try {
			String insertSQL = "insert into `"
					+ TABLE_NAME
					+ "` (`cid`,`clock0`,`clock1`,`clock2`,`clock3`,`clock4`,`clock5`,`clock6`,"
					+ " `clock7`,`clock8`,`clock9`,`clock10`,`clock11`,`clock12`,`clock13`,`clock14`,"
					+ " `clock15`,`clock16`,`clock17`,`clock18`,`clock19`,`clock20`,`clock21`,"
					+ " `clock22`,`clock23`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			long id = dp.insert(insertSQL, this.cid, this.clock0, this.clock1,
					this.clock2, this.clock3, this.clock4, this.clock5,
					this.clock6, this.clock7, this.clock8, this.clock9,
					this.clock10, this.clock11, this.clock12, this.clock13,
					this.clock14, this.clock15, this.clock16, this.clock17,
					this.clock18, this.clock19, this.clock20, this.clock21,
					this.clock22, this.clock23);

			if (id > 0L) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);

			return false;
		}
	}

	public boolean rawUpdate() {

		String updateSQL = "update `"
				+ TABLE_NAME
				+ "` set `clock0` = ?, `clock1` = ?, `clock2` = ?, `clock3` = ?,"
				+ "`clock4` = ?, `clock5` = ?, `clock6` = ?, `clock7` = ?, `clock8` = ?,"
				+ "`clock9` = ?, `clock10` = ?, `clock11` = ?, `clock12` = ?, `clock13` = ?,"
				+ "`clock14` = ?, `clock15` = ?, `clock16` = ?, `clock17` = ?, `clock18` = ?,"
				+ "`clock19` = ?, `clock20` = ?, `clock21` = ?, `clock22` = ?, `clock23` = ? "
				+ " where `cid` = ?  ";

		long updateNum = dp.insert(updateSQL, this.clock0, this.clock1,
				this.clock2, this.clock3, this.clock4, this.clock5,
				this.clock6, this.clock7, this.clock8, this.clock9,
				this.clock10, this.clock11, this.clock12, this.clock13,
				this.clock14, this.clock15, this.clock16, this.clock17,
				this.clock18, this.clock19, this.clock20, this.clock21,
				this.clock22, this.clock23, this.cid);

		if (updateNum == 1) {

			return true;
		} else {

			return false;
		}
	}

	public static newCatPayHourDistribute findByCid(Long cid) {
		if (cid == null) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where cid = ? ";

		return new JDBCBuilder.JDBCExecutor<newCatPayHourDistribute>(dp, query,
				cid) {
			@Override
			public newCatPayHourDistribute doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return parsenewCatPayHourDistribute(rs);
				} else {
					return null;
				}
			}
		}.call();
	}
	
	public static Integer countTotalTradeByCid(Long cid) {
		if (cid == null) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where cid = ? ";

		return new JDBCBuilder.JDBCExecutor<Integer>(dp, query,
				cid) {
			@Override
			public Integer doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return countTotalTrade(rs);
				} else {
					return 0;
				}
			}
		}.call();
	}

	private static final String SelectAllProperties = " cid, clock0, clock1, clock2, clock3, clock4, " +
			"clock5, clock6, clock7, clock8, clock9, clock10, clock11, clock12, clock13, clock14, " +
			"clock15, clock16, clock17, clock18, clock19, clock20, clock21, clock22, clock23";
	
	private static newCatPayHourDistribute parsenewCatPayHourDistribute(ResultSet rs) {
		try {
			return new newCatPayHourDistribute(rs.getLong(1), rs.getInt(2), rs.getInt(3),
					rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8),
					rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12), rs.getInt(13),
					rs.getInt(14), rs.getInt(15), rs.getInt(16), rs.getInt(17), rs.getInt(18),
					rs.getInt(19), rs.getInt(20), rs.getInt(21), rs.getInt(22), rs.getInt(23),
					rs.getInt(24), rs.getInt(25));

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

	private static Integer countTotalTrade(ResultSet rs) {
		try {
			Integer count = 0;
			for(int i = 2; i <= 25; i++) {
				count += rs.getInt(i);
			}
			return count;

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return 0;
		}
	}
	
	public static class CloudDataRegionUpdateJob extends Job {
		@Override
		public void doJob() {
			User user = UserDao.findByUserNick("clorest510");
			dp.update("delete from " + TABLE_NAME);
			TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2961L,
					"thedate=20140111", user.getSessionKey()).call();
			List<QueryRow> rows = res.getRes();
			if (CommonUtils.isEmpty(rows)) {
				return;
			}
			for (QueryRow row : rows) {
				List<String> values = row.getValues();
				if (CommonUtils.isEmpty(values)) {
					continue;
				}
				log.info(values.toString());
				new CloudDataRegion(values).jdbcSave();

			}
		}
	}
	
	public List<Integer> orderByHour() {
		List<Integer> res = new ArrayList<Integer>();
		for(int i = 0; i < 24; i++) {
			res.add(getByHour(i));
		}
		return res;
	}
	
	public static List<newCatPayHourDistribute> findValidList(int offset, int limit) {
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " limit ?,? ";

		return new JDBCBuilder.JDBCExecutor<List<newCatPayHourDistribute>>(dp, query, offset, limit) {
			@Override
			public List<newCatPayHourDistribute> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<newCatPayHourDistribute> res = new ArrayList<newCatPayHourDistribute>();
				while (rs.next()) {
					res.add(parsenewCatPayHourDistribute(rs));
				}
				return res;
			}
		}.call();
	}
	
	public static abstract class newCatPayHourDistributeBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 500L;

        public newCatPayHourDistributeBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public newCatPayHourDistributeBatchOper(int limit) {
            this.limit = limit;
        }

        public List<newCatPayHourDistribute> findNext() {
            return newCatPayHourDistribute.findValidList(offset, limit);
        }

        public abstract void doForEachDistribute(newCatPayHourDistribute distribute);

        @Override
        public Boolean call() {

            while (true) {

                List<newCatPayHourDistribute> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (newCatPayHourDistribute distribute : findList) {
                    offset++;
                    doForEachDistribute(distribute);
                }

                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);
            }

        }
    }
}
