
package job;

import ats.TMCAddAllListener;
import ats.TMCSdkListener;
import com.ciaosir.client.CommonUtils;
import configs.TMConfigs;
import controllers.APIConfig;
import models.CatClickRatePic;
import models.CatPayHourDistribute;
import models.CategoryProps;
import models.CPEctocyst.ChiefStaffDetail;
import models.associate.AssociateModel;
import models.associate.AssociatePlan;
import models.associate.AssociatedItems;
import models.comment.CommentConf;
import models.defense.TidReceiveTime;
import models.fenxiao.ItemDescLinks;
import models.fenxiao.ItemDescPlay;
import models.helpcenter.HelpNavLevel1;
import models.fenxiao.RemoveDescLinkLog;
import models.group.FavoriteModel;
import models.group.GroupModel;
import models.group.GroupPlan;
import models.group.GroupedItems;
import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.item.NoPropsItemCat;
import models.jd.JDItemPlay;
import models.mainsearch.MainSearchHistory;
import models.newCatPayHourDistribute;
import models.oplog.TMUserWorkRecord;
import models.order.JDOrderDisplay;
import models.order.OrderDisplay;
import models.ppmanage.PPStock;
import models.pricelog.EditItemPriceLog;
import models.promotion.TMProActivity;
import models.relation.RelationedItems;
import models.sms.SmsSendLog;
import models.trade.JDTradeDisplay;
import models.trade.TradeDisplay;
import models.traderate.TradeRatePlay;
import models.ump.PromotionPlay;
import models.ump.ShopMinDiscountGetLog;
import models.ump.ShopMinDiscountPlay;
import models.ump.removeMjsTmplForEndActivityLog;
import models.updatetimestamp.updates.WorkTagUpdateTs;
import models.user.TitleOptimised;
import models.user.UserOPVisitCount;
import models.word.ElasticRawWord;
import models.word.top.TopKey;
import models.word.top.TopURLBase;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import ppapi.models.PaiPaiItem;
import ppapi.models.PaiPaiTradeDisplay;
import ppapi.models.PaiPaiTradeItem;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.PolicyDBUtil;

import java.util.List;
import java.util.concurrent.Callable;

@OnApplicationStart(async = true)
public class AsyncStartUpJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(AsyncStartUpJob.class);

    public static final String TAG = "StartUpAsyncJob";

    public void doJob() {
        try {
            APIConfig.get().doOnStartUpAsync();
//            MixPlanRecommend.ensure();
            HelpNavLevel1.ensureBase();

            if (Play.mode.isProd() && APIConfig.get().isItemScoreRelated() && TMConfigs.Server.jobTimerEnable) {
                // new FixUserSaleCacheJob().now();
            }

            try {
                ensureEditItemPriceLog();
                
                ensureShopMindiscount();
                
                ensureOuterid();
                ensureTidReceiveTime();
                ensureWorkTag();
                ensureTMJdpFailLog();
                ensureTopKey();
                ensureItemTable();
                ensureItemCat();

                ensureTradeRate();

                ensureItemDesc();

                ensureCommentConf();
                ensureSmsSendLog();
                ensureMainSearchHistory();

                ensureJDTradeDisplay();
                ensureJDOrderDisplay();
                ensureJDItemPlay();
                // BusTopKey.ensure();

                ensurePaiPaiItem();
                ensurePaiPaiTradeDisplay();
                ensurePaiPaiTradeItem();
//                ensureFenxiangToken();
                ensurePPStock();
                ensureTitleOpyimised();
                ensureRecentlyDiagedItem();
                ensureRelationedItems();
                ensureUserOPVisitCount();
                ensureElasticWord();
                ensureNoPropsItemCat();
                ensureCategoryProps();

                ensureTradeDisplay();
                ensureOrderDisplay();
                ensureOrderTitleAndReceiveName();
                ensureCatPayHourDistribute();
                ensureNewCatPayHourDistribute();
                ensureremoveMjsTmplForEndActivityLog();
                ensureUserCDNPic();
                
                
                ensureAssociateModel();
                ensureAssociatePlan();
                ensureAssociatedItems();

                
                ensureMainSearchHistoryAreaColumn();

                ensuerGroupModels();
                ensuerGroupPlan();
                ensuerGroupedItem();
                ensureFavoriteModel();
                ensureChiefStaffDetail();
//                ensureCatClickRatePic();
//                ensureTMProActivity16();
//                ensurePromotionPlay16();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            // ensureTopUrlBase();

            if (Play.mode.isProd()) {
//                if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
//                    new FixAllUserListener().now();
//                } else {
//                }
                TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                    @Override
                    public ItemPlay call() throws Exception {
                        new TMCSdkListener().doJob();
                        new TMCAddAllListener().doJob();
                        return null;
                    }
                });

            }
            //MemcachedClient client = new MemcachedClient();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }
    
    private void ensureMainSearchHistoryAreaColumn() {
        try {
            String query = "select area from " + MainSearchHistory.TABLE_NAME + " limit 1 ";
            boolean isSuccess = MainSearchHistory.dp.checkExecuteQuery(query);
            
            if (isSuccess == false) {
                PolicyDBUtil.loadSqlFile(MainSearchHistory.dp, "alter_mainsearchhistory.sql");
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    private void ensureEditItemPriceLog() {
        try {
            String query = "select id from " + EditItemPriceLog.TABLE_NAME + " limit 1 ";
            long id = EditItemPriceLog.dp.singleLongQuery(query);
            
            if (id <= 0) {
                PolicyDBUtil.loadSqlFile(EditItemPriceLog.dp, "edit_item_price_log.sql");
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    private void ensureShopMindiscount() {
        
        try {
            String query = "select userId from " + ShopMinDiscountPlay.TABLE_NAME + " where userId > 0 limit 1 ";
            long userId = ShopMinDiscountPlay.dp.singleLongQuery(query);
            
            if (userId <= 0) {
                PolicyDBUtil.loadSqlFile(ShopMinDiscountPlay.dp, "shopmindiscountplay.sql");
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        
        try {
            String query = "select id from " + ShopMinDiscountGetLog.TABLE_NAME + " where id > 0 limit 1 ";
            long id = ShopMinDiscountGetLog.dp.singleLongQuery(query);
            
            if (id <= 0) {
                PolicyDBUtil.loadSqlFile(ShopMinDiscountGetLog.dp, "shopmindiscountgetlog.sql");
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    }

    private void ensureOuterid() {
        log.warn("ensure outer id");
        try {
            boolean hasTable = JDBCBuilder.singleIntQuery(ItemPlay.dp.getSrc(), " select outerId from "
                    + ItemPlay.TABLE_NAME + "0 limit 1") >= 0;
            log.info(" res : " + hasTable);
            if (!hasTable) {
                PolicyDBUtil.loadSqlFile(ItemPlay.dp.getSrc(), "alterOuterid.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void ensureTidReceiveTime() {
        try {

            boolean hasTable = JDBCBuilder.singleIntQuery(TidReceiveTime.src, " select 1 from "
                    + TidReceiveTime.TABLE_NAME + " limit 1") >= 0;

            log.info(" res : " + hasTable);
            if (!hasTable) {
                PolicyDBUtil.loadSqlFile(TidReceiveTime.src, "tid_receive_time.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureWorkTag() {
        log.error("ensureWorkTag");
        try {

            boolean hasTable = JDBCBuilder.singleIntQuery(WorkTagUpdateTs.getSrc(), " select 1 from "
                    + WorkTagUpdateTs.TABLE_NAME + " limit 1") >= 0;

            log.info(" res : " + hasTable);
            if (!hasTable) {
                PolicyDBUtil.loadSqlFile(WorkTagUpdateTs.getSrc(), "tm_jdp_work_tag_update_ts.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureTMJdpFailLog() {
        log.error("ensureTMJdpFailLog ");
        try {

            boolean hasTable = JDBCBuilder.singleIntQuery(TMUserWorkRecord.getSrc(), " select 1 from "
                    + TMUserWorkRecord.TABLE_NAME + " limit 1") >= 0;

            log.info(" res : " + hasTable);
            if (!hasTable) {
                PolicyDBUtil.loadSqlFile(TMUserWorkRecord.getSrc(), "tm_jdp_fail_record.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }

    private void ensureTopKey() {
        log.error(" ensure top key ");
        try {

            List<TopKey> list = TopKey.fetch(" true limit 1");

            if (CommonUtils.isEmpty(list)) {
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "topkey_.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }

    private void ensureOrderTitleAndReceiveName() {
        log.error(" ensure order display receive name");
        try {

            int res = JDBCBuilder.singleIntQuery(OrderDisplay.dp.getSrc(),
                    "select 1 from order_display_0 where phone is null or phone = ''  limit 1;;");

            if (res < 0) {
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "tradedisplay16_add_picpath.sql");
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "orderdisplay16_alter_title.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }

    private void ensureNewCatPayHourDistribute() {
        log.error(" ensure NewCatPayHourDistribute");
        try {

            int res = JDBCBuilder.singleIntQuery(newCatPayHourDistribute.dp.getSrc(),
                    "select 1 from new_cat_payhour_distribute limit 1;");

            if (res < 0) {
                PolicyDBUtil.loadSqlFile(newCatPayHourDistribute.dp.getSrc(), "new_cat_payhour_distribute.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }
    
    private void ensureUserCDNPic() {
        log.error(" ensure ensureUserCDNPic");
        try {
            long existId = JDBCBuilder.singleLongQuery("select picId from user_cdn_pic limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(DataSrc.BASIC, "user_cdn_pic.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }
    
    private void ensureremoveMjsTmplForEndActivityLog() {
        log.error(" ensure removeMjsTmplForEndActivityLog");
        try {

            int res = JDBCBuilder.singleIntQuery(removeMjsTmplForEndActivityLog.dp.getSrc(),
                    "select 1 from remove_mjs_tmpl_for_end_activity_log limit 1;");

            if (res < 0) {
                PolicyDBUtil.loadSqlFile(removeMjsTmplForEndActivityLog.dp.getSrc(), "remove_mjs_tmpl_for_end_activity_log.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }

    private void ensureCatPayHourDistribute() {
        log.error(" ensure CatPayHourDistribute");
        try {

            int res = JDBCBuilder.singleIntQuery(CatPayHourDistribute.dp.getSrc(),
                    "select 1 from cat_payhour_distribute limit 1;");

            if (res < 0) {
                PolicyDBUtil.loadSqlFile(CatPayHourDistribute.dp.getSrc(), "cat_payhour_distribute.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }
    
    private void ensureCatClickRatePic() {

        log.error(" ensure CatClickRatePic");
        try {

            int res = JDBCBuilder.singleIntQuery(CatClickRatePic.dp.getSrc(),
                    "select 1 from cat_click_rate limit 1;");

            if (res < 0) {
                PolicyDBUtil.loadSqlFile(CatClickRatePic.dp.getSrc(), "cat_click_rate.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }

    private void ensureTopUrlBase() {
        boolean hasTopUrlBase = false;
        try {
            hasTopUrlBase = TopURLBase.find(" 1 =1 ").first() != null;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        if (!hasTopUrlBase) {
            PolicyDBUtil.loadSqlFile("top_url_base.sql");
        }
    }

    private void ensureItemCat() {
        try {
            long existId = 0L;
            existId = ItemCatPlay.findExistId(16L);
            log.error(" find exist id :" + existId);
            if (existId <= 0L) {
                PolicyDBUtil.loadSqlFile("item_cat.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
    
    private void ensureNoPropsItemCat() {
        try {
            long existId = 0L;
            existId = NoPropsItemCat.findExistId(1622L);
            log.error(" find exist id :" + existId);
            if (existId <= 0L) {
                PolicyDBUtil.loadSqlFile(NoPropsItemCat.src, "no_props_item_cat.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void ensureItemTable() {
        try {
            long existId = 0L;
            existId = ItemPlay.findExistId(0L, 0L);
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("createitem16.sql");
                PolicyDBUtil.loadQuotaSqlFile("createitem16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureTradeDisplay() {
        try {
            long existId = 0L;
            existId = TradeDisplay.findExistId(0L, 0L);
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("tradedisplay16.sql");
                PolicyDBUtil.loadQuotaSqlFile("tradedisplay16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureOrderDisplay() {
        log.error(" ensure order display:");
        try {
            long existId = 0L;
            existId = OrderDisplay.findExistId(0L, 0L);
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("orderdisplay16.sql");
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "orderdisplay16.sql");
            }

            existId = OrderDisplay.dp.singleLongQuery("select sellerRate from order_display_0 limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "orderdisplay16_alter.sql");
            }

            String ret = OrderDisplay.dp.singleStringQuery("select receiverName from order_display_0 limit 1");
            if (ret == StringUtils.EMPTY) {
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "orderdisplay16_alter_name.sql");
            }

            ret = OrderDisplay.dp.singleStringQuery("select buyerNick from order_display_0 limit 1;");
            if (ret == StringUtils.EMPTY) {
                PolicyDBUtil.loadSqlFile(OrderDisplay.dp.getSrc(), "orderdisplay16_alter_buynernick.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureCommentConf() {
        try {
            long existId = CommentConf.dp.singleLongQuery("select commentType from comment_conf limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(CommentConf.dp.getSrc(), "alter_commentconf.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }
    
    private void ensureAssociateModel(){
        try{
            long existId = AssociateModel.dp.singleLongQuery("select id from associate_model limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(AssociateModel.dp.getSrc(),"associate_model.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensureAssociatePlan(){
        try{
            long existId = AssociatePlan.dp.singleLongQuery("select id from associate_plan limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(AssociatePlan.dp.getSrc(),"associate_plan.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensureAssociatedItems(){
        try{
            long existId = AssociatedItems.dp.singleLongQuery("select id from associated_items limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(AssociatedItems.dp.getSrc(),"associated_items.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensuerGroupModels(){
        try{
            long existId = GroupModel.dp.singleLongQuery("select id from group_Model limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(GroupModel.dp.getSrc(),"group_Model.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensuerGroupPlan(){
        try{
            long existId = GroupPlan.dp.singleLongQuery("select id from group_Plan limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(GroupPlan.dp.getSrc(),"group_Plan.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensuerGroupedItem(){
        try{
            long existId = GroupedItems.dp.singleLongQuery("select id from grouped_Items limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(GroupedItems.dp.getSrc(),"grouped_Items.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensureChiefStaffDetail(){
        try{
            long existId = ChiefStaffDetail.dp.singleLongQuery("select id from chief_staff_detail limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(ChiefStaffDetail.dp.getSrc(),"chief_staff_detail.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensureFavoriteModel(){
        try{
            long existId = FavoriteModel.dp.singleLongQuery("select id from group_FavoriteModel limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(FavoriteModel.dp.getSrc(),"group_FavoriteModel.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
    private void ensureTitleOpyimised() {
        try {
            long existId = TitleOptimised.dp.singleLongQuery("select id from title_optimised limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(TitleOptimised.dp.getSrc(), "title_optimised.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureRecentlyDiagedItem() {
        try {
            long existId = JDBCBuilder.singleLongQuery("select numIid from recently_diaged_item limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(DataSrc.BASIC, "recently_diaged_item.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureRelationedItems() {
        try {
            long existId = RelationedItems.dp.singleLongQuery("select id from relationed_items limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(RelationedItems.dp.getSrc(), "relationed_items.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureUserOPVisitCount() {
        try {
            long existId = UserOPVisitCount.dp.singleLongQuery("select id from user_op_visit_count limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(UserOPVisitCount.dp, "user_op_visit_count.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureElasticWord() {
        try {
            long existId = ElasticRawWord.rawwordDispatcher.singleLongQuery("select id from searchkey_ limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(ElasticRawWord.rawwordDispatcher, "searchkey.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureCategoryProps() {
        try {
            long existId = CategoryProps.dp.singleLongQuery("select cid from category_props limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(CategoryProps.dp, "category_props.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureMainSearchHistory() {
        try {
            long existId = MainSearchHistory.dp.singleLongQuery("select id from mainsearch_history limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(MainSearchHistory.dp, "mainsearch_history.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureSmsSendLog() {
        try {
            long existId = SmsSendLog.dp.singleLongQuery("select tid from SmsSendLog_ limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(SmsSendLog.dp, "alter_smssendlog.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void ensureJDTradeDisplay() {
        try {
            long existId = 0L;
            existId = JDTradeDisplay.findExistId(0L, 0L);
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("jd_tradedisplay16.sql");
                PolicyDBUtil.loadQuotaSqlFile("jd_tradedisplay16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureJDOrderDisplay() {
        try {
            long existId = 0L;
            existId = JDOrderDisplay.findExistId(0L, 0L);
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("jd_orderdisplay16.sql");
                PolicyDBUtil.loadQuotaSqlFile("jd_orderdisplay16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureTMProActivity16() {

        boolean isExist = false;

        for (int i = 0; i < 16; i++) {

            String sql = "select id from " + TMProActivity.TABLE_NAME + i + " limit 1";

            long existId = TMProActivity.dp.singleLongQuery(sql);
            if (existId > 0) {
                isExist = true;
                break;
            }
        }
        if (isExist == false) {
            PolicyDBUtil.loadSqlFile(TMProActivity.dp.getSrc(), "createactivity16.sql");
        }

    }

    private void ensurePromotionPlay16() {

        boolean isExist = false;

        for (int i = 0; i < 16; i++) {

            String sql = "select promotionId from " + PromotionPlay.TABLE_NAME + i + " limit 1";

            long existId = PromotionPlay.dp.singleLongQuery(sql);
            if (existId > 0) {
                isExist = true;
                break;
            }
        }
        if (isExist == false) {
            PolicyDBUtil.loadSqlFile(PromotionPlay.dp.getSrc(), "createpromotionplay16.sql");
        }

    }

    private void ensureJDItemPlay() {
        try {
            long existId = 0L;
            existId = JDItemPlay.dp.singleLongQuery("select uid from jd_item_0 limit 1");
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("jd_item16.sql");
                PolicyDBUtil.loadQuotaSqlFile("jd_item16.sql");
            }

            existId = JDItemPlay.dp.singleLongQuery("select skuId from jd_item_0 limit 1");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(JDItemPlay.dp.getSrc(), "jd_item_alter_skuid.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }
    
    private void ensureTMProActivityTmplHtml16() {
        try {
        	 boolean isExist = false;

             for (int i = 0; i < 16; i++) {

                 String sql = "select tmplHtml from " + TMProActivity.TABLE_NAME + i + " limit 1";

                 long existId = PromotionPlay.dp.singleLongQuery(sql);
                 if (existId > 0) {
                     isExist = true;
                     break;
                 }
             }
             if (isExist == false) {
                PolicyDBUtil.loadSqlFile(PromotionPlay.dp.getSrc(), "alter_tmproactivity.sql");
             }
            
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void ensureTradeRate() {
        try {
            long existId = 0L;
            existId = TradeRatePlay.findExistId(0L, 0L);
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("traderate16.sql");
                PolicyDBUtil.loadQuotaSqlFile("traderate16.sql");
            }

            existId = TradeRatePlay.dp.singleLongQuery("select sellerRate from trade_rate_0 limit 1");
            if (existId < 0L) {
                // PolicyDBUtil.loadSqlFile("traderate16_alter.sql");
                PolicyDBUtil.loadSqlFile(TradeRatePlay.dp, "traderate16_alter.sql");
            }

            String ret = TradeRatePlay.dp.singleStringQuery("select remark from trade_rate_0 limit 1");
            if (ret == StringUtils.EMPTY) {
                PolicyDBUtil.loadSqlFile(TradeRatePlay.dp, "traderate16_alter_remark.sql");
            }

            existId = TradeRatePlay.dp.singleLongQuery("select dispatchId from trade_rate_0 limit 1");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile(TradeRatePlay.dp, "traderate16_alter_dispatchId.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void ensureItemDesc() {
        try {
            long existId = 0L;
            existId = ItemDescPlay.findExistId(0L, 0L);
            if (existId < 0L) {
                PolicyDBUtil.loadQuotaSqlFile("item_desc16.sql");
            }

            existId = ItemDescPlay.dp.singleLongQuery("select status from item_desc_0 limit 1");
            if (existId < 0L) {
                PolicyDBUtil.loadQuotaSqlFile("item_desc16_alter_status.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        
        
        try {
            long existId = ItemDescLinks.dp.singleLongQuery("select id from "
                    + ItemDescLinks.TABLE_NAME + " limit 1");
            if (existId <= 0L) {
                PolicyDBUtil.loadSqlFile(ItemDescLinks.dp, "item_desc_link.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        
        try {
            long existId = RemoveDescLinkLog.dp.singleLongQuery("select id from "
                    + RemoveDescLinkLog.TABLE_NAME + " limit 1");
            if (existId <= 0L) {
                PolicyDBUtil.loadSqlFile(RemoveDescLinkLog.dp, "remove_desc_link_log.sql");
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    private void ensurePaiPaiItem() {
        try {
            long existId = PaiPaiItem.findExistId(0L, "");
            if (existId < 0L) {
                PolicyDBUtil.loadQuotaSqlFile("paipai_item16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensurePaiPaiTradeDisplay() {
        try {
            long existId = PaiPaiTradeDisplay.findExistId(0L, "");
            if (existId < 0L) {
                PolicyDBUtil.loadQuotaSqlFile("paipai_tradedisplay16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensurePaiPaiTradeItem() {
        try {
            long existId = PaiPaiTradeItem.findExistId(0L, "", "");
            if (existId < 0L) {
                PolicyDBUtil.loadQuotaSqlFile("paipai_tradeitem16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensureFenxiangToken() {
        try {
            long existId = JDBCBuilder.singleLongQuery("select id from fenxiangtoken limit 1;");
            if (existId < 0L) {
                PolicyDBUtil.loadSqlFile("fenxiangtoken.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void ensurePPStock() {
        try {
            long existId = PPStock.findExistId(0L, 0L);
            if (existId < 0L) {
                PolicyDBUtil.loadQuotaSqlFile("ppstock16.sql");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

}
