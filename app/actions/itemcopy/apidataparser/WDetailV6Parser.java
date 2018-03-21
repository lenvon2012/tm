package actions.itemcopy.apidataparser;

import actions.itemcopy.model.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import proxy.CommonProxyPools;
import actions.itemcopy.apidataparser.WDetailV6DataBean.*;
import utils.NewProxyToolsUtils;

import java.util.*;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class WDetailV6Parser {

    private static final Logger log = LoggerFactory.getLogger(WDetailV6Parser.class);

    Long numIid;

    WDetailV6DataBean dataBean;

    ResultBean resultBean = new ResultBean();

    public WDetailV6Parser(Long numIid) {
        this.numIid = numIid;
        this.dataBean = requestWDetailAPI(numIid);
    }

    private WDetailV6DataBean requestWDetailAPI(Long numIid) {
        for (int i = 0; i < 10; i++) {
            String url = "http://h5api.m.taobao.com/h5/mtop.taobao.detail.getdetail/6.0/?jsv=2.4.8&appKey=12574478&t=1510293497543&sign=1d715afb87fec7f7fe7afa5f6dc601df&api=mtop.taobao.detail.getdetail&v=6.0&ttid=2016%2540taobao_h5_2.0.0&isSec=0&ecode=0&AntiFlood=true&AntiCreep=true&H5Request=true&type=jsonp&dataType=jsonp&callback=&data=%7B%22itemNumId%22%3A%22" + numIid + "%22%2C%22exParams%22%3A%22%7B%22id%22%3A%22" + numIid + "%22%7D%22%7D&_=1516709033506";
            String referer = "h5api.m.taobao.com";
            String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36";
            String resultHead = "{\"api\":\"wdetail\",\"v\":\"6.0\",\"ret\":[\"SUCCESS::调用成功\"]";
            String apiData;

            if(Play.mode.isProd()) {
                apiData = NewProxyToolsUtils.proxyGet(url, referer, userAgent, resultHead);
            } else {
                apiData = CommonProxyPools.directGet(url, referer, userAgent, null, StringUtils.EMPTY);
            }

            WDetailV6DataBean wDetailV6DataBean = checkAPIData(apiData);
            if (wDetailV6DataBean == null) continue;

            return wDetailV6DataBean;
        }

        return null;
    }

    private WDetailV6DataBean checkAPIData(String apiData) {
        WDetailV6DataBean wDetailV6DataBean = null;
        try {
            wDetailV6DataBean = JSON.parseObject(apiData, WDetailV6DataBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wDetailV6DataBean != null
                && wDetailV6DataBean.getApi().equals("wdetail")
                && wDetailV6DataBean.getV().equals("6.0")
                && wDetailV6DataBean.getRet().get(0).equals("SUCCESS::调用成功")) {
            return wDetailV6DataBean;
        }

        return null;
    }

    public ResultBean parse() {
        if (dataBean == null) {
            log.error("API返回数据错误");
            return null;
        } else if (dataBean.getData().getItem() == null){
            log.error("宝贝不存在");
            throw new RuntimeException("宝贝不存在");
        }
        parseDataBean(dataBean.getData());
        return resultBean;
    }

    // 解析.data
    private void parseDataBean(DataBean data) {
        parseApiStack(data.getApiStack());
        parseItemBean(data.getItem());
        parseMockData(data.getMockData());
        parseProps(data.getProps());
        parseSeller(data.getSeller());
        parseSkuBase(data.getSkuBase());
    }

    // 解析.data.apiStack  宝贝价格|sku价格(真实售卖价格) sku库存
    private void parseApiStack(List<DataBean.ApiStackBean> apiStack) {
        if (apiStack != null && !apiStack.isEmpty()) {
            DataBean.ApiStackBean apiStackBean = apiStack.get(0);
            String value = apiStackBean.getValue();
            JSONObject valueObject = JSON.parseObject(value);
            System.out.println();
            JSONObject skuCore = valueObject.getJSONObject("skuCore");
            JSONObject sku2info = skuCore.getJSONObject("sku2info");
            Map<String, SkuCoreInfo> resultBeanSkuCore = resultBean.getSkuCore();
            if (sku2info != null && !sku2info.isEmpty()) {
                for (Map.Entry entry : sku2info.entrySet()) {
                    String skuId = (String) entry.getKey();
                    JSONObject info = (JSONObject) entry.getValue();
                    JSONObject price = info.getJSONObject("price");
                    // 获取价格 库存
                    String priceText = price.getString("priceText");
                    String quantity = info.getString("quantity");
                    // 获取resultBeanSkuCore中skuId对应对象  未获取到则创建，获取到则修改
                    SkuCoreInfo skuCoreInfo = resultBeanSkuCore.get(skuId);
                    if (skuCoreInfo == null) {
                        skuCoreInfo = new SkuCoreInfo(skuId, null, priceText, quantity);
                        resultBeanSkuCore.put(skuId, skuCoreInfo);
                    } else {
                        skuCoreInfo.setSalePrice(priceText).setQuantity(quantity);
                    }
                }
            }
        }

    }

    // 解析.data.skuBase  宝贝销售属性  sku属性信息
    private void parseSkuBase(DataBean.SkuBaseBean skuBase) {
        // 宝贝销售属性
        List<SkuPropInfo> resultBeanSkuProp = resultBean.getSkuProp();
        List<DataBean.SkuBaseBean.PropsBeanX> props = skuBase.getProps();
        if (props != null)
        for (DataBean.SkuBaseBean.PropsBeanX prop : props) {
            String pid = prop.getPid();
            String name = prop.getName();
            List<DataBean.SkuBaseBean.PropsBeanX.ValuesBean> values = prop.getValues();
            List<SkuPropValueInfo> skuPropValueInfos = new ArrayList<SkuPropValueInfo>();
            if (values != null)
            for (DataBean.SkuBaseBean.PropsBeanX.ValuesBean valuesBean : values) {
                String vname = valuesBean.getName();
                String vid = valuesBean.getVid();
                String image = valuesBean.getImage();
                skuPropValueInfos.add(new SkuPropValueInfo(vid, vname, image));
            }
            resultBeanSkuProp.add(new SkuPropInfo(name, pid, skuPropValueInfos));
        }
        // sku属性信息
        Map<String, SkuCoreInfo> skuCore = resultBean.getSkuCore();
        List<DataBean.SkuBaseBean.SkusBean> skus = skuBase.getSkus();
        if (skus != null)
        for (DataBean.SkuBaseBean.SkusBean skusBean : skus) {
            String skuId = skusBean.getSkuId();
            String propPath = skusBean.getPropPath();
            SkuCoreInfo skuCoreInfo = skuCore.get(skuId);
            if (skuCoreInfo != null) {
                skuCoreInfo.setSkuId(skuId).setPropPath(propPath);
            } else {
                skuCoreInfo = new SkuCoreInfo(skuId, propPath);
                skuCore.put(skuId, skuCoreInfo);
            }
        }
    }

    // 解析.data.seller  卖家昵称
    private void parseSeller(DataBean.SellerBean seller) {
        // 卖家昵称
        String sellerNick = seller.getSellerNick();
        resultBean.setSellerNick(sellerNick);
    }

    // 解析.data.props   宝贝普通属性信息
    private void parseProps(DataBean.PropsBean props) {
        List<PropInfo> prop = resultBean.getProp();
        List<Map<String, List<Map>>> groupProps = props.getGroupProps();
        Map<String, List<Map>> map = groupProps.get(0);
        List<Map> 基本信息 = map.get("基本信息");
        if (基本信息 != null)
        for (Map m : 基本信息) {
            Set<Map.Entry<String, String>> set = m.entrySet();
            Iterator<Map.Entry<String, String>> iterator = set.iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                prop.add(new PropInfo(key, value));
            }
        }
    }

    // 解析.data.mockData   宝贝价格|sku价格(原价)
    private void parseMockData(String mockData) {
        JSONObject mockDataObject = JSON.parseObject(mockData);
        JSONObject skuCore = mockDataObject.getJSONObject("skuCore");
        JSONObject sku2info = skuCore.getJSONObject("sku2info");
        Map<String, SkuCoreInfo> resultBeanSkuCore = resultBean.getSkuCore();
        if (sku2info != null && !sku2info.isEmpty()) {
            for (Map.Entry entry : sku2info.entrySet()) {
                String skuId = (String) entry.getKey();
                JSONObject info = (JSONObject) entry.getValue();
                // 获取sku对应的价格
                JSONObject price = info.getJSONObject("price");
                String priceText = price.getString("priceText");
                // 获取resultBeanSkuCore中skuId对应对象  未获取到则创建，获取到则修改
                SkuCoreInfo skuCoreInfo = resultBeanSkuCore.get(skuId);
                if (skuCoreInfo == null) {
                    skuCoreInfo = new SkuCoreInfo(skuId, priceText, null, "0");
                    resultBeanSkuCore.put(skuId, skuCoreInfo);
                } else {
                    skuCoreInfo.setOriginalPrice(priceText);
                }
            }
        }
    }

    // 解析.data.item   宝贝标题、宝贝cid、宝贝主副图
    private void parseItemBean(DataBean.ItemBean item) {
        // 宝贝标题
        String title = item.getTitle();
        resultBean.setTitle(title);
        // 宝贝cid
        String categoryId = item.getCategoryId();
        resultBean.setCategoryId(categoryId);
        // 宝贝主副图
        List<String> images = item.getImages();
        resultBean.setImages(images);
    }

    public static void main(String[] args) {
        Play.mode = Play.Mode.DEV;
        ResultBean parse = new WDetailV6Parser(562656463234L).parse();

        System.out.println();

    }
}
