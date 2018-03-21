package models.watermarker;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;

import play.db.jpa.Blob;
import play.db.jpa.Model;

import com.taobao.api.internal.util.StringUtils;

@Entity(name = WaterMarkerOriginImage.TABLE_NAME)
public class WaterMarkerOriginImage extends Model {
	public static final String TABLE_NAME = "water_marker_origin_image";
	
	@Index(name = "userId")
    private Long userId;
    @Index(name = "numIid")
    private Long numIid;
    
    private String picUrl;
    
    private Blob img;
    
    private Long ts;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public Blob getImg() {
		return img;
	}

	public void setImg(Blob img) {
		this.img = img;
	}
	
	

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public WaterMarkerOriginImage(Long userId, Long numIid, String picUrl,
			Blob img, Long ts) {
		super();
		this.userId = userId;
		this.numIid = numIid;
		this.picUrl = picUrl;
		this.img = img;
		this.ts = ts;
	}
    
    public static WaterMarkerOriginImage findByNumIid(Long userId, Long numIid) {
    	String query = "userId=? and numIid=?";
    	return WaterMarkerOriginImage.find(query, userId, numIid).first();
    }
    
    public static List<WaterMarkerOriginImage> findByUserId(Long userId) {
    	String query = "userId=? and ts>0";
    	return WaterMarkerOriginImage.find(query, userId).fetch();
    }
    
    public static List<WaterMarkerOriginImage> findByNumIids(Long userId, String numIids) {
    	if (StringUtils.isEmpty(numIids))
    		return new ArrayList<WaterMarkerOriginImage>();
    	String query = "userId=? and numIid in (";
    	String[] numIidArray = numIids.split(",");
    	if (numIidArray == null || numIidArray.length == 0)
    		return new ArrayList<WaterMarkerOriginImage>();
    	for (int i = 0, size = numIidArray.length; i < size; i++) {
            if (i < size - 1) {
            	query += numIidArray[i] + ",";
            } else {
            	query += numIidArray[i];
            }
        }
    	query += ") and ts > 0";
    	return WaterMarkerOriginImage.find(query, userId).fetch();
    }
}
