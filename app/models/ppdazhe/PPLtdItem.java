package models.ppdazhe;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name =PPLtdItem.TABLE_NAME)
public class PPLtdItem extends Model implements PolicySQLGenerator{
	private static final Logger log=LoggerFactory.getLogger(PPDazheActive.class);
	
	public static final String TABLE_NAME="ppltd_item";
	
	public static final PPLtdItem EMPTY=new PPLtdItem();
	   
    private String itemCode;
    
    @Index(name = "activityId")
    private String activityId; 
    
    private Long sellerUin;
    
    private int buyLimit;
    
    private String itemBeginTime;
    
    private String itemEndTime;
    
    private int itemDiscount;
    
    public PPLtdItem(){
    	
    }
    
    public PPLtdItem(Long id,String itemCode,String activityId,Long sellerUin,
    		int buyLimit,String itemBeginTime,String itemEndTime, int itemDiscount){
    	//this.totalNum=totalNum;
    	this.id=id;
    	this.itemCode=itemCode;
    	this.activityId=activityId;
    	this.sellerUin=sellerUin;
    	this.buyLimit=buyLimit;
    	this.itemBeginTime=itemBeginTime;
    	this.itemEndTime=itemEndTime;
    	this.itemDiscount=itemDiscount;

    }
    
    public PPLtdItem(String itemCode,String activityId,Long sellerUin,
    		int buyLimit,String itemBeginTime,String itemEndTime, int itemDiscount){
    	//this.totalNum=totalNum;
    	this.itemCode=itemCode;
    	this.activityId=activityId;
    	this.sellerUin=sellerUin;
    	this.buyLimit=buyLimit;
    	this.itemBeginTime=itemBeginTime;
    	this.itemEndTime=itemEndTime;
    	this.itemDiscount=itemDiscount;

    }
    
    public PPLtdItem(String itemCode,String activityId,Long sellerUin, int buyLimit,int itemDiscount){
    	this.activityId=activityId;
    	this.itemCode=itemCode;
    	this.sellerUin=sellerUin;
    	this.buyLimit=buyLimit;
    	this.itemDiscount=itemDiscount;
    }

    

    
    public String getItemCode(){
    	return itemCode;
    }

	public void setItemCode(String itemCode){
    	this.itemCode=itemCode;
    }
    public String getActivityId(){
    	return activityId;
    }

    public void setActivityId(String activityId){
    	this.activityId=activityId;
    }
    
    public Long getSellerUin(){
    	return sellerUin;
    }
    
    public void setSellerUin(Long sellerUin){
    	this.sellerUin=sellerUin;
    }
    
    public int getBuyLimit(){
    	return buyLimit;
    }
    
    public void setBuyLimit(int buyLimit){
    	this.buyLimit=buyLimit;
    }
    
    public String getItemBeginTime(){
    	return itemBeginTime;
    }

    public void setItemBeginTime(String itemBeginTime){
    	this.itemBeginTime=itemBeginTime;
    }
    
    public String getItemEndTime(){
    	return itemEndTime;
    }

    public void setItemEndTime(String itemEndTime){
    	this.itemEndTime=itemEndTime;
    }
       
    public int getItemDiscount(){
    	return itemDiscount;
    }
    
    public void setItemDiscount(int itemDiscount){
    	this.itemDiscount=itemDiscount;
    }
    
	@Override
	public String toString() {
		return "PPLtdItem [itemCode=" + itemCode + ", activityId=" + activityId
				+ ", sellerUin=" + sellerUin + ", buyLimit=" + buyLimit
				+ ", itemBeginTime=" + itemBeginTime + ", itemEndTime="
				+ itemEndTime + ", itemDiscount=" + itemDiscount + "]";
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
    static String EXIST_ID_QUERY = "select id from `ppltd_item` where sellerUin  = ? and itemCode = ?";

    public static long findExistId(Long sellerUin, String itemCode) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY,  sellerUin, itemCode);
    }
  
    /*
     * long totalNum,String itemCode,String activityId,long sellerUin,
     * long buyLimit,long minPrice,String lastModifyTime,String addTime,String itemPreTime,
     * String itemBeginTime,String itemEndTime,int itemStatus, long itemDiscount,
     * long soldNum,long payNum stockNum
     * */
    @Transient
    static String insertSQL = "insert into `ppltd_item`(`itemCode`,`activityId`,`sellerUin`" +
    		",`buyLimit`,`itemBeginTime`,`itemEndTime`,`itemDiscount`) values(?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.itemCode, this.activityId,
                this.sellerUin, this.buyLimit,this.itemBeginTime, this.itemEndTime, this.itemDiscount);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }

    @Transient
    static final String updateSQL = "update `ppltd_item` set `itemCode` = ?, `activityId` = ?, " +
    		"`sellerUin` = ?, `buyLimit` = ?, " +
    		"`itemBeginTime` = ?, `itemEndTime` = ?, `itemDiscount` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(updateSQL, this.itemCode,
                this.activityId, this.sellerUin, this.buyLimit, this.itemBeginTime, this.itemEndTime,this.itemDiscount,
                this.getId());

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
            long existdId = findExistId(this.sellerUin, this.itemCode);

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
