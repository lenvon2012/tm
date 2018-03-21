package dao.paipai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiTradeDisplay;
import ppapi.models.PaiPaiTradeItem;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

public class PaiPaiTradeDao {

	private static final Logger log = LoggerFactory.getLogger(PaiPaiTradeDao.class);
	
    public static final String TAG ="PaiPaiTradeDao";
    
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static final String TradeDisplay_SQL="select id,sellerUin,buyerUin,dealCode,buyerName,receiverAddress," +
    		"receiverName,receiverMobile,receiverPhone,dealState,dealRateState,createTime,payTime,totalCash from "+
    		PaiPaiTradeDisplay.TABLE_NAME+"%s";
    
    public static PaiPaiTradeDisplay findTradeDisplayByDealCode(Long sellerUin,String dealCode){
    	
    	String sql=TradeDisplay_SQL+" where sellerUin = ? and dealCode = ?";
    	sql=PaiPaiTradeDisplay.genShardQuery(sql, sellerUin);
    	
    	PaiPaiTradeDisplay TradeDisplayList = new JDBCExecutor<PaiPaiTradeDisplay>(sql, sellerUin,dealCode) {
            @Override
            public PaiPaiTradeDisplay doWithResultSet(ResultSet rs) throws SQLException {
            	PaiPaiTradeDisplay list = null;
                if (rs.next()) {
                	PaiPaiTradeDisplay TradeDisplay = parseTradeDisplay(rs);
                    if (TradeDisplay != null)
                        list=TradeDisplay;
                }
                return list;
            }
        }.call();
        
        return TradeDisplayList;
    }
    
    public static List<PaiPaiTradeDisplay> findByOrderCondition(Long sellerUin,String buyerName,String dealState,String createTime,String itemName
    		,String dealRateState,String dealCode,String buyerUin,PageOffset po){
    	String sql=TradeDisplay_SQL+" where sellerUin = ? ";
    	
    	sql=checkSql(sql, sellerUin, buyerName, dealState, createTime, itemName, dealRateState, dealCode,buyerUin);
    	
    	sql += " order by createTime desc " ;
    	
    	sql +=" limit ?,? ";
    	
    	List<PaiPaiTradeDisplay> TradeDisplayList = new JDBCExecutor<List<PaiPaiTradeDisplay>>(sql, sellerUin,po.getOffset(),po.getPs()) {
            @Override
            public List<PaiPaiTradeDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<PaiPaiTradeDisplay> list = new ArrayList<PaiPaiTradeDisplay>();
                while (rs.next()) {
                	PaiPaiTradeDisplay TradeDisplay = parseTradeDisplay(rs);
                    if (TradeDisplay != null)
                        list.add(TradeDisplay);
                }
                return list;
            }
        }.call();
        
        return TradeDisplayList;

    }
    public static long  countByOrderCondition(Long sellerUin,String buyerName,String dealState,String creatTime,String itemName
    		,String dealRateState,String dealCode,String buyerUin){
    	
    	String sql = " select count(*) from "+PaiPaiTradeDisplay.TABLE_NAME+"%s"+" where sellerUin = ? ";
    	
    	sql=checkSql(sql, sellerUin, buyerName, dealState, creatTime, itemName, dealRateState, dealCode,buyerUin);
    	
    	long count =JDBCBuilder.singleLongQuery(sql,sellerUin);
    	
    	return count;
    }
    
    public static String checkSql(String sql,Long sellerUin,String buyerName,String dealState,String createTime,String itemName
    		,String dealRateState,String dealCode,String buyerUin){
    	if(!StringUtils.isEmpty(buyerName)){
    		sql += " and buyerName = '"+buyerName+"'";
    	}
    	if(!StringUtils.isEmpty(dealState)){
    		sql += " and dealState = '"+dealState+"'";
    	}
    	if(!StringUtils.isEmpty(createTime)){
    		try {
				long ct=sdf.parse(createTime).getTime();
	    		sql += " and createTime > "+ct;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.warn(e.getMessage(),e);
			}
    	}
    	if(!StringUtils.isEmpty(dealRateState)){
    		sql += " and dealRateState = '"+dealRateState+"'";
    	}
    	if(!StringUtils.isEmpty(dealCode)){
    		sql += " and dealCode = '"+dealCode+"'";
    	}
    	if(!StringUtils.isEmpty(buyerUin)){
    		sql += " and buyerUin = "+buyerUin;
    	}
    	sql=PaiPaiTradeDisplay.genShardQuery(sql, sellerUin);
    	if(!StringUtils.isEmpty(itemName)){
    		String query=" and "+appendTitleLike(itemName)+"";
    		sql += " and dealCode in ("+" select dealCode from "+PaiPaiTradeItem.TABLE_NAME+"%s";
    		sql=PaiPaiTradeDisplay.genShardQuery(sql, sellerUin);
    		sql+=" where sellerUin = "+sellerUin+query+" )";
    	}
    	else{
    		sql=PaiPaiTradeDisplay.genShardQuery(sql, sellerUin);
    	}
    	return sql;
    }
    
    public static PaiPaiTradeDisplay parseTradeDisplay(ResultSet rs) throws SQLException{
    	Long id=rs.getLong(1);
        Long sellerUin = rs.getLong(2);
        Long buyerUin = rs.getLong(3);
        String dealCode = rs.getString(4);
        String buyerName = rs.getString(5);
        String receiverAddress = rs.getString(6);
        String receiverName = rs.getString(7);
        String receiverMobile = rs.getString(8);
        String receiverPhone = rs.getString(9);
        String dealState = rs.getString(10);
        String dealRateState = rs.getString(11);
        long createTime = rs.getLong(12);
        long payTime = rs.getLong(13);
        int totalCash = rs.getInt(14);
        
        PaiPaiTradeDisplay TradeDisplay = new PaiPaiTradeDisplay(id, sellerUin, buyerUin, dealCode, buyerName, receiverAddress, receiverName, receiverMobile, receiverPhone, dealState, dealRateState, createTime, payTime, totalCash);

        return TradeDisplay;
    }
    
    
    /* ------------------------------------------------------------------------ */
    
    public static final String TradeItem_SQL="select id,sellerUin,dealCode,itemCode,itemName" +
    		",picLink,createTime,itemDealPrice,itemDealCount from "+PaiPaiTradeItem.TABLE_NAME+"%s";
    
    public static List<PaiPaiTradeItem> findTradeItemByDealCode(Long sellerUin,String dealCode){
    	
    	String sql=TradeItem_SQL+" where sellerUin = ? and dealCode = ?";
    	sql=PaiPaiTradeItem.genShardQuery(sql, sellerUin);
    	List<PaiPaiTradeItem> TradeItemList = new JDBCExecutor<List<PaiPaiTradeItem>>(sql, sellerUin,dealCode) {
            @Override
            public List<PaiPaiTradeItem> doWithResultSet(ResultSet rs) throws SQLException {
                List<PaiPaiTradeItem> list = new ArrayList<PaiPaiTradeItem>();
                while (rs.next()) {
                	PaiPaiTradeItem TradeItem = parseTradeItem(rs);
                    if (TradeItem != null)
                        list.add(TradeItem);
                }
                return list;
            }
        }.call();
        
        return TradeItemList;
    }
    
    public static PaiPaiTradeItem parseTradeItem(ResultSet rs) throws SQLException{
    	Long id = rs.getLong(1);
        Long sellerUin = rs.getLong(2);
        String dealCode = rs.getString(3);
        String itemCode = rs.getString(4);
        String itemName = rs.getString(5);
        String picLink = rs.getString(6);
        long createTime = rs.getLong(7);
        int itemDealPrice = rs.getInt(8);
        int itemDealCount = rs.getInt(9);
        
        PaiPaiTradeItem tradeItem = new PaiPaiTradeItem(id, sellerUin, dealCode, itemCode, itemName, picLink, createTime, itemDealPrice, itemDealCount);
        
        return tradeItem;
    }
    
    public static String appendTitleLike(String key) {

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
}
