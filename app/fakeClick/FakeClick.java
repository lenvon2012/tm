
package fakeClick;

import java.util.ArrayList;
import java.util.List;

import job.click.ItemNum;
import models.item.ItemPlay;
import models.popularized.Popularized;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;

import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.TMController;
import dao.item.ItemDao;

public class FakeClick extends TMController {

    public String referer = StringUtils.EMPTY;

    public String url1 = StringUtils.EMPTY;

    public String url2 = StringUtils.EMPTY;

    public String url3 = StringUtils.EMPTY;

    long numIid;

    public int waitTime = 3000;// ms

    public static int onejumprate = 75;

    public static int onetwojumprate = 90;

    public static int threejumprate = 10;

    public FakeClick() {
        super();
    }

//
//    // actually, all FakeClick's params is generated random, so this constructor is not often used
//    public FakeClick(String referer, String url1, String url2, String url3, int waitTime) {
//        this.referer = referer;
//        this.url1 = url1;
//        this.url2 = url2;
//        this.url3 = url3;
//        this.waitTime = waitTime;
//    }

    public FakeClick(Popularized item) {
        //this.url1 = TMConfigs.Referers.urlPrefix + item.getNumIid();
        this.url1 = APIConfig.get().getClickUrl(item.getNumIid());
        this.numIid = item.getNumIid();
    }

    public FakeClick(ItemNum item) {
        //this.url1 = TMConfigs.Referers.urlPrefix + item.getNumIid();
        this.url1 = APIConfig.get().getClickUrl(item.getNumIid());
        this.numIid = item.getNumIid();
    }

    public void setWaitTime() {
        int a = (int) (Math.random() * 2 + 1);
        int sign = (int) (Math.pow(-1, a));
        this.waitTime = 40 + sign * (int) Math.floor((Math.random() * 20));
        //this.waitTime = 25 + (int) Math.floor((Math.random() * 20));
    }

    public List<Long> getToclickItems(Long userId) {
        //List<Popularized> PopularizedItems = Popularized.find("userId = ?", userId).fetch();
        List<ItemPlay> PopularizedItems = ItemDao.findByUserIdOnsale(userId, 10);
        List<Long> ids = new ArrayList<Long>();
        if (PopularizedItems != null) {
            for (ItemPlay popularized : PopularizedItems) {
                ids.add(popularized.getNumIid());
            }
        }
        if (ids.size() < TMConfigs.Referers.urlsize) {
            List<ItemPlay> salesFirst = ItemDao.findOnlineByUserWithTradeNUm(userId, TMConfigs.Referers.urlsize);
            if (ids.size() == 0) {
                ids = ItemDao.toIdsList(salesFirst);
            } else {
                ids.addAll(ItemDao.toIdsList(salesFirst));
            }
        }
        return ids;
    }

    public List<Long> getToclickNumIids(Long userId) {
        List<Long> numIidsLong = new ArrayList<Long>();
        String thisUserNumIids = (String) Cache.get("RelationNid_" + userId);

        if (thisUserNumIids == null || thisUserNumIids.isEmpty()) {
            thisUserNumIids = ItemDao.findNumIidsStringByUser(userId);
            Cache.set("RelationNid_" + userId, thisUserNumIids, "24h");
        }
        if (!thisUserNumIids.isEmpty()) {
            String[] numIids = thisUserNumIids.split(",");
            if (numIids != null && numIids.length > 0) {
                for (String s : numIids) {
                    numIidsLong.add(Long.valueOf(s));
                }
            }
        }
        return numIidsLong;
    }

    public void setUrls(Long userId) {
        int jump = (int) Math.ceil(Math.random() * 3);
        List<Long> ids = getToclickItems(userId);
        int idsSize = ids.size();
        if (jump == 3) {
            //	this.url1 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil(Math.random()*3));
            //this.url2 = TMConfigs.Referers.urlPrefix + ids.get(((int) Math.ceil(Math.random() * 3) + 3) % idsSize);
            //this.url3 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil((Math.random() * 3) + 6) % idsSize);
            this.url2 = APIConfig.get().getClickUrl(ids.get(((int) Math.ceil(Math.random() * 3) + 3) % idsSize));
            this.url3 = APIConfig.get().getClickUrl(ids.get((int) Math.ceil((Math.random() * 3) + 6) % idsSize));
        } else if (jump == 2) {
            //	this.url1 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil(Math.random()*3));
            //this.url2 = TMConfigs.Referers.urlPrefix + ids.get(((int) Math.ceil(Math.random() * 3) + 3) % idsSize);
            
            this.url2 = APIConfig.get().getClickUrl(ids.get(((int) Math.ceil(Math.random() * 3) + 3) % idsSize));
            
        } else {
            //	this.url1 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil(Math.random()*3));
        }
    }

    public void setJumpUrls(Long userId) {
        int jump = jumprandom();
        List<Long> ids = getToclickNumIids(userId);
        int idsSize = ids.size();
        if (idsSize > 0) {
            if (jump == 3) {
                //this.url1 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil(Math.random()*3));
                int offset2 = (int) (Math.ceil(Math.random() * idsSize) % idsSize);
                //this.url2 = TMConfigs.Referers.urlPrefix + ids.get(offset2);
                this.url2 = APIConfig.get().getClickUrl(ids.get(offset2));
                
                
                int offset3 = ((int) (Math.ceil(Math.random() * idsSize))) % idsSize;
                if (offset3 == offset2) {
                    offset3 = (offset3 + 1) % idsSize;
                }
                //this.url3 = TMConfigs.Referers.urlPrefix + ids.get(offset2);
                this.url3 = APIConfig.get().getClickUrl(ids.get(offset2));
                
            } else if (jump == 2) {
                //this.url1 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil(Math.random()*3));
                int offset2 = (int) (Math.ceil(Math.random() * idsSize) % idsSize);
                //this.url2 = TMConfigs.Referers.urlPrefix + ids.get(offset2);
                this.url2 = APIConfig.get().getClickUrl(ids.get(offset2));
                
            } else {
                //this.url1 = TMConfigs.Referers.urlPrefix + ids.get((int) Math.ceil(Math.random()*3));
            }
        }
    }

    public int jumprandom() {
        int jump = 0;
        if (DateUtil.getCurrHour() % 2 == 1) {
            int random = (int) (Math.ceil(Math.random() * 100));
            if (random <= onejumprate) {
                jump = 1;
            } else if (random <= onetwojumprate) {
                jump = 2;
            } else {
                jump = 3;
            }
        } else {
            jump = (int) (Math.ceil(Math.random() * 3));
        }
        return jump;
    }

    public void setRefer() {
        String[] referes = APIConfig.get().getReferes(this.numIid);
        int length = referes.length;
        int index = (int) Math.floor(Math.random() * length);
        this.referer = referes[index];
    }

    public String getRefer() {
        String[] referes = APIConfig.get().getReferes();
        int length = referes.length;
        int index = (int) Math.floor(Math.random() * length);
        return referes[index];
    }

}
