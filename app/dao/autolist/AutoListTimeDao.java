package dao.autolist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.autolist.AutoListTime;
import models.autolist.AutoListTime.DelistState;
import models.autolist.plan.UserDelistPlan;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

public class AutoListTimeDao {
	private static final Logger log = LoggerFactory.getLogger(AutoListTimeDao.class);
	
	public static boolean saveOrUpdateAutoListTime(AutoListTime autoListTime) {
		return autoListTime.jdbcSave();
	}
	
	public static void deleteAutoListTime(AutoListTime autoListTime) {
		String sql = "delete from auto_list_time where userId =? and planId = ? and numIid =?";
		//log.info(sql);
        JDBCBuilder.update(false, sql, autoListTime.getUserId(), autoListTime.getPlanId(), autoListTime.getNumIid());
	}
	
	public static void deleteAutoListTimeByUser(long userId) {
		String sql = "delete from auto_list_time where userId =?";
        JDBCBuilder.update(false, sql, userId);
	}
	
	public static boolean deleteAutoListTimeByNumIids(Long userId, long planId, Set<Long> numIidSet) {
	    if (CommonUtils.isEmpty(numIidSet)) {
	        return true;
	    }
	    
	    String sql = "delete from auto_list_time where userId =? and planId = ? " +
	    		" and numIid in (" + StringUtils.join(numIidSet, ",") + ") ";
	    
	    
	    AutoListTime.dp.update(sql, userId, planId);
	    
	    return true;
	}
	
	
	private static final String SelectAllProperty = " select id, userId, numIid, status, relativeListTime, listTime, planId ";
	
	/**
	 * 找到所有一定时间要上架的宝贝
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static List<AutoListTime> queryAutoListTimeByTime(long startTime, long endTime,
	        final boolean isFirstStartJob) {
		// 注意要除去用户关闭自动上下架的商品
		//String select = "select A.id, A.userId, A.numIid, A.status, A.relativeListTime, A.listTime ";
        //String query = select + " from auto_list_time A where relativeListTime>=? and relativeListTime<? and  A.userId not in (select userId from auto_list_record WHERE isTurnOn is false or isCalcuComplete is false)";

		String select = SelectAllProperty;
		String query = "";
		final int delistState = DelistState.DelistSuccess;
		if (isFirstStartJob == true) {
		    query = select + " from auto_list_time A where ((relativeListTime>=? and relativeListTime<?) or A.status = " + delistState + ") and listTime=0 and (A.planId in (select id from " + UserDelistPlan.TABLE_NAME + " u WHERE u.status = 1) or A.planId = 0)";
		} else {
		    query = select + " from auto_list_time A where (relativeListTime>=? and relativeListTime<?) and listTime=0 and (A.planId in (select id from " + UserDelistPlan.TABLE_NAME + " u WHERE u.status = 1) or A.planId = 0)";
		}
		
        List<AutoListTime> result = new ArrayList<AutoListTime>();

        // 转换为相对时间查询 ,注意相对时间可能结果oldTs大于newTs
        long startRelative = startTime - DateUtil.findThisWeekStart(startTime);
		long endRelative = endTime - DateUtil.findThisWeekStart(endTime);
		
		//startRelative = 0;
		//endRelative = 7000000000L;
        if (startRelative > endRelative) {
            log.info("relativeOldTs > relativeNewTs");
            if (isFirstStartJob == true) {
                query = select + " from auto_list_time A where ((relativeListTime>=? and relativeListTime<604800000)  or (relativeListTime>=0 and relativeListTime<?) or A.status = " + delistState + ") and listTime=0  and (A.planId in (select id from " + UserDelistPlan.TABLE_NAME + " u WHERE u.status = 1) or A.planId = 0)";
            } else {
                query = select + " from auto_list_time A where ((relativeListTime>=? and relativeListTime<604800000)  or (relativeListTime>=0 and relativeListTime<?)) and listTime=0  and (A.planId in (select id from " + UserDelistPlan.TABLE_NAME + " u WHERE u.status = 1) or A.planId = 0)";
            }
            
        }
        
        result = nativeQuery(query, startRelative, endRelative);
        return result;
	}

	public static List<AutoListTime> queryAllAutoListTime(long userId) {
		String select = SelectAllProperty;
        String query = select + " from auto_list_time A where relativeListTime>=? and relativeListTime<? and userId=?";
        return nativeQuery(query, 0L, 604800000L, userId);
	}
	
	
	public static List<Long> queryAllRelativeListTime(long userId) {
	    
	    final int limitNum = 10000;
	    int startIndex = 0;
	    
	    List<Long> resultList = new ArrayList<Long>();
	    
	    while (true) {
	        String query = "select relativeListTime from auto_list_time where userId = ? limit ?, ? ";
	        
	        List<Long> tempList = new JDBCExecutor<List<Long>>(query, userId, startIndex, limitNum) {

	            @Override
	            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
	                
	                List<Long> relativeList = new ArrayList<Long>();
	                
	                while (rs.next()) {
	                    relativeList.add(rs.getLong(1));
	                } 
	                
	                return relativeList;
	            }
	        }.call();
	        
	        if (CommonUtils.isEmpty(tempList)) {
	            return resultList;
	        }
	        
	        resultList.addAll(tempList);
	        
	        if (tempList.size() < limitNum) {
	            return resultList;
	        }
	        
	        startIndex += limitNum;
	        
	        log.error("delist time size is big than " + limitNum + " for userId: " + userId + "!!!!!!!!!!!!!!!!!!!");
	    }
	    
	    

	    
	}
	
	
	private static AutoListTime singleQuery(String query, Object...params) {
        
        
        return new JDBCExecutor<AutoListTime>(query, params) {

            @Override
            public AutoListTime doWithResultSet(ResultSet rs) throws SQLException {
                
                if (rs.next()) {
                    AutoListTime autoListTime = new AutoListTime();
                    autoListTime.setId(rs.getLong(1));
                    autoListTime.setUserId(rs.getLong(2));
                    autoListTime.setNumIid(rs.getLong(3));
                    autoListTime.setStatus(rs.getInt(4));
                    autoListTime.setRelativeListTime(rs.getLong(5));
                    autoListTime.setListTime(rs.getLong(6));
                    autoListTime.setPlanId(rs.getLong(7));
                    
                    return autoListTime;
                } else {
                    return null;
                }
                
            }
        }.call();

        
    }
    
	
	private static List<AutoListTime> nativeQuery(String query, Object...params) {
		/*List<AutoListTime> result = new ArrayList<AutoListTime>();
		
		Query queryObj = AutoListTime.em().createNativeQuery(query);
		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				queryObj.setParameter(i + 1, params[i]);
			}
		}
		
		List<Object[]> list = queryObj.getResultList();

        for (Object[] valueArr : list) {
        	AutoListTime autoListTime = new AutoListTime();
        	autoListTime.setId(((BigInteger)valueArr[0]).longValue());
        	autoListTime.setUserId(((BigInteger)valueArr[1]).longValue());
        	autoListTime.setNumIid(((BigInteger)valueArr[2]).longValue());
        	autoListTime.setStatus((Integer)valueArr[3]);
        	autoListTime.setRelativeListTime(((BigInteger)valueArr[4]).longValue());
        	autoListTime.setListTime(((BigInteger)valueArr[5]).longValue());
        	result.add(autoListTime);
        }
        return result;*/
		
		return new JDBCExecutor<List<AutoListTime>>(query, params) {

            @Override
            public List<AutoListTime> doWithResultSet(ResultSet rs) throws SQLException {
                final List<AutoListTime> resulteList = new ArrayList<AutoListTime>();
                while (rs.next()) {
                	AutoListTime autoListTime = new AutoListTime();
                	autoListTime.setId(rs.getLong(1));
                	autoListTime.setUserId(rs.getLong(2));
                	autoListTime.setNumIid(rs.getLong(3));
                	autoListTime.setStatus(rs.getInt(4));
                	autoListTime.setRelativeListTime(rs.getLong(5));
                	autoListTime.setListTime(rs.getLong(6));
                	autoListTime.setPlanId(rs.getLong(7));
                	resulteList.add(autoListTime);
                }
                return resulteList;
            }
        }.call();

		
	}
	
	
	public static long queryTodayListNum(long nowTime, long userId, long planId) {
		long dailyTime = DateUtil.formDailyTimestamp(nowTime);
		
		long startRelative = 0;
		//表明是今天才开通自动上下架的，起始时间应该从开通那刻算起
		//if (dailyTime == recordDayTime) {
		//	startRelative = recordTime - DateUtil.findThisWeekStart(recordTime);
		//} else
			startRelative = dailyTime - DateUtil.findThisWeekStart(nowTime);
		long endRelative = dailyTime - DateUtil.findThisWeekStart(nowTime) + DateUtil.DAY_MILLIS;
		//String query = "relativeListTime>=? and relativeListTime<? and listTime=0 and userId = ?";
		String query = "select count(*) from "+AutoListTime.TABLE_NAME+" where relativeListTime>=? and relativeListTime<? and listTime=0 and userId = ? and planId = ?";
		//return AutoListTime.count(query, startRelative, endRelative, userId);
		return AutoListTime.countByUserIdWithListTime(query, startRelative, endRelative, userId, planId);
		
	}
	
	public static long queryTodayAreadyListNum(long nowTime, long userId) {
		long listTime = DateUtil.formDailyTimestamp(nowTime) - DateUtil.DAY_MILLIS;
		long startRelative = DateUtil.formDailyTimestamp(nowTime) - DateUtil.findThisWeekStart(nowTime);
		long endRelative = nowTime - DateUtil.findThisWeekStart(nowTime);
		String query = "select count(*) from "+AutoListTime.TABLE_NAME+" where relativeListTime>=? and relativeListTime<? and listTime>? and status=? and userId = ?";
		//return AutoListTime.count(query, startRelative, endRelative, listTime, DelistState.Success, userId);
		return AutoListTime.dp.singleLongQuery(query, startRelative, endRelative, listTime, DelistState.Success, userId);
	}
	
	public static List<AutoListTime> queryTodayList(long nowTime, long userId, long planId,
			int startPage, int pageSize) {
		long dailyTime = DateUtil.formDailyTimestamp(nowTime);
		long startRelative = 0;
		//表明是今天才开通自动上下架的，起始时间应该从开通那刻算起
		//if (dailyTime == recordDayTime) {
		//	startRelative = recordTime - DateUtil.findThisWeekStart(recordTime);
		//} else
			startRelative = dailyTime - DateUtil.findThisWeekStart(nowTime);
		long endRelative = dailyTime - DateUtil.findThisWeekStart(nowTime) + DateUtil.DAY_MILLIS;
		//String query = "relativeListTime>=? and relativeListTime<? and listTime=0 and userId = ?  order by relativeListTime asc";
		String query = SelectAllProperty +" from "+AutoListTime.TABLE_NAME+" where relativeListTime>=? and relativeListTime<? and listTime=0 and userId = ? and planId = ? order by relativeListTime asc limit ?,?";
		//return AutoListTime.find(query, startRelative, endRelative, userId)
		//		.fetch(startPage, pageSize);
		return nativeQuery(query, startRelative, endRelative, userId, planId, (startPage-1)*pageSize, pageSize);
		
	}
	
	public static List<AutoListTime> queryWeekList(long userId, long planId, int startPage, int pageSize) {
		String query = SelectAllProperty +" from "+AutoListTime.TABLE_NAME+" where userId = ? and planId = ? order by relativeListTime asc limit ?,?";
		//return AutoListTime.find(query, userId).fetch(startPage, pageSize);
		return nativeQuery(query, userId, planId, (startPage-1)*pageSize, pageSize);
	}
	
	
	public static long queryWeekNum(long userId, long planId) {
		String query = " select count(*) from "+AutoListTime.TABLE_NAME+" where userId = ? and planId = ?";
		//return AutoListTime.count(query, userId);
		return AutoListTime.dp.singleLongQuery(query, userId, planId);
	}
	
	
	public static List<AutoListTime> queryByTitle(long userId, long planId, String title, PageOffset po) {
		if (StringUtils.isBlank(title)) {
			String query = SelectAllProperty +" from "+AutoListTime.TABLE_NAME+" where userId = ? and planId = ? order by relativeListTime asc limit ?,?";
			//return AutoListTime.find(query, userId).fetch(po.getPn(), po.getPs());
			return nativeQuery(query, userId, planId, po.getOffset(), po.getPs());
		} else {
			String query = SelectAllProperty + " from auto_list_time A where userId = ? and planId = ? and numIid in (select numIid from item%s WHERE title like  '%" + title + "%') order by relativeListTime asc limit ?, ? ";
			query = ItemDao.genShardQuery(query, userId);
			return nativeQuery(query, userId, planId, po.getOffset(), po.getPs());
		}
	}
	
	public static List<AutoListTime> queryByNumIids(long userId, long planId, String numIids, PageOffset po) {
        if (StringUtils.isBlank(numIids)) {
            String query = SelectAllProperty +" from "+AutoListTime.TABLE_NAME+" where userId = ? and planId = ? order by relativeListTime asc limit ?,?";
            //return AutoListTime.find(query, userId).fetch(po.getPn(), po.getPs());
            return nativeQuery(query, userId, planId, po.getOffset(), po.getPs());
        } else {
            String query = SelectAllProperty + " from auto_list_time A where userId = ? and planId = ? and numIid in (" + numIids + ") order by relativeListTime asc limit ?, ? ";
            query = ItemDao.genShardQuery(query, userId);
            return nativeQuery(query, userId, planId, po.getOffset(), po.getPs());
        }
    }
	
	public static AutoListTime queryByNumIidWithJDBC(long userId, Long numIid) {
	    String query = SelectAllProperty + " from auto_list_time A where userId = ?  and numIid = ? ";
        query = ItemDao.genShardQuery(query, userId);
        return singleQuery(query, userId, numIid);
    }
	
	public static List<AutoListTime> queryByNumIidsAndPlanId(long userId, Set<Long> numIidSet, long planId) {
	    if (CommonUtils.isEmpty(numIidSet)) {
	        return new ArrayList<AutoListTime>();
	    }
	    
	    String query = SelectAllProperty + " from auto_list_time A where userId = ?  and planId = ? " +
	    		" and numIid in (" + StringUtils.join(numIidSet, ",") + ") ";
        
        return nativeQuery(query, userId, planId);
    }
	
	public static List<AutoListTime> queryListByNumIidWithJDBC(long userId, Long numIid) {
	    String query = SelectAllProperty + " from auto_list_time A where userId = ?  and numIid = ? ";
        query = ItemDao.genShardQuery(query, userId);
        return nativeQuery(query, userId, numIid);
    }
	
	public static long queryNumByTitle(long userId, String title) {
		if (StringUtils.isBlank(title)) {
			String query = "select count(*) from "+AutoListTime.TABLE_NAME+" where userId = ?";
			//return AutoListTime.count(query, userId);
			return AutoListTime.dp.singleLongQuery(query, userId);
		} else {
			String query = "select count(*) from auto_list_time A where userId = ?  and numIid in (select numIid from item%s WHERE title like '%" + title + "%' )";
			query = ItemDao.genShardQuery(query, userId);
			return JDBCBuilder.singleLongQuery(query, userId);
		}
			
		
	}
	
	public static long queryNumByNumIids(long userId, long planId, String numIids) {
        if (StringUtils.isBlank(numIids)) {
            String query = "select count(*) from "+AutoListTime.TABLE_NAME+" where userId = ? and planId = ?";
            //return AutoListTime.count(query, userId);
            return AutoListTime.dp.singleLongQuery(query, userId, planId);
        } else {
            String query = "select count(*) from auto_list_time A where userId = ? and planId = ? and numIid in (" + numIids + ")";
            query = ItemDao.genShardQuery(query, userId);
            return JDBCBuilder.singleLongQuery(query, userId, planId);
        }
            
        
    }
	
	public static AutoListTime queryByNumIid(Long userId, long planId, Long numIid) {
		String query = SelectAllProperty +" from "+AutoListTime.TABLE_NAME+" where userId=? and planId = ? and numIid=?";
		//return AutoListTime.find(query, userId, numIid).first();
		/*List<AutoListTime> list = nativeQuery(query, userId, numIid);
		if(list.size() == 0){
			return null;
		} else {
			return list.get(0);
		}*/
		return singleQuery(query, userId, planId, numIid);
	}
	
	
	/*public static long queryListLogNum(long userId) {
		String query = "userId = ? and listTime > 0";//好像差一点
		return AutoListTime.count(query, userId);
	}
	
	public static List<AutoListTime> queryListLog(long userId, int startPage, int pageSize) {
		String query = "userId = ? and listTime > 0 order by listTime desc";
		return AutoListTime.find(query, userId).fetch(startPage, pageSize);
	}*/

    public static long deleteOld(long endTs, int limit) {
        return JDBCBuilder.update(false, "delete from auto_list_time where listTime > 0 and listTime<? limit ?", endTs, limit);
    }

    public static void setNotFoundUser(AutoListTime autoListTime) {
        autoListTime.setStatus(DelistState.NotFoundUser);
        saveOrUpdateAutoListTime(autoListTime);
    }
	
	public static void setDelistFail(AutoListTime autoListTime) {
		autoListTime.setStatus(DelistState.DeListFail);
		saveOrUpdateAutoListTime(autoListTime);
	}
	
	public static void setListFail(AutoListTime autoListTime) {
		autoListTime.setStatus(DelistState.ListFail);
		saveOrUpdateAutoListTime(autoListTime);
	}
	
	public static void setListSuccess(AutoListTime autoListTime) {
		autoListTime.setStatus(DelistState.Success);
		saveOrUpdateAutoListTime(autoListTime);
	}
	
	
	public static boolean deleteByPlanId(Long planId, Long userId) {
	    String deleteSql = "delete from " + AutoListTime.TABLE_NAME + " where planId = ? and userId = ?";
	    
	    long deleteNum = AutoListTime.dp.update(deleteSql, planId, userId);
	    
	    if (deleteNum > 0) {
	        return true;
	    } else {
	        return false;
	    }
	    
	}
	
	public static boolean updateZeroPlanId(Long userId, Long planId) {
	    String sql = " update " + AutoListTime.TABLE_NAME + " set planId = ? where userId = ? and planId = 0";
	    
	    AutoListTime.dp.update(sql, planId, userId);
	    
	    return true;
	}
	
	public static boolean deleteNoPlanIdItems(Long userId) {
	    String sql = " delete from " + AutoListTime.TABLE_NAME + " where userId = ? and (planId is null or planId <= 0)";
	    AutoListTime.dp.update(sql, userId);
        
        return true;
	}
	
	public static long countByPlanId(Long planId, Long userId) {
	    String query = " select count(*) from " + AutoListTime.TABLE_NAME + " where planId = ? and userId = ?";
	    
	    long count = AutoListTime.dp.singleLongQuery(query, planId, userId);
	    
	    return count;
	    
	}
	
	public static long countByUserId(Long userId) {
        String query = " select count(*) from " + AutoListTime.TABLE_NAME + " where userId = ?";
        
        long count = AutoListTime.dp.singleLongQuery(query, userId);
        
        return count;
        
    }
	
	
	public static Set<Long> findNumIidsByUserId(Long userId) {
	    int EachQueryNum = 10000;
	    int limitStart = 0;
	    
	    Set<Long> resultSet = new HashSet<Long>();
	    while (true) {
            String limitSql = "select numIid from " + AutoListTime.TABLE_NAME 
                    + " where userId = ? order by numIid asc limit " + limitStart + ", " + EachQueryNum;
            Set<Long> tempSet = new JDBCLongSetExecutor(AutoListTime.dp, limitSql, userId){}.call();
            if (CommonUtils.isEmpty(tempSet)) {
                break;
            }
            resultSet.addAll(tempSet);
            if (tempSet.size() < EachQueryNum) {
                break;
            }
            limitStart += EachQueryNum;
            log.error("autolisttime size is big than " + EachQueryNum + " !!!!!!!!!!!!!!!!!!!");
        }
	    
	    return resultSet;
	}
	
	public static List<AutoListTime> findListTimeByPlanId(Long planId, Long userId) {
        int EachQueryNum = 10000;
        int limitStart = 0;
        
        List<AutoListTime> resultList = new ArrayList<AutoListTime>();
        while (true) {
            String limitSql = SelectAllProperty + " from " + AutoListTime.TABLE_NAME + " where planId = ? and userId = ? "
                    + " order by numIid asc limit " + limitStart + ", " + EachQueryNum;
            List<AutoListTime> tempList = nativeQuery(limitSql, planId, userId);
            if (CommonUtils.isEmpty(tempList)) {
                break;
            }
            resultList.addAll(tempList);
            if (tempList.size() < EachQueryNum) {
                break;
            }
            limitStart += EachQueryNum;
            log.error("autolisttime size is big than " + EachQueryNum + " !!!!!!!!!!!!!!!!!!!");
        }
        
        return resultList;
    }
	
	public static Set<Long> findNumIidsByPlanId(long planId, Long userId) {
        int EachQueryNum = 10000;
        int limitStart = 0;
        
        Set<Long> resultSet = new HashSet<Long>();
        while (true) {
            String limitSql = "select numIid from " + AutoListTime.TABLE_NAME 
                    + " where planId = ? and userId = ? order by numIid asc limit " + limitStart + ", " + EachQueryNum;
            Set<Long> tempSet = new JDBCLongSetExecutor(AutoListTime.dp, limitSql, planId, userId){}.call();
            if (CommonUtils.isEmpty(tempSet)) {
                break;
            }
            resultSet.addAll(tempSet);
            if (tempSet.size() < EachQueryNum) {
                break;
            }
            limitStart += EachQueryNum;
            log.error("autolisttime size is big than " + EachQueryNum + " !!!!!!!!!!!!!!!!!!!");
        }
        
        return resultSet;
    }
	
	
	
	public static void main(String[] args) {
		
		long time = System.currentTimeMillis();
		long daily = DateUtil.formDailyTimestamp(time);
		long startWeek = DateUtil.findThisWeekStart(time);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		log.info(sdf.format(new Date(daily)));
		log.info(sdf.format(new Date(startWeek)));
		
		//long time = 7 * 24 * 60 * 60 * 1000;
		log.info(time + "");
		log.info((1378396800000L - DateUtil.DAY_MILLIS) + "");
	}
}	
