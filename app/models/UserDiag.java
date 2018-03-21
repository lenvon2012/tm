
package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import models.item.ItemPlay.ItemPageBean;
import models.user.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Job;
import titleDiag.DiagResult;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import actions.DelistAction;
import actions.DelistAction.DelistDiag;
import bustbapi.ItemPageApi;
import bustbapi.TMTradeApi;
import bustbapi.TMTradeApi.ShopBaseTradeInfo;
import bustbapi.TMTradeApi.TradeNumUpdate;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.ShopSearchAPI;
import com.ciaosir.client.item.IShopInfoBase;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

import configs.TMConfigs;
import dao.UserDao;
import dao.item.ItemDao;

@Entity(name = UserDiag.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "tableHashKey", "persistent", "entityId", "idName", "totalNum", "idColumn", "tableName", "dataSrc",
        "hashColumnName", "hashed"
})
public class UserDiag implements PolicySQLGenerator {

    @NoTransaction
    public static class ComputeUserDelistTime extends Job<UserDiag> {

        User user = null;

        public ComputeUserDelistTime(User user) {
            super();
            this.user = user;
        }

        public UserDiag doJobWithResult() {
            Set<Long> tsSet = ItemDao.findDelistTimeArrayWithUser(user.getId());
            int size = tsSet.size();
            long[] tsArr = new long[size];
            int count = 0;
            for (Long ts : tsSet) {
                tsArr[count++] = ts;
            }

            DelistDiag res = DelistAction.compute(tsArr);
            UserDiag uDiag = UserDiag.findOrCreate(user);
            uDiag.updateWrapper(res);

            Long call = new TradeNumUpdate(user).call();
            ShowCaseInfo info = ShowCaseInfo.build(user);
            uDiag.setTradeCount(call == null ? 0 : call.intValue());
            uDiag.updateWrapper(info);
            uDiag.rawUpdate();
            return uDiag;
        }

        public void doJob() {
            doJobWithResult();
        }
    }

    @NoTransaction
    public static class ComputeUserWindowInfo extends Job {
        User user = null;

        public ComputeUserWindowInfo(User user) {
            super();
            this.user = user;
        }

        public void doJob() {
            UserDiag diag = UserDiag.findOrCreate(user);
            ShowCaseInfo info = ShowCaseInfo.build(user);
            diag.updateWrapper(info);
            diag.rawUpdate();
            if (info.getRemainWindowCount() == 0) {
//                log.info("user over:" + user);
            } else {
                log.error("After recommend :" + info + "for user:" + user);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(UserDiag.class);

    public static final String TAG = "UserDiag";

    public static final String TABLE_NAME = "user_diag";

    private static final int MAX_BURST_ITEM_NUM = 3;

    private static final int MAX_CHECK_ITEM_NUM = 8;

    @Transient
    public static UserDiag instance = new UserDiag();

    public UserDiag() {
        super();
        this.updateTs = System.currentTimeMillis();
    }

    public UserDiag(Long id) {
        this();
        this.id = id;
    }

    public UserDiag(Long id, int variance, int inBadTimeCount, String weekDistributed, int titleScore,
            int remainWindowCount, int windowUsage, long updateTs) {
        this();
        this.id = id;
        this.variance = variance;
        this.inBadTimeCount = inBadTimeCount;
        this.weekDistributed = weekDistributed;
        this.titleScore = titleScore;
        this.remainWindowCount = remainWindowCount;
        this.windowUsage = windowUsage;
        this.updateTs = updateTs;
    }

    @Id
    @PolicySQLGenerator.CodeNoUpdate
    Long id;

    /**
     * 按周为单位的分布方差
     */
    int variance;

    /**
     * 没有在高流量时间下家的宝贝数
     */
    int inBadTimeCount;

    /**
     * 一周中每天的下架宝贝数量   1,1,1,1,1,1,1
     */
    String weekDistributed;

    /**
     * 标题总得分
     * TODO 尚未加入
     */
    int titleScore;

    /**
     * 剩余橱窗数
     */
    int remainWindowCount;

    /**
     * 橱窗利用率 * 100
     */
    int windowUsage;

    long updateTs;

    /**
     * 最近一周订单量
     */
    int tradeCount;

    /**
     * 保留
     */
    int reverse;

    /**
     * 急需优化的标题总数
     */
    int badTitleCount = 0;

    /**
     * 爆款数
     */
    int goodItemCount = 0;

    /**
     * 潜在爆款数
     */
    int potentialGoodItemCount = 0;

    /**
     * 转化率
     */
    int conversionRate = 0;

    /**
     * 收藏率
     */
    int favRate = 0;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
        UserDiag exist = findByUserId(this.getId());
        if (exist == null) {
            this.rawInsert();
        } else {
            this.rawUpdate();
        }
        return true;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Override
    public String toString() {
        return "UserDiag [id=" + id + ", variance=" + variance + ", inBadTimeCount=" + inBadTimeCount
                + ", weekDistributed=" + weekDistributed + ", titleScore=" + titleScore + ", remainWindowCount="
                + remainWindowCount + ", windowUsage=" + windowUsage + ", updateTs=" + updateTs + ", tradeCount="
                + tradeCount + ", reverse=" + reverse + ", badTitleCount=" + badTitleCount + ", goodItemCount="
                + goodItemCount + ", potentialGoodItemCount=" + potentialGoodItemCount + ", conversionRate="
                + conversionRate + ", favRate=" + favRate + "]";
    }

    public UserDiag(User user) {
        this();
        this.id = user.getId();
    }

    public static UserDiag findOrCreate(User user) {
        UserDiag userDiag = findByUserId(user.getId());
        if (userDiag != null) {
            return userDiag;
        }
        userDiag = new UserDiag(user);
        userDiag.jdbcSave();
        return userDiag;
    }

    private void updateWrapper(ShowCaseInfo info) {
        if (info == null) {
            log.warn("no show case info");
            return;
        }

        this.remainWindowCount = info.getRemainWindowCount();
        this.windowUsage = info.getTotalWindowCount() == 0 ? 100 : info.getOnShowItemCount() * 100
                / info.getTotalWindowCount();
    }

    public int getVariance() {
        return variance;
    }

    public void setVariance(int variance) {
        this.variance = variance;
    }

    public int getInBadTimeCount() {
        return inBadTimeCount;
    }

    public void setInBadTimeCount(int inBadTimeCount) {
        this.inBadTimeCount = inBadTimeCount;
    }

    public String getWeekDistributed() {
        return weekDistributed;
    }

    public void setWeekDistributed(String weekDistributed) {
        this.weekDistributed = weekDistributed;
    }

    public int getTitleScore() {
        return titleScore;
    }

    public void setTitleScore(int titleScore) {
        this.titleScore = titleScore;
    }

    public int getRemainWindowCount() {
        return remainWindowCount;
    }

    public void setRemainWindowCount(int remainWindowCount) {
        this.remainWindowCount = remainWindowCount;
    }

    public int getWindowUsage() {
        return windowUsage;
    }

    public void setWindowUsage(int windowUsage) {
        this.windowUsage = windowUsage;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }

    public int getReverse() {
        return reverse;
    }

    public void setReverse(int reverse) {
        this.reverse = reverse;
    }

    public int getBadTitleCount() {
        return badTitleCount;
    }

    public void setBadTitleCount(int badTitleCount) {
        this.badTitleCount = badTitleCount;
    }

    public int getGoodItemCount() {
        return goodItemCount;
    }

    public void setGoodItemCount(int goodItemCount) {
        this.goodItemCount = goodItemCount;
    }

    public int getPotentialGoodItemCount() {
        return potentialGoodItemCount;
    }

    public void setPotentialGoodItemCount(int potentialGoodItemCount) {
        this.potentialGoodItemCount = potentialGoodItemCount;
    }

    public int getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(int conversionRate) {
        this.conversionRate = conversionRate;
    }

    public int getFavRate() {
        return favRate;
    }

    public void setFavRate(int favRate) {
        this.favRate = favRate;
    }

    public List<ItemThumb> getBurstItems() {
        return burstItems;
    }

    public void setBurstItems(List<ItemThumb> burstItems) {
        this.burstItems = burstItems;
    }

    public List<PotentialItem> getPotentionItems() {
        return potentionItems;
    }

    public void setPotentionItems(List<PotentialItem> potentionItems) {
        this.potentionItems = potentionItems;
    }

    static String INSERT_SQL = "insert into `user_diag`(`id`,`variance`,`inBadTimeCount`,`weekDistributed`,`titleScore`,`remainWindowCount`,`windowUsage`,`updateTs`,`tradeCount`,`reverse`"
            + ",badTitleCount,goodItemCount,potentialGoodItemCount,conversionRate,favRate) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, false, DBBuilder.DataSrc.BASIC, INSERT_SQL, this.id, this.variance,
                this.inBadTimeCount, this.weekDistributed, this.titleScore, this.remainWindowCount, this.windowUsage,
                this.updateTs, this.tradeCount, this.reverse, this.badTitleCount, this.goodItemCount,
                this.potentialGoodItemCount, this.conversionRate, this.favRate);
        if (id >= 0L) {
            return true;
        } else {
            log.error("insert fails :" + this);
            return id >= 0;

        }
    }

    static String UPDATE_SQL = "update `user_diag` set  `variance` = ?, `inBadTimeCount` = ?, `weekDistributed` = ?, `titleScore` = ?, `remainWindowCount` = ?, `windowUsage` = ?, `updateTs` = ?, `tradeCount` = ?, `reverse` = ?, "
            + "`badTitleCount`=?,`goodItemCount`=?,`potentialGoodItemCount`=?,`conversionRate`=?,`favRate`=? where `id` = ?  ";

    public boolean rawUpdate() {
        this.updateTs = System.currentTimeMillis();
        long updateNum = JDBCBuilder.update(false, UPDATE_SQL, this.variance, this.inBadTimeCount,
                this.weekDistributed, this.titleScore, this.remainWindowCount, this.windowUsage, this.updateTs,
                this.tradeCount, this.reverse, this.badTitleCount, this.goodItemCount, this.potentialGoodItemCount,
                this.conversionRate, this.favRate, this.getId());

        if (updateNum > 0L) {
            return true;
        } else {
            log.error("update failed:" + this);
            return false;
        }
    }

    public static void build(User user, List<Item> items) {
        Long call = new TradeNumUpdate(user).call();
        DelistDiag compute = DelistAction.compute(items);
        ShowCaseInfo info = ShowCaseInfo.build(user);
        UserDiag diag = new UserDiag(user);
        diag.setTradeCount(call == null ? 0 : call.intValue());
        diag.updateWrapper(compute);
        diag.updateWrapper(info);
        diag.jdbcSave();
    }

    static String SELECTE_SQL = "select id,variance,inBadTimeCount,weekDistributed,titleScore,remainWindowCount,windowUsage,updateTs,tradeCount,reverse,"
            + "badTitleCount,goodItemCount,potentialGoodItemCount,conversionRate,favRate from user_diag where id  = ?";

    public static UserDiag findByUserId(Long id) {
        return new JDBCBuilder.JDBCExecutor<UserDiag>(SELECTE_SQL, id) {

            @Override
            public UserDiag doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new UserDiag(rs);
                }
                return null;
            }

        }.call();
    }

    public UserDiag(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.variance = rs.getInt(2);
        this.inBadTimeCount = rs.getInt(3);
        this.weekDistributed = rs.getString(4);
        this.titleScore = rs.getInt(5);
        this.remainWindowCount = rs.getInt(6);
        this.windowUsage = rs.getInt(7);
        this.updateTs = rs.getLong(8);
        this.tradeCount = rs.getInt(9);
        this.reverse = rs.getInt(10);

        // Added in the 11.12
        this.badTitleCount = rs.getInt(11);
        this.goodItemCount = rs.getInt(12);
        this.potentialGoodItemCount = rs.getInt(13);
        this.conversionRate = rs.getInt(14);
        this.favRate = rs.getInt(15);
    }

    private void updateWrapper(DelistDiag compute) {
        if (compute == null) {
            log.warn("null compute: ");
            return;
        }

        this.inBadTimeCount = compute.getInBadTimeCount();
        this.variance = compute.getVariance();

        String arr = ArrayUtils.toString(compute.getWeekDistributed());
        this.weekDistributed = arr.substring(1, arr.length() - 1);
    }

    public void buildReport(ShopBaseTradeInfo shopbase, List<ItemThumb> thumbs) {
        Long userId = this.id;
        try {
            User user = UserDao.findById(userId);
            if (user == null) {
                log.error(" what happens...........");
                return;
            }
            String nick = user.getUserNick();
            this.titleScore = (int) ItemDao.countTitleAvgScore(userId);
            this.badTitleCount = (int) ItemDao.countTitleScoreSmaller(userId, 75);
            IShopInfoBase shopInfo = ShopSearchAPI.getShopInfo(nick, 5000, 2);

//            log.warn("[base shop info]" + shopInfo);
//            Map<Long, Integer> numIidSale = new HashM
//            List<ItemThumb> thumbs = SellerAPI.getItemArray(nick, null, 20, null, true);
            if (shopbase == null) {
                shopbase = TMTradeApi.buildNumIidSaleMap(user, 30);
            }
            if (thumbs == null) {
                thumbs = shopbase.buildItemThumbs();
            }

//            List<ItemThumb> findExplosions = findExplostions(user, shopInfo, thumbs);
            findExplostions(user, shopInfo, thumbs);
            checkBusrtItems(user, shopInfo);
            ensureItemDisplay(user, this.burstItems);
//            ensureItemDisplay(this.potentionItems);
            buildBurstInfo();
            buildDiagScore();

        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private void ensureItemDisplay(User user, List<ItemThumb> burstItems) {
        Iterator<ItemThumb> it = burstItems.iterator();
        while (it.hasNext()) {
            ItemThumb next = it.next();
            if (next.getId() == null || next.getId() < 0L) {
                it.remove();
            }

            ItemThumb thumb = ItemDao.ensure(user, next);
            if (thumb == null) {
                it.remove();
            }
        }

    }

    private void buildDiagScore() {
        int diagScore = 0;
    }

    @Transient
    @JsonProperty
    List<ItemThumb> burstItems = ListUtils.EMPTY_LIST;

    @Transient
    @JsonProperty
    List<PotentialItem> potentionItems = ListUtils.EMPTY_LIST;

    @JsonAutoDetect
    public static class PotentialItem extends ItemThumb implements Comparable<PotentialItem> {
        @JsonProperty
        public int score = 0;

        public PotentialItem(User user, ItemThumb thumb) {
//            log.info("[thumb:]" + thumb);
            setId(thumb.getId());
            ItemDao.ensure(user, thumb);
            setFullTitle(thumb.getFullTitle());
            setTradeNum(thumb.getTradeNum());
            setPeriodSoldQuantity(thumb.getPeriodSoldQuantity());
            setPrice(thumb.getPrice());
            setSellerId(thumb.getSellerId());
            setPicPath(thumb.getPicPath());
        }

        public PotentialItem(int score) {
            super();
            this.score = score;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        @Override
        public int compareTo(PotentialItem o) {
            return this.score - o.score;
        }

        @Override
        public String toString() {
            return "PotentialItem [score=" + score + ", id=" + id + ", fullTitle=" + fullTitle + ", tradeNum="
                    + tradeNum + ", periodSoldQuantity=" + periodSoldQuantity + ", price=" + price + ", sellerId="
                    + sellerId + ", picPath=" + picPath + "]";
        }

    }

    private List<ItemThumb> findExplostions(User user, IShopInfoBase shopInfo, List<ItemThumb> thumbs) {

        int size = CollectionUtils.size(thumbs);

        Stack<ItemThumb> stack = new Stack<ItemThumb>();
        for (int i = size - 1; i >= 0; i--) {
            stack.push(thumbs.get(i));
        }

        this.burstItems = new ArrayList<ItemThumb>();
        this.potentionItems = new ArrayList<PotentialItem>();

        int count = 0;
        while (count < MAX_CHECK_ITEM_NUM) {
            if (stack.isEmpty()) {
                break;
            }
            ItemThumb pop = stack.pop();

            if (burstItems.size() < MAX_BURST_ITEM_NUM && pop.getTradeNum() > 5) {
                burstItems.add(pop);
                continue;
            }

            if (pop.getTradeNum() <= 0) {
                break;
            }

            this.potentionItems.add(new PotentialItem(user, pop));
        }

        return null;
    }

    private void buildBurstInfo() {
        this.goodItemCount = this.burstItems.size();
        this.potentialGoodItemCount = this.potentionItems.size();
    }

    private void checkBusrtItems(User user, final IShopInfoBase shopInfo) {
        log.info("[curent potential items:]" + this.potentialGoodItemCount);
        if (CommonUtils.isEmpty(this.potentionItems)) {
            return;
        }

        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();

        addPotentialItemTasks(shopInfo, tasks);
        buildConverionRate(user, shopInfo);
        waitAllTasks(tasks);

        Collections.sort(this.potentionItems);
//        log.info("[after sort : ]" + this.potentionItems);
        this.potentionItems = NumberUtil.first(this.potentionItems, 3);
    }

    private void genRandConversionRate() {
        this.conversionRate = (int) (id % 500L);
        if (this.conversionRate < 67) {
            this.conversionRate += 67;
        }
    }

    private void buildConverionRate(User user, IShopInfoBase shopInfo) {
        ItemThumb thumb = NumberUtil.first(this.burstItems);
        if (thumb == null) {
//            this.conversionRate = (int) (id % 500L);
            genRandConversionRate();
        } else {
            ItemPageBean bean = new ItemPageBean(thumb, user.getId());
            bean = new ItemPageApi(bean).call();
            log.info("[bean : ]" + bean);
            if (bean == null || bean.getPv() <= 0) {
                genRandConversionRate();
            } else {
                this.conversionRate = bean.getSale() * 10000 / bean.getPv();
            }
            if (this.conversionRate > 10000) {
                this.conversionRate = 10000;
            }
        }
        if (shopInfo != null) {
            this.tradeCount = shopInfo.getLatestTradeCount();
        }
    }

    private void waitAllTasks(List<FutureTask<DiagResult>> tasks) {
        for (FutureTask<DiagResult> futureTask : tasks) {
            try {
                futureTask.get();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void addPotentialItemTasks(final IShopInfoBase shopInfo, List<FutureTask<DiagResult>> tasks) {
        for (PotentialItem item : this.potentionItems) {
            tasks.add(TMConfigs.getDiagResultPool().submit(new GetPotenialItemCaller(shopInfo, item)));
        }
    }

    public class GetPotenialItemCaller implements Callable<DiagResult> {
        PotentialItem target;

        IShopInfoBase shopInfo;

        public GetPotenialItemCaller(IShopInfoBase shopInfo2, PotentialItem target) {
            super();
            this.target = target;
            this.shopInfo = shopInfo2;
        }

        @Override
        public DiagResult call() throws Exception {
            ItemPageBean bean = new ItemPageBean(target, target.getSellerId());
            bean = new ItemPageApi(bean).call();
            if (bean == null) {
                // You know it...
                target.setScore((int) (UserDiag.this.id % 100L));
                return null;
            }

            int score = 0;
            if (shopInfo == null) {
                score = bean.getShare() + 100;
            } else if (shopInfo.isBShop() || bean.getPv() <= 0) {
                score = bean.getShare() + 100;
            } else {
                score = bean.getSale() * 10000 / bean.getPv();
            }
            if (score > 10000) {
                score = 10000;
            }

            target.setScore(score);

            return null;
        }
    }
}
