
package models.visit;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import jdbcexecutorwrapper.JDBCLongSetExecutor;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.DateUtil;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;

@Entity(name = TidLineZingBind.TABLE_NAME)
public class TidLineZingBind extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(TidLineZingBind.class);

    public static final String TAG = "TidLineZingBind";

    public static final String TABLE_NAME = "tid_linezing_record";

    int day;

    String uv;

    @Id
    Long tid;

    Long userId;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUv() {
        return uv;
    }

    public void setUv(String uv) {
        this.uv = uv;
    }

    static DataSrc src = DataSrc.RDS;

    private TidLineZingBind(int day, String uv, Long tid, Long userId) {
        super();
        this.day = day;
        this.uv = uv;
        this.tid = tid;
        this.userId = userId;
    }

    public static void addBind(Long tid, Long userId, String uv, int day) {
        String sql = " select tid  from " + TABLE_NAME + " where tid = ?";
        boolean exist = JDBCBuilder.singleLongQuery(src, sql, tid) > 0L;
        if (exist) {
            return;
        }

        TidLineZingBind model = new TidLineZingBind(day, uv, tid, userId);
        model.rawInsert();
    }

    public boolean rawInsert() {
        String sql = " insert into " + TABLE_NAME + " (tid, uv, userId, day) values(?,?,?,?)";
        long num = JDBCBuilder.insert(src, sql, tid, uv, userId, day);

        return num > 0L;
    }

    public static Set<Long> findTodayBinded(User user) {
        int day = DateUtil.formDay(System.currentTimeMillis());
        Set<Long> bindedTids = new JDBCLongSetExecutor(src, "select tid from " + TABLE_NAME
                + " where userId = ? and day = ?", user.getId(), day).call();
        return bindedTids;
    }
}
