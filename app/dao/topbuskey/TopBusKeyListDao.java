package dao.topbuskey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.word.top.BusCatPlay;
import models.word.top.BusTopKey;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.pojo.PageOffset;

public class TopBusKeyListDao {
	private static final Logger log = LoggerFactory.getLogger(TopBusKeyListDao.class);
	
	private static final String BusCatPlay_Sql = " select catId, level, name, parentId from " + BusCatPlay.TABLE_NAME;

	private static final String BusCatPlay_Count_Sql = " select count(*) from " + BusCatPlay.TABLE_NAME;
	
	private static final String BusTopKey_Sql = " select catLevel1,catLevel2,catLevel3,word, pv, click, competition from " 
	                                            + BusTopKey.TABLE_NAME;
	public static final String catlevel1 = "catlevel1";
	
	public static final String catlevel2 = "catlevel2";
	
	public static final String catlevel3 = "catlevel3";
	
	public static final String aclickdown = " order by abs(click) desc ";
	   
	private static final String BusTopKey_Count_Sql = " select count(*) from " + BusTopKey.TABLE_NAME;
	
	public static List<BusCatPlay> findBusCatPlayFirstLevelList(){
		
		String sql = BusCatPlay_Sql+" where level = 0;";
		
		return new JDBCExecutor<List<BusCatPlay>>(sql) {
            @Override
            public List<BusCatPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<BusCatPlay> list = new ArrayList<BusCatPlay>();
                while (rs.next()) {
                	BusCatPlay buscatplayList = parsebuscatplayList(rs);
                	if (buscatplayList != null)
                		list.add(buscatplayList);
                }
                return list;
            }
        }.call();
	}
	
	//下一级的parentId=上一级的catId
	public static List<BusCatPlay> findBusCatPlayListBycatId(long catId) {
		String sql = BusCatPlay_Sql+" where parentId = ?;";
		long parentId = catId;
		return new JDBCExecutor<List<BusCatPlay>>(sql,parentId) {
            @Override
            public List<BusCatPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<BusCatPlay> list = new ArrayList<BusCatPlay>();
                while (rs.next()) {
                	BusCatPlay buscatplayList = parsebuscatplayList(rs);
                	if (buscatplayList != null)
                		list.add(buscatplayList);
                }
                return list;
            }
        }.call();
	}
	/*
	 * 从BusTopkey数据库里面取
	 */
	public static TMResult finBusTopkeyListBylevel3(long catId, int ChoseLevel, String sortBy, PageOffset po){
		String level = StringUtils.EMPTY;
	    if(ChoseLevel == 1){
	        level = catlevel1;
	    } else if(ChoseLevel == 2){
	        level = catlevel2;
	    } else {
	        level = catlevel3;
	    }
	    if(sortBy == null || sortBy.isEmpty()){
	        sortBy = aclickdown;
	    }
	    String sql=BusTopKey_Sql+" where "+level+" = ? "+sortBy+" limit ?, ?";
		long catLevel3=catId;
		List<BusTopKey> bustopkeyList= new JDBCExecutor<List<BusTopKey>>(sql,catLevel3,po.getOffset(),po.getPs()){

			@Override
			public List<BusTopKey> doWithResultSet(ResultSet rs)
					throws SQLException {
	           
				List<BusTopKey> list = new ArrayList<BusTopKey>();
	               while (rs.next()) {
	                	BusTopKey bustopkeyList = parsebustopkeyList(rs);
	                	if (bustopkeyList != null)
	                		list.add(bustopkeyList);
	                }
	                return list;
			}
			
		}.call();
       
		String countSql = BusTopKey_Count_Sql+" where "+level+" = ?";
        long count = JDBCBuilder.singleLongQuery(countSql,catLevel3);
       
        TMResult tmResult = new TMResult(bustopkeyList, (int)count, po);
        return tmResult;
		
	}
	public static String DELETE_SQL = "delete from "+BusTopKey.TABLE_NAME +" where id = ";
	public static boolean deleteBustopkey(BusTopKey bustopkey){
	    String sql = DELETE_SQL + bustopkey.getId();

        long res = JDBCBuilder.insert(sql);

        if (res == 1) {
            return true;
        } else {
            log.error("delete failed...for bustopkey: [word : ]" + bustopkey.getWord());
            return false;
        }
	}
	
	public static TMResult searchWords(Long cat1Id, Long cat2Id, Long cat3Id, String sortBy, PageOffset po) {
	    String where = StringUtils.EMPTY;
	    where += " where catlevel1 = "+cat1Id;
	    if(cat2Id != null && cat2Id >= 0) {
            where += " and catlevel2 = " + cat2Id;
        }
	    if(cat3Id != null && cat3Id >= 0) {
	        where += " and catlevel3 = " + cat3Id;
	    }
	    if(sortBy == null || sortBy.isEmpty()){
            sortBy = aclickdown;
        }
	    String sql=BusTopKey_Sql+where+sortBy+" limit ?, ?";
	    List<BusTopKey> bustopkeyList= new JDBCExecutor<List<BusTopKey>>(sql,po.getOffset(),po.getPs()){

            @Override
            public List<BusTopKey> doWithResultSet(ResultSet rs)
                    throws SQLException {
               
                List<BusTopKey> list = new ArrayList<BusTopKey>();
                   while (rs.next()) {
                        BusTopKey bustopkeyList = parsebustopkeyList(rs);
                        if (bustopkeyList != null)
                            list.add(bustopkeyList);
                    }
                    return list;
            }
            
        }.call();
       
        String countSql = BusTopKey_Count_Sql+where;
        long count = JDBCBuilder.singleLongQuery(countSql);
       
        TMResult tmResult = new TMResult(bustopkeyList, (int)count, po);
        return tmResult;
	}
	
	private static BusCatPlay parsebuscatplayList(ResultSet rs) {
		try {
			Long catId = rs.getLong(1);
			int level = rs.getInt(2);
			String name = rs.getString(3);
			long parentId= rs.getLong(4);

			BusCatPlay buscatplayList = new BusCatPlay(name,level,parentId);
			buscatplayList.setCatId(catId);
			
			return buscatplayList;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}
//		/word, pv, click, competition
	private static BusTopKey parsebustopkeyList(ResultSet rs) {
			try {
				long catLevel1=rs.getLong(1);
				long catLevel2=rs.getLong(2);
				long catLevel3=rs.getLong(3);
				String word = rs.getString(4);
				int pv = Math.abs(rs.getInt(5));
				int click = Math.abs(rs.getInt(6));
				int competition= Math.abs(rs.getInt(7));

				BusTopKey bustopkeyList = new BusTopKey(catLevel1,catLevel2,catLevel3,word,pv, click,competition);

				return bustopkeyList;
				
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
				return null;
			}
	}
	
	private static String wordSelect = " select count(*) from " + BusTopKey.TABLE_NAME;
	public static boolean wordExisted(String word){
	    long count = JDBCBuilder.singleLongQuery(wordSelect+" where word = '"+word.trim()+"'");
	    return (count == 0)?false:true;
    }

}
