var Relation = Relation || {};

Relation.step1 = Relation.step1 || {};

Relation.Init = Relation.Init || {};
Relation.Init = $.extend({
    initMainNav : function(){
        var mainNav = $('<div class="mainNav"></div>');
        mainNav.append($('<h1 class="logo-glyx"></h1>'));
        var ulNav = $('<ul class="nav"></ul>');
        ulNav.append($('<li class="on"><a href="#"><i class="nav1i"></i>关联推荐</a></li>'));
        ulNav.append($('<li class=""><a href="#"><i class="nav1i"></i>关联分析</a></li>'));
        mainNav.append(ulNav);
        $("#pInt").append(mainNav);
    }
},Relation.Init);

Relation.step1 = $.extend({
    doInit : function(){
        $("#pInt").empty();
        Relation.Init.initMainNav();
        $.get('/Relation/initStep1',function(data){
            Relation.step1.createExistedPlans(data);
            Relation.Event.setAddPlanEvent();
            Relation.Event.setAddItemsEvent();
            Relation.Event.setRemoveItemsEvent();
            Relation.Event.setDeletePlanEvent();
        });
    },
    createExistedPlans : function(data){
        $('.existedPlan').remove();
        var existedPlans = $('<div class="existedPlan"></div>');
        var planGroup = $('<ul class="planGroup"></ul>');
        for(var i=0;i<data.length;i++){
            planGroup.append(Relation.step1.createOnePlan(data[i]));
        }
        planGroup.append(Relation.step1.createAddPlans());
        existedPlans.append(planGroup);
        $("#pInt").append(existedPlans);
    },
    createOnePlan : function(plan){
        var liObj = $("<li class='realPlan' planId='"+plan.id+"'></li>");
        liObj.attr("planName",plan.planName);
        liObj.attr("modelId",plan.modelId);
        liObj.attr("installedNumIids",plan.numIdList);
        liObj.append(Relation.step1.createPlanNameSpan(plan));
        liObj.append(Relation.step1.createPlanImg(plan));
        liObj.append(Relation.step1.createPlanGroupPosition(plan));
        return liObj;
    },
    createPlanNameSpan : function(plan){
        var spanObj = $("<div></div>");
        var aObj = $("<a class='planName' href='#'></a>");
        aObj.html(plan.planName);
        spanObj.append(aObj);
        var deletePlanBtn = $('<span class="deletePlanSpan">删除方案</span>');
        spanObj.append(deletePlanBtn);
        return spanObj;
    },
    createPlanImg : function(plan){
        var imgObj = $("<img style='height: 120px;width: 288px;cursor: pointer;' />")
        imgObj.attr('src',"/public/images/relation/model1.jpg");
        imgObj.click(function(){
            $.cookie("planName",plan.planName);
            $.cookie("planId",plan.id);
            $.cookie("modelId",plan.modelId);
            Relation.step2.doInit();
        });
        return imgObj;
    },
    createPlanGroupPosition : function(plan){
        var groupPosition = $('<div class="groupPosition"></div>');
        groupPosition.append(Relation.step1.createPlanInstallTo());
        groupPosition.append(Relation.step1.createPlanInstalledItems(plan));
       // groupPosition.append(Relation.step1.createPlanInstallBtn());
        return groupPosition;
    },
    createPlanInstallTo : function(){
        return $('<span class="installTo">安装到:</span>');
    },
    createPlanInstalledItems : function(plan){
        var installedItems = $('<div class="installedItems"></div>');
        var installedPic = $('<div style="float: left;"></div>')
        if(plan.numIdList.length>7){
            var pageNum = Math.ceil(plan.numIdList.length/7);
            var currentpage = 1;
            Relation.step1.createInstalledItems(plan,currentpage,installedPic);
        /*    for(var i=0;i<7;i++){
                var aObj = $('<a target="_blank"></a>');
                var url = "http://item.taobao.com/item.htm?id=" + plan.numIdList[i];
                aObj.attr("href",url);
                var imgObj = $('<img >');
                imgObj.attr("src",plan.picURLs[i]);
                aObj.append(imgObj);
                installedItems.append(aObj);
            }    */
            installedPic.appendTo(installedItems);
            var removeItem = $('<span class="removeItems"></span>');

            installedItems.append(removeItem);
            var addItem = $('<span class="addItems"></span>');

            installedItems.append(addItem);

            var scrollerRight = $('<span class="scroller-right"></span>');
            scrollerRight.click(function(){
                if(currentpage<pageNum){
                    currentpage += 1;
                    Relation.step1.createInstalledItems(plan,currentpage,installedPic);
                }
            });
            installedItems.append(scrollerRight);
            var scrollerLeft = $('<span class="scroller-left"></span>');
            scrollerLeft.click(function(){
                if(currentpage>1){
                    currentpage -= 1;
                    Relation.step1.createInstalledItems(plan,currentpage,installedPic);
                }
            });
            installedItems.append(scrollerLeft);

        } else {
            for(var i=0;i<plan.numIdList.length;i++){
                var aObj = $('<a target="_blank"></a>');
                var url = "http://item.taobao.com/item.htm?id=" + plan.numIdList[i];
                aObj.attr("href",url);
                var imgObj = $('<img >');
                imgObj.attr("src",plan.picURLs[i]);
                aObj.append(imgObj);
                installedItems.append(aObj);
                installedPic.appendTo(installedItems);
            }
            var removeItem = $('<span class="removeItems"></span>');

            installedItems.append(removeItem);
            var addItem = $('<span class="addItems"></span>');

            installedItems.append(addItem);
        }


        return installedItems;
    },
    createInstalledItems : function(plan,currentpage,installedPic){
        installedPic.empty();
        for(var i=(currentpage-1)*7;i<((currentpage*7<plan.numIdList.length)?currentpage*7:plan.numIdList.length);i++){
            var aObj = $('<a target="_blank"></a>');
            var url = "http://item.taobao.com/item.htm?id=" + plan.numIdList[i];
            aObj.attr("href",url);
            var imgObj = $('<img >');
            imgObj.attr("src",plan.picURLs[i]);
            aObj.append(imgObj);
            installedPic.append(aObj);
        }
    },
    createPlanInstallBtn : function(){
        var install = $('<span class="deletePlan install"></span>');
        return install;
    },
    createAddPlans : function(){
        var addPlanLiObj = $("<li class='addPlan'></li>");
        addPlanLiObj.append($('<a href="#" style="color: white;font-weight: bold;text-align: center;font-size: 17px;">添加方案</a>'));
       // addPlanLiObj.append($('<img style="border: none;height: 120px;">'));
        return addPlanLiObj;
    }
},Relation.step1);

Relation.step2 = Relation.step2 || {};
Relation.step2 = $.extend({
    doInit : function(){
        $("#pInt").empty();
        Relation.Init.initMainNav();
        $.get('/Relation/initStep2',function(data){
            if(data.length>0)  {
                Relation.step2.createExistedModels(data);
                Relation.step2.genQtip();
                Relation.Event.setChooseEvent();
            }
            else{
                TM.Alert.load("加载模板出错，请刷新~");
            }
        });
    },
    createPlanName:function(){
        var planName = $('<div class="planNameEdit"></div>');
        planName.append($('<span style="font-weight: bolder;color: #2DA5E9;">方案名</span>'));
        var inputObj = $('<input id="planNameInput">');
        if($.cookie("planName")!=null){
            inputObj.val($.cookie("planName"));
        }
        else {
            inputObj.val("新建方案");
        }
        planName.append(inputObj);
        return planName;
    },
    createExistedModels : function(data){
        var selectPlan = $('<div class="selectPlan"></div>');
        var newPlan = $('<div class="newPlan"></div>');
        newPlan.append(Relation.step2.createPlanName());
        newPlan.append(Relation.step2.createNPhead());
        newPlan.append(Relation.step2.createNPBody(data));
        newPlan.append(Relation.step2.createNPFootInstall());
        newPlan.append(Relation.step2.createNPFootUpdate());
        newPlan.append(Relation.step2.createNPFootBack());
        selectPlan.append(newPlan);
        $("#pInt").append(selectPlan);
    },
    createNPhead : function(){
        return $('<div class="npHead" style="text-align: right;padding: 10px 0;zoom: 1;"></div>');
    },
    createNPBody : function(data){
        var npBody = $('<div class="npBody"></div>');
        var recommendBox= $('<div class="recommendBox"></div>');
        recommendBox.append($('<h3 style="text-align:left;">选择模板</h3>'));
        recommendBox.append(Relation.step2.createModelUL(data));
        npBody.append(recommendBox);
        return npBody;
    },
    createNPFootInstall : function(){
        var mainBtn = $('<div class="npFoot"><a href="#" class="mainBtn">安装宝贝</a></div>');
        mainBtn.find(".mainBtn").click(function(){
            var planId = $.cookie("planId");
            var newName = $('#planNameInput').val();
            var modelId = $('.choosed').parent().attr('modelId');
            multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/Relation/getUnRelatedItems","actionURL":"/Relation/addRelationWithModel","pn":1,"px":8,"enableSearch":true,relation:{"isRelation":true,"relationPlanId":planId,"newName":newName,"modelId":modelId}});
            //$.cookie("planName",newName);
        });
        return mainBtn;
    },
    createNPFootUpdate: function(){
        var updateBtn = $('<div class="npFoot"><a href="#" class="updateBtn">保存修改</a></div>');
        updateBtn.find(".updateBtn").click(function(){
            var planId = $.cookie("planId");
            var newName = $('#planNameInput').val();
            var modelId = $('.choosed').parent().attr('modelId');
            $.post('/Relation/addRelationWithNoNumIids',{"planId":planId,"newPlanName":newName,"modelId":modelId},function(data){
                if(data.res)  {
                    TM.Alert.load("保存方案成功~");
                    //$.cookie("planName",newName);
                }
                else TM.Alert.load("保存方案失败~");
            });
        });
        return updateBtn;
    },
    createNPFootBack:function(){
        var back = $('<div class="npFoot"><a href="#" class="backBtn" style="margin-left: 460px;">上一步</a></div>');
        back.find(".backBtn").click(function(){
            $('.qtip-wrapper').remove();
            Relation.step1.doInit();
        });
        return back;
    },
    createModelUL : function(data){
        var ulObj = $('<ul style="margin-left: -30px;margin-top: 0px;zoom: 1;list-style: none;position: relative;clear: both;"></ul>');
        for(var i=0;i<data.length;i++){
            ulObj.append(Relation.step2.createModelLi(data[i],i));
        }
        return ulObj;
    },
    createModelLi : function(model,i){
        var liObj = $('<li class="module"></li>');
        var oneModelDiv = $('<div class="oneModel"></div>');
        oneModelDiv.css("background","url("+model.picPath+")");
        liObj.append(oneModelDiv);
        liObj.attr("modelId",model.modelId);
        if(model.isBig){
            liObj.append($('<p style="text-align: center;font-size: 12px;">大图-列表</p>'));
        } else {
            liObj.append($('<p style="text-align: center;font-size: 12px;">小图-列表</p>'));
        }
        var description = $('<p style="text-align: center;font-size: 12px;margin: 0;padding: 0;"></p>');
        description.append(model.rowNum+"行"+model.columnNum+"列，关联");
        var relationNum = $('<b></b>');
        relationNum.html(model.rowNum*model.columnNum);
        description.append(relationNum);
        description.append("个宝贝");
        liObj.append(description);
        if($.cookie("modelId")==i+1) {
            liObj.append($('<a href="#" class=" choosed ischoose"></a>'));
        }
        else {
            liObj.append($('<a href="#" class=" choosing ischoose"></a>'));
        }
        return liObj;
    },
    genQtip : function(){
        $('#planNameInput').qtip({
            content: {
                text: "提示：在初次构建方案时，请更改方案名称,避免重名~"
            },
            position: {
                at: "top left ",
                corner: {
                    target: 'topRight'
                }
            },
            show: {
                when: false,
                ready:true
            },
            hide:false,
            style: {
                name:'cream'
            }
        });
    }


},Relation.step2);

Relation.Event = Relation.Event || {};
Relation.Event = $.extend({
    setAddPlanEvent:function(){
        $(".addPlan").click(function(){
            $.cookie("planName","新建方案");
            $.cookie("planId",0);
            $.cookie("modelId",1);
            Relation.step2.doInit();
        });
    },
    setAddItemsEvent :function(){
        $(".addItems").click(function(){
            var planId = $(this).parent().parent().parent().attr("planId");
            var newName = $(this).parent().parent().parent().attr("planName");
            var modelId = $(this).parent().parent().parent().attr("modelId");
            multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/Relation/getUnRelatedItems","actionURL":"/Relation/addRelationWithModel","pn":1,"px":8,"enableSearch":true,relation:{"isRelation":true,"relationPlanId":planId,"newName":newName,"modelId":modelId}});
        });
    },
    setRemoveItemsEvent :function(){
        $(".removeItems").click(function(){
            var planId = $(this).parent().parent().parent().attr("planId");
            var newName = $(this).parent().parent().parent().attr("planName");
            var modelId = $(this).parent().parent().parent().attr("modelId");
            multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/Relation/getRelatedItems","actionURL":"/Relation/removeRelatedItems","pn":1,"px":8,"enableSearch":false,relation:{"isRelation":true,"relationPlanId":planId,"newName":newName,"modelId":modelId}});
        });
    },
    setDeletePlanEvent : function(){
        $('.deletePlanSpan').click(function(){
            if(confirm("确定要删除该方案？")){
                var planId = $(this).parent().parent().attr("planId");
                $.post("/Relation/deletePlan",{"planId":planId},function(data){
                    if(data.res){TM.Alert.load("方案删除成功~请刷新查看结果~")}
                    else {TM.Alert.load("方案删除失败~请重试或联系客服人员~")}
                });
            }
        });
    },
    setChooseEvent:function(){
        $('.ischoose').click(function(){
            $('.ischoose').removeClass('choosed');
            $('.ischoose').addClass('choosing');
            $(this).removeClass('choosing');
            $(this).addClass('choosed');
        });
    }
},Relation.Event);



