
package models.helpcenter;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.CreatedUpdatedModel;

import org.apache.commons.collections.ListUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;

import play.Play;
import controllers.APIConfig;

@Entity(name = HelpNavLevel1.TABLE_NAME)
@JsonAutoDetect
@JsonIgnoreProperties(value = {
        "hibernateLazyInitializer", "entityId"
})
public class HelpNavLevel1 extends CreatedUpdatedModel {

    @Transient
    public static final String TABLE_NAME = "help_nav_level_1";

    int appkey;

    @Index(name = "name")
    String name;

    String comment;

    boolean isDelete;

    @Transient
    List<HelpNavLevel2> level2s = ListUtils.EMPTY_LIST;

    public static List<HelpNavLevel1> allWithLevel2() {
        List<HelpNavLevel1> list = HelpNavLevel1.find(" isDelete = ? ", false).fetch();
        for (HelpNavLevel1 helpNavLevel1 : list) {
            helpNavLevel1.ensureNav2();
        }
        return list;
    }

    public HelpNavLevel1() {
        super();
    }

    public HelpNavLevel1(int appkey, String name, String comment) {
        super();
        this.appkey = appkey;
        this.name = name;
        this.comment = comment;
    }

    private void ensureNav2() {
        this.level2s = HelpNavLevel2.find(" parent = ? ", this).fetch();
    }

    public static void ensureBase() {
        int maxNum = 0;
        if (HelpNavLevel1.find(" 1 = 1 ").first() != null) {
            return;
        }

        if (Play.mode.isDev()) {
            TMHelpArticle.deleteAll();
            HelpNavLevel2.deleteAll();
            HelpNavLevel1.deleteAll();

            HelpNavLevel1 level1 = null;
            HelpNavLevel2 level2 = null;
            TMHelpArticle article = null;

            int num = 40;
            level1 = new HelpNavLevel1(APIConfig.taobiaoti.getApp(), "标题优化", "标题优化帮助中心");
            level1.save();
            
            level2 = new HelpNavLevel2("视频教程", "视频教程帮助中心", level1);
            level2.save();
            for (int i = 1; i < num; i++) {
                article = new TMHelpArticle(i + ". 视频教程-如何优化冷门类目宝贝标题?", "<div>优化冷门类目宝贝标题</div>", "--", level2, false);
                article.save();
            }

            level2 = new HelpNavLevel2("文字教程", "文字教程帮助中心", level1);
            level2.save();
            for (int i = 1; i < num; i++) {
                article = new TMHelpArticle(i + ". 文字教程-如何优化冷门类目宝贝标题?", "<div>优化冷门类目宝贝标题</div>", "--", level2, false);
                article.save();
            }

            level1 = new HelpNavLevel1(APIConfig.taobiaoti.getApp(), "站内引流", "站内引流化帮助中心");
            level1.save();

            level2 = null;
            level2 = new HelpNavLevel2("视频教程", "视频教程帮助中心", level1);
            level2.save();
            for (int i = 1; i < num; i++) {
                article = new TMHelpArticle(i + ". 视频教程--如何优化?", "<div>优化冷门类目宝贝标题</div>", "--", level2, false);
                article.save();
            }
            level2 = new HelpNavLevel2("文字教程", "文字教程帮助中心", level1);
            level2.save();

            return;
        }

        APIConfig.get().ensureHelpBase();

    }

    @Override
    public String toString() {
        return "HelpNavLevel1 [appkey=" + appkey + ", name=" + name + ", comment=" + comment + ", isDelete=" + isDelete
                + ", level2sNum=" + (level2s == null ? null : level2s.size()) + "]";
    }

    public int getAppkey() {
        return appkey;
    }

    public void setAppkey(int appkey) {
        this.appkey = appkey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

//    public List<HelpNavLevel2> getLevel2s() {
//        return level2s;
//    }
//
//    public void setLevel2s(List<HelpNavLevel2> level2s) {
//        this.level2s = level2s;
//    }

}
