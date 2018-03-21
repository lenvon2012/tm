var topKey = topKey || {};

topKey.init = topKey.init || {};

topKey.init = $.extend({
    doInit : function(target, cid){
        topKey.topkey.init(target);
        topKey.rule.type="uphot";
        if(cid == null || isNaN(cid)) {
            topKey.Event.setLevel3Select(target, "9");
        }
        else {
            $.get("/KeyWords/findLevel2ById", {baseId : cid}, function(data){
                if(data === undefined || data == null) {
                    topKey.Event.setLevel3Select(target, "9");
                    return;
                }
                if(data.success == false) {
                    topKey.Event.setLevel3Select(target, "9");
                    return;
                }
                var ids = data.message.toString().split(",");
                if(ids.length == 1) {
                    target.find('.topkeyselectCat2').val(ids[0]);
                    target.find('.topkeyselectCat2').trigger("change");
                } else if(ids.length == 2){
                    target.find('.topkeyselectCat2').val(ids[0]);
                    target.find('.topkeyselectCat2').trigger("change");
                    setTimeout(function(){
                        target.find('.topkeyselectCat3 option[urlid="'+ids[1]+'"]').attr("selected", true);
                        target.find('.topkeyselectCat3').trigger("change");
                    }, 200)

                } else {
                    topKey.Event.setLevel3Select(target, "9");
                }
            });
        }
    }
}, topKey.init);

topKey.rule = topKey.rule || {};

topKey.rule = $.extend({
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 10//每页条数

        };
        return ruleJson;
    },
    type:"categoryhot"
}, topKey.rule);

topKey.Widget = topKey.Widget ||{};
topKey.Widget.renderRate = function(rate){
    if(rate > 0){
        return '<span calss="raterise">+'+rate+'% </span><span class="trend up"></span>'
    }else if(rate < 0){
        return '<span calss="ratedown">'+rate+'% </span><span class="trend down"></span>'
    }else{
        return '<span calss="ratefair">'+rate+'% </span><span class="trend fair"></span>'
    }
}

var widget = topKey.Widget;


topKey.Widget.renderChangeRank = function(rank  ){
    if(rank > 0){
        return '<span calss="rankrise">+'+rank+' </span><span class="trend up"></span>'
    }else if(rank < 0){
        return '<span calss="rankdown">'+rank+' </span><span class="trend down"></span>'
    }else{
        return '<span calss="rankfair">'+rank+' </span><span class="trend fair"></span>'
    }
}
topKey.Widget.getUrlParam = function(name)
{
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r!=null) return unescape(r[2]); return null; //返回参数值
}


topKey.Event = topKey.Event || {};
topKey.Event= $.extend({
    setEvent:function(target){
        topKey.Event.setLevel2Event(target);
        topKey.Event.setBeginSearchEvent(target);

        topKey.Event.SetLevel3SelectedEvent(target);
        topKey.Event.setUpOrHotEvent(target);
    },
    setLevel2Event:function(target){
        target.find('.topkeyselectCat2').change(function(){
            var level2ID=target.find('.topkeyselectCat2 option:selected').val();
            topKey.Event.setLevel3Select(target, level2ID);
        });

    },
    setLevel3Select:function(target, level2ID){
        $.ajax({
            //async : false,
            url : '/KeyWords/findLevel3',
            data : {level2:level2ID},
            type : 'post',
            success : function(data) {
                topKey.topkey.setLevel3Content(target, data);
                topKey.topkey.getTopkey(target);
            }
        });
    },
    setUpOrHotEvent : function(target){
        target.find('.topkeyNav .uporhot').unbind('click').click(function(){
            if($(this).find('span').hasClass('current')) {
                return;
            }
            target.find('.topkeyNav .current').removeClass('current');
            $(this).find('span').addClass('current');
            if($(this).hasClass('sellup')) {
                topKey.rule.type="uphot";
                topKey.topkey.getTopkey(target);
            } else {
                topKey.rule.type="searchot";
                topKey.topkey.getTopkey(target);
            }

        });
    },
    SetLevel3SelectedEvent:function(target){
        target.find('.topkeyselectCat3').change(function(){
            topKey.topkey.getTopkey(target);
        });
    },
    setBeginSearchEvent:function(target){
        target.find('.beginSearch').click(function(){
            topKey.topkey.getTopkey(target);

        });
    }
},topKey.Event);

topKey.topkey = topKey.topkey || {};
topKey.topkey= $.extend({
    init:function(target){
        topKey.Event.setEvent(target);
    },
    setLevel3Content:function(target, data){
        target.find(".topkeyselectCat3").html("");

        for(var i=0;i<data.length;i++){
            var option=$('<option></option>');
            option.html(data[i].tag);
            option.attr("urlID",data[i].id);
            target.find(".topkeyselectCat3").append(option);
        }
        target.find(".topkeyselectCat3").append($('<option></option>'));
    },
    getTopkey:function(target){
        var level3ID=target.find('.topkeyselectCat3 option:selected').attr("urlID");
        var args={};
        var ruleJson=topKey.rule.getRuleJson();
        args.pn=ruleJson.pn;
        args.ps=ruleJson.ps;
        args.urlId=level3ID;
        args.type=topKey.rule.type;
        $.ajax({
            //async : false,
            url : '/KeyWords/topByUrl',
            data : args,
            type : 'post',
            success : function(data) {
                var totalCount = data.totalPnCount*data.ps;
                var per_page = data.ps;
                topKey.topkey.initPagination(target, totalCount, per_page, 1);
            }
        });
    },
    initPagination:function(target, totalCount, per_page, currentPage){
        var  callback = function(currentPage, jq){
            topKey.topkey.findTopkeyList(currentPage, jq, target);
        }
        currentPage--;
        target.find(".topkeyPagination").pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : callback,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findTopkeyList:function(currentPage, jq, target){
        var ruleJson = topKey.rule.getRuleJson();
        ruleJson.pn = currentPage+1;
        var level3ID=target.find('.topkeyselectCat3 option:selected').attr("urlID");
        var args={};
        args.pn=ruleJson.pn;
        args.ps=ruleJson.ps;
        args.urlId=level3ID;
        args.type=topKey.rule.type;

        $.ajax({
            //async : false,
            url : '/KeyWords/topByUrl',
            data : args,
            type : 'post',
            success : function(data) {
                topKey.topkey.createTopkeyTable(target, data);
            }
        });
    },
    createTopkeyTable:function(target, data){
        target.find(".catSearch").empty();
        var contentRow;
        if(topKey.rule.type=="searchot"){
            var firstRow=topKey.topkey.createSearchHothead();
            target.find(".catSearch").append(firstRow);
            for(var i=0;i<data.res.length;i++){
                var contentRow=topKey.topkey.createSearchHotRow(data.res[i]);
                target.find(".catSearch").append(contentRow);
            }
        }
        else if(topKey.rule.type=="uphot"){
            var firstRow=topKey.topkey.createUpHothead();
            target.find(".catSearch").append(firstRow);
            for(var i=0;i<data.res.length;i++){
                var contentRow=topKey.topkey.createUpHotRow(data.res[i]);
                target.find(".catSearch").append(contentRow);
            }
        }

        target.find('.catSearch span').click(function(){
            $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                TM.Alert.load(data,400, 300, function(){}, "", "添加到我的词库", 3000);
            });
        });
        target.find(".catSearch").find("tr:odd").addClass('grey-bg');
        target.find('span.addTextWrapper').unbind("click")
            .click(function(){
                TM.newAutoTitle.util.putIntoTitle($(this).text(),$(this), target);
        });
    },
    createSearchHothead:function(){
        var trObj=$('<tr class="tableRow firstRow"></tr>');
        var tdObj=$('<td class="cattd1">关键词</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2">搜索排名</td>');
        trObj.append(tdObj);
//        tdObj=$('<td class="cattd3">上升名次</td>');
//        trObj.append(tdObj);
        tdObj=$('<td class="cattd4">人气指数</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd5">升降幅度</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6">排名变化</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7">点击率</td>');
        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">操作</td>'));
        return trObj;
    },
    createSearchHotRow:function(topkey){
        var trObj=$('<tr class="tableRow hoverRow"></tr>');
        var tdObj=$('<td class="cattd1 word-content"></td>');
        //tdObj.append(topkey.text);
        tdObj.append(genKeywordSpan.gen({"text":topkey.text,"callback":TM.newAutoTitle.util.putIntoTitle,"enableStyleChange":true}));

        trObj.append(tdObj);
        tdObj=$('<td class="cattd2"></td>');
        tdObj.append(topkey.searchRank);
        trObj.append(tdObj);
//        tdObj=$('<td class="cattd3"></td>');
//        tdObj.append(topkey.upRateRank);
//        trObj.append(tdObj);
        tdObj=$('<td class="cattd4"></td>');
        tdObj.append(topkey.focusIndex);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd5"></td>');
        tdObj.append(widget.renderRate(topkey.rateChange));
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6"></td>');
        tdObj.append(widget.renderChangeRank(topkey.rankChange));
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7"></td>');
        if(topkey.ctr && topkey.ctr != 0 ){
            tdObj.append(topkey.ctr/100+'%');
        }else{
            tdObj.append('-');
        }

        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">'
//            +genKeywordSpan.gen({"text":"添加到词库","callback":"","enableStyleChange":true})+'</td>'
            + '<span class="btn btn-info addTextWrapper">添加到词库</span>'
        ));
        return trObj;
    },
    createUpHothead:function(){
        var trObj=$('<tr class="tableRow firstRow"></tr>');
        var tdObj=$('<td class="cattd1">关键词</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2">上升名次</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6">排名变化</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd5">升降幅度</td>');
        trObj.append(tdObj);
        // tdObj=$('<td class="cattd3">搜索排名</td>');
        // trObj.append(tdObj);
        tdObj=$('<td class="cattd4">人气指数</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7">点击率</td>');
        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">操作</td>'));
        var head = $("<thead></thead>");
        return trObj.appendTo(head);
    },
    createUpHotRow:function(topkey){
        var trObj=$('<tr class="tableRow hoverRow"></tr>');
        var tdObj=$('<td class="cattd1 word-content"></td>');
        tdObj.append(genKeywordSpan.gen({"text":topkey.text,"callback":TM.newAutoTitle.util.putIntoTitle,"enableStyleChange":true}));
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2"></td>');
        tdObj.append(topkey.upRateRank);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd5"></td>');
        tdObj.append(widget.renderRate(topkey.rateChange));
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6"></td>');
        tdObj.append(widget.renderChangeRank(topkey.rankChange));
        trObj.append(tdObj);
        //  tdObj=$('<td class="cattd3"></td>');
        //   tdObj.append(topkey.searchRank);
        //   trObj.append(tdObj);
        tdObj=$('<td class="cattd4"></td>');
        tdObj.append(topkey.focusIndex);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7"></td>');
        if(topkey.ctr && topkey.ctr != 0 ){
            tdObj.append(topkey.ctr/100+'%');
        }else{
            tdObj.append('-');
        }
        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">'
//            +genKeywordSpan.gen({"text":"添加到词库","callback":"","enableStyleChange":true})+'</td>'
            + '<span class="btn btn-info addTextWrapper">添加到词库</span>'
        ));
        return trObj;
    }
},topKey.topkey);
