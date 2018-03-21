
package controllers;

import models.fenxiao.Fenxiao;
import models.mysql.fengxiao.FenxiaoProvider;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import result.TMResult;

import com.ciaosir.client.utils.JsonUtil;

public class Fenxiaos extends TMController {

    public static void addGonghuo(String name) {
        name = StringUtils.trim(name);
        FenxiaoProvider provider = FenxiaoProvider.ensure(name);
        if (provider == null) {
            JsonUtil.getJson(new TMResult("亲,找不到该分销商哦"));
        }

        boolean isSuccess = false;
        if (name != null && name.length() > 0) {
            //Fenxiao fenxiao = Fenxiao.find("userId = ?", getUser().getId()).first();
        	Fenxiao fenxiao = Fenxiao.findByUserId(getUser().getId());
            if (fenxiao == null) {
                isSuccess = new Fenxiao(getUser().getId(), name + ",").jdbcSave();
            } else {
                if (fenxiao.getGonghuo().indexOf(name) < 0) {
                    fenxiao.setGonghuo(fenxiao.getGonghuo() + name + ",");
                    isSuccess = fenxiao.jdbcSave();
                } else {
                    isSuccess = true;
                }

            }

        }
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void removeGonghuo(String name) {
        name = StringUtils.trim(name);
        boolean isSuccess = false;
        if (name != null && name.length() > 0) {
            //Fenxiao fenxiao = Fenxiao.find("userId = ?", getUser().getId()).first();
        	Fenxiao fenxiao = Fenxiao.findByUserId(getUser().getId());
            if (fenxiao != null) {
                fenxiao.setGonghuo(fenxiao.getGonghuo().replace(name + ",", ""));
                isSuccess = fenxiao.jdbcSave();
            }
        }
        renderJSON(JsonUtil.getJson(new TMResult(isSuccess)));
    }

    public static void gonghuoInfo() {
        User user = getUser();
        //Fenxiao fenxiao = Fenxiao.find("userId = ?", user.getId()).first();
        Fenxiao fenxiao = Fenxiao.findByUserId(user.getId());
        renderJSON(fenxiao);
    }

}
