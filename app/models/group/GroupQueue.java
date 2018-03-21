package models.group;

import java.util.List;

import models.user.User;

public class GroupQueue {
    
    private User user;
    
    private List<Long> numIids;
    
    private Long planId;
    
    /**
     * 1: insert; 2:delete ;3 replace
     */
    private int type;
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Long> getNumIids() {
        return numIids;
    }

    public void setNumIids(List<Long> numIids) {
        this.numIids = numIids;
    }

    public Long getPlanId() {
        return planId;
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
