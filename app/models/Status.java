
package models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Status {

    public static final Logger log = LoggerFactory.getLogger(Status.class);

    public static enum TRADE_STATUS {
        TRADE_NO_CREATE_PAY,
        WAIT_BUYER_PAY,
        WAIT_SELLER_SEND_GOODS,
        WAIT_BUYER_CONFIRM_GOODS,
        TRADE_BUYER_SIGNED,
        TRADE_FINISHED,
        TRADE_CLOSED, // 6
        TRADE_CLOSED_BY_TAOBAO,
        SELLER_CONSIGNED_PART;
    };

    public static int tradestatus2int(String status) {
        return TRADE_STATUS.valueOf(status).ordinal();
    }

    /**
     * 交易来源。 WAP(手机);HITAO(嗨淘);TOP(TOP平台);TAOBAO(普通淘宝);JHS(聚划算)
     */
    public static enum TRADE_FROM {
        WAP, HITAO, TOP, TAOBAO, JHS, CUNTAO;
    }

    public static int tradefrom2int(String tradeFrom) {
        // log.info("TradeFrom to int " + tradeFrom);
        if (tradeFrom == null || tradeFrom.length() < 1) {
            return -1;
        }
        int idx = tradeFrom.indexOf(",");
        if (idx > 0) {
            tradeFrom = tradeFrom.substring(0, idx);
        }

        // log.info("TradeFrom:"+ tradeFrom);
        return TRADE_FROM.valueOf(tradeFrom).ordinal();
    }

    public static class REFUND_TYPE {
        public static final int SELLER_NOT_SEND_REFND_FULL = 0;

        public static final int BUYER_RECEIVE_REFUND_GOOD_AND_MONEY = 1;

        public static final int REFUND_MONEY_NOT_REFUND_GOOD = 2;
    }

    public enum Task {
        NEW, DOING, DONE, CHECK, CHECKING, CHECKED
    };
}
