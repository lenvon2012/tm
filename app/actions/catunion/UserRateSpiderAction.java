
package actions.catunion;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import job.writter.batchUserRateGetJob;

import models.user.UserIdNick;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Http.StatusCode;
import proxy.CommonProxyPools;
import proxy.HttphostWrapper;
import proxy.IProxy;
import proxy.NewProxyTools;
import utils.PlayUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.JsoupUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;
import com.taobao.api.domain.User;
import com.taobao.api.domain.UserCredit;

/**
 * http://ratehis.taobao.com/user-rate-UOmvyOmcWMGk4--isarchive%7Ctrue--buyerOrSeller%7C3--receivedOrPosted%7C1--gc%7Cgcc.htm
 * @author zrb
 *
 */
public class UserRateSpiderAction {

    public static final String Default_Refer = "http://i.taobao.com/my_taobao.htm";

    private static final Logger log = LoggerFactory.getLogger(UserRateSpiderAction.class);

    private static final Long sleepTs = 1100L;
    
    private static final int loopNum =3;
    
    public static User getUserByApi(String nick) {
    	Long startTs = System.currentTimeMillis();
    	if(StringUtils.isEmpty(nick)) {
    		return null;
    	}
    	
    	User user = TBUserCache.getUserRateFromCache(nick);
    	if(user != null) {
    		Long endTs = System.currentTimeMillis();
    		log.info("UserRateSpiderAction.getUserByApi: find nick in cache and took " + (endTs - startTs) + "ms");
    		return user;
    	}
    	batchUserRateGetJob.addMsg(nick);
    	CommonUtils.sleepQuietly(sleepTs);
    	for(int i = 0; i < loopNum; i++) {
    		user = TBUserCache.getUserRateFromCache(nick);
    		if(user == null) {
    			CommonUtils.sleepQuietly(sleepTs);
    		} else {
    			Long endTs = System.currentTimeMillis();
        		log.info("UserRateSpiderAction.getUserByApi: find "+nick+" by api and took " + (endTs - startTs) + "ms");
    			return user;
    		}
    	}
    	Long endTs = System.currentTimeMillis();
		log.info("UserRateSpiderAction.getUserByApi: can not find "+nick+" and took " + (endTs - startTs) + "ms");
    	return user;
    }
    
    public static UserRateInfo spiderUserRateById(UserIdNick model) {
        if (model == null) {
            return null;
        }

        UserRateInfo userRateInfo = UserRateCache.getUserRateFromCacheByNick(model.getNick());
        if (userRateInfo != null) {
            return userRateInfo;
        }
        String rateUrl = model.genRateUrl();
        //log.info("[rate url:]" + rateUrl);
        if (rateUrl == null) {
            log.warn(" fail for model:" + model);
            return userRateInfo;
        }
        String html = getRawHtml(rateUrl, "www.taobao.com", null);
        if(StringUtils.isEmpty(html)) {
        	return userRateInfo;
        }
        userRateInfo = new UserRateInfo(model);
        userRateInfo.isSeller = getIsSellerNew(html);
        Document rateDoc = null;
        
        int credit = 0;
        if(userRateInfo.isSeller) {
        	rateDoc = JsoupUtil.parseJsoupDocument(html);
        	String tempNick = JsoupUtil.getTextByCssQuery(rateDoc,
                    "div.left-box div.bd div.info-block-first div.title > a");
            userRateInfo.setUserNick(tempNick);
            boolean isTmall = getIsTmall(rateDoc);
            doSpiderSellerInfo(rateDoc, userRateInfo, isTmall);

            String createTime = JsoupUtil.getTextByCssQuery(rateDoc, "li:contains(创店时间)").replace("创店时间：", "").trim();
            userRateInfo.setRegisterTime(createTime);
            String shopStartTime = JsoupUtil.getAttrByCssQuery(rateDoc,
                    "div#content input#J_showShopStartDate", "value");
            userRateInfo.setRegisterTime(shopStartTime);
            
            if(!isTmall) {
            	userRateInfo.goodCreditRate = "100%";

    	        userRateInfo.creditImgSrc = JsoupUtil.getAttrByCssQuery(rateDoc,
    	                "div.info-block ul.sep li:contains(买家信用：) > a img.rank:eq(0)", "src");
    	        if(StringUtils.isEmpty(userRateInfo.totalCredit)) {
    	        	userRateInfo.totalCredit = JsoupUtil.getTextByCssQuery(rateDoc, "div.info-block ul.sep li:contains(买家信用：)")
    	                .replace("买家信用：", "").trim();
    	        }
            }
            if (StringUtils.isEmpty(userRateInfo.totalCredit)) {
                Element tmall = rateDoc.select(".left-box .tmall-pro").first();
                if (tmall != null) {
                    userRateInfo.setTotalCredit("tmall");
                }
            }

            
        } else {
        	int creditLevel = parseLowestCredit(html);
        	credit = levelToCredit(creditLevel);
        	userRateInfo.setTotalCredit(String.valueOf(credit));
        	defaultBuyerInfo(userRateInfo);
        	userRateInfo.goodCreditRate = "100%";

        	if(creditLevel > 0 && creditLevel <= 5) {
        		userRateInfo.creditImgSrc = "http://pics.taobaocdn.com/newrank/b_red_" + creditLevel + ".gif";
        	} else if(creditLevel > 5 && creditLevel <= 10) {
        		userRateInfo.creditImgSrc = "http://pics.taobaocdn.com/newrank/b_blue_" + (creditLevel - 5) + ".gif";
        	} else if(creditLevel > 10 && creditLevel <= 15) {
        		userRateInfo.creditImgSrc = "http://pics.taobaocdn.com/newrank/b_cap_" + (creditLevel - 10) + ".gif";
        	}
	        
	        if(StringUtils.isEmpty(userRateInfo.totalCredit)) {
	        	userRateInfo.totalCredit = "3";
	        }
        }
        if(!StringUtils.isEmpty(userRateInfo.getTotalCredit())) {
        	UserRateCache.putToCache(userRateInfo);
        }
        return userRateInfo;
        /*Document rateDoc = JsoupUtil.loadHtmlDoc(rateUrl, Default_Refer, false);
        if (rateDoc == null) {
            return userRateInfo;
        }
        userRateInfo = new UserRateInfo(model);
        userRateInfo.isSeller = getIsSeller(rateDoc);

        // 如果是卖家，卖家与买家页面的样式有点不同的
        if (userRateInfo.isSeller == true) {
            String tempNick = JsoupUtil.getTextByCssQuery(rateDoc,
                    "div.left-box div.bd div.info-block-first div.title > a");
            userRateInfo.setUserNick(tempNick);

            doSpiderSellerInfo(rateDoc, userRateInfo);

            String createTime = JsoupUtil.getTextByCssQuery(rateDoc, "li:contains(创店时间)").replace("创店时间：", "").trim();
            userRateInfo.setRegisterTime(createTime);

        } else {
            String tempNick = JsoupUtil.getTextByCssQuery(rateDoc,
                    "div.left-box div.bd div.info-block-first dl dd:eq(0) > a");
            userRateInfo.setUserNick(tempNick);
            doSpiderBuyerInfo(rateDoc, userRateInfo);

            String infoUrl = JsoupUtil.getAttrByCssQuery(rateDoc,
                    "div#content div.tb-shop ul.TabBarLevel1 li > a:contains(个人档案)", "href").trim();
            // log.error("url: " + infoUrl);
            String content = SimpleHttpRetryUtil.retryGetWebContent(infoUrl, rateUrl);
            if (!StringUtils.isEmpty(content)) {
                int startIndex = content.indexOf("注册时间：");
                if (startIndex >= 0) {
                    content = content.substring(startIndex + "注册时间：".length());
                    int endIndex = content.indexOf("日");
                    String regTime = content.substring(0, endIndex).trim().replaceAll("年|月|日", "-");
                    userRateInfo.setRegisterTime(regTime);
                    // log.error(regTime);
                }
            }
        }

        userRateInfo.goodCreditRate = JsoupUtil.getTextByCssQuery(rateDoc, "div.rate-box div.hd h4 em:contains(好评率：)")
                .replace("好评率：", "").trim();

        userRateInfo.creditImgSrc = JsoupUtil.getAttrByCssQuery(rateDoc,
                "div.info-block ul.sep li:contains(买家信用：) > a img.rank:eq(0)", "src");
        if(StringUtils.isEmpty(userRateInfo.totalCredit)) {
        	userRateInfo.totalCredit = JsoupUtil.getTextByCssQuery(rateDoc, "div.info-block ul.sep li:contains(买家信用：)")
                .replace("买家信用：", "").trim();
        }
        if (StringUtils.isEmpty(userRateInfo.totalCredit)) {
            Element tmall = rateDoc.select(".left-box .tmall-pro").first();
            if (tmall != null) {
                userRateInfo.setTotalCredit("tmall");
            }
        }

        UserRateCache.putToCacheByNick(userRateInfo);
        return userRateInfo;*/
    }
    
    public static int levelToCredit(int creditLevel){
		if(creditLevel < 0) {
			creditLevel = 0;
		}
		if(creditLevel > 20) {
			creditLevel = 20;
		}
		return levelToCreditMap.get(creditLevel);
	}
    
    public static Map<Integer, Integer> levelToCreditMap = new HashMap<Integer, Integer>();
	static {
		levelToCreditMap.put(0, 3);
		levelToCreditMap.put(1, 10);
		levelToCreditMap.put(2, 40);
		levelToCreditMap.put(3, 90);
		levelToCreditMap.put(4, 150);
		levelToCreditMap.put(5, 250);
		levelToCreditMap.put(6, 500);
		levelToCreditMap.put(7, 1000);
		levelToCreditMap.put(8, 2000);
		levelToCreditMap.put(9, 5000);
		levelToCreditMap.put(10, 10000);
		levelToCreditMap.put(11, 20000);
		levelToCreditMap.put(12, 50000);
		levelToCreditMap.put(13, 100000);
		levelToCreditMap.put(14, 200000);
		levelToCreditMap.put(15, 500000);
		levelToCreditMap.put(16, 1000000);
		levelToCreditMap.put(17, 2000000);
		levelToCreditMap.put(18, 5000000);
		levelToCreditMap.put(19, 10000000);
		levelToCreditMap.put(20, 20000000);
	}
	
    private static void defaultBuyerInfo(UserRateInfo userRateInfo) {
        userRateInfo.location = StringUtils.EMPTY;
        userRateInfo.weekCredit = new RateSort();
        userRateInfo.monthCredit = new RateSort();
        userRateInfo.halfYearCredit = new RateSort();
        userRateInfo.otherCredit = new RateSort();
        userRateInfo.isVerify = true;
        
    }
    
    public static int parseLowestCredit(String html) {
	    try{
    	    if (!isGoodResponse(html))
                return 0;
            // 先检查白号
            if(html.indexOf("http://pics.taobaocdn.com/newrank/") < 0 
            		&& html.indexOf("img.alicdn.com/newrank") < 0 
    				&& html.indexOf("window.g_config") > 0) {
            	return 0;
            }
            if(html.indexOf("http://pics.taobaocdn.com/newrank/") >= 0) {
            	String target = "http://pics.taobaocdn.com/newrank/b_red_";
	    	    int offset = 0;
	    	    int startIndex = html.indexOf(target);
	    	    if(startIndex <= 0) {
	    	    	target = "http://pics.taobaocdn.com/newrank/b_blue_";
	    	    	startIndex = html.indexOf(target);
	    	    	offset = 5;
	    	    }
	    	    if(startIndex <= 0) {
	    	    	target = "http://pics.taobaocdn.com/newrank/b_cap_";
	    	    	startIndex = html.indexOf(target);
	    	    	offset = 10;
	    	    }
	    	    int endIndex = html.indexOf(".gif", startIndex);
	    	    String level = html.substring(startIndex + (target.length()), endIndex);
	    	    int creditLevel = 0;
		    
		    	creditLevel = Integer.valueOf(level);
		    	return creditLevel + offset;
            } else if(html.indexOf("img.alicdn.com/newrank") >= 0) {
            	String target = "img.alicdn.com/newrank/b_red_";
	    	    int offset = 0;
	    	    int startIndex = html.indexOf(target);
	    	    if(startIndex <= 0) {
	    	    	target = "img.alicdn.com/newrank/b_blue_";
	    	    	startIndex = html.indexOf(target);
	    	    	offset = 5;
	    	    }
	    	    if(startIndex <= 0) {
	    	    	target = "img.alicdn.com/newrank/b_cap_";
	    	    	startIndex = html.indexOf(target);
	    	    	offset = 10;
	    	    }
	    	    int endIndex = html.indexOf(".gif", startIndex);
	    	    String level = html.substring(startIndex + (target.length()), endIndex);
	    	    int creditLevel = 0;
		    
		    	creditLevel = Integer.valueOf(level);
		    	return creditLevel + offset;
            }
    	    return 0;
	    } catch (Exception e) {
	        log.error(e.getMessage(), e);
	        return 0;
			// TODO: handle exception
		}
        
	}
    
    private static boolean getIsTmall(Document doc) {
        String html = doc.text();
        if (StringUtils.isEmpty(html))
            return true;
        if (html.indexOf("天猫搜索") >= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    private static void doSpiderSellerInfo(Document rateDoc, UserRateInfo userRateInfo, boolean isTmall) {
        userRateInfo.location = JsoupUtil.getTextByCssQuery(rateDoc, "div.info-block ul li:contains(所在地区：)")
                .replace("所在地区：", "").trim();
        ;
        userRateInfo.isVerify = true;

        // 卖家信用
        if(!isTmall) {
        	userRateInfo.sellerCreditImg = JsoupUtil.getAttrByCssQuery(rateDoc,
                "div.info-block ul.sep li:contains(卖家信用：) > a img.rank:eq(0)", "src");
        	userRateInfo.sellerCredit = JsoupUtil.getTextByCssQuery(rateDoc, "div.info-block ul.sep li:contains(卖家信用：)")
                .replace("卖家信用：", "").trim();
        }
        

        // 动态评分
        //userRateInfo.describeScore = getDynamicScore(rateDoc, 0);
        //userRateInfo.serviceScore = getDynamicScore(rateDoc, 1);
        //userRateInfo.deliveryScore = getDynamicScore(rateDoc, 2);
    }
    
    public static boolean getIsSellerNew(String html) {
    	if(StringUtils.isEmpty(html)) {
    		return false;
    	}
    	if (html.indexOf("卖家信息") >= 0 || html.indexOf("卖家信用") >= 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public static Boolean isGoodResponse(String response) {
		if(StringUtils.isEmpty(response)) {
			return Boolean.FALSE;
		}
		if(response.indexOf("http://pics.taobaocdn.com/newrank/") >= 0 
				|| response.indexOf("window.g_config") > 0) {
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}
    
    public static String getRawHtml(String url, String refer, String cookie) {
		String html = StringUtils.EMPTY;

		for (int i = 0; i < 5; i++) {
			try {
				HttphostWrapper wrapper = NewProxyTools.getHttphostWrapper();
				if (wrapper == null) {
					continue;
				}
				html = CommonProxyPools.directGet(url, refer, "", wrapper.getHttphost(),
						cookie, 20);
				if (isGoodResponse(html)) {
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
		if (!isGoodResponse(html)) {
				html = SimpleHttpRetryUtil.retryGetWebContent(url, refer,
						cookie, 1);
		}
		if (!isGoodResponse(html)) {
			html = API.directGet(url, refer, null, null, cookie);
		}
		if (!isGoodResponse(html)) {
			return StringUtils.EMPTY;
		}
		return html;
	}
    
    public static UserRateInfo spiderUserRateById(long userId) {
        if (userId <= 0)
            return null;

        UserRateInfo userRateInfo = UserRateCache.getUserRateFromCache(userId);
        if (userRateInfo != null) {
            //if (!userNick.equals(userRateInfo.getUserNick())) {
            //    log.error("the nick is not equal to nick from cache!!!! nick: " + userNick 
            //            + ", nick from cache: " + userRateInfo.getUserNick());
            //}
            return userRateInfo;
        }
        String rateUrl = "http://rate.taobao.com/user-rate-" + userId + ".htm";

        UserIdNick idNick = UserIdNick.findByUserId(userId);
        if (idNick == null) {
            return null;
        }
        String nick = UserIdNickAction.findNickById(userId);
        String encodeIdStr = UserIdNickAction.BuyerIdApi.getEncodeIdStr(nick);
        /*
         * http://rate.taobao.com/rate.htm?user_id=UMGQ4OFQyMFvb&rater=1
         */
        rateUrl = "http://rate.taobao.com/rate.htm?user_id=" + encodeIdStr;
        log.info("[rate url:]" + rateUrl);

        Document rateDoc = JsoupUtil.loadHtmlDoc(rateUrl, Default_Refer, false);

        if (rateDoc == null) {
            return null;
        }
        userRateInfo = new UserRateInfo(userId);
        userRateInfo.isSeller = getIsSeller(rateDoc);

        //如果是卖家，卖家与买家页面的样式有点不同的
        if (userRateInfo.isSeller == true) {
            String tempNick = JsoupUtil.getTextByCssQuery(rateDoc,
                    "div.left-box div.bd div.info-block-first div.title > a");
            userRateInfo.setUserNick(tempNick);
            doSpiderSellerInfo(rateDoc, userRateInfo);

            String createTime = JsoupUtil.getTextByCssQuery(rateDoc, "li:contains(创店时间)").replace("创店时间：", "").trim();
            userRateInfo.setRegisterTime(createTime);
        } else {
            String tempNick = JsoupUtil.getTextByCssQuery(rateDoc,
                    "div.left-box div.bd div.info-block-first dl dd:eq(0) > a");
            userRateInfo.setUserNick(tempNick);
            doSpiderBuyerInfo(rateDoc, userRateInfo);

            String infoUrl = JsoupUtil.getAttrByCssQuery(rateDoc,
                    "div#content div.tb-shop ul.TabBarLevel1 li > a:contains(个人档案)", "href").trim();
            // log.error("url: " + infoUrl);
            String content = SimpleHttpRetryUtil.retryGetWebContent(infoUrl, rateUrl);
            if (!StringUtils.isEmpty(content)) {
                int startIndex = content.indexOf("注册时间：");
                if (startIndex >= 0) {
                    content = content.substring(startIndex + "注册时间：".length());
                    int endIndex = content.indexOf("日");
                    String regTime = content.substring(0, endIndex).trim().replaceAll("年|月|日", "-");
                    userRateInfo.setRegisterTime(regTime);
                    // log.error(regTime);
                }
            }
        }

        userRateInfo.goodCreditRate = JsoupUtil.getTextByCssQuery(rateDoc, "div.rate-box div.hd h4 em:contains(好评率：)")
                .replace("好评率：", "").trim();

        userRateInfo.totalCredit = JsoupUtil.getTextByCssQuery(rateDoc, "div.info-block ul.sep li:contains(买家信用：)")
                .replace("买家信用：", "").trim();

        if (!StringUtils.isEmpty(userRateInfo.totalCredit) && Integer.valueOf(userRateInfo.totalCredit) > 0) {
            userRateInfo.creditImgSrc = JsoupUtil.getAttrByCssQuery(rateDoc,
                    "div.info-block ul.sep li:contains(买家信用：) > a img.rank:eq(0)", "src");
        }

        UserRateCache.putToCache(userRateInfo);
        return userRateInfo;
    }

    public static UserRateInfo doSpiderUserRate(long userId, String userNick) {
        if (userId <= 0 || StringUtils.isEmpty(userNick))
            return null;

        UserRateInfo userRateInfo = spiderUserRateById(userId);
        if (userRateInfo == null)
            return null;
        if (userRateInfo.getUserNick() != null && userRateInfo.getUserNick().equals(userNick)) {

        } else {
            log.error("userNick should be: " + userNick + ", but in fact it is: " + userRateInfo.getUserNick());
        }

        return userRateInfo;
    }

    private static void doSpiderSellerInfo(Document rateDoc, UserRateInfo userRateInfo) {
        userRateInfo.location = JsoupUtil.getTextByCssQuery(rateDoc,
                "div.info-block ul li:contains(所在地区：)").replace("所在地区：", "").trim();
        ;
        userRateInfo.isVerify = JsoupUtil.hasElementByCssQuery(rateDoc,
                "ul.quality li:contains(认证信息) a img");

        //卖家信用
        userRateInfo.sellerCreditImg = JsoupUtil.getAttrByCssQuery(rateDoc,
                "div.info-block ul.sep li:contains(卖家信用：) > a img.rank:eq(0)", "src");
        userRateInfo.sellerCredit = JsoupUtil.getTextByCssQuery(rateDoc,
                "div.info-block ul.sep li:contains(卖家信用：)").replace("卖家信用：", "").trim();

        //动态评分
        userRateInfo.describeScore = getDynamicScore(rateDoc, 0);
        userRateInfo.serviceScore = getDynamicScore(rateDoc, 1);
        userRateInfo.deliveryScore = getDynamicScore(rateDoc, 2);
    }

    private static void doSpiderBuyerInfo(Document rateDoc, UserRateInfo userRateInfo) {
        userRateInfo.location = JsoupUtil.getTextByCssQuery(rateDoc,
                "div.info-block dl dt:contains(所在地区) + dd");
        userRateInfo.isVerify = JsoupUtil.hasElementByCssQuery(rateDoc,
                "div.info-block dl dt:contains(认证信息) + dd a img");

        userRateInfo.weekCredit = getCreditRate(rateDoc, 0);
        userRateInfo.monthCredit = getCreditRate(rateDoc, 1);
        userRateInfo.halfYearCredit = getCreditRate(rateDoc, 2);
        userRateInfo.otherCredit = getCreditRate(rateDoc, 3);
        
        if(!StringUtils.isEmpty(userRateInfo.halfYearCredit.getPositiveNum())) {
        	userRateInfo.totalCredit = String.valueOf(userRateInfo.halfYearCredit.getTotalCreditNum() +
        		userRateInfo.otherCredit.getTotalCreditNum());
        }
    }

    private static boolean getIsSeller(Document doc) {
        String html = doc.text();
        if (StringUtils.isEmpty(html))
            return true;
        if (html.indexOf("卖家信息") >= 0 && html.indexOf("卖家信用") >= 0) {
            return true;
        } else
            return false;
    }

    private static RateSort getCreditRate(Document doc, int index) {
        RateSort rateSort = new RateSort();
        String baseCssQuery = "div.show-list ul.menu-content li:eq(" + index + ") table tr ";
        String goodCssQuery = baseCssQuery + " td.rateok a";
        rateSort.positiveNum = JsoupUtil.getTextByCssQuery(doc, goodCssQuery);
        String neutralCssQuery = baseCssQuery + " td.ratenormal a";
        rateSort.neutralNum = JsoupUtil.getTextByCssQuery(doc, neutralCssQuery);
        String negativeCssQuery = baseCssQuery + " td.ratebad a";
        rateSort.negativeNum = JsoupUtil.getTextByCssQuery(doc, negativeCssQuery);

        rateSort.calcuTotalNum();
        return rateSort;
    }

    private static DynamicScore getDynamicScore(Document doc, int index) {

        String baseCssQuery = "div.rate-box div.bd ul.dsr-info li.dsr-item:eq(" + index + ") div.item-scrib";

        DynamicScore dynamicScore = new DynamicScore();
        dynamicScore.score = JsoupUtil.getAttrByCssQuery(doc, baseCssQuery + " em.count", "title");

        String equalCssQuery = baseCssQuery + " em strong.percent";
        boolean isOver = JsoupUtil.hasElementByCssQuery(doc, equalCssQuery + ".over");
        boolean isNormal = JsoupUtil.hasElementByCssQuery(doc, equalCssQuery + ".normal");
        boolean isLower = JsoupUtil.hasElementByCssQuery(doc, equalCssQuery + ".lower");

        if (isOver == true) {
            dynamicScore.equals = "over";
        } else if (isNormal == true) {
            dynamicScore.equals = "normal";
        } else if (isLower == true) {
            dynamicScore.equals = "lower";
        } else {
            dynamicScore.equals = "";
        }

        dynamicScore.averageScore = JsoupUtil.getTextByCssQuery(doc, equalCssQuery);

        return dynamicScore;

    }

    /*private static String getUserInfoByContains(Document rateDoc, String cssQuery, String contains) {
        cssQuery = cssQuery + ":contains(" + contains + ")";
        String text = JsoupUtil.getTextByCssQuery(rateDoc, cssQuery);
        if (StringUtils.isEmpty(text))
            text = "";
        text = text.replace(contains, "").trim();
        return text;
    }*/

    public static String doSpiderCommentList(long taobaoId, String buyerNick, int currentPage, int commentType, boolean hasContent) {
        //rate.taobao.com/member_rate.htm?_ksTS=1365515072836_269&callback=shop_rate_list&content=&result=&from=rate&user_id=544637663&identity=1&rater=3&direction=1&page=1
    	UserIdNick model = null;
    	if(!StringUtils.isEmpty(buyerNick)) {
    		model = UserIdNick.findByNick(buyerNick);
    	} else if(taobaoId  > 0L) {
    		model = UserIdNick.findByUserId(taobaoId);
    	}
    	if(model == null || StringUtils.isEmpty(model.getEncodedId())) {
    		return StringUtils.EMPTY;
    	}
    	String encodeIdStr = model.getEncodedId();
    	
        //String encodeIdStr = UserIdNickAction.BuyerIdApi.getEncodeIdStr(buyerNick);

        long time = System.currentTimeMillis();
        String content = "";
        if (hasContent == true)
            content = "1";
        //_ksTS=1396069288357_246&callback=shop_rate_list&content=1&result=-1&from=rate&user_id=UOFNYOFNbvCHu&identity=1&rater=3&direction=1&page=1
        String commentUrl = "http://rate.taobao.com/member_rate.htm?_ksTS=" + time
                + "_190&callback=shop_rate_list&content=" + content + "&from=rate&identity=1&rater=3&direction=1" +
                "&user_id=" + encodeIdStr + "&page=" + currentPage;
        if (commentType == CommentType.All) {
            commentUrl += "&result=";
        } else if (commentType == CommentType.Positive) {
            commentUrl += "&result=1";
        } else if (commentType == CommentType.Neutral) {
            commentUrl += "&result=0";
        } else if (commentType == CommentType.Negative) {
            commentUrl += "&result=-1";
        } else {
            commentUrl += "&result=";
        }

        //log.error(commentUrl);
        /*Jsoup的问题是会自动将&quot;替换成引号，从而Json格式解析错误，clorest510的例子
        Document doc = JsoupUtil.loadHtmlDoc(commentUrl);
        if (doc == null)
            return "";
        
        String text = doc.text();
        */

        String refer = "http://rate.taobao.com/user-rate-" + taobaoId + ".htm";

        String text = StringUtils.EMPTY;
        int retry = 0;
        while (retry++ < 4) {
            HttphostWrapper wrapper = IProxy.getInstance().provideWrapper();
            if (wrapper == null || retry > 3) {
                //text = SimpleHttpRetryUtil.retryGetWebContent(commentUrl, refer, null, 1);
            	text = directGet(commentUrl, refer, null, null, null);
            } else {
                text = directGet(commentUrl, refer, null, wrapper.getHttphost(), null);
            }

            if (!StringUtils.isBlank(text)) {
                text = text.trim();
                if (!text.startsWith("shop_rate_list({\"status\":\"checkcode") && !text.startsWith("<html>")
                        && !text.startsWith("shop_rate_list({\"status\":1111") && !text.startsWith("<!DOCTYPE")
                        && !text.startsWith("<?xml") && !text.startsWith("<HTML>")) {
                    if(text.indexOf("rgv587_flag") < 0) {
                    	break;
                    }
                } else {
                    text = StringUtils.EMPTY;
                }
            }

            if (wrapper != null) {
                wrapper.addFailCount();
            }
        }

        // log.error(text);
        if (StringUtils.isEmpty(text)) {
            log.error("[checkPositiveRate]cannot get badrate for buyerId: " + taobaoId + ", commentType: "
                    + commentType + ", nick: " + buyerNick + " with retry :" + retry);

            return "";
        } else {
        	//log.info("doSpiderCommentList for "+buyerNick+" retry time = " + retry);
        	//log.info(text);
            log.error("[checkPositiveRate]fetch badrate success for buyerId: " + taobaoId + ", commentType: "
                    + commentType + ", nick: " + buyerNick + " with retry :" + retry);
        }
        if(text.indexOf("rgv587_flag") >= 0) {
        	return "";
        }
        String json = text.trim().replace("shop_rate_list(", "");
        if (StringUtils.isEmpty(json)) {
            return "";
        }

        json = json.substring(0, json.length() - 1).trim();
        // log.error(json);
        return json;

        //全部
        //http://rate.taobao.com/member_rate.htm?_ksTS=1365516032478_321&callback=shop_rate_list&content=&result=&from=rate&user_id=544637663&identity=1&rater=3&direction=1&page=1

        //好评
        //http://rate.taobao.com/member_rate.htm?_ksTS=1365515959978_282&callback=shop_rate_list&content=&result=1&from=rate&user_id=544637663&identity=1&rater=3&direction=1&page=1

        //中评
        //http://rate.taobao.com/member_rate.htm?_ksTS=1365515996456_295&callback=shop_rate_list&content=&result=0&from=rate&user_id=544637663&identity=1&rater=3&direction=1&page=1

        //差评
        //http://rate.taobao.com/member_rate.htm?_ksTS=1365516011849_308&callback=shop_rate_list&content=&result=-1&from=rate&user_id=544637663&identity=1&rater=3&direction=1&page=1

        //http://rate.taobao.com/member_rate.htm?_ksTS=1365693387214_1225&callback=shop_rate_list&content=&result=0&from=rate&user_id=50983440&identity=1&rater=3&direction=1&page=1

        //http://rate.taobao.com/member_rate.htm?_ksTS=1365693612709_1237&callback=shop_rate_list&content=1&result=-1&from=rate&user_id=50983440&identity=1&rater=3&direction=1&page=1

        //http://rate.taobao.com/member_rate.htm?_ksTS=1365693659231_1250&callback=shop_rate_list&content=&result=-1&from=rate&user_id=50983440&identity=1&rater=3&direction=1&page=1
    }

    public static String directGet(String url, String referer, String ua, HttpHost host, String cookies) {
        try {
            HttpClient httpclient = null;
            HttpResponse rsp = null;

            httpclient = new DefaultHttpClient();
            if (host != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
            }

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 4000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 4000);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

            HttpGet httpGet = new HttpGet(url);
            if (referer != null) {
                httpGet.addHeader("Referer", referer);
            }
            if (cookies != null) {
                httpGet.addHeader("Cookie", cookies);
            }

//            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
            httpGet.addHeader("User-Agent", ua == null ? API.DEFAULT_UA : ua);

            rsp = httpclient.execute(httpGet);
            if (rsp.getStatusLine().getStatusCode() == StatusCode.MOVED) {
                rsp.getHeaders("Location");
            }
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            return content;
        } catch (Exception e) {
            log.warn(e.getMessage() + " checkPositiveRate proxy exception==== using host: " + host);
//            if (e.getMessage().contains("refused")) {
//                throw new RuntimeException("");
//            }
        }
        return null;
    }

    public static void main(String[] args) {
        //doSpiderCommentList(79742176, 1, 1, true);

        UserRateInfo rateInfo = doSpiderUserRate(45938499L, "超级hero");

        log.error(JsonUtil.getJson(rateInfo));
    }

    public static class UserRateInfo implements Serializable {
        private static final long serialVersionUID = -1L;

        private long userId;

        private String userNick;

        private boolean isSeller;

        private String location;//所在地区

        private String registerTime;//注册时间

        private boolean isVerify;//是否认证过

        private String totalCredit;//买家信用

        private String creditImgSrc;//买家信用图片

        private String sellerCredit;//卖家信用

        private String sellerCreditImg;//卖家信用图片

        private DynamicScore describeScore;//宝贝描述动态评分

        private DynamicScore serviceScore;//服务态度动态评分

        private DynamicScore deliveryScore;//发货速度动态评分

        private String goodCreditRate;//

        //private RateSort commentNum;//评价数
        private RateSort weekCredit;//信用

        private RateSort monthCredit;

        private RateSort halfYearCredit;

        private RateSort otherCredit;

        public UserRateInfo(long userId) {
            super();
            this.userId = userId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getUserNick() {
            return userNick;
        }

        public void setUserNick(String userNick) {
            this.userNick = userNick;
        }

        public boolean isSeller() {
            return isSeller;
        }

        public void setSeller(boolean isSeller) {
            this.isSeller = isSeller;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getCreditImgSrc() {
            return creditImgSrc;
        }

        public void setCreditImgSrc(String creditImgSrc) {
            this.creditImgSrc = creditImgSrc;
        }

        public String getRegisterTime() {
            return registerTime;
        }

        public void setRegisterTime(String registerTime) {
            this.registerTime = registerTime;
        }

        public DynamicScore getDescribeScore() {
            return describeScore;
        }

        public void setDescribeScore(DynamicScore describeScore) {
            this.describeScore = describeScore;
        }

        public DynamicScore getServiceScore() {
            return serviceScore;
        }

        public void setServiceScore(DynamicScore serviceScore) {
            this.serviceScore = serviceScore;
        }

        public DynamicScore getDeliveryScore() {
            return deliveryScore;
        }

        public void setDeliveryScore(DynamicScore deliveryScore) {
            this.deliveryScore = deliveryScore;
        }

        public String getSellerCredit() {
            return sellerCredit;
        }

        public void setSellerCredit(String sellerCredit) {
            this.sellerCredit = sellerCredit;
        }

        public String getSellerCreditImg() {
            return sellerCreditImg;
        }

        public void setSellerCreditImg(String sellerCreditImg) {
            this.sellerCreditImg = sellerCreditImg;
        }

        public boolean isVerify() {
            return isVerify;
        }

        public void setVerify(boolean isVerify) {
            this.isVerify = isVerify;
        }

        public String getTotalCredit() {
            return totalCredit;
        }

        public void setTotalCredit(String totalCredit) {
            this.totalCredit = totalCredit;
        }

        public String getGoodCreditRate() {
            return goodCreditRate;
        }

        public void setGoodCreditRate(String goodCreditRate) {
            this.goodCreditRate = goodCreditRate;
        }

        /*public RateSort getCommentNum() {
            return commentNum;
        }
        public void setCommentNum(RateSort commentNum) {
            this.commentNum = commentNum;
        }*/
        public RateSort getWeekCredit() {
            return weekCredit;
        }

        public void setWeekCredit(RateSort weekCredit) {
            this.weekCredit = weekCredit;
        }

        public RateSort getMonthCredit() {
            return monthCredit;
        }

        public void setMonthCredit(RateSort monthCredit) {
            this.monthCredit = monthCredit;
        }

        public RateSort getHalfYearCredit() {
            return halfYearCredit;
        }

        public void setHalfYearCredit(RateSort halfYearCredit) {
            this.halfYearCredit = halfYearCredit;
        }

        public RateSort getOtherCredit() {
            return otherCredit;
        }

        public void setOtherCredit(RateSort otherCredit) {
            this.otherCredit = otherCredit;
        }

        public UserRateInfo(UserIdNick model) {
            super();
            this.userId = model.getUserid();
            this.userNick = model.getNick();
        }
        
        @Override
        public String toString() {
            return "UserRateInfo [userId=" + userId + ", userNick=" + userNick + ", isSeller=" + isSeller
                    + ", location=" + location + ", registerTime=" + registerTime + ", isVerify=" + isVerify
                    + ", totalCredit=" + totalCredit + ", creditImgSrc=" + creditImgSrc + ", sellerCredit="
                    + sellerCredit + ", sellerCreditImg=" + sellerCreditImg + ", describeScore=" + describeScore
                    + ", serviceScore=" + serviceScore + ", deliveryScore=" + deliveryScore + ", goodCreditRate="
                    + goodCreditRate + ", weekCredit=" + (weekCredit == null ? "NULL" : weekCredit.toString()) + ", "
                    + "monthCredit=" + (monthCredit == null ? "NULL" : monthCredit.toString())
                    + ", halfYearCredit=" + (halfYearCredit == null ? "NULL" : halfYearCredit.toString()) + ", otherCredit="
                    + (otherCredit == null ? "NULL" : otherCredit.toString()) + "]";
        }
    }

    public static class RateSort implements Serializable {
        private static final long serialVersionUID = -1L;

        private String positiveNum;

        private String neutralNum;

        private String negativeNum;

        private int totalCreditNum;

        private int totalTradeNum;

        public String getPositiveNum() {
            return positiveNum;
        }

        public void setPositiveNum(String positiveNum) {
            this.positiveNum = positiveNum;
        }

        public String getNeutralNum() {
            return neutralNum;
        }

        public void setNeutralNum(String neutralNum) {
            this.neutralNum = neutralNum;
        }

        public String getNegativeNum() {
            return negativeNum;
        }

        public void setNegativeNum(String negativeNum) {
            this.negativeNum = negativeNum;
        }

        public int getTotalCreditNum() {
            return totalCreditNum;
        }

        public void setTotalCreditNum(int totalCreditNum) {
            this.totalCreditNum = totalCreditNum;
        }

        public int getTotalTradeNum() {
            return totalTradeNum;
        }

        public void setTotalTradeNum(int totalTradeNum) {
            this.totalTradeNum = totalTradeNum;
        }

        public void calcuTotalNum() {
            try {
                totalCreditNum = 0;
                totalTradeNum = 0;
                if (!StringUtils.isEmpty(positiveNum)) {
                    int num = NumberUtil.parserInt(positiveNum.trim(), 0);
                    totalCreditNum += num;
                    totalTradeNum += num;
                }
                if (!StringUtils.isEmpty(neutralNum)) {
                    int num = NumberUtil.parserInt(neutralNum.trim(), 0);
                    totalCreditNum += num;
                    totalTradeNum += num;
                }
                if (!StringUtils.isEmpty(negativeNum)) {
                    int num = NumberUtil.parserInt(negativeNum.trim(), 0);
                    totalCreditNum += num;
                    totalTradeNum += num;
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public int getNonPositiveNum() {
            int count = 0;
            if (!StringUtils.isEmpty(negativeNum)) {
                count += NumberUtil.parserInt(negativeNum.trim(), 0);
            }
            if (!StringUtils.isEmpty(neutralNum)) {
                count += NumberUtil.parserInt(neutralNum.trim(), 0);
            }
            return count;
        }
        
        @Override
        public String toString() {
        	return "{RateSort positiveNum = " + positiveNum + "neutralNum = " + neutralNum +
        			"negativeNum = " + negativeNum + "}";
        }
    }

    public static class DynamicScore implements Serializable {
        private static final long serialVersionUID = -1L;

        private String score;

        private String equals;

        private String averageScore;

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getEquals() {
            return equals;
        }

        public void setEquals(String equals) {
            this.equals = equals;
        }

        public String getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(String averageScore) {
            this.averageScore = averageScore;
        }

    }

    public static class CommentType {
        public static final int All = 1;

        public static final int Positive = 2;

        public static final int Neutral = 3;

        public static final int Negative = 4;
    }

    public static class TBUserCache {
    	private static String TB_DOMAIN_USER = "TB_Domain_User_";
    	
    	public static void putToCache(com.taobao.api.domain.User user) {
            if (user == null)
                return;
            try {
                String cacheKey = TB_DOMAIN_USER + StringUtils.trim(user.getNick());
                Cache.set(cacheKey, user, "300s");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public static com.taobao.api.domain.User getUserRateFromCache(String nick) {
        	nick = StringUtils.trim(nick);
            String cacheKey = TB_DOMAIN_USER + nick;
            Object obj = Cache.get(cacheKey);
            if (obj == null)
                return null;
            try {
            	com.taobao.api.domain.User userRateInfo = (com.taobao.api.domain.User) obj;
                return userRateInfo;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);

            }
            return null;
        }
    }
    
    public static class UserRateCache {
        private static String UserRateKey = "UserRate_";

        public static void putToCache(UserRateInfo userRateInfo) {
            if (userRateInfo == null)
                return;
            try {
                String cacheKey = UserRateKey + userRateInfo.getUserId();
                Cache.set(cacheKey, userRateInfo, "72h");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public static UserRateInfo getUserRateFromCache(long userId) {
            String cacheKey = UserRateKey + userId;
            Object obj = Cache.get(cacheKey);
            if (obj == null)
                return null;
            try {
                UserRateInfo userRateInfo = (UserRateInfo) obj;
                return userRateInfo;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);

            }
            return null;
        }
        
        public static void putToCacheByNick(UserRateInfo userRateInfo) {
            if (userRateInfo == null)
                return;
            try {
                String cacheKey = UserRateKey + userRateInfo.getUserNick();
                Cache.set(cacheKey, userRateInfo, "24h");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        
        public static UserRateInfo getUserRateFromCacheByNick(String nick) {
            String cacheKey = UserRateKey + nick;
            Object obj = Cache.get(cacheKey);
            if (obj == null)
                return null;
            try {
                UserRateInfo userRateInfo = (UserRateInfo) obj;
                return userRateInfo;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return null;
        }
    }
}
