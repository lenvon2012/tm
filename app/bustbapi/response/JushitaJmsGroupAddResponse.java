package bustbapi.response;

import java.util.Date;

import com.taobao.api.TaobaoResponse;
import com.taobao.api.internal.mapping.ApiField;

/**
 * TOP API: taobao.jushita.jms.group.add response.
 * 
 * @author top auto create
 * @since 1.0, null
 */
public class JushitaJmsGroupAddResponse extends TaobaoResponse {

	private static final long serialVersionUID = 2816177883477388992L;

	/** 
	 * 创建时间
	 */
	@ApiField("created")
	private Date created;

	/** 
	 * 分组名称
	 */
	@ApiField("group_name")
	private String groupName;


	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getCreated( ) {
		return this.created;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGroupName( ) {
		return this.groupName;
	}
	


}
