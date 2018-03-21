package utils;

import java.io.Serializable;

import models.user.User;

public class UserCache implements Serializable{
    private boolean flag;
    private long runningEndTime;
    private boolean isSearch;

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public long getRunningEndTime() {
        return runningEndTime;
    }

    public void setRunningEndTime(long runningEndTime) {
        this.runningEndTime = runningEndTime;
    }
}
