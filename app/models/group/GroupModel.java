package models.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import transaction.JDBCBuilder.JDBCExecutor;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;

@Entity(name = GroupModel.TABLE_NAME)
@JsonIgnoreProperties(value = { "id", "tableName", "idColumn", "idName", "tableHashKey", "persistent", "entityId" })
public class GroupModel extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(GroupModel.class);
    
    @Transient
    public static final String TABLE_NAME = "group_Model";
    
    @Transient
    public static GroupModel EMPTY = new GroupModel();  
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Transient
    private boolean favorite;

    /**
     * 模板ID
     */
    private Long modelId;

    /**
     * 模板名称
     */
    private String modelName;

    /**
     * 模板样例图
     */
//    private String picPath;

    /**
     * 是否大图
     */
    private boolean isBig;
    
    /**
     * 宝贝图片尺寸
     */
    private int itemPicSize;

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
     * 模板宽度 可变
     */
    private String width;
    
    /**
     * 颜色
     */
    private String color;

    /**
     * 备注
     */
    private String comments;
    
    public GroupModel(){
        super();
    }
    
    public GroupModel(Long modelId,String modelName,boolean isBig,int itemPicSize,int rowNum,int columnNum,int maxNum,String width,String color,String comments){
        this.modelId = modelId;
        this.modelName = modelName;
        this.isBig = isBig;
        this.itemPicSize = itemPicSize;
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.maxNum = maxNum;
        this.width = width;
        this.color = color;
        this.comments = comments;
    }
    
    //包含是否已经收藏
    public GroupModel(Long modelId,String modelName,boolean isBig,int itemPicSize,int rowNum,int columnNum,int maxNum,String width,String color,String comments,boolean favorite){
        this.modelId = modelId;
        this.modelName = modelName;
        this.isBig = isBig;
        this.itemPicSize = itemPicSize;
        this.rowNum = rowNum;
        this.columnNum = columnNum;
        this.maxNum = maxNum;
        this.width = width;
        this.color = color;
        this.comments = comments;
        this.favorite = favorite;
    }
    
    private static final String selectAllProperties = "modelId,modelName,isBig,itemPicSize,rowNum,columnNum,maxNum,width,color,comments";
    
    public static List<GroupModel> getGroupModels(int offSet, int limit){
        return new JDBCExecutor<List<GroupModel>>(dp,"select " + selectAllProperties + " from "
                                                        + GroupModel.TABLE_NAME + " order by id desc " + " limit " + offSet + "," + limit ){
            @Override
            public List<GroupModel> doWithResultSet(ResultSet rs) throws SQLException {
                final List<GroupModel> resultList = new ArrayList<GroupModel>();
                while(rs.next()){
                    final GroupModel groupModel = new GroupModel(rs.getLong(1),rs.getString(2),rs.getBoolean(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),
                            rs.getInt(7),rs.getString(8),rs.getString(9),rs.getString(10));
                    resultList.add(groupModel);
                }
                return resultList;
            }
        }.call();
    }
    
    public static GroupModel getSingleGroupModel(Long modelId){
        return new JDBCExecutor<GroupModel>(dp,"select " + selectAllProperties + " from " + GroupModel.TABLE_NAME  + " where modelId = " + modelId ){
            
            GroupModel groupModel = null;
            @Override
            public GroupModel doWithResultSet(ResultSet rs) throws SQLException {
                while(rs.next()){
                    groupModel = new GroupModel(rs.getLong(1),rs.getString(2),rs.getBoolean(3),rs.getInt(4),rs.getInt(5),rs.getInt(6),
                            rs.getInt(7),rs.getString(8),rs.getString(9),rs.getString(10));
                }
                return groupModel;
            }
        }.call();
    }
    
    public static int getModelCount(){
        String sql = "select count(*) from " + GroupModel.TABLE_NAME ;
        int count = (int)dp.singleLongQuery(sql);
        return count;
        
    }
    
    public boolean rawInsert() {
        long id = dp.insert("insert into `group_Model` values(`modelId`,`modelName`,`isBig`,`itemPicSize`,`rowNum`,`columnNum`,`maxNum`,`width`,`color`,`comments`)"
                ,this.modelId,this.modelName,this.isBig,this.itemPicSize,this.columnNum,this.maxNum,this.width,this.color,this.comments);
        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[modelId : ]" + this.modelId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp.insert("update `group_model` set `modelId` = ?,`modelName` = ?,`isBig` = ?,`itemPicSize` = ?,`rowNum` = ?,`columnNum` = ?," +
        		"`maxNum` = ?,`width` = ?,`color` = ?,`comments` = ?",
        		this.modelId,this.modelName,this.isBig,this.itemPicSize,this.rowNum,this.columnNum,this.maxNum,this.width,this.color,this.comments);
        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for : [modelId : ]" + this.modelId);
            return false;
        }
    }
    
    private static String EXIST_ID_QUERY = "select modelId from " + TABLE_NAME + " where modelId = ?";
    
    private static long findExistId(Long modelId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, modelId);
    }
    
    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
    
    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public boolean isBig() {
        return isBig;
    }

    public void setBig(boolean isBig) {
        this.isBig = isBig;
    }

    public int getItemPicSize() {
        return itemPicSize;
    }

    public void setItemPicSize(int itemPicSize) {
        this.itemPicSize = itemPicSize;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(int columnNum) {
        this.columnNum = columnNum;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }
    
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return null;
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

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
