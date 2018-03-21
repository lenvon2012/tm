
package controllers;

public class skinWindows extends TMController {
    public static void windows() {
        render("skinWindows/skinWindows.html");
    }

    public static void relation() {
        render("skinWindows/skinRelation.html");
    }

    public static void shoucang() {
        render("skinWindows/skinShoucang.html");
    }

    public static void index(String sid) {
        render("skinWindows/skinWindows.html");
    }
}
