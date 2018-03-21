//
//package job.hotitems;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import models.hotitem.CatHotItemPlay;
//import models.item.ItemPropSale;
////import models.item.ItemCatHotProps.VNameResult;
//import models.mysql.fengxiao.CatTopSaleItem;
//
//import org.apache.commons.lang.StringUtils;
//import org.codehaus.jackson.annotate.JsonAutoDetect;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import play.jobs.Job;
//import autotitle.ItemPropAction;
//import autotitle.ItemPropAction.PropUnit;
//import bustbapi.BusAPI;
//
//import com.ciaosir.client.pojo.IWordBase;
//import com.ciaosir.client.utils.JsonUtil;
//import com.ciaosir.commons.ClientException;
//import com.taobao.api.domain.Item;
//
///**
// * 用来计算各个后台类目的销售属性
// * @author zrb
// *
// */
//public class BackendCatHotPropUpdateJob extends Job {
//
//    private static final Logger log = LoggerFactory.getLogger(BackendCatHotPropUpdateJob.class);
//
//    public static final String TAG = "CatHotPropUpdateJob";
//
//    Long backendCid;
//    long year;
//    long month;
//    Map<String, ItemPropSale> itempropSaleMap = new HashMap<String, ItemPropSale>();
//
//    public BackendCatHotPropUpdateJob(Long cid, long year, long month) {
//        super();
//        this.backendCid = cid;
//        this.year = year;
//        this.month = month;
//    }
//
//    public void doJob() {
//        List<CatTopSaleItem> catHotItems = CatTopSaleItem.findByCid(backendCid, year, month);
//        
//        Set<Long> numIids = new HashSet<Long>();
//        for (CatTopSaleItem catHotItemPlay : catHotItems) {
//            numIids.add(catHotItemPlay.getNumIid());
//        }
//
//        try {
//            Map<Long, Item>map = new BusAPI.MultiItemApi(numIids).execute();
//            for (CatTopSaleItem catHotItem : catHotItems) {
//                doForEachCatHotItem(map, catHotItem);
//            }
//
//            Collection<ItemPropSale> values = itempropSaleMap.values();
//            for (ItemPropSale itemPropSale : values) {
//                itemPropSale.jdbcSave();
//            }
//        } catch (ClientException e) {
//            log.warn(e.getMessage(), e);
//        }
//    }
//
//    private void doForEachCatHotItem(Map<Long, Item> map, CatTopSaleItem catHotItem) {
//        Long numIid = catHotItem.getNumIid();
//        Item item = null;
//        if(map.containsKey(numIid)){
//            item = map.get(numIid);
//        }else{
//            log.warn(" no tb item for :" + catHotItem);
//            return;
//        }
//        if (item == null) {
//            log.warn(" no tb item for :" + catHotItem);
//            return;
//        }
//
//        List<PropUnit> propUnits = ItemPropAction.mergePropAlis(item);
//        for (PropUnit propUnit : propUnits) {
//            long pid = propUnit.getPid();
//            long vid = propUnit.getVid();
//            String pName = propUnit.getKey();
//            String vName = propUnit.getValue();
//            String key = "" + backendCid + pid + vid;
//            
//            ItemPropSale model = itempropSaleMap.get(key);
//
//            if (model == null) {
//                model = new ItemPropSale();
//                model.setCid(backendCid);
//                model.setPid(pid);
//                model.setVid(vid);
//                model.setPname(pName);
//                model.setVname(vName);
//                //model.setSale(catHotItem.getTradeNum());
//                itempropSaleMap.put(key, model);
//            }
//            model.setTotalPrice(model.getTotalPrice() + (long)catHotItem.getPrice());
//            model.setSale(model.getSale() + catHotItem.getTradeNum());
//        }
//    }
//
//    public Map<String, ItemPropSale> getItempropSaleMap() {
//        return itempropSaleMap;
//    }
//
//    public void setItempropSaleMap(Map<String, ItemPropSale> itempropSaleMap) {
//        this.itempropSaleMap = itempropSaleMap;
//    }
//    
//    
//    //String propJsonContent;
//    
//    @JsonAutoDetect
//    static class PNameResult{
//        @JsonProperty
//        Long pid = 0L; //eg 女装---连衣裙---材质
//
//        @JsonProperty
//        String pname = StringUtils.EMPTY; //属性名称比如材质
//        
//        public Long getPid(){
//            return pid;
//        }
//        
//        public PNameResult(Long pid, String pname){
//            this.pid = pid;
//            this.pname = pname;
//        }
//        
//        @JsonProperty
//        List<VNameResult> list = new ArrayList<VNameResult>(); //属性具体名称分类---比如材质里面的雪纺、蕾丝、真丝、亚麻、纯棉等等
//    }
//    
//    @JsonAutoDetect
//    static class VNameResult{
//        @JsonProperty
//        Long vid = 0L; //属性具体分类名称id
//
//        @JsonProperty
//        String name = StringUtils.EMPTY; //属性具体分类名称name
//
//        @JsonProperty
//        Long pv = 1L; //购买次数
//        
//        @JsonProperty
//        Long totalPrice;//总的销售量
//
//        public VNameResult(Long vid2, String nameAlias, Long pv, Long totalPrice) {
//            this.vid = vid2;
//            this.name = nameAlias;
//            this.pv    = pv;
//            this.totalPrice = totalPrice;
//        }
//    }
//    public String getPackage(){
//        List<PNameResult> pNameList = new ArrayList<PNameResult>();
//        ArrayList<String> keys = new ArrayList(itempropSaleMap.keySet());
//        for(String key: keys){
//            ItemPropSale  itemProps = itempropSaleMap.get(key);
//            Long pid = itemProps.getPid();
//            String pname = itemProps.getPname();
//            if(pNameList.size() == 0){
//                PNameResult pName = new PNameResult(pid, pname);
//                pName.list.add(new VNameResult(itemProps.getVid(), itemProps.getVname(), itemProps.getSale(), itemProps.getTotalPrice()));
//                pNameList.add(pName);
//            }else{
//                int i = 0;
//                for(;i < pNameList.size(); ++i){
//                    
//                    if(pNameList.get(i).getPid().equals(pid)){
//                        pNameList.get(i).list.add(new VNameResult(itemProps.getVid(), itemProps.getVname(), itemProps.getSale(), itemProps.getTotalPrice()));
//                        break;
//                    }
//                } 
//                if(i == pNameList.size()){
//                    PNameResult pName = new PNameResult(pid, pname);
//                    pName.list.add(new VNameResult(itemProps.getVid(), itemProps.getVname(), itemProps.getSale(), itemProps.getTotalPrice()));
//                    pNameList.add(pName);
//                }
//            }
//        
//        }
//        String content = JsonUtil.getJson(pNameList);
//        return content;
//    }
//}
