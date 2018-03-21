package models.jms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = JMSMsgLog.TABLE_NAME)
public class JMSMsgLog extends GenericModel implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(JMSMsgLog.class);
	
	public static final String TABLE_NAME = "jms_msg_log";
	
	public static final JMSMsgLog EMPTY = new JMSMsgLog();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long Id;
	
	/**
	 * ONS消息Id
	 */
	public String msg_id;
	
	/**
	 * 买家昵称
	 */
	@Index(name = "buyer_nick")
	public String buyer_nick;
	
	/**
	 * 订单实付金额
	 */
	public String payment;
	
	/**
	 * 订单状态
	 */
	public String status;
	
	/**
	 * 子订单ID
	 */
	@Index(name = "oid")
	public Long oid;
	
	/**
	 * 评价者：枚举buyer,seller,unknown
	 */
	public String rater;
	
	/**
	 * 主订单ID
	 */
	@Index(name = "tid")
	public Long tid;
	
	/**
	 * 交易类型列表
	 */
	public String type;
	
	/**
	 * 卖家昵称
	 */
	@Index(name = "seller_nick")
	public String seller_nick;
	
	/**
	 * 日志保存时间
	 */
	public Long insertTs;

	public String getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}

	public String getBuyer_nick() {
		return buyer_nick;
	}

	public void setBuyer_nick(String buyer_nick) {
		this.buyer_nick = buyer_nick;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getOid() {
		return oid;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	public String getRater() {
		return rater;
	}

	public void setRater(String rater) {
		this.rater = rater;
	}

	public Long getTid() {
		return tid;
	}

	public void setTid(Long tid) {
		this.tid = tid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSeller_nick() {
		return seller_nick;
	}

	public void setSeller_nick(String seller_nick) {
		this.seller_nick = seller_nick;
	}

	public Long getInsertTs() {
		return insertTs;
	}

	public void setInsertTs(Long insertTs) {
		this.insertTs = insertTs;
	}

	public static Logger getLog() {
		return log;
	}

	public static JMSMsgLog getEmpty() {
		return EMPTY;
	}

	public static DBDispatcher getDp() {
		return dp;
	}

	public static String getSelectallproperty() {
		return SelectAllProperty;
	}

	public void setId(long id) {
		Id = id;
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
		return "Id";
	}

	@Override
	public Long getId() {
		return Id;
	}

	@Override
	public void setId(Long id) {
		this.Id = id;
	}

	@Override
	public String getIdName() {
		return "Id";
	}
	
	public JMSMsgLog() {
	
	}
	
	public JMSMsgLog(String msg_id, String buyer_nick, String payment, String status, Long oid, String rater, Long tid, String type, String seller_nick) {
		super();
		this.msg_id = msg_id;
		this.buyer_nick = buyer_nick;
		this.payment = payment;
		this.status = status;
		this.oid = oid;
		this.rater = rater;
		this.tid = tid;
		this.type = type;
		this.seller_nick = seller_nick;
	}
	
	public JMSMsgLog(String msg_id, MsgContent msgContent) {
		super();
		this.msg_id = msg_id;
		this.buyer_nick = msgContent.getBuyer_nick();
		this.payment = msgContent.getPayment();
		this.status = msgContent.getSeller_nick();
		this.oid = msgContent.getOid();
		this.rater = msgContent.getRater();
		this.tid = msgContent.getTid();
		this.type = msgContent.getType();
		this.seller_nick = msgContent.getSeller_nick();
	}
	
	public static Long findExistId(Long Id) {
		String query = "select Id from " + TABLE_NAME + " where Id = ? ";
		
		return dp.singleLongQuery(query, Id);
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.Id);
			if (existdId <= 0L) {
				return this.rawInsert();
			} else {
//				this.setId(existdId);
//				return this.rawUpdate();
				return false;
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
		
	}
	
	public boolean rawInsert() {
		this.insertTs = System.currentTimeMillis();
		
		String insertSQL = "insert into `" + TABLE_NAME + "`(`msg_id`," +
				"`buyer_nick`,`payment`,`status`,`oid`," +
				"`rater`,`tid`,`type`,`seller_nick`,`insertTs`)" +
				" values(?,?,?,?,?,?,?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.msg_id,
				this.buyer_nick, this.payment, this.status, this.oid,
				this.rater, this.tid, this.type, this.seller_nick, this.insertTs);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[Id : ]" + this.Id + "msgId : ]" + this.msg_id);
			return false;
		}
	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where Id = ? ";
		
		dp.update(sql, this.Id);
		
		return true;
	}
	
	private static JMSMsgLog findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<JMSMsgLog>(dp, query, params) {

			@Override
			public JMSMsgLog doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<JMSMsgLog> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<JMSMsgLog>>(dp, query, params) {

			@Override
			public List<JMSMsgLog> doWithResultSet(ResultSet rs) throws SQLException {

				List<JMSMsgLog> resultList = new ArrayList<JMSMsgLog>();

				while (rs.next()) {
					JMSMsgLog result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	private static final String SelectAllProperty = " `Id`, `msg_id`, `buyer_nick`, `payment`, `status`, " +
			"`oid`, `rater`, `tid`, `type`, `seller_nick`, `insertTs` ";

	private static JMSMsgLog parseResult(ResultSet rs) {
		try {
			
			JMSMsgLog rptObj = new JMSMsgLog();
			
			int colIndex = 1;
			
			rptObj.Id = rs.getLong(colIndex++);
			rptObj.msg_id = rs.getString(colIndex++);
			rptObj.buyer_nick = rs.getString(colIndex++);
			rptObj.payment = rs.getString(colIndex++);
			rptObj.status = rs.getString(colIndex++);
			rptObj.oid = rs.getLong(colIndex++);
			rptObj.rater = rs.getString(colIndex++);
			rptObj.tid = rs.getLong(colIndex++);
			rptObj.type = rs.getString(colIndex++);
			rptObj.seller_nick = rs.getString(colIndex++);
			rptObj.insertTs = rs.getLong(colIndex++);
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
