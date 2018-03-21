/**
 * 
 */
package bustbapi;

import java.io.File;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jd.open.api.sdk.request.Field;
import com.taobao.api.FileItem;
import com.taobao.api.domain.Picture;
import com.taobao.api.request.PictureUploadRequest;
import com.taobao.api.response.PictureUploadResponse;

/**
 * @author navins
 * @date: Jan 21, 2014 3:01:12 PM
 */
public class PictureApi {
    
    public final static Logger log = LoggerFactory.getLogger(PictureApi.class);

    public static class PictureCarrier extends TBApi<PictureUploadRequest, PictureUploadResponse, Picture> {

        public Long catId;
        
        public String picPath;
        
        public String imageInputTitle;
        
        public String title;
        
        public PictureCarrier(String sid, String picPath, String imageInputTitle, String title) { 
            super(sid);
            this.catId = 0L;
            this.picPath = picPath;
            this.imageInputTitle = imageInputTitle;
            this.title = title;
        }
        
        public PictureCarrier(User user, Long catId, String picPath, String imageInputTitle, String title) { 
            super(user.getSessionKey());
            this.catId = catId;
            this.picPath = picPath;
            this.imageInputTitle = imageInputTitle;
            this.title = title;
        }
        
        @Override
        public PictureUploadRequest prepareRequest() {
            PictureUploadRequest req=new PictureUploadRequest();
            req.setPictureCategoryId(0L);
            File file = new File(picPath);
            FileItem fItem = new FileItem(file);
            req.setImg(fItem);
            if (!StringUtils.isEmpty(imageInputTitle)) {
                req.setImageInputTitle(imageInputTitle);
            }
            if (!StringUtils.isEmpty(title)) {
                req.setTitle(title);
            }
            return req;
        }

        @Override
        public Picture validResponse(PictureUploadResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

//            ErrorHandler.validTaoBaoResp(resp);
            if (resp.isSuccess()) {
                return resp.getPicture();
            }

            errorMsg = resp.getSubMsg();

            return null;
        }

        @Override
        public Picture applyResult(Picture res) {
            // TODO Auto-generated method stub
            return res;
        }
        
    }
    
}
