package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import models.baidufav.BaiduFaved;
import models.user.User;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import dao.item.ItemDao;

public class BaiduFav extends TMController {

	public static void getFaved(){
		User user = getUser();
		List<BaiduFaved> baiduFaved = new ArrayList<BaiduFaved>();
		baiduFaved = BaiduFaved.find("userId = ?", user.getId()).fetch();
		if(baiduFaved.size() == 0){
			baiduFaved.add(new BaiduFaved(user.getId(), 32321l,"心形百搭白色保暖黑色酒红色全棉四季中等女羊毛兔均码浅灰色褐色","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2wB9IXhxbXXXXXXXX_!!1039626382.jpg",
					7.8,4));
		}
    	renderJSON(JsonUtil.getJson(baiduFaved));
	}
	
	public static void addFav(String numIids){
		User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        //for (String string : idStrings) {
           // BaiduFaved.add(user, NumberUtil.parserLong(string, 0L));
        BaiduFaved.add(user,ItemDao.findByIds(user.getId(), numIids));
        //}

        TMResult.renderMsg(StringUtils.EMPTY);
	}
	
	public static void getunfaved(int pn, int ps ,String s){
		User user = getUser();
        Set<Long> ids1 = BaiduFaved.findIdsByUser(user.getId());

        PageOffset po = new PageOffset(pn, ps);
        TMResult tmRes = ItemDao.findByUserAndSearchWithExcluded(user.getId(), s, po, ids1);
        renderJSON(JsonUtil.getJson(tmRes));
	}
	
	public static void removeFaved(String numIids){
		User user = getUser();
        if (StringUtils.isEmpty(numIids)) {
            ok();
        }
        String[] idStrings = StringUtils.split(numIids, ",");
        if (ArrayUtils.isEmpty(idStrings)) {
            ok();
        }
        BaiduFaved.remove(user,idStrings);
        TMResult.renderMsg(StringUtils.EMPTY);
	}
	
	
}
