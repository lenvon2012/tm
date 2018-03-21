package cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

public class CampaignTitleCache implements CacheVisitor<Long> {

    private static final Logger log = LoggerFactory.getLogger(CampaignTitleCache.class);

    public static final String TAG = "CampaignTitleCache";

    private static CampaignTitleCache _instance = new CampaignTitleCache();

    public static CampaignTitleCache getInstance() {
        return _instance;
    }

    public CampaignTitleCache() {
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "1h";
    }

    @Override
    public String genKey(Long t) {
        return CacheKeyGenerator.get(getInstance(), t.toString());
    }

    public static String get(Long campaignId) {
        return (String) Cache.get(getInstance().genKey(campaignId));
    }

    public static void putToCache(Long campaignId, String campaignTitle) {
        Cache.set(getInstance().genKey(campaignId), campaignTitle, getInstance().expired());
    }

}
