
package models.showwindow;

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

@Entity(name = WindowMoreRecommend.TABLE_NAME)
public class WindowMoreRecommend extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(WindowMoreRecommend.class);

    public static final String KEY_TAG = "_WindowMoreRecommend";

    public static final String TABLE_NAME = "window_more_recommend";

    public WindowMoreRecommend(Long id) {
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
        List<WindowMoreRecommend> findAll = WindowMoreRecommend.findAll();
        for (WindowMoreRecommend rawId : findAll) {
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
                Boolean tempRes = (WindowMoreRecommend.findById(id) != null);
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
                if (WindowMoreRecommend.findById(id) == null) {
                    new WindowMoreRecommend(id).save();
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
                WindowMoreRecommend dev = WindowMoreRecommend.findById(id);
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
