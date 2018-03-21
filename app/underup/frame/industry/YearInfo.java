package underup.frame.industry;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.*;

@JsonAutoDetect
public class YearInfo {
    private static final Logger log = LoggerFactory.getLogger(YearInfo.class);
    public static final String TAG = new String("YearInfo");
    
    @JsonProperty
    long year;
    
    @JsonProperty
    String yearName;
    
    public long getYear(){
        return year;
    }
    
    public String getYearName(){
        return yearName;
    }
    
    public void setYear(long year){
        this.year = year;
    }
    
    public void setYearName(String yearName){
        this.yearName = yearName;
    }
    
    public YearInfo(long year, String yearName){
        this.year = year;
        this.yearName = yearName;
    }
    
}
