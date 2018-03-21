
package result;

import org.apache.commons.lang.StringUtils;

import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Http.StatusCode;
import play.mvc.results.Result;

public class PolicyResult extends Result {

    public int code;

    public String msg = StringUtils.EMPTY;

    public String src = null;

    public PolicyResult(int code) {
        this.code = code;
    }

    public PolicyResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public PolicyResult(int code, String msg, String src) {
        this.code = code;
        this.msg = msg;
        this.src = src;

    }

    @Override
    public void apply(Request request, Response response) {
        try {
            setContentTypeIfNotSet(response, "text/plain");
            response.status = code;
            response.out.write(msg.getBytes("utf-8"));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public String toString() {
        return "CredResult [code=" + code + ", msg=" + msg + ", src=" + src + "]";
    }

    public static PolicyResult badrequest(String reason) {
        throw new PolicyResult(StatusCode.BAD_REQUEST, reason);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
