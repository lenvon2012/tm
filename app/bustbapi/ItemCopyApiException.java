package bustbapi;

import com.taobao.api.domain.Item;

/**
 * Created by User on 2017/11/1.
 */
public class ItemCopyApiException extends RuntimeException {
    protected Item item;
    public ItemCopyApiException(String message, Item item) {
        super(message);
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public ItemCopyApiException setItem(Item item) {
        this.item = item;
        return this;
    }
}
