var genKeywordSpan = genKeywordSpan || {};

((function ($, window) {
    genKeywordSpan.gen = function(options){
        options = $.extend({
            text:'',
            callback:'',
            enableStyleChange:true,
            spanClass:'addTextWrapper',
            enableShadow : true,
            addBtn : true
        }, options);

//        var spanObj = $("<span style='border:solid 2px #5CADAD;margin: 0px 5px 15px 5px; padding: 5px 15px;font-size: 14px;display:-moz-inline-box;  display: inline-block;cursor: pointer'></span>");
//        var plusObj = $("<img src='/public/images/plus2.png' style='margin-left:-6px; margin-right: 8px;'>");
//        spanObj.append(plusObj);
//        spanObj.append(options.text);
//        spanObj.click(function(){
//            options.callback(options.text,spanObj);
//        });
        return "<span class='"+ (options.enableShadow?'shadowbase ':'')+options.spanClass+"'>"
//                        + (options.addBtn ? "<img src='/img/btns/addblue.png' style='margin-left:-6px; margin-right: 4px;'>":"")
            + (options.addBtn ? "<img src='/img/btns/addblue.png' >":"")
                        + options.text + "</span>";

//        return spanObj;
    }
})(jQuery, window));