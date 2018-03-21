
package models.industry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.AreaOption;

@Entity(name = AreaOptionPlay.TABLE_NAME)
public class AreaOptionPlay extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(AreaOptionPlay.class);

    public static final String TAG = "BusArea";

    public static final String TABLE_NAME = "bus_area";

    @Id
    Long areaId;

    /**
     * 父地域id，若该字段为0表示该行政区为顶层，例如像北京，国外等
     */
    Long parentId;

    @Column(columnDefinition = "varchar(63) not null")
    String name;

    @Column(columnDefinition = "varchar(63) not null")
    @Index(name = "display")
    String display;

    /**
     * 地域级别，目前自治区、省、直辖市是1，其他城市、地区是2
     */
    public int level;

    public AreaOptionPlay() {
        
    }
    
    public AreaOptionPlay(AreaOption option) {
        this.areaId = option.getAreaId();
        this.parentId = option.getParentId();
        this.name = option.getName();
        this.level = option.getLevel().intValue();
        if (name.length() > 3 && level == 1) {
            this.display = name.substring(0, 2);
        } else {
            this.display = name;
        }
    }

    public static List<AreaOptionPlay> findAllProvinces() {
        
        
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " where level = 1";
        
        return new JDBCBuilder.JDBCExecutor<List<AreaOptionPlay>>(query) {

            @Override
            public List<AreaOptionPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<AreaOptionPlay> options = new ArrayList<AreaOptionPlay>();
                
                while (rs.next()) {
                    AreaOptionPlay opt = parseAreaOptionPlay(rs);
                    if (opt != null) {
                        options.add(opt);
                    }
                }
                
                return options;
            }
            
            
        }.call();
        
    }
    
    
    public static AreaOptionPlay findByAreaId(Long areaId) {
        String query = " select " + SelectAllProperties + " from " + TABLE_NAME + " where areaId = ?";
        
        return new JDBCBuilder.JDBCExecutor<AreaOptionPlay>(query, areaId) {

            @Override
            public AreaOptionPlay doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                if (rs.next()) {
                    AreaOptionPlay opt = parseAreaOptionPlay(rs);
                    
                    return opt;
                } else {
                    return null;
                }
                
                
            }
            
            
        }.call();
    }
    
    public static List<AreaOptionPlay> findAllAreas() {
        
        
        String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " where 1 = 1";
        
        return new JDBCBuilder.JDBCExecutor<List<AreaOptionPlay>>(query) {

            @Override
            public List<AreaOptionPlay> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<AreaOptionPlay> options = new ArrayList<AreaOptionPlay>();
                
                while (rs.next()) {
                    AreaOptionPlay opt = parseAreaOptionPlay(rs);
                    if (opt != null) {
                        options.add(opt);
                    }
                }
                
                return options;
            }
            
            
        }.call();
        
    }   

    static Map<String, AreaOptionPlay> map = new HashMap<String, AreaOptionPlay>();

    public static synchronized AreaOptionPlay getByDisplayName(String display) {

//        log.info(format("getByDisplayName:display".replaceAll(", ", "=%s, ") + "=%s", display));

        if (CommonUtils.isEmpty(map)) {
            List<AreaOptionPlay> list = AreaOptionPlay.findAllAreas();
            for (AreaOptionPlay model : list) {
                map.put(model.getDisplay(), model);
            }
            log.info("[map ;]" + map);
        }
        return map.get(display);
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    
    public static long findExistId(Long areaId) {
        
        String existIdQuery = "select areaId from " + TABLE_NAME + " where areaId = ? ";

        return JDBCBuilder.singleLongQuery(existIdQuery, areaId);
    }
    
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.areaId);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }
    

    public boolean rawInsert() {
        
        
        String insertSQL = "insert into `" + TABLE_NAME + "`(`areaId`, `parentId`, `name`," +
                " `level`, `display`) values(?,?,?,?,?)";

        long id = JDBCBuilder.insert(false, insertSQL, this.areaId, this.parentId, this.name, 
                this.level, this.display);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }
    
    public boolean rawUpdate() {
        
        String updateSQL = "update `" + TABLE_NAME + "` set `parentId` = ?, `name` = ?, " +
        		"`level` = ?, `display` = ? where `areaId` = ? ";

        long updateNum = JDBCBuilder.insert(false, updateSQL, this.parentId, this.name,
                this.level, this.display, this.areaId);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update Fails.....");
            return false;
        }

    } 
    
    
    private static final String SelectAllProperties = " areaId, parentId, name," +
                " level, display ";
    
    private static AreaOptionPlay parseAreaOptionPlay(ResultSet rs) {
        try {
            
            AreaOptionPlay opt = new AreaOptionPlay();
            opt.setAreaId(rs.getLong(1));
            opt.setParentId(rs.getLong(2));
            opt.setName(rs.getString(3));
            opt.setLevel(rs.getInt(4));
            opt.setDisplay(rs.getString(5));
            
            return opt;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return null;
        }
    }
    
}
