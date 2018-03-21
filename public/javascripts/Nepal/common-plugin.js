
/**
 * jQuery 公共插件
 */
jQuery.extend({
    in_array: function( str, arr ) {
        
        switch ( typeof( arr ) ) {
            case 'string':
                arr = arr.split( ' ' );
                break;
            case 'Array':
                break;
            default:
                return false;
                break;
        }
        
        len = arr.length;
        for ( var i = 0; i < len; i++ ) {
            
            if ( str == arr[i] ) {
                return true;
            }
        }
        return false;
    },
    
    /**
     * 显示遮罩
     */
    showmark: function( event ) {
        mask = $( '<div id="box-mask"></div>' );
                
        $( 'body' ).append( mask );

        height = $(document).height();
        width  = $(document).width();

        mask.css( {'opacity':0.5, 'position':'absolute', 'width':width, 'height':height, 'background-color':'#333333', 'z-index':'990', 'left':0, 'top':'0'} );
        
        if ( $.browser.msie && /msie 6\.0/i.test(navigator.userAgent) ) {
            $( 'body select' ).css( {'visibility': 'hidden'} );
        }
        
        $( '#box-mask' ).click(function(){
            event();
        });
    },
    
    /**
     * 关闭遮罩
     */
    closemark: function() {
        if ( $.browser.msie && /msie 6\.0/i.test(navigator.userAgent) ) {
            $( 'body select' ).css( {'visibility': 'visible'} );
        }
        $( '#box-mask' ).remove();
    },
    
    markresize: function() {
        $(window).resize(function(){
            mask = $( '#box-mask' );
            if ( mask.length > 0 ) {
                mask.width( $(document).width() );
                mask.height( $(document).height() );
            }
        });
    },
    
    taobaohref: function( iid ) {
        _link = 'http://item.taobao.com/item.htm?id={iid}';
        if ( iid && iid != '' ) {
            return _link.replace( '{iid}', iid );
        }
        return '';
    },
    
    cmfrun: function( head, msg, callback ) {
        
        if ( $( '#cmf-run' ).length > 0 ) {
            cmfclose();
            // return false;
        }
        
        if ( !head ) {
            head = '确认信息';
        }
        
        if ( !msg ) {
            msg = '你的操作确认是否继续！';
        }
        
        str = '';
        
        str += '<div id="cmf-run">';
        str += '<style type="text/css">';
        str += '#cmf-run{ text-align: left; position:fixed;border:1px solid #CCCCCC; width: 300px; /*height:220px;*/ top: 50%; left: 50%;'
            + 'margin-left:-150px; margin-top: -150px; background-color: #FFFFFF; z-index: 100001; overflow:hidden; '
            + '_position: absolute; /*_top: expression(eval(document.documentElement.scrollTop+document.documentElement.clientHeight / 2 -(parseInt(this.currentStyle.marginBottom,10)||0)));*/ }';
        str += '#cmf-run .entity{ padding:0;}';
        str += '#cmf-run h5{ border-bottom: 1px solid #CCCCCC; }';
        str += '#cmf-run h5{ line-height: 32px; padding: 0 12px; margin: 0; }';
        str += '#cmf-run .cmf-content{ padding: 12px; margin: 0;}';
        str += '#cmf-run p{margin:0;padding:0; line-height: 28px; overflow:hidden;}';
        str += '#cmf-run #cmf-close{float:right;cursor: pointer;}';
        str += '#cmf-run .cmf-btn{ padding: 5px 0; text-align: right; }';
        str += '</style>';
        str += '<div class="entity">';
        str += '<div class="cmf-head"><h5><span id="cmf-close">关闭</span>' + head + '</h5></div>';
        str += '<div class="cmf-content"><p>' + msg + '</p><p class="cmf-btn"><input type="button" value="确认" id="cmf-submit" /></p></div>';
        str += '</div></div>';
        
        $( 'body' ).append( str );
        if ( $.browser.msie && /6.0/.test(navigator.userAgent) ) {
            $( 'body select' ).css( {'visibility': 'hidden'} );
        }
        
        $( '#cmf-submit' ).click(function(){
            if ( callback ) {
                alert( typeOf( 'callback' ) );return;
                callback();
            }
            cmfclose(); 
        });
        $( '#cmf-close' ).click(function(){
            cmfclose(); 
        });
        
        function cmfclose(){
            $('#cmf-run').remove();
            if ( $.browser.msie && /6.0/.test(navigator.userAgent) ) {
                $( 'body select' ).css( {'visibility': 'visible'} );
            }
        }
    },
    
    addLoadState: function( the, name ) {
        
        if ( name ) {
            if ( the.html() ) {
                the.html( name );
            } else {
                the.val( name );
            }
        }
        
        the.attr( 'disabled', 'disabled' );
        the.addClass( 'col-gray' );
     },

     removeLoadState: function( the, name ) {
        
        if ( name ) {
            if ( the.html() ) {
                the.html( name );
            } else {
                the.val( name );
            }
        }
        
        the.removeAttr( 'disabled' );
        the.removeClass( 'col-gray' );
     }
});