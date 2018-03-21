/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 6/20/13
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
((function ($, window) {
    TM.invite = TM.invite || {};
    var me = TM.invite;
    me.init = function(container){
        me.textInput= $("#textinput");
        me.inviterecords = $('.inviterecords');
        me.paginer = me.inviterecords.find('.pagination');
        me.inviteBody = me.inviterecords.find('.invitebody');
        me.reqContent();
        me.reqInivtes();
    }
    me.reqInivtes = function(){
        me.paginer.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/op/li",
                callback:function(data){
                    me.bulildInvites(data);
                }
            }
        });
    }
    me.bulildInvites = function(data){
        me.inviteBody.empty();
        if(!data || !data.res || data.res.length == 0){
            me.inviteBody.append("<tr><td colspan='3'><p>亲,您还没有邀请过其他用户哟！</p></td></tr>");
            return;
        }

        var arr = [];
        $.each(data.res, function(i,elem){
            arr.push('<tr>');
            arr.push('<td>'+elem.nick+'</td>');
            arr.push('<td>'+elem.thisIp+'</td>');
            arr.push('<td>'+new Date(elem.created).formatYMS()+'</td>');
            arr.push('</tr>');
        });
        $(arr.join('')).appendTo(me.inviteBody);
    }

    me.reqContent = function(){
        $.get('/SkinDefender/genInviteUrl',function(data){
            me.textInput.text(me.textInput.text()+data.url);
            me.initCopy('paste',me.getText);
        });
    }

    me.getText = function(){
        return me.textInput.text();
    }

    me.initCopy = function(id, getText){
        ZeroClipboard.setMoviePath("/js/utils/copy.swf");
        var clip = new ZeroClipboard.Client();
        clip.setHandCursor(true);
        clip.setText('');
        clip.addEventListener('mouseDown', function (client) {
            var text =getText();
            clip.setText(text);
            $('#'+id).qtip({
                content: {
                    text: "复制成功~"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        });
        clip.glue(id);
    }
})(jQuery, window));