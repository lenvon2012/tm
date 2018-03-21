package models.jms;

public class MsgBody {
	private MsgContent content;
	
	private String topic;
	
	private Long outgoing_id;
	
	private Long publish_time;

	private Long user_id;

	private String user_nick;

	private String publisher_appkey;

	public MsgContent getContent() {
		return content;
	}

	public void setContent(MsgContent content) {
		this.content = content;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Long getOutgoing_id() {
		return outgoing_id;
	}

	public void setOutgoing_id(Long outgoing_id) {
		this.outgoing_id = outgoing_id;
	}

	public Long getPublish_time() {
		return publish_time;
	}

	public void setPublish_time(Long publish_time) {
		this.publish_time = publish_time;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public String getUser_nick() {
		return user_nick;
	}

	public void setUser_nick(String user_nick) {
		this.user_nick = user_nick;
	}

	public String getPublisher_appkey() {
		return publisher_appkey;
	}

	public void setPublisher_appkey(String publisher_appkey) {
		this.publisher_appkey = publisher_appkey;
	}
}
