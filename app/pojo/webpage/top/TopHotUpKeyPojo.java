
package pojo.webpage.top;

import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class TopHotUpKeyPojo extends WebPojo {

    @XpathFieldForm(possiblePatterns = {
            "//div[@id='crumbs']"
    })
    @JsonProperty
    public String catogery;

    @XpathFieldForm(possiblePatterns = {
            "//table[@class='textlist']/tbody/tr/td"
    }, pager = TopHotKeyParger.class)
    @JsonProperty
    public List<TopHotKeyItem> items;

    @Override
    public String toString() {
        return "TopHotUpKeyPojo [catogery=" + catogery + ", items=" + items + "]";
    }

    public String getCatogery() {
        return catogery;
    }

    public void setCatogery(String catogery) {
        this.catogery = catogery;
    }

    public List<TopHotKeyItem> getItems() {
        return items;
    }

    public void setItems(List<TopHotKeyItem> items) {
        this.items = items;
    }

}
