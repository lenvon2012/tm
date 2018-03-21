package bustbapi;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;
import com.taobao.api.request.ItemSkusGetRequest;
import com.taobao.api.request.SkusQuantityUpdateRequest;
import com.taobao.api.response.ItemSkusGetResponse;
import com.taobao.api.response.SkusQuantityUpdateResponse;

public class CarrierItemApi {

	public static final Logger log = LoggerFactory.getLogger(CarrierItemApi.class);

	/**
	 * 获取SKU
	 */
	public static class itemSkuGet extends TBApi<ItemSkusGetRequest, ItemSkusGetResponse, List<Sku>> {

		private String numIid;
		
		public itemSkuGet(String numIid, String sid) {
			super(sid);
			this.numIid = numIid;
		}

		@Override
		public ItemSkusGetRequest prepareRequest() {
			ItemSkusGetRequest req = new ItemSkusGetRequest();
			req.setFields("sku_id,quantity,outer_id");
			req.setNumIids(numIid);
			
			return req;
		}

		@Override
		public List<Sku> validResponse(ItemSkusGetResponse resp) {
			if (resp == null) {
				log.warn("No result return!!!");
			}

			ErrorHandler.validTaoBaoResp(this, resp);

			return resp.getSkus();
		}

		@Override
		public List<Sku> applyResult(List<Sku> res) {
			return res;
		}

	}
	
	/**
	 * SKU库存修改
	 */
	public static class skusQuantityUpdate extends TBApi<SkusQuantityUpdateRequest, SkusQuantityUpdateResponse, Item> {

		private Long numIid;
		
		private String skuidQuantities;
		
		public skusQuantityUpdate(Long numIid, String skuidQuantities, String sid) {
			super(sid);
			this.numIid = numIid;
			this.skuidQuantities = skuidQuantities;
		}

		@Override
		public SkusQuantityUpdateRequest prepareRequest() {
			SkusQuantityUpdateRequest req = new SkusQuantityUpdateRequest();
			req.setNumIid(numIid);
			req.setSkuidQuantities(skuidQuantities);
			
			return req;
		}

		@Override
		public Item validResponse(SkusQuantityUpdateResponse resp) {
			if (resp == null) {
				log.warn("No result return!!!");
			}

			ErrorHandler.validTaoBaoResp(this, resp);

			return resp.getItem();
		}

		@Override
		public Item applyResult(Item res) {
			return res;
		}

	}
	
}
