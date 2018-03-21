

$(function(){
    //当前页面默认加载oneTitleRestore.html

            $("#backA1").click(function(){
                $("#titleRestoreMain").load("/newAutoTitle/goOneTitle");
                $(".xdsoft_datetimepicker").remove()//修复datetimepicker的bug

            });
            $("#backA2").click(function(){

                $("#titleRestoreMain").load("/newAutoTitle/goAllTitle");
                $(".xdsoft_datetimepicker").remove()//修复datetimepicker的bug


            });
            $("#backA3").click(function(){
                $("#titleRestoreMain").load("/newAutoTitle/goTimeLineTitle");



            });




});