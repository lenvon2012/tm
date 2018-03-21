				var parsePercent = function(percent){
				    var val = percent.substring(0,percent.length-1);
				    return parseFloat(val);
				}
				//global variables

				var imageSet =["http://img.taobaocdn.com/bao/uploaded/i3/13506025086655195/T21d7uXkXXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506037008916300/T2UfwtXXlbXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025086170924/T2QFMuXjJaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025086927026/T2UsEuXlNXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024665773883/T2_ozPXb4bXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024665801916/T262otXn0aXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025092309132/T2evbZXXtbXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506025087031421/T2wSwtXXVaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024665901825/T2LgEvXX4XXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506024989624695/T2YD3tXb4aXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506026997287841/T2RbAuXbFaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506026997319790/T22pQoXoJaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506024910234233/T2dykuXlBXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025086482646/T27iUuXj8XXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024989760820/T2PL.vXXNXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506026997423786/T2x.QoXdpaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506024989832631/T2tKUuXaFaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024666177595/T2ARAuXhRXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506024666197904/T2LeEvXXpXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506024910462015/T26zwtXaBaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506026997539653/T27jItXn8aXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025086698625/T24bGfXfdcXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506024910506008/T2C93tXfRaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025092669218/T2IMAvXXxXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506026997627655/T2TSouXllXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506024666437927/T2wI3mXbNbXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025086822960/T2w37vXXtXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025092805014/T2kw7uXoJXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506024990152935/T2SLcvXblXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024910662111/T2nhAuXnpXXXXXXXXX_!!1105663506-0-p4pbp.jpg"
				];







				var imageSet2 = ["http://img.taobaocdn.com/bao/uploaded/i2/14187025204958614/T2xusxXdxaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187037133092233/T2hTsmXi4bXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/14187025205831236/T2DtcwXjpaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/14187025204974545/T2As3xXc4aXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187025028594364/T2kUwwXfVaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187024785217510/T2Va3xXc8aXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187037132764048/T2Ou.xXhJXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025205819194/T2.QUxXjlXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187025107692939/T2hpZxXgXaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025107708628/T24tMwXlVaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/14187027115187674/T2cbybXntcXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025206103424/T2DIgyXe0XXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187027114971721/T2jjIxXnJXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187025107864644/T26rZmXcdbXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025210537379/T2OKUxXnpXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187025210445275/T2LUl1XhNdXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025205827079/T2ybUmXdFbXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187037133208089/T2ia7yXd8XXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187025028886204/T2r77wXh0aXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187037132976126/T2.FEyXftXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025210537379/T2OKUxXnpXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187025210445275/T2LUl1XhNdXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025205827079/T2ybUmXdFbXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025210577421/T2X3.xXoJXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187037133208089/T2ia7yXd8XXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187025028886204/T2r77wXh0aXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187037132976126/T2.FEyXftXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/14187025029030248/T2x_sxXdRXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187025028614141/T24aehXXFdXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/14187037132864353/T2olsxXhXXXXXXXXXX_!!1105704187-0-p4pbp.jpg",


				"http://img.taobaocdn.com/bao/uploaded/i2/14187025510970989/T2XsmTXeNbXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/14187037453728171/T2DI7CXh8aXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/14187025093185606/T2xOJuXiNOXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187037453752056/T2SOrZXXpbXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187025514315276/T2b5UDXepXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/14187025514389270/T2OXXZXddcXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187037453732249/T2vpUEXXhXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/14187025514365156/T2l79qXf4XXXXXXXXX_!!1105704187-0-p4pbp.jpg"
				];

				var imageSet3 = [
				"http://img.taobaocdn.com/bao/uploaded/i4/13506037189468208/T2p2MxXoBaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025259907498/T2Jb7zXgXXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506025259386813/T2q27xXj8aXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506025262013431/T2Eu.zXXpXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506027170071847/T2au.yXhXaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506025261577246/T2m8syXohXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506024839081771/T2rCoyXjpXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025258874982/T2ar6SXo4aXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506027170571679/T2ppzJXgJbXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025260015161/T2DUZyXgRXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506037189552046/T2oy7yXaFaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506025161704544/T2LCsyXjtXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i1/13506025083922202/T2xznyXa0bXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025259943452/T2aoUyXipXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506027170103799/T2SfAyXhJXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506037189500357/T22IwzXbVXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025259555406/T2GI3zXfXXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025162728898/T2IhEzXbBXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025260359454/T2vrQzXg8XXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506025259835251/T2BnoxXkBaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025083110310/T2KWozXkpXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506024839261808/T2CrMzXg4XXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506024839749949/T2C4AyXjdXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025262089368/T2GqZzXghXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i2/13506025259987070/T2OIokXn0XXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506024839709946/T2pfMzXddXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506024839565973/T20J3yXipaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i3/13506025083342133/T29OkzXbxXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025161680549/T2iEQyXoXXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
				"http://img.taobaocdn.com/bao/uploaded/i4/13506025258926700/T2GE.xXaJbXXXXXXXX_!!1105663506-0-p4pbp.jpg"
				];

				var autoTitleImageSet = [
			"http://img.taobaocdn.com/bao/uploaded/i2/14187025318819359/T27LZAXbVXXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i4/14187024898341592/T2vXiQXj4bXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i4/14187025142610197/T25fUyXblbXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i3/14187025142586109/T2PO7pXoJaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i4/14187025318711081/T244KtXidcXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i4/14187024898393884/T2ZRJSXideXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i3/14187025220768851/T2ru7zXcpaXXXXXXXX_!!1105704187-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/14187025142554355/T2x_QzXllXXXXXXXXX_!!1105704187-0-p4pbp.jpg"
			];

			 var dragon_bigexiboSet = [
			"http://img.taobaocdn.com/bao/uploaded/i3/13506025371434578/T2vckBXelXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i1/13506025275464983/T2RbsBXihXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506037307656222/T2Qd7BXgxXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506027282883659/T2rLl.XX4dXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506025371434614/T29AIjXj0XXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506024953001590/T2FxMzXjBaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i3/13506025372727044/T2LgZyXm4aXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506027282875927/T2dO.AXeJXXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506037307644372/T2m5EzXgNaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506024952981958/T272UAXn8XXXXXXXXX_!!1105663506-0-p4pbp.jpg"
			];

			var backupDragon = [
			"http://img.taobaocdn.com/bao/uploaded/i2/13506025275468585/T2zA3zXXhaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i2/13506025275472675/T2NyozXnxaXXXXXXXX_!!1105663506-0-p4pbp.jpg",
			"http://img.taobaocdn.com/bao/uploaded/i4/13506037307652085/T2q1QaXmRaXXXXXXXX_!!1105663506-0-p4pbp.jpg"
			];

				/**
				11006513 etao
				11041512 一淘首焦
				11041513 站外计划
				11074801 旺旺卖家中心
				*/
				//compainIds = [11006513,11041512,11041513,11074801];
				compainIds = [11381311,11163053,11433858,11387260];

				((function($, window) {
				var TM = TM || {};
				TM.domain = "http://subway.etao.com/";
				TM.robotpush = TM.robotpush || {};
				TM.robotpush = $.extend({
					init:function(){
						TM.robotpush.initImages();
					},
					initImages :function(){
						//this.images = dragon_bigexiboSet;
						this.images = imageSet2;
					},
					extractMediaImgId:function(imgUrl){
						var prefix = "http://img.taobaocdn.com/bao/uploaded/i4/";
						var begin = prefix.length;
						var end = imgUrl.indexOf("/",begin);
						var subString = imgUrl.substring(begin,end);
						//console.log(subString);
						return subString;
					},
					pushOneAdGroup:function(token,compainIndex,uniquePrefix,productUrl,defaultPrice){

					},
					runPush:function(token,compainIndex,interval,targetNumber,uniquePrefix,productUrl,defaultPrice){
						var looperIndex = 0;
						
						window.setInterval(function(){
							console.log("runPush run,looperIndex:"+looperIndex+",targetNumber:"+targetNumber);

							looperIndex+=1;
							if(looperIndex<targetNumber){
								var filename = uniquePrefix+looperIndex;
			/*
								$.ajax({
								url:'/wbpageadgroup/editCreative4add.htm?token='+token+'&campaignId='+compainIds[compainIndex]+'&',
								async:false,
								data:{
									"adGroupDefaultPrice":defaultPrice,
									"adgroupDO.catId":'50017652 50018647 50050249',
									"adgroupDO.linkUrl":productUrl+'&tracelog='+filename,
									"adgroupDO.outsideNumId":'',
									"goback":'0'
								},
								success:function(response){

									var adGroupId = $(response).find('#adgroupId').val();
									console.log('adGroupId='+adGroupId);
									
									if(!isNaN(parseInt(adGroupId))){
										$.ajax({
											"url":'/wbpageadgroup/doAddwbAdgroup.htm?token='+token+'&campaignId='+compainIds[compainIndex]+'&',
											"type":'post',
											"data":{
												"adGroupDefaultPrice":defaultPrice,
												"adgroupDO.catId":'50017652 50018647 50050249',
												"adgroupDO.linkUrl":productUrl+'&tracelog='+filename,
												"adgroupDO.outsideNumId":'',
												"adgroupId":adGroupId,
												"catPathName":'TP服务商大类>流量推广>直通车优化',
												"creativeElementsJson":'[{"id":"7","context":[{"cname":"IMGURL","cvalue":"'+TM.robotpush.images[looperIndex%TM.robotpush.images.length]+'","mediaImgId":"'+TM.robotpush.extractMediaImgId(TM.robotpush.images[looperIndex%TM.robotpush.images.length])+'"},{"cname":"TITLE","cvalue":"'+filename+'"},{"cname":"LINKURL","cvalue":"'+productUrl+'&tracelog='+filename+'"}]}]',
												"creativeTitle":filename,
												"editQuery":'[{"pid":"1","sid":"107"}]',
												"goback":'0',
												"originalWords":'bus',
												"outsidePrice":'',
												"pageInfo.toPage":'',
												"plState":'1'
											},
											"success":function(response){

											},
											"error":function(request,errorText){
												console.log('error!');
											}
										});
									}
									
								},
								error:function(request,errorText){
									console.log('error!');
								}
							});
				*/
								
								$.ajax({
					    				url:TM.domain+'wbpageadgroup/doAddwbAdgroup.htm?token='+token+'&campaignId='+compainIds[compainIndex]+'&',
					    				type:'post',
									    data:{
											"adGroupDefaultPrice":defaultPrice,
											"adgroupDO.catId":'50014811 50014851 50008117',
											"adgroupDO.linkUrl":productUrl+'&tracelog='+filename,
											"adgroupDO.outsideNumId":'',
											"catPathName":'网店/网络服务/软件>网络服务>网站竞价排名',
											"creativeDO.imgURL":'',
											"creativeDO.mediaImgId":'',	
											"creativeElementsJson":'[{"cname":"IMGURL","cvalue":"'+TM.robotpush.images[looperIndex%TM.robotpush.images.length]+'","mediaImgId":"'+TM.robotpush.extractMediaImgId(TM.robotpush.images[looperIndex%TM.robotpush.images.length])+'"},{"cname":"TITLE","cvalue":"'+filename+'"},{"cname":"SUBTITLE","cvalue":""},{"cname":"DESCRIPTION","cvalue":""},{"cname":"LINKURL","cvalue":"'+productUrl+'&tracelog='+filename+'"},{"cname":"DISPLAYURL","cvalue":"'+productUrl+'&tracelog='+filename+'"}]',	
											"creativeType":'addPage',
											"imgFile":'img.jpg',
											"isCouponType":'0',
											"originalWords":'bus',
											"pageInfo.toPage":'',	
											"plState":'1',
											"templateId":'9'
									    },
									    success:function(response){
									        
									    },
									    error:function(request,errorText){
									    	
									    }
								});
								
								//looperIndex += 1;
							}
							//looperIndex += 1;
							
						},interval*1000);
						
					},
					editShowPostiion:function(token,compainIndex,searchQuery){
						var compainId = compainIds[compainIndex];
						var totalNumber = 0;
						var pageNumber = 1;
						var totalPage = 1;
							while(pageNumber<=totalPage ){
								var localPageNumber = pageNumber;
								$.ajax({
									url:TM.domain+'wbpageadgroup/listwb.htm?token='+token+'&campaignId='+compainId+'&',
									type:'post',
									async:false,
									data:{
										"pageNumber":localPageNumber,
										"formTarget":'',
										"queryState":'',
										"pageInfo.totalItem":0,
										"rptDays":1,
										"queryTitle":searchQuery,
										"queryWord":''
									},
									success:function(response){
										//totalNumber = parseInt($(".list-num").html().substring($(".list-num").html().indexOf("：")+1));
										totalPage = Math.ceil(parseInt($(response).find(".list-num").html().substring($(response).find(".list-num").html().indexOf("：")+1))/20);
										console.log("total page is :"+totalPage);
										console.log("process page:"+localPageNumber);
										
										$(response).find(".table-list").find("tr.even,tr.odd").find("td:last-child").find("a:first-child").each(function(index,value){
							
											var origin = $(this).attr("mxclick");
											var begin = origin.indexOf(":",0);
											var end = origin.indexOf(":",begin+1);
											var groupId = origin.substring(begin+1,end);
											//console.log(groupId);
											
											$.ajax({
												url:TM.domain+'wbpageTag/editAdgroupTag.htm?campaignId='+compainId+'&token='+token,
												type:"post",
												data:{
													"adGroupId":groupId,
													"dimId":1,
													"editQuery":107,
													"isAjaxRequest":true,
													"sla":"json"
												},
												success:function(response){

												},
												error:function(request,errorText){

												}
											});
										
											

										});
									},
									error:function(request,errorText){

									}
									});

									pageNumber++;
							}
					},
					updateDefaultPrice:function(token,compainId,adGroupId,defaultPrice){
						/*
						$.ajax({
							"url":'/wbpageadgroup/updatePageDefaultPrice.htm?token='+token+'&compainId='+compainId+'&',
							"type":'post',
							"data":{
								"adGroupId":adGroupId,
								"defaultPrice":defaultPrice
							},
							success:function(dataJson){
								if(dataJson.isSuccess){
									console.log("update group default price success:"+adGroupId);
								}else{
									console.log("update group default price fail:"+adGroupId);
								}
							},
							error:function(request,errorText){
								console.log("network error!");
							}
						});
						*/
						$.post('/wbpageadgroup/updatePageDefaultPrice.htm?token='+token+'&campaignId='+compainId+'&&isAjaxRequest=true',
							{adGroupId:adGroupId,defaultPrice:defaultPrice},function(){
								console.log("update group default price success:"+adGroupId);
							});
					},
					updateDefaultPriceBatchRegex:function(token,compainIndex,re,defaultPrice){
							var compainId = compainIds[compainIndex];
							var totalNumber = 0;
							var pageNumber = 1;
							var totalPage = 1;
							while(pageNumber<=totalPage ){
									var localPageNumber = pageNumber;
									$.ajax({
										url:TM.domain+'wbpageadgroup/listwb.htm?token='+token+'&campaignId='+compainId+'&',
										type:'post',
										async:false,
										data:{
											"pageNumber":localPageNumber,
											"formTarget":'',
											"queryState":'',
											"pageInfo.totalItem":0,
											"rptDays":1,
											"queryTitle":'',
											"queryWord":''
										},
										success:function(response){

											//totalNumber = parseInt($(".list-num").html().substring($(".list-num").html().indexOf("：")+1));
											totalPage = Math.ceil(parseInt($(response).find(".list-num").html().substring($(response).find(".list-num").html().indexOf("：")+1))/20);
											console.log("total page is :"+totalPage);
											console.log("process page:"+localPageNumber);
											var tableList = $(response).find(".table-list");
											$(response).find(".table-list").find("tr.even,tr.odd").find("td:last-child").find("a:first-child").each(function(index,value){
								
												var origin = $(this).attr("mxclick");
												var begin = origin.indexOf(":",0);
												var end = origin.indexOf(":",begin+1);
												var groupId = origin.substring(begin+1,end);
												var titleId = "adGroupTitle_"+groupId;
												var title = $(tableList).find("#"+titleId).text();
												if(title.search(re)!=-1){
													//send update request
													
													TM.robotpush.updateDefaultPrice(token,compainId,groupId,defaultPrice);
												}

											});
											pageNumber--;
										},
										error:function(request,errorText){

										}
								});
								pageNumber++;
							}
					},
					removeGroup:function(token,compainId,groupId){
						$.ajax({
							"url":TM.domain+'wbpageadgroup/deletePage.htm?token='+token+'&campaignId='+compainId+'&',
							"type":'post',
							"data":{
								"adGroupId":groupId
							},
							"success":function(dataJson){
								if(dataJson.isSuccess){
									console.log("delete group id success:"+groupId);
								}else{
									console.log("delete group id fail:"+groupId);
								}
							},
							"error":function(request,errorText){

							}
						})
					},

						removeGroupRegex:function(token,compainIndex,re){
							var compainId = compainIds[compainIndex];
							var totalNumber = 0;
							var pageNumber = 1;
							var totalPage = 1;
							while(pageNumber<=totalPage ){
									var localPageNumber = pageNumber;
									$.ajax({
										url:TM.domain+'wbpageadgroup/listwb.htm?token='+token+'&campaignId='+compainId+'&',
										type:'post',
										async:false,
										data:{
											"pageNumber":localPageNumber,
											"formTarget":'',
											"queryState":'',
											"pageInfo.totalItem":0,
											"rptDays":1,
											"queryTitle":'',
											"queryWord":''
										},
										success:function(response){

											//totalNumber = parseInt($(".list-num").html().substring($(".list-num").html().indexOf("：")+1));
											totalPage = Math.ceil(parseInt($(response).find(".list-num").html().substring($(response).find(".list-num").html().indexOf("：")+1))/20);
											console.log("total page is :"+totalPage);
											console.log("process page:"+localPageNumber);
											var tableList = $(response).find(".table-list");
											$(response).find(".table-list").find("tr.even,tr.odd").find("td:last-child").find("a:first-child").each(function(index,value){
								
												var origin = $(this).attr("mxclick");
												var begin = origin.indexOf(":",0);
												var end = origin.indexOf(":",begin+1);
												var groupId = origin.substring(begin+1,end);
												var titleId = "adGroupTitle_"+groupId;
												var title = $(tableList).find("#"+titleId).text();
												if(title.search(re)!=-1){
													//send delete request
													TM.robotpush.removeGroup(token,compainId,groupId);
												}

											});
											//pageNumber--;
										},
										error:function(request,errorText){

										}
								});
								pageNumber++;
							}
					},
					getAdGroupList:function(token,compainIndex){
						var list = new Array();
						var compainId = compainIds[compainIndex];
							var totalNumber = 0;
							var pageNumber = 1;
							var totalPage = 1;
							while(pageNumber<=totalPage ){
									var localPageNumber = pageNumber;
									$.ajax({
										url:TM.domain+'wbpageadgroup/listwb.htm?token='+token+'&campaignId='+compainId+'&',
										type:'post',
										async:false,
										data:{
											"pageNumber":localPageNumber,
											"formTarget":'',
											"queryState":'',
											"pageInfo.totalItem":0,
											"rptDays":1,
											"queryTitle":'',
											"queryWord":''
										},
										success:function(response){

											totalPage = Math.ceil(parseInt($(response).find(".list-num").html().substring($(response).find(".list-num").html().indexOf("：")+1))/20);
											console.log("total page is :"+totalPage);
											console.log("process page:"+localPageNumber);
											

											var tableList = $(response);


											$(response).find(".table-list").find("tr.even,tr.odd").find("td:last-child").find("a:first-child").each(function(index,value){
								
												var origin = $(this).attr("mxclick");
												var begin = origin.indexOf(":",0);
												var end = origin.indexOf(":",begin+1);
												var groupId = origin.substring(begin+1,end);
												var titleId = "adGroupTitle_"+groupId;
												var pvId = "pv_"+groupId;
												var clickNumId = "realClkCnt_"+groupId;
												var clickRateId = "clkRate_"+groupId;
												var totalCostId = "finCosts_"+groupId;
												var avgCostId = "avgClkCosts_"+groupId;
												var adGroup = new Array();
												//var tableList = $(response).find(".table-list");
												adGroup.groupId = groupId;
												adGroup.title = $(tableList).find("#"+titleId).text();
												adGroup.pv = $(tableList).find("#"+pvId).text();
												adGroup.clickNum = $(tableList).find("#"+clickNumId).text();
												adGroup.clickRate = $(tableList).find("#"+clickRateId).text();
												adGroup.totalCost = $(tableList).find("#"+totalCostId).text();
												adGroup.avgCost = $(tableList).find("#"+avgCostId).text();
												if(adGroup.pv!="-"){
													console.log(adGroup.pv);
												}
												list.push(adGroup);
											});
										},
										error:function(request,errorText){

										}
								});
								pageNumber++;
							}
							return list;
					},
					getAdGroupListSync:function(token,pageNumber){
						var totalPage = Math.ceil(parseInt($(".list-num").html().substring($(".list-num").html().indexOf("：")+1))/20);
						if(pageNumber<totalPage){
							$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#jumpto.input-text").val(pageNumber);
							$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#viewit").click();
							$(function(){
								setTimeout(function(){

									var tableList = $(".table-list");
									$(".table-list").find("tr.even,tr.odd").find("td:last-child").find("a:first-child").each(function(index,value){
						
										var origin = $(this).attr("mxclick");
										var begin = origin.indexOf(":",0);
										var end = origin.indexOf(":",begin+1);
										var groupId = origin.substring(begin+1,end);
										var titleId = "adGroupTitle_"+groupId;
										var pvId = "pv_"+groupId;
										var clickNumId = "realClkCnt_"+groupId;
										var clickRateId = "clkRate_"+groupId;
										var totalCostId = "finCosts_"+groupId;
										var avgCostId = "avgClkCosts_"+groupId;
										var adGroup = new Array();
										//var tableList = $(response).find(".table-list");
										adGroup.groupId = groupId;
										adGroup.title = $(tableList).find("#"+titleId).text();
										adGroup.pv = $(tableList).find("#"+pvId).text();
										adGroup.clickNum = $(tableList).find("#"+clickNumId).text();
										adGroup.clickRate = $(tableList).find("#"+clickRateId).text();
										adGroup.totalCost = $(tableList).find("#"+totalCostId).text();
										adGroup.avgCost = $(tableList).find("#"+avgCostId).text();
										if(adGroup.pv!="-"){
											console.log(adGroup.pv);
										}
										list.push(adGroup);
									});
									TM.robotpush.getAdGroupListSync(token,pageNumber+1);
								},10*1000);
							});
						}
					},
					policyRemoveAdGroupList:function(token){
						var list = new Array();
						var totalPage = Math.ceil(parseInt($(".list-num").html().substring($(response).find(".list-num").html().indexOf("：")+1))/20);
						var currentPage = 1;
						while(currentPage<totalPage){
							$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#jumpto.input-text").val(currentPage);
							$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#viewit").click();
							$(function(){
								setTimeout(function(){

								},1000*10);
							})
							currentPage++;
						}

						$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#jumpto.input-text").val()
						for(var i=0;i<groupList1.length;++i){
							
							if(groupList1[i].pv!="-"){
								console.log(groupList1[i].pv);
							}	
						}
						
						//console.log(groupList1);

						//aList = groupList1;
					},
					getAdGroupList2:function(token,compainIndex){

					}
				},TM.robotpush);



				TM.robotpush.init();
				
				var chedaoUrl = "http://fuwu.taobao.com/ser/detail.htm?service_code=FW_GOODS-1841777";
				var autoTitleUrl = "http://fuwu.taobao.com/ser/detail.htm?service_code=FW_GOODS-1835721";


			/*	
				TM.robotpush.runPush('F6LMr8',0,0.5,2000,'717.lgem01',chedaoUrl,'8');

				TM.robotpush.runPush('F6LMr8',1,0.5,1936,'717.lgem02',chedaoUrl,'8');
				TM.robotpush.runPush('F6LMr8',2,0.5,1922,'717.lgem03',chedaoUrl,'8');
				
				TM.robotpush.runPush('F6LMr8',3,0.5,1983,'717.lgem04',chedaoUrl,'8');

			window.setInterval(function(){

				TM.robotpush.editShowPostiion('F6LMr8',0,'717.lgem');
				TM.robotpush.editShowPostiion('F6LMr8',1,'717.lgem');
				TM.robotpush.editShowPostiion('F6LMr8',2,'717.lgem');
				TM.robotpush.editShowPostiion('F6LMr8',3,'717.lgem');
			},1000*1000);	
			*/
			/*
				TM.robotpush.runPush('GbXNr8',0,0.5,1900,'723.lgem01',chedaoUrl,'8');

				TM.robotpush.runPush('GbXNr8',1,0.5,1936,'723.lgem02',chedaoUrl,'8');
				TM.robotpush.runPush('GbXNr8',2,0.5,1922,'723.lgem03',chedaoUrl,'8');
				
				TM.robotpush.runPush('GbXNr8',3,0.5,1983,'723.lgem04',chedaoUrl,'8');
			
*/
		/*
			window.setInterval(function(){

				TM.robotpush.editShowPostiion('GbXNr8',0,'719.lgem');
				TM.robotpush.editShowPostiion('GbXNr8',1,'719.lgem');
				TM.robotpush.editShowPostiion('GbXNr8',2,'719.lgem');
				TM.robotpush.editShowPostiion('GbXNr8',3,'719.lgem');
			},500*1000);	
		*/

		
	
				TM.robotpush.editShowPostiion('GbXNr8',0,'723.lgem');
				TM.robotpush.editShowPostiion('GbXNr8',1,'723.lgem');
				TM.robotpush.editShowPostiion('GbXNr8',2,'723.lgem');
				TM.robotpush.editShowPostiion('GbXNr8',3,'723.lgem');
	

	/*
				TM.robotpush.removeGroupRegex("GbXNr8",0,/^722/);
				TM.robotpush.removeGroupRegex("GbXNr8",1,/^722/);
				TM.robotpush.removeGroupRegex("GbXNr8",2,/^722/);
				TM.robotpush.removeGroupRegex("GbXNr8",3,/^722/);


*/
				//TM.robotpush.policyRemoveAdGroupList('F6LMr8');

				//TM.robotpush.getAdGroupListSync('F6LMr8',currentPage+1);
				//TM.robotpush.removeGroupRegex('F6LMr8',0,/^6/);

				/*
				var parsePercent = function(percent){
				    var val = percent.substring(0,percent.length-1);
				    return parseFloat(val);
				}
				function paramValue(requestName,url){var requestValue="";if(url.indexOf("?")!=-1){var str=url;var paramCount="";var name=new Array;var value=new Array;var totalParam=str.substring(str.indexOf("?")+1);paramCount=totalParam.split("&").length;for(var i=0;i<paramCount;i++){name[name.length]=totalParam.split("&")[i].split("=")[0];value[value.length]=totalParam.split("&")[i].split("=")[1]}for(var j=0;j<name.length;j++)if(name[j]==requestName){requestValue=value[j];break}return requestValue}}
				var currentPage = $("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#jumpto.input-text").val();
				list = new Array();
									var tableList = $(".table-list");
									$(".table-list").find("tr.even,tr.odd").find("td:last-child").find("a:first-child").each(function(index,value){
						
										var origin = $(this).attr("mxclick");
										var begin = origin.indexOf(":",0);
										var end = origin.indexOf(":",begin+1);
										var groupId = origin.substring(begin+1,end);
										var titleId = "adGroupTitle_"+groupId;
										var pvId = "pv_"+groupId;
										var clickNumId = "realClkCnt_"+groupId;
										var clickRateId = "clkRate_"+groupId;
										var totalCostId = "finCosts_"+groupId;
										var avgCostId = "avgClkCosts_"+groupId;
										var adGroup = new Array();
										//var tableList = $(response).find(".table-list");
										adGroup.groupId = groupId;
										adGroup.title = $(tableList).find("#"+titleId).text();
										adGroup.pv = $(tableList).find("#"+pvId).text();
										adGroup.clickNum = $(tableList).find("#"+clickNumId).text();
										adGroup.clickRate = $(tableList).find("#"+clickRateId).text();
										adGroup.totalCost = $(tableList).find("#"+totalCostId).text();
										adGroup.avgCost = $(tableList).find("#"+avgCostId).text();
										if(adGroup.pv=='-' && adGroup.title.search(/713/)!=-1){
											TM.robotpush.removeGroup(paramValue('token',window.location.href),paramValue('campaignId',window.location.href),adGroup.groupId);
											console.log(adGroup.pv+":"+parsePercent(adGroup.clickRate)+":"+adGroup.avgCost);
										}	
										if(adGroup.pv!="-"){
											//console.log(adGroup.pv+":"+parsePercent(adGroup.clickRate));
											if(adGroup.pv>110 && parsePercent(adGroup.clickRate)<0.7){
												if(adGroup.title.search(/713/)!=-1){
													TM.robotpush.removeGroup(paramValue('token',window.location.href),paramValue('campaignId',window.location.href),adGroup.groupId);
													console.log(adGroup.pv+":"+parsePercent(adGroup.clickRate)+":"+adGroup.avgCost);
												}
											}
											if(adGroup.pv>50 && adGroup.pv<=110 && parsePercent(adGroup.clickRate)<0.001){
												if(adGroup.title.search(/713/)!=-1){
													TM.robotpush.removeGroup(paramValue('token',window.location.href),paramValue('campaignId',window.location.href),adGroup.groupId);
													console.log(adGroup.pv+":"+parsePercent(adGroup.clickRate)+":"+adGroup.avgCost);
												}
											}
											//console.log(parsePercent(adGroup.clickRate));
										}
										list.push(adGroup);
									});
					alert("finish one page");
					$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#jumpto.input-text").val(parseInt(currentPage)+1);
							
					$("html body#vc-root div#content div.layout div.col-main div.main-wrap div.pagination div.page-bottom span.page-skip input#viewit").click();
				*/

				})(jQuery,window));
