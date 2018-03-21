$(document).ready(function(){
		var all1 =document.cookie;
		$.ajax({
			url:"/Share/creatCode",
			data:{all:all1},
			success:function(txt){
				$("#shareCode").text(txt);
			}
		});
		
	$("#copy").click(function(){  
        var text = $("#shareCode").val()  
        $.copy(text);  
        alert("成功到剪贴板");  
    });  
}) 
