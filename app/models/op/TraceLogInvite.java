
package models.op;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import jdbcexecutorwrapper.JDBCMapStringToLongExecutor;
import models.oplog.TMErrorLog;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import result.TMResult.TMListResult;
import transaction.JDBCBuilder;

import com.ciaosir.client.pojo.PageOffset;

@Entity(name = TraceLogInvite.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "userId", "entityId", "cid", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent",
        "tableName", "idName", "idColumn", "propsName", "maxKeywordAllowPrice"
})
public class TraceLogInvite extends GenericModel {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(TraceLogInvite.class);

    @Transient
    public static final String TAG = "TraceLogInvite";

    @Transient
    public static final String TABLE_NAME = "tracelog_invite";
    /**
     * target user id;
     */
    @JsonProperty
    @Id
    Long id;

    @JsonProperty
    String nick = StringUtils.EMPTY;

    @JsonProperty
    String thisIp;

    /**
     * src user id 
     */
    @JsonProperty
//    @Index(name = "tracelog")
    String tracelog;

    @JsonProperty
    String srcIp = null;

    @JsonProperty
    @Index(name = "ts")
    long created;

    public TraceLogInvite(){
    	super();
    }
    
    public static void ensure(User user, String tracelog, String targetIp) {

        String errorContent = null;
        Long targetUid = user.getId();

        //TraceLogInvite model = TraceLogInvite.findById(targetUid);
        TraceLogInvite model = TraceLogInvite.findByTargetId(targetUid);
        if (model == null) {
            String srcIp = UserRecentLogin.userRecent(user.getId());
            new TraceLogInvite(targetUid, user.getUserNick(), targetIp, tracelog, srcIp).jdbcSave();
            return;
        }
        if (StringUtils.equals(model.tracelog, tracelog)) {
            return;
        }

        errorContent = " exist model :" + model + " with new src uid :" + tracelog;
        new TMErrorLog(errorContent).save();
        return;
    }

    public static TraceLogInvite findByTargetId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where id = ? ";

        return new JDBCBuilder.JDBCExecutor<TraceLogInvite>(query, userId) {

            @Override
            public TraceLogInvite doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseTraceLogInvite(rs);

                } else {
                    return null;
                }

            }

        }.call();
    }

    private static final String SelectAllProperties = " id,nick,thisIp,tracelog,srcIp,created ";

    private static TraceLogInvite parseTraceLogInvite(ResultSet rs) {
        try {
        	TraceLogInvite listCfg = new TraceLogInvite();
        	listCfg.setId(rs.getLong(1));
        	listCfg.setNick(rs.getString(2));
        	listCfg.setThisIp(rs.getString(3));
        	listCfg.setTracelog(rs.getString(4));
        	listCfg.setSrcIp(rs.getString(5));
        	listCfg.setCreated(rs.getLong(6));

            return listCfg;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    } 
    
    public static long findExistId(Long id) {

        String query = "select id from " + TABLE_NAME + " where id = ? ";

        return JDBCBuilder.singleLongQuery(query, id);
    }

    public boolean jdbcSave() {
        try {
            
            long existId = findExistId(this.id);
            
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
                    + "`(`id`,`nick`,`thisIp`,`tracelog`,`srcIp`,"
                    +
                    "`created`) values(?,?,?,?,?,?)";
            
            long id = JDBCBuilder.insert(insertSQL, this.id, this.nick, this.thisIp,
            		this.tracelog, this.srcIp, this.created);
            
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
                + "` set `nick` = ?, `thisIp` = ?, `tracelog` = ?, `srcIp` = ?, `created` = ? where `id` = ?  ";

        long created = System.currentTimeMillis();
        
        long updateNum = JDBCBuilder.insert(updateSQL, this.nick, this.thisIp, this.tracelog,this.srcIp,created,
                this.id);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
    
    public String getTracelog() {
		return tracelog;
	}

	public void setTracelog(String tracelog) {
		this.tracelog = tracelog;
	}

	public TraceLogInvite(Long id, String nick, String thisIp, String tracelog, String srcIp) {
        super();
        this.id = id;
        this.nick = nick;
        this.thisIp = thisIp;
        this.tracelog = tracelog;
        this.srcIp = srcIp;
        this.created = System.currentTimeMillis();
    }

    /*public static TMListResult findBySrcUid(Long srcUid, PageOffset po) {

        log.info(format("findBySrcUid:srcUid, po".replaceAll(", ", "=%s, ") + "=%s", srcUid, po));
        List<TraceLogInvite> invites = TraceLogInvite.find("srcUid = ? order by created desc", srcUid)
                .from(po.getOffset()).fetch(po.getPs());
        int count = (int) TraceLogInvite.count("srcUid =  ? ", srcUid);
        TMListResult res = new TMListResult(invites, count, po);
        return res;
    }*/

    public static TMListResult findBySrcUid(Long srcUid, final PageOffset po) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where srcUid = ? order by created desc";
        final int count = (int)JDBCBuilder.singleLongQuery("select count(*) from "+TABLE_NAME+" where srcUid = ", srcUid); 
        return new JDBCBuilder.JDBCExecutor<TMListResult>(query, srcUid) {

            @Override
            public TMListResult doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<TraceLogInvite> invites = new ArrayList<TraceLogInvite>();
                while (rs.next()) {
                	invites.add(parseTraceLogInvite(rs));
                	TMListResult res = new TMListResult(invites, count, po);
                	return res;
                }
                return null;
            }

        }.call();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getThisIp() {
        return thisIp;
    }

    public void setThisIp(String thisIp) {
        this.thisIp = thisIp;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createStr = sdf.format(new Date(created));
        return "TraceLogInvite [id=" + id + ", nick=" + nick + ", thisIp=" + thisIp + ", tracelog=" + tracelog
                + ", srcIp=" + srcIp + ", created=" + createStr + "]";
    }

    public static Map<String, Long> durationMap(long start, long end) {
        return new JDBCMapStringToLongExecutor("select tracelog, count(*) from " + TraceLogInvite.TABLE_NAME
                + " where created >= ? and created <= ? group by tracelog", start, end).call();
    }
}
