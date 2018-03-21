package models.carrierTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.CodeGenerator;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = ItemCarryCustom.TABLE_NAME)
public class ItemCarryCustom extends Model implements CodeGenerator.PolicySQLGenerator<Long> {

    public static final Logger log = LoggerFactory.getLogger(ItemCarryCustom.class);

    public static final String TABLE_NAME = "item_carry_custom";

    public static final ItemCarryCustom EMPTY = new ItemCarryCustom();

    public static final CodeGenerator.DBDispatcher dp = new CodeGenerator.DBDispatcher(DBBuilder.DataSrc.BASIC, EMPTY);

    private static final String SELECT_ALL_PROPERTY = "id, userId, sellerCids, locationState, locationCity, postageId, " +
            "stuffStatus, pictureCategoryId, mainPicIndex, whetherSaveOldNumiid, whetherSaveOldUrl, calPriceBaseNumType, multiplyNum, " +
            "addNum, titleKeywordMapper, prefixTitleString, suffixTitleString, approveStatus, diaopaiPic, createTs";

    // 卖家ID
    @Column(nullable = false)
    Long userId;

    // 卖家自定义类目编号 多层类目
    String sellerCids;

    // 宝贝所在地省份
    String locationState;

    // 宝贝所在地城市
    String locationCity;

    // 运费模板ID
    Long postageId;

    // 修旧程度 0:new 1:second
    StuffStatus stuffStatus;

    // 图片存放图片空间的目录ID
    String pictureCategoryId;

    // 宝贝主图设置
    Long mainPicIndex;

    // 把原宝贝ID设置为商家编码
    Boolean whetherSaveOldNumiid;

    // 把原宝贝链接放到描述顶部，鼠标滑过可见！
    Boolean whetherSaveOldUrl;

    // 选择价格类型作为计算价格的基数 0:原价 1:折扣价
    CalPriceBaseNumType calPriceBaseNumType;

    // 算价格公式-数字 用于算数乘
    Double multiplyNum;

    // 计算价格公式-数字 用于算数加
    Double addNum;

    // 标题关键字过滤  K:标题中的关键字  V:替换的关键字
    String titleKeywordMapper;

    // 添加到标题前面的字符串
    String prefixTitleString;

    // 添加到标题末尾的字符串
    String suffixTitleString;

    // 商品上传后的状态 0:onsale 1:instock
    ApproveStatus approveStatus;
    
    // 吊牌图
    String diaopaiPic;
    
    Long createTs;

    public Long getUserId() {
        return userId;
    }

    public ItemCarryCustom setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getSellerCids() {
        return sellerCids;
    }

    public String getSellerCidsLastOne() {
        String sellerCids = this.sellerCids;
        if (StringUtils.isNotEmpty(sellerCids)) {
            String[] split = sellerCids.split(",");
            return split[split.length - 1];
        }
        return null;
    }

    public ItemCarryCustom setSellerCids(String sellerCids) {
        Boolean check = checkSellerCids(sellerCids);
        if (check) {
            this.sellerCids = sellerCids;
        }

        return this;
    }

    private Boolean checkSellerCids(String sellerCids) {
        if (StringUtils.isNotEmpty(sellerCids)) {
            for (String scid : sellerCids.split(",")) {
                if (Long.valueOf(scid) < 0) return false;
            }
            return true;
        }
        return false;
    }

    public String getLocationState() {
        return locationState;
    }

    public ItemCarryCustom setLocationState(String locationState) {
        if (StringUtils.isNotEmpty(locationState) && !locationState.equals("-1")) {
            this.locationState = locationState;
        }
        return this;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public ItemCarryCustom setLocationCity(String locationCity) {
        if (StringUtils.isNotEmpty(locationState) && !locationCity.equals("-1")) {
            this.locationCity = locationCity;
        }
        return this;
    }

    public Long getPostageId() {
        return postageId;
    }

    public ItemCarryCustom setPostageId(Long postageId) {
        this.postageId = postageId;
        return this;
    }

    public enum StuffStatus {
        新品,二手;
    }

    public StuffStatus getStuffStatus() {
        return stuffStatus;
    }

    public ItemCarryCustom setStuffStatus(StuffStatus stuffStatus) {
        this.stuffStatus = stuffStatus;
        return this;
    }

    public String getPictureCategoryId() {
        return pictureCategoryId;
    }

    public Long getLongPictureCategoryId() {
        String pictureCategoryId = this.pictureCategoryId;
        if (StringUtils.isNotEmpty(pictureCategoryId)) {
            String[] split = pictureCategoryId.split(",");
            return Long.valueOf(split[split.length - 1]);
        }
        return null;
    }

    public ItemCarryCustom setPictureCategoryId(String pictureCategoryId) {
        pictureCategoryId = checkPictureCategoryId(pictureCategoryId);
        this.pictureCategoryId = pictureCategoryId;
        return this;
    }

    public String checkPictureCategoryId(String pictureCategoryId) {
        if (StringUtils.isNotEmpty(pictureCategoryId)) {
            List<String> list = new ArrayList();
            for (String pcid : pictureCategoryId.split(",")) {
                if (Long.valueOf(pcid) > 0) list.add(pcid);
            }
            return StringUtils.join(list, ",");
        }

        return null;
    }

    public Long getMainPicIndex() {
        return mainPicIndex;
    }

    public ItemCarryCustom setMainPicIndex(Long mainPicIndex) {
        this.mainPicIndex = mainPicIndex;
        return this;
    }

    public Boolean getWhetherSaveOldNumiid() {
        return whetherSaveOldNumiid;
    }

    public ItemCarryCustom setWhetherSaveOldNumiid(Boolean whetherSaveOldNumiid) {
        this.whetherSaveOldNumiid = whetherSaveOldNumiid;
        return this;
    }

    public Boolean getWhetherSaveOldUrl() {
        return whetherSaveOldUrl;
    }

    public ItemCarryCustom setWhetherSaveOldUrl(Boolean whetherSaveOldUrl) {
        this.whetherSaveOldUrl = whetherSaveOldUrl;
        return this;
    }

    public enum CalPriceBaseNumType {
        原价,折扣价;
    }

    public CalPriceBaseNumType getCalPriceBaseNumType() {
        return calPriceBaseNumType;
    }

    public ItemCarryCustom setCalPriceBaseNumType(CalPriceBaseNumType calPriceBaseNumType) {
        this.calPriceBaseNumType = calPriceBaseNumType;
        return this;
    }

    public Double getMultiplyNum() {
        return multiplyNum;
    }

    public ItemCarryCustom setMultiplyNum(Double multiplyNum) {
        if (multiplyNum != null && multiplyNum >= 0) this.multiplyNum = multiplyNum;
        return this;
    }

    public Double getAddNum() {
        return addNum;
    }

    public ItemCarryCustom setAddNum(Double addNum) {
        this.addNum = addNum;
        return this;
    }

    public String getTitleKeywordMapper() {
        return titleKeywordMapper;
    }

    public ItemCarryCustom setTitleKeywordMapper(String titleKeywordMapper) {
        if (StringUtils.isNotEmpty(titleKeywordMapper)) {
            this.titleKeywordMapper = titleKeywordMapper;
        }
        return this;
    }

    public String getPrefixTitleString() {
        return prefixTitleString;
    }

    public ItemCarryCustom setPrefixTitleString(String prefixTitleString) {
        if (StringUtils.isNotEmpty(prefixTitleString)) {
            this.prefixTitleString = prefixTitleString;
        }
        return this;
    }

    public String getSuffixTitleString() {
        return suffixTitleString;
    }

    public ItemCarryCustom setSuffixTitleString(String suffixTitleString) {
        if (StringUtils.isNotEmpty(suffixTitleString)) {
            this.suffixTitleString = suffixTitleString;
        }
        return this;
    }

    public enum ApproveStatus {
        onsale,instock
    }

    public ApproveStatus getApproveStatus() {
        return approveStatus;
    }

    public ItemCarryCustom setApproveStatus(ApproveStatus approveStatus) {
        this.approveStatus = approveStatus;
        return this;
    }
    
    

    public String getDiaopaiPic() {
        return diaopaiPic;
    }

    public void setDiaopaiPic(String diaopaiPic) {
        this.diaopaiPic = diaopaiPic;
    }

    public Long getCreateTs() {
        return createTs;
    }

    public ItemCarryCustom setCreateTs(Long createTs) {
        this.createTs = createTs;
        return this;
    }

    @Override
    public String getTableHashKey(Long aLong) {
        return null;
    }

    @Override
    public String getIdColumn() {
        return null;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getIdName() {
        return null;
    }

    @Override
    public String getTableName() {
        return null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<ItemCarryCustom> findListByJDBC(String query,Object... params) {
        return new JDBCBuilder.JDBCExecutor<List<ItemCarryCustom>>(dp, query, params) {
            @Override
            public List<ItemCarryCustom> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ItemCarryCustom> resultList = new ArrayList<ItemCarryCustom>();
                while (rs.next()) {
                    ItemCarryCustom result = parseResult(rs);
                    if (result != null) {
                        resultList.add(result);
                    }
                }
                return resultList;
            }
        }.call();
    }

    private static ItemCarryCustom parseResult(ResultSet rs) {
        try {

            ItemCarryCustom itemCarryCustom = new ItemCarryCustom();

            itemCarryCustom.id = rs.getLong("id");
            itemCarryCustom.userId = rs.getLong("userId");
            itemCarryCustom.sellerCids = rs.getString("sellerCids");
            itemCarryCustom.locationState = rs.getString("locationState");
            itemCarryCustom.locationCity = rs.getString("locationCity");
            itemCarryCustom.postageId = rs.getObject("postageId") == null ? null : rs.getLong("postageId");
            itemCarryCustom.stuffStatus = StuffStatus.values()[rs.getInt("stuffStatus")];
            itemCarryCustom.pictureCategoryId = rs.getString("pictureCategoryId");
            itemCarryCustom.mainPicIndex = rs.getLong("mainPicIndex");
            itemCarryCustom.whetherSaveOldNumiid = rs.getBoolean("whetherSaveOldNumiid");
            itemCarryCustom.whetherSaveOldUrl = rs.getBoolean("whetherSaveOldUrl");
            itemCarryCustom.calPriceBaseNumType = CalPriceBaseNumType.values()[rs.getInt("calPriceBaseNumType")];
            itemCarryCustom.multiplyNum = rs.getObject("multiplyNum") == null ? null : rs.getDouble("multiplyNum");
            itemCarryCustom.addNum = rs.getObject("addNum") == null ? null : rs.getDouble("addNum");
            itemCarryCustom.titleKeywordMapper = rs.getString("titleKeywordMapper");
            itemCarryCustom.prefixTitleString = rs.getString("prefixTitleString");
            itemCarryCustom.suffixTitleString = rs.getString("suffixTitleString");
            itemCarryCustom.approveStatus = ApproveStatus.values()[rs.getInt("approveStatus")];
            itemCarryCustom.diaopaiPic = rs.getString("diaopaiPic");
            itemCarryCustom.createTs = rs.getLong("createTs");

            return itemCarryCustom;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.id, this.userId);
            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                this.setId(existdId);
                return this.rawUpdate();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawUpdate() {
        String updateSQL = "update " + TABLE_NAME + " set userId = ?, sellerCids = ?, locationState = ?, locationCity = ?, postageId = ?, " +
                "stuffStatus = ?, pictureCategoryId = ?, mainPicIndex = ?, whetherSaveOldNumiid = ?, whetherSaveOldUrl = ?, " +
                "calPriceBaseNumType = ?, multiplyNum = ?, addNum = ?, titleKeywordMapper = ?, prefixTitleString = ?, " +
                "suffixTitleString = ?, approveStatus = ?, diaopaiPic = ?, createTs = ? where id = ?";
        long updateNum = dp.update(updateSQL,
                this.userId, this.sellerCids, this.locationState, this.locationCity, this.postageId,
                this.stuffStatus != null ? this.stuffStatus.ordinal() : null,
                this.pictureCategoryId, this.mainPicIndex,
                this.whetherSaveOldNumiid, this.whetherSaveOldUrl,
                this.calPriceBaseNumType != null ? this.calPriceBaseNumType.ordinal() : null,
                this.multiplyNum, this.addNum,
                this.titleKeywordMapper, this.prefixTitleString, this.suffixTitleString,
                this.approveStatus != null ? this.approveStatus.ordinal() : null, this.diaopaiPic,
                this.createTs, this.id);

        if (updateNum >= 1) {
            return true;
        } else {
            log.error("Update failed.....[userId] : " + this.userId);
            return false;
        }
    }

    public boolean rawInsert() {
        this.createTs = System.currentTimeMillis();

        String insertSQL = "insert into " + TABLE_NAME + " (userId, sellerCids, locationState, locationCity, postageId, " +
                "stuffStatus, pictureCategoryId, mainPicIndex, whetherSaveOldNumiid, whetherSaveOldUrl, calPriceBaseNumType, multiplyNum, " +
                "addNum, titleKeywordMapper, prefixTitleString, suffixTitleString, approveStatus, diaopaiPic, createTs) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        long id = dp.insert(insertSQL, this.userId, this.sellerCids, this.locationState, this.locationCity, this.postageId,
                this.stuffStatus != null ? this.stuffStatus.ordinal() : null,
                this.pictureCategoryId, this.mainPicIndex,
                this.whetherSaveOldNumiid, this.whetherSaveOldUrl,
                this.calPriceBaseNumType != null ? this.calPriceBaseNumType.ordinal() : null,
                this.multiplyNum, this.addNum,
                this.titleKeywordMapper, this.prefixTitleString, this.suffixTitleString,
                this.approveStatus != null ? this.approveStatus.ordinal() : null,
                this.diaopaiPic, this.createTs);

        if (id > 0L) {
            this.setId(id);
            return true;
        } else {
            log.error("Insert failed.....[userId] : " + this.userId);
            return false;
        }
    }

    public Long findId() {
        String findSql = "select id from " + TABLE_NAME + " ";

        StringBuilder whereSql = new StringBuilder("where 1 = 1 ");
        if (this.userId == null) whereSql.append("and userId is ?");
        else whereSql.append("and userId = ? ");
        if (this.sellerCids == null) whereSql.append("and sellerCids is ? ");
        else  whereSql.append("and sellerCids = ? ");
        if (this.locationState == null) whereSql.append("and locationState is ? ");
        else whereSql.append("and locationState = ? ");
        if (this.locationCity == null) whereSql.append("and locationCity is ? ");
        else whereSql.append("and locationCity = ? ");
        if (this.postageId == null) whereSql.append("and postageId is ? ");
        else whereSql.append("and postageId = ? ");
        if (this.stuffStatus == null) whereSql.append("and stuffStatus is ? ");
        else whereSql.append("and stuffStatus = ? ");
        if (this.pictureCategoryId == null) whereSql.append("and pictureCategoryId is ? ");
        else whereSql.append("and pictureCategoryId = ? ");
        if (this.mainPicIndex == null) whereSql.append("and mainPicIndex is ? ");
        else whereSql.append("and mainPicIndex = ? ");
        if (this.whetherSaveOldNumiid == null) whereSql.append("and whetherSaveOldNumiid is ? ");
        else whereSql.append("and whetherSaveOldNumiid = ? ");
        if (this.whetherSaveOldUrl == null) whereSql.append("and whetherSaveOldUrl is ? ");
        else whereSql.append("and whetherSaveOldUrl = ? ");
        if (this.calPriceBaseNumType == null) whereSql.append("and calPriceBaseNumType is ? ");
        else whereSql.append("and calPriceBaseNumType = ? ");
        if (this.multiplyNum == null) whereSql.append("and multiplyNum is ? ");
        else whereSql.append("and multiplyNum = ? ");
        if (this.addNum == null) whereSql.append("and addNum is ? ");
        else whereSql.append("and addNum = ? ");
        if (this.titleKeywordMapper == null) whereSql.append("and titleKeywordMapper is ? ");
        else whereSql.append("and titleKeywordMapper = ? ");
        if (this.prefixTitleString == null) whereSql.append("and prefixTitleString is ? ");
        else whereSql.append("and prefixTitleString = ? ");
        if (this.suffixTitleString == null) whereSql.append("and suffixTitleString is ? ");
        else whereSql.append("and suffixTitleString = ? ");
        if (this.approveStatus == null) whereSql.append("and approveStatus is ? ");
        else whereSql.append("and approveStatus = ? ");
        if (this.diaopaiPic == null) whereSql.append("and diaopaiPic is ? ");
        else whereSql.append("and diaopaiPic = ? ");

        findSql += whereSql;

        return dp.singleLongQuery(findSql, this.userId, this.sellerCids, this.locationState, this.locationCity, this.postageId,
                this.stuffStatus != null ? this.stuffStatus.ordinal() : null,
                this.pictureCategoryId, this.mainPicIndex,
                this.whetherSaveOldNumiid, this.whetherSaveOldUrl,
                this.calPriceBaseNumType != null ? this.calPriceBaseNumType.ordinal() : null,
                this.multiplyNum, this.addNum,
                this.titleKeywordMapper, this.prefixTitleString, this.suffixTitleString,
                this.approveStatus != null ? this.approveStatus.ordinal() : null, this.diaopaiPic);
    }


    public long findExistId(Long id, Long userId) {
        String query = "select id from " + TABLE_NAME + " where id = ? and userId = ? ";

        return dp.singleLongQuery(query, id, userId);
    }

    public static ItemCarryCustom findByUserId(Long userId) {
        String findSql = "select " + SELECT_ALL_PROPERTY + " from " + TABLE_NAME + " where userId = ? order by createTs desc limit 1";
        List<ItemCarryCustom> listByJDBC = findListByJDBC(findSql, userId);

        if (listByJDBC.isEmpty()) return null;
        else return listByJDBC.get(0);
    }

    public static ItemCarryCustom findById(Long id) {
        String sql = "select " + SELECT_ALL_PROPERTY + " from " + TABLE_NAME + " where id = ?";
        List<ItemCarryCustom> listByJDBC = findListByJDBC(sql, id);
        if (listByJDBC.size() == 0) return null;
        return listByJDBC.get(0);
    }

}
