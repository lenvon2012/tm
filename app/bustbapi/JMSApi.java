package bustbapi;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.TmcUser;

import bustbapi.request.JushitaJmsUserAddRequest;
import bustbapi.request.JushitaJmsUserGetRequest;
import bustbapi.response.JushitaJmsUserAddResponse;
import bustbapi.response.JushitaJmsUserGetResponse;

public class JMSApi {

	private static final Logger log = LoggerFactory.getLogger(JMSApi.class);
	
	public static final String TAG = "JMSApi";
	
	/**
	 * 添加ONS消息同步用户
	 */
	public static class JushitaJmsUserAdd extends TBApi<JushitaJmsUserAddRequest, JushitaJmsUserAddResponse, Boolean>{
		
		public JushitaJmsUserAdd(User user){
			super(user.getSessionKey());
		}

		@Override
		public JushitaJmsUserAddRequest prepareRequest() {
			JushitaJmsUserAddRequest req = new JushitaJmsUserAddRequest();
			return req;
		}

		@Override
		public Boolean validResponse(JushitaJmsUserAddResponse resp) {
			if (resp == null) {
				log.error("Null Resp Returned");
				return false;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			
			if (resp.isSuccess() && resp.getIsSuccess()) {
				return true;
			}
			
			return false;
		}

		@Override
		public Boolean applyResult(Boolean res) {
			return res;
		}
	}
	
	/**
	 * 查询某个用户是否同步消息
	 */
	public static class JushitaJmsUserGet extends TBApi<JushitaJmsUserGetRequest, JushitaJmsUserGetResponse, TmcUser>{
		
		private String userNick;
		
		public JushitaJmsUserGet(User user){
			super(user.getSessionKey());
			this.userNick = user.getUserNick();
		}

		@Override
		public JushitaJmsUserGetRequest prepareRequest() {
			JushitaJmsUserGetRequest req = new JushitaJmsUserGetRequest();
			req.setUserNick(userNick);
			return req;
		}

		@Override
		public TmcUser validResponse(JushitaJmsUserGetResponse resp) {
			if (resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			
			if (resp.isSuccess()) {
				return resp.getOnsUser();
			}
			
			return null;
		}

		@Override
		public TmcUser applyResult(TmcUser res) {
			return res;
		}
	}
	
}
