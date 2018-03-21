/**
 * 
 */
package job.jd;

import java.util.List;

import jdapi.JDItemApi.WareListSearch;
import models.jd.JDItemPlay;
import models.jd.JDUser;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import dao.jd.JDItemDao;

/**
 * @author navins
 *
 */
public class JDItemUpdateJob extends Job {
    
    public JDUser user;
    
    public JDItemUpdateJob(JDUser user) {
        this.user = user;
    }
    
    @Override
    public void doJob() throws Exception {
        List<JDItemPlay> list = new WareListSearch(user).call();
        if (CommonUtils.isEmpty(list)) {
            return;
        }
        
        for (JDItemPlay jdItemPlay : list) {
            jdItemPlay.jdbcSave();
        }
        
        JDItemDao.deleteNoSkuItems(user.getId());
    }
}
