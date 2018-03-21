package dao.paipai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.paipai.PaiNumIidToItemCode;
import models.popularized.Popularized;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiItem;
import ppapi.models.PaiPaiItem.ListFetcher;
import result.TMResult;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;

public class PaiPaiItemDao {
    
    private static final Logger log = LoggerFactory.getLogger(PaiPaiItemDao.class);

    public static DBDispatcher dp = PaiPaiItem.dp;
    
    /**
     * 根据itemcodes来查询
     * @param sellerUin
     * @param itemCodes
     * @return
     */
    public static List<PaiPaiItem> findByItemCodes(Long sellerUin, String itemCodes) {
        
        if (StringUtils.isEmpty(itemCodes)) {
            return new ArrayList<PaiPaiItem>();
        }
        
        String[] itemCodeArray = itemCodes.split(",");
        
        if (ArrayUtils.isEmpty(itemCodeArray)) {
            return new ArrayList<PaiPaiItem>();
        }
        
        String[] quotaItemCodeArray = new String[itemCodeArray.length];
        for (int i = 0; i < itemCodeArray.length; i++) {
            quotaItemCodeArray[i] = "'" + itemCodeArray[i] + "'";
        }
        
        String query = " itemCode in (" + StringUtils.join(quotaItemCodeArray, ",") + ") ";
        
        log.info("paipai search query: " + query);
        
        ListFetcher listFetcher = new ListFetcher(sellerUin, query);
        
        List<PaiPaiItem> paipaiItemList = listFetcher.call();
        
        if (CommonUtils.isEmpty(paipaiItemList)) {
            paipaiItemList = new ArrayList<PaiPaiItem>();
        }
        
        return paipaiItemList;
        
    }   
    
    public static PaiPaiItem findByItemCode(Long sellerUin,String itemCode){
    	String sql =Item_SQL+" where sellerUin = ? and itemCode = ? ";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	PaiPaiItem ltditem = new JDBCExecutor<PaiPaiItem>(sql,sellerUin,itemCode
                ) {
            @Override
            public PaiPaiItem doWithResultSet(ResultSet rs) throws SQLException {
            	PaiPaiItem list = new PaiPaiItem();
                if (rs.next()) {
                	PaiPaiItem promotionList = parseItem(rs);
                    if (promotionList != null)
                        list=promotionList;
                }
                return list;
            }
        }.call();
        
        return ltditem;
    }
        
    public static List<PaiPaiItem> searchPop(Long sellerUin, int offset, int limit, String search, int sort,
            int polularized, Long catId, int popularizeStatus) {

        List<Object> paramList = new ArrayList<Object>();
        
        String query = genPopQuery(sellerUin, search, sort, catId, polularized, popularizeStatus, paramList);
        
        //价格升序
        if (sort == 5) {
            query += " order by itemPrice asc, itemCode asc limit ?, ?";
        } else if (sort == 6) {
            query += " order by itemPrice desc, itemCode asc limit ?, ?";
        } else {
            query += " order by itemCode asc limit ?, ?";
        }
        
        
        
        log.info(query);
        paramList.add(offset);
        paramList.add(limit);
        
        Object[] paramArray = paramList.toArray();
        ListFetcher listFetcher = new ListFetcher(sellerUin, query, paramArray);
        
        List<PaiPaiItem> paipaiItemList = listFetcher.call();
        
        if (CommonUtils.isEmpty(paipaiItemList)) {
            paipaiItemList = new ArrayList<PaiPaiItem>();
        }
        
        return paipaiItemList;

    }
    
    
    public static long countPop(Long sellerUin, String search, int sort, Long catId, int polularized, int popularizeStatus) {
        
        List<Object> paramList = new ArrayList<Object>();
        
        String sql = " select count(*) from " + PaiPaiItem.TABLE_NAME + "%s ";
        
        sql = PaiPaiItem.genShardQuery(sql, sellerUin);

        sql += " where sellerUin = " + sellerUin + " ";
        
        String query = genPopQuery(sellerUin, search, sort, catId, polularized, popularizeStatus, paramList);
        
        sql += " and " + query;
        
        log.info(sql);
        
        
        Object[] paramArray = paramList.toArray();
        
        long count = JDBCBuilder.singleLongQuery(dp.getDataSrc(sellerUin), sql, paramArray);
        
        return count;
        
    }
    
    private static String genPopQuery(Long sellerUin, String search, int sort, Long catId,
            int polularized, int popularizeStatus, List<Object> paramList) {
        
        String query = " itemState = ? ";
        
        paramList.add("IS_FOR_SALE");
        
        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                query += " and ";
                query += appendTitleLike(search);
                query += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    query += " and ";
                    query += "itemName like '%";
                    query += CommonUtils.escapeSQL(string);
                    query += "%' ";
                }
            }
        }

        if (catId != null && catId > 0L) {
            query += " and categoryId = ? ";
            paramList.add(catId);
        }

        
        String popSql = " (select itemCode from " + PaiNumIidToItemCode.TABLE_NAME + " as code "
                    + " where code.id in (select numIid from " + Popularized.TABLE_NAME 
                    + " where userId = " + sellerUin
                    + " " + PopularizedStatusSqlUtil.getStatusRuleSql(popularizeStatus) + " ))";
        
        //查找已推广的宝贝
        if (polularized == 0) {
            
            query += " and itemCode in " + popSql;
            
        } else if (polularized == 1) {
            //未推广的宝贝
            query += " and itemCode not in " + popSql;
            
        }
        
        
        return query;
    }
    
    
    static String appendTitleLike(String key) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (itemName like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }
    
    
    public static final String Item_SQL="select sellerUin,itemCode,itemName,itemState,picLink,createTime,categoryId,itemPrice,visitCount,id,type from "+PaiPaiItem.TABLE_NAME+"%s";
    
    public static Set<Long> catIdSet(Long sellerUin) {
    	
    	String sql =Item_SQL+" where sellerUin = ? ";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
    	
    	Set<Long> catIdSet=new HashSet<Long>();
    	
    	for(PaiPaiItem paipaiItem:paipaiItemList){
    		Long catId=paipaiItem.getCategoryId();
    		catIdSet.add(catId);
    	}
    	
    	return catIdSet;
    }
    
    
    public static PaiPaiItem parseItem(ResultSet rs){
    	try {

    		PaiPaiItem item=new PaiPaiItem(rs);
    		return item;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return null;
    	
    }
    
    public static List<PaiPaiItem> findOnSaleByUserCatId(Long sellerUin,Long catid,PageOffset po){
    	String sql = Item_SQL+" where sellerUin = ? and categoryId = ? and itemState = ? limit ?, ? ";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	String itemState="IS_FOR_SALE";
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,catid,itemState,po.getOffset(),po.getPs()) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return paipaiItemList;
    }
    
    
    public static long countOnSaleByUserCatId(Long sellerUin,Long catid){
    	String sql="select count(*) from paipai_item_%s where sellerUin = ? and categoryId = ? and itemState = ? ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	long count =JDBCBuilder.singleLongQuery(sql, sellerUin,catid,itemState);
    	return count;
    }
    
    public static List<PaiPaiItem> findOnSaleByUserId(Long sellerUin,PageOffset po){
    	String sql=Item_SQL+" where sellerUin = ? and itemState = ? limit ?, ? ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,itemState,po.getOffset(),po.getPs()) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return paipaiItemList;
    }
    
    public static long countOnSaleByUserId(Long sellerUin){
    	String sql="select count(*) from paipai_item_%s where sellerUin = ? and itemState = ? ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	long count =JDBCBuilder.singleLongQuery(sql, sellerUin,itemState);
    	return count;
    }
    
    public static List<PaiPaiItem> findOnSaleByUserId(Long sellerUin){
    	String sql=Item_SQL+" where sellerUin = ? and itemState = ? ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,itemState) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return paipaiItemList;
    }
    
    public static List<PaiPaiItem> findOnSaleOutOfItemCode(Long sellerUin,String itemCodes){
    	String sql=Item_SQL+" where sellerUin = ? and itemState = ? ";
    	sql+="and itemCode not in ("+itemCodes+") ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,itemState) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return paipaiItemList;
    }
    
    public static List<PaiPaiItem> findOnSaleByKeywords(Long sellerUin,String keywords,PageOffset po){
    	String sql=Item_SQL+" where sellerUin = ? and itemState = ? ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
        if (!keywords.isEmpty()) {
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }
        
        sql+=" limit ?, ?";
        
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,itemState,po.getOffset(),po.getPs()) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return paipaiItemList;
    	
    }
    
    public static long countOnSaleByKeywords(Long sellerUin,String keywords){
    	String sql="select count(*) from paipai_item_%s where sellerUin = ? and itemState = ? ";
    	String itemState="IS_FOR_SALE";
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
        if (!keywords.isEmpty()) {
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }
    	long count =JDBCBuilder.singleLongQuery(sql, sellerUin,itemState);
    	return count;
    }
    
    static String appendKeywordsLike(String Keywords) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = Keywords.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (itemName like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }
    
    public static final String Short_Item_SQL="select sellerUin,itemCode,itemName,itemState,picLink,createTime,categoryId,itemPrice,visitCount,id,type ";
    
    public static TMResult findBatchwithOrder(Long sellerUin,String keywords,String status,Long cid,String orderProp,
    		boolean isOrderAsc, PageOffset po){
    	String sql= " from "+PaiPaiItem.TABLE_NAME+"%s"+" where sellerUin = ? ";;
    	
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
        if (!StringUtils.isEmpty(keywords)) {  
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }
        
        if(!StringUtils.isEmpty(status)&&!StringUtils.equals(status, "ALL")){
        	sql += " and itemState = '"+status+"'";
        }

        if(cid !=0){
        	sql += " and categoryId = "+cid.toString();
        }
        
        int count =(int)JDBCBuilder.singleLongQuery("select count(*) "+sql, sellerUin);
        
        if (!StringUtils.isEmpty(orderProp)) {
            sql += " order by " + orderProp + " ";
            //升序
            if (isOrderAsc == true) {
                sql += " asc ";
            } else {
                sql += " desc ";
            }
        }
        
        sql += " limit ?, ? ";
        sql = Short_Item_SQL + sql;
 	
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,po.getOffset(),po.getPs()) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return new TMResult(paipaiItemList, count, po);

    }   
    
    public static List<PaiPaiItem> findStockItemwithOrder(Long sellerUin,String keywords,String status,Long cid,String orderProp,
    		boolean isOrderAsc, PageOffset po){
    	String sql= " from "+PaiPaiItem.TABLE_NAME+"%s"+" where sellerUin = ? ";;
    	
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
        if (!StringUtils.isEmpty(keywords)) {  
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }
        
        if(!StringUtils.isEmpty(status)&&!StringUtils.equals(status, "ALL")){
        	sql += " and itemState = '"+status+"'";
        }

        if(cid !=0){
        	sql += " and categoryId = "+cid.toString();
        }
        
        int count =(int)JDBCBuilder.singleLongQuery("select count(*) "+sql, sellerUin);
        
        if (!StringUtils.isEmpty(orderProp)) {
            sql += " order by " + orderProp + " ";
            //升序
            if (isOrderAsc == true) {
                sql += " asc ";
            } else {
                sql += " desc ";
            }
        }
        
        sql += " limit ?, ? ";
        sql = Short_Item_SQL + sql;
 	
    	List<PaiPaiItem> paipaiItemList =new JDBCExecutor<List<PaiPaiItem>>(sql, sellerUin,po.getOffset(),po.getPs()) {

			@Override
			public List<PaiPaiItem> doWithResultSet(ResultSet rs)
					throws SQLException {
                List<PaiPaiItem> itemList = new ArrayList<PaiPaiItem>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
			}
        }.call();
        
        return paipaiItemList;

    }  
    
    public static long countStockItemwithOrder(Long sellerUin,String keywords,String status,Long cid){
    	String sql= " from "+PaiPaiItem.TABLE_NAME+"%s"+" where sellerUin = ? ";;
    	
    	sql=PaiPaiItem.genShardQuery(sql, sellerUin);
    	
        if (!StringUtils.isEmpty(keywords)) {  
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }
        
        if(!StringUtils.isEmpty(status)&&!StringUtils.equals(status, "ALL")){
        	sql += " and itemState = '"+status+"'";
        }

        if(cid !=0){
        	sql += " and categoryId = "+cid.toString();
        }
        
        int count =(int)JDBCBuilder.singleLongQuery("select count(*) "+sql, sellerUin);
        
        return count;
    }

    public static List<String> randOnSaleIdsByUserId(Long sellerUin, int limit ) {
        String sql = "select itemCode from paipai_item_%s where sellerUin = ? and itemState = ? order by rand() limit ?";
        String itemState = "IS_FOR_SALE";
        sql = PaiPaiItem.genShardQuery(sql, sellerUin);

        List<String> paipaiItemList = new JDBCExecutor<List<String>>(sql, sellerUin, itemState, limit) {

            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> itemList = new ArrayList<String>();
                while (rs.next()) {
                    itemList.add(rs.getString(1));
                }
                return itemList;
            }
        }.call();

        return paipaiItemList;
    }

}
