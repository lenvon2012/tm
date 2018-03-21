
package transaction;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MapIterator<K, V> implements Callable<Void> {

    private static final Logger log = LoggerFactory.getLogger(MapIterator.class);

    public static final String TAG = "HashMapIterator";

    Map<K, V> map;

    int maxIndex;

    String tag;

    boolean isLoggable;

    int batchNum;

    public MapIterator(Map<K, V> map) {
        this.map = map;
        this.maxIndex = map.size() - 1;
        this.tag = StringUtils.EMPTY;
        this.isLoggable = false;
        this.batchNum = Integer.MAX_VALUE;
    }

    public MapIterator(Map<K, V> map, String tag, boolean isLoggable, int batchNum) {
        this.map = map;
        this.maxIndex = map.size() - 1;
        this.tag = tag;
        this.isLoggable = isLoggable;
        this.batchNum = batchNum;
    }

    public abstract void execute(Entry<K, V> entry);

    public Void call() {
        int count = 0;
        Set<Entry<K, V>> entrySet = map.entrySet();
        Iterator<Entry<K, V>> it = entrySet.iterator();
        while (it.hasNext()) {
            if (isLoggable && (count++ % batchNum == 0 || count == maxIndex)) {
                log.info(String.format("[%s] done number : [%s] with max index [%s]", tag, count,
                        maxIndex));
            }
            Entry<K, V> entry = it.next();
            execute(entry);
        }
        return null;
    }

}
