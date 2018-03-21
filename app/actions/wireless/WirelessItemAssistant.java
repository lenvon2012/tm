
package actions.wireless;

import static java.lang.String.format;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.task.AutoTitleTask;
import models.task.AutoTitleTask.WireLessDetailConfig;
import models.user.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PlayUtil;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.bean.CsvToBean;
import au.com.bytecode.opencsv.bean.MappingStrategy;
import autotitle.ItemPropAction;
import carrier.WirelessAction;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

@JsonAutoDetect
public class WirelessItemAssistant extends Item {
    @JsonProperty
    int itemType = 1;

    @JsonProperty
    String propInput = null;

    public WirelessItemAssistant() {
    }

    public WirelessItemAssistant(Item item) {
        super(item);
        this.itemType = 1;
        List<String> list = ItemPropAction.splitPropList(item.getPropsName(), item.getPropertyAlias());
        this.propInput = StringUtils.join(list, "");
    }

    public String getPropInput() {
        return propInput;
    }

    public void setPropInput(String propInput) {
        this.propInput = propInput;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public static final Logger log = LoggerFactory.getLogger(WirelessItemAssistant.class);

    public static final String TAG = "ItemAssistant";

    public void doSome() {
        CsvToBean<WirelessItemAssistant> bean = new CsvToBean<WirelessItemAssistant>();
        MappingStrategy<WirelessItemAssistant> arg0 = new MappingStrategy<WirelessItemAssistant>() {

            @Override
            public PropertyDescriptor findDescriptor(int arg0) throws IntrospectionException {
                return null;
            }

            @Override
            public WirelessItemAssistant createBean() throws InstantiationException, IllegalAccessException {
                return null;
            }

            @Override
            public void captureHeader(CSVReader arg0) throws IOException {
            }
        };
    }

    public static class WireLessDescWritter {
        List<WirelessItemAssistant> list;

        Map<Long, WirelessItemAssistant> map = new HashMap<Long, WirelessItemAssistant>();

        User user;

        WireLessDetailConfig config;

        final Vector<String[]> vector = new Vector<String[]>();

        AutoTitleTask autoTasktask;

        public WireLessDescWritter(List<WirelessItemAssistant> list, User user, AutoTitleTask task) {
            super();
            this.list = list;
            this.user = user;
            this.config = task.genWirelessConfig();
            this.autoTasktask = task;

            vector.add(new String[] {
                    "version 1.00"
            });
            vector.add(new String[] {
                    WirelessItemField.NumIidField.getOne().getFieldName(),
                    WirelessItemField.WirelessDescField.getOne().getFieldName()
            });
            vector.add(new String[] {
                    WirelessItemField.NumIidField.getOne().getFieldChnName(),
                    WirelessItemField.WirelessDescField.getOne().getFieldChnName()
            });
        }

        int finishCount = 0;

        public static boolean writeToCsv(List<String[]> vector, File file) {

            log.info(format("writeToCsv:vector, file".replaceAll(", ", "=%s, ") + "=%s", vector, file));

            try {
                if (!file.exists()) {
                    file.createNewFile();
                }

                TBAssitantVer version = getVersion(vector);

                vector = vector.subList(3, vector.size());
                CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(file), "gbk"), '\t');
                csvWriter.writeAll(vector);
                csvWriter.close();

//                log.error("[version name :>>>>\n]" + version.name() + " \n<<<<<<<<");
                translate(file, version);

                return true;
            } catch (UnsupportedEncodingException e) {
                log.warn(e.getMessage(), e);

            } catch (IOException e) {
                log.warn(e.getMessage(), e);

            }

            return false;
        }

        private static void doReplace(StringBuilder sb, String content, String pattern, String src, String replace,
                int diff) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(content);
            int offset = 0;
            while (m.find()) {
                String group = m.group();
//                log.info("m start:" + m.start() + " >>> end :" + m.end() + " with offset :" + offset);
//                log.info("[>> replace :]" + sb.substring(m.start() + offset, m.end() + offset) + "[>> replace :]");
//                log.info("[<< replaced :]" + group.replaceAll(src, replace) + "[<<<<[replaces]");
                sb.replace(m.start() + offset, m.end() + offset, group.replaceAll(src, replace));
                offset -= diff;
            }
        }

        public static void translate(File file, TBAssitantVer ver) {
            try {
                String content = FileUtils.readFileToString(file, "gbk");
                StringBuilder sb = new StringBuilder(content);

                /**
                 * 各个版本纯数字的还是要换一下的
                 */
                doReplace(sb, sb.toString(), "\"(\\-)?[0-9\\.]+\"", "\"", "", 2);

                if (ver == TBAssitantVer.VER560) {
                    doReplace(sb, sb.toString(), "\t\"\"\t", "\"", "", 2);
                    doReplace(sb, sb.toString(), "\t\"\"\t", "\"", "", 2);
                    doReplace(sb, sb.toString(), "\t\"\"\n", "\"", "", 2);
                    doReplace(sb, sb.toString(), "\t\";+\"\t", "\"", "", 2);
                    doReplace(sb, sb.toString(), "\t\";+\"\t", "\"", "", 2);
                    doReplace(sb, sb.toString(), "\t\"[0-9;]+\"\t", "\"", "", 2);
                }

                String last1 = sb.substring(sb.length() - 1);
                String last2 = sb.substring(sb.length() - 2, sb.length() - 1);
                log.info(" last 1 :" + "\r".equals(last1) + " --> toN :" + "\n".equals(last1) + " toT:"
                        + "\t".equals(last1) + " toSp:" + " ".equals(last1));
                log.info(" last 2 :" + "\r".equals(last2) + " --> toN :" + "\n".equals(last2) + " toT:"
                        + "\t".equals(last2) + " toSp:" + " ".equals(last2));

//                File headerFile = new File(TMConfigs.autoDir, ver.getVer() + ".head.json");
//                String headerContent = FileUtils.readFileToString(headerFile);
                String headerContent = ver.getHeader();
//            log.info("[after replace:]" + sb.toString());
                sb.insert(0, headerContent);

                FileUtils.writeStringToFile(file, sb.toString(), "gbk");
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        }

        private class WirelessDescMaker implements Callable<Boolean> {

            WirelessItemAssistant wItem;

            public WirelessDescMaker(WirelessItemAssistant wItem) {
                super();
                this.wItem = wItem;
            }

            @Override
            public Boolean call() throws Exception {
                String shortText = wItem.getTitle();
                if (wItem.getPropInput() != null) {
                    shortText += "\n" + wItem.getPropInput();
                }
                shortText.replaceAll(",", StringUtils.EMPTY);
                shortText.replaceAll("\n", StringUtils.EMPTY);

                String wirelessDesc = WirelessAction.translateWirelessDesc(user, shortText, wItem.getDesc());
                vector.add(new String[] {
                        String.valueOf(wItem.getNumIid()), wirelessDesc
                });
                return Boolean.TRUE;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/home/zrb/code/tm/tmp/wireless/output/877/王长峰19660424_073903.csv");
        WireLessDescWritter.translate(file, TBAssitantVer.VER560);
    }

    public enum TBAssitantVer {
        
        VER560(
                "5.6", WirelessFieldLoader.loader61, "version 1.00\n" +
                        "title\tcid\tseller_cids\tstuff_status\tlocation_state\tlocation_city\titem_type\t" +
                        "price\tauction_increment\tnum\tvalid_thru\tfreight_payer\tpost_fee\tems_fee\t" +
                        "express_fee\thas_invoice\thas_warranty\tapprove_status\thas_showcase\tlist_time\t" +
                        "description\tcateProps\tpostage_id\thas_discount\tmodified\tupload_fail_msg\t" +
                        "picture_status\tauction_point\tpicture\tvideo\tskuProps\tinputPids\tinputValues\t" +
                        "outer_id\tpropAlias\tauto_fill\tnum_id\tlocal_cid\tnavigation_type\tuser_name\t" +
                        "syncStatus\tis_lighting_consigment\tis_xinpin\tfoodparame\tfeatures\tbuyareatype\t" +
                        "global_stock_type\tglobal_stock_country\tsub_stock_type\titem_size\titem_weight\t" +
                        "sell_promise\tcustom_design_flag\twireless_desc\tbarcode\tsku_barcode\tnewprepay\t" +
                        "subtitle\n" +
                        "宝贝名称\t宝贝类目\t店铺类目\t新旧程度\t省\t城市\t出售方式\t宝贝价格\t加价幅度\t宝贝数量\t" +
                        "有效期\t运费承担\t平邮\tEMS\t快递\t发票\t保修\t放入仓库\t橱窗推荐\t开始时间\t宝贝描述\t宝贝属性\t" +
                        "邮费模版ID\t会员打折\t修改时间\t上传状态\t图片状态\t返点比例\t新图片\t视频\t销售属性组合\t" +
                        "用户输入ID串\t用户输入名-值对\t商家编码\t销售属性别名\t代充类型\t数字ID\t本地ID\t宝贝分类\t" +
                        "用户名称\t宝贝状态\t闪电发货\t新品\t食品专项\t尺码库\t采购地\t库存类型\t国家地区\t库存计数\t" +
                        "物流体积\t物流重量\t退换货承诺\t定制工具\t无线详情\t商品条形码\tsku 条形码\t7天退货\t宝贝卖点\n"),
        VER550(
                "5.5", WirelessFieldLoader.loader50, "version 1.00\n" +
                        "title\tcid\tseller_cids\tstuff_status\tlocation_state\tlocation_city\t" +
                        "item_type\tprice\tauction_increment\tnum\tvalid_thru\tfreight_payer\t" +
                        "post_fee\tems_fee\texpress_fee\thas_invoice\thas_warranty\tapprove_status\t" +
                        "has_showcase\tlist_time\tdescription\tcateProps\tpostage_id\thas_discount\t" +
                        "modified\tupload_fail_msg\tpicture_status\tauction_point\tpicture\tvideo\t" +
                        "skuProps\tinputPids\tinputValues\touter_id\tpropAlias\tauto_fill\tnum_id\t" +
                        "local_cid\tnavigation_type\tuser_name\tsyncStatus\tis_lighting_consigment\t" +
                        "is_xinpin\tfoodparame\tfeatures\tbuyareatype\tglobal_stock_type\tglobal_stock_country\t" +
                        "sub_stock_type\titem_size\titem_weight\tsell_promise\tcustom_design_flag\t" +
                        "wireless_desc\tbarcode\tsku_barcode\tnewprepay\n" +
                        "宝贝名称\t宝贝类目\t店铺类目\t新旧程度\t省\t城市\t出售方式\t宝贝价格\t加价幅度\t" +
                        "宝贝数量\t有效期\t运费承担\t平邮\tEMS\t快递\t发票\t保修\t放入仓库\t橱窗推荐\t开始时间\t" +
                        "宝贝描述\t宝贝属性\t邮费模版ID\t会员打折\t修改时间\t上传状态\t图片状态\t返点比例\t" +
                        "新图片\t视频\t销售属性组合\t用户输入ID串\t用户输入名-值对\t商家编码\t销售属性别名\t" +
                        "代充类型\t数字ID\t本地ID\t宝贝分类\t用户名称\t宝贝状态\t闪电发货\t新品\t食品专项\t" +
                        "尺码库\t采购地\t库存类型\t国家地区\t库存计数\t物流体积\t物流重量\t退换货承诺\t定制工具\t" +
                        "无线详情\t商品条形码\tsku 条形码\t7天退货\n");

        private String ver;

        private String header;

        private WirelessFieldLoader loader;

        private TBAssitantVer(String ver, WirelessFieldLoader loader, String header) {
            this.ver = ver;
            this.loader = loader;
            this.header = header;
        }

        public String getVer() {
            return ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public static TBAssitantVer getByStr(String str) {
            if (VER550.getVer().equals(str)) {
                return VER550;
            } else if (VER560.getVer().equals(str)) {
                return VER560;
            }
            return VER560;

        }

        public WirelessFieldLoader getLoader() {
            return loader;
        }

        public void setLoader(WirelessFieldLoader loader) {
            this.loader = loader;
        }

    }

    public static TBAssitantVer getVersion(List<String[]> vector) {
        if (CommonUtils.isEmpty(vector) || vector.size() < 2) {
            log.warn(" not valid string args:");
            PlayUtil.infoListStringArr(vector);
            return TBAssitantVer.VER560;
        }

        String[] header = vector.get(1);
        if (StringUtils.join(header).contains("subtitle")) {
            return TBAssitantVer.VER560;
        } else {
            return TBAssitantVer.VER550;
        }

    }

}
