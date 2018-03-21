
package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Aituiguang extends TMController {
    private static final Logger log = LoggerFactory.getLogger(Aituiguang.class);

    public static final String TAG = "Bovyon";

    public static void allItems() {
        render("/boyvon/allItems.html");
    }

    public static void showedItems() {
        render("/boyvon/showedItems.html");
    }

    public static void index(String sid) {
        render("/boyvon/index.html");
    }

    public static void award() {
        render("/boyvon/award.html");
    }

    public static void help() {
        render("/boyvon/help.html");
    }

    public static void tryhelp() {
        render("/boyvon/tryhelp.html");
    }

    public static void recommend() {
        render("/boyvon/forliuliang.html");
    }

    public static void upgrade() {
        render("/boyvon/upgrade.html");
    }
}
