package models.itemCopy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;

import models.CategoryProps;
import models.item.ItemCatPlay;
import models.jms.JMSMsgLog;
import models.task.AutoTitleTask;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import utils.CommonUtil;

/**
 * 类目属性
 * @author oyster
 *
 */
@Entity(name=ItemCatProps.TABLE_NAME)
public class ItemCatProps extends GenericModel implements PolicySQLGenerator {
	
	public static final String TABLE_NAME = "item_cat_props_new";
	
	public static ItemCatProps EMPTY = new ItemCatProps();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
	

    public static final Logger log = LoggerFactory.getLogger(ItemCatPlay.class);
    
    @Index(name = "cid")
    @Id
    private long cid;
    
    private String props;
    
    @Column(name="input_pids")
    private String inputPids;

	public String getInputPids() {
		return inputPids;
	}

	public void setInputPids(String inputPids) {
		this.inputPids = inputPids;
	}

	public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
	}

	public ItemCatProps(long cid, String props) {
		super();
		this.cid = cid;
		this.props = props;
	}
	
	

	public ItemCatProps(long cid, String props, String inputPids) {
		super();
		this.cid = cid;
		this.props = props;
		this.inputPids = inputPids;
	}

	public ItemCatProps() {
		super();
	}

	@Override
	public String toString() {
		return "ItemCatProps [cid=" + cid + ", props=" + props + ", inputPids="
				+ inputPids + "]";
	}
	
	
	/**
	 * 根据叶子类目编号获取对应的属性对
	 * @return 成功返回结果，失败返回null
	 */
	public static ItemCatProps getPropStrByCid(Long cid){
		String sql="select * from "+TABLE_NAME+" where cid = ?";
		return new JDBCBuilder.JDBCExecutor<ItemCatProps>(dp, sql, cid) {

			@Override
			public ItemCatProps doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();

        
	}
	
	private static ItemCatProps parseResult(ResultSet rs) {
		try {
			
			ItemCatProps icp = new ItemCatProps();
			icp.cid = rs.getLong("cid");
			icp.inputPids = rs.getString("input_pids");
			icp.props = rs.getString("props");
			return icp;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
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
		return "cid";
	}

	@Override
	public Long getId() {
		return cid;
	}

	@Override
	public void setId(Long id) {
		this.cid=id;
		
	}
	
	@Override
	public boolean jdbcSave() {
		// TODO Auto-generated method stub
		try {
            ItemCatProps exist = getPropStrByCid(this.cid);
            if (exist ==null) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
	}

	public boolean rawInsert() {
        try {
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`cid`,`props`,`input_pids`) values(?,?,?)";
            long id = dp.insert(insertSQL, this.cid, this.props,this.inputPids);
            if (id > 0L) {
                return true;
            } else {
                return false;
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
    
    
    public boolean rawUpdate() {
        String updateSQL = "update `"
                + TABLE_NAME
                + "` set `props` = ?, `input_pids` = ? where `cid` = ?  ";
        
        long updateNum = dp.insert(updateSQL,  this.props,this.inputPids, this.cid);

        if (updateNum == 1) {

            return true;
        } else {
            return false;
        }
    }

	@Override
	public String getIdName() {
		return "cid";
	}
	
	
	/**
	 * 删除数据库中的记录
	 * @param cid
	 */
	public static void deleteByCid(Long cid){
		ItemCatProps icp=findById(cid);
		if (icp!=null) {
			icp.delete();
		}
		
	}

    
    

    
}
