//package underup.frame.industry;
//
//import java.util.*;
//import java.util.Map.Entry;
//
//import org.slf4j.LoggerFactory;
//import org.slf4j.Logger;
//
//import bustbapi.BusAPI;
//
//import com.ciaosir.client.pojo.ItemThumb;
//import com.ciaosir.commons.ClientException;
//import com.google.gson.Gson;
//import com.taobao.api.domain.Item;
//
//import codegen.CodeGenerator;
//import models.mysql.fengxiao.HotWordCount;
//import autotitle.AutoSplit;
//import spider.mainsearch.MainSearchApi;
//import spider.mainsearch.MainSearchApi.TBSearchRes;
//import spider.mainsearch.MainSearchApi.MainSearchOrderType;
//
//public class HotWord {
//	
//	public static final Logger log = LoggerFactory.getLogger(HotWord.class);
//	Long frontCid;
//	public HotWord(Long frontCid){
//		this.frontCid = frontCid;
//	}
//	//根据前端cid得到满足条件（综合、人气、销量、信用、最新、价格）个数（需要几页,一页=40）的宝贝
//	public List<TBSearchRes> getTBSearchRes(int pageNum, String orderType){
//		
//		List<TBSearchRes> res = new ArrayList<TBSearchRes>();
//		for(int i = 1; i <= pageNum; i++){
//			TBSearchRes temp = MainSearchApi.search(i, orderType, this.frontCid);
//			res.add(temp);
//		}
//		
//		return res;
//		
//	}
//	
////	public void storeItem(List<TBSearchRes> tbSearchRes) throws ClientException{
////	    for(TBSearchRes res: tbSearchRes){
////	        for(ItemThumb itemThumb : res.getItems()){
////	            new CatTopSaleItem(this.frontCid, itemThumb).jdbcSave();
////	        }
////	    }
////	}
//	
////	//把根据cid和满足给定条件的宝贝存进数据库
////	public void storeItem(List<TBSearchRes> itemProp) throws ClientException{ 
////		Iterator<TBSearchRes> resIt = itemProp.iterator();
////		while(resIt.hasNext()){
////			TBSearchRes res = resIt.next();
////			List<ItemThumb> items = res.getItems();
////			Long frontCid 			  = res.getCid();
////			if(items != null){
////				for(int i = 0;i < items.size(); ++i){
////					Long numIid 	= items.get(i).getId();
////					int tradeNum 	= items.get(i).getTradeNum();
////					String picPath	= items.get(i).getPicPath();
////					String title	= items.get(i).getFullTitle();
////					int   price		= items.get(i).getPrice();
////					String wangwang = items.get(i).getWangwang();
////				
////					Set<Long> ids 	= new HashSet<Long>();
////					ids.add(numIid);
////					Map<Long, Item> map = new BusAPI.MultiItemApi(ids).execute();
////					log.debug("------------------------------ok--------------------");
////					System.out.println(new Gson().toJson(map));
////					Item item = map.get(numIid);
////
////					Long listTime = item.getListTime().getTime();
////					Long delistTime = item.getDelistTime().getTime();
////					double salePrice = Double.parseDouble(item.getPrice());
////					String brand = null;
////					String sleeves = null;
////					Long backCid = item.getCid();
////
////					//切词得到属性
////					String propsName = item.getPropsName();
////					AutoSplit split = new AutoSplit(propsName, null);
////					List<String> list = split.execute();
////					Iterator<String> it = list.iterator();
////					while(it.hasNext()){
////						String temp = it.next();
////						if(temp.equals("品牌")){
////							brand = it.next();
////						}
////						if(temp.equals("袖长")){
////							sleeves = it.next();
////						}
////					}
////					
////					Long cellerId = items.get(i).getSellerId();
////					CatTopSaleItem topSaleItem = new CatTopSaleItem(numIid, frontCid, tradeNum, picPath, title, price, wangwang,
////							listTime, delistTime, salePrice, brand, sleeves, backCid);
////					topSaleItem.jdbcSave();	
////				}
////			}
////		}
////	}
////	
////	
////	//得到爆款词
////	public List<Map.Entry<String, Long>> getHotWord() throws ClientException{
////		List<String> word = CatTopSaleItem.getTitleWords(this.frontCid);
////		Map<String, Long> wordCount = new HashMap<String, Long>();
////		Iterator<String> wordIt   = word.iterator();
////		while(wordIt.hasNext()){
////			String temp = wordIt.next();
////			AutoSplit splitTool = new AutoSplit(temp, null);
////			List<String> wordSplit = splitTool.execute();
////			Iterator<String> splitIt = wordSplit.iterator();
////			while(splitIt.hasNext()){
////				String splitWord = splitIt.next();
////				if(wordCount.containsKey(splitWord)){
////					Long c = wordCount.get(splitWord);
////					c += 1;
////					wordCount.remove(splitWord);
////					wordCount.put(splitWord, c);
////				}else{
////					wordCount.put(splitWord, 1L);
////				}
////			}
////			
////		}
////		
////		List<Map.Entry<String, Long>> wordCountList = new ArrayList<Map.Entry<String, Long>>(wordCount.entrySet());
////		Collections.sort(wordCountList, new Comparator<Map.Entry<String, Long>>(){
////
////			@Override
////			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
////				// TODO Auto-generated method stub
////				return (int)(o2.getValue() - o1.getValue());
////			}
////		});
////		return wordCountList;
////	}
////	
////	
////	//存储爆款词到数据库
////	public void storeHotWord(List<Map.Entry<String, Long>> hotWord){
////		Iterator<Map.Entry<String, Long>> hotWordIt = hotWord.iterator();
////		while(hotWordIt.hasNext()){
////			Map.Entry<String, Long> entry = hotWordIt.next();
////			new HotWordCount(entry.getKey(), entry.getValue(), this.frontCid).jdbcSave();
////		}
////	}
//
//}
