
package controllers;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

import job.subscribe.SubscribeAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import utils.DateUtil;

public class SubscribeUI extends Controller {

    public final static Logger log = LoggerFactory.getLogger(SubscribeUI.class);

    public static void export(String date) {

        try {
            Date exportDate = DateUtil.ymdsdf.parse(date);

            File exportFile = SubscribeAction.export(exportDate.getTime());

            renderBinary(exportFile);

        } catch (ParseException e) {
            log.error("Input date is error format : " + date);
        }

        renderText("日期格式有误！");
    }

}
