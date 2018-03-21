package models;

/**
 * Created by Administrator on 2014/10/30.
 */

import codegen.CodeGenerator;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.Model;
import play.mvc.Http;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import utils.PlayUtil;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Entity(name = SendMsgLog.TABLE_NAME)
public class SendMsgLog extends Model implements CodeGenerator.PolicySQLGenerator {








    @Transient
    private static final Logger log = LoggerFactory.getLogger(SendMsgLog.class);

    @Transient
    public static final String TABLE_NAME = "send_msg_log";

    @Transient
    public static SendMsgLog EMPTY = new SendMsgLog();

    @Transient
    public static CodeGenerator.DBDispatcher dp = new CodeGenerator.DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    //    需要写入短信log 的字段：
    //    收件人姓名：
    @Index(name = "addrName")
    private String addrName;

    //    收件人手机号码：
    @Index(name = "addrPhone")
    private String addrPhone;

    //    收件人旺旺名称：
    @Index(name = "buyernick")
    private String buyernick;

    //    发送的时间：
    private long sendTime;

    //    是否成功：
    private boolean isSuccess;

    //    短信内容：
    private String msgInfo;

    //    发件人的UserId:
    private String senderId;

    //    备注:
    private String remarks;

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAddrName() {
        return addrName;
    }

    public void setAddrName(String addrName) {
        this.addrName = addrName;
    }

    public String getAddrPhone() {
        return addrPhone;
    }

    public void setAddrPhone(String addrPhone) {
        this.addrPhone = addrPhone;
    }

    public String getBuyernick() {
        return buyernick;
    }

    public void setBuyernick(String buyernick) {
        this.buyernick = buyernick;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(String msgInfo) {
        this.msgInfo = msgInfo;
    }



    public static SendMsgLog createSendMsgListJobTs(String addrName, String addrPhone, String  buyernick, long sendTime,boolean isSuccess,String msgInfo,String senderId,String remarks) {
        SendMsgLog listLog = new SendMsgLog();
        listLog.addrName = addrName;
        listLog.addrPhone = addrPhone;
        listLog.buyernick = buyernick;
        listLog.sendTime = sendTime;
        listLog.isSuccess = isSuccess;
        listLog.msgInfo = msgInfo;
        listLog.senderId = senderId;
        listLog.remarks = remarks;

        return listLog;
    }

//    public void initItemProp() {
//        ItemPlay item = ItemDao.findByNumIid(userId, numIid);
//        if (item != null) {
//            price = item.getPrice();
//            title = item.getTitle();
//            picPath = item.getPicURL();
//        }
//    }

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

    //static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where  id = ? ";

    public static long findExistId() {
        //return dp.singleLongQuery(EXIST_ID_QUERY, id);
        return 0L;
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId();

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }




    public boolean rawInsert() {
        if (StringUtils.isEmpty(msgInfo) == false && msgInfo.length() > 255) {
            msgInfo = msgInfo.substring(0, 255);
        }
        // TODO Auto-generated method stub
        long id = dp
                .insert(
                        "insert into "+SendMsgLog.TABLE_NAME+"(`addrName`,`addrPhone`,`buyernick`,`sendTime`,`isSuccess`,`msgInfo`,`senderId`,`remarks`) values(?,?,?,?,?,?,?,?)",
                        this.addrName,
                        this.addrPhone, this.buyernick, this.sendTime, this.isSuccess, this.msgInfo, this.senderId,this.remarks);

        if (id > 0L) {

            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.id);

            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert(
                        "update "+SendMsgLog.TABLE_NAME+" set  `addrName` = ?, `addrPhone` = ?, `buyernick` = ?, `sendTime` = ?, `isSuccess` = ?, `msgInfo` = ?, `senderId` = ?, `remarks` = ? where `id` = ? ",
                        this.addrName, this.addrPhone, this.buyernick, this.sendTime, this.isSuccess, this.msgInfo,this.senderId,this.remarks, this.getId());

        if (updateNum > 0L) {

            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.id);

            return false;
        }
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



    public static boolean insertLog(String addrName,String addrPhone,String buyernick,Long sendTime,boolean isSuccess,String msgInfo,String senderId,String remarks){
        return  SendMsgLog.createSendMsgListJobTs(addrName, addrPhone, buyernick, sendTime, isSuccess, msgInfo, senderId,remarks).jdbcSave();
    }

    //获得运营商用户
    public static String getSendId(Http.Request request){
        String userName = PlayUtil.getCookieString(request, "login-user");
        if (userName == null) {
            return null;
        }
        try {
            userName = URLDecoder.decode(userName, "utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return userName;
    }

    //是否登陆运营商用户
    public static boolean isLoginSend(Http.Request request){
        String sendId=getSendId(request);
        return  (sendId==null||sendId.trim().equals(""));
    }

    public static List<SendMsgLog> findPageByUserId(String senderId,
                                                 int startPage, int pageSize) {

        int offset = 0;
        if (startPage > 0) {
            offset = (startPage - 1) * pageSize;
        }
        String query = "select id, addrName, addrPhone, buyernick,sendTime,isSuccess,msgInfo,remarks from " + TABLE_NAME
                + " where senderId = ?";



        query += " order by sendTime desc limit ?,? ";

        return new JDBCBuilder.JDBCExecutor<List<SendMsgLog>>(dp, query, senderId, offset, pageSize) {

            @Override
            public List<SendMsgLog> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<SendMsgLog> list = new ArrayList<SendMsgLog>();
                while (rs.next()) {
                    SendMsgLog sendMsgLog = new SendMsgLog();
                    sendMsgLog.setId(rs.getLong(1));
                    sendMsgLog.setAddrName(rs.getString(2));
                    sendMsgLog.setAddrPhone(rs.getString(3));
                    sendMsgLog.setBuyernick(rs.getString(4));
                    sendMsgLog.setSendTime(rs.getLong(5));
                    sendMsgLog.setSuccess(rs.getBoolean(6));
                    sendMsgLog.setMsgInfo(rs.getString(7));
                    sendMsgLog.setRemarks(rs.getString(8));

                    list.add(sendMsgLog);
                }
                return list;
            }

        }.call();
    }

    public static int countByParams(String senderId){
        String query = " select count(*) from " + TABLE_NAME + " where "
                + "senderId = ?";

        return (int)JDBCBuilder.singleLongQuery(query,senderId);
    }
}

