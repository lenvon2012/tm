
package controllers;

import static java.lang.String.format;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import jdp.ApiJdpAdapter;
import job.ItemCatOrderPayTimeDisTributeJob;
import job.UpdateCatOrderpayTimeDistributeByDelistTime;
import job.carriertask.CarrierTaskJob;
import job.jms.JMSTestJob;
import job.writter.CommentsWritter;
import job.writter.batchUserRateGetJob;
import models.CPEctocyst.ChiefStaffDetail;
import models.carrierTask.CarrierTask;
import models.comment.Comments;
import models.comment.CommentsFailed;
import models.defense.BlackListBuyer;
import models.popularized.Popularized;
import models.user.User;
import models.word.top.NoMatchTopURLBaseCid;
import models.word.top.TopURLBase;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.db.jpa.NoTransaction;
import play.libs.Crypto;
import play.mvc.Controller;
import proxy.NewProxyTools;
import result.TMResult;
import smsprovider.SendInfo;
import spider.mainsearch.MainSearchApi.PriceRangeLike;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import sun.misc.BASE64Decoder;
import transaction.JPATransactionManager;
import underup.frame.industry.CatTopSaleItemSQL;
import underup.frame.industry.UpdateItems;
import utils.PolicyDBUtil;
import utils.TaobaoUtil;
import actions.ItemGetAction;
import actions.NewUvPvDiagAction;
import actions.catunion.UserIdNickAction;
import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.UserRateInfo;
import actions.clouddata.CloudDataAction;
import actions.dama.DamaAction;
import actions.dama.DamaAction.ImageResult;
import actions.emay.EMaySmsSendAction;
import actions.pdd.CopyToPddAction;
import actions.ronghe.SMSSendRongHe;
import autotitle.AutoSplit;
import bustbapi.ItemApi;
import bustbapi.ItemCatApi;
import bustbapi.JMSApi.JushitaJmsUserGet;
import bustbapi.ShowWindowApi;
import bustbapi.TBApi;
import bustbapi.TradeRateApi;
import bustbapi.request.WirelessShareTpwdCreateRequest;
import bustbapi.request.WirelessShareTpwdCreateRequest.GenPwdIsvParamDto;
import bustbapi.response.WirelessShareTpwdCreateResponse;
import carrier.FileCarryUtils;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.FileItem;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.ItemCat;
import com.taobao.api.domain.TmcUser;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.request.ItemGetRequest;
import com.taobao.api.request.PictureUploadRequest;
import com.taobao.api.request.TraderateAddRequest;
import com.taobao.api.response.ItemGetResponse;
import com.taobao.api.response.PictureUploadResponse;
import com.taobao.api.response.TraderateAddResponse;

import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;
import controllers.TmSecurity.SecurityType;
import dao.UserDao;
import dao.UserDao.UserBatchOper;
import dao.defense.BlackListBuyerDao;
import dao.item.ItemDao;
import dao.popularized.PopularizedDao;

public class Application extends Controller {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static final String TAG = "Application";

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void test() {
    	String testUserNick = "julepcouture旗舰店";
    	User user =  UserDao.findByUserNick(testUserNick);
    	if(user == null) {
    		render("用户不存在");
    	}
    	putUser(user);
    	
    	boolean needRedirect = NewUvPvDiagAction.isNeedRedirect(user);
    	if(needRedirect) {
    		redirect(App.CONTAINER_TAOBAO_URL);
    	}
        render("Application/tbtIndex.html");
    }
    
    public static void doCarrierTaskJob() {
    	new CarrierTaskJob().doJob();
    }
    
    public static void getYCHIP() {
    	String ip1 = "ip1";
    	try {
 			ip1 = InetAddress.getLocalHost().getHostAddress();
 		} catch (UnknownHostException e) {
 			log.error(e.getMessage(), e);
 		}
    	renderText(ip1);
    }
    
    protected static void putUser(models.user.User user) {
        response.setCookie(WebParams.SESSION_USER_KEY, user.getSessionKey(), "120h");
        response.setCookie(WebParams.SESSION_USER_VERSION, String.valueOf(user.getVersion()), "120h");
        try {
            String encodeUid = Crypto.encryptAES(user.getId().toString());
            response.setCookie(WebParams.COOKIE_ENCODE_USER_ID, encodeUid, "120h");
            response.setCookie(WebParams.SESSION_USER_NICK, URLEncoder.encode(user.getUserNick(), "utf-8"), "120h");
            session.put(WebParams.COOKIE_ENCODE_USER_ID, encodeUid);
            session.put(WebParams.SESSION_USER_KEY, String.valueOf(user.getSessionKey()));

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        request.args.put(WebParams.ARGS_USER, user);
        log.info("[User has been put..]" + user);
    }
    
	public static void checkJMSAdd() {
		new UserBatchOper(32) {
			@Override
			public void doForEachUser(User user) {
				this.sleepTime = 1L;
				if (!UserDao.doValid(user)) {
					return;
				}
				// 查询某个用户是否同步消息
				JushitaJmsUserGet jmsGet = new JushitaJmsUserGet(user);
				TmcUser onsUser = jmsGet.call();
				if(onsUser == null) {
					int notAddNum = (Integer) (Cache.get("notAddNum") == null? 0 : Cache.get("notAddNum"));
					notAddNum ++;
					Cache.set("notAddNum", notAddNum, "1h");
					String notAddUserNick = (String) (Cache.get("notAddUserNick") == null? StringUtils.EMPTY : Cache.get("notAddUserNick"));
					notAddUserNick = notAddUserNick + user.getUserNick() + ",";
					Cache.set("notAddUserNick", notAddUserNick, "1h");
				} else {
					int addedNum = (Integer) (Cache.get("addedNum") == null? 0 : Cache.get("addedNum"));
					addedNum ++;
					Cache.set("addedNum", addedNum, "1h");
				}
			}
		}.call();
		
		int addedNum = (Integer) (Cache.get("addedNum") == null? 0 : Cache.get("addedNum"));
		int notAddNum = (Integer) (Cache.get("notAddNum") == null? 0 : Cache.get("notAddNum"));
		String notAddUserNick = (String) (Cache.get("notAddUserNick") == null? StringUtils.EMPTY : Cache.get("notAddUserNick"));
		
		renderText("ONS消息同步用户添加情况---已添加： " + addedNum + "---未添加： " + notAddNum + "---未添加用户昵称： " + notAddUserNick);
	}
	
	public static void startdoJMSTestJob() {
		try {
			new JMSTestJob().doJob();
		} catch (Exception e) {
			e.printStackTrace();
			renderText("error");
		}
		renderText("success");
	}
	
	public static void limitUser(Boolean isLimit) {
		String cacheKey = "limit_user_";
		Cache.set(cacheKey, isLimit, "24h");
		if(isLimit) {
			renderText("限制评价同步成功！");
		}
		renderText("解除评价同步成功！");
	}

    public static void testEmay() {
        SendInfo sendInfo = EMaySmsSendAction.send("13656676326", "您的消息2");
        log.warn(sendInfo.getStatus() + "------------------------------");
    }
    
    public static void testAutoSplit(String words) {
    	if(StringUtils.isEmpty(words)) {
    		renderText("参数为空");
    	}
    	try {
			List<String> list = new AutoSplit(words, false).execute();
			renderJSON(JsonUtil.getJson(list));
		} catch (ClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 行业销售属性分析
     */
    public static void catAnalysis() {
        render("autoTitle/txgCatAnalysis.html");
    }
    
    @NoTransaction
    public static void clearUserCache(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderText("请传入旺旺");
    	}
    	nick = StringUtils.trim(nick);
    	String key = "usernick-" + nick;
    	Cache.delete(key);
    	renderText("清除成功");
    }
    
	public static void checkRobot(String[] args) throws MalformedURLException,
			IOException, URISyntaxException, AWTException {
		// 此方法仅适用于JdK1.6及以上版本
		Desktop.getDesktop().browse(new URL("http://www.baidu.com").toURI());
		Robot robot = new Robot();
		robot.delay(10000);
		Dimension d = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
		int width = (int) d.getWidth();
		int height = (int) d.getHeight();
		// 最大化浏览器
		robot.keyRelease(KeyEvent.VK_F11);
		robot.delay(2000);
		Image image = robot.createScreenCapture(new Rectangle(0, 0, width,
				height));
		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		// 保存图片
		ImageIO.write(bi, "jpg", new File("google.jpg"));
	}
   
    public static void getRandomPopularitedItems(Long numIid) {
        User user = null;
        String sid = session.get(WebParams.SESSION_USER_KEY);
        if (StringUtils.isEmpty(sid)) {
            sid = params.get("sid");
        }

        if (!StringUtils.isEmpty(sid)) {
            user = UserDao.findBySessionKey(sid);
        }

        List<Popularized> items = new ArrayList<Popularized>();
        if (numIid != null) {
            //Popularized p = Popularized.find("numIid = ?", numIid).first();
            Popularized p = Popularized.findFirstByNumIid(numIid);
            if (p != null) {
                items.add(p);
                List<Popularized> random = PopularizedDao.getRandomPopularizedItems(user);
                if (random.contains(p)) {
                    random.remove(p);
                }
                items.addAll(random);
                renderJSON(JsonUtil.getJson(items));
            }
        }
        items = PopularizedDao.getRandomPopularizedItems(user);
        renderJSON(JsonUtil.getJson(items));
    }

    public static void ShowRandomPopularizedItems(long numIid) {
        if (numIid <= 0L) {
            numIid = tryFindANumIid();
        }

        render("Popularize/showRandomPopularitedItems.html", numIid);
    }

    static long tryFindANumIid() {
        try {
            User user = TMController.tryFindUser();
            if (user == null) {
                return 0L;
            }
            //Popularized first = Popularized.find("userId = ? ", user.getId()).first();
            Popularized first = Popularized.findFirstByUserId(user.getId());
            if (first == null) {
                return 0L;
            }
            return first.getNumIid();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return 0L;
        }
    }

    public static void doComment(Long userId, String buyerNick, Long tid, Long oid, String content) {

        log.info(format("doComment:userID, buyerNick, tid, oid, content".replaceAll(", ", "=%s, ") + "=%s", userId,
                buyerNick, tid, oid, content));

        boolean doneForThisTime = false;
        int retry = 5;
        log.info("Do autoEvaluate Job with parasm: ");
        String sessionKey = StringUtils.EMPTY;
        User user = UserDao.findById(userId);
        try {
            if (user == null) {
                return;
            } else {
                sessionKey = user.sessionKey;
            }
            
            buyerNick = TmSecurity.decrypt(buyerNick, SecurityType.SIMPLE, user);

            if (content.isEmpty()) {
                content = "欢迎下次光临";
            }
            if (BlackListBuyerDao.findByBuyerName(user.getId(), buyerNick) != null) {
                log.info("黑名单用户，不评价");
                return;
            }
            TaobaoClient client = TBApi.genClient();
            TraderateAddRequest req = new TraderateAddRequest();
            req.setTid(tid);
            req.setOid(oid);
            req.setResult("good");
            req.setRole("seller");
            req.setContent(content);
            req.setAnony(false);

            int count = 0;

            while (count++ < retry && !doneForThisTime) {
                TraderateAddResponse response = client.execute(req, sessionKey);
                if (response.isSuccess()) {
                    doneForThisTime = true;
                    // sub string when content is too long to save
                    String realContent = StringUtils.EMPTY;
                    if (content.length() > 255) {
                        realContent = content.substring(0, 250).concat("...");
                    } else {
                        realContent = content;
                    }
                    new Comments(user.getId(), tid, oid, "good", realContent,
                            user.userNick, buyerNick).save();
                    //renderText("评价成功");
                }
            }

        } catch (Exception e) {
            //log.warn(e.getMessage(), e);
            //renderText(e.getMessage());
        }

        if (!doneForThisTime) {
            log.error("[comment failed for user = " + user.userNick + " and tid = " + tid + " ]");
            // sub string when content is too long to save
            String realContent = StringUtils.EMPTY;
            if (content.length() > 255) {
                realContent = content.substring(0, 250).concat("...");
            } else {
                realContent = content;
            }
            new CommentsFailed(user.getId(), tid, oid, "good", realContent,
                    user.userNick, buyerNick, "").jdbcSave();
            //renderText("评价失败");
        }
    }

    public static void randomShow() {
        int fetchNum = 4;
        User user = null;
        String sid = session.get(WebParams.SESSION_USER_KEY);
        if (StringUtils.isEmpty(sid)) {
            sid = params.get("sid");
        }

        if (!StringUtils.isEmpty(sid)) {
            user = UserDao.findBySessionKey(sid);
        }
        List<Popularized> items = PopularizedDao.randomItems(user, fetchNum);
        renderJSON(JsonUtil.getJson(items));
    }

    public static void vgouShare() {
        render("/Application/vgouShare.html");
    }

    public static void kefuSweety() {
        render("/KefuSweety/KefuSweety.html");
    }

    public static void getDelistTimeByNumIid(Long numIid) {
        if (numIid == null) {
            renderText("宝贝id为空");
        }
        Item item = ApiJdpAdapter.tryFetchSingleItem(null, numIid);

        if (item == null) {
            renderText("找不到对应的宝贝");
        }
        java.util.Date delistDate = item.getDelistTime();
        if (delistDate == null) {
            renderText("返回宝贝没有下架时间");
        }
        renderText(sdf.format(delistDate.getTime()));
    }

    public static void checkUser(String nick) {
        if (StringUtils.isEmpty(nick)) {
            renderText("用户名为空");
        }
        User user = UserDao.findByUserNick(nick);
        if (user == null) {
            renderText("用户不存在");
        }
        Long cid = ItemDao.findMaxCid(user.getId());
        if (cid < 0) {
            renderText("找不多包含最多宝贝的cid");
        }
        List<TopURLBase> bases = TopURLBase.find("itemCidString like '%" + cid + "%'").fetch();
        if (CommonUtils.isEmpty(bases)) {
            new NoMatchTopURLBaseCid(cid).jdbcSave();
        }
        renderJSON(JsonUtil.getJson(bases));
    }

    public static void testTaocan11() {
        render("taocan/taocan11.html");
    }

    public static void testMainSearchApi() {
        List<PriceRangeLike> res = TaobaoUtil.getWordPriceRangeLikes("男装");
        log.info(res.toString());
    }

    public static void runItemCatOrderPayTimeDisTributeJobByIndex(Integer index) {
        if (index == null || index < 0 || index > 15) {
            renderText("index 不合法");
        }

        ItemCatOrderPayTimeDisTributeJob.OrderDisplayIndex = index;
        new ItemCatOrderPayTimeDisTributeJob().doJob();
    }

    public static String doComemntContent(String userNick, Long tid, String buyerNick, String content) {
        User user = UserDao.findByUserNick(userNick);
        TradeRate tradeRate = new TradeRateApi.TraderateListAdd(user, tid, "good", "seller", content).call();
        if (tradeRate == null) {
            // 如果不成功，休眠一秒
            CommonUtils.sleepQuietly(1000L);
            return null;
        }
        String realContent = StringUtils.EMPTY;
        if (content.length() > 255) {
            realContent = content.substring(0, 250).concat("...");
        } else {
            realContent = content;
        }
        CommentsWritter.addMsg(user.getId(), tid, tid, "good", realContent, user.getUserNick(), buyerNick);
        return realContent;
    }

    public static void getUpdateCatOrderpayTimeDistributeByDelistTimeCount() {
        String s = UpdateCatOrderpayTimeDistributeByDelistTime.totalCount + "," +
                UpdateCatOrderpayTimeDistributeByDelistTime.lessThanFifty + "," +
                UpdateCatOrderpayTimeDistributeByDelistTime.successCount + "," +
                UpdateCatOrderpayTimeDistributeByDelistTime.failCount + "," +
                sdf.format(new Date(UpdateCatOrderpayTimeDistributeByDelistTime.jobTs)) + "," +
                UpdateCatOrderpayTimeDistributeByDelistTime.catSearchLessThanFifty;
        renderText(s);
    }

    public static void redirectToXChedao() {
        render("/Application/redirectToXchedao.html");
    }

    public static void testGuanlianIndex() {
        render("/guanlian/guanlianIndex.html");
    }
    
    public static void testCatGetApi(Long cid) {
        List<ItemCat> tbCats = new ItemCatApi.ItemcatsGet(0L, cid).call();
        renderJSON(JsonUtil.getJson(tbCats));
    }

    public static void spideUserByUserId(Long userId) {
    	if(userId == null || userId <= 0) {
    		userId = 79742176L;
    	}
    	UserRateInfo rateInfo = UserRateSpiderAction.spiderUserRateById(userId);
    	renderJSON(JsonUtil.getJson(rateInfo));
    }
    
    public static void getBuyerIdByNick(String userNick) {
    	if(StringUtils.isEmpty(userNick)) {
    		renderText("用户名为空");
    	}
    	Long userId = UserIdNickAction.BuyerIdApi.findBuyerId(userNick);
    	renderText(userId);
    }

    public static void testMobileSearchRank() {
    	String res = API.directGet("http://s.m.taobao.com/search_turn_page_iphone.htm?q=%E7%94%B7%E8%A3%85&sst=1&wlsort=8&abtestNick=&bagtype=&bagvalue=&sid=bffb95a655f8dbc7cb4c41c0864c3941&abtest=8&style=list&sort=&page=1&callback=jsonp140262720880987982",
    			"www.taobao.com", null);
    	if(!StringUtils.isEmpty(res)) {
    		res = res.replace("jsonp140262720880987982(", "");
    		res = res.substring(0, res.length() - 1);
    	}
    	List<MainSearchItemRank> ranks = new ArrayList<MainSearchItemRank>();
    	try {
			JSONObject obj = new JSONObject(res);
			JSONArray listItem = obj.getJSONArray("listItem");
			for(int i = 0; i < listItem.length(); i++) {
				JSONObject object = (JSONObject) listItem.get(i);
				String nick = object.toString();
				log.info("Mobile Rank find item " + object.getString("name") +
						" for seller " + nick + " with rank = ");
				if(StringUtils.isEmpty(nick)) {
					continue;
				}
				
					ranks.add(new MainSearchItemRank(object.getString("img2"), 0, object.getString("url"),
							object.getLong("itemNumId"), object.getString("name"), "nanzhuan"));
					
			}
			renderJSON(JsonUtil.getJson(ranks));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public static void decodeChs(String encoded) throws UnsupportedEncodingException {
    	renderText(URLDecoder.decode(encoded, "gbk"));
    }
    
    public static void testModule() {
    	Long sellerId = -1L;
    	renderText((sellerId.intValue() % 16));
    }
    
    public static void ensureChiefStaffDetail(){
        try{
            long existId = ChiefStaffDetail.dp.singleLongQuery("select id from chief_staff_detail limit 1");
            if(existId < 0L){
                PolicyDBUtil.loadSqlFile(ChiefStaffDetail.dp.getSrc(),"chief_staff_detail.sql");
            }
        }catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
    
 // 每页40
    public static String itemUrlPre = "http://item.taobao.com/item.htm?id=";
    public static void getOnePennyWallPaper(String word, int startPage, int endPage) {
    	if(StringUtils.isEmpty(word)) {
    		word = "壁纸0.01一分钱";
    	}
    	if(startPage < 1) {
    		startPage = 1;
    	}
    	if(endPage < startPage) {
    		endPage = startPage;
    	}
    	List<String> urls = new ArrayList<String>(); 
    	StringBuilder sb = new StringBuilder("http://s.taobao.com/search?q=");
        try {
			sb.append(URLEncoder.encode(word, "utf-8"));
			sb.append("&filter=reserve_price%5B");
			sb.append("0.01");
            sb.append("%2C");
            sb.append(0.01);
            sb.append("%5D&fs=0&filterFineness=2");
            sb.append("&ssid=s5-e&&bcoffset=1&search_type=item&tab=all&sourceId=tb.index&style=list&cd=false");
            for(int page = startPage; page <= endPage; page++) {
            	sb.append("&s=");
                sb.append(40 * (page - 1));
                String url = sb.toString();
                String content = StringUtils.EMPTY;
                for(int i = 0; i < 5; i++) {
        
                    content = API.directGet(url, "http://www.taobao.com",  null);
                    if(StringUtils.isEmpty(content)) {
                    	continue;
                    }
                    break;
                }
                
                content = content.replaceAll("textarea", "div");

                int contentIndex = content.indexOf("class=\"tb-content\"");
                // log.error("content index;" + contentIndex);
                // log.error("origin length:" + content.length());
                if (contentIndex > 0) {
                	StringBuilder b = new StringBuilder();
                    b.append(content.substring(0, contentIndex));
                    String subEnd = content.substring(contentIndex);
                    subEnd = subEnd.replace("&lt;", "<");
                    subEnd = subEnd.replace("&gt;", ">");
                    subEnd = subEnd.replace("&quot;", "\"");
                    b.append(subEnd);
                    content = b.toString();
                }
                
                Document doc = Jsoup.parse(content);
                
                Elements elements = doc.select(".tb-content").select(".row");

                if (elements == null || elements.size() == 0) {
                    elements = doc.select("#list-content").select(".list-item");
                }
                if (elements == null || elements.size() == 0) {
                    elements = doc.select(".tb-content").select(".list-item");
                }
               
                for (Element element : elements) {
                    String price = element.select(".g_price-highlight strong").text();
                    if(price.equals("0.01")) {
                    	String numIid = element.attr("nid");
                    	urls.add(itemUrlPre + numIid);
                    }
                }
            }
   
            
            renderJSON(JsonUtil.getJson(urls));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void refreshRequestUser() {
    	User user = (User) request.args.get(WebParams.ARGS_USER);
    	request.args.remove(WebParams.ARGS_USER);
    	User newUser = User.findByUserId(user.getId());
    	request.args.put(WebParams.ARGS_USER, newUser);
    	renderText("OK");
    }
    
    public static void refreshNick(String oldNick, String newNick) {
    	if(StringUtils.isEmpty(oldNick) || StringUtils.isEmpty(newNick)) {
    		renderText("请传入错误的旺旺与真实旺旺");
    	}
    	User user = User.findByUserNick(oldNick);
    	if(user == null) {
    		renderText("错误旺旺对应的用户不存在");
    	}
    	user.setUserNick(newNick);
    	user.jdbcSave();
    	renderText("OK");
    }
    
    public static void testArrayClear() {
    	List<BlackListBuyer> buyers = new ArrayList<BlackListBuyer>();
    	buyers.add(new BlackListBuyer(122L, "chudas", System.currentTimeMillis(), "dsa"));
    	buyers.add(new BlackListBuyer(1234L, "chusaSAsaSAadas", System.currentTimeMillis(), "dsa"));
    	BlackListBuyerDao.testArray(buyers);
    	for(BlackListBuyer buyer : buyers) {
    		log.info("dsad");
    	}
    } 

    public static void goodluck2015() {
    	TaobaoClient client=new DefaultTaobaoClient(App.API_TAOBAO_URL, "23082368", "52693c7d664eec1f114fc61a3c7dddda");
    	ItemGetRequest req=new ItemGetRequest();
    	req.setFields("num_iid,title,price,desc_modules,sell_point");
    	req.setNumIid(43906709480L);
    	try {
			ItemGetResponse response = client.execute(req , "");
			log.info(response.getBody());
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @NoTransaction
    public static void usersGetApi(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderText("传入的用户名为空");
    	}
    	com.taobao.api.domain.User user = UserRateSpiderAction.getUserByApi(nick);
    	renderJSON(JsonUtil.getJson(user));
    }
    
    @NoTransaction
    public static void clearQueue() {
    	batchUserRateGetJob.clearMsg();
    }
    
    @NoTransaction
    public static void doUpdateItems() {
    	new UpdateItems().doJob();
    }

    @NoTransaction
    public static void getSimpleItem(Long numIid) {
    	renderJSON(JsonUtil.getJson(ItemGetAction.getSimpleItem(numIid)));
    }
    
    @NoTransaction
    public static void checkTepllate(Long numIid) {
    	Long ts = System.currentTimeMillis();
    	log.info("start at " + ts);
    	com.alibaba.fastjson.JSONArray jsonArray = new ItemApi.ItemsPropsGetApi(50008899L).call();
    	log.info("checkTepllate took " + (System.currentTimeMillis() - ts) / 1000 + "s");
    	renderJSON(JsonUtil.getJson(ItemGetAction.getTempletProps(50008899, null)));
    }
    
    @NoTransaction
    public static void getApiItem(Long numIid) {
    	Set<Long> numIids = new HashSet<Long>();
    	numIids.add(numIid);
    	Item item = (Item)ItemApi.tryItemList(UserDao.findByUserNick("求量不求价"), numIids, true).get(0);
    	String s = JsonUtil.getJson(CatTopSaleItemSQL.parseItemProps(item));
    	renderJSON(s);
    }
    
    @NoTransaction
    public static void testNewProxy() {
    	HttpHost host = NewProxyTools.getHost();
    	String s = NewProxyTools.proxyGet("http://xiaozhu61.taobao.com/i/asynSearch.htm?_ksTS=1457336046908_138&callback=jsonp139&mid=w-4956439-0&wid=4956439&path=/search.htm&search=y&spm=a1z10.1-c.0.0.bWVoy9",
    			"http://xiaozhu61.taobao.com");
    	String ss = NewProxyTools.proxyGet("http://www.baidu.com",
    			"http://www.baidu.com");
    	renderText(ss);
    }
    
    @NoTransaction
    public static void testPicUpload() {
    	User user = UserDao.findByUserNick("zai1635419745");
    	TaobaoClient client = TBApi.genClient();
    	PictureUploadRequest req = new PictureUploadRequest();
    	req.setPictureCategoryId(123L);
    	req.setImg(new FileItem("/home/lzl/图片/12.png"));
    	req.setImageInputTitle("Bule.jpg");
    	req.setTitle("图片名称");
    	req.setPictureCategoryId(0L);
    	req.setClientType("client:computer");
    	PictureUploadResponse rsp;
		try {
			rsp = client.execute(req, user.getSessionKey());
			System.out.println(rsp.getBody());
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @NoTransaction
    public static void testRecommendAPI() {
    	User user = UserDao.findByUserNick("时尚广场00");
    	TMResult<Item> result = new ShowWindowApi.AddRecommend(user, 528854518251L).call();
    	log.info("d");
    }
    
    @NoTransaction
    public static void removeShopCarrierByPublisherAndWW(String publisher, String ww) {
    	if(StringUtils.isEmpty(publisher) || StringUtils.isEmpty(ww)) {
    		renderText("请传入卖家旺旺与被复制的店铺旺旺");
    	}
    	boolean isSuccess = CarrierTask.deleteByPublisherAndww(publisher, ww);
    	if(isSuccess) {
    		renderText("删除成功");
    	}
		renderText("删除失败");
    }
    
    @NoTransaction
    public static void getHotItem(int offset, int threhold) {
    	if(threhold <= 0) {
    		threhold = 10;
    	}
    	if(offset <= 0) {
    		offset = 0;
    	}
    	int limit = 16;
    	while (true) {

            List<User> findList = UserDao.findValidList(offset, limit);
            if (CommonUtils.isEmpty(findList)) {
                renderText("算完啦");
            }
            if(offset > threhold) {
            	renderText("跑完啦");
            }
            for (final User user : findList) {
                offset++;
                log.info("before checkEachUser for user: " + user.getUserNick() + "with offset = " + offset);
                TMConfigs.getAttackPool().submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                    	CloudDataAction.checkEachUser(user);
                        return null;
                    }
                });
                
               
            }

            findList.clear();
            JPATransactionManager.clearEntities();
            CommonUtils.sleepQuietly(500);
        }
    	//CloudDataAction.checkEachUser(UserDao.findByUserNick("clorest510"));
    }
    
    public static void testRongheSms() {
    	SMSSendRongHe.sendSms("13656676326", "dddd", "dsdada");
    }
    
    @NoTransaction
    public static void genTaokouling(String url, String text, String logoUrl) {
    	if(StringUtils.isEmpty(url)) {
    		renderJSON(new TMResult(false, "请输入口令跳转url", null));
    	}
    	if(StringUtils.isEmpty(text)) {
    		renderJSON(new TMResult(false, "请输入口令弹框内容", null));
    	}
    	TaobaoClient client=new DefaultTaobaoClient(App.API_TAOBAO_URL, "23726300", "8045e469afe8039f20fcab415236c1f2");
    	WirelessShareTpwdCreateRequest req = new WirelessShareTpwdCreateRequest();
    	GenPwdIsvParamDto obj1 = new GenPwdIsvParamDto();
    	//obj1.setExt("{\"aa\":\"bb\"}");
    	obj1.setLogo(logoUrl);
    	obj1.setUrl(url);
    	obj1.setText(text);
    	//obj1.setUserId(24234234234L);
    	req.setTpwdParam(obj1);
    	try {
    		WirelessShareTpwdCreateResponse rsp = client.execute(req);
    		if(rsp.isSuccess()) {
    			renderJSON(new TMResult(true, rsp.getBody(), null));
    		}
    		renderJSON(new TMResult(false, rsp.getMsg(), null));
    	} catch (Exception e) {
    		log.error(e.getMessage());
			// TODO: handle exception
		}
    	renderJSON(new TMResult(false, "生成失败", null));
    }
    
    @NoTransaction
    public static void testPdd() throws JSONException, UnsupportedEncodingException {
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost("http://mms.pinduoduo.com/glide/v2/mms/edit/commit/create_new");
        httpPost.addHeader("Referer", "http://mms.pinduoduo.com/Pdd.html");
        //httpPost.addHeader("Cookie", "_ga=GA1.2.306482878.1506342738; api_uid=wKgVpFn2t2UXeT+7EqjgAg==; aliyungf_tc=AQAAAKGaJ1w2ygsAbe93fdA+R5PUluyE; PASS_ID=383703131d874cc0a011532c1055e815; Hm_lvt_df3f672cabd7e98ed9defe423766f3f5=1509340884,1509355827; Hm_lpvt_df3f672cabd7e98ed9defe423766f3f5=1510054749");

        httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject param = new JSONObject();
        //param.put("upload_sign", "4d000310933f71f946083b965a70257c953de2ff");
        //param.put("pageSize", "1000");
        StringEntity se = new StringEntity(param.toString());
        httpPost.setEntity(se);
        HttpResponse rsp;

        try {
            rsp = httpclient.execute(httpPost);
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            renderText(content);
        } catch (Exception e) {
            log.error(e.getMessage());
            renderText("err");
        }
    }
    
	public static void testAddPdd() throws JSONException,
			UnsupportedEncodingException {
		try {
			// 获取登陆图片验证码
			String token = StringUtils.EMPTY, imageBase64 = StringUtils.EMPTY, code = StringUtils.EMPTY,
					PASS_ID = StringUtils.EMPTY, mallId = StringUtils.EMPTY, cookie = StringUtils.EMPTY;
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(
					"http://mms.pinduoduo.com/captchaCode/getCaptchaCode");
			httpGet.addHeader("Referer", "http://mms.pinduoduo.com/Pdd.html");
		
			httpGet.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
			httpGet.addHeader("Content-Type", "application/json; charset=UTF-8");
			HttpResponse rsp;

			rsp = httpclient.execute(httpGet);
			HttpEntity entity = rsp.getEntity();
			String content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			JSONObject object = new JSONObject(content);
			Boolean isOk = object.getBoolean("result");
			if (isOk) {
				token = object.getJSONObject("data").getString("token");
				imageBase64 = object.getJSONObject("data").getString("image");
			}
			DamaAction dama = new DamaAction();
			// String pathString = GenerateImage(imageBase64);
			TMResult result = dama.updateImage("3040",
					GenerateImage(imageBase64));
			if (result.isOk()) {
				ImageResult img = (ImageResult) result.getRes();
				code = img.getResult();
			}
			
			// 登陆
			HttpPost httpPost = new HttpPost(
					"http://mms.pinduoduo.com/auth");
			httpPost.addHeader("Referer", "http://mms.pinduoduo.com/Pdd.html");
			httpPost.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
			httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
			
			JSONObject param = new JSONObject();
	        param.put("authCode", code);
	        param.put("password", "bigexibo!@#123");
	        param.put("token", token);
	        param.put("username", "pdd1016920292");
	        param.put("verificationCode", "");
	        StringEntity se = new StringEntity(param.toString());
	        httpPost.setEntity(se);
	        rsp = httpclient.execute(httpPost);
			entity = rsp.getEntity();
			content = EntityUtils.toString(entity); // {"authResult":true,"userInfo":{"id":140554,"username":"pdd1016920292","mallId":101692,"passwordStatus":1,"mobile":"18153071014","nickname":"","roleIdList":[1,2,3,4],"mallOwner":true,"roleNameList":["admin","custom service","operator","csadmin"],"permissionList":["permissions","users","roles","bank","chats"]},"inMobileWhiteList":true,"mobileVerification":true} and response is Set-CookiePASS_ID=73d33f8c0d19465aa3f11d3afa4473cd; expires=Sat, 18-Nov-2017 06:13:41 GMT; Max-Age=864000; path=/; HttpOnly
			EntityUtils.consume(entity);
			PASS_ID = rsp.getHeaders("Set-Cookie")[0].getValue();
			PASS_ID = PASS_ID.substring("PASS_ID=".length(), PASS_ID.indexOf(";"));
			if(content.indexOf("mallId") > 0) {
				int index1 = content.indexOf("mallId");
				int index2 = content.indexOf(",", index1);
				mallId = content.substring(index1 + "mallId\":".length(), index2);
			}
			cookie = "_ga=GA1.2.306482878.1506342738; api_uid=wKgVpFn2t2UXeT+7EqjgAg==; aliyungf_tc=AQAAAKGaJ1w2ygsAbe93fdA+R5PUluyE; PASS_ID=" + PASS_ID + "; Hm_lvt_df3f672cabd7e98ed9defe423766f3f5=1509340884,1509355827; Hm_lpvt_df3f672cabd7e98ed9defe423766f3f5=1510054749";
			// 获取运费模板
			httpPost = new HttpPost("http://mms.pinduoduo.com/express_base/cost_template/get_list");
			httpPost.addHeader("Referer", "http://mms.pinduoduo.com/Pdd.html");
			httpPost.addHeader("Cookie", cookie);

			httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
			httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
	        param = new JSONObject();
	        param.put("mallId", mallId);
	        param.put("pageNo", "1");
	        param.put("pageSize", "1000");
	        se = new StringEntity(param.toString());
	        httpPost.setEntity(se);
	        rsp = httpclient.execute(httpPost);
	        entity = rsp.getEntity();
			content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			
			//创建商品获取commitId
			httpPost = new HttpPost("http://mms.pinduoduo.com/glide/v2/mms/edit/commit/create_new");
			httpPost.addHeader("Referer", "http://mms.pinduoduo.com/Pdd.html");
			httpPost.addHeader("Cookie", cookie);

			httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
			httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
	        param = new JSONObject();
	        se = new StringEntity(param.toString());
	        httpPost.setEntity(se);
	        rsp = httpclient.execute(httpPost);
	        entity = rsp.getEntity();
			content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			renderText(content);
		} catch (Exception e) {
			log.error(e.getMessage());
			renderText("err");
		}

	}
    
    public static byte[] GenerateImage(String imgStr) { // 对字节数组字符串进行Base64解码并生成图片  
        if (imgStr == null) {
        	//return StringUtils.EMPTY;  
        }
        if(imgStr.indexOf("base64,") <= 0) {
        	//return StringUtils.EMPTY;  
        }
        imgStr = imgStr.substring(imgStr.indexOf("base64,") + "base64,".length());
        BASE64Decoder decoder = new BASE64Decoder();  
        try {  
            // Base64解码  
            byte[] b = decoder.decodeBuffer(imgStr);  
            for (int i = 0; i < b.length; ++i) {  
                if (b[i] < 0) {// 调整异常数据  
                    b[i] += 256;  
                }  
            }  
            // 生成jpeg图片  
            //String imgFilePath = Play.tmpDir.getAbsolutePath() + File.separator + "pddLoginVerigyCode" + File.separator + System.currentTimeMillis() + ".jpeg";  
            
            //OutputStream out = new FileOutputStream(imgFilePath);  
            //out.write(b);  
            //out.flush();  
            //out.close();  
            return b;  
        } catch (Exception e) {  
        	log.error(e.getMessage());
            //return StringUtils.EMPTY;  
        }  
        return null;
    }  
    
    
   
    public static String uploadUrlToPdd(String picUrl,int height,int width,String passId) {
		String filePath=FileCarryUtils.downOnlineDealSize(picUrl, width, height);
		try {
			//获取签名
			String content=CopyToPddAction.sendGet(CopyToPddAction.GET_UPLOAD_SIGN, CopyToPddAction.REQ_TYPE_JSON, CopyToPddAction.Referer_URL, CopyToPddAction.UA,"PASS_ID="+passId);
			JSONObject signJo=new JSONObject(content);
			
			String sign=signJo.getJSONObject("data").getString("signature");
			//上传图片
			String storeUrl=signJo.getJSONObject("data").getString("url");
			JSONObject param = new JSONObject();
			param.put("upload_sign", sign);
			param.put("image", "data:image/png;base64,"+CopyToPddAction.GetImageStrFromPath(filePath).replace("\\r","").replace("\\n",""));
			System.out.println(param.toString());
			String result=CopyToPddAction.sendPost(storeUrl, CopyToPddAction.REQ_TYPE_JSON, CopyToPddAction.Referer_URL, CopyToPddAction.UA,"PASS_ID="+passId, param);
			return new JSONObject(result).getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
		
		
	}

}
