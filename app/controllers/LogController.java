package controllers;

import play.mvc.Controller;
import job.log.SimulateRequestJob;

public class LogController extends Controller {

    public static void sendLog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean sendLog = new SimulateRequestJob().sendLog();
            }
        }).start();

        ok();

    }

    
}
