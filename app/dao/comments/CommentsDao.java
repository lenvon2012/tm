
package dao.comments;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.comment.Comments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.DBBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;

public class CommentsDao {
    private static final Logger log = LoggerFactory.getLogger(CommentsDao.class);

    public static final String TAG = "CommentsDao";

    static String COMMENT_SQL = " userId, tid, oid, result, content, nick, buyer_nick, ts, id ";

    public static List<Comments> findOnlineByUser(Long userId, int offset, int limit) {

        String sql = "select " + COMMENT_SQL
                + " from comments where userId = ? ";

        sql = genShardQuery(sql, userId);

        sql += " order by id desc limit ?, ?";

        return new JDBCExecutor<List<Comments>>(Comments.dp, sql, userId, offset, limit) {

            @Override
            public List<Comments> doWithResultSet(ResultSet rs) throws SQLException {
                List<Comments> commentList = new ArrayList<Comments>();
                while (rs.next()) {
                    commentList.add(parseComment(rs));
                }
                return commentList;
            }
        }.call();
    }

    public static long countOnlineByUser(Long userId) {

        log.info(format(
                "countOnlineByUser:userId".replaceAll(", ", "=%s, ") + "=%s",
                userId));

        String sql = "select count(*) from comments where userId = ? ";

        sql = genShardQuery(sql, userId);

        return Comments.dp.singleLongQuery(sql, userId);
    }

    public static String genShardQuery(String query, Long userId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(userId)));
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static Comments parseComment(ResultSet rs) {
        try {
            Comments comment = new Comments(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4),
                    rs.getString(5),
                    rs.getString(6), rs.getString(7), rs.getLong(8));
            comment.setId(rs.getLong(9));
            return comment;
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    public static void dayRemove() {
        long deleteNum = 0;
        int count = 0;
        do {
            count++;
            deleteNum = Comments.dp.update(false, "delete from " + Comments.TABLE_NAME + " where ts < ? limit 100",
                    System.currentTimeMillis() - DateUtil.THIRTY_DAYS);
        } while (deleteNum > 0L && count++ < 300);
    }
}
