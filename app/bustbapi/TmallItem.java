package bustbapi;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.taobao.api.domain.ItemTmall;
import com.taobao.api.request.TmallItemSchemaIncrementUpdateRequest;
import com.taobao.api.response.TmallItemSchemaIncrementUpdateResponse;

public class TmallItem {
    
    protected static final Logger log = Logger.getLogger(TmallItemUpdater.class);

    public static class TmallItemUpdater extends TBApi<TmallItemSchemaIncrementUpdateRequest, TmallItemSchemaIncrementUpdateResponse, ItemTmall> {

        public Long numIid;
        
        public String errorMsg = StringUtils.EMPTY;

        public TmallItemUpdater(String sid, Long numIid) {
            super(sid);
            this.numIid = numIid;
        }

        @Override
        public TmallItemSchemaIncrementUpdateRequest prepareRequest() {
            TmallItemSchemaIncrementUpdateRequest reqt = new TmallItemSchemaIncrementUpdateRequest();
            reqt.setItemId(numIid);
            return reqt;
        }

        @Override
        public ItemTmall validResponse(TmallItemSchemaIncrementUpdateResponse resp) {
            if (resp == null) {
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                String result = resp.getUpdateItemResult();
                log.info("tmall result" + result);
                ItemTmall item = new ItemTmall();
                item.setNumIid(numIid);
                return item;
            }

            errorMsg = ErrorHandler.CommonTaobaoHandler.getInstance().getErrorMsg(resp);

            return null;
        }
        
        @Override
        public ItemTmall applyResult(ItemTmall res) {
            return res;
        }
        
        @Override
        public String getErrorMsg() {
            return this.errorMsg;
        }
    }

    public static class TmallItemTitleUpdater extends TmallItemUpdater {
        
        public String newTitle;

        public TmallItemTitleUpdater(String sid, Long numIid, String newTitle) {
            super(sid, numIid);
            this.newTitle = newTitle;
        }

        @Override
        public TmallItemSchemaIncrementUpdateRequest prepareRequest() {
        	if(!StringUtils.isEmpty(newTitle)) {
        		newTitle = newTitle.replace("<", "&lt;").replace(">", "&gt;");
        	}
            String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><itemRule><field id=\"title\" name=\"商品标题\" type=\"input\"><value>"
                    + newTitle
                    + "</value></field><field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\"><values><value>title</value></values></field></itemRule>";
            TmallItemSchemaIncrementUpdateRequest reqt = new TmallItemSchemaIncrementUpdateRequest();
            reqt.setItemId(numIid);
            reqt.setXmlData(xml);
            return reqt;
        }
    }
    
    /**
     * 天猫更新商品的商品描述  desc
     */
    public static class TmallItemDescUpdater extends TBApi<TmallItemSchemaIncrementUpdateRequest, TmallItemSchemaIncrementUpdateResponse, String> {

        private Long itemId;
        
        private String desc;
        
        private String xmlDataBefore = "<?xml version=\"1.0\" encoding=\"utf-8\"?><itemRule><field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\">"
                + "<values><value>description</value></values></field><field id=\"description\" name=\"商品描述\" type=\"complex\"><complex-values>"
                + "<field id=\"desc_module_116_cat_mod\" type=\"complex\"><complex-values><field id=\"desc_module_116_cat_mod_order\" type=\"input\">"
                + "<value>1</value></field><field id=\"desc_module_116_cat_mod_content\" type=\"input\"><value>";
                            
        private String xmlDateAfter = "</value></field></complex-values></field></complex-values></field></itemRule>";
        
        public TmallItemDescUpdater(String sid, Long numIid, String desc){
            super(sid);
            this.itemId = numIid;
            this.desc = desc;
        }
        
        @Override
        public TmallItemSchemaIncrementUpdateRequest prepareRequest() {
            TmallItemSchemaIncrementUpdateRequest req = new TmallItemSchemaIncrementUpdateRequest();
            req.setItemId(itemId);
            req.setXmlData(desc);
            log.info("天猫增量更新请求");
            log.info("itemId: " + itemId);
            log.info("xmlData: " + desc);
            return req;
        }

        @Override
        public String validResponse(TmallItemSchemaIncrementUpdateResponse resp) {
            if (resp == null) {
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (!resp.isSuccess()) {
                errorMsg = ErrorHandler.CommonTaobaoHandler.getInstance().getErrorMsg(resp);
                return null;
            }
            
            return resp.getUpdateItemResult();
        }

        @Override
        public String applyResult(String res) {
            return res;
        }
    }
    
    /**
     * 天猫更新无线端详情页
     */
    public static class TmallItemWapDescUpdater extends TBApi<TmallItemSchemaIncrementUpdateRequest, TmallItemSchemaIncrementUpdateResponse, String> {

        private Long itemId;
        
        private String desc;
        
        private String xmlDataBefore = "<?xml version=\"1.0\" encoding=\"utf-8\"?><itemRule><field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\">"
                + "<values><value>wap_desc</value></values></field> <field id=\"wap_desc\" name=\"无线商品描述\" type=\"complex\"><complex-values>";
                            
        private String xmlDateAfter = "</complex-values></field></itemRule>";
        
        public TmallItemWapDescUpdater(String sid, Long numIid, String desc){
            super(sid);
            this.itemId = numIid;
            desc = desc.replaceAll("<", "&lt;");
            desc = desc.replaceAll(">", "&gt;");
            this.desc = desc;
        }
        
        @Override
        public TmallItemSchemaIncrementUpdateRequest prepareRequest() {
            TmallItemSchemaIncrementUpdateRequest req = new TmallItemSchemaIncrementUpdateRequest();
            req.setItemId(itemId);
            req.setXmlData(this.xmlDataBefore + this.desc + this.xmlDateAfter);
            System.out.println(req.getXmlData());
            return req;
        }

        @Override
        public String validResponse(TmallItemSchemaIncrementUpdateResponse resp) {
            if (resp == null) {
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (!resp.isSuccess()) {
                errorMsg = ErrorHandler.CommonTaobaoHandler.getInstance().getErrorMsg(resp);
                return null;
            }
            
            return resp.getUpdateItemResult();
        }

        @Override
        public String applyResult(String res) {
            return res;
        }
    }
    
}