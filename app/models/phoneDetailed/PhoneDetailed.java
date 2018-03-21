package models.phoneDetailed;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.GenericModel;
import transaction.CodeGenerator;
import transaction.JDBCBuilder;
import utils.DateUtil;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by Administrator on 2014/11/20.
 */
@Entity(name = PhoneDetailed.TABLE_NAME)
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId",
        "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey",
        "persistent", "tableName", "idName", "idColumn", })
public class PhoneDetailed extends GenericModel implements
        CodeGenerator.PolicySQLGenerator<Long> {

    public static PhoneDetailed _instance = new PhoneDetailed();

    public static CodeGenerator.DBDispatcher dp = new CodeGenerator.DBDispatcher(
            _instance);

    private static final Logger log = LoggerFactory
            .getLogger(PhoneDetailed.class);

    public static final String TABLE_NAME = "phone_detailed";


    private static final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");



    /*

    ID
            用户ID
    商品ID
            手机详情页字段
    当前模板状态:待处理  已经处理  已经过期
    备注：
    创建时间：
    最后更新时间
    */
    //当前记录的处理状态
    private  String status;

    //用户ID
    private Long sellerId;

    //商品ID
    @Id
    private  Long commodityId;

    //备注
    private String remarks;

    // 信息创建时间
    private Long createTs;

    // 信息最后更新时间
    private Long updateTs;

    // 利用构造方法赋值
    public PhoneDetailed() {
    }

    public enum phoneDetailedStatus {
        New,
        Success,
        Failure,
        Overdue,
        Closed//手动关闭，作废
    }

    public PhoneDetailed(String status, Long sellerId, Long commodityId, String remarks) {
        this.status = status;
        this.sellerId = sellerId;
        this.commodityId = commodityId;
        this.remarks = remarks;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public static long findExistId(Long id) {

        String query = "select commodityId from " + PhoneDetailed.TABLE_NAME
                + " where commodityId = ? ";


        return dp.singleLongQuery(query, id);
    }



    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.getId());
            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {
        String insertSql = "insert into "
                + PhoneDetailed.TABLE_NAME
                + "(`status`,`sellerId`,`commodityId`,`remarks` ,"
                + "`createTs`,`updateTs`) "
                + " values(?,?,?,?,?,?)";

        this.createTs = System.currentTimeMillis();
        this.updateTs = this.createTs;

        long id = dp.insert(insertSql,

                phoneDetailedStatus.New.toString(),
                this.sellerId,
                this.commodityId,
                this.remarks,
                this.createTs,
                this.updateTs);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[sellerId : ]"
                    + this.sellerId + "[detailedId : ]" + this.getId());
            return false;
        }
    }

    public boolean rawUpdate() {

        String updateSql = "update "
                + PhoneDetailed.TABLE_NAME
                + " set `status`= ?,`sellerId`= ?,`commodityId`= ?,`remarks` = ?, "
                + " `updateTs` = ? " + " where `commodityId` = ? ";

        this.updateTs = System.currentTimeMillis();

        long updateNum = dp.update(updateSql,
                this.status,
                this.sellerId,
                this.commodityId,
                this.remarks,
                this.updateTs,
                this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.sellerId + "[detailedId : ]"
                    + this.getId());
            return false;
        }
    }

    public boolean rawDelete() {

        String deleteSql = "delete from `" + PhoneDetailed.TABLE_NAME
                + "` where `id` = ? ";

        long updateNum = dp.update(deleteSql, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("delete failed...for :" + this.getId()
                    + "[userQQ : ]" + this.sellerId);
            return false;
        }
    }

    public static boolean deleteById(Long id) {

        if (findExistId(id) <= 0) {
            return true;
        }

        String deleteSql = "delete from `" + PhoneDetailed.TABLE_NAME
                + "` where `id` = ?  ";
        long updateNum = dp.update(deleteSql,id);

        if (updateNum >= 1) {
            return true;
        } else {
            log.error("delete failed...for :" + id );
            return false;
        }
    }



    public static final String QUERY_ALL_SQL = "`commodityId`,`status`,`sellerId`,`remarks`,`createTs`,`updateTs`";

    public static PhoneDetailed parseFraud(ResultSet rs) {
        try {
            PhoneDetailed phoneDetailed = new PhoneDetailed();
            phoneDetailed.setId(rs.getLong(1));
            phoneDetailed.setStatus(rs.getString(2));
            phoneDetailed.setSellerId((rs.getLong(3)));
            phoneDetailed.setRemarks(rs.getString(4));
            phoneDetailed.setCreateTs(rs.getLong(5));
            phoneDetailed.setUpdateTs(rs.getLong(6));

            return phoneDetailed;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     *
     * 获取n个待处理的商品ID ，如果剩余数量不够n个，就全部返回
     * */
    public static List<Long> findIdByPage( String status, String sellerId, String commodityId,
                                                    Long startDate, Long endDate, String orderBy, boolean isDesc, PageOffset po){
        List<Object> paramList = new ArrayList<Object>();
        String whereSql = genSearchRule(status, sellerId, commodityId, startDate, endDate, paramList);

        String query = "select " + QUERY_ALL_SQL +" from " + TABLE_NAME + " where " + whereSql;
        if(StringUtils.isEmpty(orderBy)){
            orderBy = "commodityId";
        }
        query += " order by " + orderBy;

        if(isDesc == true){
            query += " desc ";
        }else{
            query += " asc ";
        }
        query += " limit ?,? ";

        paramList.add(po.getOffset());
        paramList.add(po.getPs());

        Object[] paramArray = paramList.toArray();
        return findIds(query, paramArray);
    }

    public static Set<Long> findCommonityIdByPage(String sellerId, PageOffset pageOffset) {
        List<Object> paramList = new ArrayList<Object>();
        String whereSql = genSearchRule(phoneDetailedStatus.New.toString(), sellerId, null, 0L, 0L, paramList);

        String query = "select `commodityId` from " + TABLE_NAME + " where " + whereSql + " limit ?,? ";
        paramList.add(pageOffset.getOffset());
        paramList.add(pageOffset.getPs());

        Object[] paramArray = paramList.toArray();
        return new JDBCBuilder.JDBCLongSetExecutor(query, paramArray){}.call();
    }


    //返回一个只有ID 的集合，提供给生成模块进行生成
    private static List<Long> findIds(String query, Object... params){
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query, params) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException{
                List<Long> ids = new ArrayList<Long>();

                while (rs.next()){
                    ids.add(rs.getLong(1));
                }
                return ids;
            }
        }.call();
    }


    public static int countBySearchRules( String status,String userQQ, String complainUserQQ,
                                          Long startDate, Long endDate){

        List<Object> paramList = new ArrayList<Object>();

        String query = " select count(commodityId) from " + TABLE_NAME
                + " where ";
        String where=   genSearchRule(status, userQQ, complainUserQQ,
                startDate, endDate, paramList);

        query+=where;

        return (int) dp.singleLongQuery(query, paramList);
    }

    /**
     * 根据当前的用户和手机详情页状态，去统计记录数量
     * */
    public static int countBySellerId( String status,String sellerId, String commodityId,
                                          Long startDate, Long endDate){

        List<Object> paramList = new ArrayList<Object>();

        String query = " select count(commodityId) from " + TABLE_NAME
                + " where ";
        String where=   genSearchRule( status, sellerId, commodityId, startDate, endDate, paramList);

        query+=where;
        Object[] paramArray = paramList.toArray();
        return (int) dp.singleLongQuery(query, paramArray);
    }

    /**
     * 根据当前用户ID 和商品ID  去获取一个模板数据
     * */
    public static List<PhoneDetailed> findById(String sellerId,Long commodityId) {

        String query = "select " + QUERY_ALL_SQL + " from " + TABLE_NAME
                + " where sellerId = ?  and commodityId = ?";

        return new JDBCBuilder.JDBCExecutor<List<PhoneDetailed>>(dp,
                query,sellerId,commodityId) {

            @Override
            public List<PhoneDetailed> doWithResultSet(ResultSet rs)
                    throws SQLException {

                List<PhoneDetailed> tmplList = new ArrayList<PhoneDetailed>();

                while (rs.next()) {
                    PhoneDetailed PhoneDetailed = parseFraud(rs);
                    if (PhoneDetailed != null) {
                        tmplList.add(PhoneDetailed);
                    }
                }
                return tmplList;
            }
        }.call();
    }

    /**
     * 根据当前用户ID 获取新建的模板数据
     * */
    public static List<PhoneDetailed> findBySellerNewTemp(String sellerId) {

        String query = "select " + QUERY_ALL_SQL + " from " + TABLE_NAME
                + " where sellerId = ?  and status = '" + phoneDetailedStatus.New + "'";

        return new JDBCBuilder.JDBCExecutor<List<PhoneDetailed>>(dp,
                query,sellerId) {

            @Override
            public List<PhoneDetailed> doWithResultSet(ResultSet rs)
                    throws SQLException {

                List<PhoneDetailed> tmplList = new ArrayList<PhoneDetailed>();

                while (rs.next()) {
                    PhoneDetailed PhoneDetailed = parseFraud(rs);
                    if (PhoneDetailed != null) {
                        tmplList.add(PhoneDetailed);
                    }
                }
                return tmplList;
            }
        }.call();
    }

    public static int countBySellerId(String sellerId) {
        String query = " select count(commodityId) from " + TABLE_NAME
                + " where sellerId = ?";
        return (int) dp.singleLongQuery(query, sellerId);
    }
   //如果3天内用户提交过相同信息，就不让用户重复提交
    public static int countById(Long commodityId,String status){
        //当前时间减去3天
        Long conditions=System.currentTimeMillis()-(DateUtil.DAY_MILLIS*3);
        String query = " select count(commodityId) from " + TABLE_NAME
                + " where commodityId = ? and createTs>=? and status = ?";
        return (int) dp.singleLongQuery(query, commodityId,conditions,status);
    }

    //组合搜索条件参数
    private static String genSearchRule( String status, String sellerId, String commodityId, Long startDate, Long endDate, List<Object> paramList){

        String whereSql = " 1=1 ";  //规范sql语句


        if(!StringUtils.isEmpty(status)){
            whereSql += " and status = ?";
            paramList.add(status);
        }

        if(StringUtils.isEmpty(sellerId) == false){
            sellerId = CommonUtils.escapeSQL(sellerId);
            whereSql += " and sellerId = ?";
            paramList.add(sellerId);

        }

        if(StringUtils.isEmpty(commodityId) == false){
            commodityId = CommonUtils.escapeSQL(commodityId);
            whereSql += " and commodityId = ?";
            paramList.add(commodityId);

        }

        if(startDate>0){
            whereSql += " and createTs >= ? ";
            paramList.add(startDate);
        }

        if(endDate>0){
            whereSql += " and createTs <= ? ";
            paramList.add(endDate);
        }

        return whereSql;
    }

    public static boolean cancel(Long sellerId) {

        String updateSql = "update "
                + PhoneDetailed.TABLE_NAME
                + " set `status`= ? " + " where `sellerId` = ? ";

        long updateNum = dp.update(updateSql, phoneDetailedStatus.Closed.toString(), sellerId);

        if (updateNum >= 1) {
            return true;
        } else {
            log.error("cancel failer with sellerId " + sellerId);
            return false;
        }
    }


    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey(Long t) {
        return null;
    }

    @Override
    public String getIdColumn() {
        return "commodityId";
    }

    @Override
    public Long getId() {
        return commodityId;
    }

    @Override
    public void setId(Long commodityId) {
        this.commodityId=commodityId;
    }

    @Override
    public String getIdName() {
        return "commodityId";
    }
}

