
package job.subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import tbapi.subcribe.VasOrderApi;
import utils.DateUtil;
import utils.ExcelUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.ApiException;
import com.taobao.api.domain.ArticleBizOrder;

import controllers.APIConfig;

public class SubscribeAction {

    private static final Logger log = LoggerFactory.getLogger(SubscribeAction.class);

    public static final String TAG = "SubscribeAction";

    public static File export(long day) {

        long start = DateUtil.formDailyTimestamp(day);
        long end = start + DateUtil.DAY_MILLIS;

        List<ArticleBizOrder> orderList = new ArrayList<ArticleBizOrder>();

        try {
            VasOrderApi.varOrderSearch(APIConfig.get().getSubCode(), start, end, orderList);

            if (CommonUtils.isEmpty(orderList)) {
                return null;
            }
            SubscribeInfo dayInfo = statSubscribe(day, orderList);
            log.error("day info :" + dayInfo);

            Map<Integer, List<ArticleBizOrder>> ordersGroupByHour = groupByHour(orderList);

            List<SubscribeInfo> hourInfoList = new ArrayList<SubscribeInfo>(25);
            for (int i = 0; i < 24; i++) {
                List<ArticleBizOrder> hourOrders = ordersGroupByHour.get(i);

                if (CommonUtils.isEmpty(hourOrders)) {
                    hourInfoList.add(new SubscribeInfo(i));
                } else {
                    hourInfoList.add(statSubscribe(i, hourOrders));
                }
            }

            return exportToExcel(day, dayInfo, hourInfoList);

        } catch (ApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public static File exportToExcel(long day, SubscribeInfo dayInfo, List<SubscribeInfo> hourInfoList) {

        String filePath = ExcelUtil.writeToExcel(Play.tmpDir, day, dayInfo, hourInfoList, SubscribeInfo.FIELDS);

        return new File(filePath);
    }

    public static Map<Integer, List<ArticleBizOrder>> groupByHour(List<ArticleBizOrder> orderList) {

        Map<Integer, List<ArticleBizOrder>> orderMap = new HashMap<Integer, List<ArticleBizOrder>>();

        for (ArticleBizOrder order : orderList) {
            int hour = DateUtil.getHourOfDay(order.getCreate().getTime());
            List<ArticleBizOrder> hourOrders = orderMap.get(hour);

            if (hourOrders == null) {
                hourOrders = new ArrayList<ArticleBizOrder>();
                orderMap.put(hour, hourOrders);
            }
            hourOrders.add(order);
        }

        return orderMap;
    }

    public static SubscribeInfo statSubscribe(long dayOrHour, List<ArticleBizOrder> orders) {

        SubscribeInfo info = new SubscribeInfo(dayOrHour);

        if (CommonUtils.isEmpty(orders)) {
            return info;
        }

        info.setTotalNum(orders.size());

        double totalPayFee = 0.0d;
        int payNum = 0;

        for (ArticleBizOrder order : orders) {
            double payFee = NumberUtil.parserDouble(order.getTotalPayFee(), 0d);
            if (payFee > 0.0) {
                payNum++;
                totalPayFee += payFee;
            }
        }

        info.setPayNum(payNum);
        info.setTotalPayFee(totalPayFee);

        return info;

    }

}
