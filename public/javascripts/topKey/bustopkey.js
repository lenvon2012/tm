
var TM = TM || {};
((function ($, window) {
    TM.BusTopKey = TM.BusTopKey || {};
    var BusTopKey = TM.BusTopKey;

    BusTopKey.Init = BusTopKey.Init || {};
    BusTopKey.Init = $.extend({
        init : function(container, numIid){
            // 这是全店的
            if(numIid === undefined || numIid == null) {
                $.get("/Buskey/myMostCat", function(data){
                    BusTopKey.Init.genLevel(data);
                    BusTopKey.Init.initLevelSelect();
                    BusTopKey.Event.setStaticEvent();
                });
            }
            // 这是宝贝的
            else {
                $.get("/Buskey/itemBusCatPlay", {numIid:numIid}, function(data){
                    // 如果宝贝找不到对应的类目，那么就用全店的
                    if(data ===undefined || data == null || data.success == false) {
                        $.get("/Buskey/myMostCat", function(data){
                            BusTopKey.Init.genLevel(data);
                            BusTopKey.Init.initLevelSelect();
                            BusTopKey.Event.setStaticEvent();
                        });
                    }
                    BusTopKey.Init.genLevel(data);
                    BusTopKey.Init.initLevelSelect();
                    BusTopKey.Event.setStaticEvent();
                });
            }

            BusTopKey.Init.initMycats();
        },
        initMycats :function(){
            $.get("/Buskey/myAllCats", function(data){
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    return;
                }
                if(data.length > 0) {
                    $(data).each(function(i, cat){
                        $('.catName').append($('<span class="inlineblock" cid="'+cat.cid+'">'+cat.name+'</span>'));
                    });
                    $('.myAllCats').show();
                    $('.catName').show();
                    $('.catName span').unbind("click").click(function(){
                        var cid = $(this).attr("cid");
                        $.get("/Buskey/getCatLevel", {cid : cid}, function(data){
                            BusTopKey.Init.genLevel(data);
                            BusTopKey.Init.Level2Select();
                        });
                    });
                }
            })
        },
        genLevel : function(data){
            // 这段是生成当前店铺对应的类目
            BusTopKey.Init.level0 = -1;
            BusTopKey.Init.level1 = -1;
            BusTopKey.Init.level2 = -1;
            if(data === undefined || data == null) {
                // do not change level1,2,3
            } else {
                if(data.success == false) {
                    // do not change level1,2,3
                }
                if(data.length > 0) {
                    $(data).each(function(i, cat){
                        switch (parseInt(cat.level)) {
                            case 0 :
                                BusTopKey.Init.level0 = cat.catId;
                                break;
                            case 1 :
                                BusTopKey.Init.level1 = cat.catId;
                                break;
                            case 2 :
                                BusTopKey.Init.level2 = cat.catId;
                                break;
                        }
                    });
                }
            }

        },
        initLevelSelect : function(){
            $.ajax({
                //async : false,
                url : '/Buskey/findLevel1',
                data : {},
                type : 'post',
                success : function(data) {
                    //初始化第一个
                    $(".selectCat1").empty();

                    if(data == null || data.length == 0){
                        TM.Alert.load("没有找到一级类目数据");
                    } else {
                        for(var i=0;i<data.length;i++){
                            var option=$('<option catId="'+data[i].catId+'">'+data[i].name+'</option>');
                            $(".selectCat1").append(option);
                        }
                    }
                   // $(".selectCat1").append($('<option></option>'));
                    //设置事件$('.XXX).change(function(){});
                    BusTopKey.Init.Level2Select();
                }
            });
        } ,
        Level2Select : function(){
            $(".selectCat1").unbind();
            $(".selectCat1").change(function(){
                var catId = $(".selectCat1 option:selected").attr('catId');
                var name = $(".selectCat1 option:selected").html();

                $.ajax({
                    url:'/Buskey/findLevel2or3',
                    data:{catId:catId},
                    type:'post',
                    success:function(data){
                        $(".selectCat2").empty();
                        var Choselevel = 0;
                        if(data == null){
                            TM.Alert.load("获取类目数据出错");
                        } else if(data.length==0){
                            $(".selectCat2").append($('<option catId="'+catId+'">'+name+'</option>'));
                            $(".selectCat3").empty();
                            $(".selectCat3").append($('<option catId="'+catId+'">'+name+'</option>'));
                            Choselevel = 1;
                            $.cookie('Choselevel',1);
                        } else{
                            for(var i=0;i<data.length;i++){
                                var option=$('<option catId="'+data[i].catId+'">'+data[i].name+'</option>');
                                $(".selectCat2").append(option);
                            }

                        }


                        BusTopKey.Init.Level3Select(Choselevel);

                    }

                })
            });
            if(BusTopKey.Init.level0 > 0) {
                $('.selectCat1 option[catid="'+BusTopKey.Init.level0+'"]').attr("selected", true);
                $(".selectCat1").trigger('change');
            } else {
                $(".selectCat1").trigger('change');
            }

        } ,
        Level3Select : function(Choselevel){
            if(Choselevel==1)      {
                BusTopKey.Init.CreateTable(Choselevel);
            } else {
                $(".selectCat2").unbind('change');
                $(".selectCat2").change(function(){
                    var catId = $(".selectCat2 option:selected").attr('catId');
                    var name = $(".selectCat2 option:selected").html();
                        $.ajax({
                            url:'/Buskey/findLevel2or3',
                            data:{catId:catId},
                            type:'post',
                            success:function(data){
                                $(".selectCat3").empty();
                                var Choselevel = -1;
                                if(data == null){
                                    TM.Alert.load("获取类目数据出错");
                                } else if(data.length == 0){
                                    $(".selectCat3").append($('<option catId="'+catId+'">'+name+'</option>'));
                                    Choselevel = 2;
                                    $.cookie('Choselevel',2);
                                } else {
                                    for(var i=0;i<data.length;i++){
                                        var option=$('<option catId="'+data[i].catId+'">'+data[i].name+'</option>');
                                        $(".selectCat3").append(option);
                                    }
                                    $.cookie('Choselevel',0);
                                }


                                BusTopKey.Init.CreateTable(Choselevel);

                            }

                        })
                })  ;
                if(BusTopKey.Init.level1 > 0) {
                    $('.selectCat2 option[catid="'+BusTopKey.Init.level1+'"]').attr("selected", true);
                    $(".selectCat2").trigger('change');
                } else {
                    $(".selectCat2").trigger('change');
                }
            }
        }  ,
        TableChange : function(Choselevel){
            $(".selectCat3").unbind('change');
            $(".selectCat3").change(function(){
                var catId = $(".selectCat3 option:selected").attr('catId');
                $(".bustopkey-page").tmpage({
                    currPage: 1,
                    pageSize: 15,
                    pageCount: 1,
                    ajax:{
                        on: true,
                        param: {catId:catId,Choselevel:Choselevel,ps:15},
                        dataType: 'json',
                        url: '/Buskey/buslistlevel3',
                        callback:function(dataJson){
                            if(!dataJson || !dataJson.isOk){
                                TM.Alert.load("数据获取发生错误，请重试或联系客服");
                            } else{
                                 $('.busSearch-tbody').remove();
                                if(dataJson.res.length > 0){
                                    $('.catWordTable').append(BusTopKey.Init.CreateTbody(dataJson.res));
                                } else {
                                    $('.catWordTable').append('<tbody class="busSearch-tbody" ><tr><td colspan="8" style="height:40px;">亲，暂无数据哦</td></tr></tbody>');
                                }
                            }
                        }
                    }
                })


            }) ;
            if(BusTopKey.Init.level2 > 0) {
                $('.selectCat3 option[catid="'+BusTopKey.Init.level2+'"]').attr("selected", true);
                $(".selectCat3").trigger('change');
            } else {
                $(".selectCat3").trigger('change');
            }
        } ,
        CreateTable : function(Choselevel){
            $(".selectCat3").unbind('change');
            $(".selectCat3").change(function(){
                var cat1Id = $(".selectCat1 option:selected").attr('catId');
                var cat2Id, cat3Id;
                if(Choselevel == 1){
                    cat2Id = null;
                    cat3Id = null;
                }else if(Choselevel == 2){
                    cat2Id = $(".selectCat2 option:selected").attr('catId');
                    cat3Id = null;
                } else {
                    cat2Id = $(".selectCat2 option:selected").attr('catId');
                    cat3Id = $(".selectCat3 option:selected").attr('catId');
                }

                $(".bustopkey-page").tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax:{
                        on: true,
                        param: {cat1Id:cat1Id,cat2Id:cat2Id,cat3Id:cat3Id},
                        dataType: 'json',
                        url: '/Buskey/searchWords',
                        callback:function(dataJson){
                            if(!dataJson || !dataJson.isOk){
                                TM.Alert.load("数据获取发生错误，请重试或联系客服");
                            } else{
                                $('.busSearch-tbody').remove();
                                if(dataJson.res.length > 0){
                                    $('.catWordTable').append(BusTopKey.Init.CreateTbody(dataJson.res));
                                } else {
                                    $('.catWordTable').append('<tbody class="busSearch-tbody" ><tr><td colspan="8" style="height:40px;">亲，暂无数据哦</td></tr></tbody>');
                                }
                            }
                        }
                    }
                })


            }) ;

            if(BusTopKey.Init.level2 > 0) {
                $('.selectCat3 option[catid="'+BusTopKey.Init.level2+'"]').attr("selected", true);
                $(".selectCat3").trigger('change');
            } else {
                $(".selectCat3").trigger('change');
            }
        } ,
        CreateTbody :function(results) {
            var tbody = $('<tbody class="busSearch-tbody" ></tbody>');
            var allwords = "";
            $(results).each(function(i,result){
                if(result.word != "") {
                    allwords += result.word + ",";
                }
                /*var rate = (result.pv == 0)?0:(result.click/result.pv);
                var clickRate = new Number(rate).toPercent(2);
                tbody.append($('<tr>'+
                    '<td class="word-content">'+result.word+'</td>'+
                    '<td>'+result.click+'</td>'+
                    '<td>'+result.pv+'</td>'+
                    '<td>'+clickRate+'</td>'+
                    '<td>'+result.competition+'</td>'+
                    '<td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper btn btn-info">添加到我的词库</span>'
//                        + genKeywordSpan.gen({"text":'添加到词库',"callback":"","enableStyleChange":true})
                        + '</td></tr>'));*/
            });
            var isEven = false;
            $.post('/Words/tmEqual',{words:allwords},function(wordBeans){
                if(wordBeans != null && wordBeans.length > 0){
                    var even = "";
                    if(isEven) {
                        even = "even";
                        isEven = false;
                    } else {
                        isEven = true;
                    }
                    $(wordBeans).each(function(i,wordBean){
                        var scount = wordBean.scount > 0 ? wordBean.scount:"-";
                        var score;
                        if(wordBean.score > 0){
                            score = wordBean.score;
                        } else if(wordBean.scount > 0){
                            score = Math.round(wordBean.pv/wordBean.scount).toFixed(0)
                        } else {
                            score = "-";
                        }
                        var isPvOverTenThousand = parseInt(wordBean.pv) > 10000 ? new Number(wordBean.pv).toTenThousand(1) : wordBean.pv;
                        var isClickOverTenThousand = parseInt(wordBean.click) > 10000 ? new Number(wordBean.click).toTenThousand(1) : wordBean.click;
                        tbody.append($('<tr>'+
                            '<td class="word-content">'+wordBean.word+'</td>'+
                            '<td>'+isPvOverTenThousand+'</td>'+
                            '<td>'+isClickOverTenThousand+'</td>'+
                            '<td>'+scount+'</td>'+
                            '<td>'+score+'</td>'+
                            '<td>'+wordBean.price/100+'</td>'+
                            '<td class="add-to-mywords" style="cursor: pointer;"><span class="tmbtn sky-blue-btn">添加到词库</span>'
//                        + genKeywordSpan.gen({"text":'添加到词库',"callback":"","enableStyleChange":true})
                            + '</td></tr>'))
                    });
                    tbody.find('tr:odd').addClass('even');
                    tbody.find('.add-to-mywords span').click(function(){
                        $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                            TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
                        });
                    });
                }

            });
            return tbody;

        }
    },BusTopKey.Init);

    BusTopKey.Util = BusTopKey.Util || {};
    BusTopKey.Util = $.extend({
        genSortBy : function($this){
            var bywhat = $this.attr('sortby');
            var down,sortBy;
            if($this.hasClass('sort-down')){
                $this.removeClass('sort-down');
                $this.addClass('sort-up');
                down = false;
            } else {
                $this.removeClass('sort-up');
                $this.addClass('sort-down');
                down = true;
            }
            switch(bywhat){
                case "aclick":{
                    if(down){sortBy = " order by abs(click) desc ";break;}
                    else {sortBy = " order by abs(click) asc ";break;}
                }
                case "pv":{
                    if(down){sortBy = " order by abs(pv) desc ";break;}
                    else {sortBy = " order by abs(pv) asc ";break;}
                }
                case "competition":{
                    if(down){sortBy = " order by abs(competition) desc ";break;}
                    else {sortBy = " order by abs(competition) asc ";break;}
                }
            }
            return sortBy;
        }
    },BusTopKey.Util);

    BusTopKey.Event = BusTopKey.Event || {};
    BusTopKey.Event = $.extend({
        setStaticEvent : function(){
            BusTopKey.Event.setSortByClickEvent();
        },
        setSortByClickEvent : function(){
            $('.sort-td').click(function(){
                var cat1Id = $(".selectCat1 option:selected").attr('catId');
                var cat2Id, cat3Id;
                if($.cookie('Choselevel') == 1){
                    cat2Id = null;
                    cat3Id = null;
                }else if($.cookie('Choselevel') == 2){
                    cat2Id = $(".selectCat2 option:selected").attr('catId');
                    cat3Id = null;
                } else {
                    cat2Id = $(".selectCat2 option:selected").attr('catId');
                    cat3Id = $(".selectCat3 option:selected").attr('catId');
                }
                var $this = $(this);
                var sortBy = BusTopKey.Util.genSortBy($this);
                $(".bustopkey-page").tmpage({
                    currPage: 1,
                    pageSize: 15,
                    pageCount: 1,
                    ps: 10,
                    ajax:{
                        on: true,
                        param: {cat1Id:cat1Id,cat2Id:cat2Id,cat3Id:cat3Id,sortBy:sortBy, ps : 10},
                        dataType: 'json',
                        url: '/Buskey/searchWords',
                        callback:function(dataJson){
                            if(!dataJson || !dataJson.isOk){
                                TM.Alert.load("数据获取发生错误，请重试或联系客服");
                            } else{
                                $('.busSearch-tbody').remove();
                                if(dataJson.res.length > 0){
                                    $('.catWordTable').append(BusTopKey.Init.CreateTbody(dataJson.res));
                                } else {
                                    $('.catWordTable').append('<tbody class="busSearch-tbody" ><tr><td colspan="8" style="height:40px;">亲，暂无数据哦</td></tr></tbody>');
                                }
                            }
                        }
                    }
                })
            });
        }
    },BusTopKey.Event);
})(jQuery,window));

