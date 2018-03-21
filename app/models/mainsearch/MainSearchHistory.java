/**
 * 
 */
package models.mainsearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.pojo.PageOffset;

/**
 * @author navins
 * @date: Oct 30, 2013 6:28:01 PM
 */
@Entity(name = MainSearchHistory.TABLE_NAME)
public class MainSearchHistory extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(MainSearchHistory.class);

    @Transient
    public static final String TABLE_NAME = "mainsearch_history";

    @Transient
    public static final MainSearchHistory EMPTY = new MainSearchHistory();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Index(name = "userId")
    public long userId;

    // 查询时排名
    public int rank;

    // 物品id
    public long numIid;

    // 时间戳
    public long ts;

    @Column(columnDefinition = "varchar(63) default '' not null")
    public String word;

    @Column(columnDefinition = "varchar(31) default '' not null")
    public String sort;

    @Column(columnDefinition = "varchar(63)")
    public String title;
    
    @Column(columnDefinition = "varchar(63)")
    public String area;

    public String picPath;

    public MainSearchHistory() {
        this.ts = System.currentTimeMillis();
    }

    public MainSearchHistory(long userId, String word, String sort) {
        super();
        this.userId = userId;
        this.word = word;
        this.sort = sort;
        this.ts = System.currentTimeMillis();
    }
    
    public MainSearchHistory(long userId, String word, String sort, String area) {
        super();
        this.userId = userId;
        this.word = word;
        this.sort = sort;
        this.ts = System.currentTimeMillis();
        this.area = area;
    }

    public MainSearchHistory(MainSearchItemRank itemRank, String sort) {
        this.sort = sort;
        this.userId = itemRank.getSellerId();
        this.word = itemRank.getKeyword();
        this.title = itemRank.getTitle();
        this.numIid = itemRank.getNumIid();
        this.picPath = itemRank.getPicPath();
        this.rank = itemRank.getRank();
        this.area = itemRank.getArea();
        this.ts = System.currentTimeMillis();
    }
    
    public MainSearchHistory(Long userId, MainSearchItemRank itemRank, String sort) {
        this.sort = sort;
        this.userId = userId;
        this.word = itemRank.getKeyword();
        this.title = itemRank.getTitle();
        this.numIid = itemRank.getNumIid();
        this.picPath = itemRank.getPicPath();
        this.rank = itemRank.getRank();
        this.ts = System.currentTimeMillis();
    }

    public MainSearchHistory(ItemThumb thumb, int rank, String word) {
        // this.setNumIid(thumb.getId());
        // this.setSellerId(thumb.getSellerId());
        // this.setRank(rank);
        // this.setKeyword(word);
        // this.setTitle(thumb.getFullTitle());
        // this.setTs(System.currentTimeMillis());
        // this.setPicPath(thumb.getPicPath());
    }

    public MainSearchHistory(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.rank = rs.getInt(3);
        this.numIid = rs.getLong(4);
        this.ts = rs.getLong(5);
        this.word = rs.getString(6);
        this.sort = rs.getString(7);
        this.title = rs.getString(8);
        this.picPath = rs.getString(9);
        this.area = rs.getString(10);
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {
        return this.rawInsert();
    }

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `mainsearch_history`(`userId`,`rank`,`numIid`,`ts`,`word`,`sort`,`title`,`picPath`,`area`) values(?,?,?,?,?,?,?,?,?)",
                        this.userId, this.rank, this.numIid, this.ts, this.word, this.sort, this.title, this.picPath,this.area);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder
                .insert("update `mainsearch_history` set  `userId` = ?, `rank` = ?, `numIid` = ?, `ts` = ?, `word` = ?, `sort` = ?, `title` = ?, `picPath` = ? ,`area` = ? where `id` = ? ",
                        this.userId, this.rank, this.numIid, this.ts, this.word, this.sort, this.title, this.picPath,this.area,
                        this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);
            return false;
        }
    }

    public static boolean deleteMainSearchHistory(Long userId, Long id) {
        if (userId == null || id == null) {
            return false;
        }
        long deleteNum = dp.insert("delete from `mainsearch_history` where `userId` = ? and `id` = ? ", userId, id);

        if (deleteNum == 1) {
            return true;
        } else {
            log.error("delete failed...for :" + id + "[userId : ]" + userId);
            return false;
        }
    }

    static String Select_Query = "select id,userId,rank,numIid,ts,word,sort,title,picPath,area from mainsearch_history";

    public static TMResult querySearchHistory(Long userId, PageOffset po) {
        String query = Select_Query + " where userId = ? and rank > 0 order by ts desc limit ? offset ? ";
        List<MainSearchHistory> list = new JDBCBuilder.JDBCExecutor<List<MainSearchHistory>>(dp, query, userId,
                po.getPs(), po.getOffset()) {
            @Override
            public List<MainSearchHistory> doWithResultSet(ResultSet rs) throws SQLException {
                List<MainSearchHistory> list = new ArrayList<MainSearchHistory>();
                while (rs.next()) {
                    list.add(new MainSearchHistory(rs));
                }
                return list;
            }
        }.call();

        long count = countSearchHistory(userId);
        return new TMResult(list, (int) count, po);
    }

    static String Select_Count_Query = "select count(*) from mainsearch_history";

    public static long countSearchHistory(Long userId) {
        String query = Select_Count_Query + " where userId = ? and rank > 0";
        long count = dp.singleLongQuery(query, userId);
        return count;
    }

}
