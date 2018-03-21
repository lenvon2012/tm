
package titleDiag;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Transient;

import models.item.ItemCatPlay;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import actions.industry.IndustryDelistGetAction;
import autotitle.AutoSplit;
import autotitle.AutoWordBase;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.api.WidAPIs.WordBaseAPI;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.word.WordCountBean;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.commons.ClientException;

@JsonAutoDetect
public class DiagResult implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(DiagResult.class);

    public static final String TAG = "DiagResult";

    private static final long serialVersionUID = 1L;

    public static String[] promoteWordsArr = new String[] {
            //            "促销", "清仓", "秒杀", "包邮", "特价", "送", "换季", "正品", "试用", "代购", "如假包换", "假一陪十", "行货", "正品", "质保", "商城", "皇冠",
//            "窜货", "金冠", "甩卖", "正版", "仅此", "货到付款", "如假包换", "现货", "限量", "暴亏", "成本", "正品", "联保", "超值", "绝杀", "秒冲", "新款",
//            "新品", "货到付款", "如假包换", "现货", "限量", "冲钻特价", "厂家", "直销", "疯抢", "限时", "新款上市", "亏本", "专柜正品", "满就送", "批发", "爆款",
//            "国庆", "双节"

            "送", "促销", "清仓", "秒杀", "特价", "正品", "疯抢", "试用", "代购", "行货", "质保", "窜货", "甩卖", "正版", "质检", "折扣", "免费", "省钱",
            "现货", "限量", "暴亏", "联保", "超值", "绝杀", "秒冲", "新款", "新品", "冲钻", "优惠", "热卖", "批发", "原装", "全新", "打折", "降价", "返现",
            "爆款", "国庆", "双节", "特价", "限时", "亏本", "满就送", "双十一", "跳楼价", "送积分", "成本价", "低价", "送红包", "优惠劵", "优惠价", "跳楼处理",
            "月销千款", "专柜验货", "质量保证", "厂家直销", "收藏有礼", "如假包换", "假一赔十", "如假包换", "新款上市", "专柜正品", "货到付款", "如假包换", "货到付款",
            "无条件退换"

    };

    public static Set<String> promotes = new HashSet<String>();

    static {
        for (String s : promoteWordsArr) {
            promotes.add(s);
        }
    }

    public static void reInit(String[] promoteWords) {
        promoteWordsArr = promoteWords;
        promotes.clear();
        for (String string : promoteWords) {
            promotes.add(string);
        }
    }

    public static void merge(String[] promoteWords) {
        for (String string : promoteWords) {
            promotes.add(string);
        }
        promoteWordsArr = promotes.toArray(NumberUtil.EMPTY_STRING_ARRAY);
    }

    @JsonProperty
    long numIid = 0L;

    @JsonProperty
    Long cid = 0L;

    // 这个是宝贝类目的标准属性名
    @JsonProperty
    String cidProps = StringUtils.EMPTY;
   
    // 这个是宝贝详情页已经填写的属性名， 需要跟cidProps比较，计算出没有填写的属性名
    @JsonProperty
    String itemProps = StringUtils.EMPTY;
    
    @JsonProperty
    String catName = StringUtils.EMPTY;
    
    @JsonProperty
    double price = 0d;

    @JsonProperty
    String picPath = StringUtils.EMPTY;

    @JsonProperty
    int spaceNum = 0;

    @JsonProperty
    int wordLength = 0;

    @JsonProperty
    String promoteWords = StringUtils.EMPTY;

    Set<String> promoteWordSet = new HashSet<String>();

    @JsonProperty
    int lowPvWordCount = 0;

    @JsonProperty
    String lowPvWords = StringUtils.EMPTY;

    @JsonProperty
    int mediumPvWordCount = 0;

    @JsonProperty
    String mediumPvWords = StringUtils.EMPTY;

    @JsonProperty
    int hotPvWordCount = 0;

    @JsonProperty
    String hotPvWords = StringUtils.EMPTY;

    @JsonProperty
    int engPunctuationNum = 0;

    @JsonProperty
    int chsPunctuationNum = 0;

    @JsonProperty
    int score = 0;

    @Transient
    boolean isTradeInited;

    @Transient
    boolean isOptimised;
    
    @Transient
    long created;

    @Transient
    Integer tradeCount;
    
    @Transient
    private long delistTime;//下架时间
    
    
    @SuppressWarnings("unchecked")
    @JsonProperty
    public Map<String, Integer> dumpCount = MapUtils.EMPTY_MAP;

    @SuppressWarnings("unchecked")
    @JsonProperty
    public Set<String> props = SetUtils.EMPTY_SET;

    @JsonProperty
    public List<WordCountBean> searchCount;

    int lowPv = 0;

    int hotPv = 0;

    @JsonProperty
    int pv = 0;

    @JsonProperty
    String title = StringUtils.EMPTY;

    @JsonProperty
    double improveSpace = 1.0d;

    public DiagResult(String title) {
        super();
        this.title = title;
    }
    
    @Transient
    private Long titleCatPv = 0L;
    
    // 两位小数
    @Transient
    private String titleCatPrePv = "0.00";
    
    private List<String> simpleParserResult;

    private List<String> rawPropsList;

    public DiagResult(Long numIid, double price, String title, String path, List<String> propsList,
            List<String> simpleParseResults, List<String> level2Results, Integer tradeCount, long delistTime) throws ClientException {
        this.numIid = numIid;
        this.price = price;
        this.title = title;
        this.picPath = path;
        this.dumpCount = findCountMoreThan(appendVerbCount(simpleParseResults), 1);
        this.wordLength = ChsCharsUtil.length(title);
        this.spaceNum = ChsCharsUtil.spaceNum(title);
        this.engPunctuationNum = ChsCharsUtil.engPunctuationNum(title);
        this.chsPunctuationNum = ChsCharsUtil.chsPunctuationNum(title);
        this.simpleParserResult = simpleParseResults;
        this.rawPropsList = propsList;
        this.tradeCount = tradeCount;
        this.delistTime = delistTime;
        
        buildPromoteWordsList(title, simpleParseResults);

        try {
            buildWordInfo(level2Results);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        //    buildProps(simpleParseResults, propsList);
        buildProps(title, propsList);
        buildScore();
    }
    
    public DiagResult(Long numIid, double price, String title, String path, List<String> propsList,
            List<String> simpleParseResults, List<String> level2Results, Integer tradeCount,
            long delistTime, String cidProps, List<String> itemProps) throws ClientException {
        this.numIid = numIid;
        this.price = price;
        this.title = title;
        this.picPath = path;
        this.dumpCount = findCountMoreThan(appendVerbCount(simpleParseResults), 1);
        this.wordLength = ChsCharsUtil.length(title);
        this.spaceNum = ChsCharsUtil.spaceNum(title);
        this.engPunctuationNum = ChsCharsUtil.engPunctuationNum(title);
        this.chsPunctuationNum = ChsCharsUtil.chsPunctuationNum(title);
        this.simpleParserResult = simpleParseResults;
        this.rawPropsList = propsList;
        this.tradeCount = tradeCount;
        this.delistTime = delistTime;
        this.cidProps = cidProps;
        if(!CommonUtils.isEmpty(itemProps)) {
        	this.itemProps = StringUtils.join(itemProps, ",");
        }
        buildPromoteWordsList(title, simpleParseResults);

        try {
            buildWordInfo(level2Results);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        //    buildProps(simpleParseResults, propsList);
        buildProps(title, propsList);
        buildScore();
    }
    
	public DiagResult(Long numIid, double price, String title, String path, Integer tradeCount, long delistTime) {
		this.numIid = numIid;
		this.price = price;
		this.title = title;
		this.picPath = path;
		this.wordLength = ChsCharsUtil.length(title);
		this.spaceNum = ChsCharsUtil.spaceNum(title);
		this.engPunctuationNum = ChsCharsUtil.engPunctuationNum(title);
		this.chsPunctuationNum = ChsCharsUtil.chsPunctuationNum(title);
		this.tradeCount = tradeCount;
		this.delistTime = delistTime;
		
		buildScore();
	}

/*
    private void buildProps(List<String> simpleParseResults, List<String> propsList) {
        if (CommonUtils.isEmpty(simpleParseResults) || CommonUtils.isEmpty(propsList)) {
            return;
        }
        this.props = new ArrayList<String>();
        Set<String> segs = new HashSet<String>();
        for (String string : simpleParseResults) {
            if (string.length() > 1) {
                segs.add(string);
            }
        }
    }
*/
    private void buildProps(String title, List<String> propsList) {
        if (StringUtils.isEmpty(title) || CommonUtils.isEmpty(propsList)) {
            return;
        }
        this.props = new HashSet<String>();

        for (String string : propsList) {
            if (title.indexOf(string) >= 0) {
                this.props.add(string);
            }
        }
    }

    public void buildScore() {

        this.score = 10;
//
//        int size = promoteWordSet.size();
//        if (size == 0) {
//            score += 0;
//        } else if (size > 3) {
//            score += 5;
//        } else {
//            score += 10;
//        }

        if (dumpCount.size() > 1) {
            score += 1;
        } else {
            score += 10;
        }

//        if (wordLength >= 57) {
//            score += 30;
//        } else {
        score += wordLength - 48;
//        }

        int punctuationNum = engPunctuationNum + chsPunctuationNum;
//        if (punctuationNum == 0) {
//            score += 6;
//        } else
        score += 3 * (8 - punctuationNum);
//        if (punctuationNum > 7) {
//            score += 0;
//        } else if (punctuationNum > 5) {
//            score += 8;
//        } else if (punctuationNum > 3) {
//            score += 16;
//        } else if (punctuationNum > 1) {
//            score += 24;
//        } else {
//            score += 30;
//        }

        /**
         * 热搜词压到7个
         */
        int hotPvScore = hotPvWordCount * 4;
        if (hotPvScore > 24) {
            hotPvScore = hotPvScore / 4 + 18;
        }

//        if (hotPvWordCount > 6) {
//            score += 30;
//        } else if (hotPvWordCount > 4) {
//            score += 20;
//        } else if (hotPvWordCount > 2) {
//            score += 10;
//        } else {
//            score += 0;
//        }

        /*
         * 这是原来的属性得分逻辑
            propNum = CollectionUtils.size(props);
            int propScore = propNum * 2 + 10;
            if (propScore > 20) {
                propScore = 20;
            }
        */

        /*这是新的属性逻辑*/
        int itemPropsNum = itemProps.split(",").length;
        int catPropsNum = cidProps.split(",").length;
        int diffValue = (catPropsNum - itemPropsNum) * 2;
        if (diffValue > 20) {
            diffValue = 20;
        }

        int propScore = 30 - diffValue;

        if (propScore > 0) {
            score += propScore;
        }

        /*rawPropNum = this.rawPropsList.size();
        int rawPropScore = (rawPropNum) * 2;
        if (rawPropScore > 16) {
            rawPropScore = 16;
        }

        this.score += rawPropScore;*/

//
//        if (propNum < 3) {
//            this.score -= 24;
//        } else if (propNum < 5) {
//            this.score -= 16;
//        } else if (propNum < 8) {
//            this.score -= 8;
//        } else {
//            this.score -= 1;
//        }
//
//        if (CommonUtils.isEmpty(this.props)) {
//            this.score -= 10;
//        }

        // 宝贝销量得分
        if(tradeCount <= 0) {
        	score -= 8;
        } else if(tradeCount <= 10) {
        	score -= 6;
        } else if(tradeCount <= 50) {
        	score -= 4;
        } else if(tradeCount <= 100) {
        	score -= 2;
        }
        
        score += 10;
        
        if (score > 95) {
            score = 95;
        } else if (score < 10) {
            score = 10;
        }
    }

    @JsonProperty
    int propNum = 0;

    @JsonProperty
    int rawPropNum = 0;

    private void buildPromoteWordsList(String title, List<String> simpleParseResults) {
        for (String word : simpleParseResults) {
            if (promotes.contains(word)) {
                promoteWordSet.add(word);
            }
        }

        if (title.indexOf("包邮") >= 0) {
            promoteWordSet.add("包邮");
        }

        this.promoteWords = StringUtils.join(promoteWordSet, ',');
    }

    private boolean isHot(IWordBase base) {
        return base != null && base.getPv() > 500;
    }

    public static class WordWSBaseAPI extends WordBaseAPI {

        public WordWSBaseAPI(Collection<String> arg0) {
            super(arg0);
            this.defaultTimeout = 10000;
        }

    }

	public static String replaceBlank(String str) {
		String dest = "";

		if (str != null) {

			Pattern p = Pattern.compile("\\s*|\t|\r|\n");

			Matcher m = p.matcher(str);

			dest = m.replaceAll("");

		}
		return dest;

	}
    
    DecimalFormat df = new DecimalFormat("#.00");
    public static String itemTitleCatPvPre = "itemTitleCatPvPre_";
	public void setItemTitleCatPv(String title, Long cid) {
		if (this.numIid <= 0) {
			return;
		}
		if (cid == null || cid <= 0) {
			return;
		}
		if (StringUtils.isEmpty(title)) {
			return;
		}
		TitleCatPvInfo info = (TitleCatPvInfo) Cache.get(itemTitleCatPvPre + replaceBlank(title) + "_" + cid);
		if(info != null) {
			this.setTitleCatPv(info.getTitleCatPv());
			this.setTitleCatPrePv(info.getTitleCatPrePv());
			return;
		}
		List<String> splits;
		try {
			splits = new AutoSplit(title, false).execute();
			if (CommonUtils.isEmpty(splits)) {
				return;
			}

			Map<String, IWordBase> resMap = new AutoWordBase(splits, cid)
					.execute();
			Collection<IWordBase> c = resMap.values();
			Iterator it = c.iterator();
			Long titleCatPv = 0L;
			double titleCatPrePv = 0.00;
			while (it.hasNext()) {
				IWordBase base = (IWordBase) it.next();
				titleCatPv += base.getPv();
				titleCatPrePv += base.getCompetition() == 0 ? 
						0.00 : base.getPv() * 1.0 / base.getCompetition();
			}

			Cache.set(itemTitleCatPvPre + replaceBlank(title) + "_" + cid, new TitleCatPvInfo(titleCatPv, df.format(titleCatPrePv)), "24h");
			this.setTitleCatPv(titleCatPv);
			this.setTitleCatPrePv(df.format(titleCatPrePv));
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class TitleCatPvInfo implements Serializable{
		public Long titleCatPv;
		
		// 两位小数
		public String titleCatPrePv;

		public TitleCatPvInfo(Long titleCatPv, String titleCatPrePv) {
			super();
			this.titleCatPv = titleCatPv;
			this.titleCatPrePv = titleCatPrePv;
		}

		public Long getTitleCatPv() {
			return titleCatPv;
		}

		public void setTitleCatPv(Long titleCatPv) {
			this.titleCatPv = titleCatPv;
		}

		public String getTitleCatPrePv() {
			return titleCatPrePv;
		}

		public void setTitleCatPrePv(String titleCatPrePv) {
			this.titleCatPrePv = titleCatPrePv;
		}
		
		
	}
	
    private void buildWordInfo(List<String> level2Results) throws ClientException {

//        log.info("[splits : ]" + level2Results);
        final List<String> lowStrings = new ArrayList<String>();
        final List<String> mediumStrings = new ArrayList<String>();
        final List<String> hotStrings = new ArrayList<String>();

        Map<String, IWordBase> wordBaseInfo = new WordWSBaseAPI(level2Results).execute();
        if (CommonUtils.isEmpty(wordBaseInfo)) {
            wordBaseInfo = new WidAPIs.WordBaseAPI(level2Results).execute();
        }

        if (CommonUtils.isEmpty(wordBaseInfo)) {
            wordBaseInfo = new WidAPIs.WordBaseAPI(level2Results).execute();
        }
        if (CommonUtils.isEmpty(wordBaseInfo)) {
            return;
        }

        int size = level2Results.size();
        for (int i = 0; i < size; i++) {
            String curr = level2Results.get(i);
            IWordBase base = wordBaseInfo.get(curr);
            if (isHot(base)) {
                hotStrings.add(curr);
                DiagResult.this.hotPv += base.getPv();
                continue;
            }
            if (i > 0) {
                String prev = level2Results.get(i - 1);
//                    log.info("[prev :]" + prev);
                if (prev.indexOf(curr.substring(0, 1)) == (prev.length() - 1)) {
                    continue;
                }
            }
            if (i < level2Results.size() - 1) {
                String after = level2Results.get(i + 1);
//                    log.info("[after :]" + after);
                if (after.indexOf(curr.substring(curr.length() - 1)) == 0) {
                    continue;
                }
            }

        }
/*
        new MapIterator<String, IWordBase>(wordBaseInfo) {

            @Override
            public void execute(Entry<String, IWordBase> entry) {
                if (entry.getValue() == null) {
                    return;
                }

                IWordBase base = entry.getValue();
                if (base.getPv() == null || base.getPv() < 0) {
                    lowStrings.add(entry.getKey());
                    return;
                }

                if (base.getPv() > 5000) {
                    hotStrings.add(entry.getKey());
                    DiagResult.this.hotPv += base.getPv();

//                } else if (base.getPv() > 200) {
//                    mediumStrings.add(entry.getKey());

                } else {
                    lowStrings.add(entry.getKey());
                    DiagResult.this.lowPv += base.getPv();
                }

                DiagResult.this.pv += base.getPv();

            }
        }.call();
*/

        this.lowPvWordCount = lowStrings.size();
        this.lowPvWords = StringUtils.join(lowStrings, ',');
        this.mediumPvWordCount = mediumStrings.size();
        this.mediumPvWords = StringUtils.join(mediumStrings, ',');
        this.hotPvWordCount = hotStrings.size();
        this.hotPvWords = StringUtils.join(hotStrings, ',');

        if (this.hotPv == 0 || this.lowPv == 0) {
            this.improveSpace = 1.0d;
        } else {
            this.improveSpace = ((double) (this.hotPv % 10000)) / 10000d;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> findCountMoreThan(final Map<String, Integer> wordsCount, final int count) {
        if (CommonUtils.isEmpty(wordsCount)) {
            return MapUtils.EMPTY_MAP;
        }

        final Map<String, Integer> res = new HashMap<String, Integer>();
        new MapIterator<String, Integer>(wordsCount) {

            @Override
            public void execute(Entry<String, Integer> entry) {
                if (entry.getValue() != null && entry.getValue() > count) {
                    res.put(entry.getKey(), entry.getValue());
                }
            }
        }.call();

        return res;
    }

    private Map<String, Integer> appendVerbCount(List<String> simpleParseResults) {
        Map<String, Integer> wordsCount = new HashMap<String, Integer>();
        for (String simpleRes : simpleParseResults) {
            if (ChsCharsUtil.length(simpleRes) < 4) {
                continue;
            }

            Integer count = wordsCount.get(simpleRes);
            if (count == null) {
                wordsCount.put(simpleRes, 1);
            } else {
                wordsCount.put(simpleRes, count + 1);
            }
        }
        return wordsCount;
    }

    @Override
    public String toString() {
        return "DiagResult [spaceNum=" + spaceNum + ", wordLength=" + wordLength + ", promoteWords=" + promoteWordSet
                + ", lowPvWordCount=" + lowPvWordCount + ", lowPvWords=" + lowPvWords + ", highPvWordCount="
                + mediumPvWordCount + ", highPvWords=" + mediumPvWords + ", engPunctuationNum=" + engPunctuationNum
                + ", chsPunctuationNum=" + chsPunctuationNum + ", dumpCount=" + dumpCount + ", pv=" + pv + ", title="
                + title + "]";
    }

    public static String[] getPromoteWordsArr() {
        return promoteWordsArr;
    }

    public static void setPromoteWordsArr(String[] promoteWordsArr) {
        DiagResult.promoteWordsArr = promoteWordsArr;
    }

    public static Set<String> getPromotes() {
        return promotes;
    }

    public static void setPromotes(Set<String> promotes) {
        DiagResult.promotes = promotes;
    }

    public int getSpaceNum() {
        return spaceNum;
    }

    public void setSpaceNum(int spaceNum) {
        this.spaceNum = spaceNum;
    }

    public int getWordLength() {
        return wordLength;
    }

    public void setWordLength(int wordLength) {
        this.wordLength = wordLength;
    }

    public String getCidProps() {
		return cidProps;
	}

	public void setCidProps(String cidProps){
		this.cidProps = cidProps;
	}

	public String getItemProps() {
		return itemProps;
	}

	public void setItemProps(List<String> props) {
		if(!CommonUtils.isEmpty(props)) {
			this.itemProps = StringUtils.join(props, ",");
		}
		
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}

	public String getPromoteWords() {
        return promoteWords;
    }

    public void setPromoteWords(String promoteWords) {
        this.promoteWords = promoteWords;
    }

    public Set<String> getPromoteWordSet() {
        return promoteWordSet;
    }

    public void setPromoteWordSet(Set<String> promoteWordSet) {
        this.promoteWordSet = promoteWordSet;
    }

    public boolean isTradeInited() {
		return isTradeInited;
	}

	public void setTradeInited(boolean isTradeInited) {
		this.isTradeInited = isTradeInited;
	}

	public Integer getTradeCount() {
		return tradeCount;
	}

	public void setTradeCount(Integer tradeCount) {
		this.tradeCount = tradeCount;
	}

	public long getDelistTime() {
        return delistTime;
    }

    public void setDelistTime(long delistTime) {
        this.delistTime = delistTime;
    }
    
    public String getDelistWeekDay() {
        if (delistTime <= 0) {
            return "-";
        }
        return IndustryDelistGetAction.getDelistWeekDay(delistTime);
    }
    
    public String getDelistHHmmss() {
        if (delistTime <= 0) {
            return "-";
        }
        return IndustryDelistGetAction.getDelistHHmmss(delistTime);
    }

    public boolean isOptimised() {
		return isOptimised;
	}

	public void setOptimised(boolean isOptimised) {
		this.isOptimised = isOptimised;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public List<String> getSimpleParserResult() {
		return simpleParserResult;
	}

	public void setSimpleParserResult(List<String> simpleParserResult) {
		this.simpleParserResult = simpleParserResult;
	}

	public List<String> getRawPropsList() {
		return rawPropsList;
	}

	public void setRawPropsList(List<String> rawPropsList) {
		this.rawPropsList = rawPropsList;
	}

	public int getPropNum() {
		return propNum;
	}

	public void setPropNum(int propNum) {
		this.propNum = propNum;
	}

	public int getRawPropNum() {
		return rawPropNum;
	}

	public void setRawPropNum(int rawPropNum) {
		this.rawPropNum = rawPropNum;
	}

	public Long getTitleCatPv() {
		return titleCatPv;
	}

	public void setTitleCatPv(Long titleCatPv) {
		this.titleCatPv = titleCatPv;
	}

	public String getTitleCatPrePv() {
		return titleCatPrePv;
	}

	public void setTitleCatPrePv(String titleCatPrePv) {
		this.titleCatPrePv = titleCatPrePv;
	}

	public int getLowPvWordCount() {
        return lowPvWordCount;
    }

    public void setLowPvWordCount(int lowPvWordCount) {
        this.lowPvWordCount = lowPvWordCount;
    }

    public String getLowPvWords() {
        return lowPvWords;
    }

    public void setLowPvWords(String lowPvWords) {
        this.lowPvWords = lowPvWords;
    }

    public int getMediumPvWordCount() {
        return mediumPvWordCount;
    }

    public void setMediumPvWordCount(int mediumPvWordCount) {
        this.mediumPvWordCount = mediumPvWordCount;
    }

    public String getMediumPvWords() {
        return mediumPvWords;
    }

    public void setMediumPvWords(String mediumPvWords) {
        this.mediumPvWords = mediumPvWords;
    }

    public int getHotPvWordCount() {
        return hotPvWordCount;
    }

    public void setHotPvWordCount(int hotPvWordCount) {
        this.hotPvWordCount = hotPvWordCount;
    }

    public String getHotPvWords() {
        return hotPvWords;
    }

    public void setHotPvWords(String hotPvWords) {
        this.hotPvWords = hotPvWords;
    }

    public int getEngPunctuationNum() {
        return engPunctuationNum;
    }

    public void setEngPunctuationNum(int engPunctuationNum) {
        this.engPunctuationNum = engPunctuationNum;
    }

    public int getChsPunctuationNum() {
        return chsPunctuationNum;
    }

    public void setChsPunctuationNum(int chsPunctuationNum) {
        this.chsPunctuationNum = chsPunctuationNum;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<String, Integer> getDumpCount() {
        return dumpCount;
    }

    public void setDumpCount(Map<String, Integer> dumpCount) {
        this.dumpCount = dumpCount;
    }

    public List<WordCountBean> getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(List<WordCountBean> searchCount) {
        this.searchCount = searchCount;
    }

    public int getLowPv() {
        return lowPv;
    }

    public void setLowPv(int lowPv) {
        this.lowPv = lowPv;
    }

    public int getHotPv() {
        return hotPv;
    }

    public void setHotPv(int hotPv) {
        this.hotPv = hotPv;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getImproveSpace() {
        return improveSpace;
    }

    public void setImproveSpace(double improveSpace) {
        this.improveSpace = improveSpace;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public Set<String> getProps() {
        return props;
    }

    public void setProps(Set<String> props) {
        this.props = props;
    }

    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(long numIid) {
        this.numIid = numIid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;

    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

	public String getCatName() {
		ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
		if(itemCatPlay != null) {
			return itemCatPlay.getName();
		}
		return StringUtils.EMPTY;
	}

}
