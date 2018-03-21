
package actions.catunion;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.user.UserIdNick;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.StatusCode;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import utils.JsoupUtil;
import actions.catunion.UserRateSpiderAction.UserRateInfo;

import com.ciaosir.client.api.API;
import com.ciaosir.client.api.SimpleHttpApi;
import com.ciaosir.client.utils.HttpClientUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.internal.util.StringUtils;

public class UserIdNickAction {
    private static final Logger log = LoggerFactory.getLogger(UserIdNickAction.class);

    public static String findNickById(long userId) {
        if (userId <= 0)
            return "";
        UserIdNick idNick = UserIdNick.findByUserId(userId);
        if (idNick == null) {
            UserRateInfo rateInfo = UserRateSpiderAction.spiderUserRateById(userId);
            if (rateInfo == null)
                return "";
            String userNick = rateInfo.getUserNick();
            if (StringUtils.isEmpty(userNick))
                return "";
            idNick = new UserIdNick();
            idNick.setNick(userNick);
            idNick.setUserid(userId);
            idNick.jdbcSave();

            return userNick;
        } else {
            return idNick.getNick();
        }

    }

    public static long findUserIdByNick(String nick) {
        if (StringUtils.isEmpty(nick)) {
            return 0;
        }
        UserIdNick idNick = UserIdNick.findByNick(nick);

        if (idNick == null) {
            log.error("can not find nick: " + nick + " from database");
            Long userId = findWithTaobao(nick);
            if (userId != null && userId.longValue() > 0) {
                idNick = new UserIdNick();
                idNick.setNick(nick);
                idNick.setUserid(userId);
                idNick.jdbcSave();
                log.error("find nick: " + nick + " from taobao, userId: " + userId);
                return userId.longValue();
            } else {
                log.error("can not find nick: " + nick + " from taobao");
                return 0;
            }
        } else {
            return idNick.getUserid();
        }

    }

    public static Long findWithTaobao(String nick) {
        if (StringUtils.isEmpty(nick)) {
            return 0L;
        }

        Long userId = BuyerIdApi.findBuyerId(nick);

        return userId;

        /*Map<String, Long> idNickMap = new HashMap<String, Long>();
        idNickMap.put("柠檬绿茶", 39512L);
        idNickMap.put("tb_676700", 544637663L);
        idNickMap.put("tb676700", 479522565L);

        return idNickMap.get(nick);*/
    }

    public static class BuyerIdApi {

        static String target = "http://my.taobao.com/homepage/ajax/get_user_info.json?user_id=";

        static String directApiTarget = "http://wwc.taobaocdn.com/avatar/getAvatar.do?userId=";
        
        public static Long spiderMyTaobaoUserId(String url) {
            String webContent = SimpleHttpApi.webContent(url, "http://my.taobao.com", 2);
            if(webContent == null) {
            	return 0L;
            }
            //log.info("web content length :" + webContent.length());
            int indexOf = webContent.indexOf(target);
            if (indexOf > 0) {
                int endIndex = webContent.indexOf("\"", indexOf + 1);
                String userIdStr = webContent.substring(indexOf + target.length(), endIndex);
                //log.info("[fetch userIdx str:]" + userIdStr);

                if (StringUtils.isNumeric(userIdStr)) {
                    return NumberUtil.parserLong(userIdStr, -1L);
                }
            }
            return null;

        }

        public static Long spiderMyTaobaoUserIdUseDirextAPI(String url, String nick) {
        	//log.info("spiderMyTaobaoUserIdUseDirextAPI for buyer " + nick);
        	String webContent = API.directGet(url, "http://my.taobao.com", null);
            int indexOf = webContent.indexOf(directApiTarget);
            if (indexOf > 0) {
                int endIndex = webContent.indexOf("&width", indexOf + 1);
                String userIdStr = webContent.substring(indexOf + directApiTarget.length(), endIndex);
                //log.info("[fetch userIdx str:]" + userIdStr);

                if (StringUtils.isNumeric(userIdStr)) {
                    return NumberUtil.parserLong(userIdStr, -1L);
                } else {
                	log.info("holy shit!! userIdStr [" + userIdStr + "] is not long");
                }
            } else {
            	//log.info(webContent);
            	log.info("can not find directApiTarget in Directget Html for user " + nick);
            }
            return null;

        }
        
        public static Long findBuyerId(String nick) {
            String userUrl = getUserInfoUrl(nick);
            if (StringUtils.isEmpty(userUrl)) {
                return null;
            }

            if (userUrl.startsWith("http://my.taobao.com/")) {
//                String idStr = userUrl.substring("http://my.taobao.com/".length());
//                if (StringUtils.isEmpty(idStr)) {
//                    return null;
//                }
//                long userId = Long.valueOf(idStr);
//                return userId;
                Long userId = spiderMyTaobaoUserId(userUrl);
                if(userId != null && userId > 0L) {
                	return userId;
                }
                userId = spiderMyTaobaoUserIdUseDirextAPI(userUrl, nick);
                if(userId != null && userId > 0L) {
                	return userId;
                }
                //log.info("can not find userid for buyer " + nick + " anyway!!!!");
                return null;
            }

            try {
                userUrl = removeBlank(userUrl);
                int lastIndex = userUrl.lastIndexOf("/front.htm");
                if (lastIndex <= 0)
                    return null;
                userUrl = userUrl.substring(0, lastIndex);
                String frontUrl = "http://i.taobao.com/u/";
                int startIndex = userUrl.lastIndexOf(frontUrl);
                if (startIndex < 0)
                    return null;
                String encodeId = userUrl.substring(startIndex + frontUrl.length());
                if (StringUtils.isEmpty(encodeId))
                    return null;
                //log.error(encodeId);
                byte[] idBytes = (new BASE64Decoder()).decodeBuffer(encodeId);
                String idStr = new String(idBytes);
                //log.error(idStr);
                long userId = 0L;
                try {
                    userId = Long.parseLong(idStr);
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
                return userId;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

            return null;

        }

        /*private static String getUserInfoUrl(String nick) {
            if (StringUtils.isEmpty(nick))
                return null;
            String encodeNick = "";
            try {
                encodeNick = (new BASE64Encoder()).encodeBuffer(nick.getBytes("gbk"));
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
            if (StringUtils.isEmpty(encodeNick))
                return null;
            try {
                String url = "http://jianghu.taobao.com//n/" + encodeNick + "/front.htm";
                url = removeBlank(url);
                //log.error(url);
                Document doc = JsoupUtil.loadHtmlDoc(url);
                if (doc == null)
                    return null;
                String userUrl = doc.baseUri();
                //log.error(userUrl);
                
                return userUrl;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            

            return null;
        }*/

        public static String getUserInfoUrl(String nick) {
            if (StringUtils.isEmpty(nick))
                return null;
            String encodeNick = "";
            try {
                encodeNick = (new BASE64Encoder()).encodeBuffer(nick.getBytes("gbk"));

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
            if (StringUtils.isEmpty(encodeNick))
                return null;
            try {
                String url = "http://jianghu.taobao.com//n/" + encodeNick + "/front.htm";
                url = removeBlank(url);
//                url = url.replaceAll("\n", "");
                //log.info("[target url :]" + url);
                //log.error(url);
                HttpResponse rsp = HttpClientUtil.loadResponse(url, UserRateSpiderAction.Default_Refer, false);
                //log.error(rsp.toString());
                if (rsp != null
                        && (rsp.getStatusLine().getStatusCode() == StatusCode.MOVED
                        || rsp.getStatusLine().getStatusCode() == StatusCode.FOUND)) {
                    Header[] headerArray = rsp.getHeaders("Location");
                    if (headerArray == null || headerArray.length == 0)
                        return null;
                    String userUrl = headerArray[0].getValue();
                    //log.error(userUrl);
                    return userUrl;
                } else
                    return null;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

            return null;

        }
        
        public static String getEncodeIdStr(String nick) {
            String url = getUserInfoUrl(nick);
            String encodeStr = url.substring(url.lastIndexOf("/")+1);
            return encodeStr;
        }

        private static String removeBlank(String str) {
            if (StringUtils.isEmpty(str))
                return "";
            String reg = "(\r\n|\n)+\\s*";
            Pattern p = Pattern.compile(reg);
            Matcher m = p.matcher(str);
            str = m.replaceAll("");

            return str;
        }
        
        /**
         * <div id="SellerInfo" data-spm="1000507" data-spm-max-idx="3">
        <ul class="TabBarLevel1">
            <li class="Selected"><a href="http://member1.taobao.com/member/user-profile-726db3ad63867814d44d8f6402014efe.htm?spm=a1z08.6.1000507.1.RWwybj" data-spm-anchor-id="a1z08.6.1000507.1">个人档案 </a></li>
        <li><a href="http://rate.taobao.com/user-rate-726db3ad63867814d44d8f6402014efe.htm?spm=a1z08.6.1000507.2.RWwybj" data-spm-anchor-id="a1z08.6.1000507.2">信用评价</a></li>
        <li><a href="http://jianghu.taobao.com//n/eHNzZXI=/front.htm?spm=a1z08.6.1000507.3.RWwybj" data-spm-anchor-id="a1z08.6.1000507.3">个人空间</a></li>
        </ul>
        <div class="HackBox"></div>
        </div>
         * @param nick
         * @return
         */
        static String memberUrl = "http://member1.taobao.com/member/user_profile.jhtml?user_id=";

        
        public static String buildUserHashByNick(String nick) {
            String hash = null;
            try {
//                UserIdNick info = new UserIdNick();
//                info.setNick(nick);
                String encodeNick = URLEncoder.encode(nick,"gbk");
                String targetUrl = memberUrl + encodeNick;
                log.info("[target  url]" + targetUrl);
                String webContent = SimpleHttpApi.webContent(targetUrl, "http://www.taobao.com", 3);
                Document doc = JsoupUtil.parseJsoupDocument(webContent);

                Element selerInfo = doc.getElementById("SellerInfo");
                Elements elems = selerInfo.getElementsByTag("a");
                for (Element element : elems) {
                    String href = element.attr("href");
                    log.info("[href:]" + href);
                    if (href.contains("http://member1.taobao.com/member/user-profile-")) {
                        int startIndex = href.indexOf("user-profile-") + "user-profile-".length();
                        int endIndex = href.indexOf(".htm", startIndex);
                        hash = href.substring(startIndex, endIndex);
                        log.info("[has:]" + hash);
//                        info.setHash(hash);
                        if (!StringUtils.isEmpty(hash)) {
                            return hash;
                        }
                    }
                    log.info("[href:]" + href);
                    if (href.contains("http://rate.taobao.com/user-rate")) {
                        int startIndex = href.indexOf("user-rate-") + "user-rate-".length();
                        int endIndex = href.indexOf(".htm", startIndex);
                        hash = href.substring(startIndex, endIndex);
//                        info.setHash(hash);
                        log.info("[hash]:" + hash);
                        if (!StringUtils.isEmpty(hash)) {
                            return hash;
                        }
                    }
                }

                return hash;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }
    }

    public static void main(String[] args) {
        BuyerIdApi.findBuyerId("clorest510");

        //Document doc = JsoupUtil.loadHtmlDoc("http://jianghu.taobao.com//n/Y2hpbmcwMDc4NQ==/front.htm");
        //log.error(doc.baseUri());
        //log.error(doc.text());
    }

}
