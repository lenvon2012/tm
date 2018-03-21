package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;

public class QianniuCrm extends Controller {

    private static final Logger log = LoggerFactory.getLogger(QianniuCrm.class);
    
    public static void index() {
        render("/qianniucrm/crmindex.html");
    }
    
}
