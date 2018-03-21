
package models.item;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.persistence.Entity;
import javax.persistence.Id;

import jdp.ApiJdpAdapter;
import models.mysql.fengxiao.FenxiaoProvider;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import play.jobs.Job;
import autotitle.ItemPropAction;

import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.api.ShopSearchAPI;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

@Entity(name = FenxiaoItem.TABLE_NAME)
public class FenxiaoItem extends GenericModel {

    public static final String TABLE_NAME = "fenxiao_item";

    private static final Logger log = LoggerFactory.getLogger(FenxiaoItem.class);

    public static final String TAG = "FenxiaoItem";

    @Id
    Long numIid;

    String title;

    String picpath;

    String props;

    @Index(name = "userId")
    Long userId;

    @Index(name = "userNick")
    String userNick;

    @Index(name = "serialNum")
    String serialNum;

    public FenxiaoItem(FenxiaoProvider provider, Item item) {
        this.numIid = item.getNumIid();
        this.title = item.getTitle();
        this.picpath = item.getPicUrl();
        this.userId = provider.getUserId();
        this.userNick = provider.getUserNick();
        this.serialNum = ItemPropAction.fetchSerialNum(item);
        log.error("serial num : " + serialNum);
    }

    public static void ensure(String nick) {
        NumberUtil.first(ShopSearchAPI.getShopInfo(nick, new PYSpiderOption(true, 2)));
    }

    public static void ensure(final FenxiaoProvider provider, List<ItemThumb> list) {
        new FenxiaoAsyncEnsuerJob(provider, list).now();
    }

    public static class FenxiaoAsyncEnsuerJob extends Job {
        FenxiaoProvider provider = null;

        List<ItemThumb> list = null;

        public FenxiaoAsyncEnsuerJob(FenxiaoProvider provider, List<ItemThumb> list) {
            this.provider = provider;
            this.list = list;
        }

        public void doJob() {
            PYFutureTaskPool<FenxiaoItem> pool = new PYFutureTaskPool<FenxiaoItem>(16);
            List<FutureTask<FenxiaoItem>> tasks = new ArrayList<FutureTask<FenxiaoItem>>();

            for (final ItemThumb thumb : list) {
                tasks.add(pool.submit(new Callable<FenxiaoItem>() {
                    @Override
                    public FenxiaoItem call() throws Exception {
                        /**
                         * Direct get by single item..
                         */
                        Item item = ApiJdpAdapter.tryFetchSingleItem(null, thumb.getId());
//                        log.info("fnexiao item :" + new Gson().toJson(item));
                        return new FenxiaoItem(provider, item);
                    }
                }));
            }
            List<FenxiaoItem> items = new ArrayList<FenxiaoItem>();

            for (FutureTask<FenxiaoItem> task : tasks) {
                try {
                    items.add(task.get());
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }

            pool.shutdown();
            writeitems(items);
        }
    }

    private static void writeitems(List<FenxiaoItem> items) {
        for (FenxiaoItem fenxiaoItem : items) {
            Long id = fenxiaoItem.getNumIid();
            FenxiaoItem exist = FenxiaoItem.findById(id);
            if (exist != null) {
                exist.delete();
            }

            fenxiaoItem.save();
        }
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPicpath() {
        return picpath;
    }

    public void setPicpath(String picpath) {
        this.picpath = picpath;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
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

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public static String tryFindItem(String string, String fetchSerialNum) {

        log.info(format("tryFindItem:string, fetchSerialNum".replaceAll(", ", "=%s, ") + "=%s", string, fetchSerialNum));

        if (StringUtils.length(string) < 3) {
            return null;
        }
        List<FenxiaoItem> fetch = FenxiaoItem.find("serialNum = ? and userNick in (" + string + ") ", fetchSerialNum)
                .fetch();

        FenxiaoItem item = NumberUtil.first(fetch);
        if (item == null) {
            return null;
        }
        return item.getTitle();
    }
}
