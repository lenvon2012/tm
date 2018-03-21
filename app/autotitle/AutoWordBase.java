
package autotitle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bustbapi.TMApi.BusCategoryWordBaseGetApi;

import com.ciaosir.client.api.WidAPIs.WordBaseAPI;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.INWordCategory;

public class AutoWordBase extends WordBaseAPI {

    List<String> src = new ArrayList<String>();

    Long cid = null;

    public AutoWordBase(Collection<String> arg0, Long cid) {
        super(arg0);
        this.src.addAll(arg0);
        this.cid = cid;
    }

    public AutoWordBase(String[] arg0, Long cid) {
        super(arg0);
        for (String string : arg0) {
            src.add(string);
        }
        this.cid = cid;
    }

    int LARER_SIZE = 60;

    @SuppressWarnings("unchecked")
    public Map<String, IWordBase> execute() throws ClientException {
//

        final Map<String, IWordBase> res = new HashMap<String, IWordBase>();
        try {
            if (cid == null || cid.longValue() == 0L) {
                res.putAll(super.execute());
            } else {
/*                Map<String, Long> exec = BusAPI.buildCatClickCached(src, cid);
                res = new HashMap<String, IWordBase>();
                for (String key : exec.keySet()) {
                    Long pv = exec.get(key);
                    WordBaseBean bean = new WordBaseBean(key, pv.intValue());
                    bean.setClick(bean.getPv());
                    res.put(key, bean);
                }*/
                int cache = 7 + (int) (System.currentTimeMillis() % 8);
                final int maxCacheDays = 15;
                Map<String, INWordCategory> newRes = new BusCategoryWordBaseGetApi(cid, src, cache, maxCacheDays, true)
                        .execute();
                new MapIterator<String, INWordCategory>(newRes) {
                    @Override
                    public void execute(Entry<String, INWordCategory> entry) {
                        INWordCategory cat = entry.getValue();
                        if (cat == null || cat.getPv() == null || cat.getClick() == null || cat.getAvgPrice() == null
                                || cat.getCompetition() == null) {
                            return;
                        }

                        WordBaseBean bean = new WordBaseBean();
                        bean.setWord(entry.getKey());
                        bean.setPv(new Integer(cat.getPv().intValue()));
                        bean.setClick(new Integer(cat.getClick().intValue()));
                        bean.setCompetition(new Integer(cat.getCompetition().intValue()));
                        bean.setPrice(new Integer(cat.getAvgPrice().intValue()));
//                        bean.setCid(new Integer(cat.getCategoryId().intValue()));
                        res.put(entry.getKey(), bean);
                    }
                }.call();

//                res.values();
            }

            for (String str : MergedKeys.mergedWords) {
                IWordBase base = res.get(str);
                if (base == null) {
                    continue;
                }
                base.setPv(base.getPv() * LARER_SIZE);
                base.setClick(base.getClick() * LARER_SIZE);
            }
            return res;
        } catch (Exception e) {
            throw new ClientException(500, e.getMessage());
        }
    }
}
