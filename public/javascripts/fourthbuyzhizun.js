$(document).ready(function(){
	$(function() {      
        $("#dialog_shangjia").dialog({
    		closeText: "关闭",
            autoOpen: false,
            width: 800,
            resizable: false,
            title:"自动上架",
            modal: true,
        });
        $("#dialog_biaoti").dialog({
    		closeText: "关闭",
            autoOpen: false,
            width: 800,
            height:580,
            resizable: false,
            title:"标题优化",
            modal: true,
        });
        $("#dialog_chuchuang").dialog({
    		closeText: "关闭",
            autoOpen: false,
            width: 800,
            resizable: false,
            title:"自动橱窗",
            modal: true,
        });
        $("#dialog_pingjia").dialog({
    		closeText: "关闭",
            autoOpen: false,
            width: 800,
            resizable: false,
            title:"自动评价",
            modal: true,
        });
        // Link to open the dialog
        $( "#shangjia_btn" ).click(function( event ) {
            $( "#dialog_shangjia" ).dialog( "open" );
            event.preventDefault();
        });
        $( "#biaoti_btn" ).click(function( event ) {
            $( "#dialog_biaoti" ).dialog( "open" );
            event.preventDefault();
        });
        $( "#chuchuang_btn" ).click(function( event ) {
            $( "#dialog_chuchuang" ).dialog( "open" );
            event.preventDefault();
        });
        $( "#pingjia_btn" ).click(function( event ) {
            $( "#dialog_pingjia" ).dialog( "open" );
            event.preventDefault();
        });
    });
});
	var version_data = "h_12m";
	$("#version_zhizun_1").click(function(){
		$(this).attr("style","background-position:left -228px");
		$("#version_zhizun_3,#version_zhizun_6,#version_zhizun_12").removeAttr("style");
		version_data="h_1m";
	});
	$("#version_zhizun_3").click(function(){
		$(this).attr("style","background-position:-252px -228px");
		$("#version_zhizun_1,#version_zhizun_6,#version_zhizun_12").removeAttr("style");
		version_data="h_3m";
	});
	$("#version_zhizun_6").click(function(){
		$(this).attr("style","background-position:-505px -228px");
		$("#version_zhizun_1,#version_zhizun_3,#version_zhizun_12").removeAttr("style");
		version_data="h_6m";
	});
	$("#version_zhizun_12,#version_givendata").click(function(){
		$(this).attr("style","background-position:-757px -228px");
		$("#version_zhizun_1,#version_zhizun_3,#version_zhizun_6").removeAttr("style");
		version_data="h_12m";
	});
	$("#version_buynow_btn,#version_givendata").click(function(){
//		alert(version_data);
		$.ajax({
			url:"/Buy/toPayPage",
			data:{"time":version_data},
			success:function(text){
//				alert(text);
				window.location.href=text;
			}
		})
	});
	$("#version_buynow_btn_second").click(function(){
		var version_data="h_12m";
		$.ajax({
			url:"/Buy/toPayPage",
			data:{"time":version_data},
			success:function(text){
//				alert(text);
				window.location.href=text;
			}
		})
	});
//});

