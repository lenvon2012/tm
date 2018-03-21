
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

@Entity(name = District.TABLE_NAME)
public class District extends GenericModel implements SimpleStringModel {

    public static final String TABLE_NAME = "receiver_district";

    @Id
    @Column(columnDefinition = "int NOT NULL AUTO_INCREMENT")
    public Integer id;

    public String district;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public District(Integer id, String district) {
        super();
        this.id = id;
        this.district = district;
    }

    public static Map<String, Integer> districtCache = new HashMap<String, Integer>();

    public static District findOrCreate(String district) {
        if (StringUtils.isEmpty(district)) {
            return null;
        }

        long id = JDBCBuilder.singleLongQuery(
                "select id from receiver_district where district = ?", district);
        if (id > 0) {
            return new District(Integer.valueOf((int) id), district);
        }
        id = JDBCBuilder.insert("insert into receiver_district (`district`) values(?);", district);
        return new District(Integer.valueOf((int) id), district);

    }

    public static synchronized Integer getId(String district) {
        if (StringUtils.isEmpty(district)) {
            return 0;
        }

        Integer id = districtCache.get(district);
        if (id != null) {
            return id;
        }

        District instance = findOrCreate(district);
        if (districtCache.size() > MAX_CACHE_SIZE) {
            districtCache.clear();
        }
        districtCache.put(district, instance.getId());

        return instance.getId();
    }
}
