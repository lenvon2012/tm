
package controllers;

import java.util.List;

import models.lottery.LotteryPlay;

import org.apache.commons.lang.StringUtils;

import play.mvc.With;

import com.ciaosir.client.utils.JsonUtil;

@With(Secure.class)
@CRUD.For(LotteryPlay.class)
public class CRUDLotterys extends CRUD {

    public static void getPrize(String date, String prize) {
        if (StringUtils.isBlank(date) || StringUtils.isBlank(prize)) {
            renderJSON("[]");
        }
        date = date.trim();
        prize = prize.trim();
        List<LotteryPlay> list = LotteryPlay.findPrizeList(date, prize);
        renderJSON(JsonUtil.getJson(list));
    }

    public static void getUserPrize(String nick) {
        if (StringUtils.isBlank(nick)) {
            renderJSON("[]");
        }
        nick = nick.trim();
        List<LotteryPlay> list = LotteryPlay.findByNick(nick);
        renderJSON(JsonUtil.getJson(list));
    }
}
