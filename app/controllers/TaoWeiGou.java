
package controllers;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import actions.shopping.RandomCatTopWordAction;
import actions.shopping.RandomShareAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.SplitUtils;

import dao.popularized.PopularizedDao;
import dao.popularized.ShoppingDao;

public class TaoWeiGou extends Controller {

    private static final Logger log = LoggerFactory.getLogger(TaoWeiGou.class);
    
    private static final int ItemLimitNum = 30;
    
    private static final int TopWordLimitNum = 30;

    public static void index(Long numIid, String search, Long catid) {
        showItems(numIid, search, catid);
    }

    static void goIndex() {
        //render("taoweigou/itemlist.html");
        showItems(0L, "", 0L);
    }
    
    private static String decodeSearch(String search) {
        if (!StringUtils.isEmpty(search)) {
            try {
                search = URLDecoder.decode(search, "utf-8");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        
        return search;
    }
    
    static void showItems(Long numIid, String search, Long catid) {
        
        search = decodeSearch(search);
        
        Long userId = Shopping.getVisitUserId();
        
        List<PopularizeItemInfo> normalItemList = findItemInfos(numIid, userId, catid, 
                search, PopularizedStatus.Normal);
        List<List<PopularizeItemInfo>> normalItemsList = SplitUtils.splitToSubList(normalItemList, 3);
        boolean isNormalMasonry = normalItemList.size() >= ItemLimitNum;
        
        List<PopularizeItemInfo> hotItemList = findItemInfos(numIid, userId, catid, 
                search, PopularizedStatus.HotSale);
        boolean isHotMasonry = hotItemList.size() >= ItemLimitNum;
        
        List<String> wordList = RandomCatTopWordAction.doRandomWords(TopWordLimitNum);
        WeiGouCommonInfo commonInfo = new WeiGouCommonInfo(search, hotItemList, catid, wordList);
        
        boolean isNoItem = CommonUtils.isEmpty(normalItemList);
        
        String pageTitle = "淘微购 - 宝贝推广";
        
        render("taoweigou/itemlistnew.html", normalItemsList, commonInfo, isNormalMasonry, 
                isHotMasonry, isNoItem, pageTitle);
    }

    public static void detail(Long numIid, String search, Long catid) {
        search = decodeSearch(search);
        
        Long userId = Shopping.getVisitUserId();
        
        List<PopularizeItemInfo> hotItemList = findItemInfos(numIid, userId, catid, 
                search, PopularizedStatus.HotSale);
        
        Long count = PopularizedDao.countAllHot();
    	int start = (int) Math.floor(Math.random() * (count - 10));
    	List<Popularized> guesses = PopularizedDao.getRandomHotItems(start, 10);
        List<List<Popularized>> guessLists = new ArrayList<List<Popularized>>();
    	if(guesses.size() > 5) {
    		guessLists.add(guesses.subList(0, 5));
    		guessLists.add(guesses.subList(5, 10));
    	} else {
    		guessLists.add(guesses.subList(0, guesses.size()));
    	}
    	
    	Popularized item = ShoppingDao.findByNumIid(numIid, "", "", PopularizedStatus.Normal + PopularizedStatus.HotSale);
    	
    	boolean isShowSku = checkIsShowSku(item.getPrice(), item.getSkuMinPrice());
    	
    	List<String> wordList = RandomCatTopWordAction.doRandomWords(TopWordLimitNum);
    	WeiGouCommonInfo commonInfo = new WeiGouCommonInfo(search, hotItemList, 0L, wordList);
    	
    	String pageTitle = "淘微购 - " + item.getTitle();
    	
        render("taoweigou/itemdetailnew.html", isShowSku, item, commonInfo, guessLists, pageTitle);
    }

    public static void apply() {
        Long userId = Shopping.getVisitUserId();
        List<PopularizeItemInfo> hotItemList = findItemInfos(0L, userId, 0L, 
                "", PopularizedStatus.HotSale);
        
        WeiGouCommonInfo commonInfo = new WeiGouCommonInfo("", hotItemList, 0L, null);
        
        String pageTitle = "淘微购 - 商家报名 - 热门宝贝每日推荐";
        
        render("taoweigou/apply.html", commonInfo, pageTitle);
    }
    
    private static List<PopularizeItemInfo> findItemInfos(Long numIid, Long userId, 
            Long topCatId, String title, int status) {
        List<Popularized> itemList = RandomShareAction.randomWithUser(numIid, userId, topCatId, 
                title, status, ItemLimitNum);
        
        if (CommonUtils.isEmpty(itemList)) {
            return new ArrayList<PopularizeItemInfo>();
        }
        List<PopularizeItemInfo> itemInfoList = new ArrayList<PopularizeItemInfo>();
        
        for (Popularized item : itemList) {
            if (item == null) {
                continue;
            }
            itemInfoList.add(new PopularizeItemInfo(item, topCatId));
        }
        
        return itemInfoList;
    }
    
    
    private static boolean checkIsShowSku(double originPrice, double skuMinPrice) {
        if (skuMinPrice <= 0 || skuMinPrice >= originPrice) {
            return false;
        } else {
            return true;
        }
    }
    
    
    public static class PopularizeItemInfo {
        
        private String itemLink;
        private String imgPath;
        private double originPrice;
        private double skuMinPrice;
        private boolean isShowSku;
        private String title;
        
        public PopularizeItemInfo(Popularized item, Long topCatId) {
            //
            itemLink = "/item/" + item.getNumIid() + ".html";
            itemLink += "?ll=2185480046";
            
            /*
            if (topCatId != null && topCatId > 0L) {
                itemLink += "&catid=" + topCatId;
            }
            */
            
            imgPath = item.getPicPath() + "_190x190.jpg";
            originPrice = item.getPrice();
            skuMinPrice = item.getSkuMinPrice();
            isShowSku = checkIsShowSku(originPrice, skuMinPrice);
            
            title = item.getTitle();
        }

        public String getItemLink() {
            return itemLink;
        }

        public void setItemLink(String itemLink) {
            this.itemLink = itemLink;
        }

        public String getImgPath() {
            return imgPath;
        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
        }

        public double getOriginPrice() {
            return originPrice;
        }

        public void setOriginPrice(double originPrice) {
            this.originPrice = originPrice;
        }

        public double getSkuMinPrice() {
            return skuMinPrice;
        }

        public void setSkuMinPrice(double skuMinPrice) {
            this.skuMinPrice = skuMinPrice;
        }

        public boolean isShowSku() {
            return isShowSku;
        }

        public void setShowSku(boolean isShowSku) {
            this.isShowSku = isShowSku;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        
        
        
    }
    
    public static class WeiGouCommonInfo {
        private boolean isShowSearch;
        private String search;
        private List<PopularizeItemInfo> hotItemList;
        
        private List<WeiGrouSearchInfo> searchInfoList;
        
        public WeiGouCommonInfo(String search, List<PopularizeItemInfo> hotItemList, 
                Long catId, List<String> wordList) {
            super();
            if (StringUtils.isEmpty(search)) {
                isShowSearch = false;
            } else {
                isShowSearch = true;
            }
            this.search = search;
            
            if (CommonUtils.isEmpty(hotItemList)) {
                hotItemList = new ArrayList<PopularizeItemInfo>();
            }
            this.hotItemList = hotItemList;
            
            if (CommonUtils.isEmpty(wordList)) {
                wordList = new ArrayList<String>();
            }
            
            this.searchInfoList = new ArrayList<WeiGrouSearchInfo>();
            
            for (String word : wordList) {
                if (StringUtils.isEmpty(word)) {
                    continue;
                }
                WeiGrouSearchInfo searchInfo = new WeiGrouSearchInfo(catId, word);
                this.searchInfoList.add(searchInfo);
            }
        }

        public boolean isShowSearch() {
            return isShowSearch;
        }

        public void setShowSearch(boolean isShowSearch) {
            this.isShowSearch = isShowSearch;
        }

        public String getSearch() {
            return search;
        }
        
        public void setSearch(String search) {
            this.search = search;
        }

        public List<PopularizeItemInfo> getHotItemList() {
            return hotItemList;
        }

        public void setHotItemList(List<PopularizeItemInfo> hotItemList) {
            this.hotItemList = hotItemList;
        }

        public List<WeiGrouSearchInfo> getSearchInfoList() {
            return searchInfoList;
        }

        public void setSearchInfoList(List<WeiGrouSearchInfo> searchInfoList) {
            this.searchInfoList = searchInfoList;
        }

        
    }
    
    public static class WeiGrouSearchInfo {
        
        private String word;
        private String searchUrl;
        
        public WeiGrouSearchInfo(Long catId, String word) {
            super();
            this.word = word;
            if (catId == null || catId <= 0L) {
                catId = 0L;
            }
            try {
                String encodeWord = URLEncoder.encode(word, "utf-8");
                searchUrl = "/cat" + catId + "/s/" + encodeWord + ".html";
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                searchUrl = "";
            }
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getSearchUrl() {
            return searchUrl;
        }

        public void setSearchUrl(String searchUrl) {
            this.searchUrl = searchUrl;
        }
        
        
        
    }
    
}
