package models.message;

import javax.persistence.Entity;

import models.CreatedUpdatedModel;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;

@Entity(name = TradeSuccessMessage.TABLE_NAME)
@JsonIgnoreProperties(value = {})
public class TradeSuccessMessage extends CreatedUpdatedModel {

	public static final String TABLE_NAME = "tradeSuccessMessage";
	
	@Index(name = "userId")
    Long userId;
	
	public String topic;
	public String status;
	public String nick;
	public String buyer_nick;
	public Long oid;
	public Long tid;
	
	public TradeSuccessMessage(String topic, String status, String nick, String buyer_nick, Long oid, Long tid){
		this.topic = topic;
		this.status = status;
		this.nick = nick;
		this.buyer_nick = buyer_nick;
		this.oid = oid;
		this.tid = tid;
	}
}
