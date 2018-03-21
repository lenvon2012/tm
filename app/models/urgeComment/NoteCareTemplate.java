package models.urgeComment;

import codegen.CodeGenerator;
import com.ciaosir.client.pojo.PageOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Entity(name = NoteCareTemplate.TABLE_NAME)
public class NoteCareTemplate extends GenericModel implements CodeGenerator.PolicySQLGenerator {

    private static final long serialVersionUID = -6783885164952528230L;

    private static final Logger log = LoggerFactory.getLogger(NoteCareTemplate.class);

    public static final String TABLE_NAME = "note_care_template";

    public static NoteCareTemplate EMPTY = new NoteCareTemplate();

    @Transient
    public static CodeGenerator.DBDispatcher dp = new CodeGenerator.DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    public NoteCareTemplate() {
    }

    public NoteCareTemplate(ResultSet resultSet) {
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


    private static final String[] COLUMN_NAMES = {"id", "noteContent", "noteDigest", "noteStatus", "created", "checkMessage"};

    @Id
    @GeneratedValue
    public long id;

    public long userId;//用户ID

    public String noteContent;//短信内容

    public String noteDigest;//短信签名

    public int noteStatus;//短信状态 0:未审核 1:审核通过 2:审核不通过

    public String checkMessage;//审核信息

    public long created;//创建时间

    public long lastTime;//最近一次修改操作

    public int status = 1;//0:被删除 1:正常

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getNoteDigest() {
        return noteDigest;
    }

    public void setNoteDigest(String noteDigest) {
        this.noteDigest = noteDigest;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }


    @Override
    public String getTableName() {
        return TABLE_NAME;
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
        return id;
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
        return "id";
    }


    public Long insert() {
        String sql = "insert into " + TABLE_NAME + " (noteContent, noteDigest, noteStatus, created, lastTime, status, userId) value(?, ?, ?, ?, ?, ?, ?)";

        return dp.insert( sql, getNoteContent(), getNoteDigest(), 0, getCreated(), getCreated(), getStatus(), getUserId());
    }

    public boolean del() {
        String sql = "update " + TABLE_NAME + " set status = 0, lastTime = ? where id = ? and userId = ?";
        long currentTime = new Date().getTime();

        return dp.update(sql, currentTime, getId(), getUserId()) == 1;
    }

    public boolean update() {
        String sql = "update " + TABLE_NAME + " set noteContent = ?, noteDigest = ?, lastTime = ? where id = ? and userId = ? and status = ?";
        long currentTime = new Date().getTime();

        return dp.update(sql, getNoteContent(), getNoteDigest(), currentTime, getId(), getUserId(), getStatus()) == 1;
    }

    /**
     * 查询用户所有短信模板的数量
     * @param userId 用户ID
     * @return 总数
     */
    public static int countAll(long userId) {
        String sql = "select count(*) from " + TABLE_NAME +" where userId = ? and status = 1";

        return (int) dp.singleLongQuery(sql, userId);
    }

    /**
     * 查询用户的所有短信模板
     * @param userId 用户ID
     * @param po 分页信息
     * @return 短信模板集合
     */
    public static List<NoteCareTemplate> selectALL(long userId ,PageOffset po) {
        Map<String, Object> condition = new LinkedHashMap<String, Object>();
        condition.put("userId", userId);
        condition.put("status", 1);

        return select(condition, po);
    }

    /**
     * 查询用户的短信模板
     * @param userId 用户ID
     * @param po 分页信息
     * @param noteStatus 短信状态 0:未审核 1:审核通过 2:审核不通过
     * @return
     */
    public static List<NoteCareTemplate> selectAllByNoteStatus(long userId, PageOffset po, int noteStatus) {
        Map<String, Object> condition = new LinkedHashMap<String, Object>();
        condition.put("userId", userId);
        condition.put("noteStatus", noteStatus);
        condition.put("status", 1);

        return select(condition, po);
    }

    /**
     * 查询模板通过模板ID
     * @param userId 用户ID
     * @param id 模板ID
     * @return 模板对象
     */
    public static NoteCareTemplate findById(Long userId, Long id) {
        Map<String, Object> condition = new LinkedHashMap<String, Object>();
        condition.put("id", id);
        condition.put("userId", userId);

        List<NoteCareTemplate> list = select(condition, null);

        if(list != null && list.size() >0)
            return list.get(0);

        return null;
    }

    /**
     * 更改模板审核状态为审核通过
     * @param userId 用户ID
     * @param id 模板ID
     * @return 更新是否成功
     */
    public static Boolean updateNoteStatusPass(Long userId, Long id) {
        String sql = "update " + TABLE_NAME + " set noteStatus = 1 where id = ? and userId = ?";

        return dp.update(sql, id, userId) == 1;
    }

    /**
     * 更改短信模板审核状态为未通过审核
     * @param userId 用户ID
     * @param id 模板ID
     * @param checkMessage 未通过原因
     * @return 更新是否成功
     */
    public static Boolean updateNoteStatusNotPass(Long userId, Long id, String checkMessage) {
        String sql = "update" + TABLE_NAME + "set noteStatus = 2, checkMessage = ? where id = ? and userId = ?";

        return dp.update(sql, checkMessage, id, userId) == 1;
    }


    /**
     * 查询
     * @param condition 条件
     * @param pageOffset 分页 null:不分页
     * @return 短信模板集合
     */
    private static List<NoteCareTemplate> select(Map<String, Object> condition, PageOffset pageOffset) {
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
            String orderSql = " order by lastTime desc";
            sql.append(orderSql);
        }
        if (pageOffset != null) {
            String limitSql = " limit ? offset ?";
            params.add(pageOffset.getPs());
            params.add(pageOffset.getOffset());
            sql.append(limitSql);
        }

        List<NoteCareTemplate> call = new JDBCBuilder.JDBCExecutor<List<NoteCareTemplate>>(NoteCareTemplate.dp, sql.toString(), params.toArray()) {
            @Override
            public List<NoteCareTemplate> doWithResultSet(ResultSet rs) throws SQLException {
                List<NoteCareTemplate> list = new ArrayList<NoteCareTemplate>();
                while (rs.next()) {
                    NoteCareTemplate order = new NoteCareTemplate(rs);
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
