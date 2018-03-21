/**
 * 分页
 * @type {*}
 */
var Paging = Paging || {};

Paging.util = Paging.util || {};

Paging.util = $.extend({
    defaultOpt: {
        countUrl: "",
        dataUrl: "",
        pageSize: 5,
        startPage: 1,
        getRuleData: function(isCurrentPage, currentPage) {
            return {};
        },
        parseTotalCount: function(resultJson) {
            return resultJson;
        },
        queryCallback: function(dataJson) {

        }
    },
    setPaging: function(opt, pagingObj) {
        opt = $.extend({}, Paging.util.defaultOpt, opt);

        var data = opt.getRuleData(false, opt.startPage);
        $.ajax({
            url: opt.countUrl,
            dataType: 'json',
            type: 'post',
            global: false,
            data: data,
            error: function() {
            },
            success: function (resultJson) {
                var totalCount = opt.parseTotalCount(resultJson);
                if (totalCount === undefined)
                    return;
                Paging.util.initPagination(opt, pagingObj, totalCount);
            }
        });
    },
    initPagination: function(opt, pagingObj, totalCount) {
        var callback = function(currentPage, jq) {
            Paging.util.queryData(opt, currentPage);
        };
        pagingObj.pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            callback : callback,
            current_page: opt.startPage - 1,
            link_to: 'javascript:void(0);',
            items_per_page : opt.pageSize,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    queryData: function(opt, currentPage) {
        var data = opt.getRuleData(true, currentPage + 1);
        Loading.init.show({});
        $.ajax({
            //async : false,
            url : opt.dataUrl,
            data : data,
            type : 'post',
            global: false,
            error: function() {
            },
            success: function (dataJson) {
                opt.queryCallback(dataJson);
            }
        });
    }
}, Paging.util);


(function($) {
    $.fn.setPaging = function(options) {
        Paging.util.setPaging(options, $(this));
    }
})(jQuery);