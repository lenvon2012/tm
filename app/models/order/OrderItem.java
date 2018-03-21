
package models.order;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = OrderItem.TABLE_NAME)
public class OrderItem extends GenericModel implements PolicySQLGenerator {

    public static final String TABLE_NAME = "order_item";

    public static final OrderItem EMPTY = new OrderItem();

    public OrderItem() {
        super();
    }

    public OrderItem(OrderDisplay orderDisplay) {
        super();
        this.oid = orderDisplay.oid;
        this.picPath = orderDisplay.picPath;
        this.title = orderDisplay.title;
    }

    public OrderItem(Long oid, String picPath, String title) {
        super();
        this.oid = oid;
        this.picPath = picPath;
        this.title = title;
    }

    @Id
    public Long oid;

    @Column(columnDefinition="varchar(511) default NULL")
    String picPath;

    @Column(columnDefinition="varchar(511) default NULL")
    String title;

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "oid";
    }

    @Override
    public Long getId() {
        return oid;
    }

    @Override
    public void setId(Long id) {
        this.oid = id;
    }

    @Override
    public boolean jdbcSave() {
        long existId = exist();
        if (existId != 0L) {
            return rawUpdate();
        } else {
            return rawInsert();
        }
    }

    public long exist() {
        return JDBCBuilder.singleLongQuery("select oid from order_item where oid = ?", this.oid);
    }

    public boolean rawInsert() {
        long id = JDBCBuilder.insert(
                "insert into `order_item`(`oid`,`picPath`,`title`) values(?,?,?)", this.oid,
                this.picPath, this.title);

        return id != 0L;
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(
                "update `order_item` set  `picPath` = ?, `title` = ? where `oid` = ? ",
                this.picPath, this.title, this.getId());
        return updateNum != 0L;
    }
    
    public boolean rawInsertOnDupUpdate() {
        long id = JDBCBuilder.insert(
                "insert into `order_item`(`oid`,`picPath`,`title`) values(?,?,?) on duplicate key update `picPath` = ?, `title` = ?", this.oid,
                this.picPath, this.title, this.picPath, this.title);

        return id != 0L;
    }

    @Override
    public String getIdName() {
        return "oid";
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }
}
