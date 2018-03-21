package dao.rpt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.rpt.response.RptCustBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.pojo.rpt.RptCustResultPojo;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;

public class RptCustDao {

    public final static Logger log = LoggerFactory.getLogger(RptCustDao.class);

    static String QueryForRptCust = "select `nick`,`dateTime`,`impressions`,`click`,`cost`,`aclick`,`ctr`,`cpc`,`cpm`,`directpay`,"
            + "`indirectpay`,`directpaycount`,`indirectpaycount`,`favitemcount`,`favshopcount` from "
            + " RptCustBase_ where nick = ? and dateTime >= ? and dateTime <= ? order by dateTime desc ";

    public static List<RptCustResultPojo> findByUserNick(Long userId, String userNick, Long startTs, Long endTs) {

        List<RptCustBase> baseList = new JDBCExecutor<List<RptCustBase>>(QueryForRptCust, userNick, startTs, endTs) {
            @Override
            public List<RptCustBase> doWithResultSet(ResultSet rs) throws SQLException {
                List<RptCustBase> resultList = new ArrayList<RptCustBase>();
                while (rs.next()) {
                    resultList.add(new RptCustBase(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs
                            .getInt(5), rs.getInt(6), rs.getDouble(7), rs.getDouble(8), rs.getDouble(9), rs.getInt(10),
                            rs.getInt(11), rs.getInt(12), rs.getInt(13), rs.getInt(14), rs.getInt(15)));
                }

                return resultList;
            }
        }.call();

        // List<RptCustBase> baseList = RptCustBase.find(
        // "nick = ?  and dateTime >= ? and dateTime <= ? and sourceId = 4 ",
        // userNick, startTs, endTs).fetch();

//        log.info(format("RptCustDao.findByUserNick: userId, userNick, ts, baseList size".replaceAll(", ", "=%s ,")
//                + "=%s ", userId, userNick, new SimpleDateFormat("yyyy-MM-dd").format(new Date(startTs)),
//                new SimpleDateFormat("yyyy-MM-dd").format(new Date(endTs)), baseList.size()));

        RptCustResultPojo sum = new RptCustResultPojo("总和");
        RptCustResultPojo avg = new RptCustResultPojo("平均");

        List<RptCustResultPojo> resultList = new ArrayList<RptCustResultPojo>();
        resultList.add(sum);
        resultList.add(avg);

        Map<Long, RptCustBase> custBaseMap = new HashMap<Long, RptCustBase>();
        if (!CommonUtils.isEmpty(baseList)) {
            for (RptCustBase base : baseList) {
                custBaseMap.put(base.getDateTime(), base);
            }
        }
        for (Long start = startTs; start <= endTs; start += DateUtil.DAY_MILLIS) {
            RptCustBase base = custBaseMap.get(start);
            RptCustResultPojo pojo = new RptCustResultPojo(start);
            if (base != null) {
                pojo = new RptCustResultPojo(base);
            }
            resultList.add(pojo);
            sum = sum.add(pojo);
            avg = avg.add(pojo);
        }
        avg = avg.divide(Math.max(baseList.size(), 1));

        return resultList;

    }

    public static RptCustResultPojo findTotalByUserNick(Long userId, String userNick, Long startTs, Long endTs) {

        String query = "select `nick`,`dateTime`,`impressions`,`click`,`cost`,`aclick`,`ctr`,`cpc`,`cpm`,`directpay`,"
                + "`indirectpay`,`directpaycount`,`indirectpaycount`,`favitemcount`,`favshopcount` from "
                + " RptCustBase_ where nick = ? and dateTime >= ? and dateTime <= ? and sourceId = 4 ";
        
        List<RptCustBase> baseList = new JDBCExecutor<List<RptCustBase>>(query, userNick, startTs, endTs) {
            @Override
            public List<RptCustBase> doWithResultSet(ResultSet rs) throws SQLException {
                List<RptCustBase> resultList = new ArrayList<RptCustBase>();
                while (rs.next()) {
                    resultList.add(new RptCustBase(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs
                            .getInt(5), rs.getInt(6), rs.getDouble(7), rs.getDouble(8), rs.getDouble(9), rs.getInt(10),
                            rs.getInt(11), rs.getInt(12), rs.getInt(13), rs.getInt(14), rs.getInt(15)));
                }

                return resultList;
            }
        }.call();
//        log.info(format("RptCustDao.findByUserNick: userId, userNick, ts, baseList size".replaceAll(", ", "=%s ,")
//                + "=%s ", userId, userNick, new SimpleDateFormat("yyyy-MM-dd").format(new Date(startTs)),
//                new SimpleDateFormat("yyyy-MM-dd").format(new Date(endTs)), baseList.size()));

        RptCustResultPojo sum = new RptCustResultPojo("总和");

        if (!CommonUtils.isEmpty(baseList)) {
            for (RptCustBase base : baseList) {
                RptCustResultPojo pojo = new RptCustResultPojo("");
                if (base != null) {
                    pojo = new RptCustResultPojo(base);
                }
                sum = sum.add(pojo);
            }
        }
        return sum;
    }

    static String QueryForROIByUserNick = " select sum(cost), sum(directpay+indirectpay) from "
            + RptCustBase.TABLE_NAME
            + " as rpt where rpt.nick = ?  and rpt.dateTime >= ? and rpt.dateTime <= ? and rpt.sourceId = 4 ";

    public static double getRoiByUserNick(Long userId, String userNick, Long startTs, Long endTs) {

        return new JDBCExecutor<Double>(QueryForROIByUserNick, userNick, startTs, endTs) {
            @Override
            public Double doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    long cost = rs.getLong(1);
                    long pay = rs.getLong(2);
                    return (double) pay / (cost + 1);
                }
                return 0d;
            }
        }.call();
    }

}
