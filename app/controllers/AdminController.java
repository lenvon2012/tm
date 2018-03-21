package controllers;

import play.mvc.Controller;
import play.mvc.With;

import com.ciaosir.client.utils.JsonUtil;

import controllers.TMController.BusUIResult;

@With(Secure.class)
public class AdminController extends Controller {

    
    protected static void renderBusJson(Object json) {
        renderJSON(JsonUtil.getJson(new BusUIResult(json)));
    }

    protected static void renderFailedJson(String message) {
        renderJSON(JsonUtil.getJson(new BusUIResult(false, message)));
    }

    protected static void renderSuccessJson() {
        renderJSON(JsonUtil.getJson(new BusUIResult(true, "")));
    }

    protected static void renderSuccessJson(String msg) {
        renderJSON(JsonUtil.getJson(new BusUIResult(true, msg)));
    }
    
}
