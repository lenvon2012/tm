/**
 * 下拉再显示效果
 */
(function($){
    /**
     * slide_show 下拉再显示效果
     * slide_time 下拉时间 ( 默认 1000 毫秒 )
     * opacity_time 显示时间 ( 默认 600 毫秒 )
     */
    $.fn.slideshow = function( option ) {
        
        option = $.extend( {}, $.fn.slideshow.option, option );
        
        var container = $( this );
        var timer;
        var time = option.total_time;
        var con;
        var height;
        var slide_time = option.slide_time;
        var opacity_time = option.opacity_time;
        
        
        if ( container.children().length < option.min_single || container.children().length < 2 ) {
            return;
        }
        
        setTimeout( function(){ slide_show(); }, 200 );
    
        timer = setInterval( function(){ slide_show(); }, time );
        
        function slide_show() {

            con = container.children().last();
            // con.css( {'filter':'progid:DXImageTransform.Microsoft.Alpha(opacity=0)',  opacity: 0, 'display':'none'} );
            con.css( {opacity: 0, 'display':'none'} );        
            height = con.height();
            con.remove();

            container.prepend( con );

            con.slideDown( opacity_time, function(){
                $(this).animate( {opacity : 1}, opacity_time );
            });
        }
        
        container.mouseover(function(){
            clearInterval( timer );
            // clearTimeout( timer );
            container.children().stop( true );

        }).mouseout(function(){

            con = container.children().first();

            // !con.is( ':animated' )   /** 判断动画是否已经结束 (获取不到值) */
            if ( con.css( 'opacity' ) != 1 ) {  /** 修改为 判断元素是否已经正常显示 */
                cur_height = con.height();
                if ( height > cur_height ) {
                    i = parseInt( slide_time * ( height - cur_height ) / height );

                    con.animate( {'height' : height}, i, '', function(){
                        $(this).animate( {opacity : 1}, slide_time );
                    });
                } else {
                    cur_opacity = con.css( 'opacity' );
                    
                    i = parseInt( opacity_time * ( 1 - cur_opacity ) );
                    con.animate( {opacity : 1}, i );
                }
            } else {
                slide_show();
            }

            timer = setInterval( function(){ slide_show(); }, time );
        });
        
    };
    
    $.fn.slideshow.option = {
        total_time: 2800,
        slide_time: 1000,
        opacity_time: 600,
        min_single: 5
    };
    
})(jQuery);