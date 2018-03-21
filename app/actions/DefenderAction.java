/**
 * 
 */
package actions;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Status;
import models.defense.DefenderOption;
import models.trade.TradeDisplay;
import models.traderate.TradeRatePlay;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.CommentType;
import actions.catunion.UserRateSpiderAction.RateSort;
import actions.catunion.UserRateSpiderAction.UserRateInfo;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.UserCredit;

import dao.defense.BlackListBuyerDao;
import dao.trade.TradeDisplayDao;
import dao.trade.TradeRatePlayDao;

/**
 * @author navins
 * @date 2013-6-8 下午8:55:53
 */
public class DefenderAction {

    private static final Logger log = LoggerFactory.getLogger(DefenderAction.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static DefenderRet checkSellerConfig(DefenderOption option, Trade trade, String buyerNick) {
    	 if (option == null) {
             // option = DefenderOption.DEFAULT_OPTION;
             return DefenderRet.NO_DEFENDER_OPTION;
         }

         DefenderRet ret = null;
         ret = checkPriceLimit(option.getPriceLimit(), trade.getPayment());
         if (ret.isChaping()) {
             return ret;
         }
         
         ret = checkBuyerArea(trade.getReceiverState(), option.getExcludeAreas(), trade.getTid());
         if(ret.isChaping()) {
         	log.info("trade ["+trade.getTid()+"], defend by rule with excludeArea = "+ 
         			option.getExcludeAreas() + " and receiver_state = " + 
         			trade.getReceiverState() + ret.getMsg());
         	return ret;
         }

         ret = checkGiveMeChaping(option, buyerNick);
         if (ret.isChaping()) {
             return ret;
         }

         ret = checkRefundNum(option, buyerNick);
         if (ret.isChaping()) {
             return ret;
         }

         log.info("[DefenderAction] checkSellerConfig not defense by rule: userId=" + option.getUserId() + "  buyerNick="
                 + buyerNick);
         return DefenderRet.NORMAL;
    }
    
    public static DefenderRet checkDefender(DefenderOption option, UserRateInfo info, Trade trade) {
        if (option == null) {
            // option = DefenderOption.DEFAULT_OPTION;
            return DefenderRet.NO_DEFENDER_OPTION;
        }

        DefenderRet ret = null;

        ret = checkSellerAllow(option.isAllowSeller(), info.isSeller());
        if(ret.isChaping()) {
        	log.info("trade ["+trade.getTid()+"], seller ["+option.getUserId()+"]," +
        			" buyer ["+info.getUserNick()+"] defend by rule" + ret.getMsg());
        	return ret;
        }
        
        /*ret = checkBuyerWeekMonthHalfYearCredit(option, info);
        if(ret.isChaping()) {
        	log.info("trade ["+trade.getTid()+"], seller ["+option.getUserId()+"]," +
        			" buyer ["+info.getUserNick()+"] defend by rule" + ret.getMsg());
        	return ret;
        }*/
        
        ret = checkVerify(option.isVerifyLimit(), info.isVerify());
        if (ret.isChaping()) {
            return ret;
        }

        ret = checkBuyerCredit(option, info, trade.getBuyerNick());
        if (ret.isChaping()) {
            return ret;
        }

        ret = checkVipLevel(option, info.getUserId());
        if (ret.isChaping()) {
            return ret;
        }

        // ret = checkAddBlackListTimes(option, info.getUserNick());

        // ret = checkRecentChaping(option, info.getUserId());
        // if (ret.isChaping()) {
        // return ret;
        // }

        if (info.isSeller() == false) {
            ret = checkRegDays(option.getRegDays(), info.getRegisterTime());
            if (ret.isChaping()) {
                return ret;
            }

            ret = checkGoodRate(option.getGoodCreditRateLimit(), info.getGoodCreditRate());
            if (ret.isChaping()) {
                return ret;
            }

            // ret = checkPositiveRate(option, info);
            // if (ret.isChaping()) {
            // return ret;
            // }

            ret = checkNegComment(option, info);
            if (ret.isChaping()) {
                return ret;
            }
        }
        log.info("[DefenderAction] checkDefender not defense by rule: userId=" + option.getUserId() + "  buyerNick="
                + info.getUserNick());
        return DefenderRet.NORMAL;
    }
    
    public static DefenderRet checkNewDefender(DefenderOption option, com.taobao.api.domain.User user, Long tid) {
        if (option == null) {
            // option = DefenderOption.DEFAULT_OPTION;
            return DefenderRet.NO_DEFENDER_OPTION;
        }
        if(user == null) {
        	return DefenderRet.NORMAL;
        }
        DefenderRet ret = null;

        boolean isSeller = isSeller(user);
        ret = checkSellerAllow(option.isAllowSeller(), isSeller);
        if(ret.isChaping()) {
        	log.info("trade ["+tid+"], seller ["+option.getUserId()+"]," +
        			" buyer ["+user.getNick()+"] defend by rule" + ret.getMsg());
        	return ret;
        }
        
        ret = checkVerify(option.isVerifyLimit(), true);
        if (ret.isChaping()) {
            return ret;
        }

        ret = checkRegDays(option.getRegDays(), user.getCreated());
        if (ret.isChaping()) {
            return ret;
        }

        ret = checkGoodRate(option, user);
        if (ret.isChaping()) {
            return ret;
        }
        
        log.info("[DefenderAction] checkDefender not defense by rule: userId=" + option.getUserId() + "  buyerNick="
                + user.getNick());
        return DefenderRet.NORMAL;
    }

    // priceLimit 以元为单位
    private static DefenderRet checkPriceLimit(double priceLimit, String totalFee) {
    	log.info("checkPriceLimit: priceLimit = " + priceLimit + " and trade totalFee = " + totalFee);
        if (priceLimit > 0) {
            double fee = NumberUtils.toDouble(totalFee, 0);
            if (fee > 0 && fee < priceLimit) {
                return DefenderRet.PRICE_BELOW_LIMIT;
            }
        }

        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkAddBlackListTimes(DefenderOption option, String userNick) {
        if (option.getAddBlackListTimes() > 0) {
            long count = BlackListBuyerDao.countDistinctUser(userNick);
            if (count >= option.getAddBlackListTimes()) {
                return DefenderRet.ADD_BLACKLIST_TIME_HIGH;
            }
        }

        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkPositiveRate(DefenderOption option, UserRateInfo info) {
        RateSort halfYearCredit = info.getHalfYearCredit();
        if (halfYearCredit == null) {
            log.error("UserRateInfo halfYearCredit NULL! info: " + info);
            return DefenderRet.NORMAL;
        }
        if (option.getHalfYearTotalNum() > 0 && halfYearCredit.getTotalCreditNum() > 0) {
            if (halfYearCredit.getTotalCreditNum() <= option.getHalfYearTotalNum()) {
                if (option.getHalfYearNonPositiveNum() > 0
                        && halfYearCredit.getNonPositiveNum() > option.getHalfYearNonPositiveNum()) {
                    return DefenderRet.HALFYEAR_NONPOSITIVE_HIGH;
                }
            } else {
                double positiveRate = 100.0 - halfYearCredit.getNonPositiveNum() * 100.0
                        / halfYearCredit.getTotalCreditNum();
                if (option.getPositiveRate() > 0 && positiveRate < option.getPositiveRate()) {
                    return DefenderRet.HALFYEAR_NONPOSITIVE_HIGH;
                }
            }
        }
        // else if (halfYearCredit.getTotalCreditNum() == 0 && option.isAllowNoCreditBuyer() == false) {
        // return DefenderRet.DISALLOW_NOCREDIT_BUYER;
        // }
        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkVipLevel(DefenderOption option, long userId) {
        // TODO 根据vipLevel限制
        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkGiveMeChaping(DefenderOption option, UserRateInfo info) {
        // 查该用户是否给我差评
        if (option.getRecentMeChapingCount() > 0) {
            List<TradeRatePlay> list = TradeRatePlayDao.findByUserIdNickBadComment(option.getUserId(),
                    info.getUserNick());
            if (list != null && list.size() >= option.getRecentMeChapingCount()) {
                // 给过差评
                return DefenderRet.RECENT_CHAPING_ME_HIGH;
            }
        }
        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkGiveMeChaping(DefenderOption option, String buyerNick) {
        // 查该用户是否给我差评
        if (option.getRecentMeChapingCount() > 0) {
            List<TradeRatePlay> list = TradeRatePlayDao.findByUserIdNickBadComment(option.getUserId(),
            		buyerNick);
            if (list != null && list.size() >= option.getRecentMeChapingCount()) {
                // 给过差评
                return DefenderRet.RECENT_CHAPING_ME_HIGH;
            }
        }
        return DefenderRet.NORMAL;
    }
    
    private static DefenderRet checkRefundNum(DefenderOption option, String buyerNick) {
        // 判断退款次数是否超过 TRADE_CLOSED(付款以后用户退款成功，交易自动关闭)
        List<Integer> status = new ArrayList<Integer>();
        status.add(Status.TRADE_STATUS.TRADE_CLOSED.ordinal());
        List<TradeDisplay> list = TradeDisplayDao.findByUserIdBuyerNick(option.getUserId(), buyerNick, 0L,
                status);
        int count = 0;
        if (!CommonUtils.isEmpty(list)) {
            count = list.size();
        }
        if (option.getRefundNum() > 0 && count > option.getRefundNum()) {
            return DefenderRet.REFUNDNUM_PASS_LIMIT;
        }
        return DefenderRet.NORMAL;
    }
    
    private static DefenderRet checkRefundNum(DefenderOption option, UserRateInfo info) {
        // 判断退款次数是否超过 TRADE_CLOSED(付款以后用户退款成功，交易自动关闭)
        List<Integer> status = new ArrayList<Integer>();
        status.add(Status.TRADE_STATUS.TRADE_CLOSED.ordinal());
        List<TradeDisplay> list = TradeDisplayDao.findByUserIdBuyerNick(option.getUserId(), info.getUserNick(), 0L,
                status);
        int count = 0;
        if (!CommonUtils.isEmpty(list)) {
            count = list.size();
        }
        if (option.getRefundNum() > 0 && count > option.getRefundNum()) {
            return DefenderRet.REFUNDNUM_PASS_LIMIT;
        }
        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkVerify(boolean verifyLimit, boolean verify) {
        if (verifyLimit == true && verify == false) {
            return DefenderRet.NOT_VARIFIED;
        }
        return DefenderRet.NORMAL;
    }
    
    public static DefenderRet checkSellerAllow(boolean allowSeller, boolean isSeller) {
        if (allowSeller == false && isSeller == true) {
            return DefenderRet.SELLER_NOT_ALLOWED;
        }
        return DefenderRet.NORMAL;
    }
    
    public static boolean isSeller(com.taobao.api.domain.User user) {
        if (user == null) {
            return false;
        }
        UserCredit sellerCredit = user.getSellerCredit();
        if(sellerCredit == null) {
        	return false;
        }
        return ((sellerCredit.getGoodNum() > 0) || (sellerCredit.getLevel() > 0) ||
        		(sellerCredit.getScore() > 0) || (sellerCredit.getTotalNum() > 0));
    }
    
    public static DefenderRet checkBuyerArea(String buyerArea, String sellerExcludeAreas, Long tid) {
    	log.info("checkBuyerArea for trade = "+tid+", buyerArea = " +buyerArea+
    			" sellerExcludeAreas = " + sellerExcludeAreas);
    	// 卖家未设置任何排除区域
    	if(StringUtils.isEmpty(sellerExcludeAreas)) {
    		return DefenderRet.NORMAL;
    	}
    	// 一般意味着是虚拟
    	if(StringUtils.isEmpty(buyerArea)) {
    		return DefenderRet.NORMAL;
    	}
    	if(sellerExcludeAreas.indexOf(buyerArea) >= 0) {
    		return DefenderRet.AREA_NOT_ALLOW;
    	}
        return DefenderRet.NORMAL;
    }
    
    public static DefenderRet checkBuyerWeekMonthHalfYearCredit(DefenderOption option, UserRateInfo info) {
    	// 如果是卖家，则此条规则不适用
    	if(info.isSeller()) {
    		return DefenderRet.NORMAL;
    	}
    	if(option.getBuyerWeekCreditLimit() > 0) {
    		RateSort weekCredit = info.getWeekCredit();
    		if(weekCredit != null && weekCredit.getTotalCreditNum() > option.getBuyerWeekCreditLimit()) {
    			return DefenderRet.BUYER_WEEK_CREDIT_OVER;
    		}
    	}
    	if(option.getBuyerMonthCreditLimit() > 0) {
    		RateSort monthCredit = info.getMonthCredit();
    		if(monthCredit != null && monthCredit.getTotalCreditNum() > option.getBuyerMonthCreditLimit()) {
    			return DefenderRet.BUYER_MONTH_CREDIT_OVER;
    		}
    	}
    	if(option.getBuyerHalfYearCreditLimit() > 0) {
    		RateSort halfYearCredit = info.getHalfYearCredit();
    		if(halfYearCredit != null && halfYearCredit.getTotalCreditNum() > option.getBuyerHalfYearCreditLimit()) {
    			return DefenderRet.BUYER_HALFYEAR_CREDIT_OVER;
    		}
    	}

        return DefenderRet.NORMAL;
    }
    
    private static DefenderRet checkBuyerCredit(DefenderOption option, UserRateInfo info, String buyerNick) {
        if (StringUtils.isEmpty(info.getTotalCredit())) {
            log.error("[TradeDefenseCaller][checkBuyerCredit] totalCredit NULL!  UserRateInfo: " + info);
            return DefenderRet.NORMAL;
        }
        int credit = Integer.valueOf(info.getTotalCredit());
        if (credit < 0) {
            log.error("[TradeDefenseCaller][checkBuyerCredit] totalCredit < 0!  UserRateInfo: " + info);
            return DefenderRet.NORMAL;
        }
        if (credit == 0) {
            if (option.isAllowNoCreditBuyer() == true) {
                return DefenderRet.NORMAL;
            } else {
                return DefenderRet.CREDIT_BELOW_LIMIT;
            }
        }
        if (option.getBuyerCreditLimt() > 0 && option.getBuyerCreditLimt() > credit) {
            log.warn("[TradeDefenseCaller][checkBuyerCredit] buyer: " + info.getUserNick() + "totalCredit: " + credit
                    + " below limit: " + option.getBuyerCreditLimt());
            return DefenderRet.CREDIT_BELOW_LIMIT;
        }

        // 计算买家给出好评率
        /*if (credit > 0 && option.getPositiveRate() > 0) {
            int badCount = countComments(info.getUserId(), buyerNick, CommentType.Negative)
                    + countComments(info.getUserId(), buyerNick, CommentType.Neutral);

            // 很奇怪，有些时候会出现badcount > credit的情况
            if (badCount > 0 && badCount <= credit) {
                double positiveRate = 100.0 - badCount * 100.0 / credit;
                log.info("[checkPositiveRate]badcomment count: "+badCount+", credit="+credit+", " +
                		"positiveRate="+positiveRate+", option.getPositiveRate()="+option.getPositiveRate()+"," +
                				" userNick="+info.getUserNick()+", buyer="+buyerNick);
                if (positiveRate < option.getPositiveRate()) {
                    return DefenderRet.POSITIVE_RATE_LOW;
                }
            }
        }*/

        return DefenderRet.NORMAL;
    }
    
    @Deprecated
    private static DefenderRet checkRecentChaping(DefenderOption option, long userId) {
        int recentGivenChapin = 0;
        int recentGivenMeChapin = 0;
        boolean hasBuyProductWithGoodComment = false;
        int size = 40;
        if (option.getRecentTimes() > 0) {
            size = option.getRecentTimes();
        }
        List<CommentInfo> comments = buildCommentList(userId, size);
        for (int i = 0; i < comments.size(); i++) {
            CommentInfo ci = comments.get(i);
            if (ci.getRate() <= 0) {
                recentGivenChapin++;
                if (option.getUserId() > 0 && ci.getUserId() == option.getUserId()) {
                    recentGivenMeChapin++;
                }
            }
            if (option.getUserId() > 0 && ci.getRate() == 1 && ci.getUserId() == option.getUserId()) {
                hasBuyProductWithGoodComment = true;
            }
        }

        if (option.getRecentChapingCount() > 0 && recentGivenChapin >= option.getRecentChapingCount()) {
            // 给过差评
            return DefenderRet.RECENT_CHAPING_HIGH;
        }
        if (option.getRecentMeChapingCount() > 0 && recentGivenMeChapin >= option.getRecentMeChapingCount()) {
            // 对我给过差评
            return DefenderRet.RECENT_CHAPING_ME_HIGH;
        }
        if (option.isHasBuyProductWithGoodComment() == true && hasBuyProductWithGoodComment == true) {
            return DefenderRet.RECENT_BUYPRODUCT_WITH_GOOD_COMMENT;
        }
        return DefenderRet.NORMAL;
    }

    static List<CommentInfo> buildCommentList(Long buyerId, int size) {
        if (Play.mode.isDev()) {
            return ListUtils.EMPTY_LIST;
        }
        List<CommentInfo> finalRes = new ArrayList<CommentInfo>();

        int fetchTimes = (size % 40 == 0) ? size / 40 : size / 40 + 1;
        for (int k = 1; k <= fetchTimes; k++) {
            String res = fetctchCommentJSON(buyerId, k, CommentType.All, true);
            JsonNode node = JsonUtil.readJsonResult(res);
            List<JsonNode> titles = node.findValues("title");
            List<JsonNode> contents = node.findValues("content");
            List<JsonNode> rates = node.findValues("rate");
            List<JsonNode> userIds = node.findValues("userId");
            int fetchsize = Math.min(titles.size(), contents.size());
            for (int i = 0; i < fetchsize; i++) {
                finalRes.add(new CommentInfo(titles.get(i).getTextValue(), contents.get(i).getTextValue(), NumberUtil
                        .parserInt(rates.get(i).getTextValue(), 2), userIds.get(i).getLongValue()));
            }
            if (fetchsize < 40) {
                break;
            }
        }

        return finalRes;
    }
    
    
    static class BadCommentCache {
        public static Integer getCommentCount(String key) {
            if (StringUtils.isEmpty(key)) {
                return -1;
            }
            try {
                Object obj = Cache.get(key);
                if (obj != null) {
                    return (Integer) obj;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return -1;
        }

        public static void setCommentCount(String key, Integer count) {
            if (StringUtils.isEmpty(key)) {
                return;
            }
            try {
                Cache.set(key, count, "12h");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static int countComments(Long buyerId, String buyerNick, int commentType) {
        int count = 0;
        String key = "COMMENT-" + buyerNick.trim() + "-" + commentType;
        int cacheCount = BadCommentCache.getCommentCount(key);
        if (cacheCount >= 0) {
            return cacheCount;
        }

        String content = UserRateSpiderAction.doSpiderCommentList(buyerId, buyerNick, 1, commentType, true);
        if (!StringUtils.isEmpty(content)) {
            if (!content.startsWith("{\"status\":\"checkcode")) {
                try {
                	if(!content.startsWith("{")) {
                		log.error("countComments return content is not a json object!!!");
                		return 0;
                	}
                    JSONObject json = new JSONObject(content);
                    log.info("doSpiderCommentList: " + content);
                    JSONArray jsonArray = json.getJSONArray("rateListDetail");
                    count += jsonArray.length();

                    // cache it
                    BadCommentCache.setCommentCount(key, count);
                } catch (JSONException e) {
                    log.error(e.getMessage() + " [checkPositiveRate] cc: " + content);
                }
            } else {
                log.info("[checkPositiveRate]need checkcode! buyerNick: " + buyerNick);
            }
        } else {
            log.info("[checkPositiveRate]no valid res! buyerNick: " + buyerNick);
        }
        log.info("countComments for user : " + buyerNick + " with commentType = " +
        		commentType + " and count = " + count);
        return count;
    }

    static List<CommentInfo> buildCommentList(Long buyerId, int size, int commentType) {
        if (Play.mode.isDev()) {
            return ListUtils.EMPTY_LIST;
        }
        List<CommentInfo> finalRes = new ArrayList<CommentInfo>();

        String res = null;
        try {
            int fetchTimes = (size % 40 == 0) ? size / 40 : size / 40 + 1;
            for (int k = 1; k <= fetchTimes; k++) {
                res = fetctchCommentJSON(buyerId, k, commentType, true);
                JsonNode node = JsonUtil.readJsonResult(res);
                List<JsonNode> titles = node.findValues("title");
                List<JsonNode> contents = node.findValues("content");
                List<JsonNode> rates = node.findValues("rate");
                List<JsonNode> userIds = node.findValues("userId");
                int fetchsize = Math.min(titles.size(), contents.size());
                for (int i = 0; i < fetchsize; i++) {
                    finalRes.add(new CommentInfo(titles.get(i).getTextValue(), contents.get(i).getTextValue(),
                            NumberUtil.parserInt(rates.get(i).getTextValue(), 2), userIds.get(i).getLongValue()));
                }
                if (fetchsize < 40) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn(">>>>[buildCommentList] error: " + res);
            log.error(e.getMessage(), e);
        }

        return finalRes;
    }

    private static String fetctchCommentJSON(Long userId, int pn, int commentType, boolean hasContent) {
        String cacheKey = String.format("doQueryCommentList%d_%d_%d_%s", userId, pn, commentType,
                String.valueOf(hasContent));
        String json = (String) Cache.get(cacheKey);
        if (json == null) {
            json = UserRateSpiderAction.doSpiderCommentList(userId, StringUtils.EMPTY,  pn, commentType, hasContent);
            Cache.set(cacheKey, json);
        }
        return json;
    }

    private static DefenderRet checkRegDays(int regDaysLimit, String registerTime) {
        // 买家注册时间限制
        String regStr = registerTime;
        if (regDaysLimit > 0 && !StringUtils.isEmpty(regStr)) {
            try {
                Date regDate = sdf.parse(regStr);
                long regDays = (new Date().getTime() - regDate.getTime()) / 86400000; // 24*3600000
                if (regDays < regDaysLimit) {
                    return DefenderRet.REGDAYS_TOO_SHORT;
                }
            } catch (ParseException e) {
                log.error("checkChapin parse Date error : " + regStr);
            }
        }
        return DefenderRet.NORMAL;
    }
    
    private static DefenderRet checkRegDays(int regDaysLimit, Date regDate) {
    	if(regDate == null) {
    		return DefenderRet.NORMAL;
    	}
    	Long regTs = regDate.getTime();
        if (regDaysLimit > 0) {
        	long regDays = (new Date().getTime() - regTs) / 86400000; // 24*3600000
            if (regDays < regDaysLimit) {
                return DefenderRet.REGDAYS_TOO_SHORT;
            }
        }
        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkGoodRate(double goodCreditRateLimit, String goodCreditRateStr) {
        double goodCreditRate = 100.0;
        if (!StringUtils.isEmpty(goodCreditRateStr)) {
            goodCreditRateStr = goodCreditRateStr.replaceAll("%", "");
            goodCreditRate = NumberUtil.parserDouble(goodCreditRateStr, 100.0);
        }
        if (goodCreditRate > 0 && goodCreditRateLimit > goodCreditRate) {
            // 好评率低于设定值，好评率为0的不在这里比较
            return DefenderRet.GOODRATE_TOO_LOW;
        }
        return DefenderRet.NORMAL;
    }
    
    private static DefenderRet checkGoodRate(DefenderOption option, com.taobao.api.domain.User user) {
        UserCredit buyerCredit = user.getBuyerCredit();
        if(buyerCredit == null) {
        	return DefenderRet.NORMAL;
        }
        long goodNum = buyerCredit.getGoodNum();
        long totalNum = buyerCredit.getTotalNum();
        long score = buyerCredit.getScore();
        if (score == 0) {
            if (option.isAllowNoCreditBuyer() == true) {
                return DefenderRet.NORMAL;
            } else {
                return DefenderRet.CREDIT_BELOW_LIMIT;
            }
        }
        if (option.getBuyerCreditLimt() > 0 && option.getBuyerCreditLimt() > score) {
            log.warn("[TradeDefenseCaller][checkBuyerCredit] buyer: " + user.getNick() + "totalCredit: " + score
                    + " below limit: " + option.getBuyerCreditLimt());
            return DefenderRet.CREDIT_BELOW_LIMIT;
        }
        double goodCreditRate = 100.0 * goodNum / totalNum;
        if (goodCreditRate > 0 && option.getGoodCreditRateLimit() > goodCreditRate) {
            // 好评率低于设定值，好评率为0的不在这里比较
            return DefenderRet.GOODRATE_TOO_LOW;
        }
        return DefenderRet.NORMAL;
    }

    private static DefenderRet checkNegComment(DefenderOption option, UserRateInfo info) {
        RateSort halfYearCredit = info.getHalfYearCredit();
        if (halfYearCredit == null) {
            return DefenderRet.NORMAL;
        }
        double halfYearPositiveRate = 100 - halfYearCredit.getNonPositiveNum() * 100.0
                / halfYearCredit.getTotalTradeNum();
        if (option.getPositiveRate() > 0 && halfYearPositiveRate < option.getPositiveRate()) {
            return DefenderRet.HALFYEAR_POSITIVE_LOW;
        }

        if (option.getHalfYearTotalNum() > 0 && halfYearCredit.getTotalTradeNum() <= option.getHalfYearTotalNum()) {
            if (option.getHalfYearNonPositiveNum() > 0
                    && halfYearCredit.getNonPositiveNum() > option.getHalfYearNonPositiveNum()) {
                return DefenderRet.HALFYEAR_NONPOSITIVE_HIGH;
            }
        }
        return DefenderRet.NORMAL;
    }

    public static enum DefenderRet implements Serializable {
        NORMAL(false, "正常"), NO_DEFENDER_OPTION(false, "卖家未设置差评防御"), DEFENDER_AUTOCLOSE_OFF(false, "卖家未开启差评防御选项"), RECENT_BUYPRODUCT_WITH_GOOD_COMMENT(
                false, "该用户购买过本店产品并给好评"),

        NOT_VARIFIED(true, "要求用户认证，用户未认证"),
        CREDIT_BELOW_LIMIT(true, "该买家信誉值低于设定值"), 
        RECENT_CHAPING_HIGH(true, "最近给的中差评超过设定值"), 
        RECENT_CHAPING_ME_HIGH(true, "最近给过本店过中差评次数超过设定值"), 
        REGDAYS_TOO_SHORT(true, "该用户注册时间过短"), 
        GOODRATE_TOO_LOW(true, "该用户被好评率低于设定值"), 
        HALFYEAR_POSITIVE_LOW(true, "该用户最近半年中好评率低于设定值"), 
        POSITIVE_RATE_LOW(true, "该用户给出好评率低于设定值"), 
        MONTH_NONPOSITIVE_HIGH(true, "该用户最近一个月中差评次数超过设定值"), 
        HALFYEAR_NONPOSITIVE_HIGH(true, "该用户最近半年中差评次数超过设定值"), 
        REFUNDNUM_PASS_LIMIT(true, "该用户退款次数超过设定值"), 
        DISALLOW_NOCREDIT_BUYER(true, "该用户信誉为0，被设置为不允许购买"), 
        ADD_BLACKLIST_TIME_HIGH(true, "该买家被其他卖家加入黑名单次数超过设定值"), 
        PRICE_BELOW_LIMIT(true, "该买家订单金额低于设定值"), 
        SELLER_NOT_ALLOWED(true, "不允许卖家账号拍单"), 
        BUYER_WEEK_CREDIT_OVER(true, "拍手最近一周收到的评价数超过"), 
        BUYER_MONTH_CREDIT_OVER(true, "拍手最近一个月收到的评价数超过"),
        BUYER_HALFYEAR_CREDIT_OVER(true, "拍手最近半年收到的评价数超过"), 
        AREA_NOT_ALLOW(true, "不允许该地域的买家下单");

        private boolean chaping;

        private String msg;

        private DefenderRet() {

        }

        private DefenderRet(boolean chaping, String chapingmsg) {
            this.chaping = chaping;
            this.msg = chapingmsg;
        }

        public boolean isChaping() {
            return chaping;
        }

        public void setChaping(boolean chaping) {
            this.chaping = chaping;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    @JsonAutoDetect
    static class CommentInfo {
        @JsonProperty
        String title;

        @JsonProperty
        String content;

        @JsonProperty
        int rate;

        @JsonProperty
        long userId;

        public CommentInfo(String title, String content, int rate, long userId) {
            super();
            this.title = title;
            this.content = content;
            this.rate = rate;
            this.userId = userId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        @Override
        public String toString() {
            return "CommentInfo [title=" + title + ", content=" + content + ", rate=" + rate + ", userId=" + userId
                    + "]";
        }

    }

}
