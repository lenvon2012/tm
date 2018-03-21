var TM = TM || {};
((function ($, window) {
    TM.WhiteList = TM.WhiteList || {};

    var WhiteList = TM.WhiteList;

    /**
     * 初始化
     * @type {*}
     */
    WhiteList.init = WhiteList.init || {};
    WhiteList.init = $.extend({
        doInit: function(container) {
            var html = WhiteList.init.createHtml();
            container.html(html);
            WhiteList.container = container;

            $.ajax({
                url : '/skinwhitelist/querySkinWhiteExplain',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    var explain = dataJson.res;
                    container.find(".explain-text").val(explain);
                }
            });

            WhiteList.container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    WhiteList.container.find(".search-btn").click();
                }
            });
            WhiteList.container.find(".search-btn").click(function() {
                WhiteList.show.doShow();
            });

            WhiteList.container.find(".submit-explain-btn").click(function() {
                WhiteList.submit.doSaveExplain();
            });
            WhiteList.container.find(".add-black-btn").click(function() {
                WhiteList.submit.doAddWhiteList();
            });
            WhiteList.container.find(".check-all-black").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = WhiteList.container.find(".whitelist-check");
                checkObjs.attr("checked", isChecked);
            });
            WhiteList.container.find(".delete-checked-black-btn").click(function() {
                var checkObjs = WhiteList.container.find(".whitelist-check:checked");
                var whiteIdList = [];
                var blackJson = {};
                checkObjs.each(function(index, checkObj) {
                    whiteIdList[whiteIdList.length] = checkObj.blackJson.id;
                    if (index == 0) {
                        blackJson = checkObj.blackJson;
                    }
                });
                WhiteList.submit.doDeleteBlacks(whiteIdList, blackJson);
            });

            //WhiteList.init.initSwitchOp();

            WhiteList.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<table>' +
                '   <tbody>' +
                '   <tr>' +
                '       <td><div class="whitelist-tab"></div> </td>' +
                '       <td><div class="switch-defense-div"></div></td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table>' +
                '<div class="blacklist-tab" style="">添加白名单：</div> ' +
                '<div class="blacklist-table-div">' +
                '   <table class="add-black-table skincomment-table" style="width: 100%;">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 300px; text-align: center;">*添加多个买家昵称，一行一个</td>' +
                '           <td style="width: 300px;">设置备注（可不填）</td> ' +
                '           <td>操作</td> ' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td>' +
                //'               <div class="add-tip">添加多个买家昵称，一行一个</div>' +
                '               <textarea rows="7" cols="30" class="add-black-text"></textarea>' +
                '           </td>' +
                '           <td>' +
                '               <div style="color: #a10000;width: 180px;margin: 0 auto;">记录设置白名单的原因，如：该买家给过好评</div> ' +
                '               <input type="text" class="add-black-remark-text" />' +
                '           </td>' +
                '           <td><span class="add-black-btn commbutton btntext4 ">添加白名单</span> </td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div>' +
                '<div class="blacklist-tab" style="margin-top: 20px;">白名单列表：</div> ' +
                '<div class="blacklist-table-div">' +
                '   <table width="100%">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td width="20%"><span class="delete-checked-black-btn commbutton btntext4 ">删除选中</span> </td> ' +
                '           <td width="80%" style="text-align:right">请输入买家昵称：<input class="search-text" /><span class="search-btn commbutton btntext4 ">搜索白名单</span></td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div>' +
                '<div class="blacklist-table-div">' +
                '   <div class="paging-div"></div>' +
                '   <table class="blacklist-table skincomment-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 10%;"><input class="check-all-black" type="checkbox" /> </td> ' +
                '           <td style="width: 20%;">买家昵称</td>' +
                '           <td style="width: 19%;">备注</td>' +
                '           <td style="width: 17%;">添加时间</td>' +
                '           <td style="width: 12%;">修改备注</td>' +
                '           <td style="width: 12%;">删除白名单</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table> ' +
                '   <div class="paging-div"></div>' +
                '</div> ' +
                '';

            return html;

        },
        initSwitchOp: function() {
            $.ajax({
                url : '/skinwhitelist/isOn',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    var isOn = dataJson.res;
                    var switchStatus = TM.Switch.createSwitch.createSwitchForm("白名单开启状态");
                    switchStatus.appendTo(WhiteList.container.find(".switch-defense-div"));
                    switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
                        labels:['已开启','已关闭'],
                        doChange:function(isCurrentOn){
                            if (isCurrentOn == false) {//要开启
                                $.ajax({
                                    url : '/skinwhitelist/turnOn',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                                            return;
                                    }
                                });
                            } else if (isCurrentOn == true) {//要关闭
                                $.ajax({
                                    url : '/skinwhitelist/turnOff',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                                            return;
                                    }
                                });
                            }
                            return true;
                        },
                        isOn : isOn
                    });
                }
            });
        }
    }, WhiteList.init);

    WhiteList.show = WhiteList.show || {};
    WhiteList.show = $.extend({
        doShow: function() {
            var ruleData = WhiteList.show.getQueryRule();
            var tbodyObj = WhiteList.container.find(".blacklist-table").find("tbody");
            //tbodyObj.html("");
            WhiteList.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/skinwhitelist/queryWhiteListBuyers',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var blackListArray = dataJson.res;
                        WhiteList.container.find(".check-all-black").attr("checked", false);
                        $(blackListArray).each(function(index, blackJson) {
                            var trObj = WhiteList.row.createRow(index, blackJson);
                            tbodyObj.append(trObj);
                        });
                        
                        if (blackListArray == undefined || blackListArray == null || blackListArray.length == 0) {
                            tbodyObj.html('<tr><td colspan="6" height="40px">亲，暂未添加白名单！</td></tr>');
                        }
                    }
                }

            });
        },
        refresh: function() {
            WhiteList.show.doShow();
        },
        getQueryRule: function() {
            var ruleData = {};
            var buyerName = WhiteList.container.find(".search-text").val();
            ruleData.buyerName = buyerName;
            return ruleData;
        }
    }, WhiteList.show);


    WhiteList.row = WhiteList.row || {};
    WhiteList.row = $.extend({
        createRow: function(index, blackJson) {
            var html = '' +
                '<tr>' +
                '   <td><input type="checkbox" class="whitelist-check" /></td>' +
                '   <td><span class="whitelist-buyer-name whitelist-check-span"></span></td>' +
                '   <td><span class="whitelist-remark"></span></td>' +
                '   <td><span class="whitelist-time"></span></td>' +
                '   <td><span class="modify-remark-btn commbutton btntext4">修改备注</span></td>' +
                '   <td><span class="delete-whitelist-btn commbutton btntext4">删除白名单</span></td>' +
                '</tr>' +
                '';

            var trObj = $(html);
            var blackCheckObj = trObj.find(".whitelist-check");
            blackCheckObj.attr("whiteId", blackJson.id);
            blackCheckObj.each(function() {
                this.blackJson = blackJson;
            });

            trObj.find(".whitelist-buyer-name").html(blackJson.buyerName);
            trObj.find(".whitelist-remark").html(blackJson.remark);
            var theDate = new Date();
            theDate.setTime(blackJson.ts);
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
            trObj.find(".whitelist-time").html(timeStr);


            var checkCallback = function(checkObj) {
                var isChecked = checkObj.is(":checked");
                if (isChecked == false) {
                    WhiteList.container.find(".check-all-black").attr("checked", false);
                } else {
                    var checkObjs = WhiteList.container.find(".whitelist-check");
                    var flag = true;
                    checkObjs.each(function() {
                        if ($(this).is(":checked") == false)
                            flag = false;
                    });
                    WhiteList.container.find(".check-all-black").attr("checked", flag);
                }
            }
            trObj.find(".whitelist-check").click(function() {
                checkCallback($(this));
            });
            trObj.find(".whitelist-check-span").click(function() {
                var checkObj = $(this).parents("tr").find(".whitelist-check");
                var isChecked = checkObj.is(":checked");
                if (isChecked == true)
                    checkObj.attr("checked", false);
                else
                    checkObj.attr("checked", true);
                checkCallback(checkObj);
            });


            trObj.find(".modify-remark-btn").click(function() {
                WhiteList.submit.modifyRemarkParams.whiteId = blackJson.id;
                WhiteList.submit.modifyRemarkParams.blackJson = blackJson;
                WhiteList.submit.doModifyRemark();
            });
            trObj.find(".delete-whitelist-btn").click(function() {
                var whiteIdList = [];
                whiteIdList[whiteIdList.length] = blackJson.id;
                WhiteList.submit.doDeleteBlacks(whiteIdList, blackJson);
            });

            return trObj;
        }
    }, WhiteList.row);


    WhiteList.submit = WhiteList.submit || {};
    WhiteList.submit = $.extend({
        doSaveExplain: function() {
            var explain = WhiteList.container.find(".explain-text").val();
            if (explain == "") {
                alert("请先输入关闭交易的解释");
                return;
            }
            if (confirm("确定要修改关闭交易的解释") == false)
                return;
            var data = {};
            data.explain = explain;
            $.ajax({
                url : '/skinwhitelist/submitExplain',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;

                }
            });
        },
        doAddWhiteList: function() {
            var buyerNames = WhiteList.container.find(".add-black-text").val();
            if (buyerNames == "") {
                alert("请先输入白名单买家的昵称");
                return;
            }
            var remark = WhiteList.container.find(".add-black-remark-text").val();
            if (confirm("确定保存这些白名单？") == false)
                return;

            var data = {};
            data.buyerNames = buyerNames;
            data.remark = remark;
            $.ajax({
                url : '/skinwhitelist/addWhiteList',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    //刷新白名单列表
                    WhiteList.show.refresh();
                }
            });
        },
        modifyRemarkParams: {
            whiteId: 0,
            blackJson: {}
        },
        doModifyRemark: function() {
            var dialogObj = $(".modify-remark-dialog");

            if (dialogObj.length == 0) {
                var html = '' +
                    '<div class="modify-remark-dialog">' +
                    '   <span>请先输入要修改的备注：</span>' +
                    '   <input type="text" class="new-remark-text" /> ' +
                    '</div> ' +
                    '';
                dialogObj = $(html);
                $("body").append(dialogObj);
                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:200,
                    width:450,
                    title:'修改白名单备注',
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var whiteId = WhiteList.submit.modifyRemarkParams.whiteId;
                        var blackJson = WhiteList.submit.modifyRemarkParams.blackJson;
                        var remark = dialogObj.find(".new-remark-text").val();
                        if (remark == "") {
                            alert("请先输入要修改的备注");
                            return;
                        }
                        if (confirm("确定修改" + blackJson.buyerName + "的备注？") == false) {
                            //dialogObj.dialog('close');
                            return;
                        }
                        var data = {};
                        data.whiteId = whiteId;
                        data.remark = remark;
                        $.ajax({
                            url : '/skinwhitelist/modifyRemark',
                            data : data,
                            type : 'post',
                            success : function(dataJson) {
                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                                    return;
                                //刷新白名单列表
                                dialogObj.dialog('close');
                                WhiteList.show.refresh();
                            }
                        });


                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.find(".new-remark-text").val(WhiteList.submit.modifyRemarkParams.blackJson.remark);
            dialogObj.dialog("open");
        },
        doDeleteBlacks: function(whiteIdList, blackJson) {
            if (whiteIdList === undefined || whiteIdList == null || whiteIdList.length == 0) {
                alert("请先选择要删除的白名单");
                return;
            }
            if (whiteIdList.length == 1) {
                if (confirm("确定要删除白名单：" + blackJson.buyerName + "?") == false) {
                    return;
                }
            } else {
                if (confirm("确定要删除" + whiteIdList.length + "个白名单?") == false) {
                    return;
                }
            }
            var data = {};
            data.whiteIdList = whiteIdList;
            $.ajax({
                url : '/skinwhitelist/deleteWhiteList',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    //刷新白名单列表
                    WhiteList.show.refresh();
                }
            });
        }
    }, WhiteList.submit);

})(jQuery,window));