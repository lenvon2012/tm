
package search;

import java.io.File;

import models.mysql.word.TMCWordBase;

import org.junit.Test;

import play.test.UnitTest;

public class TMCWordTest extends UnitTest {

    @Test
    public void testReadLines() {
        File file = new File("/home/zrb/code/wordtook.txt");
        File output = new File("/home/zrb/code/tm/conf/queries.txt");
        TMCWordBase.readFetchLineParams(file, output);
    }
}
