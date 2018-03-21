
package models.industry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import result.TMResult;
import titleDiag.DiagResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.word.PaodingSpliter.SplitMode;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs;

@Entity(name = TopShopItem.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "parentBaseId", "cat", "cid", "cidInt", "level", "url", "catText", "type", "tableHashKey", "persistent",
        "entityId", "idName", "totalNum"
})
@JsonAutoDetect
public class TopShopItem extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(TopShopItem.class);

    public static final String TAG = "TopShopItem";

    public static final String TABLE_NAME = "topshopitems";

    @Id
    @JsonProperty
    Long numIid;

    @Index(name = "userId")
    @JsonProperty
    Long userId;

    @JsonProperty
    int sale;

    @JsonProperty
    String picPath;

    @Index(name = "title")
    @JsonProperty
    String title;

    @Transient
    @JsonProperty
    List<String> splits;

    public TopShopItem(Long numIid, Long userId, int sale, String picPath, String title) {

//    public TopShopItem(Long numIid, Long userId, int sale, String picPath, String title) {
        super();
        this.numIid = numIid;
        this.userId = userId;
        this.sale = sale;
        this.picPath = picPath;
        this.title = title;
    }

    public static TopShopItem ensure(ShopInfo sInfo, ItemThumb thumb) {
        TopShopItem shopItem = TopShopItem.findById(thumb.getId());
        if (shopItem == null) {
            shopItem = new TopShopItem(thumb.getId());
        }
        shopItem.updateWrapper(thumb);
        shopItem.save();
        return shopItem;
    }

    private void updateWrapper(ItemThumb thumb) {
        this.userId = thumb.getSellerId();
        this.sale = thumb.getTradeNum();
        this.picPath = thumb.getPicPath();
        this.title = thumb.getFullTitle();
    }

    public TopShopItem(Long numIid) {
        super();
        this.numIid = numIid;
    }

    public static List<TopShopItem> findByUser(long userId) {
        List<TopShopItem> shopItems = TopShopItem.find("userId = ?", userId).fetch();
        return shopItems;
    }

    public static TMResult search(String key, PageOffset po, boolean ensureSplits) {
        List<String> splits;
        try {
            splits = new WidAPIs.SplitAPI(key, SplitMode.BASE, false).execute();
            StringBuilder sb = new StringBuilder();
            sb.append(" 1 = 1 ");
            if (!CommonUtils.isEmpty(splits)) {
                for (String string : splits) {
                    sb.append(" and title like '%");
                    sb.append(CommonUtils.escapeSQL(string));
                    sb.append("%'");
                }
            }

            String whereQuery = sb.toString();
            List<TopShopItem> items = TopShopItem.find(whereQuery).from(po.getOffset()).fetch(po.getPs());
            if (ensureSplits) {
                ensureSplits(items);
            }

            log.error("where query :" + sb.toString());
            int count = (int) TopShopItem.count(whereQuery);
            TMResult res = new TMResult(items, count, po);
            return res;
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        return TMResult.failMsg("亲，系统出了一点小问题，给您造成的不便，我们深感歉意，请您稍后片刻，当然也可以联系客服哟");
    }

    private static void ensureSplits(List<TopShopItem> items) {
        List<FutureTask<DiagResult>> list = new ArrayList<FutureTask<DiagResult>>();

        for (final TopShopItem topShopItem : items) {
            if (topShopItem == null) {
                continue;
            }

            list.add(TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    String title = topShopItem.getTitle();
                    if (StringUtils.isEmpty(title)) {
                        return null;
                    }

                    List<String> res = new WidAPIs.SplitAPI(title, SplitMode.BASE, true).execute();
                    topShopItem.setSplits(res);
                    return null;
                }
            }));
        }

        CommonUtils.sleepQuietly(100L);

        for (FutureTask<DiagResult> task : list) {
            try {
                task.get();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public String toString() {
        return "TopShopItem [numIid=" + numIid + ", userId=" + userId + ", sale=" + sale + ", picPath=" + picPath
                + ", title=" + title + "]";
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getSale() {
        return sale;
    }

    public void setSale(int sale) {
        this.sale = sale;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getSplits() {
        return splits;
    }

    public void setSplits(List<String> splits) {
        this.splits = splits;
    }

}
