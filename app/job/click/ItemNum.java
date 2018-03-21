
package job.click;

public class ItemNum {

    public Long userId;

    public Long numIid;

    public ItemNum() {
        super();
    }

    public ItemNum(Long userId, Long numIid) {
        this.userId = userId;
        this.numIid = numIid;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getNumIid() {
        return this.numIid;
    }

    @Override
    public String toString() {
        return "ItemNum [userId=" + userId + ", numIid=" + numIid + "]";
    }

}
