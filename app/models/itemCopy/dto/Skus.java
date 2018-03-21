package models.itemCopy.dto;

import java.util.List;

public class Skus {

	    private int is_onsale=1;
	    private int limit_quantity;
	    private int quantity_delta;
	    private String thumb_url="";
	    private int multi_price;
	    private int price;
	    private String out_sku_sn="";
	    private String id="";
	    private List<Spec> spec;
	    private int weight;
	    public void setIs_onsale(int is_onsale) {
	         this.is_onsale = is_onsale;
	     }
	     public int getIs_onsale() {
	         return is_onsale;
	     }

	    public void setLimit_quantity(int limit_quantity) {
	         this.limit_quantity = limit_quantity;
	     }
	     public int getLimit_quantity() {
	         return limit_quantity;
	     }

	    public void setQuantity_delta(int quantity_delta) {
	         this.quantity_delta = quantity_delta;
	     }
	     public int getQuantity_delta() {
	         return quantity_delta;
	     }

	    public void setThumb_url(String thumb_url) {
	         this.thumb_url = thumb_url;
	     }
	     public String getThumb_url() {
	         return thumb_url;
	     }

	    public void setMulti_price(int multi_price) {
	         this.multi_price = multi_price;
	     }
	     public int getMulti_price() {
	         return multi_price;
	     }

	    public void setPrice(int price) {
	         this.price = price;
	     }
	     public int getPrice() {
	         return price;
	     }

	    public void setOut_sku_sn(String out_sku_sn) {
	         this.out_sku_sn = out_sku_sn;
	     }
	     public String getOut_sku_sn() {
	         return out_sku_sn;
	     }

	    public void setId(String id) {
	         this.id = id;
	     }
	     public String getId() {
	         return id;
	     }

	    public void setSpec(List<Spec> spec) {
	         this.spec = spec;
	     }
	     public List<Spec> getSpec() {
	         return spec;
	     }

	    public void setWeight(int weight) {
	         this.weight = weight;
	     }
	     public int getWeight() {
	         return weight;
	     }

}
