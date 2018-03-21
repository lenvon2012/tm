package models.user;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.UserCredit;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

@Entity(name = UserRateApiFetchResultLog.TABLE_NAME)
public class UserRateApiFetchResultLog extends Model implements PolicySQLGenerator  {

    public final static Logger log = LoggerFactory.getLogger(UserRateApiFetchResultLog.class);

    public final static String TABLE_NAME = "user_rate_api_fetch_result_log";

    @Transient
    public static UserRateApiFetchResultLog EMPTY = new UserRateApiFetchResultLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Index(name = "nick")
    public String nick;

    /*
     * 收到的好评总条数。取值范围:大于零的整数
     */
    public long buyerCreditGoodNum;
     
    /*
     * 信用等级（是根据score生成的），信用等级：淘宝会员在淘宝网上的信用度，分为20个级别，级别如：level = 1 时，表示一心；level = 2 时，表示二心
     */
    public long buyerCreditLevel;
    
    /*
     * 信用总分（“好评”加一分，“中评”不加分，“差评”扣一分。分越高，等级越高）
     */
    public long buyerCreditScore;
    
    /*
     * 收到的评价总条数。取值范围:大于零的整数
     */
    public long buyerCreditTotalNum;
    
    public long sellerCreditGoodNum;
    
    public long sellerCreditLevel;
    
    public long sellerCreditScore;
    
    public long sellerCreditTotalNum;
    
    /*
     * 用户注册时间
     */
    public Long regTs;
    
    public Long createTs;
    
    public Long updateTs;

    public UserRateApiFetchResultLog() {
    	super();
    }
    
    public UserRateApiFetchResultLog(String nick, long buyerCreditGoodNum,
    		long buyerCreditLevel, long buyerCreditScore,
    		long buyerCreditTotalNum, long sellerCreditGoodNum,
    		long sellerCreditLevel, long sellerCreditScore,
    		long sellerCreditTotalNum, Long regTs) {
		super();
		this.nick = nick;
		this.buyerCreditGoodNum = buyerCreditGoodNum;
		this.buyerCreditLevel = buyerCreditLevel;
		this.buyerCreditScore = buyerCreditScore;
		this.buyerCreditTotalNum = buyerCreditTotalNum;
		this.sellerCreditGoodNum = sellerCreditGoodNum;
		this.sellerCreditLevel = sellerCreditLevel;
		this.sellerCreditScore = sellerCreditScore;
		this.sellerCreditTotalNum = sellerCreditTotalNum;
		this.regTs = regTs;
	}
    
    public UserRateApiFetchResultLog(com.taobao.api.domain.User user) {
		super();
		if(user != null) {
			this.nick = user.getNick();
			UserCredit buyerCredit = user.getBuyerCredit();
			if(buyerCredit != null) {
				this.buyerCreditGoodNum = buyerCredit.getGoodNum();
				this.buyerCreditLevel = buyerCredit.getLevel();
				this.buyerCreditScore = buyerCredit.getScore();
				this.buyerCreditTotalNum = buyerCredit.getTotalNum();
			}
			
			UserCredit sellerCredit = user.getSellerCredit();
			if(buyerCredit != null) {
				this.sellerCreditGoodNum = sellerCredit.getGoodNum();
				this.sellerCreditLevel = sellerCredit.getLevel();
				this.sellerCreditScore = sellerCredit.getScore();
				this.sellerCreditTotalNum = sellerCredit.getTotalNum();
			}

			Date date = user.getCreated();
			if(date != null) {
				this.regTs = date.getTime();
			}
			
		}
		
	}

    static String EXIST_NICK_QUERY = "select 1 from " + TABLE_NAME + " where  nick = ? ";
    public static long findExist(String nick) {
        return JDBCBuilder.singleLongQuery(EXIST_NICK_QUERY, nick);
    }
    
    public boolean jdbcSave() {
    	long existdId = findExist(this.nick);
        try {
        	if (existdId <= 0L) {
                return this.rawInsert();
            } else {
            	return this.rawUpdate();
            }
        } catch (Exception e) {
            return false;
        }
    }

    static String insertSQL = "insert into `user_rate_api_fetch_result_log`" +
    		"(`nick`,`regTs`,`buyerCreditGoodNum`,`buyerCreditLevel`,`buyerCreditScore`,`buyerCreditTotalNum`," +
    		"`sellerCreditGoodNum`,`sellerCreditLevel`,`sellerCreditScore`,`sellerCreditTotalNum`,`createTs`,`updateTs`) " +
    		" values(?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.nick, this.regTs, 
        		this.buyerCreditGoodNum, this.buyerCreditLevel, this.buyerCreditScore, this.buyerCreditTotalNum,
        		this.sellerCreditGoodNum, this.sellerCreditLevel, this.sellerCreditScore, this.sellerCreditTotalNum,
        		System.currentTimeMillis(), 0L);

        log.info("[Insert UserRateApiFetchResultLog nick:]" + nick);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert UserRateApiFetchResultLog Fails.....");
            return false;
        }
    }

    public boolean rawUpdate() {
    	if(StringUtils.isEmpty(this.nick)) {
    		return false;
    	}
        long updateNum = dp.insert(
                "update `" + TABLE_NAME + "` set  `regTs` = ?, `buyerCreditGoodNum` = ?, `buyerCreditLevel`=?," +
                		" `buyerCreditScore` = ?, `buyerCreditTotalNum` = ?, `sellerCreditGoodNum`=?," +
                		" `sellerCreditLevel` = ?, `sellerCreditScore` = ?, `sellerCreditTotalNum`=?," +
                		" `updateTs` = ? where `nick` = ? ",
                this.regTs, this.buyerCreditGoodNum, this.buyerCreditLevel, this.buyerCreditScore, this.buyerCreditTotalNum,
                this.sellerCreditGoodNum, this.sellerCreditLevel, this.sellerCreditScore, this.sellerCreditTotalNum, 
                System.currentTimeMillis(), this.nick);

        if (updateNum > 0L) {
            log.info("update ts success! " + nick);
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.nick);

            return false;
        }
    }

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public long getBuyerCreditGoodNum() {
		return buyerCreditGoodNum;
	}

	public void setBuyerCreditGoodNum(long buyerCreditGoodNum) {
		this.buyerCreditGoodNum = buyerCreditGoodNum;
	}

	public long getBuyerCreditLevel() {
		return buyerCreditLevel;
	}

	public void setBuyerCreditLevel(long buyerCreditLevel) {
		this.buyerCreditLevel = buyerCreditLevel;
	}

	public long getBuyerCreditScore() {
		return buyerCreditScore;
	}

	public void setBuyerCreditScore(long buyerCreditScore) {
		this.buyerCreditScore = buyerCreditScore;
	}

	public long getBuyerCreditTotalNum() {
		return buyerCreditTotalNum;
	}

	public void setBuyerCreditTotalNum(long buyerCreditTotalNum) {
		this.buyerCreditTotalNum = buyerCreditTotalNum;
	}

	public long getSellerCreditGoodNum() {
		return sellerCreditGoodNum;
	}

	public void setSellerCreditGoodNum(long sellerCreditGoodNum) {
		this.sellerCreditGoodNum = sellerCreditGoodNum;
	}

	public long getSellerCreditLevel() {
		return sellerCreditLevel;
	}

	public void setSellerCreditLevel(long sellerCreditLevel) {
		this.sellerCreditLevel = sellerCreditLevel;
	}

	public long getSellerCreditScore() {
		return sellerCreditScore;
	}

	public void setSellerCreditScore(int sellerCreditScore) {
		this.sellerCreditScore = sellerCreditScore;
	}

	public long getSellerCreditTotalNum() {
		return sellerCreditTotalNum;
	}

	public void setSellerCreditTotalNum(long sellerCreditTotalNum) {
		this.sellerCreditTotalNum = sellerCreditTotalNum;
	}

	public Long getRegTs() {
		return regTs;
	}

	public void setRegTs(Long regTs) {
		this.regTs = regTs;
	}

	public Long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Long createTs) {
		this.createTs = createTs;
	}

	public Long getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(Long updateTs) {
		this.updateTs = updateTs;
	}
   
	/*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableHashKey()
     */
    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableName()
     */
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.id = id;
    }

    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

}
