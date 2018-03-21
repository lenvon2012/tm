
package models.words;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import com.ciaosir.client.pojo.PageOffset;

@JsonAutoDetect
public class ALResult<T> implements Serializable {

    private boolean needWarnLogin = false;//需要提示用户登录
    
    @JsonProperty
    public boolean isOk = true;

    @JsonProperty
    public int pn;

    @JsonProperty
    public int ps;

    @JsonProperty
    public int count;

    public int getPn() {
        return pn;
    }

    public void setPn(int pn) {
        this.pn = pn;
    }

    public int getPs() {
        return ps;
    }

    public void setPs(int ps) {
        this.ps = ps;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPnCount() {
        return pnCount;
    }

    public void setPnCount(int pnCount) {
        this.pnCount = pnCount;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @JsonProperty
    public int pnCount;

    @JsonProperty
    public String msg;

    @JsonProperty
    public int errorCode;

    @JsonProperty
    protected T res;

    public ALResult(String msg) {
        this.isOk = false;
        this.msg = msg;
    }

    public ALResult(boolean isOk, int pn, int ps, int count, String msg, T res) {
        super();
        this.isOk = isOk;
        this.pn = pn;
        this.ps = ps;
        this.count = count;
        this.msg = msg;
        this.res = res;
        this.pnCount = (count + ps - 1) / ps;
    }

    public ALResult(List list, int count, PageOffset po) {
        this.isOk = true;
        this.pn = po.getPn();
        this.ps = po.getPs();
        this.count = count;
        this.pnCount = (count + ps - 1) / ps;
        this.res = (T) list;
    }

    public ALResult(T list, int count, PageOffset po) {
        this.isOk = true;
        this.pn = po.getPn();
        this.ps = po.getPs();
        this.count = count;
        this.pnCount = (count + ps - 1) / ps;
        this.res = list;
    }

    public ALResult(boolean isOk, String msg, T res) {
        this.isOk = isOk;
        this.msg = msg;
        this.res = res;
    }
    
    public ALResult(boolean isOk, String msg) {
        this.isOk = isOk;
        this.msg = msg;
    }

    public ALResult(int errorCode, String msg) {
        this.isOk = false;
        this.errorCode = errorCode;
        this.msg = msg;
    }

    public ALResult(T res) {
        this.isOk = true;
        this.msg = StringUtils.EMPTY;
        this.res = res;
    }

    public ALResult() {
        super();
    }

    public ALResult(ALResult res2) {
        this.count = res2.count;
        this.errorCode = res2.errorCode;
        this.isOk = res2.isOk;
        this.msg = res2.msg;
        this.pn = res2.pn;
        this.pnCount = res2.pnCount;
        this.ps = res2.ps;
    }

    public static ALResult OK = new ALResult();

    public static ALResult failMsg(String msg) {
        return new ALResult(msg);
    }

    public static ALResult renderMsg(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return new ALResult();
        } else {
            return new ALResult(msg);
        }
    }

    public T getRes() {
        return res;
    }

    public void setRes(T res) {
        this.res = res;
    }

    @JsonAutoDetect
    public static class TMListResult<T> extends ALResult {
        @JsonProperty
        protected List<T> res;

        public TMListResult(List<T> list, int count, PageOffset po) {
            this.isOk = true;
            this.pn = po.getPn();
            this.ps = po.getPs();
            this.count = count;
            this.pnCount = (count + ps - 1) / ps;
            this.res = list;
        }
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean isOk) {
        this.isOk = isOk;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isNeedWarnLogin() {
        return needWarnLogin;
    }

    public void setNeedWarnLogin(boolean needWarnLogin) {
        this.needWarnLogin = needWarnLogin;
    }

    @Override
    public String toString() {
        return "ALResult [needWarnLogin=" + needWarnLogin + ", isOk=" + isOk
                + ", pn=" + pn + ", ps=" + ps + ", count=" + count
                + ", pnCount=" + pnCount + ", msg=" + msg + ", errorCode="
                + errorCode + ", res=" + res + "]";
    }

    
    
}
