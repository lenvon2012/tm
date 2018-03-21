
package models.trade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;


@Entity(name = "trade_receiver")
public class TradeReceiver extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TradeReceiver.class);

    public static final String TAG = "TradeReceiver";

    public static final String TABLE_NAME = "trade_receiver";

    public static final TradeReceiver EMPTY = new TradeReceiver();

    public TradeReceiver() {

    }

    @Id
    public Long id;

    @Column(columnDefinition = "int default 0")
    int receiverCity;

    @Column(columnDefinition = "int default 0")
    int receiverDistrict;

    @Column(columnDefinition = "int default 0")
    int receiverState;

    @Column(columnDefinition = "int default 0")
    int receiverZip;

    @Column(columnDefinition = "int default 0")
    int buyerArea;

    @Column(columnDefinition = "varchar(64) default NULL")
    String receiverMobile = "";

    @Column(columnDefinition = "varchar(384) default NULL")
    String receiverAddress;

    @Column(columnDefinition = "varchar(64) default NULL")
    String receiverName;
    
    

    public TradeReceiver(TradeDisplay rawTrade) {

    	this.id = rawTrade.getId();
        this.receiverDistrict = District.getId(rawTrade.receiverDistrict);
        this.receiverState = State.getId(rawTrade.receiverState);
        this.receiverZip = ZipCode.getId(rawTrade.receiverZip);
        this.receiverCity = City.getId(rawTrade.receiverCity);
        this.buyerArea = Area.getId(rawTrade.buyerArea);
        this.receiverMobile = rawTrade.receiverMobile;
        this.receiverAddress = rawTrade.receiverAddress;
        this.receiverName = rawTrade.receiverName;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static final String INSERT_SQL = "insert into `trade_receiver`(`id`,`receiverCity`,`receiverDistrict`,`receiverState`,`receiverZip`,`buyerArea`,`receiverMobile`,`receiverAddress`,`receiverName`) values(?,?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {
        Long insertId = JDBCBuilder.insert(INSERT_SQL, this.id, this.receiverCity,
                this.receiverDistrict, this.receiverState, this.receiverZip, this.buyerArea,
                this.receiverMobile, this.receiverAddress, this.receiverName);

        return insertId != null && insertId != 0L;
    }
    
    public static final String INSERT_ONDUP_UPDATE_SQL = "insert into `trade_receiver`(`id`,`receiverCity`,`receiverDistrict`,`receiverState`,`receiverZip`,`buyerArea`,`receiverMobile`,`receiverAddress`,`receiverName`) values(?,?,?,?,?,?,?,?,?) on duplicate key update `receiverCity` = ?, `receiverDistrict` = ?, `receiverState` = ?, `receiverZip` = ?, `buyerArea` = ?, `receiverMobile` = ?, `receiverAddress` = ?, `receiverName` = ?";

    public boolean rawInsertOnDupUpdate() {
        Long insertId = JDBCBuilder.insert(INSERT_ONDUP_UPDATE_SQL, this.id, this.receiverCity, this.receiverDistrict,
                this.receiverState, this.receiverZip, this.buyerArea, this.receiverMobile, this.receiverAddress,
                this.receiverName, this.receiverCity, this.receiverDistrict, this.receiverState, this.receiverZip,
                this.buyerArea, this.receiverMobile, this.receiverAddress, this.receiverName);

        return insertId != null && insertId != 0L;
    }

    public boolean rawUpdate() {
        return false;
    }

    @Override
    public boolean jdbcSave() {
        try {
//            rawInsert();
            rawInsertOnDupUpdate();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public String getIdName() {
        return "id";
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

}
