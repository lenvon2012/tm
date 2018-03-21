package result.pojo.rpt;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.rpt.response.RptCustBase;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;

import com.ciaosir.client.utils.NumberUtil;

@JsonAutoDetect
@Entity(name = RptCustResultPojo.TABLE_NAME)
public class RptCustResultPojo extends GenericModel implements IRptResultPojo<RptCustResultPojo> {

    private static final Logger log = LoggerFactory.getLogger(RptCustResultPojo.class);

    public static final String TAG = "RptCustResultPojo";

    public static final String TABLE_NAME = "RptCustResultPojo_";

    public static RptCustResultPojo EMPTY = new RptCustResultPojo();

    public RptCustResultPojo() {
    }

    @JsonProperty(value = "userNick")
    @Id
    public String userNick;

    @JsonProperty(value = "day")
    public String day;

    /**
     * 总花费
     */
    @JsonProperty(value = "cost")
    public double cost;

    /**
     * 总收入
     */
    @JsonProperty(value = "payamount")
    public double payamount;

    /**
     * 总展现量
     */
    @JsonProperty(value = "impressions")
    public long impressions;

    /**
     * 总点击数
     */
    @JsonProperty(value = "aclick")
    public long aclick;

    /**
     * 总成交笔数
     */
    @JsonProperty(value = "paycount")
    public long paycount;

    /**
     * 点击率
     */
    @JsonProperty(value = "ctr")
    public double ctr;

    /**
     * 点击转化率 click conversion rate
     */
    @JsonProperty(value = "ccr")
    public double ccr;

    /**
     * 平均点击花费
     */
    @JsonProperty(value = "cpc")
    public double cpc;

    /**
     * 店铺收藏次数
     */
    @JsonProperty(value = "favshopcount")
    public int favshopcount;

    /**
     * 宝贝收藏次数
     */
    @JsonProperty(value = "favitemcount")
    public int favitemcount;

    /**
     * roi
     */
    @JsonProperty(value = "roi")
    public double roi;

    public RptCustResultPojo(RptCustBase base) {

        this.userNick = base.getNick();
        this.day = new SimpleDateFormat("yyyy-MM-dd").format(new Date(base.getDateTime()));    

        this.cost = base.getCost() == null ? 0 : ((double) base.getCost()) / 100;
        this.impressions = base.getImpressions() == null ? 0 : base.getImpressions();
        this.aclick = base.getClick() == null ? 0 : base.getClick();

        this.payamount += (base.getDirectpay() == null ? 0 : base.getDirectpay());
        this.payamount += (base.getIndirectpay() == null ? 0 : base.getIndirectpay());

        this.paycount += (base.getDirectpaycount() == null ? 0 : base.getDirectpaycount());
        this.paycount += (base.getIndirectpaycount() == null ? 0 : base.getIndirectpaycount());

        this.favitemcount = (base.getFavitemcount() == null ? 0 : base.getFavitemcount());
        this.favshopcount = (base.getFavshopcount() == null ? 0 : base.getFavshopcount());

        this.payamount /= 100;

        calIndex();
    }

    public void calIndex() {
        this.ctr = impressions > 0 ? (double) aclick / impressions : 0;
        this.cpc = aclick > 0 ? (double) cost / aclick : 0;
        this.ccr = aclick > 0 ? (double) paycount / aclick : 0;
        this.roi = cost > 0 ? (double) payamount / cost : 0;

        this.ctr = NumberUtil.double4Formatter(this.ctr);
        this.cpc = NumberUtil.double4Formatter(this.cpc);
        this.ccr = NumberUtil.double4Formatter(this.ccr);
        this.roi = NumberUtil.doubleFormatter(this.roi);

    }

    public RptCustResultPojo add(RptCustResultPojo pojo) {
        this.cost += pojo.getCost();
        this.impressions += pojo.getImpressions();

        this.aclick += pojo.getAclick();

        this.payamount += pojo.getPayamount();
        this.paycount += pojo.getPaycount();

        this.favitemcount += pojo.getFavitemcount();
        this.favshopcount += pojo.getFavshopcount();

        this.cost = NumberUtil.doubleFormatter(cost);
        this.payamount = NumberUtil.doubleFormatter(payamount);

        calIndex();
        return this;
    }

    public RptCustResultPojo divide(int divider) {

        this.cost /= divider;
        this.impressions /= divider;
        this.aclick /= divider;
        this.payamount /= divider;
        this.paycount /= divider;
        this.favitemcount /= divider;
        this.favshopcount /= divider;

        this.cost = NumberUtil.doubleFormatter(cost);
        this.payamount = NumberUtil.doubleFormatter(payamount);

        this.calIndex();

        return this;
    }

    public RptCustResultPojo(Long dateTime) {
        this(new SimpleDateFormat("yyyy-MM-dd").format(new Date(dateTime)));

    }

    public RptCustResultPojo(String day) {
        this.day = day;

        this.cost = 0.0d;
        this.payamount = 0.0d;
        this.impressions = 0;
        this.aclick = 0;
        this.paycount = 0;
        this.ctr = 0.0d;
        this.ccr = 0.0d;
        this.cpc = 0.0d;
        this.favitemcount = 0;
        this.favshopcount = 0;
        this.roi = 0.0;

    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getPayamount() {
        return payamount;
    }

    public void setPayamount(double payamount) {
        this.payamount = payamount;
    }

    public long getImpressions() {
        return impressions;
    }

    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }

    public long getAclick() {
        return aclick;
    }

    public void setAclick(long aclick) {
        this.aclick = aclick;
    }

    public long getPaycount() {
        return paycount;
    }

    public void setPaycount(long paycount) {
        this.paycount = paycount;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public double getCcr() {
        return ccr;
    }

    public void setCcr(double ccr) {
        this.ccr = ccr;
    }

    public double getCpc() {
        return cpc;
    }

    public void setCpc(double cpc) {
        this.cpc = cpc;
    }

    public int getFavshopcount() {
        return favshopcount;
    }

    public void setFavshopcount(int favshopcount) {
        this.favshopcount = favshopcount;
    }

    public int getFavitemcount() {
        return favitemcount;
    }

    public void setFavitemcount(int favitemcount) {
        this.favitemcount = favitemcount;
    }

    public double getRoi() {
        return roi;
    }

    public void setRoi(double roi) {
        this.roi = roi;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    @Override
    public boolean jdbcSave(Long userId) {
        // TODO Auto-generated method stub
        return false;
    }
}
