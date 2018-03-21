package controllers;

import com.ciaosir.client.pojo.PageOffset;
import models.phoneDetailed.PhoneDetailed;
import models.user.User;
import play.Play;
import result.TMResult;
import utils.DateUtil;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2015/1/26.
 */
public class PhoneDetaileds extends TMController {

    /**
     * 参数配置页面
     */
//    public static void options() {
//        render();
//    }

    /**
     * 生成n 条测试数据
     */
//    public static void testData() {
//        //当前登陆用户
//        Long uid= getUser().getId();
//        if(uid==0){
//            renderJSON(new TMResult(false,"你还没有登陆哦亲，无法生成手机详情页",null));
//        }
//
//        String sellerId = "初秋的星星520",
//                commodityIds = "43751343396,43827512453,43811169153,40003754821,39980122479";
//        createTemplates(sellerId, commodityIds);
//    }

    /**
     * 生成n 条测试数据
     */
    public static void createData(String commodityIds,boolean isFilter) {

        //当前登陆用户
        Long uid= getUser().getId();
        if(uid==0){
            //统计生成结果的beans
            renderJSON(new CreateRes(0, 0, 0, System.currentTimeMillis(), "","亲，你还没有登录哦",false));
        }
        if(commodityIds==null||commodityIds.trim().length()==0){
            renderJSON(new CreateRes(0, 0, 0, System.currentTimeMillis(), "","商品ID不合法，生成手机详情页失败",false));
        }
        createTemplates(uid, commodityIds,isFilter);
    }

    /**
     * ID
     * 用户ID
     * 商品ID
     * 手机详情页字段
     * 当前模板状态:待处理  已经处理  已经过期
     * 备注：
     * 创建时间：
     * 最后更新时间
     */


    /**
     * 选择商品页面
     * 这里要有一个登陆机制,先判断有没有登陆
     */
    public static void comList() {
          render();
    }
    public static void phonesIndex() {
          render();
    }
    /**
     * 添加待处理的商品列表
     */
    public static void createTemplates(Long sellerId,String commodityIds,boolean isFilter) {
        //统计生成结果的beans
        CreateRes createRes = new CreateRes(0, 0, 0, System.currentTimeMillis(), "","",true);
        String[] ids = commodityIds.split(",");

        //无论保存成功还是失败，都加入count中
        createRes.setCount(createRes.getCount() + ids.length);
        for (String id : ids) {
            //如果是一个不合法的商品ID  就直接跳过
            if (!isNumIid(id)) {
                createRes.setFailureNum(createRes.getFailureNum() + 1);
                createRes.setFailureIds(createRes.getFailureIds() + id + ",");
                continue;
            }
            //过滤掉3天内生成过的商品
            Long l = Long.parseLong(id);
            if(isFilter && PhoneDetailed.countById(l,"Success")>0){
                createRes.setFailureNum(createRes.getFailureNum() + 1);
                createRes.setFailureIds(createRes.getFailureIds() + id + ",");
                continue;
            }

            Long longId = new Long(id);
            PhoneDetailed phoneDetailed = new PhoneDetailed(PhoneDetailed.phoneDetailedStatus.New.toString(), sellerId, longId, null);
            boolean isSuccess= phoneDetailed.jdbcSave();
            //这里判断这个商品保存数据库是否成功
            if(isSuccess){
                createRes.setSuccessNum(createRes.getSuccessNum() + 1);
            }else{
                createRes.setFailureNum(createRes.getFailureNum() + 1);
                createRes.setFailureIds(createRes.getFailureIds() + id + ",");
            }

        }

        renderJSON(createRes);
    }

    /**
     * 用递归调用的形式保存数据
     */
//    public static void saveIds(List<Long> ids,Long sellerId) {
//        Long id = ids.get(0);
//        ids.remove(0);
//
//        PhoneDetailed phoneDetailed = new PhoneDetailed(PhoneDetailed.phoneDetailedStatus.New.toString(), sellerId, id, null);
//        phoneDetailed.jdbcSave();
//
//        if (ids.size() <= 0) {
//            return;
//        } else {
//            saveIds(ids,sellerId);
//        }
//    }

    /**
     * 生成全店商品的手机详情页
     * 这里采取的策略是page + 递归调用的形式逐批生成、
     * 这样有利于计算机资源的优化
     * */
    public static void getItems(int pn, int ps) {
        TMResult tmResult = UmpPromotion.getItems(68L, "", null, null, 2, "df", "all", pn, ps);
        renderJSON(tmResult);

        //Long userid = getUser().getId();
//        List<UmpPromotion.ItemPromotionBean> res = (List<UmpPromotion.ItemPromotionBean>) tmResult.getRes();
        //递归调用
//        if (res.size() <= 0) {
//            renderSuccess("完成",null);
//        } else {
//            StringBuffer sb = new StringBuffer();
//            for (UmpPromotion.ItemPromotionBean r : res) {
//                Long numIid = r.getNumIid();
//                sb.append(numIid+",");
//            }
//            //处理字符串并且保存
//            String ids = sb.substring(0, sb.length() - 2);
//            createTemplates(userid, ids);
//            createAllPhones(++pn, ps);
//        }
    }

    /**
     * 一键生成全店商品的手机详情页
     * */
    public static void createAllPhones(boolean isFilter) {

        //获取该用户的所有商品
        StringBuffer sb = new StringBuffer();

        for(int i=0;;i++){
            TMResult tmResult = UmpPromotion.getItems(68L, "", null, null, 2, "df", "all", i, 10000);
            List<UmpPromotion.ItemPromotionBean> res = (List<UmpPromotion.ItemPromotionBean>) tmResult.getRes();

            for (UmpPromotion.ItemPromotionBean r : res) {
                Long numIid = r.getNumIid();
                sb.append(numIid+",");
            }
            //取出当前用户待处理的商品列表，如果没有 10000个，说明数据库中没有待处理的商品了，直接结束循环
            if(res.size()<10000){
                break;
            }

        }

        String ids = sb.substring(0, sb.length() - 2);

        Long uid= getUser().getId();
        createTemplates(uid,ids,isFilter);

    }
    /**
     * 判断是不是一个合法的商品ID
     * 要求是一个长度大于4的数字
     */
    public static boolean isNumIid(String iId) {
           return  TMCheck.isNumeric(iId)&&iId.length()>4;
    }

    /**
     * 根据当前登陆的用户ID ，去数据库中获取待处理的商品列表的 ID 集合,类似 page方式获取数据，这里建议 一次不超过 10个
     */
    public static void getDeliveryTemplates(String sellerId, int pn, int ps) {
        PageOffset po = new PageOffset(pn, ps);
        User user = TMController.getUser();
        Long id = user.getId();
        if(!sellerId.equals(id.toString())) {
            TMResult tmResult = new TMResult(false, "当前淘宝用户和系统用户不一致！请重新登陆！", null);
            renderJSON(tmResult);
        }

        Set<Long> commonityIds = PhoneDetailed.findCommonityIdByPage(sellerId, po);
        TMResult tmResult = new TMResult(true, po.getPn(), po.getPs(), commonityIds.size(), "", commonityIds);
        renderJSON(tmResult);

    }

    /**
     * 一键全店生成手机详情页
     * */
    public static void oneKeyCreate() {
           //获取全店待处理的商品Id Strings
    }
    /**
     * 更新一条数据的状态
     */
    public static void chengeStatus(Long sellerId,Long itemId,String status) {
        PhoneDetailed phoneDetailed = null;
        List<PhoneDetailed> phoneDetaileds = PhoneDetailed.findById(sellerId.toString(), itemId);
        if(!phoneDetaileds.isEmpty()) {
            phoneDetailed = phoneDetaileds.get(0);
        } else {
            renderError("更新数据失败, 请重试 !");
        }

        phoneDetailed.setStatus(status);
        boolean isSuccess= phoneDetailed.jdbcSave();

        TMResult tmResult;

         if (isSuccess) {
            tmResult = new TMResult(true, "更新状态成功   "+phoneDetailed.getStatus() +"  id  "+itemId, phoneDetailed);
        } else {
            tmResult = new TMResult(false, "更新状态失败   "+phoneDetailed.getStatus() +"  id  "+itemId, phoneDetailed);
        }

        renderJSON(tmResult);
    }

    /**
     * 获取一个模板数据
     * */
    public static void getItemTemplate(String sellerId, Long commodityId) {
        List<PhoneDetailed> phoneDetaileds = PhoneDetailed.findById(sellerId, commodityId);
        TMResult tmResult;

        if (phoneDetaileds.size() > 0) {
            tmResult = new TMResult(true, "获取成功", phoneDetaileds);
        } else {
            tmResult = new TMResult(false, "没有符合要求的数据哦亲", phoneDetaileds);
        }

        renderJSON(tmResult);
    }

    /**
     * 下载谷歌插件
     */
    public static void pluginDownload() {
        String redirURL = APIConfig.get().getRedirURL();
        String[] urlStr = redirURL.split("\\.");
        String folder = urlStr[1];
        String path = Play.applicationPath.getPath();

        String fileName = path + "/conf/phoneDetailplugin/" + folder + "/phoneDetailed.zip";

        File f = new File(fileName);
        renderBinary(f);

    }

    /**
     * 取消安装
     * @param sellerId
     */
    public static void cancel(Long sellerId) {
        boolean success = PhoneDetailed.cancel(sellerId);
        if(success) {
            renderJSON(new TMResult(true, "取消成功!", null));
        } else {
            renderJSON(new TMResult(true, "部分取消失败,请刷新后重试!", null));
        }
    }

    /**
     * 获取3天前的时间
     * 返回 long
     */
    public static Long get3Day() {
        //当前时间减去3天
        return System.currentTimeMillis() - (DateUtil.DAY_MILLIS * 3);
    }

    //一个内部 bean用来封装生成结果
    public static class CreateRes {

        private boolean isOk;
        private int successNum;
        private int failureNum;
        private int count;
        private Long nowTime;
        private String failureIds;
        private String mes;

        public CreateRes() {

        }
        public CreateRes(int successNum, int failureNum, int count, Long nowTime, String failureIds,String mes,boolean isOk) {
            this.successNum = successNum;
            this.failureNum = failureNum;
            this.count = count;
            this.nowTime = nowTime;
            this.failureIds = failureIds;
            this.mes = mes;
            this.isOk = isOk;
        }

        public int getSuccessNum() {
            return successNum;
        }

        public void setSuccessNum(int successNum) {
            this.successNum = successNum;
        }

        public int getFailureNum() {
            return failureNum;
        }

        public void setFailureNum(int failureNum) {
            this.failureNum = failureNum;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Long getNowTime() {
            return nowTime;
        }

        public void setNowTime(Long nowTime) {
            this.nowTime = nowTime;
        }

        public String getFailureIds() {
            return failureIds;
        }

        public void setFailureIds(String failureIds) {
            this.failureIds = failureIds;
        }

        public String getMes() {
            return mes;
        }

        public void setMes(String mes) {
            this.mes = mes;
        }

        public boolean isOk() {
            return isOk;
        }

        public void setOk(boolean isOk) {
            this.isOk = isOk;
        }
    }
}
