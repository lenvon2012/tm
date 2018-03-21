
var vgouShow = vgouShow || {};
vgouShow.show = function(container){
    $.get('/Application/randomShow', function(data){
        var ulObj = $('<ul class="items"></ul>');
        if(data.length > 0) {
            $(data).each(function(i,item){
                var href = "http://item.taobao.com/item.htm?id=" + item.numIid;
                ulObj.append($('<li><a target="_blank" href="'+href+'" title="'+item.title+'"><img style="width: 610px;height: 280px;" src="'+item.picPath+'"/></a></li>'));
            });
        }

        container.append(ulObj);
        container.slideBox();
    });
}
