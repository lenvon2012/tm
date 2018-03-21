package controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import proxy.CommonProxyPools;

import org.elasticsearch.common.mvel2.optimizers.impl.refl.nodes.ArrayLength;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.JsonUtil;
import com.dbt.commons.Params.Comm;

import play.mvc.Controller;
import proxy.HttphostWrapper;

public class Proxy extends Controller {
	private static final Logger log = LoggerFactory.getLogger(Proxy.class);

	public static final String TAG = "Proxy";
	
	public static void getMainLandProxiesSize() {
		renderText("CommonProxyPools mainland size = " + CommonProxyPools.mainland.size());
	}
	
	public static void popHttpProxy() {
		if(CommonProxyPools.mainland.size() > 0) {
			renderJSON(JsonUtil.getJson(CommonProxyPools.provideWrapper()));
		}
		renderText("当前代理池为空");
	}
	
	public static void popHttpProxy(int size) {
		if(size <= 0) {
			size = 1;
		}
		int popSize = size;
		if(CommonProxyPools.mainland.size() == 0) {
			renderText("当前代理池为空");
		}
		if(CommonProxyPools.mainland.size() < size) {
			popSize = CommonProxyPools.mainland.size();
		}
		if(popSize > 0) {
			List<HttphostWrapper> hosts = new ArrayList<HttphostWrapper>();
			for(int i = 0; i < popSize; i++) {
				hosts.add(CommonProxyPools.provideWrapper());
			}
			renderJSON(JsonUtil.getJson(hosts));
		}
		if(CommonProxyPools.mainland.size() == 0) {
			renderText("当前代理池为空");
		}
	}
	
	public static void getCommonProxyPoolSize() {
    	renderJSON(JsonUtil.getJson(CommonProxyPools.mainland.size()));
    }
    
    public static void restartPanzhigao() {
    	CommonProxyPools.panzhigao_shutDownAt = 0L;
    	renderText("重启成功");
    }
    
    public static void restartDaili71() {
    	CommonProxyPools.daili71_shutDownAt = 0L;
    	renderText("重启成功");
    }
    
    public static void getPanzhidaoDeadTime() {
    	renderJSON(JsonUtil.getJson(new Date(CommonProxyPools.panzhigao_shutDownAt)));
    }
    
    public static void getDaili71DeadTime() {
    	renderJSON(JsonUtil.getJson(new Date(CommonProxyPools.daili71_shutDownAt)));
    }
}
