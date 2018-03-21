
package autotitle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.item.ItemCatPlay;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import utils.TaobaoUtil;
import autotitle.ItemPropAction.PropUnit;

import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.Item;

/**
 * only for bingding....
 * @author zrb
 *
 */
public class AutoTitleOption extends Model {

    static class PropDefinedValue {

        String serialNumber = StringUtils.EMPTY;

        String brandName = StringUtils.EMPTY;

        String collarStyle = StringUtils.EMPTY;
        
        /**
         * 产品名称
         */
        String productName = StringUtils.EMPTY;

        List<String> brandNameList = new ArrayList<String>();

        Set<String> colorSet = new HashSet<String>();

        boolean isBigElectricAppliance = false;

        BatchPageOption opt = null;
        
        Set<String> enhanceProps = new HashSet<String>();

        public PropDefinedValue() {
        }

        public void setPropsName(String propsName, String propAlias) {

//            log.info(format("setPropsName:propsName, propAlias".replaceAll(", ", "=%s, ") + "=%s", propsName, propAlias));
            List<PropUnit> splitProp = ItemPropAction.mergePropAlis(propsName, propAlias);
            Iterator<PropUnit> it = splitProp.iterator();
            while (it.hasNext()) {
                StringPair pair = it.next();
                
                if (pair == null || StringUtils.isEmpty(pair.getKey())) {
                    continue;
                }
                
//                if (this.opt != null && this.opt.isNoColor() && "颜色分类".equals(pair.getKey())) {
                if ("颜色分类".equals(pair.getKey())) {
                    this.colorSet.add(pair.getValue());
                    continue;
                }

//                log.info("[key:]" + pair.getKey() + " : " + pair.getValue());

                if ("货号".equals(pair.getKey()) || "ISBN编号".equals(pair.getKey()) || "款号".equals(pair.getKey()) || "型号".equals(pair.getKey())) {
                    this.serialNumber = pair.getValue();
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "机");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "免");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "女");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "满");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "大");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "富");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "国");
                    this.serialNumber = TaobaoUtil.fastRemove(this.serialNumber, "实");
                    continue;
                }

                if ("领型".equals(pair.getKey())) {
                    this.collarStyle = pair.getValue();
                    addToEnhanceProps(pair.getValue());
                    continue;
                }

                if ("产品名称".equals(pair.getKey())) {
                    this.productName = pair.getValue();
                    addToEnhanceProps(pair.getValue());
                    continue;
                }

                if ("洗衣机品牌".equals(pair.getKey())) {
                    this.isBigElectricAppliance = true;
                    addToEnhanceProps(pair.getValue());
                    continue;
                }

                if ("品牌".equals(pair.getKey())) {
//                    log.info("[brand name :]" + brandName);
                    this.brandName = pair.getValue();
                    if (brandName.contains("/")) {
                        String[] splits = brandName.split("/");
                        for (String split : splits) {
                            this.brandNameList.add(split.toLowerCase());
                        }
                    } else {
                        this.brandNameList.add(brandName.toLowerCase());
                    }
                    addToEnhanceProps(pair.getValue());
                    continue;
                }
                
                if ("风格".equals(pair.getKey())) {
                    addToEnhanceProps(pair.getValue());
                    continue;
                }
                
                if ("适用场景".equals(pair.getKey())) {
                    addToEnhanceProps(pair.getValue());
                    continue;
                }
                
                if ("图案".equals(pair.getKey())) {
                    addToEnhanceProps(pair.getValue());
                    continue;
                }
                
                if ("材质".equals(pair.getKey())) {
                    addToEnhanceProps(pair.getValue());
                    continue;
                }
                
                if ("款式".equals(pair.getKey())) {
                    addToEnhanceProps(pair.getValue());
                    continue;
                }

            }

            log.warn("[this  serail name:]" + this.serialNumber);
        }

        public PropDefinedValue(BatchPageOption opt, String propsName, String propAlias) {
            this.opt = opt;
            setPropsName(propsName, propAlias);
        }

        public void addToEnhanceProps(String prop) {
            if (StringUtils.isEmpty(prop)) {
                return;
            }

            if (prop.contains("/") || prop.contains(" ")) {
                String[] splits = prop.split("\\s|/");
                for (String split : splits) {
                    if (!StringUtils.isBlank(split)  && !split.contains("其他") && !split.contains("其它")) {
                        enhanceProps.add(split);
                    }
                }
            } else {
                if (!StringUtils.isBlank(prop)  && !prop.contains("其他") && !prop.contains("其它")) {
                    enhanceProps.add(prop);
                }
            }
        }
        
        @Override
        public String toString() {
            return "PropDefinedValue [serialNumber=" + serialNumber + ", brandName=" + brandName + "]";
        }

    }

    static final Logger log = LoggerFactory.getLogger(AutoTitleOption.class);

    public static final String TAG = "AutoTitleOption";

//    User user;

    Long numIid;

    String rawTitle;

    String sellerCidNames;

    String propsName;

    String propAlias = StringUtils.EMPTY;

    Set<String> propSet = SetUtils.EMPTY_SET;

    int chsLength = 0;

    boolean replaceSerial = true;

    boolean keepBrand = true;

    String fixedStart = StringUtils.EMPTY;

    String fixedEnd = StringUtils.EMPTY;

    Set<String> fixedStartSet = new HashSet<String>();

    Long cid;

    Long level1Cid;

    Set<String> mustExcluded = new HashSet<String>();

    PropDefinedValue definedProps = new PropDefinedValue();

    boolean addPromoteWords = true;

    private String catName = StringUtils.EMPTY;

    private BatchPageOption pageOpt = null;

    public BatchPageOption getPageOpt() {
        return pageOpt;
    }

    public void setPageOpt(BatchPageOption pageOpt) {
        this.pageOpt = pageOpt;
    }

    public AutoTitleOption(Item item, BatchPageOption pageOpt) {
//        this.user = user;
        this.numIid = item.getNumIid();
        this.rawTitle = item.getTitle();

        log.info("[now raw title:]" + this.rawTitle);
        if (pageOpt == null) {
            pageOpt = new BatchPageOption();
        }

        this.pageOpt = pageOpt;
        this.fixedStart = pageOpt.getFixedStart();

        if (!StringUtils.isBlank(this.fixedStart)) {
            try {
//                fixedStart = TaobaoUtil.fastRemove(fixedStart, " ");
                List<String> execute = new AutoSplit(this.fixedStart, ListUtils.EMPTY_LIST).execute();
                this.fixedStartSet.addAll(execute);
            } catch (ClientException e) {
                log.warn(e.getMessage(), e);
                this.fixedStartSet.add(this.fixedStart);
            }
        }

        this.chsLength = ChsCharsUtil.length(rawTitle);
        this.propsName = item.getPropsName();
//        log.error(" prop names :" + propsName);

        this.propAlias = item.getPropertyAlias();
        this.propSet = ItemPropAction.splitProps(propsName, propAlias, pageOpt);
        if (this.definedProps != null && this.definedProps.serialNumber != null) {
            this.propSet.remove(this.definedProps.serialNumber);
        }

        this.cid = item.getCid();
        this.level1Cid = ItemCatPlay.findTopCid(cid);


        this.catName = ItemCatPlay.findNameByCid(cid);
        this.definedProps = new PropDefinedValue(pageOpt, propsName, propAlias);
        log.error("[prop arr :]" + StringUtils.join(this.propSet, ','));
        log.error("[prop arr :]" + this.definedProps);

//        if (this.definedProps.isBigElectricAppliance && this.fixedStart.length() < 2) {
//            this.fixedStart = this.fixedStart + this.definedProps.productName;
//        }

//        buildPunishWordSet();

        buildCidNames(item.getSellerCids());
        appendTitleIfTooShort();

        buildPageeOpt(pageOpt);

//        List<String> tempPropArr = new ArrayList<String>();

//        log.warn("item cid :" + item.getCid());
    }

    private void buildPageeOpt(BatchPageOption pageOpt) {
        if (pageOpt == null) {
            return;
        }

        this.keepBrand = pageOpt.keepBrand;
        if (!keepBrand) {
            for (String name : this.definedProps.brandNameList) {
                this.mustExcluded.add(name.toUpperCase());
                this.mustExcluded.add(name.toLowerCase());
                this.mustExcluded.add(name);
            }
        }

        this.replaceSerial = !pageOpt.keepSerial;
        if (replaceSerial) {
            this.mustExcluded.add(this.definedProps.serialNumber);
            this.mustExcluded.add(this.definedProps.serialNumber.toLowerCase());
            this.mustExcluded.add(this.definedProps.serialNumber.toUpperCase());
        }

        this.addPromoteWords = pageOpt.toAddPromote;
        if (StringUtils.isNotBlank(pageOpt.getMustExcluded())) {
            String[] splits = pageOpt.getMustExcluded().split(" ");
            this.mustExcluded = new HashSet<String>();
            for (String string : splits) {
                if (StringUtils.isBlank(string)) {
                    continue;
                }
                mustExcluded.add(string);
            }
        }

//        log.error(" must exluced:" + mustExcluded);
    }

    private void appendTitleIfTooShort() {
        // Append The content for the title...
//        log.info("[prop length:]" + propArr.length);
        if (this.propSet.size() < 6 && this.chsLength < 30 && StringUtils.length(this.rawTitle) < 22) {
            if (!StringUtils.isEmpty(sellerCidNames) && sellerCidNames.length() <= 3) {
                this.rawTitle += this.sellerCidNames;
            }
            if (this.cid != null) {

                if (AutoTitleEngine.hasBadWord(catName)) {
                    return;
                }

                log.info("[catname :]" + catName);
                catName = AutoTitleEngine.trimAllPunctuation(catName);
                log.info("[cname :]" + catName);
                if (!StringUtils.isEmpty(catName) && catName.length() <= 3) {
                    this.rawTitle += catName;
                }
            }
        }
//        log.info("[rawtitle :]" + rawTitle);
    }

    private void buildCidNames(String cids) {
//        log.info("[for cids : ]" + cids);
        if (StringUtils.isEmpty(cids)) {
            return;
        }
        if (StringUtils.equals("-1", cids)) {
            return;
        }

        String[] rawcids = cids.split(",");

        if (ArrayUtils.isEmpty(rawcids)) {
            return;
        }
        int num = rawcids.length;
        List<Long> longCids = new ArrayList<Long>();

        for (int i = 0; i < num; i++) {
            if (StringUtils.isEmpty(rawcids[i])) {
                continue;
            }

            long cidLong = Long.parseLong(rawcids[i]);
            if (cidLong > 0L) {
                longCids.add(cidLong);
            }
        }
//        log.info("[long cids : ]" + longCids);

//        Collection<SellerCat> list = CountSellerCatCache.get().getByUser(user).keySet();
//        if (CommonUtils.isEmpty(list)) {
//            return;
//        }
//
//        StringBuilder sb = new StringBuilder();
//        for (Long long1 : longCids) {
//            for (SellerCat sellerCat : list) {
//                if (long1 == sellerCat.getCid().longValue()) {
//                    String name = sellerCat.getName();
//                    if (name.length() > 3) {
//                        continue;
//                    }
//
//                    String finalName = name;
//                    sb.append(finalName);
//                }
//            }
//        }
//
//        this.sellerCidNames = AutoTitleEngine.trimAllPunctuation(sb);

//        log.info("[build cid name ;]" + sellerCidNames);
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public String getRawTitle() {
        return rawTitle;
    }

    public void setRawTitle(String rawTitle) {
        this.rawTitle = rawTitle;
    }

    public String getSellerCidNames() {
        return sellerCidNames;
    }

    public String getPropsName() {
        return propsName;
    }

    public Set<String> getPropSet() {
        return propSet;
    }

    public void setPropSet(Set<String> propSet) {
        this.propSet = propSet;
    }

    public int getChsLength() {
        return chsLength;
    }

    public void setChsLength(int chsLength) {
        this.chsLength = chsLength;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public boolean isReplaceSerial() {
        return replaceSerial;
    }

    public void setReplaceSerial(boolean replaceSerial) {
        this.replaceSerial = replaceSerial;
    }

    public String getSerial() {
        return definedProps.serialNumber;
    }

    public String getBrandName() {
        return definedProps.brandName;
    }

    public List<String> getBrandNameList() {
        return definedProps.brandNameList;
    }

    public boolean isKeepBrand() {
        return keepBrand;
    }

    public void setKeepBrand(boolean keepBrand) {
        this.keepBrand = keepBrand;
    }

    @JsonAutoDetect
    public static class BatchPageOption {
        @JsonProperty
        boolean allSale = true;

        @JsonProperty
        boolean keepBrand = true;

        @JsonProperty
        boolean keepSerial = true;

        @JsonProperty
        String fixedStart = StringUtils.EMPTY;

        @JsonProperty
        String mustExcluded = StringUtils.EMPTY;

        @JsonProperty
        boolean toAddPromote = true;

        @JsonProperty
        long sellerCatId = 0L;

        @JsonProperty
        long itemCatId = 0L;

        @JsonProperty
        int status = 0;

        @JsonProperty
        int recMode = 0;
        
        /****新的条件*****/
        private int startScore = 0;
        private int endScore = 0;
        private String title;
        private boolean newSearchRule = false;

        public BatchPageOption(boolean allSale, boolean keepBrand, boolean keepSerial) {
            super();
            this.allSale = allSale;
            this.keepBrand = keepBrand;
            this.keepSerial = keepSerial;
        }

        public BatchPageOption() {
            super();
        }

        public boolean isAllSale() {
            return allSale;
        }

        

        @Override
        public String toString() {
            return "BatchPageOption [allSale=" + allSale + ", keepBrand="
                    + keepBrand + ", keepSerial=" + keepSerial
                    + ", fixedStart=" + fixedStart + ", mustExcluded="
                    + mustExcluded + ", toAddPromote=" + toAddPromote
                    + ", sellerCatId=" + sellerCatId + ", itemCatId="
                    + itemCatId + ", status=" + status + ", recMode=" + recMode
                    + ", startScore=" + startScore + ", endScore=" + endScore
                    + ", title=" + title + ", newSearchRule=" + newSearchRule
                    + ", replaceNumber=" + replaceNumber + ", repalceAlpha="
                    + repalceAlpha + ", repalcePunctuation="
                    + repalcePunctuation + ", repalceSpace=" + repalceSpace
                    + ", repalceOrigin=" + repalceOrigin + ", noColor="
                    + noColor + ", noNumber=" + noNumber + "]";
        }

        public boolean isToAddPromote() {
            return toAddPromote;
        }

        public void setToAddPromote(boolean toAddPromote) {
            this.toAddPromote = toAddPromote;
        }

        public void setAllSale(boolean allSale) {
            this.allSale = allSale;
        }

        public boolean isKeepBrand() {
            return keepBrand;
        }

        public void setKeepBrand(boolean keepBrand) {
            this.keepBrand = keepBrand;
        }

        public boolean isKeepSerial() {
            return keepSerial;
        }

        public void setKeepSerial(boolean keepSerial) {
            this.keepSerial = keepSerial;
        }

        public String getFixedStart() {
            return fixedStart;
        }

        public void setFixedStart(String fixedStart) {
            this.fixedStart = fixedStart;
        }

        public String getMustExcluded() {
            return mustExcluded;
        }

        public void setMustExcluded(String mustExcluded) {
            this.mustExcluded = mustExcluded;
        }

        public long getSellerCatId() {
            return sellerCatId;
        }

        public void setSellerCatId(long sellerCatId) {
            this.sellerCatId = sellerCatId;
        }

        public long getItemCatId() {
            return itemCatId;
        }

        public void setItemCatId(long itemCatId) {
            this.itemCatId = itemCatId;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getRecMode() {
            return recMode;
        }

        public void setRecMode(int recMode) {
            this.recMode = recMode;
        }

        @JsonProperty
        boolean replaceNumber = false;

        @JsonProperty
        boolean repalceAlpha = false;

        @JsonProperty
        boolean repalcePunctuation = false;

        @JsonProperty
        boolean repalceSpace = false;

        @JsonProperty
        boolean repalceOrigin = true;

        @JsonProperty
        boolean noColor = false;

        @JsonProperty
        boolean noNumber = false;

        public static BatchPageOption parseByJson(String json) {

            try {
                JSONObject obj = new JSONObject(json);
                if (!obj.has("noColor")) {
                    obj.put("noColor", false);
                }
                if (!obj.has("noNumber")) {
                    obj.put("noNumber", false);
                }
                if (!obj.has("replaceNumber")) {
                    obj.put("replaceNumber", false);
                }
                if (!obj.has("repalceAlpha")) {
                    obj.put("repalceAlpha", false);
                }
                if (!obj.has("repalcePunctuation")) {
                    obj.put("repalcePunctuation", false);
                }
                if (!obj.has("repalceSpace")) {
                    obj.put("repalceSpace", false);
                }
                if (!obj.has("repalceOrigin")) {
                    obj.put("repalceOrigin", true);
                }
                
                /*******新的条件*********/
                if (!obj.has("startScore")) {
                    obj.put("startScore", 0);
                }
                if (!obj.has("endScore")) {
                    obj.put("endScore", 0);
                }
                if (!obj.has("title")) {
                    obj.put("title", "");
                }
                if (!obj.has("newSearchRule")) {
                    obj.put("newSearchRule", false);
                }

//                if (obj.has("replaceNumber")) {
//                    obj.remove("replaceNumber");
//                }
//                if (obj.has("repalceAlpha")) {
//                    obj.remove("repalceAlpha");
//                }
//                if (obj.has("repalcePunctuation")) {
//                    obj.remove("repalcePunctuation");
//                }
//                if (obj.has("repalceSpace")) {
//                    obj.remove("repalceSpace");
//                }

                log.info("[obj:]" + obj.toString());
                BatchPageOption opt = JsonUtil.toObject(obj.toString(), BatchPageOption.class);

                return opt;
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return null;
            }
        }

        public boolean isReplaceNumber() {
            return replaceNumber;
        }

        public void setReplaceNumber(boolean replaceNumber) {
            this.replaceNumber = replaceNumber;
        }

        public boolean isRepalceAlpha() {
            return repalceAlpha;
        }

        public void setRepalceAlpha(boolean repalceAlpha) {
            this.repalceAlpha = repalceAlpha;
        }

        public boolean isRepalcePunctuation() {
            return repalcePunctuation;
        }

        public void setRepalcePunctuation(boolean repalcePunctuation) {
            this.repalcePunctuation = repalcePunctuation;
        }

        public boolean isRepalceSpace() {
            return repalceSpace;
        }

        public void setRepalceSpace(boolean repalceSpace) {
            this.repalceSpace = repalceSpace;
        }

        public boolean isRepalceOrigin() {
            return repalceOrigin;
        }

        public void setRepalceOrigin(boolean repalceOrigin) {
            this.repalceOrigin = repalceOrigin;
        }

        public boolean isNoColor() {
            return noColor;
        }

        public void setNoColor(boolean noColor) {
            this.noColor = noColor;
        }

        public boolean isNoNumber() {
            return noNumber;
        }

        public void setNoNumber(boolean noNumber) {
            this.noNumber = noNumber;
        }

        public int getStartScore() {
            return startScore;
        }

        public void setStartScore(int startScore) {
            this.startScore = startScore;
        }

        public int getEndScore() {
            return endScore;
        }

        public void setEndScore(int endScore) {
            this.endScore = endScore;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isNewSearchRule() {
            return newSearchRule;
        }

        public void setNewSearchRule(boolean newSearchRule) {
            this.newSearchRule = newSearchRule;
        }

        
        
    }

    public String getPropAlias() {
        return propAlias;
    }

    public void setPropAlias(String propAlias) {
        this.propAlias = propAlias;
    }

    public String getFixedStart() {
        return fixedStart;
    }

    public void setFixedStart(String fixedStart) {
        this.fixedStart = fixedStart;
    }

    public String getFixedEnd() {
        return fixedEnd;
    }

    public void setFixedEnd(String fixedEnd) {
        this.fixedEnd = fixedEnd;
    }

    public Set<String> getMustExcluded() {
        return mustExcluded;
    }

    public void setMustExcluded(Set<String> mustExcluded) {
        this.mustExcluded = mustExcluded;
    }

    public PropDefinedValue getDefinedProps() {
        return definedProps;
    }

    public void setDefinedProps(PropDefinedValue definedProps) {
        this.definedProps = definedProps;
    }

    public void setSellerCidNames(String sellerCidNames) {
        this.sellerCidNames = sellerCidNames;
    }

    public void setPropsName(String propsName) {
        this.propsName = propsName;
    }

    public boolean isAddPromoteWords() {
        return addPromoteWords;
    }

    public void setAddPromoteWords(boolean addPromoteWords) {
        this.addPromoteWords = addPromoteWords;
    }

    private void buildPunishWordSet() {

    }

    Set<String> willBePunishWordsSet = new HashSet<String>();

    public boolean willBeInPunish(String originTitle, String toAppenWord) {
        String newTitle = originTitle + toAppenWord;
        for (String target : willBePunishWordsSet) {
            if (newTitle.indexOf(target) >= 0) {
                return true;
            }
        }

        return false;
    }

    static Set<String> noCollarSet = new HashSet<String>();

    static Set<String> collarSet = new HashSet<String>();
    static {
        for (String word : new String[] {
                "立领", "翻领", "立领", "立领", "翻驳领", "帽领", "结带领", "花式领"
        }) {
            collarSet.add(word);
        }
        for (String word : new String[] {
                "其他", "无领"
        }) {
            noCollarSet.add(word);
        }

    }

    public boolean isAllowed(String cleanTitle, String word) {
        if (StringUtils.isBlank(word)) {
            return true;
        }
        if (!this.keepBrand) {
            for (String split : this.definedProps.brandNameList) {
                if (split.contains(word)) {
                    return false;
                }
            }
        }

        if (this.replaceSerial) {
            if (this.definedProps.serialNumber.contains(word.toLowerCase())) {
                return false;
            }
        }

        /*
         * 检测一下翻领
         */
        if (noCollarSet.contains(this.definedProps.collarStyle) && collarSet.contains(word)) {
            return false;
        }

        if (this.pageOpt.isNoNumber() && StringUtils.isNumeric(word)) {
            return false;
        }
        if (this.pageOpt.isNoColor() && ItemPropAction.getColorSet().contains(word)) {
            return false;
        }

        return true;
    }

    public Long getLevel1Cid() {
        return level1Cid;
    }

    public void setLevel1Cid(Long level1Cid) {
        this.level1Cid = level1Cid;
    }

    
}
