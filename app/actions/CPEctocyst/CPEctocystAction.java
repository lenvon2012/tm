package actions.CPEctocyst;

import java.util.ArrayList;
import java.util.List;

import models.CPEctocyst.SellerToStaff;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import controllers.CPEctocyst;
import controllers.newAutoTitle;
import controllers.CPEctocyst.SellerInfo;
import dao.UserDao;
import dao.trade.TradeRatePlayDao;

public class CPEctocystAction {

	public static final String TAG = "CPEctocystAction";
	
	private static final Logger log = LoggerFactory.getLogger(CPEctocystAction.class);
	
	public static List<SellerInfo> genSellerInfoList(List<SellerToStaff> sellers) {
		List<SellerInfo> infos = new ArrayList<CPEctocyst.SellerInfo>();
		if(CommonUtils.isEmpty(sellers)) {
			return infos;
		}
		for(SellerToStaff sellerToStaff : sellers) {
			SellerInfo sellerInfo = genSellerInfo(sellerToStaff);
			if(sellerInfo == null) {
				continue;
			}
			infos.add(sellerInfo);
		}
		return infos;
	}
	
	public static SellerInfo genSellerInfo(SellerToStaff sellerToStaff) {
		if(sellerToStaff == null) {
			return null;
		}
		SellerInfo sellerInfo = new SellerInfo();
		Long userId = sellerToStaff.getUserId();
		User user = UserDao.findById(userId);
		if(user == null) {
			return null;
		}
		sellerInfo.setUserId(userId);
		sellerInfo.setUserNick(user.getUserNick());
		sellerInfo.setUserLevel(user.getLevel());
		sellerInfo.setChiefId(sellerToStaff.getChiefId());
		sellerInfo.setChiefName(sellerToStaff.getChiefName());
		
		// 如果已经分配给客服了
		if(sellerToStaff.getSubStaffId() != null && sellerToStaff.getSubStaffId() >= 0L) {
			sellerInfo.setSubStaffId(sellerToStaff.getSubStaffId());
			sellerInfo.setSubStaffName(sellerToStaff.getSubStaffName());
		}
		
		// 设置可处理的中差评数目
		Long endTs = System.currentTimeMillis();
		Long startTs = endTs - com.ciaosir.client.utils.DateUtil.DAY_MILLIS * 30;

		int count = (int) TradeRatePlayDao.countWithArgs(userId, null,
				StringUtils.EMPTY, 4, startTs, endTs, -1L, null);
		sellerInfo.setTotalToDeal(count);
		
		return sellerInfo;
	}
	
	
}
