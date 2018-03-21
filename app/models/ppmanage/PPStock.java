package models.ppmanage;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = PPStock.TABLE_NAME)
public class PPStock extends Model implements PolicySQLGenerator{
	
	private static final Logger log=LoggerFactory.getLogger(PPStock.class);
	
    public static final String TAG = "PPStock";
	
    public static final String TABLE_NAME ="ppstock_";
    
    public static PPStock _instance = new PPStock();
    
    public PPStock(){
    	
    }
    
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, _instance);
    
    @Index(name = "sellerUin")
    private Long sellerUin;

    private String itemCode;
    
    @Index(name = "skuId")
    private Long skuId;

    private Long price;
    
    private String picLink;
    
    //库存数量
    private Long num;
    
    //库存状态码:1-[IS_IN_STORE:仓库中],2-[IS_FOR_SALE:上架销售中],9-[IS_PRE_DELETE:预删除],
    //64-[IS_SALE_ON_TIME:自定义时间上架],6-[IS_SOLD_OUT:售完]
    private int status;
    
    private Long soldNum;
    
    private String saleAttr;
    
    private String stockAttr;
        
    public PPStock(Long sellerUin,String itemCode,Long skuId,Long price,String picLink,Long num
    		,int status,Long soldNum,String saleAttr,String stockAttr){
    	this.sellerUin=sellerUin;
    	this.itemCode=itemCode;
    	this.skuId=skuId;
    	this.price=price;
    	this.picLink=picLink;
    	this.num=num;
    	this.status=status;
    	this.soldNum=soldNum;
    	this.saleAttr=saleAttr;
    	this.stockAttr=stockAttr;
    }
    
    public PPStock(ResultSet rs) throws SQLException {
    	this.sellerUin=rs.getLong(1);
    	this.itemCode=rs.getString(2);
    	this.skuId=rs.getLong(3);
    	this.price=rs.getLong(4);
    	this.picLink=rs.getString(5);
    	this.num=rs.getLong(6);
    	this.status=rs.getInt(7);
    	this.soldNum=rs.getLong(8);
    	this.saleAttr=rs.getString(9);
    	this.stockAttr=rs.getString(10);
    }
    
    public Long getSellerUin() {
        return sellerUin;
    }

    public void setSellerUin(Long sellerUin) {
        this.sellerUin = sellerUin;
    }
    
    public String getItemCode(){
    	return itemCode;
    }
    
    public void setItemCode(String itemCode){
    	this.itemCode=itemCode;
    }
    
    public Long getSkuId(){
    	return skuId;
    }
    
    public void setSkuId(Long skuId){
    	this.skuId=skuId;
    }
    
    public Long getPrice(){
    	return price;
    }
    
    public void setPrice(Long price){
    	this.price=price;
    }
    
    public String getPicLink(){
    	return picLink;
    }
    
    public void setPicLink(String picLink){
    	this.picLink=picLink;
    }
    
    public Long getNum(){
    	return num;
    }
    
    public void setNum(Long num){
    	this.num=num;
    }
    
    public int getStatus(){
    	return status;
    }
    
    public void setStatus(int status){
    	this.status=status;
    }
    
    public Long getSoldNum(){
    	return soldNum;
    }
    
    public void setSoldNum(Long soldNum){
    	this.soldNum=soldNum;
    }
    
    public String getSaleAttr(){
    	return saleAttr;
    }
    
    public void setSaleAttr(String saleAttr){
    	this.saleAttr=saleAttr;
    }
    
    public String getStockAttr(){
    	return stockAttr;
    }
    
    public void setStockAttr(String stockAttr){
    	this.stockAttr=stockAttr;
    }
    
	@Override
	public String getTableName() {
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
		this.id=id;
	}

	@Override
	public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.sellerUin, this.skuId);

            if (existdId <= 0L) {
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
		return "id";
	}
	
	
	
    @Override
	public String toString() {
		return "PPStock [sellerUin=" + sellerUin + ", itemCode=" + itemCode
				+ ", skuId=" + skuId + ", price=" + price + ", picLink="
				+ picLink + ", num=" + num + ", status=" + status
				+ ", soldNum=" + soldNum + ", saleAttr=" + saleAttr
				+ ", stockAttr=" + stockAttr + "]";
	}

	public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long sellerUin) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(sellerUin)));
    }
    
    @Transient
    static String EXIST_ID_QUERY = "select id from `ppstock_%s` where sellerUin  = ? and skuId = ?";
    
    public static long findExistId(Long sellerUin, Long skuId) {
        return dp.singleLongQuery(genShardQuery(EXIST_ID_QUERY, sellerUin), sellerUin, skuId);
    }

    @Transient
    static String insertSQL = "insert into `ppstock_%s`(`sellerUin`,`itemCode`,`skuId`,`price`,`picLink`,`num`,`status`,`soldNum`,`saleAttr`,`stockAttr`) values(?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(genShardQuery(insertSQL, sellerUin), this.sellerUin, this.itemCode, this.skuId,
                this.price, this.picLink, this.num, this.status, this.soldNum, this.saleAttr, this.stockAttr);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    @Transient
    static final String updateSQL = "update `ppstock_%s` set  `sellerUin` = ?, `itemCode` = ?, `skuId` = ?, `price` = ?, `picLink` = ?, `num` = ?, `status` = ?, `soldNum` = ?, `saleAttr` = ?, `stockAttr` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(genShardQuery(updateSQL, this.sellerUin), this.sellerUin, this.itemCode,
                this.skuId, this.price, this.picLink, this.num, this.status, this.soldNum,
                this.saleAttr, this.stockAttr, this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }
    
    public long getTableHashKey(Long hashKeyId) {
        return DBBuilder.genUserIdHashKey(hashKeyId);
    }
    
}
