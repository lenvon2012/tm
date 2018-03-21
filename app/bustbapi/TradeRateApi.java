
package bustbapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import utils.PlayUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.ApiRuleException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.request.TraderateAddRequest;
import com.taobao.api.request.TraderateListAddRequest;
import com.taobao.api.request.TraderatesGetRequest;
import com.taobao.api.response.TraderateAddResponse;
import com.taobao.api.response.TraderateListAddResponse;
import com.taobao.api.response.TraderatesGetResponse;

import configs.TMConfigs;
import controllers.APIConfig;
import dao.UserDao;

public class TradeRateApi {

    private static final Logger log = LoggerFactory.getLogger(TradeRateApi.class);

    public static final String TAG = "TradeRateApi";

    public static final String FIELDS = "tid,oid,role,nick,result,created,rated_nick,item_title,item_price,content,reply,num_iid";



    public static class TraderatesGet extends TBApi<TraderatesGetRequest, TraderatesGetResponse, List<TradeRate>> {

        public static Long PAGE_SIZE = 150L;

        private Date startDate;

        private Date endDate;

        private String result;

        private Long tid;

        private Long numIid;

        public TraderatesGet(User user, long start, long end, String result) {
            this(user, new Date(start), new Date(end));
            this.result = result;
            this.user = user;
            SimpleDateFormat df = new SimpleDateFormat("MM-dd:HH");
            String ymdStart = df.format(new Date(start));
            String ymdEnd = df.format(new Date(end));
            Thread.currentThread().setName(result + "-" + user.getId() + "-" + ymdStart + "->" + ymdEnd);
        }

        public TraderatesGet(User user, Date startDate, Date endDate) {
            super(user.sessionKey);
            this.startDate = startDate;
            this.endDate = endDate;
            this.user = user;
        }

        public TraderatesGet(User user, Long tid) {
            super(user.sessionKey);
            this.tid = tid;
            this.user = user;
            Thread.currentThread().setName("current tid " + user.getId() + " [" + tid + "]");
            if (tid == null || tid <= 0L) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                PlayUtil.printStack(stack);
            }
        }

        long pageNo = 1L;

        @Override
        public TraderatesGetRequest prepareRequest() {
            if (!APIConfig.get().enableSyncTradeRate()) {
                return null;
            }

            TraderatesGetRequest req = new TraderatesGetRequest();
            req.setFields(FIELDS);
            req.setRateType("get");
            req.setRole("buyer");
            if (!StringUtils.isEmpty(result)) {
                req.setResult(result);
            }
            if (startDate != null) {
                req.setStartDate(startDate);
            }
            if (endDate != null) {
                req.setEndDate(endDate);
            }
            if (tid != null && tid > 0) {
                req.setTid(tid);
            }
            if (numIid != null && numIid > 0) {
                req.setNumIid(numIid);
            }
            req.setPageSize(PAGE_SIZE);
            req.setPageNo(pageNo);

            req.setUseHasNext(true);
            return req;
        }

        List<TradeRate> returnRes = new ArrayList<TradeRate>();

        /**
         * {
        "traderates_get_response": {
        "trade_rates": {
            "trade_rate": [{
                "content": "好评！",
                "created": "2010-05-20 22:00:37",
                "item_price": "1.2",
                "item_title": "【24小时自动电脑充值】海南移动快充1元67.la#AAA",
                "nick": "easesou",
                "oid": 37750250222274,
                "rated_nick": "匿名",
                "result": "good",
                "role": "seller",
                "tid": 37750250222274,
                "num_iid": 1234
            },
            {
                "content": "好评！",
                "created": "2010-05-20 22:00:37",
                "item_price": "1.2",
                "item_title": "【24小时自动电脑充值】湖北移动快充1元67.la#AAA",
                "nick": "easesou",
                "oid": 37749751692274,
                "rated_nick": "匿名",
                "result": "good",
                "role": "seller",
                "tid": 37749751692274,
                "num_iid": 5678
            }]
        },
        "total_results": 12
        }
        }
         */
        @Override
        protected TraderatesGetResponse execProcess() throws ApiException {
            try {
                req.check();//if check failed,will throw ApiRuleException.
            } catch (ApiRuleException e) {
                TraderatesGetResponse localResponse = null;
                try {
                    localResponse = new TraderatesGetResponse();
                } catch (Exception e2) {
                    throw new ApiException(e2);
                }
                localResponse.setErrorCode(e.getErrCode());
                localResponse.setMsg(e.getErrMsg());
                //localResponse.setBody("this.");
                return localResponse;
            }
            TraderatesGetResponse localResponse = new TraderatesGetResponse();
            List<TradeRate> rates = new ArrayList<TradeRate>();
            localResponse.setTradeRates(rates);

            Map<String, Object> rt = ((DefaultTaobaoClient) client).doPost(req, sid);
            try {
                JSONObject first = new JSONObject(rt.get("rsp").toString());
//                log.info("[first :]" + first);
                if (!first.has("traderates_get_response")) {
                    log.warn("no resp????:" + rt);
                    localResponse.setTotalResults(-1L);
                    log.error(" start to parse error resp:" + first);
                    if (first.has("error_response")) {
                        JSONObject obj = first.getJSONObject("error_response");
                        if (obj.has("msg")) {
                            localResponse.setMsg(obj.getString("msg"));
                        }
                        if (obj.has("sub_code")) {
                            localResponse.setSubCode(obj.getString("sub_code"));
                        }
                        if (obj.has("sub_msg")) {
                            localResponse.setSubMsg(obj.getString("sub_msg"));
                        }
                        if (obj.has("code")) {
                            localResponse.setErrorCode(obj.get("code").toString());
                        }
                    }

                    log.warn("parsed resp :" + new Gson().toJson(localResponse));
                    return localResponse;
                }

                JSONObject obj = first.getJSONObject("traderates_get_response");
//                log.info("[obj:]" + obj);

                if (obj.has("total_results")) {
                    Long totalNum = obj.getLong("total_results");
                    localResponse.setTotalResults(totalNum);
                } else {
                    localResponse.setTotalResults(NumberUtil.DEFAULT_LONG);
                }
                if (obj.has("has_next")) {
                    localResponse.setHasNext(obj.getBoolean("has_next"));
                } else {
                    localResponse.setHasNext(false);
                }

                if (!obj.has("trade_rates")) {
                    return localResponse;
                }
                JSONArray arr = obj.getJSONObject("trade_rates").getJSONArray("trade_rate");
                int size = arr.length();
                for (int i = 0; i < size; i++) {

                    JSONObject itemJson = arr.getJSONObject(i);
                    TradeRate item = parseTradeRateRespJson(itemJson);
                    rates.add(item);
                }

//                log.info("new gson:" + rates.size() + " with arry size:" + size);

                return localResponse;

            } catch (JSONException e) {
                log.warn(e.getMessage(), e);
            } catch (ParseException e) {
                log.warn(e.getMessage(), e);
            }

            return super.execProcess();
        }

        @Override
        public List<TradeRate> validResponse(TraderatesGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());

                if("27".equals(resp.getErrorCode())){
                    UserDao.updateIsVaildAndSessionKey(user, false, "", null);
                } else if ("该子帐号无此操作权限taobao.traderates.get, 请通过主帐号设置开通相应权限（应用）!".equals(resp.getSubMsg())) { 
                /**
                 * resp submsg该子帐号无此操作权限taobao.traderates.get, 请通过主帐号设置开通相应权限（应用）!
                 * resp error code 12
                 * resp Mesg Insufficient user permissions
                 */
                    UserDao.updateIsVaildAndSessionKey(user, false, "", null);
                }

                return null;
            }

            log.info("page no for[" + this.pageNo + "] and start = "+(startDate == null ? "null" : new SimpleDateFormat("MM-dd:HH").format(startDate))
            		+" and " +
            		"end = "+(endDate == null ? "null" : new SimpleDateFormat("MM-dd:HH").format(endDate))+":" + user.toIdNick() + " with rate size:"
                    + CommonUtils.size(resp.getTradeRates()) + " and tid = " + tid + " and hasNext = " + resp.getHasNext());

            if (resp.getHasNext() != null && resp.getHasNext()) {
                this.iteratorTime = 1;
                this.pageNo++;
            }

            if (resp.getTotalResults() == null || resp.getTotalResults() < 0L) {
                log.warn(" no result for user:" + user);
                return null;
            }
            return resp.getTradeRates() == null ? ListUtils.EMPTY_LIST : resp.getTradeRates();
        }

        @Override
        public List<TradeRate> applyResult(List<TradeRate> res) {
//            log.info("[get size;]" + res.size());

            if (res == null) {
                return returnRes;
            }
            returnRes.addAll(res);
            return returnRes;
        }

    }

    public static class TraderateAdd extends TBApi<TraderateAddRequest, TraderateAddResponse, TradeRate> {

        private Long tid;

        private Long oid;

        private String result;

        private String role;

        private String content;

        //在评价之前需要对订单成功的时间进行判定（end_time）,如果超过15天，不能再通过该接口进行评价
        /*
         * *******************************************
         */
        public TraderateAdd(User user, long tid, String result, String role, String content) {
            super(user.sessionKey);
            this.tid = tid;
            this.result = result;
            this.role = role;
            this.content = content;
        }

        public TraderateAdd(User user, long tid, long oid, String result, String role,
                String content) {
            super(user.sessionKey);
            this.tid = tid;
            this.oid = oid;
            this.result = result;
            this.role = role;
            this.content = content;
        }

        @Override
        public TraderateAddRequest prepareRequest() {
            TraderateAddRequest req = new TraderateAddRequest();
            req.setTid(tid);
            if (this.oid > 0) {
                req.setOid(oid);
            }
            req.setResult(result);
            req.setRole(role);
            req.setContent(content);
            req.setAnony(false);
            return req;
        }

        @Override
        public TradeRate validResponse(TraderateAddResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }
            return resp.getTradeRate();
        }

        @Override
        public TradeRate applyResult(TradeRate res) {
            return res;
        }
    }

    public static class TraderateListAdd extends TBApi<TraderateListAddRequest, TraderateListAddResponse, TradeRate> {

        private Long tid;

        private String result;

        private String role;

        private String content;

        //在评价之前需要对订单成功的时间进行判定（end_time）,如果超过15天，不能再通过该接口进行评价
        /*
         * *******************************************
         */
        public TraderateListAdd(User user, long tid, String result, String role, String content) {
            super(user.sessionKey);
            this.tid = tid;
            this.result = result;
            this.role = role;
            this.content = content;
        }

        @Override
        public TraderateListAddRequest prepareRequest() {
            TraderateListAddRequest req = new TraderateListAddRequest();
            req.setTid(tid);
            req.setResult(result);
            req.setRole(role);
            req.setContent(content);
            req.setAnony(false);
            return req;
        }

        @Override
        public TradeRate validResponse(TraderateListAddResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            if (!resp.isSuccess()) {
            	ErrorHandler.validTaoBaoResp(this, resp);
                return null;
            }
            return resp.getTradeRate();
        }

        @Override
        public TradeRate applyResult(TradeRate res) {
            return res;
        }
    }

    // 先从10天改回30天   遇到单月中或差评 超1500的卖家再单独处理 2018-01-16
    public static final Long fetchBadTradeRateInterval = DateUtil.DAY_MILLIS * 30;

    public static List<TradeRate> fetchBadTradeRate(User user, long start, long end) {
        if (end < start) {
            return ListUtils.EMPTY_LIST;
        }
        if (!APIConfig.get().enableSyncTradeRate()) {
            return ListUtils.EMPTY_LIST;
        }
        
        List<TradeRate> rates = new ArrayList<TradeRate>();
        for (long tmpStart = start; tmpStart < end; tmpStart += fetchBadTradeRateInterval) {

            long tmpEnd = tmpStart + fetchBadTradeRateInterval;
            if (tmpEnd > end) {
                tmpEnd = end;
            }
            TraderatesGet traderatesGetNeutral = new TraderatesGet(user, tmpStart, tmpEnd, "neutral");
            List<TradeRate> tmpRes = traderatesGetNeutral.call();

            log.info("[neutral] for trade rate" + (tmpRes == null ? 0 : tmpRes.size()));

            if (!CommonUtils.isEmpty(tmpRes)) {
                rates.addAll(tmpRes);
            }

            TraderatesGet traderatesGetBad = new TraderatesGet(user, tmpStart, tmpEnd, "bad");
            tmpRes = traderatesGetBad.call();

//            tmpRes = new TraderatesGet(user, tmpStart, tmpEnd, "good").call();
            log.info("[Bad] for trade rate" + (tmpRes == null ? 0 : tmpRes.size()));
            if (!CommonUtils.isEmpty(tmpRes)) {
                rates.addAll(tmpRes);
            }
        }

        return rates;
    }

	public static TMResult<List<TradeRate>> fetchBadTradeRateNew(User user, long start, long end) {
		if (end < start) {
			return new TMResult<List<TradeRate>>(true, "", ListUtils.EMPTY_LIST);
		}
		if (!APIConfig.get().enableSyncTradeRate()) {
			return new TMResult<List<TradeRate>>(true, "", ListUtils.EMPTY_LIST);
		}
		
		List<TradeRate> rates = new ArrayList<TradeRate>();
		for (long tmpStart = start; tmpStart < end; tmpStart += fetchBadTradeRateInterval) {

			long tmpEnd = tmpStart + fetchBadTradeRateInterval;
			if (tmpEnd > end) {
				tmpEnd = end;
			}
			TraderatesGet neutralRatesGet = new TraderatesGet(user, tmpStart, tmpEnd, "neutral");
			List<TradeRate> tmpRes = neutralRatesGet.call();

            if(!neutralRatesGet.isApiSuccess()) {
				log.info("[neutralRatesGet] error: " + neutralRatesGet.getErrorMsg());
				log.info("[neutralRatesGet] error: " + neutralRatesGet.getSubErrorCode());
				log.info("[neutralRatesGet] error: " + neutralRatesGet.getSubErrorMsg());
				return new TMResult<List<TradeRate>>(false, neutralRatesGet.getSubErrorMsg(), ListUtils.EMPTY_LIST);
			}
			log.info("[neutral] for trade rate" + (tmpRes == null ? 0 : tmpRes.size()));

			if (!CommonUtils.isEmpty(tmpRes)) {
				rates.addAll(tmpRes);
			}
			
			TraderatesGet badRatesGet = new TraderatesGet(user, tmpStart, tmpEnd, "bad");
			tmpRes = badRatesGet.call();
            if(!badRatesGet.isApiSuccess()) {
				log.info("[badRatesGet] error: " + badRatesGet.getErrorMsg());
				log.info("[badRatesGet] error: " + badRatesGet.getSubErrorCode());
				log.info("[badRatesGet] error: " + badRatesGet.getSubErrorMsg());
				return new TMResult<List<TradeRate>>(false, badRatesGet.getSubErrorMsg(), ListUtils.EMPTY_LIST);
			}
			log.info("[bad] for trade rate" + (tmpRes == null ? 0 : tmpRes.size()));

			if (!CommonUtils.isEmpty(tmpRes)) {
				rates.addAll(tmpRes);
			}

		}
		return new TMResult<List<TradeRate>>(true, "", rates);
	}
    
    public static final Long fetchGoodTradeRateInterval = DateUtil.DAY_MILLIS * 3;


	public static List<TradeRate> fetchGoodTradeRate(final User user, long start, long end) {
		if (end < start) {
			return ListUtils.EMPTY_LIST;
		}
		if (!APIConfig.get().enableSyncTradeRate()) {
				return ListUtils.EMPTY_LIST;
		}
		
		List<TradeRate> rates = new ArrayList<TradeRate>();
		List<FutureTask<List<TradeRate>>> tasks = new ArrayList<FutureTask<List<TradeRate>>>();

		for (long tmpStart = start; tmpStart < end; tmpStart += fetchGoodTradeRateInterval) {
			long tmpEnd = tmpStart + fetchGoodTradeRateInterval;
			if (tmpEnd > end) {
				tmpEnd = end;
			}
			
			final long t1 = tmpStart;
			final long t2 = tmpEnd;
			
			FutureTask<List<TradeRate>> task = TMConfigs.getTradeRateListPool().submit(new Callable<List<TradeRate>>() {
				@Override
				public List<TradeRate> call() throws Exception {
                    TraderatesGet traderatesGet = new TraderatesGet(user, t1, t2, "good");
                    List<TradeRate> tmpRes = traderatesGet.call();

                    log.info("[good] for trade rate" + (tmpRes == null ? 0 : tmpRes.size()));
					return tmpRes;
				}
			});
			tasks.add(task);
		}

		for (FutureTask<List<TradeRate>> task : tasks) {
			List<TradeRate> doItem;
			try {
				doItem = task.get();
				rates.addAll(doItem);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		
//		List<TradeRate> rates = new ArrayList<TradeRate>();
//		for (long tmpStart = start; tmpStart < end; tmpStart += fetchGoodTradeRateInterval) {
//
//			long tmpEnd = tmpStart + fetchGoodTradeRateInterval;
//			if (tmpEnd > end) {
//				tmpEnd = end;
//			}
//			List<TradeRate> tmpRes = new TraderatesGet(user, tmpStart, tmpEnd, "good").call();
//			log.info("[good] for trade rate" + (tmpRes == null ? 0 : tmpRes.size()));
//
//			if (!CommonUtils.isEmpty(tmpRes)) {
//				rates.addAll(tmpRes);
//				log.info("~~~~~~~~~~~~~~~User:" + user.getUserNick() + "-----单次返回好评数：" + tmpRes.size() + "~~~~~~~~~~~~~~~");
//			}
//
//		}
		
		log.info("~~~~~~~~~~~~~~~User:" + user.getUserNick() + "-----总计返回好评数：" + rates.size() + "~~~~~~~~~~~~~~~");
		return rates;
	}

//    [2013-06-17 01:23:50,746] INFO  [play-thread-1]  job.apiget.TradeRateUpdateJob.requestUpdate(TradeRateUpdateJob.java:64) - [to write :]{"content":"好","created":"Jun 11, 2013 11:28:15 AM","itemPrice":"140.0","itemTitle":"限时包邮：韩国进口cd包 车用 汽车cd夹遮阳板套 CD夹 遮阳板cd夹","nick":"茵茵唯唯","numIid":9818878862,"oid":360417448829575,"ratedNick":"xiaochun114","result":"good","role":"buyer","tid":360417448829575}
//    [2013-06-17 01:23:50,749] INFO  [play-thread-1]  job.apiget.TradeRateUpdateJob.requestUpdate(TradeRateUpdateJob.java:64) - [to write :]{"content":"好","created":"Jun 11, 2013 11:28:15 AM","itemPrice":"140.0","itemTitle":"限时包邮：韩国进口cd包 车用 汽车cd夹遮阳板套 CD夹 遮阳板cd夹","nick":"茵茵唯唯","numIid":9818878862,"oid":360417448829575,"ratedNick":"xiaochun114","result":"good","role":"buyer","tid":360417448829575
}
