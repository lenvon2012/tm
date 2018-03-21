//这个文件用来往页面中写入标题还原中心的table内容
var TM=TM||{};


$(function(){
     //改变菜单栏背景颜色
    $(".header-nav").removeClass("current");
    $("#nav3").addClass("current");
    //根据json文件的路径，返回一个ObjectArray
    var me=$(".infoTable");
    var jsonUrl="/Titles/getItemsWithDiagResult";
    var alertText=$(".alertText");

    //做搜索框的提示功能
    var searchText=$("#searchText");
    searchText.click(function () {
        if ($.trim($(this).val()) == "输入关键词搜索商品") {
            searchText.val("")
        }
    }).blur(function () {
        if ($.trim($(this).val()) == "") {
            searchText.val("输入关键词搜索商品")
        }
    });

    //搜索框功能
    $(".startBtn").click(function(){
           var searchTextVal= $.trim($("#searchText").val());


        //如果参数不合法就不去搜索而直接结束进程
        if(searchTextVal==""||searchTextVal=="输入关键词搜索商品"||searchTextVal==null){
            return;
        }

        var params = {
            s: searchTextVal,
            status:0,
            catId:null,
            sort:1,
            lowBegin:0,
            ps:10,
            topEnd:100,
            pn:1
        };



//        $.ajax({
//            data:params,
//            url:"/Titles/getItemsWithDiagResult",
//            async:true,
//            datatype:"json",
//            on:true,
//            success:function(data){
//                      console.log(data)
//                      console.log("哈哈哈我拿到数据了ye")
//            },
//            error:function(){
//                alert("哎呀出错了")
//            }
//        })

        alertText.html("亲请稍后，系统正在玩命加载数据...")
        $("#holder").tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax:{
                param :params,
                url:"/Titles/getItemsWithDiagResult",
                dataType:"json",
                type:"get",
                async:true,//异步
                on: true,
                callback:function(data){
                    var objData=eval(data);


                    if(objData.res.length>0){
                        alertText.html("提示：一共有  <span style='color: red;'>"+objData.count+"</span>  条符合要求的数据。")
                    }else{
                        alertText.html("<span style='color: red;'>提示：亲，暂时没有符合要求的数据哦。</span>");
                        me.html("");

                        return;
                    }

//                for(i=0;i<objData.titleInfo.length;i++){
//                    objData.titleInfo[i].numIid=new Date(objData.titleInfo[i].numIid).formatYMDHMS();//格式化日期时间
//
//
//                }


                    var desk=$("#desk");

                    desk.load("/public/tmpl/oneTitleRecord.html",function(){
                        var meHtml=desk.find("#record").tmpl(objData);
                        desk.html("")
                        me.html("")
                        me.append(meHtml);

                        $("#noteTbody>tr:even").css("background-color","#efefef");//隔行变色


                        /* initiate plugin */
//                     $(".holder").jPages({
//                         containerID: "noteTbody",
//                         next:"下一页",
//                         previous: "上一页",
//                         perPage:15,
//                         fallback:20
//                     });








                                //查看详情功能
                                $('.recoverInfo').click(function(){
                                    //所有参数都用默认值,并且是遮住整个页面的

                                    var oThis = $(this);//得到当前对象
                                    var targetId= oThis.attr("targetId");
                                    //判断是否有还原记录，如果没有就不弹出层，直接提示用户没有数据
//                         var succn=oThis.attr("succn");
//
//                         //如果还原的宝贝数量为0
//                         if(succn<1){
//                             alertText.html("<span style='color: red;'>提示：亲，优化成功的宝贝数量为0，无法查看详情哦</span>");
//                             return;
//                         }

                                    var desk=$("#desk");
                                    desk.load("/public/tmpl/oneTitleRecordInfo.html",function(){



                                        $("body").mask("<div class='mymask'></div>");



                                        $(".renameHistoryPagging").tmpage({
                                            currPage: 1,
                                            pageSize: 10,
                                            pageCount: 1,
                                            ajax: {
                                                on: true,
                                                dataType: 'json',
                                                url: "/Titles/renameHistory?numIid=" + targetId,
                                                callback: function (data) {

                                                    if (data == null || data == undefined || data == "") {
                                                        alertText.html("<span style='color: red;'>提示：没有成功还原的数据哦亲</span>");
                                                        return;

                                                    }


                                                    //格式化日期时间
                                                    $.each(data.res, function (index, item) {
                                                        item.updated = new Date(item.updated).formatYMDHMS();
                                                    });



//                                if(data<1){
//                                    alertText.html("<span style='color: red;'>提示：没有成功还原的数据哦亲</span>");
//                                    return;
//
//                                }


                                                    var mytmpl = desk.find("#maskTable").tmpl(data);


                                                    desk.html("");
                                                    $(".mymask").append(mytmpl);//添加到页面中


                                                    $(".maskTbody>tr:even").css("background-color", "#e3e3e3");//隔行变色

                                                    $(".titleClose").click(function () {
                                                        $("body").unmask();//关闭遮罩层

                                                    });


                                                    //还原数据功能
                                                    $('.recoverBtn').click(function () {
                                                        var oThis = $(this);//得到当前对象
                                                        var targetId = oThis.attr("targetId");
                                                        var title = oThis.attr("oldTitle");
                                                        console.log(title);
                                                        console.log(targetId);
                                                        if (confirm("点击确定将还原标题")) {
                                                            $.ajax({
                                                                url: "/Titles/rename",
                                                                data: {"numIid": targetId, "title": title},
                                                                type: "post",
                                                                datatype: "json",
                                                                async: true,//异步
                                                                success: function (data) {
                                                                    if (data.ok && data.res != null && data.res.length > 0) {
                                                                        alertText.html("<span style='color: #008000;'>提示：亲，还原成功哦。</span>");

                                                                    } else {
                                                                        alertText.html("<span style='color: red;'>提示：还原失败  " + data.msg + "</span>");

                                                                    }
                                                                },
                                                                error: function () {
                                                                    alertText.html("<span style='color: red;'>提示：哎呀出错了，请尝试刷新页面，如果问题依然存在，请联系我们</span>");

                                                                }
                                                            })
                                                        }
                                                    });

                                                }
                                            }
                                        });

                                    })

                                });




















                    })













                }
            }
        });


    });

    //jQuery回车键提交
    $("#searchText").keyup(function(event){
        if (event.which == 13) {

            $(".startBtn").trigger("click");
        }
    })




    //默认将全部修改过的商品加载到页面
    alertText.html("亲请稍后，系统正在玩命加载数据...")
    $("#holder").tmpage({
        currPage: 1,
        pageSize: 10,
        pageCount:1,
        ajax:{
            param : {status:0,
                catId:null,
                sort:1,
                lowBegin:0,
                topEnd:100},
            url:jsonUrl,
            dataType:"json",
            type:"get",
            async:true,//异步
            on: true,
             callback:function(data){
                 var objData=eval(data);


                 // objData.titleInfo.length=0;

                 if(objData.res.length>0){
                     alertText.html("提示：一共有  <span style='color: red;'>"+objData.count+"</span>  条符合要求的数据。")
                 }else{
                     alertText.html("<span style='color: red;'>提示：亲，暂时没有符合要求的数据哦。</span>");
                     me.html("");
                     return;
                 }

//                for(i=0;i<objData.titleInfo.length;i++){
//                    objData.titleInfo[i].numIid=new Date(objData.titleInfo[i].numIid).formatYMDHMS();//格式化日期时间
//
//
//                }
                 var desk=$("#desk");

                 desk.load("/public/tmpl/oneTitleRecord.html",function(){
                     var meHtml=desk.find("#record").tmpl(objData);
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







                     //查看详情功能
                     $('.recoverInfo').click(function(){
                         //所有参数都用默认值,并且是遮住整个页面的

                         var oThis = $(this);//得到当前对象
                         var targetId= oThis.attr("targetId");
                         //判断是否有还原记录，如果没有就不弹出层，直接提示用户没有数据
//                         var succn=oThis.attr("succn");
//
//                         //如果还原的宝贝数量为0
//                         if(succn<1){
//                             alertText.html("<span style='color: red;'>提示：亲，优化成功的宝贝数量为0，无法查看详情哦</span>");
//                             return;
//                         }

                         var desk=$("#desk");
                         desk.load("/public/tmpl/oneTitleRecordInfo.html",function(){



                             $("body").mask("<div class='mymask'></div>");



                             $(".renameHistoryPagging").tmpage({
                                 currPage: 1,
                                 pageSize: 10,
                                 pageCount: 1,
                                 ajax: {
                                     param:{"numIid":targetId},
                                     on: true,
                                     dataType: 'json',
                                     url: "/Titles/renameHistoryAll",
                                     callback: function (data) {

                                         if (data == null || data == undefined || data == "") {
                                             alertText.html("<span style='color: red;'>提示：没有成功还原的数据哦亲</span>");
                                             return;

                                         }


                                         //格式化日期时间
                                         $.each(data.res, function (index, item) {
                                             item.updated = new Date(item.updated).formatYMDHMS();
                                         });



//                                if(data<1){
//                                    alertText.html("<span style='color: red;'>提示：没有成功还原的数据哦亲</span>");
//                                    return;
//
//                                }


                                         var mytmpl = desk.find("#maskTable").tmpl(data);


                                         desk.html("");
                                         $(".mymask").append(mytmpl);//添加到页面中


                                         $(".maskTbody>tr:even").css("background-color", "#e3e3e3");//隔行变色

                                         $(".titleClose").click(function () {
                                             $("body").unmask();//关闭遮罩层

                                         });


                                         //还原数据功能
                                         $('.recoverBtn').click(function () {
                                             var oThis = $(this);//得到当前对象
                                             var targetId = oThis.attr("targetId");
                                             var title = oThis.attr("oldTitle");
                                             if (confirm("点击确定将还原标题")) {
                                                 $.ajax({
                                                     url: "/Titles/rename",
                                                     data: {"numIid": targetId, "title": title},
                                                     type: "post",
                                                     datatype: "json",
                                                     async: true,//异步
                                                     success: function (data) {
                                                         if (data.ok && data.res != null && data.res.length > 0) {
                                                             alertText.html("<span style='color: #008000;'>提示：亲，还原成功哦。</span>");

                                                         } else {
                                                             alertText.html("<span style='color: red;'>提示：还原失败  " + data.msg + "</span>");

                                                         }
                                                     },
                                                     error: function () {
                                                         alertText.html("<span style='color: red;'>提示：哎呀出错了，请尝试刷新页面，如果问题依然存在，请联系我们</span>");

                                                     }
                                                 })
                                             }
                                         });

                                     }
                                 }
                             });

                         })

                     });







//                    $('.detailBtn').click(function(){
//                        //所有参数都用默认值,并且是遮住整个页面的
//                        $("body").mask("<div class='mymask'></div>");
//
//
//                        var desk=$("#desk");
//                        desk.load("/public/tmpl/allTitleRecordInfo.html",function(){
//
//                            $.getJSON("/public/javascripts/newAutoTitle/titleRestoreInfo.json",function(data){
//                                var myJson=[];
//                                var myJson=eval(data);
//
//                                var mytmpl= desk.find("#maskTable").tmpl(myJson);
//
//                                //alert(mytmpl.html())
//
//                                desk.html("");
//                                $(".mymask").append(mytmpl);//添加到页面中
//
////
//                                $(".maskTbody>tr:even").css("background-color","#e3e3e3");//隔行变色
//
//                                $(".titleClose").click(function(){
//                                    $("body").unmask();//关闭遮罩层
//
//                                });
//
//
//                            })
//
//                        })
//
//                    });








                 })













             }
        }
    });






});