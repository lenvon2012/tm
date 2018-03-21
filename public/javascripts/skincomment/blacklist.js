var TM = TM || {};
((function ($, window) {
    TM.BlackList = TM.BlackList || {};

    var BlackList = TM.BlackList;

    /**
     * 初始化
     * @type {*}
     */
    BlackList.init = BlackList.init || {};
    BlackList.init = $.extend({
        doInit: function(container) {
            var html = BlackList.init.createHtml();
            container.html(html);
            BlackList.container = container;

            $.ajax({
                url : '/skinblacklist/querySkinBlackExplain',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false){
                        return;
                    }
                    var explain = dataJson.res;
                    container.find(".explain-text").val(explain);
                    if (dataJson.autoChapingBlackList == true) {
                    	container.find("#autoChapingBlackListOn").attr("checked",true);
                    } else {
                    	container.find("#autoChapingBlackListOff").attr("checked",true);
                    }
                    if (dataJson.autoRefundBlackList == true) {
                    	container.find("#autoRefundBlackListOn").attr("checked",true);
                    } else {
                    	container.find("#autoRefundBlackListOff").attr("checked",true);
                    }
                }
            });

            BlackList.container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    BlackList.container.find(".search-btn").click();
                }
            });
            BlackList.container.find(".search-btn").click(function() {
                BlackList.show.doShow();
            });

            BlackList.container.find(".submit-explain-btn").click(function() {
                return BlackList.submit.doSaveExplain();
            });
            BlackList.container.find(".add-black-btn").click(function() {
                BlackList.submit.doAddBlackList();
            });
            BlackList.container.find(".check-all-black").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = BlackList.container.find(".blacklist-check");
                checkObjs.attr("checked", isChecked);
            });
            BlackList.container.find(".delete-checked-black-btn").click(function() {
                var checkObjs = BlackList.container.find(".blacklist-check:checked");
                var blackIdList = [];
                var blackJson = {};
                checkObjs.each(function(index, checkObj) {
                    blackIdList[blackIdList.length] = checkObj.blackJson.id;
                    if (index == 0) {
                        blackJson = checkObj.blackJson;
                    }
                });
                BlackList.submit.doDeleteBlacks(blackIdList, blackJson);
            });

            //BlackList.init.initSwitchOp();

            BlackList.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<table>' +
                '   <tbody>' +
                '   <tr>' +
                '       <td><div class="blacklist-tab"></div> </td>' +
                '       <td><div class="switch-defense-div"></div></td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table>' +
                
                '<form action="/skinblacklist/submitSettings" method="post">' +
                '<div class="blacklist-op-div">' +
                '   <div class="blacklist-tip-div">' +
                '       <span>请注意：</span>' +
                '       <span>(1)黑名单必须在买家拍之前设置才有效。</span>' +
                '       <span>(2)如果买家在拍后10秒内付款，淘宝将无法关闭交易。</span>' +
                '   </div> ' +
                '   <table>' +
                '       <tbody>' +
                '<tr>' +
				'<td style="padding-left:30px;text-align:right;width:40%;">' +
				'<font color="red" style="font-weight: bold;">自动把给我中差评的买家加入黑名单：</font>' +
				'</td><td><div>' +
				'<input type="radio" id="autoChapingBlackListOn" name="autoChapingBlackList" value="1"> ' +
				'<span  for="autoChapingBlackListOn">打开&nbsp;&nbsp;&nbsp;</span>' +
				'<input type="radio"id="autoChapingBlackListOff" name="autoChapingBlackList" value="0">' +
				'<span  for="autoChapingBlackListOff">关闭&nbsp;</span>' +
				'</div></td>' +
				'</tr>' +
				'<tr>' +
				'<td style="padding-left:30px;text-align:right;width:40%;">' +
				'<font color="blue" style="font-weight: bold;">自动把有退款的买家加入黑名单：</font>' +
				'</td><td>' +
				'<input type="radio" id="autoRefundBlackListOn" name="autoRefundBlackList" value="1"> ' +
				'<span for="autoRefundBlackListOn">打开&nbsp;&nbsp;&nbsp;</span>' +
				'<input type="radio"id="autoRefundBlackListOff" name="autoRefundBlackList" value="0">' +
				'<span for="autoRefundBlackListOff">关闭&nbsp;</span>' +
				'</td>' +
				'</tr>' +
                '       <tr>' +
                '           <td colspan="2">设置关闭交易的解释(在交易详情中显示)：' +
                '             <select class="explain-text" name="explain" style="width: 200px;">' +
                '               <option>未及时付款</option>' +
                '               <option>买家信息填写错误，重新拍</option>' +
                '               <option>恶意买家/同行捣乱</option>' +
                '               <option>买家拍错了</option>' +
                '               <option>同城见面交易</option>' +
                '               <option>买家不想买了</option>' +
                '               <option>缺货</option>' +
                '             </select> ' +
                '           <input type="submit" class="btn btn-primary" value="保存设置"></td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div>' +
                '</form>' +
                '<div class="blacklist-tab" style="margin-top: 20px;">添加黑名单：</div> ' +
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
                '               <div style="color: #a10000;width: 180px;margin: 0 auto;">记录设置黑名单的原因，如：该买家给过我差评</div> ' +
                '               <input type="text" class="add-black-remark-text" />' +
                '           </td>' +
                '           <td><span class="add-black-btn commbutton btntext4 ">添加黑名单</span> </td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div>' +
                '<div class="blacklist-tab" style="margin-top: 20px;">黑名单列表：</div> ' +
                '<div class="blacklist-table-div">' +
                '   <table width="100%">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td width="20%"><span class="delete-checked-black-btn commbutton btntext4 ">删除选中</span> </td> ' +
                '           <td width="80%" style="text-align:right">请输入买家昵称：<input class="search-text" /><span class="search-btn commbutton btntext4 ">搜索黑名单</span></td>' +
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
                '           <td style="width: 12%;">删除黑名单</td>' +
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
                url : '/skinblacklist/isOn',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    var isOn = dataJson.res;
                    var switchStatus = TM.Switch.createSwitch.createSwitchForm("黑名单开启状态");
                    switchStatus.appendTo(BlackList.container.find(".switch-defense-div"));
                    switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
                        labels:['已开启','已关闭'],
                        doChange:function(isCurrentOn){
                            if (isCurrentOn == false) {//要开启
                                $.ajax({
                                    url : '/skinblacklist/turnOn',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                                            return;
                                    }
                                });
                            } else if (isCurrentOn == true) {//要关闭
                                $.ajax({
                                    url : '/skinblacklist/turnOff',
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
    }, BlackList.init);

    BlackList.show = BlackList.show || {};
    BlackList.show = $.extend({
        doShow: function() {
            var ruleData = BlackList.show.getQueryRule();
            var tbodyObj = BlackList.container.find(".blacklist-table").find("tbody");
            //tbodyObj.html("");
            BlackList.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/skinblacklist/queryBlackListBuyers',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var blackListArray = dataJson.res;
                        BlackList.container.find(".check-all-black").attr("checked", false);
                        
                        $(blackListArray).each(function(index, blackJson) {
                            var trObj = BlackList.row.createRow(index, blackJson);
                            tbodyObj.append(trObj);
                        });
                        
                        if (blackListArray == undefined || blackListArray == null || blackListArray.length == 0) {
                            tbodyObj.html('<tr><td colspan="6" height="40px">亲，暂未添加黑名单！</td></tr>');
                        }
                    }
                }

            });
        },
        refresh: function() {
            BlackList.show.doShow();
        },
        getQueryRule: function() {
            var ruleData = {};
            var buyerName = BlackList.container.find(".search-text").val();
            ruleData.buyerName = buyerName;
            return ruleData;
        }
    }, BlackList.show);


    BlackList.row = BlackList.row || {};
    BlackList.row = $.extend({
        createRow: function(index, blackJson) {
            var html = '' +
                '<tr>' +
                '   <td><input type="checkbox" class="blacklist-check" /></td>' +
                '   <td><span class="blacklist-buyer-name blacklist-check-span"></span></td>' +
                '   <td><span class="blacklist-remark"></span></td>' +
                '   <td><span class="blacklist-time"></span></td>' +
                '   <td><span class="modify-remark-btn commbutton btntext4">修改备注</span></td>' +
                '   <td><span class="delete-blacklist-btn commbutton btntext4">删除黑名单</span></td>' +
                '</tr>' +
                '';

            var trObj = $(html);
            var blackCheckObj = trObj.find(".blacklist-check");
            blackCheckObj.attr("blackId", blackJson.id);
            blackCheckObj.each(function() {
                this.blackJson = blackJson;
            });

            trObj.find(".blacklist-buyer-name").html(blackJson.buyerName);
            trObj.find(".blacklist-remark").html(blackJson.remark);
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
            trObj.find(".blacklist-time").html(timeStr);


            var checkCallback = function(checkObj) {
                var isChecked = checkObj.is(":checked");
                if (isChecked == false) {
                    BlackList.container.find(".check-all-black").attr("checked", false);
                } else {
                    var checkObjs = BlackList.container.find(".blacklist-check");
                    var flag = true;
                    checkObjs.each(function() {
                        if ($(this).is(":checked") == false)
                            flag = false;
                    });
                    BlackList.container.find(".check-all-black").attr("checked", flag);
                }
            }
            trObj.find(".blacklist-check").click(function() {
                checkCallback($(this));
            });
            trObj.find(".blacklist-check-span").click(function() {
                var checkObj = $(this).parents("tr").find(".blacklist-check");
                var isChecked = checkObj.is(":checked");
                if (isChecked == true)
                    checkObj.attr("checked", false);
                else
                    checkObj.attr("checked", true);
                checkCallback(checkObj);
            });


            trObj.find(".modify-remark-btn").click(function() {
                BlackList.submit.modifyRemarkParams.blackId = blackJson.id;
                BlackList.submit.modifyRemarkParams.blackJson = blackJson;
                BlackList.submit.doModifyRemark();
            });
            trObj.find(".delete-blacklist-btn").click(function() {
                var blackIdList = [];
                blackIdList[blackIdList.length] = blackJson.id;
                BlackList.submit.doDeleteBlacks(blackIdList, blackJson);
            });

            return trObj;
        }
    }, BlackList.row);


    BlackList.submit = BlackList.submit || {};
    BlackList.submit = $.extend({
        doSaveExplain: function() {
            var explain = BlackList.container.find(".explain-text").val();
            if (explain == "") {
                alert("请先输入关闭交易的解释");
                return false;
            }
            if (confirm("确定要修改黑名单设置吗？") == false)
                return false;
            
            return true;
//            var data = {};
//            data.explain = explain;
//            $.ajax({
//                url : '/skinblacklist/submitExplain',
//                data : data,
//                type : 'post',
//                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
//
//                }
//            });
        },
        doAddBlackList: function() {
            var buyerNames = BlackList.container.find(".add-black-text").val();
            if (buyerNames == "") {
                alert("请先输入黑名单买家的昵称");
                return;
            }
            var remark = BlackList.container.find(".add-black-remark-text").val();
            if (confirm("确定保存这些黑名单？") == false)
                return;

            var data = {};
            data.buyerNames = buyerNames;
            data.remark = remark;
            $.ajax({
                url : '/skinblacklist/addBlackList',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    //刷新黑名单列表
                    BlackList.show.refresh();
                }
            });
        },
        modifyRemarkParams: {
            blackId: 0,
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
                    title:'修改黑名单备注',
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var blackId = BlackList.submit.modifyRemarkParams.blackId;
                        var blackJson = BlackList.submit.modifyRemarkParams.blackJson;
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
                        data.blackId = blackId;
                        data.remark = remark;
                        $.ajax({
                            url : '/skinblacklist/modifyRemark',
                            data : data,
                            type : 'post',
                            success : function(dataJson) {
                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                                    return;
                                //刷新黑名单列表
                                dialogObj.dialog('close');
                                BlackList.show.refresh();
                            }
                        });


                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.find(".new-remark-text").val(BlackList.submit.modifyRemarkParams.blackJson.remark);
            dialogObj.dialog("open");
        },
        doDeleteBlacks: function(blackIdList, blackJson) {
            if (blackIdList === undefined || blackIdList == null || blackIdList.length == 0) {
                alert("请先选择要删除的黑名单");
                return;
            }
            if (blackIdList.length == 1) {
                if (confirm("确定要删除黑名单：" + blackJson.buyerName + "?") == false) {
                    return;
                }
            } else {
                if (confirm("确定要删除" + blackIdList.length + "个黑名单?") == false) {
                    return;
                }
            }
            var data = {};
            data.blackIdList = blackIdList;
            $.ajax({
                url : '/skinblacklist/deleteBlackList',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    //刷新黑名单列表
                    BlackList.show.refresh();
                }
            });
        }
    }, BlackList.submit);

})(jQuery,window));