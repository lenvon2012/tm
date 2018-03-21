package models.taoda;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = TaodaOrderPlay.TABLE_NAME)
public class TaodaOrderPlay extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TaodaOrderPlay.class);
    
    @Transient
    public static final String TABLE_NAME = "taoda_order_play";

    @Transient
    public static TaodaOrderPlay EMPTY = new TaodaOrderPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name = "videoId")
    private Long videoId;
    
    @Index(name = "userNick")
    private String userNick;
    
    private String priceStr;
    
    private int orderNum;
    
    private String orderTsStr;
    
    private long orderTs;
    
    private long createTs;
    
    private long updateTs;

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getPriceStr() {
        return priceStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public String getOrderTsStr() {
        return orderTsStr;
    }

    public void setOrderTsStr(String orderTsStr) {
        this.orderTsStr = orderTsStr;
    }

    public long getOrderTs() {
        return orderTs;
    }

    public void setOrderTs(long orderTs) {
        this.orderTs = orderTs;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public TaodaOrderPlay() {
        super();
    }

    public TaodaOrderPlay(Long videoId, String userNick, String priceStr,
            int orderNum, String orderTsStr, long orderTs) {
        super();
        this.videoId = videoId;
        this.userNick = userNick;
        this.priceStr = priceStr;
        this.orderNum = orderNum;
        this.orderTsStr = orderTsStr;
        this.orderTs = orderTs;
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
    public void setId(Long id) {
        this.id = id;
    }


    @Override
    public String getIdName() {
        return TABLE_NAME;
    }
    
    public static long findExistId(Long videoId, String userNick, long orderTs) {
        
        String query = "select id from " + TABLE_NAME + " where videoId = ? and userNick = ? and orderTs = ? ";
        
        return dp.singleLongQuery(query, videoId, userNick, orderTs);
    }

    public static boolean hasExisted(Long videoId, String userNick, long orderTs) {
        long existdId = findExistId(videoId, userNick, orderTs);
        
        return existdId > 0L;
    }
    
    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.videoId, this.userNick, this.orderTs);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {

        String insertSQL = "insert into `" + TABLE_NAME + "`" +
                "(`videoId`,`userNick`,`priceStr`,`orderNum`,`orderTsStr`,`orderTs`," +
                "`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.videoId, this.userNick, this.priceStr, this.orderNum, this.orderTsStr, this.orderTs,
                this.createTs, this.updateTs);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }
    
    public boolean rawUpdate() {
        
        String updateSQL = "update `" + TABLE_NAME + "` set  " +
                " `priceStr` = ?, `orderNum` = ?, `orderTsStr` = ?, `updateTs` = ? " +
                " where `id` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.priceStr, this.orderNum, this.orderTsStr, this.updateTs, 
                this.id);

        if (updateNum == 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    
    
}
