
package message;

public class UserItemTimestampMsg extends UserTimestampMsg {

    Long numIid;

    public UserItemTimestampMsg(Long userId, Long ts, Long numIid) {
        super(userId, ts);
        this.numIid = numIid;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    @Override
    public String toString() {
        return "UserItemTimestampMsg [numIid=" + numIid + ", userId=" + userId + ", ts=" + ts + "]";
    }

}
