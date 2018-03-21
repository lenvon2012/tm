package models.vas;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.user.UserTracer;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;

import com.taobao.api.domain.ArticleSub;

@Entity(name = AritcleSubPlay.TABLE_NAME)
public class AritcleSubPlay extends Model {

    private static final long serialVersionUID = 1L;

    @Transient
    private static final Logger log = LoggerFactory.getLogger(AritcleSubPlay.class);

    @Transient
    public static final String TABLE_NAME = "articlesub_";
    
    @Transient
    public static UserTracer EMPTY = new UserTracer();

    @Transient
    static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    /**
     * 淘宝会员名
     */
    @Index(name = "nick")
    private String nick;

    private String articleCode;

    /**
     * 收费项目代码，从合作伙伴后台（my.open.taobao.com）-收费管理-收费项目列表 能够获得收费项目代码
     */
    private String itemCode;

    /**
     * 订购关系到期时间
     */
    private Long deadline;

    /**
     * 是否自动续费
     */
    private Boolean autosub;

    /**
     * 是否到期提醒
     */
    private Boolean expireNotice;

    public static class STATUS {
        // 有效
        public static int VAILD = 1;
        // 过期
        public static int EXPIRED = 2;
        // 续订
        public static int RENEW = 8;
        // 升级
        public static int UPGRADE = 16;
        //付费续订
        public static int RENEW_PAY = 32;

    }

    /**
     * 状态，1=有效 2=过期
     */
    private int status;

    public AritcleSubPlay(ArticleSub articleSub) {
        this.nick = articleSub.getNick();
        this.articleCode = articleSub.getArticleCode();
        this.itemCode = articleSub.getItemCode();
        this.deadline = articleSub.getDeadline().getTime();
        this.autosub = articleSub.getAutosub();
        this.expireNotice = articleSub.getExpireNotice();
        this.status = articleSub.getStatus().intValue();
    }

    @Override
    public void _save() {
        rawInsert();
    }

    public boolean jdbcSave() {
        long existedId = findExistId(this.nick);

        if (existedId == 0) {
            return rawInsert();
        }
        return true;
    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where nick = ? ";

    public static long findExistId(String nick) {
        return dp.singleLongQuery(EXIST_ID_QUERY, nick);
    }

    public boolean rawInsert() {
        long id = dp
                .insert("insert into `articlesub_`(`nick`,`articleCode`,`itemCode`,`deadline`,`autosub`,`expireNotice`,`status`) values(?,?,?,?,?,?,?)",
                        this.nick, this.articleCode, this.itemCode, this.deadline, this.autosub, this.expireNotice,
                        this.status);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[nick : ]" + this.nick);
            return false;
        }
    }

    static String UPDATE_STATUS = "update `articlesub_` set `status` = ?  where `nick` = ? ";

    public static boolean rawUpdateStatus(String nick, int status) {
        long updateNum = dp.insert(UPDATE_STATUS, status, nick);

        if (updateNum == 1) {
            log.info("update ok for :" + nick);
            return true;
        } else {
            log.error("update failed...for :" + nick);
            return false;
        }
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Long getDeadline() {
        return deadline;
    }

    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }

    public Boolean getAutosub() {
        return autosub;
    }

    public void setAutosub(Boolean autosub) {
        this.autosub = autosub;
    }

    public Boolean getExpireNotice() {
        return expireNotice;
    }

    public void setExpireNotice(Boolean expireNotice) {
        this.expireNotice = expireNotice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AritcleSubPlay [nick=" + nick + ", articleCode=" + articleCode + ", itemCode=" + itemCode
                + ", deadline=" + deadline + ", autosub=" + autosub + ", expireNotice=" + expireNotice + ", status="
                + status + "]";
    }

}
