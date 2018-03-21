/**
 * 内容向左滚动显示效果
 */
//(function($){
//    /**
//     * articleroll 内容向左滚动显示效果
//     * dis_time 动画时间 ( 默认 1600 毫秒 )
//     */
//    $.fn.articleroll = function( option ) {
//        
//        option = $.extend( {}, $.fn.articleroll.option, option );
//        
//        var dis = $( this );
//        var dis_timer;
//        var dis_time = option.dis_time;
//        var the;
//        
//        article_display();
//
//        dis_timer = setInterval(function(){
//            article_display();
//        }, 1600);
//
//        function  article_display () {
//            the = dis.children().first();
//            width = the.width();
//            dis.animate( {'margin-left': -width}, dis_time, '', function() {
//                the.remove();
//                dis.css( {'margin-left': '0'} );
//                dis.append( the );
//            });
//        }
//
//        dis.mouseover(function(){
//            clearInterval( dis_timer );
//            dis.stop( true );
//        }).mouseout(function(){
//            dis_timer = setInterval( article_display, dis_time );
//        });
//    };
//    
//    $.fn.articleroll.option = {
//        dis_time: 1600
//    };
//    
//})(jQuery);


window.onload = function() {
    var speed = 10;
    var timer;

    var container = document.getElementById( 'app-roll' );
    var _original = document.getElementById( 'roll_ori' );
    var _clone    = document.getElementById( 'roll_clone' );

    if ( container != undefined && _original != undefined ) {
        
        if ( _original.getElementsByTagName( 'li' ).length < 4 ) {
            return;
        }
        
        _clone.innerHTML = _original.innerHTML;

        timer = setInterval( marquee_left, speed );

        container.onmouseover = function() {
            clearInterval( timer );
        };

        container.onmouseout  = function() {
            timer = setInterval( marquee_left, speed );
        };
    }
    
    function marquee_left() {
        if ( _clone.offsetWidth - container.scrollLeft <= 0 ) {
            container.scrollLeft -= _original.offsetWidth;
        } else {
            container.scrollLeft++;
        }
    }
}