package models.ump;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;


@Entity(name = removeMjsTmplForEndActivityLog.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn", "id"
})
public class removeMjsTmplForEndActivityLog extends GenericModel implements
		PolicySQLGenerator {

	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(removeMjsTmplForEndActivityLog.class);

	@Transient
	public static final String TABLE_NAME = "remove_mjs_tmpl_for_end_activity_log";

	@Transient
	public static removeMjsTmplForEndActivityLog EMPTY = new removeMjsTmplForEndActivityLog();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	
	public removeMjsTmplForEndActivityLog() {
		super();
	}

	public Long jobTs;
	
	@Id
	public Long activityId;
	
	public int activityType;
	
	public Long userId;
	
	@Column(columnDefinition = "varchar(2045) default null")
	public String numIids;

	public removeMjsTmplForEndActivityLog(Long jobTs, Long activityId, int activityType, 
			Long userId, String numIids) {
		super();
		this.jobTs = jobTs;
		this.activityId = activityId;
		this.activityType = this.activityType;
		this.userId = userId;
		this.numIids = numIids;
	}

	public Long getJobTs() {
		return jobTs;
	}

	public void setJobTs(Long jobTs) {
		this.jobTs = jobTs;
	}

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public int getActivityType() {
		return activityType;
	}

	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getNumIids() {
		return numIids;
	}

	public void setNumIids(String numIids) {
		this.numIids = numIids;
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
		return "activityId";
	}

	@Override
	public Long getId() {
		return activityId;
	}

	@Override
	public void setId(Long activityId) {
		this.activityId = activityId;
	}

	public static long findExistId(Long activityId) {

		String query = "select activityId from " + TABLE_NAME + " where activityId = ?";

		return dp.singleLongQuery(query, activityId);
	}

	@Override
	public boolean jdbcSave() {
		try {

			long existId = findExistId(this.activityId);

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
					+ "` (`jobTs`,`activityId`,`activityType`,`userId`,`numIids`)" 
					+ " values(?,?,?,?,?)";

			long id = dp.insert(insertSQL, this.jobTs, this.activityId, this.activityType,
					this.userId, this.numIids);

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
				+ "` set `jobTs` = ?, `userId` = ?, `activityType` = ?, `numIids` = ? where `activityId` = ? ";

		long updateNum = dp.insert(updateSQL, this.jobTs, this.userId, this.activityType, 
				this.numIids, this.activityId);

		if (updateNum == 1) {

			return true;
		} else {

			return false;
		}
	}

}

