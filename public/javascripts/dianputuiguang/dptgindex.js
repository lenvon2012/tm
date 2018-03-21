var TM = TM || {};
((function ($, window) {
    TM.DptgIndex = TM.DptgIndex || {};

    var DptgIndex = TM.DptgIndex;

    DptgIndex.init = DptgIndex.init || {};
    DptgIndex.init = $.extend({
        doInit: function(container) {
            DptgIndex.container = container;
            var html = DptgIndex.init.createHtml();
            container.html(html);

            DptgIndex.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<div >' +
                /*'   <div class="new-tip-bar recommend-fuwus-bar" >强 烈 推 荐</div> ' +
                '<table class="recommend-fuwus"><tr><td class="recommend">'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<img class="borderimg" src="http://img01.taobaocdn.com/top/i1/T1JFu2XzdXXXaCwpjX.png" style="margin-bottom:15px;"/></a>'+
                '<a target="_blank"  href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<div class="btn-ordernow"></div></a>'+
                '</td><td class="recommend">'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<img class="borderimg" src="http://img01.taobaocdn.com/top/i1/T1JFu2XzdXXXaCwpjX.png" style="margin-bottom:15px;" /></a>'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<div class="btn-ordernow"></div></a>'+
                '</td><td class="recommend">'+
*//*                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-13126&tracelog=taoliuliang" >'+
                '<img class="borderimg" src="http://img01.taobaocdn.com/top/i1/T1oSONXEXdXXb1upjX.jpg" style="margin-bottom:15px;" /></a>'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-13126&tracelog=taoliuliang" >'+
                '<div class="btn-ordernow"></div></a>'+
*//*
                '</td></tr></table>'+*/
                '   <div class="new-tip-bar">店 铺 信 息</div> ' +
                /*'   <table class="tip-bar-table">' +
                '       <tbody>' +
                '           <tr><td class="bar-left"></td><td class="bar-middle">店 铺 信 息</td><td class="bar-right"></td></tr>' +
                '       </tbody>' +
                '   </table> ' +*/
                '   <table style="margin: 40px 0px; width: 100%;">' +
                '       <tbody>' +
                '           <td style="text-align: center; width: 90%; ">' +
                '               <table class="shop-info-table base-info-table" style="margin: 0 auto; width: 70%;">' +
                '                   <tbody>' +
                '                       <tr>' +
                '                           <td colspan="1" class="tip-dot" >掌柜：</td>' +
                '                           <td colspan="1" style="" class="result-info user-nick-td"></td>' +
                '                           <td colspan="2" style="" class=""></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td style="vertical-align: middle;" colspan="1" class="tip-dot">订购版本：</td>' +
                '                           <td style="vertical-align: middle;" colspan="1" class="result-info buy-version-td"></td>' +
                '                           <td colspan="2"><div><a target="_blank" title="我要升级" class="tmbtn yellow-btn" href="http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.12.gbGCFZ&service_code=FW_GOODS-1848326&tracelog=search&scm=&ppath=">我 要 升 级</a></div></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">总优质推广位个数：</td>' +
                '                           <td class="result-info total-popularized-num"></td>' +
                '                           <td class="tip-dot">总热销推广位个数：</td>' +
                '                           <td class="result-info total-hot-popularized-num"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">已用优质推广位个数：</td>' +
                '                           <td class="result-info used-popularized-num"></td>' +
                '                           <td class="tip-dot">已用热销推广位个数：</td>' +
                '                           <td class="result-info used-hot-popularized-num"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">剩余优质推广位个数：</td>' +
                '                           <td class="result-info remain-popularized-num"></td>' +
                '                           <td class="tip-dot">剩余热销推广位个数：</td>' +
                '                           <td class="result-info remain-hot-popularized-num"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">参加好评送优质推广：</td>' +
                '                           <td class="result-info has-award"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td colspan="2"><div><a class="big-op-btn" href="/dianputuiguang/tuiguang">优 质 推 广</a></div></td>' +
                '                           <td colspan="2"><div><a class="big-op-btn" href="/dianputuiguang/hotrecommend">热 销 推 广</a></div></td>' +
                '                       </tr>' +

                '                   </tbody>' +
                '               </table> ' +
                '           </td>' +
                /*'           <td style="width: 40%; text-align: left;">' +
                '               <div><a target="_blank" title="我要升级" class="big-op-btn" href="http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.12.gbGCFZ&service_code=FW_GOODS-1848326&tracelog=search&scm=&ppath=">我 要 升 级</a></div>'+
                '               <div  style="margin-top: 30px;display:none;"><a class="big-op-btn" target="_blank" href="http://www.taovgo.com/">查 看 推 广</a></div>' +
                '           </td>' +*/
                '       </tbody>' +
                '   </table> ' +
                '</div>' +
                '';


            return html;
        }
    }, DptgIndex.init);


    DptgIndex.show = DptgIndex.show || {};
    DptgIndex.show = $.extend({
        doShow: function() {
            var data = {};
            $.ajax({
                url : '/popularize/getUserInfo',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;
                    var container = DptgIndex.container;

                    container.find(".user-nick-td").html(userJson.username);
                    container.find(".buy-version-td").html(userJson.version);

                    //userJson.award = true;
                    if (userJson.award == true) {
                        container.find(".total-popularized-num").html(userJson.totalNum + " + 1 (好评送推广)");
                        container.find(".has-award").html("已参加");
                        userJson.remainNum++;
                    } else {
                        container.find(".total-popularized-num").html(userJson.totalNum);
                        var html = '' +
                            '<a href="/dianputuiguang/award" class="attend-award-link">立即参加>></a>' +
                            '';
                        container.find(".has-award").html(html);
                    }
                    container.find('.total-hot-popularized-num').html(userJson.hotTotalNum);
                    container.find('.used-hot-popularized-num').html(userJson.hotUsedNum);
                    container.find('.remain-hot-popularized-num').html(userJson.hotRemainNum);
                    container.find(".used-popularized-num").html(userJson.popularizedNum);
                    container.find(".remain-popularized-num").html(userJson.remainNum);
                    if(TM.util.isNotTaobaoCeshi(dataJson.username)) {
                        DptgIndex.show.showXufeiOrNot(userJson.version);
                        // 5元续费弹窗
                        $.get('/OPUserInterFace/show5yuanXufei',function(data){
                            if(data == "show") {
                                $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                                    if(data == 'unshowed'){

                                        $.get("/OPUserInterFace/aituiguangNewFiveYuan", function(link){
                                            if(link === undefined || link == null) {
                                                return;
                                            }
                                            if(link.success == false) {
                                                return;
                                            }
                                            var left = ($(document).width() - 500)/2;
                                            var top = 130;
                                            var html = '<div style="z-index: 19000" class="five-yuan-xufei-img-dialog"><a target="_blank" href="'+link.message+'"><img src="http://img04.taobaocdn.com/imgextra/i4/1132351118/T2k3ASXkXXXXXXXXXX_!!1132351118.jpg" style="width: 500px;height: 330px;"></a><span class="inlineblock close-five-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;"></span></div>';
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
                                            });
                                            $('body').mask();
                                            five_yuan.appendTo($('body'));
                                            $.post('/OPUserInterFace/set5YuanXufeiShowed',function(data){

                                            });
                                        });

                                    }
                                })
                            }
                        })

                        DptgIndex.show.showHaoPingOrNor();
                        DptgIndex.show.showFreeOneMonthOrNor();
                    }
                }
            });
        },
        showFreeOneMonthOrNor: function(){
            $.get('/OPUserInterFace/showFreeOneMonth',function(res){
                if(res == 'show'){
                    $.get('/OPUserInterFace/freeonemonthshowed',function(data){
                        if(data == "unshowed") {
                            var version;
                            if(TM.ver === undefined || TM.ver == null) {
                                return;
                            }
                            var version = parseInt(TM.ver);
                            var link = "";
                            if(version == 1) {
                                // 1个推广位
                                link = "http://tb.cn/dEc6vgy";
                            } else if(version == 20) {
                                // 3个推广位
                                link = "http://tb.cn/cpV6vgy";
                            } else if(version == 30) {
                                // 5个推广位
                                link = "http://tb.cn/pyW6vgy";
                            } else if(version == 40) {
                                // 10个推广位
                                link = "http://tb.cn/L9T6vgy";
                            } else if(version == 50) {
                                // 20个推广位
                                link = "http://tb.cn/9AV6vgy";
                            } else if(version == 60) {
                                // 30个推广位
                                link = "http://tb.cn/ca66vgy";
                            }
                            var html = '<table style="width: 100%; height: 100%;text-align: center;vertical-align: middle;">' +
                                '<tbody>' +
                                '<tr>' +
                                '<td><span style="font-size: 30px;color: red;font-weight: bold;">夏日特惠</span></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td><span>恭喜您获得爱推广</span><span style="font-size: 30px;color: red;font-weight: bold;margin-left: 10px;">免费一个月</span><span>奖励</span></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td><span style="font-size: 30px;color: red;font-weight: bold;margin-left: 10px;">联系客服代付</span>' +
                                '<a style="margin-left: 20px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&amp;touid=boyvon%3A%E6%99%AF%E5%AE%8F&amp;siteid=cntaobao&amp;status=1&amp;charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
                                '</td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td><span style=""><a target="_blank" class="freeonemonthlink red" style="font-size: 30px;font-weight: bold;" href="'+link+'">立即获取</a></span></td>' +
                                '</tr>' +
                                '</tbody>' +
                                '</table>';
                            var obj = $(html);
                            setInterval(function(){
                                obj.find('.freeonemonthlink').toggleClass("red");
                            }, 300);

                            TM.Alert.loadDetail(obj, 600, 380, function(){

                            }, "夏日特惠");
                            $.post('/OPUserInterFace/setFreeOneMonthShowed',function(data){

                            });
                        }
                    });

                }
            });
        },
        showHaoPingOrNor: function(){
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
                                        '<table style="z-index: 1001;width: 750px;height: 350px;background: url(http://img02.taobaocdn.com/imgextra/i2/1039626382/T2ViPDXHpXXXXXXXXX-1039626382.png);position: fixed;_position:absolute;">' +
                                        '<tbody>' +
                                        '<tr style="height: 270px;width: 750px;">' +
                                        '<td style="width: 550px;">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=FW_GOODS-1848326">' +
                                        '<div style="cursor: pointer;width: 550px;height: 270px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '<td style="width: 200px;">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=FW_GOODS-1848326">' +
                                        '<div style="cursor: pointer;width: 200px;height: 270px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '</tr>' +
                                        '<tr style="height: 80px;width: 750px;">' +
                                        '<td style="width: 550px">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=FW_GOODS-1848326">' +
                                        '<div style="cursor: pointer;width: 200px;height: 80px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '<td class="" style="width: 200px;cursor: pointer;">' +
                                        '<a style="margin-left: 110px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E6%99%AF%E5%AE%8F%E5%A6%B9%E5%AD%90&siteid=cntaobao&status=1&charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
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
                                    content.unbind('click').click(function(){
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
        showXufeiOrNot : function(version){
            $.get('/OPUserInterFace/showXufeiOrNot',function(data){
                if(data == "show"){
                    DptgIndex.show.showXufei(version);
                }
            });
        },
        showXufei : function(version){
            var gongxi = "恭喜你获得免费六个月 奖励";
            if(version == "体验版"){
                gongxi = "恭喜您获得升级一个优质位六个月 奖励"
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
                        data :{key:version},
                        success :function(data){
                            if(data == "找不到用户名" || data=="用户名为空" || data=="找不到营销链接" || data=="获取营销链接出错"){
                                TM.Alert.load(data);
                            } else {
                                var html = ''+
                                    '<p style="font-size: 60px;color: red;font-weight: bold;margin: 20px 0px;">年中大促</p>'+
                                    '<p style="font-size:50px;font-weight: bold;margin: 20px 0px;">'+gongxi+'</p>'+
                                    '<p style="font-size:50px;font-weight: bold;margin: 20px 0px;">点击以下链接获取哦亲</p>'+
                                    '<p style="margin: 20px 0px;" class="free-link"><a target="_blank" href="'+data+'"><span class="free-link" style="font-size: 35px;font-weight: bold;">'+data+'</span></a></p>'+
                                    '<p>备注：此活动每天仅限15人，剩余<span class="remain" style="font-size: 60px;font-weight: bold;">'+remain+'</span>人</p>'+
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
        }
    },  DptgIndex.show);

})(jQuery,window));