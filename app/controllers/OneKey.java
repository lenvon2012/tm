package controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Administrator on 2014/5/10.
 */
public class OneKey extends  TMController {
    
    private static final Logger log = LoggerFactory.getLogger(FenxiaoBatch.class);
    
    public  static  void index(){
        render("/newAutoTitle/onekey.html");
    }
    
    public  static  void indexNew(){
        render("/newAutoTitle/onekeynew.html");
    }
}
