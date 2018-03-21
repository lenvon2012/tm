package models.relation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = RelationModel.TABLE_NAME)
@JsonIgnoreProperties(value = {
		"id","tableName","idColumn","idName","tableHashKey","persistent","entityId"
})
public class RelationModel extends Model implements PolicySQLGenerator {
	@Transient
	private static final Logger log = LoggerFactory.getLogger(RelationModel.class);
	
	@Transient
    public static final String TABLE_NAME = "relation_model"; 
	
    @Transient
    public static RelationModel EMPTY = new RelationModel();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
	
	private Long modelId;
	private String modelName;
	private String picPath ;
	private boolean isBig;
	private int rowNum;
	private int columnNum;

	public RelationModel() {
        super();
    }
	
	public RelationModel(Long modelId, String modelName, String picPath, boolean isBig, int row, int column) {
        this.modelId = modelId;
        this.modelName = modelName;
		this.picPath=picPath;
        this.isBig=isBig;
        this.rowNum=row;
        this.columnNum=column;
    }
	
	public Long getModelId() {
		return this.modelId;
	}
	
	public void setModelId(Long modelId) {
		this.modelId=modelId;
	}
	
	public void setModelName (String name) {
		this.modelName = name;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public void setBig(boolean isBig) {
		this.isBig = isBig;
	}
	
	public boolean getIsBig() {
		return this.isBig;
	}
	
	public void setRowNum(int row) {
		this.rowNum = row;
	}
	
	public int getRowNum() {
		return this.rowNum;
	}
	public void setColumnNum(int column) {
		this.columnNum = column;
	}
	
	public int getColumnNum() {
		return this.columnNum;
	}
	public void setPicPath(String path) {
		this.picPath = path;
	}
	
	public String getPicPath() {
		return this.picPath;
	}
	
	/*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where modelId = ?";

    private static long findExistId(Long modelId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, modelId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.modelId);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id=existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp
                .insert(
                        "insert into `relation_model`(`modelId`, `modelName`, `picPath`, `isBig`,`rowNum`,`columnNum`) values(?,?,?,?,?,?)",
                        this.modelId, this.modelName, this.picPath, this.isBig, this.rowNum, this.columnNum);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[modelId : ]" + this.modelId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert(
                        "update `relation_model` set `modelId` = ?, `modelName` = ?, `picPath` = ?, `isBig` = ?, `rowNum` = ? , `columnNum` = ? where `id` = ? ",
                        this.modelId, this.modelName, this.picPath, this.isBig, this.rowNum, this.columnNum,  this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[modelId : ]" + this.modelId);

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

    public static List<RelationModel> findAllModel() {
    	return new JDBCExecutor<List<RelationModel>>(dp,"select id,modelId,modelName,picPath,isBig,rowNum,columnNum from " + TABLE_NAME ) {

            @Override
            public List<RelationModel> doWithResultSet(ResultSet rs) throws SQLException {
                final List<RelationModel> resulteList = new ArrayList<RelationModel>();
                while (rs.next()) {
                	RelationModel model = new RelationModel(rs.getLong(2), rs.getString(3), rs.getString(4),
                			rs.getBoolean(5), rs.getInt(6), rs.getInt(7));
                	model.setId(rs.getLong(1));
                	resulteList.add(model);
                }
                return resulteList;
            }
        }.call();
    }
}
