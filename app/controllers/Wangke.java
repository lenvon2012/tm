
package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wangke extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Wangke.class);

    public static final String TAG = "Wangke";

    public static void mywords() {
        render("/Wangkesouci/mywords.html");
    }

    public static void wordsexport() {
        render("/Wangkesouci/wordsexport.html");
    }

    public static void keywords() {
        render("/Wangkesouci/keywords.html");
    }

    public static void relation() {
        render("/Wangkesouci/relation.html");
    }
}
