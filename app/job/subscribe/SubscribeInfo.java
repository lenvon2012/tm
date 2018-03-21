
package job.subscribe;

import com.ciaosir.client.utils.NumberUtil;

public class SubscribeInfo {

    public long hourOrDay;

    /**
     * totalPayFee/totalNum;
     */
    public double apru;

    public double payRate;

    public int totalNum;

    public int payNum;

    public double totalPayFee;

    public double perCustomerPay;

    public static String FIELDS = "时间,apru,付费率,总订购人数,付费人数,订购金额,客单价";

    public SubscribeInfo(long hourOrDay) {
        this.hourOrDay = hourOrDay;
        this.apru = 0.0;
        this.payRate = 0.0;
        this.totalNum = 0;
        this.payNum = 0;
        this.totalPayFee = 0.0d;
    }

    public double getApru() {
        return NumberUtil.doubleFormatter(totalNum > 0 ? totalPayFee / totalNum : 0);

    }

    public double getPerCustomerPay() {
        return NumberUtil.doubleFormatter(payNum > 0 ? totalPayFee / payNum : 0);

    }

    public void setApru(double apru) {
        this.apru = apru;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getPayNum() {
        return payNum;
    }

    public void setPayNum(int payNum) {
        this.payNum = payNum;
    }

    public double getTotalPayFee() {
        return totalPayFee;
    }

    public void setTotalPayFee(double totalPayFee) {
        this.totalPayFee = totalPayFee / 100;
    }

    public long getHourOrDay() {
        return hourOrDay;
    }

    public void setHourOrDay(long hourOrDay) {
        this.hourOrDay = hourOrDay;
    }

    public double getPayRate() {
        return this.totalNum > 0 ? (double) this.payNum / this.totalNum : 0.0;
    }

    public void setPayRate(double payRate) {
        this.payRate = payRate;
    }

    @Override
    public String toString() {
        return "SubscribeInfo [hourOrDay=" + hourOrDay + ", apru=" + apru + ", payRate=" + payRate + ", totalNum="
                + totalNum + ", payNum=" + payNum + ", totalPayFee=" + totalPayFee + ", perCustomerPay="
                + perCustomerPay + "]";
    }

}
