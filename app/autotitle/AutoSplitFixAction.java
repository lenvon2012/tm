package autotitle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

public class AutoSplitFixAction {

    private static final Logger log = LoggerFactory.getLogger(AutoSplitFixAction.class);
    
    private static Map<String, String[]> FixSplitMap = new HashMap<String, String[]>();
    
    static {
        FixSplitMap.put("t恤", new String[] {"t", "恤"});
        FixSplitMap.put("polo衫", new String[] {"polo", "衫"});
        FixSplitMap.put("v领", new String[] {"v", "领"});
    }
    
    public static void doFixAutoSplit(String word, List<String> splitList) {
        
        word = toLowercase(word);
        
        if (StringUtils.isEmpty(word)) {
            return;
        }
        if (CommonUtils.isEmpty(splitList)) {
            return;
        }
        
        for (String key : FixSplitMap.keySet()) {
            if (word.contains(key) == false) {
                continue;
            }
            String[] fixArray = FixSplitMap.get(key);
            if (fixArray == null || fixArray.length <= 0) {
                continue;
            }
            for (String fixSplit : fixArray) {
                if (splitList.contains(fixSplit)) {
                    continue;
                }
                splitList.add(fixSplit);
            }
            
            splitList.remove(key);
        }
        
        
    }
    
    
    private static String toLowercase(String word) {
        if (StringUtils.isEmpty(word)) {
            return "";
        }
        
        word = word.toLowerCase();
        
        return word;
    }
    
}
