
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

@Entity(name = ZipCode.TABLE_NAME)
public class ZipCode extends GenericModel implements SimpleStringModel {

    public static final String TABLE_NAME = "receiver_zipCode";

    @Id
    @Column(columnDefinition = "int NOT NULL AUTO_INCREMENT")
    public Integer id;

    public String zipCode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public ZipCode(Integer id, String zipCode) {
        super();
        this.id = id;
        this.zipCode = zipCode;
    }

    public static Map<String, Integer> zipCodeCache = new HashMap<String, Integer>();

    public static ZipCode findOrCreate(String zipCode) {
        if (StringUtils.isEmpty(zipCode)) {
            return null;
        }

        long id = JDBCBuilder.singleLongQuery("select id from receiver_zipCode where zipCode = ?",
                zipCode);
        if (id > 0) {
            return new ZipCode(Integer.valueOf((int) id), zipCode);
        }
        id = JDBCBuilder.insert("insert into receiver_zipCode (`zipCode`) values(?);", zipCode);
        return new ZipCode(Integer.valueOf((int) id), zipCode);

    }

    public static synchronized Integer getId(String zipCode) {
        if (StringUtils.isEmpty(zipCode)) {
            return 0;
        }

        Integer id = zipCodeCache.get(zipCode);
        if (id != null) {
            return id;
        }

        ZipCode instance = findOrCreate(zipCode);
        if (zipCodeCache.size() > MAX_CACHE_SIZE) {
            zipCodeCache.clear();
        }
        zipCodeCache.put(zipCode, instance.getId());

        return instance.getId();
    }
}
