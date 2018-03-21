
package models.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

/**
 * 商品类目
 * 
 * @author hyg
 * modify by uttp
 */
@Entity
@Table(name = FrontCatPlay.TABLE_NAME)
public class FrontCatPlay extends GenericModel implements PolicySQLGenerator{

    @Transient
    public static final String TABLE_NAME = "front_cat_play";

    @Transient
    public static final Logger log = LoggerFactory.getLogger(FrontCatPlay.class);

    @Transient
    public static FrontCatPlay EMPTY = new FrontCatPlay();

    // 商品id
    @Id
    private Long cid;

    // 获取父id  为null 代表是一级的目录
    @Column(name = "parentCid ")
    private Long parentCid;

    // 获取父名称 name=null代表 第一级
    @Column(name = "parentName")
    private String parentName;

    // 类目名称
    @Column(name = "name")
    private String name;

    // 该类目是否为父类目
    @Column(name = "isParent")
    private Boolean isParent;

    // level
    @Column(name = "level")
    private Integer level;

    public FrontCatPlay() {
    }

    public FrontCatPlay(Long cid, Long parentCid, String parentName, String name, Boolean isParent, Integer level) {
        super();
        this.cid = cid;
        this.parentCid = parentCid;
        this.parentName = parentName;
        this.name = name;
        this.isParent = isParent;
        this.level = level;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getParentCid() {
        return parentCid;
    }

    public void setParentCid(Long parentCid) {
        this.parentCid = parentCid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsParent() {
        return isParent;
    }

    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "FrontCatPlay [cid=" + cid + ", parentCid=" + parentCid + ", parentName=" + parentName + ", name="
                + name + ", isParent=" + isParent + ", level=" + level + "]";
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
	public Long getId() {
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
	
	
	@Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
	public static List<Long> getSecondLevel(){
		String query = "select cid from front_cat_play where level=2";
		return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> wordList = new ArrayList<Long>();
                while(rs.next()){
                	Long temp = rs.getLong(1);
                	wordList.add(temp);
                }
                return wordList;
            }
        }.call();
	}
	
	public static List<Long> getFirstLevel(){
	    String query = "select cid from front_cat_play where level=1";
	    return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query){
	        @Override
	        public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
	            List<Long> frontCids = new ArrayList<Long>();
	            while(rs.next()){
	                long frontCid = rs.getLong(1);
	                frontCids.add(frontCid);
	            }
	            return frontCids;
	        }
	    }.call();
	}
	
	//Long cid, Long parentCid, String parentName, String name, Boolean isParent, Integer level
	public static List<FrontCatPlay> getFirstLevelInfo(){
	    String query = "select cid, parentCid, parentName, name, isParent, level from front_cat_play where level=1";
	    return new JDBCBuilder.JDBCExecutor<List<FrontCatPlay>>(dp, query){
	        @Override
	        public List<FrontCatPlay> doWithResultSet(ResultSet rs) throws SQLException{
	            List<FrontCatPlay> frontCatPlays = new ArrayList<FrontCatPlay>();
	            while(rs.next()){
	                long cid = rs.getLong(1);
	                long parentCid = rs.getLong(2);
	                String parentName = rs.getString(3);
	                String name = rs.getString(4);
	                boolean isParent = rs.getBoolean(5);
	                int level = rs.getInt(6);
	                frontCatPlays.add(new FrontCatPlay(cid, parentCid, parentName, name, isParent, level));
	            }
	            return frontCatPlays;
	        }
	    }.call();
	}
}
