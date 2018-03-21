var TM = TM || {};
((function ($, window) {
    TM.DisAct = TM.DisAct || {};

    var DisAct = TM.DisAct;

    DisAct.init = DisAct.init || {};
    DisAct.init = $.extend({
        doInit: function(container) {
            DisAct.container = container;
            DisAct.search.doSearch();
        }

    },DisAct.init);

    DisAct.search =DisAct.search || {};
    DisAct.search= $.extend({
        doSearch: function() {
            DisAct.search.doShow();
        },
        doShow: function() {
            var tbodyObj = DisAct.container.find(".item-table").find("tbody");
            DisAct.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {},
                    dataType: 'json',
                    url: '/PaiPaiDiscount/getLtdActive',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = DisAct.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }
            });

        }


    },DisAct.search);

    DisAct.row = DisAct.row || {};
    DisAct.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DisAct.row.createHtml();
            var trObj = $(html);

            trObj.find(".item-activityId").attr("value", itemJson.activityId);
            var href = "http://auction2.paipai.com/" + itemJson.itemCode;
            trObj.find(".item-href").attr("href", href);
            trObj.find(".item-href").attr("target", "_blank");
            trObj.find(".item-name").html(itemJson.activityName);
            trObj.find(".item-beginTime").html(itemJson.beginTime);
            trObj.find(".item-endTime").html(itemJson.endTime);


            var refreshCallback = function() {
                DisAct.search.doSearch();
            };

                var html1 = '' +
                    '<a  href="javascript:;" class="lightBlueBtn reviseItem">修改商品</a>' +
                    '<a  href="javascript:;" class="lightBlueBtn addItem">添加商品</a>' +
                    '<a  href="javascript:;" class="lightBlueBtn reviseAct">修改活动信息</a>' +
                    '<a  href="javascript:;" class="lightBlueBtn deleteAct">结束活动</a>' +
                    '';

            trObj.find(".op-td").html(html1);

            trObj.find(".reviseItem").click(function() {
                var href="/paipaidiscount/reviseItem?activityId="+itemJson.activityId;
                trObj.find(".reviseItem").attr("href", href);
                trObj.find(".reviseItem").attr("target","_blank");
            });
            trObj.find(".addItem").click(function(){
                var href="/paipaidiscount/addItem?activityId="+itemJson.activityId;
                trObj.find(".addItem").attr("href", href);
                trObj.find(".addItem").attr("target","_blank");
            }) ;
            trObj.find(".reviseAct").click(function(){
                var href="/paipaidiscount/reviseAct?activityId="+itemJson.activityId;
                trObj.find(".reviseAct").attr("href", href);
                trObj.find(".reviseAct").attr("target","_blank");
            }) ;
            trObj.find(".deleteAct").click(function(){
                $.ajax({
                    url : '/PaiPaiDiscount/delLtdActive',
                    data : {activityId:itemJson.activityId},
                    type : 'post',
                    success : function(data) {
                        if(data == null || data.length == 0){
                            alert("删除活动成功");
                        }
                        else {
                            alert(data.msg);
                        }
                    }
                });
            }) ;



            return trObj;
        },
        createHtml: function(itemJson) {

            var html = '' +
                '<tr>' +
                '   <input type="hidden" class="item-activityId" /> ' +
                '   <td class="result-td"><a class="item-href item-link item-name"></a></td>' +
                '   <td class="result-td"><span class="item-beginTime" style=""></span></td>'+
                '   <td class="result-td"><span class="item-endTime" style=""></span></td>'+
                '   </td class="result-td">' +
                '   <td class="result-td op-td">' +
                '       ' +
                '   </td> ' +
                '</tr>' +
                '';
            return html;
        }

    }, DisAct.row);


})(jQuery,window));
