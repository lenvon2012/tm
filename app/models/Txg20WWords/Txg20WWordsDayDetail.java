package models.Txg20WWords;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.item.ItemPlay;
import models.item.ItemPlay.Status;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.taobao.api.domain.Item;

import controllers.ToolInterface;

@Entity(name = Txg20WWordsDayDetail.TABLE_NAME)
@JsonIgnoreProperties(value = { "entityId", "tableHashKey", "persistent",
		"tableName", "idName", "idColumn", "id" })
public class Txg20WWordsDayDetail extends Model implements PolicySQLGenerator {

	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(Txg20WWordsDayDetail.class);

	@Transient
	public static final String TABLE_NAME = "txg_20w_words_day_detail";

	@Transient
	public static Txg20WWordsDayDetail EMPTY = new Txg20WWordsDayDetail();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Index(name = "numIid")
	public Long numIid;

	@Index(name = "rank")
	public int rank;

	@Index(name = "day")
	public String day;

	@Index(name = "wordId")
	public Long wordId;

	public Txg20WWordsDayDetail() {
		super();
	}

	public Txg20WWordsDayDetail(Long wordId, Long numIid, int rank, String day) {
		super();
		this.numIid = numIid;
		this.rank = rank;
		this.day = day;
		this.wordId = wordId;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public Long getWordId() {
		return wordId;
	}

	public void setWordId(Long wordId) {
		this.wordId = wordId;
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	@Override
	public String getTableHashKey() {
		return null;
	}

	@Override
	public String getIdColumn() {
		return "numIid";
	}

	@Override
	public Long getId() {
		return numIid;
	}

	@Override
	public void setId(Long numIid) {
		this.numIid = numIid;
	}

	public static long findExistId(Long wordId, Long day) {

		String query = "select wordId from " + TABLE_NAME
				+ " where wordId = ? and day = ? ";

		return dp.singleLongQuery(query, wordId, day);
	}

	@Override
	public boolean jdbcSave() {
		try {

			return this.rawInsert();

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	public static boolean batchInsert(Long wordId, String[] rankArr, String day, int offset) {
		if (rankArr == null || rankArr.length <= 0 || rankArr.length > ToolInterface.WireLessPageSize) {
			log.info("Txg20WWordsDayDetail batchInsert rankArr 为空或长于20");
			return true;
		}
		if (wordId == null || wordId <= 0L) {
			log.info("Txg20WWordsDayDetail batchInsert wordId 为空");
			return true;
		}
		if (StringUtils.isEmpty(day)) {
			log.info("Txg20WWordsDayDetail batchInsert day 为空");
			return true;
		}
		if(offset < 0 || (offset % ToolInterface.WireLessPageSize) > 0) {
			log.info("Txg20WWordsDayDetail batchInsert offset 不合法");
			return true;
		}
		// StringBuilder insert_sql = new StringBuilder(BATCH_INSERT_SQL);
		StringBuilder insert_sql = new StringBuilder(
				"insert into `txg_20w_words_day_detail`"
						+ "(`wordId`,`numIid`,`rank`,`day`) values");

		int count = 1;
		int size = rankArr.length;
		for (int i = 1; i <= rankArr.length; i++) {
			String numIid = rankArr[i - 1];
			if (StringUtils.isEmpty(numIid)) {
				continue;
			}
			insert_sql.append(genInsertValues(wordId, numIid, i + offset, day));
			if (count < size) {
				count++;
				insert_sql.append(',');
			}
		}

		String rawInsertSQl = insert_sql.toString();
		try {
			// String.valueOf(DBBuilder.genUserIdHashKey(userId))
			String query = rawInsertSQl;
			return dp.insert(query) > 0L;
		} catch (Exception e) {
			log.warn("bad query:" + rawInsertSQl);
			log.warn(e.getMessage(), e);
			return false;
		}
	}

	public static String genInsertValues(Long wordId, String numIid, int i,
			String day) {

		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(wordId);
		sb.append(",");
		sb.append(numIid);
		sb.append(",");
		sb.append(i);
		sb.append(",'");
		sb.append(day);
		sb.append("')\n");
		return sb.toString();

	}

	@Override
	public String getIdName() {
		return "id";
	}

	public boolean rawInsert() {
		try {
			String insertSQL = "insert into `" + TABLE_NAME
					+ "` (`wordId`,`numIid`,`rank`,`day`)" + " values(?,?,?,?)";

			long id = dp.insert(insertSQL, this.wordId, this.numIid, this.rank,
					this.day);

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

	public static Integer isExisted(Long wordId, String day) {
		if (wordId == null || StringUtils.isEmpty(day)) {
			return null;
		}
		String query = "select id from " + TABLE_NAME
				+ " where wordId = ? and day = ? limit 1";

		return new JDBCBuilder.JDBCExecutor<Integer>(dp, query, wordId, day) {
			@Override
			public Integer doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		}.call();
	}

	public static List<Txg20WWordsDayDetail> findByParams(Long wordId,
			String day) {
		if (wordId == null || StringUtils.isEmpty(day)) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where wordId = ? and day = ?";

		return new JDBCBuilder.JDBCExecutor<List<Txg20WWordsDayDetail>>(dp,
				query, wordId, day) {
			@Override
			public List<Txg20WWordsDayDetail> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<Txg20WWordsDayDetail> result = new ArrayList<Txg20WWordsDayDetail>();
				while (rs.next()) {
					result.add(parseTxg20WWordsDayDetail(rs));
				}
				return result;
			}
		}.call();
	}

	private static final String SelectAllProperties = " id, wordId, numIid, rank, day,"
			+ " aclick, impression ";

	private static Txg20WWordsDayDetail parseTxg20WWordsDayDetail(ResultSet rs) {
		try {
			Txg20WWordsDayDetail detail = new Txg20WWordsDayDetail(
					rs.getLong(2), rs.getLong(3), rs.getInt(4), rs.getString(5));
			detail.setId(rs.getLong(1));
			return detail;

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
