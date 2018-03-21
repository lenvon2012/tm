
package models.word.top;

import static java.lang.String.format;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.Unique;
import play.db.jpa.Model;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.commons.ClientException;

@Entity(name = "top_url_base")
@JsonIgnoreProperties(value = {
        "parentBaseId", "cat", "cid", "cidInt", "level", "catText", "type", "tableHashKey", "persistent",
        "entityId", "idName", "totalNum"
})
public class TopURLBase extends Model {

    private static final Logger log = LoggerFactory.getLogger(TopURLBase.class);

    public static final String TAG = "TopURLBase";

    int level;

    @Unique
    @Index(name = "url")
    String url;

    @Column(columnDefinition = "varchar(127) default ''")
    String cid = StringUtils.EMPTY;

    @Index(name = "cat")
    public String cat;

    public String catText;

    public String tag;

    public int type;

    @Index(name = "parent")
    public Long parentBaseId;

    @Column(columnDefinition = "int default 0 ")
    int itemCid = 0;

    @Column(columnDefinition = "varchar(127) default ''")
    String itemCidString = StringUtils.EMPTY;
    
    public static class Type {
        public static final int IS_EXACT_CID = 2;
    }

    public TopURLBase(int level, String url) {
        super();
        this.level = level;
        this.url = url;
    }

    public TopURLBase(int level, String url, String tag) {
        super();
        this.level = level;
        this.url = url;
        this.tag = tag;
    }

    public TopURLBase(int level, String url, String catId, String tag) {
        super();
        this.level = level;
        this.url = url;
        this.cid = catId;
        this.tag = tag;
    }

    public static List<TopURLBase> findAllByLevel(int level) {
        return TopURLBase.find("level = ?", level).fetch();
    }

    public static List<TopURLBase> findAllByLevelAndCatId(int level, String cid) {
        return TopURLBase.find("level = ? and cid = ?", level, cid).fetch();
    }

    public static int deleteByLevel(int level) {
        return TopURLBase.delete("level = ?", level);
    }

    public static int deleteByLevelAndCid(int level, String cid) {
        // TODO Auto-generated method stub
        return TopURLBase.delete("level = ? and cid = ?", level, cid);
    }

    public static int deleteByLevelAndPositiveCid(int level) {
        // TODO Auto-generated method stub
        return TopURLBase.delete("level = ? and LENGTH(cid) > 0", level);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getItemCidString() {
		return itemCidString;
	}

	public void setItemCidString(String itemCidString) {
		this.itemCidString = itemCidString;
	}

	public TopURLBase(int level, String url, String catId, String cat, String tag) {
        super();
        this.level = level;
        this.url = url;
        this.cid = catId;
        this.cat = cat;
        this.tag = tag;
    }

    public static List<TopURLBase> findAllLevel3WithCid() {
        return findAllLevel3WithCid(0);
    }

    public static List<TopURLBase> findAllLevel3WithCid(int offset) {
        return TopURLBase.find("level = 3 and length(cid) > 0").from(offset).fetch();
    }

    public String getCatText() {
        return catText;
    }

    public void setCatText(String catText) {
        this.catText = catText;
    }

    public TopURLBase updateType() {
        if (NumberUtils.isNumber(cid)) {
            this.type |= Type.IS_EXACT_CID;
        }
        return this.save();
    }

    public int getCidInt() {
        if (NumberUtils.isNumber(cid)) {
            return Integer.parseInt(cid);
        }
        return -1;
    }
    
    public int getCatInt() {
        if (NumberUtils.isNumber(cat)) {
            return Integer.parseInt(cat);
        }
        return -1;
    }

    public TopURLBase(int level, String url, String cid, String cat, String catText, String tag, int type,
            Long parentBaseId) {
        super();
        this.level = level;
        this.url = url;
        this.cid = cid;
        this.cat = cat;
        this.catText = catText;
        this.tag = tag;
        this.type = type;
        this.parentBaseId = parentBaseId;
    }

    public static TopURLBase findbyCatAndCid(String cat2, String cid2) {

        log.info(format("findbyCatAndCid:cat2, cid2".replaceAll(", ", "=%s, ") + "=%s", cat2, cid2));

        if (StringUtils.isEmpty(cid2)) {
            return TopURLBase.find("cat = ?", cat2).first();
        } else {
            return TopURLBase.find("cat = ? and cid = ?", cat2, cid2).first();
        }
    }

    public static TopURLBase findByCat(String cat) {

        log.info(format("findByCat:cat".replaceAll(", ", "=%s, ") + "=%s", cat));

        TopURLBase base = TopURLBase.find("tag = ?", cat).first();
//    	System.out.println("************");
//    	System.out.println("************");
//    	System.out.println(cat);
//    	System.out.println("************");
//    	System.out.println("************");

        return base;
    }

    public static List<TopURLBase> findByLevel2(String level2) {

        log.info("findbyLevel2:2" + level2);

        if (StringUtils.isEmpty(level2)) {
            return null;
        } else {
            //TopURLBase level2Id=TopURLBase.find("tag = ? ",level2).first();
            return TopURLBase.find("parentBaseId = ? ", Long.parseLong(level2)).fetch();
        }
    }

    public Long getParentBaseId() {
        return parentBaseId;
    }

    public void setParentBaseId(Long parentBaseId) {
        this.parentBaseId = parentBaseId;
    }

    public static void ensureAllLevel3Cid() {
        List<TopURLBase> bases = TopURLBase.find("level = 3").fetch();
        for (TopURLBase me : bases) {
            String cat = me.getCat();
            TopURLBase parent = TopURLBase.find("cat  = ?", cat).first();
            me.setParentBaseId(parent.getId());
            me.save();
        }
    }

    @Override
    public String toString() {
        return "TopURLBase [level=" + level + ", url=" + url + ", cid=" + cid + ", cat=" + cat + ", catText=" + catText
                + ", tag=" + tag + ", type=" + type + ", parentBaseId=" + parentBaseId + "]";
    }

    public int getItemCid() {
        return itemCid;
    }

    public void setItemCid(int itemCid) {
        this.itemCid = itemCid;
    }

    public static class TopUrlBaseCidUpdateJob extends Job {
        @Override
        public void doJob() {
            try {
                update(0, 128);
            } catch (ClientException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static void update(int offset, int limit) throws ClientException {
        List<TopURLBase> next = null;
        while (!CommonUtils.isEmpty((next = TopURLBase.find(" level > 1 ").from(offset).fetch(limit)))) {
            log.info("[current offset :]" + offset);
            for (TopURLBase base : next) {
            	StringBuilder builder = new StringBuilder();
            	
            	int cid = base.getCidInt();
            	if(cid != -1) {
            		builder.append(cid).append(",");
            	}
            	
            	int cat = base.getCatInt();
            	if(cat != -1) {
            		if(builder.indexOf(String.valueOf(cat)) < 0) {
            			builder.append(cat).append(",");
            		}
            		
            	}
            	String tag = base.getTag();
                if(!StringUtils.isEmpty(tag)) {
                	List<ItemCatPlay> cats = ItemCatPlay.findByName(tag);
                	if(!CommonUtils.isEmpty(cats)) { 
                		for(ItemCatPlay itemCatPlay : cats) {
                			if(builder.indexOf(String.valueOf(itemCatPlay.getCid())) < 0) {
                				builder.append(itemCatPlay.getCid()).append(",");
                			}
                			if(builder.indexOf(String.valueOf(itemCatPlay.getParentCid())) < 0) {
                				builder.append(itemCatPlay.getParentCid()).append(",");
                			}
                		}
                    }
                }
                base.setItemCidString(builder.toString());
                base.save();
                
               
            }

            offset += limit;
        }
    }
    
}
