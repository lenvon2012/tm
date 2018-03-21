$(function(){
    var tip = $( '<div id="tip_desc" style="position: absolute; z-index: 10000;"></div>' );
    var timer;
    
    $( '.txt-tip a' ).hover(function(){
        the = $(this);
        
        _desc = the.next( '.desc' );
        
        if ( _desc.length > 0 ) {
            
            tip.html( _desc.html() );
            
            timer = setTimeout(function(){
                
                offset = the.offset();
                
                win_width = $(window).width();
                offset_left = offset.left;
                
                if( win_width - offset_left - 360 > 0  ) {
                    _left = offset.left;
                } else {
                    _left = offset.left - 360;
                }
            
                if ( $( '#tip_desc' ).length == 0 ) {
                    tip.appendTo( 'body' );
                }

                tip.css( {'left':_left + the.width(), 'top':offset.top} );
                
            }, 300);
        }
        
    }, function(){
        clearTimeout( timer );
        timer = setTimeout(function(){
            tip.remove();
        }, 150);
    });
});