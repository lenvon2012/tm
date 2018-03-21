package actions.itemcopy.model;

import java.util.List;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class SkuPropInfo {
    String name;
    String pid;
    List<SkuPropValueInfo> values;

    public SkuPropInfo(String name, String pid, List<SkuPropValueInfo> values) {
        this.name = name;
        this.pid = pid;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public SkuPropInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getPid() {
        return pid;
    }

    public SkuPropInfo setPid(String pid) {
        this.pid = pid;
        return this;
    }

    public List<SkuPropValueInfo> getValues() {
        return values;
    }

    public SkuPropInfo setValues(List<SkuPropValueInfo> values) {
        this.values = values;
        return this;
    }
}
