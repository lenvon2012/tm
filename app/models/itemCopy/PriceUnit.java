package models.itemCopy;

/**
 * Created by User on 2017/11/10.
 */
public class PriceUnit {
    String name;
    String price;
    String display;

    public PriceUnit(String name, String price, String display) {
        this.name = name;
        this.price = price;
        this.display = display;
    }

    public PriceUnit() {}

    public String getName() {
        return name;
    }

    public PriceUnit setName(String name) {
        this.name = name;
        return this;
    }

    public String getPrice() {
        return price;
    }

    public PriceUnit setPrice(String price) {
        this.price = price;
        return this;
    }

    public String getDisplay() {
        return display;
    }

    public PriceUnit setDisplay(String display) {
        this.display = display;
        return this;
    }
}