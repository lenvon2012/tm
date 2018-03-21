
package dao.rpt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.campaign.CampaignPlay;
import models.rpt.response.RptCampaignBase;
import models.updatetimestamp.updatets.RptCampaignUpdateTs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.pojo.rpt.RptCampaignResultPojo;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.CommonUtils;

import dao.campaign.CampaignDao;

public class RptCampaignDao {

    public final static Logger log = LoggerFactory.getLogger(RptCampaignDao.class);

    public static DBDispatcher rptCampaignBaseDispatcher = new DBDispatcher(DataSrc.BASIC, RptCampaignBase.EMPTY);

    static String QueryForRptCampaign = "select campaignId, dateTime,  cost,  impressions, click, indirectpay, "
            + "directpay, indirectpaycount, directpaycount,   favshopcount, favitemcount from "
            + " RptCampaignBaseNew_ where campaignId = ?  and dateTime >= ? and searchtypeId = 3 and sourceId = 4 order by dateTime desc ";

    /**
     * rpt for campaign
     * 
     * @param campaignId
     * @param ts
     * @return
     */
    public static List<RptCampaignResultPojo> findByCampaignId(Long campaignId, Long ts) {

        List<RptCampaignBase> baseList = new JDBCExecutor<List<RptCampaignBase>>(rptCampaignBaseDispatcher,
                QueryForRptCampaign, campaignId, ts) {

            @Override
            public List<RptCampaignBase> doWithResultSet(ResultSet rs) throws SQLException {
                List<RptCampaignBase> resultList = new ArrayList<RptCampaignBase>();
                while (rs.next()) {
                    resultList.add(new RptCampaignBase(rs.getLong(1), rs.getLong(2), rs.getInt(3), rs.getLong(4), rs
                            .getInt(5), rs.getInt(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs
                            .getInt(11)));
                }

                return resultList;
            }
        }.call();

//        log.info(format("RptCampaignDao.findByCampaignId: campaign, ts, nickbaseList size".replaceAll(", ", "=%s ,")
//                + "=%s ", campaignId, new SimpleDateFormat("yyyy-MM-dd").format(new Date(ts)), baseList.size()));

        RptCampaignResultPojo sum = new RptCampaignResultPojo(RptCampaignResultPojo.DAY.SUM);
        RptCampaignResultPojo avg = new RptCampaignResultPojo(RptCampaignResultPojo.DAY.AVG);

        List<RptCampaignResultPojo> resultList = new ArrayList<RptCampaignResultPojo>();
        resultList.add(sum);
        resultList.add(avg);

        Map<Long, RptCampaignBase> custBaseMap = new HashMap<Long, RptCampaignBase>();
        if (!CommonUtils.isEmpty(baseList)) {
            for (RptCampaignBase base : baseList) {
                custBaseMap.put(base.getDateTime(), base);
            }
        }
        Long yestoday = DateUtil.formYestadyMillis();
        for (Long start = ts; start <= yestoday; start += DateUtil.DAY_MILLIS) {

            RptCampaignBase base = custBaseMap.get(start);
            RptCampaignResultPojo pojo = new RptCampaignResultPojo(start);
            if (base != null) {
                pojo = new RptCampaignResultPojo(base, 0L);
            }
            resultList.add(pojo);
            sum = sum.add(pojo);
            avg = avg.add(pojo);
        }
        avg = avg.divide(Math.max(baseList.size(), 1));

        return resultList;
    }

    public static List<RptCampaignResultPojo> findByUserNick(Long userId, String userNick, long timeLength) {

        List<CampaignPlay> campaigns = CampaignDao.findbyNick(userNick);

        if (CommonUtils.isEmpty(campaigns)) {
            log.warn("No campaign records for " + userNick);
            return new ArrayList<RptCampaignResultPojo>();
        }

        List<RptCampaignResultPojo> resultList = new ArrayList<RptCampaignResultPojo>();

        RptCampaignUpdateTs updateTs = RptCampaignUpdateTs.findById(userId);

        Long day = updateTs == null ? DateUtil.formCurrDate() - DateUtil.DAY_MILLIS : updateTs.getLastUpdateTime();
        for (CampaignPlay campaign : campaigns) {
            RptCampaignResultPojo rptCampaignResult = findTotalByCampaignId(userNick, campaign, day, timeLength);
            rptCampaignResult.setBasicInfo(campaign);
            resultList.add(rptCampaignResult);
        }

        return resultList;
    }

    public static RptCampaignResultPojo findTotalByCampaignId(String userNick, CampaignPlay campaign, Long day,
            long timeLength) {

        // RptCampaignResultPojo result =
        // RptCampaignResultPojo.findByCampaignIdAndTimeLength(campaign.getCampaignId(),
        // timeLength);

        RptCampaignResultPojo result = findResultPojoByCampaignIdAndTimeLength(campaign, timeLength);

        if (result != null && result.getDay() >= day) {
            log.warn("RptCampaignResultPojo hit in DB!!!");
            return result;
        }

        return calculatePojoByCampaignId(campaign.getUserId(), userNick, campaign, day, timeLength);
    }

    public static RptCampaignResultPojo findTotalByCampaignId(Long userId, String userNick, CampaignPlay campaign,
            long timeLength) {

        // RptCampaignResultPojo result =
        // RptCampaignResultPojo.findByCampaignIdAndTimeLength(campaign.getCampaignId(),
        // timeLength);
        RptCampaignUpdateTs updateTs = RptCampaignUpdateTs.findByUserId(userId);
        if (updateTs == null) {
            return null;
        }

        Long day = updateTs.getLastUpdateTime();

        return calculatePojoByCampaignId(campaign.getUserId(), userNick, campaign, day, timeLength);
    }

    static String QueryForResultPojoByCampaignIdAndTimeLength = "select `campaignId`,`cost`,`indirectpaycount`,`directpaycount`,`indirectpayamount`,"
            + "`directpayamount`,`impressions`,`aclick`,`favshopcount`,`favitemcount` from "
            + RptCampaignResultPojo.TABLE_NAME + " where campaignId = ? and timeLength = ? ";

    public static RptCampaignResultPojo findResultPojoByCampaignIdAndTimeLength(final CampaignPlay campaign,
            final long timeLength) {

        RptCampaignResultPojo campaginResult = new JDBCExecutor<RptCampaignResultPojo>(
                QueryForResultPojoByCampaignIdAndTimeLength, campaign.getCampaignId(), timeLength) {

            @Override
            public RptCampaignResultPojo doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {

                    RptCampaignResultPojo result = new RptCampaignResultPojo(campaign.getNick(), rs.getLong(1), 0L,
                            timeLength, rs.getDouble(2), rs.getDouble(3), rs.getDouble(4), rs.getLong(5),
                            rs.getLong(6), rs.getLong(7), rs.getLong(8), rs.getInt(9), rs.getInt(10));
                    result.setBasicInfo(campaign);
                    return result;
                }
                return null;
            }
        }.call();

        return campaginResult;

    }

    static String QueryForTotalResultByCampaignId = "select campaignId, sum(cost),sum(indirectpay), sum(directpay), "
            + "  sum(indirectpaycount), sum(directpaycount),sum(impressions), sum(click), sum(favshopcount), "
            + "sum(favitemcount) from RptCampaignBaseNew_  where campaignId = ? and searchtypeId = 3 and sourceId = 4  "
            + "and dateTime >= ? ";

    public static RptCampaignResultPojo calculatePojoByCampaignId(Long userId, final String userNick,
            final CampaignPlay campaign, final Long day, final long timeLength) {

        long startTs = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS * timeLength;

        if (System.currentTimeMillis() - DateUtil.formCurrDate() < 5 * DateUtil.ONE_HOUR_MILLIS) {
            startTs = startTs - DateUtil.DAY_MILLIS;
        }

        RptCampaignResultPojo campaginResult = new JDBCExecutor<RptCampaignResultPojo>(rptCampaignBaseDispatcher,
                QueryForTotalResultByCampaignId, campaign.getCampaignId(), startTs) {

            @Override
            public RptCampaignResultPojo doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {

                    return new RptCampaignResultPojo(userNick, rs.getLong(1), day, timeLength, rs.getDouble(2),
                            rs.getDouble(3), rs.getDouble(4), rs.getLong(5), rs.getLong(6), rs.getLong(7),
                            rs.getLong(8), rs.getInt(9), rs.getInt(10));
                }
                return null;
            }
        }.call();

        if (campaginResult == null) {
            /**
             * delete the saved result pojo
             */
            deleteResultPojoByCampaignId(campaign.getCampaignId());
        } else {
            /**
             * update result pojo
             */
            campaginResult.setBasicInfo(campaign);
            campaginResult.jdbcSave(userId);
        }

        return campaginResult;
    }

    public static String DeleteResultPojoByCampaignId = "delete from " + RptCampaignResultPojo.TABLE_NAME
            + " where campaignId = ?";

    public static boolean deleteResultPojoByCampaignId(Long campaignId) {
        return rptCampaignBaseDispatcher.update(DeleteResultPojoByCampaignId, campaignId) > 0L;

    }

    static String QueryForSumClickLastDays = "select campaignId, sum(click)  from RptCampaignBaseNew_ where nick =  ? and dateTime >= ? group by campaignId;";

    public static Map<Long, Long> sumClickLastDays(Long userId, String nick, Long startTs) {

        return new JDBCExecutor<Map<Long, Long>>(QueryForSumClickLastDays, nick, startTs) {

            @Override
            public Map<Long, Long> doWithResultSet(ResultSet rs) throws SQLException {

                Map<Long, Long> res = new HashMap<Long, Long>();
                while (rs.next()) {
                    res.put(rs.getLong(1), rs.getLong(2));
                }
                return res;
            }
        }.call();
    }
}
