package models.word.top;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = BusCatPlay.TABLE_NAME)
public class BusCatPlay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(BusCatPlay.class);

    public static final String TABLE_NAME = "buscatplay";

    @Id
    @GeneratedValue
    private long catId;

    private long parentId;

    private int level;

    private String name;

    public BusCatPlay() {

    }

    public BusCatPlay(String name, int level) {
        super();
        this.name = name;
        this.level = level;
        this.parentId = 0;
    }

    public BusCatPlay(String name, int level, long parentId) {
        super();
        this.name = name;
        this.level = level;
        this.parentId = parentId;
    }

    public static long findOrCreate(String name, int level, long parentId) {
        //BusCatPlay catPlay = BusCatPlay.find("name = ? and level = ?", name, level).first();
    	BusCatPlay catPlay = BusCatPlay.findByNameAndLevel(name, level);
        if (catPlay == null) {
            catPlay = new BusCatPlay(name, level, parentId);
            return catPlay.insert();
        }
        return catPlay.getCatId();
    }
    
    public static BusCatPlay findByNameAndLevel(String name, int level) {

        String query = "select catId,parentId,level,name from " + TABLE_NAME
                + " where name = ? and level = ?";

        return new JDBCBuilder.JDBCExecutor<BusCatPlay>(query, name, level) {

            @Override
            public BusCatPlay doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	BusCatPlay buscat = new BusCatPlay(rs.getString(4), rs.getInt(3), rs.getLong(2));
                	buscat.setCatId(rs.getLong(1));
                	return buscat;
                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static BusCatPlay findByCid(Long cid) {

        String query = "select catId,parentId,level,name from " + TABLE_NAME
                + " where catId = ?";

        return new JDBCBuilder.JDBCExecutor<BusCatPlay>(query, cid) {

            @Override
            public BusCatPlay doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	BusCatPlay buscat = new BusCatPlay(rs.getString(4), rs.getInt(3), rs.getLong(2));
                	buscat.setCatId(rs.getLong(1));
                	return buscat;
                } else {
                    return null;
                }

            }

        }.call();
    }
    
    public static BusCatPlay findByCatName(String name) {

        String query = "select catId,parentId,level,name from " + TABLE_NAME
                + " where name = ?";

        return new JDBCBuilder.JDBCExecutor<BusCatPlay>(query, name) {

            @Override
            public BusCatPlay doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	BusCatPlay buscat = new BusCatPlay(rs.getString(4), rs.getInt(3), rs.getLong(2));
                	buscat.setCatId(rs.getLong(1));
                	return buscat;
                } else {
                    return null;
                }

            }

        }.call();
    }

    public static List<BusCatPlay> findListByCatId(Long catId) {

        String query = "select catId,parentId,level,name from " + TABLE_NAME
                + " where parentId = ?";

        return new JDBCBuilder.JDBCExecutor<List<BusCatPlay>>(query, catId) {

            @Override
            public List<BusCatPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<BusCatPlay> list = new ArrayList<BusCatPlay>();
                while (rs.next()) {
                	BusCatPlay buscat = new BusCatPlay(rs.getString(4), rs.getInt(3), rs.getLong(2));
                	buscat.setCatId(rs.getLong(1));
                	list.add(buscat);
                } 
                return list;
            }

        }.call();
    }
    
    public long getCatId() {
        return catId;
    }

    public void setCatId(long catId) {
        this.catId = catId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BusCatPlay(long catId, long parentId, int level, String name) {
        super();
        this.catId = catId;
        this.parentId = parentId;
        this.level = level;
        this.name = name;
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
        return "catId";
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return catId;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.catId = id;
    }

    @Override
    public boolean jdbcSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "catId";
    }

    public long insert() {
        long id = JDBCBuilder.insert("insert into `buscatplay`(`catId`,`parentId`,`level`,`name`) values(?,?,?,?)",
                this.catId, this.parentId, this.level, this.name);
        if (id != 0L) {
            return id;
        } else {
            log.warn("Raw insert Fails... for :" + catId);
            return id;
        }
    }

    public boolean update() {
        long updateNum = JDBCBuilder.insert(
                "update `buscatplay` set  `parentId` = ?, `level` = ?, `name` = ? where `catId` = ? ", this.parentId,
                this.level, this.name, this.getId());

        if (updateNum > 0L) {
            return true;
        } else {
            log.warn("Update Fails... for :" + catId);
            return false;
        }
    }

    public static List<BusCatPlay> findByParentId(long catId) {
        if (catId <= 0) {
            return ListUtils.EMPTY_LIST;
        }

        //return BusCatPlay.find("parentId = ?", catId).fetch();
        return BusCatPlay.findListByCatId(catId);
    }

    public static long countByParentId(long catId) {
        if (catId <= 0) {
            return 0L;
        }

        //return BusCatPlay.count("parentId = ?", catId);
        return JDBCBuilder.singleLongQuery("select count(*) from "+TABLE_NAME+" where parentId = ?", catId); 
    }

}
