
package models.trade;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import models.SimpleStringModel;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

@Entity(name=Area.TABLE_NAME)
public class Area extends GenericModel implements SimpleStringModel {

    public static final String TABLE_NAME = "areas";
    

    @Id
    @Column(columnDefinition = "int NOT NULL AUTO_INCREMENT")
    public Integer id;

    @Index(name="area")
    public String area;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getarea() {
        return area;
    }

    public void setarea(String area) {
        this.area = area;
    }

    public Area(Integer id, String area) {
        super();
        this.id = id;
        this.area = area;
    }

    public static Map<String, Integer> areaCache = new HashMap<String, Integer>();

    public static Area findOrCreate(String area) {
        if (StringUtils.isEmpty(area)) {
            return null;
        }

        long id = JDBCBuilder.singleLongQuery(
                "select id from areas where area = ?", area);
        if (id > 0) {
            return new Area(Integer.valueOf((int) id), area);
        }
        id = JDBCBuilder.insert("insert into areas (`area`) values(?);", area);
        return new Area(Integer.valueOf((int) id), area);

    }

    public static synchronized Integer getId(String area) {
        if (StringUtils.isEmpty(area)) {
            return 0;
        }

        Integer id = areaCache.get(area);
        if (id != null) {
            return id;
        }

        Area instance = findOrCreate(area);
        if (areaCache.size() > MAX_CACHE_SIZE) {
            areaCache.clear();
        }
        areaCache.put(area, instance.getId());

        return instance.getId();
    }

}
