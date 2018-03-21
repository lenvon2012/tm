$(document).ready(function () {
    $(".container").data({ seriouscount: 0, normcount: 0, haelthcount: 0, totalscore: 0, currentprecent: 0 });
    $.ajaxSetup({ cache: false });
    var DefaultUrl = "/clt/sysfile/ashx/detection-result.ashx";

    function analysis() {
        $(".detectionbg,.detectiontop", ".detectioncanvas").hide();
        $(".detectioncanvas .detectionitems").show();
        $(".leftcanvas").css("width", "100%");
        $(".leftcanvas .detectionitems").css("width", "800px");
        //执行转化诊断
        $.get(DefaultUrl, { gettype: 0, isuse: 0 }, function (result) {
            if (result === "false") {
                if ($("#setcurrentper").val() === "3")
                    self.location.href = "/onekey.html";
                else
                    self.location.href = "/detection/buy.html";
                return;
            }
            AnalysisFormat(result);
            //执行滞销商品诊断
            $.get(DefaultUrl, { gettype: 1, isuse: 0 }, function (result) {
                AnalysisFormat(result);
                //执行N顾客看过宝贝诊断
                $.get(DefaultUrl, { gettype: 2, isuse: 0 }, function (result) {
                    AnalysisFormat(result);
                    //执行用户一次多款购买诊断
                    $.get(DefaultUrl, { gettype: 3, isuse: 0 }, function (result) {
                        AnalysisFormat(result);
                        //执行跳变率检测
                        $.get(DefaultUrl, { gettype: 4, isuse: 0 }, function (result) {
                            AnalysisFormat(result);
                            //执行客单价检测
                            $.get(DefaultUrl, { gettype: 5, isuse: 0 }, function (result) {
                                AnalysisFormat(result);
                                //爆款
                                $.get(DefaultUrl, { gettype: 18, isuse: 0 }, function (result) {
                                    AnalysisFormat(result);
                                    LastThing();
                                });

                            });
                        });
                    });
                });
            });
        });
    }

    //立刻体验
    $("#nowdetection,.detectiontop", ".detectioncanvas").click(function () {


        $(".toolslist").hide(200);


        window.setTimeout(function () {
            analysis();
        }, 300);


    });

    //分析当前得分，给出相应的放置区域
    var AnalysisFormat = function (result) {
        //定义数组，0=检测结果 1=影响因素 2=得分

        var arrs = new Array(); //定义一数组

        arrs = result.split("★");

        //得分
        var score = arrs[3];

        //当前进度
        $(".container").data("currentprecent", $(".container").data("currentprecent") + 1);

        $(".detectionitems .topcell .loadingbar .gry").css("left", 95.4 * $(".container").data("currentprecent"));
        //$(".detectionitems .topcell .loadingbar .gry").animate({ "left": 95.4 * $(".container").data("currentprecent") }, { queue: true, duration: 500 }, function () { alert(1)});
        if (score < 9) {
            $(".detectionitems .seriouscanvas ul").append("<li><div class='question'>" + arrs[0] + "</div><div class='answer'>" + arrs[1] + "</div></li>");
            $(".container").data("seriouscount", $(".container").data("seriouscount") + 1);
            $(".detectionitems .bottomcell .seriouscanvas .caption .itemcount").html($(".container").data("seriouscount"));
        }
        else if (score < 13) {
            $(".detectionitems .normcanvas ul").append("<li><div class='question'>" + arrs[0] + "</div><div class='answer'>" + arrs[1] + "</div></li>");
            $(".container").data("normcount", $(".container").data("normcount") + 1);
            $(".detectionitems .bottomcell .normcanvas .caption .itemcount").html($(".container").data("normcount"));
        }
        else {
            $(".detectionitems .haelthcanvas ul").append("<li><div class='question'>" + arrs[0] + "</div><div class='answer'>" + arrs[1] + "</div></li>");
            $(".container").data("haelthcount", $(".container").data("haelthcount") + 1);
            $(".detectionitems .bottomcell .haelthcanvas .caption .itemcount").html($(".container").data("haelthcount"));
        }

        $(".detectionitems .topcell .detectiontext").text(arrs[2]);


        //总分
        $(".container").data("totalscore", $(".container").data("totalscore") + parseFloat(score));

    };

    //最后要操作的项目
    var LastThing = function () {



        if ($(".detectionitems .seriouscanvas ul li").length === 0)
            $(".detectionitems .seriouscanvas ul").append("<li><div class='question'>共<span class='digit'>0</span>个待处理项</div></li>");
        if ($(".detectionitems .normcanvas ul li").length === 0)
            $(".detectionitems .normcanvas ul").append("<li><div class='question'>共<span class='digit'>0</span>个待处理项</div></li>");
        if ($(".detectionitems .haelthcanvas ul li").length === 0)
            $(".detectionitems .haelthcanvas ul").append("<li><div class='question'>共<span class='digit'>0</span>个待处理项</div></li>");

        if ($(".container").data("totalscore") < 50) {
            $(".detectionitems .topinfluence .dianpupingji").text("很差");
            $(".detectionitems .detectioneditem .detectionresult .one .totalscoretext").text("【很差】");
            $(".detectioncanvas .detectionitems .detectioning img:first-child").attr("src", "/clt/sysfile/ui/detection/images/seriouscanvas.gif");
        }
        else {
            $(".detectioncanvas .detectionitems .detectioning img:first-child").attr("src", "/clt/sysfile/ui/detection/images/normcanvas.png");
            $(".detectionitems .topinfluence .dianpupingji").text("较差");
            $(".detectionitems .detectioneditem .detectionresult .one .totalscoretext").text("【较差】");
        }

        //得分
        $(".detectionitems .detectioneditem .totalscore").text(changeTwoDecimal_f($(".container").data("totalscore")));
        //需要注意项
        $(".detectionitems .detectioneditem .yanzhongxiang").text($(".container").data("seriouscount"));

        //显示评级项
        $(".detectionitems .topinfluence").show();



        //隐藏进度条
        $(".detectionitems .cellright .detectioningitem").hide();
        $(".detectionitems .cellright .detectioneditem").show();
    };

    //重新检测
    $(".detectionitems .detectioneditem .detectionresult .three").click(function () {
        $(".container").data({ seriouscount: 0, normcount: 0, haelthcount: 0, totalscore: 0, currentprecent: 0 });
        $(".detectionitems .cellright .detectioningitem").show();
        $(".detectionitems .cellright .detectioneditem").hide();
        $(".detectionitems .bottomcell .item ul li").remove();
        $(".detectioncanvas .detectionitems .detectioning img:first-child").attr("src", "/clt/sysfile/ui/detection/images/saomiaox86.gif");
        $(".detectionitems .topcell .loadingbar .gry").css("left", "0");
        $(".detectionitems .topinfluence").hide();
        $("#nowdetection").trigger("click");
    });

    //一键优化
    $(".SmallOnkeySet,.OneKeySet,#viewdetail").live("click", function () {
        if ($("#setcurrentper").val() === "3")
            self.location.href = "/onekey.html";
        else
            self.location.href = "/detection/buy.html";
    });

    function changeTwoDecimal_f(x) {
        var f_x = parseFloat(x);
        if (isNaN(f_x)) {
            alert('function:changeTwoDecimal->parameter error');
            return false;
        }
        var f_x = Math.round(x * 100) / 100;
        var s_x = f_x.toString();
        var pos_decimal = s_x.indexOf('.');
        if (pos_decimal < 0) {
            pos_decimal = s_x.length;
            s_x += '.';
        }
        while (s_x.length <= pos_decimal + 2) {
            s_x += '0';
        }
        return s_x;
    }
});