    TM = TM || {};
((function($,window){
    TM.groupCancel = TM.groupCancel || {};
    var groupCancel = TM.groupCancel || {};
    groupCancel.showPlans = groupCancel.showPlans || {};
    groupCancel.showPlans = $.extend({
        init:function(){
            var status = 3;
            var bottom = $(".tmp_ip_bottom");
            bottom.tmpage({
                currPage: 1,
                pageSize: 5,
                pageCount:1,
                ajax:{
                    on: true,
                    dataType: 'json',
                    url:'/group/showPlansNoHtml',
                    param:{status:status},
                    callback:function(data){
                        if(data.res.length != 0){
                            $(".tmp_ip_area").empty();
                            var planObjs = $("#cancelPlanItemBox").tmpl(data.res);
                            $(".tmp_ip_area").append(planObjs);

                            groupCancel.showPlans.event();
                        }else{
                            $(".tmp_ip_area").html('<div class="item-status-msg">暂无已经结束的计划</div>');
                        }
                    }
                }
            });
        },

        event:function(){
            $(".gp_can_de").unbind("click").click(function(){
                var divObj = $(this).parents(".gp_cal_div");
                var planId = divObj.attr("data-ta");
                TM.Alert.showDialog("确定要删除此计划？",250,150,function(){
                    $.post('/group/deletePlanId',{planId:planId},function(data){
                        if(data){
                            divObj.animate({opacity: "hide"},"slow");
                            TM.Alert.load("删除成功",250,150,function(){return false},"提示");
                        }
                    });
                },function(){return false},"提示");
            });


            $(".gp_can_sh").unbind("click").click(function(){
                if($(this).hasClass("gp_can_hi")){
//                    $(this).parents(".gp_cal_div").find(".template").animate({opacity: "hide"},"slow");
                    $(this).parents(".gp_cal_div").find(".template").css("display","none");
                    $(this).removeClass("gp_can_sh");
                    $(this).removeClass("gp_can_hi");
                    $(this).addClass("gp_can_bl");
                    $(this).html("点击展示");
                }else if($(this).hasClass("gp_can_bl")){
                    $(this).removeClass("gp_can_hi");
//                    $(this).parents(".gp_cal_div").find(".template").animate({opacity: "show"},"slow");
                    $(this).parents(".gp_cal_div").find(".template").css("display","block");
                    $(this).removeClass("gp_can_bl");
                    $(this).addClass("gp_can_hi");
                    $(this).html("点击隐藏");
                }else{
                    var btnObj = $(this);
                    var divObj = $(this).parents(".gp_cal_div");
                    var planId = divObj.attr("data-ta");
                    $.post("/group/showOnePlan",{planId:planId},function(data){
                        divObj.append(data);
                        btnObj.html("点击隐藏");
                        btnObj.removeClass("gp_can_sh");
                        btnObj.addClass("gp_can_hi");
                    });
                }
            });

            $(".gp_can_tm").unbind("click").click(function(){
                var planId = $(this).parents(".gp_cal_div").attr("data-ta");
                if(planId !== undefined && planId != ""){
                    window.location.href = "/group/groupAll?id=" + planId ;
                }else{
                    alert("参数错误 请联系客服 -15");
                }
            });

            $(".gp_can_tp").unbind("click").click(function(){
                var planId = $(this).parents(".gp_cal_div").attr("data-ta");
                if(planId !== undefined && planId != ""){
                    window.location.href = "/group/groupAll?id=" + planId + "&toput=true";
                }else{
                    alert("参数错误 请联系客服 -16");
                }
            });
        }

    },groupCancel.showPlans);


})(jQuery,window));