package cn.alibaba.open.param;

import com.alibaba.ocean.rawsdk.client.APIId;
import com.alibaba.ocean.rawsdk.common.AbstractAPIRequest;

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class MemberMemberIdGetParam extends AbstractAPIRequest<MemberMemberIdGetResult> {

    public MemberMemberIdGetParam() {
        super();
        oceanApiId = new APIId("cn.alibaba.open", "member.memberId.get", 1);
    }

    private Long userId;

    /**
     * @return 
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置     *
     * 参数示例：<pre></pre>     
     * 此参数必填
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
