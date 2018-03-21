package actions;

import java.util.Map;
import java.util.concurrent.Callable;

import com.ciaosir.client.pojo.PageOffset;

import actions.industry.IndustryDelistResultAction;
import result.TMResult;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import utils.CollectInfoByWebpage;
import models.user.User;

public class CallableThreadMultiUser implements Callable<TMResult>{

    private String searchKey;
    private String itemOrderType;
    private int searchPages;
    private String searchPlace;
    private String orderBy;
    private boolean isDesc;
    private PageOffset po;
    private User user;
    
    public CallableThreadMultiUser(String searchKey, String itemOrderType, int searchPages,
            String searchPlace, String orderBy, boolean isDesc, PageOffset po, User user) {
        this.searchKey = searchKey;
        this.itemOrderType = itemOrderType;
        this.searchPages = searchPages;
        this.searchPlace = searchPlace;
        this.orderBy = orderBy;
        this.isDesc = isDesc;
        this.po = po;
        this.user = user;
    }
    
    @Override
    public TMResult call() throws Exception {
        TMResult tmResult = IndustryDelistResultAction.findTaobaoItemsWithPaging(searchKey, 
                itemOrderType, searchPages, searchPlace, orderBy, isDesc, po, user);
        return tmResult;
    }
}
