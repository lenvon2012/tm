
package fakeClick;

import java.util.List;

import job.click.ItemNum;
import models.paipai.PaiNumIidToItemCode;
import models.popularized.Popularized;

import org.apache.commons.lang.StringUtils;

import configs.TMConfigs;
import dao.paipai.PaiPaiItemDao;

public class PaiPaiFakeClick extends FakeClick {

    public String referer = StringUtils.EMPTY;

    public String url1 = StringUtils.EMPTY;

    public String url2 = StringUtils.EMPTY;

    public String url3 = StringUtils.EMPTY;

    public int waitTime = 3000;// ms

    public static int onejumprate = 75;
    
    public static int onetwojumprate = 90;
    
    public static int threejumprate = 10;
    public PaiPaiFakeClick() {
        super();
    }

    // actually, all FakeClick's params is generated random, so this constructor is not often used
    public PaiPaiFakeClick(String referer, String url1, String url2, String url3, int waitTime) {
        this.referer = referer;
        this.url1 = url1;
        this.url2 = url2;
        this.url3 = url3;
        this.waitTime = waitTime;
    }

    public PaiPaiFakeClick(Popularized item) {
        String itemCode = PaiNumIidToItemCode.fetchItemCode(item.getNumIid());
        this.url1 = TMConfigs.Referers.urlPrefixPaiPai + itemCode;
    }

    public PaiPaiFakeClick(ItemNum item) {
        String itemCode = PaiNumIidToItemCode.fetchItemCode(item.getNumIid());
        this.url1 = TMConfigs.Referers.urlPrefixPaiPai + itemCode;
    }
    
    public void setJumpUrls(Long userId) {
        int jump = (int) Math.ceil(Math.random() * 2);
        List<String> ids = PaiPaiItemDao.randOnSaleIdsByUserId(userId, jump);
        int idsSize = ids.size();
        if (idsSize == 1) {
            this.url2 = TMConfigs.Referers.urlPrefixPaiPai + ids.get(0);
        } else if(idsSize == 2) {
            this.url2 = TMConfigs.Referers.urlPrefixPaiPai + ids.get(0);
            this.url3 = TMConfigs.Referers.urlPrefixPaiPai + ids.get(1);
        }
    }
}
