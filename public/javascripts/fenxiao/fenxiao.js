((function ($, window) {

var autoTitle = autoTitle || {};

autoTitle.Init = autoTitle.Init || {};
autoTitle.Init = $.extend({
    init : function(){
        autoTitle.Init.initSearchArea();
        autoTitle.Init.initDiagArea();
        autoTitle.Init.initYingxiaoDialog();
    },
    initYingxiaoDialog : function(){
        $.ajax({
            url : '/popularize/getUserInfo',
            data : {},
            type : 'post',
            success : function(dataJson) {
                // 检测卖家昵称是否淘宝小二测试用户，否则弹窗
                if(TM.util.isNotTaobaoCeshi(dataJson.username)) {
                    $.get('/OPUserInterFace/showXufeiOrNot',function(data){
                        if(data == "show"){
                            autoTitle.Init.showXufei(dataJson.version);
                        }
                    });

                    // 5星好评弹图片
                    autoTitle.Init.showHaoPingOrNor();

                    // 5元续费弹窗
                    $.get('/OPUserInterFace/show5yuanXufei',function(data){
                        if(data == "show") {
                            $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                                if(data == 'unshowed'){
                                    var version = dataJson.level;
                                    var link = "";
                                    if(version <= 20) {
                                        link = 'http://to.taobao.com/E5zcpgy';
                                    } else if(version <= 30){
                                        link = 'http://to.taobao.com/pWzcpgy';
                                    } else if(version <= 40){
                                        link = 'http://to.taobao.com/Yj1dpgy';
                                    } else if(version <= 50){
                                        link = 'http://to.taobao.com/xFycpgy';
                                    } else if(version <= 60){
                                        link = 'http://to.taobao.com/CW0dpgy';
                                    }
                                    var left = ($(document).width() - 500)/2;
                                    var top = 130;
                                    var html = '<div style="z-index: 19000;position: absolute;" class="five-yuan-xufei-img-dialog">' +
                                        '<a target="_blank" href="'+link+'">' +
                                        '<img src="http://img04.taobaocdn.com/imgextra/i4/1132351118/T2k3ASXkXXXXXXXXXX_!!1132351118.jpg" style="width: 500px;height: 330px;">' +
                                        '</a>' +
                                        '<span class="inlineblock close-five-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;z-index: 19100;">' +
                                        '</span>' +
                                        '</div>';
                                    $('.five-yuan-xufei-img-dialog').remove();
                                    var five_yuan = $(html);
                                    if($.browser.msie) {
                                        five_yuan.find('.close-five-yuan-dialog').css('filter',"alpha(opacity=10)");
                                        five_yuan.find('.close-five-yuan-dialog').css('background-color',"#D53A04");
                                    }
                                    five_yuan.css('top',top+"px");
                                    five_yuan.css('left',left+"px");
                                    five_yuan.find('.close-five-yuan-dialog').click(function(){
                                        five_yuan.remove();
                                        $('body').unmask();
                                    });five_yuan.appendTo($('body'));
                                    $('body').mask();

                                    $.post('/OPUserInterFace/set5YuanXufeiShowed',function(data){

                                    });
                                }
                            })
                        }
                    })
                }
            }
        });
    },
    showHaoPingOrNor : function(){
        $.get('/OPUserInterFace/show5XingHaoPing',function(res){
            if(res == 'show'){
                $.get('/OPUserInterFace/haopingshowed',function(data){
                    if(data == "unshowed") {
                        $.get('/status/user',function(data){
                            var firstlogintime = TM.firstLoginTime;
                            var now = new Date().getTime();
                            var interval = now - firstlogintime;
                            if(interval > 7*24*3600*1000){
                                var html = ''+
                                    '<table style="z-index: 1001;width: 750px;height: 396px;background: url(http://img04.taobaocdn.com/imgextra/i4/1132351118/T2ouE7Xh4XXXXXXXXX_!!1132351118.jpg);position: fixed;_position:absolute;">' +
                                    '<tbody>' +
                                    '<tr style="height: 300px;">' +
                                    '<td style="width: 50%;"></td>'+
                                    '<td style="width: 50%;"></td>'+
                                    '</tr>' +
                                    '<tr style="height: 96px;">' +
                                    '<td style="width: 50%;">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=FW_GOODS-1835721">' +
                                    '<div class="goto-pingjia" style="cursor: pointer;width: 375px;height: 96px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td class="not-goto-haoping" style="width: 50%;cursor: pointer;">' +
                                    '</td>'+
                                    '</tr>' +
                                    '</tbody>'+
                                    '</table>'+
                                    '';
                                var left = ($(document).width() - 750)/2;
                                var top = 130;
                                var content = $(html);
                                content.css('top',top+"px");
                                content.css('left',left+"px");
                                content.find('.not-goto-haoping').click(function(){
                                    content.remove();
                                    $('body').unmask();
                                });
                                $('body').mask();
                                $('body').append(content);
                                $.post('/OPUserInterFace/setHaoPingShowed',function(data){

                                });
                            }
                        });
                    }
                });

            }
        });
    },
    showXufei : function(version){
        var key = "500以下版本";
        if(version <= 20){
            key = "500以下版本";
        } else {
            key = "500以上版本";
        }
        $.get("/OPUserInterFace/xufeishowed",function(data){
            if(data == "unshowed"){
                var hour = new Date().getHours();
                var remain;
                if(hour <= 8){
                    remain = 12;
                } else if(hour <= 12){
                    remain = 9;
                } else if(hour <= 16){
                    remain = 6;
                } else if(hour <= 20){
                    remain = 3;
                } else if(hour <= 22){
                    remain = 2;
                } else {
                    remain = 1;
                }
                $.ajax({
                    type:'GET',
                    url :'/OPUserInterFace/freeLink',
                    dataType:'text',
                    data :{key:key},
                    success :function(data){
                        if(data == "找不到用户名" || data=="用户名为空" || data=="找不到营销链接" || data=="获取营销链接出错"){
                            //TM.Alert.load(data);
                        } else {
                            var html = ''+
                                '<p style="text-align:center;font-size: 60px;color: red;font-weight: bold;margin: 20px 0px;">年中大促</p>'+
                                '<p style="text-align:center;font-size:50px;font-weight: bold;margin: 20px 0px;">恭喜你获得免费半年 奖励</p>'+
                                '<p style="text-align:center;font-size:50px;font-weight: bold;margin: 20px 0px;">点击以下链接获取哦亲</p>'+
                                '<p style="text-align:center;margin: 20px 0px;" class="free-link"><a target="_blank" href="'+data+'"><span class="free-link" style="font-size: 35px;font-weight: bold;">'+data+'</span></a></p>'+
                                '<p style="text-align:center;">备注：此活动每天仅限15人，剩余<span class="remain" style="font-size: 60px;font-weight: bold;">'+remain+'</span>人</p>'+
                                '';
                            var content = $(html);
                            /*content.find('.free-link').click(function(){
                             $('.ui-dialog ').hide();
                             $('.ui-widget-overlay').hide();
                             });*/
                            var redshow = function(){
                                content.find('.remain').toggleClass('red');
                                content.find('.free-link').toggleClass('red');
                            }
                            setInterval(redshow,300);
                            TM.Alert.loadDetail(content,1000,650,function(){
                                return true;
                            },"年中大促")
                        }
                    }
                })
                // no show any more
                $.get('/OPUserInterFace/setShowed',function(data){
                    return true;
                });
            }
        });
    },
    isFengxiao : function(isFenxiao){
            if(isFenxiao){
                var fengxiaoUl = $("<ul class='sidemenu sidebox side-tracked-tag' style='display: none'><li class='aside-hot-tag'><a class='followed-tag clearfix sidemenuAnchor' href='javascript:void(0)'>"
                +"<span class='aside-icon'></span>"
                +"<span>分销</span>"
                +"</a><span class='fold-icon unfold'></span><div class='secondUl' style='height: 100px;overflow-y: scroll;'></div></li></ul>");
                fengxiaoUl.find('.sidemenuAnchor').click(function(){
                    fengxiaoUl.find('.fold-icon').trigger("click");
                });
                $.get('/Fenxiaos/gonghuoInfo',function(info){
                    if(info != null && info.gonghuo != null && info.gonghuo.length > 0){
                        // get items with fenxiao title
                        autoTitle.ItemsDiag.getItemsDiag(true);

                        var gonghuos = info.gonghuo.split(",");
                        $(gonghuos).each(function(i,gonghuo){
                            if(gonghuo.length > 0){
                                fengxiaoUl.find('.secondUl').append($('<div class="secondLi" tag="fengxiao" name="'+gonghuo+'"> <a href="javascript:void(0)" style="border-bottom: 0px;width: 120px;">'+gonghuo+'</a><div class="inlineblock removeFengxiao" ></div>'));
                            }
                        });
                        fengxiaoUl.find('.secondUl').append($('<div class="secondLi" tag="addFengxiao" name="addFengxiao"> <a class="addFengxiao" href="javascript:void(0)" style="border-bottom: 0px;width: 120px;">添加供货商</a><div class="inlineblock addFengxiao addFenxiaoBack" ></div>'));
                        fengxiaoUl.find('.addFengxiao').click(function(){
                            $('<p>请输入供货商名称:</br><input id="honghuoname" type="text" value="" style="width:320px;"></p>').dialog({
                                modal: true,
                                bgiframe: true,
                                height:230,
                                width:370,
                                title:'请输入供货商名称',
                                autoOpen: true,
                                resizable: false,
                                buttons:{'确定':function() {
                                    var name = $('#honghuoname').val();
                                    $.post('/Fenxiaos/addGonghuo',{name:name},function(data){
                                        if(data.res){
                                            TM.Alert.load("供货商添加成功~",300,200,function(){
                                                location.reload();
                                            });
                                        } else {
                                            TM.Alert.load("供货商添加失败~");
                                        }
                                    });
                                    $(this).dialog('close');
                                },'取消':function(){
                                    $(this).dialog('close');
                                }}
                            });
                        });
                        fengxiaoUl.find('.removeFengxiao').click(function(){
                            var name = $(this).parent().find('a').text();
                            if(confirm("确定要删除【"+name+"】该供货商?")){
                                $.post("/Fenxiaos/removeGonghuo",{name:name},function(data){
                                    if(data.res){
                                        TM.Alert.load("供货商删除成功~",300,200,function(){
                                            location.reload();
                                        });
                                    } else {
                                        TM.Alert.load("供货商删除失败~");
                                    }
                                });
                            }
                        });
                        /*fengxiaoUl.qtip({
                            content: {
                                text: "您是分销卖家，在此添加您的供货商就可以使用官方标题了哟~"
                            },
                            position: {
                                at: "top",
                                corner: {
                                    target: 'topright'
                                }
                            },
                            show: {
                                ready:true
                            },
                            hide: {
                                delay:1000
                            },
                            style: {
                                name:'cream'
                            }
                        });*/
                    } else {

                        // get items without fenxiao title
                        autoTitle.ItemsDiag.getItemsDiag(isFenxiao);

                        fengxiaoUl.find('.secondUl').append($('<div class="secondLi" tag="addFengxiao"> <a class="addFengxiao" href="javascript:void(0)" style="border-bottom: 0px;width: 120px;">添加供货商</a><div class="inlineblock addFengxiao addFenxiaoBack" ></div>'));
                        fengxiaoUl.find('.addFengxiao').click(function(){
                            $('<p>请输入供货商名称:</br><input id="honghuoname" type="text" value="" style="width:320px;"></p>').dialog({
                                modal: true,
                                bgiframe: true,
                                height:230,
                                width:370,
                                title:'请输入供货商名称',
                                autoOpen: true,
                                resizable: false,
                                buttons:{'确定':function() {
                                    var name = $('#honghuoname').val();
                                    $.post('/Fenxiaos/addGonghuo',{name:name},function(data){
                                        if(data.res){
                                            TM.Alert.load("供货商添加成功~",300,200,function(){
                                                location.reload();
                                            });
                                        } else {
                                            TM.Alert.load("供货商添加失败~");
                                        }
                                    });
                                    $(this).dialog('close');
                                },'取消':function(){
                                    $(this).dialog('close');
                                }}
                            });
                        });
                    }
                    //
                });
                fengxiaoUl.find('.fold-icon').click(function(){
                    if($(this).hasClass("fold")) {
                        $(this).removeClass("fold");
                        $(this).addClass("unfold");
                        if($(this).parent().find('.secondUl').length > 0) {
                            $(this).parent().find('.secondUl').css("display","block");
                        }
                    } else if($(this).hasClass("unfold")) {
                        $(this).removeClass("unfold");
                        $(this).addClass("fold");
                        if($(this).parent().find('.secondUl').length > 0) {
                            $(this).parent().find('.secondUl').css("display","none");
                        }
                    }
                });
                $('.navmain .aside').append(fengxiaoUl);
            } else {
                // hide batch guanfang and guanfangRecomm title btn
                $('.batchAllUseGuanfang').hide();
                $('.batchAllUseGuanfangRecomm').hide();
                var tobeFenxiao = $("<span style='font-size: 14px;font-family: 微软雅黑;color: red;cursor: pointer;'>我是分销商</span>");
                tobeFenxiao.click(function(){
                    if(confirm("亲,您确定是分销商？")){
                        $.post('/AutoTitle/tobeFenxiao',{toBeOn:true},function(data){
                            TM.Alert.load(data, 400, 300, function(){
                                location.reload();
                            });
                        });
                    }
                });
                $('.batchOpBody').append(tobeFenxiao);

                autoTitle.ItemsDiag.getItemsDiag(false);
            }
    },
    initSearchArea : function(){
        $.get("/items/sellerCatCount",function(data){
            var sellerCat = $('#itemsCat');
            sellerCat.empty();
            if(!data || data.length == 0){
                sellerCat.hide();
            }

            var exist = false;
            var cat = $('<option>自定义类目</option>');
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
        });
        $.get("/items/itemCatCount",function(data){
            var taobaoCat = $('#taobaoCat');
            taobaoCat.empty();
            if(!data || data.length == 0){
                taobaoCat.hide();
            }

            var exist = false;
            var cat = $('<option>淘宝类目</option>');
            taobaoCat.append(cat);
            for(var i=0;i<data.length;i++) {
                if(data[i].count <= 0){
                    continue;
                }

                exist = true;
                var option = $('<option></option>');
                option.attr("catId",data[i].id);
                option.html(data[i].name+"("+data[i].count+")");
                taobaoCat.append(option);
            }
            if(!exist){
                taobaoCat.hide();
            }
        });
    },
    initDiagArea : function(){
        $.get("/Home/firstSync",function(){
            $.getScript("/Status/user",function(data){
                $.cookie("isFenxiao",TM.isFenxiao);
                autoTitle.Init.isFengxiao(TM.isFenxiao);
               // autoTitle.ItemsDiag.getItemsDiag(true);
            })
        });

    }
},autoTitle.Init);

autoTitle.ItemsDiag = autoTitle.ItemsDiag || {};
autoTitle.ItemsDiag = $.extend({
    getItemsDiag : function(isFengxiao){
        $('.diagResultArea').empty();
        var table = $('<div></div>');
        //var bottom = $('<div class="autoTitleBottom" style="text-align: center;"></div>');
        var bottom = $('.autoTitleBottom');
        var params = $.extend({
            "s":"",
            "status":2,
            "catId":null,
            "sort":1,
            "lowBegin":0,
            "ps":5,
            "topEnd":100
        },autoTitle.util.getParams());
        if(window.location.hash == '#score-desc'){
            params.sort = 2;
            window.location.hash = "";
        }
        bottom.tmpage({
            currPage: 1,
            pageSize: 5,
            pageCount:1,
            useSmallPageSize: true,
            ajax: {
                param : params,
                on: true,
                dataType: 'json',
                url: "/Titles/getItemsWithDiagResult",
                callback:function(data){
                    $('.diagResultArea').empty();
                    $('.autoTitleDiv').find(".clear").remove();
                    $('.diagResultArea').append(autoTitle.ItemsDiag.createDiagHead(isFengxiao));
                    $('.diagResultArea').append(autoTitle.ItemsDiag.createItemsWithDiagResult(data.res,isFengxiao));
                    $('.autoTitleDiv').append($('<div class="clear"></div>'));
                    autoTitle.Event.initEvent();
                    if(isFengxiao){
                        $('.diagBriefInf').css("height","170px");
                    }
                }
            }

        });
    },
    createItemsWithDiagResult : function(res,isFengxiao){
        var itemsWithDiagResult = $('<div class="itemsWithDiagResult"></div>');
        itemsWithDiagResult.append(autoTitle.ItemsDiag.createDiagBody(res,isFengxiao));
        return itemsWithDiagResult;
    },
    createDiagHead : function(isFengxiao){
        var head = $('<div class="diagHead"></div>');
        var ulObj = $('<ul class="diagHeadUL"></ul>');

        var liObjSelect = $('<li class=" fl" style="width:40px;" ></li>');
        liObjSelect.append($('<input type="checkbox" id="checkAllItems" style="margin-top:15px;" class="width17" >'));
        //liObjSelect.append("全选");
        ulObj.append(liObjSelect);
        ulObj.append($('<li class="fl" style="width:150px;text-align: center;"><span style="margin-top: 5px;" class="inlineblock batchSelectUseRecommend tmbtn long-flat-green-btn">选中使用推荐标题</span></li>'));
        ulObj.append($('<li class="fl" style="width:150px;text-align: center;"><span style="margin-top: 5px;" class="inlineblock advancedRecom tmbtn long-flat-green-btn">选中重新高级推荐</span></li>'));
        ulObj.append($('<li class="fl" style="width:360px;line-height: 45px;">亲，推荐的标题不够好？<span style="color: blue;cursor: pointer;" class="feedback">[反馈给我们]</span></li>'));
      //  ulObj.append($('<li class="fl" style="width:460px;line-height: 45px;"></li>'));
        //ulObj.append($('<li class="fl" style="width:120px"><span class="batchAllUseRecommend commbutton btntext6 btn6orange">全店一键自动标题</span></li>'));
//        ulObj.append($('<li class="fl" style="width:35px;text-align: center;">状态</li>'));
//        ulObj.append($('<li class="fl" style="width:35px;text-align: center;">月销量</li>'));
//        ulObj.append($('<li class="fl" style="width:120px;text-align: center;">操作</li>'));
        ulObj.find('.advancedRecom').click(function(){
            if($(".itemsWithDiagResult").find(".subCheckBox:checked").length == 0){
                TM.Alert.load("亲，您还没有选择宝贝哦");
                return;
            }
            $.get('/items/itemCatCount',function(itemCatCount){
                $.get('/items/sellerCatCount',function(sellerCatCount){
                    autoTitle.Event.advancedBatchRecommend(itemCatCount,sellerCatCount,isFengxiao);
                })
            });
        });
        ulObj.find('.feedback').click(function(){
            var oThis = $(this);
            var exist = $(".feeddiag");
            exist.remove();
            var titles = [];
            $('.singleDiagRes').each(function(){
                titles.push("["+$(this).find('.oldTitleContent').val()+"]");
            });
            var pageTitles = titles.join(",");
            var htmls = [];
            htmls.push("<div class='feeddiag'>")
            htmls.push("<div><table>");
            htmls.push("<tr><td style='width: 120px;'>当前页面标题:&nbsp;</td><td style='word-break:break-all;width: 450px;'>"+pageTitles
                +"</td></tr>");
            htmls.push("<tr><td>您的建议:&nbsp;</td><td><textarea class='feedbackarea'>如果您对我们的推荐结果不够满意,请给我们留下您最宝贵的建议,我们一定努力改进,尽快让您看到最好的推荐效果</textarea></td></tr>")
            htmls.push("</table></div>");
            htmls.push("</div><div>");
            htmls.push("</div>");
            var body =$(htmls.join(''));
            TM.Alert.loadDetail(body, 700, 550, function(){
                var feedbackcontent = body.find('.feedbackarea').val();
                if(feedbackcontent == "如果您对我们的推荐结果不够满意,请给我们留下您最宝贵的建议,我们一定努力改进,尽快让您看到最好的推荐效果"){
                TM.Alert.load("亲,请您留下一点建议哟~");
                return false;
            } else {
                $.get('/op/commitRecommenFeedBack',{
                    'feed.origin':"origin",
                    'feed.recommendTitle':"recommendTitle",
                    'feed.content':body.find('.feedbackarea').val()+"!@#"+pageTitles,
                    'feed.numIid':""
                }, function(){
                    TM.Alert.load('非常感谢您的建议,升流量一定尽快做出优化');
                });
            }
            return true;
        },'请留下您的宝贵建议');
        });;
        head.append(ulObj);
        return head;
    },
    createDiagBody : function(results,isFengxiao){
        var body = $('<div class="diagBody"></div>');
        var numIids = [];
        for(var i = 0; i<results.length;i++) {
            var item = results[i];
            body.append(autoTitle.ItemsDiag.createSingleDiagRes(item,i,isFengxiao));
            numIids.push(item.id);
        }

        $.ajax({
            type:"get",
            url:'/Titles/getRecommends',
            data:{numIids:numIids.join(',')},
            global:false,
            success: function(data){
                $.each(data,function(i,diag){
                    var newTitle = body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .newTitleContent");
                    newTitle.val(diag.title);
                    newTitle.show();
                    newTitle.parent().find('.waitingrecommend').hide();
                    var newTitleScore = body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .recommendTitleScore");
                    var oldScore = body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .oldTitleScore");
                    /* if(autoTitle.util.countCharacters(diag.title) > 40){
                     newTitleScore.empty();
                     newTitleScore.append($('<span class="titleScoreNumber" style="font-size: 12px;">良好</span>'));
                     } else {
                     newTitleScore.empty();
                     newTitleScore.append($('<span class="titleScoreNumber"  style="font-size: 12px;">不够理想</span>'));
                     }*/
                    if(diag.score < oldScore.html()){
                        newTitleScore.html(oldScore.html());
                    } else {
                        newTitleScore.html(diag.score);
                    }

                    var boxId ="newTitleRemainObj"+i;
                    newTitle.inputlimitor({
                        limit: 60,
                        boxId: boxId,
                        remText: '<span class="twelve" >剩余字数:</span><span class="newRemainLength">%n</span>',
                        limitText: '/ %n'
                    });
                    newTitle.keyup();
                    $('#'+boxId).find('br').remove();
                })
            }
        });
        if(isFengxiao){
            $.ajax({
                type:"get",
                url:'/Items/fenxiaoTitles',
                data:{numIids:numIids.join(',')},
                global:false,
                success: function(data){
                if(data.isOk){
                    $.each(data.res,function(i,diag){
                        if(diag != null && diag.ok){
                            // original fenxiao title
                            var oldfenxiaoTitle =  body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .oldfenxiaoTitle");
                            if(oldfenxiaoTitle.length == 0) {
                                var oldfenxiaoTitle = $('<div style="margin: 15px 0px 35px 0px;*margin-top: 20px;*margin-bottom: 37px;" class="oldfenxiaoTitle"></div>');
                            }
                            oldfenxiaoTitle.empty();
                            oldfenxiaoTitle.append($('<span class="twelve">官方标题:</span>'));
                            var oldfenxiaoTitleContent = $('<input type="text" style="width: 450px;" class="oldfenxiaoTitleContent" value="'+diag.originTitle+'">');
                            oldfenxiaoTitle.append(oldfenxiaoTitleContent);
                            body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .newTitle").parent().append(oldfenxiaoTitle);
                            var saveOldFenxiaoTitle = $('<a href="javascript:void(0);" class="saveOldFenxiaoTitle tmbtn long-sky-blue-btn titleOpBtn" style="margin-top: 25px;">保存官方标题</a>');
                            body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .saveRecommendTitle").parent().append(saveOldFenxiaoTitle);
                            saveOldFenxiaoTitle.click(function(){
                                var oldfenxiaoTitleVal = oldfenxiaoTitleContent.val();
                                if(autoTitle.util.countCharacters(oldfenxiaoTitleVal) > 60) {
                                    alert("原始分销标题已超过淘宝字数限制，请删减后再提交");
                                } else {
                                    var numIid = diag.numIid;
                                    autoTitle.util.modifyTitle(numIid,oldfenxiaoTitleVal,body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .oldTitleContent"));
                                }
                            });

                            // fenxiao recommend title
                            var fenxiaoTitle =  body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .fenxiaoTitle");
                            if(fenxiaoTitle.length == 0) {
                                var fenxiaoTitle = $('<div class="fenxiaoTitle"></div>');
                            }
                            fenxiaoTitle.empty();
                            fenxiaoTitle.append($('<span class="twelve">官方推荐:</span>'));
                            var fenxiaoTitleContent = $('<input type="text" style="width: 450px;" class="fenxiaoTitleContent" value="'+diag.title+'">');
                            fenxiaoTitle.append(fenxiaoTitleContent);
                            body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .newTitle").parent().append(fenxiaoTitle);
                            var saveFenxiaoTitle = $('<a href="javascript:void(0);" class="saveFenxiaoTitle tmbtn long-sky-blue-btn titleOpBtn" style="margin-top: 25px;">保存官方推荐</a>');
                            body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .saveRecommendTitle").parent().append(saveFenxiaoTitle);
                            saveFenxiaoTitle.click(function(){
                                var fenxiaoTitleVal = fenxiaoTitleContent.val();
                                if(autoTitle.util.countCharacters(fenxiaoTitleVal) > 60) {
                                    alert("官方标题已超过淘宝字数限制，请删减后再提交");
                                } else {
                                    var numIid = diag.numIid;
                                    autoTitle.util.modifyTitle(numIid,fenxiaoTitleVal,body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .oldTitleContent"));
                                }
                            });
                            body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .diagBriefInf").css("height","220px");
                        } else {

                        }
                    })
                }
            }});
        }

        return body;
    },
    createSingleDiagRes : function(result,i,isFengxiao){
        var singleDiagRes = $('<div class="singleDiagRes" cid="'+result.cid+'" numIid="'+result.id+'"></div>');
        singleDiagRes.append(autoTitle.ItemsDiag.createDiagBriefInf(result,i,isFengxiao));
        singleDiagRes.append(autoTitle.ItemsDiag.createShowDetailTag(result));
        singleDiagRes.append(autoTitle.ItemsDiag.createDiagDetailInf(result));
       // singleDiagRes.append($('<br />'));
        return singleDiagRes;
    },
    createDiagBriefInf : function(result,i,isFengxiao){
        var briefInf = $('<div class="diagBriefInf"></div>');
        var ulObj = $('<ul class="singleDiagULObj"></ul>');
        ulObj.attr("numIid",result.numIid);
        ulObj.append(autoTitle.ItemsDiag.createCheckboxLiObj(result));
        ulObj.append(autoTitle.ItemsDiag.createItemImgLiObj(result));
        ulObj.append(autoTitle.ItemsDiag.createTitleLiObj(result,i,isFengxiao));
        ulObj.append(autoTitle.ItemsDiag.createOPLiObj(result,isFengxiao));
//        ulObj.append(autoTitle.ItemsDiag.createStatusLiObj(result));
//        ulObj.append(autoTitle.ItemsDiag.createSalesCountLiObj(result));

        briefInf.append(ulObj);
        return briefInf;
    },
    createCheckboxLiObj : function(result){
        var liObjSelect = $('<li class="checkBoxLi fl" style="" ></li>');
        liObjSelect.append($('<input type="checkbox" style="width:14px;" name="subCheck" class="subCheckBox" numIid="'+result.id+'">'));
        return  liObjSelect;
    },
    createItemImgLiObj : function(result){
        var liObjimg = $('<li class=" fl" style="width:100px;text-align: center;" ></li>');

        var aObj = $('<a target="_blank"></a>');
        var url = "http://item.taobao.com/item.htm?id=" + result.id;
        aObj.attr("href",url);
        var imgObj = $('<img style="width:60px;height:60px;border: 1px solid #5DB0F9;">');
        imgObj.attr("src",result.picURL);
        aObj.append(imgObj);
        liObjimg.append(aObj);
//        liObjimg.append("");
        liObjimg.append('<div style="height:32px;line-height: 32px;font-size:13px;">月销量<span class="red">'+(result.tradeItemNum>10000?(result.tradeItemNum/10000 + '万'):result.tradeItemNum)+'</span>件</div>');
        liObjimg.append('<a target="_blank" class="tmbtn sky-blue-btn" href="http://upload.taobao.com/auction/publish/edit.htm?item_num_id='+result.id+'&auto=false">修改属性</a>');

        return liObjimg;
    },
    createTitleLiObj : function(result,i,isFengxiao){
        var liObj = $('<li class=" fl" style="width:520px;" ></li>');
        liObj.append(autoTitle.ItemsDiag.createOldTitle(result,i));
        liObj.append(autoTitle.ItemsDiag.createNewTitle(result, i));
        /*if(isFengxiao == true){
            // TODO fenxiao title
            liObj.append(autoTitle.ItemsDiag.createFenxiaoTitle(result,i));
        }*/
        return liObj;
    },
    createFenxiaoTitle : function(result,i){
        var fenxiaoTitle = $('<div class="fenxiaoTitle"></div>');
        fenxiaoTitle.append($('<span class="twelve">分销推荐:</span>'));
        var fenxiaoTitleContent = $('<input type="text" style="width: 450px;" class="fenxiaoTitleContent">');
        fenxiaoTitle.append(fenxiaoTitleContent);
        /*var fenxiaoTitleInf = $('<div ></div>');
        //oldTitleInf.attr("id","oldTitleInf"+i);
        *//*if(autoTitle.util.countCharacters(result.title) > 40){
         oldTitleInf.append($('<span class="twelve">原始标题:<span class="titleScoreNumber">&nbsp;良好</span></span>'));
         } else {
         oldTitleInf.append($('<span class="twelve">原始标题:<span class="titleScoreNumber">&nbsp;不够理想</span></span>'));
         }*//*
        fenxiaoTitleInf.append($('<span class="twelve">官方标题得分:</span>'));
        var fenxiaoTitleScore = $('<span class="fenxiaoTitleScore titleScoreNumber" ></span>');
        fenxiaoTitleScore.html(result.score);
        fenxiaoTitleInf.append(fenxiaoTitleScore);
        //oldTitleInf.append($('<span class="twelve">剩余字数:</span>'));
        var fenxiaoTitleRemainObj = $('<div style="display: inline-block;*display:inline;width:120px;"></div>');
        fenxiaoTitleRemainObj.attr("id","fenxiaoTitleRemainObj"+i);
        //var remainStr = 30-result.title.length;
        var remainStr = ((60-autoTitle.util.countCharacters(result.title))/2).toFixed(0);
        var fenxiaoTitleRemainWordsNum = $('<span class="twelve" >剩余字数:</span><span class="fenxiaoRemainLength">'+remainStr+'</span>');
        fenxiaoTitleRemainObj.append(fenxiaoTitleRemainWordsNum);
        fenxiaoTitleRemainObj.append(" / 30");
        //oldTitleRemainObj.append($('<span class="twelve">剩余字数:</span>'));
        fenxiaoTitleContent.inputlimitor({
            limit: 60,
            boxId: "fenxiaoTitleRemainObj"+i,
            remText: '<span class="twelve" >剩余字数:</span><span class="fenxiaoRemainLength">%n</span>',
            limitText: '/ %n'
        });
        fenxiaoTitleContent.keyup();
        fenxiaoTitleContent.val(result.title);
//        oldTitleRemainWordsNum.html(30-result.wordLength);
        fenxiaoTitleInf.append(fenxiaoTitleRemainObj);
        //fenxiaoTitleInf.append($('<a href="javascript:void(0);" class="diagCurrentTitle commbutton btntext4 titleOpBtn" style="display: inline-block;">立即诊断</a>'));
        fenxiaoTitleInf.appendTo(fenxiaoTitle);*/
        return fenxiaoTitle;
    },
    createOldTitle : function(result,i){
        var oldTitle = $('<div class="oldTitle"></div>');
        oldTitle.append($('<span class="twelve">原始标题:</span>'));
        var oldTitleContent = $('<input type="text" style="width: 450px;" class="oldTitleContent" id="oldTitleRemainObjInput'+i+'">');
        oldTitle.append(oldTitleContent);
        var oldTitleInf = $('<div ></div>');
        //oldTitleInf.attr("id","oldTitleInf"+i);
        /*if(autoTitle.util.countCharacters(result.title) > 40){
            oldTitleInf.append($('<span class="twelve">原始标题:<span class="titleScoreNumber">&nbsp;良好</span></span>'));
        } else {
            oldTitleInf.append($('<span class="twelve">原始标题:<span class="titleScoreNumber">&nbsp;不够理想</span></span>'));
        }*/
        oldTitleInf.append($('<span class="twelve">原始标题得分:</span>'));
        var oldTitleScore = $('<span class="oldTitleScore titleScoreNumber" ></span>');
        oldTitleScore.html(result.score);
        oldTitleInf.append(oldTitleScore);
        //oldTitleInf.append($('<span class="twelve">剩余字数:</span>'));
        var oldTitleRemainObj = $('<div style="display: inline-block;*display:inline;width:120px;"></div>');
        oldTitleRemainObj.attr("id","oldTitleRemainObj"+i);
        //var remainStr = 30-result.title.length;
        var remainStr = ((60-autoTitle.util.countCharacters(result.title))/2).toFixed(0);
        var oldTitleRemainWordsNum = $('<span class="twelve" >剩余字数:</span><span class="oldRemainLength">'+remainStr+'</span>');
        oldTitleRemainObj.append(oldTitleRemainWordsNum);
        oldTitleRemainObj.append(" / 30");
        //oldTitleRemainObj.append($('<span class="twelve">剩余字数:</span>'));
        oldTitleContent.inputlimitor({
            limit: 60,
            boxId: "oldTitleRemainObj"+i,
            remText: '<span class="twelve" >剩余字数:</span><span class="oldRemainLength">%n</span>',
            limitText: '/ %n'
        });
        oldTitleContent.keyup();
        oldTitleContent.val(result.title);
//        oldTitleRemainWordsNum.html(30-result.wordLength);
        oldTitleInf.append(oldTitleRemainObj);
        oldTitleInf.append($('<a href="javascript:void(0);" class="diagCurrentTitle tmbtn long-sky-blue-btn titleOpBtn" style="display: inline-block;margin: 5px 0 5px 0;">立即诊断</a>'));
        oldTitleInf.appendTo(oldTitle);
        return oldTitle;
    },
    createNewTitle : function(res, i){
        var newTitle = $('<div class="newTitle" style="margin: 0 0 10px 0;"></div>');
        newTitle.append($('<span class="twelve">推荐标题:<span class="waitingrecommend">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img  src="/img/loading/024.gif"></span></span>'));
        var newTitleContent = $('<input type="text" style="width: 450px;display:none;" id="newTitleRemainObjInput'+i+'" class="newTitleContent" value="">');
        newTitle.append(newTitleContent);
        var newTitleInf = $('<div></div>');
        newTitleInf.append($('<span class="twelve">推荐标题得分:</span>'));
        var newTitleScore = $('<span class="recommendTitleScore titleScoreNumber" ></span>');
        newTitleScore.html(0);
        newTitleInf.append(newTitleScore);

        //newTitleInf.append($('<span class="twelve">剩余字数:</span>'));
        var newTitleRemainObj = $('<div class="divinline" style=""></div>');
        newTitleRemainObj.attr("id","newTitleRemainObj"+i);
        //var remainStr = 0+ "/" + "30";
        var newTitleRemainWordsNum = $('<span class="twelve" >剩余字数:</span><span class="newRemainLength" style="margin-right: 5px;"></span>');
        newTitleRemainObj.append(newTitleRemainWordsNum);

       // var newTitleRemainWordsNum = $('<span class="recommendTitleRemainWordsNum" style="margin-right: 5px;"></span>');
       // newTitleRemainWordsNum.html(0);


        var spannewTitle = $("<div class='newTitleWrapper divinline' style='width:400px;'></div>");
        spannewTitle.append(newTitleRemainObj);
        //spannewTitle.append(feedAnchor);


        newTitleInf.append(spannewTitle);
        newTitleInf.appendTo(newTitle);


        var feed = $("<span class='feedbackanchor'>推荐的不够理想</span>");
        feed.appendTo(newTitleRemainObj);
        return newTitle;
    },
    createStatusLiObj : function(result){
        var liObj = $('<li class=" fl" style="width:5px;text-align: center;line-height: 100px;" ></li>');
        var span = $('<span></span>');
        span.html(1);
        liObj.append(span);
        return liObj;
    },
    createSalesCountLiObj : function(result){
        var liObj = $('<li class=" fl" style="width:95px;text-align: center;line-height: 100px;" ></li>');
        var span = $('<span></span>');
//        span.html('月销量');
        liObj.append('<div style="height:32px;line-height: 32px;">月销量<span>'+(result.tradeItemNum>10000?(result.tradeItemNum/10000 + '万'):result.tradeItemNum)+'</span>件</div>');
        liObj.append("<div>"+(result.status > 0 ?"在售":"在库") +"</div>");
        return liObj;
    },
    createOPLiObj : function(result,isFengxiao){
        var liObj = $('<li class=" fl" style="width:90px;text-align: center;" ></li>');
       // var saveCurrentTitle = $('<div id="saveCurrentTitle" style="font-size: 12px;">保存当前标题</div>');
       // var saveRecommendTitle = $('<div id="saveRecommendTitle" style="font-size: 12px;">保存推荐标题</div>');
        //var saveCurrentTitle = $('<a href="javascript:void(0);" class="mzc_button6 mzc_selectPageAll" style="margin-right: 20px;"><span><span id="goSearchItems">保存当前标题</span></span></a>');
//        var saveCurrentTitle = $('<a href="javascript:void(0);" class="mzc_button6 mzc_selectPageAll" ><span><span class="saveCurrentTitle" style="padding: 0px;">保存当前标题</span></span></a>');
//        var saveRecommendTitle = $('<a href="javascript:void(0);" class="mzc_button6 mzc_selectPageAll" style="margin-top: 15px;"><span><span class="saveRecommendTitle" style="padding: 0px;">保存推荐标题</span></span></a>');
        var saveCurrentTitle = $('<a href="javascript:void(0);" class="saveCurrentTitle tmbtn long-sky-blue-btn titleOpBtn">保存当前标题</a>');
        var saveRecommendTitle = $('<a href="javascript:void(0);" class="saveRecommendTitle tmbtn long-sky-blue-btn titleOpBtn" style="margin-top: 25px;">保存推荐标题</a>');
       /*if(isFengxiao == true){
            var saveFenxiaoTitle = $('<a href="javascript:void(0);" class="saveFenxiaoTitle commbutton btntext4 titleOpBtn" style="margin-top: 25px;">保存官方标题</a>');
        }*/
        liObj.append(saveCurrentTitle);
        liObj.append(saveRecommendTitle);
        //liObj.append(saveFenxiaoTitle);
        return liObj;
    },
    createShowDetailTag : function(result){
        var showDetailTag = $('<div class="clear" style="height: 32px;margin-bottom:10px"></div>');
        var leftLint = $('<div class="borderline"></div>');
        var tagImg = $('<a class="showDetailTag left tmbtn long-flat-green-btn" numIid="'+result.id+'">展开手动优化</a>');
        var rightLine = $('<div class="borderline"></div>');
        showDetailTag.append(leftLint);
        showDetailTag.append(tagImg);
        showDetailTag.append(rightLine);
        return showDetailTag;
    },
    createDiagDetailInf : function(result){
        var detailInf = $('<div class="diagDetailInf recommendDiv rowDiv" numIid="'+result.id+'"></div>');
        return detailInf;
    }
},autoTitle.ItemsDiag);

autoTitle.ItemDetail = autoTitle.ItemDetail || {};
((function ($, window) {
    var me = autoTitle.ItemDetail;

    me.init = function(container, numIid){

        if(container.find('.tabDiv').length > 0){
            return;
        }

        var tabHolder = $('<div class="tabDiv" style="width:100%;margin:0px auto 10px auto;"></div>');
        var contentHolder = $('<div class="liTargetDiv"></div>');

        var arr = [];
        arr.push('<ul class="clearfix" iid="'+numIid+'">');

        arr.push('<li><a targetcls="diagResultBlock" href="javascript:void(0);">诊断结果</a></li>');
        arr.push('<li><a targetcls="bussearch" href="javascript:void(0);">关键词推荐</a></li>');
        arr.push('<li><a targetcls="hotprops" href="javascript:void(0);">热销属性</a></li>');
//        arr.push('<li><a targetcls="competition" href="javascript:void(0);">类目优秀标题</a></li>');
        arr.push('<li><a targetcls="keywords" href="javascript:void(0);">开车热词</a></li>');
        arr.push('<li><a targetcls="longTailWords" href="javascript:void(0);">长尾词</a></li>');
        arr.push('<li><a targetcls="prosList" href="javascript:void(0);">属性列表</a></li>');
        arr.push('<li><a targetcls="promoteWordsBlock" href="javascript:void(0);">促销词</a></li>');
        //arr.push('<li><a targetcls="hotWordsExport" href="javascript:void(0);">热词导出</a></li>');
        arr.push('<li><a targetcls="renamehistory" href="javascript:void(0);">历史标题</a></li>');
        arr.push('<li><a targetcls="200words" href="javascript:void(0);" style="width: 110px;">直通车关键词预览</a></li>');
        /*arr.push('<li class="hidden"><a targetcls="CWords" href="javascript:void(0);">中心词</a></li>');*/
        arr.push('</ul>');

        tabHolder.append($(arr.join('')));

        tabHolder.find('a').click(function(){
            var tabClicked = $(this);
            if(tabClicked.hasClass(('select'))){
                return;
            }
            var con = tabClicked.parent().parent();
            var itemId = con.attr('iid');
            contentHolder.empty();
            switch(tabClicked.attr('targetcls')){
                case 'diagResultBlock':
                    me.initDiag(contentHolder, itemId);
                    break;
                case 'competition':
                    me.initCompetition(contentHolder, itemId);
                    break;
                case 'promoteWordsBlock':
                    me.initPromoteWords(contentHolder);
                    break;
                case 'hotWordsExport':
                    me.initHotWordsExport(contentHolder,itemId);
                    break;
                case 'longTailWords':
                    me.initLongtailWords(contentHolder,itemId);
                    break;
                case 'prosList':
                    me.initProsList(contentHolder,itemId);
                    break;
                case 'keywords':
                    me.initKeywords(contentHolder,itemId);
                    break;
                case 'hotprops':
                    me.initHotProps(contentHolder,itemId);
                    break;
                case 'renamehistory':
                    me.initRenameHistory(contentHolder,itemId);
                    break;
                case 'bussearch':
                    me.initBusSearch(contentHolder);
                    break;
                case '200words':
                    me.init200words(contentHolder,numIid);
                    break;
                /*case 'CWords':
                    me.initCWords(contentHolder);
                    break;*/
            }

            tabHolder.find('.select').removeClass('select');
            tabClicked.addClass('select');
        });

        container.append(tabHolder);
        container.append(contentHolder);

        tabHolder.find('a:eq(0)').trigger('click');

    }

    me.init200words = function(container, numIid, orderBy, isDesc) {
        if(orderBy === undefined || orderBy == null || orderBy == ""){
            orderBy = "pv";
        }
        if(isDesc === undefined || isDesc == null || isDesc == ""){
            isDesc = true;
        }
        container.empty();
        var html = '' +
            '<div style="width: 100%;">' +
            '<table class="busSearch" style="width: 100%;text-align: center;">' +
            '<thead>' +
            '<tr>' +
            '<th>关键词</th>' +
            '<th class="sortTd">展现量<span class="inlineblock sort Desc" orderBy="pv"></span></th>' +
            '<th class="sortTd">点击量<span class="inlineblock sort Asc" orderBy="click"></span></th>' +
            '<th class="sortTd">行业出价<span class="inlineblock sort Asc" orderBy="price"></span></th>' +
            '<th class="sortTd">竞争指数<span class="inlineblock sort Asc" orderBy="competition"></span></th>' +
            '</tr>' +
            '</thead>' +
            '<tbody class="tmAllRecommendTbody">' +
            '</tbody>' +
            '</table>' +
            '</div>'+
            '';
        var obj = $(html);
        container.append(obj);
        me.gen200words(container, numIid, orderBy, isDesc);

    }

    me.gen200words = function(container, numIid, orderBy, isDesc){
        var tbody = container.find('.tmAllRecommendTbody');
        tbody.empty();
        $.ajax({
            on:true,
            dataType: 'jsonp',
            url: 'http://chedao.tobti.com/commons/tmAllRecommend?numIid='+numIid + '&orderBy=' + orderBy + '&isDesc=' + isDesc,
            success:function(data){
                $(data).each(function(i,word){
                    tbody.append('<tr><td>'+genKeywordSpan.gen({"text":word.word, "callback":"", "enableStyleChange":true, "spanClass":'addTextWrapperSmall'})+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+word.price+'</td><td>'+word.competition+'</td></tr>');
                });
                tbody.find('.addTextWrapperSmall').click(function () {
                    autoTitle.util.putIntoTitle($(this).text(),$(this),container);
                });
                tbody.parent().find('.sortTd').unbind('click').click(function(){
                    var orderBy = $(this).find('span').attr("orderBy");
                    if($(this).find('span').hasClass('Desc')) {
                        $(this).find('span').removeClass('Desc');
                        $(this).find('span').addClass('Asc');
                    } else {
                        $(this).find('span').removeClass('Asc');
                        $(this).find('span').addClass('Desc');
                    }
                    var isDesc = $(this).find('span').hasClass("Desc");console.log(isDesc)
                    me.gen200words(container, numIid, orderBy, isDesc);
                });
            }
        });
    }

    me.initCWords = function(container, order, sort, s) {
        if (order === undefined || order == '') {
            order = 'pv';
        }
        if (sort === undefined || sort == '') {
            sort = 'desc';
        }

        container.empty();
        var title = container.parent().parent().find('.oldTitleContent').val();
        var cid = container.parent().parent().attr("cid");
        $.ajax({
            on:true,
            dataType: 'jsonp',
            url: 'http://chedao.tobti.com/Commons/getCWord?title='+title + '&cid=' + cid,
            success:function(data){
            var searchbtn = $('<input type="text" id="bus-search-text" style="margin-left: 300px;margin-right: 15px;"><span class="tmbtn sky-blue-btn search-bus-now">立即搜索</span>');
            container.append(searchbtn);
            var estimate = $('<div class="titlesplits"></div>');
            $(data).each(function(i,word){
                estimate.append($('<span class="baseblock">'+word+'</span>'));
            });
            container.append(estimate);


            var bustable = $('<table class="bussearch" style="text-align: center;width: 100%;"><thead><tr style="text-align: center;" align="center" class="tableRow">' +
                '<th style="width: 240px;">关键词</th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">搜索指数<span class="inlineblock sort Desc" sort="pv"></span></th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">点击量<span class="inlineblock sort Asc" sort="click"></span></th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">宝贝数<span class="inlineblock sort Asc" sort="scount"></span></th>' +
                '<th>转化率</th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">性价比<span class="inlineblock sort Asc" sort="score"></span><span class="question" content="搜索量/宝贝数，搜索量除以宝贝数的指标,表示关键词的性价比"></span></th>' +
                '<th>行业平均出价</th>' +
                // '<th style="width: 120px;">添加到我的词库</th>' +
                '</tr></thead><tbody id="bus-search-tbody"></tbody></table>');

            container.append($('<div style="height:30px;text-align: center;" class="bussearch-pagingArea"></div>'));
            container.append(bustable);
            container.append($('<div style="height:30px;text-align: center;" class="bussearch-pagingArea"></div>'));
            var pageList = container.find('.bussearch-pagingArea');
            container.find('.search-bus-now').click(function(){
                var word = container.find('#bus-search-text').val();
                me.bussearch(container, pageList, bustable, order, sort, word);
            });
            //container.find('.search-bus-now').trigger('click');
            container.find('.bus-search-sort-th').click(function(){
                var order = $(this).find('.sort').attr('sort');
                var sort = "";
                if($(this).find('.sort').hasClass('Desc')){
                    $(this).find('.sort').removeClass('Desc');
                    $(this).find('.sort').addClass('Asc');
                    sort = "asc";
                } else {
                    $(this).find('.sort').removeClass('Asc');
                    $(this).find('.sort').addClass('Desc');
                    sort = 'desc';
                }
                var s = container.find('input').val();
                me.bussearch(container,pageList,bustable,order,sort,s);
            });
            container.find('.baseblock').click(function(){
                container.find('input').val($(this).text());
                container.find('.search-bus-now').trigger("click");
            });
            container.find('.baseblock').eq(0).trigger("click");
            container.show();
        }});


    }

    me.initBusSearch = function(container, order, sort, s) {
        if (order === undefined || order == '') {
            order = 'pv';
        }
        if (sort === undefined || sort == '') {
            sort = 'desc';
        }
        container.empty();
        $.post("/Titles/getCWords",{title:container.parent().parent().find('.oldTitleContent').val(), cid:container.parent().parent().attr("cid")},function(data){
            var searchbtn = $('<input type="text" id="bus-search-text" style="margin-left: 300px;margin-right: 15px;"><span class="tmbtn sky-blue-btn search-bus-now">立即搜索</span>');
            container.append(searchbtn);
            var estimate = $('<div class="titlesplits"></div>');
            $(data).each(function(i,word){
                estimate.append($('<span class="baseblock">'+word+'</span>'));
            });
            container.append(estimate);


            var bustable = $('<table class="bussearch" style="text-align: center;width: 100%;"><thead><tr style="text-align: center;" align="center" class="tableRow">' +
                '<th style="width: 240px;">关键词</th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">搜索指数<span class="inlineblock sort Desc" sort="pv"></span></th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">点击量<span class="inlineblock sort Asc" sort="click"></span></th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">宝贝数<span class="inlineblock sort Asc" sort="scount"></span></th>' +
                '<th>转化率</th>' +
                '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">性价比<span class="inlineblock sort Asc" sort="score"></span><span class="question" content="搜索量/宝贝数，搜索量除以宝贝数的指标,表示关键词的性价比"></span></th>' +
                '<th>行业平均出价</th>' +
               // '<th style="width: 120px;">添加到我的词库</th>' +
                '</tr></thead><tbody id="bus-search-tbody"></tbody></table>');

            container.append($('<div style="height:30px;text-align: center;" class="bussearch-pagingArea"></div>'));
            container.append(bustable);
            container.append($('<div style="height:30px;text-align: center;" class="bussearch-pagingArea"></div>'));
            var pageList = container.find('.bussearch-pagingArea');
            container.find('.search-bus-now').click(function(){
                var word = container.find('#bus-search-text').val();
                me.bussearch(container, pageList, bustable, order, sort, word);
            });
            //container.find('.search-bus-now').trigger('click');
            container.find('.bus-search-sort-th').click(function(){
                var order = $(this).find('.sort').attr('sort');
                var sort = "";
                if($(this).find('.sort').hasClass('Desc')){
                    $(this).find('.sort').removeClass('Desc');
                    $(this).find('.sort').addClass('Asc');
                    sort = "asc";
                } else {
                    $(this).find('.sort').removeClass('Asc');
                    $(this).find('.sort').addClass('Desc');
                    sort = 'desc';
                }
                var s = container.find('input').val();
                me.bussearch(container,pageList,bustable,order,sort,s);
            });
            container.find('.baseblock').click(function(){
                container.find('input').val($(this).text());
                container.find('.search-bus-now').trigger("click");
            });
            container.find('.baseblock').eq(0).trigger("click");
            container.show();
        });


    }

    me.bussearch = function (container,pageList,bustable, order, sort, s) {
        if (order === undefined || order == '') {
            order = 'pv';
        }
        if (sort === undefined || sort == '') {
            sort = 'desc';
        }

        pageList.tmpage({
            currPage:1,
            pageSize:10,
            pageCount:1,
            ajax:{
                on:true,
                dataType:'json',
                url:"/words/busSearch",
                param:{order:order, sort:sort, word:s},
                callback:function (data) {
                    bustable.find('#bus-search-tbody').empty();
                    if (data != null) {
                        if (data.res.length > 0) {
                            $(data.res).each(function (i, myword) {
                                if (myword.word != "") {
                                    var pv = myword.pv > 0 ? myword.pv : "~";
                                    var click = myword.click > 0 ? myword.click : "~";
                                    var scount = myword.scount > 0 ? myword.scount : "~";
                                    var score = myword.score > 0 ? myword.score : "~";
                                    bustable.find('#bus-search-tbody').append($('<tr><td class="word-content">' + genKeywordSpan.gen({"text":myword.word, "callback":"", "enableStyleChange":true, "spanClass":'addTextWrapperSmall'}) + '</td><td>' + pv + '</td><td>' + click + '</td><td>' + scount + '</td><td>' + myword.transRate + '</td><td>' + score + '</td><td>' + myword.bidPrice + '</td></tr>'))
                                }
                            })
                            bustable.find('#bus-search-tbody').find('tr:even').addClass('even');
                            // add to mywords
                            /*bustable.find('.add-to-mywords span').click(function () {
                                $.post('/KeyWords/addMyWord', {word:$(this).parent().parent().find('.word-content').text()}, function (data) {
                                    TM.Alert.load(data, 400, 300, function () {
                                    }, "", "添加到词库");
                                });
                            });*/

                            // add to mylexicon words
                            /*bustable.find('.add-to-mywords span').click(function () {
                                $.ajax({
                                    url:"/Words/addmylexicon",
                                    data:{wordId:$(this).parent().parent().find('.word-content').text()},
                                    success:function(data){
                                        TM.Alert.load(data);
                                    }
                                });
                            });*/
                            bustable.find('.addTextWrapperSmall').click(function () {
                                autoTitle.util.putIntoTitle($(this).text(),$(this),container);
                            });
                        } else {
                            bustable.find('#bus-search-tbody').append($('<td colspan="9"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，请输入关键词搜索</p></td>'));
                        }

                    }
                }
            }
        });
    }

    me.initRenameHistory = function(container, numIid) {
        container.empty();
        var pageList = $('<div style="height:30px;text-align: center;" class="pagingArea"></div><div class="table-area"></div><div style="height:30px;text-align: center;" class="pagingArea"></div>');
        container.append(pageList);
        container.find('.pagingArea').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/titles/renameHistory",
                param:{numIid : numIid},
                callback:function(data){
                    if(!data) {
                        TM.Alert.load("亲，获取数据出错啦，请重试或联系客服");
                    } else {
                        if(data.res.length > 0) {
                            var table = ''+
                                '<table class="renameHistoryTable"> '+
                                '    <thead>'+
                                '           <tr class="renameHistoryTr">'+
                                '               <td class="rename-history-oldtitle" style="width: 265px;">旧标题</td>'+
                                '               <td class="rename-history-newtitle" style="width: 265px;">新标题</td>'+
                                '               <td class="rename-history-optimise-ts" style="width: 160px;">优化时间</td>'+
                                '               <td style="width: 110px;">还原为旧标题</td>'+
                                '           </tr>'+
                                '       </thead>'+
                                '   </table>'+
                                '';
                            var tableObj = $(table);
                            var tbody = autoTitle.util.createRenameHistoryTbody(data.res);
                            tableObj.append(tbody);
                            container.find('.table-area').empty();
                            container.find('.table-area').append(tableObj)
                        }

                    }
                }
            }
        });
        container.show();

    }

    me.initHotProps = function(container, numIid) {
        container.empty();
        var htmls = [];
        $.get('/items/catProps',{numIid : numIid},function(res){
//            htmls.push('<table><thead><th>属性名</th><th>热销属性词</th></thead><tbody>');
            htmls.push('<table class="oplogs"><thead><th style="width:120px;">属性名</th><th>热销属性词/搜索热度</th></thead><tbody>');
            if(!res || res.length == 0){
                // TODO no res temp...
                htmls.push('<tr><td colspan="2">您这个类目暂无热销属性哟,您也可以联系客服,如果是您发现了升流量的存在的问题,我们会给您奖励一个月的使用时间</td></tr>');
                return;
            }else{

            var pPvMax = 0;
                $.each(res, function(i, prop){
                    if(i == 0){
                        pPvMax = prop.pv;
                    }

                    htmls.push('<tr><td class="greybottom"><div><b>'+prop.pname+'</b></div><div>热度:<b class="red"> '+prop.pv+'</b></div></td>');
                    // values...
                    var maxWidth = 300;
                    htmls.push('<td class="greybottom" > <table width="100%">');
                    var vPvMax = 0;
                    $.each(prop.list, function(j, value){
                        if(j == 0 && vPvMax < value.pv){
                            vPvMax = value.pv;
                        }
                        if(vPvMax < 1 ){
                            vPvMax = 1;
                        }

                        var currWidth = (j==0)?maxWidth:(value.pv*maxWidth/vPvMax);
                        currWidth = Math.round(currWidth);
                        if(currWidth < 5){
                            currWidth = 5;
                        }

    //                    genKeywordSpan.gen({"text":data[i].word,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})
                        htmls.push('<tr><td width="140px"><span class="addTextWrapper shadowbase" style="padding-top:5px;"><img src="/img/btns/addblue.png" >'+value.name+'</span></td><td style="text-align: left;width:300px;"><div style="width:'+
                            currWidth+'px;height:6px;border-top: 6px solid #2D8ABE"></div></td><td>'+value.pv+'</td></tr>');
                    });

                    htmls.push('</table></td></tr>');
                })
            }
            htmls.push('</tbody></table>');
            var res = $(htmls.join(''));
            res.find('.addTextWrapper').click(function(){
                autoTitle.util.putIntoTitle($(this).text(),$(this),container);
            });
            container.append(res);
        });

    }

    me.initKeywords = function(container, numIid) {

        container.empty();
        container.append($('<div style="margin-left: 30px;"><span style="font-size: 13px;color: #2796F9;font-weight: bold;margin-right: 10px;">关键词：</span><input style="margin-right: 15px;width: 300px;" value="袜子"><a href="javascript:void(0);" class="searchKeywords tmbtn sky-blue-btn" style="display: inline-block;">立即搜索</a></div>'));

        container.find('.searchKeywords').click(function(){
            container.find('.blank0').remove();
            var word = container.find('input').val();
            var title = container.parent().parent().find('.oldTitleContent').val();
            var pageList = container.find('.pagingArea');
            if(pageList.length == 0) {
                pageList = $('<div style="height:30px;text-align: center;" class="pagingArea"></div><div class="blank0" style="3px;"></div>');
            }
            pageList.empty();
            var pageListtop = pageList.clone();
            pageList.empty();
            pageListtop.empty();
            container.append(pageList);
            container.append($('<div class="table-contaier"></div>'));
            container.append(pageListtop);
            container.find('.pagingArea').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/words/searchKeywords",
                    param:{s:word,numIid : numIid,title:title},
                    callback:function(data){
                        if(!data.isOk){
                            // TODO, no res...
                        }
                        var table = container.find('.keywordsTable');
                        if(table.length == 0){
                            table = $('<table class="keywordsTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
                        }
                        table.empty();
                        table.append($('<thead ><th style="width: 380px;">关键词列表</th><th style="width: 100px;">热卖指数</th><th style="width: 100px;">搜索指数</th><th style="width: 100px;">竞争指数</th><th style="width: 100px;">获取相关推荐词</th></thead>'));
                        $(data.res).each(function(i,word){
                            table.append($('<tr word="'+word.word+'"><td class="word">'+genKeywordSpan.gen({"text":word.word,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+word.competition+'</td><td class="searchMore" style="cursor: pointer;">查看更多</td></tr>'));
                        });
                        table.find('span').css("margin","5px 0 5px 0");
                        table.find('tr:odd').addClass('bgwhite');
                        table.find('span').click(function(){
                            autoTitle.util.putIntoTitle($(this).text(),$(this),container);
                        });
                        table.find('.searchMore').click(function(){
                            container.find('input').val($(this).parent().attr("word"));
                            container.find('.searchKeywords').trigger('click');
                        });
                        container.find('.table-contaier').append(table);
                        container.append('<div class="blank0" style="height:8px;"></div>');
                    }
                }
            });
            container.show();
        });
        $.post("/Titles/getCWords",{title:container.parent().parent().find('.oldTitleContent').val(), cid:container.parent().parent().attr("cid")},function(data){
            var estimate = $('<div class="titlesplits"></div>');
            $(data).each(function(i,word){
                estimate.append($('<span class="baseblock">'+word+'</span>'));
            });
            container.append(estimate);
            container.find('.baseblock').click(function(){
                container.find('input').val($(this).text());
                container.find('.searchKeywords').click();
            });
            container.find('.baseblock:eq(0)').click();
        });
    }

    me.initLongtailWords = function(container, numIid) {
        container.empty();
        container.append($('<div style="margin-left: 30px;"><span style="font-size: 13px;color: #2796F9;font-weight: bold;margin-right: 10px;">长尾词根：</span><input style="margin-right: 15px;width: 300px;"><a href="javascript:void(0);" class="searchLongtailWords tmbtn sky-blue-btn" style="display: inline-block;">立即搜索</a></div>'));
        $.post("/Titles/getCWords",{title:container.parent().parent().find('.oldTitleContent').val(), cid:container.parent().parent().attr("cid")},function(data){
            var estimate = $('<div class="titlesplits"></div>');
            $(data).each(function(i,word){
                estimate.append($('<span class="baseblock">'+word+'</span>'));
            });
            container.append(estimate);
            container.find('.baseblock').click(function(){
                container.find('input').val($(this).text());
                container.find('.searchLongtailWords').trigger("click");
            });
            container.find('.baseblock').eq(0).trigger("click");
        });
        container.find('.searchLongtailWords').click(function(){
            var word = container.find('input').val();
            $.post("/Titles/longTail",{s:word,numIid:numIid}, function(data){
                var table = container.find('.longtailTable');
                if(table.length == 0){
                    table = $('<table class="longtailTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
                }
                table.empty();
                table.append($('<thead ><th style="width: 480px;">长尾词列表</th><th style="width: 300px;">获取相关长尾词</th></thead>'));
                $(data).each(function(i,word){
                    table.append($('<tr style="height: 30px;" word="'+word+'"><td class="word">'+genKeywordSpan.gen({"text":word,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td><td class="searchMore" style="cursor: pointer;">查看更多</td></tr>'));
                });
                table.find('span').css("margin","5px 0 5px 0");
                table.find('span').click(function(){
                    autoTitle.util.putIntoTitle($(this).text(),$(this),container);
                });
                table.find('.searchMore').click(function(){
                    container.find('input').val($(this).parent().attr("word"));
                    container.find('.searchLongtailWords').trigger('click');
                });
                container.append(table);

            });
        });
    }

    me.initProsList = function(container, numIid) {
        container.empty();
        //container.find('.searchLongtailWords').click(function(){
        $.get("/Titles/props",{numIid:numIid}, function(data){
            var table = container.find('.propsTable');
            if(table.length == 0){
                 table = $('<table class="propsTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
            }
            table.empty();
            /*table.append($('<thead ><th style="width: 200px;">属性列表</th><th style="width: 580px;">属性内容</th></thead>'));
            $(data).each(function(i,word){
                table.append($('<tr style="height: 30px;"><td class="word">'+word.key+'</td><td class="searchMore" style="cursor: pointer;">'+genKeywordSpan.gen({"text":word.value,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td></tr>'));
            });*/
            var trsize = 3;
            var tmpTr;
            $(data).each(function(i,word){
                if(i%trsize == 0){
                    var trObj = $('<tr style="height: 30px;width: 100%;"></tr>');
                    tmpTr = trObj;
                    table.append(tmpTr);
                }
                tmpTr.append($('<td class="word" style="width: 258px;"><span class="inlineblock" style="width: 98px;">'+word.key+':</span>'+genKeywordSpan.gen({"text":word.value,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td>'));
            });
            table.find('span').css("margin","5px 0 5px 0");
            table.find('span').click(function(){
                autoTitle.util.putIntoTitle($(this).text(),$(this),container);
            });

            container.append(table);
        });
        //});

    }

    me.initHotWordsExport = function(container, numIid) {
        container.empty();
        var href="/Items/downloadWords?numIid=" + numIid;
        var exportAll = $('<span class="exportAllHotWords commbutton btntext6"><a style="text-decoration: none;color: #ffffff;">导出全部热词</a></span>');
        exportAll.find("a").attr("href",href);
        container.append(exportAll);
        var wordsTable = $('<table class="wordsTable"></table>');
        wordsTable.append($('<thead><tr><td class="wordContent">热词</td><td class="wordPrice">行业出价</td><td class="wordClick">点击数</td></tr></thead>'));
        container.append(wordsTable);
        $.post("/Items/getWords",{numIid : numIid}, function(data){
            var tbody = $('<tbody></tbody>')
            var showLength = data.length > 20 ? 20 : data.length;
            for(var i = 0; i < showLength; i++) {
                var trObj = $('<tr class="wordTr"><td class="wordContent">'+genKeywordSpan.gen({"text":data[i].word,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td><td class="wordPrice">'+data[i].price/100+'</td><td class="wordClick">'+data[i].click+'</td></tr>');
                tbody.append(trObj);
            }
            tbody.find('span').click(function(){
                autoTitle.util.putIntoTitle($(this).text(),$(this),container);
            });
            container.append(tbody);
        });
    }

    me.initPromoteWords = function(container){
        container.empty();

        $.ajax({
            url: '/titles/getPromoteWords',
            dataType: 'json',
            type: 'post',
            data: {},
            error: function() {
            },
            success: function (promoteArray) {
                var rowCount = 5;
                var count = 0;
                var html = [];

                html.push('<table class="promoteWordsTable">');
                html.push('<tbody>');
                html.push('<tr>')
                $(promoteArray).each(function(index, promoteWord) {
                     var spanObj = genKeywordSpan.gen({"text":promoteWord,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true});
                    if(count++ == rowCount){
                        count = 1;
                        html.push('</tr></tr>');
                    }
                    html.push('<td>'+spanObj+'</td>');
                });
                html.push('</tbody>');
                html.push('</table>');
                var table = $(html.join(''));
                table.find('span').click(function(){
                    autoTitle.util.putIntoTitle($(this).text(),$(this),container);
                });

                $(table).appendTo(container);
                //ModifyTitle.util.hideLoading();
            }
        });
        container.show();
    }


    me.initDiag = function(container, numIid){
        container.empty();
        var title = container.parent().parent().find('.oldTitleContent').val();
        $.post('/titles/singleDiag',{numIid : numIid, title : title}, function(data){
            container.parent().parent().find(".oldTitleScore").html(data.score);
            container.append(QueryCommodity.commodityDiv.createDetail(data));
           // TM.Alert.load(QueryCommodity.commodityDiv.createDetail(data),780,520,function(){},false,"诊断结果: "+title);
        });
        container.show();
    }
    me.initCompetition = function(container, numIid){
        var pageList = $('<div style="height:30px;text-align: center;" class="pagingArea"></div>');
        container.append(pageList);
        pageList.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/titles/topTitle",
                param:{numIid : numIid},
                callback:function(data){
                    if(!data.isOk){
                        // TODO, no res...
                    }
                    if(container.find('.itemShowTable').length == 0) {
                        var table = $('<table class="itemShowTable"></table>');
                    } else {
                        var table =container.find('.itemShowTable');
                        table.empty();
                    }

                    var htmls = [];
                    htmls.push('<thead><th>宝贝图片</th><th>宝贝标题</th><th style="width:50px;">月销量</th></thead>');
                    $.each(data.res,function(i, item){
                        htmls.push('<tr numIid="'+item.numIid+'">');
                        htmls.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+item.numIid+'"><img class="itemsnap" src="'+item.picPath+'" /></a></td>');
                        //htmls.push('<td><div>'+item.title+'</div><div>'+item.splits.join(',')+'</div></td>');
                        htmls.push('<td><div class="topItemTitle">'+item.title+'</div><div class="topItemSplitWords">');
                        for(var i = 0; i < item.splits.length; i++) {
                            htmls.push(genKeywordSpan.gen({"text":item.splits[i],"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true,"spanClass":'addTextWrapperSmall'}));
                        }
                        htmls.push('</div></td>');
                        htmls.push('<td>'+item.sale+'</td>');
                        htmls.push('</tr>');
                    })
                    table.append($(htmls.join('')));
                    table.find('span').click(function(){
                        autoTitle.util.putIntoTitle($(this).text(),$(this),container);
                    });
                    table.find('tr:even').addClass('even');
                    container.append(table);
                }
            }
        });
        container.show();
    }

})(jQuery, window));





autoTitle.util = autoTitle.util || {};
autoTitle.util = $.extend({
    checkItemsCountLimitOrNot : function(){
        return (parseInt($.cookie("userItemsCount")) - parseInt($.cookie("itemCountLimit"))) > 0 ? true : false;
    },
    getParams : function(){
        var params = {};
        var status = $("#itemsStatus option:selected").attr("tag");
        switch(status){
            case "onsale":params.status=0;break;
            case "instock" : params.status=1;break;
            default : params.status=2;break;
        }

        var catId = $('#itemsCat option:selected').attr("catId");
        params.catId = catId;

        var cid = $('#taobaoCat option:selected').attr("catId");
        params.cid = cid;

        var sort = $('#itemsSortBy option:selected').attr("tag");
        switch(sort){
            case "sortByScoreUp" : params.sort=1;break;
            case "sortByScoreDown" : params.sort=2;break;
            case "sortBySaleCountUp" : params.sort=3;break;
            case "sortBySaleCountDown" : params.sort=4;break;
            default : params.sort=1;break;
        }

        params.lowBegin = $('#lowScore').val();
        params.topEnd = $('#highScore').val();
        params.s = $('#searchWord').val();
        return params;
    },
    modifyTitle : function(numIid, newTitle,oldTitleInput){
        var params = {};
        params.numIid = numIid;
        params.title = newTitle;
        $.post("/Titles/rename",params,function(res){
            if(!res){
                TM.Alert.load("标题修改失败！请您稍后重试哟!");
            }else if(res.ok){
                TM.Alert.load("标题修改成功！");
                if(oldTitleInput.val()!=newTitle) {
                    oldTitleInput.val(newTitle);
                    oldTitleInput.trigger("keyup");
                }
            }else{
                TM.Alert.load(res.msg);
            }
        });
    },
    putIntoTitle : function(text,spanObj,container){
        var newTitle = container.parent().parent().find(".oldTitleContent");
        if(autoTitle.util.countCharacters(newTitle.val()+text) > 60) {
            spanObj.qtip({
                content: {
                    text: "标题长度将超过字数限制，请先删减标题后再添加~"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        }
        else {
            var start = {}, end = {};
            start.left = spanObj.offset().left+"px";
            start.top = spanObj.offset().top+"px";
            end.left = container.parent().parent().find(".oldTitleContent").offset().left+"px";
            end.top = container.parent().parent().find(".oldTitleContent").offset().top+"px";
            autoTitle.util.flyFromTo(start,end,spanObj,function(){
                /*var dthis = newTitle[0];
                if(document.selection){
                    dthis.focus();
                    var fus = document.selection.createRange();
                    fus.text = text;
                    dthis.focus();
                }
                else if(dthis.selectionStart || dthis.selectionStart == '0'){
                    var start = dthis.selectionStart;
                    var end =dthis.selectionEnd;
                    dthis.value = dthis.value.substring(0, start) + text + dthis.value.substring(end, dthis.value.length);
                }
                else{this.value += text; this.focus();}*/
                newTitle.val(newTitle.val()+text);
                 newTitle.trigger("keyup");
                 container.parent().parent().find(".oldTitleRemainWordsNum").html(30-newTitle.val().length);
            })



            spanObj.qtip({
                content: {
                    text: "已添加至标题尾部哟"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        }
    },
    flyToTitle : function($this,$targrt){
        var start = {}, end = {};
        start.left = $this.offset().left+"px";
        start.top = $this.offset().top+"px";
        end.left = $targrt.offset().left+"px";
        end.top = $targrt.offset().top+"px";
        autoTitle.util.flyFromTo(start,end,$this,null)
    },
    flyFromTo : function(start,end,flyObj,callback){
        //var img = $('<span id="fly-from-to-img" class="inlineblock" style="z-index:200001;position: absolute;top:'+start.top+';left: '+start.left+'"></span>');
        var obj = flyObj.clone();
        obj.css("position","absolute");
        obj.css('left',start.left);
        obj.css('top',start.top);
        obj.appendTo($('body'));

        obj.animate({top:end.top,left:end.left},1500, function(){
            obj.fadeOut(1000,function(){
                obj.remove();
            });
            callback && callback();
        });
    },
    createBatchOPResult : function(data){
        if(!data || data.length==0 || data.failNum == 0) {
//            TM.Alert.load("批量应用推荐标题成功"+((!data||data.length==0)?0:data.successNum)+"个，失败0个~,点击确定刷新数据",400,300,TM.sync);
            TM.Alert.loadDetail("<div class='largp'><p>批量推荐标题成功"+((!data||data.length==0)?0:data.successNum)+
                "个，失败0个~,点击确定刷新页面</p><p>若您对标题优化的结果不满意,您可以稍后在<b>左侧导航标题还原中心</b>进行还原操作,非常感谢</p><p>若有问题,也欢迎联系我们的客服,非常感谢</p></div>",
                850,350,function(){window.location.hash='score-desc';$('.gopage-submit').click();},'推荐成功');
            return;
        }

        var multiModifyArea = $('.multiModifyArea');
        if(multiModifyArea.length == 0) {
            var multiModifyArea=$('<div class="multiModifyArea"></div>');
        }
        multiModifyArea.empty();
        multiModifyArea.css("display","block");
        multiModifyArea.css("left",(screen.width-1000)/2 + 200 +"px");

        //var tableDiv =  multiModifyArea.find('#tableDiv');
        //if(tableDiv.length==0) {
            var tableDiv=$('<div id="tableDiv"></div>');
       // }
       // tableDiv.empty();
        var tableObj=MultiModify.createErrTable.createTableObj(data);
        tableDiv.append(tableObj);
        multiModifyArea.append(tableDiv);
        //var exitBatchOPMsg = multiModifyArea.find('.exitBatchOPMsg');
        //if(exitBatchOPMsg.length>0) {
        //   exitBatchOPMsg.remove();
        // }
        var successNum = $('<span >'+'批量推荐标题成功:'+'<span class="successNum">'+data.successNum+'</span>'+'</span>') ;
        var failNum = $('<span >'+'批量推荐标题失败:'+'<span class="failNum">'+data.failNum+'</span>'+'</span>') ;
        multiModifyArea.append(successNum);
        multiModifyArea.append(failNum);
        multiModifyArea.append($('<div style="width:600px;display: inline-block;"></div>'));
        multiModifyArea.append($('<div class="exitBatchOPMsg"><span ></span></div>'));
        multiModifyArea.find('.exitBatchOPMsg').click(function(){
            multiModifyArea.hide();
        });

        multiModifyArea.appendTo($("body"));
    },
    updateRecomTitle : function(data){
        $('.singleDiagRes').each(function(){
            if(data[$(this).attr("numiid")] !== undefined){
                $(this).find('.newTitleContent').val((data[$(this).attr("numiid")]));
                $(this).find('.newTitleContent').removeClass('WaitContent');
            }
        });

        //alert(data)
    },
    updateGuanfangRecomTitle : function(data){
        $('.singleDiagRes').each(function(){
            if(data[$(this).attr("numiid")] !== undefined){
                $(this).find('.fenxiaoTitleContent').val((data[$(this).attr("numiid")]));
                $(this).find('.fenxiaoTitleContent').removeClass('WaitContent');
            }
        });

        //alert(data)
    },
    countCharacters : function(str){
        var totalCount = 0;
        for (var i=0; i<str.length; i++) {
            var c = str.charCodeAt(i);
            if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                totalCount++;
            }else {
                totalCount+=2;
            }
        }
        return totalCount;
    },
    createRenameHistoryTbody : function (results) {
        var tbody = $('<tbody></tbody>');
        $(results).each(function(i,result){
            tbody.append('<tr numIid="'+result.numIid+'">' +
                '<td class="rename-history-oldtitle">'+result.oldTitle+'</td>'+
                '<td class="rename-history-newtitle">'+result.newTitle+'</td>'+
                '<td class="rename-history-optimise-ts">'+new Date(result.created).formatYMDHMS()+'</td>'+
                '<td><span class="set-old-title-back tmbtn sky-blue-btn">还原</span></td>'+
                '</tr>');
        });
        tbody.find('.set-old-title-back').click(function(){
            var oldTitle = $(this).parent().parent().find('.rename-history-oldtitle').text();
            var numIid = $(this).parent().parent().attr("numIid");
            var data = {};
            data.numIid = numIid;
            data.title = oldTitle;
            //弹出loading动画
            //ModifyTitle.util.showLoading();
            $.ajax({
                url: '/Titles/rename',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {
                    alert("标题修改失败！");
                },
                success: function (res) {
                    if(!res){
                        alert("标题修改失败！请您稍后重试哟!");
                    }else if(res.ok){
                        //诊断新标题
                        //Diagnose.newTitle.doDiagnose(params, originScore);
                        TM.Alert.loadDetail("亲，标题修改成功，点击确定刷新",400,300,function(){
                            location.reload();
                        })
                    }else{
                        alert(res.msg);
                    }

                   // ModifyTitle.util.hideLoading();
                }
            });
        });
        return tbody;
    }
},autoTitle.util);


autoTitle.Event = autoTitle.Event || {};
autoTitle.Event = $.extend({
    initEvent : function(){
        autoTitle.Event.setCheckAllEvent();
        autoTitle.Event.setSubcheckEvent();
        autoTitle.Event.setShowDetailTagEvent();
        autoTitle.Event.setSaveCurrentTitleEvent();
        autoTitle.Event.setSaveRecommendTitleEvent();
        autoTitle.Event.setSaveFenxiaoTitleEvent();
        autoTitle.Event.setGOSearchEvent();
        autoTitle.Event.setsearchWordEnterEvent();
        autoTitle.Event.setCloseWarmNoticeEvent();
        autoTitle.Event.setSelectBatch();
        autoTitle.Event.setAllUseRecommendBatch();
        autoTitle.Event.setAllUseGuanfangBatch();
        autoTitle.Event.setAllUseGuanfangRecomBatch();
        autoTitle.Event.setDiagCurrentTitle();
    },
    setCheckAllEvent : function(){
        $("#checkAllItems").click(function(){
            $('input[name="subCheck"]').attr("checked",this.checked);
        });
    },
    setSubcheckEvent : function(){
        $(".subCheckBox").click(function(){
            var $subBox = $("input[name='subCheck']");
            $("#checkAllItems").attr("checked",$subBox.length == $("input[name='subCheck']:checked").length ? true : false);
        });
    },
    setShowDetailTagEvent : function(){
        $('.showDetailTag').click(function(){

            if($(this).hasClass("fadeout")) {
                $(this).removeClass("fadeout");
                $(this).addClass("fadein");
                $(this).html('展开手动优化');
                $(this).parent().parent().find(".diagDetailInf").fadeOut(300);
            }else {
                $(this).removeClass("fadein");
                $(this).addClass("fadeout");
                $(this).html('收起手动优化');
                var  detail = $(this).parent().parent().find(".diagDetailInf");
                autoTitle.ItemDetail.init(detail,detail.attr('numIid'));
                detail.fadeIn(300);
            }
        });
        //$('.showDetailTag:eq(0)').trigger('click');
    },
    setSaveCurrentTitleEvent : function(){
        $('.saveCurrentTitle').click(function(){
            var currentTitle = $(this).parent().parent().parent().parent().find('.oldTitleContent').val();
            if(autoTitle.util.countCharacters(currentTitle) > 60) {
                alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
            }else {
                var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                autoTitle.util.modifyTitle(numIid,currentTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
            }

            /*params.numIid = $(this).parent().parent().parent().parent().attr("numIid");
            params.title = currentTitle;
            $.post("/Titles/rename",params,function(res){
                if(!res){
                    TM.Alert.load("标题修改失败！请您稍后重试哟!");
                }else if(res.ok){
                    TM.Alert.load("标题修改成功！");
                }else{
                    TM.Alert.load(res.msg);
                }
            });*/
        });
    },
    setDiagCurrentTitle : function(){
        $('.diagCurrentTitle').click(function(){
            var oldTitle = $(this).parent().parent().find('.oldTitleContent').val();
            if(autoTitle.util.countCharacters(oldTitle) > 60) {
                alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
            } else {
                /*if($(this).parent().parent().parent().parent().parent().parent().find('.diagDetailInf ').css("display")=="none"){
                    $(this).parent().parent().parent().parent().parent().parent().find('.showDetailTag').trigger("click");
                } else {*/
                    /*if($(this).parent().parent().parent().parent().parent().parent().find('a[targetcls="diagResultBlock"]').hasClass("select")){
                        var contentHolder = $(this).parent().parent().parent().parent().parent().parent().find('.liTargetDiv');
                        var numIid = $(this).parent().parent().parent().parent().parent().parent().attr("numiid");
                        autoTitle.ItemDetail.initDiag(contentHolder, numIid);
                    } else {
                        $(this).parent().parent().parent().parent().parent().parent().find('a[targetcls="diagResultBlock"]').trigger("click");
                    }*/
                    var contentHolder = $(this).parent().parent().parent().parent().parent().parent().find('.liTargetDiv');
                    var numIid = $(this).parent().parent().parent().parent().parent().parent().attr("numiid");
                    var title = $(this).parent().parent().find('.oldTitleContent').val();
                    var titleScoreNumber = $(this).parent().find(".titleScoreNumber");
                    $.post('/titles/singleDiag',{numIid : numIid, title : title}, function(data){
                        titleScoreNumber.html(data.score);
                        /*if(autoTitle.util.countCharacters(title) > 40){
                           // titleScoreNumber.empty();
                           // titleScoreNumber.append($('<span class="titleScoreNumber">&nbsp;良好</span>'));
                        } else {
                           // titleScoreNumber.empty();
                           // titleScoreNumber.append($('<span class="titleScoreNumber">&nbsp;不够理想</span>'));
                        }*/

                        TM.Alert.load(QueryCommodity.commodityDiv.createDetail(data),820,550,function(){},false,"诊断结果: "+title);
                    });

                }

        });
    },
    setSaveRecommendTitleEvent : function(){
        $('.saveRecommendTitle').click(function(){
            var recommendTitle = $(this).parent().parent().parent().parent().find('.newTitleContent').val();
            if(autoTitle.util.countCharacters(recommendTitle) > 60) {
                alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
            } else {
                var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                autoTitle.util.modifyTitle(numIid,recommendTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
            }
        });
    },
    setSaveFenxiaoTitleEvent : function(){
        $('.saveFenxiaoTitle').click(function(){
            var fenxiaoTitle = $(this).parent().parent().parent().parent().find('.fenxiaoTitleContent').val();
            if(autoTitle.util.countCharacters(fenxiaoTitle) > 60) {
                alert("官方标题已超过淘宝字数限制，请删减后再提交");
            } else {
                var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                autoTitle.util.modifyTitle(numIid,fenxiaoTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
            }
        });
    },
    setGOSearchEvent : function(){
        $('#goSearchItems').click(function(){
            autoTitle.ItemsDiag.getItemsDiag($.cookie("isFenxiao"));
        });
    },
    setsearchWordEnterEvent:function () {
        $("#searchWord").keydown(function(event) {
            if (event.keyCode == "13") {//keyCode=13是回车键
                autoTitle.ItemsDiag.getItemsDiag($.cookie("isFenxiao"));
            }
        });
    },
    setCloseWarmNoticeEvent : function(){
        $("#closeImg").click(function(){
            if($("#warmNotice ol").css("display")=="block")
            {
                $("#warmNotice ol").slideUp('normal');
                $("#closeImg").attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2S5O1XoJXXXXXXXXX_!!1039626382.gif");
            }
            else if($("#warmNotice ol").css("display")=="none")
            {
                $("#warmNotice ol").slideDown('normal');
                $("#closeImg").attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2No11XjNXXXXXXXXX_!!1039626382.gif");
            }
        });
    },
    setSelectBatch : function(){
        $('.batchSelectUseRecommend').click(function(){
            var idArr = [];
            var titles = [];
            if($(".itemsWithDiagResult").find(".subCheckBox:checked").length == 0){
                TM.Alert.load("亲，您还没有选择宝贝哦");
                return;
            }
            $.each($(".itemsWithDiagResult").find(".subCheckBox:checked"),function(i, input){
                var oThis = $(input);
                idArr.push(oThis.attr('numIid'));
                titles.push(oThis.parent().parent().parent().find(".newTitleContent").val());
            });
            //timeout: 30000s
            $.ajax({
                url : '/titles/batchChange',
                data:{numIids:idArr.join(','),titles:titles.join(',:,')},
                timeout: 200000,
                type:'post',
                success : function(data){
                    autoTitle.util.createBatchOPResult(data);
                }
            });
        });
    },
    setAllUseRecommendBatch : function(){


//        $('.batchAllUseRecommend').click(function(){
//            autoTitle.Event.batchRecommend();
//            if(confirm("点击确定,进行全店一个键自动标题优化")){
//                $.get('/titles/batchChangeALl',function(data){
////                    autoTitle.util.createBatchOPResult(data);
//
//                })
//            }
//        });
        $('.batchAllUseRecommend').click(function(){
            if(!TM.isAutoOnekeyOK){
                TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
                return;
            }
            $.get('/items/itemCatCount',function(itemCatCount){
                $.get('/items/sellerCatCount',function(sellerCatCount){
                    autoTitle.Event.batchRecommend(itemCatCount,sellerCatCount,0);
                })
            });
        });
    },
    setAllUseGuanfangBatch : function() {
        $('.batchAllUseGuanfang').click(function(){
            if(!TM.isAutoOnekeyOK){
                TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
                return;
            }
            $.get('/items/itemCatCount',function(itemCatCount){
                $.get('/items/sellerCatCount',function(sellerCatCount){
                    autoTitle.Event.batchRecommend(itemCatCount,sellerCatCount,1);
                })
            });
        });
    },
    setAllUseGuanfangRecomBatch : function() {
        $('.batchAllUseGuanfangRecomm').click(function(){
            if(!TM.isAutoOnekeyOK){
                TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
                return;
            }
            $.get('/items/itemCatCount',function(itemCatCount){
                $.get('/items/sellerCatCount',function(sellerCatCount){
                    autoTitle.Event.batchRecommend(itemCatCount,sellerCatCount,2);
                })
            });
        });
    },
    advancedBatchRecommend : function(itemCatCount, sellerCatCount, isFengxiao){
        var idArr = [];
        var titles = [];

//        var opt = $("#autorecommendopt");
//        opt.remove();
       // if(!opt || opt.length ==0){
//            var htmls = [];
//            htmls.push("<form id='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
//            htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option value="false">否.只优化没有销量的宝贝</option><option name="sale" value="true" >是,不限销量</option></select></td></tr>')
//            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
//            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
//            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart" style="width:200px"></td></div>')
//            htmls.push('<tr><td>标题不包含以下词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded" style="width:200px"></td></div>')
//            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
//            htmls.push('</table></form>');
//            opt = $(htmls.join(''));
       // }

        var content = $('<div></div>')
        var opt = $('#autorecommendoptTmpl').tmpl().appendTo(content);

        TM.Alert.loadDetail(content,700,390,function(){
            var idArr = [];
            $.each($(".itemsWithDiagResult").find(".subCheckBox:checked"),function(i, input){
                var oThis = $(input);

                oThis.parent().parent().parent().find('.fenxiaoTitleContent').val("");
                oThis.parent().parent().parent().find('.fenxiaoTitleContent').addClass('WaitContent');
                oThis.parent().parent().parent().find('.newTitleContent').val("");
                oThis.parent().parent().parent().find('.newTitleContent').addClass('WaitContent');

                idArr.push(oThis.attr('numIid'));
            });
            $.ajax({
                url : '/titles/advancedBatchRecommend',
                type : 'post',
                data:{
                    numIids : idArr.join(","),

                    'opt.fixedStart' : content.find('input[name=fixedStart]').val(),
                    'opt.allSale' : content.find('.allsale option:selected').attr('value'),
                    'opt.keepBrand' : content.find('input[name=brand]:checked').attr('value'),
                    'opt.keepSerial' : content.find('input[name=serialNum]:checked').attr('value'),
                    'opt.mustExcluded' : content.find('input[name=mustExcluded]').val(),
                    'opt.toAddPromote' : content.find('input[name=promotewords]:checked').attr('value'),
                    'opt.noColor' : content.find('input[name=noColor]:checked').attr('value'),
                    'opt.noNumber' : content.find('input[name=noColor]:checked').attr('value')
                },
                timeout: 200000,
                success : function(data){
                    //autoTitle.util.createBatchOPResult(data)
                    autoTitle.util.updateRecomTitle(data);
                }
            });
            if(isFengxiao){
                $.ajax({
                    url : '/titles/advancedFengxiaoRecommend',
                    type : 'post',
                    data:{
                        numIids : idArr.join(","),

                        'opt.fixedStart' : content.find('input[name=fixedStart]').val(),
                        'opt.allSale' : content.find('.allsale option:selected').attr('value'),
                        'opt.keepBrand' : content.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : content.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : content.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : content.find('input[name=promotewords]:checked').attr('value'),
                        'opt.noColor' : content.find('input[name=noColor]:checked').attr('value'),
                        'opt.noNumber' : content.find('input[name=noColor]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        //autoTitle.util.createBatchOPResult(data)
                        autoTitle.util.updateGuanfangRecomTitle(data);
                    }
                });
            }

            },'全店标题优化选项'
        );
    },
    batchRecommend : function(itemCatCount, sellerCatCount, recMode){

        var idArr = [];
        var titles = [];
        $('.ui-dialog').remove();
        $('.tmAlertDetail').remove();
        //var opt = $("#autorecommendopt");

      //  if(!opt || opt.length ==0){
        var htmls = [];
//        htmls.push("<form id='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
        var pushCatNames = function(res, name, rowClass){
            if(res && res.length > 0){
                htmls.push('<tr class="'+rowClass+'"><td>'+name+':</td><td><select><option cid="0">所有分类</option>');
                $.each(res, function(i, elem){
                    if(elem.count > 0){
                        htmls.push('<option cid="'+elem.id+'">'+elem.name+'('+elem.count+')'+'</option>');
                    }
                });
                htmls.push('</select></td></tr>');
            }
        }

        pushCatNames(itemCatCount, '淘宝类目', 'itemCat');
        pushCatNames(sellerCatCount, '自定义类目', 'sellerCat');
        var opt = $(htmls.join(''));
     //   }

        var content = $('<div></div>');
        content.append($('#shopRecTmpl').tmpl());
        content.find('tbody').prepend(opt);

        TM.Alert.loadDetail(content,700,390,function(){
            $.ajax({
                url : '/titles/batchChangeAll',
                type : 'post',
                data:{
                    sellerCatId : content.find('.sellerCat option:selected').attr('cid'),
                    itemCatId : content.find('.itemCat option:selected').attr('cid'),
                    status : content.find('.status  option:selected').attr('value'),
                    recMode : recMode,
                    'opt.fixedStart' : content.find('input[name=fixedStart]').val(),
                    'opt.allSale' : content.find('.allsale option:selected').attr('value'),
                    'opt.keepBrand' : content.find('input[name=brand]:checked').attr('value'),
                    'opt.keepSerial' : content.find('input[name=serialNum]:checked').attr('value'),
                    'opt.mustExcluded' : content.find('input[name=mustExcluded]').val(),
                    'opt.toAddPromote' : content.find('input[name=promotewords]:checked').attr('value'),
                    'opt.noColor' : content.find('input[name=noColor]:checked').attr('value'),
                    'opt.noNumber' : content.find('input[name=noColor]:checked').attr('value')
                },
                timeout: 200000,
                success : function(data){
                    //autoTitle.util.createBatchOPResult(data)

                    if (data.success == false) {
                        var message = data.message;
                        if (message === undefined || message == null || message == "") {
                            message = "自动标题任务提交失败，请联系我们！";
                        }
                        alert(data.message);
                        return;
                    } else {
                        //alert("系统提交了自动标题的后台任务，请进入任务中心查看");

                        TM.Alert.showDialog('由于宝贝数量过多，系统已为您提交了自动标题的<span style="font-weight: bold; color: #a10000;">后台任务</span>，是否立即进入<span style="font-weight: bold; color: #a10000;">任务中心</span>查看？',
                            400,300,function(){
                                location.href = "/titletaskop/index";
                            },function(){},"提示");
                        autoTitle.Fly.centerFly();
                    }

                }
            });
        },'全店标题优化选项');
    }
},autoTitle.Event);

    autoTitle.Fly = $.extend({
        // user like this

        fly : function($this){
            var start = {}, end = {};
            start.left = $this.offset().left+"px";
            start.top = $this.offset().top+"px";
            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            autoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        },
        flyFromTo : function(start,end,callback){
            $('#fly-from-to-img').remove();
            var img = $('<img id="fly-from-to-img" src="/img/favicon.png" style="z-index:200001;width: 16px;height: 16px;position: absolute;top:'+start.top+';left: '+start.left+'"/>');
            img.appendTo($('body'));

            img.animate({top:end.top,left:end.left},1500, function(){
                img.fadeOut(1000);
                callback && callback();
            });
        },
        centerFly: function() {
            var start = {};
            var end = {};

            var windowWidth = $(window).width();
            var windowHeight = $(window).height();
            var scrollLeft = $(document).scrollLeft();
            var scrollTop = $(document).scrollTop();

            var left = (windowWidth) / 2 + scrollLeft;
            var top = (windowHeight) / 2 + scrollTop;
            start.left = left + "px";
            start.top = top + "px";

            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            autoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        }
    },autoTitle.Fly);

    TM.autoTitle = autoTitle;

})(jQuery, window));