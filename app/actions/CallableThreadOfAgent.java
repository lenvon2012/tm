package actions;

import java.util.concurrent.Callable;

import com.ciaosir.client.pojo.ItemThumb;

import spider.ItemThumbSecond;
import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.TBSearchRes;

public class CallableThreadOfAgent implements Callable<String>{
    
//    private String queryArea;
//    private String url;
//    private String refer;
//    private TBSearchRes res;
//    
//    public CallableThreadOfAgent(String queryArea, String url, String refer, TBSearchRes res) {
//        this.queryArea = queryArea;
//        this.url = url;
//        this.refer = refer;
//        this.res = res;
//    }
    
    private int size;
    private ItemThumb itemThumb;
    private String refer;
    
    public CallableThreadOfAgent(int size, ItemThumb itemThumb, String refer) {
        this.size = size;
        this.itemThumb = itemThumb;
        this.refer = refer;
    }

    @Override
    public String call() throws Exception {
        String content = MainSearchApi.detailPageContent(size, itemThumb, refer);
        return content;
    }

}
