/**
 * 分页内容切换 ( 首页：我的好店铺 )
 */
$(document).ready(function(){
    
    var pagination = $( '#my-shop-page .page-result' );
    
    pagination.children( 'a' ).click(function(){

        var the = $( this );
        var _class = the.attr( 'class' );

        if ( _class && $.in_array( 'current', _class.split( ' ' ) ) ) {
            return;
        }

        var block = 196;
        var index = parseInt ( $( this ).html() ) - 1;

        if ( index >= 0 ) {
            $( '#shop-con ul' ).animate( { 'margin-left': - 196 * index }, 800, '', function(){
                pagination.children( 'a' ).removeClass( 'current' );
                the.addClass( 'current' );
            });
        }
    });
});