package actions.clouddata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.user.User;

import org.apache.commons.lang.StringUtils;

import actions.UvPvDiagAction;

import com.taobao.api.domain.QueryRow;

public class WirelessSrcUvPv implements Serializable{
    
    private String thedate;

    private String srcId;
    
    private String srcName;
    
    private String parentSrcId;
    
    private String parentSrcName;
    
    private String pv;
    
    private String uv;
    
    private int srcLevel;
    
    /** 支付的子订单数  pay_ord_cnt_holotree_lastbe_guide */
    private String alipayTradeNum;
    
    /** 支付商品件数  pay_ord_item_qty_holotree_lastbe_guide */
    private String alipayAuctionNum;
    
    /** 下单买家数  crt_ord_byr_cnt_holotree_lastbe_guide */
    private String gmvWinnerNum;
    
    /** 下单金额  crt_ord_amt_holotree_lastbe_guide */
    private String gmvTradeAmt;
    
    /** 下单商品件数  crt_ord_item_qty_holotree_lastbe_guide */
    private String gmvAuctionNum;
    
    /** 下单子订单数 crt_ord_cnt_holotree_lastbe_guide */
    private String gmvTradeNum; 
    
    /** 支付买家数  pay_ord_byr_cnt_holotree_lastbe_guide */
    private String alipayWinnerNum;
    
    /** 支付金额  pay_ord_amt_holotree_lastbe_guide */
    private String alipayTradeAmt; 
    
    public WirelessSrcUvPv(QueryRow row, User user){
        List<String> values = row.getValues();
        this.thedate = values.get(0);
        this.srcId = values.get(3);
        this.srcName = UvPvDiagAction.getWirelessSrcNameById(this.srcId, user);
        this.srcLevel = Integer.valueOf(values.get(4));
        this.parentSrcId = values.get(5);
        if(StringUtils.isEmpty(parentSrcId) || "0".equals(parentSrcId)){
            this.parentSrcName = "~";
        } else {
            this.parentSrcName = UvPvDiagAction.getWirelessSrcNameById(this.parentSrcId, user);
        }
        this.pv = values.get(6);
        this.uv = values.get(7);
        this.alipayTradeNum = values.get(8);
        this.alipayAuctionNum = values.get(9);
        this.gmvWinnerNum = values.get(10);
        this.gmvTradeAmt = values.get(11);
        this.gmvAuctionNum = values.get(12);
        this.gmvTradeNum = values.get(13);
        this.alipayWinnerNum = values.get(14);
        this.alipayTradeAmt = values.get(15);
    }
    
    public WirelessSrcUvPv(WirelessSrcUvPv src) {
        this.srcId = src.getParentSrcId();
        this.srcName = src.getParentSrcName();
        this.parentSrcId = "0";
        this.parentSrcName = "~";
        this.pv = src.getPv();
        this.uv = src.getUv();
        this.alipayTradeNum = src.getAlipayTradeNum();
        this.alipayWinnerNum = src.getAlipayWinnerNum();
        this.alipayTradeAmt = src.getAlipayTradeAmt();
    }

    public void add(WirelessSrcUvPv src) {
        this.pv = String.valueOf(Integer.parseInt(this.pv) + Integer.parseInt(src.getPv()));
        this.uv = String.valueOf(Integer.parseInt(this.uv) + Integer.parseInt(src.getUv()));
        this.alipayTradeNum = String.valueOf(Integer.parseInt(this.alipayTradeNum) + Integer.parseInt(src.getAlipayTradeNum()));
        this.alipayWinnerNum = String.valueOf(Integer.parseInt(this.alipayWinnerNum) + Integer.parseInt(src.getAlipayWinnerNum()));
        this.alipayTradeAmt = String.valueOf(Double.parseDouble(this.alipayTradeAmt) + Double.parseDouble(src.getAlipayTradeAmt()));
    }
    
    public static List<WirelessSrcUvPv> sort(List<WirelessSrcUvPv> srcLevelTwo, List<WirelessSrcUvPv> srcLevelThree) {
        List<WirelessSrcUvPv> wirelessSrcUvPvs = new ArrayList<WirelessSrcUvPv>();
        
        for (WirelessSrcUvPv w : srcLevelTwo) {
            wirelessSrcUvPvs.add(w);
            String parentSrcId = w.getSrcId();
            List<WirelessSrcUvPv> temp = new ArrayList<WirelessSrcUvPv>();
            for (WirelessSrcUvPv wirelessSrcUvPv : srcLevelThree) {
                if(parentSrcId.equals(wirelessSrcUvPv.getParentSrcId())){
                    temp.add(wirelessSrcUvPv);
                }
            }
            // 根据访问数进行排序
            Collections.sort(temp, new Comparator<WirelessSrcUvPv>() {

                @Override
                public int compare(WirelessSrcUvPv o1, WirelessSrcUvPv o2) {
                    String uv1 = o1.getUv();
                    String uv2 = o2.getUv();
                    return Integer.valueOf(uv1) > Integer.valueOf(uv2) ? -1 : 1;
                }  
                
            });
            for (WirelessSrcUvPv wirelessSrcUvPv2 : temp) {
                wirelessSrcUvPvs.add(wirelessSrcUvPv2);
            }
        }
        return wirelessSrcUvPvs;
    }
    
    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public String getPv() {
        return pv;
    }

    public void setPv(String pv) {
        this.pv = pv;
    }

    public String getUv() {
        return uv;
    }

    public void setUv(String uv) {
        this.uv = uv;
    }

    public String getSrcName() {
        return srcName;
    }

    public void setSrcName(String srcName) {
        this.srcName = srcName;
    }

    public String getThedate() {
        return thedate;
    }

    public void setThedate(String thedate) {
        this.thedate = thedate;
    }

    public String getParentSrcId() {
        return parentSrcId;
    }

    public void setParentSrcId(String parentSrcId) {
        this.parentSrcId = parentSrcId;
    }

    public String getParentSrcName() {
        return parentSrcName;
    }

    public void setParentSrcName(String parentSrcName) {
        this.parentSrcName = parentSrcName;
    }

    public String getAlipayTradeNum() {
        return alipayTradeNum;
    }

    public void setAlipayTradeNum(String alipayTradeNum) {
        this.alipayTradeNum = alipayTradeNum;
    }

    public String getAlipayAuctionNum() {
        return alipayAuctionNum;
    }

    public void setAlipayAuctionNum(String alipayAuctionNum) {
        this.alipayAuctionNum = alipayAuctionNum;
    }

    public String getGmvWinnerNum() {
        return gmvWinnerNum;
    }

    public void setGmvWinnerNum(String gmvWinnerNum) {
        this.gmvWinnerNum = gmvWinnerNum;
    }

    public String getGmvTradeAmt() {
        return gmvTradeAmt;
    }

    public void setGmvTradeAmt(String gmvTradeAmt) {
        this.gmvTradeAmt = gmvTradeAmt;
    }

    public String getGmvAuctionNum() {
        return gmvAuctionNum;
    }

    public void setGmvAuctionNum(String gmvAuctionNum) {
        this.gmvAuctionNum = gmvAuctionNum;
    }

    public String getGmvTradeNum() {
        return gmvTradeNum;
    }

    public void setGmvTradeNum(String gmvTradeNum) {
        this.gmvTradeNum = gmvTradeNum;
    }

    public String getAlipayWinnerNum() {
        return alipayWinnerNum;
    }

    public void setAlipayWinnerNum(String alipayWinnerNum) {
        this.alipayWinnerNum = alipayWinnerNum;
    }

    public String getAlipayTradeAmt() {
        return alipayTradeAmt;
    }

    public void setAlipayTradeAmt(String alipayTradeAmt) {
        this.alipayTradeAmt = alipayTradeAmt;
    }

    public int getSrcLevel() {
        return srcLevel;
    }

    public void setSrcLevel(int srcLevel) {
        this.srcLevel = srcLevel;
    }

}
