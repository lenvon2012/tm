
package pojo.webpage.top;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.HtmlListUtils;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

@JsonAutoDetect
@JsonIgnoreProperties(value = {
        "pathIndex", "fields "
})
public class TopHotKeyItem extends WebPojo {

    private static final Logger log = LoggerFactory.getLogger(TopHotKeyItem.class);

    public static final String TAG = "TopHotKeyItem";

    @XpathFieldForm(possiblePatterns = {
        "//tr/td/span[1][@class='title']/a"
    })
    @JsonProperty
    public String title;

    @XpathFieldForm(possiblePatterns = {
        "//tr/td/span[2][@class='focus']/em"
    })
    @JsonProperty
    public String focus;

    @XpathFieldForm(possiblePatterns = {
        "//tr/td/span[3][@class='grow']/em"
    })
    @JsonProperty
    public String upGrowRate;

    @XpathFieldForm(possiblePatterns = {
        "//tr/td/span[3][@class='grow']/i"
    }, attr = "class")
    @JsonProperty
    public String upGrowRateClass;

    @XpathFieldForm(possiblePatterns = {
        "//tr/td/span[4][@class='grow']/em"
    })
    @JsonProperty
    public String upGrowRank;

    @XpathFieldForm(possiblePatterns = {
        "//tr/td/span[4][@class='grow']/i"
    }, attr = "class")
    @JsonProperty
    public String upGrowRankClass;

    @Override
    public String toString() {
        return "TopHotKeyItem [title=" + title + ", focus=" + focus + ", upGrowRate=" + upGrowRate
                + ", upGrowRateClass=" + upGrowRateClass + ", upGrowRank=" + upGrowRank + ", upGrowRankClass="
                + upGrowRankClass + "]";
    }

    public void parse(DomNode elem, String href) {
        HtmlElement hElem = (HtmlElement) elem;

        List<HtmlElement> elems;
        HtmlElement target, anchor;

        target = HtmlListUtils.getFirst(hElem.getElementsByAttribute("span", "class", "title"));
        if (target != null) {
        	this.title = StringUtils.trim(target.asText());
        	 /*anchor = HtmlListUtils.getFirst(target.getElementsByAttribute("a", "", ""));
             if (anchor != null) {
             	String link = anchor.getAttribute("href");
             	if(!StringUtils.isEmpty(link)) {
             		String word = getSearchWordsByUrldecoder(link);
             		if(!StringUtils.isEmpty(word)) {
             			this.title = StringUtils.trim(word);
             		}
             	}
                 
             }
             
             if(StringUtils.isEmpty(this.title)) {
             	this.title = StringUtils.trim(target.asText());
             }*/
        }

       
        
        target = HtmlListUtils.getFirst(hElem.getElementsByAttribute("span", "class", "focus"));
        if (target != null) {
            this.focus = StringUtils.trim(target.asText());
        }

        elems = hElem.getElementsByAttribute("span", "class", "grow");
        if (elems.size() != 2) {
            log.warn("Something wrong...:" + elems);
        }

        target = elems.get(0);
        upGrowRate = StringUtils.trim(target.asText());
        target = HtmlListUtils.getFirst(target.getElementsByTagName("i"));
        upGrowRateClass = target.getAttribute("class");

        target = elems.get(1);
        upGrowRank = StringUtils.trim(target.asText());
        target = HtmlListUtils.getFirst(target.getElementsByTagName("i"));
        upGrowRankClass = target.getAttribute("class");

    }
    
    public static String eTaoPrefixString = "http://ju.atpanel.com/?url=http://s.taobao.com/search?source=top_search&q=";

    public static int eTaoPrefixLength = eTaoPrefixString.length();
    
    public static String getSearchWordsByUrldecoder(String link) {
		try {
			if (StringUtils.isEmpty(link)) {
				return StringUtils.EMPTY;
			}
			String encoded = link.substring(eTaoPrefixLength,
					link.indexOf("&style=grid&"));
			encoded = new String(encoded.getBytes("GBK"), "GBK");
			String word;

			log.info(URLDecoder.decode(encoded, "GBK"));
			word = URLDecoder.decode(encoded, "GBK");
			return word;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return StringUtils.EMPTY;
	}
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public String getUpGrowRate() {
        return upGrowRate;
    }

    public void setUpGrowRate(String upGrowRate) {
        this.upGrowRate = upGrowRate;
    }

    public String getUpGrowRateClass() {
        return upGrowRateClass;
    }

    public void setUpGrowRateClass(String upGrowRateClass) {
        this.upGrowRateClass = upGrowRateClass;
    }

    public String getUpGrowRank() {
        return upGrowRank;
    }

    public void setUpGrowRank(String upGrowRank) {
        this.upGrowRank = upGrowRank;
    }

    public String getUpGrowRankClass() {
        return upGrowRankClass;
    }

    public void setUpGrowRankClass(String upGrowRankClass) {
        this.upGrowRankClass = upGrowRankClass;
    }

}
