/**
 * 
 */
package ppapi.models;

/**
 * @author navins
 * @date 2013-7-8 下午6:45:17
 */
public class PaiPaiUserInfo {

    public static PaiPaiUserInfo EMPTY = new PaiPaiUserInfo();

    long uin;

    String nick;

    int buyerCredit;

    int sellerCredit;

    public PaiPaiUserInfo() {

    }

    public PaiPaiUserInfo(long uin, String nick, int buyerCredit, int sellerCredit) {
        super();
        this.uin = uin;
        this.nick = nick;
        this.buyerCredit = buyerCredit;
        this.sellerCredit = sellerCredit;
    }

    public long getUin() {
        return uin;
    }

    public void setUin(long uin) {
        this.uin = uin;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getBuyerCredit() {
        return buyerCredit;
    }

    public void setBuyerCredit(int buyerCredit) {
        this.buyerCredit = buyerCredit;
    }

    public int getSellerCredit() {
        return sellerCredit;
    }

    public void setSellerCredit(int sellerCredit) {
        this.sellerCredit = sellerCredit;
    }

    @Override
    public String toString() {
        return "PaiPaiUserInfo [uin=" + uin + ", nick=" + nick + ", buyerCredit=" + buyerCredit + ", sellerCredit="
                + sellerCredit + "]";
    }

}
