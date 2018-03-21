package models.popularized;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name=FenXiangToken.TABLE_NAME)
public class FenXiangToken extends Model implements PolicySQLGenerator{
    
    private static final Logger log=LoggerFactory.getLogger(FenXiangToken.class);
    
    public static final String TABLE_NAME ="fenxiangtoken";

    private Long UserId;
    
    private String Sweibo_Token;
    
    private String qq_Token;
    
    private String openid;
    
    private String openkey;
    
    private int type;
    
    public static class Type {
        public static final int isSweiboBD = 1;
        public static final int isqqBD = 2;
    }
    
    public FenXiangToken(){
        
    }
    
    public FenXiangToken(Long UserId){
        this.UserId=UserId;
    }
    
    public FenXiangToken(Long id,Long userId,String Sweibo_Token,String qq_Token,int type,String openid,String openkey){
        this.id=id;
        this.UserId=userId;
        this.Sweibo_Token=Sweibo_Token;
        this.qq_Token=qq_Token;
        this.type=type;
        this.openid=openid;
        this.openkey=openkey;
    }
    
    public Long getUserId() {
        return UserId;
    }

    public void setUserId(Long userId) {
        UserId = userId;
    }

    public String getSweibo_Token() {
        return Sweibo_Token;
    }

    public void setSweibo_Token(String sweibo_Token) {
        Sweibo_Token = sweibo_Token;
    }

    public String getQq_Token() {
        return qq_Token;
    }

    public void setQq_Token(String qq_Token) {
        this.qq_Token = qq_Token;
    }

    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getOpenkey() {
        return openkey;
    }

    public void setOpenkey(String openkey) {
        this.openkey = openkey;
    }

    public boolean isSweiboBD() {
        return (this.type & Type.isSweiboBD) > 0;
    }

    public void setSweiboBD(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.isSweiboBD;
        } else {
            this.type &= (~Type.isSweiboBD);
        }
        log.error(" current type:" + this.type);
    }
    
    public boolean isqqBD() {
        return (this.type & Type.isqqBD) > 0;
    }

    public void setqqBD(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.isqqBD;
        } else {
            this.type &= (~Type.isqqBD);
        }
        log.error(" current type:" + this.type);
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
      this.id=id;
        
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.UserId);

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
        return "id";
    }



    @Override
    public String toString() {
        return "FenXiangToken [UserId=" + UserId + ", Sweibo_Token="
                + Sweibo_Token + ", qq_Token=" + qq_Token + ", openid="
                + openid + ", openkey=" + openkey + ", type=" + type + "]";
    }



    @Transient
    static String EXIST_ID_QUERY = "select id from `fenxiangtoken` where UserId  = ?";
    
    public static long findExistId(Long UserId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, UserId);
    }
    
    @Transient
    static String insertSQL = "insert into `fenxiangtoken`(`UserId`,`Sweibo_Token`,`qq_Token`,`type`,`openid`,`openkey`) values(?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.UserId, this.Sweibo_Token, this.qq_Token,this.type,this.openid,this.openkey);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    @Transient
    static final String updateSQL = "update `fenxiangtoken` set  `UserId` = ?, `Sweibo_Token` = ?, `qq_Token` = ?, `type` = ?, `openid` = ?, `openkey` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false,updateSQL, this.UserId, this.Sweibo_Token, this.qq_Token,this.type,this.openid,this.openkey,               
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
    
    public static final String QUERY_SQL=" id,userId,Sweibo_Token,qq_Token,type,openid,openkey ";

    public static FenXiangToken findByUserId(Long userId){
        String sql = " select "+QUERY_SQL+" from "+FenXiangToken.TABLE_NAME+" where userId = ?";
        
        FenXiangToken token =new JDBCExecutor<FenXiangToken>(sql,userId){
            @Override
            public FenXiangToken doWithResultSet(ResultSet rs) throws SQLException { 
                FenXiangToken token =null;
                if (rs.next()) {
                    FenXiangToken tk= parseToken(rs);
                    if (tk != null){
                        token=tk;
                    }                      
                }
                return token;
            }
        }.call();
        
        return token;
    }
    
    public static FenXiangToken parseToken(ResultSet rs) throws SQLException{
        
        Long id = rs.getLong(1);
        Long userId = rs.getLong(2);
        String Sweibo_Token=rs.getString(3);
        String qq_Token=rs.getString(4);
        int type = rs.getInt(5);
        String openid=rs.getString(6);
        String openkey=rs.getString(7);
        
        FenXiangToken token = new FenXiangToken(id,userId,Sweibo_Token,qq_Token,type,openid,openkey);
        
        return token;
    }


}
