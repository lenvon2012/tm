TM.DisItem = TM.DisItem || {};

var DisItem = TM.DisItem;

DisItem.init = DisItem.init || {};
DisItem.init = $.extend({
    doInit: function(container) {
        DisItem.container = container;
        DisItem.search.doSearch();
        DisItem.search.doquanxuan();
        DisItem.submit.doSub();
    }

},DisItem.init);

var selectItems =[];

DisItem.search =DisItem.search || {};
DisItem.search= $.extend({
    doSearch: function() {
        $.get("/paipaidiscount/sellerCatCount",function(data){
            var sellerCat = $('#sellerCat');
            sellerCat.empty();
            if(!data ||data.res.length == 0){
                sellerCat.hide();
            }

            var exist = false;
            var cat = $('<option>所有分类</option>');
            sellerCat.append(cat);
            for(var i=0;i<data.res.length;i++) {
                if(data.res[i].count <= 0){
                    continue;
                }
                var item_cat=data.res[i];
                exist = true;
                var option = $('<option></option>');
                option.attr("catId",item_cat.cid);
                option.html(item_cat.name);
                sellerCat.append(option);
            }
        });
        $('#sellerCat').change(function(){
            var catId = $("#sellerCat option:selected").attr("catid");
            var status=1;
            DisItem.search.doShow(1,status, catId);
        }) ;
        $('.guanjianci-select').click(function(){
            var status =2;
            var catId=$('.guanjianci').val();
            DisItem.search.doShow(1, status,catId);
        })  ;
        var status=0;
        var catId = $("#sellerCat option:selected").attr("catid");
        DisItem.search.doShow(1,status, catId);
        //批量修改选中宝贝
        $('.piliang').keyup(function(){
            var piliang= Math.round(($('.piliang').val())*1000)/1000;
            $(".item").each(function(i,val){
                var input =$(val).find(".item-dis");
                var status=$(val).find(".item-status").val();

                if(status==8) {
                    $(val).find('.item-dis').attr("value",piliang);
                    var price = $(val).find('.old-price').val();
                    var discount =Math.round((price*piliang/10)*100)/100;
                    $(val).find('.item-disprice').attr("value",discount);
                }

            })
        });
    },
    doShow: function(currentPage,status,catId) {
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
                param: {activityId:activityId,status:status,catId:catId},
                dataType: 'json',
                url: '/paipaidiscount/searchAddItems',
                callback: function(dataJson){
                    DisItem.row.setArrayString();
                    if($(".error").length > 0) {
                        alert("折扣范围（7--9.9）折，请修正错误再提交");
                        setTimeout(function() {
                            $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                        }, 300);
                        return false;
                    }
                    tbodyObj.html("");
                    var itemArray = dataJson.res;
                    $(itemArray).each(function(index, itemJson) {
                        var trObj = DisItem.row.createRow(index, itemJson);
                        tbodyObj.append(trObj);
                    });
                }
            }
        });
    } ,
    doquanxuan: function(){
        $(".unchoose").hide();
        $('.choose').click(function(){
            $(".choose").hide();
            $(".unchoose").show();
            var piliang= Math.round(($('.piliang').val())*1000)/1000;
            $(".item").each(function(i,val){
                var status=$(val).find(".item-status").val();
                if(status==0) {
                    $(val).find('.lightBlueBtn').hide();
                    $(val).find(".lightGrayBtn").show();
                    if(piliang){
                        $(val).find('.item-dis').attr("value",piliang);
                        var price = $(val).find('.old-price').val();
                        var discount =Math.round((price*piliang/10)*100)/100;
                        $(val).find('.item-disprice').attr("value",discount);
                    }
                    $(val).find(".item-status").attr("value", 8);
                }
            })
        })
        $('.unchoose').click(function(){
            $(".unchoose").hide();
            $(".choose").show();
            $(".item").each(function(i,val){
                var status=$(val).find(".item-status").val();
                if(status==8) {
                    $(val).find('.lightGrayBtn').hide();
                    $(val).find(".lightBlueBtn").show();
                    $(val).find('.item-dis').attr("value","");
                    $(val).find('.item-disprice').attr("value","");
                    $(val).find(".item-status").attr("value", 0);
                }
            })
        })
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
        trObj.find(".old-price").attr("value",itemJson.itemPrice/100) ;
//            trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的


        var refreshCallback = function() {
            DisItem.search.doSearch();
        };


        var html1 = '' +
            '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn">加入活动</a>' +
            '<a href="javascript:;"  class="lightGrayBtn addedToActBtn productBtn">已添加</a>' +
            '';
        var html2=''+
            '<span class="item_disable">已参加其他活动</span>' +
            '';

        var html3='<span class="item_thisActivity">已参加本次活动</span>' ;

        if(itemJson.itemDiscount!=0) {
            trObj.find(".item-status").attr("value", 4);//已经参加其他活动的
            if(itemJson.isthisActivity==true){
                trObj.addClass("thisActivity");
                trObj.find(".op-td").html(html3);
            }
            else{
                trObj.addClass("disabled");
                trObj.find(".op-td").html(html2);
            }
            trObj.find(".item-dis").attr("value",itemJson.itemDiscount/1000);
            var after_price =itemJson.itemPrice*itemJson.itemDiscount/1000000;
            after_price=Math.round(after_price*100)/100;
            trObj.find(".item-disprice").attr("value",after_price);
        }
        else{
            trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的
            trObj.find(".op-td").html(html1);
            trObj.find('.lightGrayBtn').hide();

            for (var i=0; i<selectItems.length;i++){
                if (selectItems[i].id ==itemJson.itemCode){
                    trObj.find(".item-status").attr("value", 8);
                    trObj.find(".item-disprice").attr("value",selectItems[i].disprice);
                    trObj.find(".item-dis").attr("value",selectItems[i].discount/1000);
                    trObj.find(".lightGrayBtn").css("display","block");
                    trObj.find('.lightBlueBtn').css("display","none");
                }
            }

            trObj.find(".lightBlueBtn").click(function() {
                trObj.find('.lightBlueBtn').hide();
                trObj.find(".lightGrayBtn").show();
                var piliang= Math.round(($('.piliang').val())*1000)/1000;
                if(piliang){
                    trObj.find('.item-dis').attr("value",piliang);
                    var price = itemJson.itemPrice/100;
                    var discount =Math.round((price*piliang/10)*100)/100;
                    trObj.find('.item-disprice').attr("value",discount);
                }
                trObj.find(".item-status").attr("value", 8);
            });
            trObj.find(".lightGrayBtn").click(function(){
                trObj.find('.lightBlueBtn').show();
                trObj.find(".lightGrayBtn").hide();
                trObj.find('.item-dis').attr("value","");
                trObj.find('.item-disprice').attr("value","");
                trObj.find(".item-status").attr("value", 0);
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
                trObj.find(".item-dis").attr("value",discount) ;
            });

        }
        return trObj;
    },
    createHtml: function(itemJson) {

        var html = '' +
            '<tr class="item">' +
            '   <input type="hidden" class="item-code" /> ' +
            '   <input type="hidden" class="item-status" /> ' +
            '   <input type="hidden" class="old-price" >'+
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
    },
    addToSelectedItems :function(item){
        var existed = DisItem.row.removeFromSelectedItems(item);
        selectItems.push(item);
        return !existed;
    },
    removeFromSelectedItems : function(item){
        for (var i=0; i<selectItems.length;i++){
            if (selectItems[i].id == item.id){
                selectItems.splice(i,1);
                return true;
            }
        }
        return false;
    },
    setArrayString : function(){
        $(".item").each(function(i,val){
            var item={};
            var itemCode=$(val).find('.item-code').val();
            var status=$(val).find(".item-status").val();
            var discount=Math.round(($(val).find('.item-dis').val())*1000)/1000;
            discount=discount*1000;
            var input =$(val).find(".item-dis");
            var disprice=$(val).find(".item-disprice").val();

            if(status==8) {
                DisItem.submit.checkDiscount(input);
                item.id=itemCode;
                item.discount=discount;
                item.disprice=disprice;
                DisItem.row.addToSelectedItems(item);
            }
            else{
                DisItem.row.removeFromSelectedItems(item);
            }
        })
    }
}, DisItem.row);

DisItem.submit = DisItem.submit || {};
DisItem.submit = $.extend({
    doSub:function(){
        $('.StepBtn2').click(function(){
            var subString=DisItem.submit.subString();
            if($(".error").length > 0) {
                alert("折扣范围（5--9.9）折，请修正错误再提交");
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
                        window.location.href="/paipaidiscount/addSuccess";
                    } else {
                        alert(data.msg);
                    }
                }
            });
        })

    },
    subString : function(){
        var subString="";
        var reqType=1;
        var activityId=$('.activityId').val();
        DisItem.row.setArrayString();

        for (var i=0; i<selectItems.length;i++){
            subString +=reqType+","+activityId+","+selectItems[i].id+","+selectItems[i].discount+"!";
        }
        return subString;
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
    }
}, DisItem.submit) ;