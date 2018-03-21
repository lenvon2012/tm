
package actions.industry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.industry.CatPropsIndustryBean;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.industry.RemoteIndustryGetAction.CatPNameResult;
import actions.industry.RemoteIndustryGetAction.CatVNameBaseBean;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.commons.ClientException;
import com.taobao.api.ApiException;

public class CatPropsIndustryAction {

    private static final Logger log = LoggerFactory.getLogger(CatPropsIndustryAction.class);

    public static List<CatPNameResult> findCatPNameList(Long cid) {

        try {
            List<CatPNameResult> pNameList = findCatProperties(cid, false);

            if (CommonUtils.isEmpty(pNameList)) {
                return new ArrayList<CatPNameResult>();
            }

            List<CatPNameResult> resultList = new ArrayList<CatPNameResult>();

            for (CatPNameResult pNameRes : pNameList) {
                if (pNameRes == null) {
                    continue;
                }
                List<CatVNameBaseBean> vNameBaseList = pNameRes.getvNameBaseList();
                if (CommonUtils.isEmpty(vNameBaseList)) {
                    continue;
                }
                //将vNameList置空
                pNameRes.setvNameBaseList(new ArrayList<CatVNameBaseBean>());
                resultList.add(pNameRes);
            }

            return resultList;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ArrayList<CatPNameResult>();
        }

    }

    public static List<CatVNameBaseBean> findCatVNameBaseList(Long cid, Long pid,
            final String orderBy, final boolean isDesc) {

        try {

            if (pid == null || pid <= 0L) {
                return new ArrayList<CatVNameBaseBean>();
            }

            List<CatPNameResult> pNameList = findCatProperties(cid, true);

            if (CommonUtils.isEmpty(pNameList)) {
                return new ArrayList<CatVNameBaseBean>();
            }

            List<CatVNameBaseBean> vNameBaseList = new ArrayList<CatVNameBaseBean>();
            for (CatPNameResult pNameRes : pNameList) {
                if (pNameRes == null) {
                    continue;
                }
                Long tempPid = pNameRes.getPid();
                if (pid.equals(tempPid)) {
                    vNameBaseList = pNameRes.getvNameBaseList();
                    break;
                }

            }

            if (CommonUtils.isEmpty(vNameBaseList)) {
                vNameBaseList = new ArrayList<CatVNameBaseBean>();
            }

            //排序
            Collections.sort(vNameBaseList, new Comparator<CatVNameBaseBean>() {

                @Override
                public int compare(CatVNameBaseBean o1, CatVNameBaseBean o2) {

                    int descResult = 0;
                    if ("pv".equals(orderBy)) {
                        descResult = o2.getPv() - o1.getPv();
                    }
                    if (isDesc == true) {
                        return descResult;
                    } else {
                        return descResult * -1;
                    }

                }

            });

            return vNameBaseList;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ArrayList<CatVNameBaseBean>();
        }

    }

    private static List<CatPNameResult> findCatProperties(Long cid, boolean isNeedVNameBase) throws ApiException,
            ClientException {
        if (cid == null || cid <= 0) {
            return new ArrayList<CatPNameResult>();
        }

        CatPropsIndustryBean propsBean = CatPropsIndustryBean.findByCatId(cid);
        List<CatPNameResult> pNameResList = new ArrayList<CatPNameResult>();

        if (propsBean != null) {
            pNameResList = getPNameResultsFromJson(cid, propsBean, isNeedVNameBase);
        }
        if (CommonUtils.isEmpty(pNameResList)) {
            pNameResList = getPNameResultFromApi(cid, propsBean, isNeedVNameBase);
        }

        return pNameResList;
    }

    //从数据库中获取属性
    private static List<CatPNameResult> getPNameResultsFromJson(Long cid, CatPropsIndustryBean propsBean,
            boolean isNeedVNameBase) throws ApiException, ClientException {

        if (propsBean == null) {
            return new ArrayList<CatPNameResult>();
        }

        String propsJson = propsBean.getPropsJson();
        if (StringUtils.isEmpty(propsJson)) {
            return new ArrayList<CatPNameResult>();
        }

        CatPNameResult[] pNameResArray = JsonUtil.toObject(propsJson, CatPNameResult[].class);

        if (pNameResArray == null) {
            log.error("fail to parse propsJson: " + propsJson + "-------------------");
            return new ArrayList<CatPNameResult>();
        }

        List<CatPNameResult> pNameResList = Arrays.asList(pNameResArray);

        if (isNeedVNameBase == false) {
            return pNameResList;
        }
        long updateTs = propsBean.getUpdateTs();
        boolean hasLoadedWordBase = propsBean.isLoadedWordBase();

        //一段时间没有更新，用api
        if (updateTs - System.currentTimeMillis() > DateUtil.DAY_MILLIS * 3) {
            return getPNameResultFromApi(cid, propsBean, isNeedVNameBase);
        }

        //刷新属性的wordbase
        if (hasLoadedWordBase == false) {
            RemoteIndustryGetAction.putVNameBaseInfo(cid, pNameResList);
            //更新数据库
            updateCatPropsIndustryBean(propsBean, cid, pNameResList, true);
        }

        return pNameResList;
    }

    //通过api获取类目属性
    private static List<CatPNameResult> getPNameResultFromApi(Long cid, CatPropsIndustryBean propsBean,
            boolean isNeedVNameBase) throws ApiException, ClientException {

        List<CatPNameResult> pNameResList = RemoteIndustryGetAction.getPNameResultFromApi(cid, isNeedVNameBase);

        if (CommonUtils.isEmpty(pNameResList)) {
            return new ArrayList<CatPNameResult>();
        }

        if (isNeedVNameBase == true) {
            //更新数据库
            updateCatPropsIndustryBean(propsBean, cid, pNameResList, true);
        } else {
            //更新数据库
            updateCatPropsIndustryBean(propsBean, cid, pNameResList, false);
        }

        return pNameResList;
    }

    //更新数据库
    private static void updateCatPropsIndustryBean(CatPropsIndustryBean propsBean,
            Long cid, List<CatPNameResult> pNameResList, boolean hasLoadedWordBase) {

        String propsJson = JsonUtil.getJson(pNameResList);

        if (propsBean == null) {
            propsBean = new CatPropsIndustryBean(cid);
        }

        propsBean.setPropsJson(propsJson);

        if (hasLoadedWordBase == true) {
            propsBean.setLoadedWordBase();
        } else {
            propsBean.removeLoadedWordBase();
        }

        propsBean.jdbcSave();
    }

}
