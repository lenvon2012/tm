
package models.op;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = TMRefundName.TABLE_NAME)
//@Table(name = TMRefundName.TABLE_NAME, uniqueConstraints = {
//        @UniqueConstraint(columnNames = {
//                "app", "wangwang"
//        }),
//})
public class TMRefundName extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(TMRefundName.class);

    @Transient
    public static final String TAG = "SelfRefundName";

    @Transient
    public static final String TABLE_NAME = "tm_refund_trade";

    public static final TMRefundName EMPTY = new TMRefundName();

    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Enumerated(EnumType.STRING)
    String app;

    @Enumerated(EnumType.STRING)
    String status;

    @Index(name = "wangwang")
    String wangwang;

    double amount;

    long created;

    //处理日期
    long updated;

    //发起人
    String upname;

    String reason;

    //审核人
    String assessor;

    String advice;

    public TMRefundName() {

    }

    public TMRefundName(long id, String wangwang, long created, long updated, String upname, String reason,
            String assessor, String app, String status, double amount, String advice) {
        this.id = id;
        this.wangwang = wangwang;
        this.app = app;
        this.upname = upname;
        this.reason = reason;
        this.assessor = assessor;
        this.created = created;
        this.updated = updated;
        this.status = status;
        this.amount = amount;
        this.advice = advice;

    }

    public TMRefundName(String wangwang, long created, long updated, String upname, String reason, String assessor,
            String app, String status, double amount, String advice) {
        this.wangwang = wangwang;
        this.created = created;
        this.updated = updated;
        this.app = app;
        this.upname = upname;
        this.reason = reason;
        this.assessor = assessor;
        this.status = status;
        this.amount = amount;
        this.advice = advice;

    }

    public TMRefundName(String wangwang, String upname, String reason, String app, double amount) {
        this.wangwang = wangwang;
        this.upname = upname;
        this.reason = reason;
        this.app = app;
        //创建退款交易, 创建时间
        this.created = System.currentTimeMillis();
        this.amount = amount;
        this.status = "已提交";
    }

    public String getWangwang() {
        return wangwang;
    }

    public void setWangwang(String wangwang) {
        this.wangwang = wangwang;
    }

    public String getUpname() {
        return upname;
    }

    public void setUpname(String upname) {
        this.upname = upname;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAssessor() {
        return assessor;
    }

    public void setAssessor(String assessor) {
        this.assessor = assessor;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return "TABLE_NAME";
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
        this.id = id;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Transient
    static String EXIST_ID_QUERY = "select id from `tm_refund_trade` where wangwang  = ? and created = ?";

    public static long findExistId(String wangwang, long created) {
        return dp.singleLongQuery(EXIST_ID_QUERY, wangwang, created);
    }

    @Transient
    static String insertSQL = "insert into `tm_refund_trade`(`wangwang`,`created`,`updated`,`upname`,`reason`,`assessor`,`app`,`status`,`amount`,`advice`) values(?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(insertSQL, this.wangwang, this.created, this.updated,
                this.upname, this.reason, this.assessor, this.app, this.status, this.amount, this.advice);

        if (id != 0L) {
            return true;
        } else {
            return false;
        }
    }

//    @Transient
//	String updateIdSQL="update `tm_refund_trade` set  `updated` = ?, `assessor` = ?, `status` = ? where `id` = ? ";
//    
//    public boolean updateId(long id,String assessor){
//    	
//    	long updated=System.currentTimeMillis();
//    	
//    	String status="已经审核处理";
//    	
//    	long updateNum = dp.insert(updateIdSQL,updated,assessor,status,id);
//    	
//    	 if (updateNum > 0L) {
//             // log.info("Update Order Display OK:" + tid);
//             return true;
//         } else {
//             // log.warn("Update Fails... for :" + tid);
//             return false;
//         }
//    	
//    }

    @Transient
    static final String updateSQL = "update `tm_refund_trade` set  `wangwang` = ?, `created` = ?, `updated` = ?, `upname` = ?, `reason` = ?, `assessor` = ?, `app` = ?, `status` = ?, `amount` = ?, `advice` = ? where `id` = ? ";

    public boolean rawUpdate() {
        long updateNum = dp.insert(updateSQL, this.wangwang, this.created, this.updated,
                this.upname, this.reason, this.assessor, this.app, this.status, this.amount, this.advice,
                this.getId());

        if (updateNum > 0L) {
            // log.info("Update Order Display OK:" + tid);
            return true;
        } else {
            // log.warn("Update Fails... for :" + tid);
            return false;
        }
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.wangwang, this.created);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                this.setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

}
