package controllers;

import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import result.pojo.rpt.RptCampaignResultPojo;
import result.pojo.rpt.RptCustResultPojo;
import actions.UserAction;
import bustbapi.AccountApis;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;

import configs.BusConfigs;
import dao.item.ItemDao;
import dao.rpt.RptCampaignDao;
import dao.rpt.RptCustDao;

public class BusCampaign extends TMController {

    private static final Logger log = LoggerFactory.getLogger(BusCampaign.class);

    public static void index() {
        render("subway/buscampaign.html");
    }
    
    //检查直通车权限
    public static void checkAuth() {
        User user = getUser();
        String userNick = getPrefUserNick();
        int subwayTokenStatus = UserAction.checkUserBusEnable(user.getSessionKey(), userNick);
        if (subwayTokenStatus == UserAction.SubwayTokenErrorType.NOT_MAIN_MEMBER) {
            renderFailedJson("发生了一些错误，您可能尚未开通直通车或者刚刚开通，请您先开通或等待一天~~");
        }
        if (subwayTokenStatus == UserAction.SubwayTokenErrorType.MEMBERID_IS_NULL) {
            renderFailedJson("发生了一些错误，您可能尚未开通直通车或者刚刚开通，请您先开通或等待一天~~");
        }
        
        renderSuccessJson();
    }
    
    //账户余额
    public static void queryUserBalance() {
        User user = getUser();
        
        Double balance = new AccountApis.BalanceGetApi(user.getSessionKey()).call();
        if (balance == null) {
            balance = 0d;
        }
        
        renderBusJson(balance);
    }
    
    //店铺报表
    public static void userDataRpt(long timeLength) {

        User user = getUser();
        String userNick = getPrefUserNick();

        if (timeLength <= 0) {
            timeLength = BusConfigs.RptConfig.RPT_DAY_SHOW;
        }

        Long startTs = 0L;
        Long endTs = 0L;
        
        if (timeLength > 0) {
            startTs = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS * timeLength;
            endTs = DateUtil.formYestadyMillis();
        } 

        List<RptCustResultPojo> resultList = RptCustDao.findByUserNick(user.getId(), userNick, startTs, endTs);

        renderBusJson(resultList);
    }
    
    
    //推广计划报表
    public static void queryRptCampaigns(long timeLength) {

        User user = getUser();
        String userNick = getPrefUserNick();

        if (timeLength <= 0) {
            timeLength = BusConfigs.RptConfig.RPT_DAY_SHOW;
        }
        //要不要先同步直通车计划。。。。
        List<RptCampaignResultPojo> rptPojoList = RptCampaignDao.findByUserNick(user.getId(), userNick, timeLength);

        renderBusJson(rptPojoList);

    }
    
    
    public static void busPreKey() {
        render("subway/busprekey.html");
    }
    
    public static void queryItems(String title, Long catId, String state,
            String orderProp, String orderType, int pn, int ps) {
        User user = getUser();

        boolean isOrderAsc = true;
        if (!StringUtils.isEmpty(orderType) && orderType.toLowerCase().equals("desc"))
            isOrderAsc = false;
        else
            isOrderAsc = true;

        String catIdStr = "";
        if (catId != null && catId.longValue() > 0) {
            catIdStr = String.valueOf(catId);
        }

        PageOffset po = new PageOffset(pn, ps, 10);

        int status = 2;//表示全部
        if (StringUtils.isEmpty(state)) {
            status = 2;
        }
        else if (state.toLowerCase().equals("all")) {
            status = 2;
        }
        else if (state.toLowerCase().equals("onsale")) {
            status = 0;
        }
        else if (state.toLowerCase().equals("instock")) {
            status = 1;
        }

        TMResult result = ItemDao.findWithOrder(user, title, status, catIdStr, orderProp, isOrderAsc, po);

        renderJSON(result);
    }
    
    public static void queryUser() {
        User user = getUser();
        
        renderBusJson(user);
    }
    
}
