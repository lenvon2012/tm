var TM = TM || {};

((function($, window) {
	TM.AutoTitleRollBack = TM.AutoTitleRollBack || {};
	var AutoTitleRollBack = TM.AutoTitleRollBack;
	AutoTitleRollBack = $.extend(TM.AutoTitleRollBack,{
		init:function(){
            $(".onekey-danger").unbind().click(function(){
//				$('body').mask('<div class="titleRollBack"></div>');
				AutoTitleRollBack.singleEvent();
//				AutoTitleRollBack.initTitleSingle();
//                AutoTitleRollBack.confiCancelEvent();
//				AutoTitleRollBack.initDetailEvent();
//				AutoTitleRollBack.initClose();
			})
		},
		initTabMenu:function(){
			$('ul.tabMenu li').unbind().click(function() {
					var index = $(this).index();
					$('ul.tabMenu li').removeClass('current');
					$(this).addClass('current');
					$('.tabBox').children().removeClass('current').addClass(
							'hide').eq(index).removeClass('hide').addClass(
							'current');
//					$('.tabBox').children().addClass("test")
					var box = $('.tabBox').children().eq(index);
					if (box.hasClass('single')) {
						AutoTitleRollBack.singleEvent();
					} else if (box.hasClass('time')) {
                        AutoTitleRollBack.timeEvent();
					}
			})

		},
		singleEvent:function(){
			$.ajax({
				url:'/Titles/batchOpLogs',
				type:'post',
				data:{pn:1,ps:10000},
				async:false,
				success:function(data){
					if(data.res == null){
	                    return;
	                }

					var resleng = data.res.length;

					for (var i = 0; i < resleng; i++) {
						data.res[i].ts = new Date(
								data.res[i].ts)
								.formatYMDHMS();
					}
					$('.titleRollBack').empty();
					$('body').mask('<div class="titleRollBack"></div>');
		            var html = $('#maskTitleRollBack').tmpl(data);
					$('.titleRollBack').append(html);
                    AutoTitleRollBack.initTabMenu();
                    AutoTitleRollBack.confiCancelEvent();
                    AutoTitleRollBack.initDetailEvent();
                    AutoTitleRollBack.initClose();
				},
				beforeSend:function(){
    				$('body').mask('正在加载');
    			}
		    })
        },
        timeEvent:function(){
        	$.ajax({
        		type:'post',
        		url:'/Titles/batchRollBack',
                data:{pn:1, ps:10000},
                async:false,
                success:function(data){
                    if(data.res == null){
                        return;
                    }
                    var len = data.res.length;
                    for(var i = 0; i < len; ++i){
                        data.res[i].ts = new Date(data.res[i].ts).formatYMDHMS();
                    }
                    $('.table_time').empty();
                    var html = $('#timeContent').tmpl(data);
                    $('.table_time').append(html);
                }
        	})
        },
        confiCancelEvent:function(){
            $('button').click(function(){
                var te = $(this).attr('id');
                if(te == 'confirm'){

                }else if(te == 'cancel'){
                    $('body').unmask();
                }
            })

        },
        initDetailEvent:function(){
        	$('.detailBtn').click(function(){
        		var id = $(this).attr('targetId');
        		$.ajax({
        			type:'post',
        			url:'/Titles/batchOpLogDetail',
        			data:{id:id},
        			success:function(data){
        				$('body').mask('<div class="titleDetail"></div>')
        				var dataArr = {
										"dataArr" : data
						};
        				var html = $('#singleDetail').tmpl(dataArr);
        				$('.titleDetail').append(html);
        				
        				$('.titleClose').click(function(){
        					$(".onekey-danger").trigger('click');
        	        	});
        			}
        		})
        	});
        	
        	$('.recoverBtn').click(function() {

				var thisBtn = $(this);//得到当前对象
				var targetId = thisBtn.attr("targetId");
				var succn = thisBtn.attr("succn");
				console.log(succn)
				if (succn < 1) {
					alert("亲，成功优化的宝贝数量为0，无法还原哦！ ");
					return;
				}
				if (confirm("点击确定还原标题")) {

					$.ajax({
						url : "/Titles/recoverBatch",
						data : {
							"id" : targetId
						},
						type : "post",
						datatype : "json",
						async : true,//异步
						success : function(data) {

							alert("提示：亲，" + data.successNum + "个还原成功"
									+ data.failNum + "个还原失败。");

						},
						error : function() {
							alert("提示：哎呀出错了，请尝试刷新页面，如果问题依然存在，请联系我们");

						}
					})
				}
			});
        	
        },
        initClose:function(){
        	$('.loadmask').click(function(){
        		$('body').unmask();
        	})
        },
        initTitleSingle:function(){
//             $('.single').find('.pagenav').tmpage({
//                 currPage: 1,
//                 pageSize: 10,
//                 pageCount: 1,
//                 ajax: {
//                	 param:{},
//                     on: true,
//                     dataType: 'json',
//                     url: "/Titles/batchOpLogs",
//                     callback: function(data){
//                         if (!data || data.length == 0) {
//                             return;
//                         }
//                         $('.titleRollBack').empty();
//     		             var html = $('#maskTitleRollBack').tmpl(data);
//     					 $('.titleRollBack').append(html);
//                         AutoTitleRollBack.initTabMenu();
//                         AutoTitleRollBack.confiCancelEvent();
//                     }
//                 }
//             });
        }
	})
})(jQuery, window));