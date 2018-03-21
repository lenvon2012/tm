var TM = TM || {};
((function ($, window) {
    TM.MyWords = TM.MyWords || {};
    var myWords = TM.MyWords;
    myWords.Init = myWords.Init || {};
    myWords.Init = $.extend({
        show : function(){
            myWords.Init.setTips();
            //var tbody = $('<tbody id="ML_result"></tbody>');
            $('.paging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/KeyWords/searchmywords",
                    param:{},
                    callback:function(data){
                        $('#ML_result').empty()
                        var mywordsMap = new Map();
                        var allwords = "";
                        if(data != null && data.res.length > 0){
                            $(data.res).each(function(i,myword){
                                if(myword.word != "") {
                                    allwords += myword.word + ",";
                                    mywordsMap.set(myword.word, "~");
                                    //$('#ML_result').append($('<tr class="'+even+'"><td class="word-content">'+myword.word+'</td><td>0</td><td>0</td><td>0</td><td>0</td><td class="" style=""><span class="delete-myword btn btn-danger">删除</span> </td></tr>'))
                                }
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
                                        // 判断关键词是否有展现量等数据
                                        if(mywordsMap.get(wordBean.word) != '~'){
                                            return;
                                        };
                                        mywordsMap.delete(wordBean.word);
                                        var competition = wordBean.competition < 0 ? '~' : wordBean.competition;
                                        $('#ML_result').append($('<tr class="'+even+'"><td class="word-content">'+wordBean.word+'</td><td>'+wordBean.pv+'</td><td>'+competition+'</td><td>'+wordBean.price/100+'</td><td class="" style=""><span class="delete-myword tmbtn red-short-btn">删除</span> </td></tr>'))
                                    });
                                };
                                // 没有展现量等数据的显示方式
                                mywordsMap.forEach(function(value, key) {
                                    $('#ML_result').append($('<tr class="'+even+'"><td class="word-content">'+key+'</td><td>'+value+'</td><td>'+value+'</td><td>'+value+'</td><td class="" style=""><span class="delete-myword tmbtn red-short-btn">删除</span> </td></tr>'))
                                }, mywordsMap);
                                // 添加删除的点击事件
                                $('#ML_result').find('.delete-myword').click(function(data){
                                    if(confirm("确定要删除该关键词？")){
                                        var $this = $(this);
                                        $.post('/KeyWords/deleteMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                                            if(data == "删除成功"){
                                                TM.Alert.load("<p style='text-align: center'>删除成功</p>",400,300,function(){
                                                    $this.parent().parent().fadeOut(1000);
                                                },false,"删除成功",3000);
                                            } else {
                                                TM.Alert.load(data);
                                            }
                                        })
                                    }
                                });
                            });
                        }else{
                            $('#ML_result').append($('<td colspan="7"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，没有搜索到相关的直通车热词呢</p></td>'));
                        }
                    }
                }
            });
            /*$.get('/KeyWords/searchmywords',function(data){
                if(data != null && data.res.length > 0){
                    $(data.res).each(function(i,myword){
                        if(myword.word != "") {
                            tbody.append($('<tr><td>'+myword.word+'</td><td>0</td><td>0</td><td>0</td><td>0</td><td class="delete-myword" style="cursor: pointer;color: red;">删除</td></tr>'))
                        }
                    })
                    tbody.find('.delete-myword').click(function(data){
                        $.post('/KeyWords/deleteMyWord',{word:$(this).text()},function(data){
                            TM.Alert.load(data);
                        })
                    });
                }
            });*/

        },
        setTips : function(){
            $('.myword-word').qtip({
                content: {
                    text: "我的关键词"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });
            $('.myword-pv').qtip({
                content: {
                    text: "该关键词展现给买家的次数"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });
            $('.myword-click').qtip({
                content: {
                    text: "关键词被点击的次数"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });
            $('.myword-competition').qtip({
                content: {
                    text: "反应购买该关键词的卖家多少"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });
            $('.myword-price').qtip({
                content: {
                    text: "关键词的平均出价"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });
            $('.myword-option').qtip({
                content: {
                    text: "删除关键词"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });
        }
    },myWords.Init);
})(jQuery,window));
