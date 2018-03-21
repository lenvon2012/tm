package dao.item.sub;

public  class NumIidScoreModifed {
    Long numIid;

    int score;

    long modified;

    public NumIidScoreModifed(Long numIid, int score, long modified) {
        super();
        this.numIid = numIid;
        this.score = score;
        this.modified = modified;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

}