TM.DisItem = TM.DisItem || {};

var DisItem = TM.DisItem;

DisItem.init = DisItem.init || {};
DisItem.init = $.extend({
    doInit: function(container) {
        DisItem.container = container;
        DisItem.search.doSearch();
        DisItem.Chart.initSearchParams();
        DisItem.Chart.drawGoodRate(0);
    }

},DisItem.init);


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

        DisItem.search.doShow(1);
    },
    doShow: function(currentPage) {
        var data={};
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
                url: '/TaoDiscount/searchPromotionItems',
                callback: function(dataJson){
                    if(dataJson == null || dataJson.res.length == 0){
                        alert("亲，您还没有参加促销活动的宝贝哦！！");
                    }
                    else{
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

        var html1 = '' +
            '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn">查看促销效果</a>' +
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
        }


        trObj.find(".lightBlueBtn").click(function() {
            DisItem.Chart.drawGoodRate(itemJson.numiid);
        });


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
            '           打折：' +
            '            <br>' +
            '            <input class="item-dis" type="text" style="border: 1px solid #B0A59F"  />' +
            '           折' +
            '        </div>' +
            '        <div class="jianjiaValue">' +
            '           减价：' +
            '            <br>' +
            '            <input class="item-jianjia" type="text" style="color:#B0A59F;border: 1px solid #B0A59F" />' +
            '           元' +
            '        </div>' +
            '        <div class="zhehoujiaValue">' +
            '           折后价：' +
            '            <br>' +
            '            <input class="item-disprice" type="text" style="color:#C00;border: 1px solid #B0A59F" />' +
            '           元' +
            '        </div>' +
            '        ' +
            '    </div>' +
            '    <div class="op-td">'+
            '    </div>' +
//            '    <div>' +
//            '        <a class="delete-promotion" href="javascript:void(0)" style="width: 30px;height: 30px;display: block;border: 1px solid #fff;position: absolute;right:0;top:0;"> <a class="closed-img delete-promotion" href="javascript:void(0)" style="right: 9px;top:9px;"></a></a>' +
//            '    </div>' +
            '</div>' +
            '';

        return html;
    }

}, DisItem.row);

DisItem.Chart = DisItem.Chart || {};

DisItem.Chart = $.extend({
    drawGoodRate : function(numIid){
        var ruleData = DisItem.Chart.getQueryRule(numIid);
        $.post('/taodiscount/getQuerySalesCount', ruleData, function(data){
            DisItem.Chart.renderChart(data.res);
        });
    },

    getQueryRule: function(numIid) {
        var ruleData = {};
        var startTime = DisItem.container.find(".start-time-text").val();
        var endTime = DisItem.container.find(".end-time-text").val();
        var numIid = numIid;
        ruleData.numIid=numIid;
        ruleData.startTime = startTime;
        ruleData.endTime = endTime;
        return ruleData;
    },
    initSearchParams : function(){
        var now = new Date();
        var lastMonth = new Date();
        lastMonth.setDate(now.getDate() - 30);
        $(".start-time-text").val(lastMonth.format("yyyy-MM-dd"));
        $(".end-time-text").val(now.format("yyyy-MM-dd"));

        DisItem.container.find(".start-time-text").datepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".end-time-text").datepicker("option", "minDate", selectedDate);
                    }
                }}
        );
        DisItem.container.find(".end-time-text").datepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".start-time-text").datepicker("option", "maxDate", selectedDate);
                    }
                }}
        );
    },

    renderChart : function(data){
        var days = DisItem.Chart.genDays();
        var rates = DisItem.Chart.genRates(data);
        //        console.info(days);
        //        console.info(rates);

        var start = DisItem.Chart.parseDate($(".start-time-text").val());
        var end = DisItem.Chart.parseDate($(".end-time-text").val());

        var step = Math.round(days.length / 10);
        var minY = DisItem.Chart.smallest(rates) - 0.1;

        var chart = new Highcharts.Chart({
            chart : {
                renderTo : 'goodRate-charts',
                defaultSeriesType: 'line' //图表类型line(折线图)
            },
            credits : {
                enabled: false   //右下角不显示LOGO
            },
            title: {
                text: '宝贝促销效果分析'
            }, //图表标题
            xAxis: {  //x轴
                categories: days,   //['六天前', '五天前', '四天前', '三天前', '大前天',  '前天', '昨天'], //x轴标签名称
                title: '日期',
                gridLineWidth: 1, //设置网格宽度为1
                lineWidth: 2,  //基线宽度
                labels:{step: step, y:26}  //x轴标签位置：距X轴下方26像素
            },
            yAxis: [{  //y轴
                //                startOnTick: false,
                //                endOnTick: false,
                //                min: minY,
                title: {text: '销量'}, //标题
                lineWidth: 2 //基线宽度
            }, {
                //                min: minY,
                title: {text: '销量'}, //标题
                opposite: true,
                lineWidth: 2 //基线宽度
            }],
            plotOptions:{ //设置数据点
                line:{
                    dataLabels:{
                        enabled:false  //在数据点上显示对应的数据值
                    },
                    enableMouseTracking: true //取消鼠标滑向触发提示框
                }
            },
            tooltip: {
                useHTML: true,
                formatter: function () {                 //当鼠标悬置数据点时的格式化提示
                    return '<div style="line-height: 20px;">日期: <b>' + this.x + '</b>&nbsp;<br><b>' + this.series.name + ': </b><span style="color:red;">' + Highcharts.numberFormat(this.y, 0) + "件</span></div>";
                }
            },
            series: [
                {  //数据列
                    name: '销量',
                    data: rates,
                    yAxis:0
                }
            ]
        });
    },

    genRates : function(data) {
        var res = [];
        var start = DisItem.Chart.parseDate($(".start-time-text").val());
        var end = DisItem.Chart.parseDate($(".end-time-text").val());

        var k = 0;
        var tmp = 0;
        if(data && data.length > 0) {
            tmp = data[0].salesCount;
        }
        for(var i=0; start.getTime() <= end.getTime();i++){
            if(k < data.length){
                var d = new Date(data[k].ts).formatYMS();
                if(start.formatYMS() == d){
                    tmp = data[k++].salesCount;
                    res.push(tmp);
                    start.setDate(start.getDate() + 1);
                    continue;
                }
            }
            res.push(tmp);
            start.setDate(start.getDate() + 1);
        }
        return res;
    },

    genDays : function() {
        var days = [];
        var start = DisItem.Chart.parseDate($(".start-time-text").val());
        var end = DisItem.Chart.parseDate($(".end-time-text").val());

        for(var i=0; start.getTime() <= end.getTime();i++){
            days.push(start.format("MM/dd"));
            start.setDate(start.getDate() + 1);
        }
        return days;
    },

    smallest : function(array){
        return Math.min.apply( Math, array );
    },

    largest : function(array){
        return Math.max.apply( Math, array );
    },

    parseDate : function(str){
        str=str.split('-');
        var date = new Date(str[0], str[1]-1, str[2]);
        return date;
    }
}, DisItem.Chart);
