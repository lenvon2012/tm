
package controllers;

import play.mvc.Controller;

public class PaiPaiIn extends Controller {

    public static void logout() {
        PaiPaiController.clearUser();
    }
}
