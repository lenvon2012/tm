TM.DisItem = TM.DisItem || {};

var DisItem = TM.DisItem;

DisItem.init = DisItem.init || {};
DisItem.init = $.extend({
    doInit: function(container) {
        DisItem.container = container;
        DisItem.search.doSearch();
        DisItem.submit.doSub();
    }

},DisItem.init);

DisItem.search =DisItem.search || {};
DisItem.search= $.extend({
    doSearch: function() {
        DisItem.search.doShow(1);
    },
    doShow: function(currentPage) {
        if (currentPage < 1)
            currentPage = 1;
        var tbodyObj = DisItem.container.find(".item-table").find("tbody");
        var activityId=$('.activityId').val();
        DisItem.container.find(".paging-div").tmpage({
            currPage: currentPage,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                param: {activityId:activityId},
                dataType: 'json',
                url: '/paipaidiscount/getLtdItem',
                callback: function(dataJson){
                    tbodyObj.html("");
                    var itemArray = dataJson.res;
                    $(itemArray).each(function(index, itemJson) {
                        var trObj = DisItem.row.createRow(index, itemJson);
                        tbodyObj.append(trObj);
                    });
                }
            }

        });

    }

},DisItem.search);

DisItem.row = DisItem.row || {};
DisItem.row = $.extend({
    createRow: function(index, itemJson) {
        var html = DisItem.row.createHtml();
        var trObj = $(html);

        trObj.find(".item-code").attr("value", itemJson.itemCode);
        var href = "http://auction2.paipai.com/" + itemJson.itemCode;
        trObj.find(".item-href").attr("href", href);
        trObj.find(".item-href").attr("target", "_blank");
        trObj.find(".item-img").attr("src", itemJson.picLink);
        trObj.find(".item-name").html(itemJson.itemName);
        trObj.find(".item-price").html(itemJson.itemPrice/100);
//            trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的


        var refreshCallback = function() {
            DisItem.search.doSearch();
        };


        var html1 = '' +
            '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn">退出活动</a>' +
            '<a href="javascript:;"  class="lightGrayBtn addedToActBtn productBtn">已删除</a>' +
            '';

        trObj.find(".item-status").attr("value", 8);//已经参加活动的
        trObj.find(".op-td").html(html1);

        trObj.find('.lightGrayBtn').hide();

        trObj.find(".item-dis").attr("value",itemJson.itemDiscount/1000);
        var after_price =itemJson.itemPrice*itemJson.itemDiscount/1000000;
        after_price=Math.round(after_price*100)/100;
        trObj.find(".item-disprice").attr("value",after_price);


        trObj.find(".lightBlueBtn").click(function() {
            trObj.find('.lightBlueBtn').hide();
            trObj.find(".lightGrayBtn").show();
            trObj.find(".item-status").attr("value", 0);
        });
        trObj.find(".lightGrayBtn").click(function(){
            trObj.find('.lightBlueBtn').show();
            trObj.find(".lightGrayBtn").hide();
            trObj.find(".item-status").attr("value", 8);
        }) ;

        trObj.find(".item-dis").keyup(function(){
            var dis= trObj.find(".item-dis").val();
            var price =itemJson.itemPrice/100;
            var discount =Math.round((price*dis/10)*100)/100;
            trObj.find(".item-disprice").attr("value",discount) ;
        });

        trObj.find(".item-disprice").keyup(function(){
            var dis= trObj.find(".item-disprice").val();
            var price =itemJson.itemPrice/100;
            var discount=Math.round((dis/price)*10000)/1000;
            trObj.find(".item-dis").attr("value",discount);
        });


        return trObj;
    },
    createHtml: function(itemJson) {

        var html = '' +
            '<tr class="item">' +
            '   <input type="hidden" class="item-code" /> ' +
            '   <input type="hidden" class="item-status" /> ' +
            '   <td class="result-td"><a class="item-href"><img class="item-img" style="width: 90px; height: 90px;"/></a> </td>' +
            '   <td class="result-td"><a class="item-href item-link item-name"></a></td>' +
            '   <td class="result-td"><span class="item-price"></span></td>'+
            '   <td class="result-td op-item-dis"><input type="text"  class="item-dis" style="color: #FF4400;"></td>'+
            '   <td class="result-td op-item-disprice"><input type="text" class="item-disprice" ></td>'+
            '   </td class="result-td">' +
            '   </td class="result-td">' +
            '   <td class="result-td op-td">' +
            '       ' +
            '   </td> ' +
            '</tr>' +
            '';
        return html;
    }
}, DisItem.row);

DisItem.submit = DisItem.submit || {};
DisItem.submit = $.extend({
    doSub:function(){
        $('.StepBtn2').click(function(){

            var subString= DisItem.submit.getString();

            if(subString!="")  {
                if($(".error").length > 0) {
                    alert("折扣范围（5--9.99）折，请修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }
                $.ajax({
                    url : '/paipaidiscount/setLtdItem',
                    data : {itemString:subString},
                    type : 'post',
                    success : function(data) {
                        if(data == null || data.length == 0){
                            alert("更新商品信息成功");
                            location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                });
            }
        })


    } ,
    checkDiscount : function(input) {
        var val = $.trim(input.val());
        if(isNaN(val) || val < 5 || val >= 10) {
            var text = '折扣应是大于5小于10的数字';
            input.addClass("error");
        }
        else {
            input.removeClass("error");
        }
    },
    getString: function(){
        var subString="";
        var activityId=$('.activityId').val();
        $(".item").each(function(i,val){
            var discount=Math.round(($(val).find('.item-dis').val())*1000)/1000;
            discount=discount*1000;
            var input= $(val).find('.item-dis')  ;
            var itemCode= $(val).find('.item-code').val() ;
            if(($(val).find(".item-status")).val()==8) {
                DisItem.submit.checkDiscount(input);
                var reqType3=3;//更新商品活动信息
                subString+=reqType3+","+activityId+","+itemCode+","+discount+"!";
            }
            if(($(val).find(".item-status")).val()==0) {
                var reqType2=2;//取消参加活动商品
                var discount2=0;//表示不传入discount值
                subString+=reqType2+","+activityId+","+itemCode+","+discount2+"!";
            }
        })
        return subString;
    }
}, DisItem.submit) ;