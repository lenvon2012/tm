var TM = TM || {};
((function ($, window) {
    TM.DaZhe = TM.DaZhe || {};
    var DaZhe = TM.DaZhe;

    DaZhe.Init = DaZhe.Init || {};
    DaZhe.Init = $.extend({
        init : function(){
            var nowTime=new Date();
            var month=nowTime.getMonth()+1;
            var def_name=nowTime.getFullYear()+"年"+month+"月"+nowTime.getDate()+"日"+nowTime.getHours()+"点"+nowTime.getMinutes()+"分"+ nowTime.getSeconds()+"秒满减送";
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

                var activityString="";

                activityString +=name+","+StartTime+","+EndTime;

                var enString=encodeURI(activityString) ;

                window.location.href="/paipaidiscount/manjiansongdetail?activityString="+enString;

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

    TM.MJD = TM.MJD || {};

    var MJD = TM.MJD;

    MJD.init = MJD.init || {};
    MJD.init = $.extend({
        doInit: function(container) {
            MJD.init.doShow();
            MJD.init.doSub();
        },
        doShow:function(){
           $('.manyuan').click(function(){
               $('.selectIcon-manyuan').addClass("selectedIcon");
               $('.selectIcon-manjian').removeClass("selectedIcon");
               $('.manyuan').addClass("activeType");
               $('.manjian').removeClass("activeType");
               $('.costFlag').attr("value",1);
           }) ;
           $('.manjian').click(function(){
               $('.selectIcon-manjian').addClass("selectedIcon");
               $('.selectIcon-manyuan').removeClass("selectedIcon");
               $('.manjian').addClass("activeType");
               $('.manyuan').removeClass("activeType");
               $('.costFlag').attr("value",0);
           }) ;
           $('.discount-input-jianjia').show();
           $('.discount-input-zhekou').hide();
           $('.discount-select').change(function(){
               var disSelect=$('.discount-select').val();
               if(disSelect==1){
                   $('.discount-input-jianjia').show();
                   $('.discount-input-zhekou').hide();
               }
               else{
                   $('.discount-input-zhekou').show();
                   $('.discount-input-jianjia').hide();
               }
           });
            var pDtl= $('#content').find(".promotion-details");
           $('.add-promotion-detail').click(function(){
               var newDtl= MJD.init.creatNewDtl();
               pDtl.append(newDtl) ;
           });
        },
        creatNewDtl: function(){
            var html= MJD.init.creatHtml();
            var newDtl = $(html);
            newDtl.find('.manyuan').click(function(){
                newDtl.find('.selectIcon-manyuan').addClass("selectedIcon");
                newDtl.find('.selectIcon-manjian').removeClass("selectedIcon");
                newDtl.find('.manyuan').addClass("activeType");
                newDtl.find('.manjian').removeClass("activeType");
                newDtl.find('.costFlag').attr("value",1);
            }) ;
            newDtl.find('.manjian').click(function(){
                newDtl.find('.selectIcon-manjian').addClass("selectedIcon");
                newDtl.find('.selectIcon-manyuan').removeClass("selectedIcon");
                newDtl.find('.manjian').addClass("activeType");
                newDtl.find('.manyuan').removeClass("activeType");
                newDtl.find('.costFlag').attr("value",0);
            }) ;
            newDtl.find('.discount-input-jianjia').show();
            newDtl.find('.discount-input-zhekou').hide();
            newDtl.find('.discount-select').change(function(){
                var disSelect=newDtl.find('.discount-select').val();
                if(disSelect==1){
                    newDtl.find('.discount-input-jianjia').show();
                    newDtl.find('.discount-input-zhekou').hide();
                }
                else{
                    newDtl.find('.discount-input-zhekou').show();
                    newDtl.find('.discount-input-jianjia').hide();
                }
            });
            newDtl.find(".delete-tab").click(function(){
                newDtl.remove();
            });
            return newDtl;
        } ,
        creatHtml: function(){
            var html ='' +
                '<div class="promotion-detail">' +
                    '<div class="filter">' +
                        '<div class="tabs">' +
                           '<a class="tab" href="javascript:">优惠详情：</a>' +
                        '</div>' +
                           '<a class="delete-tab" href="javascript:" >删除此优惠</a>' +
                        '</div>' +
                    '<div style=""></div>' +
                    '<div class="tiaojian" style="border:1px solid red; width: 850px">' +
                        '<div class="up" >' +
                            '<div class="tmLine" >' +
                                '<input type="hidden" class="costFlag" value="1">'+
                                '<span class="selectIcon-manyuan selectedIcon"></span>' +
                                '<span style="font-size: 18px;">满</span>' +
                                '<input class="text manyuan activeType" type="text" value="100">' +
                                '<span style="font-size: 18px;">元</span>' +
                                '<span class="selectIcon-manjian"></span>' +
                                '<span style="font-size: 18px;">满</span>' +
                                '<input class="text manjian" type="text" value="2">' +
                                '<span style="font-size: 18px;">件</span>' +
                                '<span class="appendmanxuan">满足2选1条件，做以下优惠措施：</span>' +
                            '</div>' +
                        '</div>' +
                        '<div class="down">' +
                            '<div class="chose-discount">' +
                                '<input type="checkbox" class="checkbox-discount" style="margin: 0 10px;">' +
                                '<span style="font-size: 18px">减价/打折：</span>' +
                                '<select class="discount-select">' +
                                    '<option value="1">减价</option>' +
                                    '<option value="2">打折</option>' +
                                '</select>' +
                                '<div class="discount-input-jianjia" style="font-size: 15px;display: inline;margin: 0 25px;">' +
                                    '<span style="font-size: 15px">减</span>' +
                                    '<input class="jianjia text activeType" type="text" >' +
                                    '<span style="font-size: 15px">元</span>' +
                                '</div>' +
                                '<div class="discount-input-zhekou" style="font-size: 15px;display: inline;margin: 0 25px;">' +
                                    '<span style="font-size: 15px">打</span>' +
                                    '<input class="zhekou text activeType" type="text" >' +
                                    '<span style="font-size: 15px">折</span>' +
                                '</div>' +
                            '</div>' +
                            '<div class="separateLine"></div>' +
                            '<div class="chose-songli" >' +
                                '<input type="checkbox" class="checkbox-songli" style="margin: 0 10px;">' +
                                '<span style="font-size: 18px">送礼物!</span>' +
                                '<span style="margin-left: 20px">礼物名称：</span>' +
                                '<input class="liwu-name text activeType" type="text" style="width: 150px;" >' +
                                '<span>礼物链接：</span>' +
                                '<input class="liwu-href text activeType" type="text" style="width: 300px;">' +
                            '</div>' +
                            '<div class="chose-baoyou">' +
                                '<input type="checkbox" class="checkbox-baoyou" style="margin: 0 10px;">' +
                                '<span style="font-size: 18px">包邮!</span><span class="append"></span>' +
                            '</div>' +
                        '</div>' +
                    '</div>' +
                '</div>' +
                '';
            return html;
        },
        doSub: function(){
            $('.StepBtn2').click(function(){
                var activityString=$('.activityString').val();
                var DisString=MJD.init.DisString();
                var PreString=MJD.init.PreString();
                var BaoString=MJD.init.BaoString();
                if(DisString==""&&PreString==""&&BaoString==""){
                    alert("请至少选择一个优惠活动内容！");
                    return false;
                }
                if($("input.error").length > 0) {
                    alert("请修正错误再提交");
                    return false;
                }
                $.ajax({
                    url : '/paipaidiscount/createManJianSong',
                    data : {activityString:activityString,DisString:DisString,PreString:PreString,BaoString:BaoString},
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
        DisString: function(){
            var DisString="";
            $(".promotion-detail").each(function(i,val){
                if($(val).find('.checkbox-discount').attr("checked")=="checked"){
                    var input=$(val);
                    var costFlag=$(val).find('.costFlag').val();
                    var costMoney=0;
                    if(costFlag==1){
                        costMoney=$(val).find('.manyuan').val();
                        costMoney=Math.round(costMoney*10000)/100;
                        MJD.init.checkManyuan(input);
                    }
                    else{
                        costMoney=$(val).find('.manjian').val();
                        MJD.init.checkManjian(input);
                    }
                    var favorableFlag=$(val).find('.discount-select').val();
                    var freeMoney=0;
                    if(favorableFlag==1){
                        freeMoney=$(val).find('.jianjia').val();
                        freeMoney=Math.round(freeMoney*10000)/100;
                        MJD.init.checkfreeMoney(input) ;
                    }
                    else{
                        freeMoney=$(val).find('.zhekou').val();
                        freeMoney=Math.round(freeMoney*100)/10;
                        MJD.init.checkfreeRebate(input);
                    }
                    DisString+=costFlag+","+costMoney+","+favorableFlag+","+freeMoney+"!";
                }
            })
            return DisString;
        },
        PreString :function(){
            var PreString="";
            $(".promotion-detail").each(function(i,val){
                if($(val).find('.checkbox-songli').attr("checked")=="checked"){
                    var input=$(val);
                    var costFlag=$(val).find('.costFlag').val();
                    var costMoney=0;
                    if(costFlag==1){
                        costMoney=$(val).find('.manyuan').val();
                        costMoney=Math.round(costMoney*10000)/100;
                        MJD.init.checkManyuan(input);
                    }
                    else{
                        costMoney=$(val).find('.manjian').val();
                        MJD.init.checkManjian(input);
                    }
                    var favorableFlag=4;
                    var presentName=$(val).find('.liwu-name').val();
                    MJD.init.checkpreName(input);
                    var presentUrl=$(val).find('.liwu-href').val();
                    MJD.init.checkherf(input);
                    PreString+=costFlag+","+costMoney+","+favorableFlag+","+presentName+","+presentUrl+"!";
                }
            })
            return PreString;
        },
        BaoString:function(){
            var BaoString="";
            $(".promotion-detail").each(function(i,val){
                if($(val).find('.checkbox-baoyou').attr("checked")=="checked"){
                    var input=$(val);
                    var costFlag=$(val).find('.costFlag').val();
                    var costMoney=0;
                    if(costFlag==1){
                        costMoney=$(val).find('.manyuan').val();
                        costMoney=Math.round(costMoney*10000)/100;
                        MJD.init.checkManyuan(input);
                    }
                    else{
                        costMoney=$(val).find('.manjian').val();
                        MJD.init.checkManjian(input);
                    }
                    var favorableFlag=16;
                    BaoString+=costFlag+","+costMoney+","+favorableFlag+"!";
                }
            })
            return BaoString;
        },
        checkManyuan: function(input){
            var costMoney= $.trim(input.find('.manyuan').val());
            if(isNaN(costMoney) || costMoney < 0.01 ) {
                input.find('.manyuan').addClass("error");
                var text = '满几元必须是大于0.01的数字！';
                MJD.init.addAppendError(input.find(".append"), text);
            }
            else {
                input.find('.manyuan').removeClass("error");
            }
        },
        checkManjian: function(input){
            var costMoney= $.trim(input.find('.manjian').val());
            var r =   /^[0-9]*[1-9][0-9]*$/;
            if(isNaN(costMoney) || costMoney < 1|| r.test(costMoney)==false ) {
                input.find('.manjian').addClass("error");
                var text = '满几件必须是正整数！';
                MJD.init.addAppendError(input.find(".append"), text);
            }
            else {
                input.find('.manjian').removeClass("error");
            }
        },
        checkfreeMoney: function(input){
            var freeMoney= $.trim(input.find('.jianjia').val());
            if(isNaN(freeMoney) || freeMoney < 0.01 ) {
                input.find('.jianjia').addClass("error");
                var text = '减免金额必须是大于0.01的数字！';
                MJD.init.addAppendError(input.find(".append"), text);
            }
            else {
                input.find('.jianjia').removeClass("error");
            }
        },
        checkfreeRebate :function(input){
            var freeRebate= $.trim(input.find('.zhekou').val());
            if(isNaN(freeRebate) || freeRebate < 1||freeRebate>99 ) {
                input.find('.zhekou').addClass("error");
                var text = '折扣必须在0.1-9.9折之间！';
                MJD.init.addAppendError(input.find(".append"), text);
            }
            else {
                input.find('.zhekou').removeClass("error");
            }
        },
        checkpreName: function(input){
            var val = $.trim(input.find('.liwu-name').val());
            if(val.length <= 0 || val.length > 50) {
                var text = '礼物名称为1到50个汉字(现在长度:' + val.length + ')';
                input.find('.liwu-name').addClass("error");
                MJD.init.addAppendError(input.find(".append"), text);
            }
            else {
                input.find('.liwu-name').removeClass("error");
            }
        },
        checkherf: function(input){
            var val = $.trim(input.find('.liwu-href').val());
            var http=val.substring(0,7);
            if(val.length <= 0 || http!='http://') {
                var text = '礼物链接须为http://开头的链接';
                input.find('.liwu-href').addClass("error");
                MJD.init.addAppendError(input.find(".append"), text);
            }
            else {
                input.find('.liwu-href').removeClass("error");
            }
        },
        addAppendError : function(line, text) {
            var append = line;
            append.html(text)
                .addClass("error");
        }

    }, MJD.init);

})(jQuery,window));