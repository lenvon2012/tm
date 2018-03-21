package dao.popularized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.popularized.Popularized;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import actions.shopping.RandomShareAction;

import com.ciaosir.client.CommonUtils;

import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;

public class ShoppingDao {
    private static final Logger log = LoggerFactory.getLogger(ShoppingDao.class);
    
    private static final String SelectSql = PopularizedDao.SelectPopularizedSql;
    

    public static List<Popularized> findByUserAndTitle(Long userId, String catName, String title, int status) {
        List<Popularized> userItemList = new ArrayList<Popularized>();
        if (userId == null || userId <= 0L)
            return userItemList;
        
        //userId条件
        StringBuilder sb = new StringBuilder();
        sb.append(SelectSql + " userId = ? ");
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(userId);
        
        //添加其他条件
        addRuleSql(sb, paramList, title, catName, status);
        
        String sql = sb.toString();
        //log.error(sql);
        Object[] params = paramList.toArray();
        userItemList = PopularizedDao.queryListByJDBC(sql, params);
        
        if (CommonUtils.isEmpty(userItemList))
            userItemList = new ArrayList<Popularized>();
        return userItemList;
    }
    
    
    
    
    public static Popularized findByNumIid(Long numIid, String catName, String title, int status) {
        if (numIid == null || numIid <= 0)
            return null;
        
        //numIid条件
        StringBuilder sb = new StringBuilder();
        sb.append(SelectSql + " numIid = ? ");
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(numIid);
        
        //添加其他条件
        addRuleSql(sb, paramList, title, catName, status);
        
        String sql = sb.toString();
        //log.error(sql);
        Object[] params = paramList.toArray();
        
        Popularized item = PopularizedDao.queryByJDBC(sql, params);
        
        return item;
    }
    
    
    private static void addRuleSql(StringBuilder sb, List<Object> paramList,
            String title, String catName, int status) {
        
        //关键词条件
        if (!StringUtils.isEmpty(title))
            title = title.trim();
        if (!StringUtils.isEmpty(title)) {
            String like = getTitleSearchSql(title);
            sb.append(" and " + like + " ");
        }
  
        //类目条件
        if (!StringUtils.isEmpty(catName)) {
            catName = catName.trim();
        }
        if (!StringUtils.isEmpty(catName)) {
            sb.append(" and bigCatName = ? ");
            paramList.add(catName);
        }
        
        //状态条件
        sb.append(PopularizedStatusSqlUtil.getStatusRuleSql(status));
        
    }
    
    private static String getTitleSearchSql(String title) {
        if (StringUtils.isEmpty(title))
            return "";
        
        StringBuilder sb = new StringBuilder("(( 1 = 1)");
        
        title = title.trim();
        String[] splits = title.split(" ");
        for (String split : splits) {
            if (StringUtils.isEmpty(split)) {
                continue;
            }
            split = split.trim();
            if (StringUtils.isEmpty(split)) {
                continue;
            }
            sb.append(" and (title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%') ");
        }
        sb.append(")");
        return sb.toString();
    }
    
    public static Random random = new Random();
    public static Popularized findRandomOne(long startId, Long userId) {
    	
        //id条件
        StringBuilder sb = new StringBuilder();
        sb.append(SelectSql + " id > ?");
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(startId + random.nextInt(20));
        if(userId != null && userId > 0) {
        	sb.append(" and userId = ?");
        	paramList.add(userId);
        }

        String sql = sb.toString();
        sql += " order by id asc limit 0,1";

        Object[] params = paramList.toArray();
        Popularized itemList = PopularizedDao.queryByJDBC(sql, params);
        if(itemList == null) {
        	RandomShareAction.allPreviousMaxId = random.nextInt(20);
        } else {
        	RandomShareAction.allPreviousMaxId = itemList.getId();
        }	  
        return itemList;
    }
    
    public static List<Popularized> findByTitle(long startId, String title, String catName, int status, int fetchNum) {

        //id条件
        StringBuilder sb = new StringBuilder();
        sb.append(SelectSql + " id > ? ");
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(startId);
        
        //添加其他条件
        addRuleSql(sb, paramList, title, catName, status);
        
        String sql = sb.toString();
        sql += " order by id asc limit 0,?";
        paramList.add(fetchNum);
        
        //log.error(sql);
        Object[] params = paramList.toArray();
        List<Popularized> itemList = PopularizedDao.queryListByJDBC(sql, params);

        if (CommonUtils.isEmpty(itemList))
            itemList = new ArrayList<Popularized>();
        else {
            
        }
        
        return itemList;
    }
    
    
}
