package models.promotion;

import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.GenericModel;

import com.ciaosir.client.CommonUtils;
@Entity(name=UserTag.TABLE_NAME)
public class UserTag extends GenericModel{
	public static final String TABLE_NAME = "user_tag";
	public UserTag(Long id,Long userId,Long createDate,String tagName,String description){
		users = new TreeSet<String>();
		this.id = id;
		this.userId = userId;
		this.createDate = createDate;
		this.tagName = tagName;
		this.description = description;
	}
	public UserTag(Long userId,Long createDate,String tagName,String description){
		this.userId = userId;
		users = new TreeSet<String>();
		this.createDate = createDate;
		this.tagName = tagName;
		this.description = description;
	}
	public Long userId;
	public TreeSet<String> users;
	public TreeSet<String> getUsers() {
		return users;
	}
	public void setUsers(TreeSet<String> users) {
		this.users = users;
	}
	public void addUser(String userNick){
		users.add(userNick);
	}
	/**
	 * 
	 * @param userNick
	 * @return true if users set contains that userNick
	 */
	public boolean deleteUser(String userNick){
		return users.remove(userNick);
	}
	/**
	 * 创建时间
	 */
	public Long createDate;

	/**
	 * 标签描述
	 */
	public String description;

	/**
	 * 标签ID
	 */
	@Id
	@GeneratedValue
	public Long id;
	
	/**
	 * 标签名称
	 */
	public String tagName;

	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getUsersString(){
		return StringUtils.join(users, ",");
	}
	public String getCreateDateString(){
		if(this.createDate!=null){
			return CommonUtils.TimeToString(this.createDate);
		}else{
			return "-";
		}
	}
}
