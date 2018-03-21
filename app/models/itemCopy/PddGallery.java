package models.itemCopy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.carrierTask.CarrierTask;
import models.itemCopy.dto.Gallery;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.Model;
import play.db.jpa.Transactional;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;

/**
 * 拼多多图片相关
 * 
 * @author oyster
 * 
 */
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts",
        "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = PddGallery.TABLE_NAME)
public class PddGallery extends Model implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory
			.getLogger(PddGallery.class);

	public static final String TABLE_NAME = "pdd_gallery";

	private static PddGallery EMPTY = new PddGallery();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	private String url;
	private int width;
	private int height;

	private String md5;

	private long createTs;

	private static final String insertSQL = "insert into `" + TABLE_NAME
			+ "`(`url`,`width`,`height`,`md5`,`createTs`) values(?,?,?,?,?)";

	public boolean rawInsert() {

		this.setCreateTs(System.currentTimeMillis());

		long id = dp.insert(insertSQL, url, width, height, md5,createTs);
		if (id > 0L) {
			return true;
		} else {
			log.error("Insert Fails....." + this);
			return false;
		}
	}

	public boolean rawUpdate() {

		String updateSQL = "update  `"
				+ TABLE_NAME
				+ "` set `url` = ?,`width` = ?,`height` = ?,`md5` = ? where `id` = ?";
		long updateNum = dp.update(updateSQL, url, width, height, md5,
				this.getId());

		if (updateNum == 1) {
			return true;
		} else {
			log.error("update failed...for :[id ]" + this.id);
			return false;
		}
	}

	public boolean rawDelete() {
		String deleteSql = "delete from " + TABLE_NAME + " where id = ? ";
		long deleteNum = dp.update(deleteSql, this.getId());
		if (deleteNum >= 1) {
			return true;
		} else {
			log.error("delete failed...for :[id : ]" + this.getId());
			return false;
		}
	}

	@Transactional
	public static boolean batchInsert(List<PddGallery> list) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBBuilder.getConn(DataSrc.BASIC);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(insertSQL, 1);

			for (PddGallery obj : list) {
				// 批量插入订单商品信息
				setExtraArgs(obj, ps);
				ps.addBatch();
			}
			ps.executeBatch();
			conn.commit();
			return true;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			JDBCBuilder.closeQuitely(conn);
			JDBCBuilder.closeQuitely(ps);
		}
		return false;
	}

	private static void setExtraArgs(PddGallery obj, PreparedStatement ps)
			throws SQLException {
		int num = 0;
		ps.setString(++num, obj.getUrl());
		ps.setInt(++num, obj.getWidth());
		ps.setInt(++num, obj.getHeight());
		ps.setString(++num, obj.getMd5());

	}

	public String getUrl() {
		return url;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getMd5() {
		return md5;
	}

	public long getCreateTs() {
		return createTs;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public void setCreateTs(long createTs) {
		this.createTs = createTs;
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
	public void setId(Long id) {
		this.id = id;

	}

	@Override
	public boolean jdbcSave() {
		return false;
	}

	@Override
	public String getIdName() {
		return TABLE_NAME;
	}
	
    private static PddGallery findByJDBC(String query, Object...params) {

        return new JDBCBuilder.JDBCExecutor<PddGallery>(dp, query, params) {

            @Override
            public PddGallery doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parsePddGallery(rs);
                }

                return null;
            }
        }.call();
    }

    private static List<PddGallery> findListByJDBC(String query, Object...params) {

        return new JDBCBuilder.JDBCExecutor<List<PddGallery>>(dp, query, params) {

            @Override
            public List<PddGallery> doWithResultSet(ResultSet rs) throws SQLException {

                List<PddGallery> pddGallerys = new ArrayList<PddGallery>();

                while (rs.next()) {
                    PddGallery pddGallery = parsePddGallery(rs);
                    if (pddGallery != null) {
                    	pddGallerys.add(pddGallery);
                    }
                }

                return pddGallerys;
            }
        }.call();
    }

	public static PddGallery parsePddGallery(ResultSet rs) {
		try {
			PddGallery result = new PddGallery();
			result.id = rs.getLong("id");
			result.url = rs.getString("url");
			result.width = rs.getInt("width");
			result.height = rs.getInt("height");
			result.md5 = rs.getString("md5");
			return result;
		} catch (Exception e) {
			log.error(e.getMessage());

		}
		return null;
	}
	
	//根据文件MD5值获取数据库中匹配数据
	public static PddGallery findByMd5(String md5,int width,int height){
		String sql="select * from "+TABLE_NAME+" where md5 = ? and width = ? and height = ? ";
		return findByJDBC(sql, md5,width,height);
	}

	@Override
	public String toString() {
		return "PddGallery [url=" + url + ", width=" + width + ", height="
				+ height + ", md5=" + md5 + ", createTs=" + createTs + "]";
	}

	public PddGallery(String url, int width, int height, String md5,
			long createTs) {
		super();
		this.url = url;
		this.width = width;
		this.height = height;
		this.md5 = md5;
		this.createTs = createTs;
	}

	public PddGallery() {
		super();
	}

	public PddGallery(String url, int width, int height, String md5) {
		super();
		this.url = url;
		this.width = width;
		this.height = height;
		this.md5 = md5;
	}

	public PddGallery(Gallery gallery,String md5) {
		super();
		this.url = gallery.getUrl();
		this.width =  gallery.getWidth();
		this.height =  gallery.getHeight();
		this.md5 = md5;
	}
	
	

}
