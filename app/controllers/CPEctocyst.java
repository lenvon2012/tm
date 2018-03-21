package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import models.CPEctocyst.SellerToStaff;
import models.op.CHEctocystLog;
import models.op.CPStaff;
import models.op.CPStaff.Role;
import models.order.OrderDisplay;
import models.traderate.OrderPlay;
import models.traderate.TradeRatePlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.NoTransaction;
import play.mvc.Before;
import play.mvc.Controller;
import result.TMResult;
import transaction.MapIterator;
import utils.DateUtil;
import utils.PlayUtil;
import actions.CPEctocyst.CPEctocystAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NetworkUtil;
import com.google.common.collect.Lists;

import controllers.SkinComment.CommentStatus;
import controllers.TMController.BusUIResult;
import dao.UserDao;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeRatePlayDao;

public class CPEctocyst extends Controller{
	private static final Logger log = LoggerFactory.getLogger(CPEctocyst.class);
	
	public static final SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");

    public static final String TAG = "CPEctocyst";
    
    public static void CPlogin() {
        render("CPEctocyst/login.html");
    }
    
    public static void staffAdmin() {
    	render("CPEctocyst/staffAdmin.html");
    }
    
    public static void rateAdmin(String name) {
    	render("CPEctocyst/rateAdmin.html", name);
    }
    
    public static void sellerAdmin() {
    	render("CPEctocyst/sellerAdmin.html");
    }
    
    @Before(unless = {
    		"CPEctocyst.CPlogin", "CPEctocyst.ensureCPStaff"
    })
    
    protected static void before() {

        CPStaff user = getStaff();

        if (user == null) {
            log.error("不存在登陆用户，请先登陆");
            redirect("/CPEctocyst/CPlogin");
        }

        String name = user.getName();

        String requestUrl = request.url;

        String requestAction = request.action;

        String ip = NetworkUtil.getRemoteIPForNginx(request);

        CHEctocystLog log = new CHEctocystLog(name, requestUrl, requestAction, ip);

        log.jdbcSave();

    }
    
    static CPStaff getStaff() {
        String userName = PlayUtil.getCookieString(request, "login-user");
        if (userName == null) {
            return null;
        }
        try {
			userName = URLDecoder.decode(userName, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        CPStaff user = CPStaff.findByName(userName);
        return user;
    }
    
    public static void addSubStaff(String name, int role, String password, String phone) {
    	if(StringUtils.isEmpty(name)) {
    		renderFailedJson("用户名为空");
    	}
    	if(StringUtils.isEmpty(password)) {
    		renderFailedJson("密码为空");
    	}
    	if(StringUtils.isEmpty(phone)) {
    		renderFailedJson("电话为空");
    	}
    	
    	Role r  = CPStaff.Role.SWEETY;
    	if(role == 2) {
    		r = CPStaff.Role.ADMIN;
    	}
    	
    	CPStaff parent = getStaff();
    	if(parent == null) {
    		log.error("不存在登陆用户，请先登陆");
            redirect("/CPEctocyst/CPlogin");
    	}
    	if(!parent.isSuperAdmin()) {
    		renderFailedJson("亲，您尚无该操作权限！");
    	}
    	
    	CPStaff subStaff = CPStaff.findByName(name);
    	if(subStaff != null) {
    		renderFailedJson("该客服名已经被注册，请重新输入");
    	}
    	
    	subStaff = new CPStaff(name, password, phone, StringUtils.EMPTY, CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL,r);
    	subStaff.setParentName(parent.getName());
    	
    	subStaff = subStaff.save();
    	if(subStaff == null) {
    		renderFailedJson("新增客服失败");
    	}
    	Cache.set(CPStaff.CPSTaffCache + name.trim(), subStaff, "2h");
    	Cache.delete(ALL_SUB_STAFFS_CACHE + parent.getName().trim());
    	renderSuccessJson("新增客服成功");
    }
    
    public static void getSubStaffs(int pn, int ps, String searchText) {
    	CPStaff staff = getStaff();
    	if(staff == null) {
    		log.error("不存在登陆用户，请先登陆");
            redirect("/CPEctocyst/CPlogin");
    	}
    	if(!staff.isSuperAdmin()) {
    		if(!staff.isAdmin()) {
    			renderFailedJson("亲还不是主账号哦");
    		}
    		staff = CPStaff.findByName(staff.getParentName());
    	}
    	
    	PageOffset po = new PageOffset(pn, ps, 10);
    	List<CPStaff> staffs = new ArrayList<CPStaff>();
    	int count = 0;
    	if(StringUtils.isEmpty(searchText)) {
    		staffs = CPStaff.find("parentName = ?", staff.getName()).fetch(po.getPn(), po.getPs());
    		count = (int) CPStaff.count("parentName = ?", staff.getName());
    	} else {
    		staffs = CPStaff.find("parentName = ? and name = ?", staff.getName(), searchText).fetch(po.getPn(), po.getPs());
    		count = (int) CPStaff.count("parentName = ? and name = ?", staff.getName(), searchText);
    	}
    	if(!CommonUtils.isEmpty(staffs)) {
    		for(CPStaff sub : staffs) {
    			sub.setAcceptNum(SellerToStaff.countBySubStaffId(sub.getId(), StringUtils.EMPTY));
    		}
    	}
    	renderJSON(JsonUtil.getJson(new TMResult(staffs, count, po)));
    }
    
    public static final String ALL_SUB_STAFFS_CACHE = "ALL_SUB_STAFFS_CACHE_";
    public static void getAllSubStaffs() {
    	CPStaff staff = getStaff();
    	if(staff == null) {
    		log.error("不存在登陆用户，请先登陆");
            redirect("/CPEctocyst/CPlogin");
    	}
    	if(!staff.isSuperAdmin()) {
    		if(!staff.isAdmin()) {
    			renderFailedJson("亲还不是主账号哦");
    		}
    		staff = CPStaff.findByName(staff.getParentName());
    	}
    	if(StringUtils.isEmpty(staff.getName())) {
    		renderFailedJson("公司名怎么会空呢!");
    	}
    	List<CPStaff> staffs = new ArrayList<CPStaff>();
    	staffs = (List<CPStaff>) Cache.get(ALL_SUB_STAFFS_CACHE + staff.getName().trim());
    	if(!CommonUtils.isEmpty(staffs)) {
    		renderJSON(JsonUtil.getJson(staffs));
    	}
    	staffs = CPStaff.find("parentName = ? and role = 0", staff.getName()).fetch();
    	Cache.set(ALL_SUB_STAFFS_CACHE + staff.getName().trim(), staffs, "1h");
    	renderJSON(JsonUtil.getJson(staffs));
    }
    
    public static void deleteAllSubStaffsCache(String parentName) {
    	if(StringUtils.isEmpty(parentName)) {
    		renderFailedJson("传入的公司名为空");
    	}
    	Cache.delete(ALL_SUB_STAFFS_CACHE + parentName.trim()); 
    }
    
	public static void deleteByParent(Long subStaffId) {
		CPStaff parent = getStaff();
		if(!parent.isSuperAdmin()) {
			renderFailedJson("亲，您尚无该操作权限！");
		}
		
		if(subStaffId == null || subStaffId < 0L) {
			renderFailedJson("要修改的客服ID为空");
		}
		CPStaff staff = CPStaff.findById(subStaffId);
		if(staff == null) {
			renderFailedJson("找不到要修改的客服");
		}
		
		staff = staff.delete();
		
		if(staff == null) {
			renderFailedJson("删除客服失败，请重试或联系客服");
		}
		Cache.delete(CPStaff.CPSTaffCache + staff.getName().trim());
		renderSuccessJson("删除客服失败，请重试或联系客服");
	}
	
	public static void changePswByParent(Long subStaffId, String newPassword) {
		CPStaff parent = getStaff();
		if(!parent.isSuperAdmin()) {
			renderFailedJson("亲，您尚无该操作权限！");
		}
		
		if(subStaffId == null || subStaffId < 0L) {
			renderFailedJson("要修改的客服ID为空");
		}
		if(StringUtils.isEmpty(newPassword)) {
			renderFailedJson("不允许空密码");
		}
		if(!CPStaff.isPasswordValid(newPassword)) {
			renderFailedJson("密码格式不规范，建议以数字字母组成，6-64个字符长度");
		}
		CPStaff staff = CPStaff.findById(subStaffId);
		if(staff == null) {
			renderFailedJson("找不到要修改的客服");
		}
		staff.setPassword(newPassword);
		staff = staff.save();
		if(staff == null) {
			renderFailedJson("修改客服密码失败，请重试或联系客服");
		}
		Cache.set(CPStaff.CPSTaffCache + staff.getName().trim(), staff, "2h");
		renderSuccessJson("修改客服密码失败，请重试或联系客服");
	}
	
	public static void allocateSellerToSubStaff(Long userId, Long subStaffId) {
		CPStaff parent = getStaff();
		if(!parent.isSuperAdmin()) {
			renderFailedJson("亲，您尚无该操作权限！");
		}
		
		if(userId == null || userId < 0L) {
			renderFailedJson("请传入正确的卖家ID");
		}
		if(subStaffId == null || subStaffId < 0L) {
			renderFailedJson("请传入正确的客服ID");
		}
		SellerToStaff sellerToStaff = SellerToStaff.findByUserId(userId);
		if(sellerToStaff == null) {
			renderFailedJson("该卖家选择其他外包商");
		}
		CPStaff subStaff = CPStaff.findById(subStaffId);
		if(subStaff == null) {
			renderFailedJson("亲，你当前没有该客服哦");
		}
		sellerToStaff.setSubStaffName(subStaff.getName());
		sellerToStaff.setSubStaffId(subStaff.getId());
		Boolean isSuccess = sellerToStaff.jdbcSave();
		if(!isSuccess) {
			renderFailedJson("分配失败，请重试或联系客服");
		}
		renderSuccessJson("分配成功");
	}
	
    public static class SellerInfo {
        public long userId;
        
        public String userNick;
        
        // 外包老板ID
        public long chiefId;
        
        // 外包老板Name
        public String chiefName;
        
        // 外包客服ID
        public long subStaffId;
        
        // 外包客服Name
        public String subStaffName;
        
        // 店铺等级
        public int userLevel;
        
        // 所有可处理
        public int totalToDeal;

        public SellerInfo() {
			super();
		}
        
		public SellerInfo(long userId, String userNick, long chiefId,
				String chiefName, long subStaffId, String subStaffName,
				int userLevel, int totalToDeal) {
			super();
			this.userId = userId;
			this.userNick = userNick;
			this.chiefId = chiefId;
			this.chiefName = chiefName;
			this.subStaffId = subStaffId;
			this.subStaffName = subStaffName;
			this.userLevel = userLevel;
			this.totalToDeal = totalToDeal;
		}

		public long getUserId() {
			return userId;
		}

		public void setUserId(long userId) {
			this.userId = userId;
		}

		public String getUserNick() {
			return userNick;
		}

		public void setUserNick(String userNick) {
			this.userNick = userNick;
		}

		public int getUserLevel() {
			return userLevel;
		}

		public void setUserLevel(int userLevel) {
			this.userLevel = userLevel;
		}

		public long getChiefId() {
			return chiefId;
		}

		public void setChiefId(long chiefId) {
			this.chiefId = chiefId;
		}

		public String getChiefName() {
			return chiefName;
		}

		public void setChiefName(String chiefName) {
			this.chiefName = chiefName;
		}

		public long getSubStaffId() {
			return subStaffId;
		}

		public void setSubStaffId(long subStaffId) {
			this.subStaffId = subStaffId;
		}

		public String getSubStaffName() {
			return subStaffName;
		}

		public void setSubStaffName(String subStaffName) {
			this.subStaffName = subStaffName;
		}

		public int getTotalToDeal() {
			return totalToDeal;
		}

		public void setTotalToDeal(int totalToDeal) {
			this.totalToDeal = totalToDeal;
		}
        
        
    }
    
	@NoTransaction
	public static void getSellers(int pn, int ps, String searchText, String staffName) {
		CPStaff staff = getStaff();
		if(staff == null) {
			log.error("不存在登陆用户，请先登陆");
			redirect("/CPEctocyst/CPlogin");
		}
		PageOffset po = new PageOffset(pn, ps, 10);
		
		// 这是外包老板账号
		if(staff.isAdmin()) {
			if(!staff.isSuperAdmin()) {
				staff = CPStaff.findByName(staff.getParentName());
			}
			List<SellerToStaff> sellers = SellerToStaff.findByChiefId(po, staff.getId(), searchText, staffName);
			List<SellerInfo> sellerInfos = CPEctocystAction.genSellerInfoList(sellers);
	
			int count = (int) SellerToStaff.countByChiefId(staff.getId(), searchText, staffName);
			renderJSON(JsonUtil.getJson(new TMResult(sellerInfos, count, po)));
		} 
		// 这是外包客服
		else {
			List<SellerToStaff> sellers = SellerToStaff.findBySubStaffId(po,
					staff.getId(), searchText);
			List<SellerInfo> sellerInfos = CPEctocystAction
					.genSellerInfoList(sellers);
			int count = (int) SellerToStaff.countBySubStaffId(staff.getId(),
					searchText);
			renderJSON(JsonUtil.getJson(new TMResult(sellerInfos, count, po)));
		}
	}
    
    protected static void renderFailedJson(String message) {
        renderJSON(JsonUtil.getJson(new BusUIResult(false, message)));
    }

    protected static void renderSuccessJson(String message) {
        renderJSON(JsonUtil.getJson(new BusUIResult(true, message)));
    }
    
    public static void name(String name) {
    	if (StringUtils.isBlank(name)) {
            notFound();
        }

        User user = UserDao.findByUserNick(name.trim());
        if (user == null) {
            notFound();
        }
        TMController.clearUser();
        TMController.putUser(user);
        SkinDefender.CPEctocystIndex();
    }
    
	public static void queryStatus(String startTime, String endTime, String staffName) {
		final List<CommentStatus> res = new ArrayList<CommentStatus>();
		CPStaff staff = getStaff();
		if(staff == null) {
			redirect("/CPEctocyst/CPlogin");
		}
		Long staffId = staff.getId();
		
		if(staff.isAdmin()) {
			if(!staff.isSuperAdmin()) {
				staff = CPStaff.findByName(staff.getParentName());
			}
			CPStaff existStaff = CPStaff.find("parentName = ? and name = ?", staff.getName(), staffName).first();
			if(existStaff == null) {
				log.error("该客服不存在！chiefId:" + staff.getId() + "subStaffName:" + staffName);
				return;
			}
			staffId = existStaff.getId();
		}
		
		Long startTs = null;
		Long endTs = null;

		Date startDate = null;
		Date endDate = null;
		try {
			startDate = dateSDF.parse(startTime.trim());
			endDate = dateSDF.parse(endTime.trim());

			if (!StringUtils.isEmpty(startTime)) {
				startTs = startDate.getTime();
			}
			if (!StringUtils.isEmpty(endTime)) {
				endTs = endDate.getTime();
				// 要加上一天的时间
				endTs += 24L * 3600L * 1000L;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		HashMap<String, CommentStatus> dailyComments = new HashMap<String, CommentStatus>();
		for (Date tmp = startDate; !tmp.after(endDate); tmp.setDate(tmp.getDate() + 1)) {
			String tmpDate = dateSDF.format(tmp);
			dailyComments.put(tmpDate, new CommentStatus(tmpDate));
		}
		
		List<TradeRatePlay> tradeRateList = new ArrayList<TradeRatePlay>();
		
		List<SellerToStaff> sellers = SellerToStaff.findBySubStaffId(staffId);
		for (SellerToStaff seller : sellers) {
			List<TradeRatePlay> tradeRate = TradeRatePlayDao.findByUserBadComment(seller.getUserId(), startTs, endTs);
			if(!CommonUtils.isEmpty(tradeRate)) {
				tradeRateList.addAll(tradeRate);
			}
		}
		
		HashSet<String> totalModifyUser = new HashSet<String>();
		HashSet<String> totalModifyNeutralUser = new HashSet<String>();
		HashSet<String> totalModifyBadUser = new HashSet<String>();
		if (!CommonUtils.isEmpty(tradeRateList)) {
			for (TradeRatePlay tradeRate : tradeRateList) {
				String created = dateSDF.format(new Date(tradeRate.getCreated()));
				String updated = dateSDF.format(new Date(tradeRate.getUpdated()));
				if (tradeRate.getRate() <= 3) {
					CommentStatus comment = dailyComments.get(created);
					if (comment != null) {
						int rate = tradeRate.getRate() & 3;
						if (rate == 2) {
							// 中评
							comment.count += 1;
							comment.neutralCount += 1;
						} else if (rate == 3) {
							// 差评
							comment.count += 1;
							comment.badCount += 1;
						}
					}
				} else if (tradeRate.getRate() > 3) {
					int rate = tradeRate.getRate() & 3;
					int pastRate = ((tradeRate.getRate() >> 2) & 3);
					CommentStatus comment = dailyComments.get(created);
					if (comment != null) {
						if (pastRate == 2) {
							// 中评
							comment.count += 1;
							comment.neutralCount += 1;
						} else if (pastRate == 3) {
							// 差评
							comment.count += 1;
							comment.badCount += 1;
						}
					}

					comment = dailyComments.get(updated);
					if (comment != null) {
						if (rate <= 1 && pastRate == 2) {
							// 中评改好评
							comment.modifyCount += 1;
							comment.modifyNeutralCount += 1;

							comment.modifyUser.add(tradeRate.getNick());
							comment.modifyNeutralUser.add(tradeRate.getNick());
							
							totalModifyUser.add(tradeRate.getNick());
							totalModifyNeutralUser.add(tradeRate.getNick());
						} else if (rate <= 1 && pastRate == 3) {
							// 差评改好评
							comment.modifyCount += 1;
							comment.modifyBadCount += 1;

							comment.modifyUser.add(tradeRate.getNick());
							comment.modifyBadUser.add(tradeRate.getNick());

							totalModifyUser.add(tradeRate.getNick());
							totalModifyBadUser.add(tradeRate.getNick());
						}
					}
				}
			}
		}

		//统计当天未评价订单数 评价数
		List<OrderDisplay> orderDisplays = Lists.newArrayList();
		List<TradeRatePlay> tradeRatePlayList = Lists.newArrayList();
		List<TradeRatePlay> byUserGoodComment = Lists.newArrayList();
		List<TradeRatePlay> originalGoodComment = Lists.newArrayList();
		//将一个客服对应的多个商家里的记录合并
		for (SellerToStaff seller : sellers) {
			//未评价订单记录
			List<OrderDisplay> subOrderDisplays = OrderDisplayDao.searchWithArgs(seller.getUserId(), null, null, null, 5, false, null, startTs, endTs);
			if(!CommonUtils.isEmpty(subOrderDisplays)) {
				orderDisplays.addAll(subOrderDisplays);
			}
			//评价记录
			List<TradeRatePlay> subTradeRatePlayList = TradeRatePlayDao.findByUserIdDate(seller.getUserId(), startTs, endTs);
			if(!CommonUtils.isEmpty(subTradeRatePlayList)) {
				tradeRatePlayList.addAll(subTradeRatePlayList);
			}
			//好评记录
			List<TradeRatePlay> subByUserGoodComment = TradeRatePlayDao.findByUserGoodComment(seller.getUserId(), startTs, endTs);
			if(!CommonUtils.isEmpty(subByUserGoodComment)) {
				byUserGoodComment.addAll(subByUserGoodComment);
			}
			//催评好评记录
			List<TradeRatePlay> subOriginalGoodComment = TradeRatePlayDao.findByUserOriginalGoodComment(seller.getUserId(), startTs, endTs);
			if(!CommonUtils.isEmpty(subOriginalGoodComment)) {
				originalGoodComment.addAll(subOriginalGoodComment);
			}

		}
		for (OrderDisplay orderDisplay : orderDisplays) {
			String created = dateSDF.format(new Date(orderDisplay.getCreated()));//未评价订单创建时间
			CommentStatus comment = dailyComments.get(created);
			if (comment != null) {
				comment.noCommentCount++;
			}
		}
		for (TradeRatePlay tradeRatePlay : tradeRatePlayList) {
			String created = dateSDF.format(new Date(tradeRatePlay.getCreated()));//订单评价时间
			CommentStatus comment = dailyComments.get(created);
			if (comment != null) {
				comment.haveCommentCount++;
			}
		}

		//统计当天好评数
		for (TradeRatePlay tradeRatePlay : byUserGoodComment) {
			long created = tradeRatePlay.getCreated();
			long updated = tradeRatePlay.getUpdated();
			long max = Math.max(created, updated);
			String date = dateSDF.format(new Date(max));//订单评价时间
			CommentStatus comment = dailyComments.get(date);
			if (comment != null) {
				comment.goodCommentCount++;
			}
		}
		//统计催评得到的好评
		for (TradeRatePlay tradeRatePlay : originalGoodComment) {
			long created = tradeRatePlay.getCreated();
			String date = dateSDF.format(new Date(created));//评价创建时间
			CommentStatus comment = dailyComments.get(date);
			if (comment != null) {
				OrderPlay orderPlay = OrderPlay.findByOid(tradeRatePlay.getUserId(), tradeRatePlay.getOid());
				if (orderPlay != null && StringUtils.isNotEmpty(orderPlay.getRemark()))
					comment.urgeGoodCommentCount ++;
			}
		}



		String totalKey = "总 计";
		final CommentStatus totalComment = new CommentStatus(totalKey);
		new MapIterator<String, CommentStatus>(dailyComments) {
			@Override
			public void execute(Entry<String, CommentStatus> entry) {
				CommentStatus comment = entry.getValue();
				comment.modifyUserCount = comment.modifyUser.size();
				comment.modifyNeutralUserCount = comment.modifyNeutralUser.size();
				comment.modifyBadUserCount = comment.modifyBadUser.size();
				res.add(comment);

				totalComment.count += comment.count;
				totalComment.neutralCount += comment.neutralCount;
				totalComment.badCount += comment.badCount;

				totalComment.modifyCount += comment.modifyCount;
				totalComment.modifyNeutralCount += comment.modifyNeutralCount;
				totalComment.modifyBadCount += comment.modifyBadCount;

				totalComment.noCommentCount += comment.noCommentCount;
				totalComment.haveCommentCount += comment.haveCommentCount;
				totalComment.goodCommentCount += comment.goodCommentCount;
				totalComment.urgeGoodCommentCount += comment.urgeGoodCommentCount;
			}
		}.call();

		totalComment.modifyUserCount = totalModifyUser.size();
		totalComment.modifyNeutralUserCount = totalModifyNeutralUser.size();
		totalComment.modifyBadUserCount = totalModifyBadUser.size();

		if (dailyComments.size() > 1) {
			res.add(totalComment);
		}

		Collections.sort(res, new Comparator<CommentStatus>() {
			@Override
			public int compare(CommentStatus o1, CommentStatus o2) {
				return o2.date.compareTo(o1.date);
			}
		});

		renderJSON(JsonUtil.getJson(res));
	}
	
	/**
	 * 测试用
	 * 2017-09-05
	 */
	public static void test(String staffName, String date) {
		if(StringUtils.isEmpty(staffName)) {
			renderFailedJson("请输入员工姓名。");
		}
		if(StringUtils.isEmpty(date)) {
			renderFailedJson("请输入时间。例：2017-09-04");
		}
		
		CPStaff existStaff = CPStaff.find("name = ?", staffName).first();
		if(existStaff == null) {
			renderFailedJson("该客服不存在！");
		}
		Long staffId = existStaff.getId();
		
		Long startTime = 0L;
		Long endTime = 0L;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			java.util.Date a = sdf.parse(date);
			startTime = a.getTime();
			endTime = startTime + DateUtil.DAY_MILLIS;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		List<TradeRatePlay> tradeRateList = new ArrayList<TradeRatePlay>();

		List<SellerToStaff> sellers = SellerToStaff.findBySubStaffId(staffId);
		for (SellerToStaff seller : sellers) {
			List<TradeRatePlay> tradeRate = TradeRatePlayDao.findByUserBadComment(seller.getUserId(), startTime, endTime);
			if(!CommonUtils.isEmpty(tradeRate)) {
				tradeRateList.addAll(tradeRate);
			}
		}
		
		List<TradeRatePlay> result = new ArrayList<TradeRatePlay>();
		
		for (TradeRatePlay tradeRate : tradeRateList) {
			if (tradeRate.getRate() > 3) {
				int rate = tradeRate.getRate() & 3;
				int pastRate = ((tradeRate.getRate() >> 2) & 3);
				if (rate <= 1 && pastRate == 2) {
					// 中评改好评
					tradeRate.setItemPrice("中评改好评");
					result.add(tradeRate);
				} else if (rate <= 1 && pastRate == 3) {
					// 差评改好评
					tradeRate.setItemPrice("差评改好评");
					result.add(tradeRate);
				}
			}
		}
		
		renderJSON(JsonUtil.getJson(result));
	}
}
