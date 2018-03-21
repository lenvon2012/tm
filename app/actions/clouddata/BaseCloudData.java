
package actions.clouddata;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import actions.clouddata.InstantTransRateManager.UvBaseData;

@JsonAutoDetect
public class BaseCloudData extends UvBaseData implements Serializable {

    /**
     * Payed is 
     */
    @JsonProperty
    int sale;

    @JsonProperty
    int payedSale;

    @JsonProperty
    int createdSale;

    @JsonProperty
    int amount;

    public static class BaseItemCloudData extends BaseCloudData {

    }
}
