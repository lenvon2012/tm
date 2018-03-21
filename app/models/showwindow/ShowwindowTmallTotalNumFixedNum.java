
package models.showwindow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import models.user.User;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import transaction.TransactionSecurity;
import dao.UserDao;

@Entity(name = ShowwindowTmallTotalNumFixedNum.TABLE_NAME)
public class ShowwindowTmallTotalNumFixedNum extends GenericModel {
    public static final String TABLE_NAME = "showwindow_tmall_fixed_total_num";

    public static final String KEY_TAG = TABLE_NAME;

    @Id
    Long id;

    Integer fixedNum;

    Long created;

    Long updated;
    
    Boolean enableManualWindowNum = false;

    static String expired = "7d";

    public ShowwindowTmallTotalNumFixedNum(Long userId, int i) {
        this.id = userId;
        this.fixedNum = i;
        this.created = System.currentTimeMillis();
        this.updated = System.currentTimeMillis();
    }
    
    public ShowwindowTmallTotalNumFixedNum(Long userId, int i, Boolean enableManualWindowNum) {
        this.id = userId;
        this.fixedNum = i;
        this.enableManualWindowNum = enableManualWindowNum;
        this.created = System.currentTimeMillis();
        this.updated = System.currentTimeMillis();
    }

    static int simpleMax = -1;

    public static Integer findOrCreate(final User user) {
        return findOrCreate(user, null);
    }

    public static Integer findOrCreate(final User user, final Integer onShowCount) {
        final Long userId = user.getId();
        final String key = KEY_TAG + userId;
        Integer fixedNum = (Integer) Cache.get(key);
        if (fixedNum != null) {
            return fixedNum;
        }
        Integer totalNum = new TransactionSecurity<Integer>() {
            @Override
            public Integer operateOnDB() {
                ShowwindowTmallTotalNumFixedNum model = ShowwindowTmallTotalNumFixedNum.findById(userId);
                if (model != null) {
                	if(model.getEnableManualWindowNum()) {
                		return model.fixedNum;
                	} else {
                		return simpleMax;
                	}
                    
                }
                model = new ShowwindowTmallTotalNumFixedNum(userId, simpleMax);
                if(user.isTmall()) {
                	if (onShowCount != null) {
                        if (onShowCount < 60) {
                            model.fixedNum = 60;
                        } else if (onShowCount <= 100) {
                            model.fixedNum = 100;
                        } else if (onShowCount <= 200) {
                            model.fixedNum = 200;
                        } else if (onShowCount <= 300) {
                            model.fixedNum = 300;
                        } else if (onShowCount <= 500) {
                            model.fixedNum = 500;
                        } else if (onShowCount <= 1000) {
                            model.fixedNum = 1000;
                        } else {
                            model.fixedNum = 2000;
                        }
                    }
                }
                
                model.save();
                return model.fixedNum;
            }
        }.execute();

        Cache.safeSet(key, totalNum, expired);
        return totalNum;
    }

    public static void updateUserTotalNum(final Long userId, final int maxNum) {
        final String key = KEY_TAG + userId;
        Integer fixedNum = (Integer) Cache.get(key);
        if (fixedNum != null && fixedNum.intValue() == maxNum) {
            return;
        }
        Cache.safeSet(key, fixedNum, expired);
        new TransactionSecurity<Integer>() {
            @Override
            public Integer operateOnDB() {
                ShowwindowTmallTotalNumFixedNum model = ShowwindowTmallTotalNumFixedNum.findById(userId);
                if (model == null) {
                    model = new ShowwindowTmallTotalNumFixedNum(userId, maxNum);
                } else {
                    model.fixedNum = maxNum;
                }
                model.updated = System.currentTimeMillis();
                model.save();

                return model.fixedNum;
            }
        }.execute();
    }

    public static void clear(final User user) {
        final String key = KEY_TAG + user.getId();
        Cache.delete(key);
        new TransactionSecurity<Integer>() {
            @Override
            public Integer operateOnDB() {
                ShowwindowTmallTotalNumFixedNum model = ShowwindowTmallTotalNumFixedNum.findById(user.getId());
                if (model == null) {
                    return null;
                }
                model.delete();
                return null;
            }
        }.execute();

    }

    public static void fixNoCountUser() {

        List<ShowwindowTmallTotalNumFixedNum> models = ShowwindowTmallTotalNumFixedNum.find("fixedNum < 0").fetch();
        Set<Long> userIds = new HashSet<Long>();
        for (ShowwindowTmallTotalNumFixedNum model : models) {
            userIds.add(model.id);
        }
        for (Long long1 : userIds) {
            User user = UserDao.findById(long1);
            ShowCaseInfo.build(user);
        }
    }

    public static void fixAllUserCache() {

        List<ShowwindowTmallTotalNumFixedNum> models = ShowwindowTmallTotalNumFixedNum.find(" 1 =1  ").fetch();
        Set<Long> userIds = new HashSet<Long>();
        for (ShowwindowTmallTotalNumFixedNum model : models) {
            userIds.add(model.id);
        }
        for (Long long1 : userIds) {
            User user = UserDao.findById(long1);
            Set<Long> onWindowNumiids = OnWindowItemCache.get().refresh(user);
            clear(user);
            findOrCreate(user, onWindowNumiids.size());
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getFixedNum() {
		return fixedNum;
	}

	public void setFixedNum(Integer fixedNum) {
		this.fixedNum = fixedNum;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getUpdated() {
		return updated;
	}

	public void setUpdated(Long updated) {
		this.updated = updated;
	}

	public Boolean getEnableManualWindowNum() {
		return enableManualWindowNum;
	}

	public void setEnableManualWindowNum(Boolean enableManualWindowNum) {
		this.enableManualWindowNum = enableManualWindowNum;
	}

}
