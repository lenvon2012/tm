
package autotitle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TaobaoUtil;

import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs;

public class CidNameRecommender {

    private static final Logger log = LoggerFactory.getLogger(CidNameRecommender.class);

    public static final String TAG = "CidNameRecommender";

    static CidNameRecommender _intance = null;

    private Map<Long, String> directCidName = new HashMap<Long, String>();

    /**
     * 这里还缺了个level字段
     */
    private CidNameRecommender() {
        File file = new File(TMConfigs.autoDir, "cidName.txt");
        try {
            List<String> lines = FileUtils.readLines(file, "utf-8");
            for (String line : lines) {
                String[] splits = StringUtils.split(line, '|');
                int length = splits.length;
                if (length < 2) {
                    continue;
                }
                for (int i = 0; i < length; i++) {
                    String string = splits[i];
                    String raw = TaobaoUtil.fastRemove(string, " ");
                    splits[i] = raw;
                }

                Long cid = NumberUtil.parserLong(splits[0], 0L);
                if (NumberUtil.isNullOrZero(cid)) {
                    continue;
                }
                String name = StringUtils.trim(splits[1]);
                if (StringUtils.isBlank(name)) {
                    continue;
                }

                directCidName.put(cid, name);
            }

            log.info("[build cid name :]" + directCidName);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static synchronized CidNameRecommender get() {
        if (_intance == null) {
            _intance = new CidNameRecommender();
        }

        return _intance;
    }

    public String tryGetName(Long cid) {
        if (cid == null) {
            return null;
        }
        return directCidName.get(cid);
    }

    public String tryGetName(Integer cid) {
        if (cid == null) {
            return null;
        }
        return directCidName.get(new Long(cid.longValue()));
    }
}
