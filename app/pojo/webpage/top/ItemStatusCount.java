
package pojo.webpage.top;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect
public class ItemStatusCount implements Serializable {

    private static final long serialVersionUID = -213052982409164L;

    @JsonProperty
    long id = 0L;

    @JsonProperty
    int totalCount = 0;

    @JsonProperty
    int onSaleCount = 0;

    @JsonProperty
    int inStockCount = 0;

    @JsonProperty
    String name = StringUtils.EMPTY;

    @JsonProperty
    String rule = StringUtils.EMPTY;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getOnsaleCount() {
        return onSaleCount;
    }

    public void setOnsaleCount(int onsaleCount) {
        this.onSaleCount = onsaleCount;
    }

    public int getInstockCount() {
        return inStockCount;
    }

    public void setInstockCount(int instockCount) {
        this.inStockCount = instockCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public ItemStatusCount(long id, int totalCount, int onsaleCount, int instockCount) {
        super();
        this.id = id;
        this.totalCount = totalCount;
        this.onSaleCount = onsaleCount;
        this.inStockCount = instockCount;
    }

    public ItemStatusCount(long id, int totalCount, int onSaleCount, int inStockCount, String name) {
        super();
        this.id = id;
        this.totalCount = totalCount;
        this.onSaleCount = onSaleCount;
        this.inStockCount = inStockCount;
        this.name = name;
    }

    public ItemStatusCount() {
        super();
    }

}
