
package dao.campaign;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.campaign.CampaignPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;
import utils.PlayUtil;
import cache.CampaignTitleCache;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Campaign;

public class CampaignDao {

    public final static Logger log = LoggerFactory.getLogger(CampaignDao.class);


    // public static long campaignNum(Long userId) {
    // return CampaignPlay.count("userId = ? ", userId);
    // }

    // public static List<CampaignPlay> findByUserId(Long userId) {
    // return CampaignPlay.find("userId = ? ", userId).fetch();
    // }

    // public static List<CampaignPlay> findByUserNick(String userNick) {
    // return CampaignPlay.find("nick = ? ", userNick).fetch();
    // }

    static String QueryForUserHasCampaign = "select 1 from " + CampaignPlay.TABLE_NAME + " where userId = ? limit 1";

    public static boolean hasCampaign(Long userId) {
        // return CampaignPlay.find("userId = ? ", userId).first() != null;
        return JDBCBuilder.singleLongQuery(QueryForUserHasCampaign, userId) > 0;

    }

    // public static boolean addCampaign(Long userId, String title){
    //
    // }

    static String QueryForCheckCampaignId = "select campaignId from " + CampaignPlay.TABLE_NAME
            + " where userId = ?  and campaignId = ?";

    public static boolean checkCampaignId(Long userId, Long campaignId) {
        return JDBCBuilder.singleLongQuery(QueryForCheckCampaignId, userId, campaignId) > 0;
    }

    public static boolean saveCampaigns(User user, List<Campaign> campaignList) {

        return saveCampaigns(user, campaignList, 0);

    }

    public static boolean saveCampaigns(User user, List<Campaign> campaignList, int retryTime) {

        if (retryTime++ > 5) {
            return false;
        }
        if (CommonUtils.isEmpty(campaignList)) {
            return false;
        }
        for (Campaign campaign : campaignList) {
            if (!(new CampaignPlay(user.getId(), campaign).jdbcSave())) {
                log.info("Save campaign error!!! for " + user.getUserNick());
                PlayUtil.sleepQuietly(DateUtil.THREE_SECONDS_MILLIS);
                return saveCampaigns(user, campaignList, retryTime++);
            }
        }
        return true;
    }

    public static String CAMPAIGN_SQL = " campagin.campaignId, campagin.userId, campagin.nick, campagin.title, campagin.settleStatus, campagin.onlineStatus, campagin.settleReason, "
            + "campagin.createTime, campagin.modifiedTime, campagin.budget, campagin.keywordMatchType, campagin.platForm, campagin.areaIds, campagin.demographicIds, campagin.addPrices, campagin.campaignStatus, campagin.campaignType, campagin.smooth ";

    static List<CampaignPlay> parseCampaigns(ResultSet rs) throws SQLException {
        List<CampaignPlay> campaignList = new ArrayList<CampaignPlay>();
        while (rs.next()) {
            campaignList.add(new CampaignPlay(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs
                    .getInt(5), rs.getInt(6), rs.getInt(7), rs.getLong(8), rs.getLong(9), rs.getInt(10), rs.getInt(11),
                    rs.getInt(12), rs.getString(13), rs.getString(14), rs.getString(15), rs.getInt(16), rs.getInt(17),
                    rs.getBoolean(18)));
        }
        return campaignList;
    }

    public static CampaignPlay parseCampaign(ResultSet rs) throws SQLException {

        if (rs.next()) {
            return new CampaignPlay(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getString(4), rs.getInt(5),
                    rs.getInt(6), rs.getInt(7), rs.getLong(8), rs.getLong(9), rs.getInt(10), rs.getInt(11),
                    rs.getInt(12), rs.getString(13), rs.getString(14), rs.getString(15), rs.getInt(16), rs.getInt(17),
                    rs.getBoolean(18));
        }
        return null;
    }


    static String QueryForFindByUserId = " select " + CAMPAIGN_SQL + " from " + CampaignPlay.TABLE_NAME
            + " as campagin where campagin.userId = ? order by campagin.modifiedTime desc ";

    public static List<CampaignPlay> findByUserId(Long userId) {

        // log.info("FindByNick:" + userNick);
        return new JDBCExecutor<List<CampaignPlay>>(QueryForFindByUserId, userId) {
            @Override
            public List<CampaignPlay> doWithResultSet(ResultSet rs) throws SQLException {
                return parseCampaigns(rs);
            }
        }.call();
    }

    static String QueryForFindByUserNick = " select " + CAMPAIGN_SQL + " from " + CampaignPlay.TABLE_NAME
            + " as campagin where campagin.nick = ? order by campagin.modifiedTime desc ";

    public static List<CampaignPlay> findbyNick(String userNick) {
        if (org.apache.commons.lang.StringUtils.isEmpty(userNick)) {
            return ListUtils.EMPTY_LIST;
        }
        // log.info("FindByNick:" + userNick);
        return new JDBCExecutor<List<CampaignPlay>>(QueryForFindByUserNick, userNick) {
            @Override
            public List<CampaignPlay> doWithResultSet(ResultSet rs) throws SQLException {
                return parseCampaigns(rs);
            }
        }.call();
    }

    static String QueryForMatchTypeByCampaignId = "select keywordMatchType from " + CampaignPlay.TABLE_NAME
            + " where campaignId  = ?";

    public static long findKeywordMatchTypeByCampaignId(Long campaignId) {
        return new JDBCExecutor<Long>(QueryForMatchTypeByCampaignId, campaignId) {
            @Override
            public Long doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return Long.valueOf(CampaignPlay.MatchType.Exact_Match);
            }
        }.call();
    }

    static String QueryForAddInBusCampaign = "select campaignId from " + CampaignPlay.TABLE_NAME
            + " where nick = ? and (onlineStatus & 8) > 0 ";

    public static List<Long> findAddInBusCampaign(Long userId, String userNick) {

        return new JDBCExecutor<List<Long>>(QueryForAddInBusCampaign, userNick) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> result = new ArrayList<Long>();
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
                return result;
            }
        }.call();
    }

    static String FIND_CAMPAIGNID_WITH_TITILE = "select campaignId, title from " + CampaignPlay.TABLE_NAME
            + " where nick = ? ";

    public static String findCacheCampaignTitle(String nick, Long campaignId) {

        String campaignTitle = CampaignTitleCache.get(campaignId);

        if (campaignTitle == null) {

            new JDBCExecutor<Void>(FIND_CAMPAIGNID_WITH_TITILE, nick) {
                @Override
                public Void doWithResultSet(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        CampaignTitleCache.putToCache(rs.getLong(1), rs.getString(2));
                    }
                    return null;
                }
            }.call();

            campaignTitle = CampaignTitleCache.get(campaignId);
            if (campaignTitle != null) {
                // log.warn("Campaign Title hit in cache after put campaign title!!!");
            }
        } else {
            // log.warn("Campaign Title hit in cache!!!");
        }

        return campaignTitle;
    }

}
