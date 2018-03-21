package models.ppdazhe;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name =PPDazheActive.TABLE_NAME)
public class PPDazheActive extends Model implements PolicySQLGenerator{
	
	private static final Logger log=LoggerFactory.getLogger(PPDazheActive.class);
	
	public static final String TABLE_NAME="pp_dazhe_active";

    public static final PPDazheActive EMPTY = new PPDazheActive();
    
	public static class Status {
		public static final String ACTIVE = "ACTIVE";
		public static final String UNACTIVE = "UNACTIVE";
	}
    
    @Index(name = "sellerUin")
    private Long sellerUin;
    
    private String beginTime;
    
    private String endTime;
    
    private String activityName;
    
    private String activityId;
    
	@Lob
    private String  itemStrings;
    
    private String status;
    
    public PPDazheActive(){
    	
    }
    
    public PPDazheActive(Long sellerUin,String beginTime,String endTime,String activityName,String activityId){
    	this.sellerUin=sellerUin;
    	this.beginTime=beginTime;
    	this.endTime=endTime;
    	this.activityName=activityName;
    	this.activityId=activityId;
    }
    
    public PPDazheActive(Long sellerUin,String beginTime,String endTime,String activityName,String activityId,String status){
    	this.sellerUin=sellerUin;
    	this.beginTime=beginTime;
    	this.endTime=endTime;
    	this.activityName=activityName;
    	this.activityId=activityId;
    	this.status=status;
    }
    
    public PPDazheActive(Long id,Long sellerUin,String beginTime,String endTime,String activityName,String activityId,String itemStrings,String status){
    	this.id=id;
    	this.sellerUin=sellerUin;
    	this.beginTime=beginTime;
    	this.endTime=endTime;
    	this.activityName=activityName;
    	this.activityId=activityId;
    	this.status=status;
    	this.itemStrings=itemStrings;
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
    
    public String getActivityName() {
		return activityName;
	}
    
    public void setActivityName(String activityName) {
		this.activityName=activityName;
	}
    
    public String getActivityId() {
		return activityId;
	}
    
    public void setActivityId(String activityId) {
		this.activityId=activityId;
	}
    
    public String getItemStrings() {
		return itemStrings;
	}
    
    public void setItemStrings(String itemStrings) {
		this.itemStrings=itemStrings;
	}
    public void addItemStrings(String item){
    	if(StringUtils.isEmpty(itemStrings)){
    		itemStrings=item;
    	}
    	else itemStrings+=","+item;
    }

    public String getStatus(){
    	return status;
    }
    
    public void setStatus(String status){
    	this.status=status;
    }
    
	@Override
	public String toString() {
		return "PPDazheActive [sellerUin=" + sellerUin + ", beginTime="
				+ beginTime + ", endTime=" + endTime + ", activityName="
				+ activityName + ", activityId=" + activityId
				+ ", itemStrings=" + itemStrings + ", status=" + status + "]";
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
		return "id";
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub
		this.id=id;
	}

	@Override
	public String getIdName() {
		// TODO Auto-generated method stub
		return "id";
	}
	
    @Transient
    static String EXIST_ID_QUERY = "select id from `pp_dazhe_active` where sellerUin  = ? and activityId = ?";
    
    public static long findExistId(Long sellerUin, String activityId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, sellerUin, activityId);
    }
    
    @Transient
    static String insertSQL = "insert into `pp_dazhe_active`(`sellerUin`,`beginTime`,`endTime`,`activityName`,`activityId`,`itemStrings`,`status`) values(?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.sellerUin, this.beginTime, this.endTime,
                this.activityName, this.activityId,this.itemStrings,this.status);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    @Transient
    static final String updateSQL = "update `pp_dazhe_active` set  `sellerUin` = ?, `beginTime` = ?, `endTime` = ?, `activityName` = ?, `activityId` = ?, `itemStrings` = ? , `status` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false,updateSQL, this.sellerUin, this.beginTime, this.endTime,
                this.activityName, this.activityId,this.itemStrings,this.status,
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
    
	@Override
	public boolean jdbcSave() {
		 try {
	            long existdId = findExistId(this.sellerUin, this.activityId);

	            if (existdId == 0L) {
	                return this.rawInsert();
	            } else {	            	
	                return this.rawUpdate();
	            }

	        } catch (Exception e) {
	            log.warn(e.getMessage(), e);
	            return false;
	        }
	}

}
