
package models.op;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@Entity(name = MixPlanRecommend.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "userId", "entityId", "ts", "sellerCids", "tableHashKey", "persistent", "tableName", "idName", "idColumn",
        "hashed", "propsName", "dataSrc", "topCate_1", "topCate_2", "topCate_3", "topCate_4", "hashColumnName",
        "parentCid", "isParent", "parent", "sortOrder", "level"
})
@JsonAutoDetect
public class MixPlanRecommend extends Model {
    @Transient
    public static final String TABLE_NAME = "mix_plans";

    @Index(name = "tag")
    @JsonProperty
    String tag;

    @JsonProperty
    String href;

    @JsonProperty
    String src;

    @JsonProperty
    int width = 750;

    @JsonProperty
    int heigth = 160;

    boolean isShown;

    public static List<MixPlanRecommend> findAllTagShown(String tag) {
        return MixPlanRecommend.find("tag = ? and isShown is true order by id desc ", tag).fetch();
//        return MixPlan.find("isShown is true order by id desc ").fetch();
    }

    public MixPlanRecommend(String tag, String href, String src) {
        super();
        this.tag = tag;
        this.href = href;
        this.src = src;
        this.isShown = true;
    }

    public static void ensure() {
//        if (MixPlanRecommend.count("tag = ?", APIConfig.taovgo.getName()) > 0L) {
//            return;
//        }
        MixPlanRecommend.deleteAll();

//        new MixPlanRecommend(APIConfig.taovgo.getName(),
//                "http://img02.taobaocdn.com/imgextra/i2/333336410/T2ykv6Xf4aXXXXXXXX_!!333336410.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_22902351_130516012346&&tracelog=vgo_dapei_round1")
//                .save();
//        new MixPlanRecommend(APIConfig.taovgo.getName(),
//                "http://img03.taobaocdn.com/imgextra/i3/333336410/T2XxfvXopaXXXXXXXX_!!333336410.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_333336410_130520164508&tracelog=vgo_dapei_round2")
//                .save();
//
//        new MixPlanRecommend(APIConfig.aituiguang.getName(),
//                "http://img02.taobaocdn.com/imgextra/i2/333336410/T2ykv6Xf4aXXXXXXXX_!!333336410.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_22902351_130516012346&tracelog=dapei_inner1")
//                .save();
//
//        new MixPlanRecommend(APIConfig.aituiguang.getName(),
//                "http://img03.taobaocdn.com/imgextra/i3/333336410/T2XxfvXopaXXXXXXXX_!!333336410.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_333336410_130520164508&tracelog=atg_dapei_round2")
//                .save();
//
//        new MixPlanRecommend(APIConfig.relationSale.getName(),
//                "http://img02.taobaocdn.com/imgextra/i2/333336410/T2ykv6Xf4aXXXXXXXX_!!333336410.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_22902351_130516012346&tracelog=dapei_inner1")
//                .save();
//
//        new MixPlanRecommend(APIConfig.relationSale.getName(),
//                "http://img03.taobaocdn.com/imgextra/i3/333336410/T2XxfvXopaXXXXXXXX_!!333336410.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_333336410_130520164508&tracelog=atg_dapei_round2")
//                .save();
//
//        new MixPlanRecommend(
//                APIConfig.taovgo.getName(),
//                "http://img04.taobaocdn.com/imgextra/i4/1039626382/T2_zHiXhVbXXXXXXXX_!!1039626382.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_22902351_130516012346&referer=FW_GOODS-1845420&tracelog=dapei_inner1")
//                .save();
//        new MixPlanRecommend(
//                APIConfig.taovgo.getName(),
//                "http://img04.taobaocdn.com/imgextra/i4/1039626382/T2_zHiXhVbXXXXXXXX_!!1039626382.jpg",
//                "http://fuwu.taobao.com/ser/plan/mix_plan_detail.htm?planCode=PACK_22902351_130516012346&referer=FW_GOODS-1845420&tracelog=dapei_inner1")
//                .save();
    }

    @Override
    public String toString() {
        return "MixPlanRecommend [tag=" + tag + ", href=" + href + ", src=" + src + ", width=" + width + ", heigth="
                + heigth + ", isShown=" + isShown + "]";
    }

}
