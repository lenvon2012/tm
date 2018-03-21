package bustbapi;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.taobao.api.domain.QueryRow;
import com.taobao.api.request.ClouddataMbpDataGetRequest;
import com.taobao.api.response.ClouddataMbpDataGetResponse;

public class MBPApi {

	private final static Logger log = LoggerFactory.getLogger(MBPApi.class);
	
	public static class MBPDataGet extends TBApi<ClouddataMbpDataGetRequest, 
		ClouddataMbpDataGetResponse, TMResult<List<QueryRow>>> {

        Long sqlId;

        String paramStr;

        public MBPDataGet(Long sqlId, String paramStr) {
            super();
            this.sqlId = sqlId;
            this.paramStr = paramStr;
        }
        
        public MBPDataGet(Long sqlId, String paramStr, String sid) {
            super();
            this.sqlId = sqlId;
            this.paramStr = paramStr;
            this.sid = sid;
        }

        @Override
        public ClouddataMbpDataGetRequest prepareRequest() {
        	ClouddataMbpDataGetRequest req = new ClouddataMbpDataGetRequest();
        	if(sqlId != null) {
        		req.setSqlId(sqlId);
        	}
        	if(!StringUtils.isEmpty(paramStr)) {
        		req.setParameter(paramStr);
        	}  
            return req;
        }

        @Override
        public TMResult<List<QueryRow>> validResponse(ClouddataMbpDataGetResponse resp) {
        	if(!resp.isSuccess()) {
        		log.error(resp.getMsg());
        		return new TMResult(false, "", null);
        	}
        	return new TMResult(true, "", resp.getRowList());
        	
        }

        @Override
        public TMResult<List<QueryRow>> applyResult(TMResult<List<QueryRow>> res) {
            return res;
        }

    }
}
