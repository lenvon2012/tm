


    $(document).ready(function(){

        $('.funimg').hover(function(){
            var preparedContent = $(this).find('.preparecontent').clone();
            preparedContent.show();
            $('#funcDesc').empty();
            $('#funcDesc').append(preparedContent);
            var diagHolder = $('.marquee_container');
                 diagHolder.dialog({
                    autoOpen: false,
                    height:620,
                    width:800,
                    modal:true

                 }) ;
            preparedContent.find('.view-detail').click(function(){
               diagHolder.dialog("open");
                return false;
            });


            $('.marquee_panels .funimg').each(function(index){
                $('.marquee_nav').append('<a class="marquee_nav_item"></a>');
            });

            var images = preparedContent.find('img.ps_image_wrapper');
            if(images.length <= 1){
                diagHolder.html( preparedContent.find('.ps_image_wrapper')) ;
            }else{
                var photoWidth = $('.marquee_container').width();
                //console.info(images);
                var htmls = [];
                htmls.push('<div class="imgContainer">');
                images.each(function(index, image){
                    htmls.push('<img src="'+$(image).attr('src')+'" />');
                });

                htmls.push('</div>')


                diagHolder.html($(htmls.join('')));

                $('.marquee_nav a.marquee_nav_item').click(function(){

                    $('.marquee_nav a.marquee_nav_item').removeClass('selected');
                    $(this).addClass('selected');

                    var navClicked = $(this).index();
                    var marqueeWidth = $('.marquee_container').width();
                    var distanceToMove = marqueeWidth * (-1);
                    var newPhotoPosition = navClicked * distanceToMove + 'px';

                    diagHolder.animate({left:newPhotoPosition}, 1000);

                })  ;

                $('.marquee_panels img').imgpreload(function(){
                    initializeMarquee();
                });
            }

          //  diagHolder.html( preparedContent.find('.ps_image_wrapper')) ;
//            .each(function(index){
//                var photoWidth = $('.marquee_container').width();
//                var photoPosition = index * photoWidth;
//                preparedContent.find('.ps_image_wrapper'), style="left: '+photoPosition+'", src="'+$(this).attr('src')+'"
////                diagHolder.html( );
//            });

        });

    });
    function initializeMarquee(){
        $('.marquee_nav a.marquee_nav_item:first').addClass('selected');
        $('.marquee_container').fadeIn(1500);
    }



