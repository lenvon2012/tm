var TM = TM || {};

TM.Pop = TM.Pop || {};

((function ($, window) {
    var me = TM.Pop;

    TM.Pop.init = function(){
        $.getScript('/js/Kits/comment.js',function(){
            TM.Comment.init($('.popuparize'));
        });
    }

})(jQuery,window))