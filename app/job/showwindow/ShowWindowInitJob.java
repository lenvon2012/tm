
package job.showwindow;

import java.io.Serializable;

import jdp.ApiJdpAdapter;
import models.showwindow.ShowwindowTmallTotalNumFixedNum;
import models.user.User;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.OperateItemApi;
import bustbapi.OperateItemApi.ItemOnShowcaseCount;
import bustbapi.ShowWindowApi.GetShopShowcase;

public class ShowWindowInitJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ShowWindowInitJob.class);

    public static final String TAG = "ShowWindowInitJob";

    @JsonAutoDetect
    public static class WindowItemInfo extends ShowCaseInfo {

        @Override
        public String toString() {
            return "WindowItemInfo [onSaleCount=" + onSaleCount + ", inventoryCount=" + inventoryCount
                    + ", onShowItemCount=" + onShowItemCount + ", totalWindowCount=" + totalWindowCount
                    + ", remainWindowCount=" + remainWindowCount + "]";
        }

        @JsonProperty
        int onSaleCount;

        @JsonProperty
        int inventoryCount;

        @JsonProperty
        int mustCount;

        @JsonProperty
        int excludeCount;

        public WindowItemInfo(User user, ShowCaseInfo caseInfo) {
            super();

            this.onShowItemCount = caseInfo.onShowItemCount;
            this.totalWindowCount = caseInfo.totalWindowCount;
            this.remainWindowCount = caseInfo.remainWindowCount;

            Long saleCount = ApiJdpAdapter.get(user).onSaleItemNum(user);
            Long inventoryCountLong = ApiJdpAdapter.get(user).inventoryItemNum(user);
            onSaleCount = saleCount == null ? 0 : saleCount.intValue();
            inventoryCount = inventoryCountLong == null ? 0 : inventoryCountLong.intValue();
        }

        public WindowItemInfo(User user, ShowCaseInfo caseInfo, int onSaleCount, int inventoryCount) {
            super();
            this.onShowItemCount = caseInfo.onShowItemCount;
            this.totalWindowCount = caseInfo.totalWindowCount;
            this.remainWindowCount = caseInfo.remainWindowCount;

            this.onSaleCount = onSaleCount;
            this.inventoryCount = inventoryCount;
        }

        public WindowItemInfo(User user) {
            super(user);
            Long saleCount = ApiJdpAdapter.get(user).onSaleItemNum(user);
            Long inventoryCountLong = ApiJdpAdapter.get(user).inventoryItemNum(user);
            onSaleCount = saleCount == null ? 0 : saleCount.intValue();
            inventoryCount = inventoryCountLong == null ? 0 : inventoryCountLong.intValue();
        }

        public int getOnSaleCount() {
            return onSaleCount;
        }

        public void setOnSaleCount(int onSaleCount) {
            this.onSaleCount = onSaleCount;
        }

        public int getInventoryCount() {
            return inventoryCount;
        }

        public void setInventoryCount(int inventoryCount) {
            this.inventoryCount = inventoryCount;
        }

        public int getMustCount() {
            return mustCount;
        }

        public void setMustCount(int mustCount) {
            this.mustCount = mustCount;
        }

        public int getExcludeCount() {
            return excludeCount;
        }

        public void setExcludeCount(int excludeCount) {
            this.excludeCount = excludeCount;
        }

    }

    @JsonAutoDetect
    public static class ShowCaseInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        @JsonProperty
        public int onShowItemCount;

        @JsonProperty
        public int totalWindowCount;

        @JsonProperty
        public int remainWindowCount;

        public ShowCaseInfo(int onShowItemCount, int totalWindowCount, int remainWindowCount) {
            super();
            this.onShowItemCount = onShowItemCount;
            this.totalWindowCount = totalWindowCount;
            this.remainWindowCount = remainWindowCount;
            if (this.remainWindowCount < 0) {
                this.remainWindowCount = 0;
            }
        }

        public int getOnShowItemCount() {
            return onShowItemCount;
        }

        public void setOnShowItemCount(int onShowItemCount) {
            this.onShowItemCount = onShowItemCount;
        }

        public int getTotalWindowCount() {
            return totalWindowCount;
        }

        public void setTotalWindowCount(int totalWindowCount) {
            this.totalWindowCount = totalWindowCount;
        }

        public int getRemainWindowCount() {
            return remainWindowCount;
        }

        public void setRemainWindowCount(int remainWindowCount) {
            this.remainWindowCount = remainWindowCount;
        }

        public ShowCaseInfo() {

        }

        /**
         * 天猫规则:
         * 月销售金额（M）
        橱窗推荐位（个）
        M< 1.5万
        60
        1.5万=<M<10
        100
        10万=<M<30万
        200
        30万=<M<100万
        300
        100万=<M<300万
        500
        300万=<M<500万
        1000
        M>= 500万
        2000
         * @param user
         */
        public ShowCaseInfo(User user) {

            ShowCaseInfo info = null;
            if (user.isTmall()) {
                Integer onShowCount = new ItemOnShowcaseCount(user).call();
                Integer maxNum = ShowwindowTmallTotalNumFixedNum.findOrCreate(user, onShowCount);
//                log.error(" back on show count for user:" + user + " with on show count :" + onShowCount);

                this.onShowItemCount = onShowCount;
                this.totalWindowCount = maxNum;
                this.remainWindowCount = this.totalWindowCount - this.onShowItemCount;

            } else {
                info = new GetShopShowcase(user.getSessionKey()).call();
                if (info == null) {
                    info = new GetShopShowcase(user.getSessionKey()).call();
                }

                if (info == null) {
                    Integer onShowCount = new ItemOnShowcaseCount(user).call();
                    this.remainWindowCount = 0;
                    this.onShowItemCount = onShowCount == null ? 0 : onShowCount.intValue();
                    this.totalWindowCount = this.onShowItemCount + this.remainWindowCount;
                } else {
                    this.onShowItemCount = info.onShowItemCount;
                    this.totalWindowCount = info.totalWindowCount;
                    this.remainWindowCount = info.remainWindowCount;
                }
                
            }
            Integer manualNum = ShowwindowTmallTotalNumFixedNum.findOrCreate(user, this.onShowItemCount);
                if(manualNum > 0) {
                	this.totalWindowCount = manualNum;
                }
           
            if (totalWindowCount > 0) {
                OperateItemApi.setUserTotalNum(user, totalWindowCount);
            }

        }

        @Override
        public String toString() {
            return "ShowCaseInfo [onShowItemCount=" + onShowItemCount + ", totalWindowCount=" + totalWindowCount
                    + ", remainWindowCount=" + remainWindowCount + "]";
        }

        public static ShowCaseInfo build(User user) {
            return ApiJdpAdapter.get(user).buildShowCase(user);
        }

    }

}
