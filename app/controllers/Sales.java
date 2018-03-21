package controllers;


/**
 * Created by Administrator on 2014/11/27.
 */
public class Sales extends TMController {



    //包邮打折首页
    public static void index(){
        render();
    }

    //创建限时打折
    public static void createDisTime(){
        render();
    }

    public static void addPromotion(Long activityId) {
        render("/Sales/promotionadd.html", activityId);
    }

    public static void modifyPromotion(Long activityId) {
		render("/Sales/promotionmodify.html", activityId);
	}
    
    public static void modifySales(Long activityId) {
        render("/Sales/SalesModify.html", activityId);
    }


    //创建满就送/包邮
    public static void giftAndFreePostage(){
        render();
    }

    //满就送商品选择
    public static void mjsItemSelect(Long activityId) {
        render("/Sales/mjsItemSelect.html", activityId);
    }

    //更新满就送活动规则
    public static void mjsUpdateActivity(Long activityId) {
        render("/Sales/mjsUpdateActivity.html", activityId);
    }
    
    //更新店铺满就送活动规则
    public static void shopMjsUpdateActivity(Long activityId) {
        render("/Sales/shopMjsUpdateActivity.html", activityId);
    }

    public static void mjsItemAdd(Long activityId) {
		render("/Sales/mjsItemAdd.html", activityId);
	}
    
    //创建全店满减/包邮
    public static void shopFreePostage(){
        render();
    }
     //创建全店打折
    public static void shopSales(){
        render();
    }

    //修改全店打折
    public static void modifyShopDiscount(long activityId){
        render("/Sales/modifyShopDiscount.html", activityId);
    }

    //重启打折活动
    public static void restartDiscount(Long activityId) {
        render("/Sales/discountrestart.html", activityId);
    }

    //重启全店打折活动
    public static void restartShopDiscount(Long activityId) {
        render("/Sales/shopdiscountrestart.html", activityId);
    }

    //重启活动选择商品
    public static void restartPromotion(Long activityId) {
        render("/Sales/promotionrestart.html", activityId);
    }

    //重启满就送活动
    public static void mjsRestartActivity(Long activityId) {
        render("/Sales/restartMjsActivity.html", activityId);
    }

    //重启全店满就送
    public static void mjsRestartShopActivity(Long activityId) {
        render("/Sales/restartMjsShopActivity.html", activityId);
    }



}
