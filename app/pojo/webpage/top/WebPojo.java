
package pojo.webpage.top;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

;

/**
 * Only String, String[], WebPojo, WebPojo[], or the List<WebPojo> is defined for the xpath object..
 * @author zhourunbo
 *
 */
public abstract class WebPojo {

    private static final Logger log = LoggerFactory.getLogger(WebPojo.class);

    public static final String TAG = "WebPojo";

    @Transient
    @JsonIgnore
    public int pathIndex;

    @JsonIgnore
    public int getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(int pathIndex) {
        this.pathIndex = pathIndex;
    }

    @Transient
    @JsonIgnore
    Field[] fields = null;

    @JsonIgnore
    public Field[] getPojoFields() {
        List<Field> res = new ArrayList<Field>();

        for (Field field : this.getClass().getDeclaredFields()) {
            Class<?> cls = field.getType();
//            log.info("[For fields:]" + field + "   and class:" + cls);
            if (Modifier.isStatic(cls.getModifiers())) {
                continue;
            }

            if (cls.equals(String.class) || cls.equals(String[].class) || cls.isAssignableFrom(WebPojo.class)
                    || cls.isAssignableFrom(WebPojo[].class)) {
                res.add(field);
            }

            if (cls.isAssignableFrom(List.class)) {

                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                Class ptCls = (Class) pt.getActualTypeArguments()[0];

                if (ptCls.equals(String.class) || WebPojo.class.isAssignableFrom(ptCls)) {
                    res.add(field);
                }
            }
        }
//        log.info("[found field list]" + res);
        return res.toArray(new Field[] {});
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface XpathFieldForm {
        String[] possibleParentIds() default {};

        String[] possiblePatterns();

        boolean mustDone() default false;

        boolean isArray() default false;

        String attr() default "";

        Class<? extends Pager> pager() default Pager.class;
    }

    public WebPojo() {

    }

    public void parse(DomNode page, String href) {
//        log.info("[Parser Nodes]");
        log.info("the href is :" + href);
        for (Field field : getPojoFields()) {
            log.info("[For Field]" + field);

            try {
                XpathFieldForm pathForm = field.getAnnotation(XpathFieldForm.class);

                if (pathForm == null) {
                    continue;
                }
                String[] patterns = pathForm.possiblePatterns();

                if (pathForm.pager() != Pager.class) {
                    log.info("[Pager :]" + pathForm.pager());

                    String name = pathForm.pager().getName();
                    Pager pager = Pager.instances.get(name);
                    List<WebPojo> pojos = null;

                    for (String string : patterns) {
                        pojos = pager.getAll(page, href, string);
                        if (!CommonUtils.isEmpty(pojos)) {
                            continue;
                        }
                    }

//                    log.info("[Pojo List]" + pojos);
                    field.set(this, pojos);
                    return;
                }

                for (String patt : patterns) {
                    log.info("For Pattern:" + patt);

                    List<HtmlElement> elems = (List<HtmlElement>) page.getByXPath(patt);
//                    log.info(page.getByXPath(patt).toString());
                    if (CommonUtils.isEmpty(elems)) {
                        continue;
                    }

                    setField(elems, field, pathForm, href);
                }
            } catch (Exception e) {
                log.error("Get Field Error for:" + field);
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void setField(List<HtmlElement> htmlElement, Field field, XpathFieldForm pathForm, String href)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException {

        int size = htmlElement.size();

        Class<?> cls = field.getType();

        if (cls.equals(String.class)) {
            log.info("set htmlemelnt:" + htmlElement.get(0).asText());
            String text = getContent(htmlElement.get(0), pathForm);
            field.set(this, text);
            return;
        }

        if (cls.equals(String[].class)) {
            field.set(this, appendContent(htmlElement, pathForm).toArray());
            return;
        }

        if (cls.equals(WebPojo.class)) {
            WebPojo newInstance = (WebPojo) cls.newInstance();
            newInstance.parse(htmlElement.get(0), href);
            return;
        }

        if (cls.equals(WebPojo[].class)) {
            WebPojo[] res = new WebPojo[size];

            for (int i = 0; i < size; i++) {
                WebPojo newInstance = setForElementIndex(htmlElement, href, cls, i);
                res[i] = newInstance;
            }

            field.set(this, res);
        }

        if (cls.isAssignableFrom(List.class)) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Type type = pt.getActualTypeArguments()[0];
            if (type.getClass().isAssignableFrom(WebPojo.class)) {
                List<WebPojo> res = new ArrayList<WebPojo>(size);

                for (int i = 0; i < size; i++) {
                    WebPojo newInstance = setForElementIndex(htmlElement, href, cls, i);
                    res.add(newInstance);
                }

                field.set(this, res);
            }
        }
    }

    private WebPojo setForElementIndex(List<HtmlElement> htmlElement, String href, Class<?> cls, int i)
            throws InstantiationException, IllegalAccessException {
        HtmlElement elem = htmlElement.get(i);
        WebPojo newInstance = (WebPojo) cls.newInstance();
        newInstance.parse(elem, href);
        newInstance.setPathIndex(i);
        return newInstance;
    }

    private List<String> appendContent(List<HtmlElement> htmlElement, XpathFieldForm pathForm) {
        List<String> texts = new ArrayList<String>();
        for (HtmlElement elem : htmlElement) {
            texts.add(getContent(elem, pathForm));
        }
        return texts;
    }

    private String getContent(HtmlElement htmlElement, XpathFieldForm pathForm) {
        if (StringUtils.isEmpty(pathForm.attr())) {
            return htmlElement.getTextContent();
        } else {
            return htmlElement.getAttribute(pathForm.attr());
        }
    }
}
