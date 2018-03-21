var TM = TM || {};
((function ($, window) {

    TM.DefenderIndex = TM.DefenderIndex || {};

    var DefenderIndex = TM.DefenderIndex;

    /**
     * 初始化
     * @type {*}
     */
    DefenderIndex.init = DefenderIndex.init || {};
    DefenderIndex.init = $.extend({
        doInit: function(container, isAllcoted) {
        	DefenderIndex.container = container;
            if(isAllcoted == undefined || isAllcoted == null) {
                isAllcoted == false;
            }
        	DefenderIndex.init.initCommentSwitchOp();
        	DefenderIndex.init.initDefenderSwitchOp();
        	DefenderIndex.init.initSmsSwitchOp();
        	
        	DefenderIndex.init.fillUserInfo();
        	
        	DefenderIndex.init.fetchDefenseStatus();
            //DefenderIndex.init.initChapingDealing();
            if(isAllcoted == false) {
                DefenderIndex.init.initNewChapingDealing();
            }
//            TM.Chart.initSearchParams();
//            TM.Chart.drawGoodRate();
//
//            container.find(".search-btn").click(function() {
//                TM.Chart.drawGoodRate();
//            });
//            container.find('.update-btn').click(function(){
//                $.get("/items/syncSellerDsr", function(data){
//                    TM.Alert.load('<p style="font-size:14px">亲，好评率更新成功,点击确定刷新页面</p>',400,230,function(){
//                        window.location.reload();
//                    });
//                });
//            });
        },
        initNewChapingDealing: function(){
            var dealingDiv = $('.chaping-dealing-ranks');
            dealingDiv.find('.chaping-dealing-ranks-table tbody').empty();
            $.get("/SkinDefender/getChiefStaffDetailList", function(data){
                if(data === undefined || data == null) {
                    DefenderIndex.init.initChapingDealing();
                    return;
                }

                dealingDiv.find('.chaping-dealing-ranks-table tbody').append($('#ChiefStaffDetailTr').tmpl(data));
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr').unbind("hover").hover(function(){
                    var thisIndex = $(this).attr("index");
                    var nowIndex =  dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr:visible').attr("index");
                    dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr').hide();
                    dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr[index="'+nowIndex+'"]').show();
                    dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr[index="'+thisIndex+'"]').hide();
                    dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr[index="'+thisIndex+'"]').show();
                }, function(){

                });

                // 展开第一家外包商
                var firstId = dealingDiv.find('..chaping-dealing-ranks-table tbody tr.chapingNameTr').eq(0).attr("index");
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr[index="'+firstId+'"]').hide();
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr[index="'+firstId+'"]').show();

            })

        },
        initChapingDealing: function(){
            var dealingDiv = $('.chaping-dealing-ranks');
            dealingDiv.find('.chaping-dealing-ranks-table tbody').empty();
            var day = new Date().getDay();
            dealingDiv.find('.chaping-dealing-ranks-table tbody').append(DefenderIndex.init.createChapingDealing(DefenderIndex.init.getChapingDealingMap(0), 0));
            dealingDiv.find('.chaping-dealing-ranks-table tbody').append(DefenderIndex.init.createChapingDealing(DefenderIndex.init.getChapingDealingMap(1), 1));
            dealingDiv.find('.chaping-dealing-ranks-table tbody').append(DefenderIndex.init.createChapingDealing(DefenderIndex.init.getChapingDealingMap(2), 2));
            for(var i = 0; i < 6; i++) {
                var index = (i + day) % 6 + 3;
                var chapingDealing = DefenderIndex.init.getChapingDealingMap(index);
                dealingDiv.find('.chaping-dealing-ranks-table tbody').append(DefenderIndex.init.createChapingDealing(chapingDealing, index));
            }
            dealingDiv.find('.chaping-dealing-ranks-table tbody').append(DefenderIndex.init.createChapingDealing(DefenderIndex.init.getChapingDealingMap(9), 9));
            dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr').unbind("hover").hover(function(){
                var thisIndex = $(this).attr("index");
                var nowIndex =  dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr:visible').attr("index");
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr').hide();
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr[index="'+nowIndex+'"]').show();
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingNameTr[index="'+thisIndex+'"]').hide();
                dealingDiv.find('.chaping-dealing-ranks-table tbody tr.chapingDetailTr[index="'+thisIndex+'"]').show();
            }, function(){

            });

        },
        getChapingDealingMap: function(index){
            if(index === undefined || index == null) {
                index = 0;
            }
            if(index < 0 || index > 9) {
                index = 0;
            }

            if(index == 0) {
                return {title: "羞花中差评修改 ", img: "http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2PkVpapXXXXXmXpXXXXXXXXXX-1039626382.jpg",
                    wangwang: "羞花中差评修改", requirement: "10元一条不包赔付8条以上承接", startNum: 8};
            }
            else if(index == 9) {
                return {title: "中差评修改工作室 ", img: "http://img04.taobaocdn.com/imgextra/i4/1039626382/TB21ZZNaXXXXXb4XXXXXXXXXXXX-1039626382.png",
                    wangwang: "maoyang365", requirement: " 20个以上起接  优惠价10元起", startNum: 20};
            }
            else if(index == 4) {
                return {title: "怡心电子商务有限公司 ", img: "http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2JDIMaXXXXXcxXXXXXXXXXXXX-1039626382.png",
                    wangwang: "chenyan809224973", requirement: "处理10个起，10元可议价", startNum: 10};
            }
            else if(index == 3) {
                return {title: "鑫弘售后工作室 ", img: "http://img02.taobaocdn.com/imgextra/i2/1039626382/TB2LvgOaXXXXXaCXXXXXXXXXXXX-1039626382.png",
                    wangwang: "念520456", requirement: "20个起接/8元每条", startNum: 20};
            }
            else if(index == 1) {
                return {title: "鑫盛网络服务公司 ", img: "http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2yYkSaXXXXXXEXXXXXXXXXXXX-1039626382.jpg",
                    wangwang: "流行寂寞1", requirement: "10个中差评开始处理  底价10元", startNum: 20};
            }
            else if(index == 5) {
                return {title: "百合中差评 ", img: "http://img01.taobaocdn.com/imgextra/i1/1039626382/TB27dcQaXXXXXbDXXXXXXXXXXXX-1039626382.gif",
                    wangwang: "shirley4319", requirement: "承接中差评个数按 15元一个 包赔付", startNum: 10};

            }
            else if(index == 2) {
                return {title: "隆泰评价处理公司 ", img: "http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2ky.MaXXXXXcFXXXXXXXXXXXX-1039626382.jpg",
                    wangwang: "rxboy123456:售后经理", requirement: "11元一条，20条起", startNum: 20};
            }
            else if(index == 6) {
                return {title: "悟空电子商务服务公司 ", img: "http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2YipkapXXXXa8XpXXXXXXXXXX-1039626382.png",
                    wangwang: "love谁love", requirement: "10元起步，1个起接单，量大优惠", startNum: 1};
            }
            else if(index == 7) {
                return {title: "淘专家删评工作室 ", img: "http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2BYlHapXXXXaVXXXXXXXXXXXX-1039626382.jpg",
                    wangwang: "lucy5202002", requirement: "10每条 8个起接", startNum: 8};
            }
            // 无忧不要啦
            /*else if(index == 7) {
                return {title: "好评无忧工作室 ", img: "http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2FE0wapXXXXa2XXXXXXXXXXXX-1039626382.jpg",
                    wangwang: "开拓者208111", requirement: "价格8-20元，10个起接", startNum: 10};
            }*/
            else if(index == 8) {
                return {title: "帮帮淘网络 ", img: "http://img01.taobaocdn.com/imgextra/i1/1039626382/TB2EmNyapXXXXXmXXXXXXXXXXXX-1039626382.jpg",
                    wangwang: "zy5201314zt", requirement: "11元一条，可议价，5个起接", startNum: 5};
            }
            // 小杨
            else {
                return {title: "羞花中差评修改 ", img: "http://img04.taobaocdn.com/imgextra/i4/1039626382/TB2yMEKaXXXXXaEXpXXXXXXXXXX-1039626382.jpg",
                    wangwang: "羞花中差评修改", requirement: "10元一条不包赔付8条以上承接", startNum: 8};
            }
        },
        createChapingDealing: function(chapingDealing, index){
            if(chapingDealing === undefined || chapingDealing == null) {
                return $('<tr><td colspan="3"></td></tr>');
            }
            if(index == 0) {
                var html =
                    '<tr style="font-size: 14px;" class="chapingNameTr hidden" index="'+index+'">' +
                        '<td colspan="2" title="'+chapingDealing.title+'"><span class="chapingCompanyName">'+chapingDealing.title+'</span><span class="startNumSpan">'+chapingDealing.startNum+'个起接</span></td>' +
                        '</tr>'+
                        '<tr class="chapingDetailTr" index="'+index+'">' +
                        '<td title="'+chapingDealing.title+'"><img src="'+chapingDealing.img+'" alt="'+chapingDealing.title+'" style="width: 64px; height: 64px;margin: 2px;border: 1px solid #ccc;"></td>' +
                        '<td style="border-right: 1px solid #ccc;" title="'+chapingDealing.requirement+'">' +
                        '<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+chapingDealing.wangwang+'&siteid=cntaobao&status=1&charset=utf-8">' +
                        '<img src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="使用QQ联系我们">' +
                        '</a>' +
                        '<img style="margin-left:18px;" src="http://img03.taobaocdn.com/imgextra/i3/1039626382/TB2StgRaXXXXXamXXXXXXXXXXXX-1039626382.gif" alt="'+chapingDealing.requirement+'">' +
                        '</td>' +
                        '</tr>';
                return html;
            } else {
                var html =
                    '<tr style="font-size: 14px;" class="chapingNameTr" index="'+index+'">' +
                        '<td colspan="2" title="'+chapingDealing.title+'"><span class="chapingCompanyName">'+chapingDealing.title+'</span><span class="startNumSpan">'+chapingDealing.startNum+'个起接</span></td>' +
                        '</tr>'+
                        '<tr class="chapingDetailTr hidden" index="'+index+'">' +
                        '<td title="'+chapingDealing.title+'"><img src="'+chapingDealing.img+'" alt="'+chapingDealing.title+'" style="width: 64px; height: 64px;margin: 2px;border: 1px solid #ccc;"></td>' +
                        '<td style="border-right: 1px solid #ccc;" title="'+chapingDealing.requirement+'">' +
                        '<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+chapingDealing.wangwang+'&siteid=cntaobao&status=1&charset=utf-8">' +
                        '<img src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="使用QQ联系我们">' +
                        '</a>' +
                        '<img style="margin-left:18px;" src="http://img03.taobaocdn.com/imgextra/i3/1039626382/TB2StgRaXXXXXamXXXXXXXXXXXX-1039626382.gif" alt="'+chapingDealing.requirement+'">' +
                        '</td>' +
                        '</tr>';
                return html;
            }

        },
        shoufiveyuanXufei : function(version){
            // 15元续费弹窗
            $.get('/OPUserInterFace/show5yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                        if(data == 'unshowed'){
                            var link = "";
                            if(version <= 20) {
                                link = 'http://to.taobao.com/ECKo2gy';
                            } else if(version <= 30){
                                link = 'http://to.taobao.com/t7Fo2gy';
                            } else if(version <= 40){
                                link = 'http://to.taobao.com/plDo2gy';
                            }
                            var left = ($(document).width() - 500)/2;
                            var top = 130;
                            var html = '<div style="z-index: 19000;position: absolute;" class="five-yuan-xufei-img-dialog"><a target="_blank" href="'+link+'"><img src="http://img02.taobaocdn.com/imgextra/i2/292391495/T2oCN0XpdaXXXXXXXX-292391495.jpg" style="width: 500px;height: 330px;"></a><span class="inlineblock close-five-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;"></span></div>';
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
        },
        shouReInfiveyuanXufei : function(version){
            // 第二次进入才弹5元续费弹窗 （测试效果）
            $.get('/OPUserInterFace/show5yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/ReInfiveyuanshowed',function(data){
                        if(data == 'toshow'){
                            var link = "";
                            if(version <= 20) {
                                link = 'http://to.taobao.com/ECKo2gy';
                            } else if(version <= 30){
                                link = 'http://to.taobao.com/t7Fo2gy';
                            } else if(version <= 40){
                                link = 'http://to.taobao.com/plDo2gy';
                            }
                            var left = ($(document).width() - 500)/2;
                            var top = 130;
                            var html = '<div style="z-index: 19000;position: absolute;" class="five-yuan-xufei-img-dialog"><a target="_blank" href="'+link+'"><img src="http://img04.taobaocdn.com/imgextra/i4/1132351118/T2k3ASXkXXXXXXXXXX_!!1132351118.jpg" style="width: 500px;height: 330px;"></a><span class="inlineblock close-five-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;"></span></div>';
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
                        }
                    })
                }
            })
        },
        shouthreeyuanXufei : function(version){

            // 星级版才弹窗
            if(version <= 20){
                $.get('/OPUserInterFace/showOld3yuanXufei',function(data){
                    if(data == "show") {
                        $.get('/OPUserInterFace/oldthreeyuanshowed',function(data){
                            if(data == 'unshowed'){
                                var link = "http://fuwu.taobao.com/ser/plan/planSubDetail.htm?planId=12651&isHidden=true&cycle=1&itemIds=335746";
                                var left = ($(document).width() - 500)/2;
                                var top = 130;
                                var html = '<table class="three-yuan-xufei-img-dialog" style="position: absolute;z-index: 19000;width: 500px;height: 330px;background: url(http://img04.taobaocdn.com/imgextra/i4/1132351118/T2cB4hXrlaXXXXXXXX_!!1132351118.gif)"><tbody>' +
                                    '<tr style="height: 40px;"><td style="width: 460px;"></td><td style="width: 40px;"><span class="inlineblock close-three-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;z-index: 19100;"></td></tr>' +
                                    '<tr style="height: 290px;"><td style="width: 460px;vertical-align: top;"><a class="inlineblock" style="position: absolute;width: 460px;height: 290px;" target="_blank" href="'+link+'"></a></td><td style="width: 40px;"></td></tr>'+
                                    '</tbody></table>';
                                $('.three-yuan-xufei-img-dialog').remove();
                                var three_yuan = $(html);
                                if($.browser.msie) {
                                    three_yuan.find('.close-five-yuan-dialog').css('filter',"alpha(opacity=10)");
                                    three_yuan.find('.close-five-yuan-dialog').css('background-color',"#D53A04");
                                }
                                three_yuan.css('top',top+"px");
                                three_yuan.css('left',left+"px");
                                three_yuan.find('.close-three-yuan-dialog').click(function(){
                                    three_yuan.remove();
                                    $('body').unmask();
                                });
                                three_yuan.appendTo($('body'));
                                $('body').mask();

                                $.post('/OPUserInterFace/setold3YuanXufeiShowed',function(data){

                                });
                            }
                        })
                    }
                })
            }
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
                                //if(interval > 7*24*3600*1000){
                                    var html = ''+
                                        '<table style="z-index: 1001;width: 500px;height: 330px;background: url(http://img04.taobaocdn.com/imgextra/i4/333336410/T2XuXZXwFXXXXXXXXX-333336410.jpg);position: fixed;_position:absolute;">' +
                                        '<tbody>' +
                                        '<tr style="height: 40px;">' +
                                        '<td style="width: 25%;"></td>'+
                                        '<td style="width: 25%;"></td>'+
                                        '<td style="width: 25%;"></td>'+
                                        '<td style="width: 25%;" class="not-goto-haoping"></td>'+
                                        '</tr>' +
                                        '<tr style="height: 190px;"><td colspan="4"></td></tr>' +
                                        '<tr style="height: 100px;">' +
                                        '<td colspan="2">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=FW_GOODS-1850391">' +
                                        '<div style="cursor: pointer;height: 100px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '<td class="not-goto-haoping" style="cursor: pointer;" colspan="2">' +
                                        '<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E%3A%E9%A3%98%E9%9B%AA&siteid=cntaobao&status=1&charset=utf-8">' +
                                        '<div style="cursor: pointer;height: 100px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '</tr>' +
                                        '</tbody>'+
                                        '</table>'+
                                        '';
                                    var left = ($(document).width() - 500)/2;
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
                                //}
                            });
                        }
                    });

                }
            });
        },
        fillUserInfo: function() {
        	var text = DefenderIndex.container.find('#verText');
            var verBtn = DefenderIndex.container.find('#upgradeBtn');
            var dayLeft = DefenderIndex.container.find('#dayLeftText');
            
            if(TM.isTmall == true) {
            	var notice = DefenderIndex.container.find('#defender-notice');
            	notice.show();
            }
            
            DefenderIndex.container.find('#username').text(TM.name);
            dayLeft.text(TM.timeLeft);
            if(TM.ver > 40){
                text.text('专用版');
                verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.27.QfrmaM&service_code=FW_GOODS-1850391&tracelog=chapingIndex');
                verBtn.text('点击续费');
            } else if(TM.ver == 40){
            	text.text('旗舰版');
            	verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.27.QfrmaM&service_code=FW_GOODS-1850391&tracelog=chapingIndex');
                verBtn.text('点击续费');
            } else if(TM.ver == 30){
            	text.text('标准版');
            	verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.27.QfrmaM&service_code=FW_GOODS-1850391&tracelog=chapingIndex');
                verBtn.text('点击续费');
            } else if(TM.ver == 20) {
            	text.text('扶持版');
            	verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.27.QfrmaM&service_code=FW_GOODS-1850391&tracelog=chapingIndex');
                verBtn.text('点击升级');
            } else if(TM.ver > 0) {
            	text.text('扶持版');
            	verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.27.QfrmaM&service_code=FW_GOODS-1850391&tracelog=chapingIndex');
                verBtn.text('点击升级');
            }
            //DefenderIndex.init.shoufiveyuanXufei(TM.ver);
//            DefenderIndex.init.shouReInfiveyuanXufei(TM.ver);
            DefenderIndex.init.showHaoPingOrNor();
//            DefenderIndex.init.shouthreeyuanXufei(TM.ver);
        },
        fetchDefenseStatus: function() {
	        $.ajax({
	            url : '/skindefender/status',
	            data : {},
	            type : 'post',
	            success : function(dataJson) {
	            	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
	            	DefenderIndex.container.find('#defense-count').text(dataJson.defenseCount);
	            	DefenderIndex.container.find('#comment-count').text(dataJson.commentCount);
	            	DefenderIndex.container.find('#sms-count').text(dataJson.smsCount);
	            	DefenderIndex.container.find('#blacklist-count').text(dataJson.blacklistCount);
	            	DefenderIndex.container.find('#whitelist-count').text(dataJson.whitelistCount);
	            	DefenderIndex.container.find('#smsLeftText').text(dataJson.smsLeft);
	            	if (dataJson.smswarnsCount <= 0) {
	            		DefenderIndex.container.find('#smswarns-config').show();
	            	}
	            }
	        });
        },
 	    initCommentSwitchOp: function() {
	        $.ajax({
	            url : '/autocomments/isOn',
	            data : {},
	            type : 'post',
	            success : function(dataJson) {
//	                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                    return;
	            	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
	                var isOn = dataJson.res;
	                var title = "";
	                var switchStatus = TM.Switch.createSwitch.createSwitchForm(title,true);
	                switchStatus.appendTo(DefenderIndex.container.find(".preComment"));
	                switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
	                    labels:['已开启','已关闭'],
	                    doChange:function(isCurrentOn){
	                        if (isCurrentOn == false) {//要开启
	                            $.ajax({
	                                url : '/autocomments/setOn',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        } else if (isCurrentOn == true) {//要关闭
	                            $.ajax({
	                                url : '/autocomments/setOff',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        }
	                        return true;
	                    },
	                    isOn : isOn
	                });
	            }
	        });
	    },
	    
	    initDefenderSwitchOp: function() {
	        $.ajax({
	            url : '/skindefender/isOn',
	            data : {},
	            type : 'post',
	            success : function(dataJson) {
//	                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                    return;
	            	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
	                var isOn = dataJson.res;
	                var title = "";
	                var switchStatus = TM.Switch.createSwitch.createSwitchForm(title, true);
	                switchStatus.appendTo(DefenderIndex.container.find(".preDelist"));
	                switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
	                    labels:['已开启','已关闭'],
	                    doChange:function(isCurrentOn){
	                        if (isCurrentOn == false) {//要开启
	                            $.ajax({
	                                url : '/skindefender/turnOn',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        } else if (isCurrentOn == true) {//要关闭
	                            $.ajax({
	                                url : '/skindefender/turnOff',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        }
	                        return true;
	                    },
	                    isOn : isOn
	                });
	            }
	        });
	    },
	    
	    initSmsSwitchOp: function() {
	        $.ajax({
	            url : '/skincomment/isOn',
	            data : {},
	            type : 'post',
	            success : function(dataJson) {
//	                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                    return;
	            	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
	                var isOn = dataJson.res;
	                var title = "";
	                var switchStatus = TM.Switch.createSwitch.createSwitchForm(title,true);
	                //console.info(DefenderIndex.container.find(".preSms"));
	                switchStatus.appendTo(DefenderIndex.container.find(".preSms"));
	                switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
	                    labels:['已开启','已关闭'],
	                    doChange:function(isCurrentOn){
	                        if (isCurrentOn == false) {//要开启
	                        	var warnsJson = $.get("/skindefender/warnsCount", function(dataJson){
	                        		var smswarnsCount = dataJson.smswarnsCount;
		                        	if (smswarnsCount == 0) {
		                        		DefenseWarn.submit.doAddWarn();
		                        	}
	                        	});
	                        	
	                            $.ajax({
	                                url : '/skincomment/turnOn',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        } else if (isCurrentOn == true) {//要关闭
	                            $.ajax({
	                                url : '/skincomment/turnOff',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        }
	                        return true;
	                    },
	                    isOn : isOn
	                });
	            }
	        });
	    }
    }, DefenderIndex.init);
    
    
    
    TM.DefenseWarn = TM.DefenseWarn || {};

    var DefenseWarn = TM.DefenseWarn;

    /**
     * 初始化
     * @type {*}
     */
    DefenseWarn.init = DefenseWarn.init || {};
    DefenseWarn.init = $.extend({
        doInit: function(container) {
            var html = DefenseWarn.init.createHtml();
            container.html(html);
            DefenseWarn.container = container;

            container.find(".add-warn-btn").click(function() {
                DefenseWarn.submit.doAddWarn();
            });
//            DefenseWarn.init.initSwitchOp();

            DefenseWarn.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<div class="warn-switch-div"></div> ' +
                '<div class="warn-table-div">' +
                '   <table style="width:100%;border:1px solid #d9d9d9;padding-top:10px;margin-top: 10px;height: 50px;">' +
                '       <tbody>' +
                '       <tr style="width:100%;background:#ffe;">' +
                '           <td><span class="add-warn-btn btn btn-info" style="margin-left:15px;">添加手机</span><a href="/SkinComment/warn" class="btn btn-primary" style="margin-left:15px;">通知设置</a></td>' +
                '           <td style=""><span style="color: #a10000;">注：当短信通知开启后，我们就会给以下的手机号码发送短信通知。<br>通知内容包括：买家给差评提醒，疑似差评师拍下宝贝拦截提醒，黑名单上的买家拍下宝贝提醒等</span></td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <table class="skincomment-table defense-warn-table" style="margin-top: 0px;;border:1px solid #fec;">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 35%;">手机号码</td>' +
                '           <td style="width: 35%;">备注</td>' +
                '           <td style="width: 15%;">修改</td>' +
                '           <td style="width: 15%;">删除</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            return html;

        },
        initSwitchOp: function() {
            $.ajax({
                url : '/skincomment/isOn',
                data : {},
                type : 'post',
                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
                	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
                    var isOn = dataJson.res;
                    var switchStatus = TM.Switch.createSwitch.createSwitchForm("短信通知开启状态");
                    switchStatus.appendTo(DefenseWarn.container.find(".warn-switch-div"));
                    switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
                        labels:['已开启','已关闭'],
                        doChange:function(isCurrentOn){
                            if (isCurrentOn == false) {//要开启
                            	var warnsJson = $.get("/skindefender/warnsCount", function(dataJson){
	                        		var smswarnsCount = dataJson.smswarnsCount;
		                        	if (smswarnsCount == 0) {
		                        		DefenseWarn.submit.doAddWarn();
		                        	}
	                        	});
                            	
                                $.ajax({
                                    url : '/skincomment/turnOn',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
//                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                            return;
                                    	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
                                    }
                                });
                            } else if (isCurrentOn == true) {//要关闭
                                $.ajax({
                                    url : '/skincomment/turnOff',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
//                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                            return;
                                    	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
                                    }
                                });
                            }
                            return true;
                        },
                        isOn : isOn
                    });
                }
            });
        }
    }, DefenseWarn.init);

    DefenseWarn.show = DefenseWarn.show || {};
    DefenseWarn.show = $.extend({
        doShow: function() {
            var tbodyObj = DefenseWarn.container.find(".defense-warn-table").find("tbody");
            tbodyObj.html("");
            $.ajax({
                url : '/skincomment/queryDefenseWarns',
                data : {},
                type : 'post',
                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
                	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
                    tbodyObj.html("");
                    var warnJsonArray = dataJson.res;
                    $(warnJsonArray).each(function(index, warnJson) {
                        var trObj = DefenseWarn.row.createRow(index, warnJson);
                        tbodyObj.append(trObj);
                    });
                    
                    if (warnJsonArray == undefined || warnJsonArray == null || warnJsonArray.length == 0) {
                        tbodyObj.html('<tr><td colspan="4" height="40px">亲，还没有添加监控短信接收手机号哟！<span class="btn btn-primary info-add-warn-btn">添加手机</span></td></tr>');
                        DefenseWarn.container.find(".info-add-warn-btn").click(function() {
                            DefenseWarn.submit.doAddWarn();
                        });
                    }
                    
                }
            });
        }
    }, DefenseWarn.show);


    DefenseWarn.row = DefenseWarn.row || {};
    DefenseWarn.row = $.extend({
        createRow: function(index, warnJson) {
            var html = '' +
                '<tr>' +
                '   <td><span class="telephone"></span> </td>' +
                '   <td><span class="remark"></span> </td>' +
                '   <td><span class="modify-warn-btn commbutton btntext4">修改</span> </td>' +
                '   <td><span class="delete-warn-btn commbutton btntext4">删除</span> </td>' +
                '</tr>' +
                '';

            var trObj = $(html);
            trObj.find(".telephone").html(warnJson.telephone);
            trObj.find(".remark").html(warnJson.remark);
            trObj.find(".modify-warn-btn").click(function() {
                DefenseWarn.submit.doModifyWarn(warnJson.id, warnJson);
            });
            trObj.find(".delete-warn-btn").click(function() {
                DefenseWarn.submit.doDeleteWarn(warnJson.id, warnJson);
            });
            return trObj;
        }
    }, DefenseWarn.row);


    DefenseWarn.submit = DefenseWarn.submit || {};
    DefenseWarn.submit = $.extend({
        doDeleteWarn: function(warnId, warnJson) {
            if (confirm("确定删除手机号码：" + warnJson.telephone + "？") == false)
                return;
            var data = {};
            data.warnId = warnId;
            $.ajax({
                url : '/SkinComment/deleteWarn',
                data : data,
                type : 'post',
                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
                	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
                    //刷新
                    DefenseWarn.show.doShow();
                }
            });

        },
        doAddWarn: function() {
            DefenseWarn.submit.modifyWarnParams.warnId = 0;
            DefenseWarn.submit.modifyWarnParams.warnJson = {};
            DefenseWarn.submit.addOrModifyWarn(true);
        },
        doModifyWarn: function(warnId, warnJson) {
            DefenseWarn.submit.modifyWarnParams.warnId = warnId;
            DefenseWarn.submit.modifyWarnParams.warnJson = warnJson;
            DefenseWarn.submit.addOrModifyWarn(false);
        },
        modifyWarnParams: {
            warnId: 0,
            warnJson: {}
        },
        addOrModifyWarn: function(isAdd) {
            var dialogObj = $(".dialog-div");
            if (dialogObj.length == 0) {
                var html = '' +
                    '<div class="dialog-div" style="background:#fff;">' +
                    '   <table style="margin: 0 auto; margin-top: 20px;">' +
                    '       <tbody>' +
                    '       <tr>' +
                    '           <td style="height:40px;">手机号码：</td>' +
                    '           <td><input type="text" class="warn-telephone-text" style="width: 200px;"/> </td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td>备注(可不填)：</td>' +
                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
                    '       </tr>' +
                    '       </tbody>' +
                    '   </table>' +
                    '</div> ' +
                    '';

                dialogObj = $(html);
                $("body").append(dialogObj);

                var title = "";
                if (isAdd == true)
                    title = "设置手机号码";
                else
                    title = "设置手机号码";
                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:250,
                    width:450,
                    title: title,
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var warnId = DefenseWarn.submit.modifyWarnParams.warnId;
                        var warnJson = DefenseWarn.submit.modifyWarnParams.warnJson;
                        var telephone = dialogObj.find(".warn-telephone-text").val();
                        var remark = dialogObj.find(".warn-remark-text").val();
                        if (telephone == "") {
                            alert("请先输入手机号码");
                            return;
                        }
                        var confrimStr = "";
                        if (warnId > 0) {
                            if (confirm("确定修改手机号码：" + warnJson.telephone + "？") == false) {
                                return;
                            }
                        } else {

                        }

                        var data = {};
                        data.warnId = warnId;
                        data.telephone = telephone;
                        data.remark = remark;
                        $.ajax({
                            url : '/SkinComment/addOrModifyWarn',
                            data : data,
                            type : 'post',
                            success : function(dataJson) {
//                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                    return;
                            	if(dataJson == null || dataJson == undefined) {
                            		alert('操作失败，请稍后再试');
                            		return;
                            	}
                                //刷新
                                dialogObj.dialog('close');
                                DefenseWarn.show.doShow();
                            }
                        });


                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.find(".warn-telephone-text").val(DefenseWarn.submit.modifyWarnParams.warnJson.telephone);
            dialogObj.find(".warn-remark-text").val(DefenseWarn.submit.modifyWarnParams.warnJson.remark);
            dialogObj.dialog('open');
        }
    }, DefenseWarn.submit);


    TM.Chart = TM.Chart || {};

    TM.Chart = $.extend({
        drawGoodRate : function(){
            var ruleData = TM.Chart.getQueryRule();
            $.post('/skincomment/queryGoodRate', ruleData, function(list){
                TM.Chart.renderChart(list);
            });
        },

        getQueryRule: function() {
            var ruleData = {};
            var startTime = DefenderIndex.container.find(".start-time-text").val();
            var endTime = DefenderIndex.container.find(".end-time-text").val();
            ruleData.startTime = startTime;
            ruleData.endTime = endTime;
            return ruleData;
        },
        initSearchParams : function(){
            var now = new Date();
            var lastMonth = new Date();
            lastMonth.setDate(now.getDate() - 30);
            $(".start-time-text").val(lastMonth.format("yyyy-MM-dd"));
            $(".end-time-text").val(now.format("yyyy-MM-dd"));

            DefenderIndex.container.find(".start-time-text").datepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".end-time-text").datepicker("option", "minDate", selectedDate);
                    }
                }}
            );
            DefenderIndex.container.find(".end-time-text").datepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".start-time-text").datepicker("option", "maxDate", selectedDate);
                    }
                }}
            );
        },

        renderChart : function(data){
            var days = TM.Chart.genDays();
            var rates = TM.Chart.genRates(data);
    //        console.info(days);
    //        console.info(rates);

            var start = TM.Chart.parseDate($(".start-time-text").val());
            var end = TM.Chart.parseDate($(".end-time-text").val());

            var step = Math.round(days.length / 10);
            var minY = TM.Chart.smallest(rates) - 0.1;

            var chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'goodRate-charts',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: '店铺综合好评率'
                }, //图表标题
                xAxis: {  //x轴
                    categories: days,   //['六天前', '五天前', '四天前', '三天前', '大前天',  '前天', '昨天'], //x轴标签名称
                    title: '日期',
                    gridLineWidth: 1, //设置网格宽度为1
                    lineWidth: 2,  //基线宽度
                    labels:{step: step, y:26}  //x轴标签位置：距X轴下方26像素
                },
                yAxis: [{  //y轴
    //                startOnTick: false,
    //                endOnTick: false,
    //                min: minY,
                    title: {text: '好评率'}, //标题
                    lineWidth: 2 //基线宽度
                }, {
    //                min: minY,
                    title: {text: '好评率'}, //标题
                    opposite: true,
                    lineWidth: 2 //基线宽度
                }],
                plotOptions:{ //设置数据点
                    line:{
                        dataLabels:{
                            enabled:false  //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                tooltip: {
                    useHTML: true,
                    formatter: function () {                 //当鼠标悬置数据点时的格式化提示
                        return '<div style="line-height: 20px;">日期: <b>' + this.x + '</b>&nbsp;<br><b>' + this.series.name + ': </b><span style="color:red;">' + Highcharts.numberFormat(this.y, 2) + "%</span></div>";
                    }
                },
                series: [
                    {  //数据列
                        name: '好评率',
                        data: rates,
                        yAxis:0
                    }
                ]
            });
        },

        genRates : function(data) {
            var res = [];
            var start = TM.Chart.parseDate($(".start-time-text").val());
            var end = TM.Chart.parseDate($(".end-time-text").val());

            var k = 0;
            var tmp = 100;
            if(data && data.length > 0) {
                tmp = data[0].goodRate;
            }
            for(var i=0; start.getTime() <= end.getTime();i++){
                if(k < data.length){
                    var d = new Date(data[k].ts).formatYMS();
                    if(start.formatYMS() == d){
                        tmp = data[k++].goodRate;
                        res.push(tmp);
                        start.setDate(start.getDate() + 1);
                        continue;
                    }
                }
                res.push(tmp);
                start.setDate(start.getDate() + 1);
            }
            return res;
        },

        genDays : function() {
            var days = [];
            var start = TM.Chart.parseDate($(".start-time-text").val());
            var end = TM.Chart.parseDate($(".end-time-text").val());

            for(var i=0; start.getTime() <= end.getTime();i++){
                days.push(start.format("MM/dd"));
                start.setDate(start.getDate() + 1);
            }
            return days;
        },

        smallest : function(array){
            return Math.min.apply( Math, array );
        },

        largest : function(array){
            return Math.max.apply( Math, array );
        },

        parseDate : function(str){
            str=str.split('-');
            var date = new Date(str[0], str[1]-1, str[2]);
            return date;
        }
    }, TM.Chart);

})(jQuery, window));