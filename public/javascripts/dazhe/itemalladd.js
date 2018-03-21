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
            var discount=$('#discount').val();
            discount=discount*1000;

            if($(".error").length > 0) {
                alert("请先修正错误再提交");
                setTimeout(function() {
                    $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                }, 300);
                return false;
            }
            $.ajax({
                url:'/PaiPaiDiscount/itemAllAdd',
                data:{beginTime:StartTime,endTime:EndTime,activityName:name,discount:discount},
                type:'post',
                success : function(data) {
                    if(data == null || data.length == 0){
                        window.location.href="/paipaidiscount/addSuccess";
                    } else {
                        alert(data.msg);
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
        $('#discount').keyup(function() {
            DaZhe.Init.checkDiscount();
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