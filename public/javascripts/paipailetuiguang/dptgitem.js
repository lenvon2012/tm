var TM = TM || {};
((function ($, window) {
    TM.DptgItem = TM.DptgItem || {};

    var DptgItem = TM.DptgItem;

    DptgItem.init = DptgItem.init || {};
    DptgItem.init = $.extend({
        doInit: function(container) {
            var callback = function() {
                DptgItem.container = container;
                var html = DptgItem.init.createHtml();
                container.html(html);

                if (DptgItem.isTryVersion == true) {
                    container.find(".shop-cancel-popularize-td").remove();
                    container.find(".select-cancel-btn").parent().remove();
                    DptgItem.stop.doInit();
                } else {
                    container.find(".shop-stop-popularize-td").remove();
                }


                DptgItem.userInfo.show();
                DptgItem.userInfo.initCategory();

                container.find(".item-search-btn").click(function() {
                    DptgItem.search.doSearch();
                });
                container.find(".item-title-input").keydown(function(event) {
                    if (event.keyCode == 13) {//按回车
                        container.find(".item-search-btn").click();
                    }
                });
                container.find(".category-select").change(function() {
                    DptgItem.search.doSearch();
                });
                container.find(".status-select").change(function() {
                    DptgItem.search.doSearch();
                });
                container.find(".sort-select").change(function() {
                    DptgItem.search.doSearch();
                });

                container.find(".item-search-btn").click();

                container.find(".all-item-check").click(function() {
                    var isChecked = $(this).is(":checked");
                    container.find(".item-table").find(".item-check").attr("checked", isChecked);
                });

                var refreshCallback = function() {
                    DptgItem.search.doSearch();
                    DptgItem.userInfo.show();//刷新版本
                };
                TM.DptgBatchOp.submit.refreshCallback = refreshCallback;

                DptgItem.event.initBatchEvent();
            };

            $.ajax({
                url : '/PaiPaiPromote/getUserInfo',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;
                    if (userJson.level <= -1) {
                        DptgItem.isTryVersion = true;
                    } else {
                        DptgItem.isTryVersion = false;
                    }

                    callback();
                }
            });
        },
        createHtml: function() {
            var html = '' +
                '<div>' +
                '   <div class="new-tip-bar">宝 贝 推 广 管 理</div> ' +
                '   <div class="border-div" style="margin-top: 10px;">' +
                '       <table class="base-info-table" style="margin-left: 20px;">' +
                '           <tbody>' +
                '           <tr>' +
                '               <td class="tip-dot" style="width: 120px;">用户版本：</td> ' +
                '               <td class="buy-version-td result-info" style="width: 120px;;"></td> ' +
                '               <td class="tip-dot" style="width: 120px;">总推广位：</td> ' +
                '               <td style="width: 220px;"><span class="total-popularized-num result-info"></span>&nbsp;个</td> ' +
                '           </tr>' +
                '           <tr>' +
                '               <td class="tip-dot">已用推广位：</td>' +
                '               <td><span  class="used-popularized-num result-info"></span>&nbsp;个</td> ' +
                '               <td class="tip-dot">剩余推广位：</td>' +
                '               <td><span  class="remain-popularized-num result-info"></span>&nbsp;个</td> ' +
                '           </tr>' +
                '           </tbody>' +
                '       </table> ' +
                '       <table style="margin-left: 20px; margin-top: 10px;">' +
                '           <tbody>' +
                '               <tr>' +
                '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn select-popularize-btn">选中使用推广</span> </td>' +
                '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn select-cancel-btn">选中取消推广</span> </td>' +
                '                   <td style="padding-right: 10px;"><span class="basebtn long-basebtn all-popularize-btn">全店智能推广</span> </td>' +
                '                   <td style="padding-right: 10px;" class="shop-cancel-popularize-td"><span class="basebtn long-basebtn all-cancel-btn">全店取消推广</span> </td>' +
                '                   <td style="padding-right: 10px;" class="shop-stop-popularize-td"><span class="basebtn long-basebtn all-stop-btn">全店暂停推广</span> </td>' +
                '               </tr>' +
                '           </tbody>' +
                '       </table>' +
                '   </div> ' +
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
                '               <td style="padding-left: 20px;">状态：</td>' +
                '               <td>' +
                '                   <select class="status-select" style="border: #999999 1px solid; width: 100px;">' +
                '                       <option selected="selected" value="2">全部</option>' +
                '                       <option value="0">已推广</option>' +
                '                       <option value="1">未推广</option>' +
                '                   </select> ' +
                '               </td>' +
                '               <td style="padding-left: 20px;">排序：</td>' +
                '               <td>' +
                '                   <select class="sort-select" style="border: #999999 1px solid; width: 100px;">' +
                '                       <option selected="selected" value="3">默认排序</option>' +
                '                       <option value="5">价格升序</option>' +
                '                       <option value="6">价格降序</option>' +
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
    }, DptgItem.init);

    DptgItem.stop = DptgItem.stop || {};
    DptgItem.stop = $.extend({
        doInit: function() {
            //暂停按钮
            DptgItem.container.find(".all-stop-btn").click(function() {
                var btnObj = $(this);
                var data = {};
                if (btnObj.hasClass("popularized-on")) {
                    $.ajax({
                        url : '/PaiPaiPromote/setPopularOff',
                        data : data,
                        type : 'post',
                        success : function(dataJson) {
                            alert("全店暂停推广成功");
                        }
                    });
                    btnObj.removeClass("popularized-on");
                    btnObj.html("全店开启推广");
                } else {
                    $.ajax({
                        url : '/PaiPaiPromote/setPopularOn',
                        data : data,
                        type : 'post',
                        success : function(dataJson) {
                            alert("全店开启推广成功");
                        }
                    });
                    btnObj.addClass("popularized-on");
                    btnObj.html("全店暂停推广");
                }
            });
        }
    }, DptgItem.stop);

    DptgItem.userInfo = DptgItem.userInfo || {};
    DptgItem.userInfo = $.extend({
        show: function() {
            var data = {};
            $.ajax({
                url : '/PaiPaiPromote/getUserInfo',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;
                    var container = DptgItem.container;

                    container.find(".buy-version-td").html(userJson.version);
                    //userJson.award = true;
                    if (userJson.award == true) {
                        container.find(".total-popularized-num").html(userJson.totalNum + " + 1 (好评送推广)");
                        userJson.remainNum++;
                    } else {
                        container.find(".total-popularized-num").html(userJson.totalNum);
                    }

                    container.find(".used-popularized-num").html(userJson.popularizedNum);
                    container.find(".remain-popularized-num").html(userJson.remainNum);

                    if (DptgItem.isTryVersion == true) {
                        if (userJson.isPopularOn == true) {
                            container.find(".all-stop-btn").addClass("popularized-on");
                            container.find(".all-stop-btn").html("全店暂停推广");
                        } else {
                            container.find(".all-stop-btn").removeClass("popularized-on");
                            container.find(".all-stop-btn").html("全店开启推广");
                        }
                        if (userJson.remainNum <= 0) {
                            container.find(".select-popularize-btn").parent().remove();
                            container.find(".all-popularize-btn").parent().remove();
                        }
                    }

                }
            });
        },
        initCategory: function() {
            $.ajax({
                url : '/PaiPaiItems/sellerCatCount',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var categoryArray = dataJson;
                    if (categoryArray === undefined || categoryArray == null || categoryArray.length <= 0)
                        return;
                    $(categoryArray).each(function(index, categoryJson) {
                        if (categoryJson.count > 0) {
                            var option = '<option value="' + categoryJson.id + '" >' + categoryJson.name + '(' + categoryJson.count + ')</option>';
                            DptgItem.container.find(".category-select").append(option);
                        }

                    });
                }
            });
        }
    }, DptgItem.userInfo);


    DptgItem.search = DptgItem.search || {};
    DptgItem.search = $.extend({
        doSearch: function() {

            var ruleData = DptgItem.search.getSearchParams();
            DptgItem.search.doShow(1, ruleData);
        },
        doShow: function(currentPage, ruleData) {
            if (currentPage < 1)
                currentPage = 1;
            ruleData.popularizeStatus = 5;
            var tbodyObj = DptgItem.container.find(".item-table").find("tbody");
            DptgItem.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/PaiPaiPromote/searchItems',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = DptgItem.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }

            });

        },
        getSearchParams: function() {
            var ruleData = {};
            var title = DptgItem.container.find(".item-title-input").val();
            ruleData.s = title;
            var cid = DptgItem.container.find(".category-select").val();
            ruleData.catId = cid;
            var status = DptgItem.container.find(".status-select").val();
            ruleData.polularized = status;
            var sort = DptgItem.container.find(".sort-select").val();
            ruleData.sort = sort;
            return ruleData;
        }
    }, DptgItem.search);


    DptgItem.row = DptgItem.row || {};
    DptgItem.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DptgItem.row.createHtml();
            var trObj = $(html);

            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null) {
                numIid = itemJson.itemCode;
                itemJson.numIid = numIid;
            }
            trObj.find(".item-check").attr("numIid", itemJson.numIid);
            var href = "http://auction2.paipai.com/" + itemJson.numIid;
            trObj.find(".item-href").attr("href", href);
            trObj.find(".item-href").attr("target", "_blank");
            trObj.find(".item-img").attr("src", itemJson.picURL);
            trObj.find(".item-title").html(itemJson.title);
            trObj.find(".item-price").html(itemJson.itemPrice/100);
            //trObj.find(".item-trade").html(itemJson.salesCount);


            var refreshCallback = function() {
                DptgItem.search.doSearch();
            };
            if (itemJson.popularized == true) {
                var html = '' +
                    '<div style="margin-bottom: 5px;"><a href="javascript:void(0);" target="_blank" class="show-popularize-link link-btn">查看推广</a> </div> ' +
                    '<div class="cancel-popularize-div"><a href="javascript:void(0);" class="cancel-btn link-btn">取消推广</a> </div> ' +
                    '';
                trObj.find(".op-td").html(html);
                if (DptgItem.isTryVersion == true) {
                    trObj.find(".cancel-popularize-div").remove();
                }
                var showHref = TM.DptgBase.util.getPopularizedUrl(itemJson);
                if (DptgItem.isTryVersion == true) {
                    showHref = "/tryshare";
                }
                trObj.find(".show-popularize-link").attr("href", showHref);
                trObj.find(".cancel-btn").click(function() {
                    var numIidArray = [];
                    numIidArray[0] = itemJson.numIid;
                    TM.DptgBatchOp.submit.batchCancel(numIidArray);
                });

            } else {
                var html = '' +
                    '<span class="popularized-btn basebtn">立即推广</span>' +
                    //'<span class="baidushoucang-btn basebtn" style="margin-top: 10px;">百度收藏</span>' +
                    '';
                trObj.find(".op-td").html(html);

                trObj.find(".popularized-btn").click(function() {
                    var numIidArray = [];
                    numIidArray[0] = itemJson.numIid;
                    TM.DptgBatchOp.submit.batchPopularized(numIidArray);
                });
                trObj.find(".baidushoucang-btn").click(function() {
                    TM.shoucang.addToBaidu.add({
                        title : itemJson.title,
                        url : href,
                        content : itemJson.title
                    });
                });
            }


            trObj.find(".item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var allCheckObj = DptgItem.container.find(".all-item-check");
                if (isChecked == false) {
                    allCheckObj.attr("checked", false);
                } else {
                    var checkedNum = DptgItem.container.find(".item-table").find(".item-check:checked").length;
                    var allNum = DptgItem.container.find(".item-table").find(".item-check").length;
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
    }, DptgItem.row);



    DptgItem.event = DptgItem.event || {};
    DptgItem.event = $.extend({
        initBatchEvent: function() {
            var container = DptgItem.container;

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
                TM.DptgBatchOp.submit.batchPopularized(numIidArray);
            });
            container.find(".select-cancel-btn").click(function() {
                var numIidArray = getCheckedNumIidArray();
                TM.DptgBatchOp.submit.batchCancel(numIidArray);
            });
            container.find(".all-popularize-btn").click(function() {
                TM.DptgBatchOp.submit.shopPopularized();
            });
            container.find(".all-cancel-btn").click(function() {
                TM.DptgBatchOp.submit.shopCancel();
            });

        }
    }, DptgItem.event);




})(jQuery,window));


((function ($, window) {
    TM.DptgBatchOp = TM.DptgBatchOp || {};

    var DptgBatchOp = TM.DptgBatchOp;

    DptgBatchOp.submit = DptgBatchOp.submit || {};
    DptgBatchOp.submit = $.extend({
        refreshCallback: function() {

        },
        batchPopularized: function(numIidArray) {
            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert("请先选择要推广的宝贝");
                return;
            }
            $.ajax({
                url : '/PaiPaiPromote/getUserInfo',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var userJson = dataJson;

                    if (userJson.award == true) {
                        userJson.remainNum++;
                    }
                    var remainNum = userJson.remainNum;
                    var length = numIidArray.length;
                    if (remainNum <= 0) {
                        alert("您剩余的推广位个数为0，您可以先升级订购版本！");
                        return;
                    } else if (remainNum < length) {
                        alert("您选择的宝贝数大于可推广的宝贝数，您可以升级订购版本！");
                        return;
                    } else {
                        var data = {};
                        data.numIids = numIidArray.join(",");
                        //alert(data.numIids);
                        if (TM.DptgItem.isTryVersion == true) {//体验版
                            data.status = 4;
                            if (confirm("亲，体验版的推广位开启后不能更换宝贝，您确定要推广该宝贝吗？") == false)
                                return;
                        }
                        $.ajax({
                            url : '/PaiPaiPromote/addPopularized',
                            data : data,
                            type : 'post',
                            success : function(dataJson) {
                                alert("宝贝推广成功！");
                                DptgBatchOp.submit.refreshCallback();
                            }
                        });
                    }
                }
            });
        },
        batchCancel: function(numIidArray) {
            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert("请先选择要取消推广的宝贝");
                return;
            }
            if (confirm("确定要取消宝贝推广？") == false)
                return;
            var data = {};
            data.numIids = numIidArray.join(",");
            data.status = 5;
            $.ajax({
                url : '/PaiPaiPromote/removePopularized',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    alert("取消推广宝贝成功！");
                    DptgBatchOp.submit.refreshCallback();
                }
            });
        },
        shopPopularized: function() {
            var data = {};
            if (TM.DptgItem.isTryVersion == true) {//体验版
                data.status = 4;
                if (confirm("亲，体验版的推广位开启后不能更换宝贝，您确定要全店推广吗？") == false)
                    return;
            }
            $.ajax({
                url : '/PaiPaiPromote/addPopularizedAll',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        alert("全店智能推广失败，您剩余的推广位为0，或者您所有宝贝都已推广！");
                        return;
                    }

                    alert("全店智能推广成功");
                    DptgBatchOp.submit.refreshCallback();
                }
            });
        },
        shopCancel: function() {
            if (confirm("确定要全店取消宝贝推广？") == false)
                return;
            var data = {};
            data.status = 5;
            $.ajax({
                url : '/PaiPaiPromote/removePopularizedAll',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    alert("全店取消推广成功");
                    DptgBatchOp.submit.refreshCallback();
                }
            });
        }
    }, DptgBatchOp.submit);

})(jQuery,window));