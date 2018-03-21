var TM = TM || {};
((function($, window){
    TM.autoTitle = TM.autoTitle || {};
    var autoTitle = TM.autoTitle;
    autoTitle.TXGCatAnalysis = autoTitle.TXGCatAnalysis || {};
    var me = autoTitle.TXGCatAnalysis;
    var level1Cid = null;
    autoTitle.TXGCatAnalysis = $.extend(autoTitle.TXGCatAnalysis, {
        currActive: null,
        currTab: null,
        currCid: null,
        currYear: null,
        currMonth: null,
        body: $('#tmcontainer'),
        alltmpls: $('#alltmpls'),
        nodatatip: $('.nodatatip'),
        showNoDatTip: function(){
            me.showTabData(me.nodatatip);
        },
        showTabData: function(container){
            if (container.hasClass('.active')) {
                return;
            }
            var currActive = me.alltmpls.find('.toptmpl.active');
            currActive.hide();
            currActive.removeClass('active');
            container.addClass('active');
            container.show();
        },

        init: function(container){
            autoTitle.TXGCatAnalysis.initTXGCatAnalysis(container);
        },
        initTXGCatAnalysis: function(container){
            container.empty();
            autoTitle.TXGCatAnalysis.initLevelSelect(container);
        },
        initLevelSelect: function(container){
            $.ajax({
                url: '/ToolInterface/yearLevel',
                data: {},
                type: 'post',
                success: function(data){
                    $(".selectYear").empty();
                    if (data == null || data.length == 0) {
                        TM.Alert.load("没有找到时间信息");
                    }
                    else {
                        for (var i = 0; i < data.length - 1; i++) {
                            var optionYear = $('<option year="' + data[i].year + '">' + data[i].yearName + '</option>');
                            $(".selectYear").append(optionYear);
                        }
                        optionYear = $('<option year="' + data[data.length - 1].year + '" selected=\'selected\'>' + data[data.length - 1].yearName + '</option>');
                        $(".selectYear").append(optionYear);
                    }
                    autoTitle.TXGCatAnalysis.MonthSelect(container);
                }
            });

            $('.tabDiv li span').click(function(){
                var oThis = $(this);
                if (oThis.hasClass('select')) {
                    return;
                }
                var tag = oThis.attr('targetdiv');
                me.renderAdapter(tag);
                $('.tabDiv li span.select').removeClass('select');
                oThis.addClass('select');
            });
        },
        MonthSelect: function(container){
            $(".selectYear").unbind();
            $(".selectYear").change(function(){
                var year = $(".selectYear option:selected").attr('year');
                $.ajax({
                    url: '/ToolInterface/monthLevel',
                    data: {
                        year: year
                    },
                    type: 'post',
                    success: function(data){
                        $(".selectMonth").empty();
                        var Choselevel = 0;
                        if (data == null) {
                            TM.Alert.load("获取月份数据出错");
                        }
                        else {
                            for (var i = 0; i < data.length - 1; i++) {
                                var option = $('<option month="' + data[i].month + '">' + data[i].monthName + '</option>');
                                $(".selectMonth").append(option);
                            }
                            var o = $('<option month="' + data[data.length - 1].month + '" selected=\'selected\'>' + data[data.length - 1].monthName + '</option>');
                            $(".selectMonth").append(o);
                            autoTitle.TXGCatAnalysis.ShowInfo(container);
                            autoTitle.TXGCatAnalysis.Level1Select(container);
                        }
                    }
                });

            });
            $(".selectYear").trigger('change');
        },
        ShowInfo: function(container){
            var year = $(".selectYear option:selected").attr('year');
            var month = $(".selectMonth option:selected").attr('month');
            var levelTwoCid = $(".placeHolder .shopitemcatsapn").attr('cid');
            if(levelTwoCid != null){
                $.ajax({
                    url: '/ToolInterface/getLevelOneCid',
                    data:{levelTwoCid:levelTwoCid,
                        year: year,
                        month: month
                    },
                    type: 'post',
                    success: function(data){
                        if(data != null){
                            level1Cid = data;
                        }
                        if(data == null){
                        }
                    }

                })
            }
        },
        Level1Select: function(container){
            $(".selectMonth").unbind();
            $(".selectMonth").change(function(){
                var year = $(".selectYear option:selected").attr('year');
                var month = $(".selectMonth option:selected").attr('month');
                $.ajax({
                    url: '/ToolInterface/txgfindLevel1',
                    data: {
                        year: year,
                        month: month
                    },
                    type: 'post',
                    success: function(data){
                        $(".selectCat1").empty();
                        if (data == null || data.length == 0) {
                            TM.Alert.load("没有找到一级类目数据");
                        }
                        else {
                            for (var i = 0; i < data.length; i++) {
                                if(level1Cid == null){
                                    level1Cid = data[0].cid;
                                }
                                if (data[i].cid == level1Cid) {
                                    var option = $('<option catId="' + data[i].cid + '" selected="selected">' + data[i].name + '</option>');
                                    $(".selectCat1").append(option);
                                }
                                else {
                                    var option = $('<option catId="' + data[i].cid + '">' + data[i].name + '</option>');
                                    $(".selectCat1").append(option);
                                }
                            }
                        }
                        autoTitle.TXGCatAnalysis.Level2Select(container);
                    }
                })
            });
            $(".selectMonth").trigger('change');
        },
        Level2Select: function(container){
            $(".selectCat1").unbind();
            $(".selectCat1").change(function(){
                var levelOneCid = $(".selectCat1 option:selected").attr('catId');
                var name = $(".selectCat1 option:selected").html();
                var year = $(".selectYear option:selected").attr('year');
                var month = $(".selectMonth option:selected").attr('month');
                var levelTwoCid = $(".placeHolder .shopitemcatsapn").attr('cid');
                $.ajax({
                    url: '/ToolInterface/findLevel2',
                    data: {
                        levelOneCid: levelOneCid,
                        year: year,
                        month: month
                    },
                    type: 'post',
                    success: function(data){
                        $(".selectCat2").empty();
                        var Choselevel = 0;
                        if (data == null) {
                            TM.Alert.load("获取类目数据出错");
                        }
                        else {
                            if(levelTwoCid == null){
                                levelTwoCid = data[0].cid;
                            }
                            if (data.length == 0) {
                                //$(".selectCat2").append($('<option catId="' + catId + '">' + name + '</option>'));
                                Choselevel = 1;
                                $.cookie('Choselevel', 1);
                            }
                            else {
//                                console.log(data);
                                for (var i = 0; i < data.length; i++) {
                                    //第一,有子类目
                                    if (data[i].isParent == true) {
                                        var parentLevel1 = data[i].cid;
                                        if (levelTwoCid == data[i].cid) {
                                            var option = $('<option catId="' + data[i].cid + '" selected = "selected">' + data[i].name + '</option>');
                                            $(".selectCat2").append(option);
                                        }
                                        else {
                                            var option = $('<option catId="' + data[i].cid + '">' + data[i].name + '</option>');
                                            $(".selectCat2").append(option);
                                        }

                                        for (++i; i < data.length; i++) {
                                            if (parentLevel1 == data[i].parentCid) {
                                                if (data[i].isParent == false) {
                                                    if (levelTwoCid == data[i].cid) {
                                                        var option = $('<option catId="' + data[i].cid + '" selected = "selected">' + '&nbsp;&nbsp;|--' + data[i].name + '</option>');
                                                        $(".selectCat2").append(option);
                                                    }else{
                                                        var option = $('<option catId="' + data[i].cid + '">' + '&nbsp;&nbsp;|--' + data[i].name + '</option>');
                                                        $(".selectCat2").append(option);
                                                    }
                                                }
                                                else {
                                                    if (levelTwoCid = data[i].cid) {
                                                        var option = $('<option catId="' + data[i].cid + '" selected = "selected">' + '&nbsp;&nbsp;|--' + data[i].name + '</option>');
                                                        $(".selectCat2").append(option);
                                                    }else{
                                                        var option = $('<option catId="' + data[i].cid + '">' + '&nbsp;&nbsp;|--' + data[i].name + '</option>');
                                                        $(".selectCat2").append(option);
                                                    }
                                                    //第二
                                                    var parentLevel2 = data[i].cid;
                                                    for (++i; i < data.length; i++) {
                                                        if (parentLevel2 == data[i].parentCid) {
                                                            if (levelTwoCid = data[i].cid) {
                                                                var option = $('<option catId="' + data[i].cid + '" selected = "selected">' + '&nbsp;&nbsp;&nbsp;&nbsp;|--' + data[i].name + '</option>');
                                                                $(".selectCat2").append(option);
                                                            }else{
                                                                var option = $('<option catId="' + data[i].cid + '">' + '&nbsp;&nbsp;&nbsp;&nbsp;|--' + data[i].name + '</option>');
                                                                $(".selectCat2").append(option);
                                                            }
                                                        }
                                                        else {
                                                            i--;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            else {
                                                i--;
                                                break;
                                            }
                                        }
                                    }
                                    else {
                                        if (data[i].cid == levelTwoCid) {
                                            var option = $('<option catId="' + data[i].cid + '" selected = "selected">' + data[i].name + '</option>');
                                            $(".selectCat2").append(option);
                                        }else{
                                            var option = $('<option catId="' + data[i].cid + '">' + data[i].name + '</option>');
                                            $(".selectCat2").append(option);
                                        }
                                    }

                                }
                                autoTitle.TXGCatAnalysis.initPropSelect(container);
                                $('.selectCat2').trigger("change");
                            }
                        }
                    }
                })
            });
            $(".selectCat1").trigger('change');
        },
        renderAdapter: function(tag, cid, cname, year, month){
            me.currTag = tag || me.currTag;
            me.currCid = cid || me.currCid;
            me.cname = cname || me.cname;
            me.currYear = year || me.currYear;
            me.currMonth = month || me.currMonth;
            $('#cnametext').empty();
            me.cname = $.trim(me.cname);
            if (me.cname.indexOf("|--") == 0) {
                me.cname = me.cname.substring(3);
            }
            switch (tag) {
                case 'topitems': //热销宝贝
                    $('#cnametext').html('"<b>' + me.cname + '</b>"&nbsp;&nbsp; 类目&nbsp;&nbsp;&nbsp热销宝贝');
                    me.initTopItems(tag, me.currCid, me.currYear, me.currMonth);
                    break;
                case 'topshops': //热销店铺
                    $('#cnametext').html('"<b>' + me.cname + '</b>"&nbsp;&nbsp; 类目&nbsp;&nbsp;&nbsp热销店铺');
                    me.initTopShops(tag, me.currCid, me.currYear, me.currMonth);
                    break;
                case 'propsale': //热销属性
                    $('#cnametext').html('"<b>' + me.cname + '</b>"&nbsp;&nbsp; 类目&nbsp;&nbsp;&nbsp属性分析');
                    me.initPropSpans(tag, me.currCid, me.currYear, me.currMonth);
                    break;
                case 'delist': //上下架时间分布    
                    $('#cnametext').html('"<b>' + me.cname + '</b>"&nbsp;&nbsp; 类目&nbsp;&nbsp;&nbsp上下架时间分布');
                    me.initDelist(tag, me.currCid, me.currYear, me.currMonth);
                    break;
                case 'priceDuration': //成交价格分布
                    $('#cnametext').html('"<b>' + me.cname + '</b>"&nbsp;&nbsp; 类目&nbsp;&nbsp;&nbsp成交价格分布');
                    me.initPriceDuration(tag, me.currCid, me.currYear, me.currMonth);
                    break;
                case 'hotwords'://热门搜索词
                    $('#cnametext').html('"<b>' + me.cname + '</b>"&nbsp;&nbsp; 类目&nbsp;&nbsp;&nbsp热门词');
                    me.initHotWords(tag, me.currCid, me.currYear, me.currMonth);
                    break;
            }
        },
        //热销100
        initTopItems: function(tag, cid, year, month){
            var holder = me.alltmpls.find('.' + tag);
            var rowTmpl = holder.find('.tmpl');
            var tbody = holder.find('tbody');
            holder.find('.pagenav').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    param: {
                        cid: cid,
                        year: year,
                        month: month
                    }, //modify by uttp
                    on: true,
                    dataType: 'json',
                    url: "/ToolInterface/topItems",
                    callback: function(data){
                        if (!data || !data.res || data.res.length == 0) {
                            return;
                        }
                        var rows = rowTmpl.tmpl(data.res);
                        tbody.empty();
                        tbody.append(rows);
                    }
                }
            });
            me.showTabData(holder);
        },
        //热门搜索词
        initHotWords: function(tag, cid, year, month){
            var holder = me.alltmpls.find('.' + tag);
            var rowTmpl = holder.find('.tmpl');
            var tbody = holder.find('tbody');
            holder.find('.pagenav').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    param: {
                        cid: cid,
                        year: year,
                        month: month
                    }, //modify by uttp
                    on: true,
                    dataType: 'json',
                    url: "/ToolInterface/hotWords",
                    callback: function(data){
                        if (!data || data.length == 0) {
                            return;
                        }
                        var rows = rowTmpl.tmpl(data.res);
                        tbody.empty();
                        tbody.append(rows);
                    }
                }
            });
            me.showTabData(holder);
        },
        //热门店铺
        initTopShops: function(tag, cid, year, month){
            var holder = me.alltmpls.find('.' + tag);
            var rowTmpl = holder.find('.tmpl');
            var tbody = holder.find('tbody');
            holder.find('.pagenav').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    param: {
                        cid: cid,
                        year: year,
                        month: month
                    },
                    on: true,
                    dataType: 'json',
                    url: "/ToolInterface/hotShop",
                    callback: function(data){
                        if (!data || data.length == 0) {
                            return;
                        }
                        var rows = rowTmpl.tmpl(data.res);
                        tbody.empty();
                        tbody.append(rows);
                    }
                }
            });
            me.showTabData(holder);
        },
        //热销属性分布
        initPropSpans: function(tag, cid, year, month){
            var holder = me.alltmpls.find('.' + tag);
            $.get('/ToolInterface/catHotProps1', {
                cid: cid,
                year: year,
                month: month
            }, function(res){
                if (!res || res.length == 0) {
                    me.showNoDatTip();
                    return;
                }
                var htmls;
                htmls = holder.find('.tmpl').tmpl(res);
                var propspans = holder.find('.propspans');
                propspans.empty();
                propspans.append(htmls);
                holder.find('.propname').click(function(){

                    var pname = $(this).attr('pname');
                    $.each(res, function(i, elem){
                        if (elem.pname == pname) {
                            var highchartsHolder = holder.find('.placeHolder');
                            highchartsHolder.empty();
                            highchartsHolder.html('<div id="catpropsale"></div>');
                            me.genHighChart('catpropsale', elem.list);
                        }
                    })
                })
                holder.find('.propname:eq(0)').trigger('click');
            });
            me.showTabData(holder);
        },
        initPriceDuration: function(tag, cid, year, month){
            //alert(tag+ " " + cid + " " + year + " " + month);
            //alert("hehe");
            var holder = me.alltmpls.find('.' + tag);
            $.get('/ToolInterface/priceRange', {
                cid: cid,
                year: year,
                month: month
            }, function(data){
                //alert(cid+ " " +  year + " ")
                if (!data || data.length == 0) {
                    me.showNoDatTip();
                    return;
                }
                var highchartsHolder = holder.find('.placeHolder');
                highchartsHolder.empty();
                highchartsHolder.html('<div id="priceDuartionHolder" style="height:800px;"></div>');
                me.genHighChart('priceDuartionHolder', data);
            });
            me.showTabData(holder);
        },
        initDelist: function(tag, cid, year, month){
            var holder = me.alltmpls.find('.' + tag);
            TM.DelistSearch.init.doInit(holder, {
                'url': '/ToolInterface/delistTimeRange'
            });
            TM.DelistSearch.show.doAnalyse({
                cid: cid,
                year: year,
                month: month
            });
            me.showTabData(holder);
        },
        initPropSelect: function(container){
            $('.selectCat2').unbind('change').change(function(){
                $(".propselect").empty();
                var selected = $(".selectCat2 option:selected");
                var cid = selected.attr('catId');
                var cname = selected.text();
                var year = $(".selectYear option:selected").attr('year');
                var month = $(".selectMonth option:selected").attr('month');
                //alert(year+month);
                me.renderAdapter($('.tabDiv .select').attr('targetdiv'), cid, cname, year, month);
            });
        },

        createNewHighChart: function(cid, pid, container){
            container.find('#catanalysisoplogs').empty();
            $.get('/ToolInterface/catPropSale', {
                cid: cid,
                pid: pid
            }, function(res){
                if (res != null && res.length > 0) {
                    autoTitle.TXGCatAnalysis.genHighChart(pid, res);
                }
            });
        },
        CreateNewTable: function(container){
            var cid = $(".selectCat2 option:selected").attr('catId');
            var htmls = [];
            $.get('/ToolInterface/catHotProps1', {
                cid: cid
            }, function(res){
                container.find('table.catanalysisoplogs').remove();
                htmls.push('<table class="catanalysisoplogs"><thead><th style="width:120px;">属性名</th><th>热销属性词/搜索热度</th></thead><tbody>');
                if (!res || res.length == 0) {
                    // TODO no res temp...
                    htmls.push('<tr><td colspan="2">您选择的类目暂无热销属性</td></tr></tbody></table>');
                    container.append(htmls.join(""));
                    return;
                }
                else {
                    $.each(res, function(i, prop){
                        htmls.push('<tr class="propTr"><td class="greybottom"><div><b>' + prop.pname + '</b></div><div>热度:<b class="red"> ' + prop.pv + '</b></div></td>');
                        htmls.push('<td class="greybottom" ><div style="width: 670px;height: 800px;" id="chart' + prop.pid + '"></div> </td></tr>');
                    })
                }
                htmls.push('</tbody></table>');
                container.append($(htmls.join("")));
                autoTitle.TXGCatAnalysis.genPropCharts(res);
            });
        },
        genPropCharts: function(res){
            $.each(res, function(i, prop){
                autoTitle.TXGCatAnalysis.genHighChart(prop.pid, prop.list);
            })
        },
        genHighChart: function(id, list){
            var target = $('#' + id);

            if (me.currActive != null) {
                me.currActive.empty();
                me.currActive.hide();
            }

            autoTitle.TXGCatAnalysis.currActive = target;
            if (list === undefined || list == null) {
                target.empty();
                return;
            }
            if (list.length == 0) {
                target.empty();
                return;
            }
            var xAxis = autoTitle.TXGCatAnalysis.createXAxis(list);
            var pvArr = autoTitle.TXGCatAnalysis.createSaleArr(list);
            //var clickArr = autoTitle.TXGCatAnalysis.createClickArr(list);
            chart = new Highcharts.Chart({
                chart: {
                    renderTo: id,
                    inverted: true,
                    marginBottom: 50,
                    height: list.length * 25 + 100,
                    defaultSeriesType: 'column'//图表类型line(折线图)
                },
                credits: {
                    enabled: false //右下角不显示LOGO
                },
                title: {
                    text: '行业分析'
                }, //图表标题
                xAxis: { //x轴
                    categories: xAxis, //x轴标签名称
                    gridLineWidth: 1, //设置网格宽度为1
                    lineWidth: 4, //基线宽度
                    labels: {
                        //x轴标签位置：距X轴下方26像素
                        //倾斜度
                        align: 'right'
                    }
                },
                yAxis: [{ //y轴
                    title: {
                        text: ''
                    }, //标题
                    lineWidth: 2 //基线宽度
                }],
                plotOptions: { //设置数据点
                    line: {
                        dataLabels: {
                            enabled: true //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                series: [{ //数据列
                    name: '热度',
                    data: pvArr,
                    yAxis: 0
                }],
                legend: {
                    layout: "horizontal",
                    align: 'center',
                    verticalAlign: 'bottom',
                    y: 10
                }
            });
        },
        createXAxis: function(list){
            var xAxis = [];
            $(list).each(function(i, name){
                //alert(typeof name.name)
                if (name.name != undefined) {
                    if (name.name.length > 7)
                        name.name = name.name.substring(0, 9) + "...";
                }
                xAxis.push(name.name || name.vname || name.priceRange);
            });

            return xAxis;
        },
        createSaleArr: function(list){
            var pvArr = [];
            $(list).each(function(i, name){
                pvArr.push(parseInt(name.value || name.pv || name.sale || name.totalTradeNum || name.totleTradeNum));

            });
            return pvArr;
        },
        createClickArr: function(list){
            var clickArr = [];
            $(list).each(function(i, name){
                clickArr.push(name.click)
            });
            return clickArr;
        },
        CreateTable: function(container){
            var cid = $(".selectCat2 option:selected").attr('catId');
            var htmls = [];
            $.get('/ToolInterface/catHotProps1', {
                cid: cid
            }, function(res){
                container.find('table.catanalysisoplogs').remove();
                htmls.push('<table class="catanalysisoplogs"><thead><th style="width:120px;">属性名</th><th>热销属性词/搜索热度</th></thead><tbody>');
                if (!res || res.length == 0) {
                    // TODO no res temp...
                    htmls.push('<tr><td colspan="2">您选择的类目暂无热销属性</td></tr></tbody></table>');
                    container.append(htmls.join(""));
                    return;
                }
                else {

                    var pPvMax = 0;
                    $.each(res, function(i, prop){
                        if (i == 0) {
                            pPvMax = prop.pv;
                        }

                        htmls.push('<tr class="propTr"><td class="greybottom"><div><b>' + prop.pname + '</b></div><div>热度:<b class="red"> ' + prop.pv + '</b></div></td>');
                        // values...
                        var maxWidth = 300;
                        htmls.push('<td class="greybottom" > <table width="100%">');
                        var vPvMax = 0;
                        $.each(prop.list, function(j, value){
                            if (j == 0 && vPvMax < value.pv) {
                                vPvMax = value.pv;
                            }
                            if (vPvMax < 1) {
                                vPvMax = 1;
                            }

                            var currWidth = (j == 0) ? maxWidth : (value.pv * maxWidth / vPvMax);
                            currWidth = Math.round(currWidth);
                            if (currWidth < 5) {
                                currWidth = 5;
                            }
                            htmls.push('<tr><td width="140px"><span class="addTextWrapper shadowbase" style="padding-top:5px;"><img src="/img/btns/addblue.png" >' + value.name + '</span></td><td style="text-align: left;width:300px;"><div style="width:' +
                                currWidth +
                                'px;height:6px;border-top: 6px solid #2D8ABE"></div></td><td>' +
                                value.pv +
                                '</td></tr>');
                        });

                        htmls.push('</table></td></tr>');
                    })
                }
                htmls.push('</tbody></table>');
                var res = $(htmls.join(''));
                res.find('.addTextWrapper').click(function(){
                    autoTitle.util.putIntoTitle($(this).text(), $(this), container);
                });
                container.append(res);
            });
        }
    })

})(jQuery, window));
