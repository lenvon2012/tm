/**
 * 
 */
package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * @author navins
 * @date: 2013年10月22日 上午12:12:21
 */
@Entity(name = ServiceGroup.TABLE_NAME)
public class ServiceGroup extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(ServiceGroup.class);

    @Transient
    public static final String TABLE_NAME = "service_group";

    @Transient
    public static final ServiceGroup EMPTY = new ServiceGroup();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Id
    @GeneratedValue
    public Long id;

    @Index(name = "userId")
    private Long userId;

    @Column(columnDefinition = "varchar(63) default '' not null")
    private String groupName;

    private Long reserved;

    @Column(columnDefinition = "varchar(63) default '' not null")
    private String remark;

    public ServiceGroup() {

    }

    public ServiceGroup(Long userId, String groupName) {
        super();
        this.userId = userId;
        this.groupName = groupName;
        this.reserved = 0L;
        this.remark = StringUtils.EMPTY;
    }

    public ServiceGroup(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.userId = rs.getLong(2);
        this.groupName = rs.getString(3);
        this.reserved = rs.getLong(4);
        this.remark = rs.getString(5);

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getReserved() {
        return reserved;
    }

    public void setReserved(Long reserved) {
        this.reserved = reserved;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    static String Exist_Id_Query = "select id from service_group where userId = ? and groupName = ?";

    private static long findExistId(Long userId, String groupName) {
        return dp.singleLongQuery(Exist_Id_Query, userId, groupName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.groupName);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp.insert("insert into `service_group`(`userId`,`groupName`,`reserved`,`remark`) values(?,?,?,?)",
                this.userId, this.groupName, this.reserved, this.remark);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `service_group` set  `userId` = ?, `groupName` = ?, `reserved` = ?, `remark` = ? where `id` = ? ",
                        this.userId, this.groupName, this.reserved, this.remark, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);
            return false;
        }
    }

    public static boolean deleteServiceGroup(Long userId, Long id) {
        if (userId == null || id == null) {
            return false;
        }
        long deleteNum = dp.insert("delete from `service_group` where `userId` = ? and `id` = ? ", userId, id);

        if (deleteNum == 1) {
            return true;
        } else {
            log.error("delete failed...for :" + id + "[userId : ]" + userId);
            return false;
        }
    }

    static String Select_Query = "select id,userId,groupName,reserved,remark from service_group";

    public static List<ServiceGroup> queryUserGroups(Long userId) {
        String query = Select_Query + " where userId = ?";
        List<ServiceGroup> list = new JDBCBuilder.JDBCExecutor<List<ServiceGroup>>(dp, query, userId) {

            @Override
            public List<ServiceGroup> doWithResultSet(ResultSet rs) throws SQLException {
                List<ServiceGroup> list = new ArrayList<ServiceGroup>();
                while (rs.next()) {
                    list.add(new ServiceGroup(rs));
                }
                return list;
            }

        }.call();

        return list;
    }
    
    static String Select_Map_Query = "select id,groupName from service_group";

    public static Map<Long, String> queryUserGroupsMap(Long userId) {
        String query = Select_Map_Query + " where userId = ?";
        Map<Long, String> res = new JDBCBuilder.JDBCMapStringExecutor(dp, query, userId).call();
        return res;
    }
}
