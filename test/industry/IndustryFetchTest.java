
package industry;

import onlinefix.IndustryJob;

import org.junit.Test;

import play.test.UnitTest;

public class IndustryFetchTest extends UnitTest {

    @Test
    public void testFetch() {
        new IndustryJob(2, 0).doJob();
    }
}
