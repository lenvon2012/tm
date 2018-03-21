
package controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import models.comment.CommentConf;
import models.comment.Comments;
import models.traderate.TradeRatePlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import result.TMPaginger;
import result.TMResult;
import utils.DateUtil;
import utils.TaobaoUtil;
import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;
import bustbapi.TBApi;
import cache.UserLoginInfoCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.ApiException;
import com.taobao.api.SecretException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.IncrementCustomersGetRequest;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.response.IncrementCustomersGetResponse;
import com.taobao.api.response.TradesSoldGetResponse;

import configs.TMConfigs.PageSize;
import controllers.TmSecurity.SecurityType;
import dao.UserDao;
import dao.comments.CommentsDao;
import dao.trade.TradeRatePlayDao;

public class AutoComments extends TMController {

//    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void isOn() {
        //renderJSON(JsonUtil.getJson(new TMResult(TaobaoUtil.checkUserPermitted(getUser().getUserNick()))));
        renderJSON(JsonUtil.getJson(new TMResult(getUser().isAutoCommentOn())));
    }

    public static void currContent() {
        User user = getUser();
        //CommentConf commentConf = CommentConf.find("userId = ?", getUser().getId()).first();
        CommentConf commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null || commentConf.getCommentContent().isEmpty()) {
            commentConf = new CommentConf(user.getId(), user.getUserNick(), "欢迎再次光临!@#");
            commentConf.jdbcSave();
            //renderJSON(JsonUtil.getJson(new TMResult(true, null, "")));
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, null, commentConf.getCommentContent())));
    }

    public static void setOn() {
    	
        User user = getUser();
        log.info("AutoComment set on by " + user.getUserNick());
        boolean isSuccess = true;
        user.setAutoCommentOn(true);
        UserLoginInfoCache.get().doClearUser(user);
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        isSuccess = user.jdbcSave();

        checkIsZhizun();

        //if (CommentConf.find("userId = ?", user.getId()).first() == null) {
        if (CommentConf.findByUserId(user.getId()) == null) {
            isSuccess = isSuccess && new CommentConf(user.getId(), user.getUserNick(), "欢迎再次光临!@#").jdbcSave();
        }
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void setOff() {
        User user = getUser();
        boolean isSuccess = true;
        user.setAutoCommentOn(false);
        UserLoginInfoCache.get().doClearUser(user);
        isSuccess = user.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void setContent(String content) {
        if (content.contains("http://"))
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        User user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            commentConf = new CommentConf(user.getId(), user.getUserNick(), content + "!@#");
        else
            commentConf.setCommentContent(content + "!@#");
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }
    
    public static void setNewAutoTitleCommentConf(Long commentType, Long commentTime, 
    		String content) {
		if (commentType == null) {
			renderFailedJson("请选择评价时间配置");
		}
		if (StringUtils.isEmpty(content)) {
			renderFailedJson("自动评价的评语不能为空");
		}
		if (content.contains("http://")) {
			renderFailedJson("评价内容不能以http://开头");
		}      
		if (commentTime == null) {
			commentTime = 0L;
		}
		if (commentType != 0L && commentType != 2L) {
			renderFailedJson("评价时间配置不合法");
		}
		if (commentType > 0 && (commentTime <= 0 || commentTime >= 15)) {
			renderFailedJson("填写的天数必须为(大于0且小于15)的整数！");
		}
		
		User user = getUser();
		CommentConf conf = CommentConf.findByUserId(user.getId());
		if (conf == null) {
			conf = new CommentConf(user.getId(), user.getUserNick(),
					"欢迎再次光临!@#");
		}
		conf.setCommentType(commentType);
	    conf.setCommentDays(commentTime);
	    conf.setCommentContent(content.trim() + "!@#");
	    Boolean isSuccess = conf.jdbcSave();
	    if(isSuccess) {
	    	renderSuccessJson("保存成功");
	    } 
	    renderFailedJson("保存失败");
	}

    public static void editContent(String oldContent, String newContent) {
        if (newContent.contains("http://"))
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        User user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            commentConf = new CommentConf(user.getId(), user.getUserNick(), newContent + "!@#");
        else
            commentConf.editCommentContent(oldContent, newContent);
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void addContent(String content) {
        if (content.isEmpty()) {
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        }
        User user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            commentConf = new CommentConf(user.getId(), user.getUserNick(), content + "!@#");
        else
            commentConf.addCommentContent(content);
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void deleteContent(String content) {
        User user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        else
            commentConf.deleteCommentContent(content);
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void getNewAutoTitleCommentLog(int pn, int ps) throws IOException {
    	//renderMockFileInJsonIfDev("new_autotitle_comment_log.json");
    	PageOffset po = new PageOffset(pn, ps, 10);
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	List<Comments> list = CommentsDao.findOnlineByUser(user.getId(), (pn - 1) * ps, ps);
    	int count = (int) CommentsDao.countOnlineByUser(user.getId());
    	renderJSON(JsonUtil.getJson(new TMResult<Comments>(list, count, po)));
    }
    
    public static void getCommentLog(int pn, int ps) {
        User user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 10 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;

        List<Comments> list = CommentsDao.findOnlineByUser(user.getId(), (pn - 1) * ps, ps);
        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有自动评价操作日志哦！！！！！");
        }

        if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
            HashSet<Long> oids = new HashSet<Long>();
            for (Comments comments : list) {
                oids.add(comments.getOId());
            }
            List<TradeRatePlay> tradeRatePlays = TradeRatePlayDao.findByUserIdOidSet(user.getId(), oids);
            if (!CommonUtils.isEmpty(tradeRatePlays)) {
                HashMap<Long, Integer> oidRateMap = new HashMap<Long, Integer>();
                for (TradeRatePlay tradeRatePlay : tradeRatePlays) {
                    oidRateMap.put(tradeRatePlay.getOid(), tradeRatePlay.getRate());
                }
                for (Comments comments : list) {
                    Integer rate = oidRateMap.get(comments.getOId());
                    if (rate == null) {
                        comments.buyerRate = 0;
                    } else {
                        comments.buyerRate = rate;
                    }
                }
            }
        }
        
        for (Comments comment : list) {
			try {
				comment.setBuyerNick(TzgSecurity.decrypt(comment.getBuyerNick(), SecurityType.SIMPLE, user));
			} catch (SecretException e) {
				e.printStackTrace();
			}
        }

        TMPaginger tm = new TMPaginger(pn, ps, (int) CommentsDao.countOnlineByUser(user.getId()), list);
        renderJSON(JsonUtil.getJson(tm));
    }
	
	public static void getOrdersByUser(Long userId, int interval, String content) {
		User user = UserDao.findById(userId);
		if(user == null) {
			renderError("用户不存在");
		}
		
		if(interval <= 0) {
			renderError("订单时间未填写");
		}
		log.info("[AutoComments.getOrdersByUser for user]: " + user);
		
		Long pageNo = 1L;
		Date end = new Date();
		Date start = new Date(end.getTime() - DateUtil.DAY_MILLIS * interval);
		
		TaobaoClient client = TBApi.genClient();
		TradesSoldGetRequest req = new TradesSoldGetRequest();
		req.setFields("tid,orders,buyer_nick,seller_can_rate");
		req.setStartCreated(start);
		req.setEndCreated(end);
		req.setStatus("TRADE_FINISHED");
		req.setRateStatus("RATE_UNSELLER");
		req.setPageSize(100L);
		req.setUseHasNext(true);
		
		TradesSoldGetResponse rsp = null;
		do {
			req.setPageNo(pageNo);
			try {
				rsp = client.execute(req, user.sessionKey);
				if (rsp.isSuccess()) {
					log.info("补评处理中>>>第" + req.getPageNo() + "页");
					handleBody(rsp.getBody(), content, user);
					pageNo++;
				}
			} catch (ApiException e) {
				log.error("error:", e);
			}
		} while (rsp.isSuccess() && rsp.getHasNext());
	}

	private static void handleBody(String body, String content, User user) {
		JSONObject trades_sold_get_response;
		try {
			trades_sold_get_response = new JSONObject(body).getJSONObject("trades_sold_get_response");
			JSONObject obj = null;
			if (trades_sold_get_response.has("trades")) {
				obj = trades_sold_get_response.getJSONObject("trades");
				JSONArray trades = obj.getJSONArray("trade");
				if (trades != null && trades.length() > 0) {
					int i = 0;
					while (i++ < trades.length()) {
						JSONObject trade = (JSONObject) trades.get(i - 1);
						//检测卖家是否可以评价
						if (!trade.getBoolean("seller_can_rate")) {
							log.error("seller can not rate this trade!!!");
							continue;
						}
						Long tid = Long.parseLong(trade.getString("tid"));
						String buyerNick = trade.getString("buyer_nick");
						JSONObject orderObj = trade.getJSONObject("orders");
						JSONArray orders = orderObj.getJSONArray("order");
						if (orders.length() > 0) {
							int j = 0;
							while (j++ < orders.length()) {
								JSONObject order = (JSONObject) orders.get(j - 1);
								//如果卖家已评价
								if (order.getBoolean("seller_rate")) {
									log.info("seller already rated!!!");
									continue;
								}
								//如果卖家是商城卖家
								if (order.getString("seller_type").equals("B")) {
									log.info("tmall seller!!!");
									continue;
								}
								if (order.getString("end_time") == null || order.getString("end_time").isEmpty()) {
									log.info("order is 15 days before , can not rate any more!!!");
									continue;
								}
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								//检测子订单结束时间是否超过15天
								Date date = sdf.parse(order.getString(("end_time")));
								if (System.currentTimeMillis() - date.getTime() > DateUtil.FIFTEEN_DAYS) {
									log.info("order is 15 days before , can not rate any more!!!");
									continue;
								}
								//检测cid是否为“订单、赠品、定金、新品预览、邮费”子类目
								if(order.getLong("cid") > 0L){
									
								}
								Long oid = Long.parseLong(order.getString("oid"));
								String conf = CommentConf.findConf(user.getId());
								if(conf == null || conf.isEmpty()) {
									if(StringUtils.isEmpty(content)) {
										content = "很好的买家，欢迎下次再来！";
									}
								} else {
									int length = conf.split("!@#").length;
									int offset = new Random().nextInt(length);
									content = conf.split("!@#")[offset];
								}
								boolean isSuccess = TaobaoUtil.commentNow(user.userNick, user.getId(), buyerNick, tid, oid, content);
								if (isSuccess) {
									log.info("comment success for userNick = " + user.userNick
											+ " and  buyerNick = " + buyerNick + " and tid = " + tid + " and oid "
											+ oid);
								} else {
									log.info("comment failed for userNick = " + user.userNick
											+ " and  buyerNick = " + buyerNick + " and tid = " + tid + " and oid "
											+ oid);
								}
							}
						}
					}
				}
			} else {
				log.info("no trades return for user:" + user);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

    public static void checkUserPermitted(String nick) {
        TaobaoClient client = TBApi.genClient();
        IncrementCustomersGetRequest req = new IncrementCustomersGetRequest();
        req.setNicks(nick);
        req.setPageSize(10L);
        req.setPageNo(1L);
        req.setType("notify");
        req.setFields("nick,created,status,subscriptions");
        try {
            IncrementCustomersGetResponse response = client.execute(req);
            renderJSON(response);
        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            renderJSON("{\"error\",\"IncrementCustomersGetRequest failed\"}");
        }
    }
}
