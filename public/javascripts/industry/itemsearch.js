  ((function ($, window) {
      TM.ItemSearch = TM.ItemSearch || {};
      
      var ItemSearch = TM.ItemSearch;
      
      ItemSearch.init = ItemSearch.init || {};
      ItemSearch.init = $.extend({
          doInit : function(container) {
              ItemSearch.container = container;
              
              container.find(".delist-search-btn").click(function() {
                  ItemSearch.show.doShow();
                  //$('.search-input').val("");
              });
              container.find(".search-input").keydown(function() {
                if (event.keyCode == 13) {//按回车
                    ItemSearch.show.doShow();
                }
            });
          },
          getContainer : function() {
              return ItemSearch.container;
          }
      },ItemSearch.init);
      
      ItemSearch.show = ItemSearch.show || {};
      ItemSearch.show = $.extend({
          doShow : function() {
              var paramData = ItemSearch.show.getParamData();
              
              if(paramData == null) {
                  return ;
              }
              
              var container = ItemSearch.init.getContainer();
              $.ajax({
                  type : 'POST' ,
                  url : '/DelistSearch/findItemDelistByHref' ,
                  data : {
                      searchKey: paramData
                  } ,
                  dataType: "json" ,
                  success : function(dataJson) {
                      var result = JSON.parse(dataJson.message);

                      var tipDivObj = container.find(".item-tip-div");
                      tipDivObj.hide();
                      var resultDivObj = container.find(".item-delist-result");
                      resultDivObj.show();

                      var delistTime = new Date(parseInt(result.data[0].end) * 1000).formatYMDHMS();
                      resultDivObj.find(".delist-time-span").html(delistTime);
                  },
                  error : function(e){
                      alert("请输入正确的宝贝链接!")
                  }
              });
              //$.ajax({
              //    type : 'POST' ,
              //    url : '/DelistSearch/findItemDelistTime' ,
              //    data : {searchUrl: paramData},
              //    dataType: "json" ,
              //    success : function(dataJson) {
              //        if(!dataJson.success){
              //            alert(dataJson.message);
              //            return;
              //        }
              //        container.find(".item-tip-div").hide();
              //        var resultDivObj = container.find(".item-delist-result");
              //        resultDivObj.show();
              //        var delistTime = parseInt(dataJson.message) + (7 * 24 * 60 * 60 * 1000);
              //        resultDivObj.find(".delist-time-span").html(new Date(delistTime).formatYMDHMS());
              //    },
              //    error : function(e){
              //        alert("请输入正确的宝贝链接!")
              //    }
              //});
          },
          getParamData : function() {
              var paramData = {};
              
              var container = ItemSearch.init.getContainer();
              
              var searchKey = container.find(".search-input").val();
              
              if(searchKey == "") {
                  alert("请先输入要搜索的宝贝!");
                  return null;
              }
              return searchKey;
          }
      },ItemSearch.show);
      Date.prototype.formatYMDHMS = function(){
          var format = "yyyy-MM-dd hh:mm:ss";
          var o = {
              "M+" : this.getMonth()+1, //month
              "d+" : this.getDate(),    //day
              "h+" : this.getHours(),   //hour
              "m+" : this.getMinutes(), //minute
              "s+" : this.getSeconds(), //second
              "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
              "S" : this.getMilliseconds() //millisecond
          }
          if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
              (this.getFullYear()+"").substr(4 - RegExp.$1.length));
          for(var k in o)if(new RegExp("("+ k +")").test(format))
              format = format.replace(RegExp.$1,
                  RegExp.$1.length==1 ? o[k] :
                      ("00"+ o[k]).substr((""+ o[k]).length));
          return format;
      }
  })(jQuery,window));