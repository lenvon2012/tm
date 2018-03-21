
package utils;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import configs.TMConfigs;

public class PolicyDBUtil {

    private static final Logger log = LoggerFactory.getLogger(PolicyDBUtil.class);

    public static final String TAG = "PolicyDBUtil";

    public static void loadSqlFile(File file) {
        Properties prop = Play.configuration;

        String defaultUrl = prop.getProperty("db.url");
        String driver = prop.getProperty("db.driver");
        String name = prop.getProperty("db.user");
        String pswd = prop.getProperty("db.pass");

        SQLExec sqlExec = new SQLExec();
        sqlExec.setDriver(driver);
        sqlExec.setUserid(name);
        sqlExec.setPassword(pswd);
        sqlExec.setUrl(defaultUrl);

        // 要执行的脚本
        sqlExec.setSrc(file);
        // 有出错的语句该如何处理
        sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
        sqlExec.setPrint(true); // 设置是否输出
        // 输出到文件 sql.out 中；不设置该属性，默认输出到控制台
        // sqlExec.setOutput(log.);
        sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错
        sqlExec.execute();
    }

    public static void loadSqlFile(String string) {
        loadSqlFile(new File(TMConfigs.sqlDir, string));
    }

    public static void loadQuotaSqlFile(File file) {
        Properties prop = Play.configuration;

        String defaultUrl = prop.getProperty("quota.db.url");
        String driver = prop.getProperty("quota.db.driver");
        String name = prop.getProperty("quota.db.user");
        String pswd = prop.getProperty("quota.db.pass");
        if (StringUtils.isEmpty(defaultUrl)) {
            defaultUrl = prop.getProperty("db.url");
        }
        if (StringUtils.isEmpty(driver)) {
            driver = prop.getProperty("db.driver");
        }
        if (StringUtils.isEmpty(name)) {
            name = prop.getProperty("db.user");
        }
        if (StringUtils.isEmpty(pswd)) {
            pswd = prop.getProperty("db.pass");
        }

        SQLExec sqlExec = new SQLExec();
        sqlExec.setDriver(driver);
        sqlExec.setUserid(name);
        sqlExec.setPassword(pswd);
        sqlExec.setUrl(defaultUrl);

        // 要执行的脚本
        sqlExec.setSrc(file);
        // 有出错的语句该如何处理
        sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
        sqlExec.setPrint(true); // 设置是否输出
        // 输出到文件 sql.out 中；不设置该属性，默认输出到控制台
        // sqlExec.setOutput(log.);
        sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错
        sqlExec.execute();
    }

    public static void loadSqlFile(DataSrc src, String string) {
        loadSqlFile(src, new File(TMConfigs.sqlDir, string));
    }

    public static void loadSqlFile(DBDispatcher dp, String string) {
        loadSqlFile(dp.getSrc(), new File(TMConfigs.sqlDir, string));
    }

    public static void loadSqlFile(DataSrc src, File file) {
        try {
            Properties prop = Play.configuration;

            SQLExec sqlExec = new SQLExec();
            sqlExec.setDriver(src.getDriver());
            sqlExec.setUserid(src.getUsername());
            sqlExec.setPassword(src.getPassword());
            sqlExec.setUrl(src.getUrl());

            //要执行的脚本   
            sqlExec.setSrc(file);
            //有出错的语句该如何处理   
            sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
            sqlExec.setPrint(true); //设置是否输出  
            //输出到文件 sql.out 中；不设置该属性，默认输出到控制台   
//        sqlExec.setOutput(log.);
            sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错   
            sqlExec.execute();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }

    }

    public static void loadQuotaSqlFile(String string) {
        loadQuotaSqlFile(new File(TMConfigs.sqlDir, string));
    }

}
