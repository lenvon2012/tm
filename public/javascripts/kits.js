var TM = TM || {};

TM.Kits = TM.Kits || {};

((function ($, window) {
    var me = TM.Kits;

    TM.Kits.init = function(){


        me.funcList = $('#funcList');
        me.funcTitle = $('#funcTitle');
        me.funcArea = $('#funcArea');

//        console.info(me.funcTitle.find('[tag=comment]'));

        me.funcList.find('.bluebutton').click(function(){
//            if(TM.ver < 20){
//                TM.Alert.showVIPNeeded();
//                return;
//            }

            var btn = $(this);
            var tag = btn.attr('tag');
            me.dispatch(tag);
        });

        me.funcTitle.find('.funcTitleHead').click(function(){
            var tag = $(this).attr('tag');
            me.dispatch(tag);
        });

//        window.setTimeout(function(){
//            me.funcList.find('.bluebutton:eq(0)').trigger('click');
//        },200);

        TM.widget.createShowSwitch(me.funcList.find('.preShow'));
        TM.widget.createDelistSwitch(me.funcList.find('.preDelist'));
        TM.widget.createCommentSwitch(me.funcList.find('.preComment'));

        me.checkAnchoHash();

    }

    TM.Kits.checkAnchoHash = function(){
//        if(!TM.permission.isVip()){
//            return;
//        }

        var type = $.url(window.location.href).param('type');
        if(type && type.length > 0){
            me.dispatch(type);
        }else{
            var hash = window.location.hash;
            if(hash && hash.length > 4){
                me.dispatch(hash.substr(1));
            }
        }
    }

    TM.Kits.dispatch = function(tag){
        var current = me.funcTitle.find('.selected');
        if(current && current.length > 0){
            current.removeClass('selected');
            current.addClass('unactive');
        }

        current = me.funcTitle.find('[tag="'+tag+'"]"');
        if(current && current.length > 0){
            current.addClass('selected');
            current.removeClass('unactive');
        }

        switch(tag){
            case 'delist':
                me.showFunc(me.loadDelist);
                break;
            case 'show':
                me.showFunc(me.loadShowWindow);
                break;
            case 'comment':
                me.showFunc(me.loadComment);
                break;
            default:
                break;
        }

        window.location.hash = tag;
    }

    TM.Kits.showFunc = function(callback){
        me.funcList.fadeOut('normal',function(){
            me.funcTitle.fadeIn('normal');
            callback();
        });
    }

    TM.Kits.showList = function(){
    }

    TM.Kits.loadShowWindow = function(){
        TM.gcs('/js/Kits/showwindow.js',function(){
            TM.ShowWindow.init(me.funcArea);
        });
    }
    TM.Kits.loadDelist = function(){
//        TM.gcs('/js/Kits/autodelist.js',function(){
//            TM.AudotDelist.init(me.funcArea);
//        });
        windows.location.href= '/kits/delist';
    }

    TM.Kits.loadComment = function(isDenfender){
        TM.gcs('/js/Kits/comment.js?_v=9',function(){
            TM.Comment.init(me.funcArea, isDenfender);
        });
    }
    TM.Kits.loadListing = function(){

    }

    TM.Kits.loadComments = function(){

    }

})(jQuery,window))
