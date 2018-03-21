/**
 * 
 */
package jdapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.jd.JDItemPlay;
import models.jd.JDUser;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.jd.open.api.sdk.domain.ware.Sku;
import com.jd.open.api.sdk.domain.ware.Ware;
import com.jd.open.api.sdk.request.ware.WareInfoByInfoRequest;
import com.jd.open.api.sdk.request.ware.WareListRequest;
import com.jd.open.api.sdk.response.ware.WareInfoByInfoSearchResponse;
import com.jd.open.api.sdk.response.ware.WareListResponse;

/**
 * @author navins
 * 
 */
public class JDItemApi {

    public final static Logger log = LoggerFactory.getLogger(JDItemApi.class);

    public final static String FIELDS = "ware_id,spu_id,outer_id";

    public final static int PAGE_SIZE = 10;

    public static class WareListSearch extends
            JDApi<WareInfoByInfoRequest, WareInfoByInfoSearchResponse, List<JDItemPlay>> {

        public JDUser user;

        public Long pn = 1L;

        public List<JDItemPlay> resList = new ArrayList<JDItemPlay>();

        public WareListSearch(String sid) {
            super(sid);
        }

        public WareListSearch(JDUser user) {
            super(user.getAccessToken());
            this.user = user;
            this.resList = new ArrayList<JDItemPlay>();
        }

        @Override
        public WareInfoByInfoRequest prepareRequest() {
            WareInfoByInfoRequest wareInfoByInfoRequest = new WareInfoByInfoRequest();
            wareInfoByInfoRequest.setPage(String.valueOf(pn));
            wareInfoByInfoRequest.setPageSize(String.valueOf(PAGE_SIZE));
            // wareInfoByInfoRequest.setEndModified("2013-10-22");
            // wareInfoByInfoRequest.setFields(FIELDS);

            return wareInfoByInfoRequest;
        }

        @Override
        public List<JDItemPlay> validResponse(WareInfoByInfoSearchResponse resp) {
            // System.out.println(resp.getMsg());
            if (!StringUtils.equals(resp.getCode(), "0")) {
                log.error("validResponse error req code !");
                return null;
            }
            List<Ware> wareInfos = resp.getWareInfos();
            if (CommonUtils.isEmpty(wareInfos)) {
                return ListUtils.EMPTY_LIST;
            }
            
            log.info("[WareListSearch]update ware : userId=" + user.getId() + " pn=" + pn);
            
            List<JDItemPlay> list = new ArrayList<JDItemPlay>();
            HashSet<Long> idSet = new HashSet<Long>();
            for (Ware ware : wareInfos) {
                list.add(new JDItemPlay(user.getId(), ware));
                idSet.add(ware.getWareId());
            }

            HashMap<Long, Long> idSkuMap = new WareListGetSku(user, idSet).call();

            for (JDItemPlay ware : list) {
                Long skuId = idSkuMap.get(ware.getNumIid());
                if (skuId == null) {
                    skuId = 0L;
                }
                ware.setSkuId(skuId);
            }

            if (resp.getTotal() >= pn * PAGE_SIZE) {
                iteratorTime = 1;
                pn++;
            }

            return list;
        }

        @Override
        public List<JDItemPlay> applyResult(List<JDItemPlay> res) {
            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        }

    }

    public static class WareListGetSku extends JDApi<WareListRequest, WareListResponse, HashMap<Long, Long>> {

        public JDUser user;

        public HashMap<Long, Long> idSkuMap;

        public String ids;

        public WareListGetSku(String sid) {
            super(sid);
        }

        public WareListGetSku(JDUser user, Set<Long> idSet) {
            super(user.getAccessToken());
            this.user = user;
            this.idSkuMap = new HashMap<Long, Long>();
            this.ids = StringUtils.join(idSet, ",");
        }

        @Override
        public WareListRequest prepareRequest() {
            WareListRequest wareListRequest = new WareListRequest();
            wareListRequest.setWareIds(ids);
            wareListRequest.setFields("ware_id,skus");
            return wareListRequest;
        }

        @Override
        public HashMap<Long, Long> validResponse(WareListResponse resp) {
            if (!StringUtils.equals(resp.getCode(), "0")) {
                log.error("validResponse error req code !");
                return null;
            }
            List<Ware> wareList = resp.getWareList();
            if (CommonUtils.isEmpty(wareList)) {
                return idSkuMap;
            }
            for (Ware ware : wareList) {
                if (!CommonUtils.isEmpty(ware.getSkus())) {
                    Sku sku = ware.getSkus().get(0);
                    if (sku != null) {
                        Long skuId = ware.getSkus().get(0).getSkuId();
                        idSkuMap.put(ware.getWareId(), skuId);
                    }
                }
            }
            return idSkuMap;
        }

        @Override
        public HashMap<Long, Long> applyResult(HashMap<Long, Long> res) {
            return res;
        }

    }
}
