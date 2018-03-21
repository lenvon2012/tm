var BatchTitle = BatchTitle || {};

BatchTitle.createErrTable=BatchTitle.createErrTable||{};

BatchTitle.createErrTable=$.extend({
	createTableDiv:function(data){
        var tableDiv=$('<div></div>');

        var tableObj=BatchTitle.createErrTable.createTableObj(data);
        tableDiv.append(tableObj);
        return tableDiv;
    },
    multiModifyArea:function(data){
        var multiModifyArea=$("#multiModifyArea");
        $(".tableDiv").remove();
        var tableDiv=$('<div class="tableDiv"><div style="width: 100%;height: 40px;line-height: 40px;">标题修改成功个数：<span class="successNum">'+data.successNum+'</span>'+
            '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;标题修改失败个数：<span class="failNum">'+data.failNum+'</span></div></div>');

        var tableObj=BatchTitle.createErrTable.createTableObj(data);
        tableDiv.append(tableObj);
        var modifyAreaTableDiv = tableObj.clone();
        multiModifyArea.find('.errorTable').remove();
        multiModifyArea.append(modifyAreaTableDiv);
        modifyAreaTableDiv.hide();
        TM.Alert.loadDetail(tableDiv,780, 600, null,"错误列表")

    },

    createTableObj:function(data){

        var tableObj=$('<table class="errorTable busSearch" style="text-align: center;"></table>');
        var firstRow=BatchTitle.createErrTable.createFirstRow();
        tableObj.append(firstRow);

        var failNum=$(".failNum");
        failNum.html(data.failNum);
        var successNum=$(".successNum");
        successNum.html(data.successNum);

        var length=data.errorList.length;
        if(length==0)       return  tableObj;
        else
            {
                var errRow;
                for(var i=0;i<length;i++)
                {
                    errRow=BatchTitle.createErrTable.createErrorRow(data.errorList[i],i);
                    tableObj.append(errRow);
                }
            }
        return tableObj;
    },
    createFirstRow:function(){
		var firstRow=$('<tr class="firstRow "></tr>');
		var td1=$('<th class="errImg" width="120px">宝贝图片</th>');
		var td2=$('<th class="errTitle">宝贝标题</th>');
		var td3=$('<th class="errCause">错误原因</th>');
		firstRow.append(td1);
		firstRow.append(td2);
		firstRow.append(td3);
		return firstRow;
		},
	createErrorRow:function(errMsg,i){


		if(i%2==0)
		var trObj=$('<tr class="errorContent evenRow"></tr>');
		else var trObj=$('<tr class="errorContent oddRow"></tr>');
		var td1=BatchTitle.createErrTable.createTd1(errMsg.picPath);
		var td2=BatchTitle.createErrTable.createTd2(errMsg.title);
		var td3=BatchTitle.createErrTable.createTd3(errMsg.msg);
		trObj.append(td1);
		trObj.append(td2);
		trObj.append(td3);
		return trObj;
		},
	createTd1:function(imgPath){
		var tdObj=$('<td class="errImg"></td>');
		var imgObj=$('<img  style="width:50px;height:50px;" />');
		imgObj.attr("src",imgPath);
		tdObj.append(imgObj);
		return tdObj;
		},
	createTd2:function(title){
		var tdObj=$('<td class="errTitle"></td>');

		tdObj.append(title);
		return tdObj;
		},
	createTd3:function(errMsg){
		var tdObj=$('<td class="errCause"></td>');

		tdObj.append(errMsg);
		return tdObj;
		}

},BatchTitle.createErrTable);


BatchTitle.multiModify=BatchTitle.multiModify||{};
BatchTitle.multiModify=$.extend({
    init: function(){
        BatchTitle.multiModify.setAllUseGuanfangRecomBatch();
        BatchTitle.multiModify.bindReplaceAllBtn();
        BatchTitle.multiModify.bindRemoveAllBtn();
        BatchTitle.multiModify.bindAddPrefixBtn();
        BatchTitle.multiModify.bindAddSuffixBtn();
        BatchTitle.multiModify.bindRemoveAllSpaceBtn();
    },
    setAllUseGuanfangRecomBatch : function() {
        $('.batchAllUseGuanfangRecomm').click(function(){
//            if(!TM.isAutoOnekeyOK){
//                TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
//                return;
//            }
            $.get('/items/itemCatCount',function(itemCatCount){
                $.get('/items/sellerCatCount',function(sellerCatCount){
                    BatchTitle.multiModify.batchRecommend(itemCatCount,sellerCatCount,2);
                })
            });
        });
    },
    batchRecommend : function(itemCatCount, sellerCatCount, recMode){

        var idArr = [];
        var titles = [];
        $('.ui-dialog').remove();
        $('.tmAlertDetail').remove();
        //var opt = $("#autorecommendopt");

        //  if(!opt || opt.length ==0){
        var htmls = [];
//        htmls.push("<form id='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
        var pushCatNames = function(res, name, rowClass){
            if(res && res.length > 0){
                htmls.push('<tr class="'+rowClass+'"><td>'+name+':</td><td><select><option cid="0">所有分类</option>');
                $.each(res, function(i, elem){
                    if(elem.count > 0){
                        htmls.push('<option cid="'+elem.id+'">'+elem.name+'('+elem.count+')'+'</option>');
                    }
                });
                htmls.push('</select></td></tr>');
            }
        }

        pushCatNames(itemCatCount, '淘宝类目', 'itemCat');
        pushCatNames(sellerCatCount, '自定义类目', 'sellerCat');
        var opt = $(htmls.join(''));
        //   }

        var content = $('<div></div>');
        content.append($('#shopRecTmpl').tmpl());
        content.find('tbody').prepend(opt);

        TM.Alert.loadDetail(content,700,390,function(){
            $.ajax({
                url : '/titles/batchChangeAll',
                type : 'post',
                data:{
                    sellerCatId : content.find('.sellerCat option:selected').attr('cid'),
                    itemCatId : content.find('.itemCat option:selected').attr('cid'),
                    status : content.find('.status  option:selected').attr('value'),
                    recMode : recMode,
                    'opt.fixedStart' : content.find('input[name=fixedStart]').val(),
                    'opt.allSale' : content.find('.allsale option:selected').attr('value'),
                    'opt.keepBrand' : content.find('input[name=brand]:checked').attr('value'),
                    'opt.keepSerial' : content.find('input[name=serialNum]:checked').attr('value'),
                    'opt.mustExcluded' : content.find('input[name=mustExcluded]').val(),
                    'opt.toAddPromote' : content.find('input[name=promotewords]:checked').attr('value'),
                    'opt.noColor' : content.find('input[name=noColor]:checked').attr('value'),
                    'opt.noNumber' : content.find('input[name=noColor]:checked').attr('value')
                },
                timeout: 200000,
                success : function(data){
                    //autoTitle.util.createBatchOPResult(data)

                    if (data.success == false) {
                        var message = data.message;
                        if (message === undefined || message == null || message == "") {
                            message = "自动标题任务提交失败，请联系我们！";
                        }
                        alert(data.message);
                        return;
                    } else {
                        //alert("系统提交了自动标题的后台任务，请进入任务中心查看");
//                        $(".showTaskOp").show();
                        TM.Alert.showDialog('由于宝贝数量过多，系统已为您提交了自动标题的<span style="font-weight: bold; color: #a10000;">后台任务</span>，是否立即进入<span style="font-weight: bold; color: #a10000;">任务中心</span>查看？',
                            400,300,function(){
                                location.href = "/titletaskop/fenxiao";
                            },function(){},"提示");
                        BatchTitle.listItem.centerFly();
                    }

                }
            });
        },'全店标题优化选项');
    },
    bindReplaceAllBtn: function() {
        $(".replaceAllBtn").click(function(){
            BatchTitle.multiModify.modifyReplaceAll("");
        });
    },
    modifyReplaceAll: function(numIidList){
        var oldKeyword=$("#oldKeyword").val();
        var newKeyword=$("#newKeyword").val();
        $.ajax({
            //async : false,
            url : '/BatchOp/replaceAll',
            type : "post",
            data : {"src":oldKeyword,"target":newKeyword,"numIidList":numIidList},
            success:function(data){

                BatchTitle.createErrTable.multiModifyArea(data);
                Loading.init.hidden();
            },
            error:function(data){}
        });
    },
    bindRemoveAllBtn: function() {
        $(".removeAllBtn").click(function(){
            BatchTitle.multiModify.modifyRemoveAll("");
        });
    },
    modifyRemoveAll: function(numIidList){
        var deleteKeyword=$("#deleteKeyword").val();
        $.ajax({
            //async : false,
            url : '/BatchOp/removeAll',
            type : "post",
            data : {"src":deleteKeyword,"numIidList":numIidList},
            success:function(data){
                BatchTitle.createErrTable.multiModifyArea(data);
                Loading.init.hidden();
            },
            error:function(data){}
        });
    },
    bindAddPrefixBtn: function() {
        $(".addPrefixBtn").click(function(){
            BatchTitle.multiModify.modifyAddPrefix("");
        });
    },
    modifyAddPrefix: function(numIidList){
        var prefixKeyword=$("#prefixKeyword").val();

        $.ajax({
            //async : false,
            url : '/BatchOp/appendHead',
            type : "post",
            data : {"target":prefixKeyword,"numIidList":numIidList},
            success:function(data){
                BatchTitle.createErrTable.multiModifyArea(data);
                Loading.init.hidden();
            },
            error:function(data){}
        });
    },
    bindAddSuffixBtn: function() {
        $(".addSuffixBtn").click(function(){
            BatchTitle.multiModify.modifyAddSuffix("");
        });
    },
    modifyAddSuffix: function(numIidList){
        var suffixKeyword=$("#suffixKeyword").val();
        $.ajax({
            //async : false,
            url : '/BatchOp/appendTail',
            type : "post",
            data : {"target":suffixKeyword,"numIidList":numIidList},
            success:function(data){
                BatchTitle.createErrTable.multiModifyArea(data);
                Loading.init.hidden();
            },
            error:function(data){}
        });
    },
    bindRemoveAllSpaceBtn: function() {
        $(".removeAllSpaceBtn").click(function(){
            BatchTitle.multiModify.modifyRemoveAllSpace("");
        });
    },
    modifyRemoveAllSpace: function(numIidList){
        $.ajax({
            //async : false,
            url : '/BatchOp/removeAll',
            type : "post",
            data : {"src":" ","numIidList":numIidList},
            success:function(data){

                BatchTitle.createErrTable.multiModifyArea(data);
                Loading.init.hidden();
            },
            error:function(data){}
        });
    },

    modify:function(numIidList){

        var modifyRules=$("input[name='modifyRules']:checked").val();
        if(modifyRules==1)
        {

            var oldKeyword=$("#oldKeyword").val();
            var newKeyword=$("#newKeyword").val();
            $.ajax({
                //async : false,
                url : '/BatchOp/replaceAll',
                type : "post",
                data : {"src":oldKeyword,"target":newKeyword,"numIidList":numIidList},
                success:function(data){

                    BatchTitle.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==2)
        {

            var deleteKeyword=$("#deleteKeyword").val();
            $.ajax({
                //async : false,
                url : '/BatchOp/removeAll',
                type : "post",
                data : {"src":deleteKeyword,"numIidList":numIidList},
                success:function(data){
                    BatchTitle.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==3)
        {

            var prefixKeyword=$("#prefixKeyword").val();

            $.ajax({
                //async : false,
                url : '/BatchOp/appendHead',
                type : "post",
                data : {"target":prefixKeyword,"numIidList":numIidList},
                success:function(data){
                    BatchTitle.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==4)
        {

            var suffixKeyword=$("#suffixKeyword").val();
            $.ajax({
                //async : false,
                url : '/BatchOp/appendTail',
                type : "post",
                data : {"target":suffixKeyword,"numIidList":numIidList},
                success:function(data){
                    BatchTitle.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==5)
        {

            $.ajax({
                //async : false,
                url : '/BatchOp/removeAll',
                type : "post",
                data : {"src":" ","numIidList":numIidList},
                success:function(data){

                    BatchTitle.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }
    }
},BatchTitle.multiModify)


BatchTitle.listItem=BatchTitle.listItem||{};
BatchTitle.listItem=$.extend({
    init: function(){
        BatchTitle.listItem.initItemList();
    },
    initItemList: function() {
        $(".search-pagging").tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                dataType: 'json',
                url: '/fenxiao/listItems',
                callback: function(dataJson){
                    if(!dataJson || !dataJson.res || dataJson.res.length == 0){
//                        TM.Alert.load("数据获取发生错误，请稍后重试或联系客服");
                        $('.item-title-tbody').empty();
                        $('.item-title-tbody').append($('<tr><td colspan="4">无数据</td></tr>'))
                    } else {
                        // empty the old trade-search-table-tbody content
                        var titles = dataJson.res;
                        $.each(titles, function(i, one){
                            one.rowno = i;
                        });
                        $('.item-title-tbody').empty();
                        var content = $('#tplItem').tmpl(titles);
                        content.appendTo('.item-title-tbody');
                        BatchTitle.listItem.setSaveCurrentTitleEvent();
                        BatchTitle.listItem.setResetTitleEvent();
                        BatchTitle.listItem.setSetFenxiaoTitleEvent();
                        BatchTitle.listItem.setAddHotWordsEvent();
                        BatchTitle.listItem.setAddLongtailWordsEvent();
                        BatchTitle.listItem.setAddPromoteWordsEvent();

                        content.find(".newTitleTxt").each(function(i, one){
                            var boxId ="newTitleRemainObj" + i;
                            $(this).inputlimitor({
                                limit: 60,
                                boxId: boxId,
                                remText: '<span class="twelve" >剩余字数: </span><span class="newRemainLength">%n</span>',
                                limitText: ' / %n'
                            });
                            $(this).keyup();
                            $('#'+boxId).find('br').remove();
                        });
                    }
                }
            }
        });
    },
    setSaveCurrentTitleEvent : function(){
        $('.updateNewTitle').click(function(){
            var currentTitle = $(this).parent().parent().next().find('.newTitleTxt').val();
            if(BatchTitle.listItem.countCharacters(currentTitle) > 60) {
                alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
            }else {
                var numIid = $(this).parent().parent().parent().parent().attr("numIid");
//                BatchTitle.listItem.modifyTitle(numIid,currentTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
            }
        });
    },
    setResetTitleEvent : function(){
        $('.resetTitle').click(function(){
            var currentTr = $(this).parent().parent();
            var originTitle = currentTr.find('.origin-title').text();
            var newTitleInput = currentTr.next().find(".newTitleTxt");
            newTitleInput.val(originTitle);
            newTitleInput.keyup();
            newTitleInput.focus();
        });
    },
    setSetFenxiaoTitleEvent : function(){
        $('.setFenxiaoTitle').click(function(){
            var currentTr = $(this).parent().parent();
            var numIid = currentTr.attr("numiid");
            $.post("/titles/fetchFenxiaoTitle", {numIid: numIid}, function(data){
                var newTitle;
                if(data){
                    newTitle = data;
                } else {
                    newTitle = currentTr.find('.origin-title').text();
                }
                var newTitleInput = currentTr.next().find(".newTitleTxt");
                newTitleInput.val(newTitle);
                newTitleInput.keyup();
                newTitleInput.focus();
            });
        });
    },
    setAddHotWordsEvent: function(){
        $('.addHotWords').click(function(){
            var chooseArea = $(this).parent().parent().parent().next().find(".choose-word-area");
            BatchTitle.listItem.initHotWords(chooseArea);
        });
    },
    setAddLongtailWordsEvent: function(){
        $('.addLongtailWords').click(function(){
            var chooseArea = $(this).parent().parent().parent().next().find(".choose-word-area");
            BatchTitle.listItem.initLongtailWords(chooseArea);
        });
    },
    setAddPromoteWordsEvent: function(){
        $('.addPromoteWords').click(function(){
            var chooseArea = $(this).parent().parent().parent().next().find(".choose-word-area");
            BatchTitle.listItem.initPromoteWords(chooseArea);
        });
    },

    modifyTitle : function(numIid, newTitle, oldTitleInput){
        var params = {};
        params.numIid = numIid;
        params.title = newTitle;
        $.post("/Titles/rename",params,function(res){
            if(!res){
                TM.Alert.load("标题修改失败！请您稍后重试哟!");
            }else if(res.ok){
                TM.Alert.load("标题修改成功！");
                if(oldTitleInput.val()!=newTitle) {
                    oldTitleInput.val(newTitle);
                    oldTitleInput.trigger("keyup");
                }
            }else{
                TM.Alert.load(res.msg);
            }
        });
    },
    countCharacters : function(str){
        var totalCount = 0;
        for (var i=0; i<str.length; i++) {
            var c = str.charCodeAt(i);
            if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                totalCount++;
            }else {
                totalCount+=2;
            }
        }
        return totalCount;
    },
    initHotWords: function(chooseArea){
        chooseArea.empty();
        var firstEle = chooseArea.parent().prev().prev().prev();
        var title=firstEle.find(".origin-title").text();
        var cid = firstEle.attr("tag");

        $.ajax({
            url: '/titles/getCWords',
            dataType: 'json',
            type: 'post',
            data: {title: title, cid: cid},
            error: function() {
            },
            success: function (wordArray) {
                var rowCount = 5;
                var count = 0;
                var html = [];

                $(wordArray).each(function(index, promoteWord) {
                    var spanObj = '<div class="choose-this-word divinline">'+promoteWord+'</div>';
                    html.push(spanObj);
                });

                var spanObjs = $(html.join(''));
                $(spanObjs).appendTo(chooseArea);
                chooseArea.find('.choose-this-word').click(function(){
                    BatchTitle.listItem.appendToTitle($(this).text(), $(this), chooseArea);
                });
            }
        });
        chooseArea.show();
    },
    initLongtailWords: function(chooseArea){
        chooseArea.empty();
        var firstEle = chooseArea.parent().prev().prev().prev();
        var numIid = firstEle.attr("numiid");

        $.ajax({
            url: '/titles/longTail',
            dataType: 'json',
            type: 'post',
            data: {numIid: numIid},
            error: function() {
            },
            success: function (wordArray) {
                var rowCount = 5;
                var count = 0;
                var html = [];

                $(wordArray).each(function(index, promoteWord) {
                    var spanObj = '<div class="choose-this-word divinline">'+promoteWord+'</div>';
                    html.push(spanObj);
                });

                var spanObjs = $(html.join(''));
                $(spanObjs).appendTo(chooseArea);
                chooseArea.find('.choose-this-word').click(function(){
                    BatchTitle.listItem.appendToTitle($(this).text(), $(this), chooseArea);
                });
            }
        });
        chooseArea.show();
    },
    initPromoteWords: function(chooseArea){
        chooseArea.empty();

        $.ajax({
            url: '/titles/getPromoteWords',
            dataType: 'json',
            type: 'post',
            data: {},
            error: function() {
            },
            success: function (wordArray) {
                var rowCount = 5;
                var count = 0;
                var html = [];

                $(wordArray).each(function(index, promoteWord) {
                    var spanObj = '<div class="choose-this-word divinline">'+promoteWord+'</div>';
//                    var spanObj = genKeywordSpan.gen({"text":promoteWord,"callback":BatchTitle.listItem.putIntoTitle,"enableStyleChange":true});
//                    console.info(spanObj.html())
//                    return;
//                    if(count++ == rowCount){
//                        count = 1;
//                        html.push('</tr></tr>');
//                    }
                    html.push(spanObj);
                });

                var spanObjs = $(html.join(''));
                $(spanObjs).appendTo(chooseArea);
                chooseArea.find('.choose-this-word').click(function(){
                    console.info(111222)
                    BatchTitle.listItem.appendToTitle($(this).text(), $(this), chooseArea);
                });
                //ModifyTitle.util.hideLoading();
            }
        });
        chooseArea.show();
    },
    appendToTitle : function(text,spanObj,chooseArea){
        var newTitleInput = chooseArea.parent().prev().prev().find(".newTitleTxt");
        if(spanObj.attr("tag") == 1) {
            var newTitle = newTitleInput.val();
            var i = newTitle.lastIndexOf(text);
            if(i >= 0) {
                newTitle = newTitle.substring(0, i) + newTitle.substring(i+text.length);
                newTitleInput.val(newTitle);
            }
            spanObj.css("border", "1px solid #aaa");
            spanObj.attr("tag", "0");
        } else {
            newTitleInput.val(newTitleInput.val() + text);
            spanObj.css("border", "1px solid #CC3300");
            spanObj.attr("tag", "1");
        }
        newTitleInput.trigger("keyup");
    },
    putIntoTitle : function(text,spanObj,container){
        var newTitle = container.parent().parent().find(".newTitleTxt");
        if(BatchTitle.listItem.countCharacters(newTitle.val()+text) > 60) {
            spanObj.qtip({
                content: {
                    text: "标题长度将超过字数限制"
                },
                position: {
                    at: "center right "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        } else {
            var start = {}, end = {};
            start.left = spanObj.offset().left+"px";
            start.top = spanObj.offset().top+"px";
            end.left = container.parent().parent().find(".newTitleTxt").offset().left+"px";
            end.top = container.parent().parent().find(".newTitleTxt").offset().top+"px";
            BatchTitle.listItem.flyFromTo(start,end,spanObj,function(){
                newTitle.val(newTitle.val()+text);
                newTitle.trigger("keyup");
                container.parent().parent().find(".oldTitleRemainWordsNum").html(30-newTitle.val().length);
            })

            spanObj.qtip({
                content: {
                    text: "已添加至标题尾部哟"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        }
    },
    flyToTitle : function($this,$targrt){
        var start = {}, end = {};
        start.left = $this.offset().left+"px";
        start.top = $this.offset().top+"px";
        end.left = $targrt.offset().left+"px";
        end.top = $targrt.offset().top+"px";
        BatchTitle.listItem.flyFromTo(start,end,$this,null)
    },
    flyFromTo : function(start,end,flyObj,callback){
        //var img = $('<span id="fly-from-to-img" class="inlineblock" style="z-index:200001;position: absolute;top:'+start.top+';left: '+start.left+'"></span>');
        var obj = flyObj.clone();
        obj.css("position","absolute");
        obj.css('left',start.left);
        obj.css('top',start.top);
        obj.appendTo($('body'));

        obj.animate({top:end.top,left:end.left},1500, function(){
            obj.fadeOut(1000,function(){
                obj.remove();
            });
            callback && callback();
        });
    },
    centerFly: function() {
        var start = {};
        var end = {};

        var windowWidth = $(window).width();
        var windowHeight = $(window).height();
        var scrollLeft = $(document).scrollLeft();
        var scrollTop = $(document).scrollTop();

        var left = (windowWidth) / 2 + scrollLeft;
        var top = (windowHeight) / 2 + scrollTop;
        start.left = left + "px";
        start.top = top + "px";

        end.left = $('#task-going-count').offset().left+"px";
        end.top = $('#task-going-count').offset().top+"px";
        autoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
    }
},BatchTitle.listItem)


$(document).ready(function(){

//	$("#modifyButton").mouseover(function(){
//		$(this).attr("src","/public/images/button/multiModify.png");
//	});
//	$("#modifyButton").mouseout(function(){
//		$(this).attr("src","/public/images/button/multiModify2.png");
//	});
//	$("#lookupButton").mouseover(function(){
//		$(this).attr("src","/public/images/button/chakan2.png");
//	});
//	$("#lookupButton").mouseout(function(){
//		$(this).attr("src","/public/images/button/chakan1.png");
//	});
	$("#closeImg").click(function(){
		if($("#warmNotice ol").css("display")=="block")
		{
			//$("#warmNotice ol").css("display","none");
            $("#warmNotice ol").fadeOut(1000);
			$("#closeImg").attr("src","/public/images/tips/arrow_down.gif");
		}
		else if($("#warmNotice ol").css("display")=="none")
		{
			//$("#warmNotice ol").css("display","block");
            $("#warmNotice ol").fadeIn(1000);
			$("#closeImg").attr("src","/public/images/tips/arrow_up.gif");
		}
	});

//	$("#modifyButton").click(function(){
//        Loading.init.show();
//
//        BatchTitle.multiModify.modify("");

//        var isAll=$("input[name='modifyArea']:checked").val();
//
//        if(isAll==1)
//            BatchTitle.multiModify.modify("");
//        else {
//
//            BatchTitle.multiModify.modify(CommChoose.rule.numIidList);
//        }
//	});

    $("#showResultButton").click(function(){
        $("#multiModifyArea").find('.errorTable').show();
    });

    $("#itemsChooseSpan").click(function(){
        CommChoose.createChoose.createOrRefleshCommsDiv();
    });

    BatchTitle.multiModify.init();

    BatchTitle.listItem.init();

});
		
