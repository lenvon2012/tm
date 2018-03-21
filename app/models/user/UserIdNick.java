
package models.user;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.GenericModel;
import proxy.CommonProxyPools;
import proxy.HttphostWrapper;
import proxy.NewProxyTools;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.JsoupUtil;
import utils.intervalTran;
import actions.JavaEscape;
import actions.catunion.UserIdNickAction.BuyerIdApi;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.api.SimpleHttpApi;

import controllers.newAutoTitle;

@Entity(name = UserIdNick.TABLE_NAME)
public class UserIdNick extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserIdNick.class);

    @Transient
    public static final String TABLE_NAME = "idnick";

    @Transient
    public static UserIdNick EMPTY = new UserIdNick();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    
    @PolicySQLGenerator.CodeNoUpdate
    @Index(name = "userid")
    private Long userid;
    
    @Id
    @Index(name = "nick")
    private String nick;

    @Column(columnDefinition = "varchar(255) default null")
    private String encodedId;

    /**
     * "http://member1.taobao.com/member/user-profile-6057db552edf2dfa5938551b328d30db.htm?spm=a1z08.6.1000507.1.ABZYdf
     */
    @Column(columnDefinition = "varchar(127) default null")
    String hash;
    
    public UserIdNick() {

    }

    public UserIdNick(Long userid, String nick) {
        super();
        this.userid = userid;
        this.nick = nick;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "userid";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "userid";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableHashKey()
     */
    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableName()
     */
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    static String EXIST_ID_QUERY = "select userid from " + TABLE_NAME + " where  userid = ? ";

    public static long findExistId(long userid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userid);
    }
    
    static String EXIST_NICK_QUERY = "select 1 from " + TABLE_NAME + " where  nick = ? ";

    public static long findExist(String nick) {
        return JDBCBuilder.singleLongQuery(EXIST_NICK_QUERY, nick);
    }
    
    static String EXIST_NICK_ID = "select userid from " + TABLE_NAME + " where  nick = ? ";

    public static long findIdByExistedNick(String nick) {
        return JDBCBuilder.singleLongQuery(EXIST_NICK_ID, nick);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExist(this.nick);

            // 没有记录， 则插入
            if (existdId == 0L) {
                return this.rawInsert();
            } 
            // 有记录，判断是不是被淘宝拉黑的用户
            else {
            	long userid = findIdByExistedNick(this.nick);
            	log.info("UserIdNick already existed for buyer " + this.nick + " and userid = " + userid);
            	// 这里userid可能为0，表明是被淘宝拉黑的用户
                setId(userid);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {
        // TODO Auto-generated method stub
        long id = dp.insert("insert into `" + TABLE_NAME + "`(`userid`,`nick`,`encodedId`,`hash`) values(?,?,?,?)",
                this.userid, this.nick, this.encodedId, this.hash);

        if (id > 0L) {
            log.info("insert ts for the first time !" + userid);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.userid);

            return false;
        }
    }

    public boolean rawUpdate() {
    	if(userid == null || userid < 0) {
    		userid = 0L;
    	}
        long updateNum = dp.insert(
                "update `" + TABLE_NAME + "` set  `userid` = ?, `hash` = ?, `encodedId`=?" +
                		" where `nick` = ? ", this.userid, this.hash, this.encodedId, 
                this.nick);

        if (updateNum > 0L) {
            log.info("update ts success! " + userid);
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.userid);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.userid = id;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return userid;
    }

    public static UserIdNick findByNick(String nick) {
        String sql = "select userid, nick, encodedId, hash from " + TABLE_NAME + " where nick = ?";

        UserIdNick userIdNick = new JDBCExecutor<UserIdNick>(dp, sql, nick) {
            @Override
            public UserIdNick doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    UserIdNick tempObj = parseUserIdNick(rs);
                    return tempObj;
                } else
                    return null;
            }
        }.call();

        return userIdNick;
    }

    public static List<UserIdNick> findByNickList(List<String> nickList) {
        if (CommonUtils.isEmpty(nickList))
            return new ArrayList<UserIdNick>();
        String sql = "select userid, nick, encodedId, hash from " + TABLE_NAME + " where nick in ";

        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String nick : nickList) {
            if (index > 0)
                sb.append(",");
            sb.append("\"" + nick + "\"");
            index++;
        }
        sql += "(" + sb.toString() + ") ";

        //log.error(sql);
        List<UserIdNick> userIdNickList = new JDBCExecutor<List<UserIdNick>>(dp, sql) {
            @Override
            public List<UserIdNick> doWithResultSet(ResultSet rs) throws SQLException {
                List<UserIdNick> tempList = new ArrayList<UserIdNick>();
                while (rs.next()) {
                    UserIdNick tempObj = parseUserIdNick(rs);
                    if (tempObj != null)
                        tempList.add(tempObj);
                }
                return tempList;
            }
        }.call();

        return userIdNickList;
    }

    public static UserIdNick findByUserId(long userId) {
        String sql = "select userid, nick, encodedId, hash  from " + TABLE_NAME + " where userid = ?";

        UserIdNick userIdNick = new JDBCExecutor<UserIdNick>(dp, sql, userId) {
            @Override
            public UserIdNick doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    UserIdNick tempObj = parseUserIdNick(rs);
                    return tempObj;
                } else
                    return null;
            }
        }.call();

        return userIdNick;
    }

    private static UserIdNick parseUserIdNick(ResultSet rs) {
        try {
            UserIdNick userIdNick = new UserIdNick();
            long userId = rs.getLong(1);
            userIdNick.setId(userId);
            String nick = rs.getString(2);
            userIdNick.setNick(nick);
            userIdNick.setEncodedId(rs.getString(3));
            String hash = rs.getString(4);
            userIdNick.setHash(hash);
            
            return userIdNick;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public String getEncodedId() {
        return encodedId;
    }

    public void setEncodedId(String encodedId) {
        this.encodedId = encodedId;
    }

    public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public static String UserProfilePre = "http://member1.taobao.com/member/user_profile.jhtml?spm=a1z09.1.11.11.OqV7ib&userID=";
	public static String TDKCALLBACK = "jQuery17103840943349059671_1416556457313";
	public static String TDKVipIndex = "taodake_img\\/Images\\/Taobao\\/vip";
	public static Random random = new Random();
	
	public String genTDKRateUrl() {
		String url = StringUtils.EMPTY;
		try {
			if(random.nextInt(100) > 50) {
				url = "http://wwwsoso001.taodake.com/taobao_data.php?callback=" + TDKCALLBACK + 
					"&nick=" + URLEncoder.encode(JavaEscape.escape(this.nick), "UTF-8") + "&chkid=0&click=373635346667686A63786365&_=" + System.currentTimeMillis();
			} else {
				url = "http://wwwsoso002.taodake.com/taobao_data.php?callback=" + TDKCALLBACK + 
						"&nick=" + URLEncoder.encode(JavaEscape.escape(this.nick), "UTF-8") + "&chkid=0&click=373635346667686A63786365&_=" + System.currentTimeMillis();
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(StringUtils.isEmpty(url)) {
			return null;
		}
		
		String webContent = StringUtils.EMPTY;
        try {
			for(int i = 0; i < 3; i++) {
				if(random.nextInt(100) > 50) {
					webContent = SimpleHttpApi.webContent(url, "http://www.taobao.com", 2);
				} else {
					HttphostWrapper wrapper = NewProxyTools.getHttphostWrapper();
					if(wrapper == null) {
						continue;
					}
					webContent = API.directGet(url, "http://www.taodake.com/", null, wrapper.getHttphost(), null);
				}

	        	if(!StringUtils.isEmpty(webContent) && webContent.indexOf("vip") > 0) {
	        		break;
	        	}
	        }
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        if(StringUtils.isEmpty(webContent) || webContent.indexOf("vip") <= 0) {
    		return null;
    	}
        
        int index = webContent.indexOf(TDKVipIndex);
        if(index > 0) {
        	String targetUrl = updateUserIdNickByTDK(this.nick, webContent);
        	if(!StringUtils.isEmpty(targetUrl)) {
        		return targetUrl;
        	}

        }
        return null;
	}
	
	public static String ChaXiaohaoIndex = "http://www.chaxiaohao.net/rate-";
	public static int ChaXiaohaoIndexLength = ChaXiaohaoIndex.length();
	public static String analysisHashFromChaXiaohao(String msg) {
		if(StringUtils.isEmpty(msg)) {
			return null;
		}
		if(msg.indexOf("maijiaxinxi") <= 0) {
			return null;
		}
		int start = msg.indexOf(ChaXiaohaoIndex);
		if(start <= 0) {
			return null;
		}
		int end = msg.indexOf(".html", start);
		if(end <= 0) {
			return null;
		}
		String hash = msg.substring(start + ChaXiaohaoIndexLength, end);
		return hash;
	}
	
	public static String TaodengjiIndex = "http://rate.taobao.com/user-rate-";
	public static int TaodengjiIndexLength = TaodengjiIndex.length();
	public static String analysisHashFromTaodengji(String msg) {
		if(StringUtils.isEmpty(msg)) {
			return null;
		}
		if(msg.indexOf("http://rate.taobao.com/user-rate-") <= 0) {
			return null;
		}
		int start = msg.indexOf(TaodengjiIndex);
		if(start <= 0) {
			return null;
		}
		int end = msg.indexOf(".htm", start);
		if(end <= 0) {
			return null;
		}
		String hash = msg.substring(start + TaodengjiIndexLength, end);
		return hash;
	}
	
	public String genChaXiaohaoRateUrl() {
		String userProfileUrl = StringUtils.EMPTY;
        try {
			userProfileUrl = "http://www.chaxiaohao.net/ajax/get/rate/?username=" + URLEncoder.encode(this.nick, "utf8") +
					"&_t=" + System.currentTimeMillis();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(StringUtils.isEmpty(userProfileUrl)) {
        	log.info("URLEncoder.encode for nick [" + this.nick + "] is fucking wrong!!!");
        	return null;
        }
        
        String webContent = StringUtils.EMPTY;
        try {
        	 webContent = SimpleHttpApi.webContent(userProfileUrl, "http://www.chaxiaohao.net/", 5);

             if(StringUtils.isEmpty(webContent) || webContent.indexOf("maijiaxinxi") < 0) {
             	//log.info("API.directGet for buildUserHashByNick result is null!!!!!!!!");
             	webContent = API.directGet(userProfileUrl, "http://www.chaxiaohao.net/", null);
             }
		} catch (Exception e) {
			// TODO: handle exception
		}
        String hash = analysisHashFromTaodengji(webContent);
        if(StringUtils.isEmpty(hash)) {
        	return null;
        }
        this.hash = hash;
    	this.jdbcSave();
    	return "http://rate.taobao.com/user-rate-" + this.hash + ".htm";
	}
	
	public String genTaodengjiRateUrl() {
		String userProfileUrl = StringUtils.EMPTY;
        try {
			userProfileUrl = "http://www.taodengji.com/Server/Taobao/User.ashx?mark=1&key=" + URLEncoder.encode(this.nick, "utf8") +
					"&_t=" + System.currentTimeMillis();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(StringUtils.isEmpty(userProfileUrl)) {
        	log.info("URLEncoder.encode for nick [" + this.nick + "] is fucking wrong!!!");
        	return null;
        }
        
        String webContent = StringUtils.EMPTY;
        try {
        	 webContent = SimpleHttpApi.webContent(userProfileUrl, "http://www.taodengji.com/", 5);

             if(StringUtils.isEmpty(webContent) || webContent.indexOf("http://rate.taobao.com/user-rate-") < 0) {
             	//log.info("API.directGet for buildUserHashByNick result is null!!!!!!!!");
             	webContent = API.directGet(userProfileUrl, "http://www.taodengji.com/", null);
             }
		} catch (Exception e) {
			// TODO: handle exception
		}
        String hash = analysisHashFromTaodengji(webContent);
        if(StringUtils.isEmpty(hash)) {
        	return null;
        }
        this.hash = hash;
    	this.jdbcSave();
    	return "http://rate.taobao.com/user-rate-" + this.hash + ".htm";
	}
	
	public String genRateUrl() {
        if (!StringUtils.isEmpty(this.encodedId)) {
            return "http://rate.taobao.com/rate.htm?user_id=" + this.encodedId;
        }
        if (!StringUtils.isEmpty(this.hash)) {
            return "http://rate.taobao.com/user-rate-" + this.hash + ".htm";
        }

        //return genTDKRateUrl(); // 淘大客封我jbt10的ip。。wencontent的ip也被封了。。至于吗。。
        //return genChaXiaohaoRateUrl(); // www.chaxiaohao.net挂了一天了。。
        return genTaodengjiRateUrl(); // http://www.taodengji.com/
        
        /*String userProfileUrl = StringUtils.EMPTY;
        try {
			userProfileUrl = UserProfilePre + URLEncoder.encode(this.nick, "gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(StringUtils.isEmpty(userProfileUrl)) {
        	log.info("URLEncoder.encode for nick [" + this.nick + "] is fucking wrong!!!");
        	return null;
        }
        
        String webContent = StringUtils.EMPTY;
        try {
        	 webContent = API.directGet(userProfileUrl, "http://www.taobao.com", null);

             if(StringUtils.isEmpty(webContent) || (webContent.indexOf("J_TShopSummary") < 0 && webContent.indexOf("SellerInfo") < 0)) {
             	//log.info("API.directGet for buildUserHashByNick result is null!!!!!!!!");
             	webContent = SimpleHttpApi.webContent(userProfileUrl, "http://www.taobao.com", 5);
             }
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        if(StringUtils.isEmpty(webContent) || (webContent.indexOf("J_TShopSummary") < 0 && webContent.indexOf("SellerInfo") < 0)) {
        	//log.info("find UserProfilePre for nick [" + this.nick + "] is fucking wrong!!");
        	return null;
        }
        Document doc = JsoupUtil.parseJsoupDocument(webContent);
        if(doc == null) {
        	return null;
        }
        
        if(webContent.indexOf("J_TShopSummary") > 0) {
        	// 这个是卖家，能直接拿到encodedId的
            String encodedId = anaLysisEncodedIdFromJTShopSummary(doc);
            if(!StringUtils.isEmpty(encodedId)) {
            	this.encodedId = encodedId;
            	this.jdbcSave();
            	return "http://rate.taobao.com/rate.htm?user_id=" + this.encodedId;
            }
        } else if(webContent.indexOf("SellerInfo") > 0) {
        	// 这个是买家，拿hash
        	String hash = anaLysisHashFromSellerInfo(doc);
        	if(!StringUtils.isEmpty(hash)) {
        		this.hash = hash;
            	this.jdbcSave();
            	return "http://rate.taobao.com/user-rate-" + this.hash + ".htm";
        	}
            
        }*/
    }
	
	public static String updateUserIdNickByTDK(String realWangWang,
			String vipString) {
		UserIdNick idNick = new UserIdNick(0L, realWangWang);
		vipString = vipString.replace(TDKCALLBACK + "(", StringUtils.EMPTY);
		vipString = vipString.substring(0, vipString.length() - 1);
		if (vipString.startsWith("{")) {
			try {
				JSONObject object = new JSONObject(vipString);
				String hash = object.getString("d");
				if (!StringUtils.isEmpty(hash)) {
					idNick.setHash(hash);
				}
				String encodedId = object.getString("c");
				if (!StringUtils.isEmpty(encodedId)) {
					idNick.setEncodedId(encodedId);
				}
				idNick.jdbcSave();
				if (!StringUtils.isEmpty(idNick.encodedId)) {
					return "http://rate.taobao.com/rate.htm?user_id="
							+ idNick.encodedId;
				}
				if (!StringUtils.isEmpty(idNick.hash)) {
					return "http://rate.taobao.com/user-rate-" + idNick.hash
							+ ".htm";
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String anaLysisEncodedIdFromJTShopSummary(Document doc) {
    	if(doc == null) {
        	return null;
        }
    	Elements goldLog = doc.select("a.J_TGoldlog.mini-dsr");
        if(goldLog == null || goldLog.size() <= 0) {
        	return null;
        }
        for(Element element : goldLog) {
        	if(element == null) {
        		continue;
        	}
        	String href = element.attr("href");
        	if(StringUtils.isEmpty(href)) {
        		continue;
        	}
        	String encodedId = href.substring("http://rate.taobao.com/user-rate-".length(), href.indexOf(".htm"));
        	if(StringUtils.isEmpty(encodedId)) {
        		continue;
        	}
        	return encodedId;
        }
        return null;
    }
    
    public static String anaLysisHashFromSellerInfo(Document doc) {
    	if(doc == null) {
        	return null;
        }
    	 Element sellerInfo = doc.getElementById("SellerInfo");
         if(sellerInfo == null) {
         	return null;
         }
         Elements anchors = sellerInfo.select("ul.TabBarLevel1 li a");
         if(anchors == null || anchors.size() <= 0) {
         	return null;
         }
         String hash = StringUtils.EMPTY;
         for(Element anchor : anchors) {
         	if(anchor == null) {
         		continue;
         	}
         	String href = anchor.attr("href");
         	if(StringUtils.isEmpty(href)) {
         		continue;
         	}
         	if(href.indexOf("http://member1.taobao.com/member/user-profile-") >= 0) {
         		hash = href.substring("http://member1.taobao.com/member/user-profile-".length(), 
         				href.indexOf(".htm"));
         		if(!StringUtils.isEmpty(hash)) {
         			return hash;
         		}
         	} else if(href.indexOf("http://rate.taobao.com/user-rate-") >= 0) {
         		hash = href.substring("http://rate.taobao.com/user-rate-".length(), 
         				href.indexOf(".htm"));
         		if(!StringUtils.isEmpty(hash)) {
         			return hash;
         		}
         	}
         }
    	return null;
    }
	
	public static UserIdNick findOrCreate(String userNick) {
        UserIdNick model = findByNick(userNick);
        if (model != null) {
            return model;
        }

        model = new UserIdNick(0L, userNick);
        /*Long buyerId = BuyerIdApi.findBuyerId(model.nick);
        if(buyerId != null && buyerId > 0) {
        	 model.setId(buyerId);
        } else {
        	//log.info("findOrCreate UserIdNick for buyer : " + userNick + " but buyerid = 0");
        }*/
        String url = model.genRateUrl();

        return model;
    }
}
