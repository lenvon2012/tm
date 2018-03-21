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
                '           <tr><td style="text-align: center; width: 60%; ">' +
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
                '                           <td colspan="2"><div><a class="big-op-btn" href="/jdtuiguang/promoteItem">我 要 推 广</a></div></td>' +
                '                       </tr>' +*/
                '                   </tbody>' +
                '               </table> ' +
                '           </td>' +
                '           <td style="width: 40%; text-align: left;">' +
                //'               <div><a target="_blank" title="我要升级" class="big-op-btn" href="http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.12.gbGCFZ&service_code=FW_GOODS-1848326&tracelog=search&scm=&ppath=">我 要 升 级</a></div>'+
                '               <div><a class="big-op-btn" href="/jdtuiguang/promoteItem">我 要 推 广</a></div>' +
                '               <div  style="margin-top: 30px;display:none;"><a class="big-op-btn" target="_blank" href="http://www.taovgo.com/">查 看 推 广</a></div>' +
                '           </td></tr>' +
                '           <tr><td colspan="2" style="width: 40%; text-align: left;">' +
//                '               <div style="text-align: center;margin-bottom: 10px;font-size: 20px;" class="ad-middle"><span style="font-weight: bold;color:red;">活动：</span>好评送一个月，10个优质位+3个热销位，详情联系客服！</div>' +
                '               <div style="text-align: center;margin-bottom: 10px;font-size: 20px;" class="ad-middle"><span style="font-weight: bold;color:red;">京东推广：</span>感谢新老客户的信赖！</div>' +
                '           </td></tr>' +
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
                url : '/JDPromote/getUserInfo',
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

                    container.find(".used-popularized-num").html(userJson.popularizedNum+userJson.hotUsedNum);
                    container.find(".remain-popularized-num").html(userJson.remainNum+userJson.hotRemainNum);
                }
            });
        }
    },  DptgIndex.show);

})(jQuery,window));