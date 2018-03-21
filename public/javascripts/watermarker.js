var TM = TM ||{};

((function ($, window) {
    TM.WaterMarker = TM.WaterMarker || {};
    var WaterMarker = TM.WaterMarker;

    /**
     * 初始化，生成html等
     * @type {*}
     */
    WaterMarker.init = WaterMarker.init || {};
    WaterMarker.init = $.extend({
        recordDialog: null,
        doInit: function(container) {
            WaterMarker.container = container;
            var html = WaterMarker.init.createHtml();
            container.html("");
            container.append(html);

            /*$.ajax({
                url : '/watermarker/showWaterMarkerRecords',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var recordJsonArray = dataJson;
                    container.find(".item-back-select").html("");
                    $(recordJsonArray).each(function(index, recordJson) {
                        var optionHtml = '<option ';
                        if (index == 0)
                            optionHtml += 'selected="selected" ';
                        optionHtml += "></option>";
                        var optionObj = $(optionHtml)
                        optionObj.attr("value", recordJson.id);
                        optionObj.html(recordJson.recordName);

                        container.find(".item-back-select").append(optionObj);
                    });
                }
            });
             */
            container.find(".item-back-btn").click(function() {
                WaterMarker.submit.returnBackItemImages("/watermarker/returnBackAllWaterMarker");
            });
            container.find(".select-back-item-btn").click(function() {
                var callback = function(numIids) {
                    if (numIids === undefined || numIids == null || numIids == "") {
                        alert("请先选择要还原的宝贝");
                        return;
                    }
                    WaterMarker.submit.returnBackItemImages("/watermarker/returnBackSomeWaterMarker", numIids);
                }
                multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/watermarker/chooseItems","actionURL":"","pn":1,"px":8,"enableSearch":true,waterMark:{waterCallback:callback}});
            });

            WaterMarker.util.judgeIsHasItemSelect();
            WaterMarker.markerIcon.init();

            WaterMarker.hoverImg.init();

            container.find(".add-item-btn").click(function() {
                var callback = function(numIids) {
                    //alert(numIids);
                    WaterMarker.addItems.doAddItems(numIids);

                }
                multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/watermarker/chooseItems","actionURL":"","pn":1,"px":8,"enableSearch":true,waterMark:{waterCallback:callback}});
            });

            container.find(".delete-item-btn").click(function() {
                WaterMarker.submit.deleteCheckedItems();
            });

            container.find(".add-all-item-marker").click(function() {
                WaterMarker.submit.submitWaterMarker();
            });

            container.find(".delete-all-item-marker").click(function() {
                WaterMarker.submit.deleteAllWaterMark();
            });

        },
        createHtml: function() {
            var html = '<div class="watermarker-container">' +
                '<table class="watermarker-top-table">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td style="width: 180px;">' +
                '           <span class="watermarker-title">批量宝贝水印添加</span>' +
                '       </td>' +
                '       <td>' +
                '           <span class="tmbtn sky-blue-btn  add-item-btn" style="margin-left: 10px;">添加宝贝</span>' +
                '       </td>' +
                '       <td style="text-align: right;">' +
                //'           <span>请选择要还原的备份：</span>' +
                //'           <select class="item-back-select"></select>' +
                '           <span class="tmbtn long-yellow-btn  select-back-item-btn" style="">选择还原宝贝</span>' +
                '           <span class="tmbtn long-red-btn  item-back-btn" style="margin-left: 10px;">还原所有宝贝</span>' +
                '       </td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table>' +
                '<div class="blank0" style="height: 10px;"></div>' +
                '<div class="clearfix watermarker-body">' +
                '   <div class="item-img-div">' +
                '       <div class="select-item-tip show-with-no-item">请先选择要添加水印的宝贝</div> ' +
                '       <img class="item-big-img hide-with-no-item" />' +
                '       <img class="hover-marker-icon" title=""/> ' +
                //'       <div class="hover-marker-icon" title=""></div> ' +
                '       <div class="item-op-div hide-with-no-item">' +
                '           <span class="btn btn-success  add-all-item-marker">生成水印</span>' +
                '           <span class="btn btn-success  delete-all-item-marker">删除水印</span>' +
                '           <div class="blank0"></div>' +
                '       </div>' +
                '   </div>' +
                '   <div class="marker-img-div">' +
                '       <div class="marker-tab"></div>' +
                '       <div class="marker-separator"></div>' +
                '       <div class="blank0"></div>' +
                '       <div class="marker-type-div">' +
                '           <div class="select-marker-type marker-type" markerType="recommend">推荐</div>' +
                '           <div class="marker-type unselect-marker-type" markerType="hot-sale">热销</div>' +
                '           <div class="marker-type unselect-marker-type" markerType="discount">折扣</div>' +
                '           <div class="marker-type unselect-marker-type" markerType="promotion">促销</div>' +
                '           <div class="marker-type unselect-marker-type" markerType="mail">包邮</div>' +
                '           <div class="marker-type unselect-marker-type" markerType="new-item">新品</div>' +
                '           <div class="marker-type unselect-marker-type" markerType="quality">正品</div>' +
                '       </div>' +
                '       <div class="marker-img-list">' +
                '           <div class="marker-img-container"></div> ' +
                '           <div class="pagingDiv""></div> ' +
                '           <div class="blank0"></div> ' +
                '       </div>' +
                '       <div class="blank0"></div>' +
                '   </div>' +
                '   <div class="blank0"></div>' +
                '   <div class="item-list-div"></div>' +
                '</div>' +
                '<div class="blank0" style="height: 10px;"></div>' +
                '<span class="tmbtn sky-blue-btn  add-item-btn">添加宝贝</span>' +
                '<span class="tmbtn yellow-btn  delete-item-btn hide-with-no-item" style="margin-left: 10px;">删除选中</span>' +
                '<div class="blank0"></div>' +
                '<div class="blank0" style="height: 10px;"></div>' +
                '</div>';

            return html;
        }
    }, WaterMarker.init);


    WaterMarker.hoverImg = WaterMarker.hoverImg || {};
    WaterMarker.hoverImg = $.extend({
        init: function() {
            var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");
            hoverImgObj.attr("originWidth", hoverImgObj.width() + "px");
            hoverImgObj.attr("originHeight", hoverImgObj.height() + "px");

            hoverImgObj.hover(function() {
                hoverImgObj.css("border-width", "2px");
            }, function() {
                hoverImgObj.css("border-width", "0px");
            });

            //icon的拖动事件
            var isMouseDown = false;
            var dragType = 0;
            var relativeX = 0;
            var relativeY = 0;

            //判断拖动类型
            var judgeDragType = function(event) {
                var mouseDownPosX = event.pageX || event.clientX + document.body.scrollLeft;
                var mouseDownPosY = event.pageY || event.clientY + document.body.scrollTop;
                var hoverLeft = hoverImgObj.offset().left;
                var hoverTop = hoverImgObj.offset().top;
                var hoverWidth = hoverImgObj.width();
                var hoverHeight = hoverImgObj.height();

                var boundary = 10;
                if (mouseDownPosX < hoverLeft + boundary && mouseDownPosY < hoverTop + boundary) {
                    dragType = 1;//左上角
                    relativeX = mouseDownPosX - hoverLeft;
                    relativeY = mouseDownPosY - hoverTop;
                } else if (mouseDownPosX <= hoverLeft + hoverWidth - boundary && mouseDownPosY < hoverTop + boundary) {
                    dragType = 2;//上面
                    relativeX = 0;
                    relativeY = mouseDownPosY - hoverTop;
                } else if (mouseDownPosX > hoverLeft + hoverWidth - boundary && mouseDownPosY < hoverTop + boundary) {
                    dragType = 3;//右上角
                    relativeX = hoverLeft + hoverWidth - mouseDownPosX;
                    relativeY = mouseDownPosY - hoverTop;
                } else if (mouseDownPosX > hoverLeft + hoverWidth - boundary && mouseDownPosY <= hoverTop + hoverHeight - boundary) {
                    dragType = 4;//右边
                    relativeX = hoverLeft + hoverWidth - mouseDownPosX;
                    relativeY = 0;
                } else if (mouseDownPosX > hoverLeft + hoverWidth - boundary && mouseDownPosY > hoverTop + hoverHeight - boundary) {
                    dragType = 5;//右下角
                    relativeX = hoverLeft + hoverWidth - mouseDownPosX;
                    relativeY = hoverTop + hoverHeight - mouseDownPosY;
                } else if (mouseDownPosX >= hoverLeft + boundary && mouseDownPosY > hoverTop + hoverHeight - boundary) {
                    dragType = 6;//下面
                    relativeX = 0;
                    relativeY = hoverTop + hoverHeight - mouseDownPosY;
                } else if (mouseDownPosX < hoverLeft + boundary && mouseDownPosY > hoverTop + hoverHeight - boundary) {
                    dragType = 7;//左下角
                    relativeX = mouseDownPosX - hoverLeft;
                    relativeY = hoverTop + hoverHeight - mouseDownPosY;
                } else if (mouseDownPosX < hoverLeft + boundary && mouseDownPosY >= hoverTop + boundary) {
                    dragType = 8;//左边
                    relativeX = mouseDownPosX - hoverLeft;
                    relativeY = 0;
                } else {
                    dragType = 9;//中间
                    relativeX = mouseDownPosX - hoverLeft;
                    relativeY = mouseDownPosY - hoverTop;
                }
            };

            var setHoverCursor = function() {
                if (dragType == 1) {//左上角
                    hoverImgObj.css("cursor", "nw-resize");
                } else if (dragType == 2) {//上面
                    hoverImgObj.css("cursor", "n-resize");
                } else if (dragType == 3) {//右上角
                    hoverImgObj.css("cursor", "ne-resize");
                } else if (dragType == 4) {//右边
                    hoverImgObj.css("cursor", "e-resize");
                } else if (dragType == 5) {//右下角
                    hoverImgObj.css("cursor", "se-resize");
                } else if (dragType == 6) {//下面
                    hoverImgObj.css("cursor", "s-resize");
                } else if (dragType == 7) {//左下角
                    hoverImgObj.css("cursor", "sw-resize");
                } else if (dragType == 8) {//左边
                    hoverImgObj.css("cursor", "w-resize");
                } else if (dragType == 9) {
                    hoverImgObj.css("cursor", "move");
                }
            };

            //判断有没有出界
            var judgeOutOfRange = function(nextLeft, nextTop, nextWidth, nextHeight) {
                var bigImgObj = WaterMarker.container.find(".item-big-img");
                var imgLeft = bigImgObj.offset().left;
                var imgTop = bigImgObj.offset().top;
                var imgWidth = bigImgObj.width();
                var imgHeight = bigImgObj.height();

                //alert(nextLeft + ",  " + nextTop + ",  " + nextWidth + ",  " + nextHeight);

                if (nextLeft < imgLeft)
                    return false;
                if (nextTop < imgTop)
                    return false;
                if (nextWidth < 1)
                    return false;
                if (nextHeight < 1)
                    return false;
                if (nextLeft + nextWidth > imgWidth)
                    return false;
                if (nextTop + nextHeight > imgHeight)
                    return false;

                return true;
            };

            var hoverResultLeft = 0;
            var hoverResultTop = 0;
            var hoverResultWidth = 0;
            var hoverResultHeight = 0;

            //缩放和拖动
            var moveCallback = function(event) {
                if (isMouseDown == false) {
                    return;
                }

                var hoverLeft = hoverResultLeft;
                var hoverTop = hoverResultTop;
                var hoverWidth = hoverResultWidth;
                var hoverHeight = hoverResultHeight;

                var nextLeft = hoverLeft;
                var nextTop = hoverTop;
                var nextWidth = hoverWidth;
                var nextHeight = hoverHeight;

                var moveX = event.pageX || event.clientX + document.body.scrollLeft;
                var moveY = event.pageY || event.clientY + document.body.scrollTop;

                if (dragType == 1) {
                    //左上角
                    nextWidth = Math.ceil(hoverWidth + hoverLeft + relativeX - moveX);
                    nextHeight = Math.ceil(hoverHeight + hoverTop + relativeY - moveY);
                    nextLeft = hoverLeft - (nextWidth - hoverWidth);
                    nextTop = hoverTop - (nextHeight - hoverHeight);
                } else if (dragType == 2) {
                    //上面
                    nextHeight = Math.ceil(hoverHeight + hoverTop + relativeY - moveY);
                    nextTop = hoverTop - (nextHeight - hoverHeight);
                } else if (dragType == 3) {
                    //右上角
                    nextWidth = Math.ceil(moveX - hoverLeft + relativeX);
                    nextHeight = Math.ceil(hoverHeight + hoverTop + relativeY - moveY);
                    nextTop = hoverTop - (nextHeight - hoverHeight);
                } else if (dragType == 4) {
                    //右边
                    nextWidth = Math.ceil(moveX - hoverLeft + relativeX);
                } else if (dragType == 5) {
                    //右下角
                    nextWidth = Math.ceil(moveX - hoverLeft + relativeX);
                    nextHeight = Math.ceil(moveY - hoverTop + relativeY);
                } else if (dragType == 6) {
                    //下面
                    nextHeight = Math.ceil(moveY - hoverTop + relativeY);
                } else if (dragType == 7) {
                    //左下角
                    nextWidth = Math.ceil(hoverWidth + hoverLeft + relativeX - moveX);
                    nextHeight = Math.ceil(moveY - hoverTop + relativeY);
                    nextLeft = hoverLeft - (nextWidth - hoverWidth);
                } else if (dragType == 8) {
                    //左边
                    nextWidth = Math.ceil(hoverWidth + hoverLeft + relativeX - moveX);
                    nextLeft = hoverLeft - (nextWidth - hoverWidth);
                } else if (dragType == 9) {
                    //中间
                    nextLeft = moveX - relativeX;
                    nextTop = moveY - relativeY;
                }
                var nextRight = nextLeft + 0.0 + nextWidth;
                var nextBottom = nextTop + 0.0 + nextHeight;

                //alert(hoverRight + "  " + hoverBottom + ",  " + nextRight + "  " + nextBottom);
                //alert(nextWidth + "  " + hoverWidth + ",   " + nextLeft + "  " + hoverLeft);
                //alert(nextHeight + "  " + hoverHeight + ",   " + nextTop + "  " + hoverTop + "   " + moveY);
                //alert((nextWidth - hoverWidth) + "  " + (hoverLeft - nextLeft));
                //alert(nextLeft + "  " + nextTop);


                var bigImgObj = WaterMarker.container.find(".item-big-img");
                var imgLeft = bigImgObj.offset().left;
                var imgTop = bigImgObj.offset().top;
                var imgWidth = bigImgObj.width();
                var imgHeight = bigImgObj.height();

                var minSize = 10;
                if (nextWidth < minSize && nextHeight < minSize && dragType < 9)
                    return;

                //设置x
                if (nextLeft >= imgLeft && nextLeft <= imgLeft + imgWidth
                    && nextWidth >= minSize && nextLeft + nextWidth <= imgLeft + imgWidth) {
                    //这里设置不及时的。。。。。
                    hoverImgObj.offset({left: nextLeft});
                    hoverImgObj.width(nextWidth);
                    hoverResultLeft = nextLeft;
                    hoverResultWidth = nextWidth;
                } else {

                }
                //设置y
                if (nextTop >= imgTop && nextTop <= imgTop + imgHeight
                    && nextHeight >= minSize && nextTop + nextHeight <= imgTop + imgHeight) {
                    hoverImgObj.offset({top: nextTop});
                    hoverImgObj.height(nextHeight);
                    hoverResultTop = nextTop;
                    hoverResultHeight = nextHeight;
                }

                WaterMarker.hoverImg.showSmallMarkerIcon();


                /*alert("left: " + nextLeft + "  " + hoverImgObj.offset().left
                    + "   top: " + nextTop + "  " + hoverImgObj.offset().top
                    + "   width: " + nextWidth + "  " + hoverImgObj.width()
                    + "   height: " + nextHeight + "  " + hoverImgObj.height() );*/

            };

            hoverImgObj.mousemove(function(event) {
                if (isMouseDown == false) {
                    judgeDragType(event);
                    setHoverCursor();
                    return;
                }

            });


            //鼠标按下
            hoverImgObj.mousedown(function(event) {
                judgeDragType(event);
                hoverImgObj.css("border-width", "2px");
                //alert(relativeX + ",  " + relativeY + ",  " + dragType);
                isMouseDown = true;

                hoverResultLeft = hoverImgObj.offset().left;
                hoverResultTop = hoverImgObj.offset().top;
                hoverResultWidth = hoverImgObj.width();
                hoverResultHeight = hoverImgObj.height();

                setHoverCursor();

                $(document).unbind("mousemove");
                $(document).mousemove(function(event) {
                    moveCallback(event);
                });
            });
            //
            $(document).mouseup(function(event) {
                isMouseDown = false;
                $(document).unbind("mousemove");
                /*var borderWidthCss = hoverImgObj.css("border-width");
                borderWidthCss = borderWidthCss.replace("px", "0");
                var borderWidth = 0;
                try {
                    borderWidth = parseInt(borderWidthCss);
                    var left = hoverImgObj.offset().left;
                    var top = hoverImgObj.offset().top;
                    hoverImgObj.offset({left: left + borderWidth, top: top + borderWidth});
                } catch(e) {

                }*/
                hoverImgObj.css("border-width", "0px");
            });

        },
        showBigHoverImg: function(imgPath) {
            var selectItem = WaterMarker.container.find(".select-item");

            //测试时
            if (selectItem.length <= 0)
                return;
            var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");

            if (hoverImgObj.attr("isShowedBefore") == "true") {//是否之前就显示了的

            } else {
                var bigImgObj = WaterMarker.container.find(".item-big-img");

                hoverImgObj.attr("isShowedBefore", "true");
                hoverImgObj.css("left", bigImgObj.offset().left + "px");
                hoverImgObj.css("top", bigImgObj.offset().top + "px");
                hoverImgObj.css("width", hoverImgObj.attr("originWidth"));
                hoverImgObj.css("height", hoverImgObj.attr("originHeight"));
            }
            hoverImgObj.attr("src", imgPath);
            hoverImgObj.show();
            WaterMarker.hoverImg.showSmallMarkerIcon();
        },
        showSmallMarkerIcon: function() {
            //小图上面的marker
            var smallHoverImgObjs = WaterMarker.container.find(".small-hover-icon");
            if (smallHoverImgObjs.length == 0)
                return;

            var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");
            if (hoverImgObj.length == 0 || hoverImgObj.attr("isShowedBefore") != "true") {
                smallHoverImgObjs.hide();
                return;
            }
            smallHoverImgObjs.attr("src", hoverImgObj.attr("src"));

            //根据比例计算小图marker的位置和大小
            var bigImgObj = WaterMarker.container.find(".item-big-img");
            var smallImgObj = WaterMarker.container.find(".item-small-img");
            var bigImgWidth = bigImgObj.width();
            var bigImgHeight = bigImgObj.height();
            var smallImgWidth = smallImgObj.width();
            var smallImgHeight = smallImgObj.height();

            var relativeLeft = hoverImgObj.offset().left - bigImgObj.offset().left;
            var relativeTop = hoverImgObj.offset().top - bigImgObj.offset().top;

            var smallRelativeLeft = Math.ceil(relativeLeft * 1.0 * smallImgWidth / bigImgWidth);

            var smallRelativeTop = Math.ceil(relativeTop * 1.0 * smallImgHeight / bigImgHeight);
            var smallHoverWidth = Math.ceil(hoverImgObj.width() * 1.0 * smallImgWidth / bigImgWidth);
            var smallHoverHeight = Math.ceil(hoverImgObj.height() * 1.0 * smallImgHeight / bigImgHeight);


            smallHoverImgObjs.css("width", smallHoverWidth + "px");
            smallHoverImgObjs.css("height", smallHoverHeight + "px")
            smallHoverImgObjs.each(function() {
                var itemImgObj = $(this).parent().find(".item-small-img");
                //2是边框
                $(this).css("left", itemImgObj.offset().left + smallRelativeLeft + 2 + "px");
                $(this).css("top", itemImgObj.offset().top + smallRelativeTop + 2 + "px");
            });

            smallHoverImgObjs.show();

        }
    }, WaterMarker.hoverImg);


    /**
     * 水印的图片
     * @type {*}
     */
    WaterMarker.markerIcon = WaterMarker.markerIcon || {};
    WaterMarker.markerIcon = $.extend({
        init: function() {
            WaterMarker.container.find(".marker-type").click(function() {
                WaterMarker.container.find(".marker-type").removeClass("select-marker-type");
                WaterMarker.container.find(".marker-type").addClass("unselect-marker-type");
                $(this).removeClass("unselect-marker-type");
                $(this).addClass("select-marker-type");

                WaterMarker.markerIcon.show();
            });
            //第一次初始化载入
            WaterMarker.markerIcon.show();
        },
        show: function() {
            var markerType = WaterMarker.container.find(".select-marker-type").attr("markerType");
            //alert(markerType);
            WaterMarker.container.find(".marker-img-container").html("");
            var data = {};
            data.type = markerType;
            data.pn = 1;
            data.ps = 12;
            $.ajax({
                url : '/watermarker/showMarkerImgs',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var totalCount = dataJson.count;
                    var callback = function(currentPage, jq) {
                        data.pn = currentPage + 1;
                        WaterMarker.markerIcon.createMarkers(data);
                    }
                    WaterMarker.markerIcon.initPagination(totalCount, data.ps, callback);
                }
            });
        },
        initPagination: function(totalCount, per_page, callback){
            $(".pagingDiv").pagination(totalCount, {
                num_display_entries : 2, // 主体页数
                num_edge_entries : 1, // 边缘页数
                current_page: 0,
                callback : callback,
                items_per_page : per_page,// 每页显示多少项
                prev_text : "&lt上一页",
                next_text : "下一页&gt"
            });
        },
        createMarkers: function(data) {
            WaterMarker.container.find(".marker-img-container").html("");
            $.ajax({
                url : '/watermarker/showMarkerImgs',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var imgPathArray = dataJson.res;
                    var length = imgPathArray.length;

                    var rowNum = 4;
                    for (var i = 0; i < length; i += rowNum) {
                        var rowObj = $('<div class="marker-img-row clearfix"></div>');
                        for (var j = 0; j < rowNum && i + j < length; j++) {
                            var imgPath = imgPathArray[i + j];
                            var imgObj = $('<div class="marker-img-div"><img class="marker-img" /></div>');
                            imgObj.find(".marker-img").attr("src", imgPath);
                            rowObj.append(imgObj);

                        }
                        rowObj.append('<div class="blank0"></div>');
                        WaterMarker.container.find(".marker-img-container").append(rowObj);
                    }
                    //点击icon的事件
                    var imgObjs = WaterMarker.container.find(".marker-img");
                    imgObjs.click(function() {
                        imgObjs.removeClass("select-marker-img");
                        imgObjs.addClass("marker-img");
                        $(this).removeClass("marker-img");
                        $(this).addClass("select-marker-img");

                        WaterMarker.hoverImg.showBigHoverImg($(this).attr("src"));
                    });
                }
            });
        }
    }, WaterMarker.markerIcon);

    /**
     * 添加宝贝
     * @type {*}
     */
    WaterMarker.addItems = WaterMarker.addItems || {};
    WaterMarker.addItems = $.extend({
        doAddItems: function(numIids) {
            if (numIids === undefined || numIids == null || numIids == "") {
                alert("请先选择要添加的宝贝");
                return;
            }
            //关闭
            $("#itemChooseDivClose").click();
            //判断这个宝贝是否是在之前就已经是被选择了的
            var selectedNumIidArray = WaterMarker.util.getSelectedNumIidArray();
            //alert(selectedNumIidArray);
            var numIidArray = numIids.split(",");
            var targetNumIids = "";
            $(numIidArray).each(function(index, numIid) {
                if (numIid === undefined || numIid == null || numIid == "") {
                    //alert("null numIid");
                    return;
                }
                var isSelected = false;
                for (var i = 0; i < selectedNumIidArray.length; i++) {
                    if (selectedNumIidArray[i] == numIid) {
                        isSelected = true;
                        break;
                    }
                }
                if (isSelected == false) {
                    selectedNumIidArray[selectedNumIidArray.length] = numIid;
                    if (targetNumIids != "")
                        targetNumIids += ",";
                    targetNumIids += numIid;
                }
            });
            WaterMarker.util.setSelectedNumIidArray(selectedNumIidArray);
            var data = {};
            data.numIids = targetNumIids;
            $.ajax({
                url : '/watermarker/showItems',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    var itemJsonArray = dataJson;
                    var length = itemJsonArray.length;
                    var rowNum = 8;
                    var rowObjs = WaterMarker.container.find(".item-row-div");
                    var lastRowObj = null;
                    if (rowObjs.length > 0) {
                        lastRowObj = $(rowObjs.get(rowObjs.length - 1));
                    }

                    for (var i = 0; i < length; i++) {
                        var rowObj = null;
                        if (lastRowObj != null) {
                            var existObjs = lastRowObj.find(".item-desc-div");
                            if (existObjs.length >= rowNum) {

                            } else {
                                rowObj = lastRowObj;
                            }
                        }

                        if (rowObj == null) {
                            rowObj = $('<div class="item-row-div clearfix"></div>');
                            WaterMarker.container.find(".item-list-div").append(rowObj);
                        }
                        var itemJson = itemJsonArray[i];
                        var itemObj = WaterMarker.addItems.createOneItem(itemJson);
                        rowObj.append(itemObj);
                        lastRowObj = rowObj;

                    }
                    var itemObjs = WaterMarker.container.find(".item-small-img");
                    itemObjs.click(function() {
                        itemObjs.removeClass("select-item");
                        $(this).addClass("select-item");

                        WaterMarker.container.find(".item-big-img").attr("src", $(this).attr("src"));
                    });
                    if (WaterMarker.container.find(".select-item").length == 0) {
                        if (itemObjs.length > 0) {
                            $(itemObjs.get(0)).click();

                        }
                    }
                    //
                    WaterMarker.container.find(".select-marker-img").click();
                }
            });
            WaterMarker.util.judgeIsHasItemSelect();
        },
        createOneItem: function(itemJson) {
            var html = '<div class="item-desc-div">' +
                '<img class="item-small-img" />' +
                '<img class="small-hover-icon" />' +
                '<div class="item-check-div">' +
                '   <input class="item-check" type="checkbox" /><span class="item-check-span">删除宝贝</span>' +
                '</div>' +
                '</div>';

            var itemObj = $(html);
            itemObj.find(".item-small-img").attr("src", itemJson.picURL);
            itemObj.attr("numIid", itemJson.id);
            itemObj.find(".item-check-span").click(function() {
                if (confirm("确定要取消该宝贝？") == false)
                    return;
                WaterMarker.submit.deleteOneItem($(this).parents(".item-desc-div"));
            });
            //alert(itemJson.id);
            return itemObj;
        }
    }, WaterMarker.addItems);

    /**
     * 一些按钮事件，提交，删除等
     * @type {*}
     */
    WaterMarker.submit = WaterMarker.submit || {};
    WaterMarker.submit = $.extend({
        deleteItem: function(targetDivObj) {
            var numIid = targetDivObj.attr("numIid");
            var selectedNumIidArray = WaterMarker.util.getSelectedNumIidArray();
            var newArray = [];
            $(selectedNumIidArray).each(function(index, tempNumIid) {
                if (tempNumIid != numIid) {
                    //alert(tempNumIid + "  " + numIid);
                    newArray[newArray.length] = tempNumIid;
                }
            });
            WaterMarker.util.setSelectedNumIidArray(newArray);
            targetDivObj.remove();
        },
        //targetDivObj，整个item的div
        deleteOneItem: function(targetDivObj) {

            WaterMarker.submit.deleteItem(targetDivObj);

            WaterMarker.hoverImg.showSmallMarkerIcon();
            WaterMarker.util.judgeIsHasItemSelect();

        },
        deleteCheckedItems: function() {
            var targetDivObjs = WaterMarker.container.find(".item-check:checked").parents(".item-desc-div");
            //alert(targetDivObjs.length);
            if (targetDivObjs.length == 0) {
                alert("请先选择要取消的宝贝");
                return;
            }
            if (confirm("确定要取消这些宝贝？") == false)
                return;
            targetDivObjs.each(function() {
                WaterMarker.submit.deleteItem($(this));
            });

            WaterMarker.hoverImg.showSmallMarkerIcon();
            WaterMarker.util.judgeIsHasItemSelect();
        },
        deleteAllWaterMark: function() {
            if (confirm("确定删除水印？") == false) {
                return;
            }
            var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");
            hoverImgObj.hide();
            hoverImgObj.attr("isShowedBefore", "false");

            WaterMarker.hoverImg.showSmallMarkerIcon();
        },
        submitWaterMarker: function() {
            var smallImgObj = WaterMarker.container.find(".item-small-img");
            if (smallImgObj.length == 0) {
                alert("亲，您尚未选择宝贝");
            }
            var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");
            if (hoverImgObj.attr("isShowedBefore") != "true") {
                alert("亲，您尚未添加水印");
                return;
            }

            /*var recordDialog = WaterMarker.init.recordDialog;
            if (recordDialog === undefined || recordDialog == null) {
                var html = '' +
                    '<div class="watermarker-record-div">' +
                    '   <span>填写备份状态名称：</span>' +
                    '   <input class="record-name" /> ' +
                    '</div>' +
                    '' +
                    '';

                recordDialog = $(html);
                recordDialog.dialog({
                    modal: true,
                    bgiframe: true,
                    height:180,
                    width:400,
                    title:'备份原有宝贝图片',
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var recordName = recordDialog.find(".record-name").val();
                        WaterMarker.submit.doCreateImgs(recordName);
                        $(this).dialog('close');
                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });

                WaterMarker.init.recordDialog = recordDialog;
            }

            var theDate = new Date();
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }

            var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            recordDialog.find(".record-name").val("水印大师" + timeStr);
            recordDialog.dialog("open");*/


            WaterMarker.submit.doCreateImgs();

        },
        doCreateImgs: function() {
            var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");
            var bigImgObj = WaterMarker.container.find(".item-big-img");

            var data = {};
            //宝贝集合
            var selectedNumIidArray = WaterMarker.util.getSelectedNumIidArray();
            var numIids = "";
            $(selectedNumIidArray).each(function(index, numIid) {
                if (numIid == null || numIid == "")
                    return;
                if (numIids != "")
                    numIids += ",";
                numIids += numIid;
            });
            data.numIids = numIids;
            //
            data.iconPath = hoverImgObj.attr("src");
            //alert(data.iconPath);
            //位置
            data.posX = hoverImgObj.offset().left - bigImgObj.offset().left;
            data.posY = hoverImgObj.offset().top - bigImgObj.offset().top;
            data.mainWidth = bigImgObj.width();
            data.mainHeight = bigImgObj.height();
            data.hoverWidth = hoverImgObj.width();
            data.hoverHeight = hoverImgObj.height();

            $.ajax({
                url : '/watermarker/generateWaterMarks',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (dataJson.message === undefined || dataJson.message == null || dataJson.message == "") {

                    } else {
                        alert(dataJson.message);
                    }
                    if (dataJson.success == false) {

                    } else {
                        //刷新，不刷新的话，因为后台的主图变成的有水印的，前台还是原来的，这样如果卖家再对这些宝贝生成一次水印，就会出问题
                        location.reload();
                        //window.location.target = "_blank" ;
                        //window.location.href = "/watermarker/downloadWaterMarker";
                    }
                }
            });
        },
        //还原备份
        returnBackItemImages: function(url, numIids) {
            /*if (recordId === undefined || recordId == null || recordId == "") {
                alert("请先选择要还原的备份");
                return;
            }
            if (confirm("确定要还原到" + recordName + "的状态？") == false)
                return;*/
            if (confirm("确定要还原宝贝图片？") == false)
                return;
            var data = {};
            if (numIids === undefined || numIids == null) {

            } else
                data.numIids = numIids;
            $.ajax({
                url : url,
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (dataJson.message === undefined || dataJson.message == null || dataJson.message == "") {

                    } else {
                        alert(dataJson.message);
                    }
                    if (dataJson.success == false) {

                    } else {
                        //刷新，不刷新的话，因为后台的主图变成的有水印的，前台还是原来的，这样如果卖家再对这些宝贝生成一次水印，就会出问题
                        location.reload();
                    }
                }
            });
        }
    }, WaterMarker.submit);



    WaterMarker.util = WaterMarker.util || {};
    WaterMarker.util = $.extend({
        selectedNumIidArray: [],
        getSelectedNumIidArray: function() {
            var selectedNumIidArray = WaterMarker.util.selectedNumIidArray;
            if (selectedNumIidArray === undefined || selectedNumIidArray == null) {
                selectedNumIidArray = [];
                WaterMarker.util.selectedNumIidArray = [];
            }
            return selectedNumIidArray;
        },
        setSelectedNumIidArray: function(selectedNumIidArray) {
            if (selectedNumIidArray === undefined || selectedNumIidArray == null) {
                selectedNumIidArray = [];
            }
            WaterMarker.util.selectedNumIidArray = selectedNumIidArray;
        },
        judgeIsHasItemSelect: function() {
            var selectedNumIidArray = WaterMarker.util.getSelectedNumIidArray();
            //alert(selectedNumIidArray.length);
            if (selectedNumIidArray.length == 0) {
                WaterMarker.container.find(".hide-with-no-item").hide();
                WaterMarker.container.find(".show-with-no-item").show();
                var hoverImgObj = WaterMarker.container.find(".hover-marker-icon");
                hoverImgObj.hide();
            } else {
                WaterMarker.container.find(".hide-with-no-item").show();
                WaterMarker.container.find(".show-with-no-item").hide();
            }

            //测试时
            //WaterMarker.container.find(".hide-with-no-item").show();
            //WaterMarker.container.find(".show-with-no-item").hide();
        }
    }, WaterMarker.util);

})(jQuery, window));