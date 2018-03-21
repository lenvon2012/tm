package controllers;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeShouCang extends TMController{

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger log = LoggerFactory.getLogger(LeShouCang.class);

    public static void baidushoucang(String sid) {
        render("leshoucang/baidushoucang.html");
    }
	
}
