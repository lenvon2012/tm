package bustbapi;

import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.AreaOption;
import com.taobao.api.domain.Campaign;
import com.taobao.api.domain.CampaignArea;
import com.taobao.api.domain.CampaignBudget;
import com.taobao.api.domain.CampaignPlatform;
import com.taobao.api.domain.CampaignSchedule;
import com.taobao.api.domain.ChannelOption;
import com.taobao.api.request.SimbaCampaignAddRequest;
import com.taobao.api.request.SimbaCampaignAreaGetRequest;
import com.taobao.api.request.SimbaCampaignAreaUpdateRequest;
import com.taobao.api.request.SimbaCampaignAreaoptionsGetRequest;
import com.taobao.api.request.SimbaCampaignBudgetGetRequest;
import com.taobao.api.request.SimbaCampaignBudgetUpdateRequest;
import com.taobao.api.request.SimbaCampaignChanneloptionsGetRequest;
import com.taobao.api.request.SimbaCampaignPlatformGetRequest;
import com.taobao.api.request.SimbaCampaignPlatformUpdateRequest;
import com.taobao.api.request.SimbaCampaignScheduleGetRequest;
import com.taobao.api.request.SimbaCampaignScheduleUpdateRequest;
import com.taobao.api.request.SimbaCampaignUpdateRequest;
import com.taobao.api.request.SimbaCampaignsGetRequest;
import com.taobao.api.response.SimbaCampaignAddResponse;
import com.taobao.api.response.SimbaCampaignAreaGetResponse;
import com.taobao.api.response.SimbaCampaignAreaUpdateResponse;
import com.taobao.api.response.SimbaCampaignAreaoptionsGetResponse;
import com.taobao.api.response.SimbaCampaignBudgetGetResponse;
import com.taobao.api.response.SimbaCampaignBudgetUpdateResponse;
import com.taobao.api.response.SimbaCampaignChanneloptionsGetResponse;
import com.taobao.api.response.SimbaCampaignPlatformGetResponse;
import com.taobao.api.response.SimbaCampaignPlatformUpdateResponse;
import com.taobao.api.response.SimbaCampaignScheduleGetResponse;
import com.taobao.api.response.SimbaCampaignScheduleUpdateResponse;
import com.taobao.api.response.SimbaCampaignUpdateResponse;
import com.taobao.api.response.SimbaCampaignsGetResponse;

public class CampaignApi {

    public static final Logger log = LoggerFactory.getLogger(CampaignApi.class);

    public static class CampaignAdd extends TBApi<SimbaCampaignAddRequest, SimbaCampaignAddResponse, Campaign> {

        public User user;
        public String nick;
        public String title;

        public CampaignAdd(User user, String nick, String title) {
            super(user.getSessionKey());
            this.user = user;
            this.nick = nick;
            this.title = title;
        }

        public CampaignAdd(User user, String title) {
            super(user.getSessionKey());
            this.user = user;
            this.nick = null;
            this.title = title;
        }

        @Override
        public SimbaCampaignAddRequest prepareRequest() {
            SimbaCampaignAddRequest req = new SimbaCampaignAddRequest();
            req.setTitle(title);
            if (nick != null && !StringUtils.equals(nick, user.getUserNick())) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public Campaign validResponse(SimbaCampaignAddResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getCampaign();
            }
            return null;
        }

        @Override
        public Campaign applyResult(Campaign res) {
            // TODO Auto-generated method stub
            return res;
        }

    }

    public static class CampaignsGet extends TBApi<SimbaCampaignsGetRequest, SimbaCampaignsGetResponse, List<Campaign>> {

        public User user;
        public String nick;

        public CampaignsGet(User user, String nick) {
            super(user.getSessionKey());
            this.user = user;
            this.nick = nick;
        }

        public CampaignsGet(User user) {
            super(user.getSessionKey());
            this.user = user;
            this.nick = null;
        }

        @Override
        public SimbaCampaignsGetRequest prepareRequest() {
            SimbaCampaignsGetRequest req = new SimbaCampaignsGetRequest();
            if (nick != null && !StringUtils.equals(nick, user.getUserNick())) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public List<Campaign> validResponse(SimbaCampaignsGetResponse resp) {
            if (resp.isSuccess()) {
                return resp.getCampaigns();
            }
            return null;
        }

        @Override
        public List<Campaign> applyResult(List<Campaign> res) {
            return res;
        }

    }

    public static class CampaignUpdate extends TBApi<SimbaCampaignUpdateRequest, SimbaCampaignUpdateResponse, Campaign> {

        public User user;
        public String nick;
        public Long campaignId;
        public String title;
        public String onlineStatus;

        public CampaignUpdate(User user, Long campaignId, String title, String onlineStatus) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.title = title;
            this.onlineStatus = onlineStatus;
            this.nick = null;
        }

        public CampaignUpdate(User user, Long campaignId, String nick, String title, String onlineStatus) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = nick;
            this.title = title;
            this.onlineStatus = onlineStatus;
        }

        @Override
        public SimbaCampaignUpdateRequest prepareRequest() {
            SimbaCampaignUpdateRequest req = new SimbaCampaignUpdateRequest();
            req.setCampaignId(campaignId);
            req.setTitle(title);
            req.setOnlineStatus(onlineStatus);
            if (nick != null && !StringUtils.equals(nick, user.getUserNick())) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public Campaign validResponse(SimbaCampaignUpdateResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getCampaign();
            }else{
                this.errorMsg = resp.getSubMsg();
                return null;
            }
            // return null;
        }

        @Override
        public Campaign applyResult(Campaign res) {
            return res;
        }

    }

    public static class CampaignAreaGet extends
            TBApi<SimbaCampaignAreaGetRequest, SimbaCampaignAreaGetResponse, CampaignArea> {

        public User user;
        public Long campaignId;
        public String nick;

        public CampaignAreaGet(User user, Long campaignId) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = null;
        }

        public CampaignAreaGet(User user, Long campaignId, String nick) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = nick;
        }

        @Override
        public SimbaCampaignAreaGetRequest prepareRequest() {
            SimbaCampaignAreaGetRequest req = new SimbaCampaignAreaGetRequest();
            req.setCampaignId(campaignId);
            if (nick != null && !StringUtils.equals(nick, user.getUserNick())) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public CampaignArea validResponse(SimbaCampaignAreaGetResponse resp) {
            if (resp.isSuccess()) {
                return resp.getCampaignArea();
            }
            return null;
        }

        @Override
        public CampaignArea applyResult(CampaignArea res) {
            // TODO Auto-generated method stub
            return res;
        }

    }

    public static class CampaignAreaUpdate extends
            TBApi<SimbaCampaignAreaUpdateRequest, SimbaCampaignAreaUpdateResponse, CampaignArea> {

        public User user;
        public Long campaignId;
        public String nick;
        public String area;

        public CampaignAreaUpdate(User user, Long campaignId, String nick, String area) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.area = area;
            this.nick = nick;
        }

        public CampaignAreaUpdate(User user, Long campaignId, String area) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.area = area;
            this.nick = null;
        }

        @Override
        public SimbaCampaignAreaUpdateRequest prepareRequest() {
            SimbaCampaignAreaUpdateRequest req = new SimbaCampaignAreaUpdateRequest();
            req.setArea(area);
            req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public CampaignArea validResponse(SimbaCampaignAreaUpdateResponse resp) {
            if (resp.isSuccess()) {
                return resp.getCampaignArea();
            }
            return null;
        }

        @Override
        public CampaignArea applyResult(CampaignArea res) {
            // TODO Auto-generated method stub
            return res;
        }

    }
    
    /**
     * 取得推广计划的可设置投放地域列表
     * @author Administrator
     *
     */
    public static class CampaignAreaOptionsGet extends
	    TBApi<SimbaCampaignAreaoptionsGetRequest, SimbaCampaignAreaoptionsGetResponse, List<AreaOption>> {

		public CampaignAreaOptionsGet(User user) {
		    super(user.getSessionKey());
		    
		}
		
		@Override
		public SimbaCampaignAreaoptionsGetRequest prepareRequest() {
			SimbaCampaignAreaoptionsGetRequest req = new SimbaCampaignAreaoptionsGetRequest();
		   
		    return req;
		}
		
		@Override
		public List<AreaOption> validResponse(SimbaCampaignAreaoptionsGetResponse resp) {
			ErrorHandler.validTaoBaoResp(resp);
        	
		    if (resp.isSuccess()) {
		        return resp.getAreaOptions();
		    }
		    return null;
		}
		
		@Override
		public List<AreaOption> applyResult(List<AreaOption> res) {
		    // TODO Auto-generated method stub
		    return res;
		}
	
	}
    

    public static class CampaignBudgetGet extends
            TBApi<SimbaCampaignBudgetGetRequest, SimbaCampaignBudgetGetResponse, CampaignBudget> {

        public Long campaignId;
        public String nick;

        public CampaignBudgetGet(String sid, Long campaignId) {
            this(sid, campaignId, null);
        }

        public CampaignBudgetGet(String sid, Long campaignId, String nick) {
            super(sid);
            this.campaignId = campaignId;
            this.nick = nick;
        }

        @Override
        public SimbaCampaignBudgetGetRequest prepareRequest() {
            SimbaCampaignBudgetGetRequest req = new SimbaCampaignBudgetGetRequest();
            req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public CampaignBudget validResponse(SimbaCampaignBudgetGetResponse resp) {
            if (resp == null) {
                log.warn("No result return !");
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getCampaignBudget();
            }
            return null;
        }

        @Override
        public CampaignBudget applyResult(CampaignBudget res) {
            return res;
        }

    }

    public static class CampaignBudgetUpdate extends
            TBApi<SimbaCampaignBudgetUpdateRequest, SimbaCampaignBudgetUpdateResponse, CampaignBudget> {

        public Long campaignId;
        public String nick;
        public long budget;
        public boolean useSmooth;

        public CampaignBudgetUpdate(String sid, Long campaignId, long budget, boolean useSmooth) {
            super(sid);
            this.campaignId = campaignId;
            this.budget = budget;
            this.useSmooth = useSmooth;
            this.nick = null;
        }

        public CampaignBudgetUpdate(String sid, String nick, Long campaignId, long budget, boolean useSmooth) {
            super(sid);
            this.campaignId = campaignId;
            this.budget = budget;
            this.useSmooth = useSmooth;
            this.nick = nick;
        }

        @Override
        public SimbaCampaignBudgetUpdateRequest prepareRequest() {
            SimbaCampaignBudgetUpdateRequest req = new SimbaCampaignBudgetUpdateRequest();
            req.setCampaignId(campaignId);
            req.setBudget(budget);
            req.setUseSmooth(useSmooth);
            if (nick != null) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public CampaignBudget validResponse(SimbaCampaignBudgetUpdateResponse resp) {

            if (resp == null) {
                log.warn("No result return !");
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getCampaignBudget();
            }
            return null;
        }

        @Override
        public CampaignBudget applyResult(CampaignBudget res) {
            return res;
        }

    }

    public static class CampaignChanneloptionsGet extends
            TBApi<SimbaCampaignChanneloptionsGetRequest, SimbaCampaignChanneloptionsGetResponse, List<ChannelOption>> {

        public User user;

        public CampaignChanneloptionsGet(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        @Override
        public SimbaCampaignChanneloptionsGetRequest prepareRequest() {
            SimbaCampaignChanneloptionsGetRequest req = new SimbaCampaignChanneloptionsGetRequest();

            return req;
        }

        @Override
        public List<ChannelOption> validResponse(SimbaCampaignChanneloptionsGetResponse resp) {
            if (resp.isSuccess()) {
                return resp.getChannelOptions();
            }
            return null;
        }

        @Override
        public List<ChannelOption> applyResult(List<ChannelOption> res) {
            // TODO Auto-generated method stub
            return res;
        }

    }

    public static class CampaignPlatformGet extends
            TBApi<SimbaCampaignPlatformGetRequest, SimbaCampaignPlatformGetResponse, CampaignPlatform> {

        public String sid;
        public Long campaignId;
        public String nick;

        public CampaignPlatformGet(String sid, Long campaignId, String nick) {
            super(sid);
            this.campaignId = campaignId;
            this.nick = nick;
        }

        @Override
        public SimbaCampaignPlatformGetRequest prepareRequest() {
            SimbaCampaignPlatformGetRequest req = new SimbaCampaignPlatformGetRequest();

            req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }

            return req;
        }

        @Override
        public CampaignPlatform validResponse(SimbaCampaignPlatformGetResponse resp) {
            if (resp == null) {
                log.warn("No result return !");
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                return resp.getCampaignPlatform();
            }
            return null;
        }

        @Override
        public CampaignPlatform applyResult(CampaignPlatform res) {
            return res;
        }

    }

    public static class CampaignPlatformUpdate extends
            TBApi<SimbaCampaignPlatformUpdateRequest, SimbaCampaignPlatformUpdateResponse, CampaignPlatform> {

        public User user;
        public Long campaignId;
        public String nick;
        public String searchChannels;
        public String nonsearchChannels;
        public long outsideDiscount;
        public String errorMsg = "";

        public CampaignPlatformUpdate(String sid, String nick, Long campaignId, String searchChannels,
                String nonsearchChannels, long outsideDiscount) {
            super(sid);

            this.nick = nick;
            this.campaignId = campaignId;
            this.searchChannels = searchChannels;
            this.nonsearchChannels = nonsearchChannels;
            this.outsideDiscount = outsideDiscount;
        }

        @Override
        public SimbaCampaignPlatformUpdateRequest prepareRequest() {
            SimbaCampaignPlatformUpdateRequest req = new SimbaCampaignPlatformUpdateRequest();
            req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
            req.setSearchChannels(searchChannels);
            req.setNonsearchChannels(nonsearchChannels);
            req.setOutsideDiscount(outsideDiscount);
            return req;
        }

        @Override
        public CampaignPlatform validResponse(SimbaCampaignPlatformUpdateResponse resp) {
            if (resp == null) {
                log.warn("No result return !");
            }

            ErrorHandler.validTaoBaoResp(resp);

            this.errorMsg = ErrorHandler.CommonTaobaoHandler.getInstance().getErrorMsg(resp);

            if (resp.isSuccess()) {
                return resp.getCampaignPlatform();
            }
            return null;
        }

        @Override
        public CampaignPlatform applyResult(CampaignPlatform res) {
            return res;
        }

        public String getErrorMsg() {
            return this.errorMsg;
        }

    }

    public static class CampaignScheduleGet extends
            TBApi<SimbaCampaignScheduleGetRequest, SimbaCampaignScheduleGetResponse, CampaignSchedule> {

        public User user;
        public Long campaignId;
        public String nick;

        public CampaignScheduleGet(User user, Long campaignId) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = null;
        }

        public CampaignScheduleGet(User user, Long campaignId, String nick) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.nick = nick;
        }

        @Override
        public SimbaCampaignScheduleGetRequest prepareRequest() {
            SimbaCampaignScheduleGetRequest req = new SimbaCampaignScheduleGetRequest();
            req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
            return req;
        }

        @Override
        public CampaignSchedule validResponse(SimbaCampaignScheduleGetResponse resp) {
            if (resp.isSuccess()) {
                return resp.getCampaignSchedule();
            }
            return null;
        }

        @Override
        public CampaignSchedule applyResult(CampaignSchedule res) {
            // TODO Auto-generated method stub
            return res;
        }

    }

    public static class CampaignScheduleUpdate extends
            TBApi<SimbaCampaignScheduleUpdateRequest, SimbaCampaignScheduleUpdateResponse, CampaignSchedule> {

        public User user;
        public Long campaignId;
        public String nick;
        public String schedule;

        public CampaignScheduleUpdate(User user, Long campaignId, String schedule) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.schedule = schedule;
            this.nick = null;
        }

        public CampaignScheduleUpdate(User user, Long campaignId, String nick, String schedule) {
            super(user.getSessionKey());
            this.user = user;
            this.campaignId = campaignId;
            this.schedule = schedule;
            this.nick = nick;
        }

        @Override
        public SimbaCampaignScheduleUpdateRequest prepareRequest() {
            SimbaCampaignScheduleUpdateRequest req = new SimbaCampaignScheduleUpdateRequest();
            req.setCampaignId(campaignId);
            if (nick != null) {
                req.setNick(nick);
            }
            req.setSchedule(schedule);
            return req;
        }

        @Override
        public CampaignSchedule validResponse(SimbaCampaignScheduleUpdateResponse resp) {
            if (resp.isSuccess()) {
                return resp.getCampaignSchedule();
            }
            return null;
        }

        @Override
        public CampaignSchedule applyResult(CampaignSchedule res) {
            // TODO Auto-generated method stub
            return res;
        }

    }
    
    
}
