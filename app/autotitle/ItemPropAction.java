
package autotitle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import jdp.ApiJdpAdapter;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PlayUtil;
import autotitle.AutoTitleOption.BatchPageOption;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.CiaoStringUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

import dao.item.ItemDao;

public class ItemPropAction {

    static String[] exlucedChar = new String[] {
            "（", "）", "(", ")", "=", "#", "<", ">", "cm", "以上", "以下", "实拍", "不限", "尺", "看说明", "无里", "大陆", "中国", "分销",
            "仿", "图", "普通", "淘宝", "无", "一般", "米", "处理", "否", "不", "其他", "其她", "其它", "套餐", "中国", "同款", "高仿", "山寨", "外贸",
            "原单", "尾货", "主编", "药物"
    };

    /**
     * 羊毛 可能还需要对应羊毛衫类目
     */
    static String[] forbiddenWords = new String[] {
            "军品", "军用", "枪支", "壮阳", "阳具", "弩", "孥", "高仿", "山寨", "外贸", "原单", "尾货", "成人", "性器", "自慰", "刀具", "自主实拍图", "羊毛"
    };

    static String[] excludedKeys = new String[] {
            "ISBN编号", "区间", "规", "量", "删除", "售后", "服务", "尺寸", "尺码", "罩杯型", "家具产地", "城市", "产品规格（长x宽cm）",
            "产品重量[*克(g)/件]", "吸水性"
    };

    static Set<String> colorSet = new HashSet<String>();

    public static Set<String> getColorSet() {
        return colorSet;
    }

    public static Set<String> needEnhance = new HashSet<String>();

    static {
        for (String word : new String[] {
                "红色", "黄色", "紫色", "蓝色", "绿色", "紫色", "白色", "黑色", "橙色", "黄色", "青色", "灰色",
                "红", "黄", "紫", "蓝", "绿", "紫", "白", "黑", "橙", "黄", "青", "灰",
                "酒红色", "藏青色", "浅蓝色", "浅灰色", "粉红色", "军绿", "军绿色", "天蓝色", "荧光绿", "荧光绿色", "粉色", "酒红色",
                "酒红", "藏青", "浅蓝", "浅灰", "粉红", "军绿", "天蓝", "荧光绿", "粉", "酒红",
                "天蓝", "灰", "湖蓝", "果绿", "西红", "湖蓝", "咖啡色", "深蓝色",
                "玫红色", "花灰色", "紫红色", "蓝梅色", "湖绿色", "宝蓝色", "深蓝色",
                "玫红", "桔色", "紫红", "蓝梅", "蓝梅红", "湖绿", "宝蓝", "深蓝",
                "蓝色", "秋粉色", "湖蓝色", "紫罗兰", "卡其色", "粉红色", "褐色",
                "蓝", "秋粉", "湖蓝", "紫罗兰", "卡其", "粉红", "褐",
                "青绿色", "深灰色", "深灰色",
                "青绿", "深灰", "深灰",
                "青蓝色",
                "青蓝",
                "糖果色",
                "糖果"

        }) {
            colorSet.add(word);
        }

        for (String word : new String[] {
                "正品", "现货", "2016新款", "新款"
        }) {
            needEnhance.add(word);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ItemPropAction.class);

    public static final String TAG = "ItemPropAction";

    public static final long serialNumPid = 1632501L;

    public static Queue<IWordBase> prepareIWordBaseQueue(String[] splits, AutoTitleOption opt,
            List<String> toFormedWords)
            throws ClientException {
//            String[] splits = srcs.toArray(NumberUtil.EMPTY_STRING_ARRAY);
        Long cid = opt.getCid();

        LinkedList<IWordBase> res = new LinkedList<IWordBase>();

        Set<String> formedWords = new HashSet<String>(toFormedWords);
        formedWords.addAll(toFormedWords);
//        log.info("[raw map :]" + StringUtils.join(splits, ','));
        Map<String, IWordBase> map = new AutoWordBase(splits, cid).execute();
//        log.info("[raw map :]" + map.values());
        for (String srcStr : splits) {
            IWordBase base = map.get(srcStr);
            if (base == null) {
                base = new WordBaseBean();
                base.setWord(srcStr);
            }

            if (formedWords.contains(srcStr)) {
                base.setPv(0);
                base.setCid(0);
            } else if (colorSet.contains(srcStr)) {
                if (opt.getPageOpt().isNoColor()) {
                    continue;
                }

                /*
                 * Reduction for the color words...
                 */
                int pv = base.getPv() / 100;
                if (pv < 1) {
                    pv = 1;
                }
                base.setPv(pv);
                int click = base.getClick() / 100;
                if (click < 1) {
                    click = 1;
                }
                base.setClick(click);
            }
//            else {
//                base.setPv(-100);
//                base.setClick(-100);
//            }

            // 加强特别的几个重要属性
            if (opt.definedProps.enhanceProps.contains(srcStr)) {
                base.setPv(base.getPv() + 200000);
                base.setClick(base.getClick() + 2000);
            }

            if (needEnhance.contains(srcStr)) {
                base.setPv(base.getPv() + 200000);
                base.setClick(base.getClick() + 2000);
            }

            addToListIfNotContain(res, base);
//            res.add(base);
        }

        if (!opt.isReplaceSerial() && !StringUtils.isEmpty(opt.definedProps.serialNumber)) {
            IWordBase base = new WordBaseBean();
            base.setWord(opt.definedProps.serialNumber);
            base.setClick(5000);
            base.setPv(200000);
            base.setPrice(10000);
            addToListIfNotContain(res, base);
//            res.add(base);
        }

//        Collection<IWordBase> values = map.values();
//        for (IWordBase iWordBase : values) {
////            if (iWordBase.getPv() > 10) {
//                res.add(iWordBase);
////            }
//        }

        Collections.sort(res, CLICK_COMPARATOR);
//        } catch (ClientException e) {
//            log.warn(e.getMessage(), e);
//        }

//        log.info("[res]" + res);
        return res;
    }

    public static void addToListIfNotContain(LinkedList<IWordBase> res, IWordBase base) {
        Iterator<IWordBase> iterator = res.iterator();
        while (iterator.hasNext()) {
            IWordBase iWordBase = (IWordBase) iterator.next();
            if (!StringUtils.isEmpty(iWordBase.getWord()) && iWordBase.getWord().contains(base.getWord())) {
                return;
            } else if (!StringUtils.isEmpty(base.getWord()) && base.getWord().contains(iWordBase.getWord())) {
                iterator.remove();
                break;
            }
        }
        res.add(base);
    }

    public static ClickComparator CLICK_COMPARATOR = new ClickComparator();

    public static class ClickComparator implements Comparator<IWordBase> {
        public int compare(IWordBase o1, IWordBase o2) {
            return o2.getClick() - o1.getClick();
        }
    }

    public static class ClickCompititionComparator implements Comparator<IWordBase> {
        public int compare(IWordBase o1, IWordBase o2) {
            return getPvComptitionScore(o2) - getPvComptitionScore(o1);
        }
    }

    public static int getPvComptitionScore(IWordBase iwordBase) {
        int avgPrice = iwordBase.getPrice();
        int pv = iwordBase.getPv() / 100;
        int compitition = iwordBase.getCompetition();

        if (avgPrice <= 0 || pv <= 200 || compitition <= 0) {
            return 0;
        }

        return (int) (pv * 800f / (compitition * avgPrice));
    }

    static Set<String> forbiddenSet = new HashSet<String>();

    static {
        for (String word : forbiddenWords) {
            forbiddenSet.add(word);
        }
    }

    public static List<String> splitUserList(User user, Long numIid) {
        if (NumberUtil.isNullOrZero(numIid)) {
            return ListUtils.EMPTY_LIST;
        }
        Item item = ApiJdpAdapter.get(user).findItem(user, numIid);
        if (item == null) {
            return ListUtils.EMPTY_LIST;
        }

        return splitPropList(item.getPropsName(), item.getPropertyAlias());
    }

    public static List<String> splitPropList(String propsName, String propAlias) {
        return splitPropList(propsName, propAlias, null);
    }

    public static List<String> splitPropList(String propsName, String propAlias, BatchPageOption opt) {

        List<String> propsArr = new ArrayList<String>();
        List<PropUnit> splitProp = mergePropAlis(propsName, propAlias);
        for (StringPair pair : splitProp) {
            if ("品牌".equals(pair.getKey()) && pair.getValue().startsWith("361")) {
                propsArr.add("361");
                continue;
            }

            if ("颜色分类".equals(pair.getKey())) {
                if (opt != null && opt.isNoColor()) {
                    continue;
                }
                pair.setValue(extracChs(pair.getValue()));
            }

            if (StringUtils.isEmpty(pair.getKey()) || StringUtils.isEmpty(pair.getValue())) {
                continue;
            }
            if (pair.getKey().indexOf("区间") >= 0) {
                continue;
            }
            if (StringUtils.isNumeric(pair.getValue())) {
                continue;
            }
            if (StringUtils.isNumeric(pair.getValue().replaceAll(" ", ""))) {
                continue;
            }
            if (pair.getValue().contains("-")) {
                continue;
            }
            if (pair.getValue().contains("(")) {
                continue;
            }

            if (isKeyExcluded(pair.getKey())) {
                continue;
            }
            if (isValueExclued(pair.getValue())) {
                continue;
            }

            if (isValueFobidden(pair.getValue())) {
                continue;
            }

            if (StringUtils.isNumeric(pair.getValue()) && pair.getValue().length() <= 2) {
                continue;
            }
            try {
                if (pair.getValue().indexOf('\\') >= 0) {
                    String[] splits = StringUtils.split(pair.getValue(), '\\');
                    for (String string : splits) {
                        if (isValueExclued(string) || isValueFobidden(string)) {
                            continue;
                        }
                        propsArr.add(string);
                    }
                }
            } catch (Exception e) {
                log.error(" prop :" + pair);
                log.warn(e.getMessage(), e);

            }
            if (pair.getValue().contains("/")) {
                String[] splits = pair.getValue().split("/");
                for (String string : splits) {
                    if (isValueExclued(string) || isValueFobidden(string)) {
                        continue;
                    }
                    propsArr.add(string);
                }
            } else {
                propsArr.add(pair.getValue());
            }

//            log.info("[props arr ]" + propsArr);
        }
        return propsArr;
    }

    public static Set<String> splitProps(String propsName, String propAlias, BatchPageOption opt) {
        return new HashSet<String>(splitPropList(propsName, propAlias, opt));
    }

    private static boolean isValueFobidden(String value) {
        for (String word : forbiddenWords) {
            if (value.indexOf(word) >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isKeyExcluded(String key) {
        if (StringUtils.isEmpty(key)) {
            return true;
        }

        for (String ex : excludedKeys) {
            if (key.indexOf(ex) >= 0) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValueExclued(String value) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        int length = StringUtils.length(value);

        if (length <= 1 || length > 10) {
            return true;
        }

        if (StringUtils.isNumericSpace(value)) {
            return true;
        }
        if (length > 1 && StringUtils.isNumeric(value.substring(0, length - 1))) {
            return true;
        }
        String cleaned = StringUtils.replace(value, "\"", "");
        cleaned = StringUtils.replace(cleaned, "+", "");
        cleaned = StringUtils.replace(cleaned, "*", "");

        if (StringUtils.isNumericSpace(value)) {
            return true;
        }

        for (String s : exlucedChar) {
            if (value.indexOf(s) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Best example :  select * from jdp_tb_item where num_iid = 36334310153 limit 100\G
     * @param propsName
     * @param propAlias
     * @return
     */
    public static List<PropUnit> mergePropAlis(String propsName, String propAlias) {
        List<PropUnit> splitProp = PropUnit.build(propsName);
//        log.error("[split : ]" + splitProp);
        if (StringUtils.isEmpty(propAlias)) {
            return splitProp;
        }

        Map<Long, PropUnit> map = PropUnit.buildAlias(propAlias);
//        log.info("[map :]" + map);

        for (PropUnit propPair : splitProp) {
            if (map.containsKey(propPair.getVid())) {
                PropUnit unit = map.get(propPair.vid);
                propPair.setValue(unit.getValue());
            }
        }

        return splitProp;
    }

    public static class PropUnit extends StringPair {

        public PropUnit(long pid, long vid, String key, String value) {
            super(key, value);
            this.pid = pid;
            this.vid = vid;
        }

        long pid = 0L;

        long vid = 0L;

        public static Map<Long, PropUnit> buildAlias(String alias) {
            if (StringUtils.isEmpty(alias)) {
                return MapUtils.EMPTY_MAP;
            }
            String[] splits = buildSplits(alias);
            Map<Long, PropUnit> map = new HashMap<Long, PropUnit>();
            for (String string : splits) {
                PropUnit splitProps = splitSingleProps(string);
                if (splitProps != null && splitProps.vid > 0L) {
                    map.put(splitProps.vid, splitProps);
                }
            }
            return map;
        }

        public static List<PropUnit> build(String propsName) {
            String[] splits = buildSplits(propsName);

            List<PropUnit> res = new ArrayList<PropUnit>();
            for (String string : splits) {
                PropUnit splitProps = splitSingleProps(string);
                if (splitProps != null) {
                    res.add(splitProps);
                }
            }
            return res;
        }

        private static String[] buildSplits(String propsName) {
            String[] splits = StringUtils.split(propsName, ';');
            if (ArrayUtils.isEmpty(splits)) {
                return NumberUtil.EMPTY_STRING_ARRAY;
            }
            return splits;
        }

        public static PropUnit splitSingleProps(String propName) {
            propName = CiaoStringUtil.lazyReplace(propName, "#scln#", ";");

            String[] split = StringUtils.split(propName, ':');
            if (split.length < 3) {
                return null;
            } else if (split.length == 3) {
                long pid = NumberUtil.parserLong(split[0], -1L);
                long vid = NumberUtil.parserLong(split[1], -1L);

                String pText = StringUtils.EMPTY;
                String vText = split[2];
                vText = CiaoStringUtil.lazyReplace(vText, "#cln#", ":");
                return new PropUnit(pid, vid, pText, vText);
            } else {
                long pid = NumberUtil.parserLong(split[0], -1L);
                long vid = NumberUtil.parserLong(split[1], -1L);
                String pText = split[2];
                String vText = split[3];

                pText = CiaoStringUtil.lazyReplace(pText, "#cln#", ":");
                vText = CiaoStringUtil.lazyReplace(vText, "#cln#", ":");

                return new PropUnit(pid, vid, pText, vText);
            }
        }

        @Override
        public String toString() {
            return "PropUnit [pid=" + pid + ", vid=" + vid + ", super=" + super.toString();
        }

        public long getPid() {
            return pid;
        }

        public void setPid(long pid) {
            this.pid = pid;
        }

        public long getVid() {
            return vid;
        }

        public void setVid(long vid) {
            this.vid = vid;
        }

    }

    public static List<PropUnit> mergePropAlis(Item item) {
        return mergePropAlis(item.getPropsName(), item.getPropertyAlias());
    }

    public static String fetchSerialNum(Item item) {
        if (item == null) {
            return StringUtils.EMPTY;
        }
        List<PropUnit> list = mergePropAlis(item.getPropsName(), item.getPropertyAlias());
        if (CommonUtils.isEmpty(list)) {
            return null;
        }
//        log.info("[list:]" + list);
        for (PropUnit propUnit : list) {
            if ("货号".equals(propUnit.getKey())) {
                return propUnit.getValue();
            }
            if (propUnit.getPid() == serialNumPid) {
                return propUnit.getValue();
            }
            if ("型号".equals(propUnit.getKey())) {
                return propUnit.getValue();
            }
        }

        return null;
    }

    public static String extracChs(String target) {
        if (StringUtils.isEmpty(target)) {
            return StringUtils.EMPTY;
        }

        int length = target.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String str = target.substring(i, i + 1);
            if (str.matches(ChsCharsUtil.chsReg)) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    @JsonAutoDetect
    public static class SkuVidPrices implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty
        Long skuId = 0L;

        @JsonProperty
        List<String> vNames = ListUtils.EMPTY_LIST;

        /**
         * This should be 分
         */
        @JsonProperty
        int price;

        @JsonProperty
        int quantity;

        public SkuVidPrices(Sku sku, List<String> vNames2) {
            this.skuId = sku.getSkuId();
            this.price = NumberUtil.getIntFromPrice(sku.getPrice());
            this.quantity = sku.getQuantity() == null ? 0 : sku.getQuantity().intValue();
            this.vNames = vNames2;
        }

        @Override
        public String toString() {
            return "SkuVidPrices [skuId=" + skuId + ", vNames=" + vNames + ", price=" + price + ", quantity="
                    + quantity + "]";
        }

        /**
         * this item must contain sku..
         * Later add test codes for numIid 36334310153
         * @param item
         * @return 
         */
        public static List<SkuVidPrices> parseMultiplePrices(Item item) {
            if (item == null) {
                log.warn(" no item pass in:");
                return ListUtils.EMPTY_LIST;
            }

            log.info("[item:]" + PlayUtil.genPrettyGson().toJson(item));

            List<Sku> skus2 = item.getSkus();
            if (CommonUtils.isEmpty(skus2)) {
                return ListUtils.EMPTY_LIST;
            }
            Set<String> rawPrices = new HashSet<String>();
            for (Sku sku : skus2) {
                String price2 = sku.getPrice();
                if (price2 != null) {
                    rawPrices.add(price2);
                }
            }
            log.info("raw prices :" + rawPrices);
            if (rawPrices.size() <= 1) {
                return ListUtils.EMPTY_LIST;
            }

            List<PropUnit> pu = ItemPropAction.mergePropAlis(item.getPropsName(), item.getPropertyAlias());
            Map<Long, String> vidToValue = new HashMap<Long, String>();

            for (PropUnit propUnit : pu) {
                long vid = propUnit.getVid();
                String value = propUnit.getValue();
                vidToValue.put(vid, value);
            }

            List<Sku> skus = item.getSkus();
            List<SkuVidPrices> skuVidPricesList = new ArrayList<SkuVidPrices>();
            for (Sku sku : skus) {
                List<String> vNames = new ArrayList<String>();
                String names = sku.getPropertiesName();
                List<PropUnit> subUnits = ItemPropAction.mergePropAlis(names, null);
                for (PropUnit propUnit : subUnits) {
                    String string = vidToValue.get(propUnit.getVid());
                    if (string != null) {
                        vNames.add(string);
                    } else {
                        vNames.add(propUnit.getValue());
                    }
                }

                skuVidPricesList.add(new SkuVidPrices(sku, vNames));
            }
            return skuVidPricesList;

        }
    }

    public static String parseBrandProp(String props) {
        int brandStartIndex = props.indexOf("20000:");
        if (brandStartIndex < 0) {
            return null;
        }
        int endIndex = props.indexOf(';', brandStartIndex);
        if (endIndex < 0) {
            endIndex = props.length();
        }
        String brandSpan = props.substring(brandStartIndex, endIndex);

        return brandSpan;
    }

    public static String getOneBrandVidDesc(User user, Long cid) {

        List<ItemPlay> items = null;
        int limit = 10;
        items = ItemDao.findByUserAndCat(user.getId(), cid, limit);
        if (CommonUtils.isEmpty(items)) {
            items = ItemDao.findByUserId(user.getId(), limit);
        }

        List<Long> ids = new ArrayList<Long>();
        for (ItemPlay itemPlay : items) {
            ids.add(itemPlay.getNumIid());
        }

        List<Item> tbItems = new ItemApi.MultiItemsListGet(user.getSessionKey(), ids, true).call();
        for (Item item : tbItems) {
            String brandSpan = parseBrandProp(item.getProps());
            log.info("[parse brand:]" + item.getCid());
            log.info("[parse brand:]" + brandSpan);
            log.error("[parse brand:]" + item.getPropsName());

//            if (brandSpan != null) {
//                return brandSpan;
//            }
        }

        return StringUtils.EMPTY;
    }
}
