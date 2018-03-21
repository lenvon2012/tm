/* 
 * 公共头部导航样式
 */
$(document).ready(function(){
    var nav = $( '#hdp-nav .nav-panel' );
    var the;
    
    /* 旧版导航 nav */
    /* 
    nav.hover(function(){
        the = $( this );
        // the.next().show();
        // the.parent().addClass( 'activity' );
        
        offset = the.offset();
        
        the.addClass( 'activity' );
        // the.children( '.nav-list-panel' ).css( {'top': the.height() + offset.top, 'left': offset.left } ).show();
        the.children( '.nav-list-panel' ).show();
        
    }, function(){
        the = $( this );
        // the.next().hide();
        // the.parent().removeClass( 'activity' );
        
        the.children( '.nav-list-panel' ).hide();
        the.removeClass( 'activity' );
    }); */
    
    
    
    var menu = $( '#nav-top li .panel' );
    menu.hover(function(){
        the = $( this );
        the.addClass( 'hover' );
    }, function(){
        the = $( this );
        the.removeClass( 'hover' );
    });
    
    
    var nav_current = $( '#nav_current' );
    
    if ( nav_current . length > 0 ) {
        nav_init ( nav_current.val() );  // 服务器控制导航显示
        // nav_init ();
    } else {
        nav_init();  // 浏览器地址控制导航显示
    }
    
    /**
     * 导航初始化
     */
    function nav_init ( index ) {
         
        var nav_list = $( '#hdp-nav .nav-list > ul > li' );
        var isset_nav = false;
        
        if ( index == undefined  ) {
            cur_url = location.href;
            domain  = location.hostname;

            nav_list.each(function( item ) {
                the = $(this);        
                href = the.find( 'span a' ).attr( 'href' );

                if ( !isset_nav && ( href == cur_url || 'http://' + domain + href == cur_url ) ) {
                    the.children( 'span' ).addClass( 'current' );
                    isset_nav = true;
                }
            });

            if ( !isset_nav ) {
                nav_list.eq( 0 ).children( 'span' ).addClass( 'current' );
            }
        } else {
            nav_list.eq( parseInt( index ) ).children( 'span' ).addClass( 'current' );
        }
        
    }
    
    var timer;
    var nav_the;
    
    var _left = 0;
    
    if ( $.browser.msie && $.browser.version.indexOf( '6' ) >= 0 ) {
        // _left = -1;
    }
    
    /* 2012.5.25 new nav */
    /* 
    nav.hover(function(){
        
        nav_the = $( this );
        nav_offset = $( '#nav_list' ).offset();
        
        nav_the.addClass( 'activity' );
        nav_the.children( '.app-service-list' ).css( {'top': nav_the.height() + nav_offset.top - 20, 'left': nav_offset.left + _left } ).show();
        
    }, function(){
        
        nav_the = $( this );
        
        nav_the.removeClass( 'activity' );
        nav_the.children( '.app-service-list' ).hide();
        
    }); */
    
    nav_offset = $( '#nav_list' ).offset();
    
    /* 2012.5.28 new nav 2 */
    nav.hover(function(){
        the = $( this );
        
        offset = the.offset();
        
        the.addClass( 'activity' );
        the.children( '.nav-list-panel' ).css( {'top': the.height() + nav_offset.top, 'left': nav_offset.left + _left, 'width':'988px' } ).show();
        the.children( '.nav-list-panel' ).show();
        
    }, function(){
        the = $( this );
        
        the.children( '.nav-list-panel' ).hide();
        the.removeClass( 'activity' );
    });
    
});

