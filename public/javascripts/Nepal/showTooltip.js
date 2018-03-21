function showTooltip() {
    var $link = $(this),
         idx = $link.index(),
        linkOuterWidth = $link.outerWidth(),
        left = parseFloat(idx*linkOuterWidth)-$tooltip.width()/2+linkOuterWidth/ 2,
        $thumb = $link.find('a').attr('rel'),
        imageLeft;

    if(currentHovered!=idx){
        if(currentHovered!=-1){
            if(currentHovered<idx){
                imageLeft=75;

            }
            else{
                imageLeft=-75;
            }
        }
        currentHovered=idx;

        var $newImage = $('<img alt="">').css('left','0px').attr('src',$thumb);
        if($ps_preview_wrapper.children().length>1)
            $ps_preview_wrapper.children(':last').remove();

        $ps_preview_wrapper.prepend($newImage);
        var $tooltip_imgs       = $ps_preview_wrapper.children(),
            tooltip_imgs_count  = $tooltip_imgs.length;

        //if theres 2 images on the tooltip
        //animate the current one out, and the new one in
        if(tooltip_imgs_count > 1){
            $tooltip_imgs.eq(tooltip_imgs_count-1)
                .stop()
                .animate({
                    left:-imageLeft+'px'
                },150,function(){
                    //remove the old one
                    $(this).remove();
                });
            $tooltip_imgs.eq(0)
                .css('left',imageLeft + 'px')
                .stop()
                .animate({
                    left:'0px'
                },150);
        }
    }
    //if we are not using a "browser", we just show the tooltip,

    }

