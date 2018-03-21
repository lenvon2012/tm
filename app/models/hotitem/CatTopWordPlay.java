package models.hotitem;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.item.ItemCatPlay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.pojo.PageOffset;
import com.taobao.api.internal.util.StringUtils;


//@Entity(name = CatTopWordPlay.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn"
})
public class CatTopWordPlay extends Model implements PolicySQLGenerator, Serializable {

    private final static Logger log = LoggerFactory.getLogger(CatTopWordPlay.class);
    
    public static final String TABLE_NAME = "cat_top_word_play";
    

    public static CatTopWordPlay EMPTY = new CatTopWordPlay();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name = "cid")
    private Long cid;
    
    @Index(name = "cidLevel1")
    private Long cidLevel1 = 0L;
    
    @Index(name = "cidLevel2")
    private Long cidLevel2 = 0L;
    
    @Index(name = "cidLevel3")
    private Long cidLevel3 = 0L;
    
    private String word;

    private int pv;

    private int click;

    private int competition;
    
    private int ctrInt;
    
    private int price;
    
    private int itemCount;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getCidLevel1() {
        return cidLevel1;
    }

    public void setCidLevel1(Long cidLevel1) {
        this.cidLevel1 = cidLevel1;
    }

    public Long getCidLevel2() {
        return cidLevel2;
    }

    public void setCidLevel2(Long cidLevel2) {
        this.cidLevel2 = cidLevel2;
    }

    public Long getCidLevel3() {
        return cidLevel3;
    }

    public void setCidLevel3(Long cidLevel3) {
        this.cidLevel3 = cidLevel3;
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

    public int getCtrInt() {
        return ctrInt;
    }

    public void setCtrInt(int ctrInt) {
        this.ctrInt = ctrInt;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public CatTopWordPlay() {
        super();
    }
    
    private void initByItemCat(ItemCatPlay itemCat) {
        cidLevel1 = 0L;
        cidLevel2 = 0L;
        cidLevel3 = 0L;
        if (itemCat == null) {
            return;
        }
        while (itemCat != null) {
            int catLevel = itemCat.getLevel();
            if (catLevel <= 1) {
                cidLevel1 = itemCat.getCid();
                break;
            } else if (catLevel == 2) {
                cidLevel2 = itemCat.getCid();
            } else if (catLevel == 3) {
                cidLevel3 = itemCat.getCid();
            }
            itemCat = ItemCatPlay.findByCid(itemCat.getParentCid());
        }
        
        
    }

    public CatTopWordPlay(Long cid, ItemCatPlay itemCat, String word) {
        super();
        this.cid = cid;
        initByItemCat(itemCat);
        this.word = word;
    }
    
    public void updateByWordBase(IWordBase wordBase) {
        if (wordBase == null) {
            return;
        }
        pv = wordBase.getPv() == null ? 0 : wordBase.getPv();
        click = wordBase.getClick() == null ? 0 : wordBase.getClick();
        competition = wordBase.getCompetition() == null ? 0 : wordBase.getCompetition();
        price = wordBase.getPrice() == null ? 0 : wordBase.getPrice();
        
        if (pv > 0) {
            ctrInt = (int) Math.round(((double) click) * 10000 / pv);
        } else {
            ctrInt = 0;
        }
        
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public String getIdName() {
        return "id";
    }
    
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static long findExistId(Long cid, String word) {

        String query = "select id from " + TABLE_NAME + " where cid = ? and word  = ?";

        return dp.singleLongQuery(query, cid, word);
    }
    
    
    public boolean rawDelete() {
        try {
            String sql = "delete from " + TABLE_NAME + " where id = ? and cid = ? ";
            long deleteNum = dp.update(sql, id, cid);
            
            return deleteNum > 0L;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }
    
    @Override
    public boolean jdbcSave() {
        try {
            long existId = findExistId(this.cid, word);

            if (existId <= 0) {
                return this.rawInsert();
            } else {
                setId(existId);
                return this.rawUpdate();
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    
    public boolean rawInsert() {
        try {

            String insertSQL = "insert into `" + TABLE_NAME + "`(`cid`,`cidLevel1`,`cidLevel2`,`cidLevel3`,"
                    + "`word`,`pv`,`click`,`competition`,`ctrInt`,`price`,`itemCount`) " 
                    + " values(?,?,?,?,?,?,?,?,?,?,?)";

            long id = dp.insert(insertSQL, this.cid, this.cidLevel1, this.cidLevel2, this.cidLevel3, 
                    this.word, this.pv, this.click, this.competition, this.ctrInt, this.price, this.itemCount);

            if (id > 0L) {
                setId(id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {

            log.error(e.getMessage(), e);
            return false;

        }

    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + TABLE_NAME
                + "` set `cidLevel1` = ?, `cidLevel2` = ?, `cidLevel3` = ?, "
                + "`pv` = ?, `click` = ?, `competition` = ?, `ctrInt` = ?, `price` = ?, `itemCount` = ?"
                + " where `cid` = ? and word = ? ";

        long updateNum = dp.insert(updateSQL, this.cidLevel1, this.cidLevel2, this.cidLevel3, 
                this.pv, this.click, this.competition, this.ctrInt, this.price, this.itemCount, 
                this.cid, this.word);

        if (updateNum > 0) {

            return true;
        } else {

            return false;
        }
    }
    
    public static List<CatTopWordPlay> findWordsByCid(Long cid) {
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where cid = ? ";
        
        return findListByJDBC(query, cid);
    }
    
    public static List<CatTopWordPlay> findWordsByCidsWithPaging(Long firstCid, Long secondCid, Long thirdCid, 
            PageOffset po, 
            String orderBy, boolean isDesc) {
        
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = "pv";
        }
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where cidLevel1 = ? and cidLevel2 = ? and cidLevel3 = ? order by " + orderBy + " ";
        if (isDesc == true) {
            query += " desc ";
        } else {
            query += " asc ";
        }
        
        query += " limit ?, ? ";
        
        return findListByJDBC(query, firstCid, secondCid, thirdCid, po.getOffset(), po.getPs());
    }
    
    public static long countByCids(Long firstCid, Long secondCid, Long thirdCid) {
        String query = " select count(*) from " + TABLE_NAME + " where cidLevel1 = ? " +
        		" and cidLevel2 = ? and cidLevel3 = ? ";
        
        return dp.singleLongQuery(query, firstCid, secondCid, thirdCid);
    }
    
    public static long countNoBaseWords() {
        String query = " select count(*) from " + TABLE_NAME + " where pv <= ? ";
        
        return dp.singleLongQuery(query, 0);
    }
    
    public static long countNoItemCountWords() {
        String query = " select count(*) from " + TABLE_NAME + " where itemCount <= ? ";
        
        return dp.singleLongQuery(query, 0);
    }
    
    //没有行业数据的词
    public static List<CatTopWordPlay> findNoBaseWords(Long startId, int limitNum) {
        if (startId == null) {
            startId = 0L;
        }
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where pv <= ? and id > ? order by id asc limit ?, ? ";
        return findListByJDBC(query, 0, startId, 0, limitNum);
    }
    
    public static List<CatTopWordPlay> findNoItemCountWords(Long startId, int limitNum) {
        
        if (startId == null) {
            startId = 0L;
        }
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where itemCount <= ? and id > ? order by id asc limit ?, ? ";
        return findListByJDBC(query, 0, startId, 0, limitNum);
    }
    
    
    private static List<CatTopWordPlay> findListByJDBC(String query, Object...params) {
        return new JDBCBuilder.JDBCExecutor<List<CatTopWordPlay>>(dp, query, params) {

            @Override
            public List<CatTopWordPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<CatTopWordPlay> wordList = new ArrayList<CatTopWordPlay>();
                
                while (rs.next()) {
                    CatTopWordPlay wordPlay = parseCatTopWordPlay(rs);
                    if (wordPlay != null) {
                        wordList.add(wordPlay);
                    }
                }
                
                return wordList;
                
            }
            
        }.call();
    }
    
    
    private static final String SelectAllProperty = " id, cid,cidLevel1,cidLevel2,cidLevel3,"
                    + "word,pv,click,competition,ctrInt,price,itemCount ";
    
    private static CatTopWordPlay parseCatTopWordPlay(ResultSet rs) {
        try {
            
            CatTopWordPlay wordPlay = new CatTopWordPlay();
            wordPlay.setId(rs.getLong(1));
            wordPlay.setCid(rs.getLong(2));
            wordPlay.setCidLevel1(rs.getLong(3));
            wordPlay.setCidLevel2(rs.getLong(4));
            wordPlay.setCidLevel3(rs.getLong(5));
            wordPlay.setWord(rs.getString(6));
            wordPlay.setPv(rs.getInt(7));
            wordPlay.setClick(rs.getInt(8));
            wordPlay.setCompetition(rs.getInt(9));
            wordPlay.setCtrInt(rs.getInt(10));
            wordPlay.setPrice(rs.getInt(11));
            wordPlay.setItemCount(rs.getInt(12));
            
            return wordPlay;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}
