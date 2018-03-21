package models.topscoreid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import jdbcexecutorwrapper.JDBCSetLongExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

@Entity(name = TopScoreId.TABLE_NAME)
public class TopScoreId extends GenericModel {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(TopScoreId.class);

    @Transient
    private static final String TAG = "TopScoreId";

    @Transient
    public static final String TABLE_NAME = "top_score_id";

    @Column(length = 32, columnDefinition = "varchar(32)")
    private String serviceCode;

    @Id
    private long scoreId;

    private long created;

    /**
     * true: good; false: bad
     */
    private boolean type;

    public TopScoreId(String serviceCode, long scoreId, boolean type) {
        super();
        this.serviceCode = serviceCode;
        this.scoreId = scoreId;
        this.type = type;
        this.created = System.currentTimeMillis();
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public long getScoreId() {
        return scoreId;
    }

    public void setScoreId(long scoreId) {
        this.scoreId = scoreId;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "TopScoreId [serviceCode=" + serviceCode + ", scoreId=" + scoreId + ", created=" + created + ", type="
                + type + "]";
    }

    public static void saveOrUpdate(String serviceCode, long scoreId, boolean type) {
        // TopScoreId.find("scoreId = ?", scoreId).first();
        long created = System.currentTimeMillis();
        JDBCBuilder.insert("insert into top_score_id(serviceCode, scoreId, type, created) values(?,?,?,?) on duplicate key update serviceCode = ?, type = ?, created = ?",
                serviceCode, scoreId, type, created, serviceCode, type, created);
    }

    public static List<TopScoreId> findTopScoreIdList(boolean type, int pn, int ps) {
        //return TopScoreId.find("type = ?", type).fetch(pn, ps);
    	return TopScoreId.JDBCfindByType(type, pn, ps);
    }
    
    public static Set<Long> findTopScoreIdLongList(boolean type, int pn, int ps) {
        System.out.println(pn + "   " + ps);
        Set<Long> list = new JDBCSetLongExecutor("select scoreId from top_score_id limit ? offset ?", ps, pn).call();
        return list;
    }
    
    public static List<TopScoreId> JDBCfindByType(boolean type,int pn,int ps) {
        String sql = "select serviceCode,scoreId,created,type from " + TABLE_NAME + " where type = ? limit ?,?";
        if(pn < 1){
        	pn = 1;
        }
        List<TopScoreId> userShopList = new JDBCExecutor<List<TopScoreId>>(sql, type, (pn-1)*ps, ps) {
            @Override
            public List<TopScoreId> doWithResultSet(ResultSet rs) throws SQLException {
                List<TopScoreId> tempList = new ArrayList<TopScoreId>();
                while (rs.next()) {
                	TopScoreId topScore = new TopScoreId(rs.getString(1),rs.getLong(2),rs.getBoolean(4));
                	topScore.setCreated(rs.getLong(3));
                    tempList.add(topScore);
                }
                return tempList;
            }
        }.call();
        
        return userShopList;
    }

}
