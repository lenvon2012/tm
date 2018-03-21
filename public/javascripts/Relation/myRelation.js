var TM = TM || {};
((function($,window){
    TM.myAssociate = TM.myAssociate || {};
    var myAssociate = TM.myAssociate || {};
    myAssociate.init = myAssociate.init || {};
    //list associatePlan
    myAssociate.init = $.extend({
        doInit:function(container){
//            $("#mainfoot").css('display','none');
            var url = window.location.href;
            var type1 = url.indexOf("?type=1");
            var type2 = url.indexOf("?type=2");
            var type3 = url.indexOf("?type=3");
            $(".validAssociateModels").css("display","block");
            $(".invalidAssociateModels").css('display','none');
            $(".waitAssociateModels").css('display','none');
            $(".sec-page-level").removeClass("current");
            if(type1 > 0){
                $(".ass-valid").addClass("current");
                TM.myAssociate.show.doShow(1);
                $(".validAssociateModels").css("display","block");
            }
            else if(type2 > 0){
                $(".ass-wait").addClass("current");
                TM.myAssociate.show.doShow(2);
                $(".waitAssociateModels").css('display','block');
            }
            else if(type3 > 0){
                $(".ass-invalid").addClass("current");
                TM.myAssociate.show.doShow(3);
                $(".invalidAssociateModels").css('display','block');
            }else{
                $(".ass-valid").addClass("current");
                TM.myAssociate.show.doShow(1);
                $(".validAssociateModels").css("display","block");
            }
        }
    },myAssociate.init);


    myAssociate.show = myAssociate.show || {};
    myAssociate.show = $.extend({
        doShow:function(type){
            if(type == 1){
                var bottom = $(".validAssociateModels_Bottom");
                bottom.tmpage({
                    currPage: 1,
                    pageSize: 5,
                    pageCount:1,
                    ajax:{
                        on: true,
                        dataType: 'json',
                        url:'/Associate/listAssociatePlan',
                        param:{type:type},
                        callback:function(data){
                            var templateTop = $(".validAssociateModels_area");
                            templateTop.empty();
                            if(data.res.length == 0){
                                templateTop.append('<div style="text-align: center;color: #434a54;font-size: 24px;margin: 20px 0 20px;">暂无投放中的模版</div>');
                                return;
                            }
                            for(var n=0;n<data.res.length;n++){
                                $.each(data.res[n],function(key,value){
                                    var templateTop = $(".validAssociateModels_area");
                                    templateTop.append(
                                         "<div class='associateDiv'> " +
                                            "<div class='associateOperate'>" +
                                                "<div class='small_Ass_Btn checkItems' style='float:left;margin-right:20px;'><>已投放宝贝</span></div>" +
                                                "<div class='small_Ass_Btn to-add-put' style='float:left;'><span>+添加投放宝贝</span></div>" +
                                            "</div>" +
                                            "<div class='associateOper'>" +
                                                "<span class='ass-ope-hov ass-ope-span associateStop' >✘取消投放</span>" +
                                                "<span class='ass-ope-hov ass-ope-span associateModify' >✎修改模板</span>" +
//                                                "<span class='ass-ope-hov ass-ope-span associateCopy' >©复制代码</span>" +
                                                "<span class='ass-ope-span checkItems associatedItemsNum'>投放宝贝数：</span>" +
                                                "<span class='ass-ope-span checkItems'>个</span>" +
                                                "<span class='ass-ope-span planName' style='cursor:default;'>模板名称：</span>" +
//                                                "<span style='display:none'>" + value +"</span>"  +
                                            "</div>" +
                                            "<div>"+ key + "</div>" +
                                        "</div>"
                                    );
                                    var template = $(".validAssociateModels_area .associateDiv").eq(n);
                                    var name = template.find(".tzg_tag_name").attr("name");
                                    var planId = name.substring(12,name.length);
                                    template.find(".associatedItemsNum").append(value.count);
                                    template.find(".planName").append(value.planName);
                                    myAssociate.setEvent.toModifyInPut(template);
                                    myAssociate.setEvent.toCopy(template);
                                    myAssociate.setEvent.toStop(template,value.count);
                                    myAssociate.setEvent.toCheck(template,planId);
                                    myAssociate.setEvent.addPutItem(template,planId);
                                    myAssociate.setEvent.mouseHover();
                                });
                            }

                        }
                    }
                });
            }
            else if(type == 2){
                var bottom = $(".waitAssociateModels_Bottom");
                bottom.tmpage({
                    currPage: 1,
                    pageSize: 5,
                    pageCount:1,
                    ajax:{
                        on: true,
                        dataType: 'json',
                        url:'/Associate/listAssociatePlan',
                        param:{type:type},
                        callback:function(data){
                            $(".waitAssociateModels_area").empty();
                            var templateTop = $(".waitAssociateModels_area");
                            if(data.res.length == 0){
                                templateTop.append('<div style="text-align: center;color: #434a54;font-size: 24px;margin: 20px 0 20px;">暂无未投放的模版</div>');
                                return;
                            }
                            for(var n=0;n<data.res.length;n++){
                                $.each(data.res[n],function(key,value){
                                    //待投放
                                   templateTop.append(
                                        "<div class='associateDiv'> " +
                                           "<div class='associateOperate'>" +
                                               "<div class='small_Ass_Btn checkItems associateToPut' style='float:left;margin-right:20px;'><span>☂投放计划</span></div>" +
                                               "<div class='small_Ass_Btn associateModify' style='float:left;'><span>✎修改计划</span></div>" +
                                           "</div>" +
                                           "<div class='associateOper'>" +
//                                               "<span class='ass-ope-hov ass-ope-span associateCopy' >©复制代码</span>" +
                                               "<span class='ass-ope-hov ass-ope-span associateDelete' >✘删除计划</span>" +
                                               "<span class='ass-ope-span planName' style='cursor:default;'>模板名称：</span>" +
                                               "<span style='display:none'>" + value.modelId +"</span>"  +
                                           "</div>" +
                                           "<div>"+ key + "</div>" +
                                       "</div>"
                                   );
                                   var template = $(".waitAssociateModels_area .associateDiv").eq(n);
                                   var name = template.find(".tzg_tag_name").attr("name");
                                   var planId = name.substring(12,name.length);
                                   template.find(".planName").append(value.planName);
                                   myAssociate.setEvent.toModify(template);
                                   myAssociate.setEvent.toPut(template);
                                   myAssociate.setEvent.toCopy(template);
                                   myAssociate.setEvent.toDelete(template,type);
                                   myAssociate.setEvent.mouseOver();
                                   myAssociate.setEvent.mouseHover();
                                });
                            }

                        }
                    }
                });
            }
            else if(type == 3){
                var bottom = $(".invalidAssociateModels_Bottom");
                bottom.tmpage({
                    currPage: 1,
                    pageSize: 8,
                    pageCount:1,
//                    useSmallPageSize: false,
                    ajax:{
                        on: true,
                        dataType: 'json',
                        url:'/Associate/listAssociatePlan',
                        param:{type:type},
                        callback:function(data){
                            $(".invalidAssociateModels_area").empty();
                            var templateTop = $(".invalidAssociateModels_area");
                            if(data.res.length == 0){
                                templateTop.append('<div style="text-align: center;color: #434a54;font-size: 24px;margin: 20px 0 20px;">暂无已结束的模版</div>');
                                return;
                            }
                            for(var n=0;n<data.res.length;n++){
                                $.each(data.res[n],function(key,value){
                                      //已结束
                                    templateTop.append(
                                      "<div class='associateDiv' style='position:relative;top:15px;'><div class='associateId' style='width:100%;margin:0 auto;text-align:center;position:relative;margin-left:0px;'>" +
                                          "<table>" +
                                              "<tr style='height:30px'>" +
                                                  "<td class='inval-div-td-cursor'>" + value.modelId + "</td>" +
                                                  "<td class='inval-div-td associateToPut'><a>☂投放模板</a></td>" +
                                                  "<td class='inval-div-td associateModify'><a>✎修改模板</a></td>" +
//                                                  "<td class='inval-div-td associateCopy'><a>©复制代码</a></td>" +
                                                  "<td class='inval-div-td associateDelete'><a>✘删除模板</a></td>" +
                                                  "<td class='inval-div-td-cursor'>模板名称：</td>" +
                                                  "<td style='width:400px;'><span class='planName' style='width:300px;'></span></td>" +
                                                  "<td class='click-show-tmp' >点击展示</td>"  +
                                                  "<td style='width:10px;'></td>" +
                                              "</tr>" +
                                          "</table>" + key +
                                      "</div>"
                                      );
                                      var template = $(".invalidAssociateModels_area .associateDiv").eq(n);
                                      var name = template.find(".tzg_tag_name").attr("name");
                                      var planId = name.substring(12,name.length);
                                        template.find(".planName").html(value.planName);
                                      myAssociate.setEvent.clickCheckTmp(template);
                                      myAssociate.setEvent.toModify(template);
                                      myAssociate.setEvent.toPut(template);
                                      myAssociate.setEvent.toCopy(template);
                                      myAssociate.setEvent.toDelete(template,type);
                                      myAssociate.setEvent.mouseOver();
                                });
                            }
                            $(".invalidAssociateModels_area .template").css("display","none");

                        }
                    }
                });
            }
        }
    },myAssociate.doShow);


    myAssociate.setEvent = myAssociate.setEvent || {};
    myAssociate.setEvent = $.extend({
        mouseHover:function(){
            $(".ass-ope-hov").hover(function(){
                $(this).addClass("ass-ope-hov-in");
            },function(){
                $(this).removeClass("ass-ope-hov-in");
            });
        },
        toPut:function(template){
            template.find(".associateToPut").click(function(){
                 var name = template.find(".tzg_tag_name").attr('name');
                 var planId = name.substring(12,name.length);
                 window.location.href = "/relation/relationoper?id=" + planId + "&toput=true";
            });
        },
        toModify:function(template){
            template.find(".associateModify").click(function(){
                var name = template.find(".tzg_tag_name").attr('name');
                var planId = name.substring(12,name.length);
                window.location.href = "/relation/relationoper?id=" + planId;
                //IE
                window.event.returnValue = false;
            });
        },
        toModifyInPut:function(template){
            template.find(".associateModify").click(function(){

                var name = template.find(".tzg_tag_name").attr('name');
                var planId = name.substring(12,name.length);
                window.location.href = "/relation/relationoper?id=" + planId + "&input=true";
                //IE
                window.event.returnValue = false;
            });
        },
        toCopy:function(template){
            template.find(".associateCopy").click(function(){
                var html = "<textarea style='width:500px;height:400px' value='" + template.find(".tmp_table").html() + "'</textarea>";
                TM.Alert.showDialog(html, 500, 400, function(){}, function(){}, '模板代码');
            });
        },
        toDelete:function(template,type){
            template.find(".associateDelete").click(function(){
                var tmpObj = $(this).parents(".associateDiv");
                TM.Alert.showDialog("确定要删除此计划？",250,150,function(){
                    var name = template.find(".tzg_tag_name").attr('name');
                    var planId = name.substring(12,name.length);
                    $.post('/Associate/deletePlanId',{planId:planId},function(data){
                        if(data){
                            tmpObj.css("display","none");
                            TM.Alert.load("删除成功",250,150,function(){return false;},"提示");
                        }
                    });
                },function(){return false},"提示");
            });
        },

        toStop:function(template,count){
            template.find(".associateStop").click(function(){
                var msg = '<div class="dia-msg">确定要暂停此计划?</div>';
                msg += '<div class="dia-msg-sig">预计退出投放<strong class="dia-msg-num">' +　count + '</strong>个</div>';
                TM.Alert.showDialog(msg,350,300,function(){
                    var name = template.find(".tzg_tag_name").attr('name');
                    var planId = name.substring(12,name.length);
                    $.post('/Associate/stopPlanId',{planId:planId},function(data){
                        TM.associate.toPut.toPutResultDialog(data,'退出投放');
                    });
                },function(){return false},"提示");
            });
        },
        toCheck:function(template,planId){
            template.find(".checkItems").click(function(){
                var html = $(".checkWindowList");
                $(".checkWindow_Area").empty();
                $.ajax({
                    url:'/associate/AssociatedItems',
                    data:{planId:planId},
                    type:'post',
                    success:function(data){
                        if(data != null){
                            var tdObjs = $("#checkItemBox").tmpl(data);
                            $(".checkWindow_Area").append(tdObjs);
                        }

                        TM.myAssociate.showDialog(html, 860, 700, function(){myAssociate.setEvent.removeAssociateBatch(planId);}, function(){return false;}, '投放成功宝贝,点击图片查看宝贝,点击标题选中');
                        $(".tmAlert .checkWindowList").css('display','block');

                        myAssociate.setEvent.mouseOver();
                        myAssociate.setEvent.mouseClickOut();
                        myAssociate.setEvent.removeAssociate(planId);
                    }
                });

            });
        },
        removeAssociate:function(planId){
            $(".tmAlert .in-info-checkBtn").click(function(){
                if(confirm("确定要退出投放吗?")){
                    var currBtn = $(this);
                    var itemId = $(this).parents(".input-info").find(".item_id").attr("value");
                    $.post('/Associate/deleteByNumId',{itemId:itemId,planId:planId},function(data){
                        if(data.ok){
                            alert("退出投放成功");
                            currBtn.parents(".input-info").css("display","none");
                        }else{
                            currBtn.parents(".input-info").find(".in-info-result").html("退出投放失败");
                            currBtn.addClass("delPlanError");
                            currBtn.html("查看原因");
                            myAssociate.setEvent.checkErrorResult(data);
                        }
                    });
                }
            });
        },
        checkErrorResult:function(data){
            $(".input-info .delPlanError").unbind("click").click(function(){
                //TODO
                alert(data.msg);
            });
        },

        removeAssociateBatch:function(planId){
            var arr = new Array();
            $(".input-info input[type='checkBox']:checked").each(function(){
                arr.push($(this).parents(".input-info").find(".in-info-td-2").attr('value'));
            });
            var numIids = arr.join("!@#");
            $.post('/Associate/deleteBatch',{numIids:numIids,planId:planId},function(data){
                TM.associate.toPut.toPutResultDialog(data,'退出投放');
            });
        },

        addPutItem:function(template,planId){
            template.find(".to-add-put").click(function(){
                myAssociate.listItem.initSearch(planId);
            });
        },
        clickCheckTmp:function(template){
            template.find(".click-show-tmp").click(function(){
                if(template.find(".template").css('display') == 'none'){
                    template.find(".template").css('display','block');
                    $(this).html("点击隐藏");
                }else{
                    template.find(".template").css('display','none');
                    $(this).html("点击展示");
                }
            });
        },
        mouseOver:function(){
            $(".click-show-tmp").hover(function(){
                $(this).addClass("click-show-tmp-mouse-in");
            },function(){
                $(this).removeClass("click-show-tmp-mouse-in");
            });

            $(".inval-div-td").hover(function(){
                $(this).addClass("inval-div-td-mouse-in");
            },function(){
                $(this).removeClass("inval-div-td-mouse-in");
            });

            $(".toAddPut_Area").find(".put_item_div").hover(function(){
                if(!$(this).hasClass("toPut-item-disable") ){
                    $(this).addClass("putItem_Mouse_In");
                    $(this).css("border","1px solid #FB6E52");
                }
            },function(){
                if(!$(this).hasClass("toPut-item-disable") && !$(this).hasClass("putItem_Mouse_Click")){
                    $(this).removeClass("putItem_Mouse_In");
                    $(this).css("border","1px solid #fff");
                }
            });

            $(".tmAlert .input-info").hover(function(){
                $(this).addClass("input-info-bac");
            },function(){
                $(this).removeClass("input-info-bac");
            });

            $(".tmAlert .in-info-checkBtn").hover(function(){
                $(this).addClass("in-info-btn-dark");
            },function(){
                $(this).removeClass("in-info-btn-dark");
            });
        },
        mouseClick:function(){
            $(".toAddPut_Area").find(".put_item_div").click(function(){
            var numId = $(this).find(".item_id").attr("value");
                if(!$(this).hasClass("toPut-item-disable") && !$(this).hasClass("putItem_Mouse_Click")){
                    $(this).addClass("putItem_Mouse_Click");
                    $(this).css("border","1px solid #FB6E52");
                    $(this).find("input").attr('checked',true);
                    TM.associate.result.addSelectNumIid(numId);
                }else if(!$(this).hasClass("toPut-item-disable")){
                    $(this).removeClass("putItem_Mouse_Click");
                    $(this).find("input").attr('checked',false);
                    $(this).css("border","1px solid #fff");
                    TM.associate.result.removeSelectNumIid(numId);
                }
            });
        },

        mouseClickOut:function(){
            $(".tmAlert .in-info-td-4").click(function(){
                var infoObj = $(this).parent();
                var checkBoxObj = $(this).parent().find(".in-info-td-checkbox");
                if(checkBoxObj.is(":checked")){
//                    checkBoxObj.removeAttr("checked");

                    checkBoxObj.attr("checked",false);

                    infoObj.removeClass("input-info-che-bac");
                }else{
                    checkBoxObj.attr("checked",true);
                    infoObj.addClass("input-info-che-bac");
                }
            });
            $(".tmAlert .in-info-td-checkbox").click(function(){
                if($(this).is(":checked")){
                    $(this).attr("checked",true);
                    $(this).parents(".input-info").addClass("input-info-che-bac");
                }else{
//                    $(this).removeAttr("checked","checked");
                    $(this).attr("checked",false);
                    $(this).parents(".input-info").removeClass("input-info-che-bac");
                }
            });
        }
    },myAssociate.setEvent);

    myAssociate.listItem = myAssociate.listItem || {};
    myAssociate.listItem = $.extend({
        initSearch:function(planId){
            var paramObj = $(".toAddPutArea .searchParams");
            var sellerCat = paramObj.find(".searchSelect");
             //获取店铺分类
            $.get('/Items/sellerCatCount',function(data){
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }
                var exist = false;
                var cat = $('<option>店铺类目</option>');
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }
                    exist = true;
                        var option = $('<option></option>');
                        option.attr("catId",data[i].id);
                        option.html(data[i].name+"("+data[i].count+")");
                        sellerCat.append(option);
                }
                if(!exist){
                    sellerCat.hide();
                }
                myAssociate.listItem.init(planId);
                myAssociate.listItem.doSearch(planId,paramObj);
                myAssociate.listItem.doSort(planId,paramObj);
                myAssociate.listItem.onChange(planId);
            });
        },

        init:function(planId){
            var paramObj = $(".toAddPutArea .searchParams");
            var params = myAssociate.listItem.getParams(paramObj);
            $(".toAddPut_Area").empty();
            $(".toAddItem_Bottom").tmpage({
                currPage: 1,
                pageSize: 12,
                pageCount:1,
                ajax: {
                    url:'/Associate/listItems',
                    on: true,
                    dataType: 'json',
                    param:params,
                    callback:function(data){
                        if(data.res.length > 0){
                             $(".toAddPut_Area").empty();
                             var tdObjs = $("#toAddPutItem").tmpl(data.res);
                             $.each(tdObjs,function(i,value){
                                 if(i % 3 == 0){
                                     $(".toAddPut_Area").append("<tr class='it-li-tr'></tr>");
                                 }
                                  $("tr[class='it-li-tr']:last").append($(this));

                                  //href="//item.taobao.com/item.htm?id=123456
                                 var itemId = $(this).find(".item_id").attr('value');
                                 $(this).find(".item-text-href").attr('href','//item.taobao.com/item.html?id='+itemId);

                                 if(TM.associate.result.isInSelectArray(itemId) == true){
                                    $(this).find(".put_item_div").addClass("putItem_Mouse_Click");
                                    $(this).find(".put_item_div").css("border","1px solid #FF7744")
                                    $(this).find("input").attr("checked",true);
                                 }

                             });
                             var modelId = $(".validAssociateModels .template").attr("value");
                             TM.myAssociate.listItem.disableItem(planId);
                             TM.myAssociate.listItem.toAddPut(planId,modelId);
                        }
                    }
                }
            });
        },

        disableItem:function(planId){
            var toPutObj = $(".toAddPut_Area");
            $.post('/Associate/AssociatedItems',{planId:planId},function(data){
                var itemObj = toPutObj.find(".put_item_div");
                $.each(data,function(i,value){
                    if(value != null){
                        $.each(itemObj,function(){
                            if($(this).find(".item_id").attr('value') == value.id  ){
                                $(this).addClass("toPut-item-disable");
                                $(this).find("input").prop('disabled',true);
                                $(this).find("input").prop('checked',true);
                            }
                        });
                    }
                });
                TM.myAssociate.listItem.showArea(planId);
            });
        },

        showArea:function(){
            $(".validAssociateModels").css('display','none');
            $(".toAddPutArea").css("display","block");
            myAssociate.setEvent.mouseOver();
            myAssociate.setEvent.mouseClick();
        },

        toAddPut:function(planId,modelId){
            $(".toAddItemBtn").unbind("click").click(function(){
                var itemIidsArr = TM.associate.result.getSelectNumIidArray();
                var itemIids = itemIidsArr.join("!@#");
                if(!itemIidsArr.length > 0){
                   alert("请添加宝贝");
                   return false;
                }else{
                    TM.Alert.showDialog('<a>确定要投放以下<span class="successNum">' + itemIidsArr.length + '</span>个宝贝？</a>', 250,200, function(){TM.myAssociate.listItem.toPut(itemIids,planId,modelId)},function(){return false},"提示");
                }
            });
        },
        toPut:function(itemIids,planId,modelId){
            $.post("/Associate/addNewAssociatedItems",{itemIids:itemIids,planId:planId,modelId:modelId},function(data){
                TM.associate.toPut.toPutResultDialog(data,'投放');
            });
        },

        getParams:function(paramObj){
            paramObj.find(".searchParams");
            var searchText = paramObj.find(".textInput").attr("value");
            var sellerCid = paramObj.find(".searchSelect option:selected").attr("catid");
            var sort = paramObj.find(".current").attr("sort");
            if(sellerCid === undefined){
                sellerCid = null;
            }
            if(sort === undefined){
                sort = -1;
            }
            var params = {};
            params.sort = sort;
            params.searchText = searchText;
            params.sellerCid = sellerCid;
            return params;
        },

        onChange:function(planId){
            $(".toAddPutArea .searchSelect").change(function(){
                myAssociate.listItem.init(planId);
            });
        },

        doSearch:function(planId,paramObj){
            var searchObj = paramObj.find(".doSearch");
            searchObj.unbind("click").click(function(){
                myAssociate.listItem.init(planId);
            });
        },

        doSort:function(planId,paramObj){
            var sortObj = paramObj.find("td");
            var saleSortObj = paramObj.find(".saleSort");
            saleSortObj.click(function(){
                saleSortObj.css('display','none');
                sortObj.removeClass("current");
                if($(this).hasClass("saleSortDown")){
                    $(this).css('display','none');
                    $(this).removeClass("current");
                    paramObj.find(".saleSortUp").css('display','');
                    paramObj.find(".saleSortUp").addClass('current');

                }else if($(this).hasClass("saleSortUp")){
                    $(this).css('display','none');
                    $(this).removeClass("current");
                    paramObj.find(".saleSortDown").css('display','');
                    paramObj.find(".saleSortDown").addClass('current');
                }
                myAssociate.listItem.init(planId);
            });
            var priceSortObj = paramObj.find(".priceSort");
            priceSortObj.click(function(){
                priceSortObj.css('display','none');
                sortObj.removeClass("current");
                if($(this).hasClass("priceSortDown")){
                    $(this).css('display','none');
                    $(this).removeClass("current");
                    paramObj.find(".priceSortUp").css('display','');
                    paramObj.find(".priceSortUp").addClass('current');

                }else if($(this).hasClass("priceSortUp")){
                    $(this).css('display','none');
                    $(this).removeClass("current");
                    paramObj.find(".priceSortDown").css('display','');
                    paramObj.find(".priceSortDown").addClass('current');

                }
                myAssociate.listItem.init(planId);
            });
        }
    },myAssociate.listItemDialog);

    TM.myAssociate.showDialog=function(html, width, height, okCallback, cancelCallback, title){
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




