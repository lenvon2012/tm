package actions.carriertask;

import java.util.List;

/**
 * Created by Administrator on 2016/3/8.
 */
public class ShopBabyBean {

    private String taskId;

    private String babyCnt;

    private String pn;

    private String publisher;

    private List<BabyInfo> infos;

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getBabyCnt() {
        return babyCnt;
    }

    public void setBabyCnt(String babyCnt) {
        this.babyCnt = babyCnt;
    }

    public String getPn() {
        return pn;
    }

    public void setPn(String pn) {
        this.pn = pn;
    }

    public List<BabyInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<BabyInfo> infos) {
        this.infos = infos;
    }
}
