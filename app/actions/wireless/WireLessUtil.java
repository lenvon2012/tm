
package actions.wireless;

import java.io.File;

import play.Play;

public class WireLessUtil {

    public static File genWirelessInputDir() {
        File file = new File(WireLessUtil.genWirelessDir(), "input");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File genWirelessOutPutDir() {
        File file = new File(WireLessUtil.genWirelessDir(), "output");
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    public static File ensureUserOutputDir(Long userId) {
        File topOutputDir = genWirelessOutPutDir();
        File dir = new File(topOutputDir, String.valueOf(userId % 1000));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    static File genWirelessDir() {
        File file = new File("/data/static/wireless");
        if (file.exists()) {
            return file;
        }
        file = new File(Play.tmpDir, "wireless");
        if (!file.exists()) {
            file.mkdir();
        }

        return file;
    }

}
