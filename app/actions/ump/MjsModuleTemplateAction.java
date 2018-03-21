package actions.ump;

import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemDescModulesUpdater;

import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;

public class MjsModuleTemplateAction {
    
    private static final Logger log = LoggerFactory.getLogger(MjsModuleTemplateAction.class);
    
    public static boolean updateModuleTemplate(User user, Long numIid, 
            String tmplHtml, Long activityId, boolean isDeleteTemplate) {
        
        if (true) {
            return false;
        }
        
        if (user.isTmall() == false) {
            return false;
        }
        if (StringUtils.isEmpty(tmplHtml)) {
            tmplHtml = "";
        }
        
        Item tbItem = new ItemApi.ItemDescModulesGet(user, numIid).call();
        
        if (tbItem == null) {
            return false;
        }
        
        String descModules = tbItem.getDescModules();
        
        if (StringUtils.isEmpty(descModules)) {
            return false;
        }
        
        log.info(descModules);
        
        ItemDescModule[] moduleArray = JsonUtil.toObject(descModules, ItemDescModule[].class);
        if (moduleArray == null) {
            log.error("fail to parse ItemDescModule json: " + descModules);
            return false;
        }
        
        if (moduleArray.length <= 0) {
            return false;
        }
        
        
        boolean hasInsertTmpl = false;
        for (int i = 0; i < moduleArray.length; i++) {
            
            ItemDescModule module = moduleArray[i];
            String content = module.getContent();
            if (StringUtils.isEmpty(content)) {
                content = "";
            }
            
            if (isDeleteTemplate == false && hasInsertTmpl == false && module.isRequired() == true) {
                //更新模板
                String newContent = UmpMjsAction.addMjsTmpl(user, tmplHtml.trim(), activityId) 
                        + UmpMjsAction.deleteMjsTmpl(content, activityId);
                module.setContent(newContent);
                
                hasInsertTmpl = true;
                
            } else {
                //删除模板
                String newContent = UmpMjsAction.deleteMjsTmpl(content, activityId);
                module.setContent(newContent);
            }
        }
        
        descModules = JsonUtil.getJson(moduleArray);
        log.info(descModules);
        ItemDescModulesUpdater api = new ItemApi.ItemDescModulesUpdater(user.getSessionKey(), 
                numIid, descModules);
        Item resItem = api.call();
        /*
        if (api.isApiSuccess() == false || resItem == null) {
            return false;
        } else {
            return true;
        }*/
        
        return true;
    }
    
    
    
    public static class ItemDescModule {
        
        private Long ModuleId;
        
        private String ModuleName;
        private String type;
        private String content;
        
        private String intros;
        
        private boolean required;
        
        private String tplUrls;
        
        public Long getModuleId() {
            return ModuleId;
        }
        public void setModuleId(Long moduleId) {
            ModuleId = moduleId;
        }
        public String getModuleName() {
            return ModuleName;
        }
        public void setModuleName(String moduleName) {
            ModuleName = moduleName;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public String getIntros() {
            return intros;
        }
        public void setIntros(String intros) {
            this.intros = intros;
        }
        public boolean isRequired() {
            return required;
        }
        public void setRequired(boolean required) {
            this.required = required;
        }
        public String getTplUrls() {
            return tplUrls;
        }
        public void setTplUrls(String tplUrls) {
            this.tplUrls = tplUrls;
        }
        
        
    }
    
}
