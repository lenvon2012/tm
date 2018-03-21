package models;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = CategoryProps.TABLE_NAME)
public class CategoryProps extends GenericModel implements PolicySQLGenerator{

	@Transient
    private static final Logger log = LoggerFactory.getLogger(CategoryProps.class);
    
    @Transient
    public static final String TABLE_NAME = "category_props";
    
    @Transient
    public static CategoryProps EMPTY = new CategoryProps();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    // 类目cid
    @Id
    @Index(name="cid")
    public Long cid;
    
    // 创建时间
    public Long created;
    
    // 属性串
    public String props;
    
    public CategoryProps() {
        super();
    }
    
	public CategoryProps(Long cid, Long created, String props) {
		super();
		this.cid = cid;
		this.created = created;
		this.props = props;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public String getProps() {
		return props;
	}

	public void setProps(String props) {
		this.props = props;
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
		return "cid";
	}

	@Override
	public Long getId() {
		// TODO Auto-generated method stub
		return cid;
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub
		this.cid = id;
	}
	
	@Override
	public String getIdName() {
		// TODO Auto-generated method stub
		return "cid";
	}
	
	public static long findExistId(Long cid) {

        String query = "select cid from " + TABLE_NAME + " where cid = ? ";

        return dp.singleLongQuery(query, cid);
    }

	@Override
	public boolean jdbcSave() {
		// TODO Auto-generated method stub
		try {
            
            long existId = findExistId(this.cid);
            
            if (existId <= 0) {
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
                    + "`(`cid`,`created`,`props`) values(?,?,?)";
            
            long id = dp.insert(insertSQL, this.cid, System.currentTimeMillis(), this.props);
            
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
                + "` set `created` = ?, `props` = ? where `cid` = ?  ";

        
        long updateNum = dp.insert(updateSQL, this.created, this.props, this.cid);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
    
    public static CategoryProps findByCid(Long cid) {

        String query = "select cid, created, props from " + TABLE_NAME + " where cid = ? ";

        return new JDBCBuilder.JDBCExecutor<CategoryProps>(dp, query, cid) {
            @Override
            public CategoryProps doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseCategoryProps(rs);
                } else {
                    return null;
                }
            }
        }.call();
    }
    
    private static CategoryProps parseCategoryProps(ResultSet rs) {
        try {
        	
            return new CategoryProps(rs.getLong(1), rs.getLong(2), rs.getString(3));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 
    
}
