
package autotitle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import models.item.ItemCatPlay;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;

import configs.TMConfigs;

public class AutoTitleEngine implements Callable<String> {

    private static final Logger log = LoggerFactory.getLogger(AutoTitleEngine.class);

    public static final String TAG = "AutoTitleEngine";

    AutoTitleOption opt;

    public static final long MAX_ADD_AVAILBLE_WORD_LENGTH = 56;

    public static final long MAX_WORD_LENGTH = 60;

    public static final long REMOVE_WORD_BOUND = 44;

    static String[] promotesWord = new String[] {
            "特价", "促销", "热销"
    };

    static int promoteWordNum = AutoTitleEngine.promotesWord.length;

    public AutoTitleEngine(AutoTitleOption opt) {
        super();
        this.opt = opt;
        log.info("[for opt:]" + opt);
    }

    List<String> preparedWords = new ArrayList<String>();

    int TO_REMOVED_WORD_LENGTH_WHEN_TOO_LONG = 3;

    List<String> toRemovedWord = new ArrayList<String>();

    private Set<String> fixedStartSet;

    @Override
    public String call() throws Exception {
    	
        log.info("[fixed start :]" + this.opt.fixedStart);
        String cleanTitle = null;
        if (opt.getPageOpt().isRepalceOrigin()) {
            cleanTitle = buildClean(opt.getRawTitle());
            if (opt.getPageOpt().isNoColor()) {
                log.info("[do no color:]" + cleanTitle);
                cleanTitle = removeColors(cleanTitle);
            }

//            log.info("[clean title:]" + cleanTitle);
//            log.error("length:" + ChsCharsUtil.length(cleanTitle));
            if (ChsCharsUtil.length(cleanTitle) > REMOVE_WORD_BOUND) {
//                log.error("before remove :" + cleanTitle);
                cleanTitle = tryRemoveRawTitleWithUselessWord(cleanTitle);
//                log.error("after remove :" + cleanTitle);
            }

            if (opt.isReplaceSerial()) {
                cleanTitle = removeSerail(cleanTitle);
//                cleanTitle = TaobaoUtil.fastRemove(cleanTitle, "-");
            }

            // TODO if too shot, directly append sellercid
            cleanTitle = mergeRawTitle(cleanTitle);
//            log.error(" beofore   fixed :" + cleanTitle + ":::" + opt.fixedStart);
            if (opt.getPageOpt().isRepalceOrigin()) {
                cleanTitle = fillFixedStart(cleanTitle);
            }

        } else {
            cleanTitle = opt.getRawTitle();
            log.info("[send raw title]" + cleanTitle);
        }

        List<String> preparedSplits = prepareToAddWordss();
//        log.info("[prepared seed :]" + preparedSplits);

        String[] targetArr = preparedSplits.toArray(NumberUtil.EMPTY_STRING_ARRAY);
        Queue<IWordBase> queue = ItemPropAction.prepareIWordBaseQueue(targetArr, opt, toRemovedWord);

//        log.info("[queue:]" + queue);

        boolean available = true;
        while (available) {

            IWordBase poll = queue.poll();

            if (poll == null) {
                available = false;
                break;
            }
//            log.error("[poll out word :]" + poll.getWord() + " for current title :" + cleanTitle);

            String word = poll.getWord();
            if (TaobaoUtil.isDouble(word)) {
                if (word.length() - word.indexOf('.') > 3) {
                    word = word.substring(0, word.indexOf(".") + 3);
                }
            }

//            log.info("[to add poll word :]" + word + " to  raw title :" + cleanTitle);
            if (!opt.isAllowed(cleanTitle, word)) {
                continue;
            }

            if (cleanTitle.toLowerCase().indexOf(word.toLowerCase()) >= 0) {
                continue;
            }

            if (ChsCharsUtil.length(cleanTitle + word) > MAX_WORD_LENGTH) {
                continue;
            }

            if (opt.isReplaceSerial() && !StringUtils.isEmpty(opt.definedProps.serialNumber) &&
                    opt.definedProps.serialNumber.toLowerCase().indexOf(word.toLowerCase()) >= 0) {
                log.info("[because of serial...]");
                continue;
            }
//            if (opt.isReplaceSerial() && word.indexOf('-') >= 0) {
//                continue;
//            }

            if (isInAnti(cleanTitle, word)) {
                log.info("[add word in anti]" + word);
                continue;
            }

            if (willNewTitleContainAntiWord(cleanTitle, word)) {
                log.info("[add word containers anti ]" + word);
                continue;
            }

            if (hasRatioWord(cleanTitle, word)) {
                log.info("[add word has ratio ]" + word);
                continue;
            }

            if (hasBadWord(word)) {
                log.info("[add word has bad word]" + word);
                continue;
            }
            if (opt.getMustExcluded().contains(word)) {
                log.info("[add word has been exclude]" + word);
                continue;
            }

            int length = word.length();
            // log.info("[to add word]" + word);

            if (length >= 2 && !NumberUtils.isDigits(word)) {
                String head = word.substring(0, 2);
                int index = cleanTitle.indexOf(head);
                if (index < 0) {
                    head = word.substring(0, 1);
                    if (cleanTitle.endsWith(head)) {
//                        cleanTitle = cleanTitle.substring(0, cleanTitle.length() - 1)  + word;
                        cleanTitle += " " + word;
                    } else {
                        cleanTitle = cleanTitle + word;
                    }
                } else {
                    cleanTitle = cleanTitle.replace(head, word);
                }
            }
//            else {
//                cleanTitle = cleanTitle + word;
//            }

            if (!isTitleAddAvailable(cleanTitle)) {
                available = false;
                break;
            }
        }

        log.info("[before good word :]" + cleanTitle);
        cleanTitle = tryAppendGoodWord(cleanTitle);
        log.info("[after good word :]" + cleanTitle);
        cleanTitle = tryAppendTitlePrmote(cleanTitle);

        return cleanTitle;
    }

    private String removeColors(String cleanTitle) {
        try {
            List<String> splits = new AutoSplit(cleanTitle, false).execute();
            log.info("[to remotewords :]" + splits);
            List<String> toRemoveWords = new ArrayList<String>();
            for (String split : splits) {
                if (ItemPropAction.colorSet.contains(split)) {
                    toRemoveWords.add(split);
                }
            }
            log.info("[to remotewords :]" + toRemoveWords);
            Collections.sort(toRemoveWords, TaobaoUtil.WordLenghComparitor);
            for (String string : toRemoveWords) {
                cleanTitle = TaobaoUtil.fastRemove(cleanTitle, string);
            }
            log.info("[after remove colors:]" + cleanTitle);
            return cleanTitle;
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
            return cleanTitle;
        }
    }

    private List<String> prepareToAddWordss() throws ClientException {

//        preparedWords.addAll(toRemovedWord);
        String propArr = StringUtils.join(opt.getPropSet(), " ");
//        log.info("[prop arr :]" + propArr + " ---- cid names :" + opt.getSellerCidNames());
        if (!StringUtils.isEmpty(propArr)) {
            preparedWords.add(propArr);
        }
        if (!StringUtils.isEmpty(opt.getSellerCidNames())) {
            preparedWords.add(opt.getSellerCidNames());
        }

//        if (opt.getPropArr().length < 3 && toRemovedWord.size() == 0 && StringUtils.isEmpty(opt.getSellerCidNames())) {
//            List<String> splits = new ArrayList<String>();
//            if (StringUtils.length(opt.getCidNames())) {
//            }
//            List<String> splits = new AutoSplit(opt.getSellerCidNames()).execute();
//        }
        /*
         * 添加类目词白名单
         */
        String cidName = CidNameRecommender.get().tryGetName(opt.getCid());
        log.error("cat name :" + cidName + " with cid :" + opt.getCid());
        if (!StringUtils.isEmpty(cidName)) {
            preparedWords.add(cidName);
        }

//        log.warn("[before splits :]" + preparedWords);
//        log.warn("[brand names:]" + opt.getBrandNameList());
        /**
         * 需要重新过滤一下 品牌这个问题
         */
        List<String> preparedSplits = new AutoSplit(opt.getRawTitle(), opt.getBrandNameList()).execute();
        preparedSplits = new ArrayList<String>(preparedSplits);
//        List<String> preparedSplits = new AutoSplit(StringUtils.join(preparedWords, " "), opt.getBrandNameList()).execute();
//        log.warn("[after splits :]" + preparedSplits);
        preparedSplits.addAll(opt.getPropSet());
//        LinkedList<String> linkedList = new LinkedList<String>(preparedSplits);
//        ListIterator<String> li = linkedList.listIterator();
//        preparedSplits.add
        Set<String> splitSet = new HashSet<String>(preparedSplits);
        List<String> filtered = new ArrayList<String>();
        for (String string : splitSet) {
            filtered.add(string);
        }
//        log.info("[prepared splis :]" + preparedSplits.getClass());
//        log.info("[to removed words :]" + toRemovedWord);
//        if (!CommonUtils.isEmpty(toRemovedWord)) {
//            preparedSplits.addAll(toRemovedWord);
//        }
        return filtered;
    }

    private String tryRemoveRawTitleWithUselessWord(String cleanTitle) throws ClientException {
        String brand = opt.getBrandName();

//        log.info("[brand : ]" + brand);
        // nothing available...
        List<String> currTitleSplits = new AutoSplit(opt.getRawTitle(), opt.getBrandNameList()).execute();

        int originSplitNum = currTitleSplits.size();
        int removeLength = ChsCharsUtil.length(cleanTitle) > 54 ? 4 : 2;

        int targetLength = originSplitNum - removeLength;
        if (targetLength < 5) {
            targetLength = originSplitNum;
        }

        Map<String, IWordBase> wBaseMap = new AutoWordBase(currTitleSplits, opt.getCid()).execute();
//        log.info("[back map:]" + wBaseMap);

        List<IWordBase> bases = new ArrayList<IWordBase>(wBaseMap.values());
        Collections.sort(bases, ItemPropAction.CLICK_COMPARATOR);
//        log.info("[find word click:]" + toClickDebug(bases));
        // bases have been sorted....
        int hasBaseNum = bases.size();
        if (hasBaseNum > targetLength) {
            List<IWordBase> toRemoved = bases.subList(targetLength, bases.size());
            for (IWordBase iWordBase : toRemoved) {
                wBaseMap.remove(iWordBase.getWord());
            }
        }

        int maxRemoveNum = originSplitNum - wBaseMap.size();
        if (maxRemoveNum > 4) {
            maxRemoveNum = 4;
        }

//        Iterator<String> it = currTitleSplits.iterator();
        for (int i = originSplitNum - 1; i >= 0 && maxRemoveNum > 0; i--) {
            String next = currTitleSplits.get(i);
            if (!StringUtils.isEmpty(next) && (next.length() <= 1 || next.length() > 4)) {
                continue;
            }

            IWordBase base = wBaseMap.get(next);

            if (base != null && base.getClick() > 1) {
                continue;
            }
            if (isToKeepForRemoval(next, currTitleSplits, i)) {
                continue;
            }

            toRemovedWord.add(next);
            currTitleSplits.remove(i);
            maxRemoveNum--;

        }

//
//        while (it.hasNext() && maxRemoveNum > 0) {
//                    }

//        log.error("[toremoded words :]" + toRemovedWord);
        cleanTitle = rebuildTitle(cleanTitle, toRemovedWord);
        cleanTitle = rebuildTitle(cleanTitle, this.opt.getMustExcluded());
        if (wBaseMap.containsKey("2012")) {
            cleanTitle = TaobaoUtil.fastReplace(cleanTitle, "2012", "2016");
        }
        if (wBaseMap.containsKey("2013")) {
            cleanTitle = TaobaoUtil.fastReplace(cleanTitle, "2013", "2016");
        }
        if (wBaseMap.containsKey("2014")) {
            cleanTitle = TaobaoUtil.fastReplace(cleanTitle, "2014", "2016");
        }
        if (wBaseMap.containsKey("2015")) {
            cleanTitle = TaobaoUtil.fastReplace(cleanTitle, "2015", "2016");
        }
        if (wBaseMap.containsKey("2015")) {
            cleanTitle = TaobaoUtil.fastReplace(cleanTitle, "2016", "2017");
        }

        return cleanTitle;
    }

    public boolean isToKeepForRemoval(String currWord, List<String> currTitleSplits, int i) {

        if (opt.isKeepBrand() && !StringUtils.isEmpty(opt.getBrandName()) && currWord.indexOf(opt.getBrandName()) >= 0) {
//            log.info("[container brand:]" + currWord);
            return true;
        }

        if (hasBadWord(currWord)) {
            // 如果已经有 让我们恐惧的词。。就不要碰他了。
            return true;
        }

        if (i > 0) {
            String prev = currTitleSplits.get(i - 1);
            if (prev.indexOf(currWord.substring(0, 1)) == (prev.length() - 1)) {
                return true;
            }
        }
        if (i < currTitleSplits.size() - 1) {
            String after = currTitleSplits.get(i + 1);
            if (after.indexOf(currWord.substring(currWord.length() - 1)) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 固定标题头
     * @param cleanTitle
     * @return
     * @throws ClientException 
     */
    private String fillFixedStart(String cleanTitle) throws ClientException {

        String fixedStart = opt.fixedStart;
        if (!isTitleAddAvailable(opt.fixedStart)) {
            log.error(" so fuck long title : " + fixedStart);
            return cleanTitle;
        }
        if (StringUtils.isEmpty(opt.fixedStart)) {
            return cleanTitle;
        }
        int existIndex = cleanTitle.indexOf(fixedStart);
        if (existIndex == 0) {
            return cleanTitle;
        }
        if (existIndex > 0) {
            cleanTitle = opt.fixedStart + TaobaoUtil.fastRemove(cleanTitle, opt.fixedStart);
            return cleanTitle;
        }
        for (String str : opt.fixedStartSet) {
            cleanTitle = TaobaoUtil.fastRemove(cleanTitle, str);
        }

        String originTitle = cleanTitle;
//        cleanTitle += opt.fixedStart;
        String directAppendTitle = opt.fixedStart + cleanTitle;
        if (isTitleAddAvailable(directAppendTitle)) {
            return directAppendTitle;
        }

        // trim for origin..
        List<String> execute = new AutoSplit(originTitle, opt.getBrandNameList()).execute();
        int size = execute.size();
        for (int i = size - 1; i >= 0; i--) {
            String curr = execute.get(i);
            if (isToKeepForRemoval(curr, execute, i)) {
                continue;
            }
            log.warn(" first remove :" + curr);

            cleanTitle = TaobaoUtil.fastRemove(cleanTitle, curr);
            toRemovedWord.add(curr);

            if (ChsCharsUtil.length(opt.fixedStart + cleanTitle) <= MAX_WORD_LENGTH) {
                return opt.fixedStart + cleanTitle;
            }
        }

        /*
         * 这都没拿掉这个标题,那看来直接截断后面的吧。。
         */
        int originSize = cleanTitle.length();
        for (int i = originSize - 1; i > 0; i--) {
            cleanTitle = cleanTitle.substring(0, i);
            if (isTitleAddAvailable(opt.fixedStart + cleanTitle)) {
                return cleanTitle;
            }
        }

        return cleanTitle;
    }

    private String toClickDebug(Collection<IWordBase> bases) {
        StringBuilder sb = new StringBuilder();
        for (IWordBase iWordBase : bases) {
            sb.append("[");
            sb.append(iWordBase.getWord());
            sb.append(":");
            sb.append(iWordBase.getClick());
            sb.append("],");
        }
        return sb.toString();
    }

    private String removeSerail(String cleanTitle) {
        String serial = opt.getSerial();
        if (StringUtils.isEmpty(serial)) {
            return cleanTitle;
        }
        cleanTitle = removeAllTargets(cleanTitle, serial);
        /**
         * 深度修复 货号问题
         */
        if (serial.indexOf('-') > 0) {
            String[] serialParts = serial.split("-");
            for (String part : serialParts) {
                if (part != null && part.length() > 2) {
                    cleanTitle = removeAllTargets(cleanTitle, part);
                }
            }
        }
        return cleanTitle;
    }

    private String rebuildTitle(String cleanTitle, Collection<String> toRemovedWord) {
        if (CommonUtils.isEmpty(toRemovedWord)) {
            return cleanTitle;
        }
//        log.info("[to remove words :]" + toRemovedWord);
        for (String string : toRemovedWord) {
            if (StringUtils.isEmpty(string)) {
                continue;
            }

            cleanTitle = TaobaoUtil.fastRemove(cleanTitle, string);
        }

        return cleanTitle;
    }

    public static String removeAllTargets(String src, String toRemoved) {
        while (src.indexOf(toRemoved) >= 0) {
//            src = src.replace(toRemoved, StringUtils.EMPTY);
            src = TaobaoUtil.fastRemove(src, toRemoved);
        }
        return src;
    }

    static String[] yifuStart = new String[] {
            "2016新款",
            "2016新款",
            "2016新款",
            "2016新品",
            "2016新款",
            "2016新品",
            "2016新款",
            "2016明星同款 ",
            "2016新品",
            "2016新款",
            "2016大牌"
    };

    static String[] fushixiangbaoStart = new String[] {
            "2016新款", "2016新品"
    };

    private String tryAppendGoodWord(String title) {
        if (!isTitleAddAvailable(title)) {
            return title;
        }

        try {
            /*
             * 对于这里可以添加的关键词，重点还是在于类目的一个针对性
             */
            Long cid = opt.getLevel1Cid();
            /**
             * 先核对　一级类目
             */
            if (cid == null) {
                return title;
            }

            boolean isLongWordAdd = false;

            String[] seeds = null;
            if (ItemCatPlay.IsYifu(cid)) {
                seeds = yifuStart;
            } else if (ItemCatPlay.isFushixiangbaoLvel1(cid)) {
                seeds = fushixiangbaoStart;
            }

            if (seeds != null) {
                String newTitle = tryAddLongWord(title, seeds, 1);
                if (!StringUtils.equals(newTitle, title)) {
                    title = newTitle;
                    isLongWordAdd = true;
                    return title;
                }
            }

            if (!isLongWordAdd && ItemCatPlay.isFushixiangbaoLvel1(cid)) {
                for (String simpleShortWord : simpleShopWords) {
                    if (!title.contains(simpleShortWord) && isTitleAddAvailable(simpleShortWord + title)) {
                        String newTitle = simpleShortWord + title;
                        title = newTitle;
                        return title;
                    }
                }
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }

        return title;
    }

    static String[] simpleShopWords = new String[] {
            "2016", "新款"
    };

    private String tryAddLongWord(String title, String[] seeds, int maxAddCount) {
        if (!isTitleAddAvailable(title)) {
            return title;
        }

        int seed = title.hashCode() % seeds.length;
        if (seed < 0) {
            seed = -seed;
        }

        int appendCount = 0;
        for (int i = 0; i < seeds.length; i++) {
            String candidate = seeds[(seed + i) % seeds.length];
            if (title.contains(candidate)) {
                continue;
            }
            if (this.opt.mustExcluded.contains(candidate)) {
                continue;
            }
            if (!isTitleAddAvailable(candidate + title)) {
                continue;
            }

            title = candidate + title;
            appendCount++;

            // We do not need to append to many promote words...
            if (!isTitleAddAvailable(title) || appendCount >= maxAddCount) {
                return title;
            }

        }
        return title;
    }

    public String tryAppendTitlePrmote(String title) {
        if (!isTitleAddAvailable(title)) {
            return title;
        }
        if (!opt.isAddPromoteWords()) {
            return title;
        }

        int seed = title.hashCode() % AutoTitleEngine.promotesWord.length;
        if (seed < 0) {
            seed = -seed;
        }

        int appendCount = 0;
        for (int i = 0; i < AutoTitleEngine.promoteWordNum; i++) {
            String promote = AutoTitleEngine.promotesWord[(seed + i) % AutoTitleEngine.promoteWordNum];
            if (title.contains(promote)) {
                continue;
            }
            if (this.opt.mustExcluded.contains(promote)) {
                continue;
            }

            if (!isTitleAddAvailable(title + promote)) {
                continue;
            }

            title = promote + title;
            appendCount++;

            // We do not need to append to many promote words...
            if (!isTitleAddAvailable(title) || appendCount >= 1) {
                return title;
            }

        }

        return title;
    }

    static boolean isTitleAddAvailable(String title) {
//        return ChsCharsUtil.length(title) <= AutoTitleEngine.MAX_ADD_AVAILBLE_WORD_LENGTH;
        return ChsCharsUtil.length(title) <= AutoTitleEngine.MAX_WORD_LENGTH;
    }

    static String[] mustRemoveWords = new String[] {
            "请修改标题，", "请修改标题", "&amp;", "&middot",
            "(请自行修改价格和宝贝名称)", "(请自行修改和宝贝)", "(修改价格和宝贝)", "(修改和宝贝)",
            "请自行修改价格和宝贝名称", "请自行修改和宝贝", "修改价格和宝贝", "修改和宝贝",
            "请先上传然后在卖家中心出售中的宝贝里修改宝贝名称",
            "请先上传在卖家中心出售中的宝贝里修改名称然后",
            "请先上传在卖家中心出售中的宝贝里修改名称", "【主图务必自行修改】",
            "下载后优化标题"

    };

    public String buildClean(String title) {
        if (title == null) {
            return StringUtils.EMPTY;
        }

        for (String mustRemoveWord : mustRemoveWords) {
            title = TaobaoUtil.fastRemove(title, mustRemoveWord);
        }

        for (String mustRemoveWord : opt.mustExcluded) {
            title = TaobaoUtil.fastRemove(title, mustRemoveWord);
        }

        StringBuilder sb = new StringBuilder();
        if (title.indexOf("T恤") >= 0) {
            title = TaobaoUtil.fastReplace(title, "T恤", "t恤");
        }
        if (title.indexOf("V领") >= 0) {
            title = TaobaoUtil.fastReplace(title, "V领", "v领");
        }

        title = trimDump(title);

//        log.info("[trimed :]" + title);
        int length = title.length();
        for (int i = 0; i < length; i++) {
            String ch = title.substring(i, i + 1);
            if (goodCharSet.contains(ch)) {
                sb.append(ch);
            }

            if (StringUtils.isNumeric(ch) || StringUtils.isAlpha(ch) || ch.matches(ChsCharsUtil.chsReg)) {
                sb.append(ch);
            }
        }

//        String newTitle = sb.toString();
//        log.info("[clean :]" + newTitle);
        return sb.toString();
    }

    static String replaceLast(String src, String target) {
        StringBuilder b = new StringBuilder(src);
        int lastIndex = src.lastIndexOf(target);
        b.replace(lastIndex, lastIndex + target.length(), StringUtils.EMPTY);
        src = b.toString();
        return src;
    }

    public String trimDump(String title) {
        try {
            int index = -1;
            List<String> execute = new AutoSplit(title, opt.getBrandNameList()).execute();

//            log.error("[dump trim:]" + execute);
            int length = execute.size();
            for (int i = length - 1; i >= 0; i--) {
                String word = execute.get(i);
                if (StringUtils.length(word) <= 1) {
                    continue;
                }
                if (StringUtils.isNumeric(word)) {
                    continue;
                }
                index = title.indexOf(word);
                if (index < 0) {
                    continue;
                }

                while ((index = title.indexOf(word, index + 1)) >= 0) {
                    // This must be dump.. remove one...
                    title = AutoTitleEngine.replaceLast(title, word);
                }
            }
        } catch (ClientException e) {
            AutoTitleAction.log.warn(e.getMessage(), e);
        }
        return title;
    }

    static Set<String> goodCharSet = new HashSet<String>();
    static {
        goodCharSet.add(".");
        goodCharSet.add("(");
        goodCharSet.add(")");
        goodCharSet.add("-");
        goodCharSet.add("=");
        goodCharSet.add(":");
        goodCharSet.add("%");
        goodCharSet.add("*");
        goodCharSet.add("+");
        goodCharSet.add("/");
    }

    private String mergeRawTitle(String cleanTitle) {
        for (MergedKeys mKey : MergedKeys.merged) {
            if (cleanTitle.indexOf(mKey.key1) < 0 || cleanTitle.indexOf(mKey.key2) < 0) {
                continue;
            }
            cleanTitle = removeAllTargets(cleanTitle, mKey.key1);
            cleanTitle.replace(mKey.key2, mKey.target);
        }
        return cleanTitle;
    }

    public static String trimAllPunctuation(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        return trimAllPunctuation(sb);
    }

    public static String trimAllPunctuation(StringBuilder sb) {
        int length = sb.length();
        for (int i = length - 1; i > 0; i--) {
            String curr = sb.substring(i, i + 1);
            if (StringUtils.isAlphanumeric(curr)) {
                continue;
            }
            sb.deleteCharAt(i);
        }
        return sb.toString();
    }

    static List<StringPair> antonymWords = new ArrayList<StringPair>();

    static {
        File file = new File(TMConfigs.autoDir, "antonym.txt");
        try {
//            StringPair pair = null;
            List<String> lines = FileUtils.readLines(file);
            for (String line : lines) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                String[] splits = line.split(",");
                antonymWords.add(new StringPair(splits[0], splits[1]));
                antonymWords.add(new StringPair(splits[1], splits[0]));

            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    private boolean isInAnti(String title, String toAddWord) {
        for (StringPair pair : antonymWords) {
            if (pair.getKey().equals(toAddWord) && title.indexOf(pair.getValue()) >= 0) {
                log.warn("[find anti word :]" + title + " with to add word :" + toAddWord + " with pair.." + pair);
                return true;
            }
        }
        return false;
    }

    private boolean willNewTitleContainAntiWord(String currTitle, String toAddword) {
        String newTitle = currTitle + toAddword;
        for (StringPair pair : antonymWords) {
            if (newTitle.indexOf(pair.getKey()) >= 0 && newTitle.indexOf(pair.getValue()) >= 0) {
//                log.info("[curr pair :]" + pair);
                return true;
            }
        }
        return false;
    }

    static List<HashSet<String>> ratioWords = new ArrayList<HashSet<String>>();

    static {
        try {
            File file = new File(TMConfigs.autoDir, "ratio.txt");
            List<String> lines = FileUtils.readLines(file);
            for (String line : lines) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                HashSet<String> set = new HashSet<String>();
                String[] splits = line.split(",");
                for (String string : splits) {
                    if (StringUtils.isBlank(string)) {
                        continue;
                    }
                    set.add(string);
                }
                ratioWords.add(set);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 几个选一个的词语
     * @param cleanTitle
     * @param word
     * @return
     */
    private boolean hasRatioWord(String cleanTitle, String word) {
        for (HashSet<String> ratios : ratioWords) {
            if (!ratios.contains(word)) {
                continue;
            }

            for (String ratio : ratios) {
                if (ratio.equals(word)) {
                    continue;
                }

                if (cleanTitle.indexOf(ratio) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void tryMatch() {

    }

    static String[] badtWords = new String[] {
            "外贸", "非正品", "假货", "删", "修改", "其他", "other", "浅口", "中口", "深口", "原单", "LV", "lv", "爱玛仕",
            "军用", "军警", "军品", "弩", "死飞",
            "符咒", "冥镪", "祛病", "枪支", "阳具"
    };

    public static boolean hasBadWord(String cleanTitle) {
        if (StringUtils.isEmpty(cleanTitle)) {
            return false;
        }
        for (String word : badtWords) {
            if (cleanTitle.indexOf(word) >= 0) {
                return true;
            }
        }
        return false;
    }
}
