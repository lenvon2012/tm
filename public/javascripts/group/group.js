    var TM = TM || {};
    ((function($,window){
        TM.group = TM.group || {};
        var group = TM.group || {};
        group.showModel = group.showModel || {};
        group.showModel = $.extend({
            init:function(showFlag){
                var url = window.location.href;
                var index = "?id=".length;
                var idFlag = url.indexOf("?id=");
                var putFlag = url.indexOf("&toput=true");
                var inPut = url.indexOf("&input=true");

                //直接投放
                if(idFlag > 0 && putFlag > 0 && inPut < 0){
                    var planId = url.substring(idFlag + index,putFlag);
                    $.post('/group/getPlan',{planId:planId},function(data){
                        if(data){
                            $(".tmp_set_btn .group_next").attr("pid-data",planId);
                            group.toPut.init();

                            $(".gp-set-step").css("display","block");
                        }
                    });
                }
                //修改投放中的模板
                else if(idFlag > 0 && inPut > 0 && putFlag < 0){
                    var planId = url.substring(idFlag + index,inPut);
                    $.post('/group/showOnePlan',{planId:planId},function(data){
                        $(".tmp_set_area").append(data);
                        $(".tmp_set").css("display","block");
                        $(".tmp_set_top").css("display","none");
                        $(".tmp_set_btn .group_next").attr("pid-data",planId);
                        group.modifyItem.init();
                        group.modifyItem.event();

                        $(".gp-set-step").css("display","block");
                    });
                }
                //修改未投放的模板 和 已結束的模板
                else if (idFlag > 0 && putFlag < 0 && inPut < 0){
                    var planId = url.substring(idFlag + index,url.length);
                    $.post('/group/showOnePlan',{planId:planId},function(data){
                        $(".tmp_set_area").append(data);
                        $(".tmp_set").css("display","block");
                        $(".tmp_set_top").css("display","none");
                        $(".tmp_set_btn .group_next").attr("pid-data",planId);
                        group.modifyItem.init();
                        group.modifyItem.event();

                        $(".gp-set-step").css("display","block");
                    });
                }
                else{
                    group.showModel.doShow(showFlag)
                }

            },

            doShow:function(showFlag){
                group.showModel.doShowModel(showFlag);
            },

            doShowModel:function(showFlag){
                var templateTop = $(".tmp_list_area");
                var bottom = $(".tmp_list_bottom");
                bottom.tmpage({
                    currPage: 1,
                    pageSize:5,
                    pageCount:1,
                    ajax: {
                        param:{showFlag:showFlag},
                        on: true,
                        dataType: 'json',
                        url: "/group/listModels",
                        callback:function(data){
                            $(".tmp_list_area").empty();
                            for(var n=0;n<data.res.length;n++){
                                $.each(data.res[n],function(key,value){
                                    $("#group_tmp_prop").tmpl(value).appendTo(".tmp_list_area");
                                    $(".group_tmp_div_3").eq(n).append(key);
                                });
                            }
                            group.showModel.Event();
                        }
                    }
                });
            },
            Event:function(){
                $(".group_tmp_div").hover(function(){
                    $(this).addClass("group_tmp_div_bg");
                },function(){
                    $(this).removeClass("group_tmp_div_bg");
                });

                $(".group_tmp_div_fav_nor").hover(function(){
                    $(this).removeClass("group_tmp_div_fav_nor");
                    $(this).addClass("group_tmp_div_fav_hov");
                },function(){
                    $(this).removeClass("group_tmp_div_fav_hov");
                    $(this).addClass("group_tmp_div_fav_nor");
                });

                $(".group_tmp_div_fav").hover(function(){
                    var url = window.location.href;
                    if($(this).hasClass("group_tmp_div_fav_tru") && url.indexOf("/group/myFavorite") >= 0){
                        $(this).removeClass("group_tmp_div_fav_tru");
                        $(this).removeClass("group_tmp_div_fav_hov");
                        $(this).addClass("group_tmp_div_fav_cancel");
                    }
                },function(){
                     if($(this).hasClass("group_tmp_div_fav_cancel")){
                        $(this).addClass("group_tmp_div_fav_tru");
                        $(this).removeClass("group_tmp_div_fav_nor");
                        $(this).removeClass("group_tmp_div_fav_cancel");
                     }
                });

                $(".group_tmp_div_fav").unbind("click").click(function(){
                    var btn = $(this);
                    var modelId = $(this).parents(".group_tmp_div").find(".set_begin").attr("modelId");
                    if($(this).hasClass("group_tmp_div_fav_hov")){
                        $.post("/group/favorite",{modelId:modelId},function(data){
                            if(data){
                                btn.removeClass("group_tmp_div_fav_nor");
                                btn.addClass("group_tmp_div_fav_tru");
                            }
                        });
                    }else if($(this).hasClass("group_tmp_div_fav_cancel")){
                        $.post("/group/favoriteCancel",{modelId:modelId},function(data){
                            if(data){
                                btn.parents(".group_tmp_div").css("display","none");
                            }
                        });
                    }
                });

                $(".set_begin").unbind("click").click(function(){
                    var modelId = $(this).attr("modelId");
                    var type = "Default";
                    $.post("/group/selectModel",{modelId:modelId,type:type},function(data){
                        $.each(data,function(key,value){
                            if(key != null && key != ''){
                                $(".tmp_list").empty();
                                $(".tmp_list").css("display","none");
                                $(".tmp_set").css("display","block");
                                $(".tmp_set_area").append(key);
                                group.setModel.init(value,null);
                            }
                        });
                    });
                });
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
                    var planId = url.substring(idFlag + index,putFlag);
                }else if(idFlag > 0 && inPut > 0 && putFlag < 0){
                    var planId = url.substring(idFlag + index,inPut);
                }
                else{
                    var planId = null;
                }
                return planId;
            }
        },group.showModel);

        group.setModel = group.setModel || {};
        group.setModel = $.extend({
            init:function(value,planId){
                TM.group.setModel.appendProp(value);
                TM.group.setModel.appendModelName();

                //不管模板中有几个宝贝，都只给他默认插入一个
                TM.group.setModel.autoInsert();
                TM.group.setModel.event();

                //step show
                $(".gp-set-step").css('display',"block");
                $(".step-one").addClass("step-cur");
            },

            appendModelName:function(){
                $(".tmp_set .templateName").attr("value","淘掌柜团购模板" + new Date().formatYMDMS());
            },

            appendProp:function(value){
                    group.setModel.appendWidth(value);
                    group.setModel.appendColor(value);
                    group.setModel.appendPicSize(value);
                    group.setModel.appendMaxSize(value);
                    group.setModel.appendDay();
                    group.setModel.appendHour();
                    group.setModel.appendMinute();
                    group.setModel.appendActivityTitle();
                    group.setModel.appendLabel();
            },
            appendWidth:function(value){
                if(value.width != null && value.color == null){
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">模板尺寸:</td><td class="g_pro_td_2"></td><td class="g_pro_td_p"></td></tr>');
                    var width = value.width.split(",");
                    $.each(width,function(i,value){
                            $(".g_pro_td_p").append('<a class="g_pro_a g_pro_off" data="' + value + '"><span>' + value + '像素</span></a>');
                    });
                    var modelSize = $(".tmp_set_area").find(".tmp_table").attr('width');
                    $.each($(".g_pro_td_p a"),function(){
                        if($(this).attr("data") == modelSize){
                            $(this).removeClass("g_pro_off");
                            $(this).addClass("g_pro_on");
                        }
                    })
                }
            },
            appendColor:function(value){
                if(value.color != null){
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">模板顏色:</td><td class="g_pro_td_2"></td><td class="g_pro_td_c"></td></tr>');
                    var color = value.color.split(",");
                    $.each(color,function(i,value){
                        $(".g_pro_td_c").append('<a class="g_pro_a" style="background-color:' + value + '" data="' + value + '"><span></span></a>');
                    });
                    var modelColor = $(".tmp_set_area").find(".template").attr('data');
                    $.each($(".g_pro_td_c a"),function(){
                        if($(this).attr("data") == modelColor){
                            $(this).removeClass("g_pro_choosed");
                            $(this).addClass("g_pro_choosed");
                        }
                    })
                }
            },
            appendPicSize:function(value){
                if(value.itemPicSize != null){
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">图片尺寸:</td><td class="g_pro_td_2"></td><td class="g_pro_td_s"></td></tr>');
                    $(".g_pro_td_s").append('<a class="g_pro_a g_pro_on" style="width:40px;"><span>' + value.itemPicSize + '</span></a>');
                }
            },
            appendMaxSize:function(value){
                if(value.maxNum != null){
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">宝贝总数:</td><td class="g_pro_td_2"></td><td class="g_pro_td_n"></td></tr>');
                    $(".g_pro_td_n").append('<a><span>' + value.maxNum + '</span></a>');
                }
            },
            appendDay:function(){
                var day = $(".tmp_set_area").find(".group_days:first").text();
                if(day !== undefined && day != "" && day != null){
                     var days = $.trim(day);
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">剩余天数:</td><td class="g_pro_td_2"></td><td class="g_pro_td_day"></td></tr>');
                    $(".g_pro_td_day").append('<input type="text" class="g_pro_td_input" value="' + days + '">');
                };
            },
            appendHour:function(){
                var hour = $(".tmp_set_area").find(".group_hours:first").text();
                if(hour !== undefined && hour != "" && hour != null){
                    var hours = $.trim(hour);
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">剩余小时:</td><td class="g_pro_td_2"></td><td class="g_pro_td_hour"></td></tr>');
                    $(".g_pro_td_hour").append('<input type="text" class="g_pro_td_input" value="' + hours + '">');
                }
            },
            appendMinute:function(){
                var minute = $(".tmp_set_area").find(".group_minutes:first").text();
                if(minute !== undefined && minute != "" && minute != null){
                    var minutes = $.trim(minute);
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">剩余分钟:</td><td class="g_pro_td_2"></td><td class="g_pro_td_minute"></td></tr>');
                    $(".g_pro_td_minute").append('<input type="text" class="g_pro_td_input" value="' + minutes + '">');
                }
            },
            appendActivityTitle:function(){
                var activityTitle = $(".tmp_set_area").find(".group_activityTitle:first").text();
                if(activityTitle != "" && activityTitle != null && activityTitle !== undefined){
                    var activityTitles = $.trim(activityTitle);
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1">活动标题:</td><td class="g_pro_td_2"></td><td class="g_pro_td_activityTitle"></td></tr>');
                    $(".g_pro_td_activityTitle").append('<input type="text" class="g_pro_td_input" value="' + activityTitles + '">');
                }
            },
            appendLabel:function(){
                var label = $(".tmp_set_area").find(".group_label:first").text();
                if(label != "" && label != null && label !== undefined){
                    var labels = $.trim(label);
                    $(".tmp_set_prop_tab").append('<tr><td class="g_pro_td_1 ">活动标签:</td><td class="g_pro_td_2"></td><td class="g_pro_td_label"></td></tr>');
                    $(".g_pro_td_label").append('<input type="text" class="g_pro_td_input" value="' + label + '">');
                }
            },

            autoInsert:function(){
                $.post("/group/autoInsert",function(data){
                    //no matter how  insert One
                    $.each($(".group_item"),function(i,value){
                        if(i == 0){
                            if($(this).find(".group_image").is("img")){
                                $(this).find(".group_image").attr("src" ,data.picURL);
                            }else{
                                $(this).find(".group_image").attr("background" ,data.picURL);
                            }

                            $(this).find(".group_image").attr("data" ,data.id);

                            $(this).find(".group_title").html(data.title);
                            $(this).find(".group_href").attr("href","http://item.taobao.com/item.htm?id=" + data.id);
                            $(this).find(".group_href").attr("title", data.title);
                            $(this).find(".group_price").html(data.price);
                            $(this).find(".group_buy").attr("href","http://item.taobao.com/item.htm?id=" + data.id);

                            //折扣价
                            if(data.minPrice == 0){
                                $(this).find(".group_minPrice").html(data.price);
                                $(this).find(".group_discount").html(10);
                            }else{
                                $(this).find(".group_minPrice").html(data.minPrice);
                                $(this).find(".group_discount").html((data.minPrice * 10 / data.price).toFixed(1));
                            }

                            //差價
                            var dif;
                            if(data.minPrice == 0){
                                dif = 0;
                            }else{
                                dif = data.price - data.minPrice;
                            }
                            $(this).find(".group_save").html((dif).toFixed(2));
                        }else{
                            return false;
                        }
                    });

                })
            },

            event:function(){
                //width
                $(".g_pro_td_p a").unbind("click").click(function(){
                    var modelId = $(".template").attr("value");
                    var type = $(this).attr("data");
                    group.setModel.changeModel(modelId,type);
                });
                //color
                $(".g_pro_td_c a").unbind("click").click(function(){
                    var modelId = $(".template").attr("value");
                    var type = $(this).attr("data");
                    group.setModel.changeModel(modelId,type);
                });

                $(".g_pro_td_day input").keyup(function(){
                    $(".tmp_set_area .group_days").html($(this).val());
                });

                $(".g_pro_td_hour input").keyup(function(){
                    $(".tmp_set_area .group_hours").html($(this).val());
                });

                $(".g_pro_td_minute input").keyup(function(){
                    $(".tmp_set_area .group_minutes").html($(this).val());
                });

                $(".g_pro_td_activityTitle input").keyup(function(){
                    $(".tmp_set_area .group_activityTitle").html($(this).val());
                });

                $(".g_pro_td_label input").keyup(function(){
                    $(".tmp_set_area .group_label").html($(this).val());
                });

                $(".tmp_set_top .group_modify").unbind("click").click(function(){
                    group.itemDialog.appendSellerCat();
                });

                //下一步
                $(".tmp_set_btn .group_next").unbind("click").click(function(){
                    if(group.setModel.ifLackItem()){
                        var count = group.setModel.itemCount();
                        TM.Alert.showDialog('<div class="gp-dia-msg">确定选择以上<span class="gp-num-blue">' + count +'</span>个宝贝？</div>',250,200,function(){TM.group.setModel.nextToModfifyItems($(this));},function(){return false;},"提示");
                    }
                });
            },
            nextToModfifyItems:function(obj){
                var params = group.params.getPlanParams();
                if(params == null || params == ""){
                    return false;
                }
                var planId = obj.attr("pid-data");
                if(planId > 0){
                    params.planId = planId;
                }
                $.ajax({
                    url:'/group/savePlan',
                    data:params,
                    type:'post',
                    success:function(data){
                        if(data > 0){
                            group.modifyItem.init();

                            //bind planId to
                            $(".tmp_set_btn .group_next").attr("pid-data",data);

                            //hide tmp_set_top
                            $(".tmp_set_top").css('visibility','hidden');
                        }
                    }
                });
            },


            changeModel:function(modelId,type){
                $.post("/group/selectModel",{modelId:modelId,type:type},function(data){
                    $.each(data,function(key,value){
                        if(key != null && key != ''){
                            $(".tmp_set_area").empty();
                            $(".tmp_set_prop_tab").empty();
                            $(".tmp_set_area").append(key);
                            group.setModel.init(value,null);
                        }
                    });
                })
            },

            itemCount:function(){
                var count = 0 ;
                $.each($(".group_item"),function(){
                    var id = $(this).find(".group_image").attr('data');
                    if(id !== undefined && id != ""){
                        count ++;
                    }
                });
                return count;
            },

            ifLackItem:function(){
                var flag = true;
                var count = group.setModel.itemCount();
                var modelId = $(".tmp_table").attr('value');
                var maxNum = modelId.substring(modelId.length-1,modelId.length);
                if(count != maxNum){
                    TM.Alert.load('<div class="gp-dia-msg">还缺少<span class="gp-num-red">' + (maxNum-count) + '</span>个宝贝,请先添加</div>',250,200,function(){return false},function(){return false},"提示");
                    flag = false;
                }
                return flag;
            },

            stepEvent:function(){
                $(".step-one").unbind("click").click(function(){
                    //設置模板
                    if($(".tmp_set_prop").css("display") == "none" && $(".g_pro_td_n").text() != null && $(".g_pro_td_n").text() != ""){
                        $(".tmp_set").css('display',"block");
                        $(".tmp_set_btn").css("display","block");
                        $(".tmp_set_prop").css("display","block");
                        $(".item_set_btn").css("display","none");
                        $(".item_set_prop").css("display","none");
                        $(".tmp_set_top").css("visibility","visible");
                        $(".tmp_toPut").css("display","none");

                        $(".set-step").removeClass("step-cur");
                        $(".set-step").removeClass("step-fixed");
                        $(".set-step b").removeClass("step-tag");

                        $(".step-one").addClass("step-cur");
                    }
                });

                $(".step-two").unbind("click").click(function(){
                    if($(".tmp_set").css("display") == "none" && $(".template").attr("value") !== undefined ){
                        $(".set-step").removeClass("step-cur");
                        $(".set-step").removeClass("step-fixed");
                        $(".set-step b").removeClass("step-tag");

                        $(".step-one").addClass("step-fixed");
                        $(".step-one b").addClass("step-tag");
                        $(".step-two").addClass("step-cur");

                        $(".tmp_toPut").css('display',"none");
                        $(".tmp_set").css("display","block");
                    }
                });
            }
        },group.setModel);


        group.modifyItem = group.modifyItem || {};
        group.modifyItem = $.extend({
            init:function(){
                $(".item_set_prop").empty();
                group.modifyItem.appendItemProp();
//                $(".tmp_set_btn").animate({opacity: "hide"},"slow");
//                $(".tmp_set_prop").animate({opacity: "hide"},"slow");
                $(".tmp_set_btn").hide();
                $(".tmp_set_prop").hide();

                $(".item_set_btn").css('display',"block");
                $(".item_set_prop").css("display","block");

                //step
                $(".step-one").removeClass("step-cur");
                $(".step-one").addClass("step-fixed");
                $(".step-one b").addClass("step-tag");
                $(".step-two").addClass("step-cur");
                group.setModel.stepEvent();

            },

            appendItemProp:function(){
                var itemArr = group.params.getItemPrams();

                var itemObj = $("<div></div>");

                for(var i=0;i<itemArr.length;i++){
                    itemObj.append($("#group_item_prop").tmpl(itemArr[i]));
                }
                  //why not!!!!!!!!!!!!!
    //            var itemPropObjs = $("#group_item_prop").tmpl(itemArr);

                $(".item_set_prop").append(itemObj);
                group.modifyItem.event();
            },

            event:function(){
                $(".group_item_title").keyup(function(){
                    var id = $(this).parents(".group_item_prop").find(".group_item_image").attr("data");
                    var data = $(this).val();
                    group.modifyItem.modifyItem(data,id,".group_title");
                });

                $(".group_item_count").keyup(function(){
                    var id = $(this).parents(".group_item_prop").find(".group_item_image").attr("data");
                    var data = $(this).val();
                    group.modifyItem.modifyItem(data,id,".group_count");
                });

                $(".group_item_price").keyup(function(){
                    var id = $(this).parents(".group_item_prop").find(".group_item_image").attr("data");
                    var data = $(this).val();
                    group.modifyItem.modifyItem(data,id,".group_price");

                });
                $(".group_item_minPrice").one("focus",function(){
                    var defaultValue = $(this).val();
                    var minPriceObj = $(this);
                    $(".group_item_minPrice").keyup(function(){
                        var id = $(this).parents(".group_item_prop").find(".group_item_image").attr("data");
                        var data = $(this).val();
                        if(defaultValue == data){
                            group.modifyItem.modifyItem(data,id,".group_minPrice");
                            $(".g_pro_in_msg").html("");
                        }else{
                            //提示
                            $(".g_pro_in_msg").html("提示:折后价与当前填写价格不同*");
                        }

                    });
                });

                $(".item_set_btn .item_next").unbind("click").click(function(){
                    var itemArr = group.params.getItemParamsFinal();
                    if(itemArr == null || itemArr == ""){
                         return false;
                    }
                    var itemString = itemArr.join(",");
                    itemString = "[" + itemString + "]";
                    //TODO IE6 IE7 ?
//                    var itemString = JSON.stringify(itemArr);

                    var planId = $('.tmp_set_btn .group_next').attr("pid-data");
                    if(planId === undefined || planId == "" || planId == null){
                         alert("参数错误 请联系客服 -4");
                         return false;
                    }
                    $.post("/group/saveItemProp",{itemString:itemString,planId:planId},function(data){
                        if(data == planId){
                            group.toPut.init();
                        }
                        else{
                            alert("参数错误 请联系客服 -5");
                        }
                    });
                });
            },

            modifyItem:function(data,id,tag){
                $.each($(".group_item"),function(){
                    if($(this).find(".group_image").attr('data') == id){
                        $(this).find(tag).html(data);
                        if(tag == ".group_price" || tag == ".group_minPrice"){
                            var price = $(this).find(".group_price").text();
                            var minPrice = $(this).find(".group_minPrice").text();
                            $(this).find(".group_discount").html((minPrice * 10 / price).toFixed(1));
                            var dif ;
                            if(minPrice == 0){
                                dif = 0;
                            }else{
                                dif = price - minPrice;
                            }
                            $(this).find(".group_save").html((dif).toFixed(2));
                        }
                    }
                });
            }
        },group.modifyItem);


        group.itemDialog = group.searchPanel || {};
        group.itemDialog = $.extend({

            initDialog:function(){
                var dialog = $('<div class="tmp_set_dialog"></div>');
                dialog.append('<table class="tmp_set_dia_pan"><tr></tr></table>');
                dialog.append('<table class="tmp_set_dia_area"><tr></tr></table>');
                dialog.append('<div class="tmp_set_dia_bottom"></div>');
                return dialog;
            },

            initPanel:function(){
                var dialog = group.itemDialog.initDialog();
                dialog.find(".tmp_set_dia_pan tr").empty();
                dialog.find(".tmp_set_dia_pan tr").append('<td><input class="group_search_input_wrap" placeholder="请输入关键字" default="请输入关键字" type="text"></td><td class="group_td_gap"></td>');
                dialog.find(".tmp_set_dia_pan tr").append('<td><select name="selectCat" value="selectCat" class="searchSelect"></select></td><td class="group_td_gap"></td>');
                dialog.find(".tmp_set_dia_pan tr").append('<td><a class="group_sort sale_sort">销量</a></td><td class="group_td_gap"></td>');
                dialog.find(".tmp_set_dia_pan tr").append('<td><a class="group_sort price_sort">价格</a></td><td class="group_td_gap"></td>');
                dialog.find(".tmp_set_dia_pan tr").append('<td class="dialog_btn doSearch"><a style="color:#fff;">搜索</a></td>');
                return dialog;
            },

            appendSellerCat:function(){
                var dialog = group.itemDialog.initPanel();
                var sellerCatObj = dialog.find(".searchSelect");
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
                    group.itemDialog.showDialog(dialog);
                    group.itemDialog.listItems();
                });
            },

            listItems:function(){
                var panelObj = $(".tmAlert .tmp_set_dia_pan");
                var params = group.params.getSearchParams(panelObj);
                var listBottomObj = $(".tmAlert").find(".tmp_set_dia_bottom");
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
                            var diaAreaObj = $(".tmAlert").find(".tmp_set_dia_area");
                            if(data.res.length > 0){
                                group.itemDialog.appendItemBoxes(diaAreaObj,data.res);
                                group.itemDialog.event();
                            }else{
                                diaAreaObj.empty();
                                diaAreaObj.append('<tr><td style="text-align: center;width:880px;">抱歉，没有找到相关的宝贝<td><tr>');
                            }
                        }
                    }
                });
            },

            appendItemBoxes:function(diaAreaObj,data){
                diaAreaObj.empty();
                var dialogItemBoxes = $("#dialogItemBox").tmpl(data);
                $.each(dialogItemBoxes,function(i,value){
                    if(i % 5 == 0){
                        diaAreaObj.append('<tr class="dia-item-tr"></tr>');
                    }
                    diaAreaObj.find("tr[class='dia-item-tr']:last").append($(this));
                    group.itemDialog.addClass($(this));
                });
                return diaAreaObj;
            },

            addClass:function(tableObj){
                var idsArr = group.itemDialog.getIdsArr();
                for(var i =0 ;i <idsArr.length ; i++){
                    if(tableObj.find(".dia-item-img").attr('data') == idsArr[i]){
                        tableObj.find("table").addClass("item-box-fix");
                        tableObj.find("table").css("border","1px solid #FF7744");
                    }
                }
            },

            getIdsArr:function(){
                var idsArr = new Array();
                var itemObjs = $(".tmp_set_area").find(".group_item");
                $.each(itemObjs,function(){
                    if($(this).find(".group_image").attr("data") !== undefined){
                        idsArr.push($(this).find(".group_image").attr("data"));
                    }
                });
                return idsArr;
            },

            showDialog:function(diaAreaObj){
                var html = diaAreaObj.html();
                TM.Alert.load(html, 880,800, function(){return false;},function(){return false;},"点击图片选择宝贝,再次点击取消选择,点击标题查看宝贝");
            },


            event:function(){
                $(".tmAlert .doSearch").unbind("click").click(function(){
                    group.itemDialog.listItems();
                });

                $(".tmAlert .searchSelect").unbind("change").change(function(){
                    group.itemDialog.listItems();
                });

                $(".tmAlert .dia-item-td table").hover(function(){
                    if(!$(this).hasClass("item-box-fix")){
                        $(this).addClass("item-box-in");
                        $(this).css("border","1px solid #ff7744");
                    }
                },function(){
                    if(!$(this).hasClass("item-box-fix")){
                        $(this).removeClass("item-box-in");
                        $(this).css("border","1px solid #fff");
                    }
                });


                $(".tmAlert .dia-item-td table").unbind("click").click(function(){
                    var numIid = $(this).find(".dia-item-img").attr('data');
                    if($(this).hasClass("item-box-fix")){
                        $(this).removeClass('item-box-fix');
                        $(this).css("border","1px solid #fff");
                        group.itemDialog.removeItem(numIid);
                        group.itemDialog.reorder();
                    }
                    else if(!$(this).hasClass("item-box-fix") && !group.itemDialog.ifFullItem()){
                         return false;
                    }
                    else{
                        $(this).addClass("item-box-fix");
                        $(this).css("border","1px solid #ff7744");
                        group.itemDialog.reorder();
                        group.itemDialog.addItem(numIid);
                    }
                });

                $(".tmAlert .sale_sort").unbind("click").click(function(){
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
                    $(".tmAlert .price_sort").html("价格");
                    $(".tmAlert .price_sort").removeClass("price_sort_down");
                    $(".tmAlert .price_sort").removeClass("price_sort_up");
                    group.itemDialog.listItems();
                });


                $(".tmAlert .price_sort").unbind("click").click(function(){
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
                    $(".tmAlert .sale_sort").html("销量");
                    $(".tmAlert .sale_sort").removeClass("sales_sort_down");
                    $(".tmAlert .sale_sort").removeClass("sales_sort_up");
                    group.itemDialog.listItems();
                });
            },


            removeItem:function(numIid){
                $.each($(".group_item"),function(){
                    if($(this).find(".group_image").attr("data") == numIid){
                        $(this).find(".group_href").attr("src","javascript:void(0);");
                        $(this).find(".group_href").attr("title","");
                        $(this).find(".group_image").attr("data","");

                        if($(this).find(".group_image").is('img')){
                            $(this).find(".group_image").attr("src","");
                        }else{
                            $(this).find(".group_image").attr("background","");
                        }

                        $(this).find(".group_title").html("");
                        $(this).find(".group_minPrice").html("");
                        $(this).find(".group_price").html("");
                        $(this).find(".group_discount").html("");
                        $(this).find(".group_save").html("");
                        return false;
                    }
                });
            },

            addItem:function(numIid){
                $.post("/group/getOneItem",{numIid:numIid},function(data){
                    $.each($(".group_item"),function(){
                        var id = $(this).find(".group_image").attr("data");
                        if(id === undefined || id == "" || id == null){
                            if($(this).find(".group_image").is('img')){
                                $(this).find(".group_image").attr("src" ,data.picURL);
                            }else{
                                $(this).find(".group_image").attr("background" ,data.picURL);
                            }

                            $(this).find(".group_image").attr("data" ,data.id);

                            $(this).find(".group_title").html(data.title);
                            $(this).find(".group_href").attr("href","http://item.taobao.com/item.htm?id=" + data.id);
                            $(this).find(".group_href").attr("title", data.title);
                            $(this).find(".group_price").html(data.price);
                            $(this).find(".group_buy").attr("href","http://item.taobao.com/item.htm?id=" + data.id);

                            //折扣价
                            if(data.minPrice == 0){
                                $(this).find(".group_minPrice").html(data.price);
                                $(this).find(".group_discount").html(10);
                            }else{
                                $(this).find(".group_minPrice").html(data.minPrice);
                                $(this).find(".group_discount").html((data.minPrice * 10 / data.price).toFixed(1));
                            }

                            //差價
                            if(data.minPrice == 0){
                                dif = 0;
                            }else{
                                dif = data.price - data.minPrice;
                            }
                            $(this).find(".group_save").html((dif).toFixed(2));
                            return false;
                        }
                    });
                });
            },

            reorder:function(){
                var itemsArr = group.itemDialog.getAllItems();
                group.itemDialog.clearAll();
                for(var i = 0 ;i<itemsArr.length;i++){
                    $(".group_item").eq(i).find(".group_href").attr("href","http://item.taobao.com/item.htm?id=" + itemsArr[i].id);
                    $(".group_item").eq(i).find(".group_href").attr("title",itemsArr[i].title);

                    if($(".group_item").eq(i).find(".group_image").is("img")){
                        $(".group_item").eq(i).find(".group_image").attr("src",itemsArr[i].image);
                    }else{
                        $(".group_item").eq(i).find(".group_image").attr("background",itemsArr[i].image);
                    }

                    $(".group_item").eq(i).find(".group_image").attr("data",itemsArr[i].id);
                    $(".group_item").eq(i).find(".group_title").html(itemsArr[i].title);
                    $(".group_item").eq(i).find(".group_minPrice").html(itemsArr[i].minPrice);
                    $(".group_item").eq(i).find(".group_price").html(itemsArr[i].price);
                    $(".group_item").eq(i).find(".group_discount").html(itemsArr[i].discount);
                    $(".group_item").eq(i).find(".group_save").html(itemsArr[i].save);
                }
            },

            getAllItems:function(){
                var itemObjs = $(".group_item");
                var itemsArr = [];
                $.each(itemObjs,function(){
                    var item = {};
                    var id = $(this).find(".group_image").attr("data");
                    if(id !== undefined && id != "" && id != null){
                        item.id = id;

                        if($(this).find(".group_image").is("img")){
                            item.image = $(this).find(".group_image").attr("src");
                        }else{
                            item.image = $(this).find(".group_image").attr("background");
                        }

                        item.title = $(this).find(".group_title").text();
                        item.minPrice = $(this).find(".group_minPrice").text();
                        item.price = $(this).find(".group_price").text();
                        item.discount = $(this).find(".group_discount").text();
                        item.save = $(this).find(".group_save").text();
                        itemsArr.push(item);
                    }
                });
                return itemsArr;
            },

            clearAll:function(){
                    $(".group_item").find(".group_href").attr("src","javascript:void(0);");
                    $(".group_item").find(".group_href").attr("title","");
                    $(".group_item").find(".group_image").attr("data","");
                    if($(".group_item").find(".group_image").is("img")){
                        $(".group_item").find(".group_image").attr('src',"http://img02.taobaocdn.com/imgextra/i2/79742176/TB2V4YNXVXXXXcgXXXXXXXXXXXX-79742176.png");
                    }else{
                        $(".group_item").find(".group_image").attr('background',"http://img02.taobaocdn.com/imgextra/i2/79742176/TB2V4YNXVXXXXcgXXXXXXXXXXXX-79742176.png");
                        $(".group_item").find(".group_image").css('background-repeat',"no-repeat");
                        $(".group_item").find(".group_image").css('background-position',"50% 50%");
                    }

                    $(".group_item").find(".group_title").html("");
                    $(".group_item").find(".group_minPrice").html("");
                    $(".group_item").find(".group_price").html("");
                    $(".group_item").find(".group_discount").html("");
                    $(".group_item").find(".group_save").html("");
            },

            ifFullItem:function(){
                var flag = true;
                var count = group.setModel.itemCount();
                var modelId = $(".tmp_table").attr('value');
                var maxNum = modelId.substring(modelId.length-1,modelId.length);
                if(count >= maxNum){
                    alert("选择的宝贝数量最多不能超过" + count + "个");
                    flag = false;吃哦愛國
                }
                return flag;
            }

        },group.itemDialog);


        group.toPut = group.toPut || {};
        group.toPut = $.extend({

            init:function(){
                group.setModel.stepEvent();
                $(".step-one").addClass("step-fixed");
                $(".step-one b").addClass("step-tag");
                $(".step-two").removeClass("step-cur");
                $(".step-two").addClass("step-fixed");
                $(".step-two b").addClass("step-tag");
                $(".step-three").addClass("step-cur");
                $(".tmp_toPut").empty();
                $(".tmp_toPut").css('display',"block");


                $(".tmp_set").css("display","none");
                $(".tmp_toPut").append('<div class="group_btn group_btn_tp tp_allShop"><span>一键全店投放</span></div>' +
                                            '<div class="gp_tp_coms tp_all_tag" style="visibility: hidden;">点击全店投放,将这个模板投放到店铺内<strong>所有已上架</strong>宝贝中</div>');
                $(".tmp_toPut").append('<div class="group_btn group_btn_tp tp_showItems"><span>自定义投放</span></div>' +
                                            '<div class="gp_tp_coms tp_che_tag" style="display: none;">点击自定义投放,将显示可投放的宝贝列表信息</div>');
                $(".tmp_toPut").append('<table class="tmp_toPut_top"><tr></tr></table>');
                $(".tmp_toPut").append('<table class="tmp_toPut_area"></table>');
                $(".tmp_toPut").append('<table class="tmp_toPut_bottom"></table>');
                group.toPut.event();
            },

            event:function(){
                $(".tmp_toPut .tp_allShop").unbind("click").click(function(){
                    $.post("/group/getItemCount",function(data){
                        if(data > 50){
                            TM.Alert.showDialog("<span><a>确定要全店铺一键投放？</a></span>", 250, 150, function(){TM.group.toPut.putAll();}, function(){return false}, "提示");
                        }else{
                            TM.Alert.showDialog("<span><a>确定要全店铺一键投放？</a></span>", 250, 150, function(){TM.group.toPut.putAllNoQueue();}, function(){return false}, "提示");
                        }
                    });
                });

                $(".tmp_toPut .tp_checked").unbind("click").click(function(){
                    var arr = group.result.getSelectNumIidArray();
                    if(arr.length == 0){
                        TM.Alert.load("请选择宝贝",250,150,function(){return false},"提示");
                        return false;
                    }
                    if(arr.length > 50){
                        TM.Alert.showDialog('<a>确定要投放以下<span class="successNum">' + arr.length + '</span>个宝贝?</a>',250,150,function(){TM.group.toPut.putChecked();},function(){return false},"提示");
                    }else{
                        TM.Alert.showDialog('<a>确定要投放以下<span class="successNum">' + arr.length + '</span>个宝贝?</a>',250,150,function(){TM.group.toPut.putCheckedNoQueue();},function(){return false},"提示");
                    }
                });

                $(".tmp_toPut .tp_showItems").unbind("click").click(function(){
                    group.toPut.showItemList();
                });

                $(".tmp_toPut .doSearch").unbind("click").click(function(){
                    group.toPut.listItems();
                });

                $(".tmp_toPut .searchSelect").unbind("change").change(function(){
                    group.toPut.listItems();
                });

                $(".tmp_toPut .put_item_div").hover(function(){
                    if(!$(this).hasClass("item-box-fix")){
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
                    if($(this).hasClass("item-box-fix")){
                        $(this).removeClass("item-box-fix");
                        $(this).css("border","1px solid #fff");
                        $(this).find("input").attr("checked",false);
                        group.result.removeSelectNumIid(numIid);
                    }else{
                        $(this).addClass("item-box-fix");
                        $(this).css("border","1px solid #ff7744");
                        $(this).find("input").attr("checked",true);
                        group.result.addSelectNumIid(numIid);
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
                    group.toPut.listItems();
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
                    group.toPut.listItems();
                });

                $(".tmp_toPut .tp_allShop").hover(function(){
                    $(".tp_all_tag").css("visibility","visible");
                },function(){
                    $(".tp_all_tag").css("visibility","hidden");
                });

                $(".tmp_toPut .tp_showItems").hover(function(){
                    $(".tp_che_tag").css("display","block");
                },function(){
                    $(".tp_che_tag").css("display","none");
                });

            },

            putAll:function(){
                var planId = $(".tmp_set_btn .group_next").attr("pid-data");

                if(planId !== undefined && planId != ""){
                    $.post("/group/putAll",{planId:planId},function(data){
                        if(data){
                            TM.group.showDialog("已在后台幫您投放，请稍后",350,300,function(){window.location.href="/group/groupInput"},
                                                                    function(){window.location.href="/group/groupAll"},"提示");
                        }
                    });
                }else{
                    alert("参数错误 请联系客服 -6");
                }
            },

            putAllNoQueue:function(){
                var planId = $(".tmp_set_btn .group_next").attr("pid-data");
                if(planId !== undefined && planId != ""){
                    $.post("/group/putAllNoQueue",{planId:planId},function(data){
                        TM.showResult.result.toPutResultDialog(data,"投放","/group/groupInput","/group/groupInput");
                    });
                }else{
                    alert("参数错误 请联系客服 -7");
                }
            },

            putChecked:function(){
                var planId = $(".tmp_set_btn .group_next").attr("pid-data");
                if(planId !== undefined && planId != ""){
                    var numIidsString = TM.group.result.getSelectNumIidArray().join("!@#");
                    $.post("/group/putchecked",{planId:planId,numIidsString:numIidsString},function(data){
                        if(data){
                            TM.group.showDialog("已在后台幫您投放，请稍后",350,300,function(){window.location.href="/group/groupInput"},
                                        function(){window.location.href="/group/groupAll"},"提示");
                        }
                    });
                }
                else{
                    alert("参数错误 请联系客服 -8");
                }
            },
            putCheckedNoQueue:function(){
                var planId = $(".tmp_set_btn .group_next").attr("pid-data");
                if(planId !== undefined && planId != ""){
                    var numIidsString = TM.group.result.getSelectNumIidArray().join("!@#");
                    $.post("/group/putCheckedNoQueue",{planId:planId,numIidsString:numIidsString},function(data){
                        if(data){
                            TM.showResult.result.toPutResultDialog(data,'投放','/group/groupInput',null);
                        }
                    });
                }
                else{
                    alert("参数错误 请联系客服 -9");
                }
            },

            showItemList:function(){
                group.toPut.initPanel();
                group.toPut.appendSellerCat();
            },

            initPanel:function(){
                var panel = $(".tmp_toPut_top tr");
                panel.empty();
                panel.append('<td><input class="group_search_input_wrap" placeholder="请输入关键字" default="请输入关键字" type="text"></td><td class="group_td_gap"></td>');
                panel.append('<td><select name="selectCat" value="selectCat" class="searchSelect"></select></td><td class="group_td_gap"></td>');
                panel.append('<td><a class="group_sort sale_sort">销量</a></td><td class="group_td_gap"></td>');
                panel.append('<td><a class="group_sort price_sort">价格</a></td><td class="group_td_gap"></td>');
                panel.append('<td class="group_btn tp_search doSearch"><a style="color:#fff;">搜索</a></td>');
            },

            appendSellerCat:function(){
                var sellerCatObj = $(".tmp_toPut_top").find(".searchSelect");
                $.get('/Items/sellerCatCount',function(data){
                    sellerCatObj.empty();
                    if(!data || data.length == 0){
                        sellerCatObj.hide();
                    }
                    var exist = false;
                    var cat = $('<option>店铺类目</option>');
                    sellerCatObj.append(cat);
                    for(var i=0;i<data.length;i++){
                        if(data[i].count <= 0){
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
                    group.toPut.listItems();
                });
            },

            listItems:function(){
                var panelObj = $(".tmp_toPut_top");
                var params = group.params.getSearchParams(panelObj);
                var listBottomObj = $(".tmp_toPut_bottom");
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
                            var areaObj = $(".tmp_toPut_area");
                            if(data.res.length > 0){
                                group.toPut.appendItemBoxes(areaObj,data.res);
                                //remove btn
                                $(".tp_showItems").remove();
                            }else{
                                areaObj.empty();
                                areaObj.append('<tr><td style="text-align: center;width:100%;">抱歉，没有找到相关的宝贝<td><tr>');
                            }
                        }
                    }
                });
            },
            appendItemBoxes:function(areaObj,data){
                areaObj.empty();
                var toPutItemBox = $("#toPutItemBox").tmpl(data);
                var planId = group.showModel.getPlanId();
                var url = window.location.href;
                if(planId !== undefined &&  planId != "" && planId != null && url.indexOf("&input=true") > 0){
                    $.post("/group/getGroupedItems",{planId:planId},function(data){
                        $.each(data,function(i,value){
                            group.result.addSelectNumIid(value);
                            group.toPut.eachItemBox(toPutItemBox,areaObj);
                        });
                        group.toPut.appendToPutCheckedBtn();
                    });
                }
                else{
                    group.toPut.eachItemBox(toPutItemBox,areaObj);
                    group.toPut.appendToPutCheckedBtn();
                }
            },

            eachItemBox:function(toPutItemBox,areaObj){
                $.each(toPutItemBox,function(i,value){
                    if(i % 4 == 0){
                        areaObj.append('<tr class="tp-item-tr"></tr>');
                    }
                    areaObj.find("tr[class='tp-item-tr']:last").append($(this));

                    var numIid = $(this).find(".tp_item_img").attr("data");
                    if(group.result.isInSelectArray(numIid) == true){
                        $(this).find(".put_item_div").addClass("item-box-fix");
                        $(this).find(".put_item_div").css("border","1px solid #ff7744");
                        $(this).find("input").attr("checked",true);
                    }
                });
            },

            appendToPutCheckedBtn:function(){
                $(".tp_checked").remove();
                $(".tmp_toPut").append('<div class="group_btn tp_checked"><span>投放选中宝贝</span></div>')
                group.toPut.event();
            }

        },group.toPut);


        group.params = group.params || {};
        group.params = $.extend({

            getSearchParams:function(panelObj){

                var params = {};

                var searchText = panelObj.find(".group_search_input_wrap").attr("value");

                var sellerCid = panelObj.find(".searchSelect option:selected").attr("catid");

                var sort;
                var salesSortClass = panelObj.find(".sale_sort");

                if(salesSortClass.hasClass("sales_sort_up")){
                    sort = 'sellAsc';
                }
                if(salesSortClass.hasClass("sales_sort_down")){
                    sort = 'sellDesc';
                }

                if(sellerCid === undefined){
                    sellerCid = 0;
                }

                var priceSortClass = panelObj.find(".price_sort");

                if(priceSortClass.hasClass("price_sort_up")){
                    sort = 'priceAsc';
                }
                if(priceSortClass.hasClass("price_sort_down")){
                    sort = 'priceDesc';
                }

                if(sort === undefined){
                    sort = "sortNormal";
                }

                params.searchText = searchText;
                params.sellerCid = sellerCid;
                params.sort = sort;
                return params;
            },

            getPlanParams:function(){
                var params = {};
                var planName = $(".tmp_set .templateName").attr("value");
                if($.trim(planName).length == 0){
                    TM.Alert.load("模板名称不能为空", 250,150, function(){return false;},function(){return false;},"提示");
                    $(".tmp_set .templateName").focus();
                    return null;
                }

                if($.trim(planName).length > 40){
                    TM.Alert.load("模板名称过长", 250,150, function(){return false;},function(){return false;},"提示");
                    $(".tmp_set .templateName").focus();
                    return null;
                }

                var modelId = $(".tmp_table").attr("value");
                if(modelId == '' || modelId === undefined){
                    alert("参数错误 请联系客服 -1");
                    return null;
                }

                var planId = $(".tmp_set_btn .group_next").attr("pid-data");
                if(planId != "" && planId !== undefined){
                    params.planId = planId;
                }

                //type maybe width or color
                var type = $(".g_pro_td_p").find(".g_pro_on").attr('data');

                if(type == "" || type === undefined){
                    type = $(".g_pro_td_c").find(".g_pro_choosed").attr('data');
                }

                if(type == "" || type === undefined){
                    alert("参数错误 请联系客服 -2");
                    return null;
                }

                var days = $(".g_pro_td_day").find("input").attr("value");
                var ex = /^\d+$/;
                if(days !== undefined && days != "" && days != null){
                    if (!ex.test(days)) {
                        TM.Alert.load("剩余天数格式不正确,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }

                    if(days.length > 4){
                        TM.Alert.load("剩余天数过大,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                }

                var hours = $(".g_pro_td_hour").find("input").attr("value");
                if(hours !== undefined && hours != "" && hours != null){
                    if (!ex.test(hours)) {
                        TM.Alert.load("剩余小时格式不正确,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                    if(hours.length > 4){
                        TM.Alert.load("剩余小时过大,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                }

                var minutes = $(".g_pro_td_minute").find("input").attr("value");
                if(minutes !== undefined && minutes != "" && minutes != null){
                    if (!ex.test(minutes)) {
                        TM.Alert.load("剩余分钟格式不正确,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                    if(minutes.length > 2){
                        TM.Alert.load("剩余分钟过大,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                }

                var activityTitle = $(".g_pro_td_activityTitle").find("input").attr("value");
                if(activityTitle !== undefined && activityTitle != "" && activityTitle != null){
                    if(activityTitle.length > 30){
                        TM.Alert.load("活动标题过长,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                }

                var label = $(".g_pro_td_label").find("input").attr("value");
                if(label !== undefined && label != "" && label != null){
                    if(label.length > 10){
                        TM.Alert.load("标签名称过长,请重新填写", 250,150, function(){return false;},function(){return false;},"提示");
                        return null;
                    }
                }

                params.planName = planName;
                params.modelId = modelId;
                params.type = type;
                params.days = days;
                params.hours = hours;
                params.minutes = minutes;
                params.activityTitle = activityTitle;
                params.label = label;
                return params;
            },

            //src title price minPrice salesCount
            getItemPrams:function(){
                var itemObjs = $(".group_item");
                var itemsArr = [];
                $.each(itemObjs,function(){
                    var item = {};
                    var id = $(this).find(".group_image").attr("data");
                    if(id !== undefined || id != ""){
                        item.id = id;
                        if($(this).find(".group_image").is("img")){
                            item.image = $(this).find(".group_image").attr("src");
                        }else{
                            item.image = $(this).find(".group_image").attr("background");
                        }


                        var title = $(this).find(".group_title").text();
                        if(title === undefined || title == "" || title == null){
                            item.title = "";
                        }else{
                            item.title = title;
                        }

                        item.minPrice = $(this).find(".group_minPrice").text();
                        item.price = $(this).find(".group_price").text();
                        item.discount = $(this).find(".group_discount").text();
                        item.count = $(this).find(".group_count").text();
                        itemsArr[itemsArr.length] = item;
                    }
                });
                return itemsArr;
            },

            getItemParamsFinal:function(){
                var itemPropArr = new Array();
                $("input").css("border","1px solid #D1D1D1");
                $.each($(".group_item_prop"),function(){
                    var arrString = '';
                    var id = $(this).find(".group_item_image").attr("data");
                    if(id === undefined || id == ""){
                        alert("final 參數出錯 -1");
                        itemPropArr = null;
                    }

                    var title = $(this).find(".group_item_title").attr("value");
                    if(title === undefined || title == null){
                        title = "";
                    }else{
                        if(title.length > 40){
                            TM.Alert.load("名称过长(最多支持40个)", 250,150, function(){return false;},function(){return false;},"提示");
                            $(this).find(".group_item_title").focus();
                            $(this).find(".group_item_title").css("border","1px solid red");
                            itemPropArr = null;
                        }
                    }

                    var price = $(this).find(".group_item_price").attr("value");
                    if(price.length > 8){
                        TM.Alert.load("价格过高", 250,150, function(){return false;},function(){return false;},"提示");
                        $(this).find(".group_item_price").focus();
                        $(this).find(".group_item_price").css("border","1px solid red");
                        itemPropArr =  null;
                    }

                    var exPrice = /^\d{1,10}\.*\d{0,2}$/;
                    if(!exPrice.test(price)){
                        TM.Alert.load("价格格式不正确", 250,150, function(){return false;},function(){return false;},"提示");
                        $(this).find(".group_item_price").focus();
                        $(this).find(".group_item_price").css("border","1px solid red");
                        itemPropArr =  null;
                    }

                    var minPrice = $(this).find(".group_item_minPrice").attr("value");

                    if(minPrice.length > 8){
                        TM.Alert.load("价格过高", 250,150, function(){return false;},function(){return false;},"提示");
                        $(this).find(".group_item_minPrice").focus();
                        $(this).find(".group_item_minPrice").css("border","1px solid red");
                        itemPropArr = null;
                    }

                    if(!exPrice.test(minPrice)){
                        TM.Alert.load("价格格式不正确", 250,150, function(){return false;},function(){return false;},"提示");
                        $(this).find(".group_item_minPrice").focus();
                        $(this).find(".group_item_minPrice").css("border","1px solid red");
                        itemPropArr = null;
                    }

                    if(parseInt(minPrice) > parseInt(price)){
                        TM.Alert.load("特价不能大于原价", 250,150, function(){return false;},function(){return false;},"提示");
                        $(this).find(".group_item_minPrice").focus();
                        $(this).find(".group_item_minPrice").css("border","1px solid red");
                        itemPropArr = null;
                    }

                    var count = $(this).find(".group_item_count").attr("value");
                    if(count === undefined || count == "" || count == null){
                        count = 0;
                    }
                    var ex = /^\d+$/;
                    if(!ex.test(count)){
                        TM.Alert.load("销量格式不正确", 250,150, function(){return false;},function(){return false;},"提示");
                        $(this).find(".group_item_count").focus();
                        $(this).find(".group_item_count").css("border","1px solid red");
                        itemPropArr = null;
                    }
                    if(count > 0){
                        if(count.length > 8){
                            TM.Alert.load("销量过多", 250,150, function(){return false;},function(){return false;},"提示");
                            $(this).find(".group_item_count").focus();
                            $(this).find(".group_item_count").css("border","1px solid red");
                            itemPropArr = null;
                        }
                    }

                    arrString += '{"id":' + '"' + id +  '",';
                    arrString += '"title":' + '"' + title +  '",';
                    arrString += '"price":' + '"' + price +  '",';
                    arrString += '"minPrice":' + '"' + minPrice +  '",';
                    arrString += '"count":' + '"' + count +  '"}';
                    itemPropArr.push(arrString);
                });

                return itemPropArr;
            }
        },group.params);


        group.result = group.result || {};
        group.result = $.extend({

            selectNumIidArray: [],

            getSelectNumIidArray: function() {
                return group.result.selectNumIidArray;
            },

            isInSelectArray: function(numIid) {
                for (var i = 0; i < group.result.selectNumIidArray.length; i++) {

                    if (group.result.selectNumIidArray[i] == numIid) {
                        return true;
                    }
                }
                return false;
            },

            addSelectNumIid: function(numIid) {
                group.result.removeSelectNumIid(numIid);
                group.result.selectNumIidArray[group.result.selectNumIidArray.length] = numIid;
            },

            removeSelectNumIid: function(numIid) {

                for (var i = 0; i < group.result.selectNumIidArray.length; i++) {

                    if (group.result.selectNumIidArray[i] == numIid) {
                        group.result.selectNumIidArray.splice(i, 1);
                        return;
                    }

                }

            },

            removeSomeSelectNumIids: function(numIidArray) {

                for (var i = 0; i < numIidArray.length; i++) {
                    group.result.removeSelectNumIid(numIidArray[i]);
                }
            }
        },group.result);

        TM.group.showDialog=function(html, width, height, okCallback, cancelCallback, title){
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
                    '查看投放':function(){
                        okCallback && okCallback();
                        $(this).dialog('close');
                        $("body").unmask();
                    },
                    '继续制作':function(){
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