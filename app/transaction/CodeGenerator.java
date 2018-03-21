
package transaction;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transaction.CodeGenerator.PolicySQLGenerator.CodeNoUpdate;
import transaction.DBBuilder.DataSrc;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class CodeGenerator {

    private static final Logger log = LoggerFactory.getLogger(CodeGenerator.class);

    public static final String TAG = "CodeGenerator";

    public static void main(String[] args) {
/*
        CodeGenerator cg = new CodeGenerator(new UserPswd());
        System.out.println(cg.appendSelect());
        System.out.println(cg.appendInsert());
        System.out.println(cg.appendUpdate());
        System.out.println(cg.appendFetchFromRs());
        System.out.println(cg.appendListFetcher());
        System.out.println(cg.appendCounter());
*/
//      System.out.println(cg.appendAssignFromOther());
    }

    public static class DBDispatcher {
        private DataSrc src;

        private PolicySQLGenerator target;

        public DBDispatcher(PolicySQLGenerator target) {
            this.src = DataSrc.BASIC;
            this.target = target;
        }

        public DBDispatcher(DataSrc src, PolicySQLGenerator target) {
            this.src = src;
            this.target = target;
        }

        public String rebuildQuery(String query) {

            String table = target.getTableName();
            String hashKey = target.getTableHashKey(target.getId());
            if (StringUtils.isEmpty(hashKey)) {
                return query;
            }
            String hashedTable = table + hashKey;
            query = query.replaceAll(table, hashedTable);

            return query;
        }

        public long update(String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.update(false, src, query, objs);
        }

        public long update(boolean debug, String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.update(debug, src, query, objs);
        }

        public long insert(String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.insert(false, src, query, objs);
        }

        public long insert(boolean isKeyGenerated, String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.insert(false, isKeyGenerated, src, query, objs);
        }

        public long singleLongQuery(String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.singleLongQuery(src, query, objs);
        }

        public double singeDoubleQuery(String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.singleDoubleQuery(src, query, objs);
        }

        public String singleStringQuery(String query, Object... objs) {
            query = rebuildQuery(query);
            return JDBCBuilder.singleStringQuery(src, query, objs);
        }

        public DataSrc getSrc() {
            return src;
        }

        public void setSrc(DataSrc src) {
            this.src = src;
        }

        public DataSrc getDataSrc(Long hashKeyId) {
            return this.src;
        }

    }

    public interface PolicySQLGenerator<T> {
        public String getTableName();

        public String getTableHashKey(T t);

        public String getIdColumn();

        public T getId();

        public void setId(T id);

        public boolean jdbcSave();

        public String getIdName();

        @Target({
                METHOD, FIELD
        })
        @Retention(RUNTIME)
        public @interface CodeNoUpdate {
        }

        @Target({
                METHOD, FIELD
        })
        @Retention(RUNTIME)
        public @interface CodeIgnore {
        }
    }

    PolicySQLGenerator obj;

    public CodeGenerator(PolicySQLGenerator sql) {
        super();
        this.obj = sql;
        prepare();
    }

    Field[] rawFields;

    List<Field> memberFields = new ArrayList<Field>();

    List<Field> allMemberFields = new ArrayList<Field>();

    // int memeberFieldNum;
    int memberFieldNum;

    List<String> tableColumns = new ArrayList<String>();

    public void prepare() {
        rawFields = obj.getClass().getDeclaredFields();

        for (Field field : rawFields) {
            if (field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            if (field.isAnnotationPresent(PolicySQLGenerator.CodeIgnore.class)) {
                continue;
            }
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            memberFields.add(field);
        }

        for (Field field : rawFields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            allMemberFields.add(field);
        }

        memberFieldNum = memberFields.size();

        for (Field field : memberFields) {
            tableColumns.add("`" + getColumn(field) + "`");
        }

    }

    public final void generateJDBCSave(PolicySQLGenerator sql) {

        StringBuilder sb = new StringBuilder();
        sb.append("if(this.getId() == null || this.getId() <= 0L){\n");

        sb.append("}else{\n");

        sb.append("}");
        System.out.println(sb.toString());
    }

    public final String appendInsert() {
        List<String> insertFieldsNames = new ArrayList<String>();
        List<String> interrogationMarks = new ArrayList<String>();
        for (Field field : memberFields) {
            insertFieldsNames.add("this." + field.getName() + " ");
        }

        for (int i = 0; i < memberFieldNum; i++) {
            interrogationMarks.add("?");
        }

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(" public long rawInsert(){\n");
        sb.append("\nlong id = dp.insert(\"insert into `" + obj.getTableName() + "`(");
        sb.append(StringUtils.join(tableColumns, ","));
        sb.append(") values(");
        sb.append(StringUtils.join(interrogationMarks, ","));
        sb.append(")\", ");
        sb.append(StringUtils.join(insertFieldsNames, ","));
        sb.append(");\n");
        sb.append(" return id;\n}\n");

        return sb.toString();
    }

    public final String appendUpdate() {
        List<Field> updateFields = new ArrayList<Field>();
        StringBuilder sb = new StringBuilder();
        for (Field field : memberFields) {
            if (field.isAnnotationPresent(CodeNoUpdate.class) || obj.getIdName().equals(field.getName())) {
                continue;
            }

            updateFields.add(field);
        }

        List<String> updateSegments = new ArrayList<String>();
        for (Field field : updateFields) {
            String column = getColumn(field);
            updateSegments.add(" `" + column + "` = ?");
        }
        List<String> updateFieldNames = new ArrayList<String>();
        for (Field field : updateFields) {
            updateFieldNames.add("this." + field.getName() + " ");
        }

        // Add the where id = ?
        updateFieldNames.add("this.getId()");
        sb.append('\n');
        sb.append(" public long rawUpdate(){\n");
        sb.append("long updateNum = dp.update(\"update `" + obj.getTableName() + "` set ");
        sb.append(StringUtils.join(updateSegments, ","));
        sb.append(" where `" + obj.getIdColumn() + "` = ? \",");
        sb.append(StringUtils.join(updateFieldNames, ","));
        sb.append(");\n");
        sb.append(" return updateNum;\n}\n");
        return sb.toString();
    }

    public final String appendSelect() {
        List<String> names = new ArrayList<String>();
        for (Field field : memberFields) {
            names.add(field.getName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("static String SELECT_SQL = \"select ");
        sb.append(StringUtils.join(names, ","));
        sb.append(" from " + obj.getTableName());
        sb.append("\";\n");
        return sb.toString();
    }

    public final String appendListFetcher() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tpublic static class ListFetcher extends JDBCExecutor<List<" + obj.getClass().getSimpleName()
                + ">> {\n");
        sb.append("public ListFetcher(Long hashKeyId, String whereQuery, Object... params) {\n");
        sb.append("super(false, whereQuery, params);\n");
        sb.append("StringBuilder sb = new StringBuilder();\n");
//        sb.append("sb.append(\"" + appendSelect() + "\");\n");
        sb.append("sb.append(SELECT_SQL);\n");

//        sb.append("sb.append(\" from \");\n");
//        sb.append("sb.append(TABLE_NAME);\n");
        if (obj.getTableHashKey(null) != null) {
            sb.append("sb.append(_instance.getTableHashKey(hashKeyId));\n");
        } else {
//            sb.append("sb.append(TABLE_NAME);\n");
        }
        sb.append("            sb.append(\" where  ");
//        sb.append(obj.getHashColumnName());
//        sb.append("---------------------");
        sb.append(" 1 = 1 \");\n");
//        sb.append("sb.append(hashKeyId);");

        sb.append("if (!StringUtils.isBlank(whereQuery)) {sb.append(\" and \");sb.append(whereQuery);}");
        sb.append("\t            this.src = dp.getDataSrc(hashKeyId);\nthis.query = sb.toString();\n");
        sb.append("\n}");

        sb.append("   @Override\npublic List<");
        sb.append(obj.getClass().getSimpleName());
        sb.append("> doWithResultSet(ResultSet rs) throws SQLException {List<");
        sb.append(obj.getClass().getSimpleName());
        sb.append("> list = new ArrayList<");
        sb.append(obj.getClass().getSimpleName());
        sb.append(">();while (rs.next()) {list.add(new ");
        sb.append(obj.getClass().getSimpleName());
        sb.append("(rs));}\nreturn list;}");
        sb.append("}");
        return sb.toString();
    }

    public final String appendCounter() {
        StringBuilder sb = new StringBuilder();

        sb.append("public static int countJdbc(Long hashKeyId, String whereQuery, Object... params) {\n");
        sb.append("StringBuilder sb = new StringBuilder();\n");
        sb.append("sb.append(\"select count(*) from \");\n");
//        sb.append("sb.append(\" from \");\n");
        sb.append("sb.append(TABLE_NAME);\n");
//        if(obj.getTableHashKey()!=null){
//            
//        }
        if (obj.getTableHashKey(null) != null) {
            sb.append("sb.append(_instance.getTableHashKey(hashKeyId));\n");
        } else {
//            sb.append("sb.append(TABLE_NAME);\n");
        }
        sb.append("            sb.append(\" where  ");
//        sb.append(obj.getHashColumnName());
//        sb.append("---------------------");
        sb.append(" 1 = 1 \");\n");
//        sb.append("sb.append(hashKeyId);");

        sb.append("if (!StringUtils.isBlank(whereQuery)) {sb.append(\" and \");sb.append(whereQuery);}");
        sb.append("\t return (int) JDBCBuilder.singleLongQuery(dp.getDataSrc(hashKeyId), sb.toString(), params); \n");
        sb.append("\n}");
        return sb.toString();
    }

    public final String appendAssignFromOther() {
        StringBuilder sb = new StringBuilder();
        for (Field field : allMemberFields) {
            sb.append("this." + field.getName() + " = " + "o." + field.getName() + ";\n");
        }
        return sb.toString();
    }

    public final String appendFetchFromRs() {

        StringBuilder sb = new StringBuilder();

        sb.append(" public " + obj.getClass().getSimpleName() + " ( ResultSet rs) throws SQLException {\n");
        for (int i = 1; i <= memberFieldNum; i++) {
            Field field = memberFields.get(i - 1);

            sb.append("this." + field.getName() + " = rs.get");
            String type = getFieldClassString(field.getType());
            sb.append("Integer".equals(type) ? "Int" : type);
            sb.append("(" + i + ");\n");
        }

        sb.append("\n}");
        return sb.toString();
    }

    public final String appendFetchFromRsSQL() {

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= memberFieldNum; i++) {
            Field field = memberFields.get(i - 1);

            sb.append("this." + field.getName() + " = rs.get");
            sb.append(getFieldClassString(field.getType()));
            sb.append("(" + i + ");\n");
        }

        return sb.toString();
    }

    public final String appendJDBCSave() {

        StringBuilder sb = new StringBuilder();

        sb.append("public boolean jdbcSave(){\n");
        sb.append("long existId = findExistId();\n");
        sb.append("if(existId > 0L){return this.rawUpdate() > 0L;}\n");
        sb.append("else\n\t{return this.rawInsert() >= 0L;}\n");
        sb.append("\n}");

        return sb.toString();

    }

    public static final String find() {
        return StringUtils.EMPTY;
    }

    public final String appendFind() {
        return StringUtils.EMPTY;
    }

    public static final String getColumn(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            String name = field.getAnnotation(Column.class).name();
            if (!StringUtils.isEmpty(name)) {
                return name;
            }
        }

        return field.getName();
    }

    public static final String getFieldClassString(Class<?> fieldClass) {
        String type = null;

        if (fieldClass == Long.class || fieldClass == long.class) {
            type = "Long";
        } else if (fieldClass == Integer.class || fieldClass == int.class) {
            type = "Integer";
        } else if (fieldClass == Double.class || fieldClass == double.class) {
            type = "Double";
        } else if (fieldClass == String.class) {
            type = "String";
        } else if (fieldClass == Boolean.class || fieldClass == boolean.class) {
            type = "Boolean";
        }
        return type;
    }

    public static Comparator DESC_COMPATOR = new Comparator<PolicySQLGenerator<Long>>() {

        @Override
        public int compare(PolicySQLGenerator<Long> o1, PolicySQLGenerator<Long> o2) {
            long id1 = o1.getId().longValue();
            long id2 = o2.getId().longValue();
            if (id1 > id2) {
                return 1;
            } else if (id1 == id2) {
                return 0;
            } else {
                return -1;
            }
        }
    };

    public static Comparator AEC_COMPATOR = new Comparator<PolicySQLGenerator<Long>>() {
        @Override
        public int compare(PolicySQLGenerator<Long> o1, PolicySQLGenerator<Long> o2) {
            long id1 = o1.getId().longValue();
            long id2 = o2.getId().longValue();
            if (id1 > id2) {
                return -1;
            } else if (id1 == id2) {
                return 0;
            } else {
                return 1;
            }
        }
    };
}
