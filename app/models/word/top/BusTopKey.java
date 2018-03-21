
package models.word.top;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.persistence.Entity;

import job.writter.BusTopKeyUpdateWritter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import play.jobs.Job;
import transaction.JDBCBuilder;
import utils.PolicyDBUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.commons.ClientException;

import dao.topbuskey.TopBusKeyListDao;

@Entity(name = BusTopKey.TABLE_NAME)
public class BusTopKey extends Model {

    private static final Logger log = LoggerFactory.getLogger(BusTopKey.class);

    public static final String TABLE_NAME = "bustopkey";

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

    public BusTopKey(long catLevel1, long catLevel2, long catLevel3, String word) {
        super();
        this.catLevel1 = catLevel1;
        this.catLevel2 = catLevel2;
        this.catLevel3 = catLevel3;
        this.word = word;
    }

    public BusTopKey(long catLevel1, long catLevel2, long catLevel3, String word, int pv, int click, int competition) {
        super();
        this.catLevel1 = catLevel1;
        this.catLevel2 = catLevel2;
        this.catLevel3 = catLevel3;
        this.word = word;
        this.click = click;
        this.pv = pv;
        this.competition = competition;

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
        return "BusTopKey [catLevel1=" + catLevel1 + ", catLevel2=" + catLevel2 + ", catLevel3=" + catLevel3
                + ", word=" + word + ", pv=" + pv + ", click=" + click + ", competition=" + competition
                + ", clickRate=" + clickRate + "]";
    }

    public static void saveKey(long catLevel1, long catLevel2, long catLevel3, String word) {

        log.info(format("saveKey:catLevel1, catLevel2, catLevel3, word".replaceAll(", ", "=%s, ") + "=%s", catLevel1,
                catLevel2, catLevel3, word));
        //BusTopKey key = BusTopKey.find("catLevel1 = ? and catLevel2 = ? and catLevel3 = ? and word = ?", catLevel1,
        //        catLevel2, catLevel3, word).first();
        BusTopKey key = BusTopKey.findByLevelAndWord(catLevel1, catLevel2, catLevel3, word);
        if (key == null) {
            new BusTopKey(catLevel1, catLevel2, catLevel3, word).jdbcSave();
        }
    }

    public static class BusTopKeyWordBaseUpdateJob extends Job {
        @Override
        public void doJob() {
            try {
                update(0, 128);
            } catch (ClientException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static class BusTopKeyTrimJob extends Job {
        @Override
        public void doJob() {
            try {
                bustopkeytrim(0, 128);
            } catch (ClientException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    public static List<BusTopKey> findWithLimit(int offset, int limit) {

        String query = "select catLevel1, catLevel2, catLevel3, word, pv, click, competition, clickRate from " + TABLE_NAME
                + " limit ?,? ";

        return new JDBCBuilder.JDBCExecutor<List<BusTopKey>>(query, offset, limit) {

            @Override
            public List<BusTopKey> doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<BusTopKey> list = new ArrayList<BusTopKey>();
                while (rs.next()) {
                	BusTopKey bustop = new BusTopKey(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4),
                			rs.getInt(5), rs.getInt(6), rs.getInt(7));
                	if(bustop != null){
                		bustop.setClickRate(rs.getInt(8));
                	}
                	list.add(bustop);
                }
                return list;
            }

        }.call();
    }
    
    public static BusTopKey findByLevelAndWord(Long level1, Long level2, Long level3, String word) {

        String query = "select catLevel1, catLevel2, catLevel3, word, pv, click, competition, clickRate from " + TABLE_NAME
                + " where catLevel1 = ? and catLevel2 = ? and catLevel3 = ? and word = ? ";

        return new JDBCBuilder.JDBCExecutor<BusTopKey>(query, level1, level2, level3, word) {

            @Override
            public BusTopKey doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                	BusTopKey bustop = new BusTopKey(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4),
                			rs.getInt(5), rs.getInt(6), rs.getInt(7));
                	bustop.setClickRate(rs.getInt(8));
					return bustop;
                } else {
                	return null;
                }
            }

        }.call();
    }
    
    public static BusTopKey findFirst() {

        String query = "select catLevel1, catLevel2, catLevel3, word, pv, click, competition, clickRate from " + TABLE_NAME
                + " where 1=1 limit 1 ";

        return new JDBCBuilder.JDBCExecutor<BusTopKey>(query) {

            @Override
            public BusTopKey doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                	BusTopKey bustop = new BusTopKey(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4),
                			rs.getInt(5), rs.getInt(6), rs.getInt(7));
                	bustop.setClickRate(rs.getInt(8));
					return bustop;
                } else {
                	return null;
                }
            }

        }.call();
    }
    
    public static void bustopkeytrim(int offset, int limit) throws ClientException {
        List<BusTopKey> next = null;
        //while (!CommonUtils.isEmpty((next = BusTopKey.find(" 1 = 1").from(offset).fetch(limit)))) {
        while (!CommonUtils.isEmpty((next = BusTopKey.findWithLimit(offset,limit)))) {
            log.info("[current offset :]" + offset);
            List<FutureTask<String>> tasks = new ArrayList<FutureTask<String>>();
            for (BusTopKey wordBase : next) {
                String word = wordBase.getWord();
                if (!word.equals(word.replaceAll(" ", "").trim())) {
                    //log.info("need to trim bustopkey word "+word);
                    if (TopBusKeyListDao.wordExisted(word)) {
                        log.info("need to delete word " + wordBase.getWord() + ".........");
                        boolean success = TopBusKeyListDao.deleteBustopkey(wordBase);
                        if (success) {
                            log.info("delete successful for bustopkey word " + wordBase.getWord() + ".....");
                        } else {
                            log.info("delete failed for bustopkey word " + wordBase.getWord() + ".....");
                        }
                        /*BustopkeyTrimCaller caller = new BustopkeyTrimCaller(wordBase);
                        FutureTask<String> task = TMConfigs.getStrPool().submit(caller);
                        tasks.add(task);
                        log.info("bustopkey word "+word+" is added into Future task");*/
                    } /*else {
                        wordBase.setWord(word.replaceAll(" ", "").trim());
                        wordBase.save();
                        log.info("bustopkey word "+word+" is updated to "+ word.trim());
                      }*/
                }
            }
            for (FutureTask<String> futureTask : tasks) {
                try {
                    futureTask.get();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }

            offset += limit;
            log.info("[offset :]" + offset);
        }
    }

    public static void update(int offset, int limit) throws ClientException {
        List<BusTopKey> next = null;
        while (!CommonUtils.isEmpty((next = BusTopKey.findWithLimit(offset,limit)))) {
            log.info("[current offset :]" + offset);
            List<String> words = new ArrayList<String>();
            for (BusTopKey wordBase : next) {
                words.add(wordBase.getWord());
            }
            Map<String, IWordBase> execute = new WidAPIs.WordBaseAPI(words).execute();
            for (BusTopKey wordBase : next) {
                IWordBase iWordBase = execute.get(wordBase.getWord());
                if (iWordBase != null) {
                	wordBase.updateByWordBaseBean(iWordBase);
                }
                
                BusTopKeyUpdateWritter.addMsg(wordBase);
            }

            offset += limit;
        }
    }

    private void updateByWordBaseBean(IWordBase iWordBase) {
        this.pv = iWordBase.getPv();
        this.click = iWordBase.getClick();
        this.competition = iWordBase.getCompetition();
        this.clickRate = (int) Math.floor((this.click * 10000.0D / this.pv));
    }

    public static void ensure() {
        //BusTopKey first = BusTopKey.find("1 =1 ").first();
    	BusTopKey first = BusTopKey.findFirst();
        if (first != null) {
            return;
        }
        try {
            PolicyDBUtil.loadSqlFile("buscatplay.sql");
            PolicyDBUtil.loadSqlFile("bustopkey.sql");
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

    }

    public static class BustopkeyTrimCaller implements Callable<String> {

        BusTopKey bustopkey;

        public BustopkeyTrimCaller(BusTopKey bustopkey) {
            super();
            this.bustopkey = bustopkey;
        }

        @Override
        public String call() throws Exception {
            boolean success = TopBusKeyListDao.deleteBustopkey(bustopkey);
            if (success) {
                log.info("delete successful for bustopkey word " + bustopkey.getWord() + ".....");
            } else {
                log.info("delete failed for bustopkey word " + bustopkey.getWord() + ".....");
            }
            return null;
        }

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
