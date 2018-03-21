
package controllers;

import java.util.List;

import job.diagjob.PropDiagJob;
import job.diagjob.PropDiagJob.ItemPropDiagWrapper;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import com.ciaosir.client.utils.JsonUtil;

public class Props extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Props.class);

    public static final String TAG = "Props";

    public static void autodiag() {
        render("itemdiag/autodiag.html");
    }

    public static void dodiag() {
        User user = getUser();
        List<ItemPropDiagWrapper> res = null;
        res = (List<ItemPropDiagWrapper>) Cache.get(request.url + user.getId());
        if (res != null) {
            renderJSON(JsonUtil.getJson(res));
        }

        res = new PropDiagJob(user, false).doJobWithResult();
        Cache.set(request.url + user.getId(), res, "10min");
        renderJSON(JsonUtil.getJson(res));
    }
    
    
}
