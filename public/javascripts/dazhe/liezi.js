// main code
window.MZ_CONSTANT = {};
window.MZ_CONSTANT.USER_LEVEL_0 = 1; // 免费
window.MZ_CONSTANT.USER_LEVEL_1 = 10; // 标准
window.MZ_CONSTANT.USER_LEVEL_2 = 20; // 加强

$(document).ready(function() {

    if($("body").height() < $(window).height()) {
        $("body").css("min-height", $(window).height());
    }

    /** general classes **/
    function isIE8() {
        return $.browser.msie && $.browser.version == 8;
    }
    function isIE6or7() {
        return $.browser.msie && ($.browser.version == 6 || $.browser.version == 7);
    }

    $("h1.toggleable a").click(function() {
        var a = $(this);
        var h = a.parent();
        var div = h.next();

        var loaded = a.attr("data-loaded");
        if(typeof loaded !== 'undefined' && loaded === 'false') { // 没有数据，不展开
            return false;
        }

        h.toggleClass("expanded");
        h.addClass("toggling");
        div.addClass("toggleingDiv");
        h.next().slideToggle(400, function() {
            h.removeClass("toggling");
            div.removeClass("toggleingDiv");
        });
    });

    $("[data-tipsytext]").tipsy({
        gravity: function() {
            var d = $(this).data("tipsy_direction");
            if(typeof d === "undefined") d = 's';
            return d;
        },
        html: function() {
            var h = $(this).data("tipsy_ishtml");
            if(typeof h === "undefined" || h != true) h = false;
            else h = true;

            return h;
        },
        live: true
    });

    if($("#lastDayModal").length > 0) {
        loadModal("#lastDayModal");
    }

    $("input[type='text']").live("keyup", function() {
        CtoH(this);
    });

    /* KO custom bidings */
    ko.bindingHandlers.changeEffect = {
        init: function(element, valueAccessor, allBindingAccessor) {
            var visible = allBindingAccessor().visible;
            var element = $(element);
            if(visible) element.show();
            else element.hide();
            element.data("changeEffectCount", 1);
        },
        update: function(element, valueAccessor, allBindingAccessor) {
            var visible = allBindingAccessor().visible;
            var element = $(element);
            var count = element.data("changeEffectCount");
            element.data("changeEffectCount", count+1);
            if(count%2 !== 0 || !visible) {
                return false;
            }
            var value = valueAccessor();
            var duration = 400;
            var element = $(element);
            if(value === 'flick') { // 闪烁效果
                element.fadeOut(duration).fadeIn(duration);
            }
            var count = element.data("changeEffectCount");
        }
    }

    /* DiscountItemModelView */
    var DiscountItemModelView = function(item) {
        var _this = this;

        /* 辅助函数 */
        this._updateDiscountFields = function(value, inputType) {
            if(_this.discountType() === 'D') {
                _this.focusPosition(0);
                _this.zhekou(value);
                _this.jianjia( parseFloat((Math.floor((_this.price-value*_this.price/10)*100+0.00001)/100).toFixed(2)) );
                _this.zhehoujia( parseFloat((_this.price-_this.jianjia()).toFixed(2)) );
            }
            else if(_this.discountType() === 'P') {
                if(inputType !== 'zhehoujia') {
                    _this.focusPosition(1);
                    _this.jianjia( value );
                    _this.zhehoujia( parseFloat((_this.price - value).toFixed(2)) );
                    _this.zhekou( parseFloat((Math.floor(_this.zhehoujia()/_this.price*10*100+0.00001)/100).toFixed(2)) );
                }
                else {
                    _this.focusPosition(2);
                    _this.zhehoujia( value );
                    _this.jianjia( parseFloat((_this.price - value).toFixed(2)) );
                    _this.zhekou( parseFloat((Math.floor(_this.zhehoujia()/_this.price*10*100+0.00001)/100).toFixed(2)) );
                }
            }
            _this.updateMinMaxPrice();
            if(_this.isChanged()) _this.dirty(true);
        };
        this._checkDiscountValid = function() {
            var zhekou = $.trim(_this.zhekou());
            var jianjia = $.trim(_this.jianjia());
            var zhehoujia = $.trim(_this.zhehoujia());
            var min_price = $.trim(_this.min_price_after());
            if(zhekou == "" || jianjia == "" || zhehoujia == "") {
                _this.isDiscountValid(false);
                _this.discountErrorMsg('折扣不能为空');
            }
            else if(!zhekou || isNaN(zhekou)) {
                _this.isDiscountValid(false);
                if(zhekou == "NaN") {
                    _this.zhekou('请修改');
                }
                if(jianjia == "NaN") {
                    _this.jianjia('请修改');
                }
                if(zhehoujia == "NaN") {
                    _this.zhehoujia('请修改');
                }
                _this.discountErrorMsg('折扣格式错误');
            }
            else if(zhekou < 0.01) {
                _this.discountErrorMsg('折扣不能小于0.01折');
                _this.isDiscountValid(false);
            }
            else if(zhekou > 10) {
                _this.discountErrorMsg('折扣不能大于10折');
                _this.isDiscountValid(false);
            }
            else if(_this.isMultiplePrice && (isNaN(min_price) || min_price <= 0)) {
                _this.focusPosition(2);
                _this.discountErrorMsg('宝贝最低价打折以后不能小于0（现在是<strong>'+ parseFloat(min_price) +'</strong>)');
                _this.isDiscountValid(false);
            }
            else {
                _this.isDiscountValid(true);
            }
            // update threshold
            if(zhekou <= 1) _this.isBelowThreshold = true;
            else _this.isBelowThreshold = false;
        };
        this.updateMinMaxPrice = function() {
            if(_this.discountType() === 'D') {
                var value = _this.zhekou();
                _this.min_price_after( parseFloat(( Math.floor(_this.min_price*value/10.0*100+0.0001)/100 ).toFixed(2)) );
                _this.max_price_after( parseFloat(( Math.floor(_this.max_price*value/10.0*100+0.0001)/100 ).toFixed(2)) );
            }
            else {
                var value = _this.jianjia();
                _this.min_price_after( parseFloat((_this.min_price - value).toFixed(2)) );
                _this.max_price_after( parseFloat((_this.max_price - value).toFixed(2)) );
            }
        };

        this.pic_url = item.pic_url;
        this.num_iid = item.num_iid;
        this.title = item.title;
        this.price = +item.price;
        this.outer_id = item.outer_id
        this.product_id = item.product_id
        this.min_price = +item.min_price || 0;
        this.max_price = +item.max_price || 0;
        this.is_new = item.is_new;
        this.isBelowThreshold = false;

        // fenxiao
        this.cost_price = +item.cost_price || 0;
        this.dealer_cost_price = +item.dealer_cost_price || 0;
        this.retail_price_low = +item.retail_price_low || 0;

        this.act_link = '';
        if(typeof item.act_id !== 'undefined') this.act_link = '/huodong/zhekou-detail-' + item.act_id;

        this.old_d_type = item.old_d_type || 'D';
        if (this.old_d_type == 'D'){
            this.old_d_value = +item.old_d_value || 1000;
        }else{
            this.old_d_value = +item.old_d_value || 0;
        }

        this.price = parseFloat(this.price.toFixed(2));
        this.min_price = parseFloat(this.min_price.toFixed(2));
        this.max_price = parseFloat(this.max_price.toFixed(2));

        this.isMultiplePrice = (this.max_price > 0);
        this.isFenXiaoItem = (this.cost_price > 0);
        this.min_price_after = ko.observable(this.min_price);
        this.max_price_after = ko.observable(this.max_price);

        /* 折扣部分 */
        this.isDiscountValid = ko.observable(true);
        this.discountErrorMsg = ko.observable('');
        this.focusPosition = ko.observable(-1); // 现在在改那个input, 0:打折 1:减价 2:折后价

        this.discountType = ko.observable(item.d_type || 'D');
        this.dirty = ko.observable(false);
        this.discountType.subscribe(function() {
            _this.updateMinMaxPrice();
            if(_this.isChanged()) _this.dirty(true);
        });


        this.discountValue = function() {
            if(_this.discountType() == 'D') return parseInt((+_this.zhekou()+0.00001)*100);
            else return parseInt((+_this.jianjia()+0.00001)*100);
        };

        this.isChanged = function() {
            if (_this.old_d_type == _this.discountType() && _this.old_d_value == _this.discountValue()) return false;
            else return true;
        }

        this.zhekou = ko.observable(10);
        this.jianjia = ko.observable(0);
        this.zhehoujia = ko.observable(+item.price);

        if(typeof item.d_value !== "undefined") {
            this._updateDiscountFields(parseFloat(item.d_value/100.0));
            this._checkDiscountValid();
        }

        this.zhekouInput = ko.computed({
            read: function() {
                return _this.zhekou();
            },
            write: function(value) {
                _this.discountType('D');
                _this._updateDiscountFields(value);
                _this._checkDiscountValid();
            }
        });
        this.jianjiaInput = ko.computed({
            read: function() {
                return _this.jianjia();
            },
            write: function(value) {
                _this.discountType('P');
                if(value < 0) value = -value;
                _this._updateDiscountFields(value);
                _this._checkDiscountValid();
            }
        });
        this.zhehoujiaInput = ko.computed({
            read: function() {
                return _this.zhehoujia();
            },
            write: function(value) {
                _this.discountType('P');
                _this._updateDiscountFields(value, 'zhehoujia');
                _this._checkDiscountValid();
            }
        });

        this.multiplePriceDiscountMsg = ko.computed(function() {
            if(_this.discountType() === 'D') {
                return '每个价格<strong>打'+_this.zhekou()+'折</strong>：<br>'+_this.min_price_after()+' ~ '+_this.max_price_after()+' 元';
            }
            else if(_this.discountType() === 'P') {
                return '每个价格<strong>减'+_this.jianjia()+'元</strong>：<br>'+_this.min_price_after()+' ~ '+_this.max_price_after()+' 元';
            }
        }).extend({throttle: 50});
        this.multiplePriceDiscountText = ko.computed(function(a, b) {
            if(_this.discountType() === 'D') {
                return '打折模式';
            }
            else if(_this.discountType() === 'P') {
                return '减价模式';
            }
        }).extend({throttle: 50});
    }
    /* end of DiscountItemModelView */

    /* menu */

    /*** style */
    setTimeout(function() {
        $(".topMenu .subMenu").each(function() {
            var sub = $(this);
            var parent = sub.parent();
            var top = parent.offset().top + parent.height();
            var width = parent.width() - 2;

            sub.css({
                "width": width,
                "top": top
            });
        });
    }, 100);
    /*** effect */
    $("li.lv1").hover(
        function() {
            var li = $(this);
            var submenu = $(".subMenu", li);
            if(submenu.length > 0) {
                submenu.slideDown(100);
            }
        },
        function() {
            var li = $(this);
            var submenu = $(".subMenu", li);
            if(submenu.length > 0) {
                submenu.hide();
            }
        }
    );
    $("li.lv2").hover(
        function() {
            var li = $(this);
            var submenu = $(".intro", li);
            if(submenu.length > 0 && !isIE6or7()) {
                submenu.show();
            }
        },
        function() {
            var li = $(this);
            var submenu = $(".intro", li);
            if(submenu.length > 0 && !isIE6or7()) {
                submenu.hide();
            }
        }
    );
    $("#header li.lv2 a").click(function() {
        var a = $(this);
        var href = a.attr("href");
        if(href[0] === '/') {
            setTimeout(function() {
                location.href = href;
            }, 3000);
        }
    });
    // update menu style
    var bodyId = $("body").attr("id");
    if(bodyId === 'mixlistPage') {
        $("#mainMenu li.lv1").eq(0).find("a:eq(0)").addClass("active");
    }
    else if(bodyId.indexOf('actStep') !== -1 || bodyId.indexOf('mjsStep') !== -1) {
        $("#mainMenu li.lv1").eq(1).find("a:eq(0)").addClass("active");
    }
    else if(bodyId === 'zkzqPage') {
        $("#mainMenu li.lv1").eq(2).find("a:eq(0)").addClass("active");
    }
    else if(bodyId === 'promoToolsPage') {
        //$("#mainMenu li.lv1").eq(3).find("a:eq(0)").addClass("active");
    }
    else if(bodyId === 'postfeePage') {
        $("#mainMenu li.lv1").eq(3).find("a:eq(0)").addClass("active");
    }
    else if(bodyId === 'upgradePage') {
        $("#rightMenu .upgradeLink").addClass("active");
    }
    else if(bodyId === 'subUserPage' || bodyId === 'userInfoPage') {
        $("#rightMenu .userMenu").addClass("active");
    }
    else if(bodyId === 'invitePage') {
        $("#rightMenu .inviteLink").addClass("active");
    }
    /* end of menu */

    /* general */
    $("input.default, textarea.default").live("focus", function() {
        var input = $(this);
        setInputDefault(input);
    });
    $("input.withoutDefault, textarea.withoutDefault").live("blur", function() {
        var input = $(this);
        setInputWithoutDefault(input);
    });
    var setInputDefault = function(input) {
        if(typeof input.data("defaultText") === "undefined") input.data("defaultText", input.val());
        input.val("");
        input.removeClass("default").addClass("withoutDefault");
    }
    var setInputWithoutDefault = function(input) {
        if($.trim(input.val()) === "") {
            input.val(input.data("defaultText"));
            input.removeClass("withoutDefault").addClass("default");
        }
    }

    $("img.lazy").lazyload();

    /* gotop/bottom buttons */
    var positionScrollButtons = function() {
        var windowWidth = $(window).width();
        var content = $("#content");
        var contentRight = content.offset().left + content.width();
        // position wangwang
        if(windowWidth > 1060) {
            $("#kefuButton").css({
                left: contentRight + 10,
                right: 'auto'
            });
        }
        else {
            $("#kefuButton").css({
                left: 'auto',
                right: 0
            });
        }
        // position scroll
        var sb = $("#scrollButtons");
        if(sb.length > 0 && $("#content").length > 0) {
            $("a", sb).hide();
            sb.css({
                left: contentRight + 10,
                right: 'auto',
                top: 'auto',
                bottom: 10
            }).show();
            if(sb.offset().left + 42 > windowWidth) {
                sb.css({
                    left: 'auto',
                    right: 0
                })
            }
            var footer = $("#footer");
            if(footer.offset().top + footer.height() < $(window).height()) {
                sb.css({
                    top: footer.offset().top + footer.height() - 130,
                    bottom: 'auto'
                });
            }
        }
    }
    var updateScrollButtonsVisibility = function() {
        if($(document).height() - $(window).scrollTop() > $(window).height()) {
            $("#goBottomButton").show();
        }
        else {
            $("#goBottomButton").hide();
        }
        if($(window).scrollTop() > 10) {
            $("#goTopButton").show();
        }
        else {
            $("#goTopButton").hide();
        }
        $("#kefuButton").show();
    }
    $("#goBottomButton").click(function() {
        $( 'html, body' ).animate( {
            scrollTop: $(document).height()
        }, 'fast' );
    });
    $("#goTopButton").click(function() {
        $( 'html, body' ).animate( {
            scrollTop: 0
        }, 'fast' );
    });
    positionScrollButtons();
    updateScrollButtonsVisibility();
    $(window).scroll(function() {
        updateScrollButtonsVisibility();
    });
    $(window).resize(function() {
        positionScrollButtons();
        updateScrollButtonsVisibility();
    });

    /* end of general */

    /* alert modal */
    $("#alertModal .grayBtn").live("click", function() {
        return false;
    });
    /* end of alert modal */

    $('#hraBox .orangeBtn').click(function(){
        $("#hraBox").fadeOut();
    })

    $(".openUpgradeModal").live("click", function() {
        closeModal();
        loadModal("#upgradeModal", $(this));
    });

    if( bodyId === 'loginPage' ) {
        (function() {
        })();
    }

    /* #mixlistPage */
    if( bodyId === "mixlistPage" ) {
        (function() {

            $("#bookmarkme").show();
            $('#bookmarkme').live("click", function() {
                if (window.sidebar && window.sidebar.addPanel) { // Mozilla Firefox Bookmark
                    window.sidebar.addPanel('美折', window.location.href, '');
                } else if(window.external && window.external.AddFavorite) { // IE Favorite
                    window.external.AddFavorite(location.href, '美折');
                } else if(window.opera && window.print) { // Opera Hotlist
                    this.title='美折';
                    return true;
                } else { // webkit - safari/chrome
                    alert('请点击 ' + (navigator.userAgent.toLowerCase().indexOf('mac') != - 1 ? 'Command/Cmd' : 'CTRL') + ' + D 来收藏美折.');
                }
            });

            var status = $("#tableFilterStatus").val();
            if(status === 'stopped' || status === 'deleted') {
                var trs = $(".act-tr");
                for(var i=0; i<10; i++) {
                    trs.eq(i).show();
                }
                if(trs.length > 10) {
                    $("#moreActWrapper").show();
                }
            }
            $("#moreActBtn").click(function() {
                $(".act-tr:not(:visible)").show("highlight", {color: '#F1FCF3'}, 4000);
                $(this).parent().hide();
            });

            $(".search input.text").keyup(function(e) {
                if(e.keyCode === 13) $(".searchBtn").trigger("click");
            });

            /*** effect */
            $(".td .buttons .more").hover(
                function() {
                    var div = $(this);
                    var submenu = $(".otherBtns", div);
                    if(submenu.length > 0) {

                        var xplus = 4;
                        if($(".filter a[href='index-stopped']").hasClass("active")) {
                            xplus = 4;
                        } // stop page
                        //if(isIE8()) xplus = 10;

                        var x = $(window).width() - ( div.parent().offset().left + div.parent().width() ) + xplus;
                        var y = div.offset().top + div.height();
                        submenu.css({"right": x, "top": y, "width": "auto", "height": "auto"})
                            .slideDown(50);
                    }
                },
                function() {
                    var div = $(this);
                    var submenu = $(".otherBtns", div);
                    if(submenu.length > 0) {
                        submenu.slideUp(50);
                    }
                }
            );

            /* search button */
            $(".searchBtn").click(function() {
                var a = $(this);
                a.text('搜索中...');
                var input = a.parent().find("input.text");
                var q = input.val();
                var id = getQueryString(q, 'id');
                if(id !== '') q = id;

                window.location.href = 'zhekou-edit-all?q=' + q;
            });


            /* clear mjs tags */
            var clearTime = {};
            $(".clearLeftBtn").click(function() {
                var btn = $(this);
                var act_id = getParent(btn, '.act-tr').find('.hidden_act_id').val();
                if(clearTime[act_id]) {
                    var diff = (new Date()) - clearTime[act_id];
                    if(diff < 1000*60*5) {
                        alert('清理中...');
                        return false;
                    }
                }
                $.post('mjs-remove-tags', {aid: act_id}, function(res) {
                    if(res.success) {
                        alert(res.msg);
                        clearTime[act_id] = new Date();
                    }
                    else {
                        alert(res.msg);
                    }
                }, 'json');
            });

            /* finish act instant box */
            $(".finishBtn").click(function(e) {
                var btn = $(this);
                var box = $("#instantConfirmBox");
                var x = e.pageX - 105;
                var y = e.pageY - 75;
                box.data('action','finish');
                var act_id = getParent(btn, '.act-tr').find('.hidden_act_id').val();
                var act_type = getParent(btn, '.act-tr').find('.hidden_g_act_type').val();
                box.data('act_id',act_id);
                box.data('act_type',act_type);
                box.css({'left': x, "top": y})
                    .show("scale", {}, 200);
            });
            /* delete from list button */
            $(".removeFromListBtn").click(function(e) {
                var btn = $(this);
                var box = $("#instantConfirmBox");
                var x = e.pageX - 105;
                var y = e.pageY - 75;
                box.data('action','deleteFromList');
                box.find('.html').text('确定要永久删除该活动？');
                box.find(".cancel").text("不删除");
                box.find(".confirm").text("删除");
                var act_id = getParent(btn, '.act-tr').find('.hidden_act_id').val();
                var act_type = getParent(btn, '.act-tr').find('.hidden_g_act_type').val();
                box.data('act_id',act_id);
                box.data('act_type',act_type);
                box.css({'left': x, "top": y})
                    .show("scale", {}, 200);
            });
            $("#instantConfirmBox .cancel").click(function() {
                $("#instantConfirmBox").hide("fade", {}, 200);
            });

            $("#instantConfirmBox .confirm").click(function() {
                var box = $("#instantConfirmBox");
                var id = box.data('act_id');
                var act_type = box.data('act_type');
                box.hide("fade", {}, 200);
                if (box.data('action') === 'finish') {

                    var stoplink = $(".tabs a[href='index-stopped']");
                    var x = stoplink.offset().left;
                    var y = stoplink.offset().top;

                    $.post(act_type+'-delete',{'id':id},function(ret){
                        var tr = $("#tr-"+id);
                        var title = $(".title .text", tr);
                        stoplink.addClass("processing");
                        title.css({
                            position: 'absolute',
                            left: title.offset().left,
                            top: title.offset().top
                        }).animate({
                                left: x,
                                top: y,
                                width: '100px'
                            }, "slow", function() {
                                stoplink.removeClass("processing");
                            });
                        tr.animate({
                            opacity: 0
                        }, "slow", function() {
                            $(this).remove();
                            checkListLength();
                        });
                    },'json');
                }
                else if(box.data('action') === 'deleteFromList') {
                    var tr = $("#tr-"+id);
                    $.post('remove-'+id, {}, function(ret) {
                        if (ret.success){
                            tr.hide("drop", {}, 500, function() {
                                $(this).remove();
                                checkListLength();
                            });
                        }else{
                            alert(ret.msg);
                        }
                    });
                }
            });


            /* edit act modal */
            $(".editActBtn").click(function() {
                var btn = $(this);
                var act_tr = getParent(btn, '.act-tr');
                if(btn.parent().hasClass("otherBtns")) modalSource = btn.parent().parent();
                else modalSource = btn;

                if(user_level == MZ_CONSTANT.USER_LEVEL_0) {
                    loadModal("#upgradeModal");
                    return false;
                }

                var modal = $('#editActModal');

                $('.act_name',modal).val($('.hidden_name',act_tr).val());
                $('.act_started',modal).text($('.hidden_started',act_tr).val());
                $('.act_ended',modal).val($('.hidden_ended',act_tr).val());
                $(".act_fd_value",modal).val(+$('.fd_value',act_tr).val()/100);
                modal.data("aid", $(".hidden_act_id", act_tr).val());
                var act_type = $(".hidden_f_act_type", act_tr).val();
                modal.data("act_type", act_type);
                var title = $('.hidden_title',act_tr).val();
                $('#editActModal .tmLine').jqTransSelectRemove($("#title"));
                $("#editActModal .tmLine").jqTransform();
                if(act_type === 'fzhekou') {
                    $("#editActModal .fzhekouLine").show();
                }
                else {
                    $("#editActModal .fzhekouLine").hide();
                }
                loadModal("#editActModal", modalSource);
            });
            $("#editActModal .submitBtn").click(function(e) {
                var btn = $(this);
                var modal = $("#editActModal");
                var data = {};
                data['name'] = $('.act_name',modal).val();
                data['ended'] = $('.act_ended',modal).val();

                var title = $('#title').val();
                if(title == -1) {
                    title = $("#custom_title").val();
                }
                data['title'] = title;
                data['aid'] = modal.data("aid");
                if(modal.data("act_type") === 'fzhekou') {
                    data['fd_value'] = Math.round($(".act_fd_value", modal).val()*100);
                }

                $.post('zhekou-edit-info', data, function(res) {
                    if(!res.success) {
                        if (res.code == -37){
                            loadModal("#hraBox", null, e);
                        }else{
                            if(res.code == 1000) { // for 高级版
                                closeModal();
                                loadModal("#upgradeModal");
                            }
                            else {
                                alert(res.msg);
                            }
                        }
                    }
                    else {
                        $("#editActModal").hide();
                        $("#alertModal .title").html("修改活动信息");
                        $("#alertModal .html").text("修改成功");
                        loadModal("#alertModal");
                        $("#alertModal").data("refreshPage", true);
                    }
                },'json');
            });
            $("#alertModal .closeModal").click(function() {
                if($("#alertModal").data("refreshPage")) {
                    setTimeout(function() {
                        window.location.href = window.location.href;
                    }, 500);
                }
            });
            $("#editActModal .tmLine").jqTransform();
            $("#title").change(function(){
                if ($("#title").val() === "-1"){
                    $('#custom_title').show();
                }else{
                    $('#custom_title').hide();
                }
            });
            $('#end_time').datetimepicker();


            var checkListLength = function() {
                var len = $(".bigTable .table .act-tr").length;
                if(len === 0) {
                    $(".grayMessage").show();
                    $(".appendButtons").hide();
                }
                else {
                    $(".grayMessage").hide();
                    $(".appendButtons").show();
                }
            }

            checkListLength();

            $(".shareBtn").click(function() {
                var btn = $(this);
                btn.parent().slideUp(50);
                var act_tr = getParent($(this), '.act-tr');
                var aid = $(".hidden_act_id", act_tr).val();
                $.post('weibo-'+aid, function(res) {
                    if(res.success) {
                        var weibo = res.weibo;
                        weibo.msg = weibo.msg + ' #美折#';
                        var title = $('.hidden_name', act_tr).val();
                        var modal = $("#shareActModal");
                        $(".title span", modal).text(title);
                        $("textarea", modal).val(weibo.msg);
                        updateJiathisConfig(weibo);
                        loadModal("#shareActModal", btn.parent().parent());
                    }
                    else {
                        alert(res.msg);
                    }
                }, 'json');
            });


        })();
    }
    /* end of #mixlistPage */


    /* act step1 */
    if( bodyId === "actStep1Page" || bodyId === "mjsStep1Page") {
        (function() {

            var pagetype;
            if (bodyId === "mjsStep1Page") pagetype = 'mjs';
            else pagetype = 'zhekou';

            $(".tmLine").jqTransform();

            $("#xiangouLine .jqTransformSelectWrapper ul a:eq(1)").attr("data-tipsytext", "每一个订单中，每一种商品的第一件优惠")
                .attr("data-tipsy_direction", "e")
            $("#xiangouLine .jqTransformSelectWrapper ul a:eq(2)").attr("data-tipsytext", "每一个顾客，一种打折商品只优惠一件")
                .attr("data-tipsy_direction", "e")

            $("#title").change(function(){
                if ($("#title").val() === "-1"){
                    $('#custom_title').show()
                        .parent().find("a.tinyBtn").hide();
                }else{
                    $('#custom_title').hide()
                        .parent().find("a.tinyBtn").show();
                }
                checkCustomTitle();
            });

            $("#buy_limit").change(function() {
                if ($("#buy_limit").val() === "-1"){
                    $('#custom_buy_limit').parent().show()
                        .parent().find("a.tinyBtn").hide();
                }else{
                    $('#custom_buy_limit').parent().hide()
                        .parent().find("a.tinyBtn").show();
                }
                checkCustomBuyLimit();
            });

            $("#custom_title_wrapper a.tinyBtn, #custom_buy_limit_wrapper a.tinyBtn").click(function() {
                var a = $(this);
                var line = getParent(a, ".tmLine");
                $(".jqTransformSelectWrapper li a", line).last().trigger("click");
                var input = $("input.text", line);
                input.focus();
            });

            function updateTimeDiff() {

                var start  = $('#start_time').datepicker('getDate');
                var end  = $('#end_time').datepicker('getDate');
                var diff = end.getTime()-start.getTime();
                var parents = $("#end_time").parentsUntil(".tmLine");
                var $input = $(parents[parents.length-1]).parent().find(".hint");

                diff = (diff-diff%60000)/60000;
                var t = diff%60;
                $("em:eq(2)", $input).text(t);
                diff = (diff-t)/60;
                t = diff%24;
                $("em:eq(1)", $input).text(t);
                diff = (diff-t)/24;
                $("em:eq(0)", $input).text(diff);
                $("#end_time").parentsUntil(".tmLine").parent().find(".okMsg").show();
                $("#end_time").parentsUntil(".tmLine").parent().find(".errorMsg").hide();
            }
            $("#start_time").datetimepicker({
                minDate: new Date(),
                onClose: function(input, inst) {
                    var mindate = $('#start_time').datepicker('getDate');
                    var dateString = Date.parse($("#end_time").val().replace(/-/g, "/"));
                    var enddate = new Date(dateString);
                    $('#end_time').datetimepicker('destroy');
                    $('#end_time').datetimepicker({
                        minDate: mindate,
                        onClose: function(input, inst) {
                            updateTimeDiff();
                        }
                    });

                    var day = mindate.getTime()-mindate.getTime()%(1000*60*60*24)+7*1000*60*60*24-(1000*60*60*6) + Math.floor(Math.random()*60*60*4*1000);
                    mindate.setTime(day);
                    if( mindate.getTime() > enddate.getTime() ) {
                        $('#end_time').datetimepicker('setDate', mindate);
                    }
                    updateTimeDiff();
                    checkTime();
                }
            });
            $('#end_time').datetimepicker({
                onClose: function(input, inst) {
                    updateTimeDiff();
                    checkTime();
                }
            });


            // validate functions
            var addAppendError = function(line, text) {
                var append = $(".append", line);
                append.html(text)
                    .addClass("error");
            }
            var removeAppendError = function(line) {
                var append = $(".append", line);
                append.html(append.data("normal"))
                    .removeClass("error");
            }
            var checkName = function() {
                var input = $("#name");
                var val = $.trim(input.val());
                if(val.length < 2 || val.length > 30) {
                    var text = '2到30个汉字(现在长度:' + val.length + ')';
                    input.addClass("error");
                    addAppendError(input.parent(), text);
                }
                else {
                    input.removeClass("error");
                    removeAppendError(input.parent());
                }
            }
            var checkCustomTitle = function() {
                var input = $("#custom_title");
                if(!input.is(":visible")) {
                    input.removeClass("error");
                    removeAppendError(input.parent().parent());
                    return false;
                }

                var val = $.trim(input.val());
                if(val.length < 2 || val.length > 5) {
                    var text = '2到5个汉字(现在长度:' + val.length + ')';
                    input.addClass("error");
                    addAppendError(input.parent().parent(), text);
                }
                else {
                    input.removeClass("error");
                    removeAppendError(input.parent().parent());
                }
            }
            var checkCustomBuyLimit = function() {
                var input = $("#custom_buy_limit");
                if(!input.parent().is(":visible")) {
                    input.removeClass("error");
                    removeAppendError(input.parent().parent().parent());
                    return false;
                }

                var val = $.trim(input.val());
                if(isNaN(val) || +val < 1 || +val > 100 || Math.round(+val) !== +val) {
                    var text = '请输入大于0小于100的整数';
                    input.addClass("error");
                    addAppendError(input.parent().parent().parent(), text);
                }
                else {
                    input.removeClass("error");
                    removeAppendError(input.parent().parent().parent());
                }
            }
            var checkTime = function() {
                var sdate = parseDate($("#start_time").val()+":00");
                var edate = parseDate($("#end_time").val()+":00");

                var input = $("#start_time");
                if(!sdate || isNaN(sdate.getTime())) {
                    var text = '开始时间格式不正确';
                    input.addClass("error");
                    addAppendError(input.parent(), text);
                }
                else {
                    input.removeClass("error");
                    removeAppendError(input.parent());
                }

                var input = $("#end_time");
                if(!edate || isNaN(edate.getTime())) {
                    var text = '结束时间格式不正确';
                    input.addClass("error");
                    addAppendError(input.parent(), text);
                }
                else if(edate.getTime() < sdate.getTime()) {
                    var text = '结束时间要大于开始时间';
                    input.addClass("error");
                    addAppendError(input.parent(), text);
                }
                else {
                    input.removeClass("error");
                    removeAppendError(input.parent());
                }
            }
            var checkDiscount = function() {
                var input = $("#discount_full");
                var val = +$.trim(input.val());
                if(isNaN(val) || val < 0.1 || val >= 10) {
                    var text = '折扣应是大于0小于10的数字';
                    input.addClass("error");
                    addAppendError(input.parent(), text);
                }
                else {
                    input.removeClass("error");
                    removeAppendError(input.parent());
                }
            }
            $("#name").keyup(function() {
                checkName();
            });
            $("#name").blur(function() {
                checkName();
            });
            $("#discount_full").keyup(function() {
                checkDiscount();
            });
            $("#start_time").keyup(function() {
                checkTime();
            });
            $("#end_time").keyup(function() {
                checkTime();
            });
            $("#custom_title").keyup(function() {
                checkCustomTitle();
            });
            $("#custom_title").blur(function() {
                checkCustomTitle();
            });
            $("#custom_buy_limit").keyup(function() {
                checkCustomBuyLimit();
            });

            // initialize
            $(".append").each(function() {
                $(this).data("normal", $(this).html());
            });
            checkName();
            checkTime();
            updateTimeDiff();
            checkCustomTitle();
            checkCustomBuyLimit();
            if($('#discount_full').length>0) {
                checkDiscount();
            }

            $("#xiangouLine").hover(
                function() {
                    $("#xiangouMask").show();
                },
                function() {
                    $("#xiangouMask").hide();
                }
            );

            // start request control
            $('.nextStepBtn').click(function(e) {
                var btn = $(this);
                var data = {};
                data['name'] = $('#name').val();
                data['title'] = $('#title').val();
                if (data["title"] === "-1") data['title'] = $('#custom_title').val();
                data['started'] = $('#start_time').val();
                data['ended'] = $('#end_time').val();
                data['scids'] = $('#scids').val();
                data['tag'] = $('#tag').val();
                data['buy_limit'] = $("#buy_limit").val();
                if(data['buy_limit'] == -1) data['buy_limit'] = $("#custom_buy_limit").val();
                // validate
                if($(".error").length > 0) {
                    alert("请先修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 7, distance: 30}, 1500);
                    }, 300);
                    return false;
                }

                // full create check
                if ($('#discount_full').length>0) {
                    if(btn.hasClass("grayBtn")) return false;

                    var value = +$('#discount_full').val();
                    data['fd_value'] = parseInt((value+0.00001)*100);
                    if (!confirm('确认创建全店打'+value+"折活动?")){
                        return false;
                    }
                    // submit
                    btn.data("text", btn.text())
                        .removeClass("greenBtn")
                        .addClass("grayBtn")
                        .text("正在提交...");
                    $.post('zhekou-post-f', data, function(res){
                        if (res.success){
                            window.location.href='zhekou-4';
                        }else{
                            if (res.code == -37){
                                loadModal('#hraBox', btn, e);
                            }else{
                                alert(res.msg);
                            }
                            btn.removeClass("grayBtn")
                                .addClass("greenBtn")
                                .text(btn.data("text"));
                        }
                    },'json');
                }else{
                    $.post(pagetype+'-post-1', data, function(res){
                        if (res.success){
                            window.location.href=pagetype+'-2';
                        }else{
                            alert(res.msg);
                        }
                    },'json');
                }
                return false;
            });
            // end request control
        })();
    }
    /* end of act step1  */

    /* act step2 */
    if(bodyId === "actStep2Page" || bodyId === "addItemPage"
        || bodyId === 'mjsStep2Page' || bodyId === 'mjsAddItemPage' || bodyId === 'postfeePage') {
        (function() {

            var pagetype, search_url;
            if (bodyId === 'mjsStep2Page' || bodyId === 'mjsAddItemPage'){
                pagetype = 'mjs';
                if (bodyId === 'mjsAddItemPage') search_url = 'mjs-search-' + $('#hidden_act_id').val()
                else search_url = 'mjs-search'
            }
            else if (bodyId === "actStep2Page" || bodyId === "addItemPage"){
                pagetype = 'zhekou';
                search_url = 'zhekou-search'
            }else if (bodyId === 'postfeePage'){
                pagetype = 'postfee';
                search_url = 'postfee-search';
            }else{
                pagetype = 'zhekou';
                search_url = 'zhekou-search'
            }

            // load selected ids first
            var selectedItems = [];
            var dirty = false;
            var hideDisabled = false;
            var hideDisabledCount = 0;
            var pageSize = 20;
            var orderBy = 'modified:desc';
            var left_count = +$('#left_count').val();

            var currentActId = $('#hidden_act_id').val();
            var viewType = 'list';
            if($.cookie("gridView") === 'true') {
                var a = $(".gridMode");
                $(".mode a").removeClass("selected");
                a.addClass("selected");
                $(".productsGrid").removeClass("listView").addClass("gridView");
                viewType = 'grid';
            }

            $("#pageSize").change(function() {
                var val = $("#pageSize option:selected").val();
                pageSize = val;
                search();
            });

            var addToSelectedItems = function (id) {
                var existed = removeFromSelectedItems(id);
                selectedItems.push(id);
                checkLimited();
                return !existed;
            }

            var removeFromSelectedItems = function (id) {
                for (var i=0; i<selectedItems.length;i++){
                    if (selectedItems[i] == id){
                        selectedItems.splice(i,1);
                        return true;
                    }
                }
                return false;
            }

            var checkSelectItem = function(id){
                for (var i=selectedItems.length-1;i>=0;i--){
                    if (id == selectedItems[i]) return true;
                }
                return false;
            }
            var checkLimited = function() {
                if(left_count - selectedItems.length <= 0) {
                    $(".product:visible").not(".selected").not(".disabled").not(".disabled2").addClass("limited");
                }
                else {
                    $(".product:visible").removeClass("limited");
                }
            }

            var updateSelectCount = function(){
                $('.productsGrid .tabs .selectedTab .select_count').text(selectedItems.length);
                $('#left_show').text(+$('#left_count').val() - selectedItems.length);
                if(bodyId === 'postfeePage') $("#summary").trigger("change");
                checkLimited();
            }

            /* product grid control */
            $(".productsGrid .product").live({
                mouseenter: function() {
                    if(!$(this).hasClass("disabled2") && !$(this).hasClass("disabled") && !$(this).hasClass("selected")&& !$(this).hasClass("limited")) {
                        $(this).addClass("hover");
                    }
                },
                mouseleave: function() {
                    $(this).removeClass("hover");
                }
            });
            $(".gridView .product.limited a.image, .listView .product .m5").live("click", function(e) {
                if(window.user_level == MZ_CONSTANT.USER_LEVEL_0) {
                    $("#upgradeModal .title").text('高级版用户可选更多商品');
                    loadModal("#upgradeModal", $(this));
                }
            });
            $(".gridView .product a.image, .listView .product a.productBtn").live("click", function(e) {
                var p = getParent($(this), ".product");
                if(!p.hasClass("disabled2") && !p.hasClass("disabled") && !p.hasClass("limited") && !p.hasClass("virtual")) {
                    p.toggleClass("selected");
                    p.removeClass("hover");

                    if (p.hasClass('selected')) {
                        var tooltip = $("#topTooltip");
                        tooltip.stop(true,true);
                        var x = e.pageX - 10;
                        var y = $(window).height() - e.pageY + 6;
                        $(".html", tooltip).html("您还可以选择"+ (+$("#left_show").text()-1) + "件商品喔" );
                        tooltip.css({ left: x, bottom: y })
                            .show()
                            .animate(
                            {'bottom': y+100},
                            3000,
                            function() {
                                tooltip.hide();
                            }
                        );
                    }

                    var item_id = +p.children('.num_iid').val();
                    var change_ret = false;
                    if (p.hasClass('selected')){
                        // add to list
                        change_ret = addToSelectedItems(item_id);
                    }else{
                        // delete to list
                        change_ret = removeFromSelectedItems(item_id);
                        // select all button
                        $('.selectPageAll').removeClass('active_all').text('本页全选');
                    }
                    if (change_ret) dirty = true;
                    updateSelectCount();
                }
            });
            /* end of product grid control */

            var appendProductItem_discount = function(item){
                if(item.act_id && hideDisabled) {
                    hideDisabledCount++;
                    return false;
                }
                if (item.is_virtual && hideDisabled){
                    hideDisabledCount++;
                    return false;
                }
                var p = $('.product.hidden-template').clone();
                p.removeClass('hidden-template')
                if(viewType === 'list') {
                    $('img', p).attr('src',item['pic_url']+'_60x60.jpg');
                }
                else {
                    $('img', p).attr('src',item['pic_url']+'_180x180.jpg');
                }
                $('img', p).data("grid-img", item['pic_url']+'_180x180.jpg');
                $('img', p).data("list-img", item['pic_url']+'_60x60.jpg');
                $('.name', p).text(item.title);
                $('.name', p).attr('href','http://item.taobao.com/item.htm?id='+item.num_iid);
                $('.price', p).children('i').text(item.price);
                $('.num_iid', p).val(item.num_iid);
                p.hide().appendTo(".products").fadeIn("2000");
                if (item.act_id){
                    removeFromSelectedItems(item.num_iid);
                    if (currentActId && item.act_id == currentActId)
                        p.addClass('disabled2');
                    else {
                        p.addClass('disabled');
                        $(".itemInAct", p).attr("href", "/huodong/zhekou-edit-all?q="+item.num_iid);
                    }
                }else
                if (checkSelectItem(item.num_iid)) p.addClass('selected');
                else if (item.is_virtual) p.addClass('virtual');

                if(item.has_mjs_act) {
                    $(".status_red", p).show()
                }
                else {
                    $(".status_red", p).hide()
                }
                if(item.approve_status == 'instock') { // 已下架
                    $(".status_gray", p).show();
                }
                else {
                    $(".status_gray", p).hide();
                }

                // 智能推荐
                if(item.reason) {
                    $(".status_red", p).text(item.reason).show();
                    $("a.image", p).attr("data-ga", 'true')
                        .attr("data-ga-name", 'popular_recommend_products');
                }
            }

            var postfee_types = {};
            var addFastSelectButtons = function() {
                $("#fastSelects a:not(.selectPageAll)").remove();
                for(var t in postfee_types) {
                    var a = $('<a href="javascript:;" class="notSort grayBtn lastBtn"></a>');
                    a.text('全选"' + postfee_types[t] + '"').data("pid", t);
                    $("#fastSelects").append(a);
                }
            }
            $("#fastSelects a").live("click", function() {
                var a = $(this);
                var pid = a.data("pid");
                var isSelected = a.hasClass("selected");
                if(isSelected) { // cancel
                    $('.products .product.selected .pid'+pid).each(function(){
                        var p = getParent($(this), ".product");
                        var num_iid = $(".num_iid", p).val();
                        if (p.hasClass('selected')){
                            // remove from list
                            p.removeClass('selected')
                            removeFromSelectedItems(num_iid);
                        }
                    });
                }
                else { // select
                    $('.products .product .pid'+pid).each(function(){
                        var p = getParent($(this), ".product");
                        var num_iid = $(".num_iid", p).val();
                        if (!p.hasClass('selected') && !p.hasClass("disabled") && !p.hasClass("disabled2") && !p.hasClass("limited") && !p.hasClass("virtual")){
                            // add to list
                            p.addClass('selected')
                            addToSelectedItems(num_iid);
                        }
                    });
                }
                var t = a.text();
                if(!isSelected) {
                    a.addClass("selected");
                    t = t.replace(/全选/, "取消");
                    a.text(t);
                }
                else {
                    a.removeClass("selected");
                    t = t.replace(/取消/, "全选");
                    a.text(t);
                }
                dirty = true;
                updateSelectCount()
            });
            var appendProductItem_postFee = function(item) {

                if(item.act_id && hideDisabled) {
                    hideDisabledCount++;
                    return false;
                }
                var p = $('.product.hidden-template').clone();
                p.removeClass('hidden-template');
                if(viewType === 'list') {
                    $('img', p).attr('src',item['pic_url']+'_60x60.jpg');
                }
                else {
                    $('img', p).attr('src',item['pic_url']+'_180x180.jpg');
                }
                $('img', p).data("grid-img", item['pic_url']+'_180x180.jpg');
                $('img', p).data("list-img", item['pic_url']+'_60x60.jpg');
                $('.name', p).text(item.title);
                $('.name', p).attr('href','http://item.taobao.com/item.htm?id='+item.num_iid);

                $('.price', p).children('i').text(item.price);

                $('.num_iid', p).val(item.num_iid);
                p.hide().appendTo(".products").fadeIn();
                if(item.postage_id) {
                    $('.status_gray',p).hide();
                    var name = postage_template[''+item.postage_id];
                    if(getLength(name) > 24) name = getSubstr(name, 23) + '...';
                    $('.status_blue',p).text(name).
                        addClass("pid"+item.postage_id).show();
                    postfee_types[item.postage_id] = name;
                }
                else {
                    var name = (item.express_fee + '').replace(/\./g, '');
                    $('.status_gray', p).text('快递'+item.express_fee+'元')
                        .addClass("pid"+name).show();
                    $('.status_blue',p).hide();
                    postfee_types[name] = '快递'+item.express_fee+'元';
                }

//            p.addClass('disabled2');
                if (checkSelectItem(item.num_iid)) p.addClass('selected');
            }

            var appendProductItem = appendProductItem_discount;
            if(bodyId === 'postfeePage') appendProductItem = appendProductItem_postFee;


            var search = function() {
                var productsDiv = $('.products');
                productsDiv.css("height", productsDiv.height());
                $(".mainMsg").hide();
                $(".loading").show();
                productsDiv.children().remove();

                var search_data = {}
                var storeCat = $('#selectStoreCategory').val();
                var taobaoCat = $('#selectTaobaoCategory').val();
                var isShowcase = $('.isShowcase').hasClass("isShowcaseYes");
                var page_no = $('.now:eq(0)').text();

                // type get
                if ($('.productsGrid .tabs .onSaleTab').hasClass('active')) type = 0;
                else type = 1;
                search_data['type'] = type;

                var q = '';
                if ($('#searchInput').hasClass('withoutDefault')){
                    q = $('#searchInput').val();
                    var id = getQueryString(q, 'id');
                    if(id !== '') q = id;
                    search_data['q'] = q;
                }

                if (storeCat) search_data['scids'] = storeCat;
                if (taobaoCat) search_data['cid'] = taobaoCat;
                if (isShowcase) search_data['has_showcase'] = true;
                search_data['page_no'] = page_no;
                search_data['order_by'] = orderBy;
                search_data['page_size'] = pageSize;

                $.post(search_url,search_data,function(res) {
                    if (!res.success) {
                        alert(res.msg);
                        $(".loading").hide();
                        return;
                    }
                    hideDisabledCount = 0;
                    var products = res.res;

                    postfee_types = {};
                    for (var i in products){
                        appendProductItem(products[i]);
                    }
                    productsDiv.append($('<div class="clear"></div>'));
                    productsDiv.css("height", "auto");

                    // page no
                    $('.paging span .now').text(res.page_no);
                    $('.paging span .total').text(res.total_pages);
                    if(res.page_no == 1) $(".prevPage").hide();
                    else $(".prevPage").show();
                    if(res.page_no == res.total_pages) $(".nextPage").hide();
                    else $(".nextPage").show();
                    if(res.total_pages <= 1) $(".paging .pagingInside").hide();
                    else $(".paging .pagingInside").show();

                    // hide number
                    if(hideDisabled) {
                        $(".hideDisabledProducts").text("显示不可选宝贝(已隐藏"+ hideDisabledCount +"件)")
                            .attr("data-tipsytext", "显示本页已经参加其他活动的宝贝")
                            .data("tipsytext", "显示本页已经参加其他活动的宝贝")
                    }
                    else {
                        $(".hideDisabledProducts").text("隐藏不可选宝贝")
                            .attr("data-tipsytext", "隐藏本页已经参加其他活动的宝贝")
                            .data("tipsytext", "隐藏本页已经参加其他活动的宝贝")
                    }

                    $(".loading").hide();
                    if(hideDisabledCount === pageSize) {
                        $(".noItem").show();
                    }
                    else if(products.length === 0) {
                        $(".noItem2").show();
                    }

                    // select all
                    $('.selectPageAll').removeClass('active_all').text('本页全选');

                    checkLimited();

                    if(bodyId === 'postfeePage') addFastSelectButtons();

                },'json');
            }

            $('#selectStoreCategory').change(function(){
                $('#selectTaobaoCategory option:eq(0)').prop('selected',true);
                $('#searchInput').removeClass('withoutDefault').addClass('default').val('关键字、商品链接、商品编码');
                $('.paging span .now').text(1);
                search();
            });

            $('#selectTaobaoCategory').change(function(){
                $('#selectStoreCategory option:eq(0)').prop('selected',true);
                $('#searchInput').removeClass('withoutDefault').addClass('default').val('关键字、商品链接、商品编码');
                $('.paging span .now').text(1);
                search();
            });

            $(".isShowcase").click(function() {
                var a = $(this);
                a.toggleClass("isShowcaseYes");
                $('.paging span .now').text(1);
                search();
            });
            $('.nextPage,.prevPage').click(function(){
                var page_no = +$('.now:eq(0)').text();
                var total_page = +$('.total:eq(0)').text();
                if ($(this).hasClass('prevPage')) page_no--;
                else page_no++;
                if (page_no == 0){
                    alert('您已经在第一页了!');
                    return;
                }else if (page_no> total_page){
                    alert('您已经在最后一页了!');
                    return;
                }
                $('.now').text(page_no);
                search()
            });
            $('.searchBtn').click(function(){
                $('#selectTaobaoCategory option:eq(0)').prop('selected',true);
                $('#selectStoreCategory option:eq(0)').prop('selected',true);
                $('.paging span .now').text(1);

                recordByGA('ButtonClick', 'search_'+searchType, bodyId);
                search();
            });
            $(".toSomePageBtn").click(function() {
                var btn = $(this);
                var b = $(".toSomePageBtn");
                var idx = b.index(btn);
                var total_page = +$('.total').eq(idx).text();
                var page_no = +$(".toSomePage").eq(idx).val();
                if(isNaN(page_no) || page_no % 1 !== 0) {
                    alert('页码无效');
                }
                else if(page_no <= 0 || page_no > total_page) {
                    alert('页码要在1到'+total_page+'之间');
                }
                else {
                    $(".now").text(page_no);
                    search();
                }
            });
            $(".toSomePage").keyup(function(e) {
                if(e.keyCode === 13) { // 回车
                    $("a.toSomePageBtn", $(this).parent()).trigger("click");
                }
            });

            $('.nextBtn').click(function(e) {
                var btn = $(this);
                if(selectedItems.length === 0) {
                    alert("您还没有选择任何商品");
                    return false;
                }
                if(bodyId === 'mjsStep2Page' && selectedItems.length < 2) {
                    alert("淘宝要求一个满就送活动最少添加2个宝贝，您可以再添加1个无关紧要的宝贝，比如“邮费”商品，即可进入下一步");
                    return false;
                }
                var next_url = $(this).attr('href');
                if(bodyId === 'addItemPage') {
                    next_url += '?fr=additem';
                }
                if(bodyId === 'mjsAddItemPage') next_url = 'mjs-edit-ok';
                $.post(pagetype+'-post-2',{'ids':selectedItems.join(',')},function(res){
                    if (res.success){
                        if (bodyId === 'mjsAddItemPage') {
                            $.post(window.location,{'t':Math.random()},function(res){
                                if (!res.success){
                                    if (res.code == -37){
                                        loadModal('#hraBox', btn, e);
                                    }else{
                                        alert(res.msg);
                                    }
                                }else{
                                    window.location.href = next_url
                                }
                            },'json');

                        } else {
                            window.location.href = next_url
                        }
                    } else {
                        alert(res.msg);
                    }
                },'json')
                return false;
            });

            // inline product search
            var searchType = 'single';
            $("#searchInput").keyup(function(e) {
                searchType = 'single';
                $("#batchSearchPop textarea").val("");
                if(e.keyCode === 13) {
                    $(this).blur();
                    $(".searchBtn").trigger("click");
                }
            });
            $("#batchSearchPop textarea").keyup(function(e) {
                searchType = 'batch';
                if(e.ctrlKey && (e.keyCode === 13 || e.keyCode == 10)) {
                    $(this).blur();
                    $(".searchBtn").trigger("click");
                }
            });

            $("#searchInput").focus(function() {
                var input = $(this);
                var left = input.offset().left;
                var top = input.offset().top + 32;
                $("#batchSearchPop").css({
                    top: top,
                    left: left
                }).slideDown(200);
                input.data("focus", 1);
            });
            $(window).resize(function() {
                var input = $("#searchInput");
                var left = input.offset().left;
                var top = input.offset().top + 32;
                $("#batchSearchPop").css({
                    top: top,
                    left: left
                });
            });
            $("#batchSearchPop textarea").focus(function() {
                $(this).data("focus", 1);
            });
            $("#batchSearchPop textarea").blur(function() {
                var textarea = $(this);
                var input = $("#searchInput");
                textarea.data("focus", 0);
                setTimeout(function() {
                    if(!input.data("focus")) $("#batchSearchPop").slideUp(200);
                }, 50);
            });
            $("#searchInput").blur(function() {
                var input = $(this);
                input.data("focus", 0);
                setTimeout(function() {
                    var textarea = $("#batchSearchPop textarea");
                    if(!textarea.data("focus")) $("#batchSearchPop").slideUp(200);
                }, 50);
            });
            $("#batchSearchPop textarea").keyup(function() {
                var pop = $("#batchSearchPop");
                var textarea = $(this);
                var ids = textarea.val();
                ids = ids.split(/(\s|,|，)/);
                for(var i=0; i<ids.length; i++) {
                    ids[i] = $.trim(ids[i]);
                    if(ids[i] === "" || ids[i] === ',' || ids[i] === '，') {
                        ids.splice(i, 1);
                        i--;
                    }
                }
                if(ids.length > 0) $("span", pop).text("("+ ids.length +"/20)");
                else $("span", pop).text("");
                var q = ids.join(',');
                pop.data("q", q);

                var input = $("#searchInput");
                setInputDefault(input);
                input.val(q);
                setInputWithoutDefault(input);
            });

            /* submit for postfee page */
            $("#postfeeBlock .filter .tabs a").click(function() {
                var a = $(this);
                var links = $("a", a.parent());
                links.removeClass("active");
                a.addClass("active");
                $("#postfeeBlock .tmpls").hide().eq(links.index(a)).show();
                $("#summary").trigger("change");
            });
            $("#postfeeBlock .tmpl").click(function() {
                $("#postfeeBlock .tmpl").removeClass("active");
                $(this).addClass("active");
                $("#summary").trigger("change");
            });
            $(".updatePostfeeBtn").click(function() {
                if(selectedItems.length === 0) {
                    alert("您还没有选择任何商品");
                    return false;
                }
                var type = $("#postfeeBlock .filter a.active");
                type = $("#postfeeBlock .filter a").index(type);
                var data = {};
                if(type === 0) {
                    if($("#postfeeBlock .tmpls a.active").length === 0) {
                        alert("您还没有选择邮费模版");
                        return false;
                    }
                    data['postage_id'] = +$("#postfeeBlock .tmpls").eq(0).find("a.active").attr("id").substr("5");
                }
                else if(type === 1) {
                    if($("#postfeeBlock .tmpls input.error").length > 0) {
                        alert("请先修正邮费错误再提交");
                        return false;
                    }
                    data['postage_id'] = 0;
                    data['post_fee'] = +$("#post_fee_input").val();
                    data['express_fee'] = $("#express_fee_input").val();
                    data['ems_fee'] = $("#ems_fee_input").val();
                }
                $.post('postfee-create', data, function(res) {
                    if(res.success) {
                        window.location.href = 'postfee-info';
                    }
                    else {
                        alert(res.msg);
                    }
                }, 'json');
            });
            $("#postfeeBlock .formTmpls input.text").keyup(function() {
                var input = $(this);
                var val = input.val();
                if(isNaN(val)) input.addClass("error");
                else input.removeClass("error");
                $("#summary").trigger("change");
            });
            $("#summary").bind("change", function() {
                var summary = $(this);
                var count = $(".productsGrid .select_count").text();
                $("strong", summary).eq(0).html(count);
                var type = $("#postfeeBlock .filter a.active");
                type = $("#postfeeBlock .filter a").index(type);
                $(".tmplText", summary).hide();
                if(type === 0) {
                    var tmpl = $("#postfeeBlock .tmpls").eq(0).find("a.active").text();
                    $(".tmplText", summary).eq(0).find("strong").text(tmpl);
                }
                else if(type === 1) {
                    var span = $(".tmplText", summary).eq(1);
                    $('.post_fee_span', span).text($("#post_fee_input").val());
                    $('.express_fee_span', span).text($("#express_fee_input").val());
                    $('.ems_fee_span', span).text($("#ems_fee_input").val());
                }
                $(".tmplText", summary).eq(type).show();
            });
            /* end of submit for postfee page */


            var submit_iids = function() {
                if(dirty){
                    $.post(pagetype+'-post-2',{'ids':selectedItems.join(',')},function(res){
                        dirty = false;
                    },'json')
                }
            }

            var clearSearchArgs = function(){
                $(".mainMsg").hide();
                $('#selectTaobaoCategory option:eq(0)').prop('selected',true);
                $('#selectStoreCategory option:eq(0)').prop('selected',true);
                $('#searchInput').removeClass('withoutDefault').addClass('default').val('关键字、商品链接、商品编码');
                $('.isShowcase').removeClass('isShowcaseYes');
                $('.paging span .now').text(1);
                $(".productFilter, .hideDisabledProducts, .selection, .selection .sort, .selection .paging").show();
            }

            var loadSelected = function(page_no){
                $.post(pagetype+'-selected-detail',{page_no:page_no},function(res){
                    $(".mainMsg").hide();
                    items = res.selected;
                    for (var i=0;i<items.length;i++){
                        appendProductItem(items[i]);
                    }
                    if (!res.no_more){
                        loadSelected(page_no+1)
                    }else{
                        if(items.length === 0) {
                            $(".noItem2").show();
                        }
                        $('.products').append($('<div class="clear"></div>'));
                    }
                },'json')
            }
            var loadRecommend = function() {
                $.post('/huodong/recommend-products', {t: Math.random()}, function(res) {
                    $(".mainMsg").hide();
                    if(res.success) {
                        var items = res.res;
                        for (var i=0;i<items.length;i++){
                            appendProductItem(items[i]);
                        }
                        if(res.res.length === 0) { // only one page
                            $(".noItem2").show();
                        }
                        $('.products').append($('<div class="clear"></div>'));
                    }
                }, 'json');
            }

            $('.productsGrid .tabs .onSaleTab, .productsGrid .tabs .notOnSaleTab').click(function(){
                $('.productsGrid .tabs').children().removeClass('active');
                $(this).addClass('active');
                clearSearchArgs();
                search();
            });
            $(".productsGrid .tabs .recommendTab").click(function() {
                clearSearchArgs();
                $(".loading").show();
                $('.productsGrid .tabs').children().removeClass('active');
                $(this).addClass('active');
                $(".productFilter, .hideDisabledProducts, .selection .sort, .selection .paging").hide();
                $('.products').empty();
                if(user_level == MZ_CONSTANT.USER_LEVEL_0) {
                    $(".selection").hide();
                    $(".mainMsg").hide();
                    $(".upgradeMsg").show();
                    return false;
                }
                loadRecommend(); // only for paid user
            });

            $('.productsGrid .tabs .selectedTab').click(function(){
                clearSearchArgs();
                $(".loading").show();
                $('.productsGrid .tabs').children().removeClass('active');
                $(this).addClass('active');
                $('.products').empty();
                $(".productFilter, .hideDisabledProducts, .selection .sort, .selection .paging").hide();
                loadSelected(1);
            });

            $.post(pagetype+'-selected',{'t': Math.random()},function(res) {
                selectedItems = res.selected;
                updateSelectCount();
                search();
                setInterval(submit_iids,3000);
            },'json');

            // select page all
            $('.selectPageAll').click(function() {
                if (!$(this).hasClass('active_all')) {
                    $(this).text('取消全选');
                    $('.products .product .num_iid').each(function(){
                        var p = $(this).parent();
                        if (!p.hasClass('selected') && !p.hasClass("disabled") && !p.hasClass("disabled2") && !p.hasClass("limited") && !p.hasClass("virtual")){
                            // add to list
                            p.addClass('selected')
                            addToSelectedItems($(this).val());
                        }
                    });
                } else {
                    $(this).text('本页全选');
                    $('.products .product .num_iid').each(function(){
                        var p = $(this).parent();
                        if (p.hasClass('selected')){
                            // remove from list
                            p.removeClass('selected')
                            removeFromSelectedItems($(this).val());
                        }
                    })
                }
                dirty = true;
                updateSelectCount()
                $(this).toggleClass('active_all');
            });

            $(".hideDisabledProducts").click(function() {
                var btn = $(this);
                if(!hideDisabled) {
                    hideDisabled = true;
                    search();
                }
                else {
                    hideDisabled = false;
                    search();
                }
            });

            $(".sort a").click(function() {
                var a = $(this);
                if(a.hasClass("notSort")) return false;
                var name = a.data("name");
                if(a.hasClass("ascBtn")) { // active asc -> desc
                    a.removeClass("ascBtn");
                    orderBy = name + ":desc";
                }
                else if(a.hasClass("lightAscBtn")) { // inactive asc -> asc
                    $("a", a.parent()).removeClass("grayBtn").removeClass("ascBtn").addClass("lightGrayBtn");
                    a.removeClass("lightAscBtn").addClass("grayBtn").addClass("ascBtn");
                    orderBy = name + ":asc";
                }
                else if(a.hasClass("grayBtn")) { // active desc -> asc
                    a.addClass("ascBtn");
                    orderBy = name + ":asc";
                }
                else if(a.hasClass("lightGrayBtn")) { // inactive desc -> desc
                    $("a", a.parent()).removeClass("grayBtn").removeClass("ascBtn").addClass("lightGrayBtn");
                    a.removeClass("lightGrayBtn").addClass("grayBtn");
                    orderBy = name + ":desc";
                }
                search();
            });

            $(".mode a").click(function() {
                var a = $(this);
                if(a.hasClass("selected")) return false;
                $(".products").hide();
                $(".mode a").removeClass("selected");
                a.addClass("selected");
                var productsDiv = $(".productsGrid");
                if(a.hasClass("gridMode")) {
                    productsDiv.removeClass("listView").addClass("gridView");
                    $(".product a.image img").each(function() {
                        var img = $(this);
                        img.attr("src", img.data("grid-img"));
                    });
                    $.cookie("gridView", true);
                    viewType = 'grid';
                }
                else {
                    productsDiv.removeClass("gridView").addClass("listView");
                    $(".product a.image img").each(function() {
                        var img = $(this);
                        img.attr("src", img.data("list-img"));
                    });
                    $.cookie("gridView", false);
                    viewType = 'list';
                }
                $(".products").fadeIn();
            });


        })();
    }
    /* end of act step2  */

    /* act step3 */
    if(bodyId === "actStep3Page" || bodyId === "editPage") {
        (function() {

            var buy_limit = +$("#buy_limit").val();

            $("input.onlyDecrease").tipsy({
                gravity: 's',
                fallback: function() {
                    return "限购1件时，只能设置减价";
                },
                delayIn: 200,
                live: true
            });

            $("input.autoAdjust").live("keyup", function() {
                var input = $(this);
                if(input.val().length > 7) {
                    input.addClass("small");
                }
                else if(input.val().length <= 5) {
                    input.addClass("big");
                }
                else if(input.val().length <= 7) {
                    input.removeClass("small")
                        .removeClass("big");
                }

            });

            $(".batchOperation input.text").focus(function() {
                $(this).parent().find(".buttons").slideDown(50);
                $(".batchOperation").addClass("highlightLine");
            });
            $(".batchOperation input.text").blur(function() {
                var input = $(this);
                setTimeout(function() {
                    input.parent().find(".buttons").slideUp(50, function() {
                        if($(".batchOperation .buttons:visible").length === 0) {
                            $(".batchOperation").removeClass("highlightLine");
                        }
                    });
                }, 300);
            });
            $(".batchOperation .buttons a.cancel").click(function() {
                $(this).parent().slideUp(50, function() {
                    if($(".batchOperation .buttons:visible").length === 0) {
                        $(".batchOperation").removeClass("highlightLine");
                    }
                });
            });
            $(".batchOperation .buttons a.confirm").click(function() {
                $(".batchOperation .buttons").slideUp(50, function() {
                    $(".batchOperation").removeClass("highlightLine");
                });
            });
            $(".product .discount input.text").live("focus", function() {
                var block = getParent($(this), ".product");
                var fxInfo = block.find(".fxInfo");

                if(!fxInfo.hasClass("valid")) return false;

                $(".products .fxInfo").not(fxInfo).hide();
                fxInfo.slideDown(50);
                block.addClass("highlightLine");
            });
            $(".fxInfo").live({
                mouseenter: function() {
                    $(this).data("hover", 1);
                },
                mouseleave: function() {
                    var fx = $(this);
                    fx.data("hover", 0);
                }
            });
            $(".product").live({
                mouseenter: function() {
                },
                mouseleave: function() {
                    var block = $(this);
                    var fx = block.find(".fxInfo");
                    if(!block.hasClass("highlightLine") && !fx.data("hover")) fx.slideUp(50);
                }
            });
            $(".product .discount input.text").live("blur", function() {
                var block = getParent($(this), ".product");
                block.removeClass("highlightLine");
                setTimeout(function() {
                    var fx = block.find(".fxInfo");
                    if(!block.hasClass("highlightLine") && !fx.data("hover")) fx.slideUp(50);
                }, 50);
            });

            var load_current_url = 'zhekou-current';
            if ($('#hidden_act_id').length > 0){
                load_current_url += '-' + $('#hidden_act_id').val();
            }


            /* PageViewModel */
            var PageViewModel = function() {

                var _this = this;

                /* 辅助函数 */
                this._getItems = function() {
                    var items = [];
                    var ret = [];
                    $.ajax({
                        url: load_current_url,
                        type: 'post',
                        async: false,
                        dataType: 'json',
                        success: function(res) {
                            items = res.items
                        }
                    });
                    for(var i=0,max=items.length; i<max; i++) {
                        ret.push(new DiscountItemModelView(items[i]));
                    }
                    return ret;
                }
                /* end of 辅助函数 */


                this.onlyDecrease = (buy_limit === 99999999);
                /* 批量设置 */
                this.isDiscountValid = ko.observable(true);
                this.discountErrorMsg = ko.observable('');
                this.zhekouInput = ko.observable(10);
                this.jianjiaInput = ko.observable(0);

                this.doZhekou = function() {
                    if(!_this.isDiscountValid()) return false; // invalid

                    var items = _this.getDisplayItems();
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        item.zhekouInput(_this.zhekouInput());
                        item.discountType('D');
                    }
                };
                this.doJianjia = function() {
                    if(!_this.isDiscountValid()) return false; // invalid

                    var items = _this.getDisplayItems();
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        item.jianjiaInput(_this.jianjiaInput());
                        item.discountType('P');
                    }
                };
                this.doMoling = function() {

                    var items = _this.getDisplayItems();
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(item.isMultiplePrice) continue;
                        var zhehoujia = +item.zhehoujia();
                        zhehoujia = parseFloat(Math.floor(zhehoujia));
                        item.zhehoujiaInput(zhehoujia);
                        item.discountType('P');
                    }
                };
                this.doMolingFen = function() {
                    var items = _this.getDisplayItems();
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(item.isMultiplePrice) continue;
                        var zhehoujia = +item.zhehoujia();
                        zhehoujia = parseFloat(Math.floor(zhehoujia*10)/10);
                        item.zhehoujiaInput(zhehoujia);
                        item.discountType('P');
                    }
                };


                this.displayType = ko.observable(0); // 3个tab 0:全部，1:搜索 , -1：出错宝贝

                $(".loading").hide();
                this.items = ko.observableArray(this._getItems());
                this.errorItems = ko.computed(function() {
                    var items = _this.items();
                    var ret = [];
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(!item.isDiscountValid()) {
                            ret.push(item);
                        }
                    }
                    return ret;
                });

                this.removeItem = ko.computed({
                    write: function(item){
                        // delete from the list
                        var is_new = item.is_new;
                        _this.isRemoving = true;
                        _this.items.remove(item);
                        if (is_new){
                            var ids = '';
                            var items = _this.items();
                            for(var i=0,max=items.length; i<max; i++) {
                                var itm = items[i];
                                if (itm.is_new){
                                    ids += itm.num_iid + ',';
                                }
                            }
                            if(ids.length > 0) ids.substr(0, ids.length-1);
                            $.post('zhekou-post-2',{'ids': ids},function(res){
                            },'json');
                        }else{
                            $.post('zhekou-delete-item',{'id':item.num_iid},function(res){
                            },'json');
                        }
                    },
                    read: function(item){}
                });

                /* 搜索 */
                this.isSearching = ko.observable(false);
                this.searchInput = ko.observable('关键字、商品链接、商品编码');
                this.searchedItems = ko.computed(function() {
                    var items = _this.items();
                    var str = $.trim(_this.searchInput().toLowerCase());
                    if(str === '') return []; // empty string
                    var num_iid = getQueryString(str, 'id');
                    var ret = [];
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(item.title.toLowerCase().indexOf(str) !== -1) ret.push(item);
                        else if(item.num_iid == str || item.num_iid == num_iid) ret.push(item);
                        else if(typeof item.outer_id !== "undefined" && item.outer_id.toLowerCase() == str) ret.push(item);
                    }
                    return ret;
                }).extend({throttle: 50});

                this.oldItems = ko.computed(function(){
                    var items = _this.items();
                    var ret = [];
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(!item['is_new']) {
                            ret.push(item);
                        }
                    }
                    return ret;
                });

                this.newAddedItems = ko.computed(function(){
                    var items = _this.items();
                    var ret = [];
                    for(var i=items.length-1; i>=0; i--) {
                        var item = items[i];
                        if(item['is_new']) {
                            ret.push(item);
                        }
                    }
                    return ret;
                });

                ko.computed(function() {
                    var str = $.trim(_this.searchInput());
                    if(str !== '' && str !== '关键字、商品链接、商品编码') {
                        _this.isSearching(true);
                        _this.displayType(1);
                    }
                    else _this.isSearching(false);
                }).extend({throttle: 100});


                this.getDisplayItems = ko.computed(function() {
                    var type = _this.displayType();

                    var ret = [];
                    if(type === 0) ret = _this.items();
                    else if(type === 1) ret = _this.searchedItems();
                    else if(type === -1) ret = _this.errorItems();
                    else if(type === 2) ret = _this.newAddedItems();
                    else if(type === 3) ret = _this.oldItems();

                    return ret;
                }).extend({throttle: 10});

                /* 分页 */
                this.pageSize = 10;
                this.nowPage = ko.observable(1);
                this.totalPage = ko.computed(function() {
                    var items = _this.getDisplayItems();
                    var size = _this.pageSize;

                    var tot = items.length;

                    var max = Math.floor((tot+0.5)/size);
                    if(tot%size != 0) {
                        max++;
                    }
                    return max;
                });
                this.toPrevPage = function() {
                    var a = _this.nowPage();
                    if(a > 1) _this.nowPage(a-1);
                    else return false;
                };
                this.toNextPage = function() {
                    var a = _this.nowPage();
                    if(a < _this.totalPage()) _this.nowPage(a+1);
                    else return false;
                };
                this.toSomePage = function(idx) {
                    var val = $(".toSomePage").eq(idx).val();
                    if(val && val > 0 && val <= _this.totalPage()) _this.nowPage(+val);
                    else alert("输入错误，页码不存在");
                }

                this.displayProducts = ko.computed(function() {

                    var ret = _this.getDisplayItems();

                    var start = _this.pageSize * (_this.nowPage()-1);
                    var end = Math.min(ret.length, start + _this.pageSize);

                    return ret.slice(start, end);
                }).extend({throttle: 10});

                // remove item
                this.isRemoving = false;
                this.hideItem = function(elem) {
                    if(elem.nodeType === 1 && _this.isRemoving) { // 删除某个宝贝
                        _this.isRemoving = false;
                        $(elem).animate({
                            opacity: 0,
                            height: 0
                        }, "slow", function() {
                            $(this).remove();
                        });
                    }
                    else if(elem.nodeType === 1) { // 正常的tab切换
                        $(elem).remove();
                    }
                }

                this.generateDiscountData = function() {
                    var items = _this.items();
                    var ret = {};
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        item.dirty(false);
                        if (!item.isChanged()) continue;
                        ret[+item.num_iid] = {};
                        ret[+item.num_iid]['d_type'] = item.discountType();
                        ret[+item.num_iid]['d_value'] = item.discountValue();
                    }
                    return ret;
                }

                this.getBelowOneProducts = function() {
                    var items = _this.items();
                    var ret = '';
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(item.isBelowThreshold) ret += item.num_iid + ',';
                    }
                    return ret;
                }
            };
            /* end of PageViewModel */
            var pageModelView = new PageViewModel();
            ko.applyBindings(pageModelView);
            var link = window.location.href;
            var from = getQueryString(link, 'fr');
            if(from === 'additem') {
                pageModelView.displayType(2);
            }
            var num_iid = getQueryString(link, 'num_iid');
            if(num_iid !== '') {
                pageModelView.searchInput(num_iid);
            }

            setInterval(function() {
                var dirty = false;
                var items = pageModelView.items();
                for(var i=0,max=items.length; i<max; i++) {
                    if(items[i].dirty()) {
                        dirty = true;
                        break;
                    }
                }
                if(dirty) {
                    var data = pageModelView.generateDiscountData();
                    var data = JSON.stringify(data);
                    $.post('zhekou-post-3', {'data':data}, function(res) {
                        dirty = false;
                    },'json');
                }
            }, 6000);

            $("#settingActModal .closeModal").live("click", function() {
                var btn = $("#nextStepBtn");
                btn.removeClass("grayBtn").addClass("greenBtn").text(btn.data("text"));
            });
            $(".nextStepBtn").click(function(e) {
                var btn = $("#nextStepBtn");
                var modal = $("#settingActModal");
                var lowest_link = 'lowest';

                if($("#settingActModal:visible").length > 0) {
                    $("#settingActModal").hide();
                    $(".js_lb_overlay").remove();
                    btn.removeClass("grayBtn").addClass("greenBtn").text(btn.data("text"));
                    lowest_link = 'lowest_force';

                    btn.effect("highlight", {color: '#BBF6BB'}, 1000, function() {
                        btn.effect("highlight", {color: '#BBF6BB'}, 1000);
                    });
                }

                var errlen = pageModelView.errorItems().length;
                if(errlen > 0) {
                    alert("您有" + errlen + "件商品折扣有误，请修正后提交");
                    return false; /// error
                }
                var data = pageModelView.generateDiscountData();
                data = JSON.stringify(data);
                if(data.length < 3 && bodyId !== 'editPage') { // editPage不要阻止提交
                    var text = "您没有为商品设置折扣，请设置后提交";
                    alert(text);
                    return false; /// error
                }
                if(btn.hasClass("grayBtn")) return false;

                // submit
                btn.data("text", btn.text())
                    .removeClass("greenBtn")
                    .addClass("grayBtn")
                    .text("正在提交...");

                $.get(lowest_link, {t: Math.random()}, function(res) { // 检测店铺最低折扣
                    if(!res.success) {
                        if(res.code === -37) {
                            loadModal('#hraBox', btn, e);
                            btn.removeClass("grayBtn").addClass("greenBtn").text(btn.data("text"));
                        }
                    }
                    else { // success
                        var lowest = res.value;
                        // check lowest
                        var items = pageModelView.items();
                        var valid = true, x = 100000,y = 100000;
                        for(var i=0,max=items.length; i<max; i++) {
                            var d = items[i].zhekou()*100;
                            if(d < lowest && d < y*100) {
                                x = lowest/100;
                                y = d/100;
                                valid = false;
                            }
                        }
                        if(!valid) {
                            var spans = $(".html p span", modal);
                            spans.eq(0).text(y);
                            spans.eq(1).text(x);
                            loadModal("#settingActModal");
                            recordByGA('statusTrack', 'belowLowest', bodyId);
                            return false;
                        }

                        // submit
                        $.post('zhekou-post-3', {'data':data}, function(postres) { // 最后再提交一次
                            $.post('zhekou-create', {'t': Math.random(), page: bodyId}, function(res) { // 提交完毕以后创建活动
                                if (res.success){
                                    var url = 'zhekou-4';
                                    if(bodyId === 'editPage') url = 'zhekou-edit-ok';
                                    url = url + '?t='+Math.random();
                                    window.location.href = url;
                                }else{
                                    if (res.code == -37) {
                                        // no hra, please do hra now
                                        loadModal('#hraBox', btn, e)
                                    }else{
                                        alert(res.msg);
                                    }
                                    btn.removeClass("grayBtn")
                                        .addClass("greenBtn")
                                        .text(btn.data("text"));
                                }
                            },'json');
                        },'json');
                    }
                });
                return false;

            });

            $(".batchOperation .batch input.text").keyup(function(e) {
                if(e.keyCode === 13) { // 回车
                    $("a.confirm", $(this).parent()).trigger("click");
                }
            });
            $(".toSomePage").keyup(function(e) {
                if(e.keyCode === 13) { // 回车
                    $("a.toSomePageBtn", $(this).parent()).trigger("click");
                }
            });
            $(".product .discount input.text").live("keyup", function(e) {
                if(e.keyCode == 13) {
                    var input = $(this);
                    var product = getParent($(this), '.product');
                    var idx = $(".discount input.text", product).index(input);
                    if(product.next().hasClass("product")) {
                        $(".discount input.text", product.next()).eq(idx).focus();
                    }
                }
            });

        })();
    }
    /* end of act step3  */

    /* edit all page */
    if(bodyId === 'editAllPage') {
        (function() {

            /* PageModelView */
            var PageModelView = function() {
                var _this = this;

                this.items = ko.observableArray();

                this.removeItem = ko.computed({
                    write: function(item){
                        // delete from the list
                        _this.isRemoving = true;
                        _this.items.remove(item);
                        $.post('zhekou-delete-item',{'id':item.num_iid},function(res) {
                        },'json');
                    },
                    read: function(item){}
                });
                // remove item
                this.isRemoving = false;
                this.hideItem = function(elem) {
                    if(elem.nodeType === 1 && _this.isRemoving) { // 删除某个宝贝
                        _this.isRemoving = false;
                        $(elem).animate({
                            opacity: 0,
                            height: 0
                        }, "slow", function() {
                            $(this).remove();
                        });
                    }
                    else if(elem.nodeType === 1) { // 正常的tab切换
                        $(elem).remove();
                    }
                }

                this.searchInput = ko.observable('关键字、商品链接、商品编码');
                this.isSearching = ko.observable(false);

                this.search = function() {
                    var q = $.trim(_this.searchInput());
                    if(q === '') {
                        alert('关键词不能为空');
                        return false;
                    }

                    _this.items([]);
                    _this.isSearching(true);
                    $.post('zhekou-search', {q: q, page_size:200, request_more: 1}, function(res) {

                        _this.isSearching(false);
                        if(!res.success) {
                            alert(res.msg);
                            return false;
                        }

                        var items = [];
                        var products = res.res;
                        for(var i=0,max=products.length; i<max; i++) {
                            var product = products[i];
                            if(!product.act_id) continue; // 只显示打折商品
                            var mv = new DiscountItemModelView(product);
                            mv.act_id = product.act_id;
                            mv.dirty(false);
                            mv.submitStatus = ko.observable(0); // 0:未提交, 1:正在提交 2:提交成功
                            mv.submitText = ko.computed(function() {
                                if(this.submitStatus() === 1) return '提交中';
                                else if(this.submitStatus() === 2) return '提交成功';
                                else return '提交修改';
                            }, mv);
                            mv.submitError = ko.observable(false);
                            items.push( mv );

                        }

                        _this.items(items);

                    });
                }

                this.submit = function(item, e) {
                    if(!item.isDiscountValid()) {
                        item.submitError(item.discountErrorMsg() + '，请修正后再提交');
                        return false;
                    }

                    item.submitStatus(1);

                    ret = {};
                    ret[+item.num_iid] = {};
                    ret['id'] = +item.num_iid;
                    ret['d_type'] = item.discountType();
                    ret['d_value'] = item.discountValue();

                    $.post('zhekou-update-item', ret, function(res) {
                        if(!res.success) {
                            item.submitStatus(0);
                            if(res.code === -37) {
                                loadModal('#hraBox', null, e);
                            }
                            else {
                                item.submitError(res.msg);
                            }
                        }
                        else { // success
                            item.submitError('');
                            item.submitStatus(2);
                            setTimeout(function() {
                                item.submitStatus(0);
                                item.dirty(false);
                            }, 1500);
                        }
                    });
                }

            }
            /* end of PageModelView */

            var pageModelView = new PageModelView();
            var q = getQueryString(location.href, 'q');
            if(q !== '') {
                pageModelView.searchInput(q);
                pageModelView.search();
            }
            ko.applyBindings(pageModelView);
            $(".search input.text").keyup(function(e) {
                if(e.keyCode === 13) $(".searchBtn").trigger("click");
            });


        })();
    }
    /* end of edit all page */


    /* act detail page */
    if(bodyId === "actDetailPage") {
        (function() {

            if($(".copyPosterBtn").length > 0) {
                $(".copyPosterBtn").each(function() {
                    var a = $(this);
                    a.attr("data-clipboard-text", a.next().html());
                });
                var clip = new ZeroClipboard($(".copyPosterBtn"), {
                    moviePath: "/static/js/ZeroClipboard.swf"
                });
                clip.on('complete', function() {
                    alert('复制成功');
                });
            }

            if($(".copyMjsTagBtn").length > 0) {
                $(".copyMjsTagBtn").attr("data-clipboard-text", $("#mjsTagDiv").html());
                var clip = new ZeroClipboard($(".copyMjsTagBtn"), {
                    moviePath: "/static/js/ZeroClipboard.swf"
                });
                clip.on('complete', function() {
                    alert('复制成功');
                });
            }

            var mjsTag = $("#mjsTagDiv");
            if(mjsTag.length > 0) {
                var text = $.trim(mjsTag.text()).replace(/\s/g, '');
                var isConditionYuan = /满\d+(.\d+)?元/;
                var isConditionJian = /满\d+件/;
                if(text.search(isConditionYuan) >= 0 && text.search(isConditionJian) >= 0) { // 同时有满元和满件
                    $("#conditionWarning").show();
                }
            }

            /* clear mjs tags */
            var clearTime = {};
            $(".clearLeftBtn").click(function() {
                var btn = $(this);
                var act_id = $("dl.big").attr("id");
                if(clearTime[act_id]) {
                    var diff = (new Date()) - clearTime[act_id];
                    if(diff < 1000*60*5) {
                        alert('清理中...');
                        return false;
                    }
                }
                $.post('mjs-remove-tags', {aid: act_id}, function(res) {
                    if(res.success) {
                        alert(res.msg);
                        clearTime[act_id] = new Date();
                    }
                    else {
                        alert(res.msg);
                    }
                }, 'json');
            });
            /* finish act */
            $(".finishBtn").click(function(e) {
                var btn = $(this);
                var box = $("#instantConfirmBox");
                box.data("act_id", $("dl.big").attr("id")).data("action", "finishAct");
                $(".html", box).text("您确定结束这个活动吗？");
                $(".buttons .grayBtn", box).text("不结束");
                $(".buttons .lightGreenBtn", box).text("结束");
                var x = e.pageX - 105;
                var y = e.pageY - 75;
                box.css({'left': x, "top": y})
                    .show("scale", {}, 200);
            });
            /* edit act modal */
            $(".editActBtn").click(function() {
                var btn = $(this);
                if(user_level == MZ_CONSTANT.USER_LEVEL_0) {
                    loadModal("#upgradeModal", btn);
                    return false;
                }

                var act_dl = $("dl.big");
                var modal = $('#editActModal');

                $('.act_name',modal).val( $(".name span", act_dl).text() );
                $('.act_started',modal).text( $(".started", act_dl).text() );
                $('.act_ended',modal).val( $(".ended", act_dl).text() );
                if($("#act_type").val() === 'fzhekou') $(".act_fzhekou").val( $(".fd_value").text() );

                modal.data("aid", act_dl.attr("id") );
                var title = $(".title", act_dl).text();

                $('#editActModal .tmLine').jqTransSelectRemove($("#title"));
                $("#editActModal .tmLine").jqTransform();
                loadModal("#editActModal", btn);
            });
            $("#editActModal .tmLine").jqTransform();
            $("#editActModal .submitBtn").click(function(e) {
                var modal = $("#editActModal");
                var data = {};
                data['name'] = $('.act_name',modal).val();
                data['ended'] = $('.act_ended',modal).val();
                if($("#act_type").val() === 'fzhekou') {
                    data['fd_value'] = Math.round($(".act_fzhekou", modal).val()*100);
                }
                var title = $('#title').val();
                if(title == -1) {
                    title = $("#custom_title").val();
                }
                data['title'] = title;
                data['aid'] = modal.data("aid");
                $.post('zhekou-edit-info', data, function(res) {
                    if(!res.success) {
                        if (res.code == -37){
                            loadModal("#hraBox", null, e);
                        }else{
                            if(res.code == 1000) { // for 高级版
                                closeModal();
                                loadModal("#upgradeModal");
                            }
                            else {
                                alert(res.msg);
                            }
                        }
                    }
                    else {
                        $("#editActModal").hide();
                        $("#alertModal .title").html("修改活动信息");
                        $("#alertModal .html").text("修改成功");
                        loadModal("#alertModal");
                        $("#alertModal").data("refreshPage", true);
                    }
                },'json');
            });
            $("#alertModal .closeModal").click(function() {
                if($("#alertModal").data("refreshPage")) {
                    window.location.href = window.location.href;
                }
            });
            $("#title").change(function(){
                if ($("#title").val() === "-1"){
                    $('#custom_title').show();
                }else{
                    $('#custom_title').hide();
                }
            });
            $('#end_time').datetimepicker();

            $(".shareBtn").click(function() {
                var btn = $(this);
                var act_dl = $("dl.big");
                var aid = act_dl.attr("id");
                $.post('weibo-'+aid, function(res) {
                    if(res.success) {
                        var weibo = res.weibo;
                        weibo.msg = weibo.msg + ' #美折#';
                        var title = $('.name span', act_dl).text();
                        var modal = $("#shareActModal");
                        $(".title span", modal).text(title);
                        $("textarea", modal).val(weibo.msg);
                        updateJiathisConfig(weibo);
                        loadModal("#shareActModal", btn);
                    }
                    else {
                        alert(res.msg);
                    }
                }, 'json');
            });

            /* search product */
            $(".searchBtn, #searchProductsFilter").click(function() {
                $("#errorProducts").hide();
                $("#allProducts").show();
                $("#searchProductsFilter").show();
                var text = $(".search .text").val();
                text = $.trim(text).toLowerCase();
                var id = getQueryString(text, 'id');
                var cnt = 0;
                $("#allProducts .p").each(function() {
                    var p = $(this);
                    var a = $(".num_iid", p).val();
                    var b = $(".outer_id", p).val();
                    var c = $(".title", p).text();
                    if(text == a || c.toLowerCase().indexOf(text) !== -1 || a == id) {
                        p.show();
                        cnt++;
                    }
                    else if(b !== '' && text == b) {
                        p.show();
                        cnt++;
                    }
                    else p.hide();
                });
                $(".detailProducts .category").hide();
                $("#allProducts").show();
                $(".detailProducts .tabs a").removeClass("active");
                $("#searchProductsFilter").addClass("active");
                $("#searchLength").text(cnt);
                if(cnt === 0) $(".noSearchResult").show();
                else $(".noSearchResult").hide();

                $(".filter .funcBtns a").hide();
                //$(".filter .deleteAllInstockBtn").show();
                $(".filter .search").show();
            });
            $(".search .text").keyup(function(event) {
                if(event.keyCode == 13) {
                    $(".searchBtn").trigger("click");
                }
            });
            $("#allProductsFilter").click(function() {
                $(".detailProducts .category").hide();
                $("#allProducts").show();
                $(".detailProducts .tabs a").removeClass("active");
                $(this).addClass("active");
                $("#allProducts .p").show();
                $(".noSearchResult").hide();
                $(".filter .funcBtns a").hide();
                //$(".filter .deleteAllInstockBtn").show();
                $(".filter .search").show();
            });
            /* end of search product */

            /* handle parameter */
            var link = window.location.href;
            var id = getQueryString(link, 'num_iid');
            if(id !== '') {
                $(".search input.text").trigger("focus").val(id);
                $(".searchBtn").trigger("click");
                var href = $(".editProductsBtn").attr("href");
                href = setQueryString(href, 'num_iid', id);
                $(".editProductsBtn").attr("href", href);
            }
            /* end of handle parameter */

            /* delete product */
            $(".detailProducts .category .p").hover(
                function() {
                    $(this).addClass("phover");
                },
                function() {
                    $(this).removeClass("phover");
                }
            );
            $(".deleteProductBtn").click(function(e) {

                if($("#act_type").val() === 'mjs') {
                    if($("#allProducts .p").length <= 2) {
                        alert("删除失败：满就送活动至少要有2件商品才会生效");
                        return false;
                    }
                }

                var p = $(this).parent();
                var num_iid = $(".num_iid", p).val();
                var act_type = $("#act_type").val();
                var link = act_type + '-delete-item';
                var data = {};
                if(act_type === 'zhekou') {
                    data['id'] = num_iid;
                }
                else if(act_type === 'mjs') {
                    data['aid'] = $("dl.big").attr("id");
                    data['num_iid'] = num_iid;
                }
                $.post(link, data, function(res) {
                    p.effect('clip', {}, 500, function() {
                        p.remove();
                        var allLen = $("#allProducts .p").length;
                        var errLen = $("#errorProducts .p").length;
                        var instockLen = $("#instockProducts .p").length;
                        $("#allLength").text(allLen);
                        $("#errorLength").text(errLen);
                        $("#intockLength").text(instockLen);
                        if(allLen === 0) $("#allProducts .noSearchResult").show();
                        if(instockLen === 0) {
                            $("#instockProducts .noInstockProduct").show();
                            $(".filter .deleteAllInstockBtn").remove();
                        }
                        if(errLen === 0) $("#errorProducts .noErrorProduct").show();
                        if($("#searchProductsFilter").hasClass("active")) {
                            $(".searchBtn").trigger("click");
                        }
                    });
                },'json');
            });
            /* end of delete product */

            /* delete poster */
            $(".deletePosterBtn").live("click", function(e) {
                var a = $(this);
                var pid = a.data('pid');
                var box = $("#instantConfirmBox");
                box.data('action', 'deletePoster').data('poster_id', pid).data('block', getParent(a, ".poster"));
                $(".html", box).text("您确定删除这个海报吗？");
                $(".buttons .grayBtn", box).text("不删除");
                $(".buttons .lightGreenBtn", box).text("删除");
                var x = e.pageX - 105;
                var y = e.pageY - 75;
                box.css({'left': x, "top": y})
                    .show("scale", {}, 200);
            });
            /* end of delete poster */

            /* instance confirm box */
            $("#instantConfirmBox .cancel").click(function() {
                $("#instantConfirmBox").hide("fade", {}, 200);
            });
            $("#instantConfirmBox .confirm").click(function() {
                var box = $("#instantConfirmBox");
                box.hide("fade", {}, 200);
                if (box.data('action') == 'finishAct'){
                    var act_id = box.data('act_id');
                    var act_type = $("#act_type").val();
                    if (act_type === 'fzhekou') act_type = 'zhekou';
                    if (act_type === 'fmjs' || act_type === 'dmjs') act_type = 'mjs';
                    $.post(act_type+'-delete',{'id':act_id},function(ret){
                        if(ret.success === 1) {
                            window.location.href = window.location.href;
                        }
                    },'json');
                }
                else if(box.data('action') === 'deleteAllInstock') {
                    var act_id = box.data('act_id');
                    $.post('/huodong/fix-' + act_id, {'in_stock':1}, function(ret){
                        if(ret.success === 1) {
                            $("#instockProducts").fadeOut(function() {
                                $("#allProductsFilter").trigger("click");
                                $("#instockProductsFilter").hide();
                                $(".status_instock").each(function() {
                                    var t = $(this);
                                    getParent(t, ".p").remove();
                                });
                                var allLen = $("#allProducts .p").length;
                                var errLen = $("#errorProducts .p").length;
                                var instockLen = $("#instockProducts .p").length;
                                $("#allLength").text(allLen);
                                $("#errorLength").text(errLen);
                                $("#intockLength").text(instockLen);
                            });
                        }
                    },'json');
                }
                else if(box.data('action') === 'deletePoster') {
                    var poster_id = box.data('poster_id');
                    var block = box.data("block");
                    $.post('/huodong/poster-delete-' + poster_id, {t: Math.random()}, function(res) {
                        if(res.success === 1) {
                            block.fadeOut(function() {
                                block.remove();
                            });
                        }
                        else {
                            alert(res.msg);
                        }
                    });
                }
            });
            /* end of instance confirm box */

            /* instock products */
            var instockLen = $("#instockProducts .p").length;
            if(instockLen > 0) {
                var filter = $("#intockLength").text(instockLen);
                $("#instockProductsFilter").show();
                $("#instockProductsFilter").click(function() {
                    $(".detailProducts .category").hide();
                    $("#instockProducts").show();
                    $(".detailProducts .tabs a").removeClass("active");
                    $(this).addClass("active");
                    $(".filter .funcBtns a").hide();
                    $(".filter .deleteAllInstockBtn").show();
                    $(".filter .search").hide();
                });
                $(".deleteAllInstockBtn").click(function(e) {
                    var box = $("#instantConfirmBox");
                    var text = '您确定从活动中删除所有已下架的宝贝吗？'
                    box.data("act_id", $("dl.big").attr("id")).data("action", "deleteAllInstock");
                    $(".html", box).text(text);
                    $(".buttons .grayBtn", box).text("不删除");
                    $(".buttons .lightGreenBtn", box).text("删除");
                    var x = e.pageX - 105;
                    var y = e.pageY - 75;
                    box.css({'left': x, "top": y})
                        .show("scale", {}, 200);
                });
            }
            /* end of instock products */

            /* error products */
            if($("#allProducts .perror").length > 0) {
                var adultCategories = {
                    /* 50012829 */
                    '50003114': true, '50012831': true, '50012832': true, '50012830': true, '50006275': true,
                    /* 50019617 */
                    '50019618': true, '50019619': true, '50019623': true, '50019626': true, '50019627': true,
                    '50019628': true, '50019629': true,
                    /* 50019630 */
                    '50019631': true, '50019636': true, '50019637': true, '50019638': true, '50019639': true,
                    '50019640': true,
                    /* 50019641 */
                    '50019642': true, '50019643': true, '50019644': true, '50019645': true, '50019646': true,
                    '50019700': true, '50019647': true,
                    /* 50019651 */
                    '50019652': true, '50019653': true, '50019656': true, '50019657': true, '50019658': true,
                    '50019659': true,
                    /* 50020206 */
                    '50020205': true, '50050327': true
                }

                $("#allProducts .perror span.errorSpan").each(function() {
                    var span = $(this);
                    var a = span.attr("title");
                    var p = getParent(span, '.p');
                    var id = $(".num_iid", p).val();

                    if(adultCategories[$(".cid", p).val()]) { // 成人类目
                        span.attr('title', '成人类目商品，淘宝不允许工具插入满就送信息到宝贝详情页')
                            .removeClass('status_red')
                            .addClass('status_gray')
                            .text('成人类目商品');
                        p.removeClass('perror');
                        $("#errorProducts .item-"+id).remove();
                    }
                    else {
                        var idx = a.lastIndexOf('|');
                        span.attr("title", a.substr(idx+1));
                        p.addClass("perrorTrue");
                        $("#instockProducts .item-"+ id + " .errorSpan").attr("title", a.substr(idx+1)).show();
                    }
                    span.show();
                    $("#instockProducts .item-"+id).addClass("perrorTrue");
                });
                $("#errorProducts .errorMsg").each(function() {
                    var msg = $(this);
                    var a = msg.text();
                    var idx = a.lastIndexOf('|');
                    a = a.substr(idx+1);
                    idx = a.indexOf('该商品发售价格不在合理价格区间范围');
                    var isSpecialMsg = false;
                    if(idx > 0) {
                        a = a.substr(idx);
                        isSpecialMsg = true;
                        a += '<br><strong class="orange">淘宝3月7日新规：商品打折后明显低于市场价，会设置失败</strong> ' +
                            '<a href="http://dev.open.taobao.com/bbs/read.php?tid=24513&page=1" target="_blank" style="font-family:宋体">官方通告></a>'
                    }
                    if(!isSpecialMsg) msg.text(a);
                    else msg.html(a);
                });

                $("#errorProductsFilter").click(function() {
                    $(".detailProducts .category").hide();
                    $("#errorProducts").show();
                    $(".detailProducts .tabs a").removeClass("active");
                    filter.addClass("active");
                    $(".filter .funcBtns a").hide();
                    //$(".filter .deleteAllInstockBtn").show();
                    $(".filter .search").hide();
                });

                var filter = $("#errorProductsFilter");
                var errlen = $("#allProducts .perror").length;
                $("#errorLength", filter).text(errlen);
                if(errlen > 0) filter.show();
            }

            if($("#allLength").text() == 0) {
                $(".noSearchResult").show();
            }

            var PageViewModel = function(aid) {
                var _this = this;
                this.aid = aid;
                // poster
                this.posters = ko.observableArray([]);
                this.isLoadPosters = ko.observable(false);

                this.initPosters = function() {
                    // load posters from backend
                    $.post('/huodong/poster-list-'+_this.aid, {t: Math.random()}, function(res) {
                        if(!res.success) {
                            // 错误处理
                            return false;
                        }
                        var posters = [];
                        var ps = res.acts;
                        var items = res.items;
                        for(var i=0,max=ps.length; i<max; i++) {
                            var p = ps[i];
                            var poster = {
                                id: p['_id'],
                                from_item_count: p['banner_items'].length,
                                to_item_count: p['banner_targets'].length,
                                from_items: []
                            };
                            for(var j=0,maxj=poster.from_item_count; j<maxj && j<4; j++) {
                                var id = p['banner_items'][j] + '';
                                try {
                                    poster.from_items.push(items[id]['pic_url']);
                                } catch (e) {
                                    poster.from_items.push('http://img02.taobaocdn.com/bao/uploaded/i2/15163031343792051/T1bgw2XfNdXXXXXXXX_!!2-item_pic.png');
                                }
                            }
                            posters.push(poster);
                        }
                        _this.posters(posters);
                        _this.isLoadPosters(true);
                        $("#actPosterToggle").trigger("click");
                    });
                }
            };

            var pageViewModal = new PageViewModel($("dl.big").attr("id"));

            ko.applyBindings(pageViewModal);

            $("#actPosterToggle").click(function() {
                if(!pageViewModal.isLoadPosters()) {
                    pageViewModal.initPosters();
                }
            });

        })();
    }
    /* end of act detail page */


    /* mjs step3 */
    if (bodyId === "mjsStep3Page") {

        (function () {

            // 地区编号对应表
            var Location = {
                '54':'西藏', '65':'新疆', '71':'台湾', '81':'香港', '82':'澳门', '31':'上海', '11':'北京', '12':'天津', '13':'河北', '37':'山东',
                '14':'山西', '15':'内蒙古', '21':'辽宁', '22':'吉林', '23':'黑龙江', '32':'江苏', '33':'浙江', '34':'安徽', '36':'江西', '41':'河南',
                '42':'湖北', '43':'湖南', '44':'广东', '45':'广西', '35':'福建', '46':'海南', '50':'重庆', '51':'四川', '52':'贵州', '53':'云南',
                '61':'陕西', '62':'甘肃', '63':'青海', '64':'宁夏'
            };
            var LocationArr = ['54', '65', '31', '11', '12', '13', '37', '14', '15', '21', '22', '23', '32', '33', '34', '36', '41', '42', '43', '44', '45', '35', '46', '50', '51', '52', '53', '61', '62', '63', '64'];

            var dirty = false;
            var DetailPageView = function (detail) {
                var _this = this;

                this.uidstr = Math.round(Math.random()*1000000000);
                /* 变量 */
                this.conditionType = ko.observable('Y');		// Y表示满多少元，J表示满多少件
                this.conditionYuan = ko.observable(100);		// 满XX元
                this.conditionJian = ko.observable(2);			// 满XX件
                this.conditionError = ko.observable(0);
                this.conditionFengding = ko.observable(true);

                this.conditionYuan.subscribe(function () {
                    _this.conditionError(0);
                    var val = _this.conditionYuan();
                    val = +val;
                    if (isNaN(val) || val <= 0) {
                        _this.conditionError(1);
                    }
                });
                this.conditionJian.subscribe(function () {
                    _this.conditionError(0);
                    var val = _this.conditionJian();
                    val = +val;
                    if (isNaN(val) || val <= 0) {
                        _this.conditionError(1);
                    }
                });
                this.conditionType.subscribe(function () {
                    _this.conditionError(0);
                    var t = _this.conditionType();
                    if (t === 'Y') {
                        var val = _this.conditionYuan();
                        val = +val;
                        if (isNaN(val) || val <= 0) {
                            _this.conditionError(1);
                        }
                    }
                    else if (t === 'J') {
                        var val = _this.conditionJian();
                        val = +val;
                        if (isNaN(val) || val <= 0) {
                            _this.conditionError(1);
                        }
                    }
                });

                this.detailType = ko.observable('P');		// P和D，优惠内容是 打折还是减价
                this.detailPrice = ko.observable(10);		// 减XX元
                this.detailDiscount = ko.observable(9);		// 打XX折
                this.isPostFree = ko.observable(false);		// 是否包邮
                this.isGiftName = ko.observable(false);
                this.theGiftName = ko.observable('礼物名称');
                this.theGiftUrl = ko.observable('');
                this.theGiftId = ko.observable('');

                this.detailError = ko.observable(0);

                this.detailPrice.subscribe(function () {
                    _this.detailError(0);
                    var val = _this.detailPrice();
                    val = +val;
                    if (isNaN(val) || val <= 0) {
                        _this.detailError(1);
                    }
                });
                this.detailDiscount.subscribe(function () {
                    _this.detailError(0);
                    var val = _this.detailDiscount();
                    val = +val;
                    if (isNaN(val) || val <= 0 || val >= 10) {
                        _this.detailError(1);
                    }
                });
                this.detailType.subscribe(function () {
                    _this.detailError(0);
                    var t = _this.detailType();
                    if (t === 'Y') {
                        var val = _this.detailPrice();
                        val = +val;
                        if (isNaN(val) || val <= 0) {
                            _this.detailError(1);
                        }
                    }
                    else if (t === 'J') {
                        var val = _this.detailDiscount();
                        val = +val;
                        if (isNaN(val) || val <= 0 || val >= 10) {
                            _this.detailError(1);
                        }
                    }
                });

                // 包邮地区，默认是全部包邮
                this.postFreeLocation = ko.observableArray(LocationArr.slice(0));
                this.postFreeLocationSaved = [];
                this.index = ko.observable(0);

                this.updateDetailType = function(a) {
                    var dt = _this.detailType();
                    if(dt === a) _this.detailType('');
                    else _this.detailType(a);
                }
                this.showDetailValue = function(tag_index) {
                    tag_index = +tag_index;
                    return ko.computed(function() {
                        var d = _this.detailType();
                        var cfd = _this.conditionFengding();
                        var ret = '';
                        if(d.length > 0) {
                            if(tag_index === 0) {
                                if(d === 'P') {
                                    ret = '减 <strong style="color:#FF5400;font-size:16px;">'+_this.detailPrice()+'</strong> 元';
                                    if (cfd == 1) ret = ret + "&nbsp;上不封顶"
                                }
                                else if(d === 'D') {
                                    ret = '打 <strong style="color:#FF5400;font-size:16px;">'+_this.detailDiscount()+'</strong> 折';
                                }
                            }
                            else if(tag_index === 1) {
                                if(d === 'P') {
                                    ret = '减 <strong style="color:#FF5400;font-size:20px;">'+_this.detailPrice()+'</strong> 元';
                                    if (cfd == 1) ret = ret + "&nbsp;&nbsp;<span style='color:#999'>上不封顶</span>"
                                }
                                else if(d === 'D') {
                                    ret = '打 <strong style="color:#FF5400;font-size:20px;">'+_this.detailDiscount()+'</strong> 折';
                                }
                            }
                        }
                        return ret;
                    }).extend({throttle: 50});
                }
                this.notPostFreeLocation = ko.computed(function () {
                    var ret = [];
                    var locs = _this.postFreeLocation();
                    for(var i=0, max=LocationArr.length; i<max; i++) {
                        var t = LocationArr[i];
                        var find = false;
                        for(var j=0,maxj=locs.length; j<maxj && !find; j++) {
                            if(locs[j] == t) {
                                find = true;
                            }
                        }
                        if(!find) ret.push(t);
                    }
                    return ret;
                }).extend({throttle: 50});
                this.postFreeLocToString = ko.computed(function () {
                    var str = '不包括 ';
                    var locs = _this.notPostFreeLocation();
                    if(_this.postFreeLocation().length < _this.notPostFreeLocation().length) {
                        locs = _this.postFreeLocation();
                        str = '包邮地区：';
                    }
                    for(var i=0,max=locs.length; i<max; i++) {
                        str += Location[+locs[i]] + ',';
                    }
                    str = str.substr(0, str.length-1);
                    return str;
                }).extend({throttle: 50});
                this.loadPostFreeLocModal = function () {
                    _this.postFreeLocationSaved = _this.postFreeLocation().slice(0);
                    _this.updatePfBigBtn();
                    loadModal("#postFreeLocModal", $(".postFreeLocBtn"));
                };
                this.cancelPostFreeLoc = function() {
                    _this.postFreeLocation(_this.postFreeLocationSaved.slice(0));
                };
                this.updatePostFreeLoc = function(data, event) {
                    var locs = _this.postFreeLocation();
                    var elem = $(event.target);
                    var arr =  elem.val().split(",");
                    var checked = elem.prop("checked");
                    if(checked) {
                        for(var i=0, max=arr.length; i<max; i++) {
                            var t = arr[i];
                            var find = false;
                            for(var j=0,maxj=locs.length; j<maxj && !find; j++) {
                                if(locs[j] == t) find = true;
                            }
                            if(!find) _this.postFreeLocation.push(t);
                        }
                    }
                    else {
                        for(var i=0, max=arr.length; i<max; i++) {
                            var t = arr[i];
                            var find = false;
                            for(var j=0,maxj=locs.length; j<maxj && !find; j++) {
                                if(locs[j] == t) {
                                    _this.postFreeLocation.splice(j, 1);
                                    find = true;
                                }
                            }
                        }
                    }
                };
                this.updatePfBigBtn = function () {
                    var locs = _this.postFreeLocation();
                    $(".pfBigBtn").each(function () {
                        var arr = $(this).val().split(",");
                        var check = true;
                        for (var i = 0, max = arr.length; i < max && check; i++) {
                            var t = arr[i];
                            var find = false;
                            for (var j = 0, maxj = locs.length; j < maxj && !find; j++) {
                                if (t == locs[j]) find = true;
                            }
                            if (!find) check = false;
                        }
                        $(this).prop("checked", check);
                    });
                };
                this.fastSelectPostFreeLoc = function(str) {
                    var locs = str.split(',');
                    if(str === '') locs = [];
                    _this.postFreeLocation(locs);
                    _this.updatePfBigBtn();
                }
                this.checkPostFreeLocLength = function() {
                    var locs = _this.postFreeLocation();
                    if(locs.length === 0) {
                        alert("请至少选择一个包邮区域");
                    }
                    else {
                        closeModal();
                    }
                }
                this.togglePostFreeLoc = function(val) {
                    if(_this.postFreeLocation.indexOf(val) !== -1 ) {
                        _this.postFreeLocation.remove(val);
                    }
                    else {
                        _this.postFreeLocation.push(val);
                    }
                    _this.updatePfBigBtn();
                }
                this.toSubmitData = function() {
                    var ret = {};
                    var ct = _this.conditionType();
                    if(ct === 'J') {
                        ret['count'] = +_this.conditionJian();
                        ret['countMulti'] = +_this.conditionFengding();
                        if(isNaN(ret['count'])) ret['count'] = _this.conditionJian(); // error
                    }
                    else if(ct === 'Y') {
                        ret['totalPrice'] = Math.round((+_this.conditionYuan()+0.00001)*100);
                        ret['amountMulti'] = +_this.conditionFengding();
                        if(isNaN(ret['totalPrice'])) ret['totalPrice'] = _this.conditionYuan(); // error
                    }
                    var dt = _this.detailType();
                    if(dt === 'P') {
                        ret['decreaseMoney'] = Math.round((+_this.detailPrice()+0.00001)*100);
                        if(isNaN(ret['decreaseMoney'])) ret['decreaseMoney'] = _this.detailPrice(); // error
                    }
                    else if(dt === 'D') {
                        ret['discountRate'] = Math.round((+_this.detailDiscount()+0.00001)*100);
                        if(isNaN(ret['discountRate'])) ret['discountRate'] = _this.detailDiscount(); // error
                    }
                    if(_this.isPostFree()) {
                        var locs = _this.postFreeLocation();
                        ret['area'] = locs.join('|');
                    }
                    if(_this.isGiftName()) {
                        ret['giftName'] = _this.theGiftName();
                    }
                    return ret;
                }
                // initial object
                if(detail) {
                    if('totalPrice' in detail) {
                        this.conditionType('Y');
                        this.conditionYuan(detail.totalPrice/100);
                        this.conditionFengding(detail.amountMulti);
                        if(isNaN(this.conditionYuan())) this.conditionYuan(detail.totalPrice); // error
                    }
                    else if('count' in detail) {
                        this.conditionType('J');
                        this.conditionJian(detail.count);
                        this.conditionFengding(detail.countMulti);
                        if(isNaN(this.conditionJian())) this.conditionJian(detail.count); // error
                    }
                    if('decreaseMoney' in detail) {
                        this.detailType('P');
                        this.detailPrice(detail.decreaseMoney/100);
                        if(isNaN(this.detailPrice())) this.detailPrice(detail.decreaseMoney); // error
                    }
                    else if('discountRate' in detail) {
                        this.detailType('D');
                        this.detailDiscount(detail.discountRate/100);
                        if(isNaN(this.detailDiscount())) this.detailDiscount(detail.discountRate); // error
                    }
                    if('area' in detail) {
                        this.isPostFree(true);
                        if(detail.area !== "") this.fastSelectPostFreeLoc(detail.area.split('|').join(','));
                        else this.fastSelectPostFreeLoc("");
                    }
                    if('giftName' in detail) {
                        this.isGiftName(true);
                        this.theGiftName(detail.giftName);
                    }
                    if(('area' in detail || 'giftName' in detail) && !('decreaseMoney' in detail) && !('discountRate' in detail)) {
                        this.detailType('');
                    }
                }
                // subscribe for dirty
                this.conditionFengding.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#F1FCF3'}, 500);
                    dirty = true;
                });
                this.conditionYuan.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
                this.conditionJian.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
                this.detailDiscount.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
                this.detailPrice.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
                this.isGiftName.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
                this.isPostFree.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
                this.postFreeLocation.subscribe(function() {
                    $(".area:visible div").effect("highlight", {color: '#ECFBEE'}, 1000);
                    dirty = true;
                });
            }
            /* end of DetailPageView */
            $("input.prvc").live("click", function() {
                var b = $(this);
                var w = b.parent();
                var yes = true;
                $("input.prvc", w).each(function() {
                    if(!$(this).prop("checked")) yes = false;
                });
                if(yes) {
                    $("input.pfBigBtn", w).prop("checked", true);
                }
                else {
                    $("input.pfBigBtn", w).prop("checked", false);
                }
            });

            var PageViewModel = function () {
                var _this = this;
                this.settings = ko.observableArray([]);

                this.settings.subscribe(function () {
                    var settings = _this.settings();
                    for (var i = 0, max = settings.length; i < max; i++) {
                        var setting = settings[i];
                        setting.index(i + 1);
                    }
                });


                this.title = ko.observable('');
                this.time = ko.observable('');
                this.comments = ko.observable('');
                this.commentsShow = ko.computed(function() {
                    var c = _this.comments();
                    return htmlForTextWithEmbeddedNewlines(c);
                });
                this.currentTag = ko.observable(0);

                this.selectedIndex = ko.observable(0);

                this.selectedDetail = ko.computed(function () {
                    var i = _this.selectedIndex();
                    if (i == 0) return {
                        postFreeLocation: ko.observableArray([])
                    };
                    var s = _this.settings()[i - 1];
                    return s;
                });
                this.addNewDetail = function() {
                    if(_this.settings().length === 10) {
                        alert('最多创建10个优惠详情');
                        return false;
                    }
                    var last = _this.settings()[_this.settings().length-1];
                    var detail = new DetailPageView();
                    detail.postFreeLocation(last.postFreeLocation().slice(0));
                    _this.settings.push(detail);
                    _this.selectedIndex(_this.settings().length);
                }
                this.selectDetail = function(data, event) {
                    _this.selectedIndex(data.index());
                }
                this.removeDetail = function(data, event) {
                    _this.settings.remove(data);
                    if(_this.selectedIndex() > _this.settings().length) {
                        _this.selectedIndex(_this.settings().length);
                    }
                }
                this.toSubmitData = function() {
                    var data = {settings: []};
                    data['comments'] = _this.comments();
                    data['tag'] = _this.currentTag();
                    data['old_aid'] = $('#old_aid').val();
                    for(var i=0,max=_this.settings().length; i<max; i++) {
                        var s = _this.settings()[i];
                        data.settings.push(s.toSubmitData());
                    }
                    return data;
                }
                // subscribe for dirty
                this.settings.subscribe(function() {
                    dirty = true;
                });
                this.comments.subscribe(function() {
                    dirty = true;
                });
            }
            /* end of PageViewModel */

            var pageViewModel = new PageViewModel();

            ko.applyBindings(pageViewModel);

            // 初始化
            $.post('mjs-data-3', {'t': Math.random()}, function(res) {
                if(res.success) { // 找到了保存的内容
                    var data = res.data;
                    if (data.comments)
                        pageViewModel.comments(data.comments.replace(/<br>/g,'\n'));
                    if(!isNaN(data.tag)) pageViewModel.currentTag(data.tag);
                    else pageViewModel.currentTag(1);
                    for(var i=0,max=data.settings.length; i<max; i++) {
                        var s = data.settings[i];
                        pageViewModel.settings.push(new DetailPageView(s));
                    }
                    if (data.old_aid){
                        $('#old_aid').val(data.old_aid)
                    }
                }
                else {
                    pageViewModel.currentTag(1);
                    pageViewModel.settings.push(new DetailPageView());
                }
                pageViewModel.selectedIndex(1);
                $(".loading").hide();
            }, 'json');



            $("#nextStepBtn").click(function(e) {
                // check error
                var errorId = -1;
                for(var i=0,max=pageViewModel.settings().length; i<max; i++) {
                    var s = pageViewModel.settings()[i];
                    if(s.conditionError() || s.detailError()) {
                        errorId = i+1;
                    }
                }
                if(errorId !== -1) {
                    alert("优惠详情中有错误，请修正后再提交");
                    pageViewModel.selectedIndex(errorId);
                    setTimeout(function() {
                        $("input.error").parent().effect('bounce', {times: 7, distance: 30}, 1500);
                    }, 300);
                    return false;
                }

                var data = pageViewModel.toSubmitData();
                var isEmpty = true;
                for(var i=0,max=data.settings.length; i<max; i++) { // check if empty
                    var s = data.settings[i];
                    if( !(typeof s.decreaseMoney === "undefined" &&
                        typeof s.discountRate === "undefined" &&
                        typeof s.giftName === "undefined" &&
                        typeof s.area === "undefined")) {
                        isEmpty = false;
                    }
                }
                if(isEmpty) {
                    alert("您的优惠内容为空哦，请至少设置一种优惠内容：）");
                    return false;
                }


                for(var i=0,max=data.settings.length; i<max; i++) { // 自动计算包邮地区
                    var s1 = data.settings[i];
                    if(typeof s1.area !== "undefined") {
                        for(var j=0,max=data.settings.length; j<max; j++) {
                            var s2 = data.settings[j];
                            if(typeof s2.area === "undefined") continue;
                            if((typeof s1.totalPrice !== "undefined" && typeof s2.totalPrice !== "undefined" && s1.totalPrice > s2.totalPrice) ||
                                (typeof s1.count !== "undefined" && typeof s2.count !== "undefined" && s1.count > s2.count)) {
                                var t = s1.area + '|' + s2.area;
                                t = t.split('|');
                                t = $.grep(t, function(v, k){
                                    return $.inArray(v ,t) === k;
                                });
                                s1.area = t.join("|");
                            }
                        }
                    }
                }

                data = JSON.stringify(data);

                // submit
                var btn = $(this);
                if(btn.hasClass("grayBtn")) return false;

                btn.data("text", btn.text())
                    .removeClass("greenBtn")
                    .addClass("grayBtn")
                    .text("正在提交...");

                $.post('mjs-post-3', {'data':data}, function(postres) { // 最后再提交一次
                    $.post('mjs-post-4', {'t': Math.random()}, function(res) { // 提交完毕以后创建活动
                        if(res.success) {
                            if (res.next_url) window.location.href = res.next_url;
                            else window.location.href = 'mjs-4';
                        } else {
                            if (res.code == -37) {
                                // no hra, please do hra now
                                loadModal('#hraBox', null, e);
                            } else {
                                alert(res.msg);
                            }
                            btn.removeClass("grayBtn")
                                .addClass("greenBtn")
                                .text(btn.data("text"));
                        }
                    },'json');

                },'json');
            });

            setInterval(function() {
                if(dirty) { // 有修改时才保存
                    var data = pageViewModel.toSubmitData();
                    for(var i=0,max=data.settings.length; i<max; i++) { // 自动计算包邮地区
                        var s1 = data.settings[i];
                        if(typeof s1.area !== "undefined") {
                            for(var j=0,max=data.settings.length; j<max; j++) {
                                var s2 = data.settings[j];
                                if(typeof s2.area === "undefined") continue;
                                if((typeof s1.totalPrice !== "undefined" && typeof s2.totalPrice !== "undefined" && s1.totalPrice > s2.totalPrice) ||
                                    (typeof s1.count !== "undefined" && typeof s2.count !== "undefined" && s1.count > s2.count)) {
                                    var t = s1.area + '|' + s2.area;
                                    t = t.split('|');
                                    t = $.grep(t, function(v, k){
                                        return $.inArray(v ,t) === k;
                                    });
                                    s1.area = t.join("|");
                                }
                            }
                        }
                    }
                    data = JSON.stringify(data);
                    dirty = false;
                    $.post('mjs-post-3', {'data':data}, function(postres) {
                    },'json');
                }
            }, 5*1000); // 5秒自动保存一次

            // other function
            $("span.pfspan").live("click", function(event) {
                var val = $(this).prev().val();
                var model = ko.contextFor(this);
                var $data = model.$data;
                model.$data.togglePostFreeLoc(val);
            });

        })();
    }
    /* end of mjs step3 */

    /* start of poster details page */
    if (bodyId === 'posterDetailPage'){
        if($(".copyPosterTagBtn").length > 0) {
            $(".copyPosterTagBtn").attr("data-clipboard-text", $("#posterTagDiv").html());
            var clip = new ZeroClipboard($(".copyPosterTagBtn"), {
                moviePath: "/static/js/ZeroClipboard.swf"
            });
            clip.on('complete', function() {
                alert('复制成功');
            });
        }
    }

    /* upgrade page */
    if(bodyId === "upgradePage") {
        (function(){
        })();
    }
    /* end of upgrade page */

    /* invite page */
    if(bodyId === "invitePage") {
        (function(){

            $("textarea").click(function() {
                $(this).select();
            });

            $("#inviteCopyBtn").click(function() {
                $("textarea").select();
            });
            var clip = new ZeroClipboard($("#inviteCopyBtn"), {
                moviePath: "/static/js/ZeroClipboard.swf"
            });
            clip.on('mouseup', function() {
                clip.setText($('textarea').val());
            });
            clip.on('complete', function() {
                alert('复制成功');
            });

            $(".upgrade .block").hover(
                function() {
                    var block = $(this);
                    if(!block.hasClass("selected")) block.addClass("hover")
                },
                function() {
                    $(this).removeClass("hover");
                }
            );

        })();
    }
    /* end of invite page */

    /* poster page */
    if(bodyId === 'actPosterPage') {
        (function(){

            /* delete poster */
            $(".deletePosterBtn").click(function(e) {
                var a = $(this);
                var pid = a.data('pid');
                var box = $("#instantConfirmBox");
                box.data('action', 'deletePoster').data('poster_id', pid).data('block', getParent(a, ".poster"));
                $(".html", box).text("您确定删除这个海报吗？");
                $(".buttons .grayBtn", box).text("不删除");
                $(".buttons .lightGreenBtn", box).text("删除");
                var x = e.pageX - 105;
                var y = e.pageY - 75;
                box.css({'left': x, "top": y})
                    .show("scale", {}, 200);
            });
            /* end of delete poster */

            /* instance confirm box */
            $("#instantConfirmBox .cancel").click(function() {
                $("#instantConfirmBox").hide("fade", {}, 200);
            });
            $("#instantConfirmBox .confirm").click(function() {
                var box = $("#instantConfirmBox");
                box.hide("fade", {}, 200);
                if(box.data('action') === 'deletePoster') {
                    var poster_id = box.data('poster_id');
                    var block = box.data("block");
                    $.post('/huodong/poster-delete-' + poster_id, {t: Math.random()}, function(res) {
                        if(res.success === 1) {
                            block.fadeOut(function() {
                                block.remove();
                            });
                        }
                        else {
                            alert(res.msg);
                        }
                    });
                }
            });
            /* end of instance confirm box */

            var sumHeight = 0;
            // init selector position
            $(".editingArea .selector").each(function() {
                var selector = $(this);
                var block = getParent(selector, ".block");
                var arrow = $(".arrow", selector);
                // arrow position
                arrow.css("top", block.height()/2);
                // selector position
                selector.css("top", sumHeight + 8).data("top", sumHeight + 8);
                sumHeight += block.height();
            });

            $(".editingArea div.trigger").live("click", function() {
                var a = $(this);
                var block = getParent(a, ".block");
                var selector = $(".selector", block);
                if(selector.height() > 400) {
                    var arrow = $(".arrow", selector);
                    var top = selector.data("top");
                    arrow.css("top", top + 30);
                    selector.css("top", 8);
                }

                if(!block.hasClass("activeBlock")) {
                    var oldActiveBlock = $(".editingArea .activeBlock");
                    if(oldActiveBlock.length > 0) {
                        oldActiveBlock.removeClass("activeBlock");
                        $(".selector", oldActiveBlock).animate({width: 'toggle'}, 350);
                    }
                }

                // toggle block class
                block.toggleClass("activeBlock");

                if($(".activeBlock").length > 0) {
                    $(".previewArea table").css("opacity", 0.3);
                }
                else {
                    $(".previewArea table").css("opacity", 1);
                }

                selector.animate({width: 'toggle'}, 350);
            });

            $("body").click(function(e) {
                var target = $(e.target);
                if(!domNodeIsContainedBy(target[0], $(".editingArea")[0])) {
                    $(".activeBlock div.trigger").trigger("click");
                }
            });

            /* item page view */
            var ItemPageView = function(item) {
                var _this = this;

                this.num_iid = item.num_iid;
                this.pic_url = item.pic_url;
                this.price = item.price;
                this.n_price = item.n_price;
                this.title = item.title;

                this.discount = round2(item.n_price*10/item.price, 2);
                if(item.n_price) this.newPriceRange = item.n_price;
                else this.newPriceRange = 0;
                this.oldPriceRange = item.price;
                this.isSelected = ko.observable(false);
            }

            /*
             * 可以被选择的商品列表view
             * 继承它的类必须重写 updatePageItems 函数，表示页面刷新时的行为（比如翻页）
             * */
            var SelectableItemsView = function() {
                var _this = this;
                this.items = ko.observableArray([]);
                this.updatePageItems = {}; // 页面刷新时调用的函数，需要在继承的类中重写
                this.showSelectCountLimitMsg = {}; // 当到达选择商品上限时显示的信息，需要在继承的类中重写

                this.selectCountLimit = 100; // 当前最多选择多少个

                this.searchInput = ko.observable('');

                // 页码相关
                this.pageSize = 9;
                this.pageNo = ko.observable(1);
                this.totalPage = ko.observable(1);
                this.toSomePageInput = ko.observable(2);

                // 排序
                // 0-> modified, 1-> modified:desc,
                // 2-> delist_time, 3-> delist_time:desc,
                // 4-> num, 5-> num:desc
                this.orderBy = ko.observable(0);
                this.orderByText = ko.computed(function() {
                    var orderByTextArr = [
                        'modified:desc', 'modified:asc',
                        'delist_time:desc', 'delist_time:asc',
                        'num:desc', 'num:asc'
                    ];
                    return orderByTextArr[_this.orderBy()];
                });

                this.orderBy.subscribe(function() { // 排序方式变了，要刷新页面
                    _this.pageNo(1);
                    _this.updatePageItems();
                });

                // 橱窗
                this.isShowcase = ko.observable(0);
                this.isShowcase.subscribe(function() { // 是否显示橱窗商品切换了，要刷新页面
                    _this.pageNo(1);
                    _this.updatePageItems();
                });

                this.selectedItems = ko.observableArray([]);

                this.selectedItemIds = ko.computed(function() {
                    var itemsIids = Array();
                    var items = _this.selectedItems();
                    for (var i=0;i<items.length; i++)
                        itemsIids.push(items[i].num_iid);
                    return itemsIids;
                });

                this.selectedCount = ko.computed(function() {
                    return _this.selectedItems().length;
                });

                this.toggleSelectItem = function(d, e) { // 选择(或取消选择)某个宝贝
                    var isSelected = d.isSelected();
                    if(isSelected) {
                        d.isSelected(false);
                        var selectedItems = _this.selectedItems();
                        for(var i=0,max=selectedItems.length; i<max; i++) {
                            var item = selectedItems[i];
                            if(item.num_iid === d.num_iid) {
                                _this.selectedItems.splice(i, 1); // 从已选择列表中删除
                                break;
                            }
                        }
                    }
                    else {
                        if (_this.selectCountLimit <= _this.selectedCount()) {
                            _this.showSelectCountLimitMsg();
                            return;
                        }
                        d.isSelected(true);
                        _this.selectedItems.push(d);    // 加入到已选择商品
                    }
                }

                // 全选
                this.selectAll = function() {
                    for(var i=0,max=_this.items().length; i<max; i++) {
                        if (_this.selectCountLimit <= _this.selectedCount()) {
                            _this.showSelectCountLimitMsg();
                            return false;
                        }
                        var item = _this.items()[i];
                        if(!item.isSelected()) {
                            item.isSelected(true);
                            _this.selectedItems.push(item);    // 加入到已选择商品
                        }
                    }
                };

                this.toPrevPage = function() {
                    _this.pageNo(_this.pageNo()-1);
                    _this.updatePageItems();
                }
                this.toNextPage = function() {
                    _this.pageNo(_this.pageNo()+1);
                    _this.updatePageItems();
                }
                this.toSomePage = function() {
                    var p = +$.trim(_this.toSomePageInput());
                    if(isNaN(p) || p < 1 && p > _this.totalPage()) {
                        alert("页码需要在 1 到 " + _this.totalPage() + " 的范围内");
                        return false;
                    }
                    _this.pageNo(p);
                    _this.updatePageItems();
                }
            }

            /* 选择淘宝商品的统一view */
            var SearchItemsView = function() {
                var _this = this;
                SelectableItemsView.apply(this);

                this.searchURL = '/huodong/zhekou-search';
                this.selectCountLimit = + $('#targetMax').val(); // 投放到多少商品

                this.itemType = ko.observable(0); /* 0： 出售中 1：仓库中 2：已选择 */

                this.changeItemType = function(type) {
                    _this.itemType(type);
                    if(type == 0 || type == 1) {
                        _this.search();
                    }
                    else if(type == 2) {
                        _this.items(_this.selectedItems());
                    }
                }

                this.updatePageItems = function() {
                    _this.search();
                }

                this.showSelectCountLimitMsg = function() {
                    if(window.user_level === MZ_CONSTANT.USER_LEVEL_0) {
                        $("#upgradeModal .title").text('高级版用户可选更多商品');
                        $("#upgradeModal p").html('初级版只能选择<strong>2个</strong>商品，升级高级版选择更多：');
                        loadModal("#upgradeModal");
                    }
                    else if(window.user_level === MZ_CONSTANT.USER_LEVEL_1) {
                        $("#upgradeModal .title").text('高级版用户可选更多商品');
                        $("#upgradeModal p").html(
                            '标准版只能选择<strong>2个</strong>商品，升级高级版选择更多：' +
                                '<br><strong class="orange">升级只需补差价</strong>');
                        loadModal("#upgradeModal");
                    }
                    else {
                        alert('最多只能选择' + _this.selectCountLimit + '个商品');
                    }
                    return false;
                }

                this.search = function() { // 搜索商品
                    var storeCat = $('#selectStoreCategory').val();
                    var taobaoCat = $('#selectTaobaoCategory').val();

                    var data = {};
                    // type
                    data['type'] = _this.itemType();
                    // page
                    data['page_size'] = _this.pageSize;
                    data['page_no'] = _this.pageNo();
                    // order by
                    data['order_by'] = _this.orderByText();
                    // 橱窗
                    if(_this.isShowcase()) data['has_showcase'] = true;
                    // query string
                    var q = $.trim(_this.searchInput());
                    if(q.length > 0) {
                        var id = getQueryString(q, 'id');
                        if(id !== '') q = id;
                        data['q'] = q;
                    }
                    // category
                    if (storeCat) data['scids'] = storeCat;
                    if (taobaoCat) data['cid'] = taobaoCat;

                    $.post(_this.searchURL, data, function(res) {
                        if(!res.success) {
                            alert(res.msg);
                            return false;
                        }
                        var items = res.res;
                        var ret = [];
                        for(var i=0,max=items.length; i<max; i++) {
                            var item = items[i];
                            ret.push(new ItemPageView(item));
                            if($.inArray(ret[i].num_iid, banner_targets) !== -1) ret[i].isInPoster = true;
                            else ret[i].isInPoster = false;
                            for(var j=0,maxj=_this.selectedItems().length; j<maxj; j++) {
                                if(_this.selectedItems()[j].num_iid === ret[i].num_iid) {
                                    ret[i].isSelected(true);
                                }
                            }
                        }
                        _this.items(ret);
                        _this.totalPage(res.total_pages);
                    }, 'json');
                };
            }
            /* end of search items view */

            /* 活动备选商品的view */
            var ActItemsView = function() {
                var _this = this;
                SelectableItemsView.apply(this);

                this.selectCountLimit = + $('#bannerMax').val(); // 海报中最多多少商品

                this.allItems = ko.observableArray([]);
                this.itemType = ko.observable(0); // 0: 活动商品 2：已选择

                this.searchInput = ko.observable('');
                this.searchInput.subscribe(function() {
                    _this.pageNo(1);
                    _this.updatePageItems();
                });

                this.changeItemType = function(val) {
                    _this.itemType(val);
                    if(val == 0) {
                        _this.updatePageItems();
                    }
                    else {
                        _this.items(_this.selectedItems());
                    }
                }

                this.updatePageItems = function() {
                    var s = $.trim(_this.searchInput());
                    var items = [];
                    if(s !== '') {
                        s = s.toLowerCase();
                        var id = getQueryString(s, 'id');
                        for(var i=0,max=_this.allItems().length; i<max; i++) {
                            var item = _this.allItems()[i];
                            var title = item.title.toLowerCase();
                            if(s == item.num_iid || id == item.num_iid
                                || s == item.outer_id || title.indexOf(s) !== -1) {
                                items.push(item);
                            }
                        }
                    }
                    else {
                        items = _this.allItems();
                    }
                    var total = Math.floor(items.length/_this.pageSize+0.00000001);
                    if(items.length%_this.pageSize > 0) total++;
                    _this.totalPage(total);
                    var start = (_this.pageNo()-1)*_this.pageSize;
                    var end = start + _this.pageSize;
                    _this.items(items.slice(start, end));
                }

                this.showSelectCountLimitMsg = function() {
                    alert('最多只能选择' + _this.selectCountLimit + '个商品');
                    return false;
                }
                /* 初始化备选的海报商品列表 */
                this.initAllItems = function(items) {
                    var ret = [];
                    // parse data
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        var selectableItem = new ItemPageView(item);
                        ret.push(new ItemPageView(item));
                    }
                    _this.allItems(ret);
                    _this.totalPage(Math.ceil(ret.length/_this.pageSize-0.00000001));
                    _this.updatePageItems();
                }

            }

            // convert an ItemPageView object to preview item
            var convertToPreviewItem = function(item) {
                var ret = {};
                ret.num_iid = item.num_iid;
                ret.pic_url = item.pic_url;
                ret.price = item.price;
                ret.title = item.title;
                ret.discount = item.discount;
                ret.n_price = item.n_price;
                ret.valid = true;
                return ret;
            }

            /* poster page view */
            var posterPreivewImgDict = {
                // 预览图链接的对应表
                // 1行2个
                '0_750_2': 'http://img03.taobaocdn.com/bao/uploaded/i3/15163023688083515/T1bQ5bXppfXXXXXXXX_%21%212-item_pic.png',
                '1_750_2': 'http://img02.taobaocdn.com/bao/uploaded/i2/15163021767226125/T1.YmbXyFeXXXXXXXX_%21%212-item_pic.png',
                '2_750_2': 'http://img01.taobaocdn.com/bao/uploaded/i1/15163021951545543/T1eWKiXzFXXXXXXXXX_%21%210-item_pic.jpg',
                // 1行1个
                '0_750_1': 'http://img02.taobaocdn.com/bao/uploaded/i2/15163022167065004/T1tLGmXBFXXXXXXXXX_%21%210-item_pic.jpg',
                '1_750_1': 'http://img03.taobaocdn.com/bao/uploaded/i3/15163023339177249/T1teSIXDNfXXXXXXXX_%21%210-item_pic.jpg'
            }
            var PosterPageView = function () {
                var _this = this;

                this.selectedColumnCount = ko.observable(1); // 每行显示几个
                this.selectColumnCount = function(id, d, e) {
                    _this.selectedColumnCount(id);
                }
                this.selectedColumnCount.subscribe(function() {
                    _this.selectedStyleId(0);
                });

                this.selectedStyleId = ko.observable(0);
                this.selectStyle = function(id,d,e) {
                    _this.selectedStyleId(id);
                }

                /* 海报备选商品 */
                this.posterItemsView = new ActItemsView();

                /* 预览相关 */
                this.selectedProductsByLine = ko.computed(function() {
                    var items = _this.posterItemsView.selectedItems();
                    var ret = [];
                    var retlen = 0;
                    var retItemLen = 0;
                    for(var i=0,max=items.length; i<max; i++) {
                        var item = items[i];
                        if(item.isSelected()) { // add to list
                            var obj = convertToPreviewItem(item);
                            if(retItemLen%_this.selectedColumnCount() === 0) {
                                ret.push({'items': []});
                                retlen++;
                            }
                            ret[retlen-1].items.push(obj);
                            retItemLen++;
                        }
                    }

                    return ret;
                });
                this.posterPreviewImg = ko.computed(function() {
                    var templateId = _this.selectedStyleId() + '_750_' + _this.selectedColumnCount();
                    return posterPreivewImgDict[templateId];
                });

                this.templateName = ko.computed(function() { // 生成当前选择的模板名称 styleId + width + 一行显示几个

                    var templateId = _this.selectedStyleId() + '_750_' + _this.selectedColumnCount();

                    // 看template是不是存在
                    var node = document.getElementById(templateId);
                    if(node == null) { // 不存在的话就读取进来
                        var templatePath = '/huodong/poster-preview-' + templateId;
                        var templateHtml = null;
                        $.ajax({
                            "url": templatePath,
                            "async": false,
                            "dataType": "html",
                            "type": "GET",
                            "success": function(response) { templateHtml = response; }
                        })

                        if(templateHtml === null)
                            throw new Error("找不到模板 " + templateId);

                        var node = $("<script type='text/x-jquery-tmpl' id='"+templateId+"'>"+templateHtml+"</script>");
                        node.appendTo("body");
                    }
                    // 返回id
                    return templateId;
                });

                /* 用户要把海报投放到哪些商品 */
                this.searchItemsView = new SearchItemsView();
            }
            /* end of poster page view */

            var posterPageView = new PosterPageView();
            posterPageView.posterItemsView.initAllItems(ko.utils.parseJson(items));
            posterPageView.searchItemsView.search();

            ko.applyBindings(posterPageView);

            // 选择淘宝商品的统一view： 相关的事件和函数
            $('#selectStoreCategory').change(function(){
                $('#selectTaobaoCategory option:eq(0)').prop('selected',true);
                posterPageView.searchItemsView.searchInput('');
                posterPageView.searchItemsView.pageNo(1);
                posterPageView.searchItemsView.search();
            });

            $('#selectTaobaoCategory').change(function(){
                $('#selectStoreCategory option:eq(0)').prop('selected',true);
                posterPageView.searchItemsView.searchInput('');
                posterPageView.searchItemsView.pageNo(1);
                posterPageView.searchItemsView.search();
            });

            $('.editingArea .submitBtn').click(function(){
                var posterNumIids = posterPageView.posterItemsView.selectedItemIds();
                var targetNumIids = posterPageView.searchItemsView.selectedItemIds();
                var col = posterPageView.selectedColumnCount();
                var style = posterPageView.selectedStyleId();
                var aid = $('#aid').val();
                var data = {aid: aid, colnum: col, style: style, posterNumIids: posterNumIids, targetNumIids: targetNumIids}
                data = JSON.stringify(data);

                if(posterNumIids.length === 0) {
                    alert('您还没有选择海报的内容');
                    $(".editingArea .block").eq(0).effect("highlight", {color: '#FF4E4A'}, 700, function() {
                        $(this).effect("highlight", {color: '#FF4E4A'}, 700);
                    });
                    return false;
                }
                if(targetNumIids.length === 0) {
                    alert('您还没有选择要投放海报的商品');
                    $(".editingArea .block").eq(2).effect("highlight", {color: '#FF4E4A'}, 700, function() {
                        $(this).effect("highlight", {color: '#FF4E4A'}, 700);
                    });
                    return false;
                }

                var btn = $(this);
                btn.addClass("grayBtn").text("投放中...");
                $.post('/huodong/poster-create', {data: data}, function(res){
                    if (res.success) {
                        window.location.href = '/huodong/poster-ok?t=' + Math.random();
                    } else {
                        btn.removeClass("grayBtn").text("确定投放");
                        alert(res.msg);
                    }
                }, 'json');

            });
        })()
    }
    /* end of poster page */

    /* user info page */
    if(bodyId === 'userInfoPage') {
        (function(){

            $("#inviteBtn").click(function() {
                var val = $("#inviteField").val();
                val =$.trim(val);
                if(val !== '' && val !== '谁邀请了您') {
                    $.post('invite_by', {nick: val}, function(res) {
                        if(res.success) {
                            $("#inviteField").hide();
                            $("#inviteBtn").hide();
                            $("#inviteDD").text(val);
                        }
                        else {
                            alert(res.msg);
                        }
                    }, 'json');
                }
                else {
                    alert('请填写邀请您的用户');
                }
            });


        })();
    }
    /* end of user info page */

    /* postfee page */
    if(bodyId === 'postfeePage') {


    }
    /* end of postfee page*/

    /* zkzq page */
    if(bodyId === 'zkzqPage') {
        (function(){

            $(".zkzqCard .mask").css("opacity", 0.7);

            $(".zkzqCard .leftSide a, .zkzqCard .rightSide a, .zkzqCard .category a").hover(
                function() {
                    var li = $(this).parent();
                    $("a", li).addClass("hover");
                },
                function() {
                    var li = $(this).parent();
                    $("a", li).removeClass("hover");
                }
            );
            $(".zkzqCard .leftSide a, .zkzqCard .rightSide a").click(function() {
                var li = $(this).parent();
                var ul = li.parent();
                var btn = $(".posterBtn", li);
                var type = 0;
                if (ul.hasClass('leftSide')) type = 2;
                else type = 3;
                if(!btn.hasClass("lightGrayBtn")) {
                    $.post(window.location.href,{'type':type,value: li.data('theme')},function(ret){
                        if (ret.success){
                            $("a span", ul).hide();
                            $("a span", li).show();
                            $("a", ul).removeClass("selected");
                            $("a", li).addClass("selected");
                        }else{
                            alert(ret.msg);
                        }
                    },'json');
                }
            });
            $(".zkzqCard .category a").click(function() {
                var li = $(this).parent();
                var uls = li.parent().parent().parent();
                var btn = $(".tmplBtn", li);
                if(!btn.hasClass("lightGrayBtn")) {
                    $.post(window.location.href,{type:1,value: li.data('theme')},function(ret){
                        if (ret.success){
                            $("a span", uls).hide();
                            $("a span", li).show();
                            $("a", uls).removeClass("selected");
                            $("a", li).addClass("selected");
                        }else{
                            alert(ret.msg);
                        }
                    },'json');
                }
            });

            $(".filter .tabs a").click(function() {
                var a = $(this);
                var tabs = a.parent();
                $("a", tabs).removeClass("active");
                a.addClass("active");
                var idx = $("a", tabs).index(a);
                $(".zkzqCards .zkzqCard").hide().eq(idx).slideDown(function() {
                    $(this).find("li").hide().show()
                });
            });

            $("#zkzqBtn").click(function() {
                $.post('zkzq_open', function(res) {
                    if(res.success) {
                        window.location.href = window.location.href;
                    }
                    else {
                        alert(res.msg);
                    }
                }, 'json');
            });

            $("#refreshZKZQ").click(function() {
                if (confirm('折扣专区数据更新消耗资源非常大，为了让更多用户可以正常使用折扣专区，我们限制了每个用户更新的间隔为一个小时，如果您本次更新后，将在一小时后才能再次更新，点确定开始更新，点取消暂时不更新。')){
                    $.post('zkzq_refresh', {t:Math.random()}, function(res) {
                        alert(res.msg);
                    });
                }
            });
            $("#refreshZKZQ2").click(function() {
                if (confirm('折扣专区数据更新消耗资源非常大，为了让更多用户可以正常使用折扣专区，我们限制了每个用户更新的间隔为一个小时，如果您本次更新后，将在一小时后才能再次更新，点确定开始更新，点取消暂时不更新。')){
                    $.post('show-update', {t:Math.random()}, function(res) {
                        alert(res.msg);
                    });
                }
            });

        })();
    }
    /* end of zkzq page */


    /* footer的广告 */
    setTimeout(function() { //初始化广告信息
        $.get('/huodong/extra', function(res) {
            var data = res.data;
            var v = [];
            for(var i=0,max=data.length; i<max; i++) {
                //检查条件
                var d = data[i];
                if(typeof d.shop_type !== 'undefined'
                    && (d.shop_type !== tb_shop_type && d.shop_type !== 'A')) continue; // 用户C店还是B店
                if(typeof d.taobao_level_min !== 'undefined' && d.taobao_level_min > taobao_level) continue;  // 用户美折最低等级
                if(typeof d.shop_level_min !== 'undefined' && d.shop_level_min > tb_shop_level) continue;   // 店铺最低等级
                if(typeof d.shop_level_max !== 'undefined' && d.shop_level_max < tb_shop_level) continue;   // 店铺最高等级
                if(typeof d.shop_cid_type !== 'undefined') {    // 店铺类目
                    var valid = false;
                    if(d.shop_cid_type === 'A') { // 包括
                        valid = false;
                        for(var j=0,maxj= d.shop_cid_list.length; j<maxj; j++) {
                            if(d.shop_cid_list[j] === tb_shop_cid) {
                                valid = true;
                                break;
                            }
                        }
                    }
                    else if(d.shop_cid_type === 'X') { // 排除
                        valid = true;
                        for(var j=0,maxj= d.shop_cid_list.length; j<maxj; j++) {
                            if(d.shop_cid_list[j] === tb_shop_cid) {
                                valid = false;
                                break;
                            }
                        }
                    }
                    if(!valid) continue;
                }
                // 通过验证
                v.push(d);
            }
            if(v.length > 0) {
                var t = getRandomInt(0, v.length-1);
                var links = $("#footer .footerLinks");
                var a = $('<a target="_blank" class="link" data-ga="true" data-ga-name="'+v[t].code+'"></a>');
                a.attr({
                    href: v[t].url,
                    alt: v[t].desc
                });
                a.css({
                    "background": "url("+v[t].img_url+") 0 0 no-repeat",
                    "width": "60px"
                });
                links.prepend(a);

                // track event
                var page = $("body").attr("id");
                var name = v[t].code + '_show';
                _gaq.push(['_trackEvent', 'displayCount', name, page]);
            }

        }, 'json');
    }, 1);

    if($("#flashed_message_global_alert").length > 0) {
        var alertCats = null;
        if(typeof global_alert_to_category !== 'undefined' && global_alert_to_category !== ''
            && global_alert_to_category !== 'None') {
            var arr = global_alert_to_category.split(',');
            for(var i=0,max=arr.length; i<max; i++) {
                if(tb_shop_cid == arr[i]) {
                    $("#flashed_message_global_alert").slideDown();
                }
            }
        }
        else {
            $("#flashed_message_global_alert").slideDown();
        }
    }
});

/************** Modal Window Function **************/
function loadModal(itemId, btn, e) {
    if(itemId === '#hraBox') {
        var x = e.pageX - 165;
        var y = e.pageY - 60;
        box = $("#hraBox");
        box.css({'left': x, "top": y})
            .show("scale", {}, 200, function() {
                box.effect("highlight", {color: '#FFF4E8'}, 3000);
            });
    }
    else {
        $(itemId).lightbox_me({
            appearSource: btn
        });
    }
}
function closeModal() {
    $('.modal .closeModal').trigger("click");
}
$(".modal .closeModal").click(function() {
    $("#hraBox").hide();
});

/*****
 * google analytic mouse click record
 */
$(document).ready(function() {
    var page = $("body").attr("id");
    // button click
    $("a[data-ga]").live("click", function() {
        var anchor = $(this);
        var name = anchor.data("ga-name");
        _gaq.push(['_trackEvent', 'ButtonClick', name, page]);
    });
    // button hover
    $(".recommendBtn").live("hover", function() {
        var anchor = $(this);
        var name = anchor.data("ga-name");
        _gaq.push(['_trackEvent', 'ButtonHover', name, page]);
        _gaq.push(['_trackEvent', 'RecommendDiscount', 'hover', page]);
    });
    // user track
    var tb_version = $("#tb_version").val();
    var tb_version_name = 'gaoji_user';
    if(tb_version == 2) {
        var name = 'gaoji_user';
        _gaq.push(['_trackEvent', 'userTrack', name, page]);
    }
    else {
        var name = 'chuji_user';
        tb_version_name = name;
        _gaq.push(['_trackEvent', 'userTrack', name, page]);
    }
    // shop_type_track
    if(tb_shop_type === 'C') {
        var name = 'C_user';
        _gaq.push(['_trackEvent', 'userTrack', name, page]);
    }
    else {
        var name = 'B_user';
        _gaq.push(['_trackEvent', 'userTrack', name, page]);
    }
    // status_track
    if($("#last7daysAlertInput").length > 0) {
        var name = 'last7days_' + tb_shop_type + '_' + tb_version_name;
        _gaq.push(['_trackEvent', 'statusTrack', name, page]);
    }
    // upgrade page a/b test
    /*if($("body").attr("id") === 'upgradePage') {
     var t = $("#upgradePageType").val();
     $("a.lightGrayBtn").click(function() {
     var anchor = $(this);
     var name = t + '__' + anchor.data("ga-name");
     _gaq.push(['_trackEvent', 'ABTest', name, page]);
     });
     }*/
});
function recordByGA(type, label, value) {
    _gaq.push(['_trackEvent', type, label, value]);
}

function round2(number,fractionDigits){
    with(Math){
        return round(number*pow(10,fractionDigits))/pow(10,fractionDigits);
    }
}