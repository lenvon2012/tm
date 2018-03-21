package models.relation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.item.ItemPlay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;

import dao.item.ItemDao;



@Entity(name = RelationPlan.TABLE_NAME)
@JsonIgnoreProperties(value = {
		"tableName","idColumn","idName","tableHashKey","persistent","entityId"
})
public class RelationPlan extends Model implements PolicySQLGenerator {
	@Transient
	private static final Logger log = LoggerFactory.getLogger(RelationPlan.class);
	
	@Transient
    public static final String TABLE_NAME = "relation_plan"; 

	private String planName;
	private Long modelId;
	@Index(name="userId")
	private Long userId;
	private String numIids;
	
	public RelationPlan() {
        super();
    }
	
	public RelationPlan(String planName, Long modelId, Long userId, String numIids) {
        this.planName = planName;
        this.modelId = modelId;
		this.userId=userId;
        this.numIids=numIids;
    }
	
	
	public void setUserId(Long userId) {
		this.userId=userId;
	}
	
	public Long getModelId() {
		return this.modelId;
	}
	
	public void setModelId(Long modelId) {
		this.modelId=modelId;
	}
	
	public Long getUserId() {
		return this.userId;
	}
	
	public void setPlanName (String name) {
		this.planName = name;
	}
	
	public String getPlanName() {
		return this.planName;
	}
	
	public void setNumIids(String numIids) {
		this.numIids = numIids;
	}
	
	public String getNumIids() {
		return this.numIids;
	}
	
	public void addNumIid(Long numIid) {
		this.numIids += numIids+numIid+"!@#";
	}
	
	public void addNumIids(String numIids){
		if(!numIids.isEmpty()) {
			String numIidsStr[] = numIids.split("!@#");
			for(String numIid : numIidsStr) {
				if(!this.numIids.contains(numIid)) {
					this.numIids += numIid + "!@#";
				}
			}
		}
	}
	
	public void deleteNumIid(Long numIid) {
		this.numIids = this.numIids.replace(numIid+"!@#", "");
	}
	
	public List<Long> getNumIdList() {
		List<Long> numIidLists =  new ArrayList<Long>();
		if(this.numIids.isEmpty()) 
			return numIidLists;
		String numIidsStr[] = this.numIids.split("!@#");
		for (String numIid : numIidsStr) {
			numIidLists.add(Long.parseLong(numIid));
		}
		return numIidLists;
	}
	
	public List<String> getPicURLs() {
		if(this.numIids.isEmpty()) {
			return null;
		}
		List<String> picURLs = new ArrayList<String>();
		List<Long> numIidsList = getNumIdList();
		List<ItemPlay> items = null;
		items = ItemDao.findByNumIids(userId, numIidsList);
		if(CommonUtils.isEmpty(items)) {
			return null;
		}
		// to ensure the match of numIid and picURL
		for(Long numIid : numIidsList){
			for(ItemPlay item : items) {
				if(item.getNumIid().equals(numIid)){
					picURLs.add(item.getPicURL());
				}
			}
		}
		return picURLs;
	}
	
	public static Set<Long> findByUser(Long userId){
		//List<RelationPlan> rps = RelationPlan.find("userId = ?", userId).fetch();
		List<RelationPlan> rps = RelationPlan.nativeQuery("userId = ?", userId);
		if(CommonUtils.isEmpty(rps)){
			return null;
		}
		Set<Long> relatedItems = new HashSet<Long>();
		for(RelationPlan rp:rps){
			List<Long> numIids = rp.getNumIdList();
			relatedItems.addAll(numIids);
		}
		return relatedItems;
	}
	
	public static Set<Long> findByUserAndPlanId(Long userId,Long planId){
		//List<RelationPlan> rps = RelationPlan.find("userId = ? and id = ?", userId,planId).fetch();
		List<RelationPlan> rps = RelationPlan.nativeQuery("userId = ? and id = ?", userId,planId);
		if(CommonUtils.isEmpty(rps)){
			return null;
		}
		Set<Long> relatedItems = new HashSet<Long>();
		for(RelationPlan rp:rps){
			List<Long> numIids = rp.getNumIdList();
			relatedItems.addAll(numIids);
		}
		return relatedItems;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and id = ?";

    private static long findExistId(Long userId, Long id) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.id);
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
        long id = JDBCBuilder
                .insert(
                        "insert into `relation_plan`(`planName`, `modelId`, `userId`,`numIids`) values(?,?,?,?)",
                        this.planName, this.modelId, this.userId, this.numIids);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder
                .insert(
                        "update `relation_plan` set `planName` = ?, `modelId` = ?, `userId` = ?, `numIids` = ?  where `id` = ? ",
                        this.planName, this.modelId, this.userId, this.numIids, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);

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
    
    public static String RELATION_PLAN_QUERY = "select id,planName,modelId,userId,numIids from " + TABLE_NAME
            + " where ";
    public static List<RelationPlan> nativeQuery(String query, Object...params) {
		return new JDBCExecutor<List<RelationPlan>>(RELATION_PLAN_QUERY+query, params) {

            @Override
            public List<RelationPlan> doWithResultSet(ResultSet rs) throws SQLException {
                final List<RelationPlan> resulteList = new ArrayList<RelationPlan>();
                while (rs.next()) {
                	RelationPlan plan = new RelationPlan(rs.getString(2), rs.getLong(3), rs.getLong(4),
                			rs.getString(5));
                	plan.setId(rs.getLong(1));
                	resulteList.add(plan);
                }
                return resulteList;
            }
        }.call();
	}
    
    public static RelationPlan singleQuery(String query, Object...params) {
		return new JDBCExecutor<RelationPlan>(RELATION_PLAN_QUERY+query, params) {

            @Override
            public RelationPlan doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                	RelationPlan plan = new RelationPlan(rs.getString(2), rs.getLong(3), rs.getLong(4),
                			rs.getString(5));
                	plan.setId(rs.getLong(1));
                	return plan;
                } else {
                	return null;
                }
            }
        }.call();
	}
}
