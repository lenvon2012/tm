//package underup.frame.industry;
//
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.Transient;
//
//import models.item.FrontCatPlay;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import play.db.jpa.GenericModel;
//import transaction.DBBuilder.DataSrc;
//import codegen.CodeGenerator.DBDispatcher;
//import codegen.CodeGenerator.PolicySQLGenerator;
//
//@Entity(name = FrontCatShow.TABLE_NAME)
//public class FrontCatShow extends GenericModel implements PolicySQLGenerator {
//    @Transient
//    private static final Logger log = LoggerFactory.getLogger(FrontCatShow.class);
//
//    @Transient
//    public static final String TABLE_NAME = "front_cat_show";
//
//    @Id
//    private Long frontCid;
//
//    private Long frontParentCid;
//
//    private String name;
//
//    private Boolean isParent;
//
//    private int level;
//
//    public FrontCatShow() {
//
//    }
//
//    @Transient
//    public static final FrontCatShow EMPTY = new FrontCatShow();
//
//    @Transient
//    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
//
//    public FrontCatShow(Long frontCid, Long frontParentCid, String name, Boolean isParent, int level) {
//        this.frontCid = frontCid;
//        this.frontParentCid = frontParentCid;
//        this.name = name;
//        this.isParent = isParent;
//        this.level = level;
//    }
//    
//    public FrontCatShow(FrontCatPlay frontCatPlay){
//        this.frontCid =  frontCatPlay.getCid();
//        this.frontParentCid = frontCatPlay.getParentCid();
//        this.name = frontCatPlay.getName();
//        this.isParent = frontCatPlay.getIsParent();
//        this.level = frontCatPlay.getLevel();
//    }
//
//    public Long getFrontParentCid() {
//        return this.frontParentCid;
//    }
//
//    public void setFrontParentCid(Long frontParentCid) {
//        this.frontParentCid = frontParentCid;
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public boolean getIsParent() {
//        return this.isParent;
//    }
//
//    public int getLevel() {
//        return this.level;
//    }
//
//    @Override
//    public String getTableName() {
//        // TODO Auto-generated method stub
//        return TABLE_NAME;
//    }
//
//    @Override
//    public String getTableHashKey() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public String getIdColumn() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Long getId() {
//        // TODO Auto-generated method stub
//        return frontCid;
//    }
//
//    @Override
//    public void setId(Long id) {
//        this.frontCid = id;
//    }
//
//    @Transient
//    private static final String findExistenedId = "select frontCid from front_cat_show where frontCid=?";
//
//    private static long isFrontCidExistened(long frontCid) {
//        return dp.singleLongQuery(findExistenedId, frontCid);
//    }
//
//    @Override
//    public boolean jdbcSave() {
//        long existedId = isFrontCidExistened(this.frontCid);
//        if (existedId == 0) {
//            return this.rawInsert();
//        } else {
//            setId(existedId);
//            return this.rawUpdate();
//        }
//    }
//
//    @Transient
//    public static final String updateSQL = "update front_cat_show set frontParentCid=?, name=?, isParent=?, level=? where frontCid = ?";
//
//    public boolean rawUpdate() {
//        long id = dp.insert(false, updateSQL, this.frontParentCid, this.name, this.isParent, this.level, this.frontCid);
//        if (id > 0) {
//            log.info("update success ....");
//            return true;
//        } else {
//            log.error("update fail...");
//            return false;
//        }
//    }
//
//    @Transient
//    public static final String insertSQL = "insert into front_cat_show (frontCid, frontParentCid, name, isParent, level) values(?, ?, ?, ?, ?)";
//
//    public boolean rawInsert() {
//        long id = dp.insert(false, insertSQL, this.frontCid, this.frontParentCid, this.name, this.isParent, this.level);
//
//        if (id > 0) {
//            return true;
//        } else {
//            log.error("insert fail...");
//            return false;
//        }
//
//    }
//
//    @Override
//    public String getIdName() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//}
