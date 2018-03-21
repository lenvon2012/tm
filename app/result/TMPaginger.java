
package result;

import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import com.ciaosir.client.CommonUtils;

/**
 * @deprecated {@link TMResult}
 * @author zrb
 *
 */
@JsonAutoDetect
public class TMPaginger {

    @JsonProperty
    boolean ok = true;

    @JsonProperty
    String msg = "";

    @JsonProperty
    int pn = 1;

    @JsonProperty
    int ps = 10;

    @JsonProperty
    int totalPnCount;

    @JsonProperty
    Object res;

//    public static class DetailResults {
//
//        @SuppressWarnings("rawtypes")
//        @JsonProperty
//        List arr;
//
//        public DetailResults(List items) {
//            super();
//            this.arr = items;
//        }
//    }

    public TMPaginger(boolean success, String msg) {
        this.msg = msg;
        this.ok = success;
    }

    @SuppressWarnings("rawtypes")
    public TMPaginger(int pageNum, int pageSize, int totalCount, List res) {
        if (totalCount <= 0L) {
        }
        this.pn = pageNum;
        this.ps = pageSize;
        this.totalPnCount = ((totalCount + pageSize - 1) / pageSize);
        this.res = res;
    }

    public static TMPaginger makeEmptyOk() {
        return new TMPaginger(true, StringUtils.EMPTY);
    }

    public static TMPaginger makeEmptyFail(String msg) {
        return new TMPaginger(false, msg);
    }

    @SuppressWarnings("rawtypes")
    public static List getSubList(List list, int pageNum, int pageSize) {
        if (CommonUtils.isEmpty(list)) {
            return ListUtils.EMPTY_LIST;
        }

        int size = list.size();
        pageNum = pageNum < 1 ? 1 : pageNum;
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize;
        end = end < size ? end : size;
        if (start > (size + 1)) {
            return ListUtils.EMPTY_LIST;
        }
        return list.subList(start, end);
    }

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

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

	public int getTotalPnCount() {
		return totalPnCount;
	}

	public void setTotalPnCount(int totalPnCount) {
		this.totalPnCount = totalPnCount;
	}

	public Object getRes() {
		return res;
	}

	public void setRes(Object res) {
		this.res = res;
	}
    
}
