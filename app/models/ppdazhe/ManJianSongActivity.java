package models.ppdazhe;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name =ManJianSongActivity.TABLE_NAME)
public class ManJianSongActivity extends Model implements PolicySQLGenerator {
	
	private static final Logger log=LoggerFactory.getLogger(ManJianSongActivity.class);
	
    public static final String TABLE_NAME ="manjiansong_activity";
    
    @Index(name = "sellerUin")
    private Long sellerUin;
    
    private String beginTime;
    
    private String endTime;
    
    private String activityDesc;
    
	@Lob
    private String contentJson;
	
	public ManJianSongActivity(){
		
	}
    
    public ManJianSongActivity(Long sellerUin,String beginTime,String endTime,String activityDesc
    		,String contentJson){
    	this.sellerUin=sellerUin;
    	this.beginTime=beginTime;
    	this.endTime=endTime;
    	this.activityDesc=activityDesc;
    	this.contentJson=contentJson;
    }

    public Long getSellerUin() {
        return sellerUin;
    }

    public void setSellerUin(Long sellerUin) {
        this.sellerUin = sellerUin;
    }
    
    public String getBeginTime() {
		return beginTime;
	}
    
    public void setBeginTime(String beginTime) {
		this.beginTime=beginTime;
	}

    public String getEndTime() {
		return endTime;
	}

    public void setEndTime(String endTime) {
		this.endTime=endTime;
	}
    
    public String getActivityDesc(){
    	return activityDesc;
    }
    
    public void setActivityDesc(String activityDesc){
    	this.activityDesc=activityDesc;
    }
    
    public String getContentJson(){
    	return contentJson;
    }
    
    public void setContentJson(String contentJson){
    	this.contentJson=contentJson;
    }
	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return TABLE_NAME;
	}

	@Override
	public String getTableHashKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdColumn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean jdbcSave() {
		 try {
	            long existdId = findExistId(this.sellerUin);

	            if (existdId == 0L) {
	                return this.rawInsert();
	            } else {	
	            	this.setId(existdId);
	                return this.rawUpdate();
	            }

	        } catch (Exception e) {
	            log.warn(e.getMessage(), e);
	            return false;
	        }
	}


	@Override
	public String getIdName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "ManJianSongActivity [sellerUin=" + sellerUin + ", beginTime="
				+ beginTime + ", endTime=" + endTime + ", activityDesc="
				+ activityDesc + ", contentJson=" + contentJson + "]";
	}
	
    @Transient
    static String EXIST_ID_QUERY = "select id from `manjiansong_activity` where sellerUin  = ?";
    
    public static long findExistId(Long sellerUin) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, sellerUin);
    }

    @Transient
    static String insertSQL = "insert into `manjiansong_activity`(`sellerUin`,`beginTime`,`endTime`,`activityDesc`,`contentJson`) values(?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.sellerUin, this.beginTime, this.endTime,
                this.activityDesc, this.contentJson);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    @Transient
    static final String updateSQL = "update `manjiansong_activity` set  `sellerUin` = ?, `beginTime` = ?, `endTime` = ?, `activityDesc` = ?, `contentJson` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false,updateSQL, this.sellerUin, this.beginTime, this.endTime,
                this.activityDesc, this.contentJson,
                this.getId());
//        log.info(updateSQL+"|||||||||||||"+updateNum);
        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }

}
