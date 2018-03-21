
package actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import actions.DiagAction.BatchResultMsg;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemUpdate;
import bustbapi.ItemApi.ItemsOnsale;
import bustbapi.TBApi;
import bustbapi.TmallItem.TmallItemDescUpdater;

import com.ciaosir.client.utils.HtmlUtil;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

public class TemplateAction {

    private static final Logger log = LoggerFactory.getLogger(TemplateAction.class);

    public static final String TAG = "TemplateAction";

    public static BatchResultMsg updateItem(User user, Long numIid, String newHtml) {
        ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid, newHtml);
        api.call();
        String errorMsg = api.getErrorMsg();  
        BatchResultMsg msg = null;
        if (StringUtils.isEmpty(errorMsg) && api.isApiSuccess()) {
            msg = new BatchResultMsg(true, null, numIid);
        } else {
            msg = new BatchResultMsg(false, errorMsg, numIid);
        }
        return msg;
    }

    /**
     * 天猫、淘宝用户获取宝贝描述信息
     *
     * @param user   用户
     * @param numIid 宝贝ID
     * @return 宝贝描述
     */
    public static String getItemDesc(User user, Long numIid) {
        if (user.isTmall()) { // 天猫用户
            // 请求接口(tmall.item.increment.update.schema.get)获取增量更新商品规则
            String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><itemParam><field id=\"update_fields\" name=\"商品描述\" type=\"multiCheck\"><values><value>title</value><value>description</value></values></field></itemParam>";
            String descXmlString = new ItemApi.TmallItemIncrementUpdateSchemaGet(user, numIid, xmlData).call();
            if (StringUtils.isEmpty(descXmlString)) return StringUtils.EMPTY;

            // 提取接口(tmall.item.increment.update.schema.get)获取规则数据中的原宝贝描述
            Pattern pattern = Pattern.compile("<default-complex-values>(.*)</default-complex-values>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(descXmlString);
            if (!matcher.find()) return StringUtils.EMPTY;
            String complexValues = matcher.group(1);

            // 拼接成接口(tmall.item.schema.increment.update)参数(xmlData)要求的格式
            String xmlDataBefore = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                    "<itemRule>" +
                    "<field id=\"update_fields\" name=\"更新字段列表\" type=\"multiCheck\">" +
                    "<values>" +
                    "<value>description</value>" +
                    "</values>" +
                    "</field>" +
                    "<field id=\"description\" name=\"商品描述\" type=\"complex\">" +
                    "<complex-values>";
            String xmlDateAfter = "</complex-values></field></itemRule>";
            String desc = xmlDataBefore + complexValues + xmlDateAfter;

            return TemplateAction.unescape(desc);
        } else { // 淘宝用户
            try {
                Item item = new ItemApi.ItemDescGet(user, numIid).call();
                if (item == null) {
                    return null;
                }
                return item.getDesc();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return null;
            }
        }
    }

    public static BatchResultMsg updateItemDesc(User user,Long numIid,String newHtml){
        TBApi api = null;
        Object em = null;
        String sessionKey = user.getSessionKey();
        if(user.isTmall()){
            // 天猫用户
            api = new TmallItemDescUpdater(sessionKey, numIid, escapeDescBeforeTmallUpdate(newHtml));
            em = api.call();
        } else {
            api = new ItemApi.ItemUpdate(sessionKey, numIid, newHtml);
            em = api.call();
        }
        BatchResultMsg msg = null;
        
        if(em == null || !api.isApiSuccess()){
            msg = new BatchResultMsg(false,null,null,"系统繁忙,请稍后再试",null);
        }
        String errorMsg = api.getErrorMsg();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        
        if(item == null){
            msg = new BatchResultMsg(false,000000L,"亲，您的宝贝已经删除",errorMsg,"");
        }else{
            if(StringUtils.isEmpty(errorMsg) && api.isApiSuccess()){
                msg = new BatchResultMsg(true,numIid,item.title,null,item.picURL);
            }else{
                msg = new BatchResultMsg(false,numIid,item.title,errorMsg,item.picURL);
            }
        }
        return msg;
    }

    // 在增量更新天猫描述前 转义xmlData中的html
    public static String escapeDescBeforeTmallUpdate(String xmlData) {
        Document document = Jsoup.parse(xmlData, "", Parser.xmlParser());
        document.outputSettings(document.outputSettings().prettyPrint(false));
        // 获取自定义描述field
        String cssQuery1 = "#description field[id^=desc_module_][id$=_mods]";
        // 获取非自定义描述field
        String cssQuery2 = "#description field[id^=desc_module_][id$=_mod]";
        Elements descriptionFields = document.select(cssQuery1 + "," + cssQuery2);
        if (descriptionFields.isEmpty()) return null;
        for (Element descriptionField : descriptionFields) {
            Element contentValue = descriptionField.select("field[id$=_mod_content]>value").first();
            // 待处理html
            String html = contentValue.html();
            // >和< 转义处理
            html = escape(html);
            // &nbsp; 反转义处理
            html = html.replace("&nbsp;", " ");

            contentValue.html(html);
        }

        return document.html();
    }

    // 反转义字符串
    public static String unescape(String html) {
        return html.replace("&gt;", ">").replace("&lt;", "<");
    }

    // 转义字符串
    public static String escape(String html) {
        return html.replace(">", "&gt;").replace("<", "&lt;");
    }


    public static void removeMonitorInstall(User user) {
        ItemMonitorInstaller installer = new ItemMonitorInstaller(user, true);
        installer.doJob();
    }

    //暂时先这样。。。。
    public static void doInstallItemMonitor(User user) {
        ItemMonitorInstaller installer = new ItemMonitorInstaller(user, false);
        installer.now();
    }

    public static class ItemMonitorInstaller extends Job<List<BatchResultMsg>> {

        private boolean isRemoveInstall = false;

        User user;

        public ItemMonitorInstaller(User user, boolean isRemoveInstall) {
            super();
            this.user = user;
            this.isRemoveInstall = isRemoveInstall;
        }

        @Override
        public void doJob() {
            this.doJobWithResult();
        }

        public List<BatchResultMsg> doJobWithResult() {
            /**
             *  淘宝已经禁止了第三方图片的安装，目前我们已经没有别的办法了
             *  9.2 试了一下，目测还是可以的。。。 
             */
            if (true) {
                return ListUtils.EMPTY_LIST;
            }

//            Set<Long> ids = ItemDao.findNumIidWithUser(user.getId());
            Set<Long> ids = new HashSet<Long>();
            List<Item> call = new ItemsOnsale(user, null, null).call();

            for (Item item : call) {
                ids.add(item.getNumIid());
            }

//            List<BatchResultMsg> res = installItemOnByOne(ids);
            //}
            List<BatchResultMsg> res = installBatchItem(ids);

            return res;
        }

        public List<BatchResultMsg> installBatchItem(Set<Long> ids) {
            String sid = user.getSessionKey();
            List<Item> items = new ItemApi.MultiItemsListGet(sid, ids, ItemApi.FIELDS_ONLY_ID_DESC).call();
            List<BatchResultMsg> res = new ArrayList<BatchResultMsg>();
            for (Item item : items) {
                doForSingleItem(res, item);
            }
            return res;
        }

        public List<BatchResultMsg> installItemOnByOne(Set<Long> ids) {
            Iterator<Long> iterator = ids.iterator();
            List<BatchResultMsg> res = new ArrayList<BatchResultMsg>();
            while (iterator.hasNext()) {
                Long id = iterator.next();
                //if (id.equals(MonitorTestData.TEST_ITEM_ID)) {

                try {
                    doForSingleItem(id, res);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }

            }
            return res;
        }

        private void doForSingleItem(Long numIid, List<BatchResultMsg> res) {
            Item item = new ItemApi.ItemDescGet(user, numIid).call();
            if (item == null) {
                return;
            }
            //log.error("item : " + item.getNumIid());

            doForSingleItem(res, item);
        }

        private void doForSingleItem(List<BatchResultMsg> res, Item item) {
            if (item == null) {
                return;
            }
            Long numIid = item.getNumIid();
            String desc = item.getDesc();
            if (isRemoveInstall == true) {//删除安装
                boolean flag = HtmlUtil.isNeedToAddImg(desc, HtmlUtil.MONITOR_REPLACE_PATH);
                if (flag == false) {//如果需要删除安装
                    log.error("item : " + item.getNumIid());
                    String newHtml = HtmlUtil.replaceAll(desc, HtmlUtil.MONITOR_REPLACE_PATH);
                    res.add(updateItem(user, numIid, newHtml));
                }
            } else {//安装监控
                boolean flag = HtmlUtil.isNeedToAddImg(desc, HtmlUtil.MONITOR_REPLACE_PATH);
                if (flag == true) {
                    //desc = HtmlUtil.replaceAll(desc);
                    log.info("item : " + item.getNumIid());
                    //if (desc.length() > 12000) {
                    desc = HtmlUtil.deleteBlank(desc);
                    //}
                    String newHtml = HtmlUtil.insertTemplate(user.getId(), numIid, desc);

                    res.add(updateItem(user, numIid, newHtml));
                }
            }
        }
    }

    //关联宝贝

    //测试的淘宝店铺
    public static class MonitorTestData {
        public static String SID = "6101417110f0c5635fe9144271a406abf94b089897796ce80637167";

        public static Long TEST_ITEM_ID = 19853712911L;

        public static String NICK = "starsky_101";
    }

}
