package actions.delist;

import job.autolist.service.GoodListDistriCalcu;
import job.autolist.service.ModifyListTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 计算出好的上下架分布
 * @author ying
 *
 */
public class GoodDistriAction {
    
    private static final Logger log = LoggerFactory.getLogger(GoodDistriAction.class);
    
    /**
     * 根据分布比例，以及宝贝总数，计算出分布
     * @param hourRateArray
     * @param totalNum
     * @return
     */
    public static int[] genGoodDistriArray(int[] hourRateArray, int totalNum) {
        int arrLength = 7 * 24;
        int[] distriNumArray = new int[arrLength];
        
        for (int i = 0; i < distriNumArray.length; i++) {
            distriNumArray[i] = 0;
        }
        
        if (hourRateArray == null || hourRateArray.length != arrLength) {
            return distriNumArray;
        }
        
        distriNumArray = GoodListDistriCalcu.calcuGoodDistribute(hourRateArray, totalNum);
        
        return distriNumArray;
        
        
    }
    
    /**
     * 根据好的分布，以及当前真实的分布，返回应该添加的分布
     * @param goodDistriArray
     * @param curExistArray
     * @return
     */
    public static int[] calcuResultAddDistriArray(int[] goodDistriArray, int[] curExistArray) {
        
        int arrLength = 7 * 24;
        int[] diffArray = new int[arrLength];
        
        for (int i = 0; i < diffArray.length; i++) {
            diffArray[i] = 0;
        }
        
        if (goodDistriArray == null || goodDistriArray.length != arrLength 
                || curExistArray == null || curExistArray.length != arrLength) {
            return diffArray;
        }
        
        boolean isNoNegative = true;//diff中没有负数
        int needDistriNum = 0;
        
        for (int i = 0; i < diffArray.length; i++) {
            diffArray[i] = goodDistriArray[i] - curExistArray[i];
            if (diffArray[i] < 0) {
                isNoNegative = false;
            }
            needDistriNum += diffArray[i];
        }
        if (isNoNegative == true) {
            return diffArray;
        }
        
        int[] resultArray = new int[arrLength];
        
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = 0;
        }
        
        if (needDistriNum <= 0) {
            return resultArray;
        }
        
        for (int i = 0; i < needDistriNum; i++) {
            int hourIndex = ModifyListTime.findGoodPosition(diffArray);
            resultArray[hourIndex]++;
        }
        
        return resultArray;
    }
    
    
}
