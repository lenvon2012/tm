
package titleDiag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import actions.DiagAction;
import autotitle.AutoSplit;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.api.WidAPIs.SplitAPI;
import com.ciaosir.client.item.ItemBean;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.word.PaodingSpliter.SplitMode;
import com.ciaosir.commons.ClientException;

public class TitleDiagnose {

    private static TitleDiagnose _instance = new TitleDiagnose();;

    public static TitleDiagnose getInstance() {
        return _instance;
    }

    public TitleDiagnose() {
    }

    @Deprecated
    public DiagResult doWord(String title, String props) throws ClientException {
        return doWord(0L, 0d, title, props, StringUtils.EMPTY, -1, 0L, 0L);
    }

    @Deprecated
    public DiagResult doWord(String title) throws ClientException {
        return doWord(0L, 0d, title, null, StringUtils.EMPTY, -1, 0L, 0L);
    }
    
    @Deprecated
    public DiagResult doWord(Long numIid, double price, String title, String props, String picPath,
    		Integer tradeCount, long delistTime, Long cid)
            throws ClientException {
        List<String> propsList = ItemBean.parseValues(props);
        List<String> simpleParseResults = new AutoSplit(title, ListUtils.EMPTY_LIST).execute();
        List<String> level2Results = simpleParseResults;
        if(cid != null && cid > 0) {
        	// 这里获取类目标准属性
            String cidProps = DiagAction.getCidProps(cid);
            return new DiagResult(numIid, price, title, picPath, propsList, simpleParseResults, level2Results,
            		tradeCount, delistTime, cidProps, DiagAction.parseNames(props));
        }
//        List<String> level2Results = new SplitAPI(title, SplitMode._, true).execute();

        return new DiagResult(numIid, price, title, picPath, propsList, simpleParseResults, level2Results,
        		tradeCount, delistTime);
    }

    /**
     * Return for the competition for the title...
     * 
     * @param title
     * @return
     * @throws ClientException
     */
    @SuppressWarnings("unchecked")
    public List<IWordBase> findExpectedShowWords(String title, int minPv) throws ClientException {
        Set<String> src = new HashSet<String>();
        src.addAll(new SplitAPI(title, SplitMode.BASE, false).execute());
        src.addAll(new SplitAPI(title, SplitMode.LEVEL2_COMB, false).execute());

        Map<String, IWordBase> wordBaseInfo = new WidAPIs.WordBaseAPI(src).execute();
        if (CommonUtils.isEmpty(wordBaseInfo)) {
            return ListUtils.EMPTY_LIST;
        }

        List<IWordBase> res = new ArrayList<IWordBase>();
        Collection<IWordBase> values = wordBaseInfo.values();
        for (IWordBase iWordBase : values) {
            if (iWordBase.getPv() > minPv) {
                res.add(iWordBase);
            }
        }
        return res;
    }

    public List<IWordBase> findExpectedShowWords(String title) throws ClientException {
        return findExpectedShowWords(title, 200);
    }
}
