
package paipai;

import junit.framework.Assert;
import models.paipai.PaiNumIidToItemCode;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;

public class PaiPaiWordTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiWordTest.class);

    public static final String TAG = "PaiPaiWordTest";

    @Test
    public void testEnsureNumIid() {
        String testCode = "testCode";
        long code = PaiNumIidToItemCode.ensureItemCode(testCode);
        log.info("[ensure  code]" + code);
        String finalCode = PaiNumIidToItemCode.fetchItemCode(code);
        log.info("[final code : ]" + finalCode);

        Assert.assertEquals(testCode, finalCode);
    }
}
