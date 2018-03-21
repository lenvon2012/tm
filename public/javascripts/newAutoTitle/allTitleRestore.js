//这个文件用来往页面中写入标题还原中心的table内容
$(function(){

    //改变菜单栏背景颜色
    $(".header-nav").removeClass("current");
    $("#nav3").addClass("current");

    //根据json文件的路径，返回一个ObjectArray


    var holder=$("#holder");
    var alertText=$(".alertText");
    var me=$("#infoTable");

    holder.tmpage({
        currPage: 1,
        pageSize: 10,
        pageCount:1,
        ajax:{
            on: true,
            dataType: 'json',
            url: "/Titles/batchOpLogs",
            callback:function(data){

                if(data.res.length>0){
                    alertText.html("提示：一共有  <span style='color: red;'>"+data.count+"</span>  条符合要求的数据。")
                }else{
                    alertText.html("<span style='color: red;'>提示：亲，暂时没有符合要求的数据哦。</span>");
                    me.html("");
                    return;
                }

                for (i = 0; i < data.res.length; i++) {
                    data.res[i].ts = new Date(data.res[i].ts).formatYMDHMS();//格式化日期时间
                }



                var desk=$("#desk");
                desk.load("/public/tmpl/allTitleRecord.html",function(){
                    var meHtml=desk.find("#record").tmpl(data);
                    desk.html("")
                    me.html("")
                    me.append(meHtml);

                    $("#noteTbody>tr:even").css("background-color","#e3e3e3");//隔行变色


                    /* initiate plugin */
//                     $(".holder").jPages({
//                         containerID: "noteTbody",
//                         next:"下一页",
//                         previous: "上一页",
//                         perPage:15,
//                         fallback:20
//                     });





                    //还原数据功能
                    me.find('.recoverBtn').click(function(){
                        var oThis = $(this);//得到当前对象
                        var targetId= oThis.attr("targetId");
                        if(confirm("点击确定将还原该宝贝的标题")) {

                            $.ajax({
                                url:"/Titles/recoverBatch",
                                data:{"id":targetId},
                                type:"post",
                                datatype:"json",
                                async:true,//异步
                                success: function (data) {

                                        alertText.html("<span style='color: #008000;'>提示：亲，"+data.successNum+"个还原成功"+data.failNum+"个还原失败。</span>");

                                },
                                error: function () {
                                    alertText.html("<span style='color: red;'>提示：哎呀出错了，请尝试刷新页面，如果问题依然存在，请联系我们</span>");

                                }
                            })
                        }
                    });


                    //查看详情功能
                    $('.detailBtn').click(function(){
                        //所有参数都用默认值,并且是遮住整个页面的

                        var oThis = $(this);//得到当前对象
                        var targetId= oThis.attr("targetId");
                        var succn=oThis.attr("succn");

                        //如果还原的宝贝数量为0
                        if(succn<1){
                            alertText.html("<span style='color: red;'>提示：亲，优化成功的宝贝数量为0，无法查看详情哦</span>");
                            return;
                        }

                        $("body").mask("<div class='mymask'></div>");
                        var desk=$("#desk");
                        desk.load("/public/tmpl/allTitleRecordInfo.html",function(){


                            $.getJSON("/Titles/batchOpLogDetail?id="+targetId,function(data){

                                var dataArr={"dataArr":data};








                                if(data==null||data==undefined||data==""){
                                    alertText.html("<span style='color: red;'>提示：没有成功还原的数据哦亲</span>");
                                    return;

                                }


//                                if(data<1){
//                                    alertText.html("<span style='color: red;'>提示：没有成功还原的数据哦亲</span>");
//                                    return;
//
//                                }




                                var mytmpl= desk.find("#maskTable").tmpl(dataArr);


                                desk.html("");
                                $(".mymask").append(mytmpl);//添加到页面中


                                $(".maskTbody>tr:even").css("background-color","#e3e3e3");//隔行变色

                                $(".titleClose").click(function(){
                                    $("body").unmask();//关闭遮罩层

                                });


                            })

                        })

                    });








                })

            }
        }
    })






});