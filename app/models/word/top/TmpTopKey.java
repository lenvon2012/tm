
package models.word.top;

import javax.persistence.Entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;

@Entity(name = TmpTopKey.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "lastUpdateTime", "persistent", "entityId", "id", "score", "cid3", "cid2", "topUrlId", "topUrlBaseId"
})
public class TmpTopKey extends Model {

    private static final Logger log = LoggerFactory.getLogger(TmpTopKey.class);

    public static final String TAG = "TmpTopKey";

    public static final String TABLE_NAME = "tmptopkey_";

//    @EmbeddedId
//    public TextUrlId id;
    @Index(name = "text")
    public String text;

//    @Index(name = "topUrlId")
    public Long topUrlId;

//    @Id
//    public String text;

    /**
     * 上升名次
     */
    public int upRateRank = 0;

    /**
     * 搜索排名
     */
    public int searchRank = 0;

    /**
     * 搜索指数
     */
    public int focusIndex = 0;

    /**
     * 提升率
     */
    public double rateChange = 0;

    /**
     * 排名变化
     */
    public int rankChange = 0;

    public int cid3 = -1;

    public int cid2 = -1;

    public int score = -1;

    @Index(name = "topUrlId")
    public long topUrlBaseId = 0L;

    public long lastUpdateTime = 0L;

    /**
     * 展现量
     */
    public int pv;

    /**
     * 点击量
     */
    public int click;

    /**
     * 点击率
     */
//    @Index(name = "ctr")
    public int ctr;

    /**
     * 直通车竞争度
     */
    public int competition;

    public TmpTopKey(String text, Long id) {
        this.text = text;
        this.topUrlBaseId = id;
//        this.id = new TextUrlId(text, urlBase.getId());
    }

//
    public TmpTopKey(TopKey topkey) {
        this.id = topkey.id;
        this.cid2 = topkey.cid2;
        this.cid3 = topkey.cid3;
        this.focusIndex = topkey.focusIndex;
        this.lastUpdateTime = topkey.lastUpdateTime;
        this.rankChange = topkey.rankChange;
        this.score = topkey.score;
        this.searchRank = topkey.searchRank;
        this.text = topkey.text;
        this.topUrlBaseId = topkey.topUrlBaseId;
        this.topUrlId = topkey.topUrlId;
        this.upRateRank = topkey.upRateRank;
        this.rateChange = topkey.rateChange;
        this.click = topkey.click;
        this.competition = topkey.competition;
        this.ctr = topkey.ctr;
        this.pv = topkey.pv;
    }

    public int getFocusIndex() {
        return focusIndex;
    }

    public void setFocusIndex(int focusIndex) {
        this.focusIndex = focusIndex;
    }

    public int getUpRateRank() {
        return upRateRank;
    }

    public void setUpRateRank(int upRateRank) {
        this.upRateRank = upRateRank;
    }

    public int getSearchRank() {
        return searchRank;
    }

    public void setSearchRank(int searchRank) {
        this.searchRank = searchRank;
    }

    public double getRateChange() {
        return rateChange;
    }

    public void setRateChange(int rateChange) {
        this.rateChange = rateChange;
    }

    public int getRankChange() {
        return rankChange;
    }

    public void setRankChange(int rankChange) {
        this.rankChange = rankChange;
    }

    public int getCid3() {
        return cid3;
    }

    public void setCid3(int cid3) {
        this.cid3 = cid3;
    }

    public int getCid2() {
        return cid2;
    }

    public void setCid2(int cid2) {
        this.cid2 = cid2;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getTopUrlBaseId() {
        return topUrlBaseId;
    }

    public void setTopUrlBaseId(Long topUrlBaseId) {
        this.topUrlBaseId = topUrlBaseId;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTopUrlId() {
        return topUrlId;
    }

    public void setTopUrlId(Long topUrlId) {
        this.topUrlId = topUrlId;
    }

    public int getPv() {
        return pv;
    }

    public void setPv(int pv) {
        this.pv = pv;
    }

    public int getClick() {
        return click;
    }

    public void setClick(int click) {
        this.click = click;
    }

    public int getCtr() {
        return ctr;
    }

    public void setCtr(int ctr) {
        this.ctr = ctr;
    }

    public int getCompetition() {
        return competition;
    }

    public void setCompetition(int competition) {
        this.competition = competition;
    }
    
    public void setCtr() {
        if (this.click <= 0 || this.pv <= 0) {
            return;
        }
        if (this.pv > 1000000) {
            this.ctr = this.click / (this.pv / 10000);
        } else {
            this.ctr = this.click * 10000 / this.pv;
        }
    }
    
    @JsonProperty
    public String getWord() {
        return this.text;
    }
}
