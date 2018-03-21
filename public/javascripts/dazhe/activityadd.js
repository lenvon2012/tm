var TM = TM || {};
((function ($, window) {
    TM.DaZhe = TM.DaZhe || {};
    var DaZhe = TM.DaZhe;

    DaZhe.Init = DaZhe.Init || {};
    DaZhe.Init = $.extend({
        init : function(){
            var nowTime=new Date();
            var month=nowTime.getMonth()+1;
            var def_name=nowTime.getFullYear()+"年"+month+"月"+nowTime.getDate()+"日"+nowTime.getHours()+"点"+nowTime.getMinutes()+"分"+ nowTime.getSeconds()+"秒促销";
            $('#name').attr("value",def_name) ;
            $('#start_time').attr("value",  DaZhe.Init.parseLongToDate(nowTime.getTime()));
            $('#end_time').attr("value",  DaZhe.Init.parseLongToDate(nowTime.getTime()+7*1000*60*60*24));
            DaZhe.Init.DoTimePicker();
            DaZhe.Init.docheck();
            DaZhe.Init.GoStep2();
        } ,
        DoTimePicker : function(){
            $("#start_time").datetimepicker({

                minDate: new Date()   ,
                onClose: function(input, inst) {
                    var mindate = $('#start_time').datetimepicker('getDate');
                    if(!mindate) mindate=new Date();
                    var enddate = new Date($("#end_time").val());
                    if(!enddate) enddate=new Date();
                    $('#end_time').datetimepicker('destroy');
                    $('#end_time').datetimepicker({

                        minDate: mindate,
                        onClose: function(input, inst) {
                            DaZhe.Init.updateTimeDiff();
                            DaZhe.Init.checkTime();
                        }
                    });
                    var day = mindate.getTime()-mindate.getTime()%(1000*60*60*24)+7*1000*60*60*24-(1000*60*60*6) + Math.floor(Math.random()*60*60*4*1000);
                    mindate.setTime(day);
                    if( mindate.getTime() > enddate.getTime() ) {
                        $('#end_time').datetimepicker('setDate', mindate);
                    }
                    DaZhe.Init.updateTimeDiff();
                    DaZhe.Init.checkTime();
                }
            }) ;

            $('#end_time').datetimepicker({

                onClose: function(input, inst) {
                    DaZhe.Init.updateTimeDiff();
                    DaZhe.Init.checkTime();
                }
            });
        }   ,

        GoStep2 : function(){
            $('.StepBtn1').click(function(){
                DaZhe.Init.checkName();
                DaZhe.Init.checkTime();
                var S= $('#start_time').datetimepicker('getDate');
                var E= $('#end_time').datetimepicker('getDate');
                if(!S||!E)     {
                    alert("活动时间不能为空") ;
                    return false;
                }
                var StartTime=S.getTime();
                var EndTime=E.getTime();
                var name=$('#name').val();


                if($(".error").length > 0) {
                    alert("请先修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }
                $.ajax({
                    url:'/PaiPaiDiscount/addLtdActive',
                    data:{beginTime:StartTime,endTime:EndTime,activityName:name},
                    type:'post',
                    success:function(data){
                        if(!data.res.errorMessage) {
                            window.location.href="/PaiPaiDiscount/zhekou_2?activityId="+data.res.activityId;
                        }
                        else{
                            alert(data.res.errorMessage)  ;
                        }
                    }
                })

            })

        }  ,

        updateTimeDiff: function(){
            var start  = $('#start_time').datetimepicker('getDate');
            var end  = $('#end_time').datetimepicker('getDate');
            if(!start) start=new Date();
            if(!end) end=new Date();
            var diff = end.getTime()-start.getTime();
            var parents = $("#end_time").parentsUntil(".tmLine");
            var $input = $(parents[parents.length-1]).parent().find(".hint");

            diff = (diff-diff%60000)/60000;
            var t = diff%60;
            $("em:eq(2)", $input).text(t);
            diff = (diff-t)/60;
            t = diff%24;
            $("em:eq(1)", $input).text(t);
            diff = (diff-t)/24;
            $("em:eq(0)", $input).text(diff);
            $("#end_time").parentsUntil(".tmLine").parent().find(".okMsg").show();
            $("#end_time").parentsUntil(".tmLine").parent().find(".errorMsg").hide();
        },

        addAppendError : function(line, text) {
            var append = $(".append", line);
            append.html(text)
                .addClass("error");
        },
        removeAppendError : function(line) {
            var append = $(".append", line);
            append.html(append.data("normal"))
                .removeClass("error");
        },

        checkName: function(){
            var input = $("#name");
            var val = $.trim(input.val());
            if(val.length < 2 || val.length > 30) {
                var text = '2到30个汉字(现在长度:' + val.length + ')';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }
        },

        checkTime :function(){

            var sdate= $('#start_time').datetimepicker('getDate');
            var edate= $('#end_time').datetimepicker('getDate');
            if(!sdate||!edate)     {
                return false;
            }
            var input = $("#start_time");
            if(!sdate || isNaN(sdate.getTime())) {
                var text = '开始时间格式不正确';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }

            var input = $("#end_time");
            if(!edate || isNaN(edate.getTime())) {
                var text = '结束时间格式不正确';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else if(edate.getTime() <= sdate.getTime()) {
                var text = '结束时间要大于开始时间';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }
//            var limit=sdate.getTime()+10*24*60*60*1000;
//            if(limit<edate.getTime()) {
//                var text = '活动时间不能超过10天';
//                input.addClass("error");
//                DaZhe.Init.addAppendError(input.parent(), text);
//            }
//            else {
//                input.removeClass("error");
//                DaZhe.Init.removeAppendError(input.parent());
//            }

        } ,
        checkDiscount : function() {
            var input=$('#discount');
            var val = $.trim(input.val());
            if(isNaN(val) || val < 5 || val >= 10) {
                var text = '折扣应是大于5小于10的数字';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }
        },

        docheck :function(){
            $(".append").each(function() {
                $(this).data("normal", $(this).html());
            });

            $("#name").keyup(function() {
                DaZhe.Init.checkName();
            });
            $("#name").blur(function() {
                DaZhe.Init.checkName();
            });

            $("#start_time").keyup(function() {
                DaZhe.Init.checkTime();
            });
            $("#end_time").keyup(function() {
                DaZhe.Init.checkTime();
            });
        } ,

        parseLongToDate:function(ts) {
            var theDate = new Date();
            theDate.setTime(ts);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr =year+"-"+ month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            return timeStr;
        }
    }, DaZhe.Init);



    TM.DisItem = TM.DisItem || {};

    var DisItem = TM.DisItem;

    DisItem.init = DisItem.init || {};
    DisItem.init = $.extend({
        doInit: function(container) {
            DisItem.container = container;
            DisItem.search.doSearch();
            DisItem.search.doquanxuan();
            DisItem.submit.doSub();
        }

    },DisItem.init);

    var selectItems =[];

    DisItem.search =DisItem.search || {};
    DisItem.search= $.extend({
        doSearch: function() {
            $.get("/paipaidiscount/sellerCatCount",function(data){
                var sellerCat = $('#sellerCat');
                sellerCat.empty();
                if(!data ||data.res.length == 0){
                    sellerCat.hide();
                }

                var exist = false;
                var cat = $('<option>所有分类</option>');
                sellerCat.append(cat);
                for(var i=0;i<data.res.length;i++) {
                    if(data.res[i].count <= 0){
                        continue;
                    }
                    var item_cat=data.res[i];
                    exist = true;
                    var option = $('<option></option>');
                    option.attr("catId",item_cat.cid);
                    option.html(item_cat.name);
                    sellerCat.append(option);
                }
            });
            $('#sellerCat').change(function(){
                var catId = $("#sellerCat option:selected").attr("catid");
                var status=1;
                DisItem.search.doShow(1,status, catId);
            }) ;
            $('.guanjianci-select').click(function(){
                var status =2;
                var catId=$('.guanjianci').val();
                DisItem.search.doShow(1, status,catId);
            })  ;
            var status=0;
            var catId = $("#sellerCat option:selected").attr("catid");
            DisItem.search.doShow(1,status, catId);
            //批量修改选中宝贝
            $('.piliang').keyup(function(){
                var piliang= Math.round(($('.piliang').val())*1000)/1000;
                $(".item").each(function(i,val){
                    var input =$(val).find(".item-dis");
                    var status=$(val).find(".item-status").val();

                    if(status==8) {
                        $(val).find('.item-dis').attr("value",piliang);
                        var price = $(val).find('.old-price').val();
                        var discount =Math.round((price*piliang/10)*100)/100;
                        $(val).find('.item-disprice').attr("value",discount);
                    }

                })
            });
        },
        doShow: function(currentPage,status, catId) {
            if (currentPage < 1)
                currentPage = 1;
            var tbodyObj = DisItem.container.find(".item-table").find("tbody");
            DisItem.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {status:status,catId:catId},
                    dataType: 'json',
                    url: '/paipaidiscount/searchItems',
                    callback: function(dataJson){
                        DisItem.row.setArrayString();
                        if($(".error").length > 0) {
                            alert("折扣范围（7--9.9）折，请修正错误再提交");
                            setTimeout(function() {
                                $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                            }, 300);
                            return false;
                        }
                        tbodyObj.html("");
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = DisItem.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }
            });
        } ,
        doquanxuan: function(){
            $(".unchoose").hide();
            $('.choose').click(function(){
                $(".choose").hide();
                $(".unchoose").show();
                var piliang= Math.round(($('.piliang').val())*1000)/1000;
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status==0) {
                        $(val).find('.lightBlueBtn').hide();
                        $(val).find(".lightGrayBtn").show();
                        if(piliang){
                            $(val).find('.item-dis').attr("value",piliang);
                            var price = $(val).find('.old-price').val();
                            var discount =Math.round((price*piliang/10)*100)/100;
                            $(val).find('.item-disprice').attr("value",discount);
                        }
                        $(val).find(".item-status").attr("value", 8);
                    }
                })
            })
            $('.unchoose').click(function(){
                $(".unchoose").hide();
                $(".choose").show();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status==8) {
                        $(val).find('.lightGrayBtn').hide();
                        $(val).find(".lightBlueBtn").show();
                        $(val).find('.item-dis').attr("value","");
                        $(val).find('.item-disprice').attr("value","");
                        $(val).find(".item-status").attr("value", 0);
                    }
                })
            })
        }

    },DisItem.search);

    DisItem.row = DisItem.row || {};
    DisItem.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DisItem.row.createHtml();
            var trObj = $(html);

            trObj.find(".item-code").attr("value", itemJson.itemCode);
            var href = "http://auction2.paipai.com/" + itemJson.itemCode;
            trObj.find(".item-href").attr("href", href);
            trObj.find(".item-href").attr("target", "_blank");
            trObj.find(".item-img").attr("src", itemJson.picLink);
            trObj.find(".item-name").html(itemJson.itemName);
            trObj.find(".item-price").html(itemJson.itemPrice/100);
            trObj.find(".old-price").attr("value",itemJson.itemPrice/100) ;
//            trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的


            var refreshCallback = function() {
                DisItem.search.doSearch();
            };


            var html1 = '' +
                '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn">加入活动</a>' +
                '<a href="javascript:;"  class="lightGrayBtn addedToActBtn productBtn">已添加</a>' +
                '';
            var html2=''+
                '<span class="item_disable">已参加其他活动</span>' +
                '';
            if(itemJson.itemDiscount!=0) {
                trObj.find(".item-status").attr("value", 4);//已经参加其他活动的
                trObj.addClass("disabled");
                trObj.find(".op-td").html(html2);
                trObj.find(".item-dis").attr("value",itemJson.itemDiscount/1000);
                var after_price =itemJson.itemPrice*itemJson.itemDiscount/1000000;
                after_price=Math.round(after_price*100)/100;
                trObj.find(".item-disprice").attr("value",after_price);
            }
            else{
                trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的
                trObj.find(".op-td").html(html1);
                trObj.find('.lightGrayBtn').hide();

                for (var i=0; i<selectItems.length;i++){
                    if (selectItems[i].id ==itemJson.itemCode){
                        trObj.find(".item-status").attr("value", 8);
                        trObj.find(".item-disprice").attr("value",selectItems[i].disprice);
                        trObj.find(".item-dis").attr("value",selectItems[i].discount/1000);
                        trObj.find(".lightGrayBtn").css("display","block");
                        trObj.find('.lightBlueBtn').css("display","none");
                    }
                }

                trObj.find(".lightBlueBtn").click(function() {
                    trObj.find('.lightBlueBtn').hide();
                    trObj.find(".lightGrayBtn").show();
                    var piliang= Math.round(($('.piliang').val())*1000)/1000;
                    if(piliang){
                        trObj.find('.item-dis').attr("value",piliang);
                        var price = itemJson.itemPrice/100;
                        var discount =Math.round((price*piliang/10)*100)/100;
                        trObj.find('.item-disprice').attr("value",discount);
                    }
                    trObj.find(".item-status").attr("value", 8);
                });
                trObj.find(".lightGrayBtn").click(function(){
                    trObj.find('.lightBlueBtn').show();
                    trObj.find(".lightGrayBtn").hide();
                    trObj.find('.item-dis').attr("value","");
                    trObj.find('.item-disprice').attr("value","");
                    trObj.find(".item-status").attr("value", 0);
                }) ;

                trObj.find(".item-dis").keyup(function(){
                    var dis= trObj.find(".item-dis").val();
                    var price =itemJson.itemPrice/100;
                    var discount =Math.round((price*dis/10)*100)/100;
                    trObj.find(".item-disprice").attr("value",discount) ;
                });

                trObj.find(".item-disprice").keyup(function(){
                    var dis= trObj.find(".item-disprice").val();
                    var price =itemJson.itemPrice/100;
                    var discount=Math.round((dis/price)*10000)/1000;
                    trObj.find(".item-dis").attr("value",discount) ;
                });

            }
            return trObj;
        },
        createHtml: function(itemJson) {

            var html = '' +
                '<tr class="item">' +
                '   <input type="hidden" class="item-code" /> ' +
                '   <input type="hidden" class="item-status" /> ' +
                '   <input type="hidden" class="old-price" >'+
                '   <td class="result-td"><a class="item-href"><img class="item-img" style="width: 90px; height: 90px;"/></a> </td>' +
                '   <td class="result-td"><a class="item-href item-link item-name"></a></td>' +
                '   <td class="result-td"><span class="item-price"></span></td>'+
                '   <td class="result-td op-item-dis"><input type="text"  class="item-dis" style="color: #FF4400;"></td>'+
                '   <td class="result-td op-item-disprice"><input type="text" class="item-disprice" ></td>'+
                '   </td class="result-td">' +
                '   </td class="result-td">' +
                '   <td class="result-td op-td">' +
                '       ' +
                '   </td> ' +
                '</tr>' +
                '';
            return html;
        },
        addToSelectedItems :function(item){
            var existed = DisItem.row.removeFromSelectedItems(item);
            selectItems.push(item);
            return !existed;
        },
        removeFromSelectedItems : function(item){
            for (var i=0; i<selectItems.length;i++){
                if (selectItems[i].id == item.id){
                    selectItems.splice(i,1);
                    return true;
                }
            }
            return false;
        },
        setArrayString : function(){
            $(".item").each(function(i,val){
                var item={};
                var itemCode=$(val).find('.item-code').val();
                var status=$(val).find(".item-status").val();
                var discount=Math.round(($(val).find('.item-dis').val())*1000)/1000;
                discount=discount*1000;
                var input =$(val).find(".item-dis");
                var disprice=$(val).find(".item-disprice").val();

                if(status==8) {
                    DisItem.submit.checkDiscount(input);
                    item.id=itemCode;
                    item.discount=discount;
                    item.disprice=disprice;
                    DisItem.row.addToSelectedItems(item);
                }
                else{
                    DisItem.row.removeFromSelectedItems(item);
                }
            })
        }
    }, DisItem.row);

    DisItem.submit = DisItem.submit || {};
    DisItem.submit = $.extend({
        doSub:function(){
            $('.StepBtn2').click(function(){
                var subString=DisItem.submit.subString();
                if($(".error").length > 0) {
                    alert("折扣范围（5--9.9）折，请修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }
                if(selectItems.length==0){
                    alert("亲，请选择至少一个宝贝加入活动！！");
                    return false;
                }
                $.ajax({
                    url : '/paipaidiscount/setLtdItem',
                    data : {itemString:subString},
                    type : 'post',
                    success : function(data) {
                        if(data == null || data.length == 0){
                            window.location.href="/paipaidiscount/addSuccess";
                        } else {
                            alert(data.msg);
                        }
                    }
                });
            })

        },
        subString : function(){
            var subString="";
            var reqType=1;
            var activityId=$('.activityId').val();
            DisItem.row.setArrayString();

            for (var i=0; i<selectItems.length;i++){
                subString +=reqType+","+activityId+","+selectItems[i].id+","+selectItems[i].discount+"!";
            }
            return subString;
        } ,
        checkDiscount : function(input) {
            var val = $.trim(input.val());
            if(isNaN(val) || val < 5 || val >= 10) {
                var text = '折扣应是大于5小于10的数字';
                input.addClass("error");
            }
            else {
                input.removeClass("error");
            }
        }
    }, DisItem.submit) ;

    TM.DisAct = TM.DisAct || {};

    var DisAct = TM.DisAct;

    DisAct.init = DisAct.init || {};
    DisAct.init = $.extend({
        doInit: function(container) {
            DisAct.container = container;
            DisAct.search.doSearch();
            DisAct.init.doApidelete();
//            $.get('/OPPaiPaiUserInterFace/show1yuanhongbao',function(data){
//                if(data == "show"){
//                    DisAct.init.showFirstXufei();
//                }
//            });
        } ,
//        showFirstXufei : function(){
//            $.get("/OPPaiPaiUserInterFace/oneyuanshowed",function(data){
//                if(data == "unshowed"){
//                    var hour = new Date().getHours();
//                    var remain;
//                    if(hour <= 8){
//                        remain = 12;
//                    } else if(hour <= 12){
//                        remain = 9;
//                    } else if(hour <= 16){
//                        remain = 6;
//                    } else if(hour <= 20){
//                        remain = 3;
//                    } else if(hour <= 22){
//                        remain = 2;
//                    } else {
//                        remain = 1;
//                    }
//                    var html = '' +
//                        '<p style="font-size:20px;font-weight: bold;margin: 20px 0px;text-align: center">即日起，每邀请一个朋友订购紫金折扣，并让朋友使用时填写您的QQ号，便能领取现金1元红包，请的多，得的多！（紫金团队财付通12小时结算后转账到您的账户）</p>' +
//                        '<div><p style="font-size: 30px;color: red;font-weight: bold;margin: 20px 0px;text-align: center">邀请送现金，快来订购吧！</p></div>' +
//                        '<div><a href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=262400&chargeItemId=2878" target="_blank"><img src="/public/images/dazhe/1yuan.png" alt="1元红包" style="margin:0 270px"></a></div>' +
//                        '<p style="font-size: 30px;text-align:center;margin-top:5px;color: #1779BE">填写邀请人QQ，只能是已经订购紫金折扣用户QQ</p>' +
//                        '<div class="mainFormForm">'+
//                        '<div class="tmLine "><label>邀请人QQ号：</label><input type="text" class="text"  id="yaoqingqq"><span class="append">仅能输入一次哦！</span>'+
//                        '<a class="queren" href="javascript:void(0)"></a><div class="clear"></div></div></div>'+
//                        '';
//
//                    var content = $(html);
//                    var redshow = function () {
//                        content.find('.remain').toggleClass('red');
//                        content.find('.free-link').toggleClass('red');
//                    }
//                    setInterval(redshow, 300);
//                    TM.Alert.loadDetail(content, 800, 600, function () {
//                        return true;
//                    }, "一元红包")
//                    // no show any more
//                    $.get('/OPPaiPaiUserInterFace/setShowed',function(data){
//                        return true;
//                    });
//                    $('.queren').click(function(){
//                        var requestId=$('#yaoqingqq').val();
//                        $.ajax({
//                            url:'/PaiPaiDiscount/setPPhongbao',
//                            data:{requestId:requestId},
//                            type:'post',
//                            success:function(data){
//                                if(data == null || data.length == 0){
//                                    alert("输入成功！一元现金将于12小时内打入邀请人支付宝账户！");
//                                    window.location.href ="/paipaidiscount/index";
//                                } else {
//                                    alert(data.msg);
//                                }
//                            }
//                        })
//                    }) ;
//                }
//            });
//        },
        doApidelete:function(){
            $('.deleteApiBtn').click(function(){
                $.ajax({
                    url : '/paipaidiscount/debugApiDelete',
                    data : {},
                    type : 'post',
                    success : function(data) {
                        alert(data.msg);
                        location.reload();
                    }
                });
            })
        }
    },DisAct.init);

    DisAct.search =DisAct.search || {};
    DisAct.search= $.extend({
        doSearch: function() {
            DisAct.search.doShow(1);
            DisAct.search.doactive();
        },
        doShow: function(isactive) {
            var tbodyObj = DisAct.container.find(".discount-table").find("tbody");
            DisAct.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {isactive:isactive},
                    dataType: 'json',
                    url: '/paipaidiscount/getLtdActive',
                    callback: function(dataJson){
                        DisAct.container.html();
                        var html1=''+
                            '<div class="new-activity" style="text-align: center;margin: 20px auto 20px auto;">' +
                            '<span class="f-ziti">亲，您还没有创建活动哦！</span>'+
                            '</div>'+
                            '<div>'+
                            '<a class="f-huodong" href="/paipaidiscount/zhekou_1">点击创建限时打折活动</a>' +
                            '</div> ';
                        if(dataJson.res==null||dataJson.res.length==0){
                            DisAct.container.find(".f-chuangjian").html(html1);
                            return;
                        }
                        else{
                            tbodyObj.html("");
                            var itemArray = dataJson.res;
                            $(itemArray).each(function(index, itemJson) {
                                var trObj = DisAct.row.createRow(index, itemJson);
                                tbodyObj.append(trObj);
                            });
                        }
                    }
                }
            });

        } ,
        doactive : function(){
            $('.isactive').attr("value",1) ;
            var going=$('.going');
            going.addClass("active");
            var ended=$('.ended');
            going.click(function(){
                going.addClass("active");
                ended.removeClass("active");
                $('.isactive').attr("value",1);
                DisAct.search.doShow(1);
            });
            ended.click(function(){
                ended.addClass("active");
                going.removeClass("active");
                $('.isactive').attr("value",0);
                DisAct.search.doShow(0);
            });
            $('.deleteMan1').click(function(){
                $.ajax({
                    url : '/paipaidiscount/DeleteManJianSong',
                    data : {},
                    type : 'post',
                    success : function(data) {
                        if(data == null || data.length == 0){
                            alert("删除活动成功");
                            location.reload();
                        }
                        else {
                            alert(data.msg);
                        }
                    }
                })
            });
            $.ajax({
                url : '/paipaidiscount/getXiaoMan',
                data : {},
                type : 'post',
                success : function(data) {
                    if(data.msg== null){
                    }
                    else {
                        var html2='' +
                            '<span>' +
                            '' +data.msg+
                            '</span>';
                        DisAct.container.find(".ploy_list").html(html2);
                    }
                }
            });
            $.ajax({
                url : '/paipaidiscount/getManJianSongActivity',
                data : {},
                type : 'post',
                success : function(data) {
                    var html1=''+
                        '<div class="new-activity" style="text-align: center;margin: 20px auto 20px auto;">' +
                        '<span class="f-ziti">亲，您还没有创建活动哦！</span>'+
                        '</div>'+
                        '<div>'+
                        '<a class="f-huodong" href="/paipaidiscount/creatmanjiansong">点击创建满就送活动</a>' +
                        '</div> ';
                    if(data.res==null||data.res.length==0){
                        DisAct.container.find(".f-manjiansong").html(html1);
                        return;
                    }
                    else{
                        $('.manjiansong-des').html(data.res.activityDesc);
                        $('.manjiansong-beg').html(data.res.beginTime.substring(5,16));
                        $('.manjiansong-end').html(data.res.endTime.substring(5,16));
                        $('.deleteMan').click(function(){
                            $.ajax({
                                url : '/paipaidiscount/DeleteManJianSong',
                                data : {},
                                type : 'post',
                                success : function(data) {
                                    if(data == null || data.length == 0){
                                        alert("删除活动成功");
                                        location.reload();
                                    }
                                    else {
                                        alert(data.msg);
                                   }
                                }
                            })
                        });
                    }
                }
            });
        }
    },DisAct.search);

    DisAct.row = DisAct.row || {};
    DisAct.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DisAct.row.createHtml();
            var trObj = $(html);


            trObj.find(".item-id").attr("value",itemJson.activityId);
            trObj.find(".item-name").html(itemJson.activityName);
            var beginTime = itemJson.beginTime.substring(5,16) ;
            var endTime =  itemJson.endTime.substring(5,16) ;
            trObj.find(".item-beginTime").html(beginTime);
            trObj.find(".item-endTime").html(endTime);

            var refreshCallback = function() {
                DisAct.search.doSearch();
            };

            var html1 = '' +
                '<a  href="javascript:;" class="lightBlueBtn reviseItem">修改商品</a>' +
                '<a  href="javascript:;" class="lightBlueBtn addItem">添加商品</a>' +
                '<a  href="javascript:;" class="lightBlueBtn reviseAct">修改活动信息</a>' +
                '<a  href="javascript:;" class="lightBlueBtn deleteAct">结束活动</a>' +
                '';
            var html2 ='<a  href="javascript:;" class="lightBlueBtn deleteUnAct">删除记录</a>';
            var isactive= $('.isactive').val();
            if(isactive==1) {
                trObj.find(".op-td").html(html1);
            }
            else{
                trObj.find(".op-td").html(html2);
            }

            trObj.find(".reviseItem").click(function() {
                var href="/paipaidiscount/reviseItem?activityId="+itemJson.activityId;
                trObj.find(".reviseItem").attr("href", href);
                trObj.find(".reviseItem").attr("target","_blank");
            });
            trObj.find(".addItem").click(function(){
                var href="/paipaidiscount/addItem?activityId="+itemJson.activityId;
                trObj.find(".addItem").attr("href", href);
                trObj.find(".addItem").attr("target","_blank");
            }) ;
            trObj.find(".reviseAct").click(function(){
                alert("十分抱歉亲！拍拍平台修改活动接口暂不支持此操作，官方修复后，我们会同步开放此功能！")
//                var href="/paipaidiscount/reviseAct?activityId="+itemJson.activityId;
//                trObj.find(".reviseAct").attr("href", href);
//                trObj.find(".reviseAct").attr("target","_blank");
            }) ;
            trObj.find(".deleteAct").click(function(){
                $.ajax({
                    url : '/paipaidiscount/delLtdActive',
                    data : {activityId:itemJson.activityId},
                    type : 'post',
                    success : function(data) {
                        if(data == null || data.length == 0){
                            alert("删除活动成功");
                            location.reload();
                        }
                        else {
                            alert(data.msg);

                        }

                    }
                });
            }) ;
            trObj.find(".deleteUnAct").click(function(){
                $.ajax({
                    url : '/paipaidiscount/delUnActive',
                    data : {activityId:itemJson.activityId},
                    type : 'post',
                    success : function(data) {
                        if(data == null || data.length == 0){
                            alert("删除活动成功");
                            location.reload();
                        }
                        else {
                            alert(data.msg);
                        }
                    }
                });
            }) ;
            return trObj;
        },
        createHtml: function(itemJson) {

            var html = '' +
                '<tr>' +
                '   <input type="hidden" class="item-id" /> ' +
                '   <td class="result-td"><a class="item-href item-link item-name"></a></td>' +
                '   <td class="result-td"><span class="item-beginTime" style=""></span></td>'+
                '   <td class="result-td"><span class="item-endTime" style=""></span></td>'+
                '   </td class="result-td">' +
                '   <td class="result-td op-td">' +
                '       ' +
                '   </td> ' +
                '</tr>' +
                '';
            return html;
        }  ,

        parseLongToDate:function(ts) {
            var theDate = new Date();
            theDate.setTime(ts);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr = month+"-"+date+" "+hour+":"+minutes;//+ ":" + second;
            return timeStr;
        }

    }, DisAct.row);


})(jQuery,window));


