
package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdp.ApiJdpAdapter.JdpDelistTimeFixer;
import jdp.ApiJdpAdapter.JdpStatusClearFixJob;
import jdp.ApiJdpAdapter.JdpStatusFastFixerJob;
import jdp.ApiJdpAdapter.JdpUserOnSaleFixer;
import jdp.ApiJdpAdapter.JdpWindowStatusJob;
import jdp.ApiJdpAdapter.PrintUserJdpAvailableJob;
import jdp.ApiJdpAdapter.UserCurrJdpStatusPrintJob;
import jdp.ApiJdpAdapter.WindowStatusMatchCheckerJob;
import jdp.ApiJdpAdapter.WindowUserJdpStatusPrint;
import jdp.DoForAllModifiedItemsJob;
import jdp.JdpListenEnsuer;
import jdp.JdpReRegisterAllUserJob;
import jdp.JdpRegisterAllUserJob;
import jdp.JdpStatusValidJob.AllJdpUserPrintJob;
import job.AutoCommentCrontabJob;
import job.CatAnalysisFilterJob;
import job.CatClickRatePicJob;
import job.FixNoMatchTopURLBaseCidJob;
import job.ItemCatOrderPayTimeDisTributeJob;
import job.MidNightJob;
import job.UnCommentedTradeJob;
import job.UpdateCatOrderpayTimeDistributeByDelistTime;
import job.UserUnCommentOrders;
import job.apiget.UpdateAllUserVersionJob;
import job.apiget.VasOrderUpdateJob;
import job.autolist.AutoDelistClearJob;
import job.click.HourlyCheckerJob;
import job.clouddate.UpdateUserWeekViewTradeJob;
import job.shop.GetShopScoreJob;
import job.shop.UpdateShopSalesJob;
import job.showwindow.DropCacheInitJob;
import job.showwindow.LargeItemNumDropWindowCacheJob;
import job.showwindow.ShowWindowCrontabJob;
import job.showwindow.ShowWindowExecutor.PrintUserWinwdowStatus;
import job.ump.UmpMjsTmplUpdateJob;
import job.user.UpdateTitleOptimisedJob;
import job.word.BusRefreshWordJob;
import job.word.HotWordUpdateJob;
import job.word.HotWordUpdateJob.TopEtaoWordLevel2UpdateJob;
import job.word.MainWordJob;
import job.word.NewTopKeyNavSpider;
import job.word.NewTopKeySpider;
import job.word.Update50WWordsJob;
import models.CloudDataRegion.CloudDataRegionUpdateJob;
import models.mysql.word.WordBase.RawWordBaseUpdateJob;
import models.ppdazhe.PPhongbao;
import models.showwindow.DropWindowTodayCache.CheckRecentToDropCacheJob;
import models.user.User;
import models.word.top.BusTopKey;
import models.word.top.BusTopKey.BusTopKeyWordBaseUpdateJob;
import models.word.top.TopKey.TopKeyWordBaseUpdateJob;
import models.word.top.TopURLBase.TopUrlBaseCidUpdateJob;
import onlinefix.ClearAllDeletedExcludeItems;
import onlinefix.ClearAllSaleCacheJob;
import onlinefix.ClearAllUserCache;
import onlinefix.FixAllUserListener;
import onlinefix.FixAllUserMobile;
import onlinefix.FixAllUserTracerJob;
import onlinefix.FixAllUserValid;
import onlinefix.FixMonitorInstall;
import onlinefix.FixUserSaleCacheJob;
import onlinefix.IndustryJob;
import onlinefix.RebuildTopKeyJob;
import onlinefix.UpdateAllItemsJob;
import onlinefix.UpdateAllWordBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.mvc.Controller;
import play.mvc.With;
import result.TMResult;
import underup.frame.industry.UpdateItemProps;
import underup.frame.industry.UpdateItems;
import ats.TMCAddAllListener;
import bustbapi.ClickApi.DoChedaoCilckJob;
import bustbapi.FenxiaoApi.FixAllUserFengxiaoJob;
import bustbapi.OperateItemApi.PrintUserNotToDoWindowStatus;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.UserDao;
import dao.UserDao.UserShopCidUpdateJob;

@With(Secure.class)
public class CRUDJobExecutor extends Controller {

    private static final Logger log = LoggerFactory.getLogger(CRUDJobExecutor.class);

    public static final String TAG = "CRUDJobExecutor";

    public static Map<String, Class<? extends Job>> commonJobs = new HashMap<String, Class<? extends Job>>();

    static void putClass(Class<? extends Job> cls) {
        commonJobs.put(cls.getSimpleName(), cls);
    }

    static {

        putClass(PrintUserNotToDoWindowStatus.class);
        /**
         * Build Cache for the word..
         */
        putClass(WordCacheJob.class);
        /**
         * 检测橱窗状态
         */
        putClass(WindowStatusMatchCheckerJob.class);

        /**
         * 检测橱窗状态
         */
        putClass(WindowStatusMatchCheckerJob.class);
        /**
         * 准备jdpwindow
         */
        putClass(JdpWindowStatusJob.class);
        /**
         * 用来修正 onsale 数量不对的卖家
         */
        putClass(JdpUserOnSaleFixer.class);
        /**
         * 包含了橱窗用户的状态
         */
        putClass(WindowUserJdpStatusPrint.class);
        /**
         * 用来修复那些已经变动过的delistTime
         */
        putClass(JdpDelistTimeFixer.class);
        /**
         * 由于之前改过rdsname， 所以有重新注册的需求
         */
        putClass(JdpReRegisterAllUserJob.class);
        /**
         * 快速复活销量和流量大法
         */
        putClass(JdpStatusFastFixerJob.class);
        /**
         * 查看当前用户的jdp 的 同步状态和监听状态
         */
        putClass(UserCurrJdpStatusPrintJob.class);
        /**
         * 修正用户 jdp 监听状态
         */
        putClass(JdpStatusClearFixJob.class);

        putClass(AllJdpUserPrintJob.class);
        putClass(TopEtaoWordLevel2UpdateJob.class);

        putClass(PrintUserJdpAvailableJob.class);
        putClass(PrintUserWinwdowStatus.class);
        putClass(JdpListenEnsuer.class);

        putClass(DoForAllModifiedItemsJob.class);
        putClass(JdpRegisterAllUserJob.class);

        putClass(HotWordUpdateJob.class);
        putClass(TMCAddAllListener.class);
        
//        commonJobs.put(FixAllListenerJob.class.getSimpleName(), FixAllListenerJob.class);
        putClass(MainWordJob.class);
        putClass(BusRefreshWordJob.class);
        putClass(MidNightJob.class);
        putClass(HourlyCheckerJob.class);
        putClass(LargeItemNumDropWindowCacheJob.class);
        // 检查用户未评价订单
        putClass(UserUnCommentOrders.class);

        putClass(AutoCommentCrontabJob.class);

        putClass(TopKeyWordBaseUpdateJob.class);
        putClass(BusTopKeyWordBaseUpdateJob.class);
        putClass(RawWordBaseUpdateJob.class);

        putClass(UserShopCidUpdateJob.class);
        putClass(DoChedaoCilckJob.class);
        putClass(FixAllUserTracerJob.class);
        putClass(FixAllUserListener.class);
        putClass(FixAllUserValid.class);
        putClass(ShowWindowCrontabJob.class);
        putClass(UpdateAllItemsJob.class);
        putClass(UpdateAllWordBase.class);
        putClass(IndustryJob.class);
        putClass(FixMonitorInstall.class);
        putClass(FixAllUserMobile.class);
        putClass(UpdateAllUserVersionJob.class);
        putClass(AutoDelistClearJob.class);
        putClass(ClearAllUserCache.class);
        putClass(FixAllUserFengxiaoJob.class);
        putClass(ClearAllSaleCacheJob.class);
        putClass(NewTopKeySpider.class);
        putClass(NewTopKeyNavSpider.class);
        putClass(FixUserSaleCacheJob.class);
        putClass(VasOrderUpdateJob.class);
        putClass(ClearAllDeletedExcludeItems.class);
        putClass(DropCacheInitJob.class);
        putClass(CheckRecentToDropCacheJob.class);
        putClass(UnCommentedTradeJob.class);
        putClass(CatAnalysisFilterJob.class);
        putClass(Update50WWordsJob.class);
        //更新卖家销量的job
        putClass(UpdateShopSalesJob.class);
        // 更新topkey
        putClass(RebuildTopKeyJob.class);
        // 删除bustopkey 某些关键词的空格
        putClass(BusTopKey.BusTopKeyTrimJob.class);
        // 更新宝贝是否优化过
        putClass(UpdateTitleOptimisedJob.class);
        // 更新TopURLBase的itemcat
        putClass(TopUrlBaseCidUpdateJob.class);
        // 更新CloudDataRegion
        putClass(CloudDataRegionUpdateJob.class);
        // 更新每个用户一周内按小时聚合的流量销量数据
        putClass(UpdateUserWeekViewTradeJob.class);
        // 获取每个类目下，订单的时间分布，用paytime区分，24小时制
        putClass(ItemCatOrderPayTimeDisTributeJob.class);
        // 获取钻级以上用户，点击率大于6%的宝贝图片
        putClass(CatClickRatePicJob.class);
        // 使用行业上下架分析更新买家什么时候来的数据
        putClass(UpdateCatOrderpayTimeDistributeByDelistTime.class);
        
        putClass(UmpMjsTmplUpdateJob.class);
        
        // 更新不能映射到TopUrlBase的类目
        putClass(FixNoMatchTopURLBaseCidJob.class);
        // 更新zyh行业数据
        putClass(UpdateItems.class);
        putClass(UpdateItemProps.class);
        /**
         * 获取卖家店铺动态评分
         */
        putClass(GetShopScoreJob.class);
    }

    public static void commJobs() {
        renderJSON(commonJobs.keySet());
    }

    public static void userJobs() {
//        renderJSON(userJobClasses.keySet());
    }

    public static void runJob(String job) throws Exception {
        Class<? extends Job> jobClass = commonJobs.get(job);
        Job instance = jobClass.newInstance();
        instance.now();

        renderText("[Job" + job + "] starts to run...");
    }

    public static void runUserJob(String job, Long userId, String userNick, long ts) throws InstantiationException,
            IllegalAccessException {

    }

    public static void userDailyJobs() {

    }

    public static void allUserName() {
        //List<User> users = User.findAll();
        List<User> users = UserDao.fetchAllUser();
        List<String> names = new ArrayList<String>();
        for (User user : users) {
            names.add(user.getUserNick());
        }
        renderJSON(names);
    }

    public static void selectPPhongbaoUsers(int status, int pn, int ps) {
        PageOffset po = new PageOffset(pn, ps);
        List<PPhongbao> userInfos = PPhongbao.findByStatus(status, pn, ps);
        long count = PPhongbao.countByStatus(status);

        renderJSON(new TMResult(userInfos, (int) count, po));
    }

    public static void setPPhongbaoStatus() {
        List<PPhongbao> userInfos = PPhongbao.findByStatus(0);

        if (CommonUtils.isEmpty(userInfos)) {
            return;
        }

        else {
            for (PPhongbao userInfo : userInfos) {
                userInfo.setStatus(1);

                boolean success = userInfo.jdbcSave();
                if (!success) {
                    renderJSON(new TMResult("设置出错！！！！"));
                }
            }
        }
    }

}
