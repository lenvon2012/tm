
package models.oplog;

import java.io.Serializable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.Column;
import javax.persistence.Entity;

import models.CreatedUpdatedModel;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;

import play.jobs.Every;
import play.jobs.Job;

/**
 * http://item.taobao.com/item.htm?id=35280757270
 * http://item.taobao.com/item.htm?id=35018140257
 * @author zrb
 *
 */
@Entity(name = TMWorkLog.TABLE_NAME)
public class TMWorkLog extends CreatedUpdatedModel {

    public static final String TABLE_NAME = "tm_work_log";

    String type;

    @Index(name = "userId")
    Long userId;

    @Column(columnDefinition = "varchar(8190) default null")
    String msg;

    public TMWorkLog(String type, Long userId, String msg) {
        super();
        this.type = type;
        this.userId = userId;
        this.msg = msg;
    }

    public TMWorkLog(TMWorkMsg msg2) {
        this.type = msg2.getType();
        this.msg = msg2.getMsg();
        this.userId = msg2.getUserId();
    }

    @JsonAutoDetect
    public static class TMWorkMsg implements Serializable {
        @JsonProperty
        String type;

        @JsonProperty
        String msg;

        @JsonProperty
        Long userId;

        public TMWorkMsg(Long userId, String type, String msg) {
            super();
            this.type = type;
            this.msg = msg;
            this.userId = userId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

    }

    @Every("20s")
    public static class TMWorkWritter extends Job {

        static Queue<TMWorkMsg> queue = new ConcurrentLinkedQueue<TMWorkMsg>();

        public void doJob() {
            TMWorkMsg msg = null;
            while ((msg = queue.poll()) != null) {
                new TMWorkLog(msg).save();
            }
        }

        public static void addToWritter(Long userId, String type, String msg) {
            queue.add(new TMWorkMsg(userId, type, msg));
        }

        public static Queue<TMWorkMsg> getQueue() {
            return queue;
        }
    }
}
