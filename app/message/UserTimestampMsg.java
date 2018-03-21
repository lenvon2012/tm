
package message;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class UserTimestampMsg implements Serializable {

    @JsonProperty
    protected Long userId;

    @JsonProperty
    protected Long ts;

    protected String nick;

    public UserTimestampMsg(Long userId, Long ts) {
        super();
        this.userId = userId;
        this.ts = ts;
    }

    public UserTimestampMsg(Long userId, Long ts, String nick) {
        super();
        this.userId = userId;
        this.ts = ts;
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "UserTimestampMsg [userId=" + userId + ", ts=" + ts + ", nick=" + nick + "]";
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;

    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

}
