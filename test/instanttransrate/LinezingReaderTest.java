
package instanttransrate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.user.User;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import configs.TMConfigs;
import dao.UserDao;

public class LinezingReaderTest extends FunctionalTest {

    private static final Logger log = LoggerFactory.getLogger(LinezingReaderTest.class);

    public static final String TAG = "LinezingReaderTest";

    @Test
    public void testRead() throws IOException {
        User user = UserDao.findByUserNick("楚之小南");
        String data = FileUtils.readFileToString(new File(TMConfigs.autoDir, "linezing.json"), "utf-8");
        Map<String, String> params = new HashMap<String, String>();
        params.put("data", data);
        params.put("sid", user.getSessionKey());

        Response resp = POST("/InstantTransRate/uploadLinzingVisitLog", params);
        String content = FunctionalTest.getContent(resp);
        log.info("[content:]" + content);

    }

    @Test
    public void testeUserTs() {
        User user = UserDao.findByUserNick("楚之小南");
        Map<String, String> params = new HashMap<String, String>();
        params.put("sid", user.getSessionKey());
        Response resp = POST("/InstantTransRate/userTs", params);
        String content = FunctionalTest.getContent(resp);
        log.info("[content:]" + content);
    }
}
