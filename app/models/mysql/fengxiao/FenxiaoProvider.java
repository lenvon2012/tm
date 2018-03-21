
package models.mysql.fengxiao;

import static java.lang.String.format;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.item.FenxiaoItem;
import models.user.User;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import bustbapi.SellerAPI;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.api.ShopSearchAPI;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.NumberUtil;

@Entity(name = FenxiaoProvider.TABLE_NAME)
public class FenxiaoProvider extends GenericModel {

    public static final String TABLE_NAME = "fengxiao_provider";

    private static final Logger log = LoggerFactory.getLogger(FenxiaoProvider.class);

    public static final String TAG = "FengxiaoProvider";

    @Id
    Long userId;

    @Index(name = "userNick")
    String userNick;

    int type;

    int level;

    public FenxiaoProvider(ShopInfo shop) {
        this.level = shop.getLevel();
        this.userId = shop.getUserId();
        this.userNick = shop.getShopnick();
        if (shop.isBShop()) {
            this.type |= User.Type.IS_TMALL;
        }

    }

    public static FenxiaoProvider ensure(String nick) {

        log.info(format("ensure:nick".replaceAll(", ", "=%s, ") + "=%s", nick));

        List<ShopInfo> shops = ShopSearchAPI.getShopInfo(nick, new PYSpiderOption(false, 2));
        log.info("find shops :" + shops);
        if (CommonUtils.isEmpty(shops)) {
            return null;
        }

        ShopInfo shop = null;
        for (ShopInfo shopInfo : shops) {
            if (nick.equals(shopInfo.getShopnick())) {
                shop = shopInfo;
            }
        }
        log.info("[nick shop :]" + shop);
        if (shop == null) {
            shop = NumberUtil.first(shops);
        }

        log.info("[do for shop :]" + shop);
        return search(shop);
    }

    public static FenxiaoProvider search(ShopInfo shop) {
        if (shop == null) {
            return null;
        }
        long userId = shop.getUserId();
        FenxiaoProvider provider = FenxiaoProvider.findById(userId);
        if (provider != null) {
//            return provider;
            provider.delete();
        }

        provider = new FenxiaoProvider(shop);
        provider.save();

        // start to sync all items...
        try {
            List<ItemThumb> list = SellerAPI.getItemArray(provider.userNick, null, new PYSpiderOption(null, true, 2,
                    false, 2000));
            log.info("[fetch list num:]" + (list == null ? 0 : list.size()));
            FenxiaoItem.ensure(provider, list);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return provider;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "FengxiaoProvider [userId=" + userId + ", userNick=" + userNick + ", type=" + type + ", level=" + level
                + "]";
    }

}
