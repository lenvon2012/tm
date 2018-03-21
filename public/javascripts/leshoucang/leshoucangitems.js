var TM = TM || {};
((function ($, window) {
    TM.DptgHot = TM.DptgHot || {};

    var DptgHot = TM.DptgHot;

    DptgHot.init = DptgHot.init || {};
    DptgHot.init = $.extend({
        doInit: function(container,baidushoucang) {
            DptgHot.init.baidushoucang = baidushoucang;
            DptgHot.container = container;
            var html = DptgHot.init.createHtml();
            container.html(html);

            DptgHot.userInfo.show();
            DptgHot.userInfo.initCategory();

            container.find(".item-search-btn").click(function() {
                DptgHot.search.doSearch();
            });
            container.find(".item-title-input").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".item-search-btn").click();
                }
            });
            container.find(".category-select").change(function() {
                DptgHot.search.doSearch();
            });
            container.find(".status-select").change(function() {
                DptgHot.search.doSearch();
            });
            container.find(".sort-select").change(function() {
                DptgHot.search.doSearch();
            });

            container.find(".item-search-btn").click();

            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                container.find(".item-table").find(".item-check").attr("checked", isChecked);
            });

            var refreshCallback = function() {
                DptgHot.search.doSearch();
                DptgHot.userInfo.show();//刷新版本
            };
            TM.DptgHotBatchOp.submit.refreshCallback = refreshCallback;

            DptgHot.event.initBatchEvent();
        },
        createHtml: function() {
            var content;
            if(TM.DptgHot.init.baidushoucang) {
                content = "百 度 收 藏 管 理";
            }else {
                content = "热 销 推 荐 管 理";
            }
            var html = '' +
                '<div>' +
                '   <div class="new-tip-bar">'+content+'</div> ' +
                '';
            if(!TM.DptgHot.init.baidushoucang) {
                html += '' +
                    '   <div class="border-div" style="margin-top: 10px;">' +
                    '       <table class="base-info-table" style="margin-left: 20px;">' +
                    '           <tbody>' +
                    '           <tr>' +
                    '               <td class="tip-dot" style="width: 120px;">用户版本：</td> ' +
                    '               <td class="buy-version-td result-info" style="width: 260px;;"></td> ' +
                    '               <td class="tip-dot" style="width: 120px;">总推荐位：</td> ' +
                    '               <td style="width: 220px;"><span class="total-popularized-num result-info"></span>&nbsp;个</td> ' +
                    '           </tr>' +
                    '           <tr>' +
                    '               <td class="tip-dot">已用推荐位：</td>' +
                    '               <td><span  class="used-popularized-num result-info"></span>&nbsp;个</td> ' +
                    '               <td class="tip-dot">剩余推荐位：</td>' +
                    '               <td><span  class="remain-popularized-num result-info"></span>&nbsp;个</td> ' +
                    '           </tr>' +
                    '           </tbody>' +
                    '       </table> ' +
                    /*'       <table style="margin-left: 20px; margin-top: 10px;">' +
                     '           <tbody>' +
                     '               <tr>' +
                     '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn select-popularize-btn">选中使用推广</span> </td>' +
                     '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn select-cancel-btn">选中取消推广</span> </td>' +
                     '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn all-popularize-btn">全店智能推广</span> </td>' +
                     '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn all-cancel-btn">全店取消推广</span> </td>' +
                     '               </tr>' +
                     '           </tbody>' +
                     '       </table>' +*/
                    '   </div> ' +
                    '';
            }
            html += '' +
                '   <div class="border-div" style="margin-top: 10px;">' +
                '       <table style="margin-left: 20px;">' +
                '           <tbody>' +
                '           <tr>' +
                '               <td>宝贝类目：</td>' +
                '               <td>' +
                '                   <select class="category-select" style="border: #999999 1px solid; width: 120px;">' +
                '                       <option selected="selected" value="0">全部</option>' +
                '                   </select> ' +
                '               </td>' +

                '               <td style="padding-left: 20px;">排序：</td>' +
                '               <td>' +
                '                   <select class="sort-select" style="border: #999999 1px solid; width: 100px;">' +
                '                       <option selected="selected" value="3">默认排序</option>' +
                '                       <option value="3">销量升序</option>' +
                '                       <option value="4">销量降序</option>' +
                '                   </select> ' +
                '               </td>' +
                '           </tr>' +
                '           <tr>' +
                '               <td style="padding-top: 7px;">关键词：</td>' +
                '               <td style="padding-top: 7px;" colspan="4">' +
                '                   <input class="item-title-input" style="width: 97%; border: #999999 1px solid" />' +
                '               </td>' +
                '               <td style="padding-top: 4px;text-align: right;">' +
                '                   <span class="item-search-btn basebtn" style="font-weight: bold;font-size: 15px;">搜 索</span> ' +
                '               </td> ' +
                '           </tr>' +
                '           </tbody>' +
                '       </table>' +
                '   </div> ' +
                '   <div style="margin-top: 10px;">' +
                '       <div class="paging-div" style="text-align: center; margin-bottom: 5px;"></div> ' +
                '       <table class="item-table result-table">' +
                '           <thead>' +
                '           <tr>' +
                '               <td style="width: 10%;"><input type="checkbox" class="all-item-check" /> </td>' +
                '               <td style="width: 15%;">宝贝图片 </td>' +
                '               <td style="width: 35%;">宝贝标题 </td>' +
                '               <td style="width: 20%;">宝贝详情 </td>' +
                '               <td style="width: 20%;">操作 </td>' +
                '           </tr>' +
                '           </thead>' +
                '           <tbody>' +
                '           </tbody>' +
                '       </table>' +
                '       <div class="paging-div" style="text-align: center;margin-top: 5px;"></div> ' +
                '   </div> ' +
                '</div>' +
                '';

            return html;
        }
    }, DptgHot.init);

    DptgHot.userInfo = DptgHot.userInfo || {};
    DptgHot.userInfo = $.extend({
        show: function() {
            var data = {};
            $.ajax({
                url : '/popularize/getUserInfo',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;
                    var container = DptgHot.container;

                    container.find(".buy-version-td").html(userJson.version);
                    //userJson.award = true;
                    //if (userJson.award == true) {
                    //    container.find(".total-popularized-num").html(userJson.hotTotalNum + " + 1 (好评送推广)");
                    //    userJson.hotRemainNum++;
                    //} else {
                    container.find(".total-popularized-num").html(userJson.hotTotalNum);
                    //}

                    container.find(".used-popularized-num").html(userJson.hotUsedNum);
                    container.find(".remain-popularized-num").html(userJson.hotRemainNum);
                }
            });
        },
        initCategory: function() {
            $.ajax({
                url : '/items/sellerCatCount',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var categoryArray = dataJson;
                    if (categoryArray === undefined || categoryArray == null || categoryArray.length <= 0)
                        return;
                    $(categoryArray).each(function(index, categoryJson) {
                        if (categoryJson.count > 0) {
                            var option = '<option value="' + categoryJson.id + '" >' + categoryJson.name + '(' + categoryJson.count + ')</option>';
                            DptgHot.container.find(".category-select").append(option);
                        }

                    });
                }
            });
        }
    }, DptgHot.userInfo);


    DptgHot.search = DptgHot.search || {};
    DptgHot.search = $.extend({
        doSearch: function() {

            var ruleData = DptgHot.search.getSearchParams();
            DptgHot.search.doShow(1, ruleData);
        },
        doShow: function(currentPage, ruleData) {
            if (currentPage < 1)
                currentPage = 1;
            ruleData.popularizeStatus = 2;//热销推荐
            var tbodyObj = DptgHot.container.find(".item-table").find("tbody");
            DptgHot.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/popularize/searchItems',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = DptgHot.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }

            });

        },
        getSearchParams: function() {
            var ruleData = {};
            var title = DptgHot.container.find(".item-title-input").val();
            ruleData.s = title;
            var cid = DptgHot.container.find(".category-select").val();
            ruleData.catId = cid;
            ruleData.polularized = 2;
            var sort = DptgHot.container.find(".sort-select").val();
            ruleData.sort = sort;
            return ruleData;
        }
    }, DptgHot.search);


    DptgHot.row = DptgHot.row || {};
    DptgHot.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DptgHot.row.createHtml();
            var trObj = $(html);

            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }
            trObj.find(".item-check").attr("numIid", itemJson.numIid);
            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            trObj.find(".item-href").attr("href", href);
            trObj.find(".item-href").attr("target", "_blank");
            trObj.find(".item-img").attr("src", itemJson.picURL + "_190x190.jpg");
            trObj.find(".item-title").html(itemJson.title);
            trObj.find(".item-price").html(itemJson.price);
            /*trObj.find(".item-trade").html(itemJson.salesCount);*/


            var refreshCallback = function() {
                DptgHot.search.doSearch();
            };
            if (!DptgHot.init.baidushoucang && itemJson.popularized == true) {
                var html = '' +
                    '<div style="margin-bottom: 5px;"><a href="javascript:void(0);" target="_blank" class="show-popularize-link link-btn">查看推荐</a> </div> ' +
                    '<div><a href="javascript:void(0);" class="cancel-btn link-btn">取消推荐</a> </div> ' +
                    '';
                if (!TM.util.isNotTaobaoCeshi()) {
                    html += '<span class="analysis basebtn" style="margin-top: 5px;">推广效果分析</span>';
                }
                trObj.find(".op-td").html(html);

                var showHref = TM.DptgBase.util.getPopularizedUrl(itemJson);
                trObj.find(".show-popularize-link").attr("href", showHref);
                trObj.find(".cancel-btn").click(function() {
                    var numIidArray = [];
                    numIidArray[0] = itemJson.numIid;
                    TM.DptgHotBatchOp.submit.batchCancel(numIidArray);
                });

            } else {
                var html = '';
                if(DptgHot.init.baidushoucang){
                    html = '' +
                        '<span class="baidushoucang-btn basebtn" style="margin-top: 10px;">百度收藏</span>' +
                        '';
                } else {
                    html = '' +
                        '<span class="popularized-btn basebtn">立即推荐</span>' +
                        '';
                    if (!TM.util.isNotTaobaoCeshi()) {
                        html += '<span class="analysis basebtn" style="margin-top: 5px;">推广效果分析</span>';
                    }
                }
                trObj.find(".op-td").html(html);

                trObj.find(".popularized-btn").click(function() {
                    var numIidArray = [];
                    numIidArray[0] = itemJson.numIid;
                    TM.DptgHotBatchOp.submit.batchPopularized(numIidArray);
                });
                trObj.find(".baidushoucang-btn").click(function() {
                    TM.shoucang.addToBaidu.add({
                        title : itemJson.title,
                        url : href,
                        content : itemJson.title
                    });
                });
            }

            trObj.find('.analysis').click(function(){
                $.post("/Dianputuiguang/analysisItem",{numIid:numIid},function(data){
                    if(data == undefined || data == null) {
                        TM.Alert.load("获取数据出错~");
                        return;
                    }
                    var pvArr = [], uvArr = [], date = [];
                    if(data.length == 0){
                        TM.Alert.load("亲订购未满一天，暂无数据哦");
                        return;
                    }
                    if(data.length > 0){
                        $(data).each(function(i, result){
                            pvArr.push(result.pv);
                            uvArr.push(result.uv);
                            date.push(result.date);
                        });
                    }
                    var char = $('#analysisDivTmpl').clone();
                    char.attr('id','analysisDiv');
                    char.show();
                    TM.Alert.loadDetail(char, 650, 520, function(){

                    },"推广效果分析");
                    chart = new Highcharts.Chart({
                        chart : {
                            renderTo : 'analysisDiv',
                            defaultSeriesType: 'line' //图表类型line(折线图)
                        },
                        credits : {
                            enabled: false   //右下角不显示LOGO
                        },
                        title: {
                            text: '推广效果分析'
                        }, //图表标题
                        xAxis: {  //x轴
                            categories: date, //x轴标签名称
                            gridLineWidth: 1, //设置网格宽度为1
                            lineWidth: 2,  //基线宽度
                            labels:{y:26}  //x轴标签位置：距X轴下方26像素
                        },
                        yAxis: [/*{  //y轴
                         title: {text: '浏览量'}, //标题
                         lineWidth: 2 //基线宽度
                         }, */{
                            title: {text: '访客数'}, //标题
                            lineWidth: 2 //基线宽度
                        }],
                        plotOptions:{ //设置数据点
                            line:{
                                dataLabels:{
                                    enabled:true  //在数据点上显示对应的数据值
                                },
                                enableMouseTracking: true //取消鼠标滑向触发提示框
                            }
                        },
                        series: [
                            /*{  //数据列
                             name: '浏览量',
                             data: pvArr,
                             yAxis:0
                             },*/
                            {  //数据列
                                name: '访客数',
                                data: uvArr,
                                yAxis:0
                            }
                        ]
                    });

                });
            });

            trObj.find(".item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var allCheckObj = DptgHot.container.find(".all-item-check");
                if (isChecked == false) {
                    allCheckObj.attr("checked", false);
                } else {
                    var checkedNum = DptgHot.container.find(".item-table").find(".item-check:checked").length;
                    var allNum = DptgHot.container.find(".item-table").find(".item-check").length;
                    if (checkedNum == allNum) {
                        allCheckObj.attr("checked", true);
                    }
                }

            });

            return trObj;
        },
        createHtml: function(itemJson) {
            var html = '' +
                '<tr>' +
                '   <td class="result-td"><input type="checkbox" class="item-check" /> </td>' +
                '   <td class="result-td"><a class="item-href"><img class="item-img" style="width: 90px; height: 90px;"/></a> </td>' +
                '   <td class="result-td"><a class="item-href item-link item-title"></a></td>' +
                '   <td class="result-td">' +
                '       <table style="margin: 0 auto;">' +
                '           <tbody>' +
                '               <tr>' +
                '                   <td>价格：</td>' +
                '                   <td><span class="item-price" style="color: #FF4400;"></span>&nbsp;元</td>' +
                '               </tr>' +
                /*'               <tr>' +
                 '                   <td>销量：</td>' +
                 '                   <td><span class="item-trade" style="color: #FF4400;"></span>&nbsp;件</td>' +
                 '               </tr>' +*/
                '           </tbody>' +
                '       </table>' +
                '   </td class="result-td">' +
                '   <td class="result-td op-td">' +
                '       ' +
                '   </td> ' +
                '</tr>' +
                '';
            return html;
        }
    }, DptgHot.row);



    DptgHot.event = DptgHot.event || {};
    DptgHot.event = $.extend({
        initBatchEvent: function() {
            var container = DptgHot.container;

            var getCheckedNumIidArray = function() {
                var numIidArray = [];
                var checkedObj = container.find(".item-check:checked");
                checkedObj.each(function() {
                    numIidArray[numIidArray.length] = $(this).attr("numIid");
                });

                return numIidArray;
            };

            container.find(".select-popularize-btn").click(function() {
                var numIidArray = getCheckedNumIidArray();
                TM.DptgHotBatchOp.submit.batchPopularized(numIidArray);
            });
            container.find(".select-cancel-btn").click(function() {
                var numIidArray = getCheckedNumIidArray();
                TM.DptgHotBatchOp.submit.batchCancel(numIidArray);
            });
            /*container.find(".all-popularize-btn").click(function() {
             TM.DptgHotBatchOp.submit.shopPopularized();
             });
             container.find(".all-cancel-btn").click(function() {
             TM.DptgHotBatchOp.submit.shopCancel();
             });*/
        }
    }, DptgHot.event);




})(jQuery,window));


((function ($, window) {
    TM.DptgHotBatchOp = TM.DptgHotBatchOp || {};

    var DptgHotBatchOp = TM.DptgHotBatchOp;

    DptgHotBatchOp.submit = DptgHotBatchOp.submit || {};
    DptgHotBatchOp.submit = $.extend({
        refreshCallback: function() {

        },
        batchPopularized: function(numIidArray) {
            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert("请先选择要热销推荐的宝贝");
                return;
            }
            $.ajax({
                url : '/popularize/getUserInfo',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;

                    //if (userJson.award == true) {
                    //    userJson.remainNum++;
                    //}
                    var remainNum = userJson.hotRemainNum;
                    var length = numIidArray.length;
                    if (remainNum <= 0) {
                        alert("您剩余的热销推荐个数为0！");
                        return;
                    } else if (remainNum < length) {
                        alert("您选择的宝贝数大于可推广的热销推荐数！");
                        return;
                    } else {
                        var data = {};
                        data.numIids = numIidArray.join(",");
                        data.status = 2;//热销推荐
                        //alert(data.numIids);

                        $.ajax({
                            url : '/Popularize/addPopularized',
                            data : data,
                            type : 'post',
                            success : function(dataJson) {
                                alert("热销推荐成功！");
                                DptgHotBatchOp.submit.refreshCallback();
                            }
                        });
                    }
                }
            });
        },
        batchCancel: function(numIidArray) {
            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert("请先选择要取消热销推荐的宝贝");
                return;
            }
            if (confirm("确定要取消热销推荐？") == false)
                return;
            var data = {};
            data.numIids = numIidArray.join(",");
            data.status = 2;//热销推荐
            $.ajax({
                url : '/Popularize/removePopularized',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    alert("取消热销推荐成功！");
                    DptgHotBatchOp.submit.refreshCallback();
                }
            });
        }
    }, DptgHotBatchOp.submit);

})(jQuery,window));