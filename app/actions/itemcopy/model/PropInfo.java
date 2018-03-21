package actions.itemcopy.model;

/**
 * Created by ZhuQianli on 2018/1/24.
 */
public class PropInfo {
    String name;
    String value;

    public PropInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public PropInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public PropInfo setValue(String value) {
        this.value = value;
        return this;
    }
}
