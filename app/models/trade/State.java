
package models.trade;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import models.SimpleStringModel;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

@Entity(name = State.TABLE_NAME)
public class State extends GenericModel implements SimpleStringModel {

    public static final String TABLE_NAME = "receiver_state";

    @Id
    @Column(columnDefinition = "int NOT NULL AUTO_INCREMENT")
    public Integer id;

    public String state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public State(Integer id, String state) {
        super();
        this.id = id;
        this.state = state;
    }

    public static Map<String, Integer> stateCache = new HashMap<String, Integer>();

    public static State findOrCreate(String state) {
        if (StringUtils.isEmpty(state)) {
            return null;
        }

        long id = JDBCBuilder.singleLongQuery("select id from receiver_state where `state` = ?",
                state);
        if (id > 0) {
            return new State(Integer.valueOf((int) id), state);
        }
        id = JDBCBuilder.insert("insert into receiver_state (`state`) values(?)", state);
        return new State(Integer.valueOf((int) id), state);

    }

    public static synchronized Integer getId(String state) {
        if (StringUtils.isEmpty(state)) {
            return 0;
        }

        Integer id = stateCache.get(state);
        if (id != null) {
            return id;
        }

        State instance = findOrCreate(state);
        if (stateCache.size() > MAX_CACHE_SIZE) {
            stateCache.clear();
        }
        stateCache.put(state, instance.getId());

        return instance.getId();
    }
}
