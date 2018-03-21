  $(document).ready(function(){


        $( '#con-recom .item-trigger' ).articletrigger();
        if( $( '#logined' ).val() == "1" )
            account_update(0);
    });

    // 更新账户
    function account_update(force){
        $(".account").html("<img src='http://img01.taobaocdn.com/imgextra/i1/62192401/T2EAlEXblNXXXXXXXX_!!62192401.gif'>");
        $.get('/auth/renew?' + ( force == '1' ? '' : 'uid=0&sid=0&') + 'rnd=' + Math.random(),function(){
            $.get('/auth/synch?type=account&force='+force+'&unity=1&rnd=' + Math.random(),function(str){
                var arr = ["","非正常登录","---","---","---","---","---","---","---"];
                if( str && str.length > 10 ){
                    arr = eval(str);
                }
                if( arr && arr.length >=9 ){
                    //$("#grade").html(arr[0]);
                    $("#grade_name").html(arr[1]);
                    //$("#grade_icon").attr('src',arr[2]);
                    $("#grade0").html(arr[3]);
                    $("#grade1").html(arr[4]);
                    $("#grade2").html(arr[5]);
                    $("#grade3").html(arr[6]);
                    $("#dx").html(arr[7]);
                    $("#jf").html(arr[8]);

                    if( parseInt(arr[0]) <= 0 ){
                        account_upgrade();
                    }
                    else{
                        auth($("#auth_mode").val(), !force);
                    }
                }
            });
        });
    }

    // 升级账户
    function account_upgrade(){
        var id= "dialog_upgrade";
        var url = "/auth/upgrade?rnd=" + Math.random();
        var title = "您需要免费开通一号通服务才能继续";
        if( $("#" + id).size() <= 0 )
            $(document.body).append("<div id='" + id + "'></div>");
        $("#" + id).html("<iframe width='100%' height='100%' src='" + url + "' scrolling=0 frameborder=0 border=0></iframe>" ).dialog( {modal:true,title:title} );
        $(".ui-widget-overlay").fadeTo(5000,0.99);
    }
    
    
    $(function(){
        var ajaxevent = false;
        var oname = '';

        var feedback = $( '#fb_shadow' );

        var my_dialog = $('#my_dialog');

        $('#fb_cont a.to-feedback').click(function(){

            my_dialog.dialog({
                width: 600,
                height: 360,
                modal: true,
                draggable: true,
                resizable: false,
                zIndex: 99999,
                title: '意见反馈'
            });
            my_dialog.html( $('#fb_shadow') );

            $('#fb_shadow').show();

            $( '#fb_submit' ).click(function(){

                if ( !feedback_valid() ) {
                    return;
                }

                input = $( '#fb_shadow input' ).serialize();
                content = $( '#fb_shadow textarea' ).serialize();

                data = input + '&' + content;

                feedback_submit( $(this), data );
            });
        });

        function feedback_valid() {

            wangwang = feedback.find( 'input[name="Feedback[wangwang]"]' )
            wangwang_str = $.trim( wangwang.val() );
            wangwang_len = wangwang_str.length;

            phone = feedback.find( 'input[name="Feedback[phone]"]' )
            phone_str = $.trim( phone.val() );

            content = feedback.find( 'textarea[name="Feedback[content]"]' )
            content_str = $.trim( content.val() );
            content_len = content_str.length;

            if ( wangwang_str != '' ) {

                if ( wangwang_len < 2 || wangwang_len > 20 ) {
                    wangwang.parent().next( 'span' ).html( '旺旺的名称不正确！' );
                    return false;
                } else {
                    wangwang.parent().next( 'span' ).html( '' );
                }
            }

            if ( phone_str != '' ) {
                if ( !phone_str.match(/^\d{11}$/) ) {

                    // if ( !phone_str.match( /(^0{0,1}1[3|4|5|6|7|8|9][0-9]{9}$)/ ) ) {
                    phone.parent().next( 'span' ).html( '手机号码不正确！' );
                    return false;
                } else {
                    phone.parent().next( 'span' ).html( '' );
                }
            }

            if ( content_str == '' ) {
                content.next( 'span' ).html( '反馈内容不得为空！' );
                return false;
            } else {
                if ( content_len < 5 ) {
                    content.next( 'span' ).html( '反馈内容必须在 5 个字符及其以上！' );
                    return false;
                } else {
                    content.next( 'span' ).html( '' );
                }
            }

            return true;
        }

        function feedback_submit( the, data ) {

            if ( ajaxevent ) {
                return;
            } else {
                ajaxevent = true;
            }

            oname = the.val();

            $.addLoadState( the, '提交中...' );

            $.ajax({
                type: "POST",
                url: "http://i.haodianpu.com/admin/feedback/create.html",
                dataType: 'json',
                data: data,
                success: function( msg ){

                    if ( msg ) {
                        if ( msg.success ) {

                            feedback.find( 'input[name="Feedback[wangwang]"]' ).val( '' );
                            feedback.find( 'input[name="Feedback[phone]"]' ).val( '' );
                            feedback.find( 'textarea[name="Feedback[content]"]' ).val( '' );

                            feedback.find( '.txt-error' ).html( '' );

                            alert( msg.msg );
                            my_dialog.dialog( 'close' );
                        } else {
                            alert( msg.msg );
                        }
                    }

                }, complete: function() {
                    $.removeLoadState( the, oname );
                    ajaxevent = false;
                }
            });
        }

    });