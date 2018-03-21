package models.promotion;

import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
@Entity(name = VipConfig.TABLE_NAME)
public class VipConfig extends GenericModel {
	public static final String TABLE_NAME = "vip_config";
	
	@Transient
	private static final Logger log = LoggerFactory.getLogger(User.class);
	
	public static class Type {
		public static final String COUNT = "COUNT";
		public static final String AMOUNT = "AMOUNT";
	}
	public VipConfig(Long userId,String type,Long[] quantity,Boolean[] hierarchy,Long[] discount){
		this.userId = userId;
		this.type = type;
		this.quantity = quantity;
		this.hierarchy = hierarchy;
		this.discount = discount;
	}
	public VipConfig(Long userId,String type,Long[] quantity,Long[] discount){
		this.userId = userId;
		this.type = type;
		this.quantity = quantity;
		this.discount = discount;
		this.hierarchy[0] = true;
		this.hierarchy[1] = true;
		this.hierarchy[2] = true;
		this.hierarchy[3] = true;
	}
	@Id
	@GeneratedValue
	public Long id;
	public Long userId;
	public String type = "";
	public Long[] quantity = new Long[4];
	public Boolean[] hierarchy = new Boolean[4];
	public Long[] discount = new Long[4];
	public TreeSet<String> vipLevel0 = new TreeSet<String>();
	public TreeSet<String> vipLevel1 = new TreeSet<String>();
	public TreeSet<String> vipLevel2 = new TreeSet<String>();
	public TreeSet<String> vipLevel3 = new TreeSet<String>();
	
	
	public TreeSet<String> getVips(Integer vipLevel){
		switch(vipLevel){
		case 1:
			return vipLevel0;
		case 2:
			return vipLevel1;
		case 3:
			return vipLevel2;
		case 4:
			return vipLevel3;
			default:
				return null;
		}
	}
	public void addVip(String userNick,Integer level){
		switch(level){
		case 1:
			vipLevel0.add(userNick);
			break;
		case 2:
			vipLevel1.add(userNick);
			break;
		case 3:
			vipLevel2.add(userNick);
			break;
		case 4:
			vipLevel3.add(userNick);
			break;
			default:
				log.error("addVip level error,userNick=%s,level=%d",userNick,level);
		}
	}
	public void removeVip(String userNick,Integer level){
		switch(level){
		case 1:
			vipLevel0.remove(userNick);
			break;
		case 2:
			vipLevel1.remove(userNick);
			break;
		case 3:
			vipLevel2.remove(userNick);
			break;
		case 4:
			vipLevel3.remove(userNick);
			break;
			default:
				log.error("removeVip level error,userNick=%s,level=%d",userNick,level);
		}
	}
	public void synchronizeToTaobao(){
		//synchronize class state to taobao server
		
	}
	public void synchronizeToLocal(){
		//synchronize toabao data to local
		
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long[] getQuantity() {
		return quantity;
	}
	public void setQuantity(Long[] quantity) {
		this.quantity = quantity;
	}
	public Boolean[] getHierarchy() {
		return hierarchy;
	}
	public void setHierarchy(Boolean[] hierarchy) {
		this.hierarchy = hierarchy;
	}
	public Long[] getDiscount() {
		return discount;
	}
	public void setDiscount(Long[] discount) {
		this.discount = discount;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
