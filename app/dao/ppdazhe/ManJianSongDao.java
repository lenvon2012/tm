package dao.ppdazhe;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.ppdazhe.ManJianSongActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;


/**
 * @author haoyongzh
 *
 */
public class ManJianSongDao {
    private static final Logger log = LoggerFactory.getLogger(ManJianSongDao.class);
    
    public static final String TAG ="ManJianSongDao";
    
    public static final String SelectManJian_SQL=" select id, sellerUin, beginTime, endTime, activityDesc, contentJson from "+ManJianSongActivity.TABLE_NAME;

    public static final String CountManJian_SQL=" select count(*) from "+ ManJianSongActivity.TABLE_NAME;
    
    public static ManJianSongActivity findBySellerUin(Long sellerUin){
    	String sql=SelectManJian_SQL+" where sellerUin = ? ";
    	
    	ManJianSongActivity activity = new JDBCExecutor<ManJianSongActivity>(sql,sellerUin
                ) {
            @Override
            public ManJianSongActivity doWithResultSet(ResultSet rs) throws SQLException {
            	ManJianSongActivity list = null;
                if (rs.next()) {
                	ManJianSongActivity promotionList = parseActivity(rs);
                    if (promotionList != null)
                        list=promotionList;
                }
                return list;
            }
        }.call();
        
        return activity;
    }
    
    public static long countBySellerUin(Long sellerUin){
    	String sql=CountManJian_SQL+" where sellerUin = ?";
    	long count =JDBCBuilder.singleLongQuery(sql, sellerUin);
    	return count;
    }
    
    public static void deleteBySellerUin(Long sellerUin){
    	
    	String sql=" delete from "+ManJianSongActivity.TABLE_NAME+" where sellerUin = ? ";
    	
    	JDBCBuilder.update(false, sql, sellerUin);
    }
    
    public static ManJianSongActivity parseActivity(ResultSet rs) throws SQLException{
    	Long id=rs.getLong(1);
    	Long sellerUin=rs.getLong(2);
    	String beginTime=rs.getString(3);
    	String endTime=rs.getString(4);
    	String activityDesc=rs.getString(5);
    	String contentJson=rs.getString(6);
    	
    	ManJianSongActivity activity=new ManJianSongActivity(sellerUin, beginTime, endTime, activityDesc, contentJson);
    	
    	return activity;
    }
}
