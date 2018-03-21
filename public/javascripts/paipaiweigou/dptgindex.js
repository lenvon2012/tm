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
                /*'<div >' +
                '   <div class="new-tip-bar" >强 烈 推 荐</div> ' +
                '<table ><tr><td class="recommend">'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<img class="borderimg" src="http://img01.taobaocdn.com/top/i1/T1JFu2XzdXXXaCwpjX.png" style="margin-bottom:15px;"/></a>'+
                '<a target="_blank"  href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<div class="btn-ordernow"></div></a>'+
                '</td><td class="recommend">'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<img class="borderimg" src="http://img01.taobaocdn.com/top/i1/T1JFu2XzdXXXaCwpjX.png" style="margin-bottom:15px;" /></a>'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-1820059&tracelog=weitaoheader1" >'+
                '<div class="btn-ordernow"></div></a>'+
                '</td><td class="recommend">'+*/
/*                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-13126&tracelog=taoliuliang" >'+
                '<img class="borderimg" src="http://img01.taobaocdn.com/top/i1/T1oSONXEXdXXb1upjX.jpg" style="margin-bottom:15px;" /></a>'+
                '<a target="_blank" href="http://fuwu.taobao.com/ser/detail.htm?service_code=ts-13126&tracelog=taoliuliang" >'+
                '<div class="btn-ordernow"></div></a>'+
*/
                '</td></tr></table>'+
                '   <div class="new-tip-bar">店 铺 信 息</div> ' +
                /*'   <table class="tip-bar-table">' +
                '       <tbody>' +
                '           <tr><td class="bar-left"></td><td class="bar-middle">店 铺 信 息</td><td class="bar-right"></td></tr>' +
                '       </tbody>' +
                '   </table> ' +*/
                '   <table style="margin: 40px 0px; width: 100%;">' +
                '       <tbody>' +
                '           <td style="text-align: center; width: 60%; ">' +
                '               <table class="shop-info-table base-info-table" style="margin: 0 auto; width: 70%;">' +
                '                   <tbody>' +
                '                       <tr>' +
                '                           <td class="tip-dot" style="width: 50%;">掌柜：</td>' +
                '                           <td style="width: 50%;" class="result-info user-nick-td"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">订购版本：</td>' +
                '                           <td class="result-info buy-version-td"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">总推广位个数：</td>' +
                '                           <td class="result-info total-popularized-num"></td>' +
                '                       </tr>' +
                /*'                       <tr>' +
                '                           <td class="tip-dot">参加好评送推广：</td>' +
                '                           <td class="result-info has-award"></td>' +
                '                       </tr>' +*/
                '                       <tr>' +
                '                           <td class="tip-dot">已用推广位个数：</td>' +
                '                           <td class="result-info used-popularized-num"></td>' +
                '                       </tr>' +
                '                       <tr>' +
                '                           <td class="tip-dot">剩余推广位个数：</td>' +
                '                           <td class="result-info remain-popularized-num"></td>' +
                '                       </tr>' +
                /*'                       <tr>' +
                '                           <td colspan="2"><div><a class="big-op-btn" href="/PaiPaiWeigou/promoteItem">我 要 推 广</a></div></td>' +
                '                       </tr>' +*/
                '                   </tbody>' +
                '               </table> ' +
                '           </td>' +
                '           <td style="width: 40%; text-align: left;">' +
                //'               <div><a target="_blank" title="我要升级" class="big-op-btn" href="http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.12.gbGCFZ&service_code=FW_GOODS-1848326&tracelog=search&scm=&ppath=">我 要 升 级</a></div>'+
                '               <div><a class="big-op-btn" href="/PaiPaiWeigou/promoteItem">我 要 推 广</a></div>' +
                '               <div style="margin-top:30px;"><a class="big-op-btn" style="background: #CC0000;" target="_blank" href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=262513&chargeItemId=2941">我 要 升 级</a></div>' +
                '               <div  style="margin-top: 30px;display:none;"><a class="big-op-btn" target="_blank" href="http://www.youmiguang.com/">查 看 推 广</a></div>' +
                '           </td>' +
                '       </tbody>' +
                '   </table> ' +
                '</div>' +
                '<div style="text-align: center;margin-bottom: 10px;font-size: 20px;" class="ad-middle"><span style="font-weight: bold;color:red;">限时推广活动：</span>10个优质位+3个热销位，80元热销！  <a class="small-op-btn" href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=262513&chargeItemId=2941" target="_blank" style="font-size:20px;">立即订购</a></div>' +
                '<div style="text-align:center;"><div class="headspan" style="width:94%;padding-top: 4px;padding-bottom: 4px;">精品服务推荐 -- <span class="red">中国好服务!</span></div>' +
                '<div class="blank0" style="height:15px"></div>'+
                '<div><a style="margin-right: 100px;" href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=262513" target="_blank"><img src="http://img.paipaiimg.com/item-027C74C2-00000000000000000000000000262513.1.jpg" /></a>'+
                    '<a style="margin-right: 100px;" href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=262400&chargeItemId=2851" target="_blank"><img src="http://img.paipaiimg.com/item-02453D0C-00000000000000000000000000262400.1.jpg" /></a>'+
                    '<a style="margin-right: 100px;" href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=285239" target="_blank"><img src="http://img.paipaiimg.com/item-02770BC1-00000000000000000000000000285239.1.jpg" /></a>'+
                    '<a href="http://fuwu.paipai.com/appstore/ui/my/app/appdetail.xhtml?appId=286440" target="_blank"><img src="http://img.paipaiimg.com/item-027C585A-00000000000000000000000000286440.1.jpg" /></a>'+
                '</div>'+
                '</div>';


            return html;
        }
    }, DptgIndex.init);


    DptgIndex.show = DptgIndex.show || {};
    DptgIndex.show = $.extend({
        doShow: function() {
            var data = {};
            $.ajax({
                url : '/PaiPaiPromote/getUserInfo',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;
                    var container = DptgIndex.container;

                    container.find(".user-nick-td").html(userJson.username);
                    container.find(".buy-version-td").html(userJson.version);

                    //userJson.award = true;
                    if (userJson.award == true) {
                        container.find(".total-popularized-num").html((userJson.totalNum + userJson.hotTotalNum) + " + 1 (好评送推广)");
                        container.find(".has-award").html("已参加");
                        userJson.remainNum++;
                    } else {
                        container.find(".total-popularized-num").html((userJson.totalNum + userJson.hotTotalNum));
                        var html = '' +
                            '<a href="/jdtuiguang/award" target="_blank" class="attend-award-link">立即参加>></a>' +
                            '';
                        container.find(".has-award").html(html);
                    }

                    container.find(".used-popularized-num").html(userJson.popularizedNum+userJson.hotTotalNum);
                    container.find(".remain-popularized-num").html(userJson.remainNum+userJson.hotRemainNum);
                }
            });
        }
    },  DptgIndex.show);

})(jQuery,window));