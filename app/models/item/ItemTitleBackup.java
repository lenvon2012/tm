package models.item;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Lob;

import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import transaction.MapIterator;
import actions.DiagAction.BatchReplacer;
import actions.DiagAction.BatchResultMsg;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

@Entity(name = ItemTitleBackup.TABLE_NAME)
public class ItemTitleBackup extends Model implements PolicySQLGenerator, Serializable {

    public static final String TABLE_NAME = "title_backup";

    private static final Logger log = LoggerFactory.getLogger(ItemTitleBackup.class);

    public static final String TAG = "ItemTitleBackup";

    @JsonProperty
    long ts;

    @JsonProperty
    @Index(name = "userId")
    Long userId;

    @JsonProperty
    int count;

    @JsonProperty
    String remark;

    @Lob
    @JsonIgnore
    String opContent;

    public ItemTitleBackup() {

    }

    public ItemTitleBackup(Long userId, HashMap<Long, String> titles) {
        this.userId = userId;
        this.ts = System.currentTimeMillis();
        if (CommonUtils.isEmpty(titles)) {
            return;
        }
        this.opContent = JsonUtil.getJson(titles);
        this.count = titles.size();
    }

    public ItemTitleBackup(User user, HashMap<Long, String> titles) {
        this.userId = user.getId();
        this.ts = System.currentTimeMillis();
        if (CommonUtils.isEmpty(titles)) {
            return;
        }
        this.opContent = JsonUtil.getJson(titles);
        this.count = titles.size();
    }

    public ItemTitleBackup(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.ts = rs.getLong(2);
        this.userId = rs.getLong(3);
        this.count = rs.getInt(4);
        this.remark = rs.getString(5);
        this.opContent = rs.getString(6);
    }

    public static void build(User user, HashMap<Long, String> titles) {
        ItemTitleBackup backup = new ItemTitleBackup(user, titles);
        backup.rawInsert();
    }

    public static void build(User user, List<Item> items) {
        if (CommonUtils.isEmpty(items)) {
            return;
        }
        HashMap<Long, String> titles = new HashMap<Long, String>();
        for (Item item : items) {
            titles.put(item.getNumIid(), item.getTitle());
        }
        ItemTitleBackup backup = new ItemTitleBackup(user, titles);
        backup.rawInsert();
    }

    public static void build(Long userId, List<ItemPlay> items) {
        if (CommonUtils.isEmpty(items)) {
            return;
        }
        HashMap<Long, String> titles = new HashMap<Long, String>();
        for (ItemPlay item : items) {
            titles.put(item.getNumIid(), item.getTitle());
        }
        ItemTitleBackup backup = new ItemTitleBackup(userId, titles);
        backup.rawInsert();
    }

    public static void build(Long userId) {
        List<ItemPlay> items = ItemDao.findByUserId(userId);
        if (CommonUtils.isEmpty(items)) {
            return;
        }
        ItemTitleBackup.build(userId, items);
    }

    public static List<BatchResultMsg> recover(long id, User user) {
        // ItemTitleBackup record = ItemTitleBackup.find(" id = ? and userid = ?", id, user.getId()).first();
        List<ItemTitleBackup> list = new ListFetcher("id = ? and userId = ?", id, user.getId()).call();
        if (CommonUtils.isEmpty(list)) {
            return ListUtils.EMPTY_LIST;
        }

        ItemTitleBackup record = list.get(0);

        try {
            HashMap<String, String> originMsgs = JsonUtil.toObject(record.opContent, HashMap.class);
            if (CommonUtils.isEmpty(originMsgs)) {
                return ListUtils.EMPTY_LIST;
            }

            final Set<Long> ids = new HashSet<Long>();
            final Map<String, String> toModiyfTitles = new HashMap<String, String>();
            new MapIterator<String, String>(originMsgs) {
                @Override
                public void execute(Entry<String, String> entry) {
                    ids.add(Long.valueOf(entry.getKey()));
                    toModiyfTitles.put(entry.getKey().toString(), entry.getValue());
                }
            }.call();

            List<ItemPlay> items = ItemDao.findByNumIids(user.getId(), ids);
            List<BatchResultMsg> resMsgs = new BatchReplacer(user, items, toModiyfTitles, null).call();
            return resMsgs;
        } catch (Exception e) {
            log.error(" error content:" + record.opContent);
            log.warn(e.getMessage(), e);
        }
        return ListUtils.EMPTY_LIST;
    }

    public static ItemTitleBackup findFirstBackup(Long userId) {
        // ItemTitleBackup backup = ItemTitleBackup.find("userId = ? order by ts asc", userId).first();
        List<ItemTitleBackup> list = new ListFetcher("userId = ? and count > 0 order by ts asc limit 1", userId).call();
        if (CommonUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public static List<ItemTitleBackup> findBackups(Long userId) {
        List<ItemTitleBackup> list = new ListFetcher("userId = ? and count > 0 order by ts desc", userId).call();
        return list;
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
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    public boolean rawInsert() {
        try {
            long id = JDBCBuilder.insert(
                    "insert into `title_backup`(`ts`,`userId`,`count`,`remark`,`opContent`) values(?,?,?,?,?)",
                    this.ts, this.userId, this.count, this.remark, this.opContent);
            if (id > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public static class ListFetcher extends JDBCExecutor<List<ItemTitleBackup>> {
        public ListFetcher(String whereQuery, Object... params) {
            super(whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select id,ts,userId,count,remark,opContent from ");
            sb.append(TABLE_NAME);
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" where ");
                sb.append(whereQuery);
            }
            this.query = sb.toString();
        }

        @Override
        public List<ItemTitleBackup> doWithResultSet(ResultSet rs) throws SQLException {
            List<ItemTitleBackup> list = new ArrayList<ItemTitleBackup>();
            while (rs.next()) {
                list.add(new ItemTitleBackup(rs));
            }
            return list;
        }
    }

    public static int countQuery(String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(TABLE_NAME);
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" where ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(sb.toString(), params);

    }

}
