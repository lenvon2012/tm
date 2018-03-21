package models.CDNPIc;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.diag.RecentlyDiagedItem;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

@Entity(name = UserCDNPic.TABLE_NAME)
public class UserCDNPic extends GenericModel {

	private final static Logger log = LoggerFactory.getLogger(UserCDNPic.class);

	public final static String TABLE_NAME = "user_cdn_pic";

	@Id
	public long picId;
	
	@Index(name = "userId")
	public long userId;

	// 该字段表明这张是什么图，比如满就送模板图1，关联模板2图2
	public String title;

	// 该字段是淘宝CDN的图片链接
	public String cdnPath;

	public UserCDNPic(long picId, long userId, String title, String cdnPath) {
		super();
		this.picId = picId;
		this.userId = userId;
		this.title = title;
		this.cdnPath = cdnPath;
	}

	public long getPicId() {
		return picId;
	}

	public void setPicId(long picId) {
		this.picId = picId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCdnPath() {
		return cdnPath;
	}

	public void setCdnPath(String cdnPath) {
		this.cdnPath = cdnPath;
	}

	static String EXIST_ID_QUERY = "select picId from " + TABLE_NAME
			+ " where  picId = ? and userId = ?";

	public static long findExistId(long picId, long userId) {
		return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, picId, userId);
	}

	public boolean jdbcSave() {
		try {
			long existdId = findExistId(this.picId, this.userId);

			if (existdId == 0L) {
				return this.rawInsert();
			} else {
				setPicId(existdId);
				return this.rawUpdate();
			}

		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
	}

	public boolean rawInsert() {
		// TODO Auto-generated method stub
		long id = JDBCBuilder.insert("insert into `" + TABLE_NAME
				+ "`(`picId`,`userId`,`title`,`cdnPath`) values(?,?,?,?)",
				this.picId, this.userId, this.title, this.cdnPath);

		if (id > 0L) {
			log.info("insert ts for the first time !" + picId);
			return true;
		} else {
			log.error("Insert Fails....." + "[Id : ]" + this.picId);

			return false;
		}
	}

	public boolean rawUpdate() {
		long updateNum = JDBCBuilder
				.insert("update `"
						+ TABLE_NAME
						+ "` set  `title` = ?, `cdnPath` = ? where `picId` = ? and userId = ? ",
						this.title, this.cdnPath, this.picId, this.userId);

		if (updateNum > 0L) {
			log.info("update ts success! " + picId);
			return true;
		} else {
			log.error("update Fails....." + "[Id : ]" + this.picId);

			return false;
		}
	}
	
	public static String getCDNPicPathByTitle(Long userId, String title) {
		if(userId == null || userId <= 0) {
			return null;
		}
		if(StringUtils.isEmpty(title)) {
			return null;
		}
		String sql = "select cdnPath from " + TABLE_NAME + " where " +
                "userId = ? and title = ? ";

        return new JDBCExecutor<String>(sql, userId, title) {
            @Override
            public String doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return rs.getString(1);
                } else
                    return null;
            }
        }.call();
		
	}
	
	

}
