package actions;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import result.TMResult;
import models.item.ItemPlay;
import models.user.User;
import uvpvdiag.UvPvDiagResult;
import actions.clouddata.SrcPlay;
import bustbapi.MBPApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.domain.QueryRow;

public class UvPvDiagAction {
	
	public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
	
	public static Map<Integer, String> srcIdNameMap = new HashMap<Integer, String>();
	
	public static Map<String, String> wirelessSrcIdNameMap = new HashMap<String, String>();
	
	static {
		srcIdNameMap.put(1, "自主访问");
		srcIdNameMap.put(2, "店铺收藏");
		srcIdNameMap.put(3, "宝贝收藏");
		srcIdNameMap.put(4, "卖家中心");
		srcIdNameMap.put(5, "我的淘宝");
		srcIdNameMap.put(6, "直接访问");
		srcIdNameMap.put(7, "购物车");
		srcIdNameMap.put(8, "淘宝付费流量");
		srcIdNameMap.put(9, "超级卖霸");
		srcIdNameMap.put(10, "淘宝客");
		srcIdNameMap.put(11, "直通车");
		srcIdNameMap.put(12, "品牌广告");
		srcIdNameMap.put(13, "钻石展位");
		srcIdNameMap.put(14, "定价CPM");
		srcIdNameMap.put(15, "阿里旺旺广告");
		srcIdNameMap.put(16, "富媒体广告");
		srcIdNameMap.put(17, "商业广告");
		srcIdNameMap.put(18, "全国联播");
		srcIdNameMap.put(19, "淘宝免费流量");
		srcIdNameMap.put(20, "淘宝搜索");
		srcIdNameMap.put(21, "淘宝店铺搜索");
		srcIdNameMap.put(22, "淘宝类目");
		srcIdNameMap.put(23, "淘宝专题");
		srcIdNameMap.put(24, "淘宝首页");
		srcIdNameMap.put(25, "淘宝频道");
		srcIdNameMap.put(26, "淘宝空间问");
		srcIdNameMap.put(27, "嗨淘");
		srcIdNameMap.put(28, "淘宝画报");
		srcIdNameMap.put(29, "淘江湖");
		srcIdNameMap.put(30, "淘宝其他店铺");
		srcIdNameMap.put(31, "淘宝信用评价");
		srcIdNameMap.put(32, "阿里旺旺非广告");
		srcIdNameMap.put(33, "淘宝客搜索");
		srcIdNameMap.put(34, "天猫首页");
		srcIdNameMap.put(35, "天猫类目");
		srcIdNameMap.put(36, "天猫搜索");
		srcIdNameMap.put(37, "天猫店铺搜索");
		srcIdNameMap.put(38, "天猫专题");
		srcIdNameMap.put(39, "天猫频道");
		srcIdNameMap.put(40, "聚划算");
		srcIdNameMap.put(41, "淘女郎");
		srcIdNameMap.put(42, "淘宝旅行");
		srcIdNameMap.put(43, "新品中心");
		srcIdNameMap.put(44, "试用中心");
		srcIdNameMap.put(45, "店铺动态");
		srcIdNameMap.put(46, "淘宝会员俱乐部");
		srcIdNameMap.put(47, "哇哦");
		srcIdNameMap.put(48, "全球购");
		srcIdNameMap.put(49, "店铺街");
		srcIdNameMap.put(50, "天天特价");
		srcIdNameMap.put(51, "我爱淘折");
		srcIdNameMap.put(52, "淘宝促销");
		srcIdNameMap.put(53, "集分宝");
		srcIdNameMap.put(54, "淘宝社区");
		srcIdNameMap.put(55, "淘宝门户");
		srcIdNameMap.put(56, "一淘求购");
		srcIdNameMap.put(57, "淘宝帮派");
		srcIdNameMap.put(58, "淘宝试衣间");
		srcIdNameMap.put(59, "淘宝站内其他");
		srcIdNameMap.put(60, "淘宝站外");
		srcIdNameMap.put(61, "搜索引擎");
		srcIdNameMap.put(62, "雅虎");
		srcIdNameMap.put(63, "百度");
		srcIdNameMap.put(64, "搜狗");
		srcIdNameMap.put(65, "谷歌");
		srcIdNameMap.put(66, "必应");
		srcIdNameMap.put(67, "aol");
		srcIdNameMap.put(68, "ok365");
		srcIdNameMap.put(69, "有道");
		srcIdNameMap.put(70, "爱问");
		srcIdNameMap.put(71, "ask");
		srcIdNameMap.put(72, "搜搜");
		srcIdNameMap.put(73, "114");
		srcIdNameMap.put(74, "淘宝站外其他");
		/*srcIdNameMap.put(75, "75好像没被用到");*/
		srcIdNameMap.put(76, "淘宝圈子");
		srcIdNameMap.put(77, "淘宝礼物");
		srcIdNameMap.put(78, "双11大促");
		srcIdNameMap.put(79, "淘宝双十二");
		srcIdNameMap.put(80, "钻石展位+");
		srcIdNameMap.put(81, "淘宝婚庆");
		srcIdNameMap.put(82, "淘宝清仓");
		/*srcIdNameMap.put(83, "83好像也没被用到");*/
		srcIdNameMap.put(84, "淘宝推推");
		srcIdNameMap.put(85, "淘宝预售");
		srcIdNameMap.put(86, "淘宝星店");
		srcIdNameMap.put(87, "淘宝收藏");
		srcIdNameMap.put(88, "淘宝管理后台");
		srcIdNameMap.put(89, "一淘");  // 这里的一淘是一淘自身
		srcIdNameMap.put(90, "一淘首页");
		srcIdNameMap.put(91, "一淘搜索");
		srcIdNameMap.put(92, "促销活动");
		srcIdNameMap.put(93, "优惠券");
		srcIdNameMap.put(94, "决策页");
		srcIdNameMap.put(95, "一淘其他");
		srcIdNameMap.put(96, "超优汇");
		srcIdNameMap.put(97, "淘宝值得买");
		srcIdNameMap.put(98, "聚想团");
		srcIdNameMap.put(99, "爱购");
		srcIdNameMap.put(100, "店内浏览");
		srcIdNameMap.put(101, "独畅团");
		srcIdNameMap.put(102, "新浪微博");
		srcIdNameMap.put(103, "淘宝站外-SNS网站");
		srcIdNameMap.put(104, "一淘"); // 这里的一淘是淘宝站外， 其实我也不晓得两个一淘有什么区别
		srcIdNameMap.put(105, "一淘首页");
		srcIdNameMap.put(106, "一淘搜索");
		srcIdNameMap.put(107, "促销活动");
		srcIdNameMap.put(108, "优惠券");
		srcIdNameMap.put(109, "决策页");
		srcIdNameMap.put(110, "一淘其他");
		srcIdNameMap.put(111, "360搜索");
		srcIdNameMap.put(112, "SNS");
		srcIdNameMap.put(113, "新浪微博");
		srcIdNameMap.put(114, "美丽说");
		srcIdNameMap.put(115, "蘑菇街");
		srcIdNameMap.put(116, "腾讯微博");
		srcIdNameMap.put(117, "QQ空间");
		/*srcIdNameMap.put(118, "118也没有被用到");*/
		srcIdNameMap.put(119, "天猫超市");
		srcIdNameMap.put(120, "淘宝尺码");
		srcIdNameMap.put(121, "拍卖会");
		srcIdNameMap.put(122, "天猫预售");
		srcIdNameMap.put(123, "个人主页");
	}

	public static UvPvDiagResult doDiag(User user, ItemPlay itemPlay, String title) throws ClientException {
		if(itemPlay == null) {
			return new UvPvDiagResult();
		}
		UvPvDiagResult result = new UvPvDiagResult(itemPlay);
		if(user == null) {
        	return result;
        }
		Long endTime = System.currentTimeMillis();
		Long startTime = endTime - 7 * DateUtil.DAY_MILLIS;
		TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(2948L, 
    			"startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + itemPlay.getNumIid() +
    			",sellerId=" + user.getId() + 
    			",enddate=" + sdf.format(new Date(endTime)) + ",platform=0", user.getSessionKey()).call();
		if(res.isOk()) {
			List<QueryRow> rows = res.getRes();
			if(!CommonUtils.isEmpty(rows)) {
				for(QueryRow row : rows) {
					result.addProps(row);
				}
			}
		}
		
		TMResult<List<QueryRow>> totalRes = new MBPApi.MBPDataGet(3213L, 
    			"startdate=" + sdf.format(new Date(startTime)) + ",numIid=" + itemPlay.getNumIid() +
    			",sellerId=" + user.getId() + 
    			",enddate=" + sdf.format(new Date(endTime)), user.getSessionKey()).call();
		if(res.isOk()) {
			List<QueryRow> totalImpressionAndClicks = totalRes.getRes();
			if(!CommonUtils.isEmpty(totalImpressionAndClicks)) {
				List<String> values = totalImpressionAndClicks.get(0).getValues();
				if(!CommonUtils.isEmpty(values)) {
					result.setImpression(Long.valueOf(totalImpressionAndClicks.get(0).getValues().get(0)));
					result.setClick(Integer.valueOf(totalImpressionAndClicks.get(0).getValues().get(1)));
				}
				
			}
		}
        return result;
    }
    
    public static String getWirelessSrcNameById(String srcId, User user){
    	SrcPlay existSrc = SrcPlay.findBySrcId(srcId);
    	if(existSrc != null) {
    		String srcName = existSrc.getSrc_name();
    		if(!StringUtils.isEmpty(srcName)) {
    			return srcName;
    		}
    	}
    	
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String enddate = format.format(new Date(System.currentTimeMillis() - DateUtil.TWO_DAY_MILLIS));
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(106754L,"enddate=" + enddate + ",srcId=" + srcId, user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if(CommonUtils.isEmpty(rows)){
            return null;
        }
        
        boolean is_pc = false;
        SrcPlay src = new SrcPlay(rows.get(0), is_pc);
        src.jdbcSave();
        return rows.get(0).getValues().get(3);
    }
    
    public static String getPCSrcNameById(String srcId, User user){
    	SrcPlay existSrc = SrcPlay.findBySrcId(srcId);
    	if(existSrc != null) {
    		String srcName = existSrc.getSrc_name();
    		if(!StringUtils.isEmpty(srcName)) {
    			return srcName;
    		}
    	}
    	
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        String enddate = format.format(new Date(System.currentTimeMillis() - DateUtil.TWO_DAY_MILLIS));
        TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(106771L,"enddate=" + enddate + ",srcId=" + srcId, user.getSessionKey()).call();
        List<QueryRow> rows = res.getRes();
        if(CommonUtils.isEmpty(rows)){
            return null;
        }
        
        boolean is_pc = true;
        SrcPlay src = new SrcPlay(rows.get(0), is_pc);
        src.jdbcSave();
        return rows.get(0).getValues().get(2);
    }
    
}
