
package autotitle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs.SplitAPI;
import com.ciaosir.client.word.IKSpliter;
import com.ciaosir.client.word.PaodingSpliter;
import com.ciaosir.client.word.PaodingSpliter.SplitMode;
import com.ciaosir.commons.ClientException;
import com.ciaosir.commons.CredClientManager;

import configs.TMConfigs;

public class AutoSplit extends SplitAPI {

    private static final Logger log = LoggerFactory.getLogger(AutoSplit.class);

    public static final String TAG = "AutoSplit";

    List<String> brandNameList = ListUtils.EMPTY_LIST;

    String word;

    boolean mergeDump = false;
    
    static boolean useLocalPaoding = Play.mode.isProd() || "zrb".equals(Play.id) || "autodev".equals(Play.id)
            || "tbtdev".equals(Play.id) || "zl".equals(Play.id) || "zhy".equals(Play.id);
    //modify by uttp
    static boolean useIKAnlayzer = "ywj".equals(Play.id) || "zhy".equals(Play.id) || "hyg".equals(Play.id)
    		|| "lzl".equals(Play.id);

    static SplitMode MODE = SplitMode.BASE;

    public AutoSplit(String word, List<String> brandName) {
        super(word, MODE, false);
        this.word = word;
        this.brandNameList = brandName;
    }

    public AutoSplit(String word, List<String> brandNames, boolean mergeDump) {
        super(word, MODE, mergeDump);
        this.word = word;
        this.brandNameList = brandNames;
        this.mergeDump = mergeDump;
    }

    public AutoSplit(String word, boolean debug) {
        super(word, SplitMode.BASE, false);
        this.word = word;
        this.debug = debug;
    }

    boolean debug = false;

    @SuppressWarnings("unchecked")
    public List<String> execute() throws ClientException {
        try {

            word = word.toLowerCase();
            
            List<String> keys = ListUtils.EMPTY_LIST;
            if (useIKAnlayzer) {
                keys = IKSpliter.doSplit(word);
            } else if (useLocalPaoding) {
//                log.info("[paoding word:]" + word);
                keys = PaodingSpliter.split(word, MODE, mergeDump);
                String upper = word.toUpperCase();
                String lower = word.toLowerCase();
                Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    if (word.indexOf(next) < 0 && upper.indexOf(next) < 0 && lower.indexOf(next) < 0) {
                        it.remove();
                    }
                }

            } else {
                keys = (List<String>) CredClientManager.execute(urlBuilder.genRequest(), getValidator());
            }
            // 切不出的话，用男装为例
            // keys=Arrays.asList(new String[]{"袜子", "裤衩"});
            if (debug) {
                Logger log = LoggerFactory.getLogger(AutoSplit.class);
                log.error(" raw split :" + keys);
            }

            if (CommonUtils.isEmpty(keys)) {
                return ListUtils.EMPTY_LIST;
            }

            keys = removeByRules(keys);
            keys = removeByBrands(keys);

            AutoSplitFixAction.doFixAutoSplit(word, keys);

//            log.info("[final keys:]" + word);
            return new ArrayList<String>(keys);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * remove for the brands from list keys.......
     * @param keys
     * @return
     */
    public List<String> removeByBrands(List<String> keys) {
        if (CommonUtils.isEmpty(this.brandNameList)) {
            return keys;
        }

        for (String brandName : brandNameList) {
            if (!this.word.contains(brandName)) {
                // 标题中木有这个标题
                continue;
            }

            boolean isRightSplitted = false;
            // check the brand name...
            for (String split : keys) {
                if (StringUtils.equals(split, brandName)) {
                    // Aready contains....
                    isRightSplitted = true;
                    break;
                }
            }

            if (isRightSplitted) {
                continue;
            }

            // 看来是切出问题了。。。爱丽丝 切出来 爱丽 + 丝，只能干掉他。。。
//            List<String> newKeys = new ArrayList<String>(keys);

            int subSplitIndex = -1;
            for (int i = keys.size() - 1; i > 0; i--) {
                String currSplit = keys.get(i);
                // 目前 keys里面已经没有和brandName equal的关键词了
                if (brandName.contains(currSplit)) {
                    if (subSplitIndex < 0) {
                        subSplitIndex = i;
                        keys.set(i, brandName);
                    } else {
                        keys.remove(i);
//                        i++;
                    }
                }

            }
        }

        return keys;
    }

    public List<String> removeByRules(List<String> keys) {
        for (int i = keys.size() - 1; i > 0; i--) {
            String curr = keys.get(i);
            String before = keys.get(i - 1);
            if (curr.contains(before)) {
                keys.remove(i - 1);
                continue;
            }

            if (unitSet.contains(curr) && StringUtils.isNumeric(before)) {
                keys.set(i, before + curr);
                keys.remove(i - 1);
                continue;
            }

            for (TripleKey tripple : fixNeiborkeys) {
                if (tripple.key1.equals(before) && tripple.key2.equals(curr)) {
                    keys.set(i, tripple.key3);
                    keys.remove(i - 1);
                    break;
                } else if (tripple.key2.equals(before) && tripple.key1.equals(curr)) {
                    keys.set(i, tripple.key3);
                    keys.remove(i - 1);
                    break;
                }
            }

        }
        return keys;
    }

    public static List<TripleKey> fixNeiborkeys = new ArrayList<TripleKey>();

    static class TripleKey {
        String key1;

        String key2;

        String key3;

        @Override
        public String toString() {
            return "TripleKey [key1=" + key1 + ", key2=" + key2 + ", key3=" + key3 + "]";
        }

        public TripleKey() {
            super();
        }

        public TripleKey(String key1, String key2, String key3) {
            super();
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
        }
    }

    static void initFixNeihborkeys() {
        try {
            List<String> lines = FileUtils.readLines(new File(TMConfigs.autoDir, "fixneighbor.txt"));
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) {
                    continue;
                }

                String[] split = line.split(",");
                fixNeiborkeys.add(new TripleKey(split[0], split[1], split[2]));
            }

            log.error("init neighberwords :" + fixNeiborkeys);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    static Set<String> unitSet = new HashSet<String>();

    static void initFixUnit() {
        try {
            List<String> lines = FileUtils.readLines(new File(TMConfigs.autoDir, "x-unit.dic"));
            for (String line : lines) {
                if (StringUtils.isEmpty(line)) {
                    continue;
                }

                unitSet.add(line);
            }
            log.error("init unitSet :" + unitSet);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "AutoSplit []";
    }

    static {
        initFixNeihborkeys();
        initFixUnit();
    }

}
