
package models.trade;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import models.SimpleStringModel;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

@Entity(name = City.TABLE_NAME)
public class City extends GenericModel implements SimpleStringModel {

    public static final String TABLE_NAME = "receiver_city";

    @Id
    @Column(columnDefinition = "int NOT NULL AUTO_INCREMENT")
    public Integer id;

    public String city;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public City(Integer id, String city) {
        super();
        this.id = id;
        this.city = city;
    }

    public static Map<String, Integer> cityCache = new HashMap<String, Integer>();

    public static City findOrCreate(String city) {
        if (StringUtils.isEmpty(city)) {
            return null;
        }

        long id = JDBCBuilder.singleLongQuery("select id from receiver_city where city = ?", city);
        if (id > 0) {
            return new City(Integer.valueOf((int) id), city);
        }
        id = JDBCBuilder.insert("insert into receiver_city (`city`) values(?);", city);
        return new City(Integer.valueOf((int) id), city);

    }

    public static synchronized Integer getId(String city) {
        if (StringUtils.isEmpty(city)) {
            return 0;
        }

        Integer id = cityCache.get(city);
        if (id != null) {
            return id;
        }

        City instance = findOrCreate(city);
        if (cityCache.size() > MAX_CACHE_SIZE) {
            cityCache.clear();
        }

        cityCache.put(city, instance.getId());

        return instance.getId();
    }
}
