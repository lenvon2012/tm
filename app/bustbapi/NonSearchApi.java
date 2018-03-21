package bustbapi;

import java.util.List;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Demographic;
import com.taobao.api.domain.DemographicSetting;
import com.taobao.api.domain.Place;
import com.taobao.api.request.SimbaNonsearchAlldemographicsGetRequest;
import com.taobao.api.request.SimbaNonsearchAllplacesGetRequest;
import com.taobao.api.request.SimbaNonsearchDemographicsGetRequest;
import com.taobao.api.request.SimbaNonsearchDemographicsUpdateRequest;
import com.taobao.api.response.SimbaNonsearchAlldemographicsGetResponse;
import com.taobao.api.response.SimbaNonsearchAllplacesGetResponse;
import com.taobao.api.response.SimbaNonsearchDemographicsGetResponse;
import com.taobao.api.response.SimbaNonsearchDemographicsUpdateResponse;

/**
 * 定向推广投放
 * @author Administrator
 *
 */
public class NonSearchApi {
	public static final Logger log = LoggerFactory.getLogger(NonSearchApi.class);

    /**
     * 获取定向投放人群维度列表
     * @author Administrator
     *
     */
    public static class AllDemographicsGet extends
	    TBApi<SimbaNonsearchAlldemographicsGetRequest, SimbaNonsearchAlldemographicsGetResponse, List<Demographic>> {
	
		public User user;
		
		public AllDemographicsGet(User user) {
		    super(user.getSessionKey());
		    this.user = user;
		}

		@Override
        public SimbaNonsearchAlldemographicsGetRequest prepareRequest() {
			SimbaNonsearchAlldemographicsGetRequest req = new SimbaNonsearchAlldemographicsGetRequest();
            return req;
        }

        @Override
        public List<Demographic> validResponse(SimbaNonsearchAlldemographicsGetResponse resp) {
        	ErrorHandler.validTaoBaoResp(resp);
        	
            if (resp.isSuccess()) {
                return resp.getDemographicList();
            }
            return null;
        }

        @Override
        public List<Demographic> applyResult(List<Demographic> res) {
            // TODO Auto-generated method stub
            return res;
        }
		
		
	
	}
    
    /**
     * 获取给定campaign设置的投放人群维度列表
     * @author Administrator
     *
     */
    public static class DemographicsGet extends
	    TBApi<SimbaNonsearchDemographicsGetRequest, SimbaNonsearchDemographicsGetResponse, List<DemographicSetting>> {
	
		public User user;
		public Long campaignId;
        public String nick;
		
		public DemographicsGet(User user, Long campaignId) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = null;
        }

        public DemographicsGet(User user, Long campaignId, String nick) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = nick;
        }

		@Override
        public SimbaNonsearchDemographicsGetRequest prepareRequest() {
			SimbaNonsearchDemographicsGetRequest req = new SimbaNonsearchDemographicsGetRequest();
            
			req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
			return req;
        }

        @Override
        public List<DemographicSetting> validResponse(SimbaNonsearchDemographicsGetResponse resp) {
        	//这里可能返回错误的，没有处理
        	ErrorHandler.validTaoBaoResp(resp);
        	
            if (resp.isSuccess()) {
                return resp.getDemographicSettingList();
            }
            return null;
        }

        @Override
        public List<DemographicSetting> applyResult(List<DemographicSetting> res) {
            // TODO Auto-generated method stub
            return res;
        }
	
	}
    
    
    /**
     * 设置投放人群维度加价
     * @author Administrator
     *
     */
    public static class DemographicsUpdate extends
	    TBApi<SimbaNonsearchDemographicsUpdateRequest, SimbaNonsearchDemographicsUpdateResponse, List<DemographicSetting>> {

		public User user;
		public Long campaignId;
        public String nick;
        private String demographicIdPriceJson;//[{“demographicId”:102232,”incrementalPrice”:22}, {“demographicId”:102232,” incrementalPrice”:22} ]
		
		public DemographicsUpdate(User user, Long campaignId, String demographicIdPriceJson) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.demographicIdPriceJson = demographicIdPriceJson;
            this.nick = null;
        }

        public DemographicsUpdate(User user, Long campaignId, String nick, String demographicIdPriceJson) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = nick;
            this.demographicIdPriceJson = demographicIdPriceJson;
        }

		@Override
        public SimbaNonsearchDemographicsUpdateRequest prepareRequest() {
			SimbaNonsearchDemographicsUpdateRequest req = new SimbaNonsearchDemographicsUpdateRequest();
            
			req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
            req.setDemographicIdPriceJson(demographicIdPriceJson);
			return req;
        }

        @Override
        public List<DemographicSetting> validResponse(SimbaNonsearchDemographicsUpdateResponse resp) {
        	ErrorHandler.validTaoBaoResp(resp);
        	
            if (resp.isSuccess()) {
                return resp.getDemographicSettingList();
            }
            return null;
        }

        @Override
        public List<DemographicSetting> applyResult(List<DemographicSetting> res) {
            // TODO Auto-generated method stub
            return res;
        }
	
	}

    
    
    /**
     * 获取全部单独出价投放位置列表
     * @author Administrator
     *
     */
    public static class AllPlacesGet extends
        TBApi<SimbaNonsearchAllplacesGetRequest, SimbaNonsearchAllplacesGetResponse, List<Place>> {
    
        public User user;
        
        public AllPlacesGet(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        @Override
        public SimbaNonsearchAllplacesGetRequest prepareRequest() {
            SimbaNonsearchAllplacesGetRequest req = new SimbaNonsearchAllplacesGetRequest();
            return req;
        }

        @Override
        public List<Place> validResponse(SimbaNonsearchAllplacesGetResponse resp) {
            if (resp == null) {
                return null;
            }
            if (resp.isSuccess() == false) {
                return null;
            }
            return resp.getPlaceList();
        }

        @Override
        public List<Place> applyResult(List<Place> res) {
            return res;
        }
        
        
    
    }
    
    
    
}
