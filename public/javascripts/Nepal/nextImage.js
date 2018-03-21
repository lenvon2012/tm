function nextImage(){
    if(current < total_images){      $links.eq(current+1).trigger('click');  } } function prevImage(){   if(current > 0){
    $links.eq(current-1).trigger('click');
}
}