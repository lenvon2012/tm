package models.autolist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * 用户自动上下架的记录
 * @author Administrator
 *
 */
@Entity(name = AutoListRecord.TABLE_NAME)
public class AutoListRecord extends Model implements PolicySQLGenerator {
	@Transient
	private static final Logger log = LoggerFactory.getLogger(AutoListRecord.class);
	
	@Transient
    public static final String TABLE_NAME = "auto_list_record"; 
	
    @Transient
    public static AutoListRecord EMPTY = new AutoListRecord();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
	public static class DelistDistriType {
		public static final int Average = 0;
		public static final int Day = 1;
		public static final int Night = 2;
		public static final int Morning = 3;
		public static final int Afternoon = 4;
		public static final int MorningAndNight = 5;
		public static final int AfternoonAndNight = 6;
		
		public static final int UseSelfDefine = 7;//自定义时间段
	}
	
	@Index(name="userId")
	private long userId;
	
	private boolean isTurnOn = true;
	
	//是否计算完成
	private boolean isCalcuComplete = false;
	
	private long createTime;
	
	//更新新加的宝贝时间
	private long updateTime;
	
	//分布类型
	private int distriType = 0;
	
	private String distriTime;
	
	private String distriHours;
	
	@Column(columnDefinition = "text")
	private String autoListSchedule;//上架计划分布

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public boolean getIsTurnOn() {
		return isTurnOn;
	}

	public void setIsTurnOn(boolean isTurnOn) {
		this.isTurnOn = isTurnOn;
	}

	public boolean getIsCalcuComplete() {
		return isCalcuComplete;
	}

	public void setIsCalcuComplete(boolean isCalcuComplete) {
		this.isCalcuComplete = isCalcuComplete;
	}
	
	
	
	public String getDistriHours() {
        return distriHours;
    }

    public void setDistriHours(String distriHours) {
        this.distriHours = distriHours;
    }

    public String getDistriTime() {
		return distriTime;
	}

	public void setDistriTime(String distriTime) {
		this.distriTime = distriTime;
	}

	public String getAutoListSchedule() {
		return autoListSchedule;
	}

	public void setAutoListSchedule(String autoListSchedule) {
		this.autoListSchedule = autoListSchedule;
	}

	public int getDistriType() {
		return distriType;
	}

	public void setDistriType(int distriType) {
		this.distriType = distriType;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public AutoListRecord() {
		
	}
	
	public static AutoListRecord createAutoListRecord(Long userId) {
		AutoListRecord record = new AutoListRecord();
		record.setUserId(userId);
		record.setIsTurnOn(true);
		record.setIsCalcuComplete(false);
		long time = System.currentTimeMillis();
		record.setCreateTime(time);
		record.setUpdateTime(time);
		return record;
	}
	
	
	/*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? ";

    private static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
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
                id=existdId;
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
                        "insert into `auto_list_record`(`userId`,`isTurnOn`,`isCalcuComplete`,`createTime`,`updateTime`,`distriType`,`autoListSchedule`,`distriTime`,`distriHours`) values(?,?,?,?,?,?,?,?,?)",
                        this.userId, this.isTurnOn, this.isCalcuComplete, this.createTime, this.updateTime, this.distriType, this.autoListSchedule,this.distriTime, this.distriHours);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert(
                        "update `auto_list_record` set  `userId` = ?, `isTurnOn` = ?, `isCalcuComplete` = ?, `createTime` = ?, `updateTime` = ?, `distriType` = ?, `autoListSchedule` = ?, `distriTime` = ?, `distriHours` = ? where `id` = ? ",
                        this.userId, this.isTurnOn, this.isCalcuComplete, this.createTime, this.updateTime, this.distriType, this.autoListSchedule, this.distriTime, this.distriHours, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }
	
	
}
