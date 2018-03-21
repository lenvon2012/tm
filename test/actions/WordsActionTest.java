
package actions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import utils.PlayUtil;
import bustbapi.BusAPI.MultiItemApi;

import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;

public class WordsActionTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(WordsActionTest.class);

    public static final String TAG = "WordsActionTest";

    @Test
    public void buidItem() {
        Set<Long> ids = new HashSet<Long>();
        ids.add(36192937080L);
        ids.add(20091027408L);
        try {
            Map<Long, Item> res = new MultiItemApi(ids).execute();
            log.info("[res:]" + PlayUtil.genPrettyGson().toJson(res));

        } catch (ClientException e) {
            log.warn(e.getMessage(), e);

        }
    }

    public void testRecommendByTitle() throws ClientException {
        List<IWordBase> bases = WordsAction.buildByTitle("包邮送礼物首选清仓特价100%好评新城特产阿克苏大枣绝伦10斤鲜美");
        System.out.println(bases);
    }

}
