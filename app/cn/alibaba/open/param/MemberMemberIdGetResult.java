package cn.alibaba.open.param;

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MemberMemberIdGetResult {

    private Map memberInfo;

    /**
     * @return memberId
     */
    public Map getMemberInfo() {
        return memberInfo;
    }

    /**
     * 设置memberId     *
          
     * 此参数必填
     */
    public void setMemberInfo(Map memberInfo) {
        this.memberInfo = memberInfo;
    }

}
