package controllers;

import groovy.ui.Console;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.item.ItemCatPlay;
import models.mysql.word.WordBase;
import models.user.User;
import models.words.ALResult;
import models.words.TradeFollower;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.TMApi;
import bustbapi.TMApi.CategoryDataApi;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;

import play.mvc.Http;

public class TradeQuery extends TMController {

	private static final Logger log = LoggerFactory.getLogger(TradeQuery.class);

	public static final String TAG = "TradeQuery";
	
	public static final int FOLLOW_LIMIT = 5;
	
	public static final long MILLISECONDS_OF_MINIMUM_FOLLOW_LIFETIME = DateUtil.ONE_HOUR * 12;

	/**
	 * 热销宝贝排行榜
	 */
	public static void hotsales() {
		render("autoTitle/hotsales.html");
	}

	public static void follow(long cid) {
		User user = getUser();
		long userId = user.getId();
		// RESTful风格请求
		if ("POST".equals(Http.Request.current().method)) {// POST 添加
			if (TradeFollower.countFollowByUserId(userId) >= FOLLOW_LIMIT) {
				renderError("您最多只能关注" + FOLLOW_LIMIT + "个行业哦");
			}
			// 验证是否可添加关注，如可关注则添加
			ItemCatPlay icp = null;
			if (cid < 0L) {
				renderError("请选择正确的Category Id");
			} else if ((icp = ItemCatPlay.findByCid(cid)) == null) {
				renderError("行业类目不存在");
			} else if (icp.isParent) {
				renderError("请在" + icp.getName() + "中选择一个子目录加关注");
			}
			TradeFollower tf = new TradeFollower(userId, cid);
			if (tf.jdbcSave()) {
				renderTMSuccess("关注成功");
			} else {
				log.error("用户ID" + userId + "关注行业CID" + cid + "时数据库保存出错");
				renderError("很抱歉服务器端错误，请重试！");
			}
		} else if ("DELETE".equals(Http.Request.current().method)) {// DELETE 取消关注
			TradeFollower tf = TradeFollower.findByUserIdAndCid(userId, cid);
			if (tf == null) {
				renderError("您并未关注该行业，无需取消！");
			}
			if ((tf.getStatus().ordinal() & TradeFollower.Status.FOLLOW.ordinal()) == 0) {
				renderError("您已取消对此行业的关注，无需再次取消");
			}
			if (tf.getUpdateTs() > System.currentTimeMillis() - MILLISECONDS_OF_MINIMUM_FOLLOW_LIFETIME) {
				renderError("该行业关注时间尚不足12小时，请在" + new Date(tf.getUpdateTs() + MILLISECONDS_OF_MINIMUM_FOLLOW_LIFETIME) + "之后再试");
			}
			if (tf.unfollowTrade()) {
				renderTMSuccess("操作成功");
			} else {
				renderError("取消关注时出错，错误原因为服务器内部错误，请重试或联系联系客服解决");
			}
 		} else {// GET 拉取关注信息
			Map result = new HashMap<String, Object>();
			// 获取用户关注列表
			List<TradeFollower> tfs = TradeFollower.findFollowsByUserId(userId);
			result.put("followMore", tfs.size() < FOLLOW_LIMIT);
			result.put("follows", tfs);
			renderResultJson(result);
		}
	}
	
	public static void hotSalesRank(long cid, String day, int pn, String sort) {
		User user = getUser();
		
		ALResult result = null;
		
		boolean accessable = TradeFollower.isTradeAccessableForUser(user.getId(), cid);
		if(!accessable) {
			result = new ALResult(false, "您无权查看该类目下热销数据！！！请确保您已经关注该行业");
		}
		
		try {
			result = new TMApi.CategoryDataApi(cid, day, pn, sort).execute();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		
		if(result == null) {
			result = new ALResult(false, "暂无数据，如有问题请联系我们！");
		}
		
		renderJSON(JsonUtil.getJson(result));
	}
	
	public static void hotSalesItemRank(long numIid, String day, int pn, long wordId, int track, int rankType) {
		ALResult result = null;
		
		try {
			result = new TMApi.hotSalesItemRankApi(numIid, day, pn, wordId, track, rankType).execute();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		
		if(result == null) {
			result = new ALResult(false, "暂无数据，如有问题请联系我们！");
		}
		
		renderJSON(JsonUtil.getJson(result));
	}
	
	public static void getSearchHeatData(Long numIid, String startDate, String endDate) {
		ALResult result = null;
		
		try {
			result = new TMApi.searchHeatDataApi(numIid, startDate, endDate).execute();
		} catch (ClientException e) {
			e.printStackTrace();
		}
		
		if(result == null) {
			result = new ALResult(false, "暂无数据，如有问题请联系我们！");
		}
		
		renderJSON(JsonUtil.getJson(result));
	}

}
