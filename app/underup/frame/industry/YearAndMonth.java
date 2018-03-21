package underup.frame.industry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = YearAndMonth.TABLE_NAME)
@Table(appliesTo = YearAndMonth.TABLE_NAME)
public class YearAndMonth extends GenericModel implements PolicySQLGenerator{
    @Transient
    private static final Logger log = LoggerFactory.getLogger(YearAndMonth.class);
    
    @Transient
    public static final String TABLE_NAME = "year_and_month";
    
    @Id
    @GeneratedValue
    long id;
    
    long year;
    
    long month;
    
    public YearAndMonth(long year, long month){
        this.year = year;
        this.month = month;
    }
    
    
    @Transient
    public static YearAndMonth EMPTY = new YearAndMonth();
    
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    public YearAndMonth(){
    }
    
    
    public long getYear(){
        return year;
    }
    
    public void setYear(long year){
        this.year = year;
    }
    
    public long getMonth(){
        return month;
    }

    public void setMonth(long month){
        this.month = month;
    }
    
    @Override
    public String getTableName() {
        return YearAndMonth.TABLE_NAME;
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
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {
       long existdId = isThumbIdExcited(this.year, this.month);
       if (existdId == 0L) 
           return this.rawInsert();
       return true;
    }

    @Override
    public String getIdName() {
        return null;
    }
    
    
    @Transient
    static String getExcitedId= "select id from year_and_month where year = ? and month=?";
    
    public static Long isThumbIdExcited(long year, long month){
        return dp.singleLongQuery(getExcitedId, year, month);
    }
    
    @Transient
    static String insertSQL = "insert into year_and_month( `year`, `month`)"
            + " values(?,?)";
    
    public boolean rawInsert(){
         Long id = dp.insert(false, insertSQL, this.year, this.month);
         if (id > 0L) {
              return true;
         } else {
              log.error("Insert Fails.....");
              return false;
         }

    }
    
    public static List<Long> getYearLong(){
        String query = "select year from year_and_month group by year order by year";
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query){
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException{
                List<Long> year = new ArrayList<Long>();
                while(rs.next()){
                    long t = rs.getLong(1);
                    year.add(t);
                }
                return year;
            }
        }.call();
    }
    
    public static List<Long> getMonthLong(long year){
        String query = "select month from year_and_month where year=? order by month ";
        return new JDBCBuilder.JDBCExecutor<List<Long>>(dp, query, year){
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException{
                List<Long> month = new ArrayList<Long>();
                while(rs.next()){
                    long t = rs.getLong(1);
                    month.add(t);
                }
                return month;
            }
        }.call();
    }
}