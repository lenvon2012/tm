
package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.mysql.word.TMCWordBase;
import models.user.User;
import models.word.top.MyWords;
import models.word.top.TopKey;
import models.word.top.TopKey.QueryType;
import models.word.top.TopURLBase;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.cache.CacheFor;
import result.TMPaginger;
import result.TMResult;
import transaction.JDBCBuilder;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;

public class KeyWords extends TMController {

    private static final Logger log = LoggerFactory.getLogger(KeyWords.class);

    public static final String TAG = "KeyWords";

    //跳转到搜索关键词页面
    public static void toSerchKeyWords() {
        render("/hotWords/keyWords.html");
    }

    /**
     * 浏览器打开 ： localhost:9000/keywords/top?type=searchot&cat=TR_YYCP&pn=1&ps=20
     * 
     * @param type
     *            : searchup, searchhot, categoryhot, ctrhot;
     * @param cat
     * @param cid
     * @param pn
     * @param ps
     */
    public static void top(String type, String cat, String cid, int pn, int ps) {
        log.info("[cat ]" + cat);

        ps = ps < 5 ? 20 : ps;

        TMPaginger paginer = TopKey.findByUrlBase(cat, cid, pn, ps, QueryType.getType(type));
        renderJSON(JsonUtil.getJson(paginer));
    }

    public static String topByUrlCachePre = "topByUrlCachePre_";
    public static void topByUrl(String type, long urlId, int pn, int ps) {

        ps = ps < 5 ? 20 : ps;
        TMPaginger paginer = TopKey.findByUrlBaseId(urlId, pn, ps, QueryType.getType(type));
        
        // 更新topkey词的pv, click, ctr数据
        if(paginer != null) {
        	List<TopKey> keys = (List<TopKey>) paginer.getRes();
        	if(!CommonUtils.isEmpty(keys)) {
        		// 首先应该检查Cache是否有数据
        		String key = topByUrlCachePre + type.replace(" ", "") + "_" + urlId + "_" + pn + "_" + ps;
        		Map<String, IWordBase> execute = (Map<String, IWordBase>) Cache.get(key);
        		if(execute == null) {
        			try {
        				List<String> words = new ArrayList<String>();
                        for (TopKey wordBase : keys) {
                            words.add(wordBase.getWord());
                        } 
    					execute = new WidAPIs.WordBaseAPI(words).execute();
    					Cache.set(key, execute);
    				} catch (ClientException e) {
    					// TODO Auto-generated catch block
    					renderJSON(JsonUtil.getJson(paginer));
    					e.printStackTrace();
    				}
        		}
        		
        		for (TopKey wordBase : keys) {
                    IWordBase iWordBase = execute.get(wordBase.getWord());
                    if (iWordBase != null) {
                        wordBase.updateByWordBaseBean(iWordBase);
                    }

                }
        		
        	}
        }
        renderJSON(JsonUtil.getJson(paginer));
    }

    @CacheFor(value = "2h")
    public static void findLevel3(String level2) {
        List<TopURLBase> list = TopURLBase.findByLevel2(level2);
        if (StringUtils.isEmpty(level2)) {
            renderJSON("[]");
        }

        Iterator<TopURLBase> it = list.iterator();
        while (it.hasNext()) {
            TopURLBase next = it.next();
            TopKey topkey = TopKey.findOne(next.getId());
//            log.info("[found :]" + topkey);
            if (topkey == null) {
                it.remove();
            }
        }

        if (list.isEmpty()) {
            list.add((TopURLBase) TopURLBase.findById(Long.parseLong(level2)));
        }
        renderJSON(JsonUtil.getJson(list));
    }

    public static void findLevel2ById(Long baseId) {
        TopURLBase base = TopURLBase.findById(baseId);
        if (base == null) {
            renderFailedJson("找不到对应的TopURLBase");
        }
        if (base.getLevel() == 1) {
            renderFailedJson("居然是level 1 的TopURLBase");
        }
        if (base.getLevel() == 2) {
            renderSuccessJson(baseId + "");
        }
        if (base.getLevel() == 3) {
            renderSuccessJson(base.getParentBaseId() + "," + baseId);
        }
    }

    public static void findAllLevel2() {
        List<TopURLBase> allLevel2 = TopURLBase.findAllByLevel(2);
        renderJSON(allLevel2);
    }

    public static void searchmywords(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 5);
        //List<MyWords> myWords = MyWords.find("userId = ? limit ?,?", user.getId(),(pn - 1) * ps, ps).fetch();
        List<MyWords> myWords = MyWords.search(user.getId(), (pn - 1) * ps, ps);
        //TMResult tmRes = new TMResult(myWords, (int) MyWords.count("userId = ?", user.getId()), po);
        int count = (int) JDBCBuilder.singleLongQuery("select count(*) from my_words where userId = ?", user.getId());
        TMResult tmRes = new TMResult(myWords, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
        //renderJSON(myWords);
    }

    public static void addMyWord(String word, int pv, int price) {
        User user = getUser();
        //MyWords myWords = MyWords.find("userId = ? and word = ?", user.getId(), word).first();
        TMCWordBase.updatePriceAndPv(word, pv, price);
        MyWords myWords = MyWords.singleSearch(user.getId(), word);
        boolean isSuccess = false;
        if (myWords == null) {
            myWords = new MyWords(word, user.getId());
            isSuccess = myWords.jdbcSave();
            if (isSuccess) {
                renderText("添加成功");
            } else {
                renderText("添加失败");
            }
        }
        renderText("该词已在我的词库");
    }

    public static void deleteMyWord(String word) {
        User user = getUser();
        //MyWords myWords = MyWords.find("userId = ? and word = ?", user.getId(), word).first();
        MyWords myWords = MyWords.singleSearch(user.getId(), word);
        if (myWords == null) {
            renderText("我的词库不存在该词");
        }
        //int num = MyWords.delete("userId = ? and word = ? ", user.getId(), word);
        int num = (int) JDBCBuilder.update(false, "delete from my_words where userId = ? and word = ?", user.getId(),
                word);
        if (num > 0) {
            renderText("删除成功");
        } else {
            renderText("删除失败");
        }
    }
}
