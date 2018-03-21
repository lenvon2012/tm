
package result;

import java.io.Serializable;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import com.ciaosir.client.pojo.PageOffset;

@JsonAutoDetect
public class TMResult<T> implements Serializable {
    
    private static final long serialVersionUID = -1;

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
    public String code;

    @JsonProperty
    protected T res;

    public TMResult(String msg) {
        this.isOk = false;
        this.msg = msg;
    }

    public TMResult(boolean isOk, int pn, int ps, int count, String msg, T res) {
        super();
        this.isOk = isOk;
        this.pn = pn;
        this.ps = ps;
        this.count = count;
        this.msg = msg;
        this.res = res;
        this.pnCount = (count + ps - 1) / ps;
    }

    public TMResult(List list, int count, PageOffset po) {
        this.isOk = true;
        this.pn = po.getPn();
        this.ps = po.getPs();
        this.count = count;
        this.pnCount = (count + ps - 1) / ps;
        this.res = (T) list;
    }
    
    public TMResult(List list, int count, PageOffset po, String msg) {
        this.isOk = true;
        this.pn = po.getPn();
        this.ps = po.getPs();
        this.count = count;
        this.pnCount = (count + ps - 1) / ps;
        this.res = (T) list;
        this.msg = msg;
    }

    public TMResult(T list, int count, PageOffset po) {
        this.isOk = true;
        this.pn = po.getPn();
        this.ps = po.getPs();
        this.count = count;
        this.pnCount = (count + ps - 1) / ps;
        this.res = list;
    }

    public TMResult(boolean isOk, String msg, T res) {
        this.isOk = isOk;
        this.msg = msg;
        this.res = res;
    }

    public TMResult(String code, String msg) {
        this.isOk = false;
        this.code = code;
        this.msg = msg;
    }

    public TMResult(T res) {
        this.isOk = true;
        this.msg = StringUtils.EMPTY;
        this.res = res;
    }

    public TMResult() {
        super();
    }

    public static TMResult OK = new TMResult();

    public static TMResult failMsg(String msg) {
        return new TMResult(msg);
    }

    public static TMResult renderMsg(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return new TMResult();
        } else {
            return new TMResult(msg);
        }
    }

    public T getRes() {
        return res;
    }

    public void setRes(T res) {
        this.res = res;
    }

    @JsonAutoDetect
    public static class TMListResult<T> extends TMResult {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "TMResult [isOk=" + isOk + ", msg=" + msg + ", code=" + code + ", res=" + res + "]";
    }

}
