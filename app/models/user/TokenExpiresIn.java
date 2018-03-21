/**
 * 
 */

package models.user;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author navins
 * @date 2013-6-25 下午8:47:24
 */
@JsonAutoDetect
public class TokenExpiresIn {

    public static TokenExpiresIn EMPTY = new TokenExpiresIn(0, 0);

    @JsonProperty
    private int w2_expires_in;

    @JsonProperty
    private int r2_expires_in;

    public TokenExpiresIn(int w2_expires_in, int r2_expires_in) {
        this.w2_expires_in = w2_expires_in;
        this.r2_expires_in = r2_expires_in;
    }

    public TokenExpiresIn(String w2_expires_in, String r2_expires_in) {
        this.w2_expires_in = Integer.parseInt(w2_expires_in);
        this.r2_expires_in = Integer.parseInt(r2_expires_in);
    }

    public int getW2_expires_in() {
        return w2_expires_in;
    }

    public void setW2_expires_in(int w2_expires_in) {
        this.w2_expires_in = w2_expires_in;
    }

    public int getR2_expires_in() {
        return r2_expires_in;
    }

    public void setR2_expires_in(int r2_expires_in) {
        this.r2_expires_in = r2_expires_in;
    }

    @Override
    public String toString() {
        return "TokenExpireIn [w2_expires_in=" + w2_expires_in + ", r2_expires_in=" + r2_expires_in + "]";
    }

}
