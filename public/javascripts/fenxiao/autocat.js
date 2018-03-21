var TM = TM || {};

((function ($, window) {
    TM.AutoCat = TM.AutoCat || {};
    var AutoCat = TM.AutoCat || {};

    var pn = 1;

    AutoCat.Init = AutoCat.Init || {};
    AutoCat.Init = $.extend({
        init : function(){
            AutoCat.Init.initCatArea();
        },
        initCatArea : function(){
            $.get("/fenxiao/listCatRule",function(data){
                if(!data || data.length == 0){
                    $(".data-space").append($('<tr><td colspan="3">亲，没有设置淘宝分类哦</td></tr>'));
                }
                for(var i=0; i<data.length; i++) {
                    var ruleData = "";
                    var rule;
                    if(!data[i] || !data[i].rule) {
                        continue;
                    }
                    rule = data[i].rule;
                    if(rule.words && rule.words != "") {
                        ruleData += "[标题包含:" + rule.words + "]";
                    }
                    if(rule.brand && rule.brand != "") {
                        ruleData += "[品牌:" + rule.brand + "]";
                    }
                    if(rule.attr && rule.attr != "") {
                        ruleData += "[属性:" + rule.attr + "]";
                    }
                    if(rule.supplier && rule.supplier != "") {
                        ruleData += "[供应商:" + rule.supplier + "]";
                    }
                    data[i].ruleData = ruleData;
                }
                $('#tplItem').tmpl(data).appendTo('.data-space');

                $(".edit-autocat").click(function(){
                    var cid = $(this).parent().parent().attr("cid");
                    TM.AutoCat.Init.editDialog(cid);
                });

                $(".run-autocat").click(function(){
                    var cid = $(this).parent().parent().attr("cid");
                    TM.AutoCat.Init.runAutoCatDialog(false, cid);
                });
            });

            $(".doAllAutoCatBtn").click(function(){
                TM.AutoCat.Init.runAutoCatDialog(true, 0);
            });
        },
        editDialog : function(cid){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table class="busSearch" style="margin-top: 10px;">' +
                '       <tr>' +
                '           <th style="width:20%;">标题含此关键词：</th>' +
                '           <td><div class="tdright"><input name="words" id="words" style="width:300px;" value=""><p style="padding-top:5px;"><b>规范：</b>支持多个关键词，&amp;&amp;表示并且，||表示或者；<br>比如，短袖||长袖&amp;&amp;圆领，表示领子是圆领，袖长是短袖或者长袖</p></div></td>' +
                '       </tr>' +
                '       <tr>' +
                '           <th style="width:20%;">品牌：</th>' +
                '           <td><div class="tdright"><input name="brand" id="brand" style="width:300px;" value=""><p style="padding-top:5px;"><b>规则：</b>此品牌的宝贝自动分类到此类别</p></div></td>' +
                '       </tr>' +
                '       <tr>' +
                '           <th style="width:20%;">属性：</th>' +
                '           <td><div class="tdright"><input name="attr" id="attr" style="width:300px;" value=""><p style="padding-top:5px;"><b>规范：</b>属性含此关键词的宝贝</p></div></td>' +
                '       </tr>' +
                '       <tr>' +
                '           <th style="width:20%;">供应商：</th>' +
                '           <td><div class="tdright"><select name="supplier" id="supplier" style="width:300px;"></select><p style="padding-top:5px;"><b>规范：</b>属性含此关键词的宝贝</p></div></td>' +
                '       </tr>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);

            $("body").append(dialogObj);

            var title = "修改自动分类设置";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:350,
                width:650,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var ruleData = {};
                    ruleData.cid = cid;
                    ruleData.words = dialogObj.find('#words').val();
                    ruleData.brand = dialogObj.find('#brand').val();
                    ruleData.attr = dialogObj.find('#attr').val();
                    ruleData.supplier = dialogObj.find('#supplier').val();
                    $.post("/fenxiao/updateAutoCatRule", ruleData, function(data){
                        if(data.success){
                            window.location.reload();
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            $.post("/fenxiao/getAutoCatRule", {cid: cid}, function(data) {

                if(data){
                    dialogObj.find('#words').val(data.words);
                    dialogObj.find('#brand').val(data.brand);
                    dialogObj.find('#attr').val(data.attr);
                }
                $.get("/fenxiao/showSupplier", function(list){
                    var options = "<option></option>";
                    $.each(list, function(i, one){
                        if(data && one.supplierNick == data.supplier) {
                            options += '<option value="'+one.supplierNick+'" selected="selected">'+one.supplierNick+'</option>';
                        } else {
                            options += '<option value="'+one.supplierNick+'">'+one.supplierNick+'</option>';
                        }
                    });
                    console.info()
                    dialogObj.find('#supplier').html(options);
                });

                dialogObj.dialog('open');
            });
        },
        runAutoCatDialog : function(doAll, cid){
            TM.Loading.init.hidden();
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <div class="loading-div" style="text-align: center;padding: 40px;font-weight: bold;color: red;">' +
                '       <img class="loading-img" src="/public/images/fenxiao/loading.gif"><span class="loading-text">正在处理第0到10个宝贝</span>' +
                '   </div>' +
                '</div> ' +
                '';

            dialogObj = $(html);

            $("body").append(dialogObj);

            var title = "正在执行自动分类";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'取消执行':function(){
                    $(this).dialog('close');
                    window.location.reload();
                }}
            });

            if(doAll == true) {
                TM.AutoCat.Init.doAllAutoCatReq(dialogObj);
            } else {
                TM.AutoCat.Init.doAutoCatReq(dialogObj, cid);
            }
        },
        doAutoCatReq: function(dialogObj, cid){
            TM.Loading.init.hidden();
            $.post("/fenxiao/checkAutoCatExist", {cid: cid}, function(txt){
                if(txt == "notexist"){
                    TM.Alert.load('<br><p style="font-size:14px">亲，请先设置该分类的分类规则！</p>',400,230);
                } else {
                    dialogObj.dialog('open');
                    $.post("/fenxiao/doAutoCatAlone", {cid: cid, pn: pn}, function(data) {
                        if(data.count == 10){
                            pn++;
                            TM.AutoCat.Init.doAutoCatReq(dialogObj, cid);
                            dialogObj.find(".loading-text").html("正在处理第" + data.pn * 10 + "到" + (data.pn+1)*10 +"个宝贝");
                        } else {
                            dialogObj.find(".loading-img").hide();
                            dialogObj.find(".loading-text").html("自动分类完成，请点击确认退出！");
                        }
                    });
                }
            });
        },
        doAllAutoCatReq: function(dialogObj){
            TM.Loading.init.hidden();
            $.post("/fenxiao/checkAutoCatExist", function(txt){
                if(txt == "notexist"){
                    TM.Alert.load('<br><p style="font-size:14px">亲，请先设置该分类的分类规则！</p>',400,230);
                } else {
                    dialogObj.dialog('open');
                    $.post("/fenxiao/doAutoCatAll", {pn: pn}, function(data) {
                        if(data.count == 10){
                            pn++;
                            TM.AutoCat.Init.doAllAutoCatReq(dialogObj);
                            dialogObj.find(".loading-text").html("正在处理第" + data.pn * 10 + "到" + (data.pn+1)*10 +"个宝贝");
                        } else {
                            dialogObj.find(".loading-img").hide();
                            dialogObj.find(".loading-text").html("自动分类完成，请点击确认退出！");
                        }
                    });
                }
            });
        }
    },AutoCat.Init);

})(jQuery, window));