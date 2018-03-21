
package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;

public class Buy extends Controller {
    private static final Logger log = LoggerFactory.getLogger(Home.class);

    public static final String TAG = "Buy";

    public static void toPayPage(String time) {
        log.info("someone buy...");
        //System.out.println(time);
        String buyUrl = "http://fuwu.taobao.com/item/subsc.htm?items=ts-1820059-";
        String ver = "1";
        String vertime = ":999";
        String[] dealTime = time.split("_");
//		System.out.println(dealTime[0]);
//		System.out.println(dealTime[1]);
        if (dealTime[0].equals("a")) {
            ver = "5";
        } else if (dealTime[0].equals("b")) {
            ver = "3";
        } else if (dealTime[0].equals("c")) {
            ver = "5";
        } else if (dealTime[0].equals("e")) {
            ver = "5";
        } else if (dealTime[0].equals("d")) {
            ver = "3";
        } else if (dealTime[0].equals("f")) {
            ver = "3";
        } else if (dealTime[0].equals("h")) {
            ver = "3";
        } else if (dealTime[0].equals("g")) {
            ver = "5";
        } else if (dealTime[0].equals("j")) {
            ver = "3";
        } else if (dealTime[0].equals("i")) {
            ver = "5";
        } else if (dealTime[0].equals("l")) {
            ver = "3";
        } else if (dealTime[0].equals("k")) {
            ver = "5";
        }
        if (dealTime[1].equals("1m")) {
            vertime = ":1";
        } else if (dealTime[1].equals("3m")) {
            vertime = ":3";
        } else if (dealTime[1].equals("6m")) {
            vertime = ":6";
        } else if (dealTime[1].equals("12m")) {
            vertime = ":12";
        }
        String text = buyUrl + ver + vertime;
        renderText(text);
    }
    public static void buyxuanci(String time){
    	log.info("someone buy...");
        //System.out.println(time);
        String[] dealTime = time.split("_");
//		System.out.println(dealTime[0]);
    	String text = "";
        if (dealTime[0].equals("d")&&dealTime[1].equals("1m")) {
        	text="http://to.taobao.com/aHZ5Mjy";
        }else if (dealTime[0].equals("d")&&dealTime[1].equals("3m")) {
        	text="http://to.taobao.com/vQZ5Mjy";
        }else if (dealTime[0].equals("d")&&dealTime[1].equals("6m")) {
        	text="http://to.taobao.com/xSY5Mjy";
        }else if (dealTime[0].equals("d")&&dealTime[1].equals("12m")) {
        	text="http://to.taobao.com/i9Y5Mjy";
        }
        renderText(text);
    }
}
