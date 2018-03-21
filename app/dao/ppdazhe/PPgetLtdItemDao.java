package dao.ppdazhe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import models.ppdazhe.PPDazheActive;
import models.ppdazhe.PPLtdItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.pojo.PageOffset;

public class PPgetLtdItemDao {
	
    private static final Logger log = LoggerFactory.getLogger(PPLtdItem.class);
    
    public static final String TAG ="PPgetLtdItemDao";
    
    public static final String SelectLtdItem_Sql=" select id, itemCode, activityId, sellerUin, buyLimit, itemBeginTime, itemEndTime, itemDiscount from "+PPLtdItem.TABLE_NAME;
    
    public static final String CountLtdItem_Sql= " select count(*) from " +PPLtdItem.TABLE_NAME;
    
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:MM:ss");
    
    public static long countOnSaleByItemCode(String itemcode){
    	String sql=CountLtdItem_Sql+" where itemCode = ? ";
    	long count =JDBCBuilder.singleLongQuery(sql,itemcode);
    	return count;
    }

    public static PPLtdItem findByItemCode(String itemcode){
    	String sql=SelectLtdItem_Sql+" where itemCode = ? ";
    	PPLtdItem ltditem = new JDBCExecutor<PPLtdItem>(sql,itemcode
                ) {
            @Override
            public PPLtdItem doWithResultSet(ResultSet rs) throws SQLException {
            	PPLtdItem list = new PPLtdItem();
                if (rs.next()) {
                	PPLtdItem promotionList = parsePPLtdItem(rs);
                    if (promotionList != null)
                        list=promotionList;
                }
                return list;
            }
        }.call();
        
        return ltditem;
    }
    
    public static List<PPLtdItem> findLtdItemByactivityId(String activityId,PageOffset po){
    	String sql=SelectLtdItem_Sql+" where activityId = ? limit ?, ? ";
    	List<PPLtdItem> ActiveList = new JDBCExecutor<List<PPLtdItem>>(sql,activityId,po.getOffset(),
                po.getPs()) {
            @Override
            public List<PPLtdItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPLtdItem> list = new ArrayList<PPLtdItem>();
                while (rs.next()) {
                	PPLtdItem ActiveList = parsePPLtdItem(rs);
                    if (ActiveList != null)
                        list.add(ActiveList);
                }
                return list;
            }
        }.call();
        
        return ActiveList;
        
    }
    
    public static List<PPLtdItem> findLtdItemByactivityId(String activityId){
    	String sql=SelectLtdItem_Sql+" where activityId = ? ";
    	List<PPLtdItem> ActiveList = new JDBCExecutor<List<PPLtdItem>>(sql,activityId) {
            @Override
            public List<PPLtdItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPLtdItem> list = new ArrayList<PPLtdItem>();
                while (rs.next()) {
                	PPLtdItem ActiveList = parsePPLtdItem(rs);
                    if (ActiveList != null)
                        list.add(ActiveList);
                }
                return list;
            }
        }.call();
        
        return ActiveList;
        
    }
    
    public static long countLtdItemByactivityId(String activityId){
    	String sql=CountLtdItem_Sql+" where activityId = ? ";
    	long count = JDBCBuilder.singleLongQuery(sql, activityId);
    	return count;
    }
    
    public static PPLtdItem parsePPLtdItem(ResultSet rs){
    	try {
    		Long id=rs.getLong(1);
			String itemCode = rs.getString(2);
			String activityId=rs.getString(3);
			Long sellerUin=rs.getLong(4);
			int buyLimit=Long.valueOf(rs.getLong(5)).intValue();
			String itemBeginTime=rs.getString(6);
			String itemEndTime=rs.getString(7);
			int itemDiscount=Long.valueOf(rs.getLong(8)).intValue();
			
			PPLtdItem ltditem=new PPLtdItem(id,itemCode, activityId, sellerUin, buyLimit, itemBeginTime, itemEndTime, itemDiscount);
			
			return ltditem;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    public static List<PPLtdItem> findLtdItemListByUserId(Long sellerUin,PageOffset po){
    	String sql=SelectLtdItem_Sql+" where sellerUin = ? limit ?, ? ";
    	List<PPLtdItem> ActiveList = new JDBCExecutor<List<PPLtdItem>>(sql, sellerUin,po.getOffset(),
                po.getPs()) {
            @Override
            public List<PPLtdItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPLtdItem> list = new ArrayList<PPLtdItem>();
                while (rs.next()) {
                	PPLtdItem ActiveList = parsePPLtdItem(rs);
                    if (ActiveList != null)
                        list.add(ActiveList);
                }
                return list;
            }
        }.call();
        
        return ActiveList;
    	
    }
    
    public static List<PPLtdItem> findItemListByUserId(Long sellerUin){
    	String sql=SelectLtdItem_Sql+" where sellerUin = ? ";
    	List<PPLtdItem> ActiveList = new JDBCExecutor<List<PPLtdItem>>(sql, sellerUin) {
            @Override
            public List<PPLtdItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPLtdItem> list = new ArrayList<PPLtdItem>();
                while (rs.next()) {
                	PPLtdItem ActiveList = parsePPLtdItem(rs);
                    if (ActiveList != null)
                        list.add(ActiveList);
                }
                return list;
            }
        }.call();
        
        return ActiveList;
    	
    }
    
    public static void deleteLtdItemById(Long id){
    	
    	String sql=" delete from "+PPLtdItem.TABLE_NAME+" where id = ? ";
    	
    	JDBCBuilder.update(false, sql, id);
    }
    
    public static void deleteLtdItemByItemCode(String itemCode){
    	
    	String sql=" delete from "+PPLtdItem.TABLE_NAME+" where itemCode = ? ";
    	
    	JDBCBuilder.update(false, sql, itemCode);
    }
    
    public static void deleteLtdItemByactivityId(String activityId){
    	
    	String sql=" delete from "+PPLtdItem.TABLE_NAME+" where activityId = ? ";
    	JDBCBuilder.update(false, sql, activityId);
    }
 
    
    /*activity数据库的操作*/
    /*******************************************************************************************/
    public static final String SelectActivity_Sql=" select id, sellerUin, beginTime, endTime, activityName, activityId, itemStrings, status from "+PPDazheActive.TABLE_NAME;
    
    public static final String CountActivity_Sql=" select count(*) from "+PPDazheActive.TABLE_NAME;
    
    public static List<PPDazheActive> findListActivityOnActive(Long sellerUin,PageOffset po){
    	String sql=SelectActivity_Sql+" where sellerUin = ? and status = ? limit ?, ? ";
    	String status = "ACTIVE";
    	List<PPDazheActive> ActiveList = new JDBCExecutor<List<PPDazheActive>>(sql, sellerUin, status,po.getOffset(),
              po.getPs()) {
          @Override
          public List<PPDazheActive> doWithResultSet(ResultSet rs) throws SQLException {
              List<PPDazheActive> list = new ArrayList<PPDazheActive>();
              while (rs.next()) {
              	PPDazheActive ActiveList = parseActivity(rs);
                  if (ActiveList != null)
                      list.add(ActiveList);
              }
              return list;
          }
      }.call();
    	return ActiveList;
    } 
    
    public static List<PPDazheActive> findOnActive(Long sellerUin){
    	String sql=SelectActivity_Sql+" where sellerUin = ? and status = ? ";
    	String status = "ACTIVE";
    	List<PPDazheActive> ActiveList = new JDBCExecutor<List<PPDazheActive>>(sql, sellerUin, status) {
          @Override
          public List<PPDazheActive> doWithResultSet(ResultSet rs) throws SQLException {
              List<PPDazheActive> list = new ArrayList<PPDazheActive>();
              while (rs.next()) {
              	PPDazheActive ActiveList = parseActivity(rs);
                  if (ActiveList != null)
                      list.add(ActiveList);
              }
              return list;
          }
      }.call();
    	return ActiveList;
    } 
    
    public static long countActivityAllOnActive (Long sellerUin){
    	String status = "ACTIVE";
    	String sql=CountActivity_Sql+" where sellerUin = ? and status = ? ";
    	long count =JDBCBuilder.singleLongQuery(sql, sellerUin,status);
    	return count;
    }
    
    public static List<PPDazheActive> findListActivityUnActive(Long sellerUin,PageOffset po){
    	String sql=SelectActivity_Sql+" where sellerUin = ? and status = ? limit ?, ? ";
    	String status = "UNACTIVE";
    	List<PPDazheActive> ActiveList = new JDBCExecutor<List<PPDazheActive>>(sql, sellerUin, status,po.getOffset(),
              po.getPs()) {
          @Override
          public List<PPDazheActive> doWithResultSet(ResultSet rs) throws SQLException {
              List<PPDazheActive> list = new ArrayList<PPDazheActive>();
              while (rs.next()) {
              	PPDazheActive ActiveList = parseActivity(rs);
                  if (ActiveList != null)
                      list.add(ActiveList);
              }
              return list;
          }
      }.call();
    	return ActiveList;
    } 
    
    public static long countActivityAllUnActive (Long sellerUin){
    	String status = "UNACTIVE";
    	String sql=CountActivity_Sql+" where sellerUin = ? and status = ? ";
    	long count =JDBCBuilder.singleLongQuery(sql, sellerUin,status);
    	return count;
    }
    
    public static List<PPDazheActive> findListActivityByUserId(Long sellerUin){
    	String sql=SelectActivity_Sql+" where sellerUin = ? ";
    	List<PPDazheActive> ActiveList = new JDBCExecutor<List<PPDazheActive>>(sql, sellerUin) {
          @Override
          public List<PPDazheActive> doWithResultSet(ResultSet rs) throws SQLException {
              List<PPDazheActive> list = new ArrayList<PPDazheActive>();
              while (rs.next()) {
              	PPDazheActive ActiveList = parseActivity(rs);
                  if (ActiveList != null)
                      list.add(ActiveList);
              }
              return list;
          }
      }.call();
    	return ActiveList;
    } 
    
    public static PPDazheActive parseActivity(ResultSet rs){
    	try {
    		Long id=rs.getLong(1);
			Long sellerUin= rs.getLong(2);
			String beginTime=rs.getString(3);
			String endTime =rs.getString(4);
			String activityName=rs.getString(5);
			String activityId=rs.getString(6);
			String  itemStrings=rs.getString(7);
			String status=rs.getString(8);
			
			PPDazheActive active=new PPDazheActive(id,sellerUin, beginTime, endTime, activityName, activityId,itemStrings, status);
			
			return active;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
    }
    
    public static PPDazheActive findActivityByActivityId(String activityId){
    	String sql=SelectActivity_Sql+" where activityId = ? ";
    	
    	PPDazheActive activity = new JDBCExecutor<PPDazheActive>(sql,activityId
                ) {
            @Override
            public PPDazheActive doWithResultSet(ResultSet rs) throws SQLException {
            	PPDazheActive list = new PPDazheActive();
                if (rs.next()) {
                	PPDazheActive promotionList = parseActivity(rs);
                    if (promotionList != null)
                        list=promotionList;
                }
                return list;
            }
        }.call();
        
        return activity;
    }
    
    public static void deleteActivityByActivityId(String activityId){
    	String sql ="delete from pp_dazhe_active where activityId = ?";
    	
    	JDBCBuilder.update(false, sql, activityId);
    }

}
