
package autotitle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import configs.TMConfigs;

public class MergedKeys {
    String key1;

    String key2;

    String target;

    static Set<String> mergedWords = new HashSet<String>();

    static List<MergedKeys> merged = new ArrayList<MergedKeys>();

    public MergedKeys(String key1, String key2, String target) {
        super();
        this.key1 = key1;
        this.key2 = key2;
        this.target = target;
    }

    static {
        init();
    }

    static void init() {
        try {
            List<String> lines = FileUtils.readLines(new File(TMConfigs.autoDir, "merge.txt"));
            for (String line : lines) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                String[] splits = line.split(",");
                MergedKeys mKey = new MergedKeys(splits[0], splits[1], splits[2]);
                mergedWords.add(splits[2]);
                MergedKeys.merged.add(mKey);

            }
        } catch (IOException e) {
            AutoTitleOption.log.warn(e.getMessage(), e);
        }

    }
}
