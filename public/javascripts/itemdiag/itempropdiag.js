((function ($, window) {

    TM.PropDiag  = TM.PropDiag || {}

    var me = TM.PropDiag;

    TM.PropDiag.init = function(container){
        me.container = container;
        me.appendDiag();
        me.showDialog();
        me.showHaoPingOrNor();
    }

    me.showHaoPingOrNor = function(){
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
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=service-0-22735">' +
                                    '<div style="cursor: pointer;width: 550px;height: 270px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td style="width: 200px;">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=service-0-22735">' +
                                    '<div style="cursor: pointer;width: 200px;height: 270px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '</tr>' +
                                    '<tr style="height: 80px;width: 750px;">' +
                                    '<td style="width: 550px">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=service-0-22735">' +
                                    '<div style="cursor: pointer;width: 200px;height: 80px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td class="" style="width: 200px;cursor: pointer;">' +
                                    '<a style="margin-left: 110px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E4%B8%8A%E5%AE%98_%E5%B0%8F%E9%9B%AA&siteid=cntaobao&status=1&charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
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

    TM.PropDiag.showDialog = function(){
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
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=service-0-22735">' +
                                    '<div style="cursor: pointer;width: 550px;height: 270px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td style="width: 200px;">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=service-0-22735">' +
                                    '<div style="cursor: pointer;width: 200px;height: 270px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '</tr>' +
                                    '<tr style="height: 80px;width: 750px;">' +
                                    '<td style="width: 550px">' +
                                    '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=service-0-22735">' +
                                    '<div style="cursor: pointer;width: 200px;height: 80px;"></div>'+
                                    '</a>'+
                                    '</td>'+
                                    '<td class="" style="width: 200px;cursor: pointer;">' +
                                    '<a style="margin-left: 110px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E4%B8%8A%E5%AE%98_%E5%B0%8F%E9%9B%AA&siteid=cntaobao&status=1&charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
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

    TM.PropDiag.appendDiag = function(){
        me.container.empty();
//        var intro = $('<div class="autodiagIntro"><p>完整规范的宝贝属性，能够增加宝贝的<b class="red">类目搜索展现量</b>，这对提高宝贝流量非常关键！</p></div>')
//        me.container.append(intro);

        $(".tochekcbtn").click(function(){
            me.appendPropList();
        });

//        var diagBtn = $('<div class="diagbtnpos"><span class="diagbtn btn btn-success">立即检查</span></div>');
//        diagBtn.click(function(){
//            me.appendPropList();
//        });
//        me.container.append(diagBtn);

        var resTable = $('<table class="oplogs"><thead><th style="width:100px;">宝贝</th><th>标题</th><th>错误信息</th><th>操作</th></thead><tbody class="resBody"></tbody></table>')
        var resBody = resTable.find('tbody');
        me.resBody = resBody;
        me.resBody.append("<tr><td colspan='4'><div class='oknoproblem '><p style='padding-top:50px;'>点击<b class='red tochekcbtn' style='cursor: pointer;'>立即检查</b>，诊断您的宝贝吧！</p></div></td></tr>");
        me.resBody.find(".tochekcbtn").click(function(){
            me.appendPropList();
        });
        me.container.append(resTable);

    }
    TM.PropDiag.appendPropList = function(){
//        me.container.empty();
        $.get('/Props/dodiag',function(data){
            if(!data || data.length == 0){
                me.resBody.empty();
                me.resBody.append("<tr><td colspan='4'><div class='oknoproblem '><p style='padding-top:50px;'>恭喜您，您的宝贝属性<b class='red'>没有</b>发现问题哟</p></div></td></tr>");
                return;
            }

            var htmls = [];
//            htmls.push('<table class="">');
//            htmls.push('<thead><th>宝贝</th><th>标题</th><th>操作</th></thead>')
            //http://upload.taobao.com/auction/publish/edit.htm?spm=686.1000925.1000774.8.AthpLL&item_num_id=21587756782&auto=false
            $.each(data,function(i, elem){
                htmls.push('<tr>');
                htmls.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+elem.numIid+'"><img class="itemsnapwithborder" src="'+elem.picPath+'"></a></td>');
                htmls.push('<td>'+elem.title+'</td>');
                htmls.push('<td class="errormsg">'+elem.msg+'</td>');
                htmls.push('<td>');
                htmls.push('<a target="_blank" class="tmbtn sky-blue-btn" href="http://upload.taobao.com/auction/publish/edit.htm?item_num_id='+elem.numIid+'&auto=false">修改属性</a>');
                htmls.push('<a target="_blank" class="tmbtn yellow-btn" style="margin-top: 5px;" href="http://item.taobao.com/item.html?id='+elem.numIid+'&auto=false">查看宝贝</a>');
                htmls.push('</td>');
                htmls.push('</tr>');
            });
//            htmls.push('</table>');
            var elems = $(htmls.join(''));
            me.resBody.empty();
            elems.appendTo(me.resBody);
        });
    }

})(jQuery, window))
