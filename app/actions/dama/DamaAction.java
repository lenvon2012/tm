package actions.dama;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import play.Play;
import result.TMResult;

public class DamaAction {

    private static final Logger log = LoggerFactory.getLogger(DamaAction.class);
    
    private static final String UserName = "nijiang";
    private static final String PassWord = "kange78235keg";
    
    private static final String SoftwareID = "89101";
    private static final String SoftwareKey = "3666596c0c2947ba8897c00253b2ac59";
    
    public static DocumentBuilderFactory dbf;
    public static DocumentBuilder db;
    
    public  DamaAction() {
        dbf = DocumentBuilderFactory.newInstance();
        try {
            db = dbf.newDocumentBuilder();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
    
    // 查询剩余积分
    public  static String  GetUserInfo() {
        String result = RuoKuai.getInfo(UserName, PassWord);
       
        return result;
    }
    
    // 查询剩余积分
    public  String  GetScore() {
        String result = RuoKuai.getInfo(UserName, PassWord);
        Document dm;
        String scores = "";
        try {
            dm = db.parse(new ByteArrayInputStream(result.getBytes("utf-8")));
            NodeList resultNl = dm.getElementsByTagName("Score");
            
            if(resultNl.getLength() > 0 ){
                scores =  resultNl.item(0).getFirstChild().getNodeValue();
            } 
              
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn(result);
            return scores;
        }
        return scores;
    }
    
  
    // 上传验证码图片
    public  TMResult updateImage(String codeType, byte[] b) {
        
        log.warn("start to dama !!!");
        Long startMills = System.currentTimeMillis();
        String resultDom = RuoKuai.createByPost(UserName, PassWord, codeType, "90", SoftwareID, SoftwareKey, b);
        log.warn("end dama !!! take: "+(System.currentTimeMillis() - startMills)+ " ms");
        
        Document dm;
        ImageResult imgRes = new ImageResult();
        try {
            dm = db.parse(new ByteArrayInputStream(resultDom.getBytes("utf-8")));
            NodeList resultNl = dm.getElementsByTagName("Result");
            NodeList idNl = dm.getElementsByTagName("Id");
            NodeList errorNI = dm.getElementsByTagName("Error");
            
            if(errorNI.getLength()>0){
                String message =  idNl.item(0).getFirstChild().getNodeValue();
                return new TMResult(false, message, null);
            } 
            
            if(resultNl.getLength() > 0 ){
                imgRes.result =  resultNl.item(0).getFirstChild().getNodeValue();
            }else{
                return new TMResult(false, "打码失败", null);
            }
                
            if(idNl.getLength()>0){
                imgRes.imageId =  idNl.item(0).getFirstChild().getNodeValue();
            } 
            
               
        } catch (Exception e) {
            log.warn(resultDom);
            log.error(e.getMessage(), e);
            return new TMResult(false, e.getMessage(), null);
        }
        
        return new TMResult(true,"", imgRes);      
    }
    
    public static class ImageResult{
        private String result;
        private String imageId;
        private String errorMsg;
        
        public String getResult() {
            return result;
        }
        public void setResult(String result) {
            this.result = result;
        }
        public String getImageId() {
            return imageId;
        }
        public void setImageId(String imageId) {
            this.imageId = imageId;
        }
        public String getErrorMsg() {
            return errorMsg;
        }
        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }        
        
    }
       
}
