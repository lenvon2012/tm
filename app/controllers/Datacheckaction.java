
package controllers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

public class Datacheckaction extends TMController {

    private static final Logger log = LoggerFactory.getLogger(Datacheckaction.class);

    public static final String TAG = "Datacheckaction";

    static Set<String> whitleListSet = new HashSet<String>();

    static {
        try {
            File file = new File(new File(new File(Play.applicationPath, "conf"), "init"),
                    "aliww.txt");
            whitleListSet.addAll(FileUtils.readLines(file));
        } catch (IOException e2) {
            log.warn(e2.getMessage(), e2);
        }
    }

    //checkout the buyer is in blacklist?
    public static String Isinblacklist(String userNick) {
        return whitleListSet.contains(userNick) ? "true" : "false";

    }

}
