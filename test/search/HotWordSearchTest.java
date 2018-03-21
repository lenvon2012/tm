
package search;

import job.word.HotWordUpdateJob;

import org.junit.Test;

import play.test.UnitTest;

public class HotWordSearchTest extends UnitTest {

    @Test
    public void testHot() {
        new HotWordUpdateJob().doJob();
    }
}
