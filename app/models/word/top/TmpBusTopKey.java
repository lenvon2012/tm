
package models.word.top;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;

@Entity(name = TmpBusTopKey.TABLE_NAME)
public class TmpBusTopKey extends Model {

    private static final Logger log = LoggerFactory.getLogger(TmpBusTopKey.class);

    public static final String TABLE_NAME = "tmpbustopkey";

    private long catLevel1;

    private long catLevel2;

    private long catLevel3;

    private String word;

    private int pv;

    private int click;

    private int competition;

    /**
     * 点击率
     */
    int clickRate;

    public TmpBusTopKey(long catLevel1, long catLevel2, long catLevel3, String word) {
        super();
        this.catLevel1 = catLevel1;
        this.catLevel2 = catLevel2;
        this.catLevel3 = catLevel3;
        this.word = word;
    }

    public TmpBusTopKey(long catLevel1, long catLevel2, long catLevel3, String word, int pv, int click, int competition) {
        super();
        this.catLevel1 = catLevel1;
        this.catLevel2 = catLevel2;
        this.catLevel3 = catLevel3;
        this.word = word;
        this.click = click;
        this.pv = pv;
        this.competition = competition;

    }

    public TmpBusTopKey(BusTopKey bustopkey) {
    	super();
        this.catLevel1 = bustopkey.getCatLevel1();
        this.catLevel2 = bustopkey.getCatLevel2();
        this.catLevel3 = bustopkey.getCatLevel3();
        this.word = bustopkey.getWord();
        this.click = bustopkey.getClick();
        this.pv = bustopkey.getPv();
        this.competition = bustopkey.getCompetition();
        this.clickRate = bustopkey.getClickRate();
    }

    public long getCatLevel1() {
        return catLevel1;
    }

    public void setCatLevel1(long catLevel1) {
        this.catLevel1 = catLevel1;
    }

    public long getCatLevel2() {
        return catLevel2;
    }

    public void setCatLevel2(long catLevel2) {
        this.catLevel2 = catLevel2;
    }

    public long getCatLevel3() {
        return catLevel3;
    }

    public void setCatLevel3(long catLevel3) {
        this.catLevel3 = catLevel3;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public int getClick() {
        return click;
    }

    public void setClick(int click) {
        this.click = click;
    }

    public int getCompetition() {
        return competition;
    }

    public void setCompetition(int competition) {
        this.competition = competition;
    }

    public int getClickRate() {
        return clickRate;
    }

    public void setClickRate(int clickRate) {
        this.clickRate = clickRate;
    }

    @Override
    public String toString() {
        return "TmpBusTopKey [catLevel1=" + catLevel1 + ", catLevel2=" + catLevel2 + ", catLevel3=" + catLevel3
                + ", word=" + word + ", pv=" + pv + ", click=" + click + ", competition=" + competition
                + ", clickRate=" + clickRate + "]";
    }

    public static long findExistId(Long level1, Long level2, Long level3, String word) {

        String query = "select id from " + TABLE_NAME + " where catLevel1 = ? and catLevel2 = ? and catLevel3 = ? and word = ?  ";

        return JDBCBuilder.singleLongQuery(query, level1, level2, level3, word);
    }

    public boolean jdbcSave() {
        try {
            
            long existId = findExistId(this.catLevel1, this.catLevel2, this.catLevel3, this.word);
            
            if (existId <= 0) {
                return this.rawInsert();
            } else {
            	this.id = existId;
                return this.rawUpdate();
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`catLevel1`,`catLevel2`,`catLevel3`,`word`,`pv`,`click`,`competition`,`clickRate`) values(?,?,?,?,?,?,?,?)";
            
            long id = JDBCBuilder.insert(insertSQL, catLevel1, catLevel2, catLevel3, word, pv, click, competition, clickRate);
            
            if (id > 0L) {
                return true;
            } else {
                return false;
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return false;
        }
    }
    
    
    public boolean rawUpdate() {

        String updateSQL = "update `"
                + TABLE_NAME
                + "` set `pv` = ?, `click` = ?, `competition` = ?, `clickRate` = ?, `catLevel1` = ?, `catLevel2` = ?, `catLevel3` = ?, `word` = ? where id = ?";
        
        long updateNum = JDBCBuilder.update(false, updateSQL, this.pv, this.click, this.competition, this.clickRate, this.catLevel1, this.catLevel2, this.catLevel3, this.word,
                this.id);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
}
