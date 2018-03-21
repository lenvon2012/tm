package smsprovider.QTL;

import java.util.List;

public class QLTResponseMsg {

	public String code;
	
	public List<Message> messages;
	
	public QLTResponseMsg(){
		super();
	}
	public QLTResponseMsg(String code, List<Message> messages){
		this.code = code;
		this.messages = messages;
	}
	
	public static class Message{
		public String desmobile;
		public String msgid;
		
		public Message(String desmobile , String msgid){
			this.desmobile = desmobile;
			this.msgid = msgid;
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	
}
