
package models.oplog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import actions.DiagAction.BatchReplacer;
import actions.DiagAction.BatchResultMsg;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;

import controllers.BatchOp.BatchOpResult;
import dao.item.ItemDao;

@Entity(name = TitleOpRecord.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "cid", "tableHashKey", "persistent", "tableName", "idName", "idColumn", "propsName",
        "maxKeywordAllowPrice", "dataSrc", "hashColumnName", "hashed", "opContent"
})
@Table(appliesTo = TitleOpRecord.TABLE_NAME, indexes = {
        @Index(name = "userIdTss", columnNames = {
                "userId", "ts"
        })
})
public class TitleOpRecord extends Model {
    public static final String TABLE_NAME = "title_op_record";

    private static final Logger log = LoggerFactory.getLogger(TitleOpRecord.class);

    public static final String TAG = "TitleOpRecord";

    @JsonProperty
    long ts;

    @JsonProperty
    Long userId;

    @JsonProperty
    int successNum;

    @JsonProperty
    int failNum;

    /**
     * serialize from
     * @see     actions.DiagAction.BatchResultMsg 
     */
//    @Column(columnDefinition = "varchar(32760) default null")
    @Lob
    @JsonIgnore
    String opContent;

    @Transient
    List<BatchResultMsg> msgs = ListUtils.EMPTY_LIST;

    public TitleOpRecord(User user, List<BatchResultMsg> msgs, BatchOpResult webResult) {
        this.userId = user.getId();
        this.ts = System.currentTimeMillis();
        if (CommonUtils.isEmpty(msgs)) {
            return;
        }
        this.opContent = JsonUtil.getJson(msgs);
        this.successNum = webResult.getSuccessNum();
        this.failNum = webResult.getFailNum();
    }
    
    public TitleOpRecord() {
    	super();
    }

    public static TitleOpRecord build(User user, List<BatchResultMsg> msgs, BatchOpResult webResult) {
        try {
            TitleOpRecord record = new TitleOpRecord(user, msgs, webResult);

            //record.save();
            record.rawInsert();
            return record;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    public static TMResult findRecent(User user, PageOffset po) {
        //List<TitleOpRecord> fetch = TitleOpRecord
        //        .find(" userId = ? and  (successNum >= 0 ) order by ts desc ", user.getId())
        //        .from(po.getOffset()).fetch(po.getPs());
    	List<TitleOpRecord> fetch = TitleOpRecord.nativeQuery("userId = ? and  (successNum >= 0 ) order by ts desc limit ?,?",
    			user.getId(), po.getOffset(), po.getPs());
        //int count = (int) TitleOpRecord.count(" userId = ? and  successNum >= 0 ", user.getId());
    	int count = (int) JDBCBuilder.singleLongQuery("select count(*) from "+TABLE_NAME+" where userId = ? and  successNum >= 0 ", user.getId());
        TMResult res = new TMResult(fetch, count, po);
        return res;
    }

    public static TitleOpRecord findFirstBackup(User user) {
        //TitleOpRecord fetch = TitleOpRecord.find(" userId = ? and  (successNum >= 0 ) order by ts asc ", user.getId())
        //       .first();
    	TitleOpRecord fetch = TitleOpRecord.singleQuery(" userId = ? and  (successNum >= 0 ) order by ts asc ", user.getId());
        return fetch;
    }

    public static List<BatchResultMsg> recover(long id, User user) {
//      msgs = new BatchReplacer(user, itemsAll, newTitleMap).call();
        //TitleOpRecord record = TitleOpRecord.find(" id = ? and userid = ?", id, user.getId()).first();
    	TitleOpRecord record = TitleOpRecord.singleQuery(" id = ? and userid = ?", id, user.getId());
    	if (record == null) {
            return ListUtils.EMPTY_LIST;
        }

        try {
            BatchResultMsg[] originMsgs = JsonUtil.toObject(record.opContent, BatchResultMsg[].class);

            if (ArrayUtils.isEmpty(originMsgs)) {
                return ListUtils.EMPTY_LIST;
            }

            Set<Long> ids = new HashSet<Long>();
            Map<String, String> toModiyfTitles = new HashMap<String, String>();
            for (BatchResultMsg msg : originMsgs) {
                ids.add(msg.getNumIid());
                toModiyfTitles.put(msg.getNumIid().toString(), msg.getOriginTitle());
            }
            List<ItemPlay> items = ItemDao.findByNumIids(user.getId(), ids);
            List<BatchResultMsg> resMsgs = new BatchReplacer(user, items, toModiyfTitles, null).call();
            return resMsgs;
        } catch (Exception e) {
            log.error(" error content:" + record.opContent);
            log.warn(e.getMessage(), e);
        }
        return ListUtils.EMPTY_LIST;
    }

    public static BatchResultMsg[] findDetailMsgs(long id) {
        //TitleOpRecord model = TitleOpRecord.findById(id);
    	TitleOpRecord model = TitleOpRecord.singleQuery("id = ?", id);
        if (model == null) {
            return null;
        }
        BatchResultMsg[] objs = JsonUtil.toObject(model.opContent, BatchResultMsg[].class);
        return objs;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(int successNum) {
        this.successNum = successNum;
    }

    public int getFailNum() {
        return failNum;
    }

    public void setFailNum(int failNum) {
        this.failNum = failNum;
    }

    public String getOpContent() {
        return opContent;
    }

    public void setOpContent(String opContent) {
        this.opContent = opContent;
    }

    public List<BatchResultMsg> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<BatchResultMsg> msgs) {
        this.msgs = msgs;
    }

    public static void clearOld() {
        long day = DateUtil.formCurrDate();
        day = day - (DateUtil.THIRTY_DAYS * 2);
        //TitleOpRecord.delete(" ts < " + day);
        JDBCBuilder.update(false, "delete from "+ TABLE_NAME +" where ts < " + day);
    }

    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `" + TABLE_NAME +
                    "`(`userId`,`failNum`,`opContent`,`successNum`,`ts`) values(?,?,?,?,?)";

            long id = JDBCBuilder.insert(false, true, insertSQL, userId, failNum, opContent, successNum, ts);
            if (id > 0) {
                this.id = id;
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    public static String LIST_TitleOpRecord_QUERY = "select ts,userId,successNum,failNum,opContent,id from "+TABLE_NAME +" where ";
    
    public static List<TitleOpRecord> nativeQuery(String where, Object...params) {
		
		return new JDBCExecutor<List<TitleOpRecord>>(LIST_TitleOpRecord_QUERY + where, params) {

            @Override
            public List<TitleOpRecord> doWithResultSet(ResultSet rs) throws SQLException {
                final List<TitleOpRecord> resulteList = new ArrayList<TitleOpRecord>();
                while (rs.next()) {
                	TitleOpRecord record = new TitleOpRecord();
                	record.setTs(rs.getLong(1));
                	record.setUserId(rs.getLong(2));
                	record.setSuccessNum(rs.getInt(3));
                	record.setFailNum(rs.getInt(4));
                	record.setOpContent(rs.getString(5));
                	record.setId(rs.getLong(6));
                	resulteList.add(record);
                }
                return resulteList;
            }
        }.call();		
	}
	
    public static TitleOpRecord singleQuery(String where, Object...params) {
        return new JDBCExecutor<TitleOpRecord>(LIST_TitleOpRecord_QUERY + where, params) {
            @Override
            public TitleOpRecord doWithResultSet(ResultSet rs) throws SQLException {    
                if (rs.next()) {
                	TitleOpRecord record = new TitleOpRecord();
                	record.setTs(rs.getLong(1));
                	record.setUserId(rs.getLong(2));
                	record.setSuccessNum(rs.getInt(3));
                	record.setFailNum(rs.getInt(4));
                	record.setOpContent(rs.getString(5));
                    return record;
                } else {
                    return null;
                }
            }
        }.call();
    }

    public static TitleOpRecord findFirstByUserId(Long userId) {
    	List<TitleOpRecord> records = nativeQuery(" userId = ? and  (successNum >= 0 ) limit 1", userId);
    	if(records.size() == 0){
    		return null;
    	} else {
    		return records.get(0);
    	}
    }
    
    public static TitleOpRecord findFirstById(Long Id, Long userId) {
    	List<TitleOpRecord> records = nativeQuery(" id = ? and userId = ? limit 1", userId);
    	if(records.size() == 0){
    		return null;
    	} else {
    		return records.get(0);
    	}
    }
    
}
