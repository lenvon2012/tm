package models.urgeComment;

import codegen.CodeGenerator;
import com.ciaosir.client.pojo.PageOffset;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by User on 2017/7/28.
 */
public abstract class NoteModel extends GenericModel implements CodeGenerator.PolicySQLGenerator {

    public abstract String getTableName();

    public abstract String getTableHashKey();

    public abstract String getIdColumn();

    public abstract Long getId();

    public abstract void setId(Long id);

    public abstract boolean jdbcSave();

    public abstract String getIdName();

    public abstract String[] getColumnNames();



}
