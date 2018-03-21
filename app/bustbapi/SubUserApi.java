package bustbapi;

import java.util.List;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.taobao.api.domain.SubUserInfo;
import com.taobao.api.request.SellercenterSubusersGetRequest;
import com.taobao.api.response.SellercenterSubusersGetResponse;

public class SubUserApi {

	private static final Logger log = LoggerFactory.getLogger(SubUserApi.class);
	
	public static final String TAG = "SubUserApi";
	
	/**
	 * 查询指定账户的子账号列表
	 * @author Administrator
	 */
	public static class SellercenterSubusersGet extends TBApi<SellercenterSubusersGetRequest, SellercenterSubusersGetResponse, TMResult<List<SubUserInfo>>>{
		
		private String sellerNick;
		
		public SellercenterSubusersGet(User user){
			super(user.getSessionKey());
			this.sellerNick = user.getUserNick();
		}

		@Override
		public SellercenterSubusersGetRequest prepareRequest() {
			SellercenterSubusersGetRequest req = new SellercenterSubusersGetRequest();
			req.setNick(sellerNick);
			return req;
		}

		@Override
		public TMResult<List<SubUserInfo>> validResponse(SellercenterSubusersGetResponse resp) {
			if(!resp.isSuccess()) {
				log.error(resp.getMsg());
				return new TMResult(false, resp.getMsg(), null);
			}
			return new TMResult(true, "", resp.getSubusers());
		}

		@Override
		public TMResult<List<SubUserInfo>> applyResult(TMResult<List<SubUserInfo>> res) {
			return res;
		}
	}
	
}
