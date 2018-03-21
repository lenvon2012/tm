var TM = TM || {};
((function($,window){
    TM.groupUnput = TM.groupUnput || {};
    var groupUnput = TM.groupUnput || {};
    groupUnput.showPlans = groupUnput.showPlans || {};
    groupUnput.showPlans = $.extend({
        init:function(){
            var status = 2;
            var bottom = $(".tmp_ip_bottom");
            bottom.tmpage({
                currPage: 1,
                pageSize: 5,
                pageCount:1,
                ajax:{
                    on: true,
                    dataType: 'json',
                    url:'/group/showPlans',
                    param:{status:status},
                    callback:function(data){
                        $(".tmp_ip_area").empty();
                        if(data.res.length != 0){
                            for(var n=0;n<data.res.length;n++){
                                $.each(data.res[n],function(key,value){
                                    $(".tmp_ip_area").append('<div class="tmp_ip_div" data="' + value.id + '">' +
                                                                  '<div class="tmp_ip_btn_div" style="position:relative">' +
                                                                      '<div class="group_btn group_ip_btn gp_ip_btn_lo1">☂投放计划</div>' +
                                                                      '<div class="group_btn group_ip_btn gp_ip_btn_lo2">✎修改计划</div>' +
                                                                  '</div>' +
                                                                  '<div class="tmp_ip_op_div">' +
                                                                      '<span class="tmp_ip_op_sp tmp_ip_cl">✘删除计划</span>' +
                                                                      '<span class="tmp_ip_op_sp">模板名称：<strong class="tmp_ip_name">' + value.planName + '</strong></span>' +
                                                                  '</div>' +
                                                                  '<div class="tmp_tp_area_div">' + key +'</div>' +
                                                              '</div>');

                                });
                            }
                        }else{
                            $(".tmp_ip_area").html('<div class="item-status-msg">暂无未投放的计划</div>');
                        }
                        groupUnput.showPlans.event();
                    }
                }
            });
        },

        event:function(){
            $(".gp_ip_btn_lo1").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                window.location.href = "/group/groupAll?id=" + planId + "&toput=true";
            });

            $(".tmp_ip_cl").unbind("click").click(function(){
                var divObj = $(this).parents(".tmp_ip_div");
                var planId = divObj.attr("data");
                TM.Alert.showDialog("确定要删除此计划？",250,150,function(){
                    $.post('/group/deletePlanId',{planId:planId},function(data){
                        if(data){
                            divObj.animate({opacity: "hide"},"slow");
                            TM.Alert.load("删除成功",250,150,function(){return false},"提示");
                        }
                    });
                },function(){return false},"提示");
            });


            $(".gp_ip_btn_lo2").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                if(planId !== undefined && planId != ""){
                    window.location.href = "/group/groupAll?id=" + planId ;
                }else{
                    alert("参数错误 请联系客服 -14");
                }
            });
        }




    },groupUnput.showPlans);

})(jQuery,window));