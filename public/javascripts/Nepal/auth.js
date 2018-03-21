
/**
* 授权 
*/
var auth_callback = null;

function auth( mode, scope, callback ){

    // 检查参数及赋值
          
    if( typeof(mode) == "undefined" )
        return;  
                      
    short_auth = false; // 是否短授权
    force_auth = false; // 是否强制验证
        
    if( typeof(scope) == "string" ){
        short_auth = true;
        force_auth = true;
    }
    else if( typeof(scope) == "boolean" ){
        force_auth = scope;
        scope = "";        
    } 
    else{         
        scope = "";
    }


    if( typeof(scope) == "function" ){
        auth_callback = scope;
    }             
    if( typeof(callback) == "function" ){
        auth_callback = callback;
    }

    // 服务端查询授权是否到期(包含正常授权和短授权) ，如果到期、且需要强制授权则弹出授权窗口      
    url = "/auth/check?mode=" + mode + "&short=" + short_auth + "&rnd=" + Math.random();
    $.get( url,function( rtn ){
        // alert( rtn ); 
        
        if( ( rtn == -1 && force_auth ) || ( rtn != 1 && short_auth )  ){
            url = "/auth/quick?mode=" + mode + "&scope=" + scope + "&rnd=" + Math.random(); 
            title = force_auth ? "您需要完成淘宝授权操作才能继续" : "您正在进行的操作需要淘宝二次授权";
            if( $("#dialog_auth").size() <= 0 )           
                $(document.body).append( "<div id='dialog_auth'></div>" );   
            $( "#dialog_auth" ).html( "<iframe width='100%' height='100%' src='" + url + "' scrolling=0 frameborder=0 border=0></iframe>" ).dialog( {modal:true,width:"640",height:"600",title:title} );
            return;
        }
        
        if( rtn == 1 && typeof(auth_callback) == "function" ){
            auth_callback();     
        }                 
    });
}


function auth_succeed(){    
    $("#dialog_auth").html("").dialog("close"); 
    if( typeof(auth_callback) == "function" )
        auth_callback();   
}

