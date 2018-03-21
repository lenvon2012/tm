
package models.mysql.word;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;

@Entity(name = MyLexicon.TABLE_NAME)
public class MyLexicon extends Model {

    private static final Logger log = LoggerFactory.getLogger(MyLexicon.class);

    public static final String TABLE_NAME = "lexicon";

    @Index(name = "myWordId")
    @Column(columnDefinition = "bigInt(20) default 0")
    public Long myWordId = NumberUtils.LONG_ZERO;

    @Index(name = "userId")
    @Column(columnDefinition = "bigInt(20) default 0")
    public Long userId = NumberUtils.LONG_ZERO;

//    static String INSERT_ONE_SQL = "insert into `" + TABLE_NAME + "`(`myWordId`, `userId`) values(?,?)";

    public Long getMyWordId() {
        return myWordId;
    }

    public void setMyWordId(Long myWordId) {
        this.myWordId = myWordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

//
//    @Override
//    public String toString() {
//        return "MyLexicon [myWordId=" + myWordId + ", userId=" + userId + "]";
//    }

    public static List<WordBase> searchAll(Long userId) {
        List<MyLexicon> mywords = MyLexicon.find("userId = ?", userId).fetch();
        List<WordBase> wordlist = new ArrayList<WordBase>();
        for (MyLexicon myword: mywords) {
			Long wordId = myword.myWordId;
			WordBase word = WordBase.searchAWord(wordId);
			if(word != null){
			    wordlist.add(word);
			}
		}
        return wordlist;
    }

    public static int countSearchAll(Long userId) {
        return (int) MyLexicon.count("userId = ?", userId);
    }

    @Override
    public String toString() {
        return "MyLexicon [myWordId=" + myWordId + ", userId=" + userId + "]";
    }

    public static int getTotalHits(Long userId) {
        return countSearchAll(userId);
    }

    public static boolean isIn(Long wordId, Long userId) {
        MyLexicon word = MyLexicon.find("myWordId = ? and userId = ?", wordId, userId).first();
        if (word == null) {
            return false;
        } else {
            return true;
        }
    }

//    public boolean saveMyword() {
//        long id = JDBCBuilder.insert(false, false, DataSrc.BASIC, INSERT_ONE_SQL, this.getMyWordId(), this.getUserId());
//
//        if (id >= 0L) {
////		            this.setId(id);
//            return true;
//        } else {
//            log.warn("Insert Failes.....");
//            return false;
//        }
//    }

}
