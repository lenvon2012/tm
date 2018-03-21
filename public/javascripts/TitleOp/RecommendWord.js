
/**
 * 关键词推荐
 * @type {*}
 */
var RecWord = RecWord || {};

/**
 * 主要入口
 * @type {*}
 */
RecWord.main = RecWord.main || {};

/**
 * 显示html元素
 * @type {*}
 */
RecWord.show = RecWord.show || {};


RecWord.main = $.extend({
    ruleJson: {
        searchWord: '',
        currentPage: 1,
        pageSize: 10
    },//记录查询的条件，关键字，当前页
    //载入时，初始化
    doInit: function(itemTitle, numIid) {
        RecWord.main.searchWords("", itemTitle, numIid);
        $(".recommendWordBlock #recommendSearchText").keydown(function(event) {
            if (event.keyCode == 13) {//按回车
                $(".recommendWordBlock #recommendSearchBtn").click();
            }
        });
        $(".recommendWordBlock #recommendSearchBtn").click(function() {
            var word = $(".recommendWordBlock #recommendSearchText").val();
            if (word == "") {
                //alert("请先输入查询条件");
                //return;
            }
            RecWord.main.searchWords(word, itemTitle, numIid);
        });
    },
    searchWords: function(word, itemTitle, numIid) {
        ModifyTitle.util.showLoading();
        if (word === undefined || word == null)
            word = "";
        RecWord.main.ruleJson.searchWord = word;
        RecWord.main.ruleJson.currentPage = 1;

        $(".recommendTable tbody").html("");

        var opt = {
            countUrl: "/words/search",
            dataUrl: "/words/search",
            pageSize: 10,
            getRuleData: function(isCurrentPage, currentPage) {//isCurrentPage判断是否需要当前页的条件
                var data = {};
                //if (isCurrentPage == true)
                data.pn = currentPage;
                data.ps = 10;
                data.s = word;
                data.title = itemTitle;
                data.numIid = numIid;
                return data;
            },
            parseTotalCount: function(resultJson) {
                return resultJson.totalPnCount;
            },
            queryCallback: function(dataJson) {
                /*var rowJson = {
                    word: '其他配件',
                    pv: 69,
                    click: 2791,
                    competition: 69
                };
                dataJson = [
                    rowJson, rowJson, rowJson, rowJson, rowJson, rowJson, rowJson, rowJson, rowJson, rowJson
                ];*/
                RecWord.show.showElement(dataJson.res, word);
                ModifyTitle.util.hideLoading();
            }
        };
        $(".recommendWordBlock #recommendWordPaging").setPaging(opt);
    }
}, RecWord.main);


RecWord.show = $.extend({
    showElement: function(wordsJson, word) {
        var str = (!word || word.length == 0)?"亲，请输入关键词来查询哟":"亲，找不到相关的推荐词，换一个核心词试试。T___T";

        $(".recommendTable tbody").html("");
        if (!wordsJson || wordsJson == null || wordsJson.length == 0) {
            $(".recommendWordBlock #recommendWarning").html(str);
            $(".recommendWordBlock #recommendWarning").show();
            $(".recommendTable").hide();
            return;
        } else {
            $(".recommendWordBlock #recommendWarning").hide();
            $(".recommendTable").show();
        }
        $(wordsJson).each(function(index, rowJson) {
            if(rowJson.word != ''){
                var trObj = RecWord.show.createTableRow(rowJson);
                if (index % 2 == 0)
                    trObj.addClass("evenTr");
                else
                    trObj.addClass("oddTr");
                $(".recommendTable tbody").append(trObj);
            }
        });
        $(".recommendTable tbody").find('.addTextWrapperSmall').click(function(){
            ModifyTitle.util.putIntoTitle($(this).text(),$(this));
        });
    },
    createTableRow: function(rowJson) {
        var trObj = $("<tr class='recommendTr'></tr>");
        var wordTd = $("<td class='wordTd'></td>");
        wordTd.html(genKeywordSpan.gen({"text":rowJson.word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'}));
        var saleTd = $("<td class='saleTd'></td>");
        saleTd.html(rowJson.pv);
        var searchTd = $("<td class='searchTd'></td>");
        searchTd.html(rowJson.click);
        var competeTd = $("<td class='competeTd'></td>");
        competeTd.html(rowJson.competition);
        var opTd = $('<td class="opTd"><span class="addTextWrapper tmbtn long-sky-blue-btn" style="margin: 0;border: 0;padding: 0;">查看更多</span></td>');
        var myword = $('<td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper tmbtn long-yellow-btn" style="width: 112px;margin: 0;border: 0;padding:0;">添加到词库</span></td>');
        var searchWord = rowJson.word;
        opTd.find("span").click(function() {
            $(".recommendWordBlock #recommendSearchText").val(searchWord);
            $(".recommendWordBlock #recommendSearchBtn").click();
        });
        myword.find('span').click(function(){
            $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.wordTd').text()},function(data){
                TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
            });
        });
        trObj.append(wordTd);
        trObj.append(saleTd);
        trObj.append(searchTd);
        trObj.append(competeTd);
        trObj.append(opTd);
        trObj.append(myword);
        return trObj;
    }
}, RecWord.show);