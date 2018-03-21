package actions.itemcopy.apidataparser;

import java.util.List;
import java.util.Map;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class WDetailV6DataBean {
    private String api;
    private String v;
    private DataBean data;
    private List<String> ret;

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public List<String> getRet() {
        return ret;
    }

    public void setRet(List<String> ret) {
        this.ret = ret;
    }

    public static class DataBean {

        private ItemBean item;
        private String mockData;
        private ParamsBean params;
        private PropsBean props;
        private Props2Bean props2;
        private RateBean rate;
        private ResourceBean resource;
        private SellerBean seller;
        private SkuBaseBean skuBase;
        private VerticalBean vertical;
        private List<ApiStackBean> apiStack;

        public ItemBean getItem() {
            return item;
        }

        public void setItem(ItemBean item) {
            this.item = item;
        }

        public String getMockData() {
            return mockData;
        }

        public void setMockData(String mockData) {
            this.mockData = mockData;
        }

        public ParamsBean getParams() {
            return params;
        }

        public void setParams(ParamsBean params) {
            this.params = params;
        }

        public PropsBean getProps() {
            return props;
        }

        public void setProps(PropsBean props) {
            this.props = props;
        }

        public Props2Bean getProps2() {
            return props2;
        }

        public void setProps2(Props2Bean props2) {
            this.props2 = props2;
        }

        public RateBean getRate() {
            return rate;
        }

        public void setRate(RateBean rate) {
            this.rate = rate;
        }

        public ResourceBean getResource() {
            return resource;
        }

        public void setResource(ResourceBean resource) {
            this.resource = resource;
        }

        public SellerBean getSeller() {
            return seller;
        }

        public void setSeller(SellerBean seller) {
            this.seller = seller;
        }

        public SkuBaseBean getSkuBase() {
            return skuBase;
        }

        public void setSkuBase(SkuBaseBean skuBase) {
            this.skuBase = skuBase;
        }

        public VerticalBean getVertical() {
            return vertical;
        }

        public void setVertical(VerticalBean vertical) {
            this.vertical = vertical;
        }

        public List<ApiStackBean> getApiStack() {
            return apiStack;
        }

        public void setApiStack(List<ApiStackBean> apiStack) {
            this.apiStack = apiStack;
        }

        public static class ItemBean {

            private String itemId;
            private String title;
            private String subtitle;
            private String categoryId;
            private String rootCategoryId;
            private String brandValueId;
            private String skuText;
            private String commentCount;
            private String favcount;
            private String taobaoDescUrl;
            private String tmallDescUrl;
            private String taobaoPcDescUrl;
            private String moduleDescUrl;
            private ModuleDescParamsBean moduleDescParams;
            private String h5moduleDescUrl;
            private List<String> images;
            private List<?> countMultiple;

            public String getItemId() {
                return itemId;
            }

            public void setItemId(String itemId) {
                this.itemId = itemId;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getSubtitle() {
                return subtitle;
            }

            public void setSubtitle(String subtitle) {
                this.subtitle = subtitle;
            }

            public String getCategoryId() {
                return categoryId;
            }

            public void setCategoryId(String categoryId) {
                this.categoryId = categoryId;
            }

            public String getRootCategoryId() {
                return rootCategoryId;
            }

            public void setRootCategoryId(String rootCategoryId) {
                this.rootCategoryId = rootCategoryId;
            }

            public String getBrandValueId() {
                return brandValueId;
            }

            public void setBrandValueId(String brandValueId) {
                this.brandValueId = brandValueId;
            }

            public String getSkuText() {
                return skuText;
            }

            public void setSkuText(String skuText) {
                this.skuText = skuText;
            }

            public String getCommentCount() {
                return commentCount;
            }

            public void setCommentCount(String commentCount) {
                this.commentCount = commentCount;
            }

            public String getFavcount() {
                return favcount;
            }

            public void setFavcount(String favcount) {
                this.favcount = favcount;
            }

            public String getTaobaoDescUrl() {
                return taobaoDescUrl;
            }

            public void setTaobaoDescUrl(String taobaoDescUrl) {
                this.taobaoDescUrl = taobaoDescUrl;
            }

            public String getTmallDescUrl() {
                return tmallDescUrl;
            }

            public void setTmallDescUrl(String tmallDescUrl) {
                this.tmallDescUrl = tmallDescUrl;
            }

            public String getTaobaoPcDescUrl() {
                return taobaoPcDescUrl;
            }

            public void setTaobaoPcDescUrl(String taobaoPcDescUrl) {
                this.taobaoPcDescUrl = taobaoPcDescUrl;
            }

            public String getModuleDescUrl() {
                return moduleDescUrl;
            }

            public void setModuleDescUrl(String moduleDescUrl) {
                this.moduleDescUrl = moduleDescUrl;
            }

            public ModuleDescParamsBean getModuleDescParams() {
                return moduleDescParams;
            }

            public void setModuleDescParams(ModuleDescParamsBean moduleDescParams) {
                this.moduleDescParams = moduleDescParams;
            }

            public String getH5moduleDescUrl() {
                return h5moduleDescUrl;
            }

            public void setH5moduleDescUrl(String h5moduleDescUrl) {
                this.h5moduleDescUrl = h5moduleDescUrl;
            }

            public List<String> getImages() {
                return images;
            }

            public void setImages(List<String> images) {
                this.images = images;
            }

            public List<?> getCountMultiple() {
                return countMultiple;
            }

            public void setCountMultiple(List<?> countMultiple) {
                this.countMultiple = countMultiple;
            }

            public static class ModuleDescParamsBean {
                /**
                 * f : TB14lwFh4TI8KJjSspi8qvM4FXa
                 * id : 556395187059
                 */

                private String f;
                private String id;

                public String getF() {
                    return f;
                }

                public void setF(String f) {
                    this.f = f;
                }

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }
            }
        }

        public static class ParamsBean {
            /**
             * trackParams : {"brandId":"3329161","BC_type":"B","categoryId":"50012031"}
             */

            private TrackParamsBean trackParams;

            public TrackParamsBean getTrackParams() {
                return trackParams;
            }

            public void setTrackParams(TrackParamsBean trackParams) {
                this.trackParams = trackParams;
            }

            public static class TrackParamsBean {
                /**
                 * brandId : 3329161
                 * BC_type : B
                 * categoryId : 50012031
                 */

                private String brandId;
                private String BC_type;
                private String categoryId;

                public String getBrandId() {
                    return brandId;
                }

                public void setBrandId(String brandId) {
                    this.brandId = brandId;
                }

                public String getBC_type() {
                    return BC_type;
                }

                public void setBC_type(String BC_type) {
                    this.BC_type = BC_type;
                }

                public String getCategoryId() {
                    return categoryId;
                }

                public void setCategoryId(String categoryId) {
                    this.categoryId = categoryId;
                }
            }
        }

        public static class PropsBean {
            private List<Map<String, List<Map>>> groupProps;

            public List<Map<String, List<Map>>> getGroupProps() {
                return groupProps;
            }

            public PropsBean setGroupProps(List<Map<String, List<Map>>> groupProps) {
                this.groupProps = groupProps;
                return this;
            }
        }

        public static class Props2Bean {
        }

        public static class RateBean {
            private String totalCount;
            private List<KeywordsBean> keywords;
            private List<RateListBean> rateList;

            public String getTotalCount() {
                return totalCount;
            }

            public void setTotalCount(String totalCount) {
                this.totalCount = totalCount;
            }

            public List<KeywordsBean> getKeywords() {
                return keywords;
            }

            public void setKeywords(List<KeywordsBean> keywords) {
                this.keywords = keywords;
            }

            public List<RateListBean> getRateList() {
                return rateList;
            }

            public void setRateList(List<RateListBean> rateList) {
                this.rateList = rateList;
            }

            public static class KeywordsBean {
                /**
                 * attribute : 260-11
                 * word : 鞋子不错
                 * count : 198
                 * type : 1
                 */

                private String attribute;
                private String word;
                private String count;
                private String type;

                public String getAttribute() {
                    return attribute;
                }

                public void setAttribute(String attribute) {
                    this.attribute = attribute;
                }

                public String getWord() {
                    return word;
                }

                public void setWord(String word) {
                    this.word = word;
                }

                public String getCount() {
                    return count;
                }

                public void setCount(String count) {
                    this.count = count;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }
            }

            public static class RateListBean {
                /**
                 * content :
                 * userName : 少**悲
                 * headPic : http://gtms03.alicdn.com/tps/i3/TB1yeWeIFXXXXX5XFXXuAZJYXXX-210-210.png_80x80.jpg
                 * memberLevel : 7
                 * dateTime : 2017-12-06
                 * skuInfo : 颜色分类:白/大学蓝/深藏青蓝;鞋码:43
                 * images : ["http://gw.alicdn.com/bao/uploaded/i3/0/TB21F8Tg9YH8KJjSspdXXcRgVXa_!!0-rate.jpg"]
                 * tmallMemberLevel : 3
                 * headExtraPic :
                 * memberIcon :
                 */

                private String content;
                private String userName;
                private String headPic;
                private String memberLevel;
                private String dateTime;
                private String skuInfo;
                private String tmallMemberLevel;
                private String headExtraPic;
                private String memberIcon;
                private List<String> images;

                public String getContent() {
                    return content;
                }

                public void setContent(String content) {
                    this.content = content;
                }

                public String getUserName() {
                    return userName;
                }

                public void setUserName(String userName) {
                    this.userName = userName;
                }

                public String getHeadPic() {
                    return headPic;
                }

                public void setHeadPic(String headPic) {
                    this.headPic = headPic;
                }

                public String getMemberLevel() {
                    return memberLevel;
                }

                public void setMemberLevel(String memberLevel) {
                    this.memberLevel = memberLevel;
                }

                public String getDateTime() {
                    return dateTime;
                }

                public void setDateTime(String dateTime) {
                    this.dateTime = dateTime;
                }

                public String getSkuInfo() {
                    return skuInfo;
                }

                public void setSkuInfo(String skuInfo) {
                    this.skuInfo = skuInfo;
                }

                public String getTmallMemberLevel() {
                    return tmallMemberLevel;
                }

                public void setTmallMemberLevel(String tmallMemberLevel) {
                    this.tmallMemberLevel = tmallMemberLevel;
                }

                public String getHeadExtraPic() {
                    return headExtraPic;
                }

                public void setHeadExtraPic(String headExtraPic) {
                    this.headExtraPic = headExtraPic;
                }

                public String getMemberIcon() {
                    return memberIcon;
                }

                public void setMemberIcon(String memberIcon) {
                    this.memberIcon = memberIcon;
                }

                public List<String> getImages() {
                    return images;
                }

                public void setImages(List<String> images) {
                    this.images = images;
                }
            }
        }

        public static class ResourceBean {

            private EntrancesBean entrances;

            public EntrancesBean getEntrances() {
                return entrances;
            }

            public void setEntrances(EntrancesBean entrances) {
                this.entrances = entrances;
            }

            public static class EntrancesBean {


                private AskAllBean askAll;

                public AskAllBean getAskAll() {
                    return askAll;
                }

                public void setAskAll(AskAllBean askAll) {
                    this.askAll = askAll;
                }

                public static class AskAllBean {

                    private String icon;
                    private String text;
                    private String link;

                    public String getIcon() {
                        return icon;
                    }

                    public void setIcon(String icon) {
                        this.icon = icon;
                    }

                    public String getText() {
                        return text;
                    }

                    public void setText(String text) {
                        this.text = text;
                    }

                    public String getLink() {
                        return link;
                    }

                    public void setLink(String link) {
                        this.link = link;
                    }
                }
            }
        }

        public static class SellerBean {

            private String userId;
            private String shopId;
            private String shopName;
            private String shopUrl;
            private String taoShopUrl;
            private String shopIcon;
            private String fans;
            private String allItemCount;
            private boolean showShopLinkIcon;
            private String shopCard;
            private String sellerType;
            private String shopType;
            private String sellerNick;
            private String creditLevel;
            private String starts;
            private String goodRatePercentage;
            private List<EvaluatesBean> evaluates;

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getShopId() {
                return shopId;
            }

            public void setShopId(String shopId) {
                this.shopId = shopId;
            }

            public String getShopName() {
                return shopName;
            }

            public void setShopName(String shopName) {
                this.shopName = shopName;
            }

            public String getShopUrl() {
                return shopUrl;
            }

            public void setShopUrl(String shopUrl) {
                this.shopUrl = shopUrl;
            }

            public String getTaoShopUrl() {
                return taoShopUrl;
            }

            public void setTaoShopUrl(String taoShopUrl) {
                this.taoShopUrl = taoShopUrl;
            }

            public String getShopIcon() {
                return shopIcon;
            }

            public void setShopIcon(String shopIcon) {
                this.shopIcon = shopIcon;
            }

            public String getFans() {
                return fans;
            }

            public void setFans(String fans) {
                this.fans = fans;
            }

            public String getAllItemCount() {
                return allItemCount;
            }

            public void setAllItemCount(String allItemCount) {
                this.allItemCount = allItemCount;
            }

            public boolean isShowShopLinkIcon() {
                return showShopLinkIcon;
            }

            public void setShowShopLinkIcon(boolean showShopLinkIcon) {
                this.showShopLinkIcon = showShopLinkIcon;
            }

            public String getShopCard() {
                return shopCard;
            }

            public void setShopCard(String shopCard) {
                this.shopCard = shopCard;
            }

            public String getSellerType() {
                return sellerType;
            }

            public void setSellerType(String sellerType) {
                this.sellerType = sellerType;
            }

            public String getShopType() {
                return shopType;
            }

            public void setShopType(String shopType) {
                this.shopType = shopType;
            }

            public String getSellerNick() {
                return sellerNick;
            }

            public void setSellerNick(String sellerNick) {
                this.sellerNick = sellerNick;
            }

            public String getCreditLevel() {
                return creditLevel;
            }

            public void setCreditLevel(String creditLevel) {
                this.creditLevel = creditLevel;
            }

            public String getStarts() {
                return starts;
            }

            public void setStarts(String starts) {
                this.starts = starts;
            }

            public String getGoodRatePercentage() {
                return goodRatePercentage;
            }

            public void setGoodRatePercentage(String goodRatePercentage) {
                this.goodRatePercentage = goodRatePercentage;
            }

            public List<EvaluatesBean> getEvaluates() {
                return evaluates;
            }

            public void setEvaluates(List<EvaluatesBean> evaluates) {
                this.evaluates = evaluates;
            }

            public static class EvaluatesBean {
                /**
                 * title : 宝贝描述
                 * score : 4.8
                 * type : desc
                 * level : 0
                 */

                private String title;
                private String score;
                private String type;
                private String level;

                public String getTitle() {
                    return title;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                public String getScore() {
                    return score;
                }

                public void setScore(String score) {
                    this.score = score;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public String getLevel() {
                    return level;
                }

                public void setLevel(String level) {
                    this.level = level;
                }
            }
        }

        public static class SkuBaseBean {
            private List<SkusBean> skus;
            private List<PropsBeanX> props;

            public List<SkusBean> getSkus() {
                return skus;
            }

            public void setSkus(List<SkusBean> skus) {
                this.skus = skus;
            }

            public List<PropsBeanX> getProps() {
                return props;
            }

            public void setProps(List<PropsBeanX> props) {
                this.props = props;
            }

            public static class SkusBean {
                /**
                 * skuId : 3669294710048
                 * propPath : 20549:44897;1627207:537075003
                 */

                private String skuId;
                private String propPath;

                public String getSkuId() {
                    return skuId;
                }

                public void setSkuId(String skuId) {
                    this.skuId = skuId;
                }

                public String getPropPath() {
                    return propPath;
                }

                public void setPropPath(String propPath) {
                    this.propPath = propPath;
                }
            }

            public static class PropsBeanX {
                /**
                 * pid : 20549
                 * name : 鞋码
                 * values : [{"vid":"44897","name":"38.5"},{"vid":"672","name":"39"},{"vid":"28389","name":"40"},{"vid":"44899","name":"40.5"},{"vid":"28390","name":"41"},{"vid":"28391","name":"42"},{"vid":"44901","name":"42.5"},{"vid":"28392","name":"43"},{"vid":"28393","name":"44"},{"vid":"44903","name":"44.5"},{"vid":"28394","name":"45"},{"vid":"44904","name":"45.5"},{"vid":"28395","name":"46"},{"vid":"44906","name":"47.5"}]
                 */

                private String pid;
                private String name;
                private List<ValuesBean> values;

                public String getPid() {
                    return pid;
                }

                public void setPid(String pid) {
                    this.pid = pid;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public List<ValuesBean> getValues() {
                    return values;
                }

                public void setValues(List<ValuesBean> values) {
                    this.values = values;
                }

                public static class ValuesBean {
                    private String vid;
                    private String name;
                    private String image;

                    public String getVid() {
                        return vid;
                    }

                    public void setVid(String vid) {
                        this.vid = vid;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getImage() {
                        return image;
                    }

                    public ValuesBean setImage(String image) {
                        this.image = image;
                        return this;
                    }
                }
            }
        }

        public static class VerticalBean {
            /**
             * askAll : {"askText":"产地那里？","askIcon":"https://gw.alicdn.com/tps/TB1J7X6KXXXXXc4XXXXXXXXXXXX-102-60.png","linkUrl":"//h5.m.taobao.com/wendajia/question2017.html?refId=556395187059","title":"问大家(302)","questNum":"302","showNum":"2","modelList":[{"askText":"产地那里？","answerCountText":"1个回答"},{"askText":"夏天能穿吗 热不热","answerCountText":"3个回答"}]}
             */

            private AskAllBeanX askAll;

            public AskAllBeanX getAskAll() {
                return askAll;
            }

            public void setAskAll(AskAllBeanX askAll) {
                this.askAll = askAll;
            }

            public static class AskAllBeanX {
                /**
                 * askText : 产地那里？
                 * askIcon : https://gw.alicdn.com/tps/TB1J7X6KXXXXXc4XXXXXXXXXXXX-102-60.png
                 * linkUrl : //h5.m.taobao.com/wendajia/question2017.html?refId=556395187059
                 * title : 问大家(302)
                 * questNum : 302
                 * showNum : 2
                 * modelList : [{"askText":"产地那里？","answerCountText":"1个回答"},{"askText":"夏天能穿吗 热不热","answerCountText":"3个回答"}]
                 */

                private String askText;
                private String askIcon;
                private String linkUrl;
                private String title;
                private String questNum;
                private String showNum;
                private List<ModelListBean> modelList;

                public String getAskText() {
                    return askText;
                }

                public void setAskText(String askText) {
                    this.askText = askText;
                }

                public String getAskIcon() {
                    return askIcon;
                }

                public void setAskIcon(String askIcon) {
                    this.askIcon = askIcon;
                }

                public String getLinkUrl() {
                    return linkUrl;
                }

                public void setLinkUrl(String linkUrl) {
                    this.linkUrl = linkUrl;
                }

                public String getTitle() {
                    return title;
                }

                public void setTitle(String title) {
                    this.title = title;
                }

                public String getQuestNum() {
                    return questNum;
                }

                public void setQuestNum(String questNum) {
                    this.questNum = questNum;
                }

                public String getShowNum() {
                    return showNum;
                }

                public void setShowNum(String showNum) {
                    this.showNum = showNum;
                }

                public List<ModelListBean> getModelList() {
                    return modelList;
                }

                public void setModelList(List<ModelListBean> modelList) {
                    this.modelList = modelList;
                }

                public static class ModelListBean {

                    private String askText;
                    private String answerCountText;

                    public String getAskText() {
                        return askText;
                    }

                    public void setAskText(String askText) {
                        this.askText = askText;
                    }

                    public String getAnswerCountText() {
                        return answerCountText;
                    }

                    public void setAnswerCountText(String answerCountText) {
                        this.answerCountText = answerCountText;
                    }
                }
            }
        }

        public static class ApiStackBean {

            private String name;
            private String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
