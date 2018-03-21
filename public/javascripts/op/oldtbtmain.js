// set #right-award absolute position
$('#right-award').css('right', (($(window).width() - 1000) / 2 - 120) + 'px');
$(window).resize(function () {
    $('#right-award').css('right', (($(window).width() - 1000) / 2 - 120) + 'px');
});
$('#right-award').click(function () {
    // 这是原始3元弹窗，图片形式
    /*var link = "http://fuwu.taobao.com/ser/plan/planSubDetail.htm?planId=12651&isHidden=true&cycle=1&itemIds=285831";
     var left = ($(document).width() - 500)/2;
     var top = 130;
     var html = '<table class="three-yuan-xufei-img-dialog" style="position: absolute;z-index: 19000;width: 500px;height: 330px;background: url(http://img04.taobaocdn.com/imgextra/i4/1132351118/T2cB4hXrlaXXXXXXXX_!!1132351118.gif)"><tbody>' +
     '<tr style="height: 40px;"><td style="width: 460px;"></td><td style="width: 40px;"><span class="inlineblock close-three-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;z-index: 19100;"></td></tr>' +
     '<tr style="height: 290px;"><td style="width: 460px;vertical-align: top;"><a class="inlineblock" style="position: absolute;width: 460px;height: 290px;" target="_blank" href="'+link+'"></a></td><td style="width: 40px;"></td></tr>'+
     '</tbody></table>';
     $('.three-yuan-xufei-img-dialog').remove();
     var three_yuan = $(html);
     if($.browser.msie) {
     three_yuan.find('.close-five-yuan-dialog').css('filter',"alpha(opacity=10)");
     three_yuan.find('.close-five-yuan-dialog').css('background-color',"#D53A04");
     }
     three_yuan.css('top',top+"px");
     three_yuan.css('left',left+"px");
     three_yuan.find('.close-three-yuan-dialog').click(function(){
     three_yuan.remove();
     $('body').unmask();
     });
     three_yuan.appendTo($('body'));
     $('body').mask();*/

    // 现在使用新版的3元弹窗，可选套餐
    TM.loadMeal();
});
$('.three-yuan-tanchuang-left-li').click(function () {
    TM.loadMeal();
});


TM.loadMeal = function () {
    //http://fuwu.taobao.com/ser/plan/planSubDetail.htm?planId=12651&isHidden=true&cycle=1&itemIds=285831
    var meal = $('#meal3');
    var currDuration = null;
    var payment = meal.find('.payment');
    var recomputePrice = function () {
        var price = meal.find('.durations').find(".mealselected").attr('value') * meal.find('.mealitems .mealselected').length * 3;
        payment.html(price);
    }

    meal.find('.durations').find('.duration').click(function () {
        var clicked = $(this);
        $('.durations').find('.mealselected').removeClass('mealselected');
        clicked.addClass('mealselected');
        recomputePrice();
    });

    meal.find('.mealitems').find('.mealitem').click(function () {
        var clicked = $(this);
        if (clicked.hasClass('mealselected')) {
            clicked.removeClass('mealselected');
        } else {
            clicked.addClass('mealselected');
        }
        recomputePrice();
    });
    meal.find('.btn-ordernow').click(function () {
        var itemIds = [];
        meal.find('.mealitems .mealselected').each(function (i, elem) {
            itemIds.push($(this).attr('itemId'));
        });
        if (itemIds.length == 0) {
            alert('亲,请至少选择一个服务哟');
            return;
        }
        var cycle = meal.find('.durations').find(".mealselected").attr('value');
        var href = "http://fuwu.taobao.com/ser/plan/planSubDetail.htm?planId=12651&isHidden=true&cycle="
            + cycle + "&itemIds=" + itemIds.join("%2C");
        window.open(href);
    });


    meal.dialog({
        modal:true,
        bgiframe:true,
        height:480,
        width:980,
        title:"套餐订购",
        autoOpen:false,
        resizable:false,
        zIndex:6003,
        buttons:{'关闭':function () {
            $(this).dialog('close');
        }}
    });
    meal.dialog('open');
}