package models.op;

import javax.persistence.Entity;

import play.db.jpa.Model;
import utils.DateUtil;
import dao.trade.TradeRatePlayDao;

@Entity(name = CommentDeal.TABLE_NAME)
public class CommentDeal extends Model {

    public static final String TABLE_NAME = "comment_deal";

    private Long userId;

    private String nick;

    private String qq;

    private String shopUrl;

    private Long created;

    private Long badCommentCount;

    public CommentDeal() {

    }

    public CommentDeal(Long userId, String nick, String qq) {
        super();
        this.userId = userId;
        this.nick = nick;
        this.qq = qq;
        this.shopUrl = "http://store.taobao.com/shop/view_shop.htm?user_number_id=" + userId;
        this.badCommentCount = TradeRatePlayDao.countWithArgs(userId, null, null, 4, System.currentTimeMillis()
                - DateUtil.THIRTY_DAYS, null, null, null);
        this.created = System.currentTimeMillis();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getBadCommentCount() {
        return badCommentCount;
    }

    public void setBadCommentCount(Long badCommentCount) {
        this.badCommentCount = badCommentCount;
    }

    @Override
    public String toString() {
        return "CommentDeal [userId=" + userId + ", nick=" + nick + ", qq=" + qq + ", shopUrl=" + shopUrl + "]";
    }

}
