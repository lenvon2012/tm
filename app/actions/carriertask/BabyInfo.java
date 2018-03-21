package actions.carriertask;

/**
 * Created by Administrator on 2016/3/8.
 */
public class BabyInfo {

    private String url;

    private String babyTitle;

    private String picUrl;

    public BabyInfo() {
    }

    public BabyInfo(String url, String babyTitle, String picUrl) {
        this.url = url;
        this.babyTitle = babyTitle;
        this.picUrl = picUrl;
    }

    public String getBabyTitle() {
        return babyTitle;
    }

    public void setBabyTitle(String babyTitle) {
        this.babyTitle = babyTitle;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
