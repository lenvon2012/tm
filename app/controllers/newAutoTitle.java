package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class newAutoTitle extends TMController{
	public static final Logger log = LoggerFactory.getLogger(newAutoTitle.class);

    public static final String TAG = "newAutoTitle";
    
    //自动标题首页
    public static void index() {
    	render("newAutoTitle/newAutoTitle.html");
    }
    
    public static void indexNew() {
        render("newAutoTitle/newAutoTitleNew.html");
    }

    // 标题还原中心首页
    public static void goTitleRestore(){
    	//titleRestoreCenter
    	//titleReduce
        render("/newAutoTitle/titleReduce.html");
    }
    //单个标题还原页面
    public static void goOneTitle(){

        render("/newAutoTitle/oneTitleRestore.html");
    }

    //全店标题还原页面
    public static void goAllTitle(){
        render("/newAutoTitle/allTitleRestore.html");
    }

    //按时间还原标题页面
    public static void goTimeLineTitle(){
        render("/newAutoTitle/timeLineRestore.html");
    }

    // 批量标题
    public static void batchTitle() {
    	 render("/newAutoTitle/batchTitle.html");
    }
    
    // 手机详情页
    public static void mobileDesc() {
    	 render("/newAutoTitle/mobileDesc.html");
    }
    
    // 淘宝天猫宝贝复制
    public static void itemCarrier() {
    	 render("/newAutoTitle/itemCarrier.html");
    }
    
    
    	
    
    public static void checkBuss() {
        render("/topBus/bussearch.html");
    }

    public static void titletask() {
        render("/newAutoTitle/titletask.html");
    }
    
    public static void windowAdmin() {
        render("/newAutoTitle/windowadmin.html");
    }
    
    public static void commentAdmin() {
        render("/newAutoTitle/commentadmin.html");
    }


    public  static void autoAdded() {
        render();
    }

    
    public static void distributeDelist(Long planId, boolean isEdit) {
    	render("/newAutoTitle/distributeDelist.html", planId, isEdit);
    }

    public static void delistCreate(){
        render("/newAutoTitle/delistCreate.html");
    }

    public static void delistExclude(Long planId, boolean isEdit){
        render("/newAutoTitle/excludeItem.html", planId,isEdit);
    }

    public static void delistPlans(){
        render("/newAutoTitle/delistPlans.html");
    }

    public static void editPlan(Long planId){
        render("/newAutoTitle/neweditplan.html", planId);
    }

}
