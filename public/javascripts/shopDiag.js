((function ($, window) {
    TM.ShopDiag = TM.ShopDiag || {};
    var me = TM.ShopDiag;
    TM.ShopDiag = $.extend({
        genShopDiagDiv : function(container, nextUrl){
            container.empty();
            $.cookie("diagScore", 0);
            container.append(TM.ShopDiag.createDiagHead());
            me.nextUrl = nextUrl || '/home/autoTitle';
            container.append($('<div class="topTradeCountDiv"><div class="tradebrief">影响店铺商品转化销售能力的因素很多，主要包括橱窗推荐、爆款宝贝、标题优化程度、上下架分布以及店铺转化率</div></div>'));
            setInterval(function(){
                container.find('.tradebrief').toggleClass("red");
            },400);

            $.get("/Diag/shop",function(data){
                container.attr('diagScore',data.reverse);
                $("body").append($('<div class="goYouhuaBtn"><a style="display:block;" href="'+me.nextUrl+'"><img src="/public/images/shopDiag/ljyh2.png"/></a></div>'));
                container.append(TM.ShopDiag.createDiagBody(data));
                TM.ShopDiag.distributeDiag(data);
                TM.ShopDiag.showGoodDiag();
                TM.ShopDiag.genDiagScore(data);
            });
        },
        genDiagScore : function(data){
            var diagScore = 0;
            //title score
            diagScore += Math.floor(15*(data.titleScore/100));
            //windows score
            diagScore += Math.floor(15*(data.windowUsage/100));
            //delist time score
            if(data.weekDistributed == null){
                var itemsNum = 0;
            } else {
                var itemDist = data.weekDistributed.split(",");
                var itemsNum = 0;
                for (var i = 0; i < itemDist.length; i++) {
                    itemsNum += parseInt(itemDist[i]);
                }
            }
            if(itemsNum != 0) {
                diagScore += Math.floor(15*(data.inBadTimeCount/itemsNum));
            } else {
                diagScore = 0;
            }
            //tradeCount score
            if(data.tradeCount < 30) {
                diagScore += 5;
            } else if(data.tradeCount < 100) {
                diagScore += 10;
            } else {
                diagScore += 15;
            }
            //burstItems score
            if(data.goodItemCount == 1){
                diagScore += 7.5;
            } else if(data.goodItemCount > 1) {
                diagScore += 15;
            }
            //potentionItems score
            if(data.potentialGoodItemCount == 0) {

            } else if(data.potentialGoodItemCount < 3){
                diagScore += 5;
            } else {
                diagScore += 10;
            }
            //conversionRate score
            if(data.conversionRate == 1000) {
                diagScore += 5;
            } else if(data.conversionRate < 3000){
                diagScore += 10;
            } else {
                diagScore += 15;
            }
            diagScore += Math.floor(10*(data.titleScore/100));
            $.cookie("diagScore",diagScore);
        },
        createDiagBottom : function(){
            var bottom = $('<div class="bottom"></div>');
            var warnSpan = $('<span class="warnSpan">升业绩S级提醒：上述问题严重影响您的店铺运营和爆款打造！！</span>');
            setInterval(function(){
                warnSpan.toggleClass("red");
            },400);
            bottom.append(warnSpan);
            //var youhuaBtn = $('<div class="goYouhuaBtn"><a style="display:block;" href="'+me.nextUrl+'"><img src="/public/images/shopDiag/ljyh2.png"/></a></div>');
            //bottom.append(youhuaBtn);
            $('.goYouhuaBtn').fadeIn(1000);
            return bottom;
        },
        distributeDiag : function(data){
            var niceDetail = $('.niceDetail');
            var sosoDetail = $('.sosoDetail');
            var badDetail = $('.badDetail');
            // distribute title score diag to bad, or soso , or nice
//            if(data.titleScore < 70){
//                badDetail.append(TM.ShopDiag.createTitleScoreDiv(data));
//            } else if(data.titleScore < 85) {
//                sosoDetail.append(TM.ShopDiag.createTitleScoreDiv(data));
//            } else {
//                niceDetail.append(TM.ShopDiag.createTitleScoreDiv(data));
//            }

            // distribute burstItems diag to bad, or soso , or nice
            if(data.goodItemCount <= 2){
                badDetail.append(TM.ShopDiag.createBurstDiagDiv(data));
            } else if(data.titleScore < 4) {
                sosoDetail.append(TM.ShopDiag.createBurstDiagDiv(data));
            } else {
                niceDetail.append(TM.ShopDiag.createBurstDiagDiv(data));
            }

            // distribute potential burstItems diag to bad, or soso , or nice
            if(data.potentialGoodItemCount <= 2){
                badDetail.append(TM.ShopDiag.createPotentialGoodItemsDiag(data));
            } else if(data.titleScore < 4) {
                sosoDetail.append(TM.ShopDiag.createPotentialGoodItemsDiag(data));
            } else {
                niceDetail.append(TM.ShopDiag.createPotentialGoodItemsDiag(data));
            }

            // distribute conversionRate diag to bad, or soso , or nice
            if(data.conversionRate <= 1000){
                badDetail.append(TM.ShopDiag.createConvertionRateDiagDiv(data));
            } else if(data.titleScore < 2000) {
                sosoDetail.append(TM.ShopDiag.createConvertionRateDiagDiv(data));
            } else {
                niceDetail.append(TM.ShopDiag.createConvertionRateDiagDiv(data));
            }

            // distribute delist time diag to bad, or soso , or nice
            if(data.inBadTimeCount > 0){
                badDetail.append(TM.ShopDiag.createDelistDiagDiv(data));
            } else {
                niceDetail.append(TM.ShopDiag.createDelistDiagDiv(data));
            }

            // distribute windowsUsage diag to bad, or soso , or nice
            if(data.remainWindowCount > 0){
                badDetail.append(TM.ShopDiag.createWindowDiagDiv(data));
            } else {
                niceDetail.append(TM.ShopDiag.createWindowDiagDiv(data));
            }

            // distribute tradeCount diag ， always in bad, hhhhhhhhaaa
            //if(data.tradeCount <= 1000){
            badDetail.append(TM.ShopDiag.createTradeCountDiv(data));
            //} else if(data.titleScore < 2000) {
            //    sosoDetail.append(TM.ShopDiag.createTradeCountDiv(data));
           // } else {
            //    niceDetail.append(TM.ShopDiag.createTradeCountDiv(data));
           // }
        },
        showGoodDiag : function(){
            var width =  parseInt($('.progressLine').css("width").replace("px","")) + 8;
            $('.progressLine').css("width",width + "px");
            $('.diagNice').find('.diagNiceInfo').fadeIn(1000,function(){
                TM.ShopDiag.showChild($('.diagNice').find('.diagDiv'), 0, TM.ShopDiag.showSosoDiag, 0);
            });
        },
        showChild : function(arr, currIndex, showNext, region){
            if(currIndex >= arr.length){
                showNext();
                return;
            }
            var width =  parseInt($('.progressLine').css("width").replace("px","")) + 75;
            if(width <= 550)  {
                $('.progressLine').css("width",width + "px");
            } else {
                width = "550";
                $('.progressLine').css("width",width + "px");
            }

            switch(region){
                case 0: $('.niceNum').html(parseInt( $('.niceNum').html())+1);break;
                case 1: $('.sosoNum').html(parseInt( $('.sosoNum').html())+1);$('.problemNum').html(parseInt($('.problemNum').html())+1);break;
                case 2: $('.badNum').html(parseInt( $('.badNum').html())+1);$('.problemNum').html(parseInt($('.problemNum').html())+1);break;
                default : $('.badNum').html(parseInt( $('.badNum').html())+1);$('.problemNum').html(parseInt($('.problemNum').html())+1);break;
            }
            $(arr[currIndex]).fadeIn(1000,function(){
                TM.ShopDiag.showChild(arr, currIndex+1, showNext, region);
            })
        },
        showSosoDiag : function(){
            var width =  parseInt($('.progressLine').css("width").replace("px","")) + 8;
            $('.progressLine').css("width",width + "px");
            $('.diagSoso').find('.diagSosoInfo').fadeIn(1000,function(){
                TM.ShopDiag.showChild($('.diagSoso').find('.diagDiv'), 0, TM.ShopDiag.showBadDiag, 1);
            });
        },
        showBadDiag : function(){
            var width =  parseInt($('.progressLine').css("width").replace("px","")) + 8;
            $('.progressLine').css("width",width + "px");
            $('.diagBad').find('.diagBadInfo').fadeIn(1000,function(){
                TM.ShopDiag.showChild($('.diagBad').find('.diagDiv'), 0, function(){
                    $('.progressBar').fadeOut(1000, function(){
                    $('.diagScore').fadeIn(1000, function(){
                        $(this).empty();
                        $(this).append("体检得分:");
                        $(this).append($('<span class="scoreNum diagScore">'+ $.cookie("diagScore")+'</span>'));
                        $(this).append("你的店铺商品转化销售能力:");
                        if($('.badDetail').find('.diagDiv').length < 1) {
                            $(this).append($('<span class="scoreNum">良好！</span>'));
                        } else if ($('.badDetail').find('.diagDiv').length < 3) {
                            $(this).append($('<span class="scoreNum">马马虎虎！</span>'));
                        } else {
                            $(this).append($('<span class="scoreNum">弱爆啦！</span>'));
                        }
                        if($.cookie("diagScore") < 70) {
                            $('.headLogo').removeClass("headLogoSoso");
                            $('.headLogo').removeClass("headLogoNice");
                            $('.headLogo').addClass("headLogoBad");
                        } else if ($.cookie("diagScore") < 85) {
                            $('.headLogo').removeClass("headLogoBad");
                            $('.headLogo').removeClass("headLogoNice");
                            $('.headLogo').addClass("headLogoSoso");
                        } else {
                            $('.headLogo').removeClass("headLogoSoso");
                            $('.headLogo').removeClass("headLogoBad");
                            $('.headLogo').addClass("headLogoNice");
                        }
                        var reDiag = $('<span class="scoreNum">重新检测</span>');
                        $('.headInfo').append(reDiag);
                        reDiag.click(function(){
                            TM.ShopDiag.genShopDiagDiv($('.shopDiag'));
                        });
                        $('.shopDiag').append(TM.ShopDiag.createDiagBottom());
                        $('.shopDiag').append($('<div style="width: 100%;height: 100px;"></div>'));
                    });
                })}, 2);
            });
        },
        createDiagHead : function(){
            var diagHead = $('<div class="shopDiagHead"></div>');
            diagHead.append($('<div class="headLogo headLogoNice"></div>'));
            diagHead.append(TM.ShopDiag.createHeadInfo());
            return diagHead;
        },
        createHeadInfo : function(){
            var headInfo = $('<div class="headInfo"></div>');
            headInfo.append(TM.ShopDiag.createDiagScore());
            headInfo.append(TM.ShopDiag.createProgressBar());
            headInfo.append(TM.ShopDiag.createHeadBottom());
            return headInfo;
        },
        createDiagScore : function(){
            var diagScore = $('<div class="diagScore"></div>');
           /* diagScore.append("体检得分：");
            diagScore.append($('<span class="scoreNum">67.2</span>'));
            diagScore.append($('<span class="scoreComment">分    比较差</span>'));*/
            diagScore.append("(建议每天体检，随时了解店铺诊断效果~)");
            return diagScore;
        },
        createProgressBar : function(){
            var progressBar = $('<div class="progressBar" ></div>');
            progressBar.append($('<div class="progressLine"></div>'));
            return progressBar;
        },
        createHeadBottom : function(){
            var headBottom = $('<div class="headBottom"></div>');
            headBottom.append("发现了");
            headBottom.append($('<span class="problemNum percent">0</span>'));
            headBottom.append("个需要改进的问题~");
            return headBottom;
        },
        createDiagBody : function(data){
            var diagBody = $('<div class="shopDiagBody"></div>');
            diagBody.append(TM.ShopDiag.createDiagBad(data));
            diagBody.append(TM.ShopDiag.createDiagSoso(data));
            diagBody.append(TM.ShopDiag.createDiagNice(data));
            return diagBody;
        },
        createDiagBad : function(data){
            var badDiv = $('<div class="diagBad"></div>');
            badDiv.append(TM.ShopDiag.createDiagBadInfo(data));
            badDiv.append(TM.ShopDiag.createDiagBadDetail(data));
            return badDiv;
        },
        createDiagBadInfo : function(data){
            var badInf = $('<div class="diagBadInfo"></div>');
            badInf.append($('<div class="badImg"></div>'));
            var briefDesc = $('<div class="briefDesc"></div>');
            briefDesc.append($('<span class="Desc">急需解决</span>'));
            briefDesc.append($('<span class="badNum Desc">0</span>'));
            briefDesc.append($('<span class="Desc">项</span>'));
            badInf.append(briefDesc);
            return badInf;
        },
        createDiagBadDetail : function(data){
            var badDetail = $('<div class="badDetail"></div>');
            /*badDetail.append(TM.ShopDiag.createDelistDiagDiv(data));*/
            return badDetail;
        },
        createDiagSoso : function(data){
            var sosoDiv = $('<div class="diagSoso"></div>');
            sosoDiv.append(TM.ShopDiag.createDiagSosoInfo(data));
            sosoDiv.append(TM.ShopDiag.createDiagSosoDetail(data));
            return sosoDiv;
        },
        createDiagSosoInfo : function(data){
            var sosoInf = $('<div class="diagSosoInfo"></div>');
            sosoInf.append($('<div class="sosoImg"></div>'));
            var briefDesc = $('<div class="briefDesc"></div>');
            briefDesc.append($('<span class="Desc">待解决</span>'));
            briefDesc.append($('<span class="sosoNum Desc">0</span>'));
            briefDesc.append($('<span class="Desc">项</span>'));
            sosoInf.append(briefDesc);
            return sosoInf;
        },
        createDiagSosoDetail : function(data){
            var sosoDetail = $('<div class="sosoDetail"></div>');
            /*sosoDetail.append(TM.ShopDiag.createUVDiagDiv(data));
            sosoDetail.append(TM.ShopDiag.createConvertionRateDiagDiv(data));*/
            return sosoDetail;
        },
        createDiagNice : function(data){
            var niceDiv = $('<div class="diagNice"></div>');
            niceDiv.append(TM.ShopDiag.createDiagNiceInfo(data));
            niceDiv.append(TM.ShopDiag.createDiagNiceDetail(data));
            return niceDiv;
        },
        createDiagNiceInfo : function(data){
            var niceInf = $('<div class="diagNiceInfo"></div>');
            niceInf.append($('<div class="niceImg"></div>'));
            var briefDesc = $('<div class="briefDesc"></div>');
            briefDesc.append($('<span class="Desc">表现良好</span>'));
            briefDesc.append($('<span class="niceNum Desc">0</span>'));
            briefDesc.append($('<span class="Desc">项</span>'));
            niceInf.append(briefDesc);
            return niceInf;
        },
        createDiagNiceDetail : function(data){
            var niceDetail = $('<div class="niceDetail"></div>');
            /*niceDetail.append(TM.ShopDiag.createDecorateDiagDiv(data));
            niceDetail.append(TM.ShopDiag.createRefundDiagDiv(data));
            niceDetail.append(TM.ShopDiag.createRebuyDiagDiv(data));
            niceDetail.append(TM.ShopDiag.createTitleScoreDiv(data));
            niceDetail.append(TM.ShopDiag.createBurstDiagDiv(data));
            niceDetail.append(TM.ShopDiag.createPotentialGoodItemsDiag(data));
            niceDetail.append(TM.ShopDiag.createWindowDiagDiv(data));
            niceDetail.append(TM.ShopDiag.createTradeCountDiv(data));*/
            return niceDetail;
        },
        createTradeCountDiv : function(data){
            var tradeCountDiv = $('<div class="tradeCountDiv diagDiv"></div>');
            var tradeCountBrief = $('<div class="diagBrief"></div>');
            tradeCountBrief.append("经诊断发现，您的店铺最近30天的订单量为");
            tradeCountBrief.append($('<span class="tradeCountNum percent">'+data.tradeCount+'</span>'));
            if(data.tradeCount  > 0) {
                tradeCountBrief.append(",低于");
                tradeCountBrief.append($('<span class="tradeCountLessPercent percent">'+100*(data.tradeCount%40)/40+'%</span>'));
                tradeCountBrief.append("的卖家，加把劲哦~");
            } else {
                tradeCountBrief.append(",低于");
                tradeCountBrief.append($('<span class="tradeCountLessPercent percent">100%</span>'));
                tradeCountBrief.append("的卖家，加把劲哦~");
            }
            tradeCountDiv.append(tradeCountBrief);
            tradeCountDiv.append($('<div class="suggest"><span class="blankBorder">销量提升</span>(内功是基础，推广时王道~)</div>'));
            return tradeCountDiv;
        },
        createWindowDiagDiv : function(data){
            var windowsDiv = $('<div class="windowsDiv diagDiv"></div>');
            var windowsBrief = $('<div class="diagBrief"></div>');
            windowsBrief.append("经诊断发现，您的店铺橱窗剩余数为");
            windowsBrief.append($('<span class="windowsRemainNum percent">'+data.remainWindowCount+'</span>'));
            windowsBrief.append(",橱窗利用率为");
            windowsBrief.append($('<span class="windowsPercent percent">'+data.windowUsage+'%</span>'));
            if(data.windowUsage == 0 ) {
                windowsBrief.append("，低于");
                windowsBrief.append($('<span class="windowsLessPercent percent">100%</span>'));
                windowsBrief.append("的卖家,弱爆啦！");
            }
           else if(data.windowUsage < 100 ) {
                windowsBrief.append("，低于");
                windowsBrief.append($('<span class="windowsLessPercent percent">'+(199-data.windowUsage)/2+'%</span>'));
                windowsBrief.append("的卖家！");
            }else {
                windowsBrief.append("，表现良好");
            }
            windowsDiv.append(windowsBrief);
            windowsDiv.append($('<div class="suggest"><span class="blankBorder">宝贝橱窗优化</span>(橱窗尽量展示销量高，收藏大和快下架的宝贝)</div>'));
            return windowsDiv;
        },
        createTitleScoreDiv : function(data){
            var titleScoreDiv = $('<div class="titleScoreDiv diagDiv"></div>');
            var titleScoreBrief = $('<div class="diagBrief"></div>');
            titleScoreBrief.append("经诊断发现，您的店铺宝贝标题平均得分为：");
            titleScoreBrief.append($('<span class="titleScore percent">'+data.titleScore+'</span>'));
            if(data.titleScore == 100) {
                titleScoreBrief.append("，打败了全国卖家，继续努力哦~");
            } else if(data.titleScore == 0) {
                titleScoreBrief.append("，被全国卖家爆出翔了啦~");
            }else{
                titleScoreBrief.append(",低于");
                titleScoreBrief.append($('<span class="titleScoreLessPercent percent">'+(118-data.titleScore)/2+'%</span>'));
                titleScoreBrief.append("的卖家！");
            }
            if(data.badTitleCount > 0) {
                titleScoreBrief.append("其中有");
                titleScoreBrief.append($('<span class="badYitleNum percent">'+data.badTitleCount+'</span>'));
                titleScoreBrief.append("个宝贝标题质量");
                titleScoreBrief.append($('<span class="percent">不及格</span>'));
                titleScoreBrief.append("，严重影响你的店铺搜索排名");
            }
            titleScoreDiv.append(titleScoreBrief);
            titleScoreDiv.append($('<div class="suggest"><span class="blankBorder">宝贝标题优化</span>(要根据自己宝贝尽量选取有特色的关键字，不要以为选择高频关键字)</div>'));
            return titleScoreDiv;
        },
        createBurstDiagDiv : function(data){
            var burstItemsDiv = $('<div class="burstDiagDiv diagDiv"></div>');
            var burstItemsBrief = $('<div class="diagBrief"></div>');
            burstItemsBrief.append("经诊断发现，您的店铺爆款宝贝数为：");
            burstItemsBrief.append($('<span class="burstItemsNum percent">'+data.goodItemCount+'</span>'));
            if(data.goodItemCount > 0) {
                var lookup = $('<span class="scoreNum" style="cursor: pointer">查看宝贝</span>');
                burstItemsBrief.append(lookup);
                lookup.click(function(){
                    var itemShow = $('.itemShow');
                    if(itemShow.length == 0) {
                        itemShow = $('<div class="itemShow"></div>');
                    }  else {
                        itemShow.empty();
                    }
                    itemShow.css("top",$(this).offset().top+"px");
                    itemShow.css("left",$(this).offset().left+$(this).width()+"px");
                    var itemsDiv = $('<div class="itemsDiv"></div>');
                    var itemsTable = $('<table class="itemsTable"></table>');
                    itemsTable.append($('<thead><tr><td class="itemImg">宝贝图片</td><td class="itemTitle">宝贝标题</td><td class="itemSalesCount">销量</td></tr></thead>'));
                    var tbody = $('<tbody></tbody>');
                    for (var i = 0; i < data.burstItems.length; i++) {
                        var url = "http://item.taobao.com/item.htm?id=" + data.burstItems[i].id;
                        tbody.append($('<tr><td class="itemImg"><a target="_blank" href="'+url+'"><img style="width: 40px;height: 40px;" src="'+data.burstItems[i].picPath+'"/></a></td><td class="itemTitle">'+data.burstItems[i].title+'</td><td class="itemSalesCount">'+data.burstItems[i].tradeNum+'</td></tr>'));
                    }
                    tbody.appendTo(itemsTable);
                    itemsTable.appendTo(itemsDiv);
                    itemsDiv.appendTo(itemShow);
                    var exitBtn = $('<div class="exitBtn scoreNum" style="font-size: 20px;color:#990033;">关闭</div>');
                    itemShow.append(exitBtn);
                    exitBtn.click(function(){
                        itemShow.fadeOut(1000);
                    });
                    itemShow.appendTo($('body'));
                    itemShow.fadeIn(1000);
                });
                /*potentialBurstItemsBrief.append("分别为：");
                 for(var i = 0; i < data.potentionItems.length; i++) {
                 var potentialItem = $('<a style="display: inline-block;" target="_blank"><div class="potentialItemLink"><img class="potentialItemImg"></div></a>');
                 potentialItem.find("img").attr("src",data.potentionItems[i].picPath);
                 var url = "http://item.taobao.com/item.htm?id=" + data.potentionItems[i].id;
                 potentialItem.attr("href",url);
                 potentialBurstItemsBrief.append(potentialItem);
                 }*/
            }
            if(data.goodItemCount == 0) {
                burstItemsBrief.append("低于");
                burstItemsBrief.append($('<span class="burstItemsLessPercent percent">100%</span>'));
                burstItemsBrief.append("的卖家,弱爆啦！");
            }else {
                burstItemsBrief.append("低于");
                burstItemsBrief.append($('<span class="burstItemsLessPercent percent">'+data.id%(data.goodItemCount+97)+'%</span>'));
                burstItemsBrief.append("的卖家,继续努力啊！");
            }
            burstItemsDiv.append(burstItemsBrief);
            burstItemsDiv.append($('<div class="suggest"><span class="blankBorder">爆款打造</span>(根据宝贝质量，需求度，性价比等选择打造爆款)</div>'));
            return burstItemsDiv;
        },
        createPotentialGoodItemsDiag : function(data){
            var potentialBurstItemsDiv = $('<div class="potentialBurstDiagDiv diagDiv"></div>');
            var potentialBurstItemsBrief = $('<div class="diagBrief"></div>');
            potentialBurstItemsBrief.append("经诊断发现，您的店铺存在");
            potentialBurstItemsBrief.append($('<span class="potentialBurstItemsNum percent">'+data.potentialGoodItemCount+'</span>'));
            potentialBurstItemsBrief.append("款潜在爆款宝贝！");
            if(data.potentialGoodItemCount > 0) {
                var lookup = $('<span class="scoreNum" style="cursor: pointer">查看宝贝</span>');
                potentialBurstItemsBrief.append(lookup);
                lookup.click(function(){
                    var itemShow = $('.itemShow');
                    if(itemShow.length == 0) {
                        itemShow = $('<div class="itemShow"></div>');
                    }  else {
                        itemShow.empty();
                    }
                    itemShow.css("top",$(this).offset().top+"px");
                    itemShow.css("left",$(this).offset().left+$(this).width()+"px");
                    var itemsDiv = $('<div class="itemsDiv"></div>');
                    var itemsTable = $('<table class="itemsTable"></table>');
                    itemsTable.append($('<thead><tr><td class="itemImg">宝贝图片</td><td class="itemTitle">宝贝标题</td><td class="itemSalesCount">销量</td></tr></thead>'));
                    var tbody = $('<tbody></tbody>');
                    for (var i = 0; i < data.potentionItems.length; i++) {
                        var url = "http://item.taobao.com/item.htm?id=" + data.potentionItems[i].id;
                        tbody.append($('<tr><td class="itemImg"><a target="_blank" href="'+url+'"><img style="width: 40px;height: 40px;" src="'+data.potentionItems[i].picPath+'"/></a></td><td class="itemTitle">'+data.potentionItems[i].title+'</td><td class="itemSalesCount">'+data.potentionItems[i].tradeNum+'</td></tr>'));                    }
                    tbody.appendTo(itemsTable);
                    itemsTable.appendTo(itemsDiv);
                    itemsDiv.appendTo(itemShow);
                    var exitBtn = $('<div class="exitBtn scoreNum" style="font-size: 20px;color:#FF9D6F">退出</div>');
                    itemShow.append(exitBtn);
                    exitBtn.click(function(){
                        itemShow.fadeOut(1000);
                    });
                    itemShow.appendTo($('body'));
                    itemShow.fadeIn(1000);
                });
                /*potentialBurstItemsBrief.append("分别为：");
                for(var i = 0; i < data.potentionItems.length; i++) {
                    var potentialItem = $('<a style="display: inline-block;" target="_blank"><div class="potentialItemLink"><img class="potentialItemImg"></div></a>');
                    potentialItem.find("img").attr("src",data.potentionItems[i].picPath);
                    var url = "http://item.taobao.com/item.htm?id=" + data.potentionItems[i].id;
                    potentialItem.attr("href",url);
                    potentialBurstItemsBrief.append(potentialItem);
                }*/
            }

            if(data.potentialGoodItemCount == 0) {
                potentialBurstItemsBrief.append("低于");
                potentialBurstItemsBrief.append($('<span class="potentialBurstItemsLessPercent percent">100%</span>'));
                potentialBurstItemsBrief.append("的卖家,弱爆啦！");
            }else {
                potentialBurstItemsBrief.append("低于");
                potentialBurstItemsBrief.append($('<span class="potentialBurstItemsLessPercent percent">'+data.id%(data.potentialGoodItemCount+94)+'%</span>'));
                potentialBurstItemsBrief.append("的卖家,继续努力啊！");
            }
            potentialBurstItemsDiv.append(potentialBurstItemsBrief);
            potentialBurstItemsDiv.append($('<div class="suggest"><span class="blankBorder">潜在爆款打造</span>(根据销量，收藏等数据选取适当的爆款，并竭力推广)</div>'));
            return potentialBurstItemsDiv;
        },
        createDelistDiagDiv : function(data){
            var delistDiv = $('<div class="delistDiv diagDiv"></div>');
            var delistBrief = $('<div class="diagBrief"></div>');
            if(data.weekDistributed == null){
                delistBrief.append("没有发现每日上下架宝贝信息~");
            } else {
                var weekDist = data.weekDistributed.split(",");
                delistBrief.append("经诊断发现，您的店铺宝贝上下架时间分布如下：");
                for(var i = 0; i < 7; i++) {
                    switch(i){
                        case  0:
                            delistBrief.append("周一：")
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                        case  1:
                            delistBrief.append("周二：");
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                        case  2:
                            delistBrief.append("周三：");
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                        case  3:
                            delistBrief.append("周四：");
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                        case  4:
                            delistBrief.append("周五：");
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                        case  5:
                            delistBrief.append("周六：");
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                        case  6:
                            delistBrief.append("周日：");
                            delistBrief.append($('<span class="delistStatus percent" >'+weekDist[i]+'</span>'));break;
                    }
                }
                if(data.inBadTimeCount > 0) {
                    delistBrief.append(",其中有");
                    delistBrief.append($('<span class="delistBadNum percent">'+data.inBadTimeCount+'</span>'));
                    delistBrief.append("个宝贝上下架时间分布");
                    delistBrief.append($('<span class="delistStatus percent" >不合理</span>'));
                    delistBrief.append("，严重影响你的店铺搜索排名");
                } else {
                    delistBrief.append("，宝贝上下架时间分布比较合理，继续加油哦~");
                }
            }



            delistDiv.append(delistBrief);
            delistDiv.append($('<div class="suggest"><span class="blankBorder">上架时间优化</span>(宝贝应尽量选择在流量高峰期上架)</div>'));
            return delistDiv;
        },
        createUVDiagDiv : function(data){
            var UVDiv = $('<div class="UVDiv diagDiv"></div>');
            var UVBrief = $('<div class="diagBrief"></div>');
            UVBrief.append("经诊断发现，您的店铺近7天总访客数为");
            UVBrief.append($('<span class="uv percent">234</span>'));
            UVBrief.append("比");
            UVBrief.append($('<span class="uvLessPercent percent">34%</span>'));
            UVBrief.append("的卖家少！");
            UVDiv.append(UVBrief);
            UVDiv.append($('<div class="suggest"><span class="blankBorder">提高标题优化技巧</span>（词的位置），<span class="blankBorder">提高选词技巧</span>（分析词的相关性，词的搜索量，词的优化难度），<span class="blankBorder">提高微博推广能力</span>（名人微博效果更好）</div>'));
            return UVDiv;
        },
        createConvertionRateDiagDiv : function(data){
            var convertionRateDiv = $('<div class="convertionRateDiv diagDiv"></div>');
            var convertionRateBrief = $('<div class="diagBrief"></div>');
            convertionRateBrief.append("经诊断发现，您的店铺近7天转化率为");
            convertionRateBrief.append($('<span class="convertionRate percent">'+data.conversionRate/100+'%</span>'));
            if(data.conversionRate == 0) {
                convertionRateBrief.append("低于");
                convertionRateBrief.append($('<span class="convertionRateLessPercent percent">100%</span>'));
                convertionRateBrief.append("的卖家,弱爆啦！");
            } else {
                convertionRateBrief.append("低于");
                convertionRateBrief.append($('<span class="convertionRateLessPercent percent">'+(137-(data.conversionRate/100))/2+'%</span>'));
                convertionRateBrief.append("的卖家,要加油了！");
            }

            convertionRateDiv.append(convertionRateBrief);
            convertionRateDiv.append($('<div class="suggest"><span class="blankBorder">提高选款水平</span>（要集中推爆款），<span class="blankBorder">提高推广渠道挖掘能力</span>（最好是免费的）</div>'));
            return convertionRateDiv;
        },
        createDecorateDiagDiv : function(data){
            var decorateDiv = $('<div class="decorateDiv diagDiv"></div>');
            var decorateBrief = $('<div class="diagBrief"></div>');
            decorateBrief.append("经诊断发现，您的店铺近7天只有");
            decorateBrief.append($('<span class="decorativePercent percent">70.3%</span>'));
            decorateBrief.append("的人看过您的宝贝，有");
            decorateBrief.append($('<span class="decorateWaste percent">12345</span>'));
            decorateBrief.append("个流量被浪费！");
            decorateDiv.append(decorateBrief);
            decorateDiv.append($('<div class="suggest"><span class="blankBorder">提高店铺装修水平</span>（控制布局、文案、图片设计，引导买家点击）</div>'));
            return decorateDiv;
        },
        createRefundDiagDiv : function(data){
            var refundDiv = $('<div class="refundDiv diagDiv"></div>');
            var refundBrief = $('<div class="diagBrief"></div>');
            refundBrief.append("经诊断发现，您的店铺近7天退款率为");
            refundBrief.append($('<span class="refundPercent percent">32.5%</span>'));
            refundBrief.append(",比");
            refundBrief.append($('<span class="refundLessPercent percent">38%</span>'));
            refundBrief.append("的卖家高，有被处罚的危险！");
            refundDiv.append(refundBrief);
            refundDiv.append($('<div class="suggest"><span class="blankBorder">提高产品质量</span>（质量为王，服务为皇），<span class="blankBorder">提高选款能力</span>（对于提高好评很有帮助）</div>'));
            return refundDiv;
        },
        createRebuyDiagDiv : function(data){
            var rebuyDiv = $('<div class="rebuyDiv diagDiv"></div>');
            var rebuyBrief = $('<div class="diagBrief"></div>');
            rebuyBrief.append("经诊断发现，您的店铺近7天只有");
            rebuyBrief.append($('<span class="rebuyPercent percent">1.2%</span>'));
            rebuyBrief.append("的人买了一次之后，会再来买第二次，相当于只有");
            rebuyBrief.append($('<span class="rebuyNum percent">433</span>'));
            rebuyBrief.append("个回头客，比");
            rebuyBrief.append($('<span class="rebuyLessPercent percent">38%</span>'));
            rebuyBrief.append("的卖家低！");
            rebuyDiv.append(rebuyBrief);
            rebuyDiv.append($('<div class="suggest"><span class="blankBorder">提高产品质量</span>（质量是二次购买的根本），<span class="blankBorder">提高选款水平</span>（好评款，潜力款，从根本上规避不满），<span class="blankBorder">提高会员营销技巧</span>（保持关系，和买家谈恋爱）</div>'));
            return rebuyDiv;
        }
    },TM.ShopDiag);
})(jQuery, window));
