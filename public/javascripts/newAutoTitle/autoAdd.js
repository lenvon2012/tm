
var TM=TM||{};
((function ($, window) {
    TM.autoAdd=TM.autoAdd||{};


    var autoAdd=TM.autoAdd;

    autoAdd.init=autoAdd.init||{};
    autoAdd.init= $.extend({
        doInit:function(container){
            autoAdd.container=container;
            autoAdd.show.doShow(container);
            autoAdd.init.initBtn(container);


        },
        //获得table所需要的数据并返回
        getTableData:function(){
            var addData=null;
            $.ajax({
                url:"/DelistPlan/queryDelistPlanList",
                async:false,
                type:"post",
                datatype:"Json",
                success:function(data){
                       console.log(data);




//                       data.results[0]={autoAddNewItem: true,
//                           createTime: 1379491812061,
//                           createTimeStr: "2013-09-18 16:10:12",
//                           delistAllTheTime: false,
//                           delistCateIds: "all",
//                           delistConfig: 9,
//                           delistInstockItems: false,
//                           distriNums: "0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0",
//                           filterGoodSalesItem: true,
//                           hourRates: "0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0",
//                           id: 165,
//                           itemStatusRule: 1,
//                           planId: 165,
//                           planTurnOn: true,
//                           ruleItemType: true,
//                           salesNumRule: 1,
//                           selectNumIids: null,
//                           selfCateIds: "all",
//                           status: 1,
//                           templateType: 1,
//                           title: "默认上下架计划",
//                           updateTime: 1403554256407,
//                           userId: 1039626382,
//                           userSelectItemType: false};
                       addData=data;
                },
                error:function(){
                    alert("加载表格数据出错")
                }


            });

            //返回数据
            return addData;
        },
        //获得Cartogram所需要的数据并返回
        getCartogramData:function(){
            var addData=null;
            $.ajax({
                url:"/AutoDelist/queryDelistDistribute",
                async:false,
                type:"post",
                datatype:"Json",
                success:function(data){
                    addData=data;
                },
                error:function(){
                    alert("加载统计图数据出错")
                }


            });

            //返回数据
            return addData;
        },
        //初始化按钮的功能
        initBtn:function(container){
               //开关计划按钮
                var onOffBtn=container.find(".onOffBtn");
                 //删除计划按钮
                var deleteBtn=container.find(".deleteBtn");
                autoAdd.init.onOffBtn(onOffBtn);
                autoAdd.init.deleteBtn(deleteBtn);
        },
        //格式化日期时间
        fomatYMD:function(date){

            //格式化date
            var vipDate=new Date(date);
            var month = vipDate.getMonth() + 1 < 10 ? "0" + (vipDate.getMonth() + 1) : vipDate.getMonth() + 1;
            var day = vipDate.getDate() < 10 ? "0" + vipDate.getDate() : vipDate.getDate();

            var formatYMD= vipDate.getFullYear()+"-"+month+"-"+day;

            return formatYMD;

        },
        //暂停、开始计划功能
        onOffBtn:function(btn){

            btn.click(function(){
                //得到当前记录的ID
                var nowObj=$(this);
                var planId=nowObj.parent().attr("planId");
                //得到当前记录的statu
                var statu=nowObj.parents().attr("statu");
                //如果当前状态时开启的话 ，执行关闭
                 if(statu==1){
                     $.ajax({
                         url:"/delistplan/turnOffPlan",
                         data:{
                             "planId":planId
                         },
                         datatype:"json",
                         async:true,
                         success:function(data){
//                             console.log(data)
                             if(data.success){
                                 nowObj.html("开启");
                             }
                         },
                         error:function(){
                             alert("关闭计划出错 ");
                         }
                     });
                 }else{
                     $.ajax({
                         url:"/delistplan/turnOnPlan",
                         data:{
                             "planId":planId
                         },
                         datatype:"json",
                         async:true,
                         success:function(data){
//                             console.log(data)

                             if(data.success){
                                 nowObj.html("关闭");
                             }
                         },
                         error:function(){
                             alert("开启计划出错 ");
                         }
                     });
                 }
            });

        },
        //查看详情功能
        infoBtn:function(btn){

        },
        //编辑功能
        updateBtn:function(btn){

        },
        //删除功能
        deleteBtn:function(btn){
             btn.click(function(){
                 if(confirm("你确定要删除这个上架计划吗？")){
                     //得到当前的对象
                     var nowObj=$(this);
                     var planId=nowObj.parent().attr("planId");
                     $.ajax({
                         url:"/delistplan/deletePlan",
                         data:{
                             "planId":planId
                         },
                         datatype:"json",
                         async:true,
                         success:function(data){
                             //如果成功就刷新当前页面
                             if(data.success){
                                 TM.autoAdd.init.doInit($("#auto-add-main"));
                             }else{  //失败就提示失败信息
                                 alert(data.message);
                             }
                         },
                         error:function(){
                             alert("关闭计划出错 ");
                         }
                     });
                 }


            });
        }


    },autoAdd.init);

    autoAdd.show=autoAdd.show||{};
    autoAdd.show= $.extend({
        doShow:function(container){
            //显示table中的数据
            autoAdd.show.doShowTable(container.find(".plan-table-body"),container.find("#tmpl-talbe-text"));
            //显示Cartogram中的数据
            autoAdd.show.doShowCartogram(container.find(".plan-cartogram-body"));
        },
        doShowTable:function(showDiv,tmplId){
            var data=autoAdd.init.getTableData();

             htmls=tmplId.tmpl(data);

            showDiv.append(htmls);
        },
        doShowCartogram:function(showEle){
            var showData=autoAdd.init.getCartogramData();

            $(".plan-cartogram-body").highcharts({
                title: {
                    text: '上架计划分布图',
                    x: -20, //center
                    style: {
                        color: '#3E576F',
                        fontSize: '24px',
                        color: '#3E576F',
                        'font-size': '20px',
                        fill: '#3E576F',
                        width: '936px',
                        'font-weight': 100,
                        'font-family': "微软雅黑"

                    },
                    y:30
                },
                subtitle: {
                    text: '[ 计划上架一周生效 ] ',
                    x: -20,
                    y:50,
                    style:{
                        color:'#999',
                        fill:'#999'
                    }
                },
                xAxis: {
                    categories: ['周一', '周二', '周三', '周四', '周五', '周六','周日'],
                    tickColor: '#FF0000',
                    labels: {
                        style: {
                            'font-family':'微软雅黑'
                        }
                    }
                },
                yAxis: {
                    title: {
                        text: ''
                    },
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }]
                },
                tooltip: {
                    valueSuffix: '个'
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'middle',
                    borderWidth: 0
                },
                series: [{
                    name: '当前上架',
                    data: showData[0]
                }, {
                    name: '计划上架',
                    data:showData[1]
                }],
                colors: ['#1aadce', '#a6c96a', '#8bbc21', '#910000', '#1aadce',
                    '#492970', '#f28f43', '#77a1e5', '#c42525', '#a6c96a'],
                credits:{
                    style: {
                        display: 'none'

                    }
                }

            });
        }
    });

})(jQuery, window));
