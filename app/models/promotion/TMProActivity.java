
package models.promotion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import models.ump.PromotionPlay.ItemPromoteType;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

import dao.item.ItemDao;

@Entity(name = TMProActivity.TABLE_NAME)
public class TMProActivity extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TMProActivity.class);
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm");
    
    @Transient
    public static final String TABLE_NAME = "activity";
    
    public static final TMProActivity EMPTY = new TMProActivity();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    public static class ActivityType {
        
        public static final int OldActivity = 0;
        
        public static final int Discount = 1;
        
        public static final int Manjiusong = 2;
        
        public static final int ShopMjs = 4;
        
        public static final int ShopDiscount = 8;
        
        public static final int NewDiscount = 16;
    }

    public static class Type {
        public static final String PRICE = "PRICE";

        public static final String DISCOUNT = "DISCOUNT";

        public static final String UMP_DISCOUNT = "SimpleDiscount";

        public static final String UMP_MJS = "ManJiuJian";
    }

    public static class ActivityStatus {
        public static final String ACTIVE = "ACTIVE";

        public static final String UNACTIVE = "UNACTIVE";

    }


    @Index(name = "userId")
    private Long userId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 活动开始时间
     */
    private Long activityStartTime;

    /**
     * 活动结束时间
     */
    private Long activityEndTime;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动描述
     */
    private String activityDescription;

    @Column(columnDefinition = "varchar(2045) default null")
    private String items;

    @Deprecated
    @Column(columnDefinition = "int default 0")
    private int itemCount;

    private String status;
    
    /*****************以下是新增的字段*********************/
    
    /**
     * 是原来的打折，还是新的打折，还是满就送
     */
    @Column(columnDefinition = "int default 0")
    private int activityType;
    
    /**
     * 满就送配置的json
     */
    @Column(columnDefinition = "varchar(4095) default null ")
    private String mjsParams;
    
    /**
     * 淘宝满就送活动的id
     */
    @Column(columnDefinition = "bigint default 0")
    private Long mjsActivityId;
    
    @Column(columnDefinition = "int default 0")
    private int buyLimit;
    
    @JsonIgnore
    @Column(columnDefinition = "varchar(4095) default null")
    private String tmplHtml;
    
    @Column(columnDefinition = "varchar(127) default null")
    private String remark;
    
    public static class TMActivityStatus {
        
        //更新活动的时候，有时需要更新所有promotion，有些可能会失败，失败后下次需要再次执行
        public static final int UpdatePromotonNotAllSuccess = 1;
        
    }
    
    @Column(columnDefinition = "int default 0")
    private int tmActivityStatus;
    
    
    @Column(columnDefinition = "bigint default 0")
    private long updateTs;
    
    // 买就送活动的模板序号，默认为第二套模板
    @Transient
    int mjsTmplIndex = 1;
    
    @Transient
    private ShopDiscountParam shopDiscountParam;

    public TMProActivity() {

    }

    //旧的，不用了
    @Deprecated
    public TMProActivity(Long userId, String title, String description,
            Long createTime, Long startTime, Long endTime, String status) {
        this.userId = userId;
        this.activityTitle = title;
        this.activityDescription = description;
        this.createTime = createTime;
        this.activityStartTime = startTime;
        this.activityEndTime = endTime;
        this.status = status;
    }
    
    public TMProActivity(Long userId, Long activityStartTime,
            Long activityEndTime, String activityTitle, 
            String activityDescription, String status, int activityType) {
        super();
        this.userId = userId;
        this.activityStartTime = activityStartTime;
        this.activityEndTime = activityEndTime;
        this.activityTitle = activityTitle;
        this.activityDescription = activityDescription;
        this.status = status;
        this.activityType = activityType;
    }
    
    public TMProActivity(Long userId, Long activityStartTime,
            Long activityEndTime, String activityTitle, 
            String activityDescription, String status, int activityType, Long disActivityId) {
        super();
        this.userId = userId;
        this.activityStartTime = activityStartTime;
        this.activityEndTime = activityEndTime;
        this.activityTitle = activityTitle;
        this.activityDescription = activityDescription;
        this.status = status;
        this.activityType = activityType;
        if(isNewDiscountActivity()) {
        	this.mjsActivityId = disActivityId;
        }
    }
    
    public void updateActivityParams(long startTime, long endTime, String title, 
            String description) {
        
        this.activityStartTime = startTime;
        this.activityEndTime = endTime;
        this.activityTitle = title;
        this.activityDescription = description;
        
        if (endTime > System.currentTimeMillis()) {
            this.status = ActivityStatus.ACTIVE;
        } else {
            this.status = ActivityStatus.UNACTIVE;
        }
        
    }
    
    public void setUpdatePromotonNotAllSuccess() {
        
        this.tmActivityStatus = this.tmActivityStatus | TMActivityStatus.UpdatePromotonNotAllSuccess;
        
    }
    
    public void removeUpdatePromotonNotAllSuccess() {
        this.tmActivityStatus = this.tmActivityStatus & (~TMActivityStatus.UpdatePromotonNotAllSuccess);
    }
    
    public boolean isUpdatePromotonNotAllSuccess() {
        return (this.tmActivityStatus & TMActivityStatus.UpdatePromotonNotAllSuccess) > 0;
    }

    public boolean isOldActivity() {
        
        if (activityType <= 0) {
            return true;
        } else {
            return false;
        }
        
    }
    
    public boolean isDiscountActivity() {
        if (activityType == ActivityType.Discount) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isMjsActivity() {
        if (activityType == ActivityType.Manjiusong) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isShopMjsActivity() {
        if (activityType == ActivityType.ShopMjs) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isShopDiscountActivity() {
        if (activityType == ActivityType.ShopDiscount) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isNewDiscountActivity() {
        if (activityType == ActivityType.NewDiscount) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isNowActive() {
        if (ActivityStatus.ACTIVE.equals(status) == false) {
            return false;
        } else {
            return true;
        }
    }
    
    public String getStartTimeStr() {
        if (this.activityStartTime != null) {
            return sdf.format(new Date(activityStartTime));
        } else {
            return "-";
        }
    }
    
    public String getEndTimeStr() {
        if (this.activityEndTime != null) {
            return sdf.format(new Date(activityEndTime));
        } else {
            return "-";
        }
    }
    
    public String getCreateTimeString() {
        if (this.createTime != null) {
            return CommonUtils.TimeToString(this.createTime);
        } else {
            return "-";
        }
    }

    public String getActivityStartTimeString() {
        if (this.activityStartTime != null) {
            return CommonUtils.TimeToString(this.activityStartTime);
        } else {
            return "-";
        }
    }

    public String getActivityEndTimeString() {
        if (this.activityEndTime != null) {
            return CommonUtils.TimeToString(this.activityEndTime);
        } else {
            return "-";
        }
    }

    public ShopDiscountParam getShopDiscountParam() {
        if (isShopDiscountActivity() == false) {
            return null;
        }
        
        if (shopDiscountParam != null) {
            return shopDiscountParam;
        }
        
        if (StringUtils.isEmpty(mjsParams)) {
            return null;
        }
        
        shopDiscountParam = JsonUtil.toObject(mjsParams, ShopDiscountParam.class);
        if (shopDiscountParam == null) {
            log.error("fail to parse json for shop discount param: " + mjsParams);
        }
        
        return shopDiscountParam;
    }
    
    public long getActivityExecutedMillis() {
        long nowTime = System.currentTimeMillis();
        long executedMills = nowTime - activityStartTime;
        
        return executedMills;
    }
    
    public long getActivityLeftMillis() {
        long nowTime = System.currentTimeMillis();
        long leftMillis = activityEndTime - nowTime;
        
        return leftMillis;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getActivityStartTime() {
        return activityStartTime;
    }

    public void setActivityStartTime(Long activityStartTime) {
        this.activityStartTime = activityStartTime;
    }

    public Long getActivityEndTime() {
        return activityEndTime;
    }

    public void setActivityEndTime(Long activityEndTime) {
        this.activityEndTime = activityEndTime;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public String getMjsParams() {
        return mjsParams;
    }

    public void setMjsParams(String mjsParams) {
        this.mjsParams = mjsParams;
    }

    public String getTmplHtml() {
		return tmplHtml;
	}

	public void setTmplHtml(String tmplHtml) {
		this.tmplHtml = tmplHtml;
	}

	public int getBuyLimit() {
        return buyLimit;
    }

    public void setBuyLimit(int buyLimit) {
        this.buyLimit = buyLimit;
    }

    public int getTmActivityStatus() {
        return tmActivityStatus;
    }

    public void setTmActivityStatus(int tmActivityStatus) {
        this.tmActivityStatus = tmActivityStatus;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTMActivityId() {
        return this.id;
    }

    public Long getMjsActivityId() {
        return mjsActivityId;
    }

    public void setMjsActivityId(Long mjsActivityId) {
        this.mjsActivityId = mjsActivityId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItems() {
        return items;
    }

    public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getMjsTmplIndex() {
	    if (StringUtils.isEmpty(tmplHtml)) {
	        return 0;
	    }
	    
		if(this.tmplHtml.indexOf("index=\"0\"") >= 0) {
			return 0;
		} else if(this.tmplHtml.indexOf("index=\"1\"") >= 0) {
			return 1;
		} else if(this.tmplHtml.indexOf("index=\"2\"") >= 0) {
			return 2;
		} else if(this.tmplHtml.indexOf("index=\"3\"") >= 0) {
			return 3;
		} else {
			return 1;
		}
	}

	public void setMjsTmplIndex(int mjsTmplIndex) {
		this.mjsTmplIndex = mjsTmplIndex;
	}

	//旧的，不用了
    @Deprecated
    public void addItem(String item) {
        if (this.items == null) {
            this.items = item;
        }
        else {
            this.items += "," + item;
        }
        this.itemCount++;
    }

    
    public void addMjsItemNumIid(Long numIid) {
        if (numIid == null || numIid <= 0L) {
            return;
        }

        List<Long> numIidList = fetchItemNumIidList();
        if (numIidList.contains(numIid)) {
            return;
        }
         
        if (StringUtils.isEmpty(items)) {
            items = numIid + "";
        } else {
            items = items + "," + numIid;
        }
    }
    
    public void removeMjsItemNumIid(Long numIid) {
        
        List<Long> numIidList = fetchItemNumIidList();
        if(numIidList.contains(numIid)) {
        	numIidList.remove(numIid);
        }
        items = StringUtils.join(numIidList, ",");
        
        return;
    }
    
    public List<Long> fetchItemNumIidList() {
        if (StringUtils.isEmpty(items)) {
            return new ArrayList<Long>();
        }
        String[] numIidArr = items.split(",");
        List<Long> numIidList = new ArrayList<Long>();
        for (String numIidStr : numIidArr) {
            Long tempId = NumberUtil.parserLong(numIidStr, 0L);
            if (tempId == null || tempId <= 0L) {
                continue;
            }
            if (numIidList.contains(tempId)) {
                continue;
            }
            numIidList.add(tempId);
        }
        return numIidList;
    }
    
    public Set<Long> fetchItemNumIidSet() {
        if (StringUtils.isEmpty(items)) {
            return new HashSet<Long>();
        }
        String[] numIidArr = items.split(",");
        Set<Long> numIidList = new HashSet<Long>();
        for (String numIidStr : numIidArr) {
            Long tempId = NumberUtil.parserLong(numIidStr, 0L);
            if (tempId == null || tempId <= 0L) {
                continue;
            }
            if (numIidList.contains(tempId)) {
                continue;
            }
            numIidList.add(tempId);
        }
        return numIidList;
    }
    
    public void setItems(String items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setUnActiveStatusAndEndTime() {
        this.status = ActivityStatus.UNACTIVE;
        this.activityEndTime = System.currentTimeMillis();
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
    public String getIdName() {
        return "id";
    }

    
    public static String genShardQuery(String query, Long userId) {
        query = ItemDao.genShardQuery(query, userId);
        
        return query;
    }

    public static long findExistId(Long userId, Long id) {
        
        String query = "select id from " + TABLE_NAME + "%s where userId = ? and id = ? ";
        
        query = genShardQuery(query, userId);
        
        return dp.singleLongQuery(query, userId, id);
        
    }
    

    public boolean rawInsert() {
        
        String insertSQL = "insert into `" + TABLE_NAME + "%s`(`userId`," 
        		+ "`activityStartTime`,`activityEndTime`,"
                + "`activityTitle`,`activityDescription`,`items`,`status`,"
                + "`activityType`,`mjsParams`,`mjsActivityId`,`buyLimit`,`tmActivityStatus`,"
                + "`createTime`,`updateTs`,`tmplHtml`,`remark`) " 
                + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        insertSQL = genShardQuery(insertSQL, userId);
        
        this.createTime = System.currentTimeMillis();
        this.updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, this.userId, 
                this.activityStartTime, this.activityEndTime, 
                this.activityTitle, this.activityDescription, this.items, this.status,
                this.activityType, this.mjsParams, this.mjsActivityId, this.buyLimit, this.tmActivityStatus,
                this.createTime, this.updateTs, this.tmplHtml, this.remark);

        if (id > 0L) {
            this.setId(id);
            return true;
        } else {
            return false;
        }

    }



    public boolean rawUpdate() {

        String updateSQL = "update `" + TABLE_NAME + "%s` set `userId` = ?, "
                + "`activityStartTime` = ?, `activityEndTime` = ?, "
                + "`activityTitle` = ?, `activityDescription` = ?, `items` = ?, `status` = ?, " 
                + "`activityType` = ?, `mjsParams` = ?, `mjsActivityId` = ?, " 
                + "`buyLimit` = ?, `tmActivityStatus` = ?, "
                + "`updateTs` = ?, `tmplHtml` = ?, `remark` = ? " 
                + " where `id` = ? ";

        updateSQL = genShardQuery(updateSQL, userId);
        
        this.updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(updateSQL, this.userId,
                this.activityStartTime, this.activityEndTime,
                this.activityTitle, this.activityDescription, this.items, this.status,
                this.activityType, this.mjsParams, this.mjsActivityId, 
                this.buyLimit, this.tmActivityStatus,
                this.updateTs, this.tmplHtml, this.remark, 
                this.getId());

        return updateNum > 0;
    }

    public boolean rawDelete() {
        
        String deleteSql = "delete from " + TABLE_NAME + "%s where userId = ? and id = ? ";
        
        deleteSql = genShardQuery(deleteSql, userId);
        
        long deleteNum = dp.update(deleteSql, userId, this.id);
        
        return true;
    }
    
    
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.id);

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

    
    public static TMProActivity findByActivityId(Long userId, Long activityId) {
        
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + "%s " +
        		" where userId = ? and id = ? ";
        
        query = genShardQuery(query, userId);
        
        
        return new JDBCExecutor<TMProActivity>(dp, query, userId, activityId) {
            @Override
            public TMProActivity doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parseTMProActivity(rs);
                } else {
                    return null;
                }
            }
        }.call();
        
    }
    
    public static List<TMProActivity> findOnActiveActivitys(Long userId) {

        final String status = ActivityStatus.ACTIVE;

        return findActivitysByStatus(userId, status);

    }
    
    public static List<TMProActivity> findUnActiveActivitys(Long userId) {

        final String status = ActivityStatus.UNACTIVE;

        return findActivitysByStatus(userId, status);

    }
    
    private static List<TMProActivity> findActivitysByStatus(final Long userId, final String status) {

        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + "%s " +
                " where userId = ? and status = ? order by activityStartTime desc";
        
        query = genShardQuery(query, userId);

        return new JDBCExecutor<List<TMProActivity>>(dp, query, userId, status) {
            @Override
            public List<TMProActivity> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMProActivity> activityList = new ArrayList<TMProActivity>();
                while (rs.next()) {
                    TMProActivity activity = parseTMProActivity(rs);
                    if (activity != null)
                        activityList.add(activity);
                }
                return activityList;
            }
        }.call();

    }
    
    public static List<TMProActivity> findShopMjsActivitysOn(final Long userId) {

        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + "%s " +
                " where userId = ? and status = ? and activityType = ? order by activityStartTime desc";
        
        query = genShardQuery(query, userId);

        return new JDBCExecutor<List<TMProActivity>>(dp, query, userId,
        		TMProActivity.ActivityStatus.ACTIVE, TMProActivity.ActivityType.ShopMjs) {
            @Override
            public List<TMProActivity> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMProActivity> activityList = new ArrayList<TMProActivity>();
                while (rs.next()) {
                    TMProActivity activity = parseTMProActivity(rs);
                    if (activity != null)
                        activityList.add(activity);
                }
                return activityList;
            }
        }.call();

    }

    public static Long checkInterva = DateUtil.ONE_HOUR;
    public static List<TMProActivity> findEndMjsActivitys(int index, Long now) {

        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + index +
                " where activityEndTime < ? and activityEndTime > ? and (activityType = 2 or activityType = 4)" +
                " and status = 'ACTIVE'";

        return new JDBCExecutor<List<TMProActivity>>(dp, query, now + checkInterva, now) {
            @Override
            public List<TMProActivity> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMProActivity> activityList = new ArrayList<TMProActivity>();
                while (rs.next()) {
                    TMProActivity activity = parseTMProActivity(rs);
                    if (activity != null)
                        activityList.add(activity);
                }
                return activityList;
            }
        }.call();

    }
    
    public static long countOnActiveActivity(Long userId) {
        
        long count = countActivityByStatus(userId, ActivityStatus.ACTIVE);

        return count;

    }
    
    public static long countUnActiveActivity(Long userId) {
        
        long count = countActivityByStatus(userId, ActivityStatus.UNACTIVE);

        return count;

    }
    
    private static long countActivityByStatus(Long userId, final String status) {
        
        String countSql = " select count(*) from " + TABLE_NAME + "%s where userId = ? and status = ? ";
        
        countSql = genShardQuery(countSql, userId);
        
        long count = dp.singleLongQuery(countSql, userId, status);

        return count;

    }
    
    
    public static boolean deleteActivityById(Long userId, Long activityId) {
        
        String deleteSql = " delete from " + TABLE_NAME + "%s where userId = ? and id = ? ";
        
        deleteSql = genShardQuery(deleteSql, userId);
        
        long deleteNum = dp.update(deleteSql, userId, activityId);
        
        
        return true;
    }
    
    
    private static final String SelectAllProperty = " id,userId," 
                + "activityStartTime,activityEndTime,"
                + "activityTitle,activityDescription,items,status,"
                + "activityType,mjsParams,mjsActivityId,buyLimit,tmActivityStatus," 
                + "createTime,updateTs,tmplHtml,remark ";
    
    
    private static TMProActivity parseTMProActivity(ResultSet rs) {
        try {
            
            TMProActivity activity = new TMProActivity();
            activity.setId(rs.getLong(1));
            activity.setUserId(rs.getLong(2));
            activity.setActivityStartTime(rs.getLong(3));
            activity.setActivityEndTime(rs.getLong(4));
            activity.setActivityTitle(rs.getString(5));
            activity.setActivityDescription(rs.getString(6));
            activity.setItems(rs.getString(7));
            activity.setStatus(rs.getString(8));
            activity.setActivityType(rs.getInt(9));
            activity.setMjsParams(rs.getString(10));
            activity.setMjsActivityId(rs.getLong(11));
            activity.setBuyLimit(rs.getInt(12));
            activity.setTmActivityStatus(rs.getInt(13));
            activity.setCreateTime(rs.getLong(14));
            activity.setUpdateTs(rs.getLong(15));
            activity.setTmplHtml(rs.getString(16));
            activity.setRemark(rs.getString(17));
            
            return activity;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
    public static class ShopDiscountParam {
        
        private ItemPromoteType promotionType;
        
        private long discountRate;
        
        private long decreaseAmount;

        
        
        public ShopDiscountParam() {
            super();
        }

        public ShopDiscountParam(ItemPromoteType promotionType,
                long discountRate, long decreaseAmount) {
            super();
            this.promotionType = promotionType;
            this.discountRate = discountRate;
            this.decreaseAmount = decreaseAmount;
        }

        public ItemPromoteType getPromotionType() {
            return promotionType;
        }

        public void setPromotionType(ItemPromoteType promotionType) {
            this.promotionType = promotionType;
        }

        public long getDiscountRate() {
            return discountRate;
        }

        public void setDiscountRate(long discountRate) {
            this.discountRate = discountRate;
        }

        public long getDecreaseAmount() {
            return decreaseAmount;
        }

        public void setDecreaseAmount(long decreaseAmount) {
            this.decreaseAmount = decreaseAmount;
        }
        
        
        
    }
    
    
}
