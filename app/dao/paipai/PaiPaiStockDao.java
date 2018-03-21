package dao.paipai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.ppmanage.PPStock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

/**
 * @author haoyongzh
 *
 */
public class PaiPaiStockDao {

	private static final Logger log = LoggerFactory.getLogger(PaiPaiStockDao.class);
	
    public static final String TAG ="PaiPaiStockDao";
    
    public static final String Select_SQL=" select id, sellerUin, itemCode, skuId, price, picLink, num, status, soldNum, saleAttr, stockAttr from "+PPStock.TABLE_NAME+"%s ";
    
    public static List<PPStock> findStockByitemCode(Long sellerUin,String itemCode){

    	String sql=Select_SQL+" where sellerUin = ? and itemCode = ?";
    	sql=PPStock.genShardQuery(sql, sellerUin);

    	List<PPStock> StockList = new JDBCExecutor<List<PPStock>>(sql, sellerUin,itemCode) {
            @Override
            public List<PPStock> doWithResultSet(ResultSet rs) throws SQLException {
                List<PPStock> list = new ArrayList<PPStock>();
                while (rs.next()) {
                	PPStock Stock = parsePPStock(rs);
                    if (Stock != null)
                        list.add(Stock);
                }
                return list;
            }
        }.call();
        
        return StockList;
    }
    
    public static PPStock findStockBySkuId(Long sellerUin,Long skuId){
    	
    	String sql=Select_SQL+" where sellerUin = ? and skuId = ?";
    	sql=PPStock.genShardQuery(sql, sellerUin);
    	
    	PPStock activity = new JDBCExecutor<PPStock>(sql,sellerUin,skuId
                ) {
            @Override
            public PPStock doWithResultSet(ResultSet rs) throws SQLException {
            	PPStock list = null;
                if (rs.next()) {
                	PPStock stock = parsePPStock(rs);
                    if (stock != null)
                        list=stock;
                }
                return list;
            }
        }.call();
        
        return activity;
    }
    
    public static PPStock parsePPStock(ResultSet rs) throws SQLException{
    	Long sellerUin=rs.getLong(2);
    	String itemCode=rs.getString(3);
    	Long skuId=rs.getLong(4);
    	Long price=rs.getLong(5);
    	String picLink=rs.getString(6);
    	Long num=rs.getLong(7);
    	int status=rs.getInt(8);
    	Long soldNum=rs.getLong(9);
    	String saleAttr=rs.getString(10);
    	String stockAttr=rs.getString(11);
    	
    	PPStock stock = new PPStock(sellerUin, itemCode, skuId, price, picLink, num, status, soldNum, saleAttr, stockAttr);
    	
    	return stock;
    }
    
}
