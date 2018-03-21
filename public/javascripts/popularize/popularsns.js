((function ($, window) {
var Popularize = Popularize || {};
var autoTitle = TM.autoTitle;
TM.Popularize = Popularize;

Popularize.Init = Popularize.Init || {};
Popularize.Init = $.extend({
    init : function(){
//        Popularize.Init.initUserInfo();
        autoTitle.Init.initSearchArea();
        Popularize.popularize.getItems();
        Popularize.Init.initUserInfo();
    },
    initUserInfo : function(){
        $.get("/Popularize/getUserInfo",function(data){
            $.cookie("awarded",data.award);
            if(data.award){
                $.cookie("totalNum",data.totalNum+1);
            } else {
                $.cookie("totalNum",data.totalNum);
            }
            $.cookie("level",data.level);
            $.cookie("popularizedNum",data.popularizedNum);

            $('.userInfo').find('span[tag="version"]').html(data.version);
            $('.userInfo').find('span[tag="totalNum"]').html($.cookie("totalNum"));
            if(data.award){
                $('.userInfo').find('span[tag="totalNum"]').parent().append($('<span class="userData" tag="awardNum">(+1)</span>'));
            }
            $('.userInfo').find('span[tag="popularizedNum"]').html($.cookie("popularizedNum"));
            $('.userInfo').find('span[tag="remainNum"]').html($.cookie("totalNum") - $.cookie("popularizedNum"));
        })
    }
},Popularize.Init);

Popularize.util = Popularize.util || {};
Popularize.util = $.extend({
    getParams : function(){
        var params = {};
        var status = $("#itemsStatus option:selected").attr("tag");
        switch(status){
            case "polularized":params.polularized=0;break;
            case "topopularize" : params.polularized=1;break;
            default : params.polularized=2;break;
        }

        var catId = $('#itemsCat option:selected').attr("catId");
        params.catId = catId;

        var sort = $('#itemsSortBy option:selected').attr("tag");
        switch(sort){
            case "sortBySaleCountUp" : params.sort=3;break;
            case "sortBySaleCountDown" : params.sort=4;break;
            default : params.sort=3;break;
        }

        params.lowBegin = $('#lowScore').val();
        params.topEnd = $('#highScore').val();
        params.s = $('#searchWord').val();
        return params;
    }
},Popularize.util);


Popularize.popularize = Popularize.popularize || {};
Popularize.popularize = $.extend({
    getItems : function(){
        $('.itemsList').empty();
        var params = $.extend({
            "s":"",
            "polularized":2,
            "catId":null,
            "sort":3,
            "lowBegin":0,
            "topEnd":100
        },Popularize.util.getParams());
        var bottom = $('<div class="popularizeBottom" style="text-align: center;"></div>');
        bottom.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                param : params,
                on: true,
                dataType: 'json',
                url: "/Popularize/getItems",
                callback:function(data){
                    $('.itemsList').empty();
                    $('.itemsList').find(".popularizeBottom").remove();
                    $('.itemsList').append(Popularize.popularize.createItemsHead());
                    $('.itemsList').append(Popularize.popularize.createItemsList(data.res));
                    bottom.appendTo($('.itemsList'));
                    Popularize.Event.setEvent();
                }
            }

        });

    },
    createItemsHead : function(){
        var head = $('<div class="diagHead"></div>');
        var ulObj = $('<ul class="diagHeadUL"></ul>');

        var liObjSelect = $('<li class=" fl" style="width:40px;" ></li>');
//        liObjSelect.append($('<input type="checkbox" id="checkAllItems" style="margin-top:15px"  >'));
//        ulObj.append(liObjSelect);
//        ulObj.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchSelectUsePopularize commbutton btntext6">选中使用自动推广</span></li>'));
//        ulObj.append($('<li class="fl" style="width:20px"></li>'));
//        ulObj.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchSelectRemovePopularize commbutton btntext6">选中取消自动推广</span></li>'));
//        ulObj.append($('<li class="fl" style="width:200px"></li>'));
//        ulObj.append($('<li class="fl" style="width:120px"><span class="batchAllAddPopularize commbutton btntext6">全店一键自动推广</span></li>'));
//        ulObj.append($('<li class="fl" style="width:20px"></li>'));
//        ulObj.append($('<li class="fl" style="width:120px"><span class="batchAllRemovePopularize commbutton btntext6">全店一键取消推广</span></li>'));
        head.append(ulObj);
        return head;
    },
    createItemsList : function(res){
        var items = $('<div class="itemsDiv" ></div>');
        for (var i = 0; i < res.length; i++) {
            items.append(Popularize.popularize.createSingleItem(res[i],i));
        }
        return items;
    },
    createSingleItem : function(result,i){
        var singleItem = $('<div class="singleItem"></div>');
        var ulObj = $('<ul class="singleDiagULObj"></ul>');
        ulObj.attr("numIid",result.numIid);
//        ulObj.append(autoTitle.ItemsDiag.createCheckboxLiObj(result));
        ulObj.append(Popularize.popularize.createItemImgLiObj(result));
        ulObj.append(Popularize.popularize.createItemTitleLiObj(result,i));
//        ulObj.append(Popularize.popularize.createOPLiObj(result));
//        ulObj.append(autoTitle.ItemsDiag.createStatusLiObj(result));
//        ulObj.append(autoTitle.ItemsDiag.createSalesCountLiObj(result));

        singleItem.append(ulObj);
        singleItem.append($('<div class="clear"></div>'));
        return singleItem;
    },
    createItemImgLiObj : function(result){
        var liObjimg = $('<li class=" fl" style="width:100px;text-align: center;" ></li>');

        var aObj = $('<a target="_blank"></a>');
        var url = "http://item.taobao.com/item.htm?id=" + result.id;
        aObj.attr("href",url);
        var imgObj = $('<img style="width:60px;height:60px;border: 1px solid #5DB0F9;">');
        imgObj.attr("src",result.picURL);
        aObj.append(imgObj);
        liObjimg.append(aObj);
//        liObjimg.append("");
        //liObjimg.append('<div style="height:32px;line-height: 32px;font-size:13px;">月销量<span class="red">'+(result.tradeItemNum>10000?(result.tradeItemNum/10000 + '万'):result.tradeItemNum)+'</span>件</div>');
        liObjimg.append('<a style="margin-top: 12px;" target="_blank" class="tmbtn sky-blue-btn" href="http://upload.taobao.com/auction/publish/edit.htm?item_num_id='+result.id+'&auto=false">修改属性</a>');
        liObjimg.append('<a style="display: none;" target="_blank" class="tmbtn yellow-btn" href="/Popularize/yijianfenxiang?numIid='+result.id+'&title='+result.title+'&picURL='+result.picURL+'" style="margin-top: 10px">一键分享</a>');

        return liObjimg;
    },
    createItemTitleLiObj : function(result, i){
        var liObj = $('<li class=" fl" style="width:600px;;" ></li>');
        var htmls = [];
        htmls.push('<div class="itemTitle "><p>'+result.title+'</p></div>');
        htmls.push('<div class="snsList jiathis_style">');




            htmls.push('<a class="jtico inlineblock jtico_qzone" href="http://www.jiathis.com/send/?webid=qzone&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到QQ空间" target="_blank">QQ空间</a>');

        htmls.push('<a class="jtico inlineblock jtico_tsina" href="http://www.jiathis.com/send/?webid=tsina&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到新浪微博" target="_blank">新浪微博</a>');
        htmls.push('<a class="jtico inlineblock jtico_tqq" href="http://www.jiathis.com/send/?webid=tqq&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到腾讯微博" target="_blank">腾讯微博</a>');
        htmls.push('<a class="jtico inlineblock jtico_renren" href="http://www.jiathis.com/send/?webid=renren&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到人人网" target="_blank">人人网</a>');
            htmls.push('<a class="jtico inlineblock jtico_kaixin001" href="http://www.jiathis.com/send/?webid=kaixin001&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到开心网" target="_blank">开心网</a>');
            htmls.push('<a class="jtico inlineblock jtico_taobao" href="http://www.jiathis.com/send/?webid=taobao&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到淘江湖" target="_blank">淘江湖</a>');
            htmls.push('<a class="jtico inlineblock jtico_meilishuo" href="http://www.jiathis.com/send/?webid=meilishuo&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到美丽说" target="_blank">美丽说</a>');
            htmls.push('<a class="jtico inlineblock jtico_mogujie" href="http://www.jiathis.com/send/?webid=mogujie&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到蘑菇街" target="_blank">蘑菇街</a>');
            htmls.push('<a class="jtico inlineblock jtico_tsohu" href="http://www.jiathis.com/send/?webid=mogujie&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到搜狐微博" target="_blank">搜狐微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_baidu" href="http://www.jiathis.com/send/?webid=baidu&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到百度搜藏" target="_blank">百度搜藏</a>');
            htmls.push('<a class="jtico inlineblock jtico_douban" href="http://www.jiathis.com/send/?webid=douban&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到豆瓣" target="_blank">豆瓣</a>');
            htmls.push('<a class="jtico inlineblock jtico_t163" href="http://www.jiathis.com/send/?webid=t163&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到网易微博" target="_blank">网易微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_qingsina" href="http://www.jiathis.com/send/?webid=qingsina&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到新浪轻博" target="_blank">新浪轻博</a>');
            htmls.push('<a class="jtico inlineblock jtico_tianya" href="http://www.jiathis.com/send/?webid=tianya&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到天涯社区" target="_blank">天涯社区</a>');
            htmls.push('<a class="jtico inlineblock jtico_feixin" href="http://www.jiathis.com/send/?webid=feixin&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到飞信" target="_blank">飞信</a>');
            htmls.push('<a class="jtico inlineblock jtico_hi" href="http://www.jiathis.com/send/?webid=hi&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到百度空间" target="_blank">百度空间</a>');
            htmls.push('<a class="jtico inlineblock jtico_51" href="http://www.jiathis.com/send/?webid=51&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到51社区" target="_blank">51社区</a>');
            htmls.push('<a class="jtico inlineblock jtico_qq" href="http://www.jiathis.com/send/?webid=qq&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到QQ收藏" target="_blank">QQ收藏</a>');
            htmls.push('<a class="jtico inlineblock jtico_mop" href="http://www.jiathis.com/send/?webid=mop&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到猫扑推客" target="_blank">猫扑推客</a>');
            htmls.push('<a class="jtico inlineblock jtico_sohu" href="http://www.jiathis.com/send/?webid=sohu&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到搜狐白社会" target="_blank">搜狐白社会</a>');
            htmls.push('<a class="jtico inlineblock jtico_huaban" href="http://www.jiathis.com/send/?webid=huaban&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到花瓣网" target="_blank">花瓣网</a>');
            htmls.push('<a class="jtico inlineblock jtico_139" href="http://www.jiathis.com/send/?webid=139&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到移动微博" target="_blank">移动微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_115收藏" href="http://www.jiathis.com/send/?webid=115收藏&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到115" target="_blank">115</a>');
            htmls.push('<a class="jtico inlineblock jtico_i139" href="http://www.jiathis.com/send/?webid=i139&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到爱分享" target="_blank">爱分享</a>');
            htmls.push('<a class="jtico inlineblock jtico_tongxue" href="http://www.jiathis.com/send/?webid=tongxue&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到同学网" target="_blank">同学网</a>');
                htmls.push('<a class="jtico inlineblock jtico_xiaoyou" href="http://www.jiathis.com/send/?webid=xiaoyou&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到朋友网" target="_blank">朋友网</a>');
                htmls.push('<a class="jtico inlineblock jtico_msn" href="http://www.jiathis.com/send/?webid=msn&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到MSN" target="_blank">MSN</a>');
                htmls.push('<a class="jtico inlineblock jtico_youdao" href="http://www.jiathis.com/send/?webid=youdao&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到有道书签" target="_blank">有道书签</a>');
                htmls.push('<a class="jtico inlineblock jtico_google" href="http://www.jiathis.com/send/?webid=google&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到谷歌" target="_blank">谷歌</a>');
                htmls.push('<a class="jtico inlineblock jtico_fanfou" href="http://www.jiathis.com/send/?webid=fanfou&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到饭否" target="_blank">饭否</a>');
                htmls.push('<a class="jtico inlineblock jtico_buzz" href="http://www.jiathis.com/send/?webid=buzz&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到谷歌Buzz" target="_blank">谷歌Buzz</a>');
                htmls.push('<a class="jtico inlineblock jtico_xianguo" href="http://www.jiathis.com/send/?webid=xianguo&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到鲜果" target="_blank">鲜果</a>');
                htmls.push('<a class="jtico inlineblock jtico_sina" href="http://www.jiathis.com/send/?webid=sina&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到新浪vivi" target="_blank">新浪vivi</a>');
                htmls.push('<a class="jtico inlineblock jtico_ifensi" href="http://www.jiathis.com/send/?webid=ifensi&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到粉丝网" target="_blank">粉丝网</a>');
                htmls.push('<a class="jtico inlineblock jtico_qu1" href="http://www.jiathis.com/send/?webid=qu1&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到趣一网" target="_blank">趣一网</a>');
                htmls.push('<a class="jtico inlineblock jtico_youshi" href="http://www.jiathis.com/send/?webid=youshi&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到优士网" target="_blank">优士网</a>');
                htmls.push('<a class="jtico inlineblock jtico_digu" href="http://www.jiathis.com/send/?webid=digu&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到嘀咕网" target="_blank">嘀咕网</a>');
                htmls.push('<a class="jtico inlineblock jtico_hexun" href="http://www.jiathis.com/send/?webid=hexun&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到和讯" target="_blank">和讯</a>');
                htmls.push('<a class="jtico inlineblock jtico_duitang" href="http://www.jiathis.com/send/?webid=duitang&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到堆糖" target="_blank">堆糖</a>');
                htmls.push('<a class="jtico inlineblock jtico_189cn" href="http://www.jiathis.com/send/?webid=189cn&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到天翼社区" target="_blank">天翼社区</a>');
                htmls.push('<a class="jtico inlineblock jtico_cnfol" href="http://www.jiathis.com/send/?webid=cnfol&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到中金微博" target="_blank">中金微博</a>');
                htmls.push('<a class="jtico inlineblock jtico_thexun" href="http://www.jiathis.com/send/?webid=thexun&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到和讯微博" target="_blank">和讯微博</a>');
                htmls.push('<a class="jtico inlineblock jtico_txinhua" href="http://www.jiathis.com/send/?webid=txinhua&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到新华微博" target="_blank">新华微博</a>');
                htmls.push('<a class="jtico inlineblock jtico_toeeee" href="http://www.jiathis.com/send/?webid=toeeee&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到南方微博" target="_blank">南方微博</a>');
                htmls.push('<a class="jtico inlineblock jtico_zuosa" href="http://www.jiathis.com/send/?webid=zuosa&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到做啥" target="_blank">做啥</a>');
                htmls.push('<a class="jtico inlineblock jtico_139mail" href="http://www.jiathis.com/send/?webid=139mail&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到139邮箱" target="_blank">139邮箱</a>');
                htmls.push('<a class="jtico inlineblock jtico_tyaolan" href="http://www.jiathis.com/send/?webid=tyaolan&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到摇篮网" target="_blank">摇篮网</a>');
                htmls.push('<a class="jtico inlineblock jtico_189mail" href="http://www.jiathis.com/send/?webid=189mail&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到189邮箱" target="_blank">189邮箱</a>');
                htmls.push('<a class="jtico inlineblock jtico_renjian" href="http://www.jiathis.com/send/?webid=renjian&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到人间网" target="_blank">人间网</a>');
                htmls.push('<a class="jtico inlineblock jtico_miliao" href="http://www.jiathis.com/send/?webid=miliao&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到米聊" target="_blank">米聊</a>');
                htmls.push('<a class="jtico inlineblock jtico_sdonote" href="http://www.jiathis.com/send/?webid=sdonote&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到麦库" target="_blank">麦库</a>');
                htmls.push('<a class="jtico inlineblock jtico_renmaiku" href="http://www.jiathis.com/send/?webid=renmaiku&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到人脉库" target="_blank">人脉库</a>');
                htmls.push('<a class="jtico inlineblock jtico_tuita" href="http://www.jiathis.com/send/?webid=tuita&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到推他" target="_blank">推他</a>');
                htmls.push('<a class="jtico inlineblock jtico_masar" href="http://www.jiathis.com/send/?webid=masar&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到玛撒网" target="_blank">玛撒网</a>');
                htmls.push('<a class="jtico inlineblock jtico_woshao" href="http://www.jiathis.com/send/?webid=woshao&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到我烧网" target="_blank">我烧网</a>');
                htmls.push('<a class="jtico inlineblock jtico_42qu" href="http://www.jiathis.com/send/?webid=42qu&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到42区" target="_blank">42区</a>');
                htmls.push('<a class="jtico inlineblock jtico_gmw" href="http://www.jiathis.com/send/?webid=gmw&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到光明网" target="_blank">光明网</a>');
                htmls.push('<a class="jtico inlineblock jtico_caimi" href="http://www.jiathis.com/send/?webid=caimi&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到财迷" target="_blank">财迷</a>');
                htmls.push('<a class="jtico inlineblock jtico_chinanews" href="http://www.jiathis.com/send/?webid=chinanews&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到中新网" target="_blank">中新网</a>');
                htmls.push('<a class="jtico inlineblock jtico_waakee" href="http://www.jiathis.com/send/?webid=waakee&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到挖客网" target="_blank">挖客网</a>');
                htmls.push('<a class="jtico inlineblock jtico_ifengkb" href="http://www.jiathis.com/send/?webid=ifengkb&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到凤凰快博" target="_blank">凤凰快博</a>');
                htmls.push('<a class="jtico inlineblock jtico_poco" href="http://www.jiathis.com/send/?webid=poco&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到Poco" target="_blank">Poco</a>');
                htmls.push('<a class="jtico inlineblock jtico_chouti" href="http://www.jiathis.com/send/?webid=chouti&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到抽屉网" target="_blank">抽屉网</a>');
                htmls.push('<a class="jtico inlineblock jtico_dream163" href="http://www.jiathis.com/send/?webid=dream163&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到梦幻人生" target="_blank">梦幻人生</a>');
                htmls.push('<a class="jtico inlineblock jtico_leihou" href="http://www.jiathis.com/send/?webid=leihou&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到雷猴" target="_blank">雷猴</a>');
                htmls.push('<a class="jtico inlineblock jtico_dig24" href="http://www.jiathis.com/send/?webid=dig24&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到递客网" target="_blank">递客网</a>');
                htmls.push('<a class="jtico inlineblock jtico_douban9dian" href="http://www.jiathis.com/send/?webid=douban9dian&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到豆瓣9点" target="_blank">豆瓣9点</a>');
                htmls.push('<a class="jtico inlineblock jtico_cyzone" href="http://www.jiathis.com/send/?webid=cyzone&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到创业邦" target="_blank">创业邦</a>');
                htmls.push('<a class="jtico inlineblock jtico_baohe" href="http://www.jiathis.com/send/?webid=baohe&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到宝盒网" target="_blank">宝盒网</a>');
                htmls.push('<a class="jtico inlineblock jtico_yijee" href="http://www.jiathis.com/send/?webid=yijee&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到易集网" target="_blank">易集网</a>');
                htmls.push('<a class="jtico inlineblock jtico_digg" href="http://www.jiathis.com/send/?webid=digg&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到Digg" target="_blank">Digg</a>');
                htmls.push('<a class="jtico inlineblock jtico_wealink" href="http://www.jiathis.com/send/?webid=wealink&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到若邻网" target="_blank">若邻网</a>');
                htmls.push('<a class="jtico inlineblock jtico_friendfeed" href="http://www.jiathis.com/send/?webid=friendfeed&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到FriendFeed" target="_blank">FriendFeed</a>');
                htmls.push('<a class="jtico inlineblock jtico_mixx" href="http://www.jiathis.com/send/?webid=mixx&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到Mixx" target="_blank">Mixx</a>');
                htmls.push('<a class="jtico inlineblock jtico_tianji" href="http://www.jiathis.com/send/?webid=tianji&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到天际网" target="_blank">天际网</a>');
                htmls.push('<a class="jtico inlineblock jtico_googleplus" href="http://www.jiathis.com/send/?webid=googleplus&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到Google+" target="_blank">Google+</a>');
                htmls.push('<a class="jtico inlineblock jtico_faxianla" href="http://www.jiathis.com/send/?webid=faxianla&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到发现啦" target="_blank">发现啦</a>');
                htmls.push('<a class="jtico inlineblock jtico_ishare" href="http://www.jiathis.com/send/?webid=ishare&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到一键分享" target="_blank">一键分享</a>');
                htmls.push('<a class="jtico inlineblock jtico_ujian" href="http://www.jiathis.com/send/?webid=ujian&url=http://item.taobao.com/item.htm?id='+result.id+'&title='+result.title+'" title="分享到猜你喜欢" target="_blank">猜你喜欢</a>');
        htmls.push('</div>')
        var icons = $(htmls.join(''));
        icons.find('.jtico').text('');
        liObj.append(icons);
        return liObj;
    },
    createOPLiObj : function(result){
        var isOn = result.popularized;

        var liObj = $('<li class=" fl" style="width:150px;text-align: center;" ></li>');
        var line = $("<div class='switchStatusLine' ></div>");
        var switchLine = TM.Switch.createSwitch.createSwitchForm("");
        switchLine.find('input[name="auto_valuation"]').tzCheckbox(
            {
                labels:['已推广','未推广'],
                doChange:function(isCurrentOn){
                    if(!TM.isVip()){
                        TM.Alert.showVIPNeeded();
                        return;
                    }

                    if(isCurrentOn == false) {
                        if(($.cookie("totalNum") - $.cookie("popularizedNum")) > 0){
                            $.post("/Popularize/addPopularized", {numIids : result.id}, function(){
                                $.cookie("popularizedNum",parseInt($.cookie("popularizedNum")) + 1);
                                $('.userData[tag="popularizedNum"]').html($.cookie("popularizedNum"));
                                $('.userData[tag="remainNum"]').html($.cookie("totalNum")-$.cookie("popularizedNum"));
                                TM.Alert.load("宝贝已添加到推广计划中~",400,200);
                            });
                            liObj.find('.gotoSeePopularited').show();
                        } else {
                            TM.Alert.load("您需要升级版本才能继续添加推广宝贝~",400,200);
                            return false;
                        }

                    }
                    else {
                        $.post("/Popularize/removePopularized", {numIids : result.id}, function(){
                            $.cookie("popularizedNum",parseInt($.cookie("popularizedNum")) - 1);
                            $('.userData[tag="popularizedNum"]').html($.cookie("popularizedNum"));
                            $('.userData[tag="remainNum"]').html($.cookie("totalNum")-$.cookie("popularizedNum"));
                            TM.Alert.load("宝贝已从推广计划中取消~",400,200);
                        });
                        liObj.find('.gotoSeePopularited').hide();
                    }
                    return true;
                },
                isOn : isOn
            });
        switchLine.appendTo(line);
        line.appendTo(liObj);
        liObj.append($('<div class="gotoSeePopularited"><span style="margin:0 0 0 50px;cursor: pointer;"><a href="/Application/ShowRandomPopularizedItems?numIid='+result.id+'" target="_blank" class="lookupPopularized">查看推广</a></span></div>'));
        if(!isOn) {
            liObj.find('.gotoSeePopularited').hide();
        }
        return liObj;
    }
},Popularize.popularize);

Popularize.Event = Popularize.Event || {};
Popularize.Event = $.extend({
    setEvent : function(){
        Popularize.Event.setCheckAllEvent();
        Popularize.Event.setSubcheckEvent();
        Popularize.Event.setGOSearchEvent();
        Popularize.Event.setSelectAddBatch();
        Popularize.Event.setAllAddBatch();
        Popularize.Event.setSelectRemoveBatch();
        Popularize.Event.setAllRemoveBatch();
        Popularize.Event.setGotoSeePopularitedEvent();
    },
    setCheckAllEvent : function(){
        $("#checkAllItems").click(function(){
            $('input[name="subCheck"]').attr("checked",this.checked);
        });
    },
    setSubcheckEvent : function(){
        $(".subCheckBox").click(function(){
            var $subBox = $("input[name='subCheck']");
            $("#checkAllItems").attr("checked",$subBox.length == $("input[name='subCheck']:checked").length ? true : false);
        });
    },
    setGOSearchEvent : function(){
        $('#goSearchItems').click(function(){
            Popularize.popularize.getItems();
        });
    },
    setSelectAddBatch : function(){
        $('.batchSelectUsePopularize').click(function(){
            var numIids = "";
            if($(".itemsList").find(".subCheckBox:checked").length == 0) {
                TM.Alert.load("请选择宝贝~",400,230);
            }
            else if($(".itemsList").find(".subCheckBox:checked").length > ($.cookie("totalNum")- $.cookie("popularizedNum"))){
                TM.Alert.load("您选择的宝贝数大于可推荐宝贝数~",400,230);
            } else {
                $.each($(".itemsList").find(".subCheckBox:checked"),function(i, input){
                    var oThis = $(input);
                    numIids += (oThis.attr('numIid')) + ",";
                });
                $.post("/Popularize/addPopularized", {numIids : numIids}, function(){
                    TM.Alert.load("宝贝已添加到推广计划中~",400,200,function(){
                        location.reload();
                    });
                });
            }
        });
    },
    setSelectRemoveBatch : function(){
        $('.batchSelectRemovePopularize').click(function(){
            var numIids = "";
            if($(".itemsList").find(".subCheckBox:checked").length == 0) {
                TM.Alert.load("请选择宝贝~",400,230);
            } else {
                $.each($(".itemsList").find(".subCheckBox:checked"),function(i, input){
                    var oThis = $(input);
                    numIids += (oThis.attr('numIid')) + ",";
                });
                $.post("/Popularize/removePopularized", {numIids : numIids}, function(){
                    TM.Alert.load("宝贝已从推广计划中取消~",400,200,function(){
                        location.reload();
                    });
                });
            }
        });
    },
    setAllAddBatch : function(){
        $('.batchAllAddPopularize').click(function(){
            $.get("/Popularize/addPopularizedAll", function(data){
                if(data.res){
                    TM.Alert.load("宝贝已添加到推广计划中~",400,200,function(){
                        location.reload();
                    });
                }else{
                    TM.Alert.load("您的全店宝贝数大于可推荐宝贝数，请先升级哦~",400,230);
                }
            });
        });
    },
    setAllRemoveBatch : function(){
        $('.batchAllRemovePopularize').click(function(){
            $.get("/Popularize/removePopularizedAll", function(data){
                TM.Alert.load("宝贝已从推广计划中取消~",400,200,function(){
                    location.reload();
                });
            });
        });
    },
    setGotoSeePopularitedEvent : function(){
        $('.gotoSeePopularited').click(function(){
            // todo
        });
    }
},Popularize.Event);
})(jQuery, window));