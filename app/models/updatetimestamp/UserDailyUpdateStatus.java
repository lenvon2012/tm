
package models.updatetimestamp;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class UserDailyUpdateStatus extends UserDailyUpdateTs {

    public final static String TABLE_NAME = "user_daily_update_status";

    public int status;

    public static class Status {

        public static final int API_EXCEP = 1;

        public static final int API_DOING = 2;

        public static final int API_DONE = 4;

        public static final int DB_DOING = 8;

        public static final int DB_DONE = 16;

        public static final int PROCESS_DOING = 32;

        public static final int PROCESS_DONE = 64;

    }

    public UserDailyUpdateStatus(Long userId, Long ts) {
        super(userId, ts);
    }

    public void setApiException() {
        this.status = Status.API_EXCEP;
    }

    public void setApiDoing() {
        this.status = Status.API_DOING;
    }

    public void setApiDone() {
        this.status = Status.API_DONE;

    }

    public void setDbDoing() {
        this.status |= Status.DB_DOING;
    }

    public void setDbDone() {
        this.status = Status.API_DONE | Status.DB_DONE;

    }

    public void setProcessDoing() {
        this.status |= Status.PROCESS_DOING;
    }

    public void setProcessDone() {
        this.status = Status.API_DONE | Status.DB_DONE | Status.PROCESS_DONE;
    }

}
