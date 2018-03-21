var topKey = topKey || {};

topKey.init = topKey.init || {};

topKey.init = $.extend({
    doInit : function(cid){
        topKey.topkey.init();
        topKey.rule.type="searchup";
        if(cid == null || isNaN(cid)) {
            return;
        } else {
            $.get("/KeyWords/findLevel2ById", {baseId : cid}, function(data){
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    return;
                }
                var ids = data.message.toString().split(",");
                if(ids.length == 1) {
                    $('.topkeyselectCat2').val(ids[0]);
                    $('.topkeyselectCat2').trigger("change");
                } else if(ids.length == 2){
                    $('.topkeyselectCat2').val(ids[0]);
                    $('.topkeyselectCat2').trigger("change");
                    setTimeout(function(){
                        $('.topkeyselectCat3 option[urlid="'+ids[1]+'"]').attr("selected", true);
                        $('.topkeyselectCat3').trigger("change");
                    }, 200)

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
    type:"searchup"
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
    setEvent:function(){
        topKey.Event.setLevel2Event();
        topKey.Event.setBeginSearchEvent();
        topKey.Event.setRSRCEvent();
        topKey.Event.setRQLMEvent();
        topKey.Event.setSSZKEvent();
        topKey.Event.setZYZTCEvent();
        topKey.Event.SetLevel3SelectedEvent();
        topKey.Event.setUpOrHotEvent();
    },
    setLevel2Event:function(){
        $('.topkeyselectCat2').change(function(){
            var level2ID=$('.topkeyselectCat2 option:selected').val();
            topKey.Event.setLevel3Select(level2ID);
        });

    },
    setUpOrHotEvent : function(){
        $('.topkeyNav .uporhot').unbind('click').click(function(){
            if($(this).find('span').hasClass('current')) {
                return;
            }
            $('.topkeyNav .current').removeClass('current');
            $(this).find('span').addClass('current');
            if($(this).hasClass('sellup')) {
                topKey.rule.type="searchup";
                topKey.topkey.getTopkey();
            } else {
                topKey.rule.type="searchot";
                topKey.topkey.getTopkey();
            }

        });
    },
    setLevel3Select:function(level2ID){
        $.ajax({
            //async : false,
            url : '/KeyWords/findLevel3',
            data : {level2:level2ID},
            type : 'post',
            global: false,
            success : function(data) {
                topKey.topkey.setLevel3Content(data);
                topKey.topkey.getTopkey();
            }
        });
    },
    SetLevel3SelectedEvent:function(){
        $('.topkeyselectCat3').change(function(){
            topKey.topkey.getTopkey();
        });
    },
    setBeginSearchEvent:function(){
        $('.beginSearch').click(function(){
            topKey.topkey.getTopkey();
        /*    var level3ID=$('.selectCat3 option:selected').attr("urlID");
            var args={};
            var ruleJson=topKey.rule.getRuleJson();
            args.pn=ruleJson.pn;
            args.ps=ruleJson.ps;
            args.urlId=level3ID;
            args.type="searchhot";
            $.ajax({
                //async : false,
                url : '/KeyWords/topByUrl',
                data : args,
                type : 'post',
                success : function(data) {

                    var totalCount = data.totalPnCount*data.ps;
                    var per_page = data.ps;
                    topKey.topkey.initPagination(totalCount, per_page, 1);
                }
            });
        */
        });
    },
    setRQLMEvent:function(){
        $('#rqlm').click(function(){
          //  topKey.topkey.getTopkey("categoryhot");
            topKey.rule.type="categoryhot";
            $('#rqlm').addClass("selected");
            $('#rsrc').removeClass("selected");
            $('#sszk').removeClass("selected");
            $('#zyztc').removeClass("selected");
            topKey.topkey.getTopkey();
        });
    },
    setRSRCEvent:function(){
        $('#rsrc').click(function(){
          //  topKey.topkey.getTopkey("searchot");
            topKey.rule.type="searchot";
            $('#rqlm').removeClass("selected");
            $('#rsrc').addClass("selected");
            $('#sszk').removeClass("selected");
            $('#zyztc').removeClass("selected");
            topKey.topkey.getTopkey();
        });
    },
    setSSZKEvent:function(){
        $('#sszk').click(function(){
          //  topKey.topkey.getTopkey("searchup");
            topKey.rule.type="searchup";
            $('#rqlm').removeClass("selected");
            $('#rsrc').removeClass("selected");
            $('#sszk').addClass("selected");
            $('#zyztc').removeClass("selected");
            topKey.topkey.getTopkey();
        });
    },
    setZYZTCEvent:function(){
        $('#zyztc').click(function(){
          //  topKey.topkey.getTopkey("ctrhot");
            topKey.rule.type="ctrhot";
            $('#rqlm').removeClass("selected");
            $('#rsrc').removeClass("selected");
            $('#sszk').removeClass("selected");
            $('#zyztc').addClass("selected");
            topKey.topkey.getTopkey();
        });
    }
},topKey.Event);

topKey.topkey = topKey.topkey || {};
topKey.topkey= $.extend({
    init:function(){
        $.get('/KeyWords/findAllLevel2', function(data) {
            if(data !== undefined && data != null) {
                for(var i = 0; i < data.length; i++){
                    var option=$('<option></option>');
                    option.html(data[i].tag);
                    option.val(data[i].id);
                    if(i === 0) {
                        option.attr("selected", "selected");
                        topKey.Event.setLevel3Select(data[i].id);
                    }
                    $(".topkeyselectCat2").append(option);
                }
                $(".topkeyselectCat2").append($('<option></option>'));
            }
        });
        topKey.Event.setEvent();
        //topKey.Event.setLevel3Select("9"); //初始level2为“时尚女装”，其id为9
    },
    setLevel3Content:function(data){
        $(".topkeyselectCat3").html("");

        for(var i=0;i<data.length;i++){
            var option=$('<option></option>');
            option.html(data[i].tag);
            option.attr("urlID",data[i].id);
            $(".topkeyselectCat3").append(option);
        }
        $(".topkeyselectCat3").append($('<option></option>'));
    },
    getTopkey:function(){
        var level3ID=$('.topkeyselectCat3 option:selected').attr("urlID");
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
                topKey.topkey.initPagination(totalCount, per_page, 1);
            }
        });
    },
    initPagination:function(totalCount, per_page, currentPage){
        currentPage--;
        $("#topkeyPagination").pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : topKey.topkey.findTopkeyList,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findTopkeyList:function(currentPage, jq){

        var ruleJson = topKey.rule.getRuleJson();
        ruleJson.pn = currentPage+1;
        var level3ID=$('.topkeyselectCat3 option:selected').attr("urlID");
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
                topKey.topkey.createTopkeyTable(data);
            }
        });
    },
    createTopkeyTable:function(data){
        $(".catSearch").empty();
        var contentRow;
        if(topKey.rule.type=="searchot"){
            var firstRow=topKey.topkey.createSearchHothead();
            $(".catSearch").append(firstRow);
            for(var i=0;i<data.res.length;i++){
                var contentRow=topKey.topkey.createSearchHotRow(data.res[i]);
                $(".catSearch").append(contentRow);
            }
        }
        else if(topKey.rule.type=="categoryhot"){
            var firstRow=topKey.topkey.createCategoryHothead();
            $(".catSearch").append(firstRow);
            for(var i=0;i<data.res.length;i++){
                var contentRow=topKey.topkey.createCategoryHotRow(data.res[i]);
                $(".catSearch").append(contentRow);
            }
        }
        else if(topKey.rule.type=="searchup"){
            var firstRow=topKey.topkey.createSearchUphead();
            $(".catSearch").append(firstRow);
            for(var i=0;i<data.res.length;i++){
                var contentRow=topKey.topkey.createSearchUpRow(data.res[i]);
                $(".catSearch").append(contentRow);
            }
        }
        else if(topKey.rule.type=="ctrhot"){
            var firstRow=topKey.topkey.createCtrHothead();
            $(".catSearch").append(firstRow);
        for(var i=0;i<data.res.length;i++){
            var contentRow=topKey.topkey.createCtrHotRow(data.res[i]);
            $(".catSearch").append(contentRow);
            }
        }
        $('.catSearch span').click(function(){
            $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                TM.Alert.load(data,400, 300, function(){}, "", "添加到我的词库", 3000);
            });
        });
        $(".catSearch").find("tr:odd").addClass('grey-bg');
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
        topkey.text = topkey.text.replace("2013", "2016");
        topkey.text = topkey.text.replace("2014", "2016");
        topkey.text = topkey.text.replace("2015", "2016");
        topkey.text = topkey.text.replace("2016", "2017");
        tdObj.append(topkey.text);

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
    createCategoryHothead:function(){
        var trObj=$('<tr class="tableRow firstRow"></tr>');
        var tdObj=$('<td class="cattd1">关键词</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2">人气指数</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd3">搜索排名</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd4">上升名次</td>');
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
    createCategoryHotRow:function(topkey){
        var trObj=$('<tr class="tableRow hoverRow"></tr>');
        var tdObj=$('<td class="cattd1 word-content"></td>');
        topkey.text = topkey.text.replace("2013", "2016");
        topkey.text = topkey.text.replace("2014", "2016");
        topkey.text = topkey.text.replace("2015", "2016");
        topkey.text = topkey.text.replace("2016", "2017");
        tdObj.append(topkey.text);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2"></td>');
        tdObj.append(topkey.focusIndex);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd3"></td>');
        tdObj.append(topkey.searchRank);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd4"></td>');
        tdObj.append(topkey.upRateRank);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd5"></td>');
        tdObj.append(topkey.rateChange);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6"></td>');
        tdObj.append(topkey.rankChange);
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
    createSearchUphead:function(){
        var trObj=$('<tr class="tableRow firstRow"></tr>');
        var tdObj=$('<td class="cattd1">关键词</td>');
        trObj.append(tdObj);
        //tdObj=$('<td class="cattd2">关注指数</td>');
        //trObj.append(tdObj);
        tdObj=$('<td class="cattd6">排名变化</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd5">升降幅度</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd4">人气指数</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7">点击率</td>');
        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">操作</td>'));
        var head = $("<thead></thead>");
        return trObj.appendTo(head);
    },
    createSearchUpRow:function(topkey){
        var trObj=$('<tr class="tableRow hoverRow"></tr>');
        var tdObj=$('<td class="cattd1 word-content"></td>');
        topkey.text = topkey.text.replace("2013", "2016");
        topkey.text = topkey.text.replace("2014", "2016");
        topkey.text = topkey.text.replace("2015", "2016");
        topkey.text = topkey.text.replace("2016", "2017");
        tdObj.append(topkey.text);
        trObj.append(tdObj);
        //tdObj=$('<td class="cattd2"></td>');
        //tdObj.append(topkey.upRateRank);
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
    },
    createCtrHothead:function(){
        var trObj=$('<tr class="tableRow firstRow"></tr>');
        var tdObj=$('<td class="cattd1">关键词</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2">点击率</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd3">搜索排名</td>');
        trObj.append(tdObj);
     //   tdObj=$('<td class="cattd4">上升名次</td>');
     //   trObj.append(tdObj);
        tdObj=$('<td class="cattd5">人气指数</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6">升降幅度</td>');
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7">排名变化</td>');
        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">操作</td>'));
        return trObj;
    },
    createCtrHotRow:function(topkey){
        var trObj=$('<tr class="tableRow hoverRow"></tr>');
        var tdObj=$('<td class="cattd1 word-content"></td>');
        topkey.text = topkey.text.replace("2013", "2016");
        topkey.text = topkey.text.replace("2014", "2016");
        topkey.text = topkey.text.replace("2015", "2016");
        topkey.text = topkey.text.replace("2016", "2017");
        tdObj.append(topkey.text);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd2"></td>');
        if(topkey.ctr && topkey.ctr != 0 ){
            tdObj.append(topkey.ctr/100+'%');
        }else{
            tdObj.append('-');
        }
        trObj.append(tdObj);
        tdObj=$('<td class="cattd3"></td>');
        tdObj.append(topkey.searchRank);
        trObj.append(tdObj);
     //   tdObj=$('<td class="cattd4"></td>');
     //   tdObj.append(topkey.upRateRank);
     //   trObj.append(tdObj);
        tdObj=$('<td class="cattd5"></td>');
        tdObj.append(topkey.focusIndex);
        trObj.append(tdObj);
        tdObj=$('<td class="cattd6"></td>');
        tdObj.append(widget.renderRate(topkey.rateChange));
        trObj.append(tdObj);
        tdObj=$('<td class="cattd7"></td>');
        tdObj.append(widget.renderChangeRank(topkey.rankChange));
        trObj.append(tdObj);
        trObj.append($('<td class="add-to-mywords">'
//            +genKeywordSpan.gen({"text":"添加到词库","callback":"","enableStyleChange":true})+'</td>'
            + '<span class="tmbtn sky-blue-btn">添加到词库</span>'
        ));
        return trObj;
    }
},topKey.topkey);
