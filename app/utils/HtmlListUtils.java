
package utils;

import java.util.List;

import com.ciaosir.client.CommonUtils;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class HtmlListUtils {

    public static HtmlElement getFirst(List<HtmlElement> list) {
        if (CommonUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }
}
