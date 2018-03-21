
package pojo.webpage.top;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pojo.webpage.top.WebPojo.XpathFieldForm;

import com.ciaosir.client.CommonUtils;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class DBTParser<T extends WebPojo> {

    private static final Logger log = LoggerFactory.getLogger(DBTParser.class);

    public static final String TAG = "DBTParser";

    public void parseNodeByFields(DomNode page, WebPojo pojo) {
        parseNodeByFields(page, pojo, pojo.getClass().getDeclaredFields());
    }

    public void parseNodeByFields(DomNode page, WebPojo pojo, Field[] fields) {

        for (Field field : fields) {
            log.info("[For Field]" + field);

            try {
                XpathFieldForm pathForm = field.getAnnotation(XpathFieldForm.class);

                if (pathForm == null) {
                    continue;
                }
                String[] patterns = pathForm.possiblePatterns();

                for (String patt : patterns) {
                    List<HtmlElement> elems = (List<HtmlElement>) page.getByXPath(patt);

                    if (CommonUtils.isEmpty(elems)) {
                        continue;
                    }
                    for (HtmlElement htmlElement : elems) {
                        log.info("[htmlElement]:" + htmlElement.asXml());
                    }

                    if (pathForm.isArray()) {
                        for (HtmlElement htmlElement : elems) {
                            setField(htmlElement, field, pathForm, pojo);
                        }
                    } else {
                        setField(elems.get(0), field, pathForm, pojo);
                    }
                }
            } catch (Exception e) {
                log.error("Get Field Error for:" + field);
                log.warn(e.getMessage(), e);
            }
        }
        log.info("[Item :]" + pojo);
    }

    private void setField(HtmlElement htmlElement, Field field, XpathFieldForm pathForm, WebPojo pojo)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        if (field.getType().equals(String.class)) {
            String text = htmlElement.getTextContent();
            field.set(pojo, text);
            return;
        }
        Class<?> cls = field.getClass();
        if (!cls.isAssignableFrom(WebPojo.class)) {
            log.warn("What's this class ? :" + cls);
            return;
        }
        WebPojo newInstance = (WebPojo) cls.newInstance();
//        newInstance.parseFromHtml(htmlElement);

        parseNodeByFields(htmlElement, newInstance, cls.getDeclaredFields());
        field.set(field, newInstance);
    }

}
