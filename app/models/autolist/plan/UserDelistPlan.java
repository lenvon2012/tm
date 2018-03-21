package models.autolist.plan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.item.ItemPlay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.DateUtil;


@Entity(name=UserDelistPlan.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn",
})
public class UserDelistPlan extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserDelistPlan.class);

    @Transient
    public static final String TABLE_NAME = "user_delist_plan_"; 
    
    @Transient
    public static UserDelistPlan EMPTY = new UserDelistPlan();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name="userId")
    private Long userId;
    
    @Column(columnDefinition = " varchar(127) default ''")
    private String title;
    
    private int templateType = DelistTemplate.Default;//上下架模板类型
    public static class DelistTemplate {
        public static final int Default = 1; //符合一定条件的宝贝
        public static final int UserSelectItems = 2;//用户自定义宝贝
    }
    
    private int status = DelistPlanStatus.ON;
    public static class DelistPlanStatus {
        public static final int ON = 1;
        public static final int OFF = 2;//暂停
    }
    
    //在架或在仓库的条件
    private int itemStatusRule = DelistItemStatusRule.OnSaleItems; 
    public static class DelistItemStatusRule {
        public static final int OnSaleItems = 1;
        public static final int InStockItems = 2;
        public static final int AllItems = 4;
    }
    
    //有无销量的条件
    private int salesNumRule = DelistSalesNumRule.AllItems;
    public static class DelistSalesNumRule {
        public static final int AllItems = 1;
        public static final int HasSales = 2;
        public static final int NoSales = 4;
    }
    
    
    
    //要上下架的宝贝的淘宝类目ID，以逗号隔开
    @Column(columnDefinition = "varchar(2000) default ''")
    private String delistCateIds = AllCateIds; 
    //自定义的类目ID
    @Column(columnDefinition = "varchar(2000) default ''")
    private String selfCateIds = AllCateIds; 
  
    public static final String AllCateIds = "all";
    
    //是否按宝贝类目均匀分布
    private int delistConfig;
    
    public static class DelistPlanConfig {
        public static final int None = 0;
        public static final int AutoAddNewItem = 1;//更新数据时，自动添加新的宝贝，在卖家自定义的宝贝的时候，这个应该是要false的
        public static final int CategoryAverage = 2;//按类目平均分布
        public static final int DelistAllTheTime = 4;//每次都要由我们系统上下架
        public static final int FilterGoodSalesItem = 8;//排除销量前10的宝贝
    }
    
    
    //每小时比例分布
    @Column(columnDefinition = "varchar(2000) default ''")
    private String hourRates;
    
    @Column(columnDefinition = "varchar(2000) default ''")
    private String distriNums;
    
    @Column(columnDefinition = "text")
    private String selectNumIids;
    
    private long createTime;

    private long updateTime;


    /**
     * 卖家自己选择宝贝
     * @return
     */
    public boolean isUserSelectItemType() {
        if (this.templateType == DelistTemplate.UserSelectItems) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 满足一定条件的宝贝
     * @return
     */
    public boolean isRuleItemType() {
        if (this.templateType == DelistTemplate.Default) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 转成宝贝在架状态的值
     * @param itemStatusRule
     * @return
     */
    public static int transItemStatus(int itemStatusRule) {
        
        if (itemStatusRule == DelistItemStatusRule.OnSaleItems) {
            return ItemPlay.Status.ONSALE;
        } else if (itemStatusRule == DelistItemStatusRule.InStockItems) {
            return ItemPlay.Status.INSTOCK;
        } else {
            return 2;
        }
        
    }
    
    public boolean isPlanTurnOn() {
        if (this.status == DelistPlanStatus.ON) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isDelistAllTheTime() {
        if ((this.delistConfig & DelistPlanConfig.DelistAllTheTime) > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    
    public Long getPlanId() {
        return id;
    }
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTemplateType() {
        return templateType;
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    

    public int getItemStatusRule() {
        return itemStatusRule;
    }

    public void setItemStatusRule(int itemStatusRule) {
        this.itemStatusRule = itemStatusRule;
    }
    
    public boolean isDelistInstockItems() {
        if (this.itemStatusRule == DelistItemStatusRule.AllItems || this.itemStatusRule == DelistItemStatusRule.InStockItems) {
            return true;
        } else {
            return false;
        }
    }

    public int getSalesNumRule() {
        return salesNumRule;
    }

    public void setSalesNumRule(int salesNumRule) {
        this.salesNumRule = salesNumRule;
    }

    public String getDelistCateIds() {
        return delistCateIds;
    }

    public void setDelistCateIds(String delistCateIds) {
        this.delistCateIds = delistCateIds;
    }

    public String getSelfCateIds() {
        return selfCateIds;
    }

    public void setSelfCateIds(String selfCateIds) {
        this.selfCateIds = selfCateIds;
    }
    
    

    public int getDelistConfig() {
        return delistConfig;
    }

    public void setDelistConfig(int delistConfig) {
        this.delistConfig = delistConfig;
    }
    
    public String getSelectNumIids() {
        return selectNumIids;
    }

    public void setSelectNumIids(String selectNumIids) {
        this.selectNumIids = selectNumIids;
    }

    public void setAutoAddNewItem() {
        
        this.delistConfig = this.delistConfig | DelistPlanConfig.AutoAddNewItem;
    }
    
    public boolean isAutoAddNewItem() {
        if (this.isRuleItemType() == false) {
            return false;
        }
        if ((this.delistConfig & DelistPlanConfig.AutoAddNewItem) > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public void removeAutoAddNewItem() {
        this.delistConfig = this.delistConfig & (~DelistPlanConfig.AutoAddNewItem);
    }
    
    
    public void setFilterGoodSalesItem() {
        
        this.delistConfig = this.delistConfig | DelistPlanConfig.FilterGoodSalesItem;
    }
    
    public boolean isFilterGoodSalesItem() {
        
        if ((this.delistConfig & DelistPlanConfig.FilterGoodSalesItem) > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public void removeFilterGoodSalesItem() {
        this.delistConfig = this.delistConfig & (~DelistPlanConfig.FilterGoodSalesItem);
    }
    
    

    public String getHourRates() {
        return hourRates;
    }

    public void setHourRates(String hourRates) {
        this.hourRates = hourRates;
    }
    

    public String getDistriNums() {
        return distriNums;
    }

    public void setDistriNums(String distriNums) {
        this.distriNums = distriNums;
    }
    

    public UserDelistPlan() {
        
    }
    
    public UserDelistPlan(Long userId, String title) {
        super();
        this.userId = userId;
        this.title = title;
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
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static long findExistId(Long id, Long userId) {
        
        String existIdQuery = "select id from " + TABLE_NAME + " where  id = ? and userId = ? ";
        
        return dp.singleLongQuery(existIdQuery, id, userId);
    }
    
    public boolean rawInsert() {
        createTime = System.currentTimeMillis();
        updateTime = System.currentTimeMillis();
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`userId`,`title`,`templateType`,`status`," +
        		"`itemStatusRule`,`salesNumRule`,`delistCateIds`,`selfCateIds`," +
        		"`delistConfig`,`hourRates`,`distriNums`,`selectNumIids`," +
        		"`createTime`,`updateTime`) " +
        		" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        long id = dp.insert(insertSql, this.userId, this.title, this.templateType, this.status, 
                this.itemStatusRule, this.salesNumRule, this.delistCateIds, this.selfCateIds,
                this.delistConfig, this.hourRates, this.distriNums, this.selectNumIids,
                this.createTime, this.updateTime);

        if (id > 0L) {
            this.setId(id);
            return true;
        } else {
            log.error("Insert Fails.....[userId : ]" + this.userId);

            return false;
        }
    }

    public boolean rawUpdate() {
        
        updateTime = System.currentTimeMillis();
        
        String updateSql = "update `" + TABLE_NAME + "` set `title` = ?, `templateType` = ?, `status` = ?," +
        		" `itemStatusRule` = ?, `salesNumRule` = ?, `delistCateIds` = ?, `selfCateIds` = ?, " +
        		" `delistConfig` = ?, `hourRates` = ?, `distriNums` = ?, `selectNumIids` = ?," +
        		" `updateTime` = ? where `id` = ? and `userId` = ? ";
        
        long updateNum = dp.insert(updateSql, this.title, this.templateType, this.status, 
                this.itemStatusRule, this.salesNumRule, this.delistCateIds, this.selfCateIds, 
                this.delistConfig, this.hourRates, this.distriNums, this.selectNumIids,
                this.updateTime, this.id, this.userId);

        if (updateNum > 0L) {
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.id);

            return false;
        }
    }
    
    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.id, this.userId);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getIdName() {
        return "id";
    }
    
    
    public static UserDelistPlan findByPlanId(Long planId, Long userId) {
        String query = " select " + SelectAllProperties + " from " + TABLE_NAME + " where id = ? and userId = ? ";
        
        return new JDBCBuilder.JDBCExecutor<UserDelistPlan>(dp, query, planId, userId){

            @Override
            public UserDelistPlan doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseUserDelistPlan(rs);
                } else {
                    return null;
                }
            }
            
        }.call();
        
    }
    
    public static long countByUserId(Long userId) {
        String query = " select count(*) from " + TABLE_NAME + " where userId = ?";
        
        return dp.singleLongQuery(query, userId);
    }
    
    public static List<UserDelistPlan> findByUserId(Long userId) {
        String query = " select " + SelectAllProperties + " from " + TABLE_NAME + " where userId = ? ";
        
        return new JDBCBuilder.JDBCExecutor<List<UserDelistPlan>>(dp, query, userId){

            @Override
            public List<UserDelistPlan> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<UserDelistPlan> planList = new ArrayList<UserDelistPlan>();
                while (rs.next()) {
                    UserDelistPlan plan = parseUserDelistPlan(rs);
                    if (plan != null) {
                        planList.add(plan);
                    }
                } 
                
                return planList;
            }
            
        }.call();
        
    }
    
    public boolean rawDelete() {
        String deleteSql = "delete from " + TABLE_NAME + " where id = ? and userId = ?";
        
        long deleteNum = dp.update(deleteSql, this.id, this.userId);
        
        if (deleteNum > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    private static final String SelectAllProperties = " id,userId,title,templateType,status," +
    		"itemStatusRule,salesNumRule,delistCateIds,selfCateIds,delistConfig,hourRates,distriNums,selectNumIids," +
    		"createTime,updateTime ";
    
    private static UserDelistPlan parseUserDelistPlan(ResultSet rs) {
        try {
            
            UserDelistPlan delistPlan = new UserDelistPlan();
            delistPlan.setId(rs.getLong(1));
            delistPlan.setUserId(rs.getLong(2));
            delistPlan.setTitle(rs.getString(3));
            delistPlan.setTemplateType(rs.getInt(4));
            delistPlan.setStatus(rs.getInt(5));
            delistPlan.setItemStatusRule(rs.getInt(6));
            delistPlan.setSalesNumRule(rs.getInt(7));
            delistPlan.setDelistCateIds(rs.getString(8));
            delistPlan.setSelfCateIds(rs.getString(9));
            delistPlan.setDelistConfig(rs.getInt(10));
            delistPlan.setHourRates(rs.getString(11));
            delistPlan.setDistriNums(rs.getString(12));
            delistPlan.setSelectNumIids(rs.getString(13));
            delistPlan.setCreateTime(rs.getLong(14));
            delistPlan.setUpdateTime(rs.getLong(15));
            
            return delistPlan;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
    @JsonProperty
    public String getCreateTimeStr() {
        return DateUtil.formDateForLog(this.createTime);
    }
    
    @Override
    public String toString(){
    	return "[UserDelistPlan]:[delistCateIds:" + this.delistCateIds +" delistConfig:"+ delistConfig +" distriNums:" + distriNums +" hourRates:" +hourRates
    			+ " itemStatusRule:" + itemStatusRule + " salesNumRule:" + salesNumRule + " selectNumIids:"+ selectNumIids + " selfCateIds:"+selfCateIds
    			+" status:" + status + " templateType:" + templateType+"]";
    }
}
