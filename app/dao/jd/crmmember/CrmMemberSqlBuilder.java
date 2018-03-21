package dao.jd.crmmember;

import java.util.ArrayList;
import java.util.List;

import models.jd.JDCrmMember;
import models.jd.JDUser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

public class CrmMemberSqlBuilder {
    private static final Logger log = LoggerFactory.getLogger(CrmMemberSqlBuilder.class);
    
    private List<Object> paramList = new ArrayList<Object>();
    
    private JDUser seller;
    
    //用户等级
    private long memberGrade;
    
    //用户昵称
    private String memberNick;
    
    //交易额条件
    private double startTradeAmount;
    private double endTradeAmount;
    
    //交易量条件
    private int startTradeCount;
    private int endTradeCount;
    
    //最后交易时间条件
    private long startLastTradeTime;
    private long endLastTradeTime;
    
    
    //排序
    private String orderByField;
    private boolean isDesc;
    
    
    public long getMemberGrade() {
        return memberGrade;
    }
    public void setMemberGrade(long memberGrade) {
        this.memberGrade = memberGrade;
    }
    public String getMemberNick() {
        return memberNick;
    }
    public void setMemberNick(String memberNick) {
        this.memberNick = memberNick;
    }
    public double getStartTradeAmount() {
        return startTradeAmount;
    }
    public void setStartTradeAmount(double startTradeAmount) {
        this.startTradeAmount = startTradeAmount;
    }
    public double getEndTradeAmount() {
        return endTradeAmount;
    }
    public void setEndTradeAmount(double endTradeAmount) {
        this.endTradeAmount = endTradeAmount;
    }
    public int getStartTradeCount() {
        return startTradeCount;
    }
    public void setStartTradeCount(int startTradeCount) {
        this.startTradeCount = startTradeCount;
    }
    public int getEndTradeCount() {
        return endTradeCount;
    }
    public void setEndTradeCount(int endTradeCount) {
        this.endTradeCount = endTradeCount;
    }
    public long getStartLastTradeTime() {
        return startLastTradeTime;
    }
    public void setStartLastTradeTime(long startLastTradeTime) {
        this.startLastTradeTime = startLastTradeTime;
    }
    public long getEndLastTradeTime() {
        return endLastTradeTime;
    }
    public void setEndLastTradeTime(long endLastTradeTime) {
        this.endLastTradeTime = endLastTradeTime;
    }
    
    public String getOrderByField() {
        return orderByField;
    }
    public void setOrderByField(String orderByField) {
        this.orderByField = orderByField;
    }
    public boolean isDesc() {
        return isDesc;
    }
    public void setDesc(boolean isDesc) {
        this.isDesc = isDesc;
    }
    
    
    public Object[] getParamArray() {
        if (CommonUtils.isEmpty(paramList)) {
            return new Object[0];
        }
        Object[] paramArray = new Object[paramList.size()]; 
        for (int i = 0; i < paramList.size(); i++) {
            paramArray[i] = paramList.get(i);
        }
        
        return paramArray;
    }
    
    public String buildQuerySql(JDUser seller, int offset, int limit) {
        paramList.clear();
        this.seller = seller;
        
        String resultSql = " select " + JDCrmMemberDao.CrmMemberProperties + " ";
        
        String fromSql = buildFromSql();
        
        resultSql += fromSql;
        
        //排序
        if (!StringUtils.isEmpty(orderByField)) {
            
            resultSql += " order by " + orderByField;
            if (isDesc == true) {
                resultSql += " desc, ";
            } else {
                resultSql += " asc, ";
            }
        } else {
            resultSql += " order by ";
        }
        resultSql += " id asc ";
        

        resultSql += " limit ?, ?";
        paramList.add(offset);
        paramList.add(limit);
        
        return resultSql;
    }
    
    public String buildCountSql(JDUser seller) {
        paramList.clear();
        this.seller = seller;
        
        String resultSql = " select count(*) ";
        
        String fromSql = buildFromSql();
        
        resultSql += fromSql;
        
        return resultSql;
    }
    
    
    
    
    private String buildFromSql() {
        
        paramList.clear();
        
        String fromSql = " from " + JDCrmMember.TABLE_NAME + "%s where sellerId = ? ";
        paramList.add(seller.getId());
        
        fromSql = JDCrmMember.genShardQuery(fromSql, seller.getId());
        
        
        //会员等级
        if (memberGrade > 0) {
            fromSql += " and grade = ? ";
            paramList.add(memberGrade);
        }
        //买家昵称
        memberNick = trimString(memberNick);
        if (!StringUtils.isEmpty(memberNick)) {
            fromSql += " and memberNick like '%" + memberNick + "%' ";
        }
        //交易额
        if (startTradeAmount > 0) {
            fromSql += " and tradeAmount >= ? ";
            paramList.add(startTradeAmount);
        }
        if (endTradeAmount > 0) {
            fromSql += " and tradeAmount <= ? ";
            paramList.add(endTradeAmount);
        }
        //交易量
        if (startTradeCount > 0) {
            fromSql += " and tradeCount >= ? ";
            paramList.add(startTradeCount);
        }
        if (endTradeCount > 0) {
            fromSql += " and tradeCount <= ? ";
            paramList.add(endTradeCount);
        }
        //最后交易时间
        if (startLastTradeTime > 0) {
            fromSql += " and lastTradeTime >= ? ";
            paramList.add(startLastTradeTime);
        }
        if (endLastTradeTime > 0) {
            fromSql += " and lastTradeTime <= ? ";
            paramList.add(endLastTradeTime);
        }
        
        return fromSql;
    }
    
    
    private static String trimString(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        str = str.trim();
        
        return str;
    }
    
    
    
    
    
}
