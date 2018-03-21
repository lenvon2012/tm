/**
 * @author lyl
 * @date 2017/11/04
 */
var TM = TM || {};
(function ($, window) {
    TM.Dianquan = TM.Dianquan || {};
    var Dianquan = TM.Dianquan;
    Dianquan.init = Dianquan.init || {};
    Dianquan.init = $.extend({
        doInit: function () {
            this.initConfig();
        },
        initConfig: function () {
            Dianquan.editorVM = new Vue({
                el: '#dianquanMain',
                data: {
                    types: [{
                        name: "全部",
                        id: -1
                    }, {
                        name: "女装",
                        id: 1
                    }, {
                        name: "男装",
                        id: 2
                    }, {
                        name: "内衣",
                        id: 3
                    }, {
                        name: "母婴儿童",
                        id: 4
                    }, {
                        name: "美妆护肤",
                        id: 5
                    }, {
                        name: "家居",
                        id: 6
                    }, {
                        name: "鞋品",
                        id: 7
                    }, {
                        name: "箱包",
                        id: 8
                    }, {
                        name: "配饰",
                        id: 9
                    }, {
                        name: "美食",
                        id: 10
                    }, {
                        name: "文体车品",
                        id: 11
                    }, {
                        name: "家电数码",
                        id: 12
                    }, {
                        name: "医疗保健",
                        id: 13
                    }, {
                        name: "果蔬",
                        id: 14
                    }],
                    selectType: -1,
                    activityFilters: [{
                        name: "双十一",
                        type: "activity"
                    }, {
                        name: "淘抢购",
                        type: "activity"
                    }, {
                        name: "聚划算",
                        type: "activity"
                    }],
                    siteFilters: [{
                        name: "天猫",
                        type: "site",
                        value: "tmall"
                    }, {
                        name: "淘宝",
                        type: "site",
                        value: "taobao"
                    }],
                    typeFilters: [{
                        name: "运费险",
                        type: "freight"
                    }, {
                        name: "视频",
                        type: "video"
                    },],
                    selectActivityFilters: [],
                    selectSiteFilters: [],
                    selectTypeFilters: [],
                    orderType: [{
                        name: "最新",
                        value: 1
                    }, {
                        name: "销量",
                        value: 2
                    }, {
                        name: "佣金比例",
                        value: 3
                    }, {
                        name: "价格",
                        value: 4
                    }],
                    selectOrderType: 1,
                    lowPrice: "",
                    highPrice: "",
                    ratio: "",
                    sales: "",
                    dianquanData: [],
                    pageSize: 15,
                    hoverBtn: "",
                    searchText: "",
                    selectGoods: [],
                    selectAll: false
                },
                methods: {
                    clickType: function (id) {
                        if (this.selectType === id) {
                            return;
                        }
                        this.selectType = id;
                        this.search();
                    },
                    clickOrderType: function (value) {
                        if (this.selectOrderType === value) {
                            return;
                        }
                        this.selectOrderType = value;
                        this.search();
                    },
                    search: function (curr) {
                        var param = this.getParam(curr);
                        if (this.checkParam(param)) {
                            Dianquan.event.searchDianquan(param);
                        }
                    },
                    clear: function () {
                        this.selectActivityFilters = [];
                        this.selectSiteFilters = [];
                        this.selectTypeFilters = [];
                        this.lowPrice = "";
                        this.highPrice = "";
                        this.ratio = "";
                        this.sales = "";
                    },
                    getParam: function (curr) {
                        return {
                            curr: curr || 1,
                            pageSize: this.pageSize,
                            type: this.selectType,
                            activityFilters: this.selectActivityFilters.map(function (filter) {
                                return filter.name;
                            }).join("|"),
                            siteFilters: this.selectSiteFilters.map(function (filter) {
                                return filter.value;
                            }).join("|"),
                            typeFilters: this.selectTypeFilters.map(function (filter) {
                                return filter.type;
                            }).join("|"),
                            lowPrice: this.lowPrice.trim(),
                            highPrice: this.highPrice.trim(),
                            ratio: this.ratio.trim(),
                            sales: this.sales.trim(),
                            order: this.selectOrderType,
                            searchText: this.searchText
                        };
                    },
                    checkParam: function (param) {
                        if (isNaN(param.lowPrice) || isNaN(param.highPrice)) {
                            layer.msg("价格必须为数字", {icon: 5});
                            return false;
                        }
                        if (param.lowPrice.trim() !== "" && param.highPrice.trim() !== "") {
                            if (+param.lowPrice > +param.highPrice) {
                                layer.msg("最低价不得高于最高价", {icon: 5});
                                return false;
                            }
                        }
                        if (isNaN(param.ratio)) {
                            layer.msg("佣金必须为数字", {icon: 5});
                            return false;
                        }
                        if (isNaN(param.sales)) {
                            layer.msg("销量必须为数字", {icon: 5});
                            return false;
                        }
                        return true;
                    },
                    selectAllGoods: function () {
                        if (this.selectAll) {
                            this.selectGoods = [];
                            var that = this;
                            this.dianquanData.forEach(function (data) {
                                that.addSelectGood(data.gid)
                            });
                        } else {
                            this.selectGoods = [];
                        }
                    },
                    addSelectGood: function (gid) {
                        var _index = this.selectGoods.indexOf(gid);
                        if (_index === -1) {
                            this.selectGoods.push(gid)
                        } else {
                            this.selectGoods.splice(_index, 1);
                        }
                    },
                    addCopy: function (gid) {
                        DianQuanJs.createCommodityLink(gid);
                        layer.msg("添加成功", {icon: 6});
                    },
                    batchCopy: function () {
                        if (this.selectGoods.length === 0) {
                            layer.msg("请选择批量导出数据", {icon: 5});
                            return;
                        }
                        this.addCopy(this.selectGoods.join(","))
                    },
                    copyAll: function () {
                        var param = this.getParam();
                        if (this.checkParam(param)) {
                            Dianquan.event.getAllGid(param);
                        }
                    }
                },
                computed: {}
            });
            Dianquan.editorVM.search();
            var clipboard = new Clipboard(".introBtn");
            clipboard.on('success', function (e) {
                layer.ready(function () {
                    layer.msg("成功复制到剪贴板", {icon: 6})
                })
            });
        }
    }, Dianquan.init);

    Dianquan.data = $.extend({
        clearData: function () {
            Dianquan.editorVM.selectGoods = [];
            Dianquan.editorVM.selectAll = false;
        }
    }, Dianquan.data);

    Dianquan.event = $.extend({
        searchDianquan: function (param) {
            $.ajax({
                url: "/DianQuan/searchDianquanList",
                type: 'get',
                data: param,
                success: function (result) {
                    Dianquan.editorVM.dianquanData = result.data;
                    Dianquan.data.clearData();
                    laypage({
                        cont: $(".dianquan-page"),
                        pages: result.extra.page,
                        skip: true,
                        first:1,
                        last:result.extra.page,
                        curr: result.extra.currPage,
                        pageSize: result.extra.pageSize,
                        skin: 'molv',
                        jump: function jump(obj, first) {
                            if (!first) {
                                Dianquan.editorVM.search(obj.curr);
                            }
                        }
                    });
                }
            });
        },
        getAllGid: function (param) {
            $.ajax({
                url: "/DianQuan/getAllGid",
                type: 'get',
                data: param,
                success: function (result) {
                    if (result.length > 500) {
                        layer.msg("选择数据量超过500个，请精准选择后重试", {icon: 5});
                        return;
                    }
                    var _param = result.map(function (data) {
                        return data.gid;
                    }).join(",");
                    DianQuanJs.createCommodityLink(_param);
                    layer.msg("添加成功", {icon: 6});
                }
            });
        }
    }, Dianquan.event)
})(jQuery, window);