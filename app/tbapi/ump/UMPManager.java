
package tbapi.ump;

import models.user.User;
import bustbapi.TBApi;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.request.UmpActivityGetRequest;
import com.taobao.api.request.UmpToolGetRequest;
import com.taobao.api.response.UmpActivityGetResponse;
import com.taobao.api.response.UmpToolGetResponse;
import com.taobao.ump.core.service.MetaDefReadService;
import com.taobao.ump.core.service.client.MetaDefReadServiceTopClient;
import com.taobao.ump.marketing.MarketingActivity;
import com.taobao.ump.marketing.MarketingBuilder;
import com.taobao.ump.marketing.MarketingTool;

import configs.TMConfigs.App;
import controllers.APIConfig;

public class UMPManager {

    String app;

    String secret;

    static boolean sandBox = false;

    Gson gson = new Gson();

    public UMPManager(String app, String secret) {
        super();
        this.app = app;
        this.secret = secret;
    }

    public MetaDefReadServiceTopClient getProvider() {
        return getProvider(sandBox);
    }

    public MetaDefReadServiceTopClient getProvider(boolean isSandBox) {
        return new MetaDefReadServiceTopClient(App.API_TAOBAO_URL, APIConfig.get().getApiKey()
                , APIConfig.get().getSecret());

    }

    public static UMPManager manager = sandBox ? new UMPManager(APIConfig.get().getApiKey(), APIConfig.get()
            .getSecret()) : new UMPManager(APIConfig.get().getApiKey(), APIConfig.get().getSecret());

    public static UMPManager get() {
        return manager;
    }

    public MarketingTool getMarketingTool(Long toolId) throws ApiException {

        // 通过TOP获取工具

        UmpToolGetRequest request = new UmpToolGetRequest();
        request.setToolId(toolId);
        UmpToolGetResponse response = TBApi.genClient().execute(request);
        String toolContent = response.getContent();
        MetaDefReadService metaDefReaderService = getProvider();
        MarketingBuilder builder = new MarketingBuilder();

        // 注入元数据定义服务,注入该服务后builder才能正常使用
        builder.setMetaDefProvider(metaDefReaderService);

        // 通过builder处理从top获取的工具内容，生成工具的对象
        MarketingTool marketingTool = builder.loadMarketingTool(toolContent);

        return marketingTool;

    }

    public MarketingActivity getMarketingActivity(Long toolId, Long actId, User user) throws ApiException {

        MarketingTool marketingTool = getMarketingTool(toolId);

        UmpActivityGetRequest request = new UmpActivityGetRequest();
        request.setActId(actId);
        UmpActivityGetResponse response = TBApi.genClient().execute(request, user.getSessionKey());

        String actContent = response.getContent();
        MetaDefReadService metaDefReaderService = getProvider();
        MarketingBuilder builder = new MarketingBuilder();

        // 注入元数据定义服务,注入该服务后builder才能正常使用
        builder.setMetaDefProvider(metaDefReaderService);
        // 通过builder处理从top获取的活动内容，生成活动的对象
        MarketingActivity marketingActivity = builder.loadMarketingActivity(marketingTool, actContent);

        return marketingActivity;

    }

    public MarketingBuilder genBuilder() {
        MetaDefReadService metaDefReaderService = getProvider();
        MarketingBuilder builder = new MarketingBuilder();
        // 注入元数据定义服务,注入该服务后builder才能正常使用
        builder.setMetaDefProvider(metaDefReaderService);
        return builder;
    }
}
