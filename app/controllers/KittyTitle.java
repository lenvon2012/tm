
package controllers;


public class KittyTitle extends TMController {

    public static void kittyComment() {
        render("KittyTitle/kittyComment.html");
    }

    public static void kittyDelist() {
        render("KittyTitle/kittyDelist.html");
    }

    public static void kittyWindow() {
        render("KittyTitle/kittyWindow.html");
    }

    public static void kittyProptest() {
        render("KittyTitle/kittyProptest.html");
    }

    public static void kittyTitle(Integer pn, Integer start, Integer end, Integer sort, Integer status) {
        if (pn == null || pn < 1)
            pn = 1;
        if (start == null || start < 0)
            start = 0;
        if (end == null || end > 100)
            end = 100;
        if (sort == null || sort > 1 || sort < -1)
            sort = 1;
        if (status == null || !status.equals(0) || !status.equals(1) || !status.equals(2)) {
            status = 0;
        }
        render("KittyTitle/itemDiag.html", pn, start, end, sort, status);
    }

    public static void KittyDo(Long numIid, Long pn, Long offset, String s, int start, int end, int sort, int status,
            Long catId) {
        if (numIid == null || pn == null || offset == null || offset < 0)
            redirect("/home/commoditydiag");
        if (s == null)
            s = "";
        if (!(status == 0 || status == 1 || status == 2)) {
            status = 2;
        }
        render(numIid, pn, offset, s, start, end, sort, status, catId);
    }
}
