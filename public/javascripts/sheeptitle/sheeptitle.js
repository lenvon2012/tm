/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 3/28/13
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */

((function ($, window) {
    TM.sheeptitle = TM.sheeptitle || {};
    TM.sheeptitle.Init = TM.sheeptitle.Init || {};
    TM.sheeptitle.Init = $.extend({
        init : function(){
            TM.sheeptitle.Init.initSearchArea();
            TM.sheeptitle.Init.initDiagArea();
        },
        initSearchArea : function(){
            $.get("/items/sellerCatCount",function(data){
                // here is the clicksearch seller cat btn
                var clicksearchcat = $('.skinsellercat');
                clicksearchcat.empty();
                if(!data || data.length == 0){
                    clicksearchcat.hide();
                }
                var flag = false;
                clicksearchcat.append($('<span class="inlineblock clicksearchcatbtn selectcolor" tag="catall"><input type="radio" name="sellercatgory" checked="true">自定义类目</span>'));
                var catname = "";
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    flag = true;
                    catname = data[i].name + "("+data[i].count+")";
                    clicksearchcat.append($('<span class="inlineblock clicksearchcatbtn" tag="'+data[i].id+'"><input type="radio" name="sellercatgory">'+catname+'</span>'));
                }
                if(!flag){
                    clicksearchcat.hide();
                }
            });
            $.get("/items/itemCatCount",function(data){
                // here is the clicksearch taobao cat btn
                var clicksearchcat = $('.skintaobaocat');
                clicksearchcat.empty();
                if(!data || data.length == 0){
                    clicksearchcat.hide();
                }
                var flag = false;
                clicksearchcat.append($('<span class="inlineblock clicksearchcatbtn selectcolor" tag="catall"><input type="radio" name="taobaocatgory" checked="true">淘宝类目</span>'));
                var catname = "";
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    flag = true;
                    catname = data[i].name + "("+data[i].count+")";
                    clicksearchcat.append($('<span class="inlineblock clicksearchcatbtn" tag="'+data[i].id+'"><input type="radio" name="taobaocatgory">'+catname+'</span>'));
                }
                if(!flag){
                    clicksearchcat.hide();
                }
            });
        },
        initDiagArea : function(){
            $.get("/Home/firstSync",function(){
                $.getScript("/Status/user",function(data){
                    $.cookie("isFenxiao",TM.isFenxiao);
                    TM.sheeptitle.Init.isFengxiao(TM.isFenxiao);
                })
            });
        },
        isFengxiao : function(isFenxiao){
            var params = $.extend({
                "s":"",
                "status":2,
                "catId":null,
                "sort":1,
                "lowBegin":0,
                "ps":5,
                "topEnd":100
            },TM.sheeptitle.util.getParams());
            TM.sheeptitle.ItemsDiag.getItemsDiag(false, params);
        }
    },TM.sheeptitle.Init);

    TM.sheeptitle.ItemsDiag = TM.sheeptitle.ItemsDiag || {};
    TM.sheeptitle.ItemsDiag = $.extend({
        getItemsDiag : function(isFenxiao, params){
            $('.diagResultArea').empty();
            var table = $('<div></div>');
            var bottom = $('<div class="autoTitleBottom" style="text-align: center;"></div>');
            /*var params = $.extend({
             "s":"",
             "status":2,
             "catId":null,
             "sort":1,
             "lowBegin":0,
             "ps":5,
             "topEnd":100
             },TM.sheeptitle.util.getParams());*/
            bottom.tmpage({
                currPage: 1,
                pageSize: 5,
                pageCount:1,
            	useSmallPageSize: true,
                ajax: {
                    param : params,
                    on: true,
                    dataType: 'json',
                    url: "/Titles/getItemsWithDiagResult",
                    callback:function(data){
                        $('.diagResultArea').empty();
                        $('.autoTitleDiv').find(".clear").remove();
                        $('.autoTitleDiv').find(".autoTitleBottom").remove();
                        $('.diagResultArea').append(TM.sheeptitle.ItemsDiag.createDiaghead());
                        $('.diagResultArea').append(TM.sheeptitle.ItemsDiag.createItemsWithDiagResult(data.res,isFenxiao));
                        $('.autoTitleDiv').append($('<div class="clear"></div>'));
                        $('.autoTitleDiv').append(bottom);
                        TM.sheeptitle.Event.initEvent();
                        /*if(isFengxiao){
                         $('.diagBriefInf').css("height","170px");
                         }*/
                    }
                }

            });
        },
        createDiaghead : function(){
            var head = $('<div class="diagHead"></div>');
            var ulObj = $('<ul class="diagHeadUL"></ul>');
            ulObj.append($('<li class="fl" style="width:100px;text-align: center;margin: 0 0 0 10px;"><span class="batchSelectUseRecommend commbutton btntext6">批量推荐</span></li>'));
            ulObj.append($('<li class="fl" style="width:100px;text-align: center;margin: 0 0 0 10px;"><span class="batchoptions">收起推荐策略</span></li>'));
            head.append(ulObj);
            head.append($('</br>'));
            var htmls = [];
            htmls.push("<form class='autorecommendopt batchrecommopt' style='text-align: center;display: block;' ><table style='margin: 0 auto;'>");
            //htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option value="false">否.只优化没有销量的宝贝</option><option name="sale" value="true" >是,不限销量</option></select></td></tr>')
            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart"></td></div>')
            htmls.push('<tr><td>排除词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded"></td></div>')
            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
            htmls.push('<tr><td><span class="inlineblock clickRecom">点击推荐</span></td></div>');
            htmls.push('</table></form>');
            var opt = $(htmls.join(''));
            head.append(opt);
            head.find('.batchoptions').click(function(){
                if($(this).html() == "收起推荐策略"){
                    $(this).html("展开推荐策略");
                    head.find('.autorecommendopt').fadeOut(1000);
                } else {
                    $(this).html("收起推荐策略");
                    head.find('.autorecommendopt').fadeIn(1000);
                }

            });
            opt.find('.clickRecom').click(function(){
                var idArr = [];
                $.each($(".diagBody").find(".subCheck:checked"),function(i, input){
                    var oThis = $(input);
                    idArr.push(oThis.parent().parent().attr('numIid'));
                });
                $.ajax({
                    url : '/titles/advancedBatchRecommend',
                    type : 'post',
                    data:{
                        numIids : idArr.join(","),

                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : false,
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        $('.singleDiagRes').each(function(i,diag){
                            if(data[$(diag).attr("numIid")] !== undefined){
                                $(diag).find('.recomskintitle').html(data[$(diag).attr("numIid")]);
                            }
                        });
                        TM.Alert.load("推荐成功");
                    }
                });
            });
            return head;
        },
        createItemsWithDiagResult : function(results,isFenxiao){
            var itemsWithDiagResult = $('<div class="itemsWithDiagResult"></div>');
            itemsWithDiagResult.append(TM.sheeptitle.ItemsDiag.createDiagBody(results,isFenxiao));
            return itemsWithDiagResult;
        },
        createDiagBody : function(results,isFenxiao){
            var body = $('<table class="diagBody"></table>');
            var numIids = [];
            body.append($('<thead style="font-size: 12px;"><td style="width: 40px;"><input type="checkbox" tag="checkAll" class="checkAll"></td>'
                +'<td style="width: 80px;">宝贝主图</td>'
                +'<td style="width: 50px;">状态</td>'
                +'<td style="width: 305px;">宝贝标题</td>'
                +'<td style="width: 305px;">推荐标题</td>'
                +'</thead>'));
            for(var i = 0; i<results.length;i++) {
                var item = results[i];
                body.append(TM.sheeptitle.ItemsDiag.createSingleDiagRes(item,i,isFenxiao));
                numIids.push(item.id);
            }
            $.get('/Titles/getRecommends',{numIids:numIids.join(',')},function(data){
                $.each(data,function(i,diag){
                    var newTitle = body.find(".singleDiagRes[numIid='"+diag.numIid+"'] .recomskintitle");
                    newTitle.html(diag.title);
                })
            });
            return body;
        },
        createSingleDiagRes : function(result,isFenxiao){
            var singleDiagRes = $('<tr class="singleDiagRes" numIid="'+result.id+'"></tr>');
            //singleDiagRes.append(autoTitle.ItemsDiag.createDiagBriefInf(result,i,isFengxiao));
            //singleDiagRes.append(autoTitle.ItemsDiag.createShowDetailTag(result));
            //singleDiagRes.append(autoTitle.ItemsDiag.createDiagDetailInf(result));
            // singleDiagRes.append($('<br />'));
            var status = result.status == 1?"上架中":"仓库中";
            var url = "http://item.taobao.com/item.htm?id=" + result.id;
            singleDiagRes.append('<td><input type="checkbox" tag="subCheck" class="subCheck"></td>'
                +'<td><a target="_blank" href="'+url+'"><img style="width: 80px;height: 80px;" src="'+result.picURL+'" ></a><div class="itemSalescount">销量：<span >'+result.salesCount+'</span></div></td>'
                +'<td>'+status+'</td>'
                +'<td class="skinTitle"></td>'
                +'<td class="skinrecommendtitle"></td>'
            );
            singleDiagRes.find('.skinTitle').append(TM.sheeptitle.ItemsDiag.createSkinTitleTdObj(result,isFenxiao));
            singleDiagRes.find('.skinrecommendtitle').append(TM.sheeptitle.ItemsDiag.createSkinRecomTitleTdObj(result,isFenxiao));
            //singleDiagRes.find('.operate').append(TM.sheeptitle.ItemsDiag.createSkinOpTdObj(result,isFenxiao));
            return singleDiagRes;
        },
        createSkinTitleTdObj : function(result,isFenxiao){
            var titleObj = $('<div style="text-align: left;"></div>');
            titleObj.append($('<div class="oldskintitlediv"><textarea class="oldskintitle" style="width: 230px;height: 30px;overflow-y: hidden;" type="text">'+result.title+'</textarea><span class="inlineblock twelve dosave">保存</span></div>'));
            titleObj.append(TM.sheeptitle.ItemsDiag.createOldTitleOp(result,isFenxiao));
            return titleObj;
        },
        createOldTitleOp : function(result,isFenxiao){
            var oldTitleOp = $('<div class="oldTitleOp"></div>');
            oldTitleOp.append($('<div class="skinopnavdiv"><span class="inlineblock skinopnav opselected" tag="titleparse">标题分词</span><span class="inlineblock skinopnav" tag="pro">属性词</span></div>'));
            var wordarea = $('<div class="wordarea"></div>');
            oldTitleOp.append(wordarea);
            oldTitleOp.find('.skinopnav').click(function(){
                oldTitleOp.find('.skinopnavdiv .opselected').removeClass("opselected");
                $(this).addClass("opselected");
                var title = $(this).parent().parent().parent().parent().find('.oldskintitle').val();
                if(title === undefined){
                    title = result.title;
                }
                switch ($(this).attr("tag")) {
                    case "titleparse" : TM.sheeptitle.ItemsDiag.titleparse(title, wordarea);break;
                    case "pro" : TM.sheeptitle.ItemsDiag.pro(result.id, wordarea);break;
                    default : TM.sheeptitle.ItemsDiag.titleparse(title, wordarea);break;
                }
            });
            oldTitleOp.find('.skinopnav').eq(0).trigger("click");
            return oldTitleOp;
        },
        createSkinRecomTitleTdObj : function(result,isFenxiao){
            var titleObj = $('<div style="text-align: left;"></div>');
            titleObj.append($('<div class="oldskinrecomtitlediv"><textarea class="recomskintitle" style="width: 230px;height: 30px;overflow-y: hidden;" type="text"></textarea><span class="inlineblock twelve dorecomsave">保存</span></div>'));
            titleObj.append(TM.sheeptitle.ItemsDiag.createRecomTitleOp(result,isFenxiao));
            return titleObj;
        },
        createRecomTitleOp : function(result,isFenxiao){
            var recomTitleOp = $('<div class="recomTitleOp"></div>');
            recomTitleOp.append($('<div class="skinopnavdiv"><span class="inlineblock skinopnav opselected" tag="recomOptions">展开推荐方式</span></div>'));
            var htmls = [];
            htmls.push("<form class='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
            //htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option value="false">否.只优化没有销量的宝贝</option><option name="sale" value="true" >是,不限销量</option></select></td></tr>')
            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart"></td></div>')
            htmls.push('<tr><td>排除词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded"></td></div>')
            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
            htmls.push('<tr><td><span class="inlineblock clickRecom">点击推荐</span></td></div>');
            htmls.push('</table></form>');
            var opt = $(htmls.join(''));
            var recomOptions = $('<div class="recomOptions" style="display: none;"></div>');
            recomOptions.append(opt);
            recomTitleOp.append(recomOptions);
            recomTitleOp.find('.skinopnav').eq(0).trigger("click");
            recomTitleOp.find('.skinopnav[tag="recomOptions"]').click(function(){
                if($(this).html() == "收起推荐方式"){
                    $(this).html("展开推荐方式");
                    recomOptions.fadeOut(1000);
                } else {
                    $(this).html("收起推荐方式");
                    recomOptions.fadeIn(1000);
                }
            });
            var numIid = result.id;
            opt.find('.clickRecom').click(function(){
                $.ajax({
                    url : '/titles/advancedBatchRecommend',
                    type : 'post',
                    data:{
                        numIids : numIid +",",

                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : false,
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        $('.diagBody').find('tr[numIid="'+numIid+'"]').find('.recomskintitle').html(data[numIid]);
                        TM.Alert.load("推荐成功");
                    }
                });
            });
            return recomTitleOp;
        },
        createSkinOpTdObj : function(result,isFenxiao){
            var skinOp = $('<div class="skinop"></div>');
            skinOp.append($('<div class="skinopnavdiv"><span class="inlineblock skinopnav opselected" tag="titleparse">标题分词</span><span class="inlineblock skinopnav" tag="pro">属性词</span></div>'));
            var wordarea = $('<div class="wordarea"></div>');
            skinOp.append(wordarea);
            skinOp.find('.skinopnav').click(function(){
                skinOp.find('.skinopnavdiv .opselected').removeClass("opselected");
                $(this).addClass("opselected");
                var title = $(this).parent().parent().parent().parent().find('.oldskintitle').val();
                if(title === undefined){
                    title = result.title;
                }
                switch ($(this).attr("tag")) {
                    case "titleparse" : TM.sheeptitle.ItemsDiag.titleparse(title, wordarea);break;
                    case "pro" : TM.sheeptitle.ItemsDiag.pro(result.id, wordarea);break;
                    default : TM.sheeptitle.ItemsDiag.titleparse(title, wordarea);break;
                }
            });
            skinOp.find('.skinopnav').eq(0).trigger("click");
            return skinOp;
        },
        titleparse : function(title, container){
            container.empty();
            $.post("/Titles/estimateKeyword",{title:title},function(data){
                var estimate = $('<div class="titlesplits"></div>');
                $(data).each(function(i,word){
                    estimate.append($('<span class="baseblock">'+word+'</span>'));
                });
                container.append(estimate);
                container.find('.baseblock').click(function(){
                    TM.sheeptitle.util.putIntoTitle($(this).text(),$(this), container.parent().parent().parent().find('.oldskintitle'));
                    // container.find('input').val($(this).text());
                    // container.find('.searchKeywords').click();
                });
            });
        },
        pro : function(numIid, container){
            container.empty();
            $.post("/Titles/props",{numIid:numIid}, function(data){
                var estimate = $('<div class="titlesplits"></div>');
                $(data).each(function(i,word){
                    estimate.append($('<span class="baseblock">'+word.value+'</span>'));
                });
                container.append(estimate);
                container.find('.baseblock').click(function(){
                    TM.sheeptitle.util.putIntoTitle($(this).text(),$(this), container.parent().parent().parent().find('.oldskintitle'));
                });
            });
        },
        Recommend : function(numIid){
            var opt = $(".autorecommendopt");
            opt.empty();
            // if(!opt || opt.length ==0){
            var htmls = [];
            htmls.push("<form class='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
            //htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option name="sale" value="true" >是,不限销量</option><option value="false">否.只优化没有销量的宝贝</option></select></td></tr>')
            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart"></td></div>')
            htmls.push('<tr><td>排除词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded"></td></div>')
            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
            htmls.push('</table></form>');
            opt = $(htmls.join(''));
            // }


            TM.Alert.loadDetail(opt,800,570,function(){
                $.ajax({
                    url : '/titles/advancedBatchRecommend',
                    type : 'post',
                    data:{
                        numIids : numIid+",",

                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : false,
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        $('.diagBody').find('tr[numIid="'+numIid+'"]').find('.skinRecommendTitle').html(data[numIid]);
                    }
                });
            },'全店标题优化选项');
        },
        advancedBatchRecommend : function(){
            var idArr = [];
            var opt = $(".autorecommendopt");
            opt.empty();
            // if(!opt || opt.length ==0){
            var htmls = [];
            htmls.push("<form class='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
            //htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option value="false">否.只优化没有销量的宝贝</option><option name="sale" value="true" >是,不限销量</option></select></td></tr>')
            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart"></td></div>')
            htmls.push('<tr><td>排除词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded"></td></div>')
            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
            htmls.push('</table></form>');
            opt = $(htmls.join(''));
            // }


            TM.Alert.loadDetail(opt,800,570,function(){
                var idArr = [];
                $.each($(".diagBody").find(".subCheck:checked"),function(i, input){
                    var oThis = $(input);
                    idArr.push(oThis.parent().parent().attr('numIid'));
                });
                $.ajax({
                    url : '/titles/advancedBatchRecommend',
                    type : 'post',
                    data:{
                        numIids : idArr.join(","),

                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : false,
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        //autoTitle.util.createBatchOPResult(data)
                        TM.sheeptitle.util.updateRecomTitle(data);
                    }
                });
            },'全店标题优化选项');
        },
        batchRecommend : function(itemCatCount, sellerCatCount){
            var idArr = [];
            var titles = [];
            var opt = $(".autorecommendopt");
            if(!opt || opt.length ==0){
                var htmls = [];
                htmls.push("<form class='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
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
                htmls.push('<tr class="status"><td>在架状态:</td><td><select><option value="2">所有宝贝</option><option value="1">在售宝贝</option><option value="0">下架宝贝</option></td></select></div>')
               // htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option name="sale" value="true" >是,不限销量</option><option value="false">否.只优化没有销量的宝贝</option></select></td></tr>')
                htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
                htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
                htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart"></td></div>')
                htmls.push('<tr><td>排除词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded"></td></div>')
                htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
                htmls.push('</table></form>');
                opt = $(htmls.join(''));
            }


            TM.Alert.loadDetail(opt,800,570,function(){
                $.ajax({
                    url : '/titles/batchChangeAll',
                    type : 'post',
                    data:{
                        sellerCatId : opt.find('.sellerCat option:selected').attr('cid'),
                        itemCatId : opt.find('.itemCat option:selected').attr('cid'),
                        status : opt.find('.status  option:selected').attr('value'),

                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : false,
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        TM.sheeptitle.util.createBatchOPResult(data)
                    }
                });
            },'全店标题优化选项');
        }
    },TM.sheeptitle.ItemsDiag);

    TM.sheeptitle.util = TM.sheeptitle.util || {};
    TM.sheeptitle.util = $.extend({
        createBatchOPResult : function(data){
            if(!data || data.length==0 || data.failNum == 0) {
//            TM.Alert.load("批量应用推荐标题成功"+((!data||data.length==0)?0:data.successNum)+"个，失败0个~,点击确定刷新数据",400,300,TM.sync);
                TM.Alert.loadDetail("<p>批量推荐标题成功"+((!data||data.length==0)?0:data.successNum)+
                    "个，失败0个~,点击确定刷新页面</p><p>若您对标题优化的结果不满意,您可以稍后在<b>左侧导航标题还原中心</b>进行还原操作,非常感谢</p><p>若有问题,也欢迎联系我们的客服,非常感谢</p>",
                    850,350,TM.sync,'推荐成功');
                return;
            }

            var multiModifyArea = $('.multiModifyArea');
            if(multiModifyArea.length == 0) {
                var multiModifyArea=$('<div class="multiModifyArea"></div>');
            }
            multiModifyArea.empty();
            multiModifyArea.css("display","block");
            multiModifyArea.css("left",(screen.width-1000)/2+"px");

            //var tableDiv =  multiModifyArea.find('#tableDiv');
            //if(tableDiv.length==0) {
            var tableDiv=$('<div id="tableDiv"></div>');
            // }
            // tableDiv.empty();
            var tableObj=MultiModify.createErrTable.createTableObj(data);
            tableDiv.append(tableObj);
            multiModifyArea.append(tableDiv);
            //var exitBatchOPMsg = multiModifyArea.find('.exitBatchOPMsg');
            //if(exitBatchOPMsg.length>0) {
            //   exitBatchOPMsg.remove();
            // }
            var successNum = $('<span >'+'批量推荐标题成功:'+'<span class="successNum">'+data.successNum+'</span>'+'</span>') ;
            var failNum = $('<span >'+'批量推荐标题失败:'+'<span class="failNum">'+data.failNum+'</span>'+'</span>') ;
            multiModifyArea.append(successNum);
            multiModifyArea.append(failNum);
            multiModifyArea.append($('<div style="width:600px;display: inline-block;"></div>'));
            multiModifyArea.append($('<div class="exitBatchOPMsg"><span ></span></div>'));
            multiModifyArea.find('.exitBatchOPMsg').click(function(){
                multiModifyArea.hide();
                TM.sync();
            });

            multiModifyArea.appendTo($("body"));
        },
        updateRecomTitle : function(data){
            $('.singleDiagRes').each(function(i,diag){
                if(data[$(diag).attr("numIid")] !== undefined){
                    $(diag).find('.skinRecommendTitle').html(data[$(diag).attr("numIid")]);
                }
            });
        },
        getParams : function(){
            var params = {};
            // get search status value
            var statustag = $('.skinstatus').find('input[name="status"]:checked').attr("tag");
                switch (statustag){
                case "statusall":params.status=2;break;
                case "statusonsale":params.status=0;break;
                case "statusinstock":params.status=1;break;
                default : params.status=2;break;
            }

            // get search sort value
            var sorttag = $('.skinsort').find('input[name="sort"]:checked').attr("tag");
            switch (sorttag){
                case "sortsaledown":params.sort=4;break;
                case "sortsaleup":params.sort=3;break;
                default : params.sort=4;break;
            }

            // get search seller catgory value
            var sellertag = $('.skinsellercat').find('input[name="sellercatgory"]:checked').parent().attr("tag");
            switch (sellertag){
                case "catall":params.catId=null;break;
                default : params.catId=sellertag;break;
            }

            // get search taobao catgory value
            var taobaotag = $('.skintaobaocat').find('input[name="taobaocatgory"]:checked').parent().attr("tag");
            switch (taobaotag){
                case "catall":params.cid=null;break;
                default : params.cid=taobaotag;break;
            }

            params.lowBegin =0;
            params.topEnd = 100;
            params.s = $('#searchWord').val();
            return params;
        },
        putIntoTitle:function(text, spanObj, oldskintitle){
            var oldskintitleval = oldskintitle.val();
            if(TM.sheeptitle.util.countCharacters(oldskintitleval) + TM.sheeptitle.util.countCharacters(text) > 60) {
                spanObj.qtip({
                    content: {
                        text: "标题长度将超过字数限制，请先删减标题后再添加~"
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
            else {
                //var dthis = newTitle[0];
                var dthis = oldskintitle[0];
                if(document.selection){
                    dthis.focus();
                    var fus = document.selection.createRange();
                    fus.text = text;
                    dthis.focus();
                }
                else if(dthis.selectionStart || dthis.selectionStart == '0'){
                    var start = dthis.selectionStart;
                    var end =dthis.selectionEnd;
                    dthis.value = dthis.value.substring(0, start) + text + dthis.value.substring(end, dthis.value.length);
                    dthis.selectionStart = start + text.length;
                    dthis.focus();
                }
                else{this.value += text; this.focus();}
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
        }
    },TM.sheeptitle.util);

    TM.sheeptitle.Event = TM.sheeptitle.Event || {};
    TM.sheeptitle.Event = $.extend({
        initEvent : function(){
            TM.sheeptitle.Event.setFeedbackEvent();
            TM.sheeptitle.Event.setCheckAllEvent();
            TM.sheeptitle.Event.setSubcheckEvent();
            TM.sheeptitle.Event.setGuardEvent();
            TM.sheeptitle.Event.setDorecommendEvent();
            TM.sheeptitle.Event.setDosaveEvent();
            TM.sheeptitle.Event.setDoRecomsaveEvent();
          //  TM.sheeptitle.Event.setSelectBatch();
            TM.sheeptitle.Event.setAllBatch();
            TM.sheeptitle.Event.setGOSearchEvent();
            TM.sheeptitle.Event.setClickSearchBtnEvent();
        },
        setFeedbackEvent : function(){
            $('.feedback').click(function(){
                var oThis = $(this);
                var exist = $(".feeddiag");
                exist.remove();
                var titles = [];
                $('.singleDiagRes').each(function(){
                    titles.push("["+$(this).find('.oldskintitle').val()+"]");
                });
                var pageTitles = titles.join(",");
                var htmls = [];
                htmls.push("<div class='feeddiag'>")
                htmls.push("<div><table>");
                htmls.push("<tr><td>您的建议:&nbsp;</td><td><textarea class='feedbackarea'>如果您对我们的推荐结果不够满意,请给我们留下您最宝贵的建议,我们一定努力改进,尽快让您看到最好的推荐效果</textarea></td></tr>")
                htmls.push("</table></div>");
                htmls.push("</div><div>");
                htmls.push("</div>");
                var body =$(htmls.join(''));
                TM.Alert.loadDetail(body, 600, 380, function(){
                    var feedbackcontent = body.find('.feedbackarea').val();
                    if(feedbackcontent == "如果您对我们的推荐结果不够满意,请给我们留下您最宝贵的建议,我们一定努力改进,尽快让您看到最好的推荐效果"){
                        TM.Alert.load("亲,请您留下一点建议哟~");
                        return false;
                    } else {
                        $.get('/op/commitRecommenFeedBack',{
                            'feed.origin':"origin",
                            'feed.recommendTitle':"recommendTitle",
                            'feed.content':body.find('.feedbackarea').val()+"!@#"+pageTitles,
                            'feed.numIid':""
                        }, function(){
                            TM.Alert.load('非常感谢您的建议,升流量一定尽快做出优化');
                        });
                    }
                    return true;
                },'请留下您的宝贵建议');
            });
        },
        setCheckAllEvent : function(){
            $(".checkAll").unbind("click");
            $(".checkAll").click(function(){
                $('input[tag="subCheck"]').attr("checked",this.checked);
            });
        },
        setSubcheckEvent : function(){
            $(".subCheck").unbind("click");
            $(".subCheck").click(function(){
                var $subBox = $("input[tag='subCheck']");
                $(".checkAll").attr("checked",$subBox.length == $("input[tag='subCheck']:checked").length ? true : false);
                if($(this).attr("checked")){
                    $(this).parent().parent().css("background-color","#ffffab");
                } else {
                    $(this).parent().parent().css("background-color","white");
                }
            });
        },
        setGuardEvent : function(){
            $(".guard").unbind("click");
            $('.guard').click(function(){
                alert(3)
            });
        },
        setDorecommendEvent : function(){
            $(".dorecommend").unbind("click");
            $('.dorecommend').click(function(){
                var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                TM.sheeptitle.ItemsDiag.Recommend(numIid);
            });
        },
        setDosaveEvent : function(){
            $(".dosave").unbind("click");
            $('.dosave').click(function(){
                var params = {};
                params.numIid = $(this).parent().parent().parent().parent().attr('numIid');
                params.title = $(this).parent().find('.oldskintitle').val();
                $.post("/Titles/rename",params,function(res){
                    if(!res){
                        TM.Alert.load("标题修改失败！请您稍后重试哟!");
                    }else if(res.ok){
                        TM.Alert.load("标题修改成功！");
                    }else{
                        TM.Alert.load(res.msg);
                    }
                });
            });
        },
        setDoRecomsaveEvent : function(){
            $(".dorecomsave").unbind("click");
            $('.dorecomsave').click(function(){
                var params = {};
                params.numIid = $(this).parent().parent().parent().parent().attr('numIid');
                params.title = $(this).parent().find('.recomskintitle').val();
                $.post("/Titles/rename",params,function(res){
                    if(!res){
                        TM.Alert.load("标题修改失败！请您稍后重试哟!");
                    }else if(res.ok){
                        TM.Alert.load("标题修改成功！");
                        $('.singleDiagRes[numIid="'+params.numIid+'"]').find('.oldskintitle').val(params.title);
                    }else{
                        TM.Alert.load(res.msg);
                    }
                });
            });
        },
        setSelectBatch : function(){
            $(".batchSelectUseRecommend").unbind("click");
            $('.batchSelectUseRecommend').click(function(){
                TM.sheeptitle.ItemsDiag.advancedBatchRecommend();
            });
        },
        setAllBatch : function(){
            $(".batchAllUseRecommend").unbind("click");
            $('.batchAllUseRecommend').click(function(){
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        TM.sheeptitle.ItemsDiag.batchRecommend(itemCatCount,sellerCatCount);
                    })
                });
            });
        },
        setGOSearchEvent : function(){
            $("#goSearchItems").unbind("click");
            $('#goSearchItems').click(function(){
                var params = $.extend({
                    "s":"",
                    "status":2,
                    "catId":null,
                    "sort":1,
                    "lowBegin":0,
                    "ps":5,
                    "topEnd":100
                },TM.sheeptitle.util.getParams());
                TM.sheeptitle.ItemsDiag.getItemsDiag($.cookie("isFenxiao"),params);
            });
        },
        setClickSearchBtnEvent : function(){
            $(".clicksearchbtn").unbind("click");
            $('.clicksearchbtn').click(function(){
                switch ($(this).attr("tag")){
                    case "statusall" : $(this).parent().find('input[tag="statusall"]').attr("checked",true);$(this).parent().find('span.selectbackground').removeClass('selectbackground');$(this).addClass('selectbackground');break;
                    case "statusinstock" : $(this).parent().find('input[tag="statusinstock"]').attr("checked",true);$(this).parent().find('span.selectbackground').removeClass('selectbackground');$(this).addClass('selectbackground');break;
                    case "statusonsale" : $(this).parent().find('input[tag="statusonsale"]').attr("checked",true);$(this).parent().find('span.selectbackground').removeClass('selectbackground');$(this).addClass('selectbackground');break;
                    case "sortsaleup" : $(this).parent().find('input[tag="sortsaleup"]').attr("checked",true);$(this).parent().find('span.selectbackground').removeClass('selectbackground');$(this).addClass('selectbackground');break;
                    case "sortsaledown" : $(this).parent().find('input[tag="sortsaledown"]').attr("checked",true);$(this).parent().find('span.selectbackground').removeClass('selectbackground');$(this).addClass('selectbackground');break;
                }
                var params = $.extend({
                    "s":"",
                    "status":2,
                    "catId":null,
                    "cid":null,
                    "sort":1,
                    "lowBegin":0,
                    "ps":5,
                    "topEnd":100
                },TM.sheeptitle.util.getParams());


                TM.sheeptitle.ItemsDiag.getItemsDiag($.cookie("isFenxiao"),params);
            });
            $(".skinsellercat .clicksearchcatbtn").unbind("click");
            $('.skinsellercat .clicksearchcatbtn').click(function(){
                $(this).find('input').attr("checked",true);
                $(this).parent().find('span.selectcolor').removeClass('selectcolor');
                $(this).addClass("selectcolor");
                var params = $.extend({
                    "s":"",
                    "status":2,
                    "catId":null,
                    "cid":null,
                    "sort":1,
                    "lowBegin":0,
                    "ps":5,
                    "topEnd":100
                },TM.sheeptitle.util.getParams());
                if($(this).attr('tag')!="catall"){
                    params.catId = $(this).attr('tag');
                }
                TM.sheeptitle.ItemsDiag.getItemsDiag($.cookie("isFenxiao"),params);
            });
            $(".skintaobaocat .clicksearchcatbtn").unbind("click");
            $('.skintaobaocat .clicksearchcatbtn').click(function(){
                $(this).find('input').attr("checked",true);
                $(this).parent().find('span.selectcolor').removeClass('selectcolor');
                $(this).addClass("selectcolor");
                var params = $.extend({
                    "s":"",
                    "status":2,
                    "catId":null,
                    "cid":null,
                    "sort":1,
                    "lowBegin":0,
                    "ps":5,
                    "topEnd":100
                },TM.sheeptitle.util.getParams());
                if($(this).attr('tag')!="catall"){
                    params.cid = $(this).attr('tag');
                }
                TM.sheeptitle.ItemsDiag.getItemsDiag($.cookie("isFenxiao"),params);
            });
        }
    },TM.sheeptitle.Event);
})(jQuery, window));
