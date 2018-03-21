
package controllers;

import models.user.User;
import play.mvc.Http.Cookie;
import actions.MonitorAction;

import com.ciaosir.client.utils.MixHelpers;
import com.ciaosir.client.utils.NetworkUtil;

public class Monitor extends TMController {

    public void refresh(long sellerId) {
        Cookie cookie = request.cookies.get("_buyer");
        String value = null;
        if (cookie == null) {
            value = MixHelpers.rand32();
            // TODO record the rand number...
        } else {
            value = cookie.value;
            // TODO refresh the current seller Id cache..
        }

        // TODO updat the user related word...

        response.setCookie("_buyer", value, "24h");
        response.setContentTypeIfNotSet("image/png");
    }

    public static void visitorMonitor() {
        render();
    }

    public static void getMonitorResult() {
        User user = getUser();
        Long userId = user.getId();
        String ip = NetworkUtil.getRemoteIPForNginx(request);
        String monitorResult = MonitorAction.getMonitorResult(userId, ip);
        String json = "";
        long time = System.currentTimeMillis();
        json = "{\"queryTime\":" + time + ", \"visitorArray\":" + monitorResult + "}";

        renderJSON(json);
    }

    /*
    public static void installMonitorItem() {
        User user = getUser();
        if (user != null) {
            TemplateAction.doInstallItemMonitor(user);
        }
    }
    
    
    public static void removeMonitorInstall() {
        User user = getUser();
        if (user != null) {
            TemplateAction.removeMonitorInstall(user);
        }
    }*/

}
