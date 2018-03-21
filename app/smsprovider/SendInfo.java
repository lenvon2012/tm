package smsprovider;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = SendInfo.TABLE_NAME)
public class SendInfo extends Model implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(SendInfo.class);

	public static final String TABLE_NAME = "SendInfo";

	public static SendInfo EMPTY = new SendInfo();

	public SendInfo() {

	}

	@Index(name = "userId")
	public Long userId;

	@Index(name = "ts")
	public Long ts;

	public Long addTs;

	public String mobile;

	public String content;

	public int smsNum;

	public int status;

	public SendInfo(Long userId, Long ts, String mobile, String content,
			int status) {
		this.userId = userId;
		this.ts = ts;
		this.mobile = mobile;
		this.content = content;
		this.status = status;
		this.smsNum = 0;
	}

	public SendInfo(Long userId, Long ts, String mobile, String content,
			int status, int smsNum) {
		this.userId = userId;
		this.ts = ts;
		this.mobile = mobile;
		this.content = content;
		this.smsNum = smsNum;
		this.status = status;
	}

	@Override
	public String getTableName() {
		return this.TABLE_NAME;
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
	public String getIdName() {
		return "id";
	}

	@Override
	public boolean jdbcSave() {
		return this.rawInsert();
	}

	public boolean rawInsert() {

		long id = JDBCBuilder
				.insert(false,
						"insert into `SendInfo`(`userId`,`ts`,`addTs`,`mobile`,`content`,`smsNum`,`status`) values(?,?,?,?,?,?,?)",
						this.userId, this.ts, this.addTs, this.mobile,
						this.content, this.smsNum, this.status);

		log.info("[Insert SendInfo Id:]" + id);

		if (id > 0L) {
			setId(id);
			return true;
		} else {
			log.error("Insert Fails.....");
			return false;
		}
	}

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Long getAddTs() {
        return addTs;
    }

    public void setAddTs(Long addTs) {
        this.addTs = addTs;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSmsNum() {
        return smsNum;
    }

    public void setSmsNum(int smsNum) {
        this.smsNum = smsNum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
