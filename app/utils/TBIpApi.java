
package utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.AreaUtil.PYArea;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NetworkUtil;

import configs.TMConfigs;

public class TBIpApi {
    private static final Logger log = LoggerFactory.getLogger(TBIpApi.class);

    private static final int RETRY_TIMES = 3;

//	public static void main(String[] args){
//	    System.out.println(getContent("218.109.217.3"));
//	}
    private static String template = "http://ip.taobao.com/service/getIpInfo.php?ip=%s";

    private static String getContent(String ip) {

        String url = String.format(template, ip);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            URL u = new URL(url);
            HttpURLConnection httpUrl = null;
            BufferedInputStream bis = null;
            byte[] buf = new byte[1024];

            httpUrl = (HttpURLConnection) u.openConnection();
            httpUrl.setRequestMethod("GET");
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());
            if (bis.available() <= 0) {
                return null;
            }
            while (true) {
                int bytes_read = bis.read(buf);
                if (bytes_read > 0) {
                    bos.write(buf, 0, bytes_read);
                } else {
                    break;
                }
            }

            bis.close();
            httpUrl.disconnect();
        } catch (Exception e) {

        }
        return bos.toString().trim();
    }

    public static String getLocation(String ip) {
        String content = null;
        int retry = 0;
        while (++retry <= RETRY_TIMES && content == null) {
            content = getContent(ip.trim());
            if (!content.startsWith("{")) {
                content = null;
            }
        }
        if (content == null) {
            log.warn(String.format("ip: %s tbipapi call > retry_times!", ip));
            return "";
        }
        //log.info(content);
        String location = parseLocation(content);
        return location;

    }

    private static String parseLocation(String content) {
        try {
            Map<String, Object> jsonMap = JsonUtil.toObject(content, Map.class);
            Object dataObj = jsonMap.get("data");
            if (dataObj == null || !(dataObj instanceof Map<?, ?>))
                return "";
            Map<String, String> dataMap = (Map<String, String>) jsonMap.get("data");
            String location = "";
            String region = dataMap.get("region");
            region = parseNullValue(region);
            String city = dataMap.get("city");
            city = parseNullValue(city);
            if (region.equals(city)) {//比如上海市
                region = "";
            }
            if (region.endsWith("省")) {
                region = region.substring(0, region.length() - 1);
            }

            if (city.endsWith("市")) {
                city = city.substring(0, city.length() - 1);
            }
            String county = dataMap.get("county");
            county = parseNullValue(county);
            String isp = dataMap.get("isp");
            isp = parseNullValue(isp);
            if (isp.equals("联通")) {
                isp = "网通";
            }

            location = region + city + county + isp;
            return location;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return "";
        }
    }

    private static String parseNullValue(String value) {
        if (value == null)
            return "";
        else
            return value.trim();
    }

    public static void main(String[] args) {
        String[] ipArray = new String[] {
                "114.80.166.240",
                "122.224.74.82",
                "60.191.132.102",
                "210.51.167.169",
                "172.18.0.10",
                "124.192.60.5",
                "210.82.113.17",
                "125.92.95.63",
                "221.219.2.103",
                "220.180.150.34"
        };
        for (String ip : ipArray) {
            String location = TBIpApi.getLocation(ip);
            log.info(location);
        }
    }

    public static IpDataBean parseIpToBean(String ip) {
        return IpDataBean.doForIP(ip);
    }

    /**
     * 

    1. 请求接口（GET）：

    http://ip.taobao.com/service/getIpInfo.php?ip=[ip地址字串]

    2. 响应信息：

    （json格式的）国家 、省（自治区或直辖市）、市（县）、运营商

    3. 返回数据格式：

    {
    "code": 0,
    "data": {
        "ip": "210.75.225.254",
        "country": "中国",
        "area": "华北",
        "region": "北京市",
        "city": "北京市",
        "county": "",
        "isp": "电信",
        "country_id": "86",
        "area_id": "100000",
        "region_id": "110000",
        "city_id": "110000",
        "county_id": "-1",
        "isp_id": "100017"
    }
    }
    {
    "code": 0,
    "data": {
        "country": "中国",
        "country_id": "CN",
        "area": "华南",
        "area_id": "800000",
        "region": "广东省",
        "region_id": "440000",
        "city": "广州市",
        "city_id": "440100",
        "county": "",
        "county_id": "-1",
        "isp": "教育网",
        "isp_id": "100027",
        "ip": "218.192.3.42"
    }
    }
    其中code的值的含义为，0：成功，1：失败。

    http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=218.192.3.42
    {
    "ret": 1,
    "start": "218.192.0.0",
    "end": "218.192.7.255",
    "country": "中国",
    "province": "广东",
    "city": "广州",
    "district": "",
    "isp": "教育网",
    "type": "学校",
    "desc": "广州大学纺织服装学院教育网"
    }

     * 
     * duiy
     * @author depvelop
     * TRADE FROM ...
     */
    public static class IpDataBean implements Serializable {

        public boolean mightHasData() {
            return !StringUtils.isEmpty(province) || !StringUtils.isEmpty(city) || !StringUtils.isEmpty(isp);
        }

        public IpDataBean(String country, String city, String province, String county, String isp) {
            super();
            this.country = country;
            this.city = city;
            this.province = province;
            this.county = county;
            this.isp = isp;
        }

        String country;

        String city;

        /**
         * 省
         */
        String province;

        /**
         * 县
         */
        String county;

        /**
         * 运营商
         */
        String isp;

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public IpDataBean() {
            super();
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCounty() {
            return county;
        }

        public void setCounty(String county) {
            this.county = county;
        }

        public static IpDataBean doForIP(String ip) {
            if (NetworkUtil.isInnerIP(ip)) {
                //
                return null;
            }

            IpDataBean bean = null;
            bean = fetchBeanFromTB(ip);
            if (bean == null) {
                bean = fetchBeanFromSina(ip);
            }

            formatBean(bean);

            return bean;
        }

        private static void formatBean(IpDataBean bean) {
            if (bean == null) {
                return;
            }

            bean.city = trimCity(bean.city);
            bean.province = trimProvince(bean.province);

        }

        /**
         *     {
        "code": 0,
        "data": {
        "ip": "210.75.225.254",
        "country": "中国",
        "area": "华北",
        "region": "北京市",
        "city": "北京市",
        "county": "",
        "isp": "电信",
        "country_id": "86",
        "area_id": "100000",
        "region_id": "110000",
        "city_id": "110000",
        "county_id": "-1",
        "isp_id": "100017"
        }
         * @param ip
         * @return
         */
        public static IpDataBean fetchBeanFromTB(String ip) {
            String url = String.format(tbTemplate, ip);
            String content = API.directGet(url, null, null);
            try {
                JSONObject obj = new JSONObject(content);
                int successCode = obj.getInt("code");
                /*
                 * 其中code的值的含义为，0：成功，1：失败。
                 */
                if (successCode != 0) {
                    return null;
                }

                JSONObject data = obj.getJSONObject("data");

                IpDataBean bean = new IpDataBean();
                bean.country = data.getString("country");
                bean.province = data.getString("region");
                bean.city = data.getString("city");
                bean.county = data.getString("county");
                bean.isp = data.getString("isp");

                return bean;

            } catch (JSONException e) {
                log.warn("bad content :" + content);
                log.warn(e.getMessage(), e);
            }
            return null;
        }

        /**
         *     {
            "ret": 1,
            "start": "218.192.0.0",
            "end": "218.192.7.255",
            "country": "中国",
            "province": "广东",
            "city": "广州",
            "district": "",
            "isp": "教育网",
            "type": "学校",
            "desc": "广州大学纺织服装学院教育网"
            }
         * @param ip
         * @return
         */
        public static IpDataBean fetchBeanFromSina(String ip) {
            String url = String.format(sinaTemplate, ip);
            String content = API.directGet(url, null, null);
            try {

                JSONObject obj = new JSONObject(content);
                int ret = obj.getInt("ret");
                if (ret != 1) {
                    return null;
                }

                IpDataBean bean = new IpDataBean();
                bean.country = obj.getString("country");
                bean.province = obj.getString("province");
                bean.city = obj.getString("city");
                bean.county = obj.getString("district");
                bean.isp = obj.getString("isp");

                return bean;

            } catch (JSONException e) {
                log.warn("bad content :" + content);
                log.warn(e.getMessage(), e);
            }
            return null;
        }

        @Override
        public String toString() {
            return "IpDataBean [country=" + country + ", city=" + city + ", province=" + province + ", county="
                    + county + ", isp=" + isp + "]";
        }

        private static String tbTemplate = "http://ip.taobao.com/service/getIpInfo.php?ip=%s";

        private static String sinaTemplate = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=%s";
    }

    public static class BuyerAreaManager {
        public static BuyerAreaManager _instance = new BuyerAreaManager();

        public static BuyerAreaManager get() {
            return _instance;
        }

        /**
         * 有时候, 没有地区, 只有运营商
         * 和记环球电讯有限公司
         */
        static String[] ispKeys = new String[] {
                "移动", "电信", "网通", "联通", "教育", "铁通", "电讯盈科有限公司", "和记环球电讯有限公司",
                "有线"
        };

        public String getISP(String buyerArea) {
            if (StringUtils.isEmpty(buyerArea)) {
                return null;
            }
            for (String key : ispKeys) {
                if (buyerArea.contains(key)) {
                    return key;
                }
            }
            return null;
        }

        public String getProvince(String buyerArea) {
            if (StringUtils.isEmpty(buyerArea)) {
                return null;
            }
            String[] provinces = getProvinceNames();
            for (String string : provinces) {
                if (buyerArea.contains(string)) {
                    return string;
                }
            }
            return null;
        }

        public BuyerAreaManager() {
        }

        private String[] provinceNames = null;

        public String[] getProvinceNames() {
            if (provinceNames != null) {
                return provinceNames;
            }
            PYArea[] values = PYArea.values();
            List<String> names = new ArrayList<String>();
            for (PYArea pyArea : values) {
                if (pyArea.getAreaNo() > 0 && pyArea.getAreaNo() < 35) {
                    names.add(pyArea.getArea());
                }
            }
            this.provinceNames = names.toArray(new String[] {});
            return this.provinceNames;
        }

        private String[] countryArr = null;

        public String getCountry(String country) {
            if (StringUtils.isEmpty(country)) {
                return country;
            }

            for (String s : genCountries()) {
                if (country.contains(s)) {
                    return s;
                }
            }
            return null;
        }

        public String[] genCountries() {
            if (countryArr != null) {
                return countryArr;
            }

            try {
                File file = new File(TMConfigs.autoDir, "countries.txt");
                List<String> lines = FileUtils.readLines(file);
                Iterator<String> it = lines.iterator();
                while (it.hasNext()) {
                    String next = it.next();
                    if (StringUtils.isBlank(next)) {
                        it.remove();
                    }
                }

                countryArr = lines.toArray(new String[] {});

            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
            return countryArr;
        }
    }

    public static String trimProvince(String province) {
        if (StringUtils.isEmpty(province)) {
            return province;
        }
        if ((province.endsWith("省") || (province.endsWith("市")))) {
            province = province.substring(0, province.length() - 1);
        }
        if (province.contains("特别行政区")) {
            province = province.replace("特别行政区", "");
        }
        if (province.contains("壮族自治区")) {
            province = province.replace("壮族自治区", "");
        }
        if (province.contains("回族自治区")) {
            province = province.replace("回族自治区", "");
        }
        if (province.contains("维吾尔自治区")) {
            province = province.replace("维吾尔自治区", "");
        }
        if (province.contains("维吾尔族自治区")) {
            province = province.replace("维吾尔族自治区", "");
        }
        if (province.contains("自治区")) {
            province = province.replace("自治区", "");
        }

        return province;
    }

    public static String trimIsp(String rawIsp) {
        return BuyerAreaManager.get().getISP(rawIsp);
    }

    public static String trimCity(String city) {
        if (StringUtils.isEmpty(city)) {
            return city;
        }

        if (city != null && (city.endsWith("市") || city.endsWith("区"))) {
            city = city.substring(0, city.length() - 1);
        }
        if (city.endsWith("自治旗")) {
            city = city.replace("自治旗", "");
        }
        return city;
    }

    public static String trimCountry(String country) {
        if (StringUtils.isEmpty(country)) {
            return country;
        }
        if (country.contains("中华人民共和国")) {
            return "中国";
        }
        return BuyerAreaManager.get().getCountry(country);
    }

}
