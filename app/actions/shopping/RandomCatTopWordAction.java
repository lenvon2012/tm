package actions.shopping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

public class RandomCatTopWordAction {

    private static final Logger log = LoggerFactory.getLogger(RandomCatTopWordAction.class);
    
    private static boolean hasReadFromFile = false;
    
    private static List<String> catTopWordList = new ArrayList<String>();
    
    private static final File TopWordFile = new File(TMConfigs.autoDir, "topword50w.txt");
    
    private synchronized static void initCatTopWordList() {
        try {
            if (hasReadFromFile == true || CommonUtils.isEmpty(catTopWordList) == false) {
                return;
            }
            long startTime = System.currentTimeMillis();
            hasReadFromFile = true;
            catTopWordList = FileUtils.readLines(TopWordFile);
            long endTime = System.currentTimeMillis();
            log.warn("end init cat top words, total size: " + catTopWordList.size() 
                    + ", used " + (endTime - startTime) + " ms------------------------------");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    }
    
    
    public static List<String> doRandomWords(int limitNum) {
        initCatTopWordList();
        
        List<String> wordList = new ArrayList<String>();
        int totalTime = limitNum * 100;
        while (wordList.size() < limitNum && wordList.size() < catTopWordList.size() && totalTime > 0) {
            totalTime--;
            
            int length = catTopWordList.size();
            int index = (int) (Math.random() * length);
            
            String word = catTopWordList.get(index);
            if (wordList.contains(word)) {
                continue;
            }
            wordList.add(word);
        }
        
        return wordList;
        
    }
    
}
