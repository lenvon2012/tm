package models.words;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.Txg20WWords.Txg20WWordsDayDetail;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.PlayUtil;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;


@Entity(name = Txg20WWordsDayNoDump.TABLE_NAME)
@JsonIgnoreProperties(value = { "entityId", "tableHashKey", "persistent",
		"tableName", "idName", "idColumn", "id" })
public class Txg20WWordsDayNoDump extends Model implements PolicySQLGenerator {

	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(Txg20WWordsDayDetail.class);

	@Transient
	public static final String TABLE_NAME = "txg_20w_words_day_nodump";

	@Transient
	public static Txg20WWordsDayDetail EMPTY = new Txg20WWordsDayDetail();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

	@Transient
	public static ThreadLocal<DateFormat> sdf = new ThreadLocal<DateFormat>(){
	    @Override
	    protected DateFormat initialValue() {
	        return new SimpleDateFormat("yyyyMMdd");
	    }
	  };
	
	@Index(name = "numIid")
	public Long numIid;
	
	/*
	 * 30天销量
	 */
	@Index(name = "sold")
	public int sold;
			
	/*
	 * 宝贝所属类目
	 */
	@Index(name = "category")
	public Long category;
	
	public Txg20WWordsDayNoDump() {
		super();
	}

	public Txg20WWordsDayNoDump(Long numIid, 
			int sold, Long category) {
		super();
		this.numIid = numIid;
		this.sold = sold;
		this.category = category;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public int getSold() {
		return sold;
	}

	public void setSold(int sold) {
		this.sold = sold;
	}

	public Long getCategory() {
		return category;
	}

	public void setCategory(Long category) {
		this.category = category;
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}
	
	public static String genDayTableName(String day) {
		return TABLE_NAME + day.trim();
	}

	public static String genDayTableName() {
		return TABLE_NAME + sdf.get().format(System.currentTimeMillis());
	}
	
	@Override
	public String getIdColumn() {
		return "id";
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public static long findExistId(Long numIid, String day) {

		String query = "select id from " + genDayTableName(day) + " where numIid = ?";

		return dp.singleLongQuery(query, numIid);
	}

	@Override
	public boolean jdbcSave() {
		return false;
	}

	public boolean rawUpdate() {
        String updateSQL = "update `" + genDayTableName() + "` set " +
                "`numIid` = ?, `sold` = ?, `category` = ?" + 
                " where `id` = ? ";

        long updateNum = dp.update(updateSQL, 
        		this.numIid, this.sold, this.category,
                this.id);

        if (updateNum >= 1) {
            return true;
        } else {
            log.error("update failed...for id : ]" + this.id + "[numIid : ]" + this.numIid);
            return false;
        }
    }
	
	public boolean rawInsert() {
		try {
			String insertSQL = "insert into `" + genDayTableName()
					+ "` (`numIid`,`sold`,`category`)" + 
					" values(?,?,?)";

			long id = dp.insert(insertSQL, this.numIid, this.sold,
					this.category);

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

	public static List<Txg20WWordsDayNoDump> findListByCidAndDate(long cid, String day, int pn, int ps, String sortOrder) {
		if (pn < 1) {
			pn = 1;
		}
		if (ps < 1 || ps > 50) {
			ps = 1;
		}
//		//日期格式检查,已在TradeQuery中进行，其他方法调用需自行校验一次
//		DateFormat fmt =new SimpleDateFormat("yyyyMMdd");
//		try {
//			if (day != fmt.format(fmt.parse(day))) {
//				day = fmt.format(new Date(System.currentTimeMillis() - 86400000L));//若日期值有误（如20160835）则使用前一日日期
//			};
//		} catch (ParseException e) {
//			return new ArrayList<Txg20WWordsDayNoDump>();
//		}
		if ("asc".equalsIgnoreCase(sortOrder)) {
			sortOrder = "ASC";
		} else {
			sortOrder = "DESC";
		}
		StringBuilder query = new StringBuilder("SELECT ").append(SelectAllProperties).append(" FROM ").append(TABLE_NAME).append(day).append(" WHERE category = ? ORDER BY sold ").append(sortOrder).append(", `id` ASC LIMIT ?, ?");
		return findListByJDBC(query.toString(), cid, (pn-1)*ps, ps);
	}

	public static void createOrUpdate(Txg20WWordsDayNoDump noDump) {
		if(noDump == null) {
			return;
		}
		Txg20WWordsDayNoDump detail = Txg20WWordsDayNoDump.findByNumIidAndDate(noDump.getNumIid(), sdf.get().format(System.currentTimeMillis()));
		if(detail == null) {
			new Txg20WWordsDayNoDump(noDump.getNumIid(), noDump.getSold(), noDump.getCategory()).rawInsert();
		}
	}
	
	public static void createOrUpdate(Long numIid, int sold,
			Long category) {
		if (numIid == null || numIid <= 0L) {
			return;
		}
		Txg20WWordsDayNoDump detail = Txg20WWordsDayNoDump.findByNumIidAndDate(numIid, sdf.get().format(System.currentTimeMillis()));
		if(detail == null) {
			new Txg20WWordsDayNoDump(numIid, sold, category).rawInsert();
		} else if(detail.getCategory() == 0L && category > 0L) {
			detail.setCategory(category);
			detail.setSold(sold);
			detail.rawUpdate();
		}
	}
	
	private static Txg20WWordsDayNoDump findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<Txg20WWordsDayNoDump>(dp, query, params) {

            @Override
            public Txg20WWordsDayNoDump doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parseTxg20WWordsDayNoDump(rs);
                }
                return null;
            }
        }.call();
	}

	public static Txg20WWordsDayNoDump findByNumIidAndDate(long numIid, String day) {
		if (numIid < 1) {
			return null;
		}
		StringBuilder query = new StringBuilder("SELECT ").append(SelectAllProperties).append(" FROM ").append(genDayTableName(day)).append(" WHERE numIid = ? LIMIT 1");
		return findByJDBC(query.toString(), numIid);
	}

	public static int countByCidAndDate(long cid, String day) {
		StringBuilder query = new StringBuilder("SELECT COUNT(`id`) from ").append(genDayTableName(day)).append(" WHERE `category` = ?");
		return (int) dp.singleLongQuery(query.toString(), cid);
	}
	
	 private static List<Txg20WWordsDayNoDump> findListByJDBC(String query, Object...params) {
	        
	        return new JDBCBuilder.JDBCExecutor<List<Txg20WWordsDayNoDump>>(dp, query, params) {

	            @Override
	            public List<Txg20WWordsDayNoDump> doWithResultSet(ResultSet rs)
	                    throws SQLException {
	            	List<Txg20WWordsDayNoDump> contents = new ArrayList<Txg20WWordsDayNoDump>();
	                while (rs.next()) {
	                	contents.add(parseTxg20WWordsDayNoDump(rs));
	                }
	                return contents;
	            }
	        }.call();
	    }

	@Override
	public String getIdName() {
		return "id";
	}

	private static final String SelectAllProperties = " id, numIid, sold, category ";
	private static Txg20WWordsDayNoDump parseTxg20WWordsDayNoDump(ResultSet rs) {
		try {
			Txg20WWordsDayNoDump detail = new Txg20WWordsDayNoDump(
					rs.getLong(2), rs.getInt(3), rs.getLong(4));
			detail.setId(rs.getLong(1));
			return detail;

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

	@Override
	public String getTableHashKey() {
		return null;
	}

}
