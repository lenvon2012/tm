
package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configs.TMConfigs;

public class TMCatUtil {

    private static Map<Long, TMCategory> catIdMap = new HashMap<Long, TMCategory>();

    static {
        List<TMCategory> catList = new ArrayList<TMCategory>();

        catList.add(new TMCategory(1, "女装"));
        catList.add(new TMCategory(2, "男装"));
        catList.add(new TMCategory(3, "鞋子"));
        catList.add(new TMCategory(4, "包包"));
        catList.add(new TMCategory(5, "美容"));
        catList.add(new TMCategory(6, "家居"));
        catList.add(new TMCategory(7, "母婴"));
        catList.add(new TMCategory(8, "数码"));
        catList.add(new TMCategory(9, "美食"));
        catList.add(new TMCategory(10, "随意淘"));

        for (TMCategory tmCat : catList) {
            catIdMap.put(tmCat.getCatId(), tmCat);
        }

    }

    public static String getFirstLevelCatName(Long catId) {
        if (catId == null || catId <= 0)
            return "";
        TMCategory tmCat = catIdMap.get(catId);
        if (tmCat == null)
            return "";
        return tmCat.getCatName();
    }

    public static int getCidInt(Long cid) {
        Long firstLevel = TaobaoUtil.getFirstLevel(cid);
        String catname = TMConfigs.PopularizeConfig.bigCidNameMap.get(firstLevel);
        if (catname.isEmpty()) {
            return 0;
        }
        int cidInt = TMConfigs.PopularizeConfig.catNamemap.get(catname);
        if (cidInt > 10 || cidInt < 1) {
            return 0;
        }
        return cidInt;
    }

    public static class TMCategory {
        private long catId;

        private int level;

        private String catName;

        public long getCatId() {
            return catId;
        }

        public void setCatId(long catId) {
            this.catId = catId;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getCatName() {
            return catName;
        }

        public void setCatName(String catName) {
            this.catName = catName;
        }

        public TMCategory(long catId, String catName) {
            super();
            this.catId = catId;
            this.catName = catName;
            this.level = 1;
        }

        public TMCategory(long catId, String catName, int level) {
            super();
            this.catId = catId;
            this.catName = catName;
            this.level = level;
        }
    }
}
