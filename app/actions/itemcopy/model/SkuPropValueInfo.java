package actions.itemcopy.model;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class SkuPropValueInfo {
    String vid;
    String name;
    String image;

    public SkuPropValueInfo(String vid, String name) {
        this.vid = vid;
        this.name = name;
    }

    public SkuPropValueInfo(String vid, String name, String image) {
        this.vid = vid;
        this.name = name;
        this.image = image;
    }

    public String getVid() {
        return vid;
    }

    public SkuPropValueInfo setVid(String vid) {
        this.vid = vid;
        return this;
    }

    public String getName() {
        return name;
    }

    public SkuPropValueInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public SkuPropValueInfo setImage(String image) {
        this.image = image;
        return this;
    }
}
