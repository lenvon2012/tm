package dao.ppdazhe;

import java.text.SimpleDateFormat;

import models.ppdazhe.PPDazheActive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PPzhekou_1Dao {
    private static final Logger log = LoggerFactory.getLogger(PPzhekou_1Dao.class);
    
    public static final String TAG ="PPzhekou_1Dao";
    
    public static final String SelectActive_Sql=" select sellerUin, beginTime, endTime, activityName, activityId, itemNum from "+PPDazheActive.TABLE_NAME;
    
    public static final String CountActive_Sql= " select count(*) from " +PPDazheActive.TABLE_NAME;
    
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:MM:ss");
    
//    public static boolean addActive(Long sellerUin,String beginTime,String endTime,String activityName,String activityId){
//
//    	PPDazheActive active=new PPDazheActive(sellerUin,beginTime,endTime,activityName,activityId);
//    	
//    	boolean success=active.jdbcSave();
//    	
//    	return success;
//    }
//    
//    public static boolean getActiveApi(List<PPDazheActive> LtdActiveList){
//    	Long sellerUin;
//    	String beginTime;
//    	String endTime;
//    	String activityName;
//    	String activityId;
//    	long itemNum;
//    	for(PPDazheActive list:LtdActiveList){
//    		sellerUin=list.getSellerUin();
//    		beginTime=list.getBeginTime();
//    		endTime=list.getEndTime();
//    		activityName=list.getActivityName();
//    		activityId=list.getActivityId();
//    		itemNum=list.getItemNum();
//    		PPDazheActive active=new PPDazheActive(sellerUin,beginTime,endTime,activityName,activityId,itemNum);
//            boolean success=active.jdbcSave();
//    		if(!success) return false;
//    	}
//    	
//    	return true;
//    }
//
//    public static TMResult getActive(PageOffset po){
//    	
//    	String sql=SelectActive_Sql+ " limit ?, ?";    	
//    	String countSql=CountActive_Sql;
//        long count = JDBCBuilder.singleLongQuery(countSql);
//        List<PPDazheActive> ActiveList = new JDBCExecutor<List<PPDazheActive>>(sql, po.getOffset(),
//                po.getPs()) {
//            @Override
//            public List<PPDazheActive> doWithResultSet(ResultSet rs) throws SQLException {
//                List<PPDazheActive> list = new ArrayList<PPDazheActive>();
//                while (rs.next()) {
//                	PPDazheActive ActiveList = parseActiveList(rs);
//                    if (ActiveList != null)
//                        list.add(ActiveList);
//                }
//                return list;
//            }
//        }.call();
//
//        TMResult tmResult = new TMResult(ActiveList, (int) count, po);
//        return tmResult;        
//    }
//    
//    public static TMResult getActByActId(String activityId){
//    	
//    	String sql=SelectActive_Sql+ " where activityId = ? ";    	
//
//        PPDazheActive Active = new JDBCExecutor<PPDazheActive>(sql, activityId
//                ) {
//            @Override
//            public PPDazheActive doWithResultSet(ResultSet rs) throws SQLException {
//                PPDazheActive list = new PPDazheActive();
//                if (rs.next()) {
//                	PPDazheActive ActiveList = parseActiveList(rs);
//                    if (ActiveList != null)
//                        list=ActiveList;
//                }
//                return list;
//            }
//        }.call();
//
//        TMResult tmResult = new TMResult(Active);
//        return tmResult;        
//    }
//    
//    public static PPDazheActive parseActiveList(ResultSet rs){
//    	  try {
//    	    	Long sellerUin=rs.getLong(1);
//    	    	String beginTime=rs.getString(2);
//    	    	String endTime=rs.getString(3);
//    	    	String activityName=rs.getString(4);
//    	    	String activityId=rs.getString(5);
//    	    	long itemNum=rs.getLong(6);
//    	    	
//    	    	PPDazheActive active=new PPDazheActive(sellerUin, beginTime, endTime, activityName, activityId,itemNum);
//    		
//    	    	return active;
//    	  }catch (Exception ex) {
//              log.error(ex.getMessage(), ex);
//              return null;
//          }
//
//    }
}
