package actions.carriertask;

/**
 * Created by Administrator on 2016/3/8.
 */
public class TaskInfo {
    private long taskId;
    private long sid;
    private String publisher;


    public TaskInfo() {
    }

    public TaskInfo(long taskId, long sid, String publisher) {
        this.taskId = taskId;
        this.sid = sid;
        this.publisher = publisher;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }
}
