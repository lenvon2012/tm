package models.associate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import jdbcexecutorwrapper.JDBCLongListExecutor;

import models.item.ItemPlay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import actions.AssociateAction;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import com.ciaosir.client.CommonUtils;
import dao.item.ItemDao;

/**
 * 关联模板计划
 * 
 * @author hyg 2014-4-3下午7:14:10
 */
@Entity(name = AssociatePlan.TABLE_NAME)
@JsonIgnoreProperties(value = { "tableName", "idColumn", "idName", "tableHashKey", "persistent", "entityId" })
public class AssociatePlan extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(AssociatePlan.class);

    @Transient
    public static final String TABLE_NAME = "associate_plan";

    @Transient
    public static AssociatePlan EMPTY = new AssociatePlan();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Transient
    private int count;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 卖家Id
     */
    @Column(columnDefinition="not null")
    private Long userId;

    /**
     * 宝贝列表
     */
    @Column(columnDefinition="not null")
    private String numIids;

    /**
     * 模板Id
     */
    @Column(columnDefinition="not null")
    private Long modelId;

    /**
     * 模板计划的类型 1：投放中，2：未投放，3：已删除
     */
    @Column(columnDefinition="not null")
    private int type;

    /**
     * 边框颜色
     */
    private String borderColor;

    /**
     * 字体颜色
     */
    private String fontColor;
    
    /**
     *  背景颜色
     */
    private String backgroundColor;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动价格
     */
    private double activityPrice;

    /**
     * 专柜价格
     */
    private double counterPrice;

    /**
     * 活动名称 template top
     */
    private String activityNameChinese;

    private String activityNameEnglish;

    /**
     * 计划宽度 与模板宽度不同，模板宽度是属于推荐宽度
     */
    private int planWidth;

    /**
     * 原价
     */
    private double originalPrice;
    
    /**
     * 活动时间
     */
    private int days;
    private int hours;
    private int minutes;

    public AssociatePlan() {
        super();
    }

    public AssociatePlan(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
    }

    public AssociatePlan(String planName, Long userId, String numIids, Long modelId, int type, String borderColor,
            String activityTitle, double activityPrice, double counterPrice, String activityNameChinese,
            String activityNameEnglish, int planWidth, double originalPrice,String fontColor,String backgroundColor,int days,int hours,int minutes) {
        this.planName = planName;
        this.userId = userId;
        this.numIids = numIids;
        this.modelId = modelId;
        this.type = type;
        this.borderColor = borderColor;
        this.activityTitle = activityTitle;
        this.activityPrice = activityPrice;
        this.counterPrice = counterPrice;
        this.activityNameChinese = activityNameChinese;
        this.activityNameEnglish = activityNameEnglish;
        this.planWidth = planWidth;
        this.originalPrice = originalPrice;
        this.fontColor = fontColor;
        this.backgroundColor = backgroundColor;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    /**
     * 获取宝贝id的列表
     * 
     * @return
     */
    public List<Long> getNumIdList() {
        List<Long> numIidLists = new ArrayList<Long>();
        if (this.numIids.isEmpty())
            return numIidLists;
        String numIidsStr[] = this.numIids.split("!@#");
        for (String numIid : numIidsStr) {
            numIidLists.add(Long.parseLong(numIid));
        }
        return numIidLists;
    }

    /**
     * 获取宝贝的标题
     * 
     * @return
     */
    public List<String> getTitles() {
        if (this.numIids.isEmpty()) {
            return null;
        }
        List<String> titles = new ArrayList<String>();
        List<Long> numIidsList = getNumIdList();
        List<ItemPlay> items = null;
        items = ItemDao.findByNumIids(userId, numIidsList);

        if (CommonUtils.isEmpty(items)) {
            return null;
        }
        for (Long numIid : numIidsList) {
            for (ItemPlay item : items) {
                if (item.getNumIid().equals(numIid)) {
                    titles.add(item.getTitle());
                }
            }
        }
        return titles;

    }

    /**
     * 获取销量
     * 
     * @return salesCounts
     */
    public List<Integer> getSalesCount() {
        if (this.numIids.isEmpty()) {
            return null;
        }
        List<Integer> salesCounts = new ArrayList<Integer>();
        List<Long> numIidsList = getNumIdList();
        List<ItemPlay> items = null;
        items = ItemDao.findByNumIids(userId, numIidsList);

        if (CommonUtils.isEmpty(items)) {
            return null;
        }
        for (Long numIid : numIidsList) {
            for (ItemPlay item : items) {
                if (item.getNumIid().equals(numIid)) {
                    salesCounts.add(item.getSalesCount());
                }
            }
        }
        return salesCounts;
    }

    /**
     * 单独增加一个宝贝到模板中
     * 
     * @param numIid
     */
    public void addNumIid(Long numIid) {
        this.numIids += numIids + numIid + "!@#";
    }

    /**
     * 批量增加宝贝到模板中
     * 
     * @param numIids
     */
    public void addNumIids(String numIids) {
        if (!numIids.isEmpty()) {
            String numIidsStr[] = numIids.split("!@#");
            for (String numIid : numIidsStr) {
                if (!this.numIids.contains(numIid)) {
                    this.numIids += numIid + "!@#";
                }
            }
        }
    }

    /**
     * 从模板中删除一个宝贝
     * 
     * @param numIid
     */
    public void deleteNumIid(Long numIid) {
        this.numIids = this.numIids.replace(numIid + "!@#", "");
    }

    /**
     * 查看哪些宝贝中关联了模板
     * 
     * @param userId
     *            买家Id
     * @return 被关联宝贝
     */
    public static Set<Long> findByUser(Long userId) {
        List<AssociatePlan> rps = AssociatePlan.nativeQuery("userId = ?", userId);
        if (CommonUtils.isEmpty(rps)) {
            return null;
        }
        Set<Long> associatedItems = new HashSet<Long>();
        for (AssociatePlan rp : rps) {
            List<Long> numIids = rp.getNumIdList();
            associatedItems.addAll(numIids);
        }
        return associatedItems;
    }

    /**
     * 查看 一个模板计划
     * 
     * @param userId
     * @param id
     * @return
     */
    public static AssociatePlan findByUserAndPlanId(Long userId, Long id) {
        AssociatePlan rps = AssociatePlan.singleQuery("id = ? and userId = ?", id, userId);
        
        if (rps == null) {
            return null;
        }
        return rps;
    }

    /**
     * 查看哪些宝贝中关联了这个模板
     * 
     * @param userId
     * @param modelId
     * @return
     */
    public static Set<Long> findByUserAndModelId(Long userId, Long modelId) {
        List<AssociatePlan> rps = AssociatePlan.nativeQuery("userId = ? and modelId = ?", userId, modelId);
        if (CommonUtils.isEmpty(rps)) {
            return null;
        }
        Set<Long> associateItems = new HashSet<Long>();
        for (AssociatePlan rp : rps) {
            List<Long> numIids = rp.getNumIdList();
            associateItems.addAll(numIids);
        }
        return associateItems;
    }


    public static String ASSOCIATE_PLAN_QUERY = "select id,planName,userId,numIids,modelId,type,borderColor,activityTitle,activityPrice,"
            + "counterPrice,activityNameChinese,activityNameEnglish,planwidth,originalPrice,fontColor,backgroundColor,days,hours,minutes from "
            + AssociatePlan.TABLE_NAME + " where ";

    public static List<AssociatePlan> nativeQuery(String query, Object... params) {
        return new JDBCExecutor<List<AssociatePlan>>(ASSOCIATE_PLAN_QUERY + query, params) {

            @Override
            public List<AssociatePlan> doWithResultSet(ResultSet rs) throws SQLException {
                final List<AssociatePlan> resultList = new ArrayList<AssociatePlan>();
                while (rs.next()) {
                    AssociatePlan plan = new AssociatePlan(rs.getString(2), rs.getLong(3), rs.getString(4),
                            rs.getLong(5), rs.getInt(6), rs.getString(7), rs.getString(8), rs.getDouble(9),
                            rs.getDouble(10), rs.getString(11), rs.getString(12), rs.getInt(13), rs.getDouble(14),rs.getString(15),rs.getString(16),
                            rs.getInt(17),rs.getInt(18),rs.getInt(19));
                    
                    plan.setId(rs.getLong(1));
                    resultList.add(plan);
                }
                return resultList;
            }
        }.call();
    }

    public static AssociatePlan singleQuery(String query, Object... params) {
        return new JDBCExecutor<AssociatePlan>(ASSOCIATE_PLAN_QUERY + query, params) {

            @Override
            public AssociatePlan doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    AssociatePlan plan = new AssociatePlan(rs.getString(2), rs.getLong(3), rs.getString(4),
                            rs.getLong(5), rs.getInt(6), rs.getString(7), rs.getString(8), rs.getDouble(9),
                            rs.getDouble(10), rs.getString(11), rs.getString(12), rs.getInt(13), rs.getDouble(14),rs.getString(15),rs.getString(16),
                            rs.getInt(17),rs.getInt(18),rs.getInt(19));
                    return plan;
                } else {
                    return null;
                }
            }
        }.call();
    }
    
    public static int getPlanCount(Long userId, int type) {

        String sql = "select count(*) from " + AssociatePlan.TABLE_NAME + " where userId = ?  and type = ?";
        
        int count = (int) dp.singleLongQuery(sql, userId, type);
        return count;
    }

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `associate_plan`(planName, userId,numIids,modelId,type,borderColor,activityTitle,activityPrice,counterPrice"
                        + ",activityNameChinese,activityNameEnglish,planWidth,originalPrice,fontColor,backgroundColor,days,hours,minutes) "
                        + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 
                        this.planName, this.userId, this.numIids,
                        this.modelId, this.type, this.borderColor, this.activityTitle, this.activityPrice,
                        this.counterPrice, this.activityNameChinese, this.activityNameEnglish, this.planWidth,
                        this.originalPrice, this.fontColor, this.backgroundColor, this.days, this.hours, this.minutes);
        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(
                "update `associate_plan` set `planName` = ?, `numIids` = ? ,`modelId` = ?,`type` = ? , "
                        + "`borderColor` = ?,`activityTitle` = ?, `activityPrice` = ?,"
                        + "`counterPrice` = ?,`activityNameChinese` = ?,`activityNameEnglish` = ?,"
                        + "`planwidth` = ?,`originalPrice` = ? ,`fontColor` = ? ,`backgroundColor` = ? ,`days` = ? ,`hours` = ?,`minutes` = ? " +
                        " where `id` = ? and `userId` = ? ", this.planName,
                            this.numIids, this.modelId, this.type, this.borderColor, this.activityTitle, this.activityPrice,
                                this.counterPrice, this.activityNameChinese, this.activityNameEnglish, this.planWidth,
                                    this.originalPrice, this.fontColor,this.backgroundColor,this.days,this.hours,this.minutes,this.id,this.userId);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);
            return false;
        }
    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and id = ?";

    private static long findExistId(Long id, Long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId, id);
    }
    

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.id, this.userId);
            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return "id";
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
        this.id = id;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
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

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public Double getActivityPrice() {
        return activityPrice;
    }

    public void setActivityPrice(Double activityPrice) {
        this.activityPrice = activityPrice;
    }

    public Double getCounterPrice() {
        return counterPrice;
    }

    public void setCounterPrice(Double counterPrice) {
        this.counterPrice = counterPrice;
    }

    public String getActivityNameChinese() {
        return activityNameChinese;
    }

    public void setActivityNameChinese(String activityNameChinese) {
        this.activityNameChinese = activityNameChinese;
    }

    public String getActivityNameEnglish() {
        return activityNameEnglish;
    }

    public void setActivityNameEnglish(String activityNameEnglish) {
        this.activityNameEnglish = activityNameEnglish;
    }

    public int getPlanWidth() {
        return planWidth;
    }

    public void setPlanWidth(int planWidth) {
        this.planWidth = planWidth;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setActivityPrice(double activityPrice) {
        this.activityPrice = activityPrice;
    }

    public void setCounterPrice(double counterPrice) {
        this.counterPrice = counterPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
}
