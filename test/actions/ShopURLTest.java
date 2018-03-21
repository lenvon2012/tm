
package actions;

import org.junit.Test;

import play.test.UnitTest;

import com.ciaosir.client.utils.HtmlUtil;

public class ShopURLTest extends UnitTest {

    @Test
    public void testShopUrl() {
        HtmlUtil.genShortUrl("http://item.taobao.com/item.htm?id=123456");
    }
}
