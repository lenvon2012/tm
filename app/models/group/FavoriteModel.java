package models.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

@Entity(name = FavoriteModel.TABLE_NAME)
@JsonIgnoreProperties(value = { "id", "tableName", "idColumn", "idName", "tableHashKey", "persistent", "entityId" })
public class FavoriteModel extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(FavoriteModel.class);
    
    @Transient
    public static final String TABLE_NAME = "group_FavoriteModel";
    
    @Transient
    public static FavoriteModel EMPTY = new FavoriteModel();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    private Long modelId;
    
    private Long userId;
    
    public FavoriteModel(){
        super();
    }
    
    public FavoriteModel(Long modelId,Long userId){
        this.modelId = modelId;
        this.userId =  userId;
    }
    
//    private static final String selectAllProperties = "modelId,userId";
    
    public static List<Long> getFavModels(Long userId,int offset,int limit){
        
        String sql = "select modelId from " + FavoriteModel.TABLE_NAME + " where userId = ? limit ? ,?"; 
        
        
        return new JDBCExecutor<List<Long>>(dp,sql,userId,offset,limit){
                
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                final List<Long> resultList = new ArrayList<Long>();
                while(rs.next()){
                    resultList.add(rs.getLong(1));
                }
                return resultList;
            }
        }.call();
    }
   
    
    public static int getFavoriteCount(Long userId){
        String sql = "select count(*) from " + FavoriteModel.TABLE_NAME + " where userId = ?";
        int count = (int)dp.singleLongQuery(sql, userId);
        return count;
    }
    
    public static boolean inStore(Long userId,Long modelId){
        String sql = "select count(*) from " + FavoriteModel.TABLE_NAME + " where userId = ? and modelId = ?";
        int count = (int)dp.singleLongQuery(sql, userId,modelId);
        boolean inStore = false;
        if(count > 0){
            inStore = true;
        }
        return inStore;
    }

    public boolean rawInsert() {
        long id = dp.insert("insert into `group_FavoriteModel`(`modelId`,`userId`) values(?,?)",this.modelId,this.userId);
        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[modelId : ]" + this.modelId  + "  [userId :]" + this.userId);
            return false;
        }
    }
    
    public boolean rawDelete(Long userId,Long modelId) {
        
        String deleteSql = "delete from " + FavoriteModel.TABLE_NAME + " where modelId = ? and userId = ?";
        
        long deleteNum = dp.update(deleteSql, modelId,userId);
        
        if (deleteNum > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
