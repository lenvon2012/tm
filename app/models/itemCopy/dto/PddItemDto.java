/**
  * Copyright 2017 bejson.com 
  */
package models.itemCopy.dto;
import java.util.ArrayList;
import java.util.List;
public class PddItemDto {

    private int cat_id;
    private String goods_type;
    private String country="";
    private String country_id="";
    private String warehouse="";
    private String customs="";
    private int is_customs;
    private int is_pre_sale;
    private String pre_sale_time="";
    private int is_refundable;
    private int shipment_limit_second;
    private String event_type="";
    private String is_folt="";
    private String goods_name="";
    private String tiny_name="";
    private int market_price;
    private String share_desc="";
    private String goods_desc;
    private String image_url="";
    private String thumb_url="";
    private String hd_thumb_url="";
    private List<Gallery> gallery;
    private String cost_template_id;
    private String weight="";
    private String warm_tips="";
    private List<Skus> skus=new ArrayList();
    private String out_goods_sn="";
    private Groups groups;
    private int shelf_life;
    private String start_production_date="";
    private String end_production_date="";
    private String production_license="";
    private String production_standard_number="";
    private String goods_commit_id="";
    public void setCat_id(int cat_id) {
         this.cat_id = cat_id;
     }
     public int getCat_id() {
         return cat_id;
     }

    public void setGoods_type(String goods_type) {
         this.goods_type = goods_type;
     }
     public String getGoods_type() {
         return goods_type;
     }

    public void setCountry(String country) {
         this.country = country;
     }
     public String getCountry() {
         return country;
     }

    public void setCountry_id(String country_id) {
         this.country_id = country_id;
     }
     public String getCountry_id() {
         return country_id;
     }

    public void setWarehouse(String warehouse) {
         this.warehouse = warehouse;
     }
     public String getWarehouse() {
         return warehouse;
     }

    public void setCustoms(String customs) {
         this.customs = customs;
     }
     public String getCustoms() {
         return customs;
     }

    public void setIs_customs(int is_customs) {
         this.is_customs = is_customs;
     }
     public int getIs_customs() {
         return is_customs;
     }

    public void setIs_pre_sale(int is_pre_sale) {
         this.is_pre_sale = is_pre_sale;
     }
     public int getIs_pre_sale() {
         return is_pre_sale;
     }

    public void setPre_sale_time(String pre_sale_time) {
         this.pre_sale_time = pre_sale_time;
     }
     public String getPre_sale_time() {
         return pre_sale_time;
     }

    public void setIs_refundable(int is_refundable) {
         this.is_refundable = is_refundable;
     }
     public int getIs_refundable() {
         return is_refundable;
     }

    public void setShipment_limit_second(int shipment_limit_second) {
         this.shipment_limit_second = shipment_limit_second;
     }
     public int getShipment_limit_second() {
         return shipment_limit_second;
     }

    public void setEvent_type(String event_type) {
         this.event_type = event_type;
     }
     public String getEvent_type() {
         return event_type;
     }

    public void setIs_folt(String is_folt) {
         this.is_folt = is_folt;
     }
     public String getIs_folt() {
         return is_folt;
     }

    public void setGoods_name(String goods_name) {
         this.goods_name = goods_name;
     }
     public String getGoods_name() {
         return goods_name;
     }

    public void setTiny_name(String tiny_name) {
         this.tiny_name = tiny_name;
     }
     public String getTiny_name() {
         return tiny_name;
     }

    public void setMarket_price(int market_price) {
         this.market_price = market_price;
     }
     public int getMarket_price() {
         return market_price;
     }

    public void setShare_desc(String share_desc) {
         this.share_desc = share_desc;
     }
     public String getShare_desc() {
         return share_desc;
     }

    public void setGoods_desc(String goods_desc) {
         this.goods_desc = goods_desc;
     }
     public String getGoods_desc() {
         return goods_desc;
     }

    public void setImage_url(String image_url) {
         this.image_url = image_url;
     }
     public String getImage_url() {
         return image_url;
     }

    public void setThumb_url(String thumb_url) {
         this.thumb_url = thumb_url;
     }
     public String getThumb_url() {
         return thumb_url;
     }

    public void setHd_thumb_url(String hd_thumb_url) {
         this.hd_thumb_url = hd_thumb_url;
     }
     public String getHd_thumb_url() {
         return hd_thumb_url;
     }

    public void setGallery(List<Gallery> gallery) {
         this.gallery = gallery;
     }
     public List<Gallery> getGallery() {
         return gallery;
     }

    public void setCost_template_id(String cost_template_id) {
         this.cost_template_id = cost_template_id;
     }
     public String getCost_template_id() {
         return cost_template_id;
     }

    public void setWeight(String weight) {
         this.weight = weight;
     }
     public String getWeight() {
         return weight;
     }

    public void setWarm_tips(String warm_tips) {
         this.warm_tips = warm_tips;
     }
     public String getWarm_tips() {
         return warm_tips;
     }

    public void setSkus(List<Skus> skus) {
         this.skus = skus;
     }
     public List<Skus> getSkus() {
         return skus;
     }

    public void setOut_goods_sn(String out_goods_sn) {
         this.out_goods_sn = out_goods_sn;
     }
     public String getOut_goods_sn() {
         return out_goods_sn;
     }

    public void setGroups(Groups groups) {
         this.groups = groups;
     }
     public Groups getGroups() {
         return groups;
     }

    public void setShelf_life(int shelf_life) {
         this.shelf_life = shelf_life;
     }
     public int getShelf_life() {
         return shelf_life;
     }

    public void setStart_production_date(String start_production_date) {
         this.start_production_date = start_production_date;
     }
     public String getStart_production_date() {
         return start_production_date;
     }

    public void setEnd_production_date(String end_production_date) {
         this.end_production_date = end_production_date;
     }
     public String getEnd_production_date() {
         return end_production_date;
     }

    public void setProduction_license(String production_license) {
         this.production_license = production_license;
     }
     public String getProduction_license() {
         return production_license;
     }

    public void setProduction_standard_number(String production_standard_number) {
         this.production_standard_number = production_standard_number;
     }
     public String getProduction_standard_number() {
         return production_standard_number;
     }

    public void setGoods_commit_id(String goods_commit_id) {
         this.goods_commit_id = goods_commit_id;
     }
     public String getGoods_commit_id() {
         return goods_commit_id;
     }

}