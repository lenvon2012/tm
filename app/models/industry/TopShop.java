
package models.industry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import models.word.top.TopURLBase;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import bustbapi.ShopApi;

import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.item.ShopInfo;
import com.google.gson.Gson;
import com.taobao.api.domain.Shop;

@Entity(name = TopShop.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "parentBaseId", "cat", "cid", "cidInt", "level", "url", "catText", "type", "tableHashKey", "persistent",
        "entityId", "idName", "totalNum"
})
public class TopShop extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(TopShop.class);

    public static final String TAG = "TopShop";

    public static final String TABLE_NAME = "top_shops";

    @Id
    Long id;

    @Index(name = "nick")
    String nick;

    int level;

    int itemCount;

    /**
     * 月销量
     */
    int recentTradeCount;

    /**
     * 人气指数
     */
    int popularity;

    int quality;

    boolean isBType;

    @Column(columnDefinition = "varchar(127) default null")
    public String picPath;

    public TopShop(Long userId, String nick, int level, int recentTradeCount, int popularity, int quality,
            boolean isBType) {
        super();
        this.id = userId;
        this.nick = nick;
        this.level = level;
        this.recentTradeCount = recentTradeCount;
        this.popularity = popularity;
        this.quality = quality;
        this.isBType = isBType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRecentTradeCount() {
        return recentTradeCount;
    }

    public void setRecentTradeCount(int recentTradeCount) {
        this.recentTradeCount = recentTradeCount;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean isBType() {
        return isBType;
    }

    public void setBType(boolean isBType) {
        this.isBType = isBType;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public TopShop() {
        super();
    }

    public TopShop(ShopInfo shop) {
        super();
        this.id = shop.getUserId();
    }

    public static void ensureInfo(ShopInfo info) {
        TopShop shop = TopShop.findById(info.getUserId());
        if (shop == null) {
            shop = new TopShop(info);
        }

        shop.updateWrapper(info);
        shop.save();
    }

    private void updateWrapper(ShopInfo info) {
        this.nick = info.getShopnick();
        this.level = info.getLevel();
        this.itemCount = info.getItemCount();
        this.recentTradeCount = info.getLatestTradeCount();
        this.popularity = info.getRenqi();
        this.isBType = info.isBShop();
        this.quality = info.getQuality();
    }

    public static List<TopShop> findByCat(String shopcat) {
        List<TopShop> topShops = new ArrayList<TopShop>();
//    	TopURLBase base = TopURLBase.findbyCatAndCid(shopcat,"");
        TopURLBase base = TopURLBase.findByCat(shopcat);
        log.info("[base ]" + base);

        Long baseId = base.getId();
        List<TopUrlToShop> topUrlToShops = TopUrlToShop.find("topUrlId = ?", baseId).fetch();
        log.info("[shops :]" + topUrlToShops);
        for (TopUrlToShop topUrlToShop : topUrlToShops) {
            Long userId = topUrlToShop.userId;
            TopShop topShop = TopShop.find("id=?", userId).first();
            topShops.add(topShop);
        }

        return topShops;
    }

    public static void ensurePicpath() {
        TopShop shop = TopShop.find(" id > 0").first();
        if (shop == null) {
            return;
        }

        if (shop.getPicPath() != null) {
            return;
        }

        List<TopShop> shops = TopShop.findAll();

        Map<String, TopShop> idShops = new HashMap<String, TopShop>();
        PYFutureTaskPool<Shop> pool = new PYFutureTaskPool<Shop>(32);
        List<FutureTask<Shop>> list = new ArrayList<FutureTask<Shop>>();

        for (TopShop topShop : shops) {
            idShops.put(topShop.getNick(), topShop);
        }
        for (final TopShop tShop : shops) {
            list.add(pool.submit(new Callable<Shop>() {

                @Override
                public Shop call() throws Exception {
                    return new ShopApi.ShopGet(tShop.getNick()).call();
                }
            }));
        }

        for (FutureTask<Shop> futureTask : list) {
            try {
                Shop shop2 = futureTask.get();
                if (shop2 == null) {
                    continue;
                }
                log.info("[shop2 :]" + new Gson().toJson(shop2));
                TopShop topShop = idShops.get(shop2.getNick());
                topShop.setPicPath("http://img02.taobaocdn.com/imgextra" + shop2.getPicPath());
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        for (TopShop topShop : shops) {
            topShop.save();
        }
    }
}
