package underup.frame.industry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.ItemThumb;
import com.taobao.api.domain.Item;

import play.Play;
import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;

@Entity(name = LevelPicShopInfo.TABLE_NAME)
public class LevelPicShopInfo extends GenericModel implements PolicySQLGenerator{
    @Transient
    private static final Logger log = LoggerFactory.getLogger(LevelPicShopInfo.class);
    
    @Transient
    public static final String TABLE_NAME = "level_pic_shop_info";

    @Id
    @GeneratedValue
    long id;
    
    @Index(name = "wangwang")
    String wangwang;

    String picPath;

    int level;

    public LevelPicShopInfo(String wangwang, String picPath, int level) {
        this.wangwang = wangwang;
        this.picPath = picPath;
        this.level = level;
    }

    public String getPicPath() {
        return this.picPath;
    }

    public int getLevel() {
        return this.level;
    }

    public String getWangWang() {
        return this.wangwang;
    }
    
    @Transient
    public static final String insertSQL = "insert into level_pic_shop_info (wangwang, picPath, level) values (?,?,?)";
    
    @Transient
    public static final String updateSQL = "update level_pic_shop_info set wangwang = ?, picPath = ?, level = ? where id = ?";

    public static void insertPatch(List<LevelPicShopInfo> levelPicShopInfos) {
        Properties prop = Play.configuration;
        Connection conn = null;

        String url = prop.getProperty("base.db.url");
        if (StringUtils.isEmpty(url)) {
            url = prop.getProperty("db.url");
        }

        String user = prop.getProperty("base.db.user");
        if (StringUtils.isEmpty(user)) {
            user = prop.getProperty("db.user");
        }

        String pwd = prop.getProperty("base.db.pass");
        if (StringUtils.isEmpty(pwd)) {
            pwd = prop.getProperty("db.pass");
        }

        try {
            conn = DriverManager.getConnection(url, user, pwd);
            ResultSet rs = null;
            conn.setAutoCommit(false);
            PreparedStatement prest = conn.prepareStatement(insertSQL);
            PreparedStatement up = conn.prepareStatement("select id from level_pic_shop_info where wangwang=?");
            PreparedStatement updatePatch = conn.prepareStatement(updateSQL);
            int insertNum = 0, updateNum = 0;
            for (LevelPicShopInfo levelPicShopInfo : levelPicShopInfos) {
                up.setString(1, levelPicShopInfo.getWangWang());
                rs = up.executeQuery();
                long flag = 0L;
                if (rs.next()) {
                    flag = rs.getLong(1);
                }
                if (flag == 0L) {
                    prest.setString(1, levelPicShopInfo.getWangWang());
                    prest.setString(2, levelPicShopInfo.getPicPath());
                    prest.setInt(3, levelPicShopInfo.getLevel());
                    prest.addBatch();
                    insertNum++;
                } else {
                    updatePatch.setString(1, levelPicShopInfo.getWangWang());
                    updatePatch.setString(2, levelPicShopInfo.getPicPath());
                    updatePatch.setInt(3, levelPicShopInfo.getLevel());
                    updatePatch.setLong(4, flag);
                    updatePatch.addBatch();
                    updateNum++;
                }
            }
            log.info("the insert num is " + insertNum + " and the update num is " + updateNum);
            if (insertNum > 0)
                prest.executeBatch();
            if (updateNum > 0)
                updatePatch.executeBatch();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            log.error("-------------------------------------------------------------------------------------------------------------------------connect to database fial........");
        }

    }
    

    @Transient
    public static LevelPicShopInfo EMPTY = new LevelPicShopInfo();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public LevelPicShopInfo() {
    }
    
    public static List<String> getAllWangwang(){
        String query = "select wangwang from level_pic_shop_info";
        return new JDBCBuilder.JDBCExecutor<List<String>>(dp, query) {
            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> wangwangs = new ArrayList<String>();
                while (rs.next()) {
                    String wangwang = rs.getString(1);
                    wangwangs.add(wangwang);
                }
                return wangwangs;
            }
        }.call();
    }

    @JsonAutoDetect
    public static class ShopLevelPicInfos{
        @JsonProperty
        int level;
        
        @JsonProperty
        String picPath;
        
        public ShopLevelPicInfos(int level, String picPath){
            this.level = level;
            this.picPath = picPath;
        }
        
        public ShopLevelPicInfos(){
            this.level = 0;
            this.picPath = StringUtils.EMPTY;
        }
        public int getLevel(){
            return this.level;
        }
        
        public String getPicPath(){
            return this.picPath;
        }
        
        public void setLevel(int level){
            this.level = level;
        }
        
        public void setPicPath(String picPath){
            this.picPath = picPath;
        }
    }
    
    public static ShopLevelPicInfos getShoLevelPic(String wangwang){
        String query = "select level,picPath from level_pic_shop_info where wangwang = ?";
        return new JDBCBuilder.JDBCExecutor<ShopLevelPicInfos>(dp, query, wangwang) {
            @Override
            public ShopLevelPicInfos doWithResultSet(ResultSet rs) throws SQLException {
                ShopLevelPicInfos shopInfos = new ShopLevelPicInfos();
                if(rs.next()) {
                    int level = rs.getInt(1);
                    String picPath = rs.getString(2);
                    shopInfos.setLevel(level);
                    shopInfos.setPicPath(picPath);
                }
                return shopInfos;
            }
        }.call();
    }
    
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return this.id;
    }

    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return null;
    }
 }
