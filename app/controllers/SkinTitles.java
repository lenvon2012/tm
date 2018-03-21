
package controllers;

public class SkinTitles extends TMController {

    public static void index(String sid) {
        render("skintitles/shoptitlediag.html");
    }

    public static void autoTitle() {
        render("skintitles/skintitleindex.html");
    }

    public static void dicHome() {
        render("skintitles/dichome.html");
    }

    public static void skinTitle() {
        render("skintitles/newskintitle.html");
    }
}
