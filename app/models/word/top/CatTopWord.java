
package models.word.top;

import static java.lang.String.format;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;

@Entity(name = CatTopWord.TABLE_NAME)
public class CatTopWord extends Model {

    private static final Logger log = LoggerFactory.getLogger(CatTopWord.class);

    public static final String TAG = "CatTopWord";

    public static final String TABLE_NAME = "cattopwords_";

    @Index(name = "cid")
    Integer cid = NumberUtil.DEFAULT_ZERO;

    Long wid = NumberUtil.DEFAULT_LONG;

    String word = StringUtils.EMPTY;

    private static boolean nullOrZero;

    public void rawInsert() {
//        long insert = JDBCBuilder.insert("insert into `" + TABLE_NAME
//                + "` (`cid`,`wid`,`word`) values(?,?,?)", cid, wid, word);
//        if (insert > 0) {
//            this.id = insert;
//        } else {
//            log.error("Insert Fails...");
//        }
        this.save();
    }

    public CatTopWord(Integer cid, String word) {
        super();
        this.cid = cid;
        this.word = word;
    }

    public CatTopWord(Integer cid, Long wid) {
        super();
        this.cid = cid;
        this.wid = wid;
    }

    public CatTopWord(Integer cid, Long wid, String word) {
        super();
        this.cid = cid;
        this.wid = wid;
        this.word = word;
    }

    public static boolean has(Integer cid) {
        if (cid == null) {
            return false;
        }
        return CatTopWord.find("cid = ?", cid).first() != null;
    }

    public static void findOrCreateByWords(Integer cid, Collection<String> words) {

        log.info(format("findOrCreateByWords:cid, words".replaceAll(", ", "=%s, ") + "=%s", cid,
                words));

        List<CatTopWord> list = CatTopWord.find("cid = ?", cid).fetch();
        if (!CommonUtils.isEmpty(list)) {
            for (CatTopWord catTopWords : list) {
                catTopWords.delete();
            }
        }
        for (String string : words) {
            new CatTopWord(cid, string).save();
        }

    }

    public static CatTopWord findByCidAndWord(Integer cid, String word) {
        return CatTopWord.find("cid = ? and word = ?", cid, word).first();
    }

    public static CatTopWord findOrCrate(Integer cid, Long wid) {
        return CatTopWord.find("cid = ? and wid = ?", cid, wid).first();
    }

    public static void findOrCreateByIds(Integer cid, Collection<Long> words) {
        List<CatTopWord> list = CatTopWord.find("cid = ?", cid).fetch();
        if (!CommonUtils.isEmpty(list)) {
            for (CatTopWord catTopWords : list) {
                catTopWords.delete();
            }
        }
        for (Long id : words) {
            new CatTopWord(cid, id).save();
        }

    }

    @Override
    public String toString() {
        return "CatTopWord [cid=" + cid + ", wid=" + wid + ", word=" + word + "]";
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Long getWid() {
        return wid;
    }

    public void setWid(Long wid) {
        this.wid = wid;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public static void findOrCreateByWords(final int cid, Map<String, Long> execute) {

        List<CatTopWord> list = CatTopWord.find("cid = ?", cid).fetch();
        if (!CommonUtils.isEmpty(list)) {
            for (CatTopWord catTopWords : list) {
                catTopWords.delete();
            }
        }

        new MapIterator<String, Long>(execute) {

            @Override
            public void execute(Entry<String, Long> entry) {
                new CatTopWord(cid, entry.getValue(), entry.getKey()).rawInsert();
            }
        }.call();

    }

    public static List<CatTopWord> findByCid(Long cid) {
        if (NumberUtil.isNullOrZero(cid)) {
            return ListUtils.EMPTY_LIST;
        }

        return CatTopWord.find("cid = ?", cid).fetch();
    }

    public static long countByCid(Integer cid) {
        if (cid == null || cid <= 0) {
            return 0L;
        }

        return CatTopWord.count("cid = ?", cid);
    }

}
