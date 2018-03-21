var TM = TM || {};
((function($,window){
    TM.groupInput = TM.groupInput || {};
    var groupInput = TM.groupInput || {};
    groupInput.showPlans = groupInput.showPlans || {};
    groupInput.showPlans = $.extend({
        init:function(){
            var status = 1;
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
                                    //status 1 投放 ; 2 未投放 ; 3 已結束 ; 4 刪除 ; 5 正在投放... ; 6 正在刪除...
                                    if(value.status == 1){
                                        $(".tmp_ip_area").append(
                                        '<div class="tmp_ip_div" data="' + value.id + '">' +
                                              '<div class="tmp_ip_btn_div" style="position:relative">' +
                                                  '<div class="group_btn group_ip_btn gp_ip_btn_lo1"><>已投放宝贝</div>' +
                                                  '<div class="group_btn group_ip_btn gp_ip_btn_lo2">+添加投放宝贝</div>' +
                                              '</div>' +
                                              '<div class="tmp_ip_op_div">' +
                                                  '<span class="tmp_ip_op_sp tmp_ip_cl" style="color:#0C0C0C;">✘取消投放</span>' +
                                                  '<span class="tmp_ip_op_sp tmp_ip_md">✎修改模板</span>' +
                                                  '<span class="tmp_ip_op_sp">模板名称：<strong class="tmp_ip_name">' + value.planName + '</strong></span>' +
                                                  '<span class="tmp_ip_op_sp">投放中：<strong class="tmp_ip_num" style="color: #5CACEE;">' + value.success + '</strong>个</span>&nbsp;' +
                                                '<span class="tmp_ip_op_sp"><strong class="tmp_ip_status" style="color: #FF4400;">查看状态</strong></span>' +
                                              '</div>' +
                                              '<div class="tmp_tp_area_div">' + key +'</div>' +
                                        '</div>'
                                        );
                                    }
                                    else if(value.status == 5){
                                        $(".tmp_ip_area").append('<div class="tmp_ip_div" data="' + value.id + '">' +
                                              '<div class="tmp_ip_btn_div">' +
                                                  //'<div class="group_btn group_ip_btn gp_ip_btn_lo1"><>已投放宝贝</div>' +
                                                  //'<div class="group_btn group_ip_btn gp_ip_btn_lo2">+添加投放宝贝</div>' +
                                              '</div>' +
                                              '<div class="tmp_ip_op_div">' +
                                                  '<span class="tmp_ip_op_sp tmp_ip_cl" style="color: #0C0C0C;">✘取消投放</span>' +
                                                  //'<span class="tmp_ip_op_sp tmp_ip_md">✎修改模板</span>' +
                                                  '<span class="tmp_ip_op_sp">模板名称：<strong class="tmp_ip_name">' + value.planName + '</strong></span>' +
                                                  '<span class="tmp_ip_op_sp">投放中：<strong class="tmp_ip_num" style="color: #5CACEE;">' + value.success +'</strong>个</span>&nbsp;' +
                                                /*'失败:<strong style="color:red;">' + value.fail + '</strong>个</span>' +*/
                                                '<span class="tmp_ip_op_sp"><strong class="tmp_ip_status" style="color: #FF4400;">查看状态</strong></span>' +
                                                '<span class="tmp_ip_op_sp">正在投放...等待处理<strong style="color:#ff0000;">' + value.wait + '</strong>个</span>' +
                                              '</div>' +
                                              '<div class="tmp_tp_area_div">' + key +'</div>' +
                                          '</div>');
                                    }
                                    else if(value.status == 6){
                                        $(".tmp_ip_area").append('<div class="tmp_ip_div" data="' + value.id + '">' +
                                          '<div class="tmp_ip_btn_div">' +
                                              //'<div class="group_btn group_ip_btn gp_ip_btn_lo1"><>已投放宝贝</div>' +
                                              //'<div class="group_btn group_ip_btn gp_ip_btn_lo2">+添加投放宝贝</div>' +
                                          '</div>' +
                                          '<div class="tmp_ip_op_div">' +
                                              '<span class="tmp_ip_op_sp tmp_ip_cl" style="color: #0C0C0C;">✘取消投放</span>' +
                                             // '<span class="tmp_ip_op_sp tmp_ip_md">✎修改模板</span>' +
                                              '<span class="tmp_ip_op_sp">模板名称：<strong class="tmp_ip_name">' + value.planName + '</strong></span>' +
                                              '<span class="tmp_ip_op_sp">投放中：<strong class="tmp_ip_num" style="color: #5CACEE;">' + value.success +'</strong>个<span>&nbsp;' +
                                            /*'失败:<strong style="color:red;">' + value.fail + '</strong>个</span>' +*/
                                            '<span class="tmp_ip_op_sp"><strong class="tmp_ip_status" style="color: #FF4400;">查看状态</strong></span>' +
                                            '<span class="tmp_ip_op_sp">正在退出 等待处理<strong style="color:#ff0000;">' + value.success + '</strong>个</span>' +
                                          '</div>' +
                                          '<div class="tmp_tp_area_div">' + key +'</div>' +
                                      '</div>');
                                    }
                                });
                             }
                        }else{
                            $(".tmp_ip_area").html('<div class="item-status-msg">暂无已经投放的计划</div>');
                        }
                        groupInput.showPlans.event(null);
                    }
                }
            });
        },

        event:function(planId){
            $(".tmp_ip_cl").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                TM.Alert.showDialog("确定要删除此计划？",250,150,function(){groupInput.showPlans.stopAllPlan(planId);},function(){return false},'提示');
            });

            $(".gp_ip_btn_lo1").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                groupInput.showPlans.checkItemOut(planId);
            });

            $(".in-info-td-4").unbind("click").click(function(){
                if($(this).parent().hasClass("in-info-fixed")){
                    $(this).parent().removeClass("in-info-fixed");
                    $(this).parent().find("input").attr("checked",false);
                }else{
                    $(this).parent().addClass("in-info-fixed");
                    $(this).parent().find("input").attr("checked",true);
                }
            });

            $(".in-info-checkBtn").unbind("click").click(function(){
                if(confirm("确定要退出投放吗?")){
                    var currBtn = $(this);
                    var numIid = $(this).parents(".input-info").find(".item_id").attr("data");
                    $.post('/group/delOneItemOnePlan',{numIid:numIid,planId:planId},function(data){
                        if(data.ok){
                            alert("退出投放成功");
                            currBtn.parents(".input-info").css("display","none");
                        }else{
                            currBtn.parents(".input-info").find(".in-info-result").html("退出投放失败");
                            currBtn.addClass("delPlanError");
                            currBtn.html("查看原因");
                            groupInput.showPlans.checkErrorResult(data);
                        }
                    });
                }
            });

            $(".gp_ip_btn_lo2").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                if(planId !== undefined && planId != ""){
                    groupInput.toPut.showToPutArea(planId);
                }else{
                    alert("参数错误 请联系客服 -11");
                }
            });

            $(".tmp_ip_status").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                if(planId !== undefined && planId != ""){
                    groupInput.status.checkPlanStatus(planId);
                }else{
                    alert("参数错误 请联系客服 -12");
                }
            });

            $(".tmp_ip_md").unbind("click").click(function(){
                var planId = $(this).parents(".tmp_ip_div").attr("data");
                if(planId !== undefined && planId != ""){
                    window.location.href = "/group/groupAll?id=" + planId + "&input=true";
                }else{
                    alert("参数错误 请联系客服 -13");
                }
            });
        },

        checkErrorResult:function(data){
            $(".input-info .delPlanError").unbind("click").click(function(){
                alert(data.msg);
            });
        },

        stopAllPlan:function(planId){
            $.post('/group/stopOnePlan',{planId:planId},function(data){
                if(data){
                    TM.Alert.load("已在后台帮您退出投放，请稍后",350,300,function(){window.location.href="/group/groupInput"},function(){return false},"提示");
                }
            });
        },

        checkItemOut:function(planId){
            var itemObj = $("<table></table>");
            $.post('/group/showGroupedItems',{planId:planId,status:2},function(data){
                itemObj.append($("#checkItemBox").tmpl(data));
                TM.Alert.load(itemObj,860, 700,function(){return false},function(){return false},'投放成功宝贝,点击图片查看宝贝,点击标题选中');
                groupInput.showPlans.event(planId);
            });
        }

    },groupInput.showPlans);


   groupInput.status = groupInput.status || {};
   groupInput.status = $.extend({

        checkPlanStatus:function(planId){
            $(".tmp_ip_li").css("display","none");
            $(".tmp_ip_st").css("display","block");
            $(".th-lev-msg").html("&gt;&nbsp;&nbsp;&nbsp;&nbsp;状态");
            $.post("/group/getItemsStatus",{planId:planId},function(data){
                $(".ip-success").html(data.success);
                $(".ip-waiting").html(data.wait);
                $(".ip-fail").html(data.fail);
                var succObj = $(".group_st_succ");
                //status ==2 投放中
                groupInput.status.listGroupedItems(planId,2,succObj);
            });
        },

        listGroupedItems:function(planId,status,statusObj){
            var bottom = statusObj.find(".tmp_tp_st_bottom");
            bottom.tmpage({
                currPage: 1,
                pageSize: 5,
                pageCount:1,
                ajax: {
                    param : {planId:planId,status:status},
                    on: true,
                    dataType: 'json',
                    url: "/group/showGroupedItemsPage",
                    callback:function(data){
                        $(".gp_st_st").css("display","none");
                        statusObj.css("display","block");
                        if(data.res.length > 0){
                            statusObj.find(".tmp_tp_st_lab").empty();
                            groupInput.status.initLabelArea(statusObj);
                            groupInput.status.appentItemBoxes(data.res,planId);
                            statusObj.find(".tmp_tp_st_btn").css("display","block");
                        }else{
                            statusObj.find(".tmp_tp_st_lab").empty();
                            if(status == 2){
                                statusObj.find(".tmp_tp_st_lab").append('<div class="item-status-msg">暂无已投放的宝贝</div>');
                            }else if(status == 1){
                                statusObj.find(".tmp_tp_st_lab").append('<div class="item-status-msg">目前没有等待处理的宝贝</div>');
                            }else if(status == 0){
                                 statusObj.find(".tmp_tp_st_lab").append('<div class="item-status-msg">目前没有处理失败的宝贝</div>');
                            }

                            statusObj.find(".tmp_tp_st_btn").css("display","none");
                        }
                        groupInput.status.event(planId);
                    }
                }
            });
        },

        initLabelArea:function(statusObj){
            statusObj.find(".tmp_tp_st_lab").append('<div class="tmp_tp_st_chekbox"><input type="checkbox">&nbsp;全选本页&nbsp;&nbsp;<span class="tmp_tp_attention">' +
                                                    '(注意:点击价格选中,点击标题或图片查看详情)</span></div>');
            statusObj.find(".tmp_tp_st_lab").append('<div class="tmp_tp_st_area"><table class="tmp_tp_st_ta"><thead>' +
                                            '<tr class="tmp_tp_st_tr"><td style="width:500px;">宝贝标题</td><td style="width:140px;">宝贝价格</td>' +
                                            '<td>状态</td><td style="width:50px;"></td></tr></thead><tbody class="tmp_ip_tbody"></tbody></div>');
        },

        appentItemBoxes:function(data,planId){
            var boxObjs = $("#groupedItems").tmpl(data);
            $(".tmp_ip_tbody").append(boxObjs);
//            groupInput.status.event(planId);
        },

        event:function(planId){
            $(".tmp_tp_st_chekbox input").unbind("click").click(function(){
                if($(this).is(":checked")){
                    $(".tp-st-tr-a").find("input").attr("checked",true);
                }else{
                    $(".tp-st-tr-a").find("input").attr("checked",false);
                }
            });

            $(".tp-st-clik").unbind("click").click(function(){
                var input = $(this).parent().find("input");
                if(input.is(":checked")){
                    input.attr("checked",false);
                }else{
                    input.attr("checked",true);
                }
            });

            $(".tp-st-tr").hover(function(){
                $(this).find(".tp_st_oper").css("display","block");
            },function(){
                $(this).find(".tp_st_oper").css("display","none");
            });

            $(".bt_sign_out").unbind("click").click(function(){
                var numIidsArr = [];
                $.each($(".group_st_succ .tp-st-tr input"),function(){
                    if($(this).is(":checked")){
                        var numIid = $(this).parents(".tp-st-tr").find(".item-pic").attr("data");
                        numIidsArr.push(numIid);
                    }
                });

                if(numIidsArr.length == 0){
                    TM.Alert.load("请添加宝贝",250,150,function(){return false;},function(){return false;},"提示");
                }else{
                    var numIidsString = numIidsArr.join("!@#");
                    TM.Alert.showDialog('<a>确定要將以下<span class="successNum">' + numIidsArr.length + '</span>个宝贝退出投放?</a>',350,200,
                                        function(){TM.groupInput.status.toStopPlan(planId,numIidsString)},function(){return false},"提示");
                }
            });


            $(".bt_in_out").unbind("click").click(function(){
                var numIidsArr = [];
                $.each($(".group_st_deal .tp-st-tr input"),function(){
                    if($(this).is(":checked")){
                        var numIid = $(this).parents(".tp-st-tr").find(".item-pic").attr("data");
                        numIidsArr.push(numIid);
                    }
                });

                if(numIidsArr.length == 0){
                    TM.Alert.load("请添加宝贝",250,150,function(){return false;},function(){return false;},"提示");
                }else{
                    var numIidsString = numIidsArr.join("!@#");
                    TM.Alert.showDialog('<a>确定要批量处理以下<span class="successNum">' + numIidsArr.length + '</span>个宝贝?</a>',250,150,
                                        function(){TM.groupInput.status.toDeal(planId,numIidsString)},function(){return false},"提示");
                }
            });


           $(".tp_st_del").unbind("click").click(function(){
                var numIid = $(this).parents(".tp-st-tr").find(".item-pic").attr("data");
                if(numIid !== undefined && numIid != "" && numIid != null){
                    var boxObj = $(this).parents(".tp-st-tr");
                    TM.Alert.showDialog("确定要将此计划从这个宝贝中退出?",350,250,function(){groupInput.status.stopOneItem(numIid,planId,boxObj);},function(){return false},"提示");
                }else{
                    alert("参数错误 请联系客服 -14");
                }
           });

           $(".tp_st_add").unbind("click").click(function(){
                var numIid = $(this).parents(".tp-st-tr").find(".item-pic").attr("data");
                if(numIid !== undefined && numIid != "" && numIid != null){
                    var boxObj = $(this).parents(".tp-st-tr");
                    TM.Alert.showDialog("确定要添加投放吗?",250,150,function(){groupInput.status.addOneItem(numIid,planId,boxObj);},function(){return false},"提示");
                }else{
                    alert("参数错误 请联系客服 -15");
                }
           });

           $(".fail-msg").hover(function(){
                if(!$(this).hasClass("error-Msg")){
                    $(this).html("点击查看原因");
                }
           },function(){
                if(!$(this).hasClass("error-Msg")){
                    $(this).html("投放失败");
                }
           });

           $(".fail-msg").unbind("click").click(function(){
                var msg = $(this);
                var numIid = $(this).parents(".tp-st-tr").find(".item-pic").attr("data");
                $.post('/group/getGroupedItem',{planId:planId,numIid:numIid},function(data){
                    msg.html(data.errorMsg);
                    msg.removeClass("fail-msg");
                    msg.addClass("error-Msg");
                });
           });

           $(".in-status").unbind("click").click(function(){
                $(".tmp_tp_st_tr span").removeClass("triangle-up");
                var statusObj;
                var status;
                if($(this).hasClass("in-st-suc")){
                    $(".tmp_tp_st_tr .st-tr-suc").addClass("triangle-up");
                    statusObj = $(".group_st_succ");
                    status = 2;
                }else if($(this).hasClass("in-st-fai")){
                    $(".tmp_tp_st_tr .st-tr-fai").addClass("triangle-up");
                    statusObj = $(".group_st_deal");
                    status = 0;
                }else if($(this).hasClass("in-st-wai")){
                    $(".tmp_tp_st_tr .st-tr-wai").addClass("triangle-up");
                    statusObj = $(".group_st_wait");
                    status = 1;
                }else{
                    return false;
                }
                 groupInput.status.listGroupedItems(planId,status,statusObj)
           });
        },
        stopOneItem:function(numIid,planId,boxObj){
            $.post('/group/delOneItemOnePlan',{numIid:numIid,planId:planId},function(data){
                if(data.ok){
                    TM.Alert.showDialog("退出投放成功",250,150,function(){return false},function(){return false},"提示");
                    boxObj.css("display","none");
                    boxObj.empty();
                    //同时将数量减1
                    $(".ip-success").html($(".ip-success").text() - 1);
                }else{
                    boxObj.find(".tp-st-tr-b span").html(data.Msg);
                }
            });
        },

        addOneItem:function(numIid,planId,boxObj){
            $.post('/group/addOneItemPlan',{numIid:numIid,planId:planId},function(data){
                //if ok change status else do nothing
                if(data.ok){
                    boxObj.find(".tp-st-tr-b").html("投放成功");
                    TM.Alert.load("投放成功",250,150,function(){return false},function(){return false},"提示");
                }
            });
        },

        toStopPlan:function(planId,numIidsString){
            $.post("/group/stopPlanByNumiids",{planId:planId,numIidsStr:numIidsString},function(data){
                TM.showResult.result.toPutResultDialog(data,"退出投放","/group/groupInput","/group/groupInput");
            });
        },

        toDeal:function(planId,numIidsString){
            $.post("/group/dealByNumIids",{planId:planId,numIidsStr:numIidsString},function(data){
                TM.showResult.result.toPutResultDialog(data,"处理","/group/groupInput","/group/groupInput");
            });
        }

    },groupInput.status);

    groupInput.toPut = groupInput.toPut || {};
    groupInput.toPut = $.extend({
        showToPutArea:function(planId){
            $(".tmp_ip_li").css("display","none");
            $(".tmp_ip_tp").css("display","block");
            $(".th-lev-msg").html("&gt;&nbsp;&nbsp;&nbsp;&nbsp;添加投放");
            groupInput.toPut.initPanel(planId);
       },

       initPanel:function(planId){
           var panel = $(".tmp_ip_tp_top tr");
           panel.empty();
           panel.append('<td><input class="group_search_input_wrap" placeholder="请输入关键字" default="请输入关键字" type="text"></td><td class="group_td_gap"></td>');
           panel.append('<td><select name="selectCat" value="selectCat" class="searchSelect"></select></td><td class="group_td_gap"></td>');
           panel.append('<td><a class="group_sort sale_sort">销量</a></td><td class="group_td_gap"></td>');
           panel.append('<td><a class="group_sort price_sort">价格</a></td><td class="group_td_gap"></td>');
           panel.append('<td class="group_btn tp_search doSearch"><a style="color:#fff;">搜索</a></td>');
           groupInput.toPut.appendSellerCat(planId);
       },

       appendSellerCat:function(planId){
           var sellerCatObj = $(".tmp_ip_tp_top").find(".searchSelect");
           $.get('/Items/sellerCatCount',function(data){
               sellerCatObj.empty();
               if(!data || data.length == 0){
                   sellerCatObj.hide();
               }
               var exist = false;
               var cat = $('<option>店铺类目</option>');
               sellerCatObj.append(cat);
               for(var i=0;i<data.length;i++){
                   if(data[i].count <=0){
                       continue;
                   }
                   exist = true;
                   var option = $('<option></option>');
                   option.attr("catId",data[i].id);
                   option.html(data[i].name);
                   sellerCatObj.append(option);
               }
               if(!exist){
                   sellerCatObj.hide();
               }
               groupInput.toPut.listItems(planId);
           });
       },

       listItems:function(planId){
           var panelObj = $(".tmp_ip_tp_top");
           var params = TM.group.params.getSearchParams(panelObj);
           var listBottomObj = $(".tmp_ip_tp_bottom");
           listBottomObj.tmpage({
               currPage: 1,
               pageSize: 5,
               pageCount:1,
               ajax: {
                   param : params,
                   on: true,
                   dataType: 'json',
                   url: "/group/listItems",
                   callback:function(data){
                       var areaObj = $(".tmp_ip_tp_area");
                       if(data.res.length > 0){
                           groupInput.toPut.appendItemBoxes(areaObj,data.res,planId);
                       }else{
                           areaObj.empty();
                           areaObj.append('<tr><td style="text-align: center;width:100%;">抱歉，没有找到相关的宝贝<td><tr>');
                       }
                   }
               }
           });
       },
       appendItemBoxes:function(areaObj,data,planId){
           areaObj.empty();
           var toPutItemBox = $("#toPutItemBox").tmpl(data);
           $.each(toPutItemBox,function(i,value){
               if(i % 4 == 0){
                   areaObj.append('<tr class="tp-item-tr"></tr>');
               }
               areaObj.find("tr[class='tp-item-tr']:last").append($(this));

               var numIid = $(this).find(".tp_item_img").attr("data");
               if(groupInput.result.isInSelectArray(numIid) == true){
                   $(this).find(".put_item_div").addClass("item-box-fix");
                   $(this).find(".put_item_div").css("border","1px solid #ff7744");
                   $(this).find("input").attr("checked",true);
               }
           });
           groupInput.toPut.disableItem(planId);
           groupInput.toPut.appendToPutCheckedBtn();
       },

       disableItem:function(planId){
            var putItemBoxes = $(".tmp_ip_tp_area .put_item_div");
            $.post('/group/showGroupedItems',{planId:planId,status:2},function(data){
                $.each(data,function(i,value){
                    if(value != null){
                        $.each(putItemBoxes,function(){
                            if($(this).find(".tp_item_img").attr('data') == value.numIid  ){
                                $(this).addClass("tm_ip_tp_dis");
                                $(this).find("input").prop('disabled',true);
                                $(this).find("input").prop('checked',true);
                            }
                        });
                    }
                });
                groupInput.toPut.event(planId);
            });
       },

       appendToPutCheckedBtn:function(){
           $(".tp_checked").remove();
           $(".tmp_ip_tp").append('<div class="group_btn tp_checked"><span>投放选中宝贝</span></div>');
       },

       event:function(planId){
            $(".tmp_ip_tp_top .doSearch").unbind("click").click(function(){
                groupInput.toPut.listItems(planId);
            });

            $(".tmp_ip_tp_top .searchSelect").unbind("change").change(function(){
                groupInput.toPut.listItems(planId);
            });

            $(".put_item_div").hover(function(){
                if(!$(this).hasClass("item-box-fix") && !$(this).hasClass("tm_ip_tp_dis")){
                    $(this).addClass("item-box-in");
                    $(this).css("border","1px solid #ff7744");
                }
            },function(){
                if(!$(this).hasClass("item-box-fix")){
                    $(this).removeClass("item-box-in");
                    $(this).css("border","1px solid #fff");
                }
            });


           $(".put_item_div").unbind("click").click(function(){
               var numIid = $(this).find(".tp_item_img").attr("data");
               if(!$(this).hasClass("tm_ip_tp_dis")){
                    if($(this).hasClass("item-box-fix")){
                       $(this).removeClass("item-box-fix");
                       $(this).css("border","1px solid #fff");
                       $(this).find("input").attr("checked",false);
                       groupInput.result.removeSelectNumIid(numIid);
                    }else{
                       $(this).addClass("item-box-fix");
                       $(this).css("border","1px solid #ff7744");
                       $(this).find("input").attr("checked",true);
                       groupInput.result.addSelectNumIid(numIid);
                   }
               }
           });


           $(".tp_checked").unbind("click").click(function(){
                var arr = groupInput.result.getSelectNumIidArray();
                if(arr.length == 0){
                    TM.Alert.load("请先选择宝贝",250,150,function(){return false},function(){return false},"提示");
                    return false;
                }
                if(arr.length > 10){
                    TM.Alert.showDialog('<a>确定要投放以下<span class="successNum">' + arr.length + '</span>个宝贝?</a>',250,150,function(){TM.groupInput.toPut.putChecked(planId);},function(){return false},"提示");
                }else{
                    TM.Alert.showDialog('<a>确定要投放以下<span class="successNum">' + arr.length + '</span>个宝贝?</a>',250,150,function(){TM.groupInput.toPut.putCheckedNoQueue(planId);},function(){return false},"提示");
                }
           });

           $(".sale_sort").unbind("click").click(function(){
               $(this).html("");
               if($(this).hasClass("sales_sort_up")){
                   $(this).removeClass("sales_sort_up");
                   $(this).addClass("sales_sort_down");
               }
               else if($(this).hasClass('sales_sort_down')){
                   $(this).removeClass("sales_sort_down");
                   $(this).addClass("sales_sort_up");
               }else{
                   $(this).addClass("sales_sort_down");
               }
               $(".price_sort").html("价格");
               $(".price_sort").removeClass("price_sort_down");
               $(".price_sort").removeClass("price_sort_up");
               groupInput.toPut.listItems(planId);
           });


           $(".price_sort").unbind("click").click(function(){
               $(this).html("");
               if($(this).hasClass("price_sort_up")){
                   $(this).removeClass("price_sort_up");
                   $(this).addClass("price_sort_down");
               }
               else if($(this).hasClass("price_sort_down")){
                   $(this).removeClass("price_sort_down");
                   $(this).addClass("price_sort_up");
               }else{
                   $(this).addClass("price_sort_down");
               }
               $(".sale_sort").html("销量");
               $(".sale_sort").removeClass("sales_sort_down");
               $(".sale_sort").removeClass("sales_sort_up");
               groupInput.toPut.listItems(planId);
           });
       },


       putChecked:function(planId){
             if(planId !== undefined || planId != ""){
                 var numIidsString = groupInput.result.getSelectNumIidArray().join("!@#");
                 $.post("/group/putchecked",{planId:planId,numIidsString:numIidsString},function(data){
                     if(data){
                         TM.group.showDialog("已在后台幫您投放，请稍后",350,300,function(){window.location.href="/group/groupInput"},
                                     function(){window.location.href="/group/groupAll"},"提示");
                     }
                 });              }
             else{
                 alert("参数错误 请联系客服 -10");
             }
         },

         putCheckedNoQueue:function(planId){
             if(planId !== undefined || planId != ""){
                 var numIidsString = groupInput.result.getSelectNumIidArray().join("!@#");
                 $.post("/group/putCheckedNoQueue",{planId:planId,numIidsString:numIidsString},function(data){
                     if(data){
                         TM.showResult.result.toPutResultDialog(data,'投放','/group/groupInput',null);
                     }
                 });
             }
             else{
                 alert("参数错误 请联系客服 -11");
             }
         }

    },groupInput.toPut);


    groupInput.result = groupInput.result || {};
    groupInput.result = $.extend({

        selectNumIidArray: [],

        getSelectNumIidArray: function() {
            return groupInput.result.selectNumIidArray;
        },

        isInSelectArray: function(numIid) {
            for (var i = 0; i < groupInput.result.selectNumIidArray.length; i++) {

                if (groupInput.result.selectNumIidArray[i] == numIid) {
                    return true;
                }
            }
            return false;
        },

        addSelectNumIid: function(numIid) {
            groupInput.result.removeSelectNumIid(numIid);
            groupInput.result.selectNumIidArray[groupInput.result.selectNumIidArray.length] = numIid;
        },

        removeSelectNumIid: function(numIid) {

            for (var i = 0; i < groupInput.result.selectNumIidArray.length; i++) {

                if (groupInput.result.selectNumIidArray[i] == numIid) {
                    groupInput.result.selectNumIidArray.splice(i, 1);
                    return;
                }

            }
        },

        removeSomeSelectNumIids: function(numIidArray) {

            for (var i = 0; i < numIidArray.length; i++) {
                groupInput.result.removeSelectNumIid(numIidArray[i]);
            }
        }
    },groupInput.result);



    TM.groupInput.showDialog=function(html, width, height, okCallback, cancelCallback, title){
        var alertDiv = TM.Alert.getDom();
        $("body").mask();
        TM.Loading.beforeShow && TM.Loading.beforeShow();
        alertDiv.html(html);
        alertDiv.dialog({
            modal: true,
            bgiframe: true,
            height: height || 300,
            width: width || 400,
            title : title || '提示',
            autoOpen: false,
            resizable: false,
            zIndex: 6003,
            buttons:{
                '批量退出投放':function(){
                    okCallback && okCallback();
                    $(this).dialog('close');
                    $("body").unmask();
                },
                '取消':function(){
                    cancelCallback && cancelCallback();
                    $(this).dialog('close');
                    $("body").unmask();
                }
            },beforeClose:function(){
                TM.Loading.afterShow && TM.Loading.afterShow();
            }
        })
        alertDiv.dialog('open');
    }

})(jQuery,window));


