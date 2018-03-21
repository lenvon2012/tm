
package bustbapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.vas.ArticleBizOrderPlay;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.ArticleBizOrder;
import com.taobao.api.domain.ArticleSub;
import com.taobao.api.domain.ArticleUserSubscribe;
import com.taobao.api.request.VasOrderSearchRequest;
import com.taobao.api.request.VasSubscSearchRequest;
import com.taobao.api.request.VasSubscribeGetRequest;
import com.taobao.api.response.VasOrderSearchResponse;
import com.taobao.api.response.VasSubscSearchResponse;
import com.taobao.api.response.VasSubscribeGetResponse;

import configs.TMConfigs.PageSize;
import dao.UserDao;

public class VasApis {
    public static Long PAGE_SIZE = 100L;

    public static final Logger log = LoggerFactory.getLogger(VasApis.class);

    public static class SubscribeGet extends
            TBApi<VasSubscribeGetRequest, VasSubscribeGetResponse, List<ArticleUserSubscribe>> {

        String nick = null;

        String articleCode = null;

        public SubscribeGet(String nick, String articleCode) {
            super();
            this.nick = nick;
            this.articleCode = articleCode;
        }

        @Override
        public VasSubscribeGetRequest prepareRequest() {
            VasSubscribeGetRequest req = new VasSubscribeGetRequest();
            req.setNick(nick);
            req.setArticleCode(articleCode);
            return req;
        }

        @Override
        public List<ArticleUserSubscribe> validResponse(VasSubscribeGetResponse resp) {
            if (resp == null) {
                log.warn("No result return!!!");
            }

            ErrorHandler.validTaoBaoResp(resp);

            return resp.getArticleUserSubscribes() == null ? ListUtils.EMPTY_LIST : resp.getArticleUserSubscribes();
        }

        @Override
        public List<ArticleUserSubscribe> applyResult(List<ArticleUserSubscribe> res) {
            return res;
        }
    }

    public static class OrderSearch extends TBApi<VasOrderSearchRequest, VasOrderSearchResponse, List<ArticleBizOrder>> {

        String articleCode;

        String itemCode = null;

        String nick = null;

        Long startCreated = null;

        Long endCreated = null;

        Long bizType = null;

        public static Long pageSize = 100L;

        Long pageNo = 1L;

        public boolean hasInit = false;

        public OrderSearch(String articleCode, Long startCreated, Long endCreated) {
            this(articleCode, null, null, startCreated, endCreated, null);
        }

        public OrderSearch(String articleCode, String itemCode, String nick, Long startCreated, Long endCreated,
                Long bizType) {
            super();
            this.articleCode = articleCode;
            this.itemCode = itemCode;
            this.nick = nick;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.bizType = bizType;
        }

        @Override
        public VasOrderSearchRequest prepareRequest() {

            VasOrderSearchRequest req = new VasOrderSearchRequest();
            req.setArticleCode(articleCode);
            log.info("ArticleCode:" + articleCode);
            if (itemCode != null) {
                req.setItemCode(itemCode);
            }
            if (nick != null) {
                req.setNick(nick);
            }
            if (startCreated != null) {
                req.setStartCreated(new Date(startCreated));
            }
            if (endCreated != null) {
                req.setEndCreated(new Date(endCreated));
            }
            if (bizType != null) {
                req.setBizType(bizType);
            }

            req.setPageNo(pageNo++);
            req.setPageSize(pageSize);
            return req;
        }

        @Override
        public List<ArticleBizOrder> validResponse(VasOrderSearchResponse resp) {
            if (resp == null) {
                log.warn("No resp return!!!");
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (!hasInit) {
                long totalResult = resp.getTotalItem();

                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, PageSize.API_ITEM_PAGE_SIZE) - 1;
                this.hasInit = true;
            }

            return resp.getArticleBizOrders();
        }

        @Override
        public List<ArticleBizOrder> applyResult(List<ArticleBizOrder> res) {
            if (CommonUtils.isEmpty(res)) {
                return null;
            }

            for (ArticleBizOrder order : res) {
                String nick = order.getNick();
                models.user.User tbUser = UserDao.findByUserNick(nick);
//                User tbUser = new UserGetApi(null, nick).call();
//                Shop tbUser = new ShopGet(nick).call();
                new ArticleBizOrderPlay(order, tbUser != null ? tbUser.getLevel() : 0L).jdbcSave();
            }
            return res;
        }
    }

    public static class OrderSearchDayTotalNum extends TBApi<VasOrderSearchRequest, VasOrderSearchResponse, Long> {

        String articleCode;

        Long startCreated = null;

        Long endCreated = null;

        public OrderSearchDayTotalNum(String articleCode, Long startCreated, Long endCreated) {
            super();
            this.articleCode = articleCode;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
        }

        @Override
        public VasOrderSearchRequest prepareRequest() {

            VasOrderSearchRequest req = new VasOrderSearchRequest();
            req.setArticleCode(articleCode);
            // log.info("ArticleCode:" + articleCode);

            if (startCreated != null) {
                req.setStartCreated(new Date(startCreated));
            }
            if (endCreated != null) {
                req.setEndCreated(new Date(endCreated));
            }

            req.setPageNo(1L);
            req.setPageSize(1L);
            return req;
        }

        @Override
        public Long validResponse(VasOrderSearchResponse resp) {
            if (resp == null) {
                log.warn("No resp return!!!");
            }
            ErrorHandler.validTaoBaoResp(resp);

            // log.warn(resp.getBody());

            return resp.getTotalItem();
        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }
    }

    public static class OrderSearchPage extends
            TBApi<VasOrderSearchRequest, VasOrderSearchResponse, List<ArticleBizOrder>> {

        String articleCode;

        Long startCreated = null;

        Long endCreated = null;

        Long pageNo = 1L;

        public OrderSearchPage(String articleCode, Long startCreated, Long endCreated, Long pageNo) {
            super();
            this.articleCode = articleCode;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.pageNo = pageNo;
        }

        @Override
        public VasOrderSearchRequest prepareRequest() {

            VasOrderSearchRequest req = new VasOrderSearchRequest();
            req.setArticleCode(articleCode);
            // log.info("ArticleCode:" + articleCode);

            if (startCreated != null) {
                req.setStartCreated(new Date(startCreated));
            }
            if (endCreated != null) {
                req.setEndCreated(new Date(endCreated));
            }
            req.setPageNo(pageNo);
            req.setPageSize(PAGE_SIZE);
            return req;
        }

        @Override
        public List<ArticleBizOrder> validResponse(VasOrderSearchResponse resp) {
            if (resp == null) {
                log.warn("No resp return!!!");
            }
            ErrorHandler.validTaoBaoResp(resp);

            return resp.getArticleBizOrders();
        }

        @Override
        public List<ArticleBizOrder> applyResult(List<ArticleBizOrder> res) {
            return res;
        }
    }

    public static class SubscSearch extends TBApi<VasSubscSearchRequest, VasSubscSearchResponse, List<ArticleSub>> {

        public static Long pageSize = 100L;

        String articleCode;

        String itemCode = null;

        String nick = null;

        Long startDeadline = null;

        Long endDeadline = null;

        long status;

        Long pageNo = 1L;

        boolean hasInit = false;

        List<ArticleSub> result = new ArrayList<ArticleSub>();

        public SubscSearch(String articleCode, String nick) {
            this(articleCode, null, nick, System.currentTimeMillis() - (12 * 30 * DateUtil.DAY_MILLIS),
                    System.currentTimeMillis(), 0L);
        }
        public SubscSearch(String articleCode, Long startDeadline, Long endDeadline, long status) {
            this(articleCode, null, null, startDeadline, endDeadline, status);
        }

        public SubscSearch(String articleCode, String itemCode, String nick, Long startDeadline, Long endDeadline,
                long status) {
            super();

            this.articleCode = articleCode;
            this.itemCode = itemCode;
            this.nick = nick;
            this.startDeadline = startDeadline;
            this.endDeadline = endDeadline;
            this.status = status;
        }

        @Override
        public VasSubscSearchRequest prepareRequest() {

            VasSubscSearchRequest req = new VasSubscSearchRequest();
            req.setArticleCode(articleCode);
            log.info("ArticleCode:" + articleCode);
            if (itemCode != null) {
                req.setItemCode(itemCode);
            }
            if (nick != null) {
                req.setNick(nick);
            }
            if (startDeadline != null) {
                req.setStartDeadline(new Date(startDeadline));
            }
            if (endDeadline != null) {
                req.setEndDeadline(new Date(endDeadline));
            }
            req.setStatus(status);

            req.setPageNo(pageNo++);
            req.setPageSize(pageSize);
            return req;
        }

        @Override
        public List<ArticleSub> validResponse(VasSubscSearchResponse resp) {
            if (resp == null) {
                log.warn("No resp return!!!");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);

            if (!hasInit) {
                long totalResult = resp.getTotalItem();

                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, pageSize) - 1;
                this.hasInit = true;
            }
            log.error("-------------" + resp.getTotalItem());
            return resp.getArticleSubs();
        }

        @Override
        public List<ArticleSub> applyResult(List<ArticleSub> res) {
            if (CommonUtils.isEmpty(res)) {
                return result;
            }

//            for (ArticleSub articleSub : res) {
//                List<ArticleUserSubscribe> subscribes = new SubscribeGet(articleSub.getNick(), articleCode).call();
//                if (!CommonUtils.isEmpty(subscribes)) {
//                    articleSub.setStatus(Long.valueOf(AritcleSubPlay.STATUS.VAILD));
//                }
//                new AritcleSubPlay(articleSub).save();
//            }
//            // result.addAll(res);

            log.error("-----------------------" + res.size() + "---------" + result.size());
//            return result;
            return res;
        }
    }

}
