package models.carrierTask;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.Model;
import transaction.CodeGenerator;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import javax.persistence.Entity;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = ItemArea.TABLE_NAME)
public class ItemArea extends Model implements CodeGenerator.PolicySQLGenerator<Long> {

    public static final Logger log = LoggerFactory.getLogger(ItemArea.class);

    public static final String TABLE_NAME = "item_area";

    public static final ItemArea EMPTY = new ItemArea();

    public static final CodeGenerator.DBDispatcher dp = new CodeGenerator.DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    Integer parentId;

    String name;

    Integer sort;

    public Integer getParentId() {
        return parentId;
    }

    public ItemArea setParentId(Integer parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemArea setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getSort() {
        return sort;
    }

    public ItemArea setSort(Integer sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public String getTableHashKey(Long aLong) {
        return null;
    }

    @Override
    public String getIdColumn() {
        return null;
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

    ////////////////////////////////////////////////////////////////////////////

    public static List<ItemArea> getAll() {
        String findSql = "select id, parentId, name, sort from " + TABLE_NAME;

        return findListByJDBC(findSql);
    }

    private static List<ItemArea> findListByJDBC(String query,Object... params) {
        return new JDBCBuilder.JDBCExecutor<List<ItemArea>>(dp, query, params) {
            @Override
            public List<ItemArea> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemArea> resultList = new ArrayList<ItemArea>();
                while (rs.next()) {
                    ItemArea result = parseResult(rs);
                    if (result != null) {
                        resultList.add(result);
                    }
                }
                return resultList;
            }
        }.call();
    }

    private static ItemArea parseResult(ResultSet rs) {
        try {

            ItemArea itemArea = new ItemArea();

            itemArea.id = rs.getLong("id");
            itemArea.parentId = rs.getInt("parentId");
            itemArea.name = rs.getString("name");
            itemArea.sort = rs.getInt("sort");

            return itemArea;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

}


