var TM = TM || {};

((function ($, window) {
    TM.MobileDesc = TM.MobileDesc || {};

    var MobileDesc = TM.MobileDesc;

    var me = MobileDesc;
    /**
     * 初始化
     * @type {*}
     */
    MobileDesc.init = MobileDesc.init || {};
    MobileDesc.init = $.extend({
        doInit: function(container) {
            MobileDesc.container = container;
            MobileDesc.init.initSearchArea();
//            MobileDesc.show.loadItems();
            container.find(".search-btn").click(function() {
            	MobileDesc.show.doShow();
            });

            container.find("#itemsCat").change(function(){
                MobileDesc.show.doShow();
            });
            container.find("#taobaoCat").change(function(){
                MobileDesc.show.doShow();
            });
            container.find("#itemsStatus").change(function(){
                MobileDesc.show.doShow();
            });

            container.find('.skincomment-table thead input.checkAll').click(function(){
                var oThis = $(this);
                if(oThis.attr('checked')=='checked'){
                    container.find('.skincomment-table tbody input.checkAll').attr('checked',true);
                }else{
                    container.find('.skincomment-table tbody input.checkAll').attr('checked',false);
                }
            });
            container.find('.batch-gen-mobile-page').click(function(){
                var arr = [];
                container.find('.skincomment-table tbody input.checkAll:checked').each(function(){
                    arr.push($(this).attr('numiid'));
                })
                if(arr.length ==0){
                    alert('请先选中宝贝哟亲');
                    return;
                }
                MobileDesc.show.submitBatch(arr.join(','));
            });
            MobileDesc.show.doShow();
            MobileDesc.init.showDialog();
        },
        showDialog : function(){
            MobileDesc.init.showFiveYuan();
            MobileDesc.init.showHaoPingOrNor();
            //MobileDesc.init.showFreeOneMonthOrNor();
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
                            var link = "http://tb.cn/M5yGIey";
                            var html = '<table style="width: 100%; height: 100%;text-align: center;vertical-align: middle;">' +
                                '<tbody>' +
                                '<tr>' +
                                '<td><span style="font-size: 30px;color: red;font-weight: bold;">夏日特惠</span></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td><span>恭喜您获得手机详情页</span><span style="font-size: 30px;color: red;font-weight: bold;margin-left: 10px;">免费一个月</span><span>奖励</span></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td><span style="font-size: 30px;color: red;font-weight: bold;margin-left: 10px;">联系客服代付</span>' +
                                '<a style="margin-left: 20px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E4%B8%8A%E5%AE%98_%E5%B0%8F%E9%9B%AA&siteid=cntaobao&status=1&charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
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
        showFiveYuan: function(){
            $.get('/OPUserInterFace/show5yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                        if(data == 'unshowed'){
                            var link = "http://tb.cn/M5yGIey";
                            //var link = "http://to.taobao.com/ZEIb3gy";

                            var left = ($(document).width() - 500)/2;
                            var top = 130;
                            var html = '<div style="z-index: 19000;position: absolute;" class="five-yuan-xufei-img-dialog">' +
                                '<a target="_blank" href="'+link+'">' +
                                //'<img src="/img/dazhe/xx.jpg" style="width: 500px;height: 330px;">' +
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

                            // no show any more
                            $.get('/OPUserInterFace/set5YuanXufeiShowed',function(data){
                                return true;
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
                                    setTimeout(function(){
                                        var html = ''+
                                            '<table style="z-index: 1001;width: 750px;height: 350px;background: url(http://img02.taobaocdn.com/imgextra/i2/1039626382/T2ViPDXHpXXXXXXXXX-1039626382.png);position: fixed;_position:absolute;">' +
                                            '<tbody>' +
                                            '<tr style="height: 270px;width: 750px;">' +
                                            '<td style="width: 550px;">' +
                                            '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?service_code=service-0-22735">' +
                                            '<div style="cursor: pointer;width: 550px;height: 270px;"></div>'+
                                            '</a>'+
                                            '</td>'+
                                            '<td style="width: 200px;">' +
                                            '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?service_code=service-0-22735">' +
                                            '<div style="cursor: pointer;width: 200px;height: 270px;"></div>'+
                                            '</a>'+
                                            '</td>'+
                                            '</tr>' +
                                            '<tr style="height: 80px;width: 750px;">' +
                                            '<td style="width: 550px">' +
                                            '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?service_code=service-0-22735">' +
                                            '<div style="cursor: pointer;width: 200px;height: 80px;"></div>'+
                                            '</a>'+
                                            '</td>'+
                                            '<td class="" style="width: 200px;cursor: pointer;">' +
                                            '<a style="margin-left: 110px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E7%BA%A2%E5%BF%83%E5%9B%A2%E9%98%9F&siteid=cntaobao&status=1&charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
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
                                    }, 60000);

                                }
                            });
                        }
                    });

                }
            });
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
        }
    }, MobileDesc.init);
    
    MobileDesc.show = MobileDesc.show || {};
    MobileDesc.show = $.extend({
        doShow: function() {
            var params = $.extend({
                "s":"",
                "status":2,
                "catId":"",
                "sort":1,
                "lowBegin":0,
                "ps":5,
                "topEnd":100
            },MobileDesc.show.getParams());
            
            MobileDesc.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: params,
                    dataType: 'json',
                    url: '/wireless/items',
                    callback: function(data) {
                    	var list = data.res;
                    	if (list == undefined || list == null) {
                    		$('#rows').empty();
                    		$('#rows').html('<tr><td colspan="4" height="40px">亲，没找到相关内容哦</td></tr>');
                    		return;
                    	}

                    	$('#rows').empty();
                        $('#tplItem').tmpl(list).appendTo('#rows');
                        $("#rows").find(".gen-mobile-page").click(function(){
                            var oThis = $(this);
                            var curTr = $(this).parent().parent();
                            var numIid = curTr.attr("numiid");
                            MobileDesc.show.submitBatch(numIid);
                        });

//                        $("#rows").find(".gen-mobile-page").click(function(){
//                            console.info($(this).parent().parent().attr("numiid"));
//                            var curTr = $(this).parent().parent();
//                            var numIid = curTr.attr("numiid");
//                            var infoArea = curTr.next().find(".info-area");
//                            $.get("/wireless/genDesc", {numIid: numIid}, function(data){
//                                console.info(data);
//                                infoArea.html('dendddddddd')
//                                infoArea.show();
//                            });
//                        });
                    }
                }
            });
        },
        submitBatch: function(numIids){
            $.post('/wireless/batchSubmit',{
                'numIids':numIids,
//                'config.numIids':numIids,
                'config.skipExist':false
            },function(){
                var num = numIids.split(',').length;
                alert(num+'个宝贝的任务已经提交,请您稍后至任务中心下载');
            });
        },
        refresh: function() {
            MobileDesc.show.doShow();
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
            
            var only = $('#onlyItemLimit option:selected').attr("tag");
            params.only = only;

            params.lowBegin = $('#lowScore').val();
            params.topEnd = $('#highScore').val();
            params.s = $('#searchText').val();
            params.numIid = $('#numIid').val();
            return params;
        }
    }, MobileDesc.show);


})(jQuery,window));
