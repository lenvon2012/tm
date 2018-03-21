package models.ppdazhe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.PageOffset;

@Entity(name =PPhongbao.TABLE_NAME)
public class PPhongbao extends Model implements PolicySQLGenerator{
	private static final Logger log=LoggerFactory.getLogger(PPhongbao.class);
	
	public static final String TABLE_NAME="pphongbao";
	
	private Long userId;
	
	private Long requestId;
	
	private int status;//0表示没有支付，1表示已经付款
	
	public PPhongbao(Long id,Long userId,Long requestId,int status){
		this.id=id;
		this.userId=userId;
		this.requestId=requestId;
		this.status=status;
	}
	
	public PPhongbao(Long userId,Long requestId,int status){
		this.userId=userId;
		this.requestId=requestId;
		this.status=status;
	}
	
	public Long getUserId(){
		return this.userId;
	}

	public void setUserId(Long userId){
		this.userId=userId;
	}
	
	public Long getRequestId(){
		return this.requestId;
	}
	
	public void setRequestId(Long requestId){
		this.requestId=requestId;
	}
	
	public int getStatus(){
		return this.status;
	}
	
	public void setStatus(int status){
		this.status=status;
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
            long existdId = findExistId(this.userId);

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

	@Override
	public String toString() {
		return "PPhongbao [userId=" + userId + ", requestId=" + requestId
				+ ", status=" + status + "]";
	}

	@Transient
    static String EXIST_ID_QUERY = "select id from `pphongbao` where userId  = ? ";
	
    public static long findExistId(Long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY,userId);
    }
    
    @Transient
    static String insertSQL = "insert into `pphongbao`(`userId`,`requestId`,`status`) values(?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.userId, this.requestId,this.status);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    

    @Transient
    static final String updateSQL = "update `pphongbao` set `userId` = ?, `requestId` = ?, `status` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(updateSQL, this.userId,
                this.requestId, this.status,
                this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }
    
    public static List<PPhongbao> findByStatus(int status,int pn,int ps){
    	PageOffset po=new PageOffset(pn, ps);
    	String sql="select id, userId, requestId, status from "+PPhongbao.TABLE_NAME+" where status = ? limit ?, ? ";
    	
    	List<PPhongbao> ActiveList = new JDBCExecutor<List<PPhongbao>>(sql,status,po.getOffset(),po.getPs()) {
            @Override
            public List<PPhongbao> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPhongbao> list = new ArrayList<PPhongbao>();
                while (rs.next()) {
                	PPhongbao ActiveList = parsePPgongbao(rs);
                    if (ActiveList != null)
                        list.add(ActiveList);
                }
                return list;
            }
        }.call();
        
        return ActiveList;
    }
    
    public static List<PPhongbao> findByStatus(int status){
    	String sql="select id, userId, requestId, status from "+PPhongbao.TABLE_NAME+" where status = ? ";
    	
    	List<PPhongbao> ActiveList = new JDBCExecutor<List<PPhongbao>>(sql,status) {
            @Override
            public List<PPhongbao> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPhongbao> list = new ArrayList<PPhongbao>();
                while (rs.next()) {
                	PPhongbao ActiveList = parsePPgongbao(rs);
                    if (ActiveList != null)
                        list.add(ActiveList);
                }
                return list;
            }
        }.call();
        
        return ActiveList;
    }
    
    public static final String Count_Sql=" select count(*) from "+TABLE_NAME;
    
    public static long countByStatus(int status){
    	String sql=Count_Sql+" where status= ? ";
    	long count = JDBCBuilder.singleLongQuery(sql, status);
    	return count;
    }
    
    public static PPhongbao parsePPgongbao(ResultSet rs) throws SQLException{
    	Long id=rs.getLong(1);
    	Long userId=rs.getLong(2);
    	Long requestId=rs.getLong(3);
    	int status=rs.getInt(4);
    	
    	PPhongbao userInfo=new PPhongbao(id,userId, requestId, status);
    	
    	return userInfo;
    }
    
    


}
