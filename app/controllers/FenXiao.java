
package controllers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import job.fenxiao.CooperationSyncJob;
import models.fenxiao.AutoCatRule;
import models.fenxiao.CooperationPlay;
import models.fenxiao.ItemDescLinks;
import models.fenxiao.ItemDescLinks.ActionType;
import models.fenxiao.ItemDescPlay;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import result.TMResult;
import utils.TBItemUtil;
import utils.TaobaoUtil;
import actions.batch.BatchEditResult;
import actions.batch.OutLinksGetAction;
import actions.batch.RemoveLinksAction;
import bustbapi.FenxiaoApi.FXScItemApi;
import bustbapi.ItemApi;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.url.URLParser;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.FenxiaoSku;
import com.taobao.api.domain.Item;

import configs.TMConfigs.App;
import controllers.Op.InviteInfo;
import controllers.SkinBatch.BatchOpMessage;
import dao.fenxiao.AutoCatRuleDao;
import dao.fenxiao.AutoCatRuleDao.CatIdNameCountRule;
import dao.fenxiao.ItemDescDao;
import dao.item.ItemDao;

public class FenXiao extends TMController {

    public static void autoTitle() {
        render("fenxiao/autoTitle.html");
    }

    public static void rmlinks() {
        render("fenxiao/rmlinks.html");
    }

    public static void autocat() {
        render("fenxiao/autocat.html");
    }

    public static void comment() {
        render("fenxiao/helicomment.html");
    }

    public static void delist() {
        render("fenxiao/helidelist.html");
    }

    public static void window() {
        render("fenxiao/heliwindow.html");
    }

    public static void diag() {
        render("fenxiao/autodiag.html");
    }

    public static void invite() {
        render("op/fenxiaoinvites.html");
    }

    public static void recover() {
        render("fenxiao/recover.html");
    }

    public static void batchmulti() {
        render("fenxiao/multiModify.html");
    }

    public static void batchTitle() {
        render("fenxiao/batchTitle.html");
    }

    public static void award() {
        render("fenxiao/award.html");
    }

    public static void batchDelist() {
        render("fenxiao/batch.html");
    }

    public static void batchPrice() {
        render("fenxiao/batchPrice.html");
    }
    
    
    public static void lottery() {
        render("lottery/lotteryfenxiao.html");
    }

    public static void mywords() {
        render("fenxiao/mywords.html");
    }

    public static void seawords() {
        render("fenxiao/seawords.html");
    }

    public static void delistPlans() {
        render("fenxiao/delistPlans.html");
    }

    public static void delistCreate() {
        render("fenxiao/delistCreate.html");
    }

    public static void delistPlanDetail(long planId) {
        render("fenxiao/delistPlanDetail.html");
    }

    public static void genInviteUrl() {
        User user = getUser();
        InviteInfo info = new InviteInfo();
        log.info("[user : ]" + user);
        String url = "http://" + request.host + "/fenxiaoinvite/" + user.getId();
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }

    public static void tobeFenxiao(boolean toBeOn) {
        User user = getUser();
        user.setFenxiaoOn(toBeOn);
        boolean isSuccess = user.jdbcSave();
        if (isSuccess) {
            renderText("设置成功");
        } else {
            renderText("设置失败");
        }
    }

    public static void test() {
//        Item call = new ItemApi.SingleItemGet(18428446438L).call();
//        System.out.println(call.getProps());
//        System.out.println(call.getPropsName());
//        System.out.println(call);

        CooperationSyncJob.syncUserCooperation(getUser());
    }

    public static void checkLinks(int pn) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, 10);
        List<ItemPlay> itemList = ItemDao.findByUserId(user.getId(), po);
        
        if (pn == 1) {
            // 删除现有记录
            ItemDescLinks.deleteLinksByUserId(user.getId());
            ItemDescDao.deleteItemDesc(user.getId());
        }

        Set<Long> numIidSet = new HashSet<Long>();
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            numIidSet.add(item.getNumIid());
        }
        
        Map<Long, Item> tbItemMap = TBItemUtil.findItemDescMap(user, numIidSet);

        List<ItemDescLinks> newAddDescLinkList = OutLinksGetAction.doCheckItemOutLinks(user, 
                tbItemMap.values(), false);
        

        TMResult res = new TMResult(newAddDescLinkList, itemList.size(), po);

        renderJSON(JsonUtil.getJson(res));
    }

    public static void updateLinkAction(String ids, Long actionType) {
        User user = getUser();
        if (StringUtils.isEmpty(ids)) {
            renderSuccessJson("更新链接成功");
        }
        if (ids.endsWith(",")) {
            ids = ids.substring(0, ids.length() - 1);
        }
        Boolean res = ItemDescLinks.updateLinkAction(user.getId(), ids, actionType);
        if (res == Boolean.FALSE) {
            renderFailedJson("更新出错，请稍后再试");
        }
        renderSuccessJson("更新成功");
    }

    public static void updateLinkActionSingle(Long id, Long actionType) {
        User user = getUser();
        Boolean res = ItemDescLinks.updateLinkActionSingle(user.getId(), id, actionType);
        if (res == Boolean.FALSE) {
            renderFailedJson("更新出错，请稍后再试");
        }
        renderSuccessJson("更新成功");
    }

    public static void listRemoveLinks(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);

        List<ItemDescLinks> list = ItemDescLinks.findLinksByUserId(user.getId());
        if (CommonUtils.isEmpty(list)) {
            renderJSON(JsonUtil.getJson(new TMResult(null, 0, po)));
        }

        HashMap<String, Long> linkAction = new HashMap<String, Long>();
        for (ItemDescLinks itemDescLink : list) {
            linkAction.put(itemDescLink.getLink(), itemDescLink.getAction());
        }

        TMResult res = ItemDescDao.findTmResult(user.getId(), po);
        List<ItemDescPlay> itemDescList = (List<ItemDescPlay>) res.getRes();

        if (!CommonUtils.isEmpty(itemDescList)) {
            for (ItemDescPlay itemDescPlay : itemDescList) {
                itemDescPlay.checkLinkActionMap(linkAction);
            }
        }

        renderJSON(JsonUtil.getJson(res));
    }

    public static void doRemoveLinks(String numIids) {
        User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            renderSuccessJson("请先选择需要修改的宝贝！");
        }
        if (numIids.endsWith(",")) {
            numIids = numIids.substring(0, numIids.length() - 1);
        }

        List<ItemDescLinks> list = ItemDescLinks.findLinksByUserId(user.getId());

        if (CommonUtils.isEmpty(list)) {
            renderSuccessJson("没有需要去除的链接哦！");
        }

        List<ItemDescPlay> itemDescList = ItemDescDao.findByUserIdNumIids(user.getId(), numIids);
        /*for (ItemDescPlay itemDescPlay : itemDescList) {
            String desc = itemDescPlay.getDesc();
            for (ItemDescLinks itemDescLinks : list) {
                String processLink = itemDescLinks.getLink().replace("?", "\\?").replace("|", "\\|")
                        .replace("[", "\\[").replace("]", "\\]");
                desc = desc.replaceAll("href=('|\")?" + processLink + "('|\")?", StringUtils.EMPTY);
                desc = desc.replaceAll(processLink, StringUtils.EMPTY);
            }
            if (!StringUtils.equals(desc, itemDescPlay.getDesc())) {
                new ItemApi.ItemUpdate(user.getSessionKey(), itemDescPlay.getNumIid(), desc).call();
                itemDescPlay.setDesc(desc);
                itemDescPlay.setStatus(1L);
                itemDescPlay.rawUpdate();
            }
        }
        renderSuccessJson("去外链操作成功！");*/
        
        BatchEditResult removeRes = RemoveLinksAction.doRemoveItemLinks(user, itemDescList, list);
        
        if (StringUtils.isEmpty(removeRes.getMessage())) {
            renderSuccessJson("去外链操作成功！");
        } else {
            renderSuccessJson(removeRes.getMessage());
        }
    }

    public static void doRemoveAllLinks() {
        User user = getUser();
        List<ItemDescLinks> list = ItemDescLinks.findLinksByUserId(user.getId());
        if (CommonUtils.isEmpty(list)) {
            renderSuccessJson("没有需要去除的链接哦！");
        }

        List<ItemDescPlay> itemDescList = ItemDescDao.findByUserId(user.getId());
        /*for (ItemDescPlay itemDescPlay : itemDescList) {
            String desc = itemDescPlay.getDesc();
            for (ItemDescLinks itemDescLinks : list) {
                String processLink = itemDescLinks.getLink().replace("?", "\\?").replace("|", "\\|")
                        .replace("[", "\\[").replace("]", "\\]");
                desc = desc.replaceAll("href=('|\")?" + processLink + "('|\")?", StringUtils.EMPTY);
                desc = desc.replaceAll(processLink, StringUtils.EMPTY);
            }
            if (!StringUtils.equals(desc, itemDescPlay.getDesc())) {
                itemDescPlay.setDesc(desc);
                itemDescPlay.setStatus(1L);
                itemDescPlay.rawUpdate();
                new ItemApi.ItemUpdate(user.getSessionKey(), itemDescPlay.getNumIid(), desc).call();
            }
        }
        renderSuccessJson("去外链操作成功！");*/
        
        BatchEditResult removeRes = RemoveLinksAction.doRemoveItemLinks(user, itemDescList, list);
        
        if (StringUtils.isEmpty(removeRes.getMessage())) {
            renderSuccessJson("去外链操作成功！");
        } else {
            renderSuccessJson(removeRes.getMessage());
        }
    }

    public static void getAutoCatRule(Long cid) {
        User user = getUser();
        AutoCatRule rule = AutoCatRuleDao.findAutoCatRule(user.getId(), cid);
        renderJSON(JsonUtil.getJson(rule));
    }

    public static void updateAutoCatRule(Long cid, String words, String brand, String attr, String supplier) {
        User user = getUser();
        AutoCatRule rule = new AutoCatRule(user.getId(), cid, words);
        rule.setBrand(brand);
        rule.setAttr(attr);
        rule.setSupplier(supplier);
        rule.jdbcSave();
        renderSuccessJson();
    }

    public static void listCatRule() {
        User user = getUser();
        List<CatIdNameCountRule> list = AutoCatRuleDao.findCatsCountRule(user);
        renderJSON(JsonUtil.getJson(list));
    }

    public static void doAutoCatAlone(Long cid, int pn) {
        if (cid == null || cid <= 0) {
            renderJSON(TMResult.renderMsg("没有类目信息"));
        }
        User user = getUser();
        PageOffset po = new PageOffset(pn, 10);
        List<ItemPlay> items = ItemDao.findAllByUser(user.getId(), po.getOffset(), po.getPs(), null, 0);
        if (CommonUtils.isEmpty(items)) {
            renderJSON(new TMResult(null, 0, po));
        }
        AutoCatRule rule = AutoCatRuleDao.findAutoCatRule(user.getId(), cid);
        if (rule == null) {
            renderJSON(new TMResult(null, items.size(), po));
        }

        for (ItemPlay item : items) {
            Boolean res = AutoCatRuleDao.checkAutoCatRule(user, rule, item);
            if (res == Boolean.TRUE) {
                log.info("call ItemCidUpdater = " + item.getNumIid());
                new ItemApi.ItemCidUpdater(user.getSessionKey(), item.getNumIid(), cid).call();
            }
        }
        renderJSON(new TMResult(null, items.size(), po));
    }

    public static void doAutoCatAll(int pn) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, 10);
        List<ItemPlay> items = ItemDao.findAllByUser(user.getId(), po.getOffset(), po.getPs(), null, 0);
        if (CommonUtils.isEmpty(items)) {
            renderJSON(new TMResult(null, 0, po));
        }
        List<AutoCatRule> rules = AutoCatRuleDao.findCatsRule(user.getId());
        if (CommonUtils.isEmpty(rules)) {
            renderJSON(new TMResult(null, items.size(), po));
        }
        for (ItemPlay item : items) {
            for (AutoCatRule rule : rules) {
                Boolean res = AutoCatRuleDao.checkAutoCatRule(user, rule, item);
                if (res == Boolean.TRUE) {
                    log.info("call ItemCidUpdater = " + item.getNumIid());
                    new ItemApi.ItemCidUpdater(user.getSessionKey(), item.getNumIid(), rule.getCatId()).call();
                    break;
                }
            }
        }
        renderJSON(new TMResult(null, items.size(), po));
    }

    public static void listItems(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), po);
        long count = ItemDao.countByUser(user.getId());
        TMResult res = new TMResult(list, (int) count, po);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void updateAllFenxiaoPrice(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), po);
        for (ItemPlay itemPlay : list) {
            FenxiaoProduct product = new FXScItemApi(user, itemPlay.getId()).call();
            if (product == null) {
                continue;
            }
            List<FenxiaoSku> skus = product.getSkus();
            if (CommonUtils.isEmpty(skus)) {
                continue;
            }
            double standardPrice = Double.valueOf(skus.get(0).getStandardPrice());
            if (itemPlay.getPrice() != standardPrice) {
                itemPlay.setPrice(standardPrice);
                itemPlay.rawUpdate();

                ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(),
                        itemPlay.getNumIid(), skus.get(0).getStandardPrice());
                updateApi.call();
            }
        }
        renderJSON(JsonUtil.getJson(new TMResult(null, list.size(), po)));
    }

    public static void updateFenxiaoLowPrice(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), po);
        for (ItemPlay itemPlay : list) {
            FenxiaoProduct product = new FXScItemApi(user, itemPlay.getId()).call();
            if (product == null) {
                continue;
            }

            double standardPrice = Double.valueOf(product.getRetailPriceLow());
            if (itemPlay.getPrice() != standardPrice) {
                itemPlay.setPrice(standardPrice);
                itemPlay.rawUpdate();

                ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(),
                        itemPlay.getNumIid(), product.getRetailPriceLow());
                updateApi.call();
            }
        }
        renderJSON(JsonUtil.getJson(new TMResult(null, list.size(), po)));
    }

    public static void updateFenxiaoHighPrice(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), po);
        for (ItemPlay itemPlay : list) {
            FenxiaoProduct product = new FXScItemApi(user, itemPlay.getId()).call();
            if (product == null) {
                continue;
            }

            double standardPrice = Double.valueOf(product.getRetailPriceHigh());
            if (itemPlay.getPrice() != standardPrice) {
                itemPlay.setPrice(standardPrice);
                itemPlay.rawUpdate();

                ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(),
                        itemPlay.getNumIid(), product.getRetailPriceHigh());
                updateApi.call();
            }
        }
        renderJSON(JsonUtil.getJson(new TMResult(null, list.size(), po)));
    }

    public static void doModifyPrice(List<Long> numIidList, String newPriceStr) {
        BigDecimal newPrice = new BigDecimal(0);
        try {
            newPrice = new BigDecimal(newPriceStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("请先输入正确的价格格式");
        }
        if (newPrice.compareTo(new BigDecimal(0)) <= 0) {
            renderError("请先输入正确的价格");
        }
        if (newPrice.compareTo(new BigDecimal(100000000)) > 0) {
            renderError("价格不得大于100000000元");
        }
        // 转换成小数的两位
        newPrice = newPrice.multiply(new BigDecimal(100));
        int integerPrice = newPrice.intValue();
        newPrice = new BigDecimal(integerPrice).divide(new BigDecimal(100));

        User user = getUser();
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        // 检查w2权限
        checkW2Expires(user, 60);

        List<Long> successList = new ArrayList<Long>();
        Map<Long, String> errorMap = new HashMap<Long, String>();

        for (Long numIid : numIidList) {
            ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(), numIid,
                    newPrice.toString());
            updateApi.call();
            boolean isSuccess = updateApi.isApiSuccess();
            if (isSuccess == true) {
                successList.add(numIid);
                // errorList.add(numIid);//测试错误
            } else {
                String errorMsg = updateApi.getErrorMsg();
                errorMap.put(numIid, errorMsg);
            }
        }

        // 更新itemPlay
        List<ItemPlay> successItemList = ItemDao.findByNumIids(user.getId(), successList);
        for (ItemPlay itemPlay : successItemList) {
            itemPlay.setPrice(newPrice.doubleValue());
            itemPlay.jdbcSave();
        }

        String message = "成功修改" + successItemList.size() + "个宝贝的价格";
        if (errorMap.size() > 0) {
            message += "，失败" + errorMap.size() + "个，点击确定后查看详情";
        }
        List<ItemPlay> errorItemList = ItemDao.findByNumIids(user.getId(), errorMap.keySet());
        List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
        for (ItemPlay itemPlay : errorItemList) {
            BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "修改价格失败，" + errorMap.get(itemPlay.getNumIid()));
            batchOpMsgList.add(batchOpMsg);
        }

        renderSuccess(message, batchOpMsgList);
    }

    public static void doUpdateAllPrice(int pn, int ps) {
        User user = getUser();
        checkW2Expires(user, 60);

        PageOffset po = new PageOffset(pn, ps);
        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), po);
        for (ItemPlay itemPlay : list) {
            ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(),
                    itemPlay.getNumIid(), String.valueOf(itemPlay.getPrice()));
            updateApi.call();
        }
        renderJSON(JsonUtil.getJson(new TMResult(null, list.size(), po)));
    }

    private static void checkW2Expires(User user, int expireTime) {
        String w2TimeStr = TaobaoUtil.getRefreshResponseProperty(user, TaobaoUtil.W2_EXPIRES_IN);
        boolean isAuthorized = true;
        if (StringUtils.isEmpty(w2TimeStr))
            isAuthorized = false;
        else {
            long w2Time = NumberUtil.parserInt(w2TimeStr, 0);
            if (w2Time <= expireTime) {// 小于120秒
                isAuthorized = false;
            }
        }
        if (isAuthorized == false) {

            String authUrl = App.TAOBAO_AUTH_URL;
            String redirectUrl = APIConfig.get().getRedirURL() + "/in/authPrice";

            try {
                redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
                renderError("系统出现一些异常，请联系我们！");
            }

            authUrl += "&redirect_uri=" + redirectUrl;

            renderAuthorizedError("", authUrl);
        }
    }

    public static void showSupplier() {
        User user = getUser();
        List<CooperationPlay> list = CooperationPlay.findByUserId(user.getId());
        renderJSON(JsonUtil.getJson(list));
    }

    public static void checkAutoCatExist(Long cid) {
        User user = getUser();
        if (cid != null && cid > 0) {
            AutoCatRule rule = AutoCatRuleDao.findAutoCatRule(user.getId(), cid);
            if (AutoCatRuleDao.checkRuleEmpty(rule)) {
                renderText("notexist");
            }
        } else {
            List<AutoCatRule> list = AutoCatRuleDao.findCatsRule(user.getId());
            if (CommonUtils.isEmpty(list)) {
                renderText("notexist");
            }

            boolean flag = false;
            for (AutoCatRule autoCatRule : list) {
                if (!AutoCatRuleDao.checkRuleEmpty(autoCatRule)) {
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                renderText("notexist");
            }
        }
        renderText("exist");
    }

}
