
package com.ciaosir.client.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.ciaosir.client.utils.ChsCharsUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.INRecordBase;

@JsonAutoDetect
public class WordBaseBean implements IWordBase, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @JsonProperty
    public long id;

    @JsonProperty
    public String word;

    @JsonProperty
    public int price = 0;

    @JsonProperty
    public int click = 0;

    @JsonProperty
    public int competition = 0;

    @JsonProperty
    public int pv = 0;

    @JsonIgnore
    public int strikeFocus = 0;

    @JsonIgnore
    public int searchFocus = 0;

    @JsonProperty
    public int score = 0;

    @JsonProperty
    public int scount = 0;

    @JsonProperty
    public int status = 0;

    @JsonProperty
    public int cid = 0;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        if (price != null) {
            this.price = price;
        }
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        if (click != null) {
            this.click = click;
        }
    }

    public Integer getCompetition() {
        return competition;
    }

    public void setCompetition(Integer compettion) {
        if (compettion != null) {
            this.competition = compettion;
        }
    }

    public Integer getPv() {
        return pv;
    }

    public void setPv(Integer pv) {
        if (pv != null) {
            this.pv = pv;
        }
    }

    public Integer getStrikeFocus() {
        return strikeFocus;
    }

    public void setStrikeFocus(Integer strikeFocus) {
        if (strikeFocus != null) {
            this.strikeFocus = strikeFocus;
        }
    }

    public Integer getSearchFocus() {
        return searchFocus;
    }

    public void setSearchFocus(Integer searchFocus) {
        if (searchFocus != null) {
            this.searchFocus = searchFocus;
        }
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        if (score != null) {
            this.score = score;
        }
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        if (status != null) {
            this.status = status;
        }
    }

    public Integer getScount() {
        return scount;
    }

    public void setScount(Integer scount) {
        if (scount != null) {
            this.scount = scount;
        }
    }

    private float match = 0f;

    public float getMatch() {
        return match;
    }

    public void setMatch(float match) {
        this.match = match;
    }

    public void setId(Long id) {
        if (id != null) {
            this.id = id;
        }
    }

    public Long getId() {
        return id;
    }

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public WordBaseBean() {
        super();
    }

    public static WordBaseBean loadFromSearchRes(SearchHit hit) {
        if (hit == null) {
            return null;
        }

        Map<String, Object> source = hit.getSource();
        WordBaseBean bean = new WordBaseBean();
        bean.setId(NumberUtil.parserLong(hit.getId(), -1L));
        bean.setWord(ChsCharsUtil.replaceMessyChar((String) source.get("word")));
        bean.setClick(NumberUtil.parserInt(source.get("click"), -1));
        bean.setCompetition(NumberUtil.parserInt(source.get("competition"), -1));
        bean.setPrice(NumberUtil.parserInt(source.get("price"), -1));
        bean.setPv(NumberUtil.parserInt(source.get("pv"), -1));
        bean.setScore(NumberUtil.parserInt(source.get("score"), -1));
        bean.setScount(NumberUtil.parserInt(source.get("scount"), -1));
        bean.setSearchFocus(NumberUtil.parserInt(source.get("searchFocus"), -1));
        bean.setStatus(NumberUtil.parserInt(source.get("status"), 0));
        bean.setStrikeFocus(NumberUtil.parserInt(source.get("strikeFocus"), -1));
        bean.setCid(NumberUtil.parserInt(source.get("cid"), -1));

        bean.setMatch(hit.getScore());
        return bean;
    }

    @SuppressWarnings("unchecked")
    public static List<IWordBase> buildFromHits(SearchHits hits, boolean clearHits) {
        if (hits == null) {
            return ListUtils.EMPTY_LIST;
        }

        List<IWordBase> list = new ArrayList<IWordBase>();
        int length = hits.getHits().length;
        for (int i = 0; i < length; i++) {
            SearchHit next = hits.getHits()[i];
            WordBaseBean base = WordBaseBean.loadFromSearchRes(next);
            list.add(base);

            if (clearHits) {
                hits.getHits()[i] = null;
            }
        }

//        Iterator<SearchHit> it = hits.iterator();
//        while (it.hasNext()) {
//            SearchHit next = it.next();
//
//        }

        return list;
    }

    public void updateByINInfo(INRecordBase record) {
        if (record == null) {
            return;
        }
        // TODO Auto-generated method stub
        this.pv = NumberUtil.parserInt(record.getPv(), NumberUtil.NONE_EXIST);
        this.click = NumberUtil.parserInt(record.getClick(), NumberUtil.NONE_EXIST);
        this.competition = NumberUtil.parserInt(record.getCompetition(), NumberUtil.NONE_EXIST);
        this.price = NumberUtil.parserInt(record.getAvgPrice(), NumberUtil.NONE_EXIST);
    }

    @Override
    public String toString() {
        return "WordBaseBean [id=" + id + ", word=" + word + ", price=" + price + ", click=" + click + ", competition="
                + competition + ", pv=" + pv + ", strikeFocus=" + strikeFocus + ", searchFocus=" + searchFocus
                + ", score=" + score + ", scount=" + scount + ", status=" + status + ", cid=" + cid + ", match="
                + match + "]";
    }

    @JsonAutoDetect
    public static class UserBidPrice {
        @JsonProperty
        Long userId;

        @JsonProperty
        int price;

        public UserBidPrice(Long userId, int price) {
            super();
            this.userId = userId;
            this.price = price;
        }

        public UserBidPrice() {
            super();
        }

        @Override
        public String toString() {
            return "UserBidPrice [userId=" + userId + ", price=" + price + "]";
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

    }

    @JsonAutoDetect
    public static class BusWordBase {

        @JsonProperty
        Long keywordId;

        @JsonProperty
        String word;

        @JsonProperty
        int qscore;

        @JsonProperty
        int bidPrice;

        @JsonProperty
        int maxPrice;

        @JsonProperty
        Long pyKeywordId;

        public BusWordBase(Long keywordId, String word, int qscore) {
            super();
            this.keywordId = keywordId;
            this.word = word;
            this.qscore = qscore;
        }

        @Override
        public String toString() {
            return "BusWordBase [keywordId=" + keywordId + ", word=" + word + ", qscore=" + qscore + "]";
        }

        public BusWordBase() {
            super();
        }

        public Long getKeywordId() {
            return keywordId;
        }

        public void setKeywordId(Long keywordId) {
            this.keywordId = keywordId;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getQscore() {
            return qscore;
        }

        public void setQscore(int qscore) {
            this.qscore = qscore;
        }

        public BusWordBase(Long keywordId, String word, int qscore, int bidPrice, int maxPrice) {
            super();
            this.keywordId = keywordId;
            this.word = word;
            this.qscore = qscore;
            this.bidPrice = bidPrice;
            this.maxPrice = maxPrice;
        }

        public BusWordBase(Long keywordId, String word, int qscore, int bidPrice, Long pyKeywordId) {
            super();
            this.keywordId = keywordId;
            this.word = word;
            this.qscore = qscore;
            this.bidPrice = bidPrice;
            this.pyKeywordId = pyKeywordId;
        }

        public int getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(int bidPrice) {
            this.bidPrice = bidPrice;
        }

        public Long getPyKeywordId() {
            return pyKeywordId;
        }

        public void setPyKeywordId(Long pyKeywordId) {
            this.pyKeywordId = pyKeywordId;
        }

    }

    public WordBaseBean(String word, int pv) {
        super();
        this.word = word;
        this.pv = pv;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((word == null) ? 0 : word.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WordBaseBean other = (WordBaseBean) obj;
        if (word == null) {
            if (other.word != null)
                return false;
        } else if (!word.equals(other.word))
            return false;
        return true;
    }

}
