/**
 * 
 */
package ppapi.models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.paipai.PaiPaiUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import ppapi.PaiPaiItemApi;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;

/**
 * @author navins
 * @date 2013-7-11 上午11:57:05
 */
@Entity(name = PaiPaiItemCatPlay.TABLE_NAME)
public class PaiPaiItemCatPlay extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiItemCatPlay.class);

    public static final String TAG = "PaiPaiItem";

    public static final String TABLE_NAME = "paipai_itemcat_";

    public static PaiPaiItemCatPlay _instance = new PaiPaiItemCatPlay();

    // 商品所属类目ID
    @PolicySQLGenerator.CodeNoUpdate
    @Id
    public Long cid;

    // 父类目ID=0时，代表的是一级的类目
    public Long parentCid;

    // 类目名称
    public String name;

    // 状态。可选值:normal(正常,0),deleted(删除,1)
    public int status;

    public int level;

    public PaiPaiItemCatPlay() {

    }

    public PaiPaiItemCatPlay(Long cid, Long parentCid, String name) {
        super();
        this.cid = cid;
        this.parentCid = parentCid;
        this.name = name;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getParentCid() {
        return parentCid;
    }

    public void setParentCid(Long parentCid) {
        this.parentCid = parentCid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "PaiPaiItemCatPlay [cid=" + cid + ", parentCid=" + parentCid + ", name=" + name + ", status=" + status
                + ", level=" + level + "]";
    }

    public static PaiPaiItemCatPlay findCatByCid(long cid) {
        return PaiPaiItemCatPlay.find("cid = ?", cid).first();
    }

    public static PaiPaiItemCatPlay findCatPlay(PaiPaiUser user,long cid) {
        PaiPaiItemCatPlay itemCat = PaiPaiItemCatPlay.find("cid = ?", cid).first();
        if (itemCat == null) {
            List<PaiPaiItemCatPlay> list = new PaiPaiItemApi.PaiPaiItemCatListApi(user, cid).call();
            if (CommonUtils.isEmpty(list)) {
                return null;
            }
            for (PaiPaiItemCatPlay cat : list) {
                if (cat.getCid().longValue() == cid) {
                    itemCat = cat;
                }
                cat.jdbcSave();
            }
        }
        return itemCat;
    }

    public static PaiPaiItemCatPlay findTopParentCatPlay(PaiPaiUser user,long cid) {
        PaiPaiItemCatPlay itemCat = PaiPaiItemCatPlay.find("cid = ?", cid).first();

        while (itemCat != null && itemCat.getParentCid().longValue() > 0) {
            itemCat = PaiPaiItemCatPlay.find("cid = ?", itemCat.getParentCid()).first();
        }

        if (itemCat == null) {
            List<PaiPaiItemCatPlay> list = new PaiPaiItemApi.PaiPaiItemCatListApi(user, cid).call();
            if (CommonUtils.isEmpty(list)) {
                return null;
            }
            for (PaiPaiItemCatPlay cat : list) {
                if (cat.getParentCid().longValue() == 0) {
                    itemCat = cat;
                }
                cat.jdbcSave();
            }
        }
        return itemCat;
    }
    
    @Transient
    static String EXIST_ID_QUERY = "select cid from `paipai_itemcat_` where cid  = ?";

    public static long findExistId(Long cid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY,cid);
    }
    
    @Transient
    static String insertSQL = "insert into `paipai_itemcat_`(`cid`,`parentCid`,`name`" +
    		",`status`,`level`) values(?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(insertSQL, this.cid, this.parentCid,
                this.name, this.status,this.level);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }
    
    @Transient
    static final String updateSQL = "update `paipai_itemcat_` set `cid` = ?, `parentCid` = ?, " +
    		"`name` = ?, `status` = ?, " +
    		"`level` = ? where `cid` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(updateSQL, this.cid,
                this.parentCid, this.name, this.status, this.level,this.getCid());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }

    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.cid);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

}
