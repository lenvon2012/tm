
package models.op;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.GenericModel;
import transaction.TransactionSecurity;

@Entity(name = RawId.TABLE_NAME)
public class RawId extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(RawId.class);

    public static final String KEY_TAG = "RawId";

    public static final String TABLE_NAME = "rawids";

    public RawId(Long id) {
        super();
        this.id = id;
    }

    @Id
    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static Set<Long> toIds() {
        Set<Long> ids = new HashSet<Long>();
        List<RawId> findAll = RawId.findAll();
        for (RawId rawId : findAll) {
            ids.add(rawId.id);
        }
        return ids;
    }

    public static boolean hasId(final long id) {
        final String key = KEY_TAG + id;
        Boolean res = (Boolean) Cache.get(key);
//        log.info("[find the target cached raw ids key[" + key + "] : ]" + res);
        if (res != null) {
            return res.booleanValue();
        }

        res = new TransactionSecurity<Boolean>() {
            @Override
            public Boolean operateOnDB() {
                Boolean tempRes = (RawId.findById(id) != null);
                Cache.set(key, tempRes);
                return tempRes;
            }
        }.execute();

        return res.booleanValue();
    }

    public static void addId(final long id) {
        new TransactionSecurity<Void>() {
            @Override
            public Void operateOnDB() {
                if (RawId.findById(id) == null) {
                    new RawId(id).save();
                }

                String key = KEY_TAG + id;
                Cache.set(key, Boolean.TRUE);
                log.info("[find the target cached raw ids key[" + key + "] : ]" + Boolean.TRUE);

                return null;
            }
        }.execute();
    }

    public static void removeId(final long id) {
        new TransactionSecurity<Void>() {
            @Override
            public Void operateOnDB() {
                RawId dev = RawId.findById(id);
                if (dev != null) {
                    dev.delete();
                }
                String key = KEY_TAG + id;
                Cache.set(key, Boolean.FALSE);
                return null;
            }
        }.execute();
    }

}
