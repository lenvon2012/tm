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
        $('.orangeBtn').click(function(){
            $('#hraBox').hide();
        });
        $('.closeModal').click(function(){
            $('#settingActModal').hide();
        });
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
                        DaZhe.Init.checkTime() ;
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
            DaZhe.Init.checkTitle();
            DaZhe.Init.checkDiscount();
            var S= $('#start_time').datetimepicker('getDate');
            var E= $('#end_time').datetimepicker('getDate');
            if(!S||!E)     {
                alert("活动时间不能为空") ;
                return false;
            }
            var StartTime=S.getTime();
            var EndTime=E.getTime();
            var name=$('#name').val();
            var title=$('.promotion_title').val();
            var discount=$('#discount').val();
            var limitTime= StartTime+10*24*60*60*1000;
            //var NowTime =new Date().getTime();  alert(NowTime)

            if($(".error").length > 0) {
                alert("请先修正错误再提交");
                setTimeout(function() {
                    $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                }, 300);
                return false;
            }

            var activityString="";

            activityString +=name+","+StartTime+","+EndTime+","+title+","+discount;

            $.ajax({
                url : '/TaoDiscount/itemAllAdd',
                data : {activityString:activityString},
                type : 'post',
                success : function(data) {
                    if(data.res == null || data.res.length == 0){
                        var scrollTop = $(document).scrollTop();
                        var scrollLeft = $(document).scrollLeft();
                        if(data.msg=="0"){
                            var Div = $('#hraBox');
                            var Divtop = ($(window).height() - Div.height())/2;
                            var Divleft = ($(window).width() - Div.width())/2;
                            Div.css( { 'top' : Divtop + scrollTop, left : Divleft + scrollLeft } ).show();
                        }
                        else if(data.msg=="-1"){
                            alert("淘宝服务器忙，请过2分钟后重试！") ;
                        }
                        else if(!data.msg){
                            alert("全店活动创建成功！");
                            window.location.href ="/TaoDiscount/index";
                        }
                        else{
                            var limitDis=data.msg;
                            limitDis=limitDis/10;
                            var settingActModal = $('#settingActModal');
                            var settingActModaltop = ($(window).height() - settingActModal.height())/2;
                            var settingActModalleft = ($(window).width() - settingActModal.width())/2;
                            settingActModal.find('.orange').html(limitDis) ;
                            settingActModal.css( { 'top' : settingActModaltop + scrollTop, left : settingActModalleft + scrollLeft } ).show();
                        }
                    } else {
                        DaZhe.error.showErrors(data.res);
                    }
                }
            });

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
    checkTitle: function(){
        var input = $(".promotion_title");
//            var val = $.trim(input.val());
        var val = input.val();
        var reg=/^(\w|[\u4E00-\u9FA5]|_)*$/;
        if(val.length < 0 || val.length > 5) {
            var text = '0到5个汉字(现在长度:' + val.length + ')';
            input.addClass("error");
            DaZhe.Init.addAppendError(input.parent(), text);
        }
        else if(!reg.test(val)){
            var text = '标签只允许有中英文字符,数字,下划线组成!';
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

    } ,
    checkDiscount : function() {
        var input=$('#discount');
        var val = $.trim(input.val());
        if(isNaN(val) || val <=0 || val >= 10) {
            var text = '折扣应是大于0.01小于10的数字';
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
        $(".title-input").keyup(function() {
            DaZhe.Init.checkTitle();
        });
        $('#discount').keyup(function() {
            DaZhe.Init.checkDiscount();
        });
        $('.title-input').hide();
        $('.select-put').hide();
        $('.self-put').click(function(){
            $('.title-select').hide();
            $('.self-put').hide();
            $('.title-input').show();
            $('.select-put').show();
            $('.title-input').addClass("promotion_title") ;
            $('.title-select').removeClass("promotion_title") ;
        });
        $('.select-put').click(function(){
            $('.promotion_title').attr("value","");
            DaZhe.Init.checkTitle();
            $('.title-input').hide();
            $('.select-put').hide();
            $('.title-select').show();
            $('.self-put').show();
            $('.title-select').addClass("promotion_title") ;
            $('.title-input').removeClass("promotion_title") ;
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

/**
 * 操作失败的日志
 * @type {*}
 */
DaZhe.error = DaZhe.error || {};
DaZhe.error = $.extend({
    showErrors: function(errorJsonArray) {

        var html='' +
            '<div class="error-item-div busSearch" style="margin-top: 10px;">' +
            '   <span class="error-tip-span">宝贝操作失败列表：</span> ' +
            '   <table class="error-item-table list-table">' +
            '       <thead>' +
            '       <tr>' +
            '           <td style="width: 15%;">宝贝图片</td>' +
            '           <td style="width: 35%;">标题</td>' +
            '           <td style="width: 30%;">失败说明</td>' +
            '           <td style="width: 15%;">操作时间</td>' +
            '       </tr>' +
            '       </thead>' +
            '       <tbody></tbody>' +
            '   </table> ' +
            '</div>' +
            '';
        var dialogObj = $(html);

        $(errorJsonArray).each(function(index, errorJson) {
            var trObj = DaZhe.error.createRow(index, errorJson);
            dialogObj.find(".error-item-table").find("tbody").append(trObj);
        });

        $("body").append(dialogObj);
        dialogObj.dialog({
            modal: true,
            bgiframe: true,
            height:500,
            width:780,
            title:'宝贝错误列表',
            autoOpen: false,
            resizable: false,
            buttons:{'返回活动列表':function() {
                window.location.href="/TaoDiscount/index";
            },'取消':function(){
                location.reload();
                $(this).dialog('close');
            }}
        });

        dialogObj.dialog("open");

    },
    createRow: function(index, errorJson) {
        var itemJson = errorJson.item;
        var opstatus = errorJson.opstatus;
        var html = '' +
            '<tr>' +
            '   <td><a class="item-link" target="_blank"><img class="item-img" style="width: 60px;height: 60px;" /> </a> </td>' +
            '   <td><a class="item-link" target="_blank"><span class="item-title"></span> </a> </td>' +
            '   <td><span class="error-intro"></span> </td>' +
            '   <td><span class="op-time"></span> </td>' +
            '</tr>' +
            '' +
            '' +
            '';
        var trObj = $(html);
        var url = "http://auction2.paipai.com/" + itemJson.numIid;
        trObj.find(".item-link").attr("href", url);
        trObj.find(".item-img").attr("src", itemJson.picURL);
        trObj.find(".item-title").html(itemJson.title);
        trObj.find(".error-intro").html(opstatus.opMsg);

        var theDate = new Date();
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
        var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
        trObj.find(".op-time").html(timeStr);

        return trObj;
    }
}, DaZhe.error);