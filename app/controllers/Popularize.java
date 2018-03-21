
package controllers;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.popularized.FenXiangToken;
import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;
import models.user.User;
import models.vgouitem.VGouItem;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import result.TMPaginger;
import result.TMResult;
import weibo4j.Oauth;
import weibo4j.Timeline;
import weibo4j.examples.oauth2.Log;
import weibo4j.http.AccessToken;
import weibo4j.http.ImageItem;
import weibo4j.model.Status;
import weibo4j.model.WeiboException;
import weibo4j.util.BareBonesBrowserLaunch;
import actions.popularized.PopularizedAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import configs.Subscribe.Version;
import configs.TMConfigs.PageSize;
import dao.UserDao;
import dao.item.ItemDao;
import dao.popularized.PopularizedDao;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;
import dao.popularized.VGItemDao;

public class Popularize extends TMController {
    private static final Logger log = LoggerFactory.getLogger(Popularize.class);

    public static final String TAG = "Popularize";

    public static void popularize(String sid) {
        render("Popularize/popularize.html");
    }

    public static void popularsns() {
        render("Popularize/popularsns.html");
    }

    public static void coolsites() {
        render("Popularize/coolsites.html");
    }

    public static void help() {
        render("Popularize/help.html");
    }

    public static void award() {
        render("/Popularize/award.html");
    }
    
    public static void yijianfenxiang(Long numIid,String title,String picURL){
        render("/Popularize/yijianfenxiang.html",numIid,title,picURL);
    }

    public static void setPopularOn() {
        User user = getUser();
        user.setPopularOff(false);
        boolean success = user.jdbcSave();
        renderJSON(new TMResult(success));
    }

    public static void setPopularOff() {
        User user = getUser();
        user.setPopularOff(true);
        boolean success = user.jdbcSave();
        renderJSON(new TMResult(success));
    }

    public static void addPopularized(String numIids, int status) {
        User user = getUser();

        status = PopularizedStatusSqlUtil.checkStatus(status);

        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }

        List<ItemPlay> items = ItemDao.findByNumIidList(user, numIids);
        if (CommonUtils.isEmpty(items)) {
            TMResult.renderMsg(StringUtils.EMPTY);
        }

        PopularizedAction.addPopularized(user, items, status);

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void removePopularized(String numIids, int status) {
        User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }

        status = PopularizedStatusSqlUtil.checkStatus(status);

        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }

        List<ItemPlay> items = ItemDao.findByNumIidList(user, numIids);
        if (CommonUtils.isEmpty(items)) {
            TMResult.renderMsg(StringUtils.EMPTY);
        }

        for (ItemPlay itemPlay : items) {
            // remove in popularized
            //Popularized popularized = Popularized.find("userId = ? and numIid = ?", user.getId(), itemPlay.getId())
            //        .first();
        	Popularized popularized = Popularized.findByNumIid(user.getId(), itemPlay.getId());
            if (popularized != null) {
                PopularizedAction.removePopularized(user.getId(), popularized, status);
            }
            // remove in vgouitem
            if (APIConfig.get().vgouSave()) {
                VGouItem vgouItem = VGouItem.find("uid = ? and numIid = ?", user.getId(), itemPlay.getNumIid()).first();
                if (vgouItem != null) {
                    vgouItem.delete();
                }
            }
        }
        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void addPopularizedAll(int status) {
        User user = getUser();

        status = PopularizedStatusSqlUtil.checkStatus(status);

        int remain = UserInfo.remainNumWithPopularAward(user, status);

        if (remain == 0) {
            renderJSON(new TMResult(false));
        } else {
            //List<ItemPlay> items = ItemDao.findPopularized(user.getId(), 0, remain, "", 3, "", 1);
        	List<ItemPlay> items = ItemDao.searchPop(user.getId(), 0, remain, "", 3, 1, "", status);
            if (CommonUtils.isEmpty(items)) {
                renderJSON(new TMResult(false));
            }

            PopularizedAction.addPopularized(user, items, status);

            renderJSON(new TMResult(true));
        }

    }

    public static void removePopularizedAll(int status) {
        User user = getUser();

        status = PopularizedStatusSqlUtil.checkStatus(status);

        /*List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        if (CommonUtils.isEmpty(items)) {
            TMResult.renderMsg(StringUtils.EMPTY);
        }
        for (ItemPlay itemPlay : items) {
            // itemPlay.setUnPopularized();
            // itemPlay.jdbcSave();
        	// remove in popularized
            Popularized popularized = Popularized.find("userId = ? and numIid = ?", user.getId(), itemPlay.getId())
                    .first();
            if (popularized != null) {
                popularized.delete();
            }
            // remove in vgouitem
            if(APIConfig.get().vgouSave()) {
                VGouItem vgouItem = VGouItem.find("uid = ? and numIid = ?", user.getId(), itemPlay.getNumIid()).first();
                if (vgouItem != null) {
                	vgouItem.delete();
                }
            }
        }
        int count = (int) Popularized.count("userId = ?", user.getId());
        if(count > 0) {
        	Popularized.removeAll(user);
        }*/

        PopularizedAction.removeAllPopularized(user.getId(), status);

        TMResult.renderMsg(StringUtils.EMPTY);
    }

    public static void PoluparizedOrNot(int status) {
        User user = getUser();
        status = PopularizedStatusSqlUtil.checkStatus(status);
        List<Popularized> popularized = new ArrayList<Popularized>();
        popularized = PopularizedDao.queryPopularizedsByUserIdAndStatus(user.getId(), status);
        renderJSON(JsonUtil.getJson(popularized));
    }

    public static void searchItems(String s, int pn, int ps, int sort, int polularized, Long catId, int popularizeStatus) {
        final User user = getUser();

        popularizeStatus = PopularizedStatusSqlUtil.checkStatus(popularizeStatus);

        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        Set<Long> pops = PopularizedDao.findNumIidsByUserIdWithStatus(user.getId(), popularizeStatus);
        if (catId == null || catId <= 0)
            catId = null;
        List<ItemPlay> list = ItemDao.searchPop(user.getId(), (pn - 1) * ps, ps, s, sort, polularized,
                catId == null ? "" : catId.toString(), popularizeStatus);
        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        } else {
            for (ItemPlay item : list) {
                if (item == null)
                    continue;
                if (pops.contains(item.getNumIid())) {
                    item.setPopularized();

                    //找到vgItemId
                    if (APIConfig.get().vgouSave()) {
                        long vgItemId = VGItemDao.findIdByNumIid(user.getId(), item.getNumIid());
                        if (vgItemId < 0)
                            vgItemId = 0;
                        item.setVgItemId(vgItemId);
                    }
                }

            }
        }
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult tmRes = new TMResult(list, (int) ItemDao.countPop(user.getId(), s, sort,
                catId == null ? "" : catId.toString(), polularized, popularizeStatus), po);
        renderJSON(JsonUtil.getJson(tmRes));
        /*if (polularized == 2) {
        	List<ItemPlay> list = ItemDao.searchPop(user.getId(), (pn - 1) * ps, ps,s,polularized,sort,catId == null ? "" : catId.toString());
            //TODO add recommend title to list

            if (CommonUtils.isEmpty(list)) {
                TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
            } else {
                for (ItemPlay item : list) {
                    if (pops.contains(item.getNumIid())) {
                        item.setPopularized();
                    }
                }
            }

            PageOffset po = new PageOffset(pn, ps, 10);
            TMResult tmRes = new TMResult(list, (int) ItemDao.countPopularized(user.getId(), s, sort,
                    catId == null ? "" : catId.toString(), polularized), po);
            renderJSON(JsonUtil.getJson(tmRes));
        } else if (polularized == 0) {
            List<ItemPlay> list = ItemDao.findByNumIids(user.getId(), pops);
            if (list.size() > 0) {
                for (ItemPlay item : list) {
                    item.setPopularized();
                }
            }
            PageOffset po = new PageOffset(pn, ps, 10);
            TMResult tmRes = new TMResult(list, (int) ItemDao.countPopularized(user.getId(), s, sort,
                    catId == null ? "" : catId.toString(), polularized), po);
            renderJSON(JsonUtil.getJson(tmRes));
        }*/
    }

    public static void getItems(String s, int pn, int ps, int sort, int polularized, Long catId) {
        /* final User user = getUser();
         pn = pn < 1 ? 1 : pn;
         ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
         log.info(format(
                 "getItemsWithDiagResult:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ",
                         "=%s, ") + "=%s", user.getId(), s, pn, ps, 0, 100, sort, 2, catId));

         List<ItemPlay> list = ItemDao.findPopularized(user.getId(), (pn - 1) * ps, ps, s, sort, catId == null ? ""
                 : catId.toString(), polularized);
         //TODO add recommend title to list

         if (CommonUtils.isEmpty(list)) {
             TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
         }

         PageOffset po = new PageOffset(pn, ps, 10);
         TMResult tmRes = new TMResult(list, (int) ItemDao.countPopularized(user.getId(), s, sort, catId == null ? ""
                 : catId.toString(), polularized), po);
         renderJSON(JsonUtil.getJson(tmRes));*/
        final User user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        log.info(format(
                "getItemsWithDiagResult:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ",
                        "=%s, ") + "=%s", user.getId(), s, pn, ps, 0, 100, sort, 2, catId));
        Set<Long> pops = PopularizedDao.findNumIidsByUserId(user.getId());
        if (polularized == 2) {
            List<ItemPlay> list = ItemDao.findPopularized(user.getId(), (pn - 1) * ps, ps, s, sort, catId == null ? ""
                    : catId.toString(), polularized);
            //TODO add recommend title to list

            if (CommonUtils.isEmpty(list)) {
                TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
            } else {
                for (ItemPlay item : list) {
                    if (pops.contains(item.getNumIid())) {
                        item.setPopularized();
                    }
                }
            }

            PageOffset po = new PageOffset(pn, ps, 10);
            TMResult tmRes = new TMResult(list, (int) ItemDao.countPopularized(user.getId(), s, sort,
                    catId == null ? "" : catId.toString(), polularized), po);
            renderJSON(JsonUtil.getJson(tmRes));
        } else if (polularized == 0) {
            List<ItemPlay> list = ItemDao.findByNumIids(user.getId(), pops);
            if (list.size() > 0) {
                for (ItemPlay item : list) {
                    item.setPopularized();
                }
            }
            PageOffset po = new PageOffset(pn, ps, 10);
            TMResult tmRes = new TMResult(list, (int) ItemDao.countPopularized(user.getId(), s, sort,
                    catId == null ? "" : catId.toString(), polularized), po);
            renderJSON(JsonUtil.getJson(tmRes));
        }

    }

    public static void getUserInfo() {
        User user = getUser();
        int level = user.getVersion();
        renderJSON(JsonUtil.getJson(UserInfo.getUserInfo(level, user.getId(), user.getUserNick())));
    }

    @JsonAutoDetect
    public static class UserInfo {

        @JsonProperty
        String username = StringUtils.EMPTY;

        @JsonProperty
        int level = 0;

        @JsonProperty
        int totalNum = 0;

        @JsonProperty
        int popularizedNum = 0;

        @JsonProperty
        int remainNum = 0;

        @JsonProperty
        boolean award = false;

        @JsonProperty
        boolean isPopularOn = true;

        @JsonProperty
        int hotTotalNum = 0;//热卖推荐总数

        @JsonProperty
        int hotUsedNum = 0;//已热卖推荐数

        @JsonProperty
        int hotRemainNum = 0;//热卖推荐剩余

        public UserInfo() {
            super();
        }

        public String getVersion() {
            Map<Integer, String> versionNameMap = APIConfig.get().getVersionNameMap();
            if (level <= Version.BLACK) {
                return versionNameMap.get(Version.BLACK);
            } else if (level <= Version.FREE) {
                return versionNameMap.get(Version.FREE);
            } else if (level <= Version.BASE) {
                return versionNameMap.get(Version.BASE);
            } else if (level <= Version.VIP) {
                return versionNameMap.get(Version.VIP);
            } else if (level <= Version.SUPER) {
                return versionNameMap.get(Version.SUPER);
            } else if (level <= Version.HALL) {
                return versionNameMap.get(Version.HALL);
            } else if (level <= Version.GOD) {
                return versionNameMap.get(Version.GOD);
            } else if (level <= Version.SUN) {
                return versionNameMap.get(Version.SUN);
            } else if (level <= Version.DAWEI) {
                return versionNameMap.get(Version.DAWEI);
            } else {
                return versionNameMap.get(Version.CUOCUO);
            }
        }

        public static UserInfo getUserInfo(int level, Long userId, String username) {
            User user = UserDao.findById(userId);
            boolean award = false;
            if (user.isPopularAward()) {
                award = true;
            }
            boolean isPopularOn = true;
            if (user.isPopularOff()) {
                isPopularOn = false;
            }
            /*if (level <= Version.FREE) {
            	int freeTotal = verCountMap.get(Version.FREE);
                return new UserInfo(level, freeTotal, count, freeTotal - count, username, award, isPopularOn);
            } else if (level <= Version.BASE) {
            	int basicTotal = verCountMap.get(Version.BASE);
                return new UserInfo(level, basicTotal, count, basicTotal - count, username, award, isPopularOn);
            } else if (level <= Version.VIP) {
            	int VIPTotal = verCountMap.get(Version.VIP);
                return new UserInfo(level, VIPTotal, count, VIPTotal - count, username, award, isPopularOn);
            } else if (level <= Version.SUPER) {
            	int superTotal = verCountMap.get(Version.SUPER);
                return new UserInfo(level, superTotal, count, superTotal - count, username, award, isPopularOn);
            } else if (level <= Version.HALL) {
            	int hallTotal = verCountMap.get(Version.HALL);
                return new UserInfo(level, hallTotal, count, hallTotal - count, username, award, isPopularOn);
            } else if (level <= Version.GOD) {
            	int godTotal = verCountMap.get(Version.GOD);
                return new UserInfo(level, godTotal, count, godTotal - count, username, award, isPopularOn);
            } else if (level <= Version.SUN) {
            	int sunTotal = verCountMap.get(Version.SUN);
                return new UserInfo(level, sunTotal, count, sunTotal - count, username, award, isPopularOn);
            } else if (level <= Version.DAWEI) {
            	int daweiTotal = verCountMap.get(Version.DAWEI);
                return new UserInfo(level, daweiTotal, count, daweiTotal - count, username, award, isPopularOn);
            } else {
            	int cuocuoTotal = verCountMap.get(Version.CUOCUO);
                return new UserInfo(level, cuocuoTotal, count, cuocuoTotal - count, username, award, isPopularOn);
            }*/

            int totalNum = getTotal(level, PopularizedStatus.Normal);
            int usedNum = getPopularizedNum(userId, PopularizedStatus.Normal + PopularizedStatus.Try);

            int hotTotalNum = getTotal(level, PopularizedStatus.HotSale);
            int hotUsedNum = getPopularizedNum(userId, PopularizedStatus.HotSale);

            return new UserInfo(level, totalNum, usedNum, totalNum - usedNum, username, award, isPopularOn,
                    hotTotalNum, hotUsedNum, hotTotalNum - hotUsedNum);
        }

        public static int remainNumWithPopularAward(User user, int status) {
            status = PopularizedStatusSqlUtil.checkStatus(status);

            int total = getTotal(user.getVersion(), status);
            //好评送推广
            if (user.isPopularAward() && status == PopularizedStatus.Normal) {
                total++;
            } else if (user.isPopularAward() && status == PopularizedStatus.Try) {
                total++;
            }

            if (status == PopularizedStatus.Normal || status == PopularizedStatus.Try) {
                status = PopularizedStatus.Normal + PopularizedStatus.Try;
            }
            int count = getPopularizedNum(user.getId(), status);
            return (total - count) > 0 ? (total - count) : 0;
        }

        private static int getPopularizedNum(Long userId, int status) {
            int count = (int) PopularizedDao.countPopularizedByUserIdAndStatus(userId, status);

            return count;
        }

        private static int getTotal(int level, int status) {
            if (status <= PopularizedStatus.Normal || status == PopularizedStatus.Try) {
                Map<Integer, Integer> verCountMap = APIConfig.get().getTuiguangCountMap();
                if (level <= Version.BLACK) {
                    return verCountMap.get(Version.BLACK);
                } else if (level <= Version.FREE) {
                    return verCountMap.get(Version.FREE);
                } else if (level <= Version.BASE) {
                    return verCountMap.get(Version.BASE);
                } else if (level <= Version.VIP) {
                    return verCountMap.get(Version.VIP);
                } else if (level <= Version.SUPER) {
                    return verCountMap.get(Version.SUPER);
                } else if (level <= Version.HALL) {
                    return verCountMap.get(Version.HALL);
                } else if (level <= Version.GOD) {
                    return verCountMap.get(Version.GOD);
                } else if (level <= Version.SUN) {
                    return verCountMap.get(Version.SUN);
                } else if (level <= Version.DAWEI) {
                    return verCountMap.get(Version.DAWEI);
                } else {
                    return verCountMap.get(Version.CUOCUO);
                }
            } else if (status == PopularizedStatus.HotSale) {
                Map<Integer, Integer> hotMap = APIConfig.get().getHotCountMap();
                Integer count = 0;
                if (level <= Version.BLACK) {
                    count = hotMap.get(Version.BLACK);
                } else if (level <= Version.FREE) {
                    count = hotMap.get(Version.FREE);
                } else if (level <= Version.BASE) {
                    count = hotMap.get(Version.BASE);
                } else if (level <= Version.VIP) {
                    count = hotMap.get(Version.VIP);
                } else if (level <= Version.SUPER) {
                    count = hotMap.get(Version.SUPER);
                } else if (level <= Version.HALL) {
                    count = hotMap.get(Version.HALL);
                } else if (level <= Version.GOD) {
                    count = hotMap.get(Version.GOD);
                } else if (level <= Version.SUN) {
                    count = hotMap.get(Version.SUN);
                } else if (level <= Version.DAWEI) {
                    count = hotMap.get(Version.DAWEI);
                } else {
                    count = hotMap.get(Version.CUOCUO);
                }
                if (count == null) {
                    return 0;
                } else {
                    return count;
                }
            } else {
                return 0;
            }

        }

        public UserInfo(int level, int totalNum, int popularizedNum, int remainNum, String username,
                boolean award, boolean isPopularOn, int hotTotalNum, int hotUsedNum, int hotRemainNum) {
            this.level = level;
            this.totalNum = totalNum;
            this.popularizedNum = popularizedNum;
            this.remainNum = remainNum;
            this.username = username;
            this.award = award;
            this.isPopularOn = isPopularOn;

            this.hotTotalNum = hotTotalNum;
            this.hotUsedNum = hotUsedNum;
            this.hotRemainNum = hotRemainNum;
        }

    }
    
    public static void isBD(){
        User user = getUser();
        
        FenXiangToken user_token=FenXiangToken.findByUserId(user.getId());
        
        StringBuilder sb = new StringBuilder();
        
        if(user_token==null){
            sb.append("TM.isSweiboBD = ");
            sb.append("false");
            sb.append(";\n");
            sb.append("TM.isqqBD = ");
            sb.append("false");
            sb.append(";\n");
            renderJs(sb.toString()); 
        }          
        sb.append("TM.isSweiboBD = ");
        sb.append(user_token.isSweiboBD());
        sb.append(";\n");
        sb.append("TM.isqqBD = ");
        sb.append(user_token.isqqBD());
        sb.append(";\n");
        renderJs(sb.toString());       
    }
    
    public static void weibobangding() throws WeiboException{
        User user=getUser();
        
        Oauth oauth = new Oauth();
        
        BareBonesBrowserLaunch.openURL(oauth.authorize("code","",""));
    }
    
    public static void AddWeiboBD(String code){
        User user = getUser();
        
        Oauth oauth = new Oauth();
        try{
            AccessToken token=oauth.getAccessTokenByCode(code);
            
            FenXiangToken user_token=FenXiangToken.findByUserId(user.getId());
            
            if(user_token==null){
                user_token=new FenXiangToken(user.getId());
            }
                      
            user_token.setSweibo_Token(token.getAccessToken());
            
            user_token.setSweiboBD(true);
            
            user_token.jdbcSave();
            
            render("/Popularize/yijianfenxiang.html");
            
        } catch (WeiboException e) {
            if(401 == e.getStatusCode()){
                Log.logInfo("Unable to get the access token.");
            }else{
                e.printStackTrace();
            }
        }
    }
    
    public static void removeWeiboBD(){
        
        User user = getUser();
        
        FenXiangToken user_token=FenXiangToken.findByUserId(user.getId());
        
        if(user_token==null){
            return;
        }
        
        user_token.setSweiboBD(false);
        
        user_token.jdbcSave();
    }
    
    public static void weibofenxiang(String statuses,String title,String picURL) throws IOException{
        
        User user=getUser();

        FenXiangToken user_token=FenXiangToken.findByUserId(user.getId());
        
        URL url=new URL(picURL);
        
        String filename=String.valueOf(user.getId())+".jpg";
//        byte[] bytes = FileUtils.readFileToByteArray(new File(""));
        
        File playfile = new File(Play.applicationPath,"tmp");
        File outFile=new File(playfile,filename);
        OutputStream os = new FileOutputStream(outFile);
        InputStream is = url.openStream();
        byte[] buff = new byte[1024];
        while(true) {
            int readed = is.read(buff);
            if(readed == -1) {
                break;
            }
            byte[] temp = new byte[readed];
            System.arraycopy(buff, 0, temp, 0, readed);
            os.write(temp);
        }
        is.close(); 
        os.close();
        
        try {
            try {
                byte[] content = readFileImage(outFile.getAbsolutePath());
                System.out.println("content length:" + content.length);
                ImageItem pic = new ImageItem("pic", content);
                String s = java.net.URLEncoder.encode(statuses, "utf-8");
                Timeline tl = new Timeline();
                tl.client.setToken(user_token.getSweibo_Token());// access_token
                Status status = tl.UploadStatus(s, pic);

                System.out.println("Successfully upload the status to ["
                        + status.getText() + "].");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception ioe) {
            System.out.println("Failed to read the system input.");
        }
         
        
    }
    
    public static byte[] readFileImage(String filename) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                new FileInputStream(filename));
        int len = bufferedInputStream.available();
        byte[] bytes = new byte[len];
        int r = bufferedInputStream.read(bytes);
        if (len != r) {
            bytes = null;
            System.out.println("读取文件不正确");
        }
        bufferedInputStream.close();
        return bytes;
    }
}
