var loaded = 0;
$links.each(function(i){
    var $link = $(this);
    $link.find('a').preload({
       onComplete:function(){
           ++loaded;
           if(loaded == total_images){
               $loader.hide();
               diagHolder.show();
               $links.bind('mouseenter',showTooltip)
                   .bind('mouseleave',hideTooltip)
                   .bind('click',showImage);
               $ps_next.bind('click',nextImage);
               $ps_prev.bind('click',prevImage);
           }
       }
    });
});