/**
*  实时任务
*/
var timer;

/**
*  form表单提交
*/
function taskAdd( obj ){
    taskIframe( obj );
    
    $(obj).attr("target", "task_iframe");
}

/**
*  post提交
*/
function taskAddGet( obj, url ){
    taskIframe( obj );
    
    $("iframe[name='task_iframe']").attr( "src", url );
}

/**
*  添加iframe框
*/
function taskIframe( obj ){
    var _width = 400;
    var _height = 260;
    if( $(obj).parents("body").width() < _width ){
        _width = $(obj).parents("body").width();
    }
    
    if( $(obj).parents("body").height() < _height ){
        _height = $(obj).parents("body").height();
    }
    
    if( $("#shade_iframe").size() <= 0 )
        $(document.body).append("<div id='shade_iframe'></div>");
    $("#shade_iframe").html("<iframe name='task_iframe' width='100%' height='100%' scrolling=0 frameborder=0 border=0></iframe>").dialog({modal:true, width:_width, height:_height, resizable:false, dialogClass:"task-dialog"});
    $(".ui-widget-header", $(".task-dialog")).hide();

}

/**
*  查询状态
*/
function taskInquire( key, url, interval, prompt ){
    $(document.body).append('<div id="shade_dialog" style="text-align: center; padding-top: 10px;"><h3 id="dealing_msg">'+prompt+'</h3><p style="line-height: 35px;">已经用时 <strong id="loading_time" style="color:#CC3300;">0</strong> 秒</p><p><img src="http://img03.taobaocdn.com/imgextra/i3/62192401/T2uLGJXfdbXXXXXXXX_!!62192401.gif" /></p></div>');
    
    var i = 0;
    timer = setInterval( function(){
        $.ajax({
            type: "post",
            data: "key="+key+"&random=Math.random()",
            url: "/task/getstatus",
            dataType: "json",   
            success: function(data){
                if( data.con == 1 ){
                    if( data.msg == "" ){
                        data.msg = "正在处理中，请稍后…";
                    }
                    
                    $("#dealing_msg").html( data.msg );
                }
                if( data.con == 2 ){
                    if( data.msg == "" ){
                        data.msg = "请求成功！";
                    }
                    
                    $("#dealing_msg").html( data.msg );
                    dialog_deal( url, data.msg );
                }
                if( data.con == 3 ){
                    if( data.msg == "" ){
                        data.msg = "请求失败，请稍后再试！";
                    }
                    
                    $("#dealing_msg").html( data.msg );
                    dialog_deal( url, data.msg );
                }
                if( data.con == -1 ){
                    if( data.msg == "" ){
                        data.msg = "参数有误！如该状况出现多次，请联系客服及时方便您处理！";
                    }
                    
                    $("#dealing_msg").html( data.msg );
                    dialog_deal( url, data.msg );
                }
            },
            error: function( request, textStatus, errorThrown  ){
                if( i >= interval ){
                    dialog_deal( url, "请求失败，请稍后再试！"+"Error:"+errorThrown+";Page:"+this.url );
                }
            }
        });
        
        // 显示时间
        $("#loading_time").html( i );
        i++;
        
        if( parseInt( i%interval ) == 0 ){
            if( !window.confirm("请求已有段时间了，您是否继续等待？") ){
                dialog_deal( url, "" );
            }
        }
        
    }, 1000 );
}

/**
*  dialog处理
*/
function dialog_deal( url, msg ){
    if( msg != "" )
        alert(msg);
    
    clearInterval( timer );
    parent.hidden(url);
}

/**
*  dialog关闭
*/
function hidden( url ){
    $(document.body).find("#shade_iframe").remove();
    if( typeof(url) == "string" && url != "" )
        window.location.href = url;
}