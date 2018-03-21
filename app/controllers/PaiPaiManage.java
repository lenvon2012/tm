package controllers;

import static java.lang.String.format;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.comment.Comments;
import models.paipai.PaiPaiUser;
import models.ppmanage.PPStock;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojo.webpage.top.TMWordBase;
import ppapi.PaiPaiItemApi;
import ppapi.PaiPaiManageApi.PPmodifyItemStateApi;
import ppapi.PaiPaiManageApi.PPmodifyItemStockApi;
import ppapi.PaiPaiManageApi.modifyResult;
import ppapi.PaiPaiOrderFormApi;
import ppapi.models.PaiPaiItem;
import ppapi.models.PaiPaiItem.PaiPaiItemAttr;
import result.TMPaginger;
import result.TMResult;
import sug.api.QuerySugAPI;
import titleDiag.DiagResult;
import actions.DiagAction;
import autotitle.AutoSplit;
import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.SearchAPIs;
import com.ciaosir.client.api.SearchAPIs.SearchParams;
import com.ciaosir.client.api.SearchAPIs.SearchRes;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;

import dao.paipai.PaiPaiItemDao;
import dao.paipai.PaiPaiStockDao;


/**
 * @author haoyongzh
 *
 */
public class PaiPaiManage extends PaiPaiController {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiManage.class);
    
    public static final String TAG = "PaiPaiManage";
    
    public static void batch() {
    	render("paipaimanage/batchmanage.html");
    }
    
    public static void stockcenter(){
    	render("paipaimanage/stockcenter.html");
    }
    
    public static void diagnose() {
    	render("paipaimanage/diagnose.html");
    }
    /**
     * 查询宝贝
     * @param title 标题
     * @param catId 分类
     * @param state 状态
     * @param orderProp 排序的属性
     * @param orderType 是升序还是降序
     */   
    public static void queryItems(String title,String state,Long catId,
    		 String orderProp, String orderType,int pn,int ps){
    	
    	PaiPaiUser user=getUser();    	

    	PageOffset po =new PageOffset(pn, ps);
    	
    	if(catId==null){
    		catId=0L;
    	}
    	
        boolean isOrderAsc = true;
        if (!StringUtils.isEmpty(orderType) && orderType.toLowerCase().equals("desc"))
            isOrderAsc = false;
        else
            isOrderAsc = true;

        if (StringUtils.isEmpty(state)) {
        	state = "ALL";
        }

        TMResult result = PaiPaiItemDao.findBatchwithOrder(user.getId(),title,state,catId, orderProp, isOrderAsc, po);
        
        renderJSON(result);
    }
    
	public static void queryItemsWithDiag(String title, String state,
			Long catId, String orderProp, String orderType, int pn, int ps) {

		PaiPaiUser user = getUser();

		PageOffset po = new PageOffset(pn, ps);

		if (catId == null) {
			catId = 0L;
		}

		boolean isOrderAsc = true;
		if (!StringUtils.isEmpty(orderType)
				&& orderType.toLowerCase().equals("desc"))
			isOrderAsc = false;
		else
			isOrderAsc = true;

		if (StringUtils.isEmpty(state)) {
			state = "ALL";
		}

		TMResult result = PaiPaiItemDao.findBatchwithOrder(user.getId(), title,
				state, catId, orderProp, isOrderAsc, po);

		renderJSON(result);
	}
    
	public static void singleDiag() {
        String title = params.get("title");
        String itemCode = params.get("itemCode");
//        int pn = NumberUtil.parserInt(params.get("pn"), 1);
//        int offset = NumberUtil.parserInt(params.get("offset"), 0);

        PaiPaiUser user = getUser();
//        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (StringUtils.isBlank(title)) {
            renderText("宝贝标题为空");
        }
        try {
        	PaiPaiItem item = new PaiPaiItemApi.PaiPaiItemDetailApi(user, itemCode).call();
            DiagResult res = DiagAction.doDiag(item.getItemPrice(), item.getTitle(),
            		formatProp(item.getAttr()), item.getPicPath());
            renderJSON(JsonUtil.getJson(res));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderText("服务器不正常");
    }
	
	public static void props(String itemCode) {

        log.info(format("props:numIid".replaceAll(", ", "=%s, ") + "=%s", itemCode));

        PaiPaiUser user = getUser();
        PaiPaiItem item = new PaiPaiItemApi.PaiPaiItemDetailApi(user, itemCode).call();
        List<StringPair> list = genPropsPair(item.getAttr());
        String content = JsonUtil.getJson(list);
        log.info("[back content:]" + content);
        renderJSON(content);
    }
	
	public static void longTail(String s, String title) {
        s = tryGetSearchKey(s, title);
        // List<String> result = QuerySugAPI.getQuerySugList(s, false);
        List<String> result = QuerySugAPI.getQuerySugListSimple(s);

        renderJSON(JsonUtil.getJson(result));
    }
	
	public static List<StringPair> genPropsPair(List<PaiPaiItemAttr> attrs) {
		List<StringPair> pairs = new ArrayList<StringPair>();
		if(attrs != null && attrs.size() > 0) {
			for(PaiPaiItemAttr attr : attrs) {
				pairs.add(new StringPair(attr.getAttrName(),StringUtils.join(attr.getAttrOptionName(),",")));
			}
		}
		return pairs;
	}
	
	public static String formatProp(List<PaiPaiItemAttr> attrs) {
		StringBuilder sb = new StringBuilder();
		if(attrs == null || attrs.size() <= 0){
			return StringUtils.EMPTY;
		}
		int count = 0;
		for(PaiPaiItemAttr attr : attrs){
			count++;
			sb.append(attr.getAttrId()).append(":").append(attr.getAttrOptionId()).append(":")
				.append(attr.attrName).append(":");
			if(attr.getAttrOptionName() != null && attr.getAttrOptionName().size() > 0) {
				sb.append(StringUtils.join(attr.getAttrOptionName(), ","));
				if(count < attrs.size()){
					sb.append(";");
				}
			}
		}
		log.info("formatProp List<PaiPaiItemAttr> attrs to " + sb.toString());
		return sb.toString();
		
	}
	public static void rename(String itemCode, String title) {

		log.info(format("paipai item rename:numIid, title".replaceAll(", ", "=%s, ") + "=%s", itemCode, title));
        if(StringUtils.isBlank(itemCode)) {
        	log.error("rename itemCode is blank");
        	renderText("itemCode is blank!");
        }
        if(StringUtils.isBlank(title)) {
        	log.error("rename title is blank");
        	renderText("title is blank!");
        }
		PaiPaiUser user = getUser();
        title = StringUtils.trim(title);
        while (ChsCharsUtil.length(title) > 60) {
            title = title.substring(0, title.length() - 1);
        }
        boolean isSuccess = new PaiPaiItemApi.PaiPaiItemUpdateTitleApi(user, itemCode, title).call();
        if(isSuccess){
        	PaiPaiItem item = PaiPaiItemDao.findByItemCode(user.getId(), itemCode);
        	item.setItemName(title);
        	item.jdbcSave();
        	renderText("rename success!");
        }
        renderText("rename failed!");
    }
	
	public static void getPromoteWords() {
	    renderJSON(JsonUtil.getJson(DiagResult.promoteWordsArr));
	}
	 
	/**
     * @param word
     * @param pn
     * @param ps

     * @param order: pv, click, scount, score, strikeFocus
     * scount --> itemCount 宝贝数
     * score --> pv / itemCount  性价比
     * strikeFocus --> ctr 点击率 -- 转化率
     * @throws IOException

     * @param order: pv desc, click desc, scount desc, score desc, strikeFocus desc,pv asc, click asc, scount asc, score asc, strikeFocus asc
     * scount --> itemCount
     * score --> pv / itemCount
     * strikeFocus --> ctr
     * @throws IOException

     */
    public static void busSearch(int pn, int ps, String order, String sort, String word)
            throws IOException {
        if (order == null || order.isEmpty()) {
            order = "pv";
        }
        if (sort == null || sort.isEmpty()) {
            sort = "desc";
        }
//        renderMockFileInJsonIfDev("words.bussearch.json");
        //String word = params.get("s");
        String title = params.get("title");
        //String order = params.get("order");
        //String sort = params.get("sort");

        word = tryGetSearchKey(word, title);
        word = CommonUtils.escapeSQL(word);
//        word = StringUtils.replace(word, " ", StringUtils.EMPTY);

        int minPageSize = 20;
        if (ps < minPageSize) {
            ps = minPageSize;
        }
        PageOffset po = new PageOffset(pn, ps, minPageSize);
        try {
            if (StringUtils.isBlank(order)) {
                order = "score";
            }
            if (StringUtils.isEmpty(sort)) {
                sort = "asc";
            }
            final String realSort = sort;
            final String realOrder = order;
            List<String> list = new AutoSplit(word, false).execute();
//            List<String> list = new ArrayList<String>();
//            String[] arr = word.split(",");
//            for (String str : arr) {
//                if (StringUtils.isBlank(str)) {
//                    continue;
//                }
//                list.add(str);
//            }
//
//            log.info("[list]" + list);

            TMResult tmresult = new TMApi.TMWordBaseApi(list, po, order, sort).execute();
            List<TMWordBase> newlistBases = (List<TMWordBase>) tmresult.getRes();
            Collections.sort(newlistBases, new Comparator<TMWordBase>() {
                public int compare(TMWordBase arg0, TMWordBase arg1) {
                    if (realSort.equals("desc")) {
                        return arg1.getByProp(realOrder) - arg0.getByProp(realOrder);
                    } else {
                        return arg0.getByProp(realOrder) - arg1.getByProp(realOrder);
                    }
                }
            });
            tmresult.setRes(newlistBases);
            renderJSON(JsonUtil.getJson(tmresult));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderJSON(TMResult.failMsg("亲,词库数据暂时有点问题哟,可以联系客服呢"));
    }
    
    static String tryGetSearchKey(String s, String title) {
        if (!StringUtils.isBlank(s)) {
            return s;
        }
        if (!StringUtils.isEmpty(title)) {
            return title;
        }

        return StringUtils.EMPTY;

    }
    
    public static void estimateKeyword(String title) {
    	if(StringUtils.isBlank(title)){
    		renderJSON(JsonUtil.getJson(new ArrayList<String>()));
    	}
        try {
            List<String> words = new AutoSplit(title, ListUtils.EMPTY_LIST, true).execute();
            renderJSON(JsonUtil.getJson(words));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }
    
    public static void searchKeywords(String s, String title, int pn, int ps) throws ClientException {

        s = tryGetSearchKey(s, title);

        if (StringUtils.isBlank(s)) {
            renderJSON(TMPaginger.makeEmptyFail("亲，请输入搜索关键词哦"));
        }
        pn = pn < 1 ? 1 : pn;
//        ps = ps < 10 ? 30 : ps;
        ps = 60;

        String[] keys = new AutoSplit(s, ListUtils.EMPTY_LIST, true).execute().toArray(new String[] {});

        if (keys.length > 2) {
            keys = (String[]) ArrayUtils.subarray(keys, 0, 2);
        }

        SearchRes call = new SearchAPIs.TermSearchApi(getSClient(), keys, pn, ps, SearchParams.MustBooleanPVNeededQuery)
                .call();
//        log.info("[res num :]" + call.getList().size());

        List<IWordBase> bases = SearchAPIs.buildWordBase(call);
//        log.info("[ basesa num]" + bases.size());

//        @SuppressWarnings("deprecation")
        //TMPaginger paginger = new TMPaginger(pn, ps, (int) call.getTotalHits(), bases);
        TMResult res = new TMResult(bases, (int) call.getTotalHits(), new PageOffset(pn, ps));
        renderJSON(JsonUtil.getJson(res));
    }
    
    public static void queryStocks(String title,String state,Long catId,
   		 String orderProp, String orderType,int pn,int ps){
    	   	
    	PaiPaiUser user=getUser();    	

    	PageOffset po =new PageOffset(pn, ps);
    	
    	if(catId==null){
    		catId=0L;
    	}
    	
        boolean isOrderAsc = true;
        if (!StringUtils.isEmpty(orderType) && orderType.toLowerCase().equals("desc"))
            isOrderAsc = false;
        else
            isOrderAsc = true;

        if (StringUtils.isEmpty(state)) {
        	state = "ALL";
        }
    	
        List<PaiPaiItem> ItemList = PaiPaiItemDao.findStockItemwithOrder(user.getId(),title, state, catId, orderProp, isOrderAsc, po);

        long count=PaiPaiItemDao.countStockItemwithOrder(user.getId(), title, state, catId);
        
        List<QueryStock> stockList=new ArrayList<QueryStock>();
        
        if(CommonUtils.isEmpty(ItemList)){
        	renderJSON(new TMResult("没有找到相应的宝贝！"));
        }
        
        for(PaiPaiItem item : ItemList){
        	List<PPStock> stockListArray=PaiPaiStockDao.findStockByitemCode(user.getId(),item.getItemCode());
        	if(!CommonUtils.isEmpty(stockListArray)){
            	for(PPStock stock : stockListArray){
            		QueryStock qs=new QueryStock(stock, item.getItemName());
            		stockList.add(qs);
            	}
        	}
        }
                
        renderJSON(new TMResult(stockList,(int)count,po));
              
    }
    
    public static class QueryStock{
    	PPStock stock;
    	String itemName;
    	public QueryStock(PPStock stock,String itemName){
    		this.stock=stock;
    		this.itemName=itemName;
    	}
    }
    
    /**
     * 批量上架
     * @param numIids
     */    
    public static void doBatchListing(List<String> numIidList) {
    	
        PaiPaiUser user = getUser();
        
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要上架的宝贝");
        }
        
        String itemCodes="";
        
        for(String numIid : numIidList){
        	if(itemCodes ==""){
        		itemCodes=numIid;
        	}
        	else{
        		itemCodes+=","+numIid;
        	}
        }

        String itemState="IS_FOR_SALE";
        List<BatchMessage> batchMsgList = new ArrayList<BatchMessage>();
        List<String>  successList=new ArrayList<String>();
         
        List<modifyResult> modifyResultList = new PPmodifyItemStateApi(user, itemCodes, itemState).call();
        
        for(modifyResult result :modifyResultList){
        	if(result.errorMessage!=null){
        		renderError(result.errorMessage);
        	}
        	if(result.result==0){        	
        		successList.add(result.itemCode);
        	}
        	else{
        		//返回错误日志
        		PaiPaiItem errorItem =PaiPaiItemDao.findByItemCode(user.getId(), result.itemCode);
        		BatchMessage batchMsg = new BatchMessage(errorItem, result.stateDesc);
        		batchMsgList.add(batchMsg);
        	}
        }

		//修改PaiPaiItem数据库
        String successCodes=StringUtils.join(successList, ',');

		List<PaiPaiItem> sucitemList=PaiPaiItemDao.findByItemCodes(user.getId(), successCodes);
		
		for(PaiPaiItem item :sucitemList){
			item.setItemState(itemState);
			boolean success=item.jdbcSave();
			if(!success){
				log.error("JDBC SAVE ERROR:"+item.getItemCode());
			}
		}

		renderJSON(new TMResult(batchMsgList));
		
    }
    
    /**
     * 批量下架
     * @param numIids
     */
    
    public static void doBatchDeListing(List<String> numIidList) {
    	
        PaiPaiUser user = getUser();
        
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要上架的宝贝");
        }
        
        String itemCodes="";
        
        for(String numIid : numIidList){
        	if(itemCodes ==""){
        		itemCodes=numIid;
        	}
        	else{
        		itemCodes+=","+numIid;
        	}
        }
        String itemState="IS_IN_STORE";
        
        List<BatchMessage> batchMsgList = new ArrayList<BatchMessage>();
        List<String>  successList=new ArrayList<String>();
         
        List<modifyResult> modifyResultList = new PPmodifyItemStateApi(user, itemCodes, itemState).call();
        
        for(modifyResult result :modifyResultList){
        	if(result.errorMessage!=null){
        		renderError(result.errorMessage);
        	}
        	if(result.result==0){        	
        		successList.add(result.itemCode);
        	}
        	else{
        		//返回错误日志
        		PaiPaiItem errorItem =PaiPaiItemDao.findByItemCode(user.getId(), result.itemCode);
        		BatchMessage batchMsg = new BatchMessage(errorItem, result.stateDesc);
        		batchMsgList.add(batchMsg);
        	}
        }

		//修改PaiPaiItem数据库
        String successCodes=StringUtils.join(successList, ',');

		List<PaiPaiItem> sucitemList=PaiPaiItemDao.findByItemCodes(user.getId(), successCodes);
		
		for(PaiPaiItem item :sucitemList){
			item.setItemState(itemState);
			boolean success=item.jdbcSave();
			if(!success){
				log.error("JDBC SAVE ERROR:"+item.getItemCode());
			}
		}

		renderJSON(new TMResult(batchMsgList));
    }
    
    public static class BatchMessage{
    	PaiPaiItem item;
    	String errorMessage;
    	
    	public BatchMessage(PaiPaiItem item, String errorMessage){
    		this.item=item;
    		this.errorMessage=errorMessage;
    	}
    }
    
    /**
     * @param numIidList
     * @param newPrice
     */
    public static void doModifyPrice(List<String> numIidList, Long newPrice){
    	PaiPaiUser user=getUser();

    	List<Comments> comment=new PaiPaiOrderFormApi.getUnCommentOrderApi(user).call();
        	
        List<BatchMessage> batchMsgList = new ArrayList<BatchMessage>();
        
        if (newPrice<= 0) {
            renderError("请先输入正确的价格");
        }
        
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        for(String numIid : numIidList){
            List<StockModifyPrice> stockList=new ArrayList<StockModifyPrice>();
        	
        	PaiPaiItem item=PaiPaiItemDao.findByItemCode(user.getId(),numIid);
        	
        	List<PPStock> stockListArray=PaiPaiStockDao.findStockByitemCode(user.getId(),numIid);
        	
        	if(CommonUtils.isEmpty(stockListArray)){
        		renderError("未找到宝贝信息！");
        	}
        	
        	for(PPStock stock :stockListArray){
            	StockModifyPrice stostr =new StockModifyPrice(user.getId(), stock.getSkuId(), newPrice,stock.getNum(),stock.getStatus());

            	stockList.add(stostr);
            	
            	String stockJsonList=JsonUtil.getJson(stockList);
            	
            	String errorMessage=new PPmodifyItemStockApi(user,numIid,stockJsonList).call();
            	
            	if(errorMessage!=null){
            		BatchMessage batchMsg = new BatchMessage(item, errorMessage);
            		batchMsgList.add(batchMsg);
            	}
            	else{
            		item.setItemPrice(Long.valueOf(newPrice).intValue());
            		item.jdbcSave();
            		stock.setPrice(newPrice);
            		stock.jdbcSave();
            	}
            	//stock未存
        	}      	        	
        }
        
		renderJSON(new TMResult(batchMsgList));
        	
    }
    
    public static void appInfo() {
        APIConfig config = APIConfig.get();
        String appKey = config.getApiKey();
        renderText(appKey);
    }
    
    /**
     * 批量比例加价
     * @param toCancelNumIid
     * @param newPriceStr
     */
    public static void doModifyPriceByScale(List<String> numIidList, String priceScaleStr) {
    	PaiPaiUser user=getUser();
    	
        List<BatchMessage> batchMsgList = new ArrayList<BatchMessage>();
    	
        BigDecimal priceScale = new BigDecimal(0);
        try {
            priceScale = new BigDecimal(priceScaleStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("请先输入正确的加价比例");
        }
        
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }
        
        for(String numIid : numIidList){
            List<StockModifyPrice> stockList=new ArrayList<StockModifyPrice>();
        	
        	PaiPaiItem item=PaiPaiItemDao.findByItemCode(user.getId(),numIid);
        	
            BigDecimal oldPrice = new BigDecimal(0);
            try {
                oldPrice = new BigDecimal(item.getItemPrice());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                continue;
            }
            //Decimal
            BigDecimal multiTime = new BigDecimal(1);
            multiTime = multiTime.add(priceScale);
            BigDecimal newPrice = oldPrice.multiply(multiTime);
        	
            
        	List<PPStock> stockListArray=PaiPaiStockDao.findStockByitemCode(user.getId(),numIid);
        	
        	if(CommonUtils.isEmpty(stockListArray)){
        		renderError("未找到宝贝信息！");
        	}        	
        	
        	for(PPStock stock :stockListArray){
            	StockModifyPrice stostr =new StockModifyPrice(user.getId(), stock.getSkuId(), newPrice.longValue(),stock.getNum(),stock.getStatus());

            	stockList.add(stostr);
            	
            	String stockJsonList=JsonUtil.getJson(stockList);
            	
            	String errorMessage=new PPmodifyItemStockApi(user,numIid,stockJsonList).call();
            	
            	if(errorMessage!=null){
            		BatchMessage batchMsg = new BatchMessage(item, errorMessage);
            		batchMsgList.add(batchMsg);
            	}
            	else{
            		item.setItemPrice(newPrice.intValue());
            		item.jdbcSave();
            		stock.setPrice(newPrice.longValue());
            		stock.jdbcSave();
            	}
        	}
        	
        	        	
        }
        
		renderJSON(new TMResult(batchMsgList));

    }
    
    public static void doModifyStock(List<Long> skuIdList, Long newStock){
    	
    	PaiPaiUser user=getUser();

        List<BatchMessage> batchMsgList = new ArrayList<BatchMessage>();
        
        if (newStock<= 0) {
            renderError("请先输入正确的价格");
        }
        
        if (skuIdList == null || skuIdList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        for(Long skuId : skuIdList){
            List<StockModifyPrice> stockList=new ArrayList<StockModifyPrice>();       	
        	
        	PPStock stock=PaiPaiStockDao.findStockBySkuId(user.getId(), skuId);
        	        	
        	if(stock==null){
        		renderError("未找到宝贝信息！");
        	}
        	
        	PaiPaiItem item=PaiPaiItemDao.findByItemCode(user.getId(),stock.getItemCode());
        	
        	StockModifyPrice stostr =new StockModifyPrice(user.getId(), stock.getSkuId(), stock.getPrice(),newStock,stock.getStatus());

        	stockList.add(stostr);
        	
        	String stockJsonList=JsonUtil.getJson(stockList);
        	
        	String errorMessage=new PPmodifyItemStockApi(user,item.getItemCode(),stockJsonList).call();
        	
        	if(errorMessage!=null){
        		BatchMessage batchMsg = new BatchMessage(item, errorMessage);
        		batchMsgList.add(batchMsg);
        	}
        	else{
        		stock.setNum(newStock);
        		stock.jdbcSave();
        	}
      	        	        	
        }
        
		renderJSON(new TMResult(batchMsgList));
    }
    
    public static class StockModifyPrice{
    	Long sellerUin;
    	Long skuId;
    	Long price;
    	Long num;
    	int status;
    	public StockModifyPrice(Long sellerUin,Long skuId,Long price,Long num,int status){
    		this.sellerUin=sellerUin;
    		this.skuId=skuId;
    		this.price=price;
    		this.num=num;
    		this.status=status;
    	}
		public Long getSellerUin() {
			return sellerUin;
		}
		public void setSellerUin(Long sellerUin) {
			this.sellerUin = sellerUin;
		}
		public Long getSkuId() {
			return skuId;
		}
		public void setSkuId(Long skuId) {
			this.skuId = skuId;
		}
		public Long getPrice() {
			return price;
		}
		public void setPrice(Long price) {
			this.price = price;
		}
		public Long getNum() {
			return num;
		}
		public void setNum(Long num) {
			this.num = num;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}

    }
    
    public static class StockString{
    	Long itemId;
    	String attr;//商品库存属性串,即原来的stockAttr
    	String desc;//描述信息
    	Long sellerUin;
    	Long soldNum;
    	Long price;
    	Long num;//库存数量
    	Long skuId;
    	Long stockId;
    	int status;
    	String saleAttr;
    	String specAttr;
    	String pic;
    	
    	public StockString(){

    	}

		public Long getItemId() {
			return itemId;
		}

		public void setItemId(Long itemId) {
			this.itemId = itemId;
		}

		public String getAttr() {
			return attr;
		}

		public void setAttr(String attr) {
			this.attr = attr;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public Long getSellerUin() {
			return sellerUin;
		}

		public void setSellerUin(Long sellerUin) {
			this.sellerUin = sellerUin;
		}

		public Long getSoldNum() {
			return soldNum;
		}

		public void setSoldNum(Long soldNum) {
			this.soldNum = soldNum;
		}

		public Long getPrice() {
			return price;
		}

		public void setPrice(Long price) {
			this.price = price;
		}

		public Long getNum() {
			return num;
		}

		public void setNum(Long num) {
			this.num = num;
		}

		public Long getSkuId() {
			return skuId;
		}

		public void setSkuId(Long skuId) {
			this.skuId = skuId;
		}

		public Long getStockId() {
			return stockId;
		}

		public void setStockId(Long stockId) {
			this.stockId = stockId;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getSaleAttr() {
			return saleAttr;
		}

		public void setSaleAttr(String saleAttr) {
			this.saleAttr = saleAttr;
		}

		public String getSpecAttr() {
			return specAttr;
		}

		public void setSpecAttr(String specAttr) {
			this.specAttr = specAttr;
		}

		public String getPic() {
			return pic;
		}

		public void setPic(String pic) {
			this.pic = pic;
		};
    	
    	
    	
    }
    
}
