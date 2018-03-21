
package models.material;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import models.CreatedUpdatedModel;

public class TMMaterialTmpl extends CreatedUpdatedModel {

    /**
     * 关联影响和海报
     * @author zrb
     */
    public enum Type {
        GuanLian, HaiBao, TuanGou, Kefu
    }

    @Enumerated(EnumType.STRING)
    protected Type mType;

    protected String code;

    protected Long userId;

    protected String doneNumIids;

    protected int doneItemNum;

    protected int failNum = 0;

    protected String failMsg;
}
