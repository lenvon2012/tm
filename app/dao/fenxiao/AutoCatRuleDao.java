/**
 * 
 */

package dao.fenxiao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdp.ApiJdpAdapter;
import models.fenxiao.AutoCatRule;
import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import bustbapi.FenxiaoApi.FXScItemSupplierApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

/**
 * @author navins
 * @date: Dec 3, 2013 1:59:11 PM
 */
public class AutoCatRuleDao {
    private static final Logger log = LoggerFactory.getLogger(AutoCatRuleDao.class);

    public static Boolean checkAutoCatRule(User user, AutoCatRule rule, ItemPlay item) {
        String title = item.getTitle();
        String words = rule.getWords();
        Boolean res = Boolean.TRUE;
        if (!StringUtils.isEmpty(words)) {
            String[] andArr = words.split("&&");
            for (int i = 0; i < andArr.length; i++) {
                Pattern pattern = Pattern.compile(andArr[i]);
                Matcher matcher = pattern.matcher(title);
                if (!matcher.find()) {
                    res = Boolean.FALSE;
                }
            }
            if (andArr.length > 0 && res == Boolean.TRUE) {
                log.info("autocat numIid=" + item.getId() + ", userId=" + user.getId());
                return Boolean.TRUE;
            }
        }

        // 匹配供应商
        FenxiaoProduct product = new FXScItemSupplierApi(user, item.getId(), rule.getSupplier()).call();
        if (product != null) {
            return Boolean.TRUE;
        }

        String brand = rule.getBrand();
        String attr = rule.getAttr();
        Item call = ApiJdpAdapter.tryFetchSingleItem(user, item.getId());
        String propsName = call.getPropsName();
        if (!StringUtils.isEmpty(propsName)) {
            // 匹配属性
            if (!StringUtils.isEmpty(attr) && propsName.contains(attr)) {
                return Boolean.TRUE;
            }

            // 匹配品牌
            if (!StringUtils.isEmpty(brand)) {
                int brandStart = propsName.indexOf("品牌:");
                if (brandStart >= 0) {
                    String brandStr = propsName.substring(brandStart);
                    brandStr = brandStr.substring(0, brandStr.indexOf(':'));
                    if (brandStr.contains(brand)) {
                        return Boolean.TRUE;
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    public static Boolean checkRuleEmpty(AutoCatRule rule) {
        if (rule == null) {
            return Boolean.TRUE;
        }
        if (StringUtils.isBlank(rule.getWords()) && StringUtils.isBlank(rule.getBrand())
                && StringUtils.isBlank(rule.getAttr()) && StringUtils.isBlank(rule.getSupplier())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static String Select_Query = "select id,userId,catId,words,brand,attr,distributor,supplier,created,reserved from autocat_rule";

    public static List<AutoCatRule> findCatsRule(Long userId) {

        String query = Select_Query + " where userId = ? ";
        return new JDBCBuilder.JDBCExecutor<List<AutoCatRule>>(AutoCatRule.dp, query, userId) {

            @Override
            public List<AutoCatRule> doWithResultSet(ResultSet rs) throws SQLException {
                List<AutoCatRule> list = new ArrayList<AutoCatRule>();
                while (rs.next()) {
                    list.add(new AutoCatRule(rs));
                }
                return list;
            }

        }.call();
    }

    public static AutoCatRule findAutoCatRule(Long userId, Long catId) {
        String query = Select_Query + " where userId = ? and catId = ?";
        return new JDBCBuilder.JDBCExecutor<AutoCatRule>(AutoCatRule.dp, query, userId, catId) {
            @Override
            public AutoCatRule doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new AutoCatRule(rs);
                }
                return null;
            }
        }.call();
    }

    public static List<AutoCatRule> findCatsRule(Long userId, Collection<Long> cids) {

        String query = Select_Query + " where userId = ? and catId in (" + StringUtils.join(cids, ',') + ")";
        return new JDBCBuilder.JDBCExecutor<List<AutoCatRule>>(AutoCatRule.dp, query, userId) {

            @Override
            public List<AutoCatRule> doWithResultSet(ResultSet rs) throws SQLException {
                List<AutoCatRule> list = new ArrayList<AutoCatRule>();
                while (rs.next()) {
                    list.add(new AutoCatRule(rs));
                }
                return list;
            }

        }.call();
    }

    public static List<CatIdNameCountRule> findCatsCountRule(User user) {
        Map<Long, Integer> cidCount = ItemDao.cidCount(user);
        Set<Long> cids = cidCount.keySet();
        if (CommonUtils.isEmpty(cids)) {
            return null;
        }
        List<ItemCatPlay> cats = ItemCatPlay.findCats(cids);
        if (CommonUtils.isEmpty(cats)) {
            return null;
        }
        List<AutoCatRule> rules = AutoCatRuleDao.findCatsRule(user.getId(), cids);
        HashMap<Long, AutoCatRule> ruleMap = new HashMap<Long, AutoCatRule>();
        if (!CommonUtils.isEmpty(rules)) {
            for (AutoCatRule autoCatRule : rules) {
                ruleMap.put(autoCatRule.getCatId(), autoCatRule);
            }
        }

        List<CatIdNameCountRule> list = new ArrayList<CatIdNameCountRule>();
        for (ItemCatPlay catPlay : cats) {
            Long cid = catPlay.getCid();
            CatIdNameCountRule catIdNameCountRule = new CatIdNameCountRule(cid, catPlay.getName(), cidCount.get(cid),
                    ruleMap.get(cid));
            list.add(catIdNameCountRule);
        }
        return list;
    }

    public static class CatIdNameCountRule {
        @JsonProperty
        long id;

        @JsonProperty
        String name;

        @JsonProperty
        int count;

        @JsonProperty
        AutoCatRule rule;

        public CatIdNameCountRule(Long id, String name, int count, AutoCatRule rule) {
            super();
            this.id = id;
            this.name = name;
            this.count = count;
            this.rule = rule;
        }
    }

}
