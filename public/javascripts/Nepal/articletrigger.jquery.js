/**
 * 内容触发切换
 */
(function($){
    /**
     * articletrigger 内容触发切换显示效果
     * trigger_time 切换时间 ( 默认 300 毫秒 )
     * width 内容 宽度  ( 默认 180 毫秒 )
     * content 内容 class
     */
    $.fn.articletrigger = function( option ) {
        
        option = $.extend( {}, $.fn.articletrigger.option, option );
        
        var container = $( this );
        var content   = '.' + option.content;
        var trigger_time = option.trigger_time;
        var trigger_timer;
        var trigger_the;
        var the_width        = option.width;

        container.children( 'li' ).click(function(){
            item_trigger( $( this ) );
        });
        container.children( 'li' ).hover(function(){
            trigger_the = $( this );
            trigger_timer = setTimeout(function(){
                item_trigger( trigger_the );
            }, trigger_time );
        }, function(){
            clearTimeout( trigger_timer );
        });

        function item_trigger( the ) {

            if ( the.attr( 'class' ) ) {
                class_all = the.attr( 'class' ).split( ' ' );
            } else {
                class_all = '';
            }

            if ( !$.in_array( 'current', class_all ) ) {

                index = the.prevAll().length;

                container.children().removeClass( 'current' );
                the.addClass( 'current' );

                _item = container.parent().children( content );
                _list = _item.children();

                _list.removeClass( 'current' );
                _list.eq(index).addClass( 'current' );

                _item.animate( { 'margin-left':-the_width * index } );
            }
        }
    };
    
    $.fn.articletrigger.option = {
        trigger_time: 300,
        width: 180,
        content: 'items'
    };
    
})(jQuery);