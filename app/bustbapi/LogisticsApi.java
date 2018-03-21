package bustbapi;

import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.AddressResult;
import com.taobao.api.domain.LogisticsCompany;
import com.taobao.api.domain.Shipping;
import com.taobao.api.request.LogisticsAddressSearchRequest;
import com.taobao.api.request.LogisticsCompaniesGetRequest;
import com.taobao.api.request.LogisticsOfflineSendRequest;
import com.taobao.api.request.LogisticsOrdersDetailGetRequest;
import com.taobao.api.response.LogisticsAddressSearchResponse;
import com.taobao.api.response.LogisticsCompaniesGetResponse;
import com.taobao.api.response.LogisticsOfflineSendResponse;
import com.taobao.api.response.LogisticsOrdersDetailGetResponse;

public class LogisticsApi {

	private static final Logger log = LoggerFactory.getLogger(LogisticsApi.class);
	
	public static final String TAG = "LogisticsApi";
	
	public static final String FILE = "id,code,name,reg_mail_no";
	
	/**
	 * 查询物流公司信息
	 * @author Administrator
	 */
	public static class LogisticsCompaniesGet extends TBApi<LogisticsCompaniesGetRequest, LogisticsCompaniesGetResponse, List<LogisticsCompany>>{
		
		public LogisticsCompaniesGet(User user){
			super(user.getSessionKey());
		}

		@Override
		public LogisticsCompaniesGetRequest prepareRequest() {
			LogisticsCompaniesGetRequest companiesGetRequest = new LogisticsCompaniesGetRequest();
			companiesGetRequest.setFields(FILE);
			return companiesGetRequest;
		}

		@Override
		public List<LogisticsCompany> validResponse(LogisticsCompaniesGetResponse resp) {
			if (resp == null) {
				log.error("Null Resp Returned");
				return null;
			}

			if (!resp.isSuccess()) {
				log.error("resp submsg" + resp.getSubMsg());
				log.error("resp error code " + resp.getErrorCode());
				log.error("resp Mesg " + resp.getMsg());
				super.errorMsg = resp.getMsg();
				return null;
			}
			
			return resp.getLogisticsCompanies();
		}

		@Override
		public List<LogisticsCompany> applyResult(List<LogisticsCompany> res) {
			return res;
		}
	}
	
	public static class LogisticsOfflineSend extends TBApi<LogisticsOfflineSendRequest, LogisticsOfflineSendResponse , Shipping>{
		
		private Long tid;
		
		private String logisticsCompCode;
		
		private String outSid;
		
		public String errorMsg = StringUtils.EMPTY;
		
		public LogisticsOfflineSend(User user, Long tid, String logisticsCompCode, String outSid){
			super(user.getSessionKey());
			this.tid = tid;
			this.logisticsCompCode = logisticsCompCode;
			this.outSid = outSid;
		}

		@Override
		public LogisticsOfflineSendRequest prepareRequest() {
			LogisticsOfflineSendRequest req = new LogisticsOfflineSendRequest();
			req.setTid(tid);
			req.setOutSid(outSid);
			req.setCompanyCode(logisticsCompCode);
			return req;
		}

		@Override
		public Shipping validResponse(LogisticsOfflineSendResponse resp) {
			if (resp == null) {
				log.error("Null Resp Returned");
				return null;
			}

			if (!resp.isSuccess()) {
				log.error("resp submsg" + resp.getSubMsg());
				log.error("resp error code " + resp.getErrorCode());
				log.error("resp Mesg " + resp.getMsg());
				errorMsg = resp.getSubMsg();
				return null;
			}
			return resp.getShipping();
		}

		@Override
		public Shipping applyResult(Shipping res) {
			return res;
		}
	}
	
	/**
	 * taobao.logistics.orders.detail.get 批量查询物流订单,返回详细信息
	 */
	public static class LogisticsOrdersDetailGet extends TBApi<LogisticsOrdersDetailGetRequest, LogisticsOrdersDetailGetResponse, List<Shipping>>{

		private Long tid;
		
		private String filed;
		
		public LogisticsOrdersDetailGet(String sid, Long tid, String filed){
			super(sid);
			this.tid = tid;
			this.filed = filed;
		}
		
		@Override
		public LogisticsOrdersDetailGetRequest prepareRequest() {
			LogisticsOrdersDetailGetRequest req = new LogisticsOrdersDetailGetRequest();
			req.setFields(filed);
			req.setTid(tid);
			return req;
		}

		@Override
		public List<Shipping> validResponse(LogisticsOrdersDetailGetResponse resp) {
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
			
			return resp.getShippings();
		}

		@Override
		public List<Shipping> applyResult(List<Shipping> res) {
			return res;
		}
	}
	
	/**
	 * taobao.logistics.address.search 查询卖家地址库
	 */
	public static class LogisticsAddressSearch extends TBApi<LogisticsAddressSearchRequest, LogisticsAddressSearchResponse, List<AddressResult>>{

		private String rdef;
		
		public LogisticsAddressSearch(String sid, String rdef){
			super(sid);
			this.rdef = rdef;
		}
		
		@Override
		public LogisticsAddressSearchRequest prepareRequest() {
			LogisticsAddressSearchRequest req = new LogisticsAddressSearchRequest();
			req.setRdef(rdef);
			return req;
		}

		@Override
		public List<AddressResult> validResponse(LogisticsAddressSearchResponse resp) {
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
			
			return resp.getAddresses();
		}

		@Override
		public List<AddressResult> applyResult(List<AddressResult> res) {
			return res;
		}
	}
	
}
