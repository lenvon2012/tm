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

var selectItems=[];
var distype=1;

DisItem.search =DisItem.search || {};
DisItem.search= $.extend({
    doSearch: function() {
        $('.guanjianci').keyup(function() {
            var lable_key=$('.guanjianci').val();
            if(!lable_key){
                $('.combobox-label-item').show();
            }
            else{
                $('.combobox-label-item').hide();
            }
        });
        $('.fRange').mousemove(function(){
            $('.fR-list').show();
        });
        $('.fRange').mouseout(function(){
            $('.fR-list').hide();
        });
        $('.fRl-ico-pu').click(function(){
            $('.fR-text').html("↑ 价格从低到高");
            $('.fR-text').attr("order","pu");
            DisItem.search.doShow(1);
        });
        $('.fRl-ico-pd').click(function(){
            $('.fR-text').html("↓ 价格从高到低");
            $('.fR-text').attr("order","pd");
            DisItem.search.doShow(1);
        });
        $('.fRl-ico-su').click(function(){
            $('.fR-text').html("↑ 下架时间");
            $('.fR-text').attr("order","su");
            DisItem.search.doShow(1);
        });
        $('.fRl-ico-sd').click(function(){
            $('.fR-text').html("↓ 下架时间");
            $('.fR-text').attr("order","sd");
            DisItem.search.doShow(1);
        });
        $('.fRl-ico-df').click(function(){
            $('.fR-text').html("默认排序");
            $('.fR-text').attr("order","df");
            DisItem.search.doShow(1);
        });

        $('.fSellercat').mousemove(function(){
            $('.fS-list').show();
        });
        $('.fSellercat').mouseout(function(){
            $('.fS-list').hide();
        });

        $('.fItemcat').mousemove(function(){
            $('.fI-list').show();
        });
        $('.fItemcat').mouseout(function(){
            $('.fI-list').hide();
        });


        $('.doDazhe').mousemove(function(){
            $('.tip-content').html("减价模式支持抹零，打折模式适合多个价格的宝贝！");
            $('.tspy').show();
        });
        $('.doDazhe').mouseout(function(){
            $('.tspy').hide();
        });
        $('.doJianjia').mousemove(function(){
            $('.tip-content').html("减价模式支持抹零，打折模式适合多个价格的宝贝！");
            $('.tspy').show();
        });
        $('.doJianjia').mouseout(function(){
            $('.tspy').hide();
        });


        $.get("/items/sellerCatCount",function(data){
            var sellerCat = $('#sellerCat');
            sellerCat.empty();
            if(!data || data.length == 0){
                sellerCat.hide();
            }
            var exist = false;
            var cat = $('<li><a href="javascript:void(0);">所有类目</a></li>');
            cat.click(function(){
                $('.fS-text').html('<a href="javascript:void(0);">所有类目</a>');
                DisItem.search.doShow(1);
            });
            sellerCat.append(cat);
            for(var i=0;i<data.length;i++) {
                if(data[i].count <= 0){
                    continue;
                }
                exist = true;
                var li_option = $('<li></li>');
                var option = $('<a href="javascript:void(0);"></a>');
                option.attr("catId",data[i].id);
                option.html(data[i].name+"("+data[i].count+")");
                option.click(function(){
                    $('.fS-text').html($(this).parent().html());
                    DisItem.search.doShow(1);
                });
                li_option.append(option);
                sellerCat.append(li_option);

            }
        });
        $.get("/items/itemCatCount",function(data){
            var sellerCat = $('#itemCat');
            sellerCat.empty();
            if(!data || data.length == 0){
                sellerCat.hide();
            }

            var exist = false;
            var cat = $('<li><a href="javascript:void(0);">所有类目</a></li>');
            cat.click(function(){
                $('.fI-text').html('<a href="javascript:void(0);">所有类目</a>');
                var catId = $(".fI-text a").attr("catid");
                var status =2;
                DisItem.search.doShow(1);
            });
            sellerCat.append(cat);
            for(var i=0;i<data.length;i++) {
                if(data[i].count <= 0){
                    continue;
                }

                exist = true;
                var li_option = $('<li></li>');
                var option = $('<a href="javascript:void(0);"></a>');
                option.attr("catId",data[i].id);
                option.html(data[i].name+"("+data[i].count+")");
                option.click(function(){
                    $('.fI-text').html($(this).parent().html());
                    var catId = $(".fI-text a").attr("catid");
                    var status =2;
                    DisItem.search.doShow(1);
                });
                li_option.append(option);
                sellerCat.append(li_option);
            }
        });
        //批量修改
        $('.piliang').keyup(function(){
            var piliang= $('.piliang').val();
            $(".item").each(function(i,val){
                var status=$(val).find(".item-status").val();
                if(status!=4){
                    $(val).find('.item-dis').attr("value",piliang);
                    var price = $(val).find('.old-price').val();
                    var discount =Math.round((price*piliang/10)*100)/100;
                    var jianjia= Math.round((price-discount)*100)/100;
                    $(val).find('.item-disprice').attr("value",discount);
                    $(val).find('.item-jianjia').attr("value",jianjia);
                }
            });
        });
        $('.piliang-jianjia').keyup(function(){
            var piliang= $('.piliang-jianjia').val();
            $(".item").each(function(i,val){
                var status=$(val).find(".item-status").val();
                if(status!=4){
                    $(val).find('.item-jianjia').attr("value",piliang);
                    var price = $(val).find('.old-price').val();
                    var disprice= Math.round((price-piliang)*100)/100;
                    var discount =Math.round((disprice/price)*1000)/100;
                    $(val).find('.item-disprice').attr("value",disprice);
                    $(val).find('.item-dis').attr("value",discount);
                }
            });
        });
        //抹零操作
        $('.dofen').click(function(){
            $(".item").each(function(i,val){
                var disprice = $(val).find('.item-disprice').val();
                var status=$(val).find(".item-status").val();
                if(status!=4){
                    if(disprice){
                        disprice= Math.round(disprice*10)/10;
                        $(val).find('.item-disprice').attr("value",disprice);
                        var price = $(val).find('.old-price').val();
                        var discount =Math.round((disprice/price)*1000)/100;
                        var jianjia =Math.round((price-disprice)*100)/100;

                        $(val).find('.item-jianjia').attr("value",jianjia);
                        $(val).find('.item-dis').attr("value",discount);
                    }
                }
            });
        });
        $('.dofenjiao').click(function(){
            $(".item").each(function(i,val){
                var disprice = $(val).find('.item-disprice').val();
                var status=$(val).find(".item-status").val();
                if(status!=4){
                    if(disprice){
                        disprice= Math.round(disprice);
                        $(val).find('.item-disprice').attr("value",disprice);
                        var price = $(val).find('.old-price').val();
                        var discount =Math.round((disprice/price)*1000)/100;
                        var jianjia =Math.round((price-disprice)*100)/100;

                        $(val).find('.item-jianjia').attr("value",jianjia);
                        $(val).find('.item-dis').attr("value",discount);
                    }
                }
            });
        });

        //错误提示
        $('.orangeBtn').click(function(){
            $('#hraBox').hide();
        });
        $('.closeModal').click(function(){
            $('#settingActModal').hide();
        });
        $('.guanjianci-select').click(function(){
            DisItem.search.doShow(1);
        })  ;

        //打折模式
        $('.doJianjia').hide();
        $('.doDazhe').click(function(){
            if (confirm("确定本页所有宝贝全部变为打折模式？" ) == false) {
                return;
            }
            $('.doDazhe').hide();
            $('.doJianjia').show();
            $(".item").each(function(i,val){
                $(val).find(".Model-dazhe").show();
                $(val).find(".Model-jianjia").hide();
                $(val).find(".item-discountType").attr("value",0);
            })
        });
        $('.doJianjia').click(function(){
            if (confirm("确定本页所有宝贝全部变为减价模式？") == false)
                return;
            $('.doJianjia').hide();
            $('.doDazhe').show();
            $(".item").each(function(i,val){
                $(val).find(".Model-jianjia").show();
                $(val).find(".Model-dazhe").hide();
                $(val).find(".item-discountType").attr("value",1);
            })

        });
        DisItem.search.doShow(1);
    },
    doShow: function(currentPage) {
        var data={};
        data.id=$('.activityId').val();
        data.title=$('.guanjianci').val();
        data.cid=$(".fI-text a").attr("catid");
        data.sellerCid= $(".fS-text a").attr("catid");
        data.order = $(".fR-text").attr("order");
        if (currentPage < 1)
            currentPage = 1;
        var tbodyObj = DisItem.container.find(".item-table");
        DisItem.container.find(".paging-div").tmpage({
            currPage: currentPage,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                param: data,
                dataType: 'json',
                url: '/TaoDiscount/searchReviseItems',
                callback: function(dataJson){
                    if(dataJson == null || dataJson.res.length == 0){
                        alert("亲，您还没有此类上架的宝贝哦！！");
                    }
                    else{
                        DisItem.row.setArrayString();
                        if($(".error").length > 0) {
                            alert("折扣范围（0.01--9.9）折，请修正错误再提交");
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
            }

        });

    } ,
    doquanxuan: function(){
        $(".unchoose").hide();
        $('.choose').click(function(){
            $(".choose").hide();
            $(".unchoose").show();
            var piliang= $('.piliang').val();
            $(".item").each(function(i,val){
                var status=$(val).find(".item-status").val();
                if(status==0) {
                    $(val).find('.lightBlueBtn').hide();
                    $(val).find(".lightGrayBtn").show();
                    if(piliang){
                        $(val).find('.item-dis').attr("value",piliang);
                        var price = $(val).find('.old-price').val();
                        var discount =Math.round((price*piliang/10)*100)/100;
                        var jianjia= Math.round((price-discount)*100)/100;
                        $(val).find('.item-disprice').attr("value",discount);
                        $(val).find('.item-jianjia').attr("value",jianjia);
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

        trObj.find(".item-code").attr("value", itemJson.numiid);
        trObj.find(".item-promotionId").attr("value", itemJson.promotionId);
        var href = "http://item.taobao.com/item.htm?id=" + itemJson.numiid;
        trObj.find(".item-href").attr("href", href);
        trObj.find(".item-href").attr("target", "_blank");
        trObj.find(".item-img").attr("src", itemJson.picURL);
        trObj.find(".item-name").html(itemJson.title);
        trObj.find(".item-price").html(itemJson.price);
        trObj.find(".item-discountType").attr("value",itemJson.discountType) ;
        trObj.find(".old-price").attr("value",itemJson.price) ;
//            trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的


        var refreshCallback = function() {
            DisItem.search.doSearch();
        };

        trObj.find(".delete-promotion").click(function(){
            if (confirm("确定删除该宝贝？" ) == false) {
                return;
            }
            var subString2="";
            var activityId = $('.activityId').val();
            subString2 =  activityId+","+ itemJson.promotionId+","+ itemJson.numiid+"!";
            $.ajax({
                url : '/TaoDiscount/deleteReviseItem',
                data : {subString:subString2},
                type : 'post',
                success : function(data) {
                    if(data == null || data.length == 0){
                        alert("删除商品成功");
                        location.reload();
                    } else {
                        alert(data.msg);
                        return false;
                    }
                }
            });
        });
        var html1 = '' +
            '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn">修改活动</a>' +
            '<a href="javascript:;"  class="lightGrayBtn addedToActBtn productBtn">已添加</a>' +
            '';

            trObj.find(".item-status").attr("value", 0);//全部没有选择
            trObj.find(".op-td").html(html1);

            trObj.find('.lightGrayBtn').hide();
        if(itemJson.discountType==0){
            var dis=itemJson.discountValue;
            var price = itemJson.price;
            var discount =Math.round((price*dis/10)*100)/100;
            var jianjia= Math.round((price-discount)*100)/100;
            trObj.find(".item-dis").attr("value",dis);
            trObj.find('.item-disprice').attr("value",discount);
            trObj.find('.item-jianjia').attr("value",jianjia);
            trObj.find(".Model-dazhe").show();
            trObj.find(".Model-jianjia").hide();
            trObj.find(".Model-dazhe").click(function(){
                trObj.find(".Model-jianjia").show();
                trObj.find(".Model-dazhe").hide();
                trObj.find(".item-discountType").attr("value",1);
            }) ;
            trObj.find(".Model-jianjia").click(function(){
                trObj.find(".Model-dazhe").show();
                trObj.find(".Model-jianjia").hide();
                trObj.find(".item-discountType").attr("value",0);
            }) ;
        }
        else{
            var jianjia= itemJson.discountValue;
            var price =itemJson.price;
            var dis=Math.round(((price-jianjia)/price)*1000)/100;
            var disprice= Math.round((price-jianjia)*100)/100;
            trObj.find(".item-disprice").attr("value",disprice);
            trObj.find(".item-dis").attr("value",dis) ;
            trObj.find('.item-jianjia').attr("value",jianjia);
            trObj.find(".Model-jianjia").show();
            trObj.find(".Model-dazhe").hide();
            trObj.find(".Model-dazhe").click(function(){
                trObj.find(".Model-jianjia").show();
                trObj.find(".Model-dazhe").hide();
                trObj.find(".item-discountType").attr("value",1);
            }) ;
            trObj.find(".Model-jianjia").click(function(){
                trObj.find(".Model-dazhe").show();
                trObj.find(".Model-jianjia").hide();
                trObj.find(".item-discountType").attr("value",0);
            }) ;
        }

        for (var i=0; i<selectItems.length;i++){
            if (selectItems[i].id == itemJson.numiid){
                trObj.find(".item-status").attr("value", 8);
                trObj.find(".item-disprice").attr("value",selectItems[i].disprice);
                trObj.find(".item-dis").attr("value",selectItems[i].discount) ;
                trObj.find('.item-jianjia').attr("value",selectItems[i].jianjia);
                trObj.find(".item-discountType").attr("value",selectItems[i].discountType);
                trObj.find(".lightGrayBtn").css("display","block");
                trObj.find('.lightBlueBtn').css("display","none");
                if(selectItems[i].discountType==0){
                    trObj.find(".Model-dazhe").show();
                    trObj.find(".Model-jianjia").hide();
                }
                else{
                    trObj.find(".Model-jianjia").show();
                    trObj.find(".Model-dazhe").hide();
                }
            }
        }


            trObj.find(".lightBlueBtn").click(function() {
                trObj.find('.lightBlueBtn').hide();
                trObj.find(".lightGrayBtn").show();
                trObj.find(".item-status").attr("value", 8);
            });
            trObj.find(".lightGrayBtn").click(function(){
                trObj.find('.lightBlueBtn').show();
                trObj.find(".lightGrayBtn").hide();
                trObj.find(".item-status").attr("value", 0);
            }) ;

        trObj.find(".item-dis").keyup(function(){
            var dis= trObj.find(".item-dis").val();
            var price =itemJson.price;
            var discount =Math.round((price*dis/10)*100)/100;
            var jianjia=Math.round((price-discount)*100)/100 ;
            trObj.find(".item-disprice").attr("value",discount);
            trObj.find(".item-jianjia").attr("value",jianjia);
        });

        trObj.find(".item-disprice").keyup(function(){
            var dis= trObj.find(".item-disprice").val();
            var price =itemJson.price;
            var discount=Math.round((dis/price)*1000)/100;
            var jianjia= Math.round((price-dis)*100)/100;
            trObj.find(".item-dis").attr("value",discount) ;
            trObj.find(".item-jianjia").attr("value",jianjia);
        });
        trObj.find(".item-jianjia").keyup(function(){
            var jianjia= trObj.find(".item-jianjia").val();
            var price =itemJson.price;
            var dis=Math.round(((price-jianjia)/price)*1000)/100;
            var disprice= Math.round((price-jianjia)*100)/100;
            trObj.find(".item-disprice").attr("value",disprice);
            trObj.find(".item-dis").attr("value",dis) ;
        });


        return trObj;
    },
    createHtml: function(itemJson) {
        var html='' +
            '<div class="item">' +
            '   <input type="hidden" class="item-code" /> ' +
            '   <input type="hidden" class="item-discountType" />'+
            '   <input type="hidden" class="item-promotionId" />'+
            '   <input type="hidden" class="item-status" /> ' +
            '   <input type="hidden" class="old-price" >'+
            '    <a class="productImg item-href">' +
            '        <img class="item-img"  />' +
            '    </a>' +
            '    <div class="productInfo">' +
            '        <div class="productTitle">' +
            '          <a style="height: 40px;width: 100%;display: block;overflow: hidden;" class="item-href item-link item-name"></a>' +
            '        </div>' +
            '        <p>' +
            '           <span style="font-size: 20px;color: #C49173">原价：</span>' +
            '           <em class="proSell-price">¥</em>' +
            '           <em class="proSell-price item-price"></em>' +
            '        </p>' +
            '        <p>' +
            '           <span style="font-size: 12px;color: #C49173">促销模式：</span>' +
            '           <a class="disModel" style="color:#FF9A36" href="javascript:void(0)"><b class="Model-dazhe">打折<span  style="color: #C49173">（点击修改）</span></b><b class="Model-jianjia">减价<span  style="color: #C49173">（点击修改）</span></b>' +
            '           </a>' +
            '        </p>' +
            '    </div>' +
            '    <div class="productDis">' +
            '        <div class="dazheValue">' +
            //'           打折：' +
            //'            <br>' +
            '            打<input class="item-dis" type="text" style="border: 1px solid #B0A59F"  />' +
            '           折' +
            '        </div>' +
            '        <div class="jianjiaValue">' +
            //'           减价：' +
            //'            <br>' +
            '            减<input class="item-jianjia" type="text" style="color:#B0A59F;border: 1px solid #B0A59F" />' +
            '           元' +
            '        </div>' +
            '        <div class="zhehoujiaValue">' +
            //'           折后价：' +
            //'            <br>' +
            '            结果<input class="item-disprice" type="text" style="color:#C00;border: 1px solid #B0A59F" />' +
            '           元' +
            '        </div>' +
            '        ' +
            '    </div>' +
            '    <div class="op-td">'+
            '    </div>' +
            '    <div>' +
            '        <a class="delete-promotion" href="javascript:void(0)" style="width: 30px;height: 30px;display: block;border: 1px solid #fff;position: absolute;right:0;top:0;"> <a class="closed-img delete-promotion" href="javascript:void(0)" style="right: 9px;top:9px;"></a></a>' +
            '    </div>' +
            '</div>' +
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
            var promotionId=$(val).find('.item-promotionId').val();
            var discountType=$(val).find('.item-discountType').val();
            var decrease_num=$(val).find(".item-decrease_num").val();
            var status=$(val).find(".item-status").val();
            var discount=$(val).find(".item-dis").val();
            var input =$(val).find(".item-dis");
            var jianjia=$(val).find(".item-jianjia").val();
            var disprice=$(val).find(".item-disprice").val();
            item.id=itemCode;

            if(status==8) {
                DisItem.submit.checkDiscount(input);
                item.promotionId = promotionId;
                item.discount=discount;
                item.jianjia=jianjia;
                item.disprice=disprice;
                item.discountType=discountType;
                item.decreaseNum=decrease_num;
                DisItem.row.addToSelectedItems(item);
            }
            else{
                input.removeClass('error');
                DisItem.row.removeFromSelectedItems(item);
            }
        })
    }
}, DisItem.row);

DisItem.submit = DisItem.submit || {};
DisItem.submit = $.extend({
    doSub:function(){
        $('.StepBtn2').click(function(){

            var subString1= DisItem.submit.getString1();

            if(subString1!="")  {
                if($(".error").length > 0) {
                    alert("折扣范围（0.01--9.9）折，请修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }
                $.ajax({
                    url : '/TaoDiscount/reviseItem',
                    data : {subString:subString1},
                    type : 'post',
                    success : function(data) {
                        if(data.res == null || data.res.length == 0){
                            var scrollTop = $(document).scrollTop();
                            var scrollLeft = $(document).scrollLeft();
                            if(data.msg=="0"){
                                var Div = $('#hraBox');
                                var Divtop = ($(window).height() - Div.height())/2;
                                var Divleft = ($(window).width() - Div.width())/2;
                                Div.css( { 'top' : Divtop + scrollTop, left : Divleft + scrollLeft } ).show();
                            }
                            else if(data.msg=="-1"){
                                alert("淘宝服务器忙，请过2分钟后重试！") ;
                            }
                            else if(!data.msg){
                                alert("商品更新成功！");
                                window.location.href ="/TaoDiscount/index";
                            }
                            else{
                                var limitDis=data.msg;
                                limitDis=limitDis/10;
                                var settingActModal = $('#settingActModal');
                                var settingActModaltop = ($(window).height() - settingActModal.height())/2;
                                var settingActModalleft = ($(window).width() - settingActModal.width())/2;
                                settingActModal.find('.orange').html(limitDis) ;
                                settingActModal.css( { 'top' : settingActModaltop + scrollTop, left : settingActModalleft + scrollLeft } ).show();
                            }
                        } else {
                            DisItem.error.showErrors(data.res);
                        }
                    }
                });
            }
//            if(subString2!=""){
//                $.ajax({
//                    url : '/TaoDiscount/deleteReviseItem',
//                    data : {subString:subString2},
//                    type : 'post',
//                    success : function(data) {
//                        if(data == null || data.length == 0){
//                            alert("删除商品成功");
//                        } else {
//                           alert(data.msg);
//                            return false;
//                        }
//                    }
//                });
//            }
        })
    } ,
    checkDiscount : function(input) {
        var val = $.trim(input.val());
        if(isNaN(val) || val <=0 || val >= 10) {
            var text = '折扣应是大于0.01小于10的数字';
            input.addClass("error");
        }
        else {
            input.removeClass("error");
        }
    },
    getString1: function(){
//        var subString="";
//        $(".item").each(function(i,val){
//            var promotionId=$(val).find('.item-promotionId').val();
//            var discountType=$(val).find('.item-discountType').val();
//            var disprice=$(val).find('.item-disprice').val();
//            var input =$(val).find(".item-dis");
//            var discount=$(val).find('.item-dis').val();
//            if(discountType==0){
//                discount=$(val).find(".item-dis").val();
//            }
//            else{
//                discount=$(val).find(".item-jianjia").val();
//            }
//            if(($(val).find(".item-status")).val()==8) {
//                DisItem.submit.checkDiscount(input);
//                subString+=promotionId+","+discount+"!";
//            }
//        })
//        return subString;
        var subString="";
        DisItem.row.setArrayString();
        for (var i=0; i<selectItems.length;i++){
            if(selectItems[i].discountType==0){
                subString +=selectItems[i].promotionId+","+selectItems[i].discount+","+selectItems[i].discountType+"!";
            }
            else{
                subString +=selectItems[i].promotionId+","+selectItems[i].jianjia+","+selectItems[i].discountType+"!";
            }
        }
        return subString;
    }
//    getString2: function(){
//        var subString="";
//        var activityId=$(".activityId").val();
//        $(".item").each(function(i,val){
//            var promotionId=$(val).find('.item-promotionId').val();
//            var itemCode=$(val).find('.item-code').val();
//            if(($(val).find(".item-status")).val()==0) {
//                subString+=activityId+","+promotionId+","+itemCode+"!";
//            }
//        })
//        return subString;
//    }
}, DisItem.submit) ;

/**
 * 操作失败的日志
 * @type {*}
 */
DisItem.error = DisItem.error || {};
DisItem.error = $.extend({
    showErrors: function(errorJsonArray) {

        var html='' +
            '<div class="error-item-div busSearch" style="margin-top: 10px;">' +
            '   <span class="error-tip-span">宝贝操作失败列表：</span> ' +
            '   <table class="error-item-table list-table">' +
            '       <thead>' +
            '       <tr>' +
            '           <td style="width: 15%;">宝贝图片</td>' +
            '           <td style="width: 35%;">标题</td>' +
            '           <td style="width: 30%;">失败说明</td>' +
            '           <td style="width: 15%;">操作时间</td>' +
            '       </tr>' +
            '       </thead>' +
            '       <tbody></tbody>' +
            '   </table> ' +
            '</div>' +
            '';
        var dialogObj = $(html);

        $(errorJsonArray).each(function(index, errorJson) {
            var trObj = DisItem.error.createRow(index, errorJson);
            dialogObj.find(".error-item-table").find("tbody").append(trObj);
        });

        $("body").append(dialogObj);
        dialogObj.dialog({
            modal: true,
            bgiframe: true,
            height:500,
            width:780,
            title:'宝贝错误列表',
            autoOpen: false,
            resizable: false,
            buttons:{'返回活动列表':function() {
                window.location.href="/TaoDiscount/index";
            },'取消':function(){
                location.reload();
                $(this).dialog('close');
            }}
        });

        dialogObj.dialog("open");

    },
    createRow: function(index, errorJson) {
        var itemJson = errorJson.item;
        var opstatus = errorJson.opstatus;
        var html = '' +
            '<tr>' +
            '   <td><a class="item-link" target="_blank"><img class="item-img" style="width: 60px;height: 60px;" /> </a> </td>' +
            '   <td><a class="item-link" target="_blank"><span class="item-title"></span> </a> </td>' +
            '   <td><span class="error-intro"></span> </td>' +
            '   <td><span class="op-time"></span> </td>' +
            '</tr>' +
            '' +
            '' +
            '';
        var trObj = $(html);
        var url = "http://auction2.paipai.com/" + itemJson.numIid;
        trObj.find(".item-link").attr("href", url);
        trObj.find(".item-img").attr("src", itemJson.picURL);
        trObj.find(".item-title").html(itemJson.title);
        trObj.find(".error-intro").html(opstatus.opMsg);

        var theDate = new Date();
        var year = theDate.getFullYear();
        var month = theDate.getMonth() + 1;//js从0开始取
        var date = theDate.getDate();
        var hour = theDate.getHours();
        var minutes = theDate.getMinutes();
        var second = theDate.getSeconds();

        if (month < 10) {
            month = "0" + month;
        }
        if (date < 10) {
            date = "0" + date;
        }
        if (hour < 10) {
            hour = "0" + hour;
        }
        if (minutes < 10) {
            minutes = "0" + minutes;
        }
        if (second < 10) {
            second = "0" + second;
        }
        var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
        trObj.find(".op-time").html(timeStr);

        return trObj;
    }
}, DisItem.error);