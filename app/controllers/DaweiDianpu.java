
package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaweiDianpu extends TMController {

    private static final Logger log = LoggerFactory.getLogger(DaweiDianpu.class);

    public static final String TAG = "DaweiDianpu";

    public static void index() {
        render("daweidianpu/index.html");
    }

}
