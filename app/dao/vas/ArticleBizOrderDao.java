
package dao.vas;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jdbcexecutorwrapper.JDBCMapStringToLongExecutor;
import models.user.User;
import models.vas.ArticleBizOrderPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

public class ArticleBizOrderDao {

    public final static Logger log = LoggerFactory.getLogger(ArticleBizOrderDao.class);

    public static String QueryForSoonExpiredUser = "select distinct(nick) "
            + "from article_biz_order as bizorder, (select max(orderId) as id from article_biz_order "
            + "where orderCycleEnd > ? group by nick) as ids where bizorder.orderId = ids.id and bizorder.orderCycleEnd <= ?;";

    public static List<String> getSoonExpiredUser(Long today, Long expiredTs) {
        return new JDBCExecutor<List<String>>(QueryForSoonExpiredUser, today, expiredTs) {
            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> expiredNickList = new ArrayList<String>();
                while (rs.next()) {
                    expiredNickList.add(rs.getString(1));
                }
                return expiredNickList;
            }
        }.call();
    }

    private static final String SelectAllProperties = " bizOrderId,orderId,nick,articleName,articleCode,itemCode,createTime,orderCycle,orderCycleStart,orderCycleEnd,bizType,fee,promFee,refundFee,totalPayFee,level,hour,monthCycle ";
    
    public static List<ArticleBizOrderPlay> fetchDurationList(String nick, long start, long end, PageOffset po,
            boolean asc) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(SelectAllProperties);
        sb.append(" from ");
        sb.append(ArticleBizOrderPlay.TABLE_NAME);
        sb.append(" where 1 = 1  ");
        if (!StringUtils.isBlank(nick)) {
            sb.append("  and  nick = '");
            sb.append(CommonUtils.escapeSQL(nick));
            sb.append("' ");
        }

        if (asc) {
            sb.append(" order by orderId limit ? offset ?  ");
        } else {
            sb.append(" order by orderId desc  limit ? offset ?  ");
        }

        String query = sb.toString();

        return new JDBCExecutor<List<ArticleBizOrderPlay>>(query, po.getPs(), po.getOffset()) {
            @Override
            public List<ArticleBizOrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ArticleBizOrderPlay> orderList = new ArrayList<ArticleBizOrderPlay>();
                while (rs.next()) {
                    orderList.add(new ArticleBizOrderPlay(rs));
                }
                return orderList;
            }
        }.call();
    }
    
    static String CountQueryVasOrderByDuration = "select count(bizOrderId) from " + ArticleBizOrderPlay.TABLE_NAME
            + " where 1 = 1  order by bizOrderId desc limit ? offset ?  ";

    public static int countDurationList(long start, long end, PageOffset po) {
        return (int) JDBCBuilder.singleLongQuery(CountQueryVasOrderByDuration, po.getPs(), po.getOffset());
    }
    
    static String QueryVasOrderByNick = "select bizOrderId,orderId,nick,articleName,articleCode,itemCode,createTime,orderCycle,orderCycleStart,orderCycleEnd,bizType,fee,promFee,refundFee,totalPayFee,"
            +
            "level,hour,monthCycle from "
            + ArticleBizOrderPlay.TABLE_NAME + " where nick = ?";

    public static List<ArticleBizOrderPlay> findVasOrderByNick(String nick) {

        return new JDBCExecutor<List<ArticleBizOrderPlay>>(QueryVasOrderByNick, nick) {
            @Override
            public List<ArticleBizOrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ArticleBizOrderPlay> orderList = new ArrayList<ArticleBizOrderPlay>();
                while (rs.next()) {
                    orderList.add(new ArticleBizOrderPlay(rs));
                }
                return orderList;
            }
        }.call();
    }

    static String QuerySevenDaysFreeUserNick = "select bizOrderId,orderId,nick,articleName,articleCode,itemCode,createTime,orderCycle,orderCycleStart,orderCycleEnd,bizType,fee,promFee,refundFee,totalPayFee,"
            +
            "level,hour,monthCycle from "
            + "(select * from "
            + ArticleBizOrderPlay.TABLE_NAME
            + " group by nick having count(*) = 1) as a"
            + " where a.orderCycle like '0%' and a.orderCycleStart > 1375200000000 order by orderCycleStart asc, nick desc limit ?,?";

    //static String QuerySevenDaysFreeUserNick = "select bizOrderId,orderId,nick,articleName,articleCode,itemCode,createTime,orderCycle,orderCycleStart,orderCycleEnd,bizType,fee,promFee,refundFee,totalPayFee," +
    //        "level,hour,monthCycle from " + ArticleBizOrderPlay.TABLE_NAME + " where (orderCycleEnd - orderCycleStart = 604800000) and (orderCycleStart > 1375200000000) order by orderCycleStart asc, nick desc limit ?,?";
    public static List<ArticleBizOrderPlay> sevenDaysFreeUserNicks(int offset, int limit) {
        return new JDBCExecutor<List<ArticleBizOrderPlay>>(QuerySevenDaysFreeUserNick, offset, limit) {
            @Override
            public List<ArticleBizOrderPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ArticleBizOrderPlay> orderList = new ArrayList<ArticleBizOrderPlay>();
                while (rs.next()) {
                    orderList.add(new ArticleBizOrderPlay(rs));
                }
                return orderList;
            }
        }.call();
    }

    static String CountSevenDaysFreeUserNick = "select count(*) from "
            + "(select * from " + ArticleBizOrderPlay.TABLE_NAME + " group by nick having count(*) = 1) as a"
            + " where a.orderCycle  like '0%' and a.orderCycleStart > 1375200000000 ";

    //static String CountSevenDaysFreeUserNick = "select count(*) from " + ArticleBizOrderPlay.TABLE_NAME + " where (orderCycleEnd - orderCycleStart = 604800000) and (orderCycleStart > 1375200000000) order by orderCycleStart desc, nick desc ";
    public static long countSevenDaysFreeUserNicks() {
        return JDBCBuilder.singleLongQuery(CountSevenDaysFreeUserNick);
    }

    public static Map<String, Long> listWillExpire(long start, long end, PageOffset po) {
        log.info(format("listWillExpire:start, end, po".replaceAll(", ", "=%s, ") + "=%s", start, end, po));

        Map<String, Long> call = new JDBCMapStringToLongExecutor(" select nick , max(orderCycleEnd) as mEnd from "
                + ArticleBizOrderPlay.TABLE_NAME + " as bizorder ,  " + User.TABLE_NAME + " as  u  "
                + "  where bizorder.nick = u.userNick   group by nick having  mEnd  between ? and ? " +
                " order by mEnd asc  limit ? offset ?", start, end, po.getPs(), po.getOffset()).call();
        return call;
    }

    public static int countWillExpire(long start, long end) {

        log.info(format("countWillExpire:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));

        return (int) JDBCBuilder
                .singleLongQuery(
                        " select count(*) from (select bizorder.nick from "
                                + ArticleBizOrderPlay.TABLE_NAME
                                + " as bizorder ,  "
                                + User.TABLE_NAME
                                + " as  u  "
                                + "  where bizorder.nick = u.userNick   group by nick having max(orderCycleEnd) between ? and ?  ) as temp",
                        //                                + "  where order.nick = u.userNick  and u.type & 16 > 0 group by nick having max(orderCycleEnd) between ? and ?  ) as temp",
                        start, end);
    }
}
