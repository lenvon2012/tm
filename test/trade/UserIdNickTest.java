
package trade;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import play.test.UnitTest;
import actions.catunion.UserIdNickAction;
import actions.catunion.UserIdNickAction.BuyerIdApi;

public class UserIdNickTest extends UnitTest {

    @Test
    public void idNickTest() {
//        String userInfoUrl = BuyerIdApi.getUserInfoUrl("紫归未回");
        long id = UserIdNickAction.findUserIdByNick("紫归未回");
        System.out.println(id);
    }

    private static final String[] arrAlphalet = {
            "Z", "A", "B", "C", "D", "E", "F",
            "J", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
            "T", "U", "V", "W", "X", "Y", "Z"
    };

    public static void main(String[] args) {
        System.out.println(dec2Alpha(279680145L));
    }

    public static String dec2Alpha(long decimal) {
        ArrayList<String> arrResult = new ArrayList<String>();
        long a = decimal;
        long b = decimal;
        do {
            b = a / 26L;
            int c = (int) (a % 26);
            if (c == 0 && b == 0) {
                arrResult.add(0, "0");
            } else if (c == 0 && b != 0) {
                b = b - 1;
                arrResult.add(0, arrAlphalet[c]);
            } else {
                arrResult.add(0, arrAlphalet[c]);
            }
            a = b;
        } while (a > 26);
        if (a != 0) {
            arrResult.add(0, arrAlphalet[(int) a]);
        }
        return StringUtils.join(arrResult.toArray());
    }
}
