
/**
 *Search function
 *@name search 
 */
var SH = SH||{};
/**
 * search condition
 */
SH.condition = SH.condition || {};
/**
 *search condition controller 
 *this function is not exist
 */
SH.controller = SH.controller || {};
/**
 * result
 */
SH.result = SH.result || {};
 /**
  * show the search result.
  */
SH.dosearch = SH.dosearch ||{};
/**
 * add to my lexicon
 */
//SH.addtomylexicon = SH.addtomylexicon ||{};

(function ($, window) {
	/**
	 * when get the result ,show them in the table'tbody;
	 * pv is 展现量
	 * zv is 直通车竞争度
	 * str is 点击率
	 */
	SH.result.show = function reslut_show(paginger){
		$(".search_res_tbody").html("");
		var res ;
		$.each(paginger.res, function(i, t) {
			res=i+1+"</td>";
			res=res+"<td>"+t.word+"</td>";
			res=res+"<td>"+ TM.util.diplaySearch(parseInt(t.pv))+"</td>";
			res=res+"<td>"+TM.util.diplaySearch(parseInt(t.click))+"</td>";
			res=res+"<td>"+TM.util.diplaySearch(parseInt(t.competition))+"</td>";
//			res=res+"<td>"+TM.util.diplaySearch(parseInt(t.ctr))+"</td>";
			//res=res+"<td ><a class='addmylexicon' wid='"+t.id+"' href='javascript:void(0)'>加入词库</a></td>";
			res=res+"<td ><img class = 'addimg' src = 'http://img04.taobaocdn.com/imgextra/i4/759708778/T23YW1XgdbXXXXXXXX_!!759708778.png'><span class = 'addmylexicon' onclick='SH.addtomylexicon("+t.id+")'>加入词库</span></td>";
			if(i%2!=0){
				showhtml="<tr class = 'tableRow hoverRow grey-bg'><td>"+res+"</tr></td>";
			}else{
				showhtml="<tr class = 'tableRow hoverRow'><td>"+res+"</tr></td>";
			}
			
//            $(showhtml).find('.addmylexicon').click(function(){
//                var wid = $(this).attr('wid');
//                SH.addtomylexicon(wid);
//            });
			$(".search_res_tbody").append(showhtml);
		});
//		fenye();
	};
	/**
	 * 分页操作
	 */
	var bottom = $('<div style="text-align: center;"></div>');
	SH.result.fenye = function result_fenye(condition){
		 bottom.tmpage({
	            currPage: 1,
	            pageSize: 10,
	            pageCount:1,
	            ajax: {
	                on: true,
	                dataType: 'json',
	                url: "/Words/norsearch",
	                param:{s:condition},
	                callback:function(data){
	                    bottom.empty();
	                    if(!data){
	                        return;
	                    }
	                    SH.result.show(data)
	                    bottom.appendTo($("#cutpage"));
	                }
	            }
	        });
	}
	SH.result.fenye.sup = function result_fenye_sup(condition){
		 bottom.tmpage({
	            currPage: 1,
	            pageSize: 30,
	            pageCount:1,
	            ajax: {
	                on: true,
	                dataType: 'json',
	                url: "/Words/supersearch",
	                param:{s:condition},
	                callback:function(data){
	                    bottom.empty();
	                    if(!data){
	                        return;
	                    }
	                    SH.result.show(data)
	                    bottom.appendTo($("#cutpage"));
	                }
	            }
	        });
	}
	/**
	 * deal the search condition
	 * this will divide into two part
	 *  one is the normal another is the super search !
	 */
	//first is the nor search
	SH.dosearch.normal = function normal_search(condition,pn){
		$.ajax({
			url:"/Words/search",
			data:{s:condition,pn:pn,ps:20},
			success:function(res){
				SH.result.show(res);
			}
		});
	};
	SH.dosearch.expert = function expert_search(morecondition,pn){
		$.ajax({
			url:"/Words/supersearch",
			data:{s:morecondition,pn:pn,ps:30},
			success:function(res){
				SH.result.show(res);
			}
		});
	};
	/**
	 * use this function will deal the super's many conditions
	 * if the condition is null use this function will assign "333"
	 */
	SH.condition.sup = function sup_condition(){
		var baohanall = SH.condition.deal($("#all_cd").val());
		var baohan1 = SH.condition.deal($("#baohan_cd1").val());
		var baohan2 = SH.condition.deal($("#baohan_cd2").val());
		var baohan3 = SH.condition.deal($("#baohan_cd3").val());
		var buhan1 = SH.condition.deal($("#buhan_cd1").val());
		var buhan2 = SH.condition.deal($("#buhan_cd2").val());
		var buhan3 = SH.condition.deal($("#buhan_cd3").val());
		/*
		 * 这里写值为空的情况下的处理
		 */
		var morecondition = baohanall+"!"+baohan1+"!"+baohan2+"!"+baohan3+"!"+buhan1+"!"+buhan2+"!"+buhan3;
		
		return morecondition;
	}
	SH.condition.deal = function deal_condition(condition){
		if(condition == "" || condition == null){
			return "~";
		}else{
			return condition;
		}
	}
	/**
	 * SH.addtomylexicon
	 * add to my lexicon
	 */
	SH.addtomylexicon = function addtomylexicon(wid){
		$.ajax({
			url:"/Words/addmylexicon",
			data:{wordId:wid},
			success:function(data){
				TM.Alert.load(data);
			}
		});
	}

})(jQuery, window);
$("#reset_input").click(function(){
	$("#all_cd,#baohan_cd1,#baohan_cd2,#baohan_cd3,#buhan_cd1,#buhan_cd2,#buhan_cd3").val("");
});
$("#serch_now").click(function(){
	var condition = $("#search_key").val();
	var pn = 1;
	SH.result.fenye(condition);
//	SH.dosearch.normal(key,pn);
});
$("#search_now_super").click(function(){
	var pn = 1;
	SH.result.fenye.sup(SH.condition.sup());
//	SH.dosearch.expert(SH.condition.sup(),pn);
});
/*
 * 高级和普通搜索的页面之间的切换
 * */
$("#tosupersearch").click(function(){
	$(".nor_search").slideUp("fast");
	$(".sup_search").slideDown("fast");
});
$("#tonorsearch").click(function(){
	$(".nor_search").slideDown("fast");
	$(".sup_search").slideUp("fast");
});












