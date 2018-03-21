package underup.frame.industry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = FrontCats.TABLE_NAME)
public class FrontCats extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(FrontCats.class);

    @Transient
    public static final String TABLE_NAME = "front_cats";
    
    @Id
    private long frontCid;
    
    public long getFrontcid(){
        return frontCid;
    }
    
    public FrontCats(long frontCid){
        this.frontCid = frontCid;
    }
    
    public FrontCats(){
    }
    
    public void setFrontCid(long frontCid){
        this.frontCid = frontCid;
    }
    
    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return null;
    }

    @Override
    public Long getId() {
        return frontCid;
    }

    @Override
    public void setId(Long id) {
        this.frontCid = id;
    }
    
    @Transient
    public static final FrontCats EMPTY = new FrontCats();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Transient
    private static final String findExisted = "select frontcid from front_cats where frontcid=?";
    
    public static long findIfExisted(long frontcid){
        return dp.singleLongQuery(findExisted, frontcid);
    }
    
    @Override
    public boolean jdbcSave() {
        long frontCid = findIfExisted(this.frontCid);
        if(frontCid == 0L){
            return rawInsert();
        }else{
            log.info("" + frontCid +" has existed!");
        }
        return true;
    }
    
    @Transient
    public static final String insertSQL = "insert into front_cats (frontCid) values (?)";
 
    public boolean rawInsert(){
        long id = dp.insert(false, insertSQL, this.frontCid);
        if (id > 0) {
            log.info("insert success!");
            return true;
        } else {
            log.error("insert fail...");
            return false;
        }
    }

    @Override
    public String getIdName() {
        return null;
    }

    @Override
    public String getTableName() {
        return null;
    }
    
    public static List<Long> getAllCats(){
        String query = "select frontcid from front_cats order by frontCid";
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> frontCids = new ArrayList<Long>();
                while(rs.next()){
                    Long temp = rs.getLong(1);
                    frontCids.add(temp);
                }
                return frontCids;
            }
        }.call();
    }
    
    public static Long getLastFrontCid(){
        String query = "SELECT * FROM front_cats ORDER BY frontCid DESC limit 0,1;";
        return dp.singleLongQuery(query);
    }
    
}
