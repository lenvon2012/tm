
package pojo.webpage.top;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import models.item.ItemCatPlay;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.WordBaseBean;
import com.ciaosir.client.utils.NumberUtil;

@JsonAutoDetect
@JsonIgnoreProperties(value = {})
public class TMWordBase extends WordBaseBean {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(TMWordBase.class);

    @Transient
    public static final String TAG = "TMWordBase";

    @Transient
    @JsonProperty
    String transRate;

    @Transient
    @JsonProperty
    String clickRate;

    @Transient
    @JsonProperty
    String bidPrice;

    @Transient
    @JsonProperty
    int search;

    @Transient
    @JsonProperty
    String catName;

    public int getByProp(String prop) {
        int val = 0;
        if (prop.equals("pv")) {
            return pv;
        } else if (prop.equals("click")) {
            return click;
        } else if (prop.equals("score")) {
            return score;
        } else if (prop.equals("scount")) {
            return scount;
        }

        return val;
    }

    public TMWordBase(IWordBase iBase) {

        setPv(iBase.getPv());
        setPrice(iBase.getPrice());
        setClick(iBase.getClick());
        setScount(iBase.getScount());
        setStrikeFocus(iBase.getStrikeFocus());
        if (iBase.getScore() < 0) {
            setScore(0);
        } else {
            setScore(iBase.getScore());
        }
        setCompetition(iBase.getCompetition());
        setId(iBase.getId());
        setWord(iBase.getWord());
        setSearchFocus(iBase.getSearchFocus());
        setCid(iBase.getCid());

        setOtherIndexes();
    }

    /**
     * 经过本人反复考证，转化率大约就是  点击率的5-6倍
     * ↑ 呵呵哒
     */
    private void setOtherIndexes() {
        this.clickRate = (this.strikeFocus / 100d) + "%";
        this.transRate = (this.score / 100d) + "%";
        this.bidPrice =  "￥" + NumberUtil.doubleFormatter((double) this.price / 100d);
        if (this.getCid() == 0 || this.getCid() == -1) {
            this.catName = "-";
        } else {
            this.catName = getCatName(this.getCid());
        }

    }

    private String getCatName(long catId) {
        String catName = ItemCatPlay.findNameByCid(catId);

        if (StringUtils.isEmpty(catName)) {
            return  "-";
        } else {
            StringBuilder catNameSB = new StringBuilder(catName);
            ItemCatPlay parent = ItemCatPlay.findParent(catId);
            while (parent != null) {
                catNameSB.insert(0, parent.getName() + " > ");
                parent = ItemCatPlay.findParent(parent.getCid());
            }
            return catNameSB.toString();
        }
    }

    public static List<TMWordBase> convert(List<IWordBase> bases) {
        if (CommonUtils.isEmpty(bases)) {
            return ListUtils.EMPTY_LIST;
        }

        List<TMWordBase> res = new ArrayList<TMWordBase>();
        for (IWordBase iWordBase : bases) {
            res.add(new TMWordBase(iWordBase));
        }

        return res;
    }
}
