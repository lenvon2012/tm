var TM = TM || {};
((function ($, window) {
    TM.VTuiguangIndex = TM.VTuiguangIndex || {};

    var VTuiguangIndex = TM.VTuiguangIndex;

    VTuiguangIndex.init = VTuiguangIndex.init || {};
    VTuiguangIndex.init = $.extend({
        doInit: function(container) {
            VTuiguangIndex.container = container;
            var html = VTuiguangIndex.init.createHtml();
            container.html(html);

            VTuiguangIndex.show.doShow();
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
    }, VTuiguangIndex.init);


    VTuiguangIndex.show = VTuiguangIndex.show || {};
    VTuiguangIndex.show = $.extend({
        doShow: function() {
            var data = {};
            $.ajax({
                url : '/popularize/getUserInfo',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;
                    var container = VTuiguangIndex.container;

                    container.find(".user-nick-td").html(userJson.username);
                    container.find(".buy-version-td").html("基础版");


                    if(TM.util.isNotTaobaoCeshi(dataJson.username)) {
                        //VTuiguangIndex.show.showXufeiOrNot(userJson.version);
                        // 5元续费弹窗
                        $.get('/OPUserInterFace/show5yuanXufei',function(data){
                            if(data == "show") {
                                $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                                    if(data == 'unshowed'){
                                        var version = dataJson.level;
                                        var link = "http://tb.cn/G7er6ey";

                                        var left = ($(document).width() - 500)/2;
                                        var top = 130;
                                        var html = '<div style="z-index: 19000" class="five-yuan-xufei-img-dialog"><a target="_blank" href="'+link+'"><img src="http://img04.taobaocdn.com/imgextra/i4/1132351118/T2k3ASXkXXXXXXXXXX_!!1132351118.jpg" style="width: 500px;height: 330px;"></a><span class="inlineblock close-five-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;"></span></div>';
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
                                    }
                                })
                            }
                        })
                    }
                }
            });
        },
        showXufeiOrNot : function(version){
            $.get('/OPUserInterFace/showXufeiOrNot',function(data){
                if(data == "show"){
                    VTuiguangIndex.show.showXufei(version);
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
    },  VTuiguangIndex.show);

})(jQuery,window));