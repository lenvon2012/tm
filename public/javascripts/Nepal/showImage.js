function showImage(e){
    var $link               = $(this),
        idx                 = $link.index(),
        $image              = $link.find('a').attr('href'),
        $currentImage       = diagHolder.html(preparedContent.find('img')),
        currentImageWidth   = $currentImage.width();

    //if we click the current one return
    if(current == idx) return false;

    //add class selected to the current page / dot
    $links.eq(current).removeClass('selected');
    $link.addClass('selected');

    //the new image element
    var $newImage = $('<img alt="">').css('left',currentImageWidth + 'px')
        .attr('src',$image);

    //if the wrapper has more than one image, remove oldest
    if($ps_image_wrapper.children().length > 1)
        $ps_image_wrapper.children(':last').remove();

    //prepend the new image
    $ps_image_wrapper.prepend($newImage);

    //the new image width
    //this will be the new width of the ps_image_wrapper
    var newImageWidth   = $newImage.width();

    //check animation direction
    if(current > idx){
        $newImage.css('left',-newImageWidth + 'px');
        currentImageWidth = -newImageWidth;
    }
    current = idx;
    //animate the new width of the ps_image_wrapper
    //(same like new image width)
    $ps_image_wrapper.stop().animate({
        width   : newImageWidth + 'px'
    },350);
    $currentImage.stop().animate({
        left    : -currentImageWidth + 'px'
    },350);

    e.preventDefault();
}