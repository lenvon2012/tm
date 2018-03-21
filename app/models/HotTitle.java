
package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.persistence.Entity;

import models.item.ItemCatPlay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.db.jpa.Model;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 12-11-5
 * Time: 下午12:27
 * To change this template use File | Settings | File Templates.
 */
@Entity(name = HotTitle.TABLE_NAME)
@JsonIgnoreProperties(value = {
    "category"
})
public class HotTitle extends Model {
    public static final String TABLE_NAME = "hottitle";

    public static final Logger log = LoggerFactory.getLogger(ItemCatPlay.class);

    public static HotTitle EMPTY = new HotTitle();

    @Index(name = "category")
    private long category;//类目

    private String title;

    public void setCategory(long category) {
        this.category = category;
    }

    public long getCategory() {
        return category;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public HotTitle() {

    }

    public HotTitle(long category, String title) {
        this.category = category;
        this.title = title;
    }

    public String toString() {
        return "HotTitle [category=" + category + ", title='" + title + "']";
    }

    public static List<HotTitle> findByCategory(long category) {
        return HotTitle.find("category=?", category).fetch();
    }

    public static void loadData() {
        if (HotTitle.count() > 0)
            return;
        File hotTitleFile = new File(Play.applicationPath, "conf/init/hotTitle.txt");
        if (hotTitleFile.exists() == false)
            return;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(hotTitleFile));
            String tempString = null;
            int rowIndex = 0;
            while ((tempString = reader.readLine()) != null) {
                rowIndex++;
                if (rowIndex == 1) {//跳过第一行
                    continue;
                }
                if (tempString == null || tempString.isEmpty()) {
                    continue;
                }
                int index = tempString.indexOf(",");
                if (index < 0) {
                    continue;
                }
                String cidStr = tempString.substring(0, index).trim();
                long cid = 0;
                try {
                    cid = Long.parseLong(cidStr);
                } catch (Exception ex) {
                    continue;
                }
                String title = tempString.substring(index + 1).trim();
                HotTitle hotTitle = new HotTitle(cid, title);
                hotTitle.save();
            }

        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    log.warn(e1.getMessage(), e1);
                }
            }
        }
    }
}
