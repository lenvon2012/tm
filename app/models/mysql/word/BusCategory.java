
package models.mysql.word;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = BusCategory.TABLE_NAME)
public class BusCategory extends GenericModel implements PolicySQLGenerator {

    public static final String TABLE_NAME = "wordcategory";

    private static final Logger log = LoggerFactory.getLogger(BusCategory.class);

    public static final String TAG = "BusCategory";

    @Id
    public Long id;

    @Index(name = "parentId")
    @Column(columnDefinition = "bigint default 0")
    public Long parentId;

    @Index(name = "text")
    public String text;

    @Index(name = "catName")
    public String catName;

    public int level;

    public long reverse;

    public static BusCategory _instance = new BusCategory();

    public BusCategory() {
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    public long rawInsert() {
        long id = JDBCBuilder.insert(false, true, DataSrc.BASIC,
                "insert into `wordcategory`(`id`,`parentId`,`text`,`level`,`reverse`) values(?,?,?,?,?)", this.id,
                this.parentId, this.text, this.level, this.reverse);
        if (id > 0) {
            this.id = id;
            return id;
        } else {
            log.error("insert fails....");
            return -1L;
        }
    }

    public long rawUpdate() {
        long updateNum = JDBCBuilder.update(false,
                "update `wordcategory` set `parentId` = ?, `text` = ?, `level` = ?, `reverse` = ? where `id` = ? ",
                this.parentId, this.text, this.level, this.reverse, this.getId());
        if (updateNum > 0) {
            return updateNum;
        } else {
            log.error("update fails....");
            return -1L;
        }
    }

    public BusCategory(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.parentId = rs.getLong(2);
        this.text = rs.getString(3);
        this.level = rs.getInt(4);
        this.reverse = rs.getLong(5);

    }

    public long findExistId() {
        long existId = JDBCBuilder.singleLongQuery("select id  from `wordcategory` where id = ?", this.getId());
        return existId;
    }

    public boolean jdbcSave() {
        long existId = findExistId();
        if (existId > 0L) {
            return this.rawUpdate() > 0L;
        } else {
            return this.rawInsert() >= 0L;
        }

    }

    public static class ListFetcher extends JDBCExecutor<List<BusCategory>> {
        public ListFetcher(String whereQuery, Object... params) {
            super(whereQuery, params);
            StringBuilder sb = new StringBuilder();
            sb.append("select id,parentId,text,level,reverse from wordcategory");
            sb.append(" where  1 =1 ");

            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }
//            this.query = sb.toString();
            this.query = sb.toString();
        }

        @Override
        public List<BusCategory> doWithResultSet(ResultSet rs) throws SQLException {
            List<BusCategory> list = new ArrayList<BusCategory>();
            while (rs.next()) {
                list.add(new BusCategory(rs));
            }
            return list;
        }
    }

    public static int count(Long hashKeyId, String whereQuery, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) from ");
        sb.append(TABLE_NAME);
        sb.append(" where  1 = 1");
//        sb.append(hashKeyId);
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }
        return (int) JDBCBuilder.singleLongQuery(sb.toString(), params);

    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getReverse() {
        return reverse;
    }

    public void setReverse(long reverse) {
        this.reverse = reverse;
    }

    public static void ensure(String text, int level) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        return;
    }

    static Map<String, BusCategory> cache = new HashMap<String, BusCategory>();

    public static BusCategory ensure(String cid1, int i, String word, BusCategory parent) {
        if (StringUtils.isEmpty(cid1)) {
            return null;
        }

        BusCategory first = null;
        String key = cid1 + i;
        first = cache.get(key);
        if (first != null) {
            return first;
        }

        List<BusCategory> cats = new ListFetcher(null, " text = ? and level = ? ", cid1, i).call();
        first = NumberUtil.first(cats);
        if (first != null) {
            return first;
        }
        ItemCatPlay cat = ItemCatPlay.tryMatch(cid1, i);
        if (cat == null) {
            cache.put(cid1 + i, _instance);
            return null;
        }

        first = new BusCategory();
        first.setLevel(1);
        first.setText(cid1);
        if (i != 1 && parent != null && parent.getId() != null) {
            first.setParentId(parent.getId());
        }

        if (cat != null) {
            first.setId(cat.getCid());
            first.setCatName(cat.getName());
        }

        first.jdbcSave();
        cache.put(cid1 + i, first);
        return first;
    }

    @Override
    public String toString() {
        return "BusCategory [parentId=" + parentId + ", text=" + text + ", level=" + level + ", reverse=" + reverse
                + "]";
    }

    @Override
    public Long getId() {
        return this.id;
    }

//
//    public static void ensure(String text, int level) {
//        ItemCatPlay.find
//    }

    public static BusCategory ensure(String cid1, int i, ItemCatPlay itemCat) {
        if (StringUtils.isEmpty(cid1)) {
            return null;
        }

        BusCategory first = null;
        List<BusCategory> cats = new ListFetcher(null, " text = ? and level = ? ", cid1, i).call();
        first = NumberUtil.first(cats);
        if (first != null) {
            return first;
        }
        first = new BusCategory();
        first.setLevel(1);
        first.setText(cid1);
//        if (i != 1) {
        first.setParentId(itemCat.getParentCid());
//        }
        ItemCatPlay item = ItemCatPlay.tryMatch(cid1, i);
        if (item != null) {
            first.setId(item.getCid());
            first.setCatName(item.getName());
        }

        first.jdbcSave();
        return first;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

}
