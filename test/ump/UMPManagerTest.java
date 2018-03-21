
package ump;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import tbapi.ump.UMPManager;
import bustbapi.TBApi;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.request.MarketingTagsGetRequest;
import com.taobao.api.request.UmpActivityAddRequest;
import com.taobao.api.request.UmpDetailAddRequest;
import com.taobao.api.request.UmpToolAddRequest;
import com.taobao.api.response.MarketingTagsGetResponse;
import com.taobao.api.response.UmpActivityAddResponse;
import com.taobao.api.response.UmpDetailAddResponse;
import com.taobao.api.response.UmpToolAddResponse;
import com.taobao.ump.client.meta.ActionDef;
import com.taobao.ump.client.meta.ResourceDef;
import com.taobao.ump.client.result.ResultSupport;
import com.taobao.ump.core.service.client.MetaDefReadServiceTopClient;
import com.taobao.ump.marketing.MarketingActivity;
import com.taobao.ump.marketing.MarketingActivity.ParticipateRange;
import com.taobao.ump.marketing.MarketingBuilder;
import com.taobao.ump.marketing.MarketingDetail;
import com.taobao.ump.marketing.MarketingDetail.ParticipateType;
import com.taobao.ump.marketing.MarketingTool;
import com.taobao.ump.marketing.MarketingTool.OrderType;
import com.taobao.ump.marketing.MarketingTool.ToolType;
import com.taobao.ump.marketing.MetaData;
import com.taobao.ump.marketing.ParameterValue;

import dao.UserDao;

/**
 * thandbox : 3592015
 * @author zrb
 *
 */
public class UMPManagerTest extends UnitTest {

    Gson gson = new Gson();

    private static final Logger log = LoggerFactory.getLogger(UMPManagerTest.class);

    public static final String TAG = "UMPManagerTest";

    @Test
    public void testProvide() {
        MetaDefReadServiceTopClient metaDefReaderService = UMPManager.get().getProvider();

        log.info("[provider:]" + gson.toJson(metaDefReaderService));

        MarketingBuilder builder = new MarketingBuilder();
        builder.setMetaDefProvider(metaDefReaderService);

        ResultSupport<ActionDef> actionResult = metaDefReaderService
                .getMetaDef("com.taobao.ump.meta.action.calcMultiple");

        log.info("[actionresult:]" + gson.toJson(actionResult));

        MetaData<ActionDef> calcDecrMultiple = builder.bind(actionResult
                .getDefaultModel());

        log.info("[每件:]" + gson.toJson(calcDecrMultiple));

        ActionDef decreaseMoneyDef = (ActionDef) metaDefReaderService
                .getMetaDef("com.taobao.ump.meta.action.decreaseMoney")
                .getDefaultModel();

        MetaData<ActionDef> decreaseMoney = builder.bind(decreaseMoneyDef,

                new ParameterValue[] {
                    builder.newUndefineParameter("decreaseMoney")
                });

        log.info("[打折:]" + gson.toJson(decreaseMoney));
        log.info("[打折def:]" + gson.toJson(decreaseMoneyDef
                ));
        // 通过资源型营销积木块取得订单金额

        log.info("[减钱:]" + gson.toJson(decreaseMoneyDef));

        ResourceDef orderPriceDef = (ResourceDef) metaDefReaderService
                .getMetaDef(
                        "com.taobao.ump.meta.resource.getOrderPriceResource")
                .getDefaultModel();

        log.info("[订单金额度:]" + gson.toJson(orderPriceDef));

        MetaData<ResourceDef> orderPrice = builder.bind(orderPriceDef,
                new ParameterValue[] {
                    builder.newConstParameter(true)
                });

        log.info("[订单金封装成啥了？？？:]" + gson.toJson(orderPrice));

        // 打折 元数据定义

        ActionDef discountDef = (ActionDef) metaDefReaderService.getMetaDef(
                "com.taobao.ump.meta.action.discount").getDefaultModel(); // 打*折

        MetaData<ActionDef> discount = builder.bind(discountDef,
                new ParameterValue[] {
                        builder.newUndefineParameter("discountRate"),
                        builder.newResourceParameter(orderPrice)
                });

        log.info("[折扣:]" + gson.toJson(discountDef));
        log.info("[折扣 meta:]" + gson.toJson(discount));

        decreaseMoney = calcDecrMultiple.and(decreaseMoney);
        decreaseMoney = builder.bindConditional(decreaseMoney, "decrease");

        discount = builder.bindConditional(discount, "discount");
        MetaData<ActionDef> fullOperationMeta = decreaseMoney.and(discount);

        MarketingTool marketingTool = builder.createMarketingTool();

        marketingTool.setName("减钱，打折");
        marketingTool.setToolCode("pySimpleDiscount");
        marketingTool.setDescription("减钱，打折");
        // ToolType.SUB_ORDER：针对子订单级别的工具；ToolType.ORDER：针对主订单级别的工具；ToolType.ACROSS_ORDER：针对跨订单级别的工具
        marketingTool.setType(ToolType.SUB_ORDER);// 子订单级别(商品级别)的优惠
        marketingTool.setOrderType(OrderType.ORADERABLE);
        marketingTool.setOperationMeta(fullOperationMeta);

        log.error("marketingTool:" + marketingTool);

        String json = builder.build(marketingTool);
        UmpToolAddRequest request = new UmpToolAddRequest();

        request.setContent(json);

        try {
            /**
             * "toolId":2862001,"
             */
            UmpToolAddResponse actResponse = TBApi.genClient().execute(request);
            log.info("[工具  打折、减钱]" + gson.toJson(actResponse));

            if (actResponse.isSuccess()) {
                // 上传成功后保存活动id，在添加活动详情时候需要使用该id
                Long activityId = actResponse.getToolId();
                log.info("activityId: " + activityId);
            } else {
                log.info("actResponse.body: " + actResponse.getBody());

            }

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }

    }

    Long toolId = 2862001L;

    User testUser = UserDao.findById(79742176L);

//    User testUser = UserDao.findById(20003478L);

    public void testUserTags() {

        MarketingTagsGetRequest req = new MarketingTagsGetRequest();
        req.setFields("tag_id,tag_name");

        try {
            MarketingTagsGetResponse response = TBApi.genClient().execute(req, testUser.getSessionKey());
            log.warn("result : " + gson.toJson(response));

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void addActivity(Long activityId, User user) {
        SimpleDateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-mm-dd");
        try {
            MarketingTool marketingTool = UMPManager.get().getMarketingTool(toolId);
            MarketingBuilder builder = new MarketingBuilder();
            builder.setMetaDefProvider(UMPManager.get().getProvider());

            MarketingActivity marketingActivity = builder
                    .createMarketingActivity(marketingTool);

            Date startTime = yyyymmddFormat.parse("2013-01-31");
            Date endTime = yyyymmddFormat.parse("2013-02-08");
            marketingActivity.setName("减10元");
            marketingActivity.setDescription("春节专享-减10元");
            marketingActivity.setStartTime(startTime);
            marketingActivity.setEndTime(endTime);

            marketingActivity.setParticipateRange(ParticipateRange.PART);
            marketingActivity.setMultipleLayerPromotion(true);

            /**************** userTag(淘宝标签)的使用 ***********************************************/
//            ResultSupport<TargetDef> targetResult = UMPManager.get().getProvider().getMetaDef("com.taobao.ump.meta.target.userTag");
//            MetaData<TargetDef> userTag = builder.bind(targetResult.getDefaultModel(), new ParameterValue[] {
//                    builder.newConstParameter("top1434025")
//            });
//            //拿到标签范围的积木块 ,并指定具有标签ID为1434025的买家可以享受优惠 。
//            marketingActivity.setTargetMeta(userTag);

            String actContent = builder.build(marketingActivity);
            UmpActivityAddRequest actRequest = new UmpActivityAddRequest();
            actRequest.setContent(actContent);
            actRequest.setToolId(toolId);

            UmpActivityAddResponse actResponse = TBApi.genClient().execute(actRequest, user.getSessionKey());
            log.error("Return resp :" + gson.toJson(actResponse));
            if (actResponse.isSuccess()) {
                Long resActivityId = actResponse.getActId();
                log.error("activityId: " + resActivityId);
            }

        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public void addDetail(Long toolId, Long activityId, User user) throws ApiException {
        MarketingBuilder builder = UMPManager.get().genBuilder();
        MarketingActivity marketingActivity = UMPManager.get().getMarketingActivity(toolId, activityId, user);
        MarketingDetail marketingDetail = builder.createMarketingDetail(marketingActivity);

        marketingDetail.define("decrease", true);
        marketingDetail.define("discount", false);

        // 只减一件
        // marketingDetail.define("calcDecrMultiple", false);
        // 每件都减
        marketingDetail.define("calcDecrMultiple", true);

        //减钱金额10元，单位是分
        marketingDetail.define("decreaseMoney", 1000L);
        // 不生效的打折，也需要赋值 1000L 十折
        marketingDetail.define("discountRate", 1000L);

        // 优惠范围为店铺
        marketingDetail.setRange(ParticipateType.SHOP, 0L);
        // 参加类型为全部参加
        // marketingDetail.setParticipateType(ParticipateType.ALL);

        // detail content...
        String detailContent = builder.build(marketingDetail);
        UmpDetailAddRequest detailReq = new UmpDetailAddRequest();

        detailReq.setContent(detailContent);

        detailReq.setActId(activityId);

        UmpDetailAddResponse detailResp = TBApi.genClient().execute(detailReq, user.getSessionKey());

        log.error("add detailed resp : " + detailReq);
        if (detailResp.isSuccess()) {

            // 上传成功后保存活动详情id，在修改活动详情时候需要使用该id
            Long detailId = detailResp.getDetailId();
            System.out.println("detailId: " + detailId);
        } else {
            System.out.println("detailResp.body: " + detailResp.getBody());

        }
    }
}
