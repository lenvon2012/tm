$(document).ready(function(){
	
	// if(new Date().getTime() - TM.created < 120000){
	        // TODO display the init dialog...
	
	        $(function() {      
	            $("#dialog").dialog({
	        		closeText: "关闭",
	                autoOpen: false,
	                width: 630,
	                resizable: false,
	                title:"温情提示栏",
	                modal: true,
	                buttons: [
	                    {
	                        text: "关闭",
	                        click: function() {
	                            $(this).dialog( "close" );
	                        }
	                    }
	                ]
	            });
	            // Link to open the dialog
	            $( "#dialog-link" ).click(function( event ) {
	                $( "#dialog" ).dialog( "open" );
	                event.preventDefault();
	            });
	        });
	   // }
	    
})
   //全局的分数
    var doShopDiag = function(){
        $.getScript('/js/ShopDiagnose/ShopDiag.js');
    }

    $.getScript('/js/highcharts.js',function(){
        // TODO start the first sync...
        $.ajax({
             type : "GET",
             url : '/home/firstSync',
             data: {sid : TM.session},
             success : function(data){
                // doShopDiag();
                 //显示
                // showcontent();
                 getdiag();
                 
             },
             error : function(xmlHttp, textStatus){
                 onError && onError(xmlHttp, textStatus);
             }
         });
        //右边分数的处理
	   var showcontent = function(){ $.ajax({
	    	url:"/Titles/scoreMul",
	    	success:function(object){
	    		if(object != null){
	    			$("#userName").html(object[0].userNick);
		    		$("#categoryName").html(object[1].name)
		    		var scoreMap = object[2];
		    		$("#babyCount").html(object[3]);
		    		if(object[3]==0){
		    			initialScore();
		    		}else{
		    			shopScore(scoreMap);
		    		}
		    		
	    		}else{
	    			TM.Alert.load("亲！数据加载错误，请手动同步数据！");
	    		}
	    	}
	    });
	   };
	   //初始化分数
	   function initialScore(){
		   //当没有宝贝的情况下的默认分数
	       $("#comprehensiveScore1").html("0");
		   $("#shopFight").html("99.99%");
		   $("#typeFight").html("99.99%");
	   }
	   function shopScore(scoreMap){
            var totalCount = 0;
            var totalScore = 0;
            var map = {//防止scoreMap中有其他key
                "5": true,
                "4": true,
                "3": true,
                "12": true
            }
            for (var key in scoreMap) {
                if (map[key] == true) {
                    totalCount++;
                    totalScore += scoreMap[key];
                }
            }

            var index = 0;
            var scoreSread = {};
            var totalSread = 0;
            for (var key in scoreMap) {
                if (map[key] != true)
                    continue;
                if (index == totalCount - 1) {
                    var sread = Math.round(10000 - totalSread);
                    scoreSread[key] = sread / 100;
                } else {
                    var score = scoreMap[key];
                    var sread = Math.round(score * 10000 / totalScore);
                    totalSread += sread;
                    scoreSread[key] = sread / 100;
                }
                index++;
            }
        	var flag = 0;
        	var assess = "";
        	var allScore =85;
        	var src = "";
        	var value = scoreSread["5"];
        	if (!(value === undefined || value == null)) {
        		var str = '优秀 ';
        		if(value > flag){
        			flag = value;
        			assess = str;
        			src = "http://img01.taobaocdn.com/imgextra/i1/1039626382/T2agX1XmtdXXXXXXXX_!!1039626382.png";
        		}
        		allScore = allScore + 0.25*value*0.54;
        	}
        	value = scoreSread["4"];
        	if (!(value === undefined || value == null)) {
        		var str = '良好 ';
        		if(value > flag){
        			flag = value;
        			assess = str;
        			src = "http://img01.taobaocdn.com/imgextra/i1/1039626382/T2_pi2XeRXXXXXXXXX_!!1039626382.jpg";
        		}
        		allScore = allScore - 0.25*value*0.54;
        	}
        	value = scoreSread["3"];
        	if (!(value === undefined || value == null)) {
        		var str = '及格  ';
        		if(value > flag){
        			flag = value;
        			assess = str;
        			src = "http://img01.taobaocdn.com/imgextra/i1/1039626382/T2PV0sXdhOXXXXXXXX_!!1039626382.jpg";
        		}
        		allScore = allScore - 0.25*value;
        	}
        	value = scoreSread["12"];
        	if (!(value === undefined || value == null)) {
        		var str = '不及格 ';
        		if(value > flag){
        			flag = value;
        			assess = str;
        			src = "http://img01.taobaocdn.com/imgextra/i1/1039626382/T2trK2XchXXXXXXXXX_!!1039626382.jpg";
        		}
        			allScore = allScore - 0.25*value*1.999;
        	}
        	var lastScore=allScore*0.9999;
        	var a = parseFloat(lastScore);
        	if(a>100){
        		a=a/10;
        	}else if(a<0){
        		a=a*10;
            	a=a+"";
        	}else{
        		a=a+"";
        	}
        	var b= Math.floor(a); 
        	$("#changeface").attr("src",src);
        	$("#comprehensiveScore").html(assess);
        	//综合得分的算法
        	$("#comprehensiveScore1").html(b);
        	$("#title_star").attr("style",star(b));
        	var data = new Date();
        	var h = data.getDate();
        	var d = parseFloat((lastScore-lastScore*0.61)/0.61/h*100);
        	while(d>=100){
            	d=d/10;
        	}while(d<10){
        		d=d*10;
        	}
        	d=d+"";
        	var c = d.substring(0,d.indexOf(".") + 3);
        	$("#shopFight").html(c+"%");
        	var e = parseFloat((lastScore+0.99)*0.61/h*100);
        	while(e>=100){
            	e=e/10;
        	}while(d<10){
        		e=e*10;
        	}
        	e=e+"";
        	var f = e.substring(0,e.indexOf(".") + 3);
        	$("#typeFight").html(Math.floor(79.99-f)+"%");
        	$("#diag_ts").html(Math.floor(89.99-f)+"%");
        	$("#list_4").fadeIn(2000);
        	changeprogress(450,500)
        	end(b);
        }
	   //检测结构显示
	   var getdiag = function(){
		   $.ajax({
			   url:"/status/userdiag",
			   success:function(objects){
				   var score_progress=0;
				   //自动橱窗
//				   alert(objects[1].variance);
				   var cargocount = objects[0];
				   doShopDiag();
				   changeprogress(0,125);
				   setTimeout(function(){diag(objects[1].tradeCount,cargocount);},1000*3);
				   //changeprogress(125,250);
				   setTimeout(function(){autotime(objects[1].inBadTimeCount,objects[1].weekDistributed,cargocount)},1000*4);
//				   changeprogress(250,375);
				   setTimeout(function(){autowin(objects[1].windowUsage,objects[1].remainWindowCount)},1000*6);
//				   changeprogress(375,450);
				   setTimeout(showcontent,1000*8);
//				   changeprogress(450,500);
			   }
		   });
	   }
	   //1得到最近七天的销售情况
	   var diag = function(tradeCount,cargocount){
		   var width=0;
		   if(tradeCount == 0){
				   width = 0;
		   }else if(tradeCount<20){
			   if(cargocount>20 && cargocount < 99){
				   width = 70;
			   }else{
				   width = 90;
			   }
		   }else if(tradeCount>= 20 && tradeCount <= 60){
			   if(cargocount>20 && cargocount < 99){
				   width = 70;
			   }else{
				   width = 90;
			   }
		   }else if(tradeCount>= 60){
			   width = 90;
		   }
		   $("#diag_star").attr("style",star(width));
		   $("#diag_xl").html(tradeCount+" ");
		   $("#diag_th").html("");
		   $("#diag_ts").html("");

           $("#list_1").fadeIn(2000);
           changeprogress(125,250);
          
	   }
	   //2.自动上下架时间合理度
	   var autotime=function(inBadTimeCount,weekDistributed,cargocount){
		   var width = 0;
		   if(inBadTimeCount ==cargocount){
			   width = 0;
		   }else if (inBadTimeCount < cargocount/3){
			   width = 90;
		   }else if (inBadTimeCount >=cargocount/3 && inBadTimeCount <= cargocount/3*2){
			   width = 70;
		   }else if (inBadTimeCount >=cargocount/3){
			   width = 0;
		   }
		  
		   $("#inBadTimeCount").html(""+inBadTimeCount);
		   if(weekDistributed == "" || weekDistributed == null){
			   $("#_week").html("您店铺宝贝没有安排自动上下架。建议您立刻优化，这对您店铺的销量很重要！");
			   $("#autoup_star").attr("style",star(90));
		   }else{
			$("#autoup_star").attr("style",star(width));
		   var cargo = weekDistributed.split(",");
		  // alert(cargo[7]);
		   $("#monday").html(cargo[0]+"&nbsp;");
		   $("#tuesday").html(cargo[1]+"&nbsp;");
		   $("#wednesday").html(cargo[2]+"&nbsp;");
		   $("#thursday").html(cargo[3]+"&nbsp;");
		   $("#friday").html(cargo[4]+"&nbsp;");
		   $("#saturday").html(cargo[5]+"&nbsp;");
		   $("#sunday").html(cargo[6]+"&nbsp;");
		   }
		   $("#list_2").fadeIn(2000);
		   changeprogress(250,375);
	   }
	   //3.自动橱窗利用率
	   var autowin=function(windowUsage,remainWindowCount){
		   $("#window_star").attr("style",star(windowUsage));
		   if(windowUsage <50){
			   $("#autowin_c").html("您的自动橱窗利用率很低，严重影响了您宝贝的排名及销量，请及时改进！");
		   }else if(windowUsage > 50 && windowUsage < 90){
			   $("#autowin_c").html("您的自动橱窗利用率还是有待提高，及时改进，会为你的销量提升更多的空间！");
		   }else if(windowUsage > 90 ){
			   if(remainWindowCount > 0){
				   $("#autowin_c").html("您的自动橱窗还有剩余，请充分利用！"); 
			   }else if(remainWindowCount == 0){
				   $("#autowin_c").html("您的自动橱窗利用很充分，请继续保持！"); 
			   }
		   }
		   $("#autowin_ratio").html(windowUsage+"%");
		   $("#_count").html(remainWindowCount+"");   
		   $("#list_3").fadeIn(2000);
		   changeprogress(375,450);
	   }
	   //改变动态的进度条和扫描雷达
	   var changeprogress = function(no1,no2){
		   var i=no1;
		   while(i<=no2){
			   setTimeout(function(){$("#_schedule").attr("width",i)},100);
			   i++;
		   }
//		   for(i=no1;i<=no2;i++){
//			   alert(no1);
//			   $("#_schedule").attr("width",i);
//		   }
		   
	   }
	   var start = function(){
		   $("#shop_check_radar_scoling").attr("src","/public/images/check/saomiaox86.gif");
	   }
	   var end = function(endscore){
		   //传一个参数进来，90以上优60~90良，60以下差
		   if(endscore>=60 && endscore<85){
			   $("#shop_check_radar_scoling").attr("src","/public/images/check/normcanvas.png"); 
		   }else if(endscore<60){
			   $("#shop_check_radar_scoling").attr("src","/public/images/check/seriouscanvas.gif");
		   }else if(endscore>=85){
			   $("#shop_check_radar_scoling").removeAttr("src");
		   }
		   
	   }
	   //综合评价返回的值是背景的style
	   var star= function(data){
		   var bgimg = "background-image:url(\"/public/images/check/de-icon.png\");background-repeat:no-repeat;	width: 24px;height: 24px;float: left;";
		   var width = "0px";
		   if(parseInt(data)>=85){
			   width="-48px"; 
		   }else if(parseInt(data)>=60 && parseInt(data)<=85){
			   width="0px";
		   }else if(parseInt(data)<60){
			   width="-24px";
		   }
		   var bgstar="background-position: "+width+" 0px;";
		   return bgimg+bgstar;
		   
	   }
	   $("#shop_check_startbtn").click(function(){
		   start();
		   getdiag();
	   });
    });
    
    
    
    
 