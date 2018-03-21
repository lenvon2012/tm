$(document).ready(function(){
	
	$('#searchBtn').click(function(){
		var searchtext=$('#key').val();
		var pn =parseInt($("#nowPage").html());
		if(searchtext.length==0){
			$('#searchBtn').qtip('show');
		}else{
			$('#searchBtn').qtip('hide');
			$('#searchBtn').qtip('disable',true);
			
			$("#Warnings").html("");
			$.ajax({
				url:"/Words/search",
				data:{s:searchtext,pn:pn,ps:30},
				success:function(paginger){
					showData(paginger);
				}
			});
		}

	});
})
//显示tip
	$('#searchBtn')
	.qtip({
		content: {
			text: "亲~请先填写宝贝的核心关键词哦···"
		},
		position: {
			at: "center left " 
		},
		show: {
			event: false, 
			ready: false 
		},
		hide: false, 
		style: {
			name:'cream' 
		}
	});
//显示数据
	function showData(paginger){
		
		var tableDom=$('#keywordsTable tbody');
		tableDom.children().remove();
		var html = null;
        if(!paginger.res || paginger.res.length == 0){
			$("#keywordsTable").html('<tr><td colspan="4">T___T找不到相关的关键词哦，亲。请换一个关键词再试试哦。</td></tr>');
		}else{
			$.each(paginger.res, function(i, t) {
				var fenshu = 0;
				var a = t.click;
				var b = t.pv;
				var c = t.competition;
//				alert(TM.util.diplaySearch(a));
				$("#keywordsTable tbody").append("<tr>"+   
	            "<td  bgcolor=\"#FFFFFF\"><div align=\"center\"><span class=\"STYLE1\">"+
	            t.word
	            +"</span></div></td>"+
	            "<td bgcolor=\"#FFFFFF\"><div align=\"center\"><span class=\"STYLE1\">"+
	            TM.util.diplaySearch(parseInt(a))
	            +"</span></div></td>"+
	            "<td bgcolor=\"#FFFFFF\"><div align=\"center\"><span class=\"STYLE1\">"+
	            TM.util.diplaySearch(parseInt(b))
	            +"</span></div></td>"+
	            "<td bgcolor=\"#FFFFFF\"><div align=\"center\"><span class=\"STYLE1\">"+
	            TM.util.diplaySearch(parseInt(c))
	            +"</span></div></td>"+
	            "<td bgcolor=\"#FFFFFF\"><div align=\"center\"><span class=\"STYLE4\"><img src=\"/public/images/tab/edt.gif\" /><a href=\"javascript:searchItem('"+t.word+"')\">获取相关关键词</a></span></div></td>"+
	          "</tr>");
				$("#totalCount").html(paginger.totalPnCount);
				var pageCount = paginger.totalPnCount/10;
				var flag=1;
				if (pageCount%1!=0){
					var flag=parseInt(pageCount+1);
				}else{
					flag=pageCount;
				}
				$("#totalPage").html(flag);
//                console.info($("#keywordsTable tbody"));
			});
		};
	};
function searchItem(word){
	$("#key").val(word);
	$('#searchBtn').click();
	
};
//上一页，下一页，首页，尾页，页面跳转
function forward(){
	var f =parseInt($("#nowPage").html());
	if(f > 1){
		$("#nowPage").html(f-1);
		$('#searchBtn').click();
	}
};

function next(){
	var f =parseInt($("#nowPage").html());
	var b =parseInt($("#totalPage").html());
	if(f<b){
		$("#nowPage").html(f+1);
		$('#searchBtn').click();
	}
};
function firstPage(){
	var f =parseInt($("#nowPage").html());
	var b =parseInt($("#totalPage").html());
	if(f>1){
		$("#nowPage").html(1);
		$('#searchBtn').click();
	}
};
function lastPage(){
	var f =parseInt($("#nowPage").html());
	var b =parseInt($("#totalPage").html());
	if(f!=b){
		$("#nowPage").html(b);
		$('#searchBtn').click();
	}
};
function toPage(){
	var f =parseInt($("#nowPage").html());
	var b =parseInt($("#totalPage").html());
	var c =parseInt($("#toPage").val());
	if(f!=c){
		$("#nowPage").html(c);
		$('#searchBtn').click();
	}
};
//下面是显示页面效果的

var  highlightcolor='#c1ebff';
var  clickcolor='#51b2f6';
function  changeto(){
source=event.srcElement;
if  (source.tagName=="TR"||source.tagName=="TABLE")
return;
while(source.tagName!="TD")
source=source.parentElement;
source=source.parentElement;
cs  =  source.children;
//alert(cs.length);
if  (cs[1].style.backgroundColor!=highlightcolor&&source.id!="nc"&&cs[1].style.backgroundColor!=clickcolor)
for(i=0;i<cs.length;i++){
	cs[i].style.backgroundColor=highlightcolor;
}
}

function  changeback(){
if  (event.fromElement.contains(event.toElement)||source.contains(event.toElement)||source.id=="nc")
return
if  (event.toElement!=source&&cs[1].style.backgroundColor!=clickcolor)
//source.style.backgroundColor=originalcolor
for(i=0;i<cs.length;i++){
	cs[i].style.backgroundColor="";
}
};

function  clickto(){
source=event.srcElement;
if  (source.tagName=="TR"||source.tagName=="TABLE")
return;
while(source.tagName!="TD")
source=source.parentElement;
source=source.parentElement;
cs  =  source.children;
//alert(cs.length);
if  (cs[1].style.backgroundColor!=clickcolor&&source.id!="nc")
for(i=0;i<cs.length;i++){
	cs[i].style.backgroundColor=clickcolor;
}
else
for(i=0;i<cs.length;i++){
	cs[i].style.backgroundColor="";
}
};
