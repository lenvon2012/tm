package controllers;

import models.jd.JDGradeConfig;
import models.jd.JDUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.jd.crmmember.JDGradeConfigDao;

public class JDMemberGrade extends JDController {
    
    private static final Logger log = LoggerFactory.getLogger(JDMemberGrade.class);
    
    public static void setMemberGrade(double advanceTradeAmount, int advanceTradeCount, double vipTradeAmount, int vipTradeCount, 
            double godTradeAmount, int godTradeCount) {
        
        JDUser seller = getUser();
        if (seller == null) {
            renderError("系统出现异常，保存失败，请联系我们！");
        }
        
        JDGradeConfig gradeConfig = JDGradeConfigDao.findBySellerId(seller.getId());
        
        if (gradeConfig == null) {
            gradeConfig = new JDGradeConfig();
            gradeConfig.setSellerId(seller.getId());
        }
        
        double normalTradeAmount = 0;
        int normalTradeCount = 0;
        gradeConfig.updateGradeConfig(normalTradeAmount, normalTradeCount, 
                advanceTradeAmount, advanceTradeCount, vipTradeAmount, vipTradeCount, 
                godTradeAmount, godTradeCount);
        
        
        boolean isSuccess = gradeConfig.jdbcSave();
        if (isSuccess == false) {
            renderError("系统出现异常，保存失败，请联系我们！");
        }
        
        //然后更新CrmMember
        
        renderSuccess("客户等级设置成功");
    }
    
    public static void queryMemberGrade() {
        JDUser seller = getUser();
        
        JDGradeConfig gradeConfig = JDGradeConfigDao.findBySellerId(seller.getId());
        
        renderJDJson(gradeConfig);
    }
    
    
}
