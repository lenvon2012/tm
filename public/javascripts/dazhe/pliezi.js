//timerPicker
$(document).ready(function(){
    /*if((document.location.href.indexOf("discount-create-page") > -1) || (document.location.href.indexOf("tuan-create-page") > -1)){
     var myDate = new Date();
     myYear=myDate.getFullYear(),    //获取完整的年份(4位,1970-????)
     myMonth=myDate.getMonth()+1,       //获取当前月份(0-11,0代表1月)
     myDaily=myDate.getDate();		//获取当前日(1-31)
     var thisMonth=(myMonth<10)?"-0":"-",
     thisDaily=(myDaily<10)?"-0":"-";
     $("#startTime").val(myYear+thisMonth+myMonth+thisDaily+myDaily+" 00:00:00");
     }	*/
//包邮
    (function($){
        var YBTM={
            areaCut:"",
            price:"",
            post:[],
            spliceArr:function(arr,currentItemId){
                for(var k=0;k<arr.length;k++){
                    var itemNum=arr[k];
                    if(currentItemId==itemNum){
                        arr.splice(k,1);
                    }
                }
            }
        }
        $("input[name='postSet']:checked").each(function(){
            if($("input[name='postSet']:checked").attr("value")==1){
                var index = $(".theMD").index($(this).closest(".theMD"));
                $(this).closest(".theMD").find(".noSet").html("选择免邮地区");
                $(this).closest(".theMD").find(".theLeft").html("免邮地区：");
            }else if($("input[name='postSet']:checked").attr("value")==2){
                $(this).closest(".theMD").find(".noSet").html("选择不免邮地区");
                $(this).closest(".theMD").find(".theLeft").html("不免邮地区：");
            }else if($("input[name='postSet']:checked").attr("value")==3){
                $(".noSet").html("");
                $(".freeMail").hide();
                $(".closeMail").hide();
            }
        })
        $("input[name='postSet']").live("click",function(){
            YBTM.post=[];
            var num=$("#manDetail select[name='conditionType']").find("option:selected").val();
            var index = $(".theMD").index($(this).closest(".theMD"));
            var conVal=$("#manDetail input[name='isCondition']:checked").attr("value");
            $(".youMail").hide();
            $(".btmBby").eq(index).hide();
            var mythis=$(this);
            var val=$(this).val();
            $(this).closest(".theMD").find(".mdLi input").attr("checked",false);
            $(".theRight span").removeClass("unpost").hide();
            if(val==1){
                $(".noCut").show();
                if(conVal==0){
                    $(".youCondition").html("无条件包邮；");
                }else if(conVal==1){
                    if(YBTM.price!=0 && num==1){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                    }else if(YBTM.price!=0 && num==2){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                    }
                }
                mythis.parents(".theMD").find(".noSet").html("选择免邮地区");
                mythis.parents(".theMD").find(".theLeft").html("免邮地区：");
            }else if(val==2){
                $(".noCut").show();
                if(conVal==0){
                    $(".youCondition").html("无条件包邮；");
                }else if(conVal==1){
                    if(YBTM.price!=0 && num==1){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                    }else if(YBTM.price!=0 && num==2){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                    }
                }
                mythis.parents(".theMD").find(".noSet").html("选择不免邮地区");
                mythis.parents(".theMD").find(".theLeft").html("不免邮地区：");
            }else if(val==3){
                $(".noCut").find("input").val("").end().hide();
                $(".youJian").hide();
                if(conVal==0){
                    $(".youCondition").html("无条件全国包邮；");
                }else if(conVal==1){
                    if(YBTM.price!=0 && num==1){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可全国包邮；");
                    }else if(YBTM.price!=0 && num==2){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可全国包邮；");
                    }
                }
                $(".noSet").html("");
                $(".freeMail").hide();
                $(".closeMail").hide();
            }
        });
        $("#theMDCon .noSet").live("click",function(){
            YBTM.post=[];
            $(this).siblings(".closeMail").show();
            $(this).parents(".theMD").find(".freeMail").show();
            var $myIput=$(this).parents(".theMD").find(".freeMail").find("input[type='checkbox']:checked");
            for(var i=0; i<$myIput.length; i++){
                var myarea=$myIput.eq(i).next("span").html();
                YBTM.post[i]=myarea;
            }
        });
        $("input[name='areaCut']").live("change",function(){
            YBTM.areaCut=$(this).val();
            if(YBTM.areaCut!=0){
                $(".youJian").html('不包邮地区减<span style="color:red;">'+YBTM.areaCut+'</span>元；').show();
            }else{
                $(".youJian").hide();
            }
        });
        $("#manDetail .mdUl input[type='checkbox']").live("click",function(){
            var area=$(this).next("span").html();
            var val=$("#manDetail input[name='isCondition']:checked").attr("value");
            var num=$("#manDetail select[name='conditionType']").find("option:selected").val();
            if(this.checked){
                YBTM.post.push(area);
            }else{
                YBTM.spliceArr(YBTM.post,area);
            }
            if(YBTM.post.length>0){
                if(val==0){
                    $(".youCondition").html("无条件包邮；");
                }else if(val==1){
                    if(YBTM.price!=0 && num==1){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                    }else if(YBTM.price!=0 && num==2){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                    }
                }
                if($("input[name='postSet']:checked").attr("value")==1){
                    $(".youMail").html("包邮地区："+(YBTM.post.toString()).replace(/,/g," ")).show();
                }else if($("input[name='postSet']:checked").attr("value")==2){
                    $(".youMail").html("不包邮地区："+(YBTM.post.toString()).replace(/,/g," ")).show();
                }
            }else{
                $(".youMail").hide();
            }
        });
        $("#manDetail select[name='conditionType']").live("change",function(){
            var num=$(this).find("option:selected").val();
            var postSet=$("input[name='postSet']:checked").attr("value");
            var val=$("#manDetail input[name='isCondition']:checked").val();
            if(val==1&&num==1&& YBTM.price!=0){
                if(postSet==3){
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可全国包邮；");
                }else{
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                }
            }else if(val==1&&num==2&&YBTM.price!=0){
                if(postSet==3){
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可全国包邮；");
                }else{
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                }
            }
        })
        $("#manDetail input[name='conditionMoney']").live("change",function(){
            YBTM.price=$(this).val();
            var val=$("#manDetail input[name='isCondition']:checked").attr("value");
            var postSet=$("input[name='postSet']:checked").attr("value");
            var num=$("#manDetail select[name='conditionType']").find("option:selected").val();
            if(val==1&&num==1&&YBTM.price!=0){
                if(postSet==3){
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可全国包邮；");
                }else{
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                }
            }else if(val==1&&num==2&&YBTM.price!=0){
                if(postSet==3){
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即全国可包邮；");
                }else{
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                }
            }else if(YBTM.price==0){
                if(postSet==3){
                    $(".youCondition").html("无条件全国包邮；");
                }else{
                    $(".youCondition").html("无条件包邮；");
                }
            }
        })
        $("#manDetail input[name='conditionNum']").live("change",function(){
            YBTM.price=$(this).val();
            var val=$("#manDetail input[name='isCondition']:checked").attr("value");
            var num=$("#manDetail select[name='conditionType']").find("option:selected").val();
            var postSet=$("input[name='postSet']:checked").attr("value");
            if(val==1&&num==1&&YBTM.price!=0){
                if(postSet==3){
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可全国包邮；");
                }else{
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                }
            }else if(val==1&&num==2&&YBTM.price!=0){
                if(postSet==3){
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可全国包邮；");
                }else{
                    $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                }
            }else if(YBTM.price==0){
                if(postSet==3){
                    $(".youCondition").html("无条件全国包邮；");
                }else{
                    $(".youCondition").html("无条件包邮；");
                }
            }
        })
        $("#manDetail input[name='isCondition']").live("click",function(){
            var val=$(this).attr("value");
            //var price=$("#manDetail input[name='conditionNum']").val();
            var num=$("#manDetail select[name='conditionType']").find("option:selected").val();
            var postSet=$("input[name='postSet']:checked").attr("value");
            if(val==0){
                YBTM.price=$(this).siblings("input[type='textarea']").val();
                if(postSet==3){
                    $(".youCondition").html("无条件全国包邮；");
                }else{
                    $(".youCondition").html("无条件包邮；");
                }
            }else if(val==1 && YBTM.price!=0){
                if(num==1){
                    if(postSet==3){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可全国包邮；");
                    }else{
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>件，即可包邮；");
                    }
                }else if(num==2 ){
                    if(postSet==3){
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可全国包邮；");
                    }else{
                        $(".youCondition").html("单笔订单满<span style='color:red;'>"+YBTM.price+"</span>元，即可包邮；");
                    }
                }
            }
        })
    })(jQuery);
//满就送
    (function($){
        var BTM = {
            number:"0",
            conditionNum:"",
            conditionType:"1",
            isDiscount:"0",
            discount:"",
            isDecrease:"0",
            decrease:"",
            isPresent:"0",
            presentName:"",
            presentUrl:"javascript:void(0);",
            isPost:"0",
            isNoPost:"",
            post:[],
            spliceArr:function(arr,currentItemId){
                for(var k=0;k<arr.length;k++){
                    var itemNum=arr[k];
                    if(currentItemId==itemNum){
                        arr.splice(k,1);
                    }
                }
            }
        };
        //
        $("input[name='postSet[]']").each(function(){
            var index = $(".theMD").index($(this).closest(".theMD"));
            var myThis=$(this);
            if(myThis.attr("value")==1){
                $(".theMD").eq(index).find(".noSet").html("选择免邮地区");
                $(".theMD").eq(index).find(".theLeft").html("免邮地区：");
            }else if(myThis.attr("value")==2){
                $(".theMD").eq(index).find(".noSet").html("选择不免邮地区");
                $(".theMD").eq(index).find(".theLeft").html("不免邮地区：");
            }else if(myThis.attr("value")==3){
                $(".theMD").eq(index).find(".noSet").html("");
                $(".theMD").eq(index).find(".closeMail").hide();
                $(".theMD").eq(index).find(".freeMail").hide();
            }
        })
        $(".postSet").live("click",function(){
            BTM.post=[];
            $(this).siblings(".postSet").attr("checked",false);
            var index = $(".theMD").index($(this).closest(".theMD"));
            $(".btmBby").eq(index).hide();
            var mythis=$(this);
            var val=$(this).val();
            $(this).siblings("input[name='postSet[]']").val(val);
            $(this).closest(".theMD").find(".mdLi input").attr("checked",false);
            $(this).closest(".theMD").find(".theRight span").removeClass("unpost").hide();
            if(val==1){
                $(".btmYh").eq(index).find(".btmBy").html("，包邮 ");
                mythis.parents(".theMD").find(".noSet").html("选择免邮地区");
                mythis.parents(".theMD").find(".theLeft").html("免邮地区：");
            }else if(val==2){
                $(".btmYh").eq(index).find(".btmBy").html("，包邮 ");
                mythis.parents(".theMD").find(".noSet").html("选择不免邮地区");
                mythis.parents(".theMD").find(".theLeft").html("不免邮地区：");
            }else if(val==3){
                $(".btmYh").eq(index).find(".btmBy").html("，全国包邮 ");
                mythis.closest(".theMD").find(".noSet").html("");
                mythis.closest(".theMD").find(".closeMail").hide();
                mythis.closest(".theMD").find(".freeMail").hide();
            }
        });
        //
        $("input[name='templateImg']").bind("change",function(){
            var val=$(this).val();
            $(".activityList .Utilities img").attr("src",val);
            if($("input[name='template']:checked").val()==0 && val!=""){
                $(".activityList .Utilities").show();
            }else{
                $(".activityList .Utilities").hide();
            }
        });
        $("input[name='template']").bind("click",function(){
            var thisVal=$(this).val();
            if(thisVal==0){
                $(".activityList table").hide();
                $(".activityList .Utilities").show();
            }else{
                $(".activityList .Utilities").hide();
                $(".activityList table").show();
            }
        });
        $("#addMD a").bind("click",function(){
            BTM.post=[];
            var numBer=$("#theMDCon .theMD").length;
            var theMd = $("#tempMD").clone(true).removeAttr("id");
            var btmYh ='<div class="btmYh" style="width:575px; color:#313131; font-weight:normal; font-size:14px; line-height:24px; font-family:Tahoma,Geneva,sans-serif; color:#313131;"><span style="display:none;" class="btmMan">满<em class="btmMj" style="color:red;"></em><em class="btmJY"></em></span><span style="display:none;" class="btmDz">，打<em style="color:red;"></em>折</span><span style="display:none;" class="btmJj">，减<em style="color:red;">9</em>元</span><span style="display:none;" class="btmLw">，送<a href="javascript:void(0);" target="_blank" style="color:red;"></a></span><span class="btmBy" style="display:none;">，全国包邮 <em style=" color: #666;"></em></span><span style="color:#666; font-size:12px; display:none;" class="btmBby"></span></div>';
            var val = $("#theMDCon").find(".theMD").eq(0).find("select[name='conditionType[]']").val();
            /*if(val == 1){
             theMd.find("select[name='conditionType[]']").val(1)
             .end()
             .find("input[name='conditionMoney[]']").attr("name","conditionNum[]");
             }else if(val == 2){
             theMd.find("select[name='conditionType[]']").val(2)
             .end()
             .find("input[name='conditionNum[]']").attr("name","conditionMoney[]");
             }*/
            theMd.show().appendTo($("#theMDCon"));
            $(".btmCon").append(btmYh); //
        });

        $(".theMD .mdBorder a").live("click",function(){
            var index = $(".theMD .mdBorder a").index($(this));
            $(".btmYh").eq(index).remove();
            $(this).closest(".theMD").remove();
        });

        $("#theMDCon .noSet").live("click",function(){
            BTM.post=[];
            $(this).siblings(".closeMail").show();
            $(this).parents(".theMD").find(".freeMail").show();
            var $myIput=$(this).parents(".theMD").find(".freeMail").find("input[type='checkbox']:checked");
            for(var i=0; i<$myIput.length; i++){
                var myarea=$myIput.eq(i).next("span").html();
                BTM.post[i]=myarea;
            }
        });
        $("#theMDCon .mdUl input[type='checkbox']").live("click",function(){
            var index=$(".theMD").index($(this).parents(".theMD"));
            var area=$(this).next("span").html();
            var postSet=$(this).closest(".theMD").find("input[name='postSet[]']").val();
            if(this.checked){
                BTM.post.push(area);
            }else{
                BTM.spliceArr(BTM.post,area);
            }
            if($(this).parents(".theMD").find(".isPost:checked").attr("checked") && BTM.post.length>0){
                if(postSet==2){
                    $(".btmYh").eq(index).find(".btmBby").html('(不包邮地区：'+(BTM.post.toString()).replace(/,/g," ")+')').show();
                    $(".btmYh").eq(index).find(".btmBy").html("，包邮 ");
                }else if(postSet==1){
                    $(".btmYh").eq(index).find(".btmBby").html('(包邮地区：'+(BTM.post.toString()).replace(/,/g," ")+')').show();
                    $(".btmYh").eq(index).find(".btmBy").html("，包邮 ");
                }
            }else{
                $(".btmYh").eq(index).find(".btmBby").hide();
            }
        });
        $("#theMDCon .isPost").live("click",function(){
            var index=$("#theMDCon .isPost").index($(this));
            var postSet=$(this).closest(".theMD").find("input[name='postSet[]']").val();
            if(this.checked){
                $(".btmYh").eq(index).find(".btmMan").show();
                $(".btmYh").eq(index).find(".btmBy").show();
                if(BTM.post.length>0){
                    if(postSet==2){
                        $(".btmYh").eq(index).find(".btmBby").html('(不包邮地区：'+(BTM.post.toString()).replace(/,/g," ")+')').show();
                        $(".btmYh").eq(index).find(".btmBy").html("，包邮 ");
                    }else if(postSet==1){
                        $(".btmYh").eq(index).find(".btmBby").html('(包邮地区：'+(BTM.post.toString()).replace(/,/g," ")+')').show();
                        $(".btmYh").eq(index).find(".btmBy").html("，包邮 ");
                    }else if(postSet==3){
                        $(".btmYh").eq(index).find(".btmBy").html("，全国包邮 ");
                    }
                }
            }else{
                $(".btmYh").eq(index).find(".btmBy").hide();
                $(".btmYh").eq(index).find(".btmBby").hide();
            }
        });
        $("#theMDCon input[name='presentUrl[]']").live("change",function(){
            var index=$("#theMDCon input[name='presentUrl[]']").index($(this));
            var url=$(this).val();
            if(url==""){
                BTM.presentUrl="javascript:void(0);";
            }else{
                BTM.presentUrl=url;
            }
            $(".btmYh").eq(index).find(".btmLw a").attr("href",BTM.presentUrl);
        });
        $("#theMDCon input[name='presentName[]']").live("change",function(){
            var index=$("#theMDCon input[name='presentName[]']").index($(this));
            BTM.presentName=$(this).val();
            if(BTM.presentName==""){
                $(".btmLw").hide();
            }else{
                if($(this).siblings(".isPresent:checked").attr("checked")){
                    $(".btmYh").eq(index).find(".btmLw a").html(BTM.presentName);
                    $(".btmYh").eq(index).find(".btmMan").show();
                    $(".btmYh").eq(index).find(".btmLw").show();
                }else{
                    $(".btmYh").eq(index).find(".btmLw").hide();
                }
            }
        });
        $("#theMDCon .isPresent").live("click",function(){
            var index=$("#theMDCon .isPresent").index($(this));
            if(this.checked){
                if($(this).siblings("input[name='presentName[]']").val()==""){
                    $(".btmYh").eq(index).find(".btmLw").hide();
                }else{
                    $(".btmYh").eq(index).find(".btmLw a").html(BTM.presentName);
                    $(".btmYh").eq(index).find(".btmMan").show();
                    $(".btmYh").eq(index).find(".btmLw").show();
                }
            }else{
                $(".btmYh").eq(index).find(".btmLw").hide();
                $(this).siblings("input[name='presentName[]']").val("");
            }
        });

        $("#theMDCon input[name='decrease[]']").live("change",function(){
            var index=$("#theMDCon input[name='decrease[]']").index($(this));
            BTM.decrease=$(this).val();
            if(BTM.decrease==""){
                $(".btmYh").eq(index).find(".btmJj").hide();
            }else{
                if($(this).siblings(".isDecrease:checked").attr("checked")){
                    $(".btmYh").eq(index).find(".btmJj em").html(BTM.decrease);
                    $(".btmYh").eq(index).find(".btmMan").show();
                    $(".btmYh").eq(index).find(".btmJj").show();
                }else{
                    $(".btmYh").eq(index).find(".btmJj").hide();
                }
            }
        });
        $("#theMDCon .isDecrease").live("click",function(){
            var index=$("#theMDCon .isDecrease").index($(this));
            if(this.checked){
                if($(this).siblings("input[name='decrease[]']").val()==""){
                    $(".btmYh").eq(index).find(".btmJj").hide();
                }else{
                    $(".btmYh").eq(index).find(".btmJj em").html(BTM.decrease);
                    $(".btmYh").eq(index).find(".btmMan").show();
                    $(".btmYh").eq(index).find(".btmJj").show();
                }
            }else{
                $(".btmYh").eq(index).find(".btmJj").hide();
                $(this).siblings("input[name='decrease[]']").val("");
            }
        });
        $("#theMDCon input[name='discount[]']").live("change",function(){
            var index = $("#theMDCon input[name='discount[]']").index($(this));
            BTM.discount=$(this).val();
            if(BTM.discount==""){
                $(".btmYh").eq(index).find(".btmDz").hide();
            }else{
                if($(this).siblings(".isDiscount:checked").attr("checked")){
                    $(".btmYh").eq(index).find(".btmDz em").html(BTM.discount);
                    $(".btmYh").eq(index).find(".btmMan").show();
                    $(".btmYh").eq(index).find(".btmDz").show();
                    BTM.discount="";
                }else{
                    $(".btmYh").eq(index).find(".btmDz").hide();
                }
            }
        });

        /*$("#theMDCon .isDiscount").live("click",function(){
         var index = $("#theMDCon .isDiscount").index($(this));
         if(this.checked){
         if($(this).siblings("input[name='discount[]']").val()==""){
         $(".btmYh").eq(index).find(".btmDz").hide();
         }else{
         $(".btmYh").eq(index).find(".btmDz em").html(BTM.discount);
         $(".btmYh").eq(index).find(".btmMan").show();
         $(".btmYh").eq(index).find(".btmDz").show();
         }
         }else{
         $(this).siblings("input[name='discount[]']").val("");
         $(".btmYh").eq(index).find(".btmDz").hide();
         }
         })	*/
        $("textarea[name='description']").bind("keyup",function(){
            var val=$(this).val();
            var num=val.length;
            if(val.length>50){
                $(this).val(val.substring(0, 50));
                event.returnValue = false;
                val=50;
            }
            $(".fontNum").html(val.length);
        })
        $("#theMDCon .discountFlag").live("click",function(){
            var index = $("#theMDCon .discountFlag").index($(this));
            var val=$(this).siblings("input[name='discountValue[]']").val();
            var sel=$(this).siblings("select[name='discountType[]']").find("option:selected").val();
            if(this.checked){
                $(this).siblings("input[name='discountFlag[]']").val("1");
                if(!val){
                    $(".btmYh").eq(index).find(".btmDz,.btmJj").hide();
                }else if(val){
                    if(sel==1){
                        $(".btmYh").eq(index).find(".btmJj em").html(val);
                        $(".btmYh").eq(index).find(".btmJj").show();
                    }else if(sel==2){
                        $(".btmYh").eq(index).find(".btmDz em").html(val);
                        $(".btmYh").eq(index).find(".btmDz").show();
                    }
                    $(".btmYh").eq(index).find(".btmMan").show();

                }
            }else{
                $(this).siblings("input[name='discountFlag[]']").val("0");
                $(this).siblings("input[name='discount[]']").val("");
                $(".btmYh").eq(index).find(".btmDz,.btmJj").hide();
            }
        })

        $("#theMDCon select[name='discountType[]']").live("change",function(){
            var index = $("#theMDCon .theMD").index($(this).parents(".theMD")),
                sel=$(this).val(),
                isVal=$(this).siblings("input[name='discountFlag[]']").val(),
                val=$(this).siblings("input[name='discountValue[]']").val();
            if(sel==1){
                $(this).parents(".theMD").find(".flagJd").html("减");
                $(this).parents(".theMD").find(".flagYz").html("元");
            }else if(sel==2){
                $(this).parents(".theMD").find(".flagJd").html("打");
                $(this).parents(".theMD").find(".flagYz").html("折");
            }
            if(sel==1 && isVal==1 && val!=""){
                $(".btmYh").eq(index).find(".btmDz").hide();
                $(".btmYh").eq(index).find(".btmJj em").html(val);
                $(".btmYh").eq(index).find(".btmJj").show();
            }else if(sel==2 && isVal==1 && val!=""){
                $(".btmYh").eq(index).find(".btmJj").hide();
                $(".btmYh").eq(index).find(".btmDz em").html(val);
                $(".btmYh").eq(index).find(".btmDz").show();
            }
        });

        $("#theMDCon input[name='discountValue[]']").live("change",function(){
            var index = $("#theMDCon .theMD").index($(this).parents(".theMD")),
                val=$(this).val(),
                isVal=$(this).siblings("input[name='discountFlag[]']").val(),
                sel=$(this).siblings("select[name='discountType[]']").find("option:selected").val();
            if(!val){
                $(".btmYh").eq(index).find(".btmDz,.btmJj").hide();
            }else if(val && isVal==1 && sel==1){
                $(".btmYh").eq(index).find(".btmMan").show();
                $(".btmYh").eq(index).find(".btmDz").hide();
                $(".btmYh").eq(index).find(".btmJj em").html(val);
                $(".btmYh").eq(index).find(".btmJj").show();
            }else if(val && isVal==1 && sel==2){
                $(".btmYh").eq(index).find(".btmMan").show();
                $(".btmYh").eq(index).find(".btmJj").hide();
                $(".btmYh").eq(index).find(".btmDz em").html(val);
                $(".btmYh").eq(index).find(".btmDz").show();
            }
        });

        $("#theMDCon input[name='conditionValue[]']").live("change",function(){
            var index=$("#theMDCon .theMD").index($(this).parents(".theMD"));
            BTM.conditionNum=$(this).val();
            if(BTM.conditionNum==0){
                $(".btmYh").eq(index).find(".btmJY").html("");
                $(".btmYh").eq(index).find(".btmMj").html("");
            }else if(BTM.conditionNum!=0){
                if($(this).siblings("select[name='conditionType[]']").find("option:selected").val()==1){
                    $(".btmYh").eq(index).find(".btmJY").html("件");
                }else{
                    $(".btmYh").eq(index).find(".btmJY").html("元");
                }
                $(".btmYh").eq(index).find(".btmMan").show();
                $(".btmYh").eq(index).find(".btmMj").html(BTM.conditionNum);
            }
        });
        $("#theMDCon input[name='conditionNum[]']").live("change",function(){
            var index=$("#theMDCon .theMD").index($(this).parents(".theMD"));
            BTM.conditionNum=$(this).val();
            if(BTM.conditionNum==0){
                $(".btmYh").eq(index).find(".btmJY").html("");
                $(".btmYh").eq(index).find(".btmMj").html("");
            }else if(BTM.conditionNum!=0){
                if($(this).siblings("select[name='conditionType[]']").find("option:selected").val()==1){
                    $(".btmYh").eq(index).find(".btmJY").html("件");
                }else{
                    $(".btmYh").eq(index).find(".btmJY").html("元");
                }
                $(".btmYh").eq(index).find(".btmMan").show();
                $(".btmYh").eq(index).find(".btmMj").html(BTM.conditionNum);
            }
        });
        $("#theMDCon input[name='conditionMoney[]']").live("change",function(){
            var index=$("#theMDCon .theMD").index($(this).parents(".theMD"));
            BTM.conditionNum=$(this).val();
            if(BTM.conditionNum==0){
                $(".btmYh").eq(index).find(".btmJY").html("");
                $(".btmYh").eq(index).find(".btmMj").html("");
            }else if(BTM.conditionNum!=0){
                if($(this).siblings("select[name='conditionType[]']").find("option:selected").val()==1){
                    $(".btmYh").eq(index).find(".btmJY").html("件");
                }else{
                    $(".btmYh").eq(index).find(".btmJY").html("元");
                }
                $(".btmYh").eq(index).find(".btmMan").show();
                $(".btmYh").eq(index).find(".btmMj").html(BTM.conditionNum);
            }
        })
        $(".theMD select[name='conditionType[]']").live("change",function(){
            var index=$(".theMD").index($(this).parents(".theMD"));
            //BTM.conditionType=$(this).find("option:selected").val();
            /*	if($(this).siblings("input[name='conditionNum[]']").val()==0 && $(this).siblings("input[name='conditionMoney[]']").val()==0){
             $(".btmYh").eq(index).find(".btmJY").html("");
             }else if($(this).siblings("input[name='conditionNum[]']").val()!=0 || $(this).siblings("input[name='conditionMoney[]']").val()!=0){*/
            if($(this).siblings("input[name='conditionValue[]']").val()==0){
                $(".btmYh").eq(index).find(".btmJY").html("");
            }else if($(this).siblings("input[name='conditionValue[]']").val()!=0){
                if($(this).find("option:selected").val()==2){
                    $(".btmYh").eq(index).find(".btmJY").html("元");
                    //$(".btmJY").html("元");
                }else if($(this).find("option:selected").val()==1){
                    $(".btmYh").eq(index).find(".btmJY").html("件");
                }
                $(".btmYh").eq(index).find(".btmMan").show();
                //$(".btmYh").eq(index).find(".btmMj").html(BTM.conditionNum);
            }
        });
        $("input[name='name']").change(function(){
            var val=$(this).val();
            $(".btmName").html(val);
        })
        $("textarea[name='description']").change(function(){
            var val=$(this).val();
            $(".btmDetails").html(val);
        })
        $("input[name='startTime']").live("change",function(){
            $(".btmStime").html("");
            var val=$(this).val();
            $(".btmTime").show();
            $(".btmStime").html(val);
        });
        $("input[name='endTime']").live("change",function(){
            $(".btmEtime").html("");
            var val=$(this).val();
            $(".btmTime").show();
            $(".btmEtime").html(val);
        });
    })(jQuery);
    (function($){
        $('#startTime').length && $('#startTime').datetimepicker({
            onClose: function(dateText, inst) {
                var endDateTextBox = $('#endTime');
                if (endDateTextBox.val() != '') {
                    var testStartDate = new Date(dateText);
                    var testEndDate = new Date(endDateTextBox.val());
                    if (testStartDate > testEndDate)
                        endDateTextBox.val(dateText);
                }
                else {
                    endDateTextBox.val(dateText);
                }
            },
            onSelect: function (selectedDateTime){
                var start = $(this).datetimepicker('getDate');
                $('#endTime').datetimepicker('option', 'minDate', new Date(start.getTime()));
            },
            showSecond: true,
            timeFormat: 'hh:mm:ss',
            stepHour: 1,
            stepMinute: 1,
            stepSecond: 1
        });

        $('#endTime').length && $('#endTime').datetimepicker({
            onClose: function(dateText, inst) {
                var startDateTextBox = $('#startTime');
                if (startDateTextBox.val() != '') {
                    var testStartDate = new Date(startDateTextBox.val());
                    var testEndDate = new Date(dateText);
                    if (testStartDate > testEndDate)
                        startDateTextBox.val(dateText);
                }
                else {
                    startDateTextBox.val(dateText);
                }
            },
            onSelect: function (selectedDateTime){
                var end = $(this).datetimepicker('getDate');
                $('#startTime').datetimepicker('option', 'maxDate', new Date(end.getTime()) );
            },
            showSecond: true,
            timeFormat: 'hh:mm:ss',
            stepHour: 1,
            stepMinute: 1,
            stepSecond: 1
        });

        var canelPick=function(btn){
            $(btn).siblings("a").click(function(){
                $(btn).unbind().focus()
                    .siblings("a,.cg").hide()
                    .end()
                    .siblings(".cr").html("日期格式：2012-01-08 12:12:12");
                return false;
            });
        };
        if($('#startTime').length && $('#endTime').length){
            canelPick("#startTime");
            canelPick("#endTime");
        }

        $("#useEndDate").length && $("#useEndDate").bind("click",function(){
            $("#endTime").val($("#endDate").val());
            $(".btmEtime").html($("#endDate").val());
            $(".btmTime").show();
        });
    })(jQuery);
    //choose template
    (function($){

        var TEMP = {
            tempNum: null,
            templateImg: null
        };

        $(".chooseTemplate").bind("click",function(){
            TEMP.tempNum = $(this).parent().children("input[name='template']").val();
            TEMP.templateImg = $(this).parent().children("input[name='templateImg']").val();

            if(TEMP.tempNum == 0){
                $("#sysNav").find("a").removeClass("tempNavCurrent");
                $("#cusNav").find("a").addClass("tempNavCurrent");

                $("#bgImg").val(TEMP.templateImg);

                $("#bgView h4").html('<img src="'+TEMP.templateImg+'" width="520" />').show();
                // $("#bgView h4").css({'background-image':url,
                // 				'background-repeat':'no-repeat',
                // 				'background-position':'0 0'}).show();

                $("#sysTemp").hide();
                $("#cusTemp").show();

            }else{
                $("#cusNav").find("a").removeClass("tempNavCurrent");
                $("#sysNav").find("a").addClass("tempNavCurrent");

                $("#sysTemp ul li h4 a").removeClass("tempCurrent");
                $(".temp" + TEMP.tempNum).find("a").addClass("tempCurrent");
                $("#cusTemp").hide();
                $("#sysTemp").show();
            }

            wait.center($("#chooseTemp"),$("#layout"));

            $("#layout,#chooseTemp").show();
        });

        $("#sysTemp ul li h4 a").live("click",function(){
            $("#sysTemp ul li h4 a").removeClass("tempCurrent");
            $(this).addClass("tempCurrent");

            TEMP.tempNum = $(this).closest("h4").attr("class").match(/\d{4}/g)[0];

            return false;
        });

        $("#sysNav a").live("click",function(){

            $("#cusNav").find("a").removeClass("tempNavCurrent");
            $(this).addClass("tempNavCurrent");

            TEMP.tempNum = $(".tempCurrent").closest("h4").attr("class").match(/\d{4}/g)[0];

            $("#cusTemp").hide();
            $("#sysTemp").show();
        });

        $("#cusNav a").live("click",function(){

            $("#sysNav").find("a").removeClass("tempNavCurrent");
            $(this).addClass("tempNavCurrent");

            TEMP.tempNum = 0;

            $("#sysTemp").hide();
            $("#cusTemp").show();
        });

        $("#cusTemp h3 a").live("click",function(){
            var tempImg=$("#bgImg").val();
            $("#bgView h4").html('<img src="'+tempImg+'" width="520" />');
            // $("#bgView h4").css({'background-image':url,
            // 					'background-repeat':'no-repeat',
            // 					'background-position':'0 0'});
            $("#bgView").show();
        });

        $(".confirm").live("click",function(){
            if(TEMP.tempNum == 0){
                TEMP.templateImg = $("#bgImg").val();
                if(!TEMP.templateImg){
                    alert("请添加背景图片链接！");
                    return false;
                }else{
                    $(".templateCon").html("自定义模板");
                    $("input[name='template']").val(0);
                    $("input[name='templateImg']").val(TEMP.templateImg);
                }
            }else{
                $(".templateCon").html("模板" + TEMP.tempNum);
                $("input[name='template']").val(TEMP.tempNum);
                $("input[name='templateImg']").val('');
            }

            $("#layout,#chooseTemp").hide();
        });

        $(".cancel").live("click",function(){
            $("#layout,#chooseTemp").hide();
        });

    })(jQuery);
    //form
    (function($){

        var theForm = {
            coder: 0,
            form: function(theModule){
                var pubPos = $(".message"),
                    pubMes = "提交中";

                if(theForm.coder == 1){
                    return false;
                }
                theForm.coder = 1;

                wait.theWait(pubMes,pubPos);
                var timer = setInterval(function(){
                    wait.theWait(pubMes,pubPos);
                },1000);

                // console.log($("#createForm").serialize());
                var _url = $("#createForm").attr("action");
                var _data = $("#createForm").serialize();
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url:_url,
                    data:_data,
                    success: function(data){
                        clearInterval(timer);
                        theForm.coder = 0;
                        if(data.code == 0){
                            window.location.href = $("#webServer").val() + "/" + theModule + "/" + theModule +"-manage-page";
                        }else{
                            pubPos.html(data.message);
                        }
                    }
                });
            },
            getItemId: function(data){
                for(var i = 0,iLen = data.length;i<iLen;i++){
                    var thisData = data[i]
                    thePro = [];
                    thePro.detail_url = $(thisData).find(".img").find("a").attr("href");
                    thePro.pic_url = $(thisData).find(".img").find("img").attr("src");
                    thePro.title = $(thisData).find(".title").find("a").html();
                    thePro.itemId = $(thisData).find(".itemId").val();
                    thePro.price = $(thisData).find(".price").find("p").html();
                    theForm.pros.push(thePro);
                }
            },
            pros: [],
            priceChange: function(){
                var cutType = $("input[name='cutType']:checked").val();
                if(cutType == 1){
                    $("input[name='discount']").trigger("change");
                }else if(cutType == 2){
                    $("input[name='cut']").trigger("change");
                }else{
                    return false;
                }
            }
        };

        $("input[name='cutType']").bind("click",function(){
            var cutType = $(this).attr("value");
            if(cutType == 1){
                $(".dis").show();
                $(".cut").hide();
                $(".priceNow").show();//
                $("input[name='discount']").val(0);
            }else if(cutType == 2){
                $(".cut").show();
                $(".dis").hide();
                $(".priceNow").hide(); //后加
                $(".xianjia").val("");  //
            }else{
                return false;
            }
            $("input[name='discount']").val('');
            $("input[name='cut']").val('');
            $(".priceNow").length && $(".priceNow").val('');
        });

        $("input[name='range']").bind("click",function(){
            var range = $(this).attr("value");
            if(range == 1 || range == 3){
                $(".JsrangeType").hide();
                $(".JscutType").show();
                $(".moType").hide();
                $(".Jscut").show();
                $(".Jslimit").show();
            }else if(range == 2){
                $(".JsrangeType").show();
                $("input[name='rangeType']:first").attr("checked","checked");
                $(".JscutType").show();
                $(".moType").show();
                $(".Jscut").show();
                $(".Jslimit").show();
            }else{
                return false;
            }
        });

        $("input[name='make']").bind("click",function(){
            var make = $(this).attr("value");
            $("input[name='cut']").val("");
            $(".priceN").html("0");
            if(make == 1){
                $(".Htcdp").show();
                $(".xianjia").hide();
                $("#dprBottom span,#dprBottom a").hide();
                $("select[name='mealId']").show();
                $(".officalTip").show();
                $(".juliuliangTip").hide();
                $(".JsType").hide();
                $(".JsLidu").hide();
                $(".JsYou").hide();
                $(".priceNow").hide();
                $(".priceN").show();
                //商品和价格初始化
                $("#theTable").html("");
                $(".priceNow").val("0");
                $(".totalPrice").html("0");
            }else if(make == 2){
                $(".Htcdp").hide();
                if($("input[name='cutType']:checked").val()==2){  //
                    $(".xianjia").val("").show();	                      //
                }
                $("#dprBottom span,#dprBottom a").show();
                $("select[name='mealId']").hide();
                $(".officalTip").hide();
                $(".juliuliangTip").show();
                $(".JsType").show();
                $(".JsLidu").show();
                $(".JsYou").show();
                $("select[name='mealId'] option").eq(0).attr('selected', 'true');
                //商品和价格初始化
                $("#theTable").html("");
                $(".priceNow").val("0");
                $(".totalPrice").html("0");
                $(".priceNow").show();
                if($("input[name='cutType']:checked").val()==2){
                    $(".priceNow").hide();
                }
                $(".priceN").hide();
            }else{
                return false;
            }
        });

        $("select[name='mealId']").change(function(data){
            $.ajax({
                type: 'GET',
                dataType: "json",
                url: $(this).attr("ref") + '?mealId=' + $(this).val(),
                success: function(data){
                    $("#theTable").html("");
                    $(".priceN").html("0");//$(".priceNow").val("0");
                    $(".totalPrice").html("0");
                    if(data.meal){
                        var theCode = "",
                            totalPrice = 0;
                        for(var i = 0,iLen = data.items.length;i < iLen; i++){
                            var _this = data.items[i];
                            theCode += '<tr><td class="img"><a href="'+_this.detail_url+'" target="_blank"><img src="'+_this.pic_url+'" width="50" /></a></td><td class="title"><a href="'+_this.detail_url+'" target="_blank">'+_this.title+'</a></td><td class="price"><p>'+_this.price+'</p></td><td class="handle"><a href="javascript:void(0);"><strong class="fl"></strong><span class="fl">撤销</span></a></td><input type="hidden" class="itemId" value="'+_this.itemId+'" /></tr>';
                            totalPrice += -(-_this.price);
                        }
                        $("#theTable").html(theCode);
                        $(".totalPrice").html(totalPrice);
                        $(".priceN").html(data.meal.meal_price);      //$(".priceNow").val(data.meal.meal_price);
                    }

                }
            });
        });

        $("input[name='rangeType']").bind("click",function(){
            var rangeType = $(this).attr("value");
            if(rangeType == 1){
                $(".JscutType").show();
                $(".moType").show();
                $(".Jscut").show();
                $(".Jslimit").show();
            }else if(rangeType == 2){
                $(".JscutType").hide();
                $(".moType").hide();
                $(".Jscut").hide();
                $(".Jslimit").hide();
            }else{
                return false;
            }
        });

        $("#discountForm").bind("click",function(){
            theForm.form("discount");
        });

        $("#tuanForm").bind("click",function(){
            theForm.form("tuan");
        });
        $("#recommendForm").bind("click",function(){
            var pubPos = $(".message"),
                pubMes = "提交中";
            if(theForm.coder == 1){
                return false;
            }
            theForm.coder = 1;
            wait.theWait(pubMes,pubPos);
            var timer = setInterval(function(){
                wait.theWait(pubMes,pubPos);
            },1000);
            // console.log($("#createForm").serialize());
            $.ajax({
                type: 'POST',
                dataType: "json",
                url: $("#createForm").attr("action"),
                data: $("#createForm").serialize(),
                success: function(data){
                    clearInterval(timer);
                    theForm.coder = 0;
                    if(data.code == 0){
                        window.location.href = $("#webServer").val() + "/visit/recommend-manage-page";
                    }else{
                        pubPos.html(data.message);
                    }
                }
            });
        });

        $("#youForm").bind("click",function(){
            var pubPos = $(".message"),
                pubMes = "提交中",
                thePost = $("#manDetail .theRight"),
                postData = "";

            if(theForm.coder == 1){
                return false;
            }
            theForm.coder = 1;

            wait.theWait(pubMes,pubPos);
            var timer = setInterval(function(){
                wait.theWait(pubMes,pubPos);
            },1000);

            for(var i = 0,len = thePost.length; i < len; i++){
                var theData = $(thePost[i]).find(".unpost"),
                    thisPost = "postArray=";
                for(var j = 0,length = theData.length;j<length;j++){
                    var thisData = $(theData[j]).attr("class").match(/\d{6}/g);
                    if(j == (length - 1)){
                        thisPost += thisData[0];
                    }else{
                        thisPost += thisData[0] + "#";
                    }

                }
                postData += "&" + thisPost;
            }

            var data = $("#createForm").serialize().replace(/%5B%5D/g,'[]') + postData;
            // console.log(data);
            $.post($("#createForm").attr("action"),data,function(data){
                clearInterval(timer);
                theForm.coder = 0;
                if(data.code == 0){
                    window.location.href = $("#webServer").val() + "/you/you-manage-page";
                }else{
                    pubPos.html(data.message);
                }
            },'json');
        });

        $("#manForm").bind("click",function(){
            var pubPos = $(".message"),
                pubMes = "提交中",
                thePost = $("#theMDCon .theMD .theRight"),
                postData = "";

            if(theForm.coder == 1){
                return false;
            }
            theForm.coder = 1;

            wait.theWait(pubMes,pubPos);
            var timer = setInterval(function(){
                wait.theWait(pubMes,pubPos);
            },1000);

            /*for(var i = 0,len = thePost.length; i < len; i++){
             var theData = $(thePost[i]).find(".unpost"),
             thisPost = "postArray[]=";
             for(var j = 0,length = theData.length;j<length;j++){
             var thisData = $(theData[j]).attr("class").match(/\d{6}/g);
             if(j == (length - 1)){
             thisPost += thisData[0];
             }else{
             thisPost += thisData[0] + "#";
             }
             }
             postData += "&" + thisPost;
             }*/

            var data = $("#createForm").serialize().replace(/%5B%5D/g,'[]');/* + postData*/
            // console.log(data);

            $.post($("#createForm").attr("action"),data,function(data){
                clearInterval(timer);
                theForm.coder = 0;
                if(data.code == 0){
                    window.location.href = $("#webServer").val() + "/man/man-manage-page";
                }else{
                    pubPos.html(data.message);
                }
            },'json');

        });
        $(".mdList select[name='conditionType[]']").live("change",function(){
            var val = $(this).children("option:selected").val(),
                nameChange = $(this).siblings(".middle");
            /*if(val == 1){
             nameChange.attr("name","conditionNum[]");
             $("#theMDCon").find(".theMD").each(function(){
             $(this).find("input[name='conditionMoney[]']").attr("name","conditionNum[]");
             })
             }else if(val == 2){
             nameChange.attr("name","conditionMoney[]");
             $("#theMDCon").find(".theMD").each(function(){
             $(this).find("input[name='conditionNum[]']").attr("name","conditionMoney[]");
             })
             }*/
            //val == 0 ? nameChange.attr("name","conditionNum[]") : nameChange.attr("name","conditionMoney[]");
        });

        /*$(".mdList .select").live("change",function(){
         var val = $(this).children("option:selected").val(),
         nameChange = $(this).siblings(".middle");
         if(val == 1){
         nameChange.attr("name","conditionNum");
         }else if(val == 2){
         nameChange.attr("name","conditionMoney");
         }
         //val == 0 ? nameChange.attr("name","conditionNum") : nameChange.attr("name","conditionMoney");
         });*/

        $(".enableMultiple,.isDiscount,.isDecrease,.isPresent,.isPost").live("click",function(){
            var _this = $(this),
                classEs = ['enableMultiple','isDiscount','isDecrease','isPresent','isPost'];
            for(var i = 0, len = classEs.length;i<len;i++){
                if( _this.hasClass(classEs[i])){
                    if(_this.next("input[type='hidden']").val() == 0){
                        _this.next("input[type='hidden']").val(1);
                    }else if(_this.next("input[type='hidden']").val() == 1){
                        _this.next("input[type='hidden']").val(0);
                    }
                    break;
                }
            };
        });

        $(".theMD .noSet").live("click",function(){
            $(this).siblings(".closeMail").show();
            $(this).closest(".theMD").find(".freeMail").show();
        });

        $(".theMD .closeMail").live("click",function(){
            $(this).hide().closest(".theMD").find(".freeMail").hide();
        });

        $(".theMD .mdList input").live("click",function(){
            var isChecked = $(this).prop("checked"),
                postId= $(this).val(),
                theClassName = "." + postId,
                mailName = $(this).closest(".freeMail").find(".theRight");
            if(isChecked){
                mailName.find(theClassName).addClass("unpost").show();
            }else{
                mailName.find(theClassName).removeClass("unpost").hide();
            }
        });

        $("#dprBottom a").bind("click",function(){
            //var test = $("input[name='make']").val();
            if($("input[name='make']:checked").val() == '1'){
                alert("使用官方的套餐，请选择一个套餐即可，无需选择宝贝");
                return false;
            }

            theForm.pros = [];

            var data = $("#theTable tr");
            data.length && theForm.getItemId(data);

            $("#chooseProBox .proCon").remove();
            $("#proTable .page").html("");
            $.ajax({
                type: 'GET',
                dataType: "json",
                url: $(this).attr("href") + '&random=' + Math.random(),
                success: function(data){
                    if(data.code == 0){
                        var theData = data.item,
                            theCode = "";
                        for(var i = 0,len = theData.length;i<len;i++){
                            var thisData = theData[i],
                                temp = "",
                                checked = "";
                            for(var j = 0,jLen = theForm.pros.length; j<jLen;j++){
                                var thisItemId = theForm.pros[j].itemId;
                                if(thisItemId == thisData.itemId){
                                    checked = "checked";
                                    break;
                                }
                            }
                            if(!checked && thisData.activeId){
                                checked = 'disabled="disabled" title="该宝贝已经加入了其他套餐"';
                            }
                            temp = '<div class="proCon"><ul><li class="checkbox"><input type="checkbox" '+checked+' value="'+thisData.itemId+'" /></li><li class="proImg"><a href="'+thisData.detail_url+'" target="_blank"><img src="'+thisData.pic_url+'" width="50" /></a></li><li class="boxTitle"><a href="'+thisData.detail_url+'" target="_blank">'+thisData.title+'</a></li><li class="boxPrice">'+thisData.price+'</li></ul></div>';
                            // console.log(temp);
                            theCode += temp;
                        }

                        $("#chooseProBox .tableTop").after(theCode);

                        wait.center($("#chooseProBox"),$("#layout"));

                        $("#layout").show();
                        $("#chooseProBox").show();
                        data.pagenavi && $("#proTable .page").html(data.pagenavi);

                    }else{
                        alert(data.message);
                        return false;
                    }
                }
            });
            return false;
        });

        $("#chooseProBox .topBottom a").live("click",function(){
            $("#chooseProBox .proCon").remove();
            $("#proTable .page").html("");
            $.ajax({
                type: 'GET',
                dataType: "json",
                url: $(this).attr("href") + '&random=' + Math.random(),
                success: function(data){
                    if(data.code == 0){

                        var theData = data.item,
                            theCode = "";
                        for(var i = 0,len = theData.length;i<len;i++){
                            var thisData = theData[i],
                                temp = "",
                                checked = "";
                            for(var j = 0,jLen = theForm.pros.length; j<jLen;j++){
                                var thisItemId = theForm.pros[j].itemId;
                                if(thisItemId == thisData.itemId){
                                    checked = "checked";
                                    break;
                                }
                            }
                            if(!checked && thisData.activeId){
                                checked = 'disabled="disabled" title="该宝贝已经加入了其他套餐"';
                            }
                            temp = '<div class="proCon"><ul><li class="checkbox"><input type="checkbox" '+checked+' value="'+thisData.itemId+'" /></li><li class="proImg"><a href="'+thisData.detail_url+'" target="_blank"><img src="'+thisData.pic_url+'" width="50" /></a></li><li class="boxTitle"><a href="'+thisData.detail_url+'" target="_blank">'+thisData.title+'</a></li><li class="boxPrice">'+thisData.price+'</li></ul></div>';
                            // console.log(temp);
                            theCode += temp;
                        }

                        $("#chooseProBox .tableTop").after(theCode);

                        data.pagenavi && $("#proTable .page").html(data.pagenavi);

                    }else{
                        alert(data.message);
                        return false;
                    }
                }
            });
            return false;
        });

        $("#cpmSearch").live("submit",function(){
            $("#chooseProBox .proCon").remove();
            $("#proTable .page").html("");
            $.ajax({
                type: 'GET',
                dataType: "json",
                url: $(this).attr("action") + '&random=' + Math.random(),
                data: $(this).serialize(),
                success: function(data){
                    if(data.code == 0){
                        var theData = data.item,
                            theCode = "";
                        for(var i = 0,len = theData.length;i<len;i++){
                            var thisData = theData[i],
                                temp = "",
                                checked = "";
                            for(var j = 0,jLen = theForm.pros.length; j<jLen;j++){
                                var thisItemId = theForm.pros[j].itemId;
                                if(thisItemId == thisData.itemId){
                                    checked = "checked";
                                    break;
                                }
                            }
                            if(!checked && thisData.activeId){
                                checked = 'disabled="disabled" title="该宝贝已经加入了其他套餐"';
                            }
                            temp = '<div class="proCon"><ul><li class="checkbox"><input type="checkbox" '+checked+' value="'+thisData.itemId+'" /></li><li class="proImg"><a href="'+thisData.detail_url+'" target="_blank"><img src="'+thisData.pic_url+'" width="50" /></a></li><li class="boxTitle"><a href="'+thisData.detail_url+'" target="_blank">'+thisData.title+'</a></li><li class="boxPrice">'+thisData.price+'</li></ul></div>';
                            // console.log(temp);
                            theCode += temp;
                        }
                        $("#chooseProBox .tableTop").after(theCode);
                        data.pagenavi && $("#proTable .page").html(data.pagenavi);
                        return false;
                    }else{
                        alert(data.message);
                        return false;
                    }
                }
            });
            return false;
        });

        $("#proTable .proCon input").live("click",function(){
            var isChecked = $(this).prop("checked"),
                thisId = $(this).val(),
                thisData = $(this).closest("ul");
            if(isChecked){
                var thePro = [];
                thePro.detail_url = thisData.find(".proImg").find("a").attr("href");
                thePro.pic_url = thisData.find(".proImg").find("img").attr("src");
                thePro.title = thisData.find(".boxTitle").find("a").html();
                thePro.price = thisData.find(".boxPrice").html();
                thePro.itemId = thisId;
                theForm.pros.push(thePro);
            }else{
                theForm.pros = $.grep(theForm.pros,function(val){
                    return (val.itemId != thisId);
                });
            }
        });

        $("#cpbTop a").live("click",function(){
            $("#layout").hide();
            $("#chooseProBox").hide();
        });

        $("#cpbBtn a").live("click",function(){

            $("#theTable").html("");
            var theCode = "",
                totalPrice = 0;
            for(var i = 0,iLen = theForm.pros.length;i < iLen; i++){
                var _this = theForm.pros[i];
                theCode += '<tr><td class="img"><a href="'+_this.detail_url+'" target="_blank"><img src="'+_this.pic_url+'" width="50" /></a></td><td class="title"><a href="'+_this.detail_url+'" target="_blank">'+_this.title+'</a></td><td class="price"><p>'+_this.price+'</p></td><td class="handle"><a href="javascript:void(0);"><strong class="fl"></strong><span class="fl">撤销</span></a></td><input type="hidden" class="itemId" value="'+_this.itemId+'" /></tr>';
                totalPrice += -(-_this.price);
            }
            $("#theTable").html(theCode);
            $(".totalPrice").html(totalPrice);
            theForm.priceChange();
            $("#layout").hide();
            $("#chooseProBox").hide();
        });

        $(".handle a").live("click",function(){
            //var test = $("input[name='make']").val();
            if($("input[name='make']:checked").val() == '1' || $(".wbm").html() == '我有官方搭配套餐'){
                alert("使用官方的套餐实现，无需进行该操作");
                return false;
            }

            var _this = $(this).closest("tr"),
                thisPrice = _this.find(".price").find("p").html(),
                totalPrice = $(".totalPrice").html();
            _this.remove();
            $(".totalPrice").html(totalPrice - thisPrice);
            theForm.priceChange();
        });

        $("input[name='discount']").live("change",function(){
            var totalPrice = $(".totalPrice").html(),
                dis = $(this).val();
            if(dis > 0 && dis <10){
                thePrice = Math.round((totalPrice * dis)*10)/100;
                $(".priceNow").val(thePrice);
            }else{
                $(".priceNow").val('');
            }
        });

        $("input[name='cut']").live("change",function(){
            var totalPrice = $(".totalPrice").html(),
                cut = $(this).val(),
                val = Math.round((totalPrice - cut)*100)/100;

            $(".xianjia").val(val);                  //
            if(val >= 0){
                $(".priceNow").val(val);
            }else{
                $(".priceNow").val('');
            }
        });


        if(($("input[name='cutType']:checked").val()==2) && ($("input[name='make']:checked").val()==2)){   //
            $(".xianjia").show();
            $(".priceNow").hide();
        }  //
        $(".xianjia").live("change",function(){                                             //
            var totalPrice = $(".totalPrice").html();
            var cut=$(this).val();
            var cutTot=Math.round((totalPrice - cut)*100)/100;
            $("input[name='cut']").val(cutTot);

        });
        $(".priceNow").live("change",function(){        //
            var cut=$(this).val();
            var totalPrice = $(".totalPrice").html();
            if(totalPrice!=0){
                $(".jianJia").trigger("click");
                var cutTot=Math.round((totalPrice - cut)*100)/100;
                $("input[name='cut']").val(cutTot);
                $(".xianjia").val(cut);
            }
        })
        if($(".wbm").html() == '我有官方搭配套餐'){
            $(".priceN").show();
        }else if($(".wbm").html() == '我没有官方搭配套餐'){
            $(".priceN").hide();
            $(".priceNow").show();
        }
        if($("input[name='make']:checked").val()==1){   //
            var total=$(".priceNow").val();
            $(".priceNow").hide();
            $(".priceN").show();//.html(total);
        }else if($("input[name='make']:checked").val()==2){
            $(".priceNow").show();
            $(".priceN").hide();
        }
        $("#daForm").bind("click",function(){
            var pubPos = $(".message"),
                pubMes = "提交中",
                thePost = $("#theYou .theRight"),
                postData = "",
                items = "",
                tr = $("#daProRight tr");

            if(theForm.coder == 1){
                return false;
            }
            theForm.coder = 1;

            wait.theWait(pubMes,pubPos);
            var timer = setInterval(function(){
                wait.theWait(pubMes,pubPos);
            },1000);

            for(var i = 0,len = thePost.length; i < len; i++){
                var theData = $(thePost[i]).find(".unpost"),
                    thisPost = "postArray=";
                for(var j = 0,length = theData.length;j<length;j++){
                    var thisData = $(theData[j]).attr("class").match(/\d{6}/g);
                    if(j == (length - 1)){
                        thisPost += thisData[0];
                    }else{
                        thisPost += thisData[0] + "#";
                    }
                }
                postData += "&" + thisPost;
            }

            for(var l = 0,len = tr.length; l < len; l++){
                var _tr = tr[l];
                items += "&items[]=" + $(_tr).find(".itemId").val();
            }

            var data = $("#createForm").serialize().replace(/%5B%5D/g,'[]') + postData + items;

            // console.log(data);

            $.post($("#createForm").attr("action"),data,function(data){
                clearInterval(timer);
                theForm.coder = 0;
                if(data.code == 0){
                    window.location.href = $("#webServer").val() + "/da/da-manage-page";
                }else{
                    pubPos.html(data.message);
                }
            },'json');

        });

        $("#shareForm").bind("click",function(){
            var pubPos = $(".message"),
                pubMes = "提交中",
                thePost = $(".freeMail .fmTop .theRight"),
                postData = "";

            if(theForm.coder == 1){
                return false;
            }
            theForm.coder = 1;

            wait.theWait(pubMes,pubPos);
            var timer = setInterval(function(){
                wait.theWait(pubMes,pubPos);
            },1000);

            for(var i = 0,len = thePost.length; i < len; i++){
                var theData = $(thePost[i]).find(".unpost"),
                    thisPost = "unpost=";
                for(var j = 0,length = theData.length;j<length;j++){
                    var thisData = $(theData[j]).attr("class").match(/\d{6}/g);
                    if(j == (length - 1)){
                        thisPost += thisData[0];
                    }else{
                        thisPost += thisData[0] + "#";
                    }
                }
                postData += "&" + thisPost;
            }

            var data = $("#createForm").serialize().replace(/%5B%5D/g,'[]') + postData;

            // console.log(data);

            $.post($("#createForm").attr("action"),data,function(data){
                clearInterval(timer);
                theForm.coder = 0;
                if(data.code == 0){
                    window.location.href = $("#webServer").val() + "/share/share-manage-page";
                }else{
                    pubPos.html(data.message);
                }
            },'json');

        });
    })(jQuery);

});