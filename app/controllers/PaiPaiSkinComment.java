package controllers;

import java.util.List;

import models.comment.CommentConf;
import models.comment.Comments;
import models.paipai.PaiPaiUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMPaginger;
import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;

import configs.TMConfigs.PageSize;
import dao.comments.CommentsDao;

public class PaiPaiSkinComment extends PaiPaiController{
	
    private static final Logger log = LoggerFactory.getLogger(PaiPaiSkinComment.class);
        
    public static final String TAG = "PaiPaiSkinComment";

    public static void index(){
    	 render("paipaimanage/skincomment.html");
    }
    
    public static void commentConf() {
        PaiPaiUser user = getUser();
        CommentConf conf = CommentConf.findByUserId(user.getId());
        if (conf == null) {
            conf = new CommentConf(user.getId(), user.getNick(), "欢迎再次光临!@#");
            conf.jdbcSave();
        }
//        conf.setBadCommentNotice(user.isBadCommentNoticeOn());
//        conf.setBadCommentBuyerSms(user.isBadCommentBuyerSmsOn());
        renderJSON(JsonUtil.getJson(conf));
    }
    
    public static void updateConf(Long commentType, Long commentTime, boolean badCommentNotice,
            boolean badCommentBuyerSms, String badCommentMsg) {
        
        if (commentTime == null || (commentType > 0 && (commentTime <= 0 || commentTime > 15))) {
            renderJSON(TMResult.renderMsg("请修改错误后提交<br><br>填写的天数必须为大于0且小于15的整数！"));
        }

        PaiPaiUser user = getUser();
        CommentConf conf = CommentConf.findByUserId(user.getId());
        if (conf == null) {
            conf = new CommentConf(user.getId(), user.getNick(), "欢迎再次光临!@#");
        }
        conf.setCommentType(commentType);
//        conf.setCommentDays(commentTime);
//        conf.setBadCommentMsg(badCommentMsg);
        conf.jdbcSave();

//        user.setBadCommentNoticeOn(badCommentNotice);
//        user.setBadCommentBuyerSmsOn(badCommentBuyerSms);
//        user.jdbcSave();
//        
//        if (badCommentNotice == true || badCommentBuyerSms == true) {
//            TaobaoUtil.permitByUser(user);
//        }
        
        renderJSON(TMResult.renderMsg("自动评价 - 设置修改成功"));
    }
    
    public static void isOn() {
        //renderJSON(JsonUtil.getJson(new TMResult(TaobaoUtil.checkUserPermitted(getUser().getUserNick()))));
        renderJSON(JsonUtil.getJson(new TMResult(getUser().isAutoCommentOn())));
    }
    
    public static void currContent() {
    	PaiPaiUser user = getUser();
        //CommentConf commentConf = CommentConf.find("userId = ?", getUser().getId()).first();
        CommentConf commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null || commentConf.getCommentContent().isEmpty()) {
            commentConf = new CommentConf(user.getId(), user.getNick(), "欢迎再次光临!@#");
            commentConf.jdbcSave();
            //renderJSON(JsonUtil.getJson(new TMResult(true, null, "")));
        }
        renderJSON(JsonUtil.getJson(new TMResult(true, null, commentConf.getCommentContent())));
    }
    
    public static void setOn() {
    	PaiPaiUser user = getUser();
        boolean isSuccess = true;
        user.setAutoCommentOn(true);
        isSuccess = user.jdbcSave();

        //if (CommentConf.find("userId = ?", user.getId()).first() == null) {
        if (CommentConf.findByUserId(user.getId()) == null) {
            isSuccess = isSuccess && new CommentConf(user.getId(), user.getNick(), "欢迎再次光临!@#").jdbcSave();
        }
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void setOff() {
    	PaiPaiUser user = getUser();
        boolean isSuccess = true;
        user.setAutoCommentOn(false);
        isSuccess = user.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }
    
    public static void setContent(String content) {
        if (content.contains("http://"))
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        PaiPaiUser user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            commentConf = new CommentConf(user.getId(), user.getNick(), content + "!@#");
        else
            commentConf.setCommentContent(content + "!@#");
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }
    
    public static void editContent(String oldContent, String newContent) {
        if (newContent.contains("http://"))
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        PaiPaiUser user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            commentConf = new CommentConf(user.getId(), user.getNick(), newContent + "!@#");
        else
            commentConf.editCommentContent(oldContent, newContent);
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }
    
    public static void addContent(String content) {
        if (content.isEmpty()) {
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        }
        PaiPaiUser user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            commentConf = new CommentConf(user.getId(), user.getNick(), content + "!@#");
        else
            commentConf.addCommentContent(content);
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void deleteContent(String content) {
    	PaiPaiUser user = getUser();
        boolean isSuccess = true;
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        if (commentConf == null)
            renderJSON(JsonUtil.getJson(new TMResult(false)));
        else
            commentConf.deleteCommentContent(content);
        isSuccess = commentConf.jdbcSave();
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void getCommentLog(int pn, int ps) {
    	PaiPaiUser user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;

        List<Comments> list = CommentsDao.findOnlineByUser(user.getId(), (pn - 1) * ps, ps);
        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有自动评价操作日志哦！！！！！");
        }

        TMPaginger tm = new TMPaginger(pn, ps, (int) CommentsDao.countOnlineByUser(user.getId()), list);
        renderJSON(JsonUtil.getJson(tm));
    }
}
