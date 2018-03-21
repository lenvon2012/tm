package models.urgeComment;


import codegen.CodeGenerator;
import com.ciaosir.client.pojo.PageOffset;
import org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.GenericModel;
import schemasMicrosoftComOfficeOffice.STInsetMode;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(name = NoteCareLog.TABLE_NAME)
public class NoteCareLog extends GenericModel implements CodeGenerator.PolicySQLGenerator{

    private static final Logger log = LoggerFactory.getLogger(NoteCareLog.class);

    public static final String TABLE_NAME = "note_care_log";

    public static NoteCareLog EMPTY = new NoteCareLog();

    @Transient
    public static CodeGenerator.DBDispatcher dp = new CodeGenerator.DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    public NoteCareLog() {}

    public NoteCareLog(ResultSet resultSet) {
        for (String columnName : COLUMN_NAMES) {
            try {
                Field declaredField = this.getClass().getDeclaredField(columnName);
                declaredField.set(this, resultSet.getObject(columnName));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String[] COLUMN_NAMES = {"id", "userId", "userNick", "noteContent", "created", "totalCount", "successCount", "failCount", "errorMessage"};

    @Id
    @GeneratedValue
    public Long id;

    public Long userId;//用户ID

    public String userNick;//关怀人名称

    public String noteContent;//关怀内容

    public String errorMessage;//错误信息

    public Long created;//创建时间

    public Integer totalCount;//短信关怀总数

    public Integer successCount;//成功数量

    public Integer failCount;//失败数量

    public Integer status;//数据状态

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public String getTableHashKey() {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
        return false;
    }

    @Override
    public String getIdName() {
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean insert() {
        String sql = "insert into " + TABLE_NAME + " (userId, userNick, noteContent, errorMessage, created, totalCount, successCount, failCount, status) value(?, ?, ?, ?, ?, ?, ?, ?, ?) ";

        return dp.insert(sql, getUserId(), getUserNick(), getNoteContent(), getErrorMessage(), getCreated(), getTotalCount(), getSuccessCount(), getFailCount(), 1) == 1;
    }

    public Boolean update() {
        return false;
    }

    public Boolean del() {
        return false;
    }

    public static List<NoteCareLog> findByUserId(Long userId, PageOffset po) {
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("userId", userId);
        condition.put("status", 1);

        return select(condition, po);
    }

    public static int countByUserId(long userId) {
        String sql = "select count(*) from " + TABLE_NAME + " where userId = ?";

        return (int) dp.singleLongQuery(sql, userId);
    }


    public static List<NoteCareLog> select(Map<String, Object> condition, PageOffset pageOffset) {
        List params = new ArrayList();
        StringBuilder sql = new StringBuilder();
        sql.append("select " + stringJoin(", ", COLUMN_NAMES) + " from " + TABLE_NAME);

        if (condition != null) {
            StringBuilder whereSql = new StringBuilder(" where 1 = 1");
            for (String columnName : condition.keySet()) {
                whereSql.append(" and " + columnName + " = ?");
            }
            params.addAll(condition.values());
            sql.append(whereSql);
        }
        if (true) {
            String orderSql = " order by created desc";
            sql.append(orderSql);
        }
        if (pageOffset != null) {
            String limitSql = " limit ? offset ?";
            params.add(pageOffset.getPs());
            params.add(pageOffset.getOffset());
            sql.append(limitSql);
        }

        List<NoteCareLog> call = new JDBCBuilder.JDBCExecutor<List<NoteCareLog>>(NoteCareLog.dp, sql.toString(), params.toArray()) {
            @Override
            public List<NoteCareLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<NoteCareLog> list = new ArrayList<NoteCareLog>();
                while (rs.next()) {
                    NoteCareLog order = new NoteCareLog(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();

        return call;


    }

    /**
     * 在指定 String 数组的每个元素之间串联指定的分隔符 String，从而产生单个串联的字符串
     * @param join 分隔符
     * @param strAry String数组
     * @return 字符串
     */
    private static String stringJoin(String join,String[] strAry) {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<strAry.length;i++){
            if(i==(strAry.length-1)){
                sb.append(strAry[i]);
            }else{
                sb.append(strAry[i]).append(join);
            }
        }
        return new String(sb);
    }



}

