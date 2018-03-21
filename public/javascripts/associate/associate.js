  var TM = TM || {};
  ((function($,window){
    TM.associate = TM.associate ||{};
    var associate = TM.associate || {};
    associate.init = associate.init || {};
    associate.init = $.extend({
        //初始化的时候 先解析url 有id 表明修改，又有toPut表明投放
        doInit:function(container){
            $("#mainfoot").css('display','none');
            associate.container = container;
            var url = window.location.href;
            var index = "?id=".length;
            var idFlag = url.indexOf("?id=");
            var putFlag = url.indexOf("&toput=true");

            var inPut = url.indexOf("&input=true");
            //只有Id 修改
            if(idFlag > 0 && putFlag < 0 && inPut < 0){
                var planId = url.substring(idFlag + index,url.length);
                $(".toAddItem").css('display','none');
                $(".set_step2 span").addClass("current_step");
                $.post('/Associate/findPlanById',{planId:planId},function(data){
                    if(data != null){
                        $("#content").hide();
                        $("#setModel").css('display','block');
                        $(".set_step").css("display",'block');
                        $.ajax({
                            url:"/Associate/getPlanIdHtml",
                            data:{planId:planId},
                            type:"post",
                            success:function(data){
                                $("#setModel .template_Msg").html(data);
                                $("#setModel .to_modify_item").css('visibility','visible');
                                $("#setModel .btn_under_template").css('display','block');
                                $("#setModel .go_back_to_default").css('display','none');
                                $("#setModel .go_back_to_select").css('display','none');
                                $("#setModel .templateName").attr('value',data.planName);

                                var flagArea = 'setModelArea';
                                TM.associate.property.appendProperty(flagArea);

                                //在每个模板上添加删除按钮
                                 var items = $("#setModel .tmp_td_item");
                                 $.each(items,function(){
                                     if($(this).find(".tmp_td_item_id").attr('value') > 0){
                                         if($(this).css("position") == 'static')
                                         {
                                             $(this).css('position','relative')
                                         }
                                         $(this).append("<div class='tmp_item_del' style='display:none;'><a><img src='/public/images/associateModel/delete.png' style='height:40px;width:35px;'/></a></div>");
                                         TM.associate.deleteItem.clickDeleteItem();
                                         TM.associate.init.delHover();
                                     }
                                 });
                                $(".set_tmp_step.set_step2").addClass("step_h_current");
                                TM.associate.listItemsDiag.init(planId,null);
//                                var saveBtn = $("#setModel .save_associate_plan");
//                                TM.associate.saveAssociatePlan.savePlan(saveBtn,planId);
                                var btn = $("#setModel .go_Next");
                                TM.associate.insertItem.clickNext(btn,planId);
                            }
                        });
                    }
                });
            }

            //直接投放 ?id= toPut=true
            else if(idFlag > 0 && putFlag > 0 && inPut < 0){
                 var planId = url.substring(idFlag + index,putFlag);
                 $.post('/Associate/findPlanById',{planId:planId},function(data){
                     if(data != null){
                         $("#content").hide();
                         $("#setModel").css('display','none');
                         $(".set_step").css("display",'block');
                         $("#toPutItemList").css('display','block');

                         $(".set_tmp_step").css('display','block');
                         $(".toPutBtn").css('display','block');
                         $("#toPutItemList").css('display','none');
                         $(".set_step3").addClass("step_h_current");

                         TM.associate.insertItem.clickCustom();
                         TM.associate.toPut.initSearch(planId);
                         associate.mouseOn.showToPutComment();
                         //一键投放
                         TM.associate.toPut.putPlanAll(planId);
                         //选择投放
                         TM.associate.toPut.putPlan(planId);
                     }
                 });
            }

            //修改投放中的计划
            //?id= inPut=true
            else if(idFlag > 0 && inPut > 0 && putFlag < 0){
                var planId = url.substring(idFlag + index,inPut);
                 $.post('/Associate/findPlanById',{planId:planId},function(data){
                    if(data != null){
                        $("#content").hide();
                        $("#setModel").css('display','block');
                        $.ajax({
                            url:"/Associate/getPlanIdHtml",
                            data:{planId:planId},
                            type:"post",
                            success:function(data){
                                $("#setModel .template_Msg").html(data);
                                $("#setModel .to_modify_item").css('visibility','visible');
                                $("#setModel .btn_under_template").css('display','block');
                                //必须remove掉
//                                $("#setModel .save_associate_plan").remove();
                                $("#setModel .go_back_to_default").remove();
                                $("#setModel .go_back_to_select").remove();
                                $(".set_step").css("display",'block');

                                $(".set_tmp_step.set_step2").addClass("step_h_current");
                                TM.associate.mouseOn.mouseOnChoose();

                                //在每个模板上添加删除按钮
                                 var items = $("#setModel .tmp_td_item");
                                 $.each(items,function(){
                                     if($(this).find(".tmp_td_item_id").attr('value') > 0){
                                          if($(this).css("position") == 'static')
                                          {
                                              $(this).css('position','relative')
                                          }
                                          $(this).append("<div class='tmp_item_del' style='display:none;'>"
                                                                              + "<a><img src='/public/images/associateModel/delete.png' style='height:40px;width:35px;'/></a></div>");
                                          TM.associate.deleteItem.clickDeleteItem();
                                          TM.associate.init.delHover();
                                     }
                                 });
                                 //设置模板区域
                                 var flagArea = 'setModelArea';
                                 TM.associate.property.appendProperty(flagArea);

                                 $(".set_step").css('display','block');
                                 var btn = $("#setModel .go_Next");
                                 var flag = 'input=true';
                                 TM.associate.listItemsDiag.init(planId,flag);
                                 TM.associate.insertItem.clickNext(btn,planId);
                            }
                        });
                    }
                 });
            }
            else{
                TM.associate.show.doShow();
                TM.associate.show.clickShow(container);
            }
            associate.init.btnHover();
        },

        getPlanId:function(){
            var url = window.location.href;
            var index = "?id=".length;
            var idFlag = url.indexOf("?id=");
            var putFlag = url.indexOf("&toput=true");
            var inPut = url.indexOf("&input=true");
            if(idFlag > 0 && putFlag < 0 && inPut < 0){
                var planId = url.substring(idFlag + index,url.length);
            }
            else if(idFlag > 0 && putFlag > 0 && inPut < 0){
                var planId = url.substring(idFlag + index,inPut);
            }else if(idFlag > 0 && inPut > 0 && putFlag < 0){
                var planId = url.substring(idFlag + index,inPut);
            }
            else{
                var planId = null;
            }
            return planId;
        },
        delHover:function(){
            $(".tmp_td_item").hover(function(){
                $(this).find(".tmp_item_del").css("display",'block');
            },function(){
                $(this).find(".tmp_item_del").css("display",'none');
            });
        },
        getContainer:function(){
            return associate.container;
        },

        btnHover:function(){
            $(".set_begin").hover(function(){
                $(this).addClass("btnDark");
            },function(){
                $(this).removeClass("btnDark");
            });
        }
    },associate.init);

    //显示模板
    associate.show = associate.show || {};
    associate.show = $.extend({
        doShow:function(){
            $(".set_step").css('display','none');
            var params = TM.associate.show.getParamData();
            var templateTop = $(".template_area");
            var bottom = $(".template_area_bottom");
            bottom.tmpage({
                currPage: 1,
                pageSize:5,
                pageCount:1,
//                useSmallPageSize: true,
                ajax: {
                    param : params,
                    on: true,
                    dataType: 'json',
                    url: "/Associate/listModel",
                    callback:function(data){
                        $(".template_area").empty();
                        if(data.res == null){
                            templateTop.append();
//                            "<div style='position:relative;clear:left;top:15px;text-align:center;'>玩命帮你设计中....." +
//                                    "<span style='position:relative;left:42%;'><a style='display:block;height:90px;width:100px;" +
//                                    "background-image:url(http://img02.taobaocdn.com/imgextra/i2/79742176/T2_yxjXYlaXXXXXXXX-79742176.gif)'></a></span></div>");
                        }
                        else{
                            $("#template_prop").tmpl(data.res).appendTo(".template_area");
                            //选择模板
                            associate.select.doDefaultSelect();
                            associate.mouseOn.chooseTmpOver();
                        }
                    }
                }
            });
        },
         clickShow:function(container){
              container.find(".templateWidth").unbind("click").click(function(){
                  var size = $(this).attr("width");
                  var count = $(".templateItemCount.tmpl_w_selected").attr("count");
                  window.location.href="/associate/associate?size=" + size + "&count=" + count;
                  $(".set_step").css('display','none');
                  associate.show.doShow();
              });

              container.find(".templateItemCount").unbind("click").click(function(){
                  var count = $(this).attr("count");
                  var size = $(".templateWidth.tmpl_w_selected").attr("width");
                  window.location.href="/associate/associate?size=" + size + "&count=" + count;
                  $(".set_step").css('display','none');
                  associate.show.doShow();
              });

              $(".set_step").css('display','none');
              var url = window.location.href;
              var sizeFlag = url.indexOf("?size=");
              var countFlag = url.indexOf("&count=");
              var size = url.substring(sizeFlag + "?size=".length,countFlag);
              var count = url.substring(countFlag + "&count=".length,url.length);

              $(".templateWidth").removeClass("tmpl_w_selected");
              $(".templateWidth").each(function(){
                    if($(this).attr('width') == size){
                        $(this).addClass("tmpl_w_selected");
                    }
              });

              $(".templateItemCount").removeClass("tmpl_w_selected");
              $(".templateItemCount").each(function(){
                    if($(this).attr('count') == count){
                        $(this).addClass("tmpl_w_selected");
                    }
              });
         },

        //获取参数,模板宽度,模板最大宝贝数量,模板类型
        getParamData:function(){
            var paramData = {};
            var url = window.location.href;
            var sizeFlag = url.indexOf("?size=");
            var countFlag = url.indexOf("&count=");
            var templateWidth = url.substring(sizeFlag + "?size=".length,countFlag);
            var templateMaxNum = url.substring(countFlag + "&count=".length,url.length);

            if(templateWidth == 750 || templateWidth == 790 || templateWidth == 950){
                 paramData.width = templateWidth;
            }else{
                window.location.href="/associate/associate?size=750&count=0";
            }

            if(templateMaxNum == 0 || templateMaxNum == 4 || templateMaxNum == 6 || templateMaxNum == 8 || templateMaxNum == 9 || templateMaxNum == 12){
                paramData.maxNum = templateMaxNum;
            }else{
                window.location.href="/associate/associate?size=750&count=0";
            }

            var templateType = $(".menu .associate-type").attr("type-data");

            paramData.type = templateType;
            return paramData;
        }
    },associate.show);


    associate.select = associate.select || {};
    associate.select = $.extend({
        //默认的给六个 点击认定我了
        doDefaultSelect:function(){
            $(".template_div .set_begin").click(function(){

                $(".set_begin").removeClass("set_begin_choosed");
                $(".set_begin").html("开始制作");
                $(this).addClass("set_begin_choosed");
                $(this).html("√已选择");

                $("#content").hide();
                $("#defaultTemplate").css('display','block');
                var  modelId= $(this).attr("modelId");
                $(".to_modify_item .templateId").html(modelId);

                $.ajax({
                    url:'/Associate/selectModel',
                    data:{modelId:modelId},
                    type:'post',
                    success:function(data){
                        $("#defaultTemplate .defaultTmp_Area").html(data[1]);
                        $("#defaultTemplate .user-defined").attr('value',modelId);
                        $.get('/Associate/getRelatedRecommends',function(data){
                            //遍历到模板中，作为推荐模板
                            $.each(data,function(i,value){
                                var itemBoxes = $("#defaultTemplate .tmp_td_item");
                                itemBoxes.eq(i).find(".tmp_td_item_id").attr('value',value.id);

                                if(itemBoxes.eq(i).find(".tmp_td_item_img").is("img")){
                                    itemBoxes.eq(i).find(".tmp_td_item_img").attr('src',value.picURL);
                                }else{
                                    itemBoxes.eq(i).find(".tmp_td_item_img").attr('background',value.picURL);
                                }
                                itemBoxes.eq(i).find(".tmp_td_item_title").html(value.title);
                                itemBoxes.eq(i).find(".tmp_td_item_price").html(value.minPrice);
                                itemBoxes.eq(i).find(".tmp_td_item_origPrice").html(value.price);

                                itemBoxes.eq(i).find(".tmp_td_item_href").attr('href','//item.taobao.com/item.htm?id=' + value.id);
                                itemBoxes.eq(i).find(".tmp_td_item_href").attr("title",value.title);
                            });
                        });
                        TM.associate.select.doSelect(data[0]);

                         $(".to_modify_item").css('visibility','visible');

                        var deModelName = associate.select.getCurTime();
                        $("#defaultTemplate .templateName").attr("value","关联模板" + deModelName);

                        var btn = $("#defaultTemplate .go_Next");
                        TM.associate.insertItem.clickNext(btn,null);


                        $(".set_step").css("display","block");
                        $(".set_tmp_step").removeClass("step_h_current");
                        $(".set_tmp_step.set_step2").addClass("step_h_current");


                        //默认模板区域
                        var flagArea = 'defaultModelArea';
                        TM.associate.property.appendProperty(flagArea);

                        //应该可以返回重新选择模板
                        TM.associate.mouseOn.mouseOnChoose();
                        TM.associate.select.goBackChoseTmp();
                    }
                });
            });
        },

        //选择自定义
        doSelect:function(){
            $(".user-defined").unbind("click").click(function(){
                $("#defaultTemplate").css('display','none');
                $("#setModel").css('display','block');
                var modelId =  $(this).attr("value");
                $.ajax({
                    url:"/Associate/selectModel",
                    data:{modelId:modelId},
                    type:"post",
                    success:function(data){
                        $("#setModel .template_Msg").html(data[1]);

                        var deModelName = associate.select.getCurTime();
                        $("#setModel .templateName").attr("value","关联模板" + deModelName);

                        $(".setModel_property").empty();

                        var tdObj = $(".template_Msg").find(".tmp_td_item");
                        $.each(tdObj,function(i,value){
                            if(i != 0){
                                $(this).css('display','none');
                            }
                        });
                        var flagArea = 'setModelArea';
                        TM.associate.property.appendProperty(flagArea);

                        //点击新增，弹出对话框
                        TM.associate.listItemsDiag.init(null);
                        TM.associate.select.backDefault();
                        TM.associate.init.delHover();
                    }
                });
            });
        },

        //返回默认模板
        backDefault:function(){
            $("#setModel .go_back_to_default").unbind("click").click(function(){
                $("#defaultTemplate").css('display','block');
                $("#setModel").css('display','none');
            });
        },

        //返回选择模板
        goBackChoseTmp:function(){
            $(".set_step .set_step1").unbind("click").click(function(){
                $("#content").show();
                $("#defaultTemplate").css('display','none');
                $("#setModel").css('display','none');
                $("#toPutItemList").css('display','none');
                $(".set_step").css('display','none');
                $(".toPutBtn").css('display','none');
            });
        },
        getCurTime:function(){
            return new Date().formatYMDMS();
        }
    },associate.select);

    //添加宝贝对话框
    associate.listItemsDiag = associate.listItemsDiag || {};
    associate.listItemsDiag = $.extend({
        init:function(planId){
            var paramObj = $(".tmAlert .searchParams");
            var params = associate.listItemsDiag.getParams(paramObj);
            $(".itemListBottom").tmpage({
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
                             $(".itemsList_Area").empty();
                             var tdObjs = $("#windowListItemBox").tmpl(data.res);
                             $.each(tdObjs,function(i,value){
                                 if(i % 5 == 0){
                                     $(".itemsList_Area").append("<tr></tr>");
                                 }
                                 $(".itemsList_Area tr:last").append($(this));

                                  //href="//item.taobao.com/item.htm?id=123456
                                 var itemId = $(this).find(".item_id").attr('value');
                                 $(this).find(".item-text-href").attr('href','//item.taobao.com/item.html?id='+itemId);
                             });
                        }else{
                            $(".itemsList_Area").empty();
                            $(".itemsList_Area").append('<tr><td style="text-align: center;width:880px;">抱歉，没有找到相关的宝贝<td><tr>');
                        }
                        TM.associate.listItemsDiag.showDialog(planId,data.res);
                    }
                }
            });
        },

        showDialog:function(planId,results){
            var container = $("#setModel");
            TM.associate.listItemsDiag.setEvent(results);
            TM.associate.mouseOn.mouseOver();
            TM.associate.insertItem.addItem();

            container.find(".to_add_item").unbind("click").click(function(){
                var html = $("#window").html();

                TM.Alert.load(html, 880,640, function(){TM.associate.insertItem.closeWindow(planId);},function(){return false},"点击图片选择宝贝，点击标题查看宝贝");
                associate.listItemsDiag.init(planId);

                $(".tmAlert .itemsWindow").css('display','block');

                TM.associate.listItemsDiag.initDialogSearch();
                TM.associate.listItemsDiag.doDialogSearch(planId);
                TM.associate.listItemsDiag.doDialogSort(planId);
                TM.associate.listItemsDiag.onChange(planId);
            });
        },

        initDialogSearch:function(){
            var paramObj = $(".tmAlert .searchParams");
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
                        option.html(data[i].name);
                        sellerCat.append(option);
                }
                if(!exist){
                    sellerCat.hide();
                }
            });
        },

        onChange:function(planId){

            $(".itemsWindow .searchSelect").unbind("change").change(function(){
                TM.associate.listItemsDiag.init(planId);
            });
        },

        doDialogSearch:function(planId,flag){
            var paramObj = $(".tmAlert .searchParams");
            var searchObj = paramObj.find(".doSearch");
            searchObj.unbind("click").click(function(){
                TM.associate.listItemsDiag.init(planId,flag);
            });
        },

        doDialogSort:function(planId,flag){
            var paramObj = $(".tmAlert .searchParams");
            var sortObj = paramObj.find("td");
            var saleSortObj = paramObj.find(".saleSort");
            saleSortObj.unbind("click").click(function(){
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
                TM.associate.listItemsDiag.init(planId,flag);


            });
            var priceSortObj = paramObj.find(".priceSort");
            priceSortObj.unbind("click").click(function(){
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
                TM.associate.listItemsDiag.init(planId,flag);
            });
        },

        setEvent:function(results){
            var arr = new Array();

            $("#setModel .tmp_td_item").each(function(){
                var id = $(this).find(".tmp_td_item_id").attr('value');

                if(id !== undefined && id != ''){
                     arr.push(id);
                }
            });

            for(var i=0;i<results.length;i++){
                for(var n=0;n<=arr.length;n++){
                    //if exist
                    if( arr[n] == results[i].id ){
                        $(".itemsList_Area td").eq(i).find(".item_id").attr('value',results[i].id);
                        $(".itemsList_Area td").eq(i).find(".item_img img").attr('src',results[i].picURL);
                        $(".itemsList_Area td").eq(i).find(".item_title").html(results[i].title);
                        $(".itemsList_Area td").eq(i).find(".item_price b").html(results[i].minPrice);
                        $(".itemsList_Area td").eq(i).find(".item_origPrice b").html(results[i].price);


                        $(".itemsList_Area td").eq(i).addClass("wid-item-box-in");
                        $(".itemsList_Area td").eq(i).css("border","1px solid #FF7744");
                    }else{
                        $(".itemsList_Area td").eq(i).find(".item_id").attr('value',results[i].id);
                        $(".itemsList_Area td").eq(i).find(".item_img img").attr('src',results[i].picURL);
                        $(".itemsList_Area td").eq(i).find(".item_title").html(results[i].title);
                        $(".itemsList_Area td").eq(i).find(".item_price b").html(results[i].minPrice);
                        $(".itemsList_Area td").eq(i).find(".item_origPrice b").html(results[i].price);

                    }
                }
            }
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
        }
    },associate.listItemsDiag);

    associate.insertItem = associate.insertItem || {};
    associate.insertItem = $.extend({
        addItem:function(){
            //获取对话框中的itemsList
            var container = $(".tmAlert .itemsList_Area");
            $(".tmAlert .itemsList_Area td").unbind("click").click(function(){
                 var template = $("#setModel");
                 var count = TM.associate.itemQuantity.itemCount(template);
                 var modelId = $("#setModel .template").attr("value");

                 var maxNum = modelId.substring(modelId.length-2,modelId.length);

                 var src = $(this).find(".item_img img").attr("src");
                 var title = $(this).find(".item_title").text();
                 var price = $(this).find(".item_price").text();
                 var origPrice = $(this).find(".item_origPrice").text();
                 var numIid = $(this).find(".item_id").attr('value');

                 if(numIid == null){
                    return false;
                 }
                 else if(!$(this).hasClass("wid-item-box-in") && (count >= maxNum)){
                    alert("选择的宝贝数量最多不能超过" + count + "个");
                    return false;
                 }
                 else if($(this).hasClass("wid-item-box-in")){
                    associate.deleteItem.unbind("click").clickItemDelete($(this));
                    $(this).removeClass("wid-item-box-in");
                 }

                 else{
                    var items = $("#setModel .tmp_td_item");
                    $.each(items,function(){
                        if($(this).find(".tmp_td_item_id").attr('value') == undefined || $(this).find(".tmp_td_item_id").attr('value') == ''){

                            if($(this).find(".tmp_td_item_img").is("img")){
                                $(this).find(".tmp_td_item_img").attr("src",src);
                            }
                            else{
                                $(this).find(".tmp_td_item_img").attr("background",src);
                            }

                            $(this).find(".tmp_td_item_title").html(title);
                            $(this).find(".tmp_td_item_price").html(price);
                            $(this).find(".tmp_td_item_origPrice").html(origPrice);


                            $(this).find(".tmp_td_item_id").attr('value',numIid);
                            $(this).find(".tmp_td_item_href").attr('href','//item.taobao.com/item.htm?id=' + $(this).find(".tmp_td_item_id").attr('value'));
                            $(this).find(".tmp_td_item_href").attr('title',title);

                           if($(this).css("position") == 'static')
                            {
                                $(this).css('position','relative')
                            }
                            $(this).append("<div class='tmp_item_del' style='cursor:pointer;display:none'>"
                                                                + "<a><img src='/public/images/associateModel/delete.png' style='height:40px;width:35px;'/></a></div>");
                            $(this).css('display','block');
                            return false;
                        }

                    });
                    if(!$(this).hasClass("wid-item-box-in")){
                        $(this).addClass("wid-item-box-in");
                    }
                    TM.associate.deleteItem.clickDeleteItem();
                 }
            });
        },

        //关闭窗口执行的方法
        closeWindow:function(planId){
            $("#setModel .to_modify_item").css('visibility','visible');
            $("#setModel .btn_under_template").css('display','block');
            var btn = $("#setModel .go_Next");
            TM.associate.insertItem.clickNext(btn,planId);
        },

        //点击下一步 先保存然后在询问是否现在投放
        clickNext:function(btn,planId){
             btn.unbind("click").click(function(){
                var modelId;
                var itemIids;
                var modelName;
                var areaFlag;
                var flag = true;
                //如果是默认模板
                if(btn.attr('name') == 'defaultNext'){
                    areaFlag = $("#defaultTemplate");
                    modelName = areaFlag.find("input[name='templateName']").val();
                    modelId = areaFlag.find(".template").attr("value");
                    itemIids = TM.associate.insertItem.getItemIids(areaFlag);
                }
                //如果是自定义的
                else if(btn.attr('name') == 'clickNext'){
                    areaFlag = $("#setModel");
                    modelName = areaFlag.find("input[name='templateName']").val();
                    modelId = areaFlag.find(".template").attr("value");
                    itemIids = TM.associate.insertItem.getItemIids(areaFlag);
                }
                var count = TM.associate.itemQuantity.itemCount(areaFlag);
                if(count == 0){
                    alert("请先添加宝贝");
                    flag = false;
                }
                if(!(modelName.length > 0)){
                    alert("请填写模板名称");
                    areaFlag.find(".templateName").focus();
                    flag = false;
                }
                if(modelName.length > 40){
                    alert("名称过长，请重新编辑");
                    areaFlag.find("input").focus();
                    flag = false;
                }
                if(flag){
                    var paramData = TM.associate.saveAssociatePlan.getParams(areaFlag);
                    paramData.planId = planId;
                    $.ajax({
                        url:'/Associate/savePlanId',
                        data:paramData,
                        type:'post',
                        success:function(data){
                            if(data > 0){
//                                TM.associate.showDialog("<span>保存计划成功,是否立即投放？</span>",
//                                        400, 300, function(){TM.associate.insertItem.toPutImmediately(areaFlag,data);}, function(){window.location.href = "/associate/myAssociate?type=2"}, '提示');
                                TM.Alert.load("<div style='height:100%;width:100%;'><span style='font-size:16px;'>计划保存成功,下一步投放模板</span></div>", 400, 300, function(){TM.associate.insertItem.toPutImmediately(areaFlag,data);},function(){return false}, '提示');
                            }
                        }
                    });
                }
             });
        },

        toPutImmediately:function(areaFlag,planId){
            areaFlag.css("display",'none');
            $(".toPutBtn").css('display','block');
            $(".set_step").css('display','block');
            $("#clickShowItems").css('display','block');
            TM.associate.insertItem.clickSetModel();
            TM.associate.mouseOn.mouseOnChoose();
            $(".set_tmp_step").removeClass("set_tmp_step_mouse_in");
            $(".set_tmp_step.set_step2").removeClass("step_h_current");
            $(".set_tmp_step.set_step3").addClass("step_h_current");
            associate.mouseOn.showToPutComment();
            TM.associate.insertItem.clickCustom();
            TM.associate.toPut.putPlanAll(planId);
            TM.associate.toPut.initSearch(planId);
            TM.associate.toPut.putPlan(planId);
        },

        //点击设置模板
        clickSetModel:function(){
            $(".set_step2").unbind("click").click(function(){
                $(".toPutBtn").css('display','none');
                $("#toPutItemList").css('display','none');
                $(this).removeClass("set_tmp_step_mouse_in");
                $(".set_tmp_step").removeClass("step_h_current");
                $(this).addClass("step_h_current");
                $(this).unbind("mouseenter").unbind("mouseleave");
                //有点击过自定义模板
                if($(".template_Msg").find(".tmp_table").attr('value') > 0){
                    $("#setModel").css('display','block');
                }
                else{
                    $("#defaultTemplate").css('display','block');
                }
            });
        },

        //点击自定义
        clickCustom:function(){
            $("#clickShowItems").unbind("click").unbind("click").click(function(){
                $("#toPutItemList").css("display","block");
                $("#clickShowItems").css("display","none");
            });
        },

        getItemIids:function(container){
             var td_items = container.find(".tmp_td_item");
             var arr = new Array();
             $.each(td_items,function(){
                 if(($(this).find(".tmp_td_item_id").attr('value') != null) && ($(this).find(".tmp_td_item_id").attr('value') != "")){
                     arr.push($(this).find(".tmp_td_item_id").attr('value'));
                 }
             });
             var itemIids = arr.join("!@#");
             return itemIids;
        }
    },associate.insertItem);

    // 判断模板中已经存放的宝贝数量
    associate.itemQuantity = associate.itemQuantity || {};
    associate.itemQuantity = $.extend({
        itemCount:function(template){
            //模板中已经存放的个数
            var items = template.find(".tmp_td_item");
            var count = 0;
            $.each(items,function(){
                //有宝贝
                if($(this).find(".tmp_td_item_id").attr('value') > 0 )
                {
                    count ++ ;
                }
            });
            return count;
        }
    },associate.itemQuantity);

    //点击删除按钮
    associate.deleteItem = associate.deleteItem || {};
    associate.deleteItem = $.extend({
        clickDeleteItem:function(){
            $(".tmp_item_del").unbind("click").click(function(){
                //删除
                $(this).find(".tmp_td_item_href").attr('href','javascript:void(0);');
                $(this).find(".tmp_td_item_href").attr("title","");
                $(this).parent().find(".tmp_td_item_id").attr('value','');

                if($(this).parent().find(".tmp_td_item_img").is("img")){
                    $(this).parent().find(".tmp_td_item_img").attr('src','');
                }else{
                    $(this).parent().find(".tmp_td_item_img").attr('background','');
                }

                $(this).parent().find(".tmp_td_item_title").html('');
                $(this).parent().find(".tmp_td_item_price").html('');
                $(this).parent().find(".tmp_td_item_origPrice").html('');


                $(this).parent().find(".tmp_item_del").remove();
                TM.associate.deleteItem.event();
                TM.associate.deleteItem.hideTdItem();
            });
        },

        clickItemDelete:function(container){
            var id = container.find(".item_id").attr('value');
            $("#setModel .tmp_td_item").each(function(){
                if($(this).find(".tmp_td_item_id").attr('value') == id){
                    $(this).find(".tmp_td_item_href").attr('href','javascript:void(0);');
                    $(this).find(".tmp_td_item_href").attr("title",'');
                    $(this).find(".tmp_td_item_id").attr('value','');

                    if($(this).find(".tmp_td_item_img").is("img")){
                        $(this).find(".tmp_td_item_img").attr('src','');
                    }else{
                        $(this).find(".tmp_td_item_img").attr('background','');
                    }

                    $(this).find(".tmp_td_item_title").html('');
                    $(this).find(".tmp_td_item_price").html('');
                    $(this).find(".tmp_td_item_origPrice").html('');

                    $(this).find(".tmp_item_del").remove();
                    TM.associate.deleteItem.event();
                    TM.associate.deleteItem.hideTdItem();
                }
            });
        },
        hideTdItem:function(){
            var tdDivs = $(".template_Msg .tmp_td_item");
            $.each(tdDivs,function(){
                if($(this).find(".tmp_td_item_id").attr("value") > 0){
                    $(this).css("display","block");
                }else{
                    $(this).css("display","none");
                }
            });
        },

        event:function(){
            var arrObj = [];
            $.each($("#setModel .tmp_td_item"),function(i,value){
                if($(this).find(".tmp_td_item_id").attr('value') > 0){
                    var href = $(this).find(".tmp_td_item_href").attr('href');
                    var id = $(this).find(".tmp_td_item_id").attr('value');

                    var src;
                    if($(this).find(".tmp_td_item_img").is("img")){
                        src =  $(this).find(".tmp_td_item_img").attr('src');
                    }else{
                        src =  $(this).find(".tmp_td_item_img").attr('background');
                    }

                    var title = $(this).find(".tmp_td_item_title").html();
                    var price = $(this).find(".tmp_td_item_price").html();
                    var origPrice = $(this).find(".tmp_td_item_origPrice").html();
                    var jsonObj = new Object();
                    jsonObj.href = href;
                    jsonObj.id = id;
                    jsonObj.src = src;
                    jsonObj.title = title;
                    jsonObj.price = price;
                    jsonObj.origPrice = origPrice;

                    arrObj.push(jsonObj);
                }
            });

            //清空数据
            if($("#setModel .tmp_td_item_img").is("img")){
                $("#setModel .tmp_td_item_img").attr('src','');
            }else{
                $("#setModel .tmp_td_item_img").attr('background','');
            }

            $("#setModel .tmp_td_item_title").html('');
            $("#setModel .tmp_td_item_price").html('');
            $("#setModel .tmp_td_item_origPrice").html('');

            $("#setModel .tmp_td_item_id").attr('value','');
            $("#setModel .tmp_td_item_href").attr('href','javascript:void(0);');
            $("#setModel .tmp_td_item_href").attr("title",'');
            $("#setModel .tmp_item_del").remove();

            //读出数据
            for(var i=0;i<arrObj.length;i++ ){
                var items = $("#setModel .tmp_td_item");
                $.each(items,function(i,value){

                    if($(this).find(".tmp_td_item_id").attr('value') == undefined || $(this).find(".tmp_td_item_id").attr('value') == ''){

                        $(this).find(".tmp_td_item_href").attr('href',arrObj[i].href);
                        $(this).find(".tmp_td_item_href").attr("title",arrObj[i].title);
                        $(this).find(".tmp_td_item_id").attr('value',arrObj[i].id);

                        if($(this).find(".tmp_td_item_img").is("img")){
                            $(this).find(".tmp_td_item_img").attr('src',arrObj[i].src);
                        }else{
                            $(this).find(".tmp_td_item_img").attr('background',arrObj[i].src);
                        }

                        $(this).find(".tmp_td_item_title").html(arrObj[i].title);
                        $(this).find(".tmp_td_item_price").html(arrObj[i].price);
                        $(this).find(".tmp_td_item_origPrice").html(arrObj[i].origPrice);


                        $(this).append("<div class='tmp_item_del' style='display:none;'>"
                                       + "<a><img src='/public/images/associateModel/delete.png' style='height:40px;width:35px;'/></a></div>");
                        TM.associate.deleteItem.clickDeleteItem();
                        return false;
                    }
                });
            }
        }
    },associate.deleteItem);


    //鼠标移动点击事件
    associate.mouseOn = associate.mouseOn || {};
    associate.mouseOn = $.extend({

        showToPutComment:function(){
            $("#toPutAllBtn .go_Next").hover(function(){
                $(".toPutAllComments").css('visibility','visible');
            },function(){
                $(".toPutAllComments").css('visibility','hidden');
            });

            $("#clickShowItems .go_Next").hover(function(){
                $(".clickShowItemsComments").css('visibility','visible');
            },function(){
                $(".clickShowItemsComments").css('visibility','hidden');
            });

            $("#toPutCheckedBtn .go_Next").hover(function(){
                $(".toPutCheckedComments").css('visibility','visible');
            },function(){
                $(".toPutCheckedComments").css('visibility','hidden');
            });
        },

        chooseTmpOver:function(){
            $(".template_div").hover(function(){
                $(this).css("background-color","rgb(234, 237, 242);");
            },function(){
                $(this).css("background-color","");
            });
        },
        //鼠标点击选择模板重新选择模板
        mouseOnChoose:function(){
            var defaultArea = $("#defaultTemplate").css("display");
            var setArea = $("#setModel").css("display");
            var toPutArea = $(".toPutBtn").css("display");
            //设置模板
            if(defaultArea == 'block' || setArea == 'block'){
                $(".set_tmp_step.set_step1").hover(function(){
                    $(this).addClass("set_tmp_step_mouse_in");
                },function(){
                    $(this).removeClass("set_tmp_step_mouse_in");
                });
            }
            //投放模板
            if(toPutArea == 'block'){
                $(".set_tmp_step.set_step1").hover(function(){
                    $(this).addClass("set_tmp_step_mouse_in");
                },function(){
                    $(this).removeClass("set_tmp_step_mouse_in");
                });
                $(".set_tmp_step.set_step2").hover(function(){
                    $(this).addClass("set_tmp_step_mouse_in");
                },function(){
                    $(this).removeClass("set_tmp_step_mouse_in");
                });
            }
        },

        mouseOver:function(){
              $(".tmAlert .wid-item-box").hover(function(){
                if($(this).find(".item_id").attr("value") > 0 && !$(this).hasClass("wid-item-box-in")){
                    $(this).addClass("wid-item-box-over");
                    $(this).css("border","1px solid #FF7744");
                }
              },function(){
                    $(this).removeClass("wid-item-box-over");
                    if(!$(this).hasClass("wid-item-box-in")){
                        $(this).css("border","1px solid #fff");
                    }
              });
        },

        toPutmouseOver:function(mouseOverObj){
            mouseOverObj.find(".put_item_div").hover(
                function(){
                    if($(this).find(".item_id").attr("value") > 0  || !$(this).hasClass("putItem_Mouse_In")){
                    $(this).addClass("putItem_Mouse_In");
                    $(this).css("border","1px solid #FF7744");
                    }
                },
                function(){
                    $(this).removeClass("putItem_Mouse_In");
                    if(!$(this).hasClass("putItem_Mouse_Click")){
                        $(this).css("border","1px solid #fff");
                    }
                });
            },

         toPutMouseClick:function(mouseOverObj){
            mouseOverObj.find(".put_item_div").click(function(){
                var numId =  $(this).find(".item_id").attr("value");
                if(!$(this).hasClass("putItem_Mouse_Click")){
                    $(this).addClass("putItem_Mouse_Click");
                    $(this).css("border","1px solid #FF7744");
                    $(this).find("input").attr("checked",true);
                    associate.result.addSelectNumIid(numId);
                }
                else {
                    $(this).removeClass("putItem_Mouse_Click");
                    $(this).find("input").attr("checked",false);
                    $(this).css("border","1px solid #fff");
                    associate.result.removeSelectNumIid(numId);
                }
            });
         }
    },associate.mouseOn);

    associate.result = associate.result || {};
    associate.result = $.extend({

        selectNumIidArray: [],

        getSelectNumIidArray: function() {
            return associate.result.selectNumIidArray;
        },

        isInSelectArray: function(numIid) {
            for (var i = 0; i < associate.result.selectNumIidArray.length; i++) {

                if (associate.result.selectNumIidArray[i] == numIid) {
                    return true;
                }
            }
            return false;
        },

        addSelectNumIid: function(numIid) {
            associate.result.removeSelectNumIid(numIid);
            associate.result.selectNumIidArray[associate.result.selectNumIidArray.length] = numIid;
        },

        removeSelectNumIid: function(numIid) {

            for (var i = 0; i < associate.result.selectNumIidArray.length; i++) {

                if (associate.result.selectNumIidArray[i] == numIid) {
                    associate.result.selectNumIidArray.splice(i, 1);
                    return;
                }

            }

        },

        removeSomeSelectNumIids: function(numIidArray) {

            for (var i = 0; i < numIidArray.length; i++) {
                associate.result.removeSelectNumIid(numIidArray[i]);
            }
        }


    },associate.result);

    //点击返回（返回重新选择模板，保存当前模板
    associate.goBack = associate.goBack ||{};
    associate.goBack = $.extend({
        goBackAndSelect:function(){
            $(".go_back_to_select").click(function(){
                TM.associate.init.doInit();
                 $("#content").show();
                 $("#setModel").css('display','none');
            });
        }
    },associate.goBack);



    //保存模板计划
   associate.saveAssociatePlan = associate.saveAssociatePlan || {};
   associate.saveAssociatePlan = $.extend({

        //获取参数
        getParams:function(templateObj){
            var params = {};
            var planName = templateObj.find("input[name='templateName']").val();
            var arr = new Array();
            var items = templateObj.find(".tmp_td_item");
            $.each(items,function(){
                if( $(this).find(".tmp_td_item_id").attr('value') > 0){
                    arr.push(($(this).find(".tmp_td_item_id").attr('value')));
                }
            });
            var modelId = templateObj.find(".tmp_table").attr('value');
            var itemIds = arr.join("!@#");

            var fontColor;
            $.each(templateObj.find(".pan-fon-col"),function(){
                if($(this).find("span").hasClass("pan-mark")){
                   fontColor = $(this).attr("data");
                }
            });
            var borderColor;
            $.each(templateObj.find(".pan-bor-col"),function(){
                if($(this).find("span").hasClass("pan-mark")){
                    borderColor = $(this).attr("data");
                }
            });
            var backgroundColor;
            $.each(templateObj.find(".pan-bac-col"),function(){
                if($(this).find("span").hasClass("pan-mark")){
                    backgroundColor = $(this).attr("data");
                }
            });
            var planWidth = templateObj.find(".pan-wid-px").attr("data");
            var columnNum = templateObj.find(".pan-bor-col").text();
            var activityTitle = templateObj.find(".pan-act-tit").attr("value");
            var counterPrice = templateObj.find(".pan-cou-pri").attr("value");
            var originalPrice = templateObj.find(".pan-ori-pri").attr("value");
            var activityNameChinese = templateObj.find(".pan-act-nam-chi").attr("value");
            var activityNameEnglish = templateObj.find(".pan-act-nam-eng").attr("value");
            params.planWidth = planWidth;
            params.activityNameEnglish = activityNameEnglish;
            params.activityNameChinese = activityNameChinese;
            params.originalPrice = originalPrice;
            params.counterPrice = counterPrice;
            params.activityTitle = activityTitle;
            params.columnNum = columnNum;
            params.borderColor = borderColor;
            params.fontColor = fontColor;
            params.backgroundColor = backgroundColor;
            params.planName = planName;
            params.itemIds = itemIds;
            params.modelId = modelId;
            return params;
        }
   },associate.saveAssociatePlan);

    //投放
   associate.toPut = associate.toPut || {};
   associate.toPut = $.extend({
        initSearch:function(planId){
            var paramObj = $(".toPutItemListTop .searchParams");
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
//                        option.html(data[i].name+"("+data[i].count+")");
                        option.html(data[i].name);
                        sellerCat.append(option);
                }
                if(!exist){
                    sellerCat.hide();
                }
                TM.associate.toPut.listItems(planId);

                var paramObj = $(".toPutItemListTop .searchParams");
                TM.associate.toPut.doSearch(planId,paramObj);
                TM.associate.toPut.doSort(planId,paramObj);
            });
        },

        listItems:function(planId){
            var paramObj = $(".toPutItemListTop .searchParams");
            var params = TM.associate.listItemsDiag.getParams(paramObj);
            var bottom = $("#toPutItemList .toPutBottom");
            bottom.tmpage({
                currPage: 1,
                pageSize: 15,
                pageCount:1,
                ajax: {
                    on:true,
                    dataType:'json',
                    url:'/Associate/listItemsNoMinPrice',
                    param:params,
                    callback:function(data){
                        if(data != null){
                            if(data.res.length > 0){

                                $(".toPutItemListArea").empty();
                                var tdObjs = $("#toPutItemBox").tmpl(data.res);
                                $.each(tdObjs,function(i,value){
                                    if(tdObjs != null){
                                        if(i % 4 == 0){
                                             $(".toPutItemListArea").append("<tr class='it-li-tr'></tr>");
                                        }
                                        $("tr[class='it-li-tr']:last").append($(this));

                                        //href="//itemF.taobao.com/item.htm?id=123456
                                         var itemId = $(this).find(".item_id").attr('value');
                                         $(this).find(".item-text-href").attr('href','//item.taobao.com/item.html?id='+itemId);

                                         if(associate.result.isInSelectArray(itemId) == true){
                                            $(this).find(".put_item_div").addClass("putItem_Mouse_Click");
                                            $(this).find(".put_item_div").css("border","1px solid #FF7744");
                                            $(this).find("input").attr("checked",true);
                                         }
                                     }
                                });
                            }
                            else{
                               $(".toPutItemListArea").empty();
                               $(".toPutItemListArea").append('<tr><td style="text-align: center;">抱歉，没有找到相关的宝贝<td><tr>');
                            }
                            var toPutObj = $(".toPutItemListArea");

                            TM.associate.mouseOn.toPutmouseOver(toPutObj);

                            TM.associate.mouseOn.toPutMouseClick(toPutObj);

                            TM.associate.toPut.checkOutItem(toPutObj,planId);

                            TM.associate.toPut.onChange(planId);


                        }
                    }
                }
            });
        },

        onChange:function(planId){
            $(".toPutItemListTop .searchSelect").unbind("change").change(function(){
                TM.associate.toPut.listItems(planId);
            });
        },

        doSearch:function(planId,paramObj){
            var searchObj = paramObj.find(".doSearch");
            searchObj.unbind("click").click(function(planId){
                TM.associate.toPut.listItems(planId);
            });
        },

        doSort:function(planId,paramObj){
            var sortObj = paramObj.find("td");
            var saleSortObj = paramObj.find(".saleSort");
            saleSortObj.unbind("click").click(function(){
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
                TM.associate.toPut.listItems(planId);
            });
            var priceSortObj = paramObj.find(".priceSort");
            priceSortObj.unbind("click").click(function(){
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
                TM.associate.toPut.listItems(planId);
            });
        },

        checkOutItem:function(toPutObj,planId){
            var planId = TM.associate.init.getPlanId();
            $.post('/Associate/AssociatedItems',{planId:planId},function(data){
                var itemObj = toPutObj.find(".put_item_div");
                $.each(data,function(i,value){
                    $.each(itemObj,function(){
                        if($(this).find(".item_id").attr('value') == value.id){
                            $(this).addClass("putItem_Mouse_Click");
                            $(this).css("border","1px solid #FB6E52");
                            $(this).find("input").attr('checked',true);
                        }
                        TM.associate.result.addSelectNumIid(value.id);
                    });
                });
            });
        },

        //modelId,modelName,itemIids,planId,null
        putPlanAll:function(planId){
            $("#toPutAllBtn .go_Next").unbind("click").click(function(){
                TM.Alert.showDialog("<span><a>确定要全店铺一键投放？</a></span>", 250, 150, function(){associate.toPut.putPlanToAll(planId)}, function(){return false}, "提示");
            });
        },

        //参数为模板中的Id,模板备注，模板中的宝贝Ids 需要判断使用的是当前的还是默认的
        putPlanToAll:function(planId){
            $.post("/Associate/toPutToAll",{planId:planId},function(data){
                associate.toPut.toPutResultDialog(data,'投放');
            });
        },

        //投放选中的 参数：模板Id 模板中宝贝itemIids 模板的备注
        putPlan:function(planId){
            $("#toPutCheckedBtn").unbind("click").click(function(){
                var arr = associate.result.getSelectNumIidArray();
                TM.Alert.showDialog('<a>确定要投放以下<span class="successNum">' + arr.length + '</span>个宝贝?</a>', 250, 150, function(){associate.toPut.toPutPlanChecked(arr,planId)}, function(){return false}, "提示");
            });
        },

        toPutPlanChecked:function(arr,planId){
            var paramsData = {};
            numIids = arr.join("!@#");
            paramsData.associateIds = numIids;
            paramsData.planId = planId;
            if(numIids.length == 0){
                alert("请选择要投放的宝贝");
                return false;
            }
            else{
                $.ajax({
                    url:"/Associate/toPut",
                    type:"post",
                    data:paramsData,
                    success:function(data){
                        associate.toPut.toPutResultDialog(data,'投放');
                    }
                });
            }
        },

        toPutResultDialog:function(data,actStr){
            if(data != null){
               var num = 0;
               var tableObj=$('<table class="errorTable busSearch" style="text-align: center;"></table>');
               var firstRow = associate.toPut.createFirstRow();
               tableObj.append(firstRow);
               var success = 0;
               var fail = 0;
               $.each(data,function(i,value){
                   if(value.ok){
                       success ++;
                   }else{
                       fail ++;
                       num ++;
                       tableObj.append(associate.toPut.createErrorRow(value,num));
                   }
               });
               if(fail == 0){
                    TM.Alert.load('<div>' + actStr + '成功：<span class="successNum">'+ success +'</span>个</div>',300,250,function(){window.location.href="/associate/myAssociate?type=1"},false,"投放结果");
               }else{
                   var tableDiv=$("<div class='tableDiv'><div style='width: 100%;height: 40px;line-height: 40px;'>" + actStr + "成功个数：<span class='successNum'>"+ success +"</span>" +
                                  "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + actStr + "失败个数：<span class='failNum'>" + fail + "</span></div></div>");
                   tableDiv.append(tableObj);
                   TM.Alert.load(tableDiv,780,600,function(){window.location.href="/associate/myAssociate?type=1"},false,"投放结果");
               }
            }
        },
        createErrorRow:function(errMsg,i){
            if(i%2==0)
            var trObj=$('<tr class="errorContent evenRow"></tr>');
            else var trObj=$('<tr class="errorContent oddRow"></tr>');
            var td1=associate.toPut.createTd1(errMsg.picPath);
            var td2=associate.toPut.createTd2(errMsg.title);
            var td3=associate.toPut.createTd3(errMsg.msg);
            trObj.append(td1);
            trObj.append(td2);
            trObj.append(td3);
            return trObj;
        },
        createFirstRow:function(){
            var firstRow=$('<tr class="firstRow "></tr>');
            var td1=$('<th class="errImg" width="120px">宝贝图片</th>');
            var td2=$('<th class="errTitle">宝贝标题</th>');
            var td3=$('<th class="errCause">错误原因</th>');
            firstRow.append(td1);
            firstRow.append(td2);
            firstRow.append(td3);
            return firstRow;
        },
        createTd1:function(imgPath){
            var tdObj=$('<td class="errImg"></td>');
            var imgObj=$('<img  style="width:50px;height:50px;" />');
            imgObj.attr("src",imgPath);
            tdObj.append(imgObj);
            return tdObj;
        },
        createTd2:function(title){
            var tdObj=$('<td class="errTitle"></td>');
            tdObj.append(title);
            return tdObj;
        },
        createTd3:function(errMsg){
            var tdObj=$('<td class="errCause"></td>');
            tdObj.append(errMsg);
            return tdObj;
        }
   },associate.toPut);


   associate.property = associate.property || {};
   associate.property = $.extend({
        //需要传入的是一个flag 判断是默认模板区域还是设置模板区域
        appendProperty:function(flagArea){
            var modelId = $(".template").attr("value");
            $.post('/Associate/findModelById',{modelId:modelId},function(data){
                $(data).each(function(i, result){
                    if(result.fontColor === undefined || result.fontColor == null || result.fontColor.length == 0) {
                        result.fontColorNotNull = false;
                    } else{
                        var arrFC = result.fontColor.split(",");
                        result.fontColorNotNull = true;
                        result.fontColor = arrFC;
                    }
                    if(result.borderColor === undefined || result.borderColor == null || result.borderColor.length == 0) {
                        result.borderColorNotNull = false;
                    } else{
                        var arrBC = result.borderColor.split(",")
                        result.borderColorNotNull = true;
                        result.borderColor = arrBC;
                    }
                    if(result.backgroundColor === undefined || result.backgroundColor == null || result.backgroundColor.length == 0){
                        result.backgroundColorNotNull = false;
                    }
                    else{
                        var arrBGC = result.backgroundColor.split(",");
                        result.backgroundColorNotNull = true;
                        result.backgroundColor = arrBGC;
                    }
                    if(result.activityTitle === undefined || result.activityTitle == null || result.activityTitle.length == 0) {
                        result.activityTitleNotNull = false;
                    } else{
                        result.activityTitleNotNull = true;
                    }
                    if(result.counterPrice === undefined || result.counterPrice == null || result.counterPrice.length == 0 || result.counterPrice == 0) {
                        result.counterPriceNotNull = false;
                    } else{
                        result.counterPriceNotNull = true;
                    }
                    if(result.originalPrice === undefined || result.originalPrice == null || result.originalPrice.length == 0 || result.originalPrice == 0) {
                        result.originalPriceNotNull = false;
                    } else{
                        result.originalPriceNotNull = true;
                    }
                    if(result.activityNameChinese === undefined || result.activityNameChinese == null || result.activityNameChinese.length == 0) {
                        result.activityNameChineseNotNull = false;
                    } else{
                        result.activityNameChineseNotNull = true;
                    }
                    if(result.activityNameEnglish === undefined || result.activityNameEnglish == null || result.activityNameEnglish.length == 0) {
                        result.activityNameEnglishNotNull = false;
                    } else{
                        result.activityNameEnglishNotNull = true;
                    }
                });
                if(flagArea == 'setModelArea'){
                    $(".setModel_property").empty();
                    var propertyObj = $("#setModel-property").tmpl(data);
                    $(".setModel_property").append(propertyObj);
                }else if(flagArea == 'defaultModelArea'){
                     $(".defaultModel_property").empty();
                    var propertyObj = $("#setModel-property").tmpl(data);
                    $(".defaultModel_property").append(propertyObj);
                }

                var planId = associate.init.getPlanId();
                if(planId > 0){
                    associate.property.modifyAppendProperty(planId);
                }
                associate.property.modifyProperty(flagArea);
            });
        },

        modifyAppendProperty:function(planId){
            $.post("/Associate/findPlanById",{planId:planId},function(data){
                if(data.activityNameChinese != null && data.activityNameChinese != ''){
                    $("#setModel .pan-act-nam-chi").attr('value',data.activityNameChinese);
                }
                if(data.activityNameEnglish != null && data.activityNameEnglish != ''){
                    $("#setModel .pan-act-nam-eng").attr('value',data.activityNameEnglish);
                }
                if(data.activityTitle != null && data.activityTitle != ''){
                    $("#setModel .pan-act-tit").attr('value',data.activityTitle);
                    $("#setModel .tmp_td_act_tit").html(data.activityTitle);
                }
                if(data.planName != null && data.planName != ''){
                    $("#setModel .templateName").attr("value",data.planName);
                }
            });
        },

        modifyProperty:function(flagArea){
            var propertyObj;
            if(flagArea == 'setModelArea'){
                propertyObj = $("#setModel");
            }else if(flagArea == 'defaultModelArea'){
                propertyObj = $("#defaultTemplate");
            }
            var fontColor = propertyObj.find(".tmp_td_fon_col").css("color");

            var backgroundColor = propertyObj.find(".tmp_td_bac_col").css("background-color");

            if(fontColor !== undefined){
                fontColor = associate.property.getHexColor(fontColor);
                $.each(propertyObj.find(".pan-fon-col"),function(){
                    if($(this).attr("data") == fontColor){
                        $(this).find("span").addClass("pan-mark");
                    }
                });
                propertyObj.find(".pan-fon-col").unbind("click").click(function(){
                    var color = $(this).attr("data");
                    propertyObj.find(".tmp_td_fon_col").css("color",color);
                    propertyObj.find(".pan-fon-col").find("span").removeClass("pan-mark");
                    $(this).find("span").addClass("pan-mark");
                });
            }
            //sometimes change the fontColor at the time(tmp_td_bac_col_fon)
            if(backgroundColor !== undefined){
                backgroundColor = associate.property.getHexColor(backgroundColor);
                $.each(propertyObj.find(".pan-bac-col"),function(){
                    if($(this).attr("data") == backgroundColor){
                        $(this).find("span").addClass("pan-mark");
                    }
                });
                propertyObj.find(".pan-bac-col").unbind("click").click(function(){
                    var color = $(this).attr("data");
                    propertyObj.find(".tmp_td_bac_col").css("background-color",color);
                    propertyObj.find(".tmp_td_bac_col_fon").css("color",color);
                    propertyObj.find(" .pan-bac-col").find("span").removeClass("pan-mark");
                    $(this).find("span").addClass("pan-mark");
                });
            }
            propertyObj.find(".pan-act-tit").keyup(function(){
                var activityTitle = propertyObj.find(".pan-act-tit").val();
                propertyObj.find(".tmp_td_act_tit").html(activityTitle);
            });
            propertyObj.find(".pan-act-nam-chi").keyup(function(){
                var activityNameChi = propertyObj.find(".pan-act-nam-chi").val();
                propertyObj.find(".tmp_td_act_nam_chi").html(activityNameChi);
            });
        },

        getHexColor:function(colorObj) {
            var rgb;
            if(colorObj !== undefined && colorObj != null && colorObj !=''){
                rgb = colorObj;
            }

            //如果是一个hex值则直接返回
            if(rgb.indexOf("#") == 0) {
                return rgb;
            }
            else{
                rgb = rgb.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
                function hex(x) {
                    return ("0" + parseInt(x).toString(16)).slice(-2);
                }
                rgb= "#" + hex(rgb[1]) + hex(rgb[2]) + hex(rgb[3]);
                return rgb;
            }
         }
   },associate.property);


   TM.associate.showDialog=function(html, width, height, okCallback, cancelCallback, title){
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
               '立即投放':function(){
                   okCallback && okCallback();
                   $(this).dialog('close');
                   $("body").unmask();
               },
               '下次投放':function(){
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