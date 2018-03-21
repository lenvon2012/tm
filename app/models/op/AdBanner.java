
package models.op;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;

@Entity(name = AdBanner.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "userId", "entityId", "ts", "sellerCids", "tableHashKey", "persistent", "tableName", "idName", "idColumn",
        "hashed", "propsName", "dataSrc", "topCate_1", "topCate_2", "topCate_3", "topCate_4", "hashColumnName",
        "parentCid", "isParent", "parent"
})
public class AdBanner extends Model {

    public Long created;

//  @Exclude
    public Long updated;

    public static final String TABLE_NAME = "ad_banner";

    private static final Logger log = LoggerFactory.getLogger(AdBanner.class);

    public static final String TAG = "AdBanner";

    @JsonProperty
    @Index(name = "name")
    String name = StringUtils.EMPTY;

    @JsonProperty
    String href = StringUtils.EMPTY;

    @JsonProperty
    String imgPath = StringUtils.EMPTY;

    @JsonProperty
    String anchorStyle = StringUtils.EMPTY;

    @JsonProperty
    String imgStyle = StringUtils.EMPTY;

    @JsonProperty
    boolean enable = true;

    String comment = StringUtils.EMPTY;

    @Lob
    String htmlStr = StringUtils.EMPTY;
    
    public AdBanner(String name, String href, String imgPath, String anchorStyle, 
    		String imgStyle, String comment) {
        super();
        this.name = name;
        this.href = href;
        this.imgPath = imgPath;
        this.anchorStyle = anchorStyle;
        this.imgStyle = imgStyle;
        this.comment = comment;
        this.htmlStr = "";
    }
    
    public AdBanner(String name, String href, String imgPath, String anchorStyle, 
    		String imgStyle, String comment, String html) {
        super();
        this.name = name;
        this.href = href;
        this.imgPath = imgPath;
        this.anchorStyle = anchorStyle;
        this.imgStyle = imgStyle;
        this.comment = comment;
        this.htmlStr = html;
    }

    public static void ensure() {
        log.info("[ensure ad banner]");
        //AdBanner first = AdBanner.find(" 1 = 1").first();
        //if (first != null) {
        //    return;
        //}
        AdBanner left1 = AdBanner.findbyName("left1");
        if (left1 == null) {
            new AdBanner("left1", "http://to.taobao.com/aHZ5Mjy",
                "http://img01.taobaocdn.com/imgextra/i1/22902351/T2RRfuXchXXXXXXXXX_!!22902351.gif",
                "width:190px;height:50px;padding-top:5px;padding-left:3px;", "width:188px;height:48px", "左侧第一个导航")
                .save();
        }
        
        AdBanner five_yuan = AdBanner.findbyName("5YuanXufei");
        if (five_yuan == null) {
            five_yuan = new AdBanner("5YuanXufei", "", "", "", "", "5元链接");
            five_yuan.setEnable(false);
            five_yuan.save();
        }
        
        /*AdBanner three_yuan = AdBanner.findbyName("3YuanXufei");
        if (three_yuan == null) {
        	three_yuan = new AdBanner("3YuanXufei", "", "", "", "", "3元链接");
        	three_yuan.setEnable(false);
        	three_yuan.save();
        }*/
        
        AdBanner showXufei = AdBanner.findbyName("showXufei");
        if (showXufei == null) {
            showXufei = new AdBanner("showXufei", "", "", "", "", "最初版本续费弹窗");
            showXufei.setEnable(false);
            showXufei.save();
        }
        
        AdBanner fiveXingHaoPing = AdBanner.findbyName("5XingHaoPing");
        if (fiveXingHaoPing == null) {
            fiveXingHaoPing = new AdBanner("5XingHaoPing", "", "", "", "", "5星好评弹窗");
            fiveXingHaoPing.setEnable(false);
            fiveXingHaoPing.save();
        }
        
       /* AdBanner oneyuanhongbao = AdBanner.findbyName("1yuanhongbao");
        if (oneyuanhongbao == null) {
        	oneyuanhongbao = new AdBanner("1yuanhongbao", "", "", "", "", "一元红包");
        	oneyuanhongbao.setEnable(false);
        	oneyuanhongbao.save();
        }
        
        AdBanner old_three_yuan = AdBanner.findbyName("old3YuanXufei");
        if (old_three_yuan == null) {
        	old_three_yuan = new AdBanner("old3YuanXufei", "", "", "", "", "最初版本3元链接，暴力弹图片形式");
        	old_three_yuan.setEnable(false);
        	old_three_yuan.save();
        }*/
        
        AdBanner autotitle_comment = AdBanner.findbyName("autotitlecomment");
        if (autotitle_comment == null) {
        	autotitle_comment = new AdBanner("autotitlecomment", "", "", "", "", "自动标题是否显示自动评价");
        	autotitle_comment.setEnable(false);
        	autotitle_comment.save();
        }
        
        AdBanner peixun = AdBanner.findbyName("peixun");
        if (peixun == null) {
        	peixun = new AdBanner("peixun", "", "", "{\"width\":\"400\",\"height\":\"300\"}", "", "是否弹窗提示培训", "培训内容，这里要填html");
        	peixun.setEnable(false);
        	peixun.save();
        }
        
        AdBanner freeonemonth = AdBanner.findbyName("freeonemonth");
        if (freeonemonth == null) {
        	freeonemonth = new AdBanner("freeonemonth", "", "", "", "", "直接免费送一个月(20140721，爱推广)");
        	freeonemonth.setEnable(false);
        	freeonemonth.save();
        }
        
        AdBanner dazhe = AdBanner.findbyName("dazhe");
        if (dazhe == null) {
        	dazhe = new AdBanner("dazhe", "", "", "", "", "淘掌柜促销打折上线弹窗");
        	dazhe.setEnable(false);
        	dazhe.save();
        }
        
    }

    public static AdBanner findbyName(String name2) {
        return AdBanner.find("name =  ? ", name2).first();
    }

    public AdBanner() {
        super();
        created = System.currentTimeMillis();
    }
    
    public static AdBanner findXufeiBanner() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "showXufei").first();
    }
    
    public static AdBanner findAutoTitleCommentBanner() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "autotitlecomment").first();
    }
    
    public static AdBanner find1yuanhongbao() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "1yuanhongbao").first();
    }
    
    public static AdBanner find5XingBanner() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "5XingHaoPing").first();
    }
    
    public static AdBanner findFreeOneMonthBanner() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "freeonemonth").first();
    }
    
    public static AdBanner find5yuanXufei() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "5YuanXufei").first();
    }
    
    public static AdBanner find3yuanXufei() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "3YuanXufei").first();
    }

    public static AdBanner findPeixun() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "peixun").first();
    }
    
    public static AdBanner findDazhe() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "dazhe").first();
    }
    
    public static AdBanner findOld3yuanXufei() {
        // TODO Auto-generated method stub
        return AdBanner.find("name = ?", "old3YuanXufei").first();
    }
    
    @Override
    public void _save() {
        updated = System.currentTimeMillis();
        super._save();
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getAnchorStyle() {
        return anchorStyle;
    }

    public void setAnchorStyle(String anchorStyle) {
        this.anchorStyle = anchorStyle;
    }

    public String getImgStyle() {
        return imgStyle;
    }

    public void setImgStyle(String imgStyle) {
        this.imgStyle = imgStyle;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getHtmlStr() {
		return htmlStr;
	}

	public void setHtmlStr(String htmlStr) {
		this.htmlStr = htmlStr;
	}

	@Override
    public String toString() {
        return "AdBanner [created=" + created + ", updated=" + updated + ", name=" + name + ", href=" + href
                + ", imgPath=" + imgPath + ", anchorStyle=" + anchorStyle + ", imgStyle=" + imgStyle + ", enable="
                + enable + ", comment=" + comment + "]";
    }

}
