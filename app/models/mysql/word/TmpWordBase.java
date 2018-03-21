
package models.mysql.word;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator.CodeNoUpdate;

import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.INRecordBase;

@Entity(name = TmpWordBase.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "persistent", "entityId", "dataSrc", "tableName", "idName", "idColumn", "dataSrc", "hashColumnName", "hashed"
})
public class TmpWordBase extends GenericModel implements IWordBase {

    private static final Logger log = LoggerFactory.getLogger(TmpWordBase.class);

    public static final String TAG = "TmpWordBase";

    public static final String TABLE_NAME = "tmp_sp_word";

    @Id
    @GeneratedValue
    public Long id;

    public Long getId() {
        return id;
    }

    @Index(name = "word")
    @Column(columnDefinition = "varchar(127) default '' not null")
    @Unique
    @CodeNoUpdate
    public String word;

    @Column(columnDefinition = "int default -1")
    public Integer price = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer click = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer competition = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer cid = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer pv = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer strikeFocus = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Integer searchFocus = NumberUtil.NEVER_START;

    @JsonIgnore
    @Column(columnDefinition = "int default -1")
    public Integer ctr = NumberUtil.NEVER_START;

    @Column(columnDefinition = "int default -1")
    public Long totalPayed = Long.MIN_VALUE;

    @Column(columnDefinition = "bigint default -1")
    public Long lastINWordUpdate = NumberUtil.DEFAULT_LONG;

    @Column(columnDefinition = "int default -1")
    public Integer score = NumberUtil.DEFAULT_ZERO;

    @Column(columnDefinition = "int default -1")
    public Integer scount = NumberUtil.DEFAULT_ZERO;

    @Column(columnDefinition = "int default 0")
    public Integer status = NumberUtil.DEFAULT_ZERO;

    public TmpWordBase() {
        super();
    }

    public static TmpWordBase _instance = new TmpWordBase();

    public static String INSERT_SQL = "insert into `" + TABLE_NAME + "` (`word`) values(?);";

    static String INSERT_ONE_SQL = "insert into `"
            + TABLE_NAME
            + "`(`id`, `word`,`price`,`click`,`competition`,"
            + "`pv`,`strikeFocus`,`searchFocus`,`lastINWordUpdate`,`score`,`status`,`scount`,`cid`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

//
//    public static DBDispatcher rawwordDispatcher = new DBDispatcher(DataSrc.ITEMBUSES, _instance) {
//    };

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(false, false, DataSrc.BASIC, INSERT_ONE_SQL, this.getId(), this.word, this.price,
                this.click, this.competition, this.pv, this.strikeFocus, this.searchFocus, this.lastINWordUpdate,
                this.score, this.status, this.scount, this.cid);

        if (id >= 0L) {
//            this.setId(id);
            return true;
        } else {
            log.warn("Insert Failes.....");
            return false;
        }
    }

    public void fetchFromResultSet(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.word = rs.getString(2);
        this.price = rs.getInt(3);
        this.click = rs.getInt(4);
        this.competition = rs.getInt(5);
        this.pv = rs.getInt(6);
        this.strikeFocus = rs.getInt(7);
        this.searchFocus = rs.getInt(8);
        this.lastINWordUpdate = rs.getLong(9);
        this.score = rs.getInt(10);
        this.status = rs.getInt(11);
        this.scount = rs.getInt(12);
        this.cid = rs.getInt(13);
    }
    
    public TmpWordBase(WordBase base) {
    	super();
    	this.id = base.id;
    	this.cid = base.cid;
    	this.click = base.click;
    	this.competition = base.competition;
    	this.ctr = base.ctr;
    	this.lastINWordUpdate = base.lastINWordUpdate;
    	this.price = base.price;
    	this.pv = base.pv;
    	this.score = base.score;
    	this.scount = base.scount;
    	this.searchFocus = base.searchFocus;
    	this.status = base.status;
    	this.strikeFocus = base.strikeFocus;
    	this.totalPayed = base.totalPayed;
    	this.word = base.word;
    }
    
    static String UPDATE_QUERY_SQL = "update `" + TABLE_NAME + "` set  `price` = ?, `click` = ?, "
            + "`competition` = ?, `pv` = ?, `strikeFocus` = ?, `searchFocus` = ?, "
            + "`lastINWordUpdate` = ?, `score` = ?, `status` = ?, `scount` = ?, `cid` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false, UPDATE_QUERY_SQL, this.price, this.click, this.competition, this.pv,
                this.strikeFocus, this.searchFocus, this.lastINWordUpdate, this.score, this.status, this.scount,
                this.cid, this.getId());

        return updateNum > 0L;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }

    public Integer getCompetition() {
        return competition;
    }

    public void setCompetition(Integer competition) {
        this.competition = competition;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getPv() {
        return pv;
    }

    public void setPv(Integer pv) {
        this.pv = pv;
    }

    public Integer getStrikeFocus() {
        return strikeFocus;
    }

    public void setStrikeFocus(Integer strikeFocus) {
        this.strikeFocus = strikeFocus;
    }

    public Integer getSearchFocus() {
        return searchFocus;
    }

    public void setSearchFocus(Integer searchFocus) {
        this.searchFocus = searchFocus;
    }

    public Integer getCtr() {
        return ctr;
    }

    public void setCtr(Integer ctr) {
        this.ctr = ctr;
    }

    public Long getTotalPayed() {
        return totalPayed;
    }

    public void setTotalPayed(Long totalPayed) {
        this.totalPayed = totalPayed;
    }

    public Long getLastINWordUpdate() {
        return lastINWordUpdate;
    }

    public void setLastINWordUpdate(Long lastINWordUpdate) {
        this.lastINWordUpdate = lastINWordUpdate;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScount() {
        return scount;
    }

    public void setScount(Integer scount) {
        this.scount = scount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public float getMatch() {
        return 0;
    }

    @Override
    public void setMatch(float match) {
    }

    @Override
    public void updateByINInfo(INRecordBase record) {
    }
}
