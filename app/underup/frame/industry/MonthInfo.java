package underup.frame.industry;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@JsonAutoDetect
public class MonthInfo {
    private static final Logger log = LoggerFactory.getLogger(MonthInfo.class);
    public static final String TAG = "MonthInfo";
    
    @JsonProperty
    long month;
    
    @JsonProperty
    String monthName;
    
    public MonthInfo(long month, String monthName){
        this.month = month;
        this.monthName = monthName;
    }
}
