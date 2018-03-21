/**
 * ML is my lexicon
 */
var ML = ML || {} ;
/**
 * this is for my lexicon
 * get my words from data base
 */
ML.searchmyword = ML.searchmyword||{};
/**
 * this function for remove from my lexicon
 */
ML.removeword = ML.removeword || {};
/**
 * deal the search result
 */
ML.result = ML.result || {};

(function ($, window) {
	/**
	 * search the words and cut pages
	 */
	var bottom = $('<div style="text-align: center;"></div>');
	ML.result.searchwords = function(){
		 bottom.tmpage({
	            currPage: 1,
	            pageSize: 10,
	            pageCount:1,
	            ajax: {
	                on: true,
	                dataType: 'json',
	                url: "/Words/searchAll",
	                callback:function(data){
	                    bottom.empty();
	                    if(!data){ 
	                    	return;
	                    }if(data == null||data==""){
	                    	ML.result.warn();
	                    }else{
	                    	ML.result.show(data)
		                    bottom.appendTo($("#cutpage"));
	                    }
	                   
	                    
	                }
	            }
	        });
	};
	ML.result.show = function result_show(paginger){
		$("#ML_result").html("");
		var res ;
		$.each(paginger.res, function(i, t){
			res=i+1+"</td>";
			res=res+"<td>"+t.word+"</td>";
			res=res+"<td>"+ TM.util.diplaySearch(parseInt(t.pv))+"</td>";
			res=res+"<td>"+TM.util.diplaySearch(parseInt(t.click))+"</td>";
			res=res+"<td>"+TM.util.diplaySearch(parseInt(t.competition))+"</td>";
			res=res+"<td>"+TM.util.diplaySearch(parseInt(t.ctr))+"</td>";
			res=res+"<td><img class ='removeimg' src='http://img01.taobaocdn.com/imgextra/i1/759708778/T2tK5gXjpcXXXXXXXX_!!759708778.png'><span class = 'remove' onclick = 'ML.result.remove("+t.id+")'>移除</span></td>";
			if(i%2!=0){
				showhtml="<tr class = 'tableRow hoverRow grey-bg'><td>"+res+"</tr></td>";
			}else{
				showhtml="<tr class = 'tableRow hoverRow'><td>"+res+"</tr></td>";
			}
			$("#ML_result").append(showhtml);
		})
	};
	
	ML.result.warn = function result_warn(){
		$("#ML_result").html("");
		var html = "<td class = 'lexicon_warn' colspan='7'>亲！您的词库里面没有词哦！返回搜索页面，查找并收藏先！</td>";
		$("#ML_result").append(html);
	}
	
	ML.result.remove = function(wid){
		$.ajax({
			url:"/Words/removeWord",
			data:{wid:wid},
			success:function(data){
				ML.result.searchwords();
			}
		});
	}
	
})(jQuery, window);
$("#mylexicon_top_back").click(function(){
	 history.back();
});
