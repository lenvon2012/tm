
package dao.autolist;

import models.autolist.AutoListJobTs;

public class AutoListJobTsDao {
    /*public static AutoListJobTs queryByJobId(String jobId) {
        return AutoListJobTs.find("jobId=? order by id desc", jobId).first();
    }*/
	public static AutoListJobTs queryByJobId(String jobId) {
        return AutoListJobTs.findByJobId(jobId);
    }
	
    public static void saveOrUpdateAutoJobTs(AutoListJobTs jobTs) {
        jobTs.jdbcSave();
    }    
}
