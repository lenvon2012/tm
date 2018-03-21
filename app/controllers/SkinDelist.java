
package controllers;

public class SkinDelist extends TMController {
    public static void index(String sid) {
        render("skindelist/autodelist.html");
    }

    public static void empty() {
        render("boyvon/empty.html");
    }
}
