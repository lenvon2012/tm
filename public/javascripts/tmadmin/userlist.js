$(document).ready(function(){

    var crud = $('#main');
    var dayMillis = 86400000;
    var curr = new Date().getTime();
    $('#rptOrderStart').val(new Date(curr-(2 * dayMillis)).formatYMSH());
    $('#rptOrderEnd').val(new Date(curr-(1 * dayMillis)).formatYMSH());
    $('#rptOrderQuery').click(function(){
        var start = $('#rptOrderStart').val();
        var end = $('#rptOrderEnd').val();
        window.open('/tmadmin/userReport?start='+start+'&end='+end);
    });

    var params = {};
    params.pn = 1;
    params.ps = 10;

    var artileTmpl = $('#articleRowTmpl');
    var articleTable = $('#articleTable');

    var startTimeInput = $("#startTimeInput");
    var endTimeInput = $("#endTimeInput");
    startTimeInput.datepicker();
    startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
    startTimeInput.val(new Date(curr - 100 * dayMillis).formatYMS());

    endTimeInput.datepicker();
    endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
    endTimeInput.val(new Date(curr).formatYMS());

    var renderUserList = function(){
        params.startYMS = startTimeInput.val();
        params.endYMS = endTimeInput.val();
        params.nick = $('#nickinput').val();

        crud.find('.pagenav').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                param : params,
                on: true,
                dataType: 'json',
                url: "/TMAdmin/list",
                callback:function(data){
                    var editComment = function(){

                    }

                    var tbody = articleTable.find('tbody');
                    tbody.empty();
                    var rows = artileTmpl.tmpl(data.res);
                    tbody.append(rows);


                    tbody.find('.editcontentbtn').click(function(){
                        var btn = $(this);
                        var td = btn.parent().parent().parent();
                        td.find('.showcontent').hide();
                        td.find('.editcontent').show();
                    });
                    tbody.find('.savecontentbtn').click(function(){
                        var btn = $(this);
                        var orderId = btn.attr('orderId');
                        var td = btn.parent().parent().parent();
                        var content = td.find('textarea.content').val();   alert(orderId)  ; alert(content) ;
                        $.post('/buscrm/editoptions',{orderId:orderId,options:content},function(msg){
                            if(msg && msg.length > 0){
                                alert(msg);
                                return;
                            }
                            td.find('div.content').html(content);
                            td.find('.showcontent').show();
                            td.find('.editcontent').hide();
                        });
                    });
                    tbody.find('.cancelcontentbtn').click(function(){
                        var btn = $(this);
                        var td = btn.parent().parent().parent();
                        td.find('.showcontent').show();
                        td.find('.editcontent').hide();
                    });
                }
            }
        });
    }

    crud.find('.reqListUser').click(function(){
        renderUserList();
    });

    renderUserList();
});
