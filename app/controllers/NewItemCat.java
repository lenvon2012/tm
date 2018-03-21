package controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import models.item.ItemCatPlay;
import models.item.NewItemCatPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import bustbapi.ItemCatApi;
import bustbapi.ItemCatApi.ItemcatsGet;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.ItemCat;

public class NewItemCat extends TMController {
	
	private static List<String> getData(List<String> paramList, String fileUrl) {
    	try {
            // 创建文件对象
            File file = new File(fileUrl);
            // 从文件流中获取Excel工作区对象（WorkBook）  
            Workbook wb = Workbook.getWorkbook(file);
            // 从工作区中取得页（Sheet）
            Sheet sheet = wb.getSheet(0);
            
            // 循环打印Excel表中的内容  
            for (int i = 0; i < sheet.getRows(); i++) {
            	String param = StringUtils.EMPTY;
                for (int j = 0; j < sheet.getColumns(); j++) {
                	Cell cell = sheet.getCell(j, i);
                	param = param + cell.getContents() + ",";
                }
                paramList.add(param);
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return paramList;
    }
    
	private static NewItemCatPlay analyzeKeyword(String param) {
		
		User user = getUser();
		
		NewItemCatPlay newItemCatPlay = null;
		
		String keyword = StringUtils.EMPTY;
		String name = StringUtils.EMPTY;
		long level_1 = 0L;
		long level_2 = 0L;
		long level_3 = 0L;
		long level_4 = 0L;
		long level_5 = 0L;
		
		String[] split = param.split(",");
		
		name = split[1];
		keyword = split[split.length - 1];
		
		// 数据库查找
		ItemCatPlay itemCatPlay = ItemCatPlay.findAccordingName(keyword);
		if(itemCatPlay == null) {
			// 第一类目
			level_1 = getCid(user, 0L, split[2].toString());
			if(level_1 == 0) {
				log.error("~~~~~~~~~~~~~~~~~~~~~~~未在数据库匹配到第1类目：" + keyword + "~~~~~~~~~~~~~~~~~~~~~~~");
			} else {
				// 第二类目
				if(split.length >= 4) {
					level_2 = getCid(user, level_1, split[3].toString());
					if(level_2 == 0) {
						log.error("~~~~~~~~~~~~~~~~~~~~~~~未在数据库匹配到第2类目：" + keyword + "~~~~~~~~~~~~~~~~~~~~~~~");
					} else {
						// 第三类目
						if(split.length >= 5) {
							level_3 = getCid(user, level_2, split[4].toString());
							if(level_3 == 0) {
								log.error("~~~~~~~~~~~~~~~~~~~~~~~未在数据库匹配到第3类目：" + keyword + "~~~~~~~~~~~~~~~~~~~~~~~");
							}
						}
					}
				}
			}
			
			newItemCatPlay = new NewItemCatPlay(name, level_1, level_2, level_3, level_4, level_5);
			return newItemCatPlay;
			
		}
		
		long cid = itemCatPlay.getCid();
		int level = itemCatPlay.getLevel();
		
		if(level == 1) {
			level_1 = cid;
		} else if (level == 2) {
			level_1 = itemCatPlay.topCate_1;
			level_2 = cid;
		} else if (level == 3) {
			level_1 = itemCatPlay.topCate_1;
			level_2 = itemCatPlay.topCate_2;
			level_3 = cid;
		} else if (level == 4) {
			level_1 = itemCatPlay.topCate_1;
			level_2 = itemCatPlay.topCate_2;
			level_3 = itemCatPlay.topCate_3;
			level_4 = cid;
		} else if (level == 5) {
			level_1 = itemCatPlay.topCate_1;
			level_2 = itemCatPlay.topCate_2;
			level_3 = itemCatPlay.topCate_3;
			level_4 = itemCatPlay.topCate_4;
			level_5 = cid;
		}
		
		newItemCatPlay = new NewItemCatPlay(name, level_1, level_2, level_3, level_4, level_5);
		log.info("-----------------关键词：" + name + "已完成-----------------");
		
		return newItemCatPlay;
		
	}
	
	public static Long getCid(User user, Long parentCid, String keyword) {
		// 缓存中查找
		Long level = 0L;
		String level_Str = StringUtils.EMPTY;
		
		Object object = Cache.get(keyword);
		if(object != null) {
			level_Str = Cache.get(keyword).toString();
			level = Long.parseLong(level_Str);
		} else {
			// Api获取
			level = itemCatApi(user, parentCid, keyword);
			if(level != 0) {
				Cache.set(keyword, level);
			}
		}
		return level;
	}
	
	public static Long itemCatApi(User user, Long parentCid, String name) {
		ItemcatsGet itemCates = new ItemcatsGet(user, parentCid);
		List<ItemCat> itemCatList = itemCates.call();
		if(CommonUtils.isEmpty(itemCatList)) {
			log.error("~~~~~~~~~~~~~~~~~~~~~~~API返回为空，类目Id：" + parentCid + "~~~~~~~~~~~~~~~~~~~~~~~");
		}
		for(ItemCat itemCat : itemCatList) {
			if(name.equalsIgnoreCase(itemCat.getName())) {
				 return itemCat.getCid();
			}
		}
		return 0L;
	}
	
    public static void CatTest() {
    	
    	List<String> paramList = new ArrayList<String>();
    	List newItemCatList = new ArrayList<NewItemCatPlay>();
    	
    	for (int i = 1; i <= 4; i++) {
    		// Excel文件所在路径
            String fileUrl = "C:\\Users\\Administrator\\Desktop\\直通车6月29日top20万词表无线" + i + ".xls";
//    		String fileUrl = "C:\\Users\\Administrator\\Desktop\\测试.xls";
            paramList = getData(paramList, fileUrl);
		}
    	
    	for (int j = 0; j < paramList.size(); j++) {
			String param = paramList.get(j);
			NewItemCatPlay newItemCatPlay = analyzeKeyword(param);
			newItemCatList.add(newItemCatPlay);
			// 每1024条数据保存一次
	        if ((newItemCatList.size() & 1023) == 0) {
	            // 保存数据
	            NewItemCatPlay.batchSave(newItemCatList);
	            newItemCatList.clear();
	        }
		}
    	NewItemCatPlay.batchSave(newItemCatList);
    	
    }
    
}
