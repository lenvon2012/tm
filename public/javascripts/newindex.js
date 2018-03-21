var TM = TM || {};

TM.Newindex = TM.Newindex || {};

((function ($, window) {


    var me = TM.Newindex;

    TM.Newindex.intro = function(){
//        if(true){
//            return;
//        }

        
//        if(TM.ver >= 20){
//            html = "<p>尊贵的尊享版用户,欢迎您来到淘标题</p><p>淘标题包含了 标题优化、热词查询、流量监控等几大神器,期待您来慢慢发现哟</p>";
//        }else{
//        	html = '<a href="http://to.taobao.com/uRbfzgy" style="border: 1px solid #ccc;"><img src="http://img02.taobaocdn.com/imgextra/i2/22902351/T2B.kQXb4XXXXXXXXX_!!22902351.jpg"></a>';
//        }
        if(TM.ver >= 20){return;}

        //html = '<a href="http://to.taobao.com/uRbfzgy" style="border: 1px solid #ccc;"><img src="http://img02.taobaocdn.com/imgextra/i2/22902351/T2B.kQXb4XXXXXXXXX_!!22902351.jpg"></a>';
//        $('.showinfree').show(300);
//        setTimeout(function(){
//        	$('.showinfree').hide(1000);
//        },5000);
        //TM.Alert.load(html, 700, 600);

    }

    TM.Newindex.showXufeiOrNot = function(version,isFirst){
        // isFirst : f means false, t means true
        if(isFirst){
            $.get('/OPUserInterFace/showXufeiOrNot',function(data){
                TM.Newindex.showFirstXufei(version,data);
            });
        } else{
            $.get('/OPUserInterFace/showXufeiOrNot',function(data){
                if(data == "show"){
                    TM.Newindex.showXufei(version);
                }
            });
        }

    }

    TM.Newindex.showFirstXufei = function(version,showXufei){
        if(showXufei == 'noshow') {
            var html = ''+
                '<p style="font-size: 25px;font-weight: bold;margin: 20px;text-align: center;">欢迎来到淘掌柜</p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">淘掌柜主打</p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;"><span style="color: red;font-size: 22px;">标题优化</span>、<span style="color: red;font-size: 22px;">自动上下架</span>、<span style="color: red;font-size: 22px;">自动橱窗</span>、<span style="color: red;font-size: 22px;">自动评价</span></p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">辅以</p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">全网词库,店铺诊断,批量标题修改,top搜词,类目搜词</p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;"><span style="color: red;font-size: 22px;">专注于<span style="font-weight: bold;font-size: 22px;">站内引流</span></span></p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">欢迎您加入卖家QQ交流群</p>'+
                '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;"><span style="color: red;font-size: 24px;">(185867410)</span></p>'+
                '';
            var content = $(html);
            TM.Alert.loadDetail(content,900,600,function(){
                return true;
            },"欢迎来到淘掌柜")
        } else {
            $.get("/OPUserInterFace/xufeishowed",function(data){
                if(data == "unshowed"){
                    $.ajax({
                        type:'GET',
                        url :'/OPUserInterFace/freeLink',
                        dataType:'text',
                        data :{key:version},
                        success :function(data){
                            if(data == "找不到用户名" || data=="用户名为空" || data=="找不到营销链接" || data=="获取营销链接出错"){
                                //TM.Alert.load(data);
                            } else {
                                var html = ''+
                                    '<p style="font-size: 25px;font-weight: bold;margin: 12px;text-align: center;">欢迎来到淘掌柜</p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;">淘掌柜主打</p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;"><span style="color: red;font-size: 40px;">标题优化</span>、<span style="color: red;font-size: 40px;">自动上下架</span>、<span style="color: red;font-size: 40px;">自动橱窗</span>、<span style="color: red;font-size: 40px;">自动评价</span></p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;">辅以</p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;">店铺诊断、批量标题修改、top搜词、类目搜词</p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;"><span style="color: red;font-size: 40px;">专注于<span style="font-weight: bold;font-size: 50px;">站内引流</span></span></p>'+
                                    '<p style="font-size:25px;font-weight: bold;margin: 12px 0px;text-align: center;">新手礼包免费送一个月，点击以下链接获取哦亲</p>'+
                                    '<p style="margin: 25px 0px;text-align: center;" class="free-link"><a target="_blank" href="'+data+'"><span class="free-link" style="font-size: 35px;font-weight: bold;">'+data+'</span></a></p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;">欢迎您加入卖家QQ交流群</p>'+
                                    '<p style="font-size: 15px;font-weight: bold;margin: 12px;text-align: center;"><span style="color: red;font-size: 60px;">(185867410)</span></p>'+
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
                                TM.Alert.loadDetail(content,900,650,function(){
                                    return true;
                                },"新手礼包")
                            }
                        }
                    })
                    // no show any more
                    $.get('/OPUserInterFace/setShowed',function(data){
                        return true;
                    });
                }else{
                    var html = ''+
                        '<p style="font-size: 25px;font-weight: bold;margin: 20px;text-align: center;">欢迎来到淘掌柜</p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">淘掌柜主打</p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;"><span style="color: red;font-size: 40px;">标题优化</span>、<span style="color: red;font-size: 40px;">自动上下架</span>、<span style="color: red;font-size: 40px;">自动橱窗</span>、<span style="color: red;font-size: 40px;">自动评价</span></p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">辅以</p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">店铺诊断、批量标题修改、top搜词、类目搜词</p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;"><span style="color: red;font-size: 40px;">专注于<span style="font-weight: bold;font-size: 50px;">站内引流</span></span></p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;">欢迎您加入卖家QQ交流群</p>'+
                        '<p style="font-size: 15px;font-weight: bold;margin: 20px;text-align: center;"><span style="color: red;font-size: 60px;">(185867410)</span></p>'+
                        '';
                    var content = $(html);
                    TM.Alert.loadDetail(content,900,600,function(){
                        return true;
                    },"欢迎来到淘掌柜")
                }
            });
        }

    }


    TM.Newindex.showXufei = function(version){
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
                            //TM.Alert.load(data);
                        } else {
                            var html = ''+
                                '<p style="font-size: 60px;color: red;font-weight: bold;margin: 20px 0px;text-align: center;">年中大促</p>'+
                                '<p style="font-size:50px;font-weight: bold;margin: 20px 0px;text-align: center;">恭喜你获得免费一个月 奖励</p>'+
                                '<p style="font-size:50px;font-weight: bold;margin: 20px 0px;text-align: center;">点击以下链接获取哦亲</p>'+
                                '<p style="margin: 20px 0px;text-align: center;" class="free-link"><a target="_blank" href="'+data+'"><span class="free-link" style="font-size: 35px;font-weight: bold;">'+data+'</span></a></p>'+
                                '<p style="text-align: center;">备注：此活动每天仅限15人，剩余<span class="remain" style="font-size: 60px;font-weight: bold;">'+remain+'</span>人</p>'+
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

    TM.Newindex.init = function(isFirst){
/*        //淘掌柜右侧礼包从天而降
        var right = (($(window).width()-1000)/2 -120)+'px';
        var bottom = "240px";
        $('#right-award').animate({bottom:bottom,right:right},1500, function(){
            $('#right-award').css('position','fixed');
            $('#right-award').css('_position','absolute');
            var ie6top = expression(eval(document.documentElement.scrollTop+document.documentElement.clientHeight-300));
            $('#right-award').css('_top', ie6top);
        });*/
    	
		// 淘掌柜 运营联系人
		$.ajax({
			url : '/diag/checkUserContact',
			type : "post",
			data : {},
			success : function(data) {
				if(!data.success) {
					var html = "<div style='margin-top: 60px; margin-left: 70px; font-size: 16px; font-family: 微软雅黑'><span style='color: red'>请输入运营联系方式，以方便我们更好的提供服务</span><br><br><span style='margin-left: 50px;'>手机号：<input style='width: 180px; height: 21px; text-align: center;' type='text' class='user_mobile'><span><div>";

					TM.Alert.loadDetail(html, 500, 300, function(){
						var mobile = $(".user_mobile").val();
						if(mobile == "") {
							TM.Alert.load("请先填写手机号码！");
							return false;
						}
						
						var reg = /^1[3|5|8]\d{9}$/;
						if(!reg.test(mobile)) {
							TM.Alert.load("请填写正确的手机号码！");
							return false;
						};
						
						$.ajax({
							url : '/diag/saveUserContact',
							type : "post",
							data : {mobile : mobile},
							timeout: 200000,
							success : function(data) {
								TM.Alert.load(data.message);
								return;
							}
						});
					}, "温馨提示");
				}
			}
		});
		
        $('#right-award').show();
        me.userInfo = $('#userInfo');
        me.subscribe = $('#subscribe');
        me.preComment = me.userInfo.find('.preComment');
        me.preDelist = me.userInfo.find('.preDelist');
        me.preWindow = me.userInfo.find('.preWindow');

        me.createSwitches();
//        me.price();

        var text = $('#verText');
        var verBtn = $('.verbtn');
        var dayLeft = $('#dayLeftText');

        $('#username').text(TM.name);
        dayLeft.text(TM.timeLeft);
//        dayLeft.text('-');
        var key = "诊断版";
        if(TM.ver >= 40){
            text.text('培训版');
            verBtn.attr('href','http://tb.cn/nYhilUy');
            verBtn.text('点击续费');
            $('.award-link-img').show();
            $('.award-li').show();
            $('.before-xianjia').hide();
        }else if(TM.ver >= 30){
            text.text('数据开车版');
            verBtn.attr('href','http://tb.cn/WSljlUy');
            verBtn.text('点击续费');
            $('.award-link-img').show();
            $('.award-li').show();
            $('.before-xianjia').hide();
        }else if(TM.ver >= 20){
            key = "尊享版";
            text.text('尊享版');
            verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.15.lyLEow&service_code=ts-1820059&tracelog=xufei');
            verBtn.text('点击续费');
            $('.award-link-img').show();
            $('.award-li').show();
            $('#before-xianjia-link').html("立即续费");
        }else if(TM.ver >= 10){
            text.text('优化版');
            verBtn.attr('href','http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.15.lyLEow&service_code=ts-1820059&tracelog=xufei');
            verBtn.text('点击升级');
            $('#before-xianjia-link').html("立即升级");
        }else{
            text.text('体验版');
//            verBtn.attr('href','http://to.taobao.com/uRbfzgy');
            verBtn.attr('href','/home/freeup');
            verBtn.text('点击升级');
            $('#before-xianjia-link').html("立即升级");
        }


        // 检测
        if(TM.util.isNotTaobaoCeshi(TM.name)){
            TM.Newindex.showHaoPingOrNor();
            /*TM.Newindex.showXufeiOrNot(key,isFirst);*/
            //TM.Newindex.show5yuanOrNot(TM.ver);

            // 这是原始3元弹窗，图片形式，只有淘掌柜链接
            //TM.Newindex.showOld3yuanOrNot(TM.ver);

            // 这是新版的3元弹窗，非图片形式，可选套餐
            //TM.Newindex.showNew3yuanOrNot(TM.ver);
            TM.Newindex.showPeixun();

            // 促销打折上线弹窗
            TM.Newindex.showDazhe();
            // 培训文案
            /*TM.Alert.load('<p style="font-size: 16px;margin: 5px 0 5px 0;">恭喜您获得<span style="color: red;font-size: 20px;margin-left: 20px;">直通车神器</span></p>' +
                '<p style="font-size: 16px;margin: 5px 0 5px 0;">' +
                '<a style="font-size: 32px;margin-right: 20px;color: red;" href="http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.12.cCu9Ln&service_code=FW_GOODS-1841777&tracelog=search&scm=&ppath=&labels=" target="_blank">车道</a>' +
                '3个月使用时间<span style="color: red;font-size: 20px;margin-left: 20px;">(每天仅需一元哦)</span></p>' +
                '<p style="font-size: 16px;margin: 5px 0 5px 0;">享受每周六晚直通车<span style="color: red;font-size: 20px;margin:0  20px 0 20px;">大神</span>免费授课</p>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE3&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE3&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE7&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE3&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE9&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE9&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE17&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE17&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<br>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE18&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE18&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE21&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE18&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE3&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE3&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>' +
                '<a  target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE9&siteid=cntaobao&status=1&charset=utf-8" ><img style="margin: 10px 8px 10px 0;" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid=%E8%BD%A6%E9%81%93%E4%BC%98%E9%A9%BE%3A%E5%AE%A2%E6%88%B7%E9%A1%BE%E9%97%AE9&site=cntaobao&s=1&charset=utf-8" alt="车道优驾" /></a>',
                450, 300);*/
        }
        //if(parseInt(new Date().getTime()) - parseInt(TM.firstLoginTime) > 24 * 3600 * 1000) {
            $('.shop-pc-online-info-table-div').show();
            $.get("/Diag/shopPCWirelessViewTrade", {platform : 0, interval: 1, endTime: new Date().getTime() - 24 * 3600 * 1000 - 8 * 3600 * 1000}, function(data){
                if(data === undefined || data == null) {
                    TM.Newindex.refreshShopViewTradeInfo();
                    return;
                }
                if(data.success == false) {
                    TM.Newindex.refreshShopViewTradeInfo();
                    return;
                }
                var viewTrade = data[0];
                TM.Newindex.setShopViewTradeInfo(viewTrade);
                $.get("/Diag/getShopPCBounceCount", {interval: 1, endTime: new Date().getTime() - 24 * 3600 * 1000 - 8 * 3600 * 1000}, function(bounceCount){
                    if(bounceCount === undefined || bounceCount == null) {
                        TM.Newindex.setBounceRate(0);
                        return;
                    }
                    if(bounceCount.success == false) {
                        TM.Newindex.setBounceRate(0);
                        return;
                    }
                    var boucceRate = parseInt(viewTrade.uv) == 0 ? 0 : new Number(parseInt(bounceCount) * 1.0 / parseInt(viewTrade.pv)).toPercent(2);
                    TM.Newindex.setBounceRate(boucceRate);
                });
            });

        //}
        // 初始化成交数据表格
        var hourMills = 3600 * 1000;
    	var dayMills = 24 * 3600 * 1000;
    	var timeZoneDiff = 8 * hourMills;
        var nowTime = new Date().getTime();
        var firstLoginTime = parseInt(TM.firstLoginTime);
        var limitTime = parseInt(((firstLoginTime + timeZoneDiff) / dayMills)) * dayMills  - timeZoneDiff + dayMills + timeZoneDiff;
        
        var interval = parseInt((nowTime - limitTime) / dayMills) + 1;
        
        interval = interval > 7 ? 7 : interval;
        if(interval < 1) {
        	$('.one_day_check').show();
        }
        if(interval < 7) {
        	$('.seven_day_check').show();
        }
        $.ajax({
            type:"GET",
            url:"/Diag/shopView",
            data:{interval : parseInt(interval)},
            global: false,
            success:function(data){
                var keyArr = new Array();
                $.each(data, function(key, values){
                    keyArr.push(key);
                });
                for(var i = keyArr.length; i>0; i--){
                    var keyVal = keyArr[i-1];
                    var values = data[keyVal];
                    var itemCollectionRate = values.uv == 0 ? '0.00%' : new Number(values.itemCollectNum / values.uv).toPercent(2);
                    var itemCartRate = values.uv == 0 ? '0.00%' : new Number(values.itemCartNum / values.uv).toPercent(2);
                    var html = '<tr class="app-word-diag-result-table-th">' +
                        '<td>'+ values.dataTime +'</td>' +
                        '<td>'+ values.pv +'</td>' +
                        '<td>'+ values.uv +'</td>' +
                        '<td>'+ Math.round(values.alipayTradeAmt * 100)/100 +'</td>' +
                        '<td>'+ values.alipayAuctionNum +'</td>' +
                        '<td>'+ values.alipayTradeNum +'</td>' +
                        '<td>'+ values.tradeRate +'</td>' +
                        '<td>'+ values.entranceNum +'</td>' +
                        '<td>'+ values.itemCollectNum +'</td>' +
                        '<td>'+ itemCollectionRate +'</td>' +
                        '<td>'+ values.itemCartNum +'</td>' +
                        '<td>'+ itemCartRate +'</td>' +
                        '<td>'+ values.searchUv +'</td>' +
                        '<td>'+ values.pcUv +'</td>' +
                        '</tr>';
                    $('#view_item_show').append(html);
                }
            }
        });
//        me.
        me.userInfo.find('.indexGoDetail').click(function(){
//            TM.widget.showWillOpen($(this).attr('tag'));
            window.location.href = '/kits/'+$(this).attr('tag');
        });

//        me.price();
        $('#shop_check_startbtn').click(function(){
            if(TM.ver < 10){
                $('#dialog').dialog('open');
            }
        });

        if(TM.comeon && TM.ver < 10){
            TM.Alert.load($('<a href="http://to.taobao.com/9pihrjy"><img src="http://img04.taobaocdn.com/imgextra/i4/22902351/T2s7N8XjBcXXXXXXXX_!!22902351.jpg"></a>'),780,450);
        }

    }

    TM.Newindex.setBounceRate = function(bounceRate){
        $('.shop-pc-online-info-table div.bounceRate').text(bounceRate);
    }

    TM.Newindex.refreshShopViewTradeInfo = function(){
        $('.shop-pc-online-info .shop-pc-online-info-table .key-value-tr .value').text("0");
    }

    TM.Newindex.setShopViewTradeInfo = function(data){
        if(data === undefined || data == null) {
            return;
        }
        if(data.success == false) {
            return;
        }
        $('.shop-pc-online-info-table div.pv').text(data.pv);
        $('.shop-pc-online-info-table div.uv').text(data.uv);
        $('.shop-pc-online-info-table div.viewRepeat').text(data.view_repeat_num);
        $('.shop-pc-online-info-table div.tradeRepeat').text(data.trade_repeat_num);
        $('.shop-pc-online-info-table div.alipayTradeNum').text(data.alipay_trade_num);
        $('.shop-pc-online-info-table div.alipayItemNum').text(data.alipay_auction_num);
        $('.shop-pc-online-info-table div.alipayTradeAmount').text(Math.floor(data.alipay_trade_amt));
        $('.shop-pc-online-info-table div.alipayUserNum').text(data.alipay_winner_num);
        var accessDeepth = parseInt(data.uv) > 0 ? new Number(data.pv / data.uv).toFixed(2) : 0;
        var tradeRate = parseInt(data.uv) > 0 ? new Number(data.alipay_winner_num / data.uv).toPercent(2) : "0.00%";
        var viewRepeatRate = parseInt(data.uv) > 0 ? new Number(data.view_repeat_num / data.uv).toPercent(2) : "0.00%";
        $('.shop-pc-online-info-table div.accessDeepth').text(accessDeepth);
        $('.shop-pc-online-info-table div.tradeRate').text(tradeRate);
        $('.shop-pc-online-info-table div.viewRepeatRate').text(viewRepeatRate);
    }

    TM.Newindex.show5yuanOrNot = function(version){
        if(version <= 20){
            $.get('/OPUserInterFace/show5yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                        if(data == 'unshowed'){
                            var link = "";
                            if(version <= 20) {
                                link = 'http://tb.cn/W259eTy';
                            } else if(version <= 30){
                                link = "http://tb.cn/gT39eTy";
                            } else if(version <= 40){
                                link = "http://tb.cn/ku09eTy";
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

    TM.Newindex.showDazhe = function(){
        $.get('/OPUserInterFace/showDazhe', function (data) {
            if (data == "noshow") {
                return;
            }
            $.get('/OPUserInterFace/dazheshowed', function (data) {
                if (data == 'unshowed') {
                    var html = '<table style="width: 100%;height: 200px;"><tbody>' +
                        '<tr><td><span style="font-size: 28px;color: red;font-weight: bold;">重要公告</span></td></tr>' +
                        '<tr><td><span style="font-size: 20px;font-weight: bold;">淘掌柜促销打折功能上线啦!!!</span></td></tr>' +
                        '<tr><td><span><a style="font-size: 28px;font-weight: bold;color: red;" href="/Sales/index">立即体验</a></span></td></tr>' +
                        '</tbody></table>';

                    var obj = $(html);
                    TM.Alert.load(obj, 500, 350, function(){

                    }, false, "促销打折上线");
                    $.post('/OPUserInterFace/setDazheShowed', function (data) {

                    });
                }
            })
        })
    }

    TM.Newindex.showPeixun = function(version){

        // 至尊版才弹窗
        //if(version == 20){
        $.get('/OPUserInterFace/showPeixun', function (data) {
            if (data == "noshow") {
                return;
            }
            var content = data.htmlStr;
            var width = $.parseJSON(data.anchorStyle).width;
            if(width == undefined || width == null || width == "") {
                width = "400";
            }
            var height = $.parseJSON(data.anchorStyle).height;
            if(height == undefined || height == null || height == "") {
                height = "300";
            }
            $.get('/OPUserInterFace/peixunshowed', function (data) {
                if (data == 'unshowed') {
                    if(content == undefined || content == null || data == "培训内容，这里要填html" || data == ""){
                        content = "敬请期待下次培训";
                    }
                    TM.Alert.loadDetail(content, width, height, null,"培训公告");
                    $.post('/OPUserInterFace/setPeixunShowed', function (data) {

                    });
                }
            })
        })
        //}
    }

    TM.Newindex.showNew3yuanOrNot = function(version){

        // 至尊版才弹窗
        if(version == 20){
            $.get('/OPUserInterFace/show3yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/threeyuanshowed',function(data){
                        if(data == 'unshowed'){

                            TM.loadMeal();
                            $.post('/OPUserInterFace/set3YuanXufeiShowed',function(data){

                            });
                        }
                    })
                }
            })
        }
    }

    TM.Newindex.showOld3yuanOrNot = function(version){

        // 至尊版才弹窗
        if(version == 20){
            $.get('/OPUserInterFace/showOld3yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/oldthreeyuanshowed',function(data){
                        if(data == 'unshowed'){
                            var link = "http://fuwu.taobao.com/ser/plan/planSubDetail.htm?planId=12651&isHidden=true&cycle=1&itemIds=285831";
                            var left = ($(document).width() - 500)/2;
                            var top = 130;
                            /*var html = '<div style="z-index: 19000;position: absolute;" class="three-yuan-xufei-img-dialog">' +
                                '<a target="_blank" href="'+link+'">' +
                                '<img src="http://img02.taobaocdn.com/imgextra/i2/79742176/T2qswpXbtaXXXXXXXX_!!79742176.gif" style="width: 500px;height: 185px;">' +
                                '</a>' +
                                '<span class="inlineblock close-three-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;z-index: 19100;">' +
                                '</span>' +
                                '</div>';*/
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
    }

    TM.Newindex.showHaoPingOrNor = function(){
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
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=ts-1820059">' +
                                    '<div style="cursor: pointer;width: 550px;height: 270px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td style="width: 200px;">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=ts-1820059">' +
                                    '<div style="cursor: pointer;width: 200px;height: 270px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '</tr>' +
                                    '<tr style="height: 80px;width: 750px;">' +
                                    '<td style="width: 550px">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=ts-1820059">' +
                                    '<div style="cursor: pointer;width: 200px;height: 80px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td class="" style="width: 200px;cursor: pointer;">' +
                                    '<a style="margin-left: 110px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&amp;touid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E%3A%E9%A3%98%E9%9B%AA&amp;siteid=cntaobao&amp;status=1&amp;charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
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
    }

    TM.Newindex.price = function(){
        if(TM.ver<10){
            $("#prize_track").show(3000);
        }

        $("#dialog").dialog({
            closeText: "关闭",
            autoOpen: false,
            width: 800,
            resizable: false,
            title:"温馨提示栏",
            modal: true,
            buttons: [
                {
                    text: "关闭",
                    click: function() {
                        $(this).dialog( "close" );
                    }
                }
            ]
        });
        // Link to open the dialog
        // alert(TM.ver);
        $( "#open_prize" ).click(function( event ) {
            $( "#dialog" ).dialog( "open" );
            event.preventDefault();
        });

        $("#buy_version_1").click(function(){
            $.ajax({
                type:"POST",
                url:"/Buy/toPayPage",
                data:{"time":"a_12m"},
                success:function(text){
                    window.location.href = text;
                }
            });
        });

        $('#version_middle').click(function(){
            $.get('/Buy/toPayPage',{"time":"a_12m"},function(text){
                window.location.href = text;
            });
        });

        $("#buy_version_2").click(function(){
            $.ajax({
                type:"POST",
                url:"/Buy/toPayPage",
                data:{"time":"a_6m"},
                success:function(text){
                    window.location.href = text;
                }
            });
        });
        $("#buy_version_3").click(function(){
            $.ajax({
                type:"POST",
                url:"/Buy/toPayPage",
                data:{"time":"b_12m"},
                success:function(text){
                    window.location.href = text;
                }
            });
        });
        $("#buy_version_4").click(function(){
            $.ajax({
                type:"POST",
                url:"/Buy/toPayPage",
                data:{"time":"b_6m"},
                success:function(text){
                    window.location.href = text;
                }
            });
        });

        $('#version_foot').click(function(){
            $.get('/Buy/toPayPage', {"time":"b_12m"},
                function (text) {
                    window.location.href = text;
                }
            );
        });
    }


    TM.Newindex.createSwitches = function(){
        TM.widget.createShowSwitch(me.preWindow);
        TM.widget.createDelistSwitch(me.preDelist);
        TM.widget.createCommentSwitch(me.preComment);
    }



})(jQuery,window))
