
package click;

import org.hibernate.property.DirectPropertyAccessor.DirectGetter;
import org.junit.Test;

import play.test.UnitTest;
import bustbapi.ClickApi.DoAttack;
import bustbapi.ClickApi.DoChedaoCilckJob;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;

public class ClickApiTest extends UnitTest {

    @Test
    public void doClick() {
//        ClickApi.doWord();
        new DoChedaoCilckJob().doJob();
    }

    public void doAttackTaodake() {
        while (true) {
            new DoAttack().doJob();
            CommonUtils.sleepQuietly(50L);
        }
    }
    
    public void testDirectGet() {
    	String s = API.directGet("http://list.taobao.com/itemlist/default.htm?atype=b&cat=50000671&style=grid&as=0&viewIndex=1&same_info=1&isnew=2&tid=0&_input_charset=utf-8", "", null);
    	System.out.println(s);
    }
}
