var TM = TM || {};

((function ($, window) {
    TM.WeigouDetail = TM.WeigouDetail || {};

    var WeigouDetail = TM.WeigouDetail;

    WeigouDetail.init = WeigouDetail.init || {};
    WeigouDetail.init = $.extend({
        doInit: function(container) {
            WeigouDetail.container = container;

            var html = WeigouDetail.init.createHtml();
            container.html(html);

            WeigouDetail.show.doShow();


        },
        createHtml: function() {
            var html = '' +
                '<div class="item-info-div" style="display: none; text-align: center;padding: 10px 0px;margin-bottom: 100px;"></div> ' +
                '<div class="show-error-div orange" style="margin: 20px auto; font-size: 24px; display: none;"></div> ' +
                '<div class="loading-div" style="text-align: center; padding: 10px 0px;display: none;">' +
                '   <img src="/img/taoweigou/loading3.gif" />' +
                '</div> ' +
                '';

            return html;
        }
    }, WeigouDetail.init);


    WeigouDetail.show = WeigouDetail.show || {};
    WeigouDetail.show = $.extend({
        doShow: function() {
            WeigouDetail.loading.show();
            var numIid = TM.WeigouBase.util.getNumIidParam();
            var data = {};
            if (numIid === undefined || numIid == null) {

            } else {
                data.numIid = numIid;

                //5秒后跳转
               /* setTimeout(function() {
                    location.href = 'http://item.taobao.com/item.htm?id=' + numIid;
                }, 5000);*/
            }
            $.ajax({
                url : "/shopping/queryPopularized",
                data : data,
                type : 'post',
                success : function(dataJson) {
                    //if (TM.WeigouBase.util.judgeAjaxResult(dataJson) == false)
                    //    return;
                    var isOk = dataJson.isOk;
                    if (isOk == true) {
                        var itemJson = dataJson.res;
                        var itemObj = WeigouDetail.item.createItemObj(itemJson);
                        var divObj = WeigouDetail.container.find(".item-info-div");
                        divObj.html(itemObj);
                        divObj.show();
                    } else {
                        var message = dataJson.msg;
                        if (message === undefined || message == null || message == "") {
                            message = "亲，找不到相应的宝贝~~"
                        }
                        var errorDivObj = WeigouDetail.container.find(".show-error-div");
                        errorDivObj.html(message);
                        errorDivObj.show();
                    }

                    WeigouDetail.loading.hide();
                }
            });
        }
    }, WeigouDetail.show);

    WeigouDetail.item = WeigouDetail.item || {};
    WeigouDetail.item = $.extend({
        createItemObj: function(itemJson) {
            var html = WeigouDetail.item.createHtml();
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }

            var itemObj = $(html);
            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;

            itemObj.find(".item-href").attr("href", href);
            itemObj.find(".item-href").attr("target", "_blank");
            itemObj.find(".item-img").attr("src", itemJson.picPath + "_480x480.jpg");

            var skuMinPrice = itemJson.skuMinPrice;
            var originPrice = itemJson.price;
            if (skuMinPrice === undefined || skuMinPrice == null || skuMinPrice <= 0 || skuMinPrice >= originPrice) {
                itemObj.find(".item-price-div").html('价格：<span class="item-price" style="font-weight: bold; font-size: 14px;"></span>&nbsp;元 ');
                itemObj.find(".item-price").html(itemJson.price);
            } else {
                var priceHtml = '' +
                    '<table>' +
                    '   <tbody>' +
                    '   <tr>' +
                    '       <td>' +
                    '           原价：' +
                    '       </td>' +
                    '       <td style="text-decoration:line-through;">' +
                    '           <span class="item-origin-price"></span>&nbsp;元' +
                    '       </td>' +
                    '   </tr>' +
                    '   <tr>' +
                    '       <td style="">' +
                    '           优惠价：' +
                    '       </td>' +
                    '       <td>' +
                    '           <span class="item-new-price item-price" style="font-weight: bold; font-size: 14px;"></span>&nbsp;元' +
                    '       </td>' +
                    '   </tr>' +
                    '   </tbody>' +
                    '</table>' +
                    '';
                itemObj.find(".item-price-div").html(priceHtml);
                itemObj.find(".item-origin-price").html(itemJson.price);
                itemObj.find(".item-new-price").html(itemJson.skuMinPrice);
            }

            itemObj.find(".item-title").html(itemJson.title);

            var shareObj = WeigouDetail.snsShare.doCreate(href, itemJson.title);
            itemObj.find(".sns-share-div").html(shareObj);

            return itemObj;
        },
        createHtml: function() {
            var html = '' +
                '<div style="margin: 0 auto; text-align: left;" class="inlineblock">' +
                '<table style="border-collapse: collapse;text-align: left;">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td colspan="2" style="padding-bottom: 10px;"><a class="item-title item-link item-href big-item-title" target="_blank"></a> </td>' +
                '   </tr>' +
                '   <tr>' +
                '       <td style="width: 65px;">分享到：</td>' +
                '       <td style="text-align: left;"><div class="sns-share-div"></div> </td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table> ' +
                '<table style="border-collapse: collapse;text-align: left;margin-top: 20px;">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td><a class="item-href" target="_blank" style="display: block;"><img class="item-img" style="" /></a></td> ' +
                '       <td style="padding-left: 20px;vertical-align: top;">' +
                '           <div class="item-price-div" style="margin-top: 60px;"></div> ' +
                '           <div style="margin-top: 30px;">' +
                '               <a class="item-href go-to-look" target="_blank">马上去看看>></a>' +
                '           </div> ' +
                '       </td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table> ' +
                '</div> ' +
                '';

            return html;
        }
    }, WeigouDetail.item);


    WeigouDetail.snsShare = WeigouDetail.snsShare || {};
    WeigouDetail.snsShare = $.extend({
        doCreate: function(itemHref, title) {
            var htmls = [];

            var targetHref = itemHref;

            htmls.push('<div class="snsList jiathis_style">');

            htmls.push('<a class="jtico inlineblock jtico inlineblock_qzone" href="http://www.jiathis.com/send/?webid=qzone&url=' + targetHref +'&title='+title+'" title="分享到QQ空间" target="_blank">QQ空间</a>');

            htmls.push('<a class="jtico inlineblock jtico_tsina" href="http://www.jiathis.com/send/?webid=tsina&url=' + targetHref +'&title='+title+'" title="分享到新浪微博" target="_blank">新浪微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_tqq" href="http://www.jiathis.com/send/?webid=tqq&url=' + targetHref +'&title='+title+'" title="分享到腾讯微博" target="_blank">腾讯微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_renren" href="http://www.jiathis.com/send/?webid=renren&url=' + targetHref +'&title='+title+'" title="分享到人人网" target="_blank">人人网</a>');
            //htmls.push('<a class="jtico inlineblock jtico_kaixin001" href="http://www.jiathis.com/send/?webid=kaixin001&url=' + targetHref +'&title='+title+'" title="分享到开心网" target="_blank">开心网</a>');
            htmls.push('<a class="jtico inlineblock jtico_taobao" href="http://www.jiathis.com/send/?webid=tsina&url=' + targetHref +'&title='+title+'" title="分享到淘江湖" target="_blank">淘江湖</a>');
            //htmls.push('<a class="jtico inlineblock jtico_meilishuo" href="http://www.jiathis.com/send/?webid=meilishuo&url=' + targetHref +'&title='+title+'" title="分享到美丽说" target="_blank">美丽说</a>');
            htmls.push('<a class="jtico inlineblock jtico_mogujie" href="http://www.jiathis.com/send/?webid=mogujie&url=' + targetHref +'&title='+title+'" title="分享到蘑菇街" target="_blank">蘑菇街</a>');
            htmls.push('<a class="jtico inlineblock jtico_tsohu" href="http://www.jiathis.com/send/?webid=mogujie&url=' + targetHref +'&title='+title+'" title="分享到搜狐微博" target="_blank">搜狐微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_baidu" href="http://www.jiathis.com/send/?webid=baidu&url=' + targetHref +'&title='+title+'" title="分享到百度搜藏" target="_blank">百度搜藏</a>');
            htmls.push('<a class="jtico inlineblock jtico_douban" href="http://www.jiathis.com/send/?webid=douban&url=' + targetHref +'&title='+title+'" title="分享到豆瓣" target="_blank">豆瓣</a>');
            htmls.push('<a class="jtico inlineblock jtico_t163" href="http://www.jiathis.com/send/?webid=t163&url=' + targetHref +'&title='+title+'" title="分享到网易微博" target="_blank">网易微博</a>');
            //htmls.push('<a class="jtico inlineblock jtico_qingsina" href="http://www.jiathis.com/send/?webid=qingsina&url=' + targetHref +'&title='+title+'" title="分享到新浪轻博" target="_blank">新浪轻博</a>');
            htmls.push('<a class="jtico inlineblock jtico_tianya" href="http://www.jiathis.com/send/?webid=tianya&url=' + targetHref +'&title='+title+'" title="分享到天涯社区" target="_blank">天涯社区</a>');
            htmls.push('<a class="jtico inlineblock jtico_feixin" href="http://www.jiathis.com/send/?webid=feixin&url=' + targetHref +'&title='+title+'" title="分享到飞信" target="_blank">飞信</a>');
            htmls.push('<a class="jtico inlineblock jtico_hi" href="http://www.jiathis.com/send/?webid=hi&url=' + targetHref +'&title='+title+'" title="分享到百度空间" target="_blank">百度空间</a>');
            htmls.push('<a class="jtico inlineblock jtico_51" href="http://www.jiathis.com/send/?webid=51&url=' + targetHref +'&title='+title+'" title="分享到51社区" target="_blank">51社区</a>');
            htmls.push('<a class="jtico inlineblock jtico_qq" href="http://www.jiathis.com/send/?webid=qq&url=' + targetHref +'&title='+title+'" title="分享到QQ收藏" target="_blank">QQ收藏</a>');
            htmls.push('<a class="jtico inlineblock jtico_mop" href="http://www.jiathis.com/send/?webid=mop&url=' + targetHref +'&title='+title+'" title="分享到猫扑推客" target="_blank">猫扑推客</a>');
            /*htmls.push('<a class="jtico inlineblock jtico_sohu" href="http://www.jiathis.com/send/?webid=sohu&url=' + targetHref +'&title='+title+'" title="分享到搜狐白社会" target="_blank">搜狐白社会</a>');
            htmls.push('<a class="jtico inlineblock jtico_huaban" href="http://www.jiathis.com/send/?webid=huaban&url=' + targetHref +'&title='+title+'" title="分享到花瓣网" target="_blank">花瓣网</a>');
            htmls.push('<a class="jtico inlineblock jtico_139" href="http://www.jiathis.com/send/?webid=139&url=' + targetHref +'&title='+title+'" title="分享到移动微博" target="_blank">移动微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_115收藏" href="http://www.jiathis.com/send/?webid=115收藏&url=' + targetHref +'&title='+title+'" title="分享到115" target="_blank">115</a>');
            htmls.push('<a class="jtico inlineblock jtico_i139" href="http://www.jiathis.com/send/?webid=i139&url=' + targetHref +'&title='+title+'" title="分享到爱分享" target="_blank">爱分享</a>');
            //htmls.push('<a class="jtico inlineblock jtico_tongxue" href="http://www.jiathis.com/send/?webid=tongxue&url=' + targetHref +'&title='+title+'" title="分享到同学网" target="_blank">同学网</a>');
            htmls.push('<a class="jtico inlineblock jtico_xiaoyou" href="http://www.jiathis.com/send/?webid=xiaoyou&url=' + targetHref +'&title='+title+'" title="分享到朋友网" target="_blank">朋友网</a>');
            htmls.push('<a class="jtico inlineblock jtico_msn" href="http://www.jiathis.com/send/?webid=msn&url=' + targetHref +'&title='+title+'" title="分享到MSN" target="_blank">MSN</a>');
            htmls.push('<a class="jtico inlineblock jtico_youdao" href="http://www.jiathis.com/send/?webid=youdao&url=' + targetHref +'&title='+title+'" title="分享到有道书签" target="_blank">有道书签</a>');
            htmls.push('<a class="jtico inlineblock jtico_google" href="http://www.jiathis.com/send/?webid=google&url=' + targetHref +'&title='+title+'" title="分享到谷歌" target="_blank">谷歌</a>');
            htmls.push('<a class="jtico inlineblock jtico_fanfou" href="http://www.jiathis.com/send/?webid=fanfou&url=' + targetHref +'&title='+title+'" title="分享到饭否" target="_blank">饭否</a>');
            htmls.push('<a class="jtico inlineblock jtico_buzz" href="http://www.jiathis.com/send/?webid=buzz&url=' + targetHref +'&title='+title+'" title="分享到谷歌Buzz" target="_blank">谷歌Buzz</a>');
            htmls.push('<a class="jtico inlineblock jtico_xianguo" href="http://www.jiathis.com/send/?webid=xianguo&url=' + targetHref +'&title='+title+'" title="分享到鲜果" target="_blank">鲜果</a>');
            //htmls.push('<a class="jtico inlineblock jtico_sina" href="http://www.jiathis.com/send/?webid=sina&url=' + targetHref +'&title='+title+'" title="分享到新浪vivi" target="_blank">新浪vivi</a>');
            htmls.push('<a class="jtico inlineblock jtico_ifensi" href="http://www.jiathis.com/send/?webid=ifensi&url=' + targetHref +'&title='+title+'" title="分享到粉丝网" target="_blank">粉丝网</a>');
            htmls.push('<a class="jtico inlineblock jtico_qu1" href="http://www.jiathis.com/send/?webid=qu1&url=' + targetHref +'&title='+title+'" title="分享到趣一网" target="_blank">趣一网</a>');
            htmls.push('<a class="jtico inlineblock jtico_youshi" href="http://www.jiathis.com/send/?webid=youshi&url=' + targetHref +'&title='+title+'" title="分享到优士网" target="_blank">优士网</a>');
            htmls.push('<a class="jtico inlineblock jtico_digu" href="http://www.jiathis.com/send/?webid=digu&url=' + targetHref +'&title='+title+'" title="分享到嘀咕网" target="_blank">嘀咕网</a>');
            htmls.push('<a class="jtico inlineblock jtico_hexun" href="http://www.jiathis.com/send/?webid=hexun&url=' + targetHref +'&title='+title+'" title="分享到和讯" target="_blank">和讯</a>');
            htmls.push('<a class="jtico inlineblock jtico_duitang" href="http://www.jiathis.com/send/?webid=duitang&url=' + targetHref +'&title='+title+'" title="分享到堆糖" target="_blank">堆糖</a>');
            htmls.push('<a class="jtico inlineblock jtico_189cn" href="http://www.jiathis.com/send/?webid=189cn&url=' + targetHref +'&title='+title+'" title="分享到天翼社区" target="_blank">天翼社区</a>');
            htmls.push('<a class="jtico inlineblock jtico_cnfol" href="http://www.jiathis.com/send/?webid=cnfol&url=' + targetHref +'&title='+title+'" title="分享到中金微博" target="_blank">中金微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_thexun" href="http://www.jiathis.com/send/?webid=thexun&url=' + targetHref +'&title='+title+'" title="分享到和讯微博" target="_blank">和讯微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_txinhua" href="http://www.jiathis.com/send/?webid=txinhua&url=' + targetHref +'&title='+title+'" title="分享到新华微博" target="_blank">新华微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_toeeee" href="http://www.jiathis.com/send/?webid=toeeee&url=' + targetHref +'&title='+title+'" title="分享到南方微博" target="_blank">南方微博</a>');
            htmls.push('<a class="jtico inlineblock jtico_zuosa" href="http://www.jiathis.com/send/?webid=zuosa&url=' + targetHref +'&title='+title+'" title="分享到做啥" target="_blank">做啥</a>');
            htmls.push('<a class="jtico inlineblock jtico_139mail" href="http://www.jiathis.com/send/?webid=139mail&url=' + targetHref +'&title='+title+'" title="分享到139邮箱" target="_blank">139邮箱</a>');
            htmls.push('<a class="jtico inlineblock jtico_tyaolan" href="http://www.jiathis.com/send/?webid=tyaolan&url=' + targetHref +'&title='+title+'" title="分享到摇篮网" target="_blank">摇篮网</a>');
            htmls.push('<a class="jtico inlineblock jtico_189mail" href="http://www.jiathis.com/send/?webid=189mail&url=' + targetHref +'&title='+title+'" title="分享到189邮箱" target="_blank">189邮箱</a>');
            htmls.push('<a class="jtico inlineblock jtico_renjian" href="http://www.jiathis.com/send/?webid=renjian&url=' + targetHref +'&title='+title+'" title="分享到人间网" target="_blank">人间网</a>');
            htmls.push('<a class="jtico inlineblock jtico_miliao" href="http://www.jiathis.com/send/?webid=miliao&url=' + targetHref +'&title='+title+'" title="分享到米聊" target="_blank">米聊</a>');
            htmls.push('<a class="jtico inlineblock jtico_sdonote" href="http://www.jiathis.com/send/?webid=sdonote&url=' + targetHref +'&title='+title+'" title="分享到麦库" target="_blank">麦库</a>');
            htmls.push('<a class="jtico inlineblock jtico_renmaiku" href="http://www.jiathis.com/send/?webid=renmaiku&url=' + targetHref +'&title='+title+'" title="分享到人脉库" target="_blank">人脉库</a>');
            htmls.push('<a class="jtico inlineblock jtico_tuita" href="http://www.jiathis.com/send/?webid=tuita&url=' + targetHref +'&title='+title+'" title="分享到推他" target="_blank">推他</a>');
            htmls.push('<a class="jtico inlineblock jtico_masar" href="http://www.jiathis.com/send/?webid=masar&url=' + targetHref +'&title='+title+'" title="分享到玛撒网" target="_blank">玛撒网</a>');
            htmls.push('<a class="jtico inlineblock jtico_woshao" href="http://www.jiathis.com/send/?webid=woshao&url=' + targetHref +'&title='+title+'" title="分享到我烧网" target="_blank">我烧网</a>');
            htmls.push('<a class="jtico inlineblock jtico_42qu" href="http://www.jiathis.com/send/?webid=42qu&url=' + targetHref +'&title='+title+'" title="分享到42区" target="_blank">42区</a>');
            htmls.push('<a class="jtico inlineblock jtico_gmw" href="http://www.jiathis.com/send/?webid=gmw&url=' + targetHref +'&title='+title+'" title="分享到光明网" target="_blank">光明网</a>');
            htmls.push('<a class="jtico inlineblock jtico_caimi" href="http://www.jiathis.com/send/?webid=caimi&url=' + targetHref +'&title='+title+'" title="分享到财迷" target="_blank">财迷</a>');
            htmls.push('<a class="jtico inlineblock jtico_chinanews" href="http://www.jiathis.com/send/?webid=chinanews&url=' + targetHref +'&title='+title+'" title="分享到中新网" target="_blank">中新网</a>');
            htmls.push('<a class="jtico inlineblock jtico_waakee" href="http://www.jiathis.com/send/?webid=waakee&url=' + targetHref +'&title='+title+'" title="分享到挖客网" target="_blank">挖客网</a>');
            htmls.push('<a class="jtico inlineblock jtico_ifengkb" href="http://www.jiathis.com/send/?webid=ifengkb&url=' + targetHref +'&title='+title+'" title="分享到凤凰快博" target="_blank">凤凰快博</a>');
            htmls.push('<a class="jtico inlineblock jtico_poco" href="http://www.jiathis.com/send/?webid=poco&url=' + targetHref +'&title='+title+'" title="分享到Poco" target="_blank">Poco</a>');
            htmls.push('<a class="jtico inlineblock jtico_chouti" href="http://www.jiathis.com/send/?webid=chouti&url=' + targetHref +'&title='+title+'" title="分享到抽屉网" target="_blank">抽屉网</a>');
            htmls.push('<a class="jtico inlineblock jtico_dream163" href="http://www.jiathis.com/send/?webid=dream163&url=' + targetHref +'&title='+title+'" title="分享到梦幻人生" target="_blank">梦幻人生</a>');
            htmls.push('<a class="jtico inlineblock jtico_leihou" href="http://www.jiathis.com/send/?webid=leihou&url=' + targetHref +'&title='+title+'" title="分享到雷猴" target="_blank">雷猴</a>');
            htmls.push('<a class="jtico inlineblock jtico_dig24" href="http://www.jiathis.com/send/?webid=dig24&url=' + targetHref +'&title='+title+'" title="分享到递客网" target="_blank">递客网</a>');
            htmls.push('<a class="jtico inlineblock jtico_douban9dian" href="http://www.jiathis.com/send/?webid=douban9dian&url=' + targetHref +'&title='+title+'" title="分享到豆瓣9点" target="_blank">豆瓣9点</a>');
            htmls.push('<a class="jtico inlineblock jtico_cyzone" href="http://www.jiathis.com/send/?webid=cyzone&url=' + targetHref +'&title='+title+'" title="分享到创业邦" target="_blank">创业邦</a>');
            htmls.push('<a class="jtico inlineblock jtico_baohe" href="http://www.jiathis.com/send/?webid=baohe&url=' + targetHref +'&title='+title+'" title="分享到宝盒网" target="_blank">宝盒网</a>');
            htmls.push('<a class="jtico inlineblock jtico_yijee" href="http://www.jiathis.com/send/?webid=yijee&url=' + targetHref +'&title='+title+'" title="分享到易集网" target="_blank">易集网</a>');
            htmls.push('<a class="jtico inlineblock jtico_digg" href="http://www.jiathis.com/send/?webid=digg&url=' + targetHref +'&title='+title+'" title="分享到Digg" target="_blank">Digg</a>');
            htmls.push('<a class="jtico inlineblock jtico_wealink" href="http://www.jiathis.com/send/?webid=wealink&url=' + targetHref +'&title='+title+'" title="分享到若邻网" target="_blank">若邻网</a>');
            htmls.push('<a class="jtico inlineblock jtico_friendfeed" href="http://www.jiathis.com/send/?webid=friendfeed&url=' + targetHref +'&title='+title+'" title="分享到FriendFeed" target="_blank">FriendFeed</a>');
            htmls.push('<a class="jtico inlineblock jtico_mixx" href="http://www.jiathis.com/send/?webid=mixx&url=' + targetHref +'&title='+title+'" title="分享到Mixx" target="_blank">Mixx</a>');
            htmls.push('<a class="jtico inlineblock jtico_tianji" href="http://www.jiathis.com/send/?webid=tianji&url=' + targetHref +'&title='+title+'" title="分享到天际网" target="_blank">天际网</a>');
            htmls.push('<a class="jtico inlineblock jtico_googleplus" href="http://www.jiathis.com/send/?webid=googleplus&url=' + targetHref +'&title='+title+'" title="分享到Google+" target="_blank">Google+</a>');
            htmls.push('<a class="jtico inlineblock jtico_faxianla" href="http://www.jiathis.com/send/?webid=faxianla&url=' + targetHref +'&title='+title+'" title="分享到发现啦" target="_blank">发现啦</a>');
            htmls.push('<a class="jtico inlineblock jtico_ishare" href="http://www.jiathis.com/send/?webid=ishare&url=' + targetHref +'&title='+title+'" title="分享到一键分享" target="_blank">一键分享</a>');
            htmls.push('<a class="jtico inlineblock jtico_ujian" href="http://www.jiathis.com/send/?webid=ujian&url=' + targetHref +'&title='+title+'" title="分享到猜你喜欢" target="_blank">猜你喜欢</a>');
*/
            htmls.push('</div>')
            var icons = $(htmls.join(''));
            icons.find('.jtico').text('');
            return icons;
        }
    }, WeigouDetail.snsShare);


    WeigouDetail.loading = WeigouDetail.loading || {};
    WeigouDetail.loading = $.extend({
        show: function() {
            var divObj = WeigouDetail.container.find(".loading-div");
            divObj.show();
        },
        hide: function() {
            var divObj = WeigouDetail.container.find(".loading-div");
            divObj.hide();
        }
    }, WeigouDetail.loading);

})(jQuery,window));