package bustbapi;

import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;

public abstract class TBApiPages<K extends TaobaoRequest<V>, V extends TaobaoResponse, W> extends TBApi<K, V, W> {

    public TBApiPages(String sid) {
        super(sid);
    }

    public abstract K setPageNo(K req, long pageNo);
    
    @Override
    public W validResponse(V resp) {
        // TODO Auto-generated method stub
        return null;
    }

    

}
