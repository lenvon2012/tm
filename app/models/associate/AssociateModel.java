package models.associate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.user.User;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Columns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.AssociateAction;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * 关联模板的model
 * 
 * @author hyg 2014-4-11上午11:19:45
 */
/**
 * @author hyg
 * 2014-5-12下午7:31:26
 */
@Entity(name = AssociateModel.TABLE_NAME)
@JsonIgnoreProperties(value = { "id", "tableName", "idColumn", "idName", "tableHashKey", "persistent", "entityId" })
public class AssociateModel extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(AssociateModel.class);

    @Transient
    public static final String TABLE_NAME = "associate_model";

    @Transient
    public static AssociateModel EMPTY = new AssociateModel();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    /**
     * 模板ID
     */
    @Column(columnDefinition="unique not null")
    private Long modelId;

    /**
     * 模板名称
     */
    private String modelName;

    /**
     * 模板样例图
     */
    private String picPath;

    /**
     * 是否大图
     */
    private boolean isBig;

    /**
     * 模板行数
     */
    private int rowNum;

    /**
     * 模板列数
     */
    private int columnNum;

    /**
     * 模板最大宝贝数
     */
    private int maxNum;

    /**
     * 模板宽度==推荐宽度
     */
    private int width;

    /**
     * 关联模板的类型：1：关联模板 2：团购模板 3：搭配套餐 4：活动海报
     */
    private int type;
    
    /**
     * 可选的宽度
     */
    private String customWidth;
    
    /**
     * 边框颜色
     */
    private String borderColor;

    /**
     * 字体颜色
     */
    private String fontColor;
    
    /**
     * 背景
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
     * 原价
     */
    private double originalPrice;

    /**
     * 活动名称 template top
     */
    private String activityNameChinese;

    private String activityNameEnglish;
    
    /**
     * 活动时间
     */
    private int days;
    private int hours;
    private int minutes;
    
    /**
     * 备注
     */
    private String comments;
    
    public AssociateModel() {
        super();
    }
    
    public AssociateModel(Long modelId, String modelName, String picPath, boolean isBig, int rowNum, int columnNum,
            int maxNum, int width, int type, String customWidth,String comments){
        this.modelId = modelId;
        this.modelName = modelName;
        this.picPath = picPath;
        this.isBig = isBig;
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.maxNum = maxNum;
        this.width = width;
        this.type = type;
        this.customWidth = customWidth;
        this.comments = comments;
    }

    public AssociateModel(Long modelId, String modelName, String picPath, boolean isBig, int rowNum, int columnNum,
            int maxNum, int width, int type, String customWidth, String borderColor, String fontColor,
            String activityTitle, double activityPrice, double counterPrice, double originalPrice,
            String activityNameChinese, String activityNameEnglish,String backgroundColor,int days,int hours,int minutes) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.picPath = picPath;
        this.isBig = isBig;
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.maxNum = maxNum;
        this.width = width;
        this.type = type;
        this.customWidth = customWidth;
        this.borderColor = borderColor;
        this.fontColor = fontColor;
        this.activityTitle = activityTitle;
        this.activityPrice = activityPrice;
        this.counterPrice = counterPrice;
        this.originalPrice = originalPrice;
        this.activityNameChinese = activityNameChinese;
        this.activityNameEnglish = activityNameEnglish;
        this.backgroundColor = backgroundColor;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public boolean rawInsert() {
        long id = dp
                .insert("insert into `associate_model`(`modelId`, `modelName`, `picPath`, `isBig`,`rowNum`,`columnNum`,`maxNum`,`width`,`type`,`customWidth`,"
                        + "`borderColor`,`fontColor`,`activityTitle`,`activityPrice`,`counterPrice`,`originalPrice`,`activityNameChinese`,"
                        + "`activityNameEnglish`,`backgroundColor`,`days`,`hours`,`minutes`) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        this.modelId, this.modelName, this.picPath, this.isBig, this.rowNum, this.columnNum,
                        this.maxNum, this.width, this.type, this.customWidth, this.borderColor, this.fontColor,
                        this.activityTitle, this.activityPrice, this.counterPrice, this.originalPrice,
                        this.activityNameChinese, this.activityNameEnglish, this.backgroundColor, this.days,
                        this.hours, this.minutes);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[modelId : ]" + this.modelId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `associate_model` set `modelId` = ?, `modelName` = ?, `picPath` = ?,`isBig` = ?, `rowNum` = ? , `columnNum` = ?, "
                        + " `maxNum` = ?, `width` = ?, `type` = ? ,`customWidth` = ? ,`borderColor` = ?,`fontColor` = ?,`activityTitle` = ?,"
                        + "`activityPrice` = ?,`counterPrice` = ?,`originalPrice` = ?,`activityNameChinese` = ?,`activityNameEnglish` = ?,`backgroundColor` = ?,"
                        + "`days` = ?,`hours` = ?,`minutes` = ? where `modelId` = ? ", this.modelId, this.modelName,
                        this.picPath, this.isBig, this.rowNum, this.columnNum, this.maxNum, this.width, this.type,
                        this.customWidth, this.borderColor, this.fontColor, this.activityTitle, this.activityPrice,
                        this.counterPrice, this.originalPrice, this.activityNameChinese, this.activityNameEnglish,
                        this.backgroundColor, this.days, this.hours, this.minutes, this.modelId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for : [modelId : ]" + this.modelId);
            return false;
        }
    }
    
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.modelId);
            // if (existdId != 0)
            // log.info("find existed Id: " + existdId);

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

    private static final String SelectAllProperties = "modelId, modelName, picPath, isBig, rowNum, columnNum, maxNum, width, type, customWidth, "
            + "borderColor,  fontColor,activityTitle,  activityPrice,  counterPrice,  originalPrice,activityNameChinese,  activityNameEnglish, backgroundColor," +
            "days,hours,minutes";
    
    private static final String SelectPartProperties = "modelId, modelName, picPath, isBig, rowNum, columnNum, maxNum, width, type, customWidth,comments";

    public static List<AssociateModel> findModels(int width, int maxNum, int type, int offset, int limit) {
        
        if(maxNum == AssociateAction.ZERO)
        {
            return new JDBCExecutor<List<AssociateModel>>(dp, "select  " + SelectPartProperties + " from " + TABLE_NAME
                    + " where width = " + width + " and type =" + type + " order by id desc " + " limit " + offset + "," + limit) {

                @Override
                public List<AssociateModel> doWithResultSet(ResultSet rs) throws SQLException {
                    final List<AssociateModel> resulteList = new ArrayList<AssociateModel>();
                    while (rs.next()) {
                        AssociateModel model = new AssociateModel(rs.getLong(1), rs.getString(2), rs.getString(3),
                                rs.getBoolean(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9),rs.getString(10),rs.getString(11));
                        resulteList.add(model);
                    }
                    return resulteList;
                }
            }.call();
        }
        else{
            return new JDBCExecutor<List<AssociateModel>>(dp, "select  " + SelectPartProperties + " from " + TABLE_NAME
                    + " where width = " + width + " and maxNum =" + maxNum + " and type =" + type + " order by id desc " + " limit " + offset + "," + limit) {

                @Override
                public List<AssociateModel> doWithResultSet(ResultSet rs) throws SQLException {
                    final List<AssociateModel> resulteList = new ArrayList<AssociateModel>();
                    while (rs.next()) {
                        AssociateModel model = new AssociateModel(rs.getLong(1), rs.getString(2), rs.getString(3),
                                rs.getBoolean(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9),rs.getString(10),rs.getString(11));
                        resulteList.add(model);
                    }
                    return resulteList;
                }
            }.call();
            
        }
       
    }
    
    /**
     * String borderColor, String fontColor,
     * String activityTitle, double activityPrice, double counterPrice, double originalPrice,
     * String activityNameChinese, String activityNameEnglish
     * 
     */
    public static AssociateModel findModelBymodelId(Long modelId) {
        return new JDBCExecutor<AssociateModel>(dp, "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where modelId = " + modelId) {

            @Override
            public AssociateModel doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    AssociateModel model = new AssociateModel(rs.getLong(1), rs.getString(2), rs.getString(3),
                            rs.getBoolean(4), rs.getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9),
                            rs.getString(10), rs.getString(11), rs.getString(12), rs.getString(13), rs.getDouble(14),
                            rs.getDouble(15), rs.getDouble(16), rs.getString(17), rs.getString(18),rs.getString(19),
                            rs.getInt(20),rs.getInt(21),rs.getInt(22));
                    model.setId(rs.getLong(1));
                    return model;
                } else {
                    return null;
                }
            }

        }.call();
    }
    
    public static int getModelCount( int width, int maxNum, int type){
        
        String sql = "select count(*) from " + AssociateModel.TABLE_NAME + " where width = ?  and type = ?";

        int count = AssociateAction.ZERO;
        
        if(maxNum == AssociateAction.ZERO){
            count = (int) dp.singleLongQuery(sql, width,type);
        }
        else{ 
            sql = sql + " and maxNum = ?";
            count = (int) dp.singleLongQuery(sql, width,type,maxNum);
        }
        return count;
    }
    

    public static String EXIST_ID_QUERY = "select modelId from " + TABLE_NAME + " where modelId = ?";

    /**
     * 确保模板存在
     * 
     * @param modelId
     * @return
     */
    private static long findExistId(Long modelId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, modelId);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return modelId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "modelId";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "modelId";
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

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.modelId = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
   
    public Long getModelId() {
        return this.modelId;
    }
    
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public void setModelName(String name) {
        this.modelName = name;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setBig(boolean isBig) {
        this.isBig = isBig;
    }

    public boolean getIsBig() {
        return this.isBig;
    }

    public void setRowNum(int row) {
        this.rowNum = row;
    }

    public int getRowNum() {
        return this.rowNum;
    }

    public void setColumnNum(int column) {
        this.columnNum = column;
    }

    public int getColumnNum() {
        return this.columnNum;
    }

    public void setPicPath(String path) {
        this.picPath = path;
    }

    public String getPicPath() {
        return this.picPath;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public String getCustomWidth() {
        return customWidth;
    }

    public void setCustomWidth(String customWidth) {
        this.customWidth = customWidth;
    }
    
    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public double getActivityPrice() {
        return activityPrice;
    }

    public void setActivityPrice(double activityPrice) {
        this.activityPrice = activityPrice;
    }

    public double getCounterPrice() {
        return counterPrice;
    }

    public void setCounterPrice(double counterPrice) {
        this.counterPrice = counterPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
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

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
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
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
    
    
}
