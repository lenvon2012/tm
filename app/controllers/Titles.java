package controllers;

import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import job.writter.OpLogWritter;
import job.writter.TitleLogWritter;
import models.HotTitle;
import models.UserDiag;
import models.autolist.AutoListConfig;
import models.autolist.AutoListLog;
import models.autolist.AutoListRecord;
import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.comment.CommentConf;
import models.comment.Comments;
import models.comment.CommentsFailed;
import models.comment.UserTradeCommentLog;
import models.defense.BlackListBuyer;
import models.defense.BlackListExplain;
import models.defense.DefenderOption;
import models.defense.DefenseLog;
import models.defense.DefenseWarn;
import models.defense.ItemBuyLimit;
import models.defense.ItemPass;
import models.defense.WhiteListBuyer;
import models.industry.TopShopItem;
import models.item.ItemPlay;
import models.item.ItemTitleBackup;
import models.oplog.OpLog;
import models.oplog.OpLog.LogType;
import models.oplog.TitleOpRecord;
import models.oplog.TitleOptimiseLog;
import models.popularized.Popularized;
import models.shop.ShopCatPlay;
import models.user.TitleOptimised;
import models.user.User;
import onlinefix.UpdateAllItemsJob;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.common.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import pojo.webpage.top.TMWordBase;
import result.TMPaginger;
import result.TMResult;
import sug.api.QuerySugAPI;
import titleDiag.DiagResult;
import utils.ClouddateUtil;
import actions.DiagAction;
import actions.DiagAction.BatchReplacer;
import actions.DiagAction.BatchResultMsg;
import actions.WordsAction;
import actions.CWordAction.CWordAction;
import actions.task.AutoTitleTaskAction;
import actions.task.AutoTitleTaskAction.UserTaskLog;
import autotitle.AutoSplit;
import autotitle.AutoTitleAction;
import autotitle.AutoTitleOption.BatchPageOption;
import autotitle.AutoWordBase;
import bustbapi.BusAPI;
import bustbapi.FenxiaoApi.FXScItemApi;
import bustbapi.FenxiaoApi.FengxiaoRecommender;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemTitleUpdater;
import bustbapi.TMApi;
import bustbapi.TmallItem.TmallItemTitleUpdater;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.pojo.StringPair;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.MixHelpers;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.word.PaodingSpliter.SplitMode;
import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemImg;

import configs.TMConfigs;
import configs.TMConfigs.PageSize;
import controllers.BatchOp.BatchOpResult;
import dao.item.ItemDao;

/**
 * 
 * @ll: {@link Popularized} {@link TitleOpRecord} {@link OpLog} {@link BaiduFav}
 * 
 * @ywj dp: {@link AutoListConfig} {@link UserDiag} {@link AutoListLog}
 *      {@link AutoListRecord} {@link AutoListTime} {@link NoAutoListItem}
 * 
 * 
 @ll: {@link CommentConf} {@link Comments} {@link CommentsFailed}
 *      {@link UserTradeCommentLog}
 * 
 * 
 * 
 @zl {@link BlackListBuyer} {@link BlackListExplain} {@link DefenderOption}
 *     {@link DefenseLog} {@link DefenseWarn} {@link ItemBuyLimit}
 *     {@link ItemPass} {@link WhiteListBuyer}
 * @author zrb
 */
public class Titles extends TMController {

    public static final int TITLE_MODE_DEFAULT_RECOMMEND = 0;

    public static final int TITLE_MODE_OFFICIAL_ORIGIN = 1;

    public static final int TITLE_MODE_OFFICIAL_RECOMMEND = 2;

    public static final Logger log = LoggerFactory.getLogger(Titles.class);

    public static final String TAG = "Titles";

    public static void index() {
        render();
    }

    public static void list() {
        render();
    }

    public static void op(Long numIid) {
        render();
    }
    
	public static void testItemExtra(Long userId) {
		User user = User.findByUserId(userId);
		
		try {
			UpdateAllItemsJob.doForUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			renderText("error");
		}
		renderText("success");
	}
	
    public static void changeImgPosition(Long numIid, Long i, Long j) {
    	if(numIid == null || numIid <= 0L) {
    		renderText("请传入numIid");
    	}
    	if(i == null || i <= 0L) {
    		renderText("请传入正确的图片位置");
    	}
    	if(j == null || j <= 0L) {
    		renderText("请传入正确的图片位置");
    	}
    	if(i == j) {
    		renderText("同一张图片不需要调换");
    	}
    	i = i - 1;
    	j = j - 1;
    	User user = getUser();
    	Item item = new ItemApi.ItemImgsGet(user, numIid).call();
    	if(item == null) {
    		renderText("获取宝贝信息失败");
    	}
    	if(CommonUtils.isEmpty(item.getItemImgs())) {
    		renderText("获取宝贝图片信息失败");
    	}
    	List<ItemImg> imgs = item.getItemImgs();
		int count = imgs.size();
		if(i >= count || j >= count) {
			renderText("当前宝贝图片数量小于要调换的图片位置下标");
		}
		ItemImg img_ = new ItemImg();
		ItemImg img_i = new ItemImg();
		ItemImg img_j = new ItemImg();
		for(ItemImg img : imgs) {
			if(img.getPosition() == i) {
				img_i = img;
			} else if(img.getPosition() == j) {
				img_j = img;
			}
		}
		// 更新第i张图片
		img_.setId(img_j.getId());
		img_.setPosition(img_j.getPosition());
		img_.setUrl(img_i.getUrl());
		FileItem fItem_i = ItemApi.fetchUrl(img_.getUrl(), img_.getId() + "_" + img_.getPosition());
		if(fItem_i == null){
			renderText("存在无效图片，请检查");
		}
		log.info("[f item_i:]" + fItem_i.getFileName());
		ItemImg img_i_result = new ItemApi.ItemImgPictureUpdate(user, numIid, img_, fItem_i).call();
		if(img_i_result == null) {
			renderText("更新宝贝第" + i + "张图片时出错");
		}
		// 更新第j张图片
		img_.setId(img_i.getId());
		img_.setPosition(img_i.getPosition());
		img_.setUrl(img_j.getUrl());
		FileItem fItem_j = ItemApi.fetchUrl(img_.getUrl(), img_.getId() + "_" + img_.getPosition());
		if(fItem_j == null){
			renderText("存在无效图片，请检查");
		}
		log.info("[f item_j:]" + fItem_j.getFileName());
		ItemImg img_j_result = new ItemApi.ItemImgPictureUpdate(user, numIid, img_, fItem_j).call();
		if(img_j_result == null) {
			renderText("更新宝贝第" + j + "张图片时出错");
		}
		renderText("操作成功");
    }

    public static void titleOp(Long numIid, Long pn, Long offset, String s, int start, int end, int sort, int status,
            Long catId, int optimised) {
        if (numIid == null || pn == null || offset == null || offset < 0)
            redirect("/home/commoditydiag");
        if (s == null)
            s = "";
        if (!(status == 0 || status == 1 || status == 2)) {
            status = 2;
        }
        if (optimised != 1 && optimised != 2 && optimised != 4) {
            optimised = 1;
        }
        render(numIid, pn, offset, s, start, end, sort, status, catId, optimised);
        // render();
    }

    public static void modify(String title) {

    }

    public static void score(Long numIid) {

    }

    public static void recommends(Long numIid) {

    }

    /**
     * 获取宝贝质量得分分布
     * 
     * @throws IOException
     */
    public static void scoreSread() throws IOException {
        renderMockFileInJsonIfDev("scoreSread.json");

        User user = getUser();
        Map<Integer, Integer> res = ItemDao.findItemScoreSpread(user.getId(), user.getUserNick());

        // 分配区间，res里面是得分
        Map<Integer, Integer> sreadMap = new HashMap<Integer, Integer>();

        for (Map.Entry<Integer, Integer> entry : res.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            Integer sreadKey = 0;
            if (key >= 85) {// 85以上是优秀
                sreadKey = 5;
            } else if (key >= 70) {// 70以上是良好
                sreadKey = 4;
            } else if (key >= 60) {
                sreadKey = 3;
            } else {
                sreadKey = 12;
            }
            Integer sreadValue = sreadMap.get(sreadKey);
            if (sreadValue == null)
                sreadValue = 0;
            sreadValue += value;
            sreadMap.put(sreadKey, sreadValue);
        }
        renderJSON(JsonUtil.getJson(sreadMap));
    }

    /*
     * public static void getBusinessCategories() {
     * 
     * User user = getUser();
     * 
     * List<ItemDao.Categories> categories = ItemDao
     * .getBusinessCategories(user);
     * 
     * renderJSON(JsonUtil.getJson(categories));
     * 
     * }
     */
    /*
     * 首页右边的信息显示 综合得分scoreMul
     */
    public static void scoreMul() {
        User user = getUser();
        long userCid = user.getCid();
        try {
            // ShopCatPlay shopCatPlay = ShopCatPlay.findById(userCid);
            ShopCatPlay shopCatPlay = ShopCatPlay.findByUserCid(userCid);
            // res里面是得分
            // Map<Integer, Integer> res =
            // ItemDao.findItemScoreSpread(user.getId(), user.getUserNick());
            Map<Integer, Integer> res = getTitleScoreMap(user);
            Map<Integer, Integer> sreadMap = new HashMap<Integer, Integer>();
            Long count = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();

            DiagAction.computeTotalScore(res, sreadMap);
            Object[] objects = { user, shopCatPlay, sreadMap, count == null ? 0L : count, res };
            renderJSON(objects);
        } catch (Exception e) {
            // TODO: handle exception
            log.warn(e.getMessage(), e);
            Object[] objects = {};
            renderJSON(objects);
        }

    }

    public static Map<Integer, Integer> getTitleScoreMap(final User user) {
        Map<Integer, Integer> scoreMap = new HashMap<Integer, Integer>();
        List<Long> ids = ItemDao.findNumIidsByTitle(user.getId(), "");
        if (CommonUtils.isEmpty(ids)) {
            return scoreMap;
        }
        List<Item> tbItems = ApiJdpAdapter.get(user).tryItemList(user, ids);
        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();
        for (final Item tbItem : tbItems) {
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    if (tbItem == null) {
                        return null;
                    }
                    return DiagAction.doDiag(user, tbItem, null, -1);
                }
            });
            tasks.add(task);
        }

        for (FutureTask<DiagResult> task : tasks) {
            DiagResult doWord;
            try {
                doWord = task.get();
                if (doWord == null) {
                    continue;
                }
                int score = doWord.getScore();
                if (score <= 0) {
                    continue;
                }
                if (scoreMap.get(score) == null) {
                    scoreMap.put(score, 1);
                } else {
                    scoreMap.put(score, scoreMap.get(score) + 1);
                }

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return scoreMap;
    }

    // @CacheFor(value = "1min")
    static String PARAM_LAST_QUERY = "_last_q";

    @CacheFor("1h")
    public static void listDiag(long userId, String s, int pn, int ps, int lowBegin, int topEnd, int sort, int status)
            throws IOException {

        // renderMockFileInJsonIfDev("diaglist.json");
        if ("输入您的关键字".equals(s)) {
            s = null;
        }

        final User user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        session.put(PARAM_LAST_QUERY, s);

        log.info(format("listDiag:userId, s, pn, ps, lowBegin, topEnd, sort".replaceAll(", ", "=%s, ") + "=%s", userId,
                s, pn, ps, lowBegin, topEnd, sort));

        List<ItemPlay> list = ItemDao.findOnlineByUserWithscore(user.getId(), (pn - 1) * ps, ps, s, lowBegin, topEnd,
                sort, status);

        // List<ItemPlay> list = ItemDao.findOnlineByUser(user.getId(), (pn - 1)
        // * ps, ps, s, sort);

        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }

        List<DiagResult> res = new ArrayList<DiagResult>();
        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();

        // try {
        List<Long> ids = ItemDao.toIdsList(list);
        List<Item> tbItems = new ItemApi.MultiItemsListGet(user.getSessionKey(), ids).call();
        for (final Item item : tbItems) {
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    return DiagAction.doDiag(user, item, null, -1);
                }
            });
            tasks.add(task);
        }

        for (FutureTask<DiagResult> task : tasks) {
            DiagResult doWord;
            try {
                doWord = task.get();
                if (doWord.getScore() >= lowBegin && doWord.getScore() <= topEnd) {
                    res.add(doWord);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        TMPaginger tm = new TMPaginger(pn, ps, (int) ItemDao.countOnlineByUserWithScore(user.getId(), lowBegin, topEnd,
                s, status), res);
        renderJSON(JsonUtil.getJson(tm));

        // } catch (ClientException e) {
        // log.warn(e.getMessage(), e);
        // TMPaginger.makeEmptyFail("亲，服务器出了点小问题，请稍后再试哦");
        // }
    }

    public static void listDiagTMpage(long userId, String s, int pn, int ps, int lowBegin, int topEnd, int sort,
            int status, String catId) throws IOException {

        // renderMockFileInJsonIfDev("diaglist.json");
        if ("输入您的关键字".equals(s)) {
            s = null;
        }

        final User user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        session.put(PARAM_LAST_QUERY, s);

        log.info(format("listDiag:userId, s, pn, ps, lowBegin, topEnd, sort".replaceAll(", ", "=%s, ") + "=%s", userId,
                s, pn, ps, lowBegin, topEnd, sort));

        List<ItemPlay> list = ItemDao.findOnlineByUserWithscoreAndCatid(user.getId(), (pn - 1) * ps, ps, s, 0L, lowBegin,
                topEnd, sort, status, catId, StringUtils.EMPTY);

        // List<ItemPlay> list = ItemDao.findOnlineByUser(user.getId(), (pn - 1)
        // * ps, ps, s, sort);

        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }

        List<DiagResult> res = new ArrayList<DiagResult>();
        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();

        List<Long> ids = ItemDao.toIdsList(list);
        Map<Long, Boolean> optimisedMap = ItemDao.getOptimisedMap(list);
        List<Item> tbItems = ApiJdpAdapter.get(user).tryItemList(user, ids);

        // try {
        for (final Item tbItem : tbItems) {
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    return DiagAction.doDiag(user, tbItem, null, -1);
                }
            });
            tasks.add(task);
        }

        for (FutureTask<DiagResult> task : tasks) {
            DiagResult doWord;
            try {
                doWord = task.get();
                if (doWord.getScore() >= lowBegin && doWord.getScore() <= topEnd) {
                    Boolean isOptimised = optimisedMap.get(doWord.getNumIid());
                    if (isOptimised == null) {
                        isOptimised = false;
                    }
                    doWord.setOptimised(isOptimised);
                    res.add(doWord);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        // TMPaginger tm = new TMPaginger(pn, ps, (int)
        // ItemDao.countOnlineByUserWithScore(user.getId(), lowBegin,
        // topEnd,
        // s, status), res);
        // renderJSON(JsonUtil.getJson(tm));

        PageOffset po = new PageOffset(pn, ps, 5);
        TMResult tmRes = new TMResult(res, (int) ItemDao.countOnlineByUserWithArgs(user.getId(), lowBegin, topEnd, s, 0L,
                status, catId, null), po);
        renderJSON(JsonUtil.getJson(tmRes));

        // } catch (ClientException e) {
        // log.warn(e.getMessage(), e);
        // TMPaginger.makeEmptyFail("亲，服务器出了点小问题，请稍后再试哦");
        // }
    }
    
	public static void listDiagTMpageWithOptimisedBefore(long userId, String s, Long numIid, int pn, int ps, int lowBegin, int topEnd,
			int sort, int status, String catId, Long taobaoCatId, int optimised) throws IOException, ClientException {

		if ("输入您的关键字".equals(s)) {
			s = null;
		}
		if (taobaoCatId == null) {
			taobaoCatId = -1L;
		}
		final User user = getUser();
		pn = pn < 1 ? 1 : pn;
		ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
		session.put(PARAM_LAST_QUERY, s);

		log.info(format("listDiag:userId, s, pn, ps, lowBegin, topEnd, sort".replaceAll(", ", "=%s, ") + "=%s", userId,
				s, pn, ps, lowBegin, topEnd, sort));

		if (optimised != TitleOptimised.Status.NORMAL && optimised != TitleOptimised.Status.OPTIMISED
				&& optimised != TitleOptimised.Status.UN_OPTIMISED) {
			optimised = TitleOptimised.Status.NORMAL;
		}
		Set<Long> optimisedIds = TitleOptimised.findNumIidsByUserId(user.getId());
		
		int count = (int) ItemDao.countOnlineByUserWithArgsAndOptimise(user.getId(), lowBegin,
				topEnd, s, numIid, status, catId, taobaoCatId, optimised);
		List<ItemPlay> list = ItemDao.findOnlineByUserWithscoreAndCatidOptimised(user.getId(), (pn - 1) * ps, ps, s, numIid,
				lowBegin, topEnd, sort, status, catId, taobaoCatId, optimised);

		if (CommonUtils.isEmpty(list)) {
			TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
		}
		
		final Map<Long, Integer> tradeCountMap = new HashMap<Long, Integer>();
		for (ItemPlay item : list) {
			if (item == null) {
				continue;
			}
			if (optimisedIds.contains(item.getNumIid())) {
				item.setOptimised(true);
			} else {
				item.setOptimised(false);
			}
			tradeCountMap.put(item.getNumIid(), item.getTradeItemNum());
		}

		List<DiagResult> res = new ArrayList<DiagResult>();

		List<Long> ids = ItemDao.toIdsList(list);
		Map<Long, Boolean> optimisedMap = ItemDao.getOptimisedMap(list);
		Map<Long, Long> createdMap = ItemDao.getCreatedMap(list);
		List<Item> tbItems = ApiJdpAdapter.get(user).tryItemList(user, ids);
		
		for (Item item : tbItems) {
			double price = NumberUtil.parserDouble(item.getPrice(), 0.0d);
			long delistTime = item.getDelistTime() == null ? 0L : item.getDelistTime().getTime();
			
			DiagResult doWord = new DiagResult(item.getNumIid(), price, item.getTitle(), item.getPicUrl(), tradeCountMap.get(item.getNumIid()), delistTime);
			Boolean isOptimised = optimisedMap.get(doWord.getNumIid());
			if (isOptimised == null) {
				isOptimised = false;
			}
			doWord.setOptimised(isOptimised);
			doWord.setCreated(createdMap.get(doWord.getNumIid()));
			res.add(doWord);
		}

		if(sort == 1) {
			Collections.sort(res, ItemScoreComparator);
		} else {
			Collections.sort(res, ItemCreatedComparator);
		}
		PageOffset po = new PageOffset(pn, ps, 5);

		TMResult tmRes = new TMResult(res, count, po);
		renderJSON(JsonUtil.getJson(tmRes));
	}

    public static void listDiagTMpageWithOptimised(long userId, String s, Long numIid, int pn, int ps, int lowBegin, int topEnd,
            int sort, int status, String catId, Long taobaoCatId, int optimised) throws IOException {

        // renderMockFileInJsonIfDev("diaglist.json");
        if ("输入您的关键字".equals(s)) {
            s = null;
        }
        if (taobaoCatId == null) {
            taobaoCatId = -1L;
        }
        final User user = getUser();
        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        session.put(PARAM_LAST_QUERY, s);

        log.info(format("listDiag:userId, s, pn, ps, lowBegin, topEnd, sort".replaceAll(", ", "=%s, ") + "=%s", userId,
                s, pn, ps, lowBegin, topEnd, sort));

        if (optimised != TitleOptimised.Status.NORMAL && optimised != TitleOptimised.Status.OPTIMISED
                && optimised != TitleOptimised.Status.UN_OPTIMISED) {
            optimised = TitleOptimised.Status.NORMAL;
        }
        Set<Long> optimisedIds = TitleOptimised.findNumIidsByUserId(user.getId());
        
        int count = (int) ItemDao.countOnlineByUserWithArgsAndOptimise(user.getId(), lowBegin,
                topEnd, s, numIid, status, catId, taobaoCatId, optimised);
        List<ItemPlay> list = ItemDao.findOnlineByUserWithscoreAndCatidOptimised(user.getId(), (pn - 1) * ps, ps, s, numIid,
                lowBegin, topEnd, sort, status, catId, taobaoCatId, optimised);

        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }
        final Map<Long, Integer> tradeCountMap = new HashMap<Long, Integer>();
        for (ItemPlay item : list) {
            if (item == null) {
                continue;
            }
            if (optimisedIds.contains(item.getNumIid())) {
                item.setOptimised(true);
            } else {
                item.setOptimised(false);
            }
            tradeCountMap.put(item.getNumIid(), item.getTradeItemNum());
        }

        List<DiagResult> res = new ArrayList<DiagResult>();
        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();

        List<Long> ids = ItemDao.toIdsList(list);
        Map<Long, Boolean> optimisedMap = ItemDao.getOptimisedMap(list);
        Map<Long, Long> createdMap = ItemDao.getCreatedMap(list);
        List<Item> tbItems = ApiJdpAdapter.get(user).tryItemList(user, ids);

        for (final Item tbItem : tbItems) {
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    Integer tradeCount = tradeCountMap.get(tbItem.getNumIid());
                    if (tradeCount == null) {
                        tradeCount = -1;
                    }
                    return DiagAction.doDiag(user, tbItem, null, tradeCount);
                }
            });
            tasks.add(task);
        }

        for (FutureTask<DiagResult> task : tasks) {
            DiagResult doWord;
            try {
                doWord = task.get();
                if (doWord.getScore() >= lowBegin && doWord.getScore() <= topEnd) {
                    Boolean isOptimised = optimisedMap.get(doWord.getNumIid());
                    if (isOptimised == null) {
                        isOptimised = false;
                    }
                    doWord.setOptimised(isOptimised);
                    doWord.setCreated(createdMap.get(doWord.getNumIid()));
                    res.add(doWord);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        if(sort == 1) {
        	Collections.sort(res, ItemScoreComparator);
        } else {
        	Collections.sort(res, ItemCreatedComparator);
		}
        PageOffset po = new PageOffset(pn, ps, 5);

        TMResult tmRes = new TMResult(res, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static Comparator<DiagResult> ItemScoreComparator = new Comparator<DiagResult>() {

        @Override
        public int compare(DiagResult o1, DiagResult o2) {
            int score1 = o1.getScore();
            int score2 = o2.getScore();

            if (score2 <= score1) {
                return 1;
            } else {
                return -1;
            }

        }
    };

    public static Comparator<DiagResult> ItemScoreDescComparator = new Comparator<DiagResult>() {

        @Override
        public int compare(DiagResult o1, DiagResult o2) {
            int score1 = o1.getScore();
            int score2 = o2.getScore();

            if (score1 <= score2) {
                return 1;
            } else {
                return -1;
            }

        }
    };
    
    public static Comparator<DiagResult> ItemCreatedComparator = new Comparator<DiagResult>() {

        @Override
        public int compare(DiagResult o1, DiagResult o2) {
            Long created1 = o1.getCreated();
            Long created2 = o2.getCreated();

            if (created1 <= created2) {
                return 1;
            } else {
                return -1;
            }

        }
    };

    /**
     * Actually, no diag needed....
     * 
     * @param s
     * @param pn
     * @param ps
     * @param lowBegin
     * @param topEnd
     * @param sort
     * @param status
     * @param catId
     *            --> sellerCid
     * @param cid
     *            --> 宝贝cid
     */
    public static void getItemsWithDiagResult(String s, int pn, int ps, final int lowBegin, final int topEnd, int sort,
            int status, Long catId, Long cid) {

        final User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 5);
        session.put(PARAM_LAST_QUERY, s);

        log.info(format(
                "getItemsWithDiagResult:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ",
                        "=%s, ") + "=%s", user.getId(), s, pn, ps, lowBegin, topEnd, sort, status, catId));

        List<ItemPlay> list = ItemDao.findOnlineByUserWithArgs(user.getId(), po.getOffset(), po.getPs(), s, lowBegin,
                topEnd, sort, status, catId == null ? "" : catId.toString(), cid, false);
        // TODO add recommend title to list

        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }

        TMResult<List<ItemPlay>> tmRes = new TMResult<List<ItemPlay>>(list, (int) ItemDao.countOnlineByUserWithArgs(
                user.getId(), lowBegin, topEnd, s, 0L, status, catId == null ? "" : catId.toString(), cid), po);

        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void getItemsWithDiagResultAndLstOptimise(String s, int pn, int ps, final int lowBegin,
            final int topEnd, int sort, int status, Long catId, Long cid) {

        final User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 5);
        session.put(PARAM_LAST_QUERY, s);

        log.info(format(
                "getItemsWithDiagResult:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ",
                        "=%s, ") + "=%s", user.getId(), s, pn, ps, lowBegin, topEnd, sort, status, catId));

        List<ItemPlay> list = ItemDao.findOnlineByUserWithArgs(user.getId(), po.getOffset(), po.getPs(), s, lowBegin,
                topEnd, sort, status, catId == null ? "" : catId.toString(), cid, false);
        // TODO add recommend title to list

        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }
        final Map<Long, Integer> tradeCountMap = new HashMap<Long, Integer>();
        for (ItemPlay itemPlay : list) {
            TitleOptimised optimised = TitleOptimised.findByUserId(user.getId(), itemPlay.getNumIid());
            if (optimised == null) {
                continue;
            }
            itemPlay.setLastOptimiseTs(optimised.getTs());
            tradeCountMap.put(itemPlay.getNumIid(), itemPlay.getTradeItemNum());
        }

        List<DiagResult> res = new ArrayList<DiagResult>();
        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();

        List<Long> ids = ItemDao.toIdsList(list);
        Map<Long, Boolean> optimisedMap = ItemDao.getOptimisedMap(list);
        List<Item> tbItems = ApiJdpAdapter.get(user).tryItemList(user, ids);

        // try {
        for (final Item tbItem : tbItems) {
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    Integer tradeCount = tradeCountMap.get(tbItem.getNumIid());
                    if (tradeCount == null) {
                        tradeCount = -1;
                    }
                    return DiagAction.doDiag(user, tbItem, null, tradeCount);
                }
            });
            tasks.add(task);
        }

        for (FutureTask<DiagResult> task : tasks) {
            DiagResult doWord;
            try {
                doWord = task.get();
                if (doWord.getScore() >= lowBegin && doWord.getScore() <= topEnd) {
                    Boolean isOptimised = optimisedMap.get(doWord.getNumIid());
                    if (isOptimised == null) {
                        isOptimised = false;
                    }
                    doWord.setOptimised(isOptimised);
                    res.add(doWord);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        // TMPaginger tm = new TMPaginger(pn, ps, (int)
        // ItemDao.countOnlineByUserWithScore(user.getId(), lowBegin,
        // topEnd,
        // s, status), res);
        // renderJSON(JsonUtil.getJson(tm));
        if (sort == 1) {
            Collections.sort(res, ItemScoreComparator);
        } else {
            Collections.sort(res, ItemScoreDescComparator);
        }

        TMResult tmRes = new TMResult(res, (int) ItemDao.countOnlineByUserWithArgs(user.getId(), lowBegin, topEnd, s, 0L,
                status, catId == null ? "" : catId.toString(), cid), po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static void getItemsBySellerCatId(int pn, int ps, Long catId) {
        final User user = getUser();
        String catIdStr = "";
        if (catId != null) {
            catIdStr = catId.toString();
        }

        List<ItemPlay> list = ItemDao.findOnlineByUserCatId(user.getId(), catIdStr, (pn - 1) * ps, ps);

        if (CommonUtils.isEmpty(list)) {
            TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
        }
        List<DiagResult> res = new ArrayList<DiagResult>();
        List<FutureTask<DiagResult>> tasks = new ArrayList<FutureTask<DiagResult>>();

        // try {
        for (final ItemPlay itemPlay : list) {
            FutureTask<DiagResult> task = TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    return DiagAction.doDiag(user, itemPlay, null);
                }
            });
            tasks.add(task);
        }

        for (FutureTask<DiagResult> task : tasks) {
            DiagResult doWord = null;
            try {
                doWord = task.get();
                if (doWord != null) {
                    res.add(doWord);
                }

            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            } catch (ExecutionException e) {
                log.warn(e.getMessage(), e);
            }

        }

        TMPaginger tm = new TMPaginger(pn, ps, (int) ItemDao.countOnlineByUserCatId(user.getId(), catIdStr), res);
        renderJSON(JsonUtil.getJson(tm));
    }

    /**
     * 
     * @param toCancelNumIid
     *            宝贝id
     * 
     */
    public static void singleDiag() {
        long numIid = NumberUtil.parserLong(params.get("numIid"), 0L);
        String title = params.get("title");
        // int pn = NumberUtil.parserInt(params.get("pn"), 1);
        // int offset = NumberUtil.parserInt(params.get("offset"), 0);

        User user = getUser();
        // ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (numIid == 0) {
            renderText("宝贝id为空");
        }
        try {
            DiagResult res = DiagAction.doDiag(user, numIid, title);
            renderJSON(JsonUtil.getJson(res));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderText("服务器不正常");
    }

    public static void diagNumIid(long numIid) {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        List<DiagResult> res = new ArrayList<DiagResult>();
        try {

            // Item call = new ItemGet(user.getSessionKey(), item.getNumIid(),
            // true).call();
            // DiagResult res = TitleDiagnose.getInstance().doWord(numIid, 0.0d,
            // StringUtils.isEmpty(title) ? item.getTitle() : title, call ==
            // null ? null : call.getPropsName(),
            // item.getPicURL());
            if (item == null) {

                // log.error(format(
                // "找不到 对应的numIid, 是不是走错了。。singleDiag:numIid, title, pn, offset".replaceAll(", ",
                // "=%s, ") + "=%s",
                // numIid, title, pn, offset));
                ok();
            }
            res.add(DiagAction.doDiag(user, item, item.title));
            TMPaginger tm = new TMPaginger(1, 10, 1, res);
            renderJSON(JsonUtil.getJson(tm));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        ok();
    }

    // private static final String[] promoteWordsArr = new String[] {
    // "送", "促销", "清仓", "秒杀", "特价", "正品", "疯抢", "试用", "代购", "行货", "质保", "窜货",
    // "甩卖", "正版", "质检", "折扣", "免费", "省钱",
    // "现货", "限量", "暴亏", "联保", "超值", "绝杀", "秒冲", "新款", "新品", "冲钻", "优惠", "热卖",
    // "批发", "原装", "全新", "打折", "降价", "返现",
    // "爆款", "国庆", "双节", "特价", "限时", "亏本", "满就送", "双十一", "跳楼价", "送积分", "成本价",
    // "低价", "送红包", "优惠劵", "优惠价", "跳楼处理",
    // "月销千款", "专柜验货", "质量保证", "厂家直销", "收藏有礼", "如假包换", "假一赔十", "如假包换", "新款上市",
    // "专柜正品", "货到付款", "如假包换", "货到付款",
    // "无条件退换"
    // };

    public static void getPromoteWords() {
        renderJSON(JsonUtil.getJson(DiagResult.promoteWordsArr));
    }

    public static void rename(long numIid, String title) {

        log.info(format("rename:numIid, title".replaceAll(", ", "=%s, ") + "=%s", numIid, title));
        User user = getUser();
        title = StringUtils.trim(title);
        while (ChsCharsUtil.length(title) > 60) {
            title = title.substring(0, title.length() - 1);
        }

        Item item = ApiJdpAdapter.tryFetchSingleItem(user, numIid);
        if (item == null) {
            renderUISuccess();
        }

        StringBuilder sb = new StringBuilder("修改标题,原标题:[");
        sb.append(item.getTitle());
        sb.append("],新标题:[");
        sb.append(title);
        sb.append("]");

        if (user.isTmall()) {
            TmallItemTitleUpdater updater = new TmallItemTitleUpdater(user.getSessionKey(), numIid, title);
            updater.call();
            if (!StringUtils.isEmpty(updater.errorMsg)) {
                sb.append(",修改失败,原因:" + updater.errorMsg);
                OpLogWritter.addMsg(user.getId(), sb.toString(), numIid, LogType.moditytitle, false);
                log.error(updater.errorMsg);
                renderUIErrorMessage(updater.errorMsg);
            }
        } else {
            ItemTitleUpdater updater = new ItemTitleUpdater(user.getSessionKey(), numIid, title);
            updater.call();
            if (!StringUtils.isEmpty(updater.errorMsg)) {
                sb.append(",修改失败,原因:" + updater.errorMsg);
                OpLogWritter.addMsg(user.getId(), sb.toString(), numIid, LogType.moditytitle, false);
                log.error(updater.errorMsg);
                renderUIErrorMessage(updater.errorMsg);
            }
        }
        CommonUtils.sleepQuietly(500L);
        item = ApiJdpAdapter.tryFetchSingleItem(user, numIid);

        if (item == null) {
            renderUISuccess();
        }

        item.setTitle(title);
        ItemPlay exist = ItemDao.findByNumIid(user.getId(), numIid);
        if (exist == null) {
            renderUISuccess();
        }

        sb.append(",修改成功");
        OpLogWritter.addMsg(user.getId(), sb.toString(), numIid, LogType.moditytitle, true);
        TitleLogWritter.addMsg(user.getId(), numIid, exist.getTitle(), item.getTitle());

        exist.updateWithTitleAndScore(item, title);

        renderUISuccess();
    }

    public static void props(long numIid) {

        log.info(format("props:numIid".replaceAll(", ", "=%s, ") + "=%s", numIid));

        User user = getUser();
        List<StringPair> list = ItemApi.getProps(user, numIid);
        String content = JsonUtil.getJson(list);
        log.info("[back content:]" + content);
        renderJSON(content);
    }

    public static void nearby(String s, int offset, int sort, int low, final int lowBegin, final int topEnd,
            int status, int isCatSearch) {
        User user = getUser();
        ItemPlay first = null;
        Map<String, Long> res = new HashMap<String, Long>();
        s = StringUtils.isBlank(s) ? session.get(PARAM_LAST_QUERY) : s;
        List<ItemPlay> list = ItemDao.findOnlineByUserWithscore(user.getId(), offset + 1, 1, s, lowBegin, topEnd, sort,
                status);
        first = NumberUtil.first(list);
        res.put("before", first == null ? 0L : first.getNumIid());

        if (offset <= 0) {
            res.put("after", 0L);
        } else {
            list = ItemDao.findOnlineByUserWithscore(user.getId(), offset + 1, 1, s, lowBegin, topEnd, sort, status);
            first = NumberUtil.first(list);
            res.put("after", first == null ? 0L : first.getNumIid());
        }

        renderJSON(JsonUtil.getJson(res));
    }

    public static void nearbyCatSearch(int offset, Long catId) {
        User user = getUser();
        ItemPlay first = null;
        Map<String, Long> res = new HashMap<String, Long>();
        List<ItemPlay> list = ItemDao.findOnlineByUserCatId(user.getId(), catId.toString(), offset + 1, 1);
        first = NumberUtil.first(list);
        res.put("before", first == null ? 0L : first.getNumIid());

        if (offset <= 0) {
            res.put("after", 0L);
        } else {
            list = ItemDao.findOnlineByUserCatId(user.getId(), catId.toString(), offset + 1, 1);
            first = NumberUtil.first(list);
            res.put("after", first == null ? 0L : first.getNumIid());
        }

        renderJSON(JsonUtil.getJson(res));
    }

    public static void longTail(String s, long numIid) {
        s = Words.tryGetSearchKey(s, null, numIid);
        // List<String> result = QuerySugAPI.getQuerySugList(s, false);
        List<String> result = QuerySugAPI.getQuerySugListSimple(s);

        renderJSON(JsonUtil.getJson(result));
    }

    /**
     * if(window.jsonp1291)jsonp1291({"result": [["男装v领短袖t恤", "186412"],
     * ["男装v领长袖t恤", "10153670"], ["男装v领打底衫", "5678243"], ["男装v领薄款羊毛衫", "5628"],
     * ["男装v领t恤短袖 新款", "91535"], ["男装v领针织衫", "12530540"], ["男装v领毛衣", "4575470"],
     * ["男装v领弹力男", "6706"], ["男装v领纯色短袖t恤", "26389"], ["男装v领拼接t桖", "1749"]], )
     * 
     * @param s
     * @param numIid
     * @param pn
     * @param ps
     * @throws IOException
     */
    public static void newLongTail(String s, long numIid, int pn, int ps) throws IOException {
        // renderMockFileInJsonIfDev("TMApi.TMWordBaseApi.json");
        s = Words.tryGetSearchKey(s, null, numIid);
        // List<String> result = QuerySugAPI.getQuerySugList(s, false);
        Map<String, Integer> result = QuerySugAPI.getQuerySugListWordCount(s);
        // log.info("[xx   ]" + result);
        if (CommonUtils.isEmpty(result)) {
            renderJSON(new ArrayList<TMWordBase>());
        }
        Map<String, WordBaseBean> map = MapUtils.EMPTY_MAP;
        try {
            map = BusAPI.wordPv(result.keySet());
            // log.info("[map --<" + map.values().size() + ">:]" + map);
        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        // PageOffset po = new PageOffset(pn, ps, 10);
        // List<WordBase> bases = WordBase.andSearch(result, po);
        // int count = WordBase.getOrCountOfNormal(result);

        List<WordBaseBean> beans = new ArrayList<WordBaseBean>(map.values());
        int size = beans.size();
        for (WordBaseBean wordBaseBean : beans) {
            wordBaseBean.setCompetition(result.get(wordBaseBean.getWord()));
        }

        // log.info("[back beans:]" + beans);

        renderJSON(JsonUtil.getJson(new TMResult<List<WordBaseBean>>(beans, size, new PageOffset(1, size))));

    }

    public static void estimateSearchWord(String title) {
        try {

            List<String> words = new AutoSplit(title, ListUtils.EMPTY_LIST, true).execute();
            List<String> res = WordsAction.genWords(words);
            renderJSON(res);

        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static void estimateSearchWordByNumIid(Long numIid) {

        try {
            /*
             * if (Play.mode.isDev()) { renderJSON(new
             * TMApi.TMWordEquelApi("男装,女装,袜子").execute()); }
             */

            if (numIid == null || numIid <= 0) {
                renderJSON(new ArrayList<String>());
            }
            User user = getUser();
            if (user == null) {
                renderJSON(new ArrayList<String>());
            }
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
            if (itemPlay == null) {
                renderJSON(new ArrayList<String>());
            }
            List<String> words = new AutoSplit(itemPlay.getTitle(), ListUtils.EMPTY_LIST, true).execute();
            List<String> res = WordsAction.genWords(words);
            renderJSON(new TMApi.TMWordEquelApi(words).execute());

        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static void estimateKeyword(String title) {
        try {
            List<String> words = new AutoSplit(title, ListUtils.EMPTY_LIST, true).execute();
            renderJSON(JsonUtil.getJson(words));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static void getCWords(String title, Long cid) {
        if (cid == null || cid < 0) {
            cid = 0L;
        }
        if (StringUtils.isEmpty(title)) {
            title = StringUtils.EMPTY;
        }
        List<String> words = CWordAction.getCWords(null, null, title, cid);
        List<String> parsedWords = parseCWords(words);
        renderJSON(JsonUtil.getJson(parsedWords));
    }

    public static List<String> parseCWords(List<String> words) {
        List<String> parsedWords = new ArrayList<String>();
        if (!CommonUtils.isEmpty(words)) {
            for (String word : words) {
                String[] subWords = word.split("/");
                if (subWords.length > 0) {
                    for (String subWord : subWords) {
                        parsedWords.add(subWord);
                    }
                } else {
                    parsedWords.add(word);
                }
            }
        }
        return parsedWords;
    }

    public static void hotTitles() {
        User user = getUser();
        if (user == null)
            renderJSON("[]");
        long cid = user.getCid();
        List<HotTitle> hotTitleList = HotTitle.findByCategory(cid);
        renderJSON(JsonUtil.getJson(hotTitleList));
    }

    /**
     * 说好的同行标题搜索，就是这里
     * 
     * @param key
     * @throws ClientException
     */
    public static void topTitle(Long numIid, String key, int pn, int ps) {
        // Find the default category information for the title
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        key = tryCompleteKey(user, numIid, key);

        TMResult res = TopShopItem.search(key, po, true);
        // log.error(JsonUtil.getJson(res));
        renderJSON(JsonUtil.getJson(res));
    }

    private static String tryCompleteKey(User user, Long numIid, String key) {
        if (!StringUtils.isBlank(key)) {
            return key;
        }

        return tryGetItemSearchWord(user, numIid);

    }

    protected static String tryGetItemSearchWord(User user, Long numIid) {

        log.info(format("tryGetItemSearchWord:user, numIid".replaceAll(", ", "=%s, ") + "=%s", user, numIid));

        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);

        return item.getACidKey();
    }

    public static void itemTopTitles(Long numIid, int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        String key = tryGetItemSearchWord(user, numIid);
        if (key.contains("//")) {
            key = key.substring(0, key.indexOf('/'));
        }

        TMResult res = TopShopItem.search(key, po, true);

        renderJSON(JsonUtil.getJson(res));
    }

    // @CacheFor("5min")
    public static void getRecommends(String numIids) {
        User user = getUser();
        String content = null;

        // String cachedKey = String.valueOf((user.getId() +
        // numIids).hashCode());
        //
        // content = (String) Cache.get(cachedKey);
        // if (content != null) {
        // renderJSON(content);
        // }

        List<DiagResult> res = AutoTitleAction.recommendTitles(user, numIids);
        content = JsonUtil.getJson(res);
        if (content == null) {
            content = StringUtils.EMPTY;
        }

        // Cache.set(cachedKey, content, "2min");
        renderJSON(content);
    }

    public static void getRecommend(long numIid, String newTitle) {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        log.info("[item:]" + item);
        String originTitle = null;
        if (item == null) {
            Item tbItem = ApiJdpAdapter.get(user).findItem(user, numIid);
            if (tbItem != null) {
                originTitle = tbItem.getTitle();
            }
        } else {
            originTitle = item.getTitle();
        }

        String res = AutoTitleAction.autoRecommend(getUser(), numIid);
        renderText("origin:" + originTitle + " \n new :" + res);
    }

    public static void getRecommendByNumIid(long numIid) {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        log.info("[item:]" + item);
        String originTitle = null;
        if (item == null) {
            Item tbItem = ApiJdpAdapter.get(user).findItem(user, numIid);
            if (tbItem != null) {
                originTitle = tbItem.getTitle();
            }
        } else {
            originTitle = item.getTitle();
        }

        String res = AutoTitleAction.autoRecommend(getUser(), numIid);
        renderText(res);
    }

    /**
     * 
     * @param numIidToTitle
     *            xxxx!@#yyyyy#@!xxx!@#yyy
     */
    public static void newBatchChange(String numIidToTitle) {

        User user = getUser();
        log.info(format("newBatchChange:numIidToTitle".replaceAll(", ", "=%s, ") + "=%s", numIidToTitle));
        String[] pieces = numIidToTitle.split("#@!");

        if (ArrayUtils.isEmpty(pieces)) {
            renderJSON(ListUtils.EMPTY_LIST);
        }

        Map<String, String> newTitleMap = new HashMap<String, String>();
        for (String piece : pieces) {
            String[] numIidTitleArr = StringUtils.split(piece, "!@#");
            String numIidStr = numIidTitleArr[0].trim();
            String titleStr = numIidTitleArr[1].trim();
            if (!NumberUtils.isNumber(numIidStr) || StringUtils.isEmpty(titleStr)) {
                continue;
            }
            newTitleMap.put(numIidStr, titleStr);
        }
        List<BatchResultMsg> msgs;
        List<ItemPlay> items = ItemDao.findByNumIidList(user, StringUtils.join(newTitleMap.keySet(), ','));

        msgs = new BatchReplacer(user, items, newTitleMap, null).call();

        DiagAction.refreshByUpdateMsgs(user, msgs);
        renderJSON(JsonUtil.getJson(new BatchOpResult(msgs)));
    }

    /**
     * 
     * @param numIids
     *            : xxx,xxxx
     * @param titles
     *            : xxx,xxx
     */
    public static void batchChange(String numIids, String titles) {
        User user = getUser();
        String[] numIidSplits = StringUtils.split(numIids, ",");
        String[] titleSplits = StringUtils.split(titles, ",:,");
        log.info("[numiids: ]" + ArrayUtils.toString(numIids));
        log.info("[titltes splits: ]" + ArrayUtils.toString(titles));
        Map<String, String> newTitleMap = new HashMap<String, String>();
        for (int i = 0; i < numIidSplits.length; i++) {
            String numIidStr = numIidSplits[i];
            if (i >= titleSplits.length) {
                break;
            }
            String titleStr = titleSplits[i];
            if (StringUtils.isEmpty(numIidStr) || StringUtils.isEmpty(titleStr)) {
                continue;
            }
            newTitleMap.put(numIidStr, titleStr);
        }

        List<BatchResultMsg> msgs;
        List<ItemPlay> items = ItemDao.findByNumIidList(user, numIids);

        msgs = new BatchReplacer(user, items, newTitleMap, null).call();

        DiagAction.refreshByUpdateMsgs(user, msgs);
        renderJSON(JsonUtil.getJson(new BatchOpResult(msgs)));
    }

    public static void tempBatch(BatchPageOption opt) {
        MixHelpers.infoAll(request, response);
        log.info("[opt ]" + opt);
        renderJSON(JsonUtil.getJson(ListUtils.EMPTY_LIST));
    }

    /**
     * 
     * @param opt
     * @param sellerCatId
     * @param itemCatId
     * @param status
     * @param allSale
     * @param recMode
     *            : 0 是 默认的推荐标题，1 是 官方原标题 2 是 官方推荐标题 [opt.keepSerial]=[true]
     *            [opt.keepBrand]=[true] [opt.toAddPromote]=[true]
     *            [opt.mustExcluded]=[] [opt.fixedStart]=[]
     *            [opt.allSale]=[false]
     */
    // public static void batchChangeAll(BatchPageOption opt) {
    public static void batchChangeAll() {
        // MixHelpers.infoAll(request, response);

        long sellerCatId = NumberUtil.parserLong(params.get("sellerCatId"), 0L);
        long itemCatId = NumberUtil.parserLong(params.get("itemCatId"), 0L);
        int status = NumberUtil.parserInt(params.get("status"), 0);
        int recMode = NumberUtil.parserInt(params.get("recMode"), 0);

        // 新的条件
        String title = params.get("title");
        int startScore = NumberUtil.parserInt(params.get("startScore"), 0);
        int endScore = NumberUtil.parserInt(params.get("endScore"), 0);
        boolean newSearchRule = "true".equals(params.get("newSearchRule"));

        BatchPageOption opt = new BatchPageOption();

        opt.setKeepSerial("true".equals(params.get("opt.keepSerial")));
        opt.setKeepBrand("true".equals(params.get("opt.keepBrand")));
        opt.setToAddPromote("true".equals(params.get("opt.toAddPromote")));
        opt.setAllSale("true".equals(params.get("opt.allSale")));
        opt.setMustExcluded(params.get("opt.mustExcluded"));
        opt.setFixedStart(params.get("opt.fixedStart"));
        opt.setNoColor("true".equals(params.get("opt.noColor")));
        opt.setNoNumber("true".equals(params.get("opt.noNumber")));
        opt.setSellerCatId(sellerCatId);
        opt.setItemCatId(itemCatId);
        opt.setStatus(status);
        opt.setRecMode(recMode);

        opt.setTitle(title);
        opt.setStartScore(startScore);
        opt.setEndScore(endScore);
        opt.setNewSearchRule(newSearchRule);

        log.info(format(
                "batchChangeAll:opt, sellerCatId, itemCatId, status, recMode".replaceAll(", ", "=%s, ") + "=%s", opt,
                sellerCatId, itemCatId, status, recMode));

        User user = getUser();

        /*
         * List<ItemPlay> items = ItemDao.findForBatchTitleOptimise(user,
         * opt.getSellerCatId(), opt.getItemCatId(), opt.getStatus(),
         * opt.isAllSale());
         * 
         * log.error("page opt : " + opt);
         * 
         * final Map<String, String> newTitleMap = new ConcurrentHashMap<String,
         * String>(); FengxiaoApi.buildResult(newTitleMap, opt.getRecMode(),
         * user, items, opt, null); List<BatchResultMsg> msgs =
         * ListUtils.EMPTY_LIST; msgs = new BatchReplacer(user, items,
         * newTitleMap, null).call();
         * 
         * 
         * DiagAction.refreshByUpdateMsgs(user, msgs); BatchOpResult webResult =
         * new BatchOpResult(msgs); TitleOpRecord.build(user, msgs, webResult);
         * renderJSON(JsonUtil.getJson(webResult));
         */
        UserTaskLog taskLog = AutoTitleTaskAction.addAutoTitleTask(user, JsonUtil.getJson(opt));

        renderJSON(JsonUtil.getJson(taskLog));
    }

    public static void advancedBatchRecommend(BatchPageOption opt, String numIids) {

        log.info(format("advancedBatchRecommend:opt, numIids".replaceAll(", ", "=%s, ") + "=%s", opt, numIids));

        User user = getUser();
        Set<Long> set = NumberUtil.splitLongSet(numIids);
        List<ItemPlay> itemsAll = ItemDao.findByNumIids(user.getId(), set);
        Map<Long, String> res = new HashMap<Long, String>();
        for (ItemPlay item : itemsAll) {
            String newTitle = AutoTitleAction.autoRecommend(user, item.getId(), opt);
            if (!StringUtils.isEmpty(newTitle)) {
                res.put(item.getNumIid(), newTitle);
            }
        }
        renderJSON(JsonUtil.getJson(res));
    }

    public static void advancedFengxiaoRecommend(BatchPageOption opt, String numIids) throws Exception {
        User user = getUser();
        Set<Long> set = NumberUtil.splitLongSet(numIids);

        List<ItemPlay> itemsAll = ItemDao.findByNumIids(user.getId(), set);
        Map<Long, String> res = new HashMap<Long, String>();
        for (ItemPlay item : itemsAll) {
            try {
                /*
                 * if (!item.isFenxiao()) { continue; }
                 */

                BatchResultMsg subMsg = new FengxiaoRecommender(user, item.getId(), opt).call();
                if (subMsg == null) {
                    continue;
                }

                String newTitle = subMsg.getNewTitle();
                if (StringUtils.isEmpty(newTitle)) {
                    newTitle = subMsg.getTitle();
                }

                if (StringUtils.isEmpty(newTitle)) {
                    continue;
                }
                res.put(item.getNumIid(), newTitle);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        renderJSON(JsonUtil.getJson(res));

    }

    public static void testDiag() {
        User user = getUser();
        UserDiag diag = UserDiag.findOrCreate(user);
        if (diag == null) {
            renderJSON("{}");
        }
        renderJSON(JsonUtil.getJson(diag));
    }

    public static void split(Long numIid) throws ClientException {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        log.info("[title :]" + item.getTitle());
        List<String> res = new WidAPIs.SplitAPI(item.getTitle(), SplitMode.BASE, false).execute();
        renderText(StringUtils.join(res, ','));
    }

    @CacheFor("3h")
    public static void batchOpLogs(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = TitleOpRecord.findRecent(user, po);
        renderJSON(JsonUtil.getJson(res));
    }

    @CacheFor("3h")
    public static void batchOpLogDetail(long id) {

        // TitleOpRecord model = TitleOpRecord.findById(id);
        TitleOpRecord model = TitleOpRecord.singleQuery("id = ?", id);
        if (model == null || StringUtils.isEmpty(model.getOpContent())) {
            renderJSON("[]");
        }

        renderJSON(model.getOpContent());
    }

    @CacheFor("3h")
    public static void recoverBatch(long id) {

        if (id <= 0L) {
            renderJSON(JsonUtil.getJson(BatchOpResult.makeEmpty()));
        }

        User user = getUser();
        List<BatchResultMsg> msgs = TitleOpRecord.recover(id, user);
        BatchOpResult webResult = new BatchOpResult(msgs);
        renderJSON(JsonUtil.getJson(webResult));
    }

    public static void rawSplit(String title) {
        List<String> execute = ListUtils.EMPTY_LIST;
        try {
            execute = new AutoSplit(title, true).execute();
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderJSON(execute);
    }

    public static void split(String title) {
        List<String> execute = ListUtils.EMPTY_LIST;
        try {
            execute = new AutoSplit(title, ListUtils.EMPTY_LIST).execute();
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
        renderJSON(execute);
    }

    public static void hotRate(String title, long numIid) {
        Set<String> seeds = new HashSet<String>();
        // ItemPropAction.
    }

    public static void fenxiaoRecommend() {

    }

    public static void renameHistory(int pn, int ps, Long numIid) throws IOException {
        // renderMockFileInJsonIfDev("titles.renamehistory.json");
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult res;
        if (NumberUtil.isNullOrZero(numIid)) {
            res = TitleOptimiseLog.fetch(user, po);
        } else {
            res = TitleOptimiseLog.fetch(user, numIid, po);
        }
        renderJSON(JsonUtil.getJson(res));
    }

    public static void renameHistoryAll(Long numIid) throws IOException {
        // renderMockFileInJsonIfDev("titles.renamehistory.json");
        User user = getUser();
        PageOffset po = new PageOffset(0, 10000, 10);
        TMResult res;
        if (NumberUtil.isNullOrZero(numIid)) {
            res = TitleOptimiseLog.fetch(user, po);
        } else {
            res = TitleOptimiseLog.fetch(user, numIid, po);
        }
        renderJSON(JsonUtil.getJson(res));
    }

    /**
     * 备份当前所有标题
     */
    public static void backupTitles() {
        User user = getUser();
        // ItemTitleBackup record =
        // ItemTitleBackup.findFirstBackup(user.getId());
        // if (record != null) {
        // return;
        // }
        ItemTitleBackup.build(user.getId());

        renderSuccess("success", null);
    }

    /**
     * 还原初始标题
     */
    public static void recoverFirstBackup() {
        User user = getUser();
        ItemTitleBackup record = ItemTitleBackup.findFirstBackup(user.getId());
        if (record == null) {
            return;
        }

        List<BatchResultMsg> msgs = ItemTitleBackup.recover(record.getId(), user);
        BatchOpResult webResult = new BatchOpResult(msgs);
        renderJSON(JsonUtil.getJson(webResult));
    }

    /**
     * 还原指定备份
     */
    public static void recoverBackup(Long id) {
        User user = getUser();
        if (id == null || id < 0) {
            return;
        }
        List<BatchResultMsg> msgs = ItemTitleBackup.recover(id, user);
        BatchOpResult webResult = new BatchOpResult(msgs);
        renderJSON(JsonUtil.getJson(webResult));
    }

    public static void shortTitleRecommend(long numIid) {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        String currTitle = item.getTitle();
        int currLength = ChsCharsUtil.length(currTitle);
        log.info("[" + currTitle + "] - [" + currLength + "]");
        if (ChsCharsUtil.length(currTitle) > 45) {
            renderText(StringUtils.EMPTY);
        }
        String res = AutoTitleAction.autoRecommend(getUser(), numIid);
        if (ChsCharsUtil.length(res) <= currLength) {
            renderText(StringUtils.EMPTY);
        }

        renderText(res);
    }

    public static void fetchFenxiaoTitle(Long numIid) {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (!item.isFenxiao()) {
            renderText("");
        }

        FenxiaoProduct product = new FXScItemApi(user, numIid).call();
        if (product != null && StringUtils.isEmpty(product.getName())) {
            renderText(product.getName());
        } else {
            renderText("");
        }
    }

    public static void TitleComeInWordsClick(Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }
            Map<String, Integer> comeInWordMap = ClouddateUtil.get7DayComeInWordsMap(user, numIid, 14L);
            if (comeInWordMap == null) {
                renderFailedJson("找不到7天入店关键词");
            }
            Map<String, Integer> resultMap = new HashMap<String, Integer>();
            for (String s : splits) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String lowCase = s.toLowerCase();
                if (comeInWordMap.get(lowCase) == null) {
                    continue;
                }
                if (comeInWordMap.get(lowCase) > 0) {
                    resultMap.put(lowCase, comeInWordMap.get(lowCase));
                }
            }
            renderJSON(JsonUtil.getJson(resultMap));
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
    
    public static void titleComeInWirelessWordsClick(Long numIid){
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }
            Map<String, Integer> wirelessWordMap = ClouddateUtil.get7DayComeInWirelessWord(user, numIid, 14L);
            if (wirelessWordMap == null) {
                renderFailedJson("找不到7天入店关键词");
            }
            Map<String, Integer> resultMap = new HashMap<String, Integer>();
            for (String s : splits) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String lowCase = s.toLowerCase();
                if (wirelessWordMap.get(lowCase) == null) {
                    continue;
                }
                if (wirelessWordMap.get(lowCase) > 0) {
                    resultMap.put(lowCase, wirelessWordMap.get(lowCase));
                }
            }
            renderJSON(JsonUtil.getJson(resultMap));
        } catch (ClientException e) {
            renderFailedJson("程序出现异常：" + e.getMessage());
        }
    }

    public static void TitleComeInWordsCatClick(Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }
            Map<String, Integer> comeInWordMap = ClouddateUtil.get7DayComeInWordsMap(user, numIid, 14L);
            if (comeInWordMap == null) {
                renderFailedJson("找不到7天入店关键词");
            }
            Map<String, Integer> resultMap = new HashMap<String, Integer>();
            List<String> toSearchCatList = new ArrayList<String>();
            for (String s : splits) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String lowCase = s.toLowerCase();
                if (comeInWordMap.get(lowCase) == null) {
                    continue;
                }
                toSearchCatList.add(lowCase);
            }
            if (CommonUtils.isEmpty(toSearchCatList)) {
                renderJSON(JsonUtil.getJson(new HashMap<String, IWordBase>()));
            }
            Map<String, IWordBase> resMap = new AutoWordBase(toSearchCatList, itemPlay.getCid()).execute();
            renderJSON(JsonUtil.getJson(resMap));
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void TitleNoClickWordsCatClick(Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }
            Map<String, Integer> comeInWordMap = ClouddateUtil.get7DayComeInWordsMap(user, numIid, 14L);
            if (comeInWordMap == null) {
                renderFailedJson("找不到7天入店关键词");
            }
            Map<String, Integer> resultMap = new HashMap<String, Integer>();
            List<String> toSearchCatList = new ArrayList<String>();
            for (String s : splits) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String lowCase = s.toLowerCase();
                if (comeInWordMap.get(lowCase) == null) {
                    toSearchCatList.add(s);
                    continue;
                }
                if (comeInWordMap.get(lowCase) <= 0) {
                    toSearchCatList.add(lowCase);
                }
            }
            if (CommonUtils.isEmpty(toSearchCatList)) {
                renderJSON(JsonUtil.getJson(new HashMap<String, IWordBase>()));
            }
            Map<String, IWordBase> resMap = new AutoWordBase(toSearchCatList, itemPlay.getCid()).execute();
            renderJSON(JsonUtil.getJson(resMap));
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void TitleWordsCatClick(Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }

            Map<String, IWordBase> resMap = new AutoWordBase(splits, itemPlay.getCid()).execute();
            renderJSON(JsonUtil.getJson(resMap));
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void newTitleWordsCatClick(Long numIid, String title) {

        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }

        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }

            Map<String, IWordBase> resMap = new AutoWordBase(splits, itemPlay.getCid()).execute();
            renderJSON(JsonUtil.getJson(resMap));
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void TitleNoClickWords(Long numIid) {
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }
            Map<String, Integer> comeInWordMap = ClouddateUtil.get7DayComeInWordsMap(user, numIid, 14L);
            if (comeInWordMap == null) {
                renderFailedJson("找不到7天入店关键词");
            }
            List<String> result = new ArrayList<String>();
            for (String s : splits) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String lowCase = s.toLowerCase();
                if (comeInWordMap.get(lowCase) == null) {
                    result.add(lowCase);
                    continue;
                }
                if (comeInWordMap.get(lowCase) <= 0) {
                    result.add(lowCase);
                }
            }
            renderJSON(JsonUtil.getJson(result));
        } catch (ClientException e) {
            log.warn(e.getMessage(), e);
        }
    }
    
    public static void titleNoClickWirelessWords(Long numIid){
        if (numIid == null) {
            renderFailedJson("宝贝ID为空");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderFailedJson("宝贝不存在");
        }
        String title = itemPlay.getTitle();
        if (StringUtils.isEmpty(title)) {
            renderFailedJson("宝贝标题为空");
        }
        List<String> splits;
        try {
            splits = new AutoSplit(title, false).execute();
            if (CommonUtils.isEmpty(splits)) {
                renderFailedJson("分词结果为空");
            }
            Map<String, Integer> wirelessWordMap = ClouddateUtil.get7DayComeInWirelessWord(user, numIid, 14L);
            if (wirelessWordMap == null) {
                renderFailedJson("找不到7天入店关键词");
            }
            List<String> result = new ArrayList<String>();
            for (String s : splits) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                String lowCase = s.toLowerCase();
                if (wirelessWordMap.get(lowCase) == null) {
                    result.add(lowCase);
                    continue;
                }
                if (wirelessWordMap.get(lowCase) <= 0) {
                    result.add(lowCase);
                }
            }
            renderJSON(JsonUtil.getJson(result));
        } catch (ClientException e) {
            renderFailedJson("程序出现异常：" + e.getMessage());
        }
    }

    public static void video() {
        render("tbtnavmain/seovideo.html");
    }

    // 测试用楚之小南，时间点为 1386921820200 = 2013-12-13 16:03:40
    public static void titleBackByTime(Long backToTs) {
        if (backToTs == null || backToTs <= 0) {
            renderFailedJson("传入的时间点不合法");
        }
        User user = getUser();
        if (user == null) {
            renderFailedJson("用户不存在");
        }
        Set<Long> optimisedIds = TitleOptimised.findNumIidsByUserId(user.getId());
        if (CommonUtils.isEmpty(optimisedIds)) {
            // 成功还原0个宝贝~~~
            renderFailedJson("没有需要还原的宝贝");
        }
        List<ItemPlay> items = ItemDao.findByNumIids(user.getId(), optimisedIds);
        final Map<String, String> newTitleMap = new ConcurrentHashMap<String, String>();
        final List<ItemPlay> toReplaceItems = new ArrayList<ItemPlay>();
        for (ItemPlay item : items) {
            TitleOptimiseLog log = TitleOptimiseLog.find(
                    " userId = ? and numIid = ? " + "and updated >= ? order by updated asc", user.getId(),
                    item.getNumIid(), backToTs).first();
            if (log == null) {
                continue;
            }
            String oldTitle = log.getOldTitle();
            // 如果原标题为空，则不还原
            if (StringUtils.isEmpty(oldTitle)) {
                continue;
            }
            // 如果标题与当前标题相同，则不还原
            String nowTitle = item.getTitle();
            if (oldTitle.equals(nowTitle)) {
                continue;
            }
            newTitleMap.put(item.getNumIid().toString(), oldTitle);
            toReplaceItems.add(item);

        }
        if (CommonUtils.isEmpty(toReplaceItems)) {
            renderFailedJson("没有需要还原的宝贝");
        }
        List<BatchResultMsg> msgs = new BatchReplacer(user, toReplaceItems, newTitleMap, -1L, true).call();

        renderJSON(JsonUtil.getJson(msgs));

    }

    public static void searchBackByTime(Long backToTs, int pn, int ps) {

        pn = pn < 1 ? 1 : pn;
        ps = ps < 1 ? PageSize.DISPLAY_ITEM_PAGE_SIZE : ps;
        PageOffset po = new PageOffset(pn, ps, 10);
        if (backToTs == null || backToTs <= 0) {
            renderJSON(JsonUtil.getJson(new TMResult(new ArrayList<ItemPlay>(), 0, po)));
        }
        User user = getUser();
        if (user == null) {
            renderJSON(JsonUtil.getJson(new TMResult(new ArrayList<ItemPlay>(), 0, po)));
        }

        int count = (int) TitleOptimised.countByUserId(user.getId());
        Set<Long> optimisedIds = TitleOptimised.findNumIidsByUserIdOffset(user.getId(), po);
        if (CommonUtils.isEmpty(optimisedIds)) {
            renderJSON(JsonUtil.getJson(new TMResult(new ArrayList<ItemPlay>(), 0, po)));
        }
        List<ItemPlay> items = ItemDao.findByNumIids(user.getId(), optimisedIds);

        final List<ItemPlay> toReplaceItems = new ArrayList<ItemPlay>();
        for (ItemPlay item : items) {
            TitleOptimiseLog log = TitleOptimiseLog.find(
                    " userId = ? and numIid = ? " + "and updated >= ? order by updated asc", user.getId(),
                    item.getNumIid(), backToTs).first();
            if (log == null) {
                continue;
            }
            String oldTitle = log.getOldTitle();
            // 如果原标题为空，则不还原
            if (StringUtils.isEmpty(oldTitle)) {
                continue;
            }
            // 如果标题与当前标题相同，则不还原
            String nowTitle = item.getTitle();
            if (oldTitle.equals(nowTitle)) {
                continue;
            }
            item.setToBackTitle(oldTitle);
            item.setLastOptimiseTs(log.getUpdated());
            toReplaceItems.add(item);

        }
        if (CommonUtils.isEmpty(toReplaceItems)) {
            renderJSON(JsonUtil.getJson(new TMResult(new ArrayList<ItemPlay>(), 0, po)));
        }

        TMResult result = new TMResult(toReplaceItems, toReplaceItems.size(), po);
        renderJSON(JsonUtil.getJson(result));

    }

    // 按照时间点进行还原
    @CacheFor("3h")
    public static void batchRollBack(int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = TitleOpRecord.findRecent(user, po);
        renderJSON(JsonUtil.getJson(res));
    }
}
