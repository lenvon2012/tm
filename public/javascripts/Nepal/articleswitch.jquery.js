/**
 * 问隔向左滚动
 */
(function($){
    /**
     * articleswitch 问隔向左滚动
     * time 动画时间 ( 默认 2600 毫秒 )
     * switch_time 显示时间 ( 默认 800 毫秒 )
     * width 单块 宽度
     * content 内容 class
     */
    $.fn.articleswitch = function( option ) {
        
        option = $.extend( {}, $.fn.articleswitch.option, option );
        
        var container   = $( this );
        var content     = '.' + option.content;
        var switch_time = option.switch_time;
        var switch_timer;
        var switch_the;
        var time        = option.time;
        var the_width   = option.width;
        
        if ( container.find( content ).children().length < 2 ) {
            return;
        }

        container.children( content ).hover(function(){
            clearInterval( switch_timer );
        }, function(){
            // item_switch();
            switch_timer = setInterval(function() {item_switch();}, time );
        });

        switch_timer = setInterval(function(){item_switch();}, time );

        function item_switch() {
            container.children( content ).animate( {'margin-left':-the_width}, switch_time, '', function() {
                $(this).css( {'margin-left': 0} );
                _child = $(this).children().first();
                _child.remove;
                $(this).append( _child );
            });
        }
    };
    
    $.fn.articleswitch.option = {
        time: 2600,
        switch_time: 800,
        width: 208,
        content: 'items'
    };
    
})(jQuery);