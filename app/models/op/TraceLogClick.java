/**
 * 
 */
package models.op;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Transient;

import jdbcexecutorwrapper.JDBCMapStringToLongExecutor;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import utils.DateUtil;

/**
 * @author navins
 * @date 2013-7-15 下午3:00:20
 */
@Entity(name = TraceLogClick.TABLE_NAME)
public class TraceLogClick extends Model{

    private static final Logger log = LoggerFactory.getLogger(TraceLogClick.class);

    public static final String TAG = "TraceLogClick";

    @Transient
    public static final String TABLE_NAME = "tracelog_click";

    @JsonProperty
    // @Index(name = "tracelog")
    String tracelog;

    @JsonProperty
    String srcIp = null;

    @JsonProperty
    @Index(name = "ts")
    long created;

    public TraceLogClick(String tracelog, String srcIp) {
        super();
        this.tracelog = tracelog;
        this.srcIp = srcIp;
        this.created = System.currentTimeMillis();
    }

    public static void click(String tracelog, String srcIp) {
        new TraceLogClick(tracelog, srcIp).jdbcSave();
    }

    public static void clean() {
        long ts = DateUtil.formCurrDate() - DateUtil.TWO_WEEK_SPAN;
        //TraceLogClick.delete("created < ?", ts);
        JDBCBuilder.update(false, "delete from "+TABLE_NAME+" where created < ?", ts);
    }
    
    public static Map<String, Long> durationMapPv(long start, long end) {
        return new JDBCMapStringToLongExecutor("select tracelog, count(*) as cc from " + TraceLogClick.TABLE_NAME
                + " where created >= ? and created <= ? group by tracelog order by cc desc", start, end).call();
    }
    
    public static Map<String, Long> durationMapUv(long start, long end) {
        return new JDBCMapStringToLongExecutor("select tracelog, count(distinct srcIp) as cc from " + TraceLogClick.TABLE_NAME
                + " where created >= ? and created <= ? group by tracelog order by cc desc", start, end).call();
    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where id = ? ";

    private static long findExistId(Long id) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */

    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.id);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `tracelog_click`(`tracelog`,`srcIp`,`created`) values(?,?,?)",
                		this.tracelog, this.srcIp, this.created);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.id);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert("update `tracelog_click` set  `tracelog` = ?, `srcIp` = ?, `created` = ? where `id` = ? ",
                this.tracelog, this.srcIp, this.created, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.id);

            return false;
        }
    }

}
