
package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = UserTracer.TABLE_NAME)
public class UserTracer extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(UserTracer.class);

    public static final String TAG = "UserTracer";

    public static final String TABLE_NAME = "user_tracer";

    @Transient
    public static UserTracer _instance = new UserTracer();

    @Transient
    static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, _instance);

    @Id
    public Long id;

    public int showcaseCount;

    public int delistCount;

    public int renameCount;

    public int queryCount;

    public int commentCount;

    public int searchCount;

    public int genTemplateCount;

    public long lastUpdateTime;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    public int getShowcaseCount() {
        return showcaseCount;
    }

    public void setShowcaseCount(int showcaseCount) {
        this.showcaseCount = showcaseCount;
    }

    public int getDelistCount() {
        return delistCount;
    }

    public void setDelistCount(int delistCount) {
        this.delistCount = delistCount;
    }

    public int getRenameCount() {
        return renameCount;
    }

    public void setRenameCount(int renameCount) {
        this.renameCount = renameCount;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(int searchCount) {
        this.searchCount = searchCount;
    }

    public int getGenTemplateCount() {
        return genTemplateCount;
    }

    public void setGenTemplateCount(int genTemplateCount) {
        this.genTemplateCount = genTemplateCount;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public UserTracer(Long id, int showcaseCount, int delistCount, int renameCount, int queryCount, int commentCount,
            int searchCount, int genTemplateCount, long lastUpdateTime) {
        super();
        this.id = id;
        this.showcaseCount = showcaseCount;
        this.delistCount = delistCount;
        this.renameCount = renameCount;
        this.queryCount = queryCount;
        this.commentCount = commentCount;
        this.searchCount = searchCount;
        this.genTemplateCount = genTemplateCount;
        this.lastUpdateTime = lastUpdateTime;
    }

    public UserTracer() {
        super();
    }

    public static boolean addShowWindowCount(Long userId) {
        return dp.update("update " + TABLE_NAME + " set showcaseCount = showcaseCount + 1 where id = ?", userId) > 0L;
    }

    public static boolean addDelistCount(Long userId) {
        return dp.update("update " + TABLE_NAME + " set delistCount = delistCount + 1 where id = ?", userId) > 0L;
    }

    public static boolean addCommendCount(Long userId) {
        return dp.update("update " + TABLE_NAME + " set commentCount = commentCount + 1 where id = ?", userId) > 0L;
    }

    public static boolean addRenameCount(Long userId) {
        return dp.update("update " + TABLE_NAME + " set renameCount = renameCount + 1 where id = ?", userId) > 0L;
    }

    public long rawInsert() {
        return dp
                .insert(false,
                        "insert into `user_tracer`(`id`,`showcaseCount`,`delistCount`,`renameCount`,`queryCount`,`commentCount`,`searchCount`,`genTemplateCount`,`lastUpdateTime`) values(?,?,?,?,?,?,?,?,?)",
                        this.id, this.showcaseCount, this.delistCount, this.renameCount, this.queryCount,
                        this.commentCount, this.searchCount, this.genTemplateCount, this.lastUpdateTime);
    }

    public long rawUpdate() {
        return dp
                .update("update `user_tracer` set  `showcaseCount` = ?, `delistCount` = ?, `renameCount` = ?, `queryCount` = ?, `commentCount` = ?, `searchCount` = ?, `genTemplateCount` = ?, `lastUpdateTime` = ? where `id` = ? ",
                        this.showcaseCount, this.delistCount, this.renameCount, this.queryCount, this.commentCount,
                        this.searchCount, this.genTemplateCount, this.lastUpdateTime, this.getId());
    }

    public static long existId(Long id) {
        return dp.singleLongQuery("select id from " + TABLE_NAME + " where id = ? ", id);
    }

    @Override
    public boolean jdbcSave() {
        if (existId(this.id) > 0) {
            return rawInsert() >= 0;
        } else {
            return rawUpdate() > 0;
        }

    }

    public UserTracer(ResultSet rs) throws SQLException {
        super();
        this.id = rs.getLong(1);
        this.showcaseCount = rs.getInt(2);
        this.delistCount = rs.getInt(3);
        this.renameCount = rs.getInt(4);
        this.queryCount = rs.getInt(5);
        this.commentCount = rs.getInt(6);
        this.searchCount = rs.getInt(7);
        this.genTemplateCount = rs.getInt(8);
        this.lastUpdateTime = rs.getLong(9);
    }

    static String SELECT_SQL = "select id,showcaseCount,delistCount,renameCount,queryCount,commentCount,searchCount,genTemplateCount,lastUpdateTime from user_tracer";

    public static UserTracer fetch(String whereQuery, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(SELECT_SQL);
        sb.append(" where 1 =1 ");
        if (!StringUtils.isBlank(whereQuery)) {
            sb.append(" and ");
            sb.append(whereQuery);
        }

        return new JDBCExecutor<UserTracer>(sb.toString(), args) {

            @Override
            public UserTracer doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new UserTracer(rs);
                }
                return null;
            }

        }.call();
    }

    public static UserTracer findByUserId(Long userId) {
        return fetch(" id = ?", userId);
    }

    public UserTracer(Long id) {
        super();
        this.id = id;
    }

    public static void ensure(Long userId) {
        if (existId(userId) > 0L) {
            return;
        }

        new UserTracer(userId).rawInsert();
    }

}
