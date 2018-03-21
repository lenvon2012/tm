package underup.frame.industry;

import java.util.concurrent.Callable;

import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.TBSearchRes;

public class SearchPages implements Callable<TBSearchRes>{
    
    private int pn;
    private String orderType;
    private long frontCid;
    private String host;
    
    public SearchPages(int pn, String orderType, long frontCid){
        this.pn = pn;
        this.orderType = orderType;
        this.frontCid = frontCid;
    }
    
    public SearchPages(String host,int pn, String orderType, long frontCid){
        this.host=host;
        this.pn = pn;
        this.orderType = orderType;
        this.frontCid = frontCid;
    }
    @Override
    public TBSearchRes call() throws Exception {
        return MainSearchApi.search(this.host,this.pn, this.orderType, this.frontCid);
    }
    
}
