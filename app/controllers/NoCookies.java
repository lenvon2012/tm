
package controllers;

import java.util.HashMap;
import java.util.Map;

import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http.Cookie;

public class NoCookies extends Controller {

    private static final Map<String, Cookie> emptyCookies = new HashMap<String, Cookie>(0);

    @Finally
    protected static void removeCookies() {
        response.cookies = emptyCookies;
    }

    public static void index() {

    }
}
