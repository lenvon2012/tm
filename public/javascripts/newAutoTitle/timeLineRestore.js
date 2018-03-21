/**
 * Created by Administrator on 2014/4/23.
 */
$(function(){

    //改变菜单栏背景颜色
    $(".header-nav").removeClass("current");
    $("#nav3").addClass("current");

    var text1=$("#startText1");
    var alertText=$(".alertText");


    //设置文本框的默认值
    var text1val=text1.val();//得到日期时间框的默认值
    var dayMilles = 3600000 * 24;
        text1.datetimepicker({lang:'ch',step:1,format:'Y-m-d H:i:s',value:new Date(new Date().getTime() - 7 * dayMilles).format("yyyy-MM-dd h:m:s"),onClose: function() {

            if($("#startText1").val()==text1val){//如果文本框中的值未发生改变，就不刷新页面
                return;
            }
            text1val=$("#startText1").val();////如果文本框中的值发生改变，就更新默认参数为当前文本框的值


            //string类型的日期转换为date类型

            // var month=date.getMonth()+1; //获取当前月份

            //     var date= new Date(Date.parse(text1val.replace(/-/g, "/"))); //转换成Date();
            //js中由data向Long转化：
            var num1 = Date.parse(text1val);





            alertText.html("亲请稍后，系统正在玩命加载数据...")

            //ajax刷新页面数据，将符合还原条件的记录筛选出来
            //根据json文件的路径，返回一个ObjectArray
            var me=$(".infoTable");
            var holder=$("#holder");
            holder.tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax:{
                    param : {backToTs: num1},
                    on: true,
                    dataType: 'json',
                    url: "/Titles/searchBackByTime",

                    callback:function(data){

                        var objData=eval(data) ;


                        if(objData.res.length>0){
                            alertText.html("提示：一共有  <span style='color: red;'>"+objData.res.length+"</span>  条符合要求的数据。")
                            $(".rebntSpan").show();    //显示一键还原按钮
                        }else{
                            $(".rebntSpan").hide();     //隐藏一键还原按钮
                            alertText.html("<span style='color: red;'>提示：亲，暂时没有符合要求的数据哦。</span>");
                            me.html("");
                            return;
                        }

                        for(i=0;i<objData.res.length;i++){
                            objData.res[i].lastOptimiseTs=new Date(objData.res[i].lastOptimiseTs).formatYMDHMS();//格式化日期时间

                        }

                        me.load("/public/tmpl/timeLineTitleRecord.html",function(){
                            var meHtml=me.find("#record").tmpl(objData);
                            me.html("")
                            me.append(meHtml);

                            $("#noteTbody>tr:even").css("background-color","#e3e3e3");//隔行变色


//                        /* initiate plugin */
//                        $(".holder").jPages({
//                            containerID: "noteTbody",
//                            next:"下一页",
//                            previous: "上一页",
//                            perPage:15,
//                            fallback:20
//                        });
















                        })


                        //一键还原功能
                        var rebnt=$("#rebnt");
                        rebnt.click(function(){
                            if(confirm("你确定要还原"+objData.res.length+"条记录吗？")){
                                $.ajax({
                                    data:{"backToTs":num1},
                                    url:"/Titles/titleBackByTime",
                                    async:true,//同步
                                    datatype:"json",
                                    success:function(data){
                                        var succ=0;
                                        var lose=0;

                                        $.each(data,function(index,element){
                                            element.ok?succ+=1:lose+=1;

                                        })
                                        alertText.html("提示：成功   <span style='color: red;'>"+succ+"</span>  条"+"失败  <span style='color: red;'>"+lose+"</span>  条");
                                        //




                                    },
                                    error:function(){
                                        alert("出错了")
                                    }


                                })
                            }
                        });














                    },
                    error:function(){
                        alertText.html("哎呀，出错了，请尝试刷新当前页面，如果问题依然存在，请联系我们的管理员")
                    }
                }
            });
        }});//调用插件





//    if($("#startText1").val()>$("#startText1").val()){
//        console.log("1大")
//    }else{
//        console.log($("#startText1").val())
//        console.log($("#startText2").val())
//    }


});