
package bustbapi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.request.TmallItemAddSchemaGetRequest;
import com.taobao.api.request.TmallItemSchemaAddRequest;
import com.taobao.api.request.TmallProductAddSchemaGetRequest;
import com.taobao.api.request.TmallProductMatchSchemaGetRequest;
import com.taobao.api.request.TmallProductSchemaAddRequest;
import com.taobao.api.request.TmallProductSchemaMatchRequest;
import com.taobao.api.response.ItempropsGetResponse;
import com.taobao.api.response.TmallItemAddSchemaGetResponse;
import com.taobao.api.response.TmallItemSchemaAddResponse;
import com.taobao.api.response.TmallProductAddSchemaGetResponse;
import com.taobao.api.response.TmallProductMatchSchemaGetResponse;
import com.taobao.api.response.TmallProductSchemaAddResponse;
import com.taobao.api.response.TmallProductSchemaMatchResponse;

public class SchemaApi {

    public static final Logger log = LoggerFactory.getLogger(SchemaApi.class);

    // 获取匹配产品规则
    public static class tmallProductMatchSchemaGet extends TBApi<TmallProductMatchSchemaGetRequest, TmallProductMatchSchemaGetResponse, String> {

    	/** 
    	* 商品发布的目标类目，必须是叶子类目
    	 */
    	private Long categoryId;
    	
    	public String errorMsg = StringUtils.EMPTY;

        public tmallProductMatchSchemaGet(String sid, Long categoryId) {
            super(sid);
            this.categoryId = categoryId;
        }

		@Override
		public TmallProductMatchSchemaGetRequest prepareRequest() {
			TmallProductMatchSchemaGetRequest req = new TmallProductMatchSchemaGetRequest();
			req.setCategoryId(categoryId);
			
			return req;
		}

		@Override
		public String validResponse(TmallProductMatchSchemaGetResponse resp) {
			if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
			ErrorHandler.validTaoBaoResp(resp);
			if (!resp.isSuccess()) {
				errorMsg = resp.getSubMsg();
                return null;
            }
            return resp.getMatchResult();
		}

		@Override
		public String applyResult(String res) {
			return res;
		}

    }
    
    // product匹配接口
    public static class tmallProductSchemaMatch extends TBApi<TmallProductSchemaMatchRequest, TmallProductSchemaMatchResponse, String> {

    	/** 
    	* 商品发布的目标类目，必须是叶子类目
    	 */
    	private Long categoryId;

    	/** 
    	* 根据tmall.product.match.schema.get获取到的模板，ISV将需要的字段填充好相应的值结果XML。
    	 */
    	private String propvalues;
    	
    	public String errorMsg = StringUtils.EMPTY;

        public tmallProductSchemaMatch(String sid, Long categoryId, String propvalues) {
            super(sid);
            this.categoryId = categoryId;
            this.propvalues = propvalues;
        }

		@Override
		public TmallProductSchemaMatchRequest prepareRequest() {
			TmallProductSchemaMatchRequest req = new TmallProductSchemaMatchRequest();
			req.setCategoryId(categoryId);
			req.setPropvalues(propvalues);
			
			return req;
		}

		@Override
		public String validResponse(TmallProductSchemaMatchResponse resp) {
			if(resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(resp);
			if(!resp.isSuccess()) {
				errorMsg = resp.getSubMsg();
				return null;
			}
			return resp.getMatchResult();
		}

		@Override
		public String applyResult(String res) {
			return res;
		}

    }
    
	// 产品发布规则获取接口
	public static class tmallProductAddSchemaGet extends TBApi<TmallProductAddSchemaGetRequest, TmallProductAddSchemaGetResponse, String> {

		/** 
		 * 商品发布的目标类目，必须是叶子类目
		 */
		private Long categoryId;
		
		private Long brandId;
		
		public tmallProductAddSchemaGet(String sid, Long categoryId, Long brandId) {
			super(sid);
			this.categoryId = categoryId;
			this.brandId = brandId;
		}

		@Override
		public TmallProductAddSchemaGetRequest prepareRequest() {
			TmallProductAddSchemaGetRequest req = new TmallProductAddSchemaGetRequest();
			req.setCategoryId(categoryId);
			req.setBrandId(brandId);
			
			return req;
		}

		@Override
		public String validResponse(TmallProductAddSchemaGetResponse resp) {
			if (resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(resp);
			if (!resp.isSuccess()) {
				return null;
			}
			return resp.getAddProductRule();
		}

		@Override
		public String applyResult(String res) {
			return res;
		}

	}
	
	// 使用Schema文件发布一个产品
	public static class tmallProductSchemaAdd extends TBApi<TmallProductSchemaAddRequest, TmallProductSchemaAddResponse, String> {

		/** 
		 * 商品发布的目标类目，必须是叶子类目
		 */
		private Long categoryId;
		
		private Long brandId;
		
		/** 
		 * 根据tmall.product.add.schema.get生成的产品发布规则入参数据。
		 */
		private String xmlData;
		
		public tmallProductSchemaAdd(String sid, Long categoryId, Long brandId, String xmlData) {
			super(sid);
			this.categoryId = categoryId;
			this.brandId = brandId;
			this.xmlData = xmlData;
		}

		@Override
		public TmallProductSchemaAddRequest prepareRequest() {
			TmallProductSchemaAddRequest req = new TmallProductSchemaAddRequest();
			req.setCategoryId(categoryId);
			req.setBrandId(brandId);
			req.setXmlData(xmlData);
			
			return req;
		}

		@Override
		public String validResponse(TmallProductSchemaAddResponse resp) {
			if(resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(resp);
			if(!resp.isSuccess()) {
				return null;
			}
			return resp.getAddProductResult();
		}

		@Override
		public String applyResult(String res) {
			return res;
		}

    }
    
    // 天猫发布商品规则获取
    public static class tmallItemAddSchemaGet extends TBApi<TmallItemAddSchemaGetRequest, TmallItemAddSchemaGetResponse, String> {

    	/** 
    	* 商品发布的目标类目，必须是叶子类目
    	 */
    	private Long categoryId;

    	/** 
    	* 商品发布的目标product_id
    	 */
    	private Long productId;
    	
    	public String errorMsg = StringUtils.EMPTY;

        public tmallItemAddSchemaGet(String sid, Long categoryId, Long productId) {
            super(sid);
            this.categoryId = categoryId;
            this.productId = productId;
        }

		@Override
		public TmallItemAddSchemaGetRequest prepareRequest() {
			TmallItemAddSchemaGetRequest req = new TmallItemAddSchemaGetRequest();
			req.setCategoryId(categoryId);
			req.setProductId(productId);
			
			return req;
		}

		@Override
		public String validResponse(TmallItemAddSchemaGetResponse resp) {
			if(resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(resp);
			if(!resp.isSuccess()) {
				errorMsg = resp.getSubMsg();
				return null;
			}
			return resp.getAddItemResult();
		}

		@Override
		public String applyResult(String res) {
			return res;
		}

    }
    
    // 天猫根据规则发布商品
    public static class tmallItemSchemaAdd extends TBApi<TmallItemSchemaAddRequest, TmallItemSchemaAddResponse, String> {

    	/** 
    	* 商品发布的目标类目，必须是叶子类目
    	 */
    	private Long categoryId;

    	/** 
    	* 发布商品的productId，如果tmall.product.match.schema.get获取到得字段为空，这个参数传入0，否则需要通过tmall.product.schema.match查询到得可用productId
    	 */
    	private Long productId;

    	/** 
    	* 根据tmall.item.add.schema.get生成的商品发布规则入参数据
    	 */
    	private String xmlData;
    	
    	public String errorMsg = StringUtils.EMPTY;

        public tmallItemSchemaAdd(String sid, Long categoryId, Long productId, String xmlData) {
            super(sid);
            this.categoryId = categoryId;
            this.productId = productId;
            this.xmlData = xmlData;
        }

		@Override
		public TmallItemSchemaAddRequest prepareRequest() {
			TmallItemSchemaAddRequest req = new TmallItemSchemaAddRequest();
			req.setCategoryId(categoryId);
			req.setProductId(productId);
			req.setXmlData(xmlData);
			
			return req;
		}

		@Override
		public String validResponse(TmallItemSchemaAddResponse resp) {
			if(resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(resp);
			if(!resp.isSuccess()) {
				errorMsg = resp.getSubMsg();
				return null;
			}
			return resp.getAddItemResult();
		}

		@Override
		public String applyResult(String res) {
			return res;
		}

    }
    
    public static class ItemsPropsGetApi extends TBApi<ItempropsGetRequest, ItempropsGetResponse, JSONArray> {

        public Long cid;
        
        public Long pid;
        
        public String errorMsg = StringUtils.EMPTY;

        public ItemsPropsGetApi(Long cid, Long pid) {
            super();
            this.cid = cid;
            this.pid = pid;
        }

        @Override
        public ItempropsGetRequest prepareRequest() {
        	ItempropsGetRequest req = new ItempropsGetRequest();
        	req.setFields("pid,name,prop_values");
            req.setCid(cid);
            req.setPid(pid);

            return req;
        }

        @Override
        public JSONArray validResponse(ItempropsGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                errorMsg = resp.getSubMsg();
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                errorMsg = resp.getSubMsg();
                return null;
            }
            String body = resp.getBody();
            return JSON.parseObject(body).getJSONObject("itemprops_get_response")
                    .getJSONObject("item_props")
                    .getJSONArray("item_prop");
        }

        @Override
        public JSONArray applyResult(JSONArray res) {
            return res;
        }

    }
    
}
