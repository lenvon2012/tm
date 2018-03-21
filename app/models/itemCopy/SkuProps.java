package models.itemCopy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.*;

import models.item.ItemCatPlay;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import utils.CommonUtil;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Table(name =SkuProps.TABLE_NAME)
public class SkuProps extends GenericModel implements PolicySQLGenerator {
	public static final String TABLE_NAME = "sku_props";

	public static SkuProps EMPTY = new SkuProps();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	public static final Logger log = LoggerFactory.getLogger(SkuProps.class);


	private Long pid;

	private String name;

	private Long vid;

	private Long cid;

	@Column(name = "prop_name")
	private String propName;

	
//	/**
//	 * 根据值名称获取对应符合的SkuProps
//	 * @return 成功返回结果，失败返回null
//	 */
//	public static SkuProps getSkuProps(String name){
//		String sql="select * from "+TABLE_NAME+" where name = ?";
//		return new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql, name) {
//
//			@Override
//			public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
//				if (rs.next()) {
//					return parseResult(rs);
//				} else {
//					return null;
//				}
//			}
//
//		}.call();
//
//        
//	}
	
	public static List<String> colors=Arrays.asList("BUCEROS/本色","Lulin Arts&Cralts/绿","中队蓝","乳白色","乳白色布","亮橙色",
					"亮金色","亮黑色","其他","其他/other","其他颜色","其它","其它颜色","军绿色","单色-红","单色-蓝","单色-黄","卡其色","印字",
					"咖啡","咖啡色","土黄色","墨绿","墨绿色","多种可选","大红色","天蓝色","姜黄色","嫩黄色","孔雀蓝","宝石蓝","宝蓝色","峡谷绿","巧克力色",
					"帆白色","帝王紫","彩色","拼色","明黄色","暗金色","更多颜色","杏色","松绿色","枚红色",
					"柠檬黄","柳黄色","标准黑","栗色","桔红色","桔色","棕色","橘红","橘红色","橘色","橙",
					"橙色","浅棕色","浅灰色","浅紫色","浅绿色","浅蓝色","浅黄色","浅黄色单片","淡紫色","深卡其布色",
					"深棕色","深灰色","深空灰色","深紫色","深蓝色","湖蓝色","火红色","灰","灰色","烟灰色","煤灰色",
					"狼灰色","玫瑰金色","玫红色","瘦身球","白","白粉色","白绿色","白色","白色 蓝色","白色粉红色","白色粉色",
					"白色系列","白色蓝色","皇家蓝","米白色","米色","米黄色","粉","粉紫色","粉紫色 银色","粉紫色银色","粉红色",
					"粉红色系列","粉色","粉色系列","粉蓝色","紫","紫红色","紫罗兰","紫色","紫葡萄干","红","红布","红木色","红的",
					"红色","红色系列","红色黑色","红莓色","红葡萄干","纯白布","经典红","绒棕色","绿","绿色","绿色系列","绿葡萄干",
					"翠绿色","花色","苹果绿","茄紫色","茶色","茶褐色","草绿色","荧光绿","荧光蓝","荧光黄","蓝","蓝布","蓝色","蓝色系列",
					"蓝色银色","藏蓝色","藏青","藏青色","藕色","褐色","西瓜红","设计资料","透明","透明红","透明蓝","酒红色","金","金色",
					"钻孔","银","银色","银色绿色","青","青色","颜色分类","香槟色","驼色","黄","黄色","黄色系列","黑","黑布","黑彩一体","黑曜石",
					"黑色","黑色系列","黑葡萄干");
	public static List<String> sizes=Arrays.asList("1-2M（儿童）","145/52A","145/80A","150/56A","150/80A","155(s)","155/60A",
			"155/80A","160(M)","160/64A","160/80(XS)","160/80(XXS)","160/80(xxs）","160/80A","160/80B",
			"160/80（XXS）","160/84(XS)","160/84A","165","165(L)","165(M)","165/68A","165/80A","165/85A",
			"165/85B","165/88A","165/88B","170(L)","170(XL)","170/72A","170/84A","170/90A","170/90B",
			"170/92A","170/92B","175(XL)","175(XXL)","175/100A","175/76A","175/80A","175/88A","175/95A",
			"175/95B","175/96A","175/96B","180","180(XXL)","180/100A","180/100B","180/84A","180/88A","180/92A",
			"185","185(XXXL)","185/104A","185/104B","185/105A","185/105B","185/92A","185/96A","190","190(XXXXL)",
			"190/100A","190/104","190/110(XXXL)","190/110A","195/112","195/115(XXXXL)","195/115A","2-3M（儿童）",
			"27","28","28-30","29","2L","2XL","3-4M（儿童）","30","30-32","31-34","31号","32","32-34","33","34",
			"34-38","34/75","35","35-38","36","36(S)","36/80","37","38","38-42","38/85","39","39-42","3L","3XL",
			"4-5M（儿童）","40","40/90","41","42","42/95","43","43-46","44","44/100","45(XXL)","46","46(XXL)","47(XXL)",
			"48","48.5","49","49.5","4XL","5-6M（儿童）","50","50*15","52","54","5XL","6-12个月（婴童）","6-7M（儿童）",
			"6XL","7-8M（儿童）","70*50","70A","70B","70C","70D","75A","75B","75C","75D","75E","8-9M（儿童）","80A","80B","80C",
			"80D","80E","85A","85B","85C","85D","85E","9-10M（儿童）","90A","90B","90C","90D","90E","95A","95B","95C","AUS10",
			"AUS12","AUS14","AUS16","AUS18","AUS20","AUS4","AUS6","AUS8","DE30","DE32","DE34","DE36","DE38","DE40","DE42",
			"DE44","DE46","DK30","DK32","DK34","DK36","DK38","DK40","DK42","DK44","DK46","EL","EUR23","EUR24","EUR25","EUR26",
			"EUR27","EUR28","EUR29","EUR30","EUR30.5","EUR31","EUR31.5","EUR32","EUR32.5","EUR33","EUR33.5","EUR34","EUR34.5",
			"EUR35","EUR35.5","EUR36","EUR36.5","EUR37","EUR37.5","eur38","EUR38.5","EUR39","EUR39.5","EUR40","EUR40.5","EUR41",
			"EUR41.5","EUR42","EUR42.5","EUR43","EUR43.5","EUR44","EUR44.5","EUR45","EUR45.5","EUR46","EUR46.5","EUR47","EUR47.5",
			"EUR48","EUR50","EUR52","EUR54","EUR56","F","fr32","FR34","FR36","FR38","FR40","fr42","fr44","fr46","FR48",
			"IT36","IT38","IT40","IT42","IT44","it46","it48","IT50","it52","JP11","JP13","JP15","JP17","JP19","JP3","JP5",
			"JP7","JP9","L","L-LL","ll","LL-LLL","lll","L（58-60cm）","M","M-L","M（56-58cm）","RUS38","RUS40","RUS42","RUS44",
			"RUS46","RUS48","RUS50","RUS52","RUS54","S","S（54-56cm）","UK10","UK12","UK14","UK16","uk18","UK20","UK4","uk6",
			"UK8","US0","us10","US12","us14","Us16","US2","US4","us6","us8","XL","XL（60cm以上）","XS","XXL","XXS","XXXL","XXXS",
			"XXXXL","XXXXXL","儿童","其他尺寸","其它","其它尺寸","其它尺码","加大A","加大XXXL","可调节","均码","大号均码","大码4XL","大码5XL",
			"更大码","更大码（成人)","特大B");
	
	/**
	 * 根据CID,属性值，获取对应符合的SkuProps
	 * @param cid
	 * @param name 属性值
	 * @param isLike 是否根据属性值进行模糊匹配
	 * @return 成功返回结果，失败返回null
	 */
	public static SkuProps getSkuProps(Long cid,String name,boolean isLike){
		StringBuffer sql=new StringBuffer("select * from "+TABLE_NAME+" where name like ? and cid=? limit 1");
		/*//判断是否是定义好的颜色或者尺码
		if (!colors.contains(name)&&!sizes.contains(name)) {
			sql.append(" and cid=? limit 1");
			return new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql.toString(), name,cid) {
				@Override
				public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
					if (rs.next()) {
						return parseResult(rs);
					} else {
						return null;
					}
				}
			}.call();
		}
		sql.append(" limit 1");*/
		
		if (name.contains("cm")) {
			name=name.replace("cm", "");
		}
		
		if (isLike) {
			name="%"+name+"%";
		}
		
		return new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql.toString(), name,cid) {
			@Override
			public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}
		}.call();
        
	}
	
	/**
	 * 查询数据库中是否存在该类目信息
	 * @param cid
	 * @return 
	 */
	public static Boolean checkExsitCid(Long cid){
		StringBuffer sql=new StringBuffer("select * from "+TABLE_NAME+" where cid=?  limit 1");
		SkuProps result=new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql.toString(), cid) {
			@Override
			public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
		
		return result!=null;
        
	}
	
	
	/**
	 * 根据CID,属性名称获取pid
	 * @param cid
	 * @param propname 属性名称
	 * @return 成功返回PID，失败返回null
	 */
	public static Long getPidByCidAndPropName(Long cid,String propname){
		StringBuffer sql=new StringBuffer("select * from "+TABLE_NAME+" where cid=? and prop_name like ? limit 1");
		if (propname.contains("尺")) {
			propname="尺";
		}else if (propname.contains("颜色")) {
			propname="颜色";
		}else if (propname.contains("身高")) {
			propname="身高";
		}
		SkuProps result=new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql.toString(), cid,"%"+propname+"%") {
			@Override
			public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
		
		
		return result!=null?result.getPid():null;
        
	}
	
	
	

	/**
	 * 查询该pid是否属于该cid
	 * @param cid
	 * @param pid 
	 * @return 
	 */
	public static Boolean checkPid(Long cid,Long pid){
		
		StringBuffer sql=new StringBuffer("SELECT * from sku_props where cid=? and pid=? limit 1");
		SkuProps result=new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql.toString(), cid,pid) {
			@Override
			public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
		
		return result!=null;
        
	}
	
	/**
	 * 查询对应的尺码PID
	 * @param cid
	 * @param pid 
	 * @return 
	 */
	public static Long getSizePid(Long cid){
		StringBuffer sql=new StringBuffer("SELECT * from sku_props where cid=? and prop_name like '%尺%' limit 1");
		SkuProps result=new JDBCBuilder.JDBCExecutor<SkuProps>(dp, sql.toString(), cid) {
			@Override
			public SkuProps doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
		
		return result==null?null:result.getPid();
        
	}
	
	
	private static SkuProps parseResult(ResultSet rs) {
		try {
			
			SkuProps sp = new SkuProps();
			sp.cid = rs.getLong("cid");
			sp.vid = rs.getLong("vid");
			sp.pid = rs.getLong("pid");
			sp.name=rs.getString("name");
			return sp;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * @return pid
	 */
	public Long getPid() {
		return pid;
	}

	/**
	 * @param pid
	 */
	public void setPid(Long pid) {
		this.pid = pid;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return vid
	 */
	public Long getVid() {
		return vid;
	}

	/**
	 * @param vid
	 */
	public void setVid(Long vid) {
		this.vid = vid;
	}

	/**
	 * @return cid
	 */
	public Long getCid() {
		return cid;
	}

	/**
	 * @param cid
	 */
	public void setCid(Long cid) {
		this.cid = cid;
	}

	/**
	 * @return prop_name
	 */
	public String getPropName() {
		return propName;
	}

	/**
	 * @param propName
	 */
	public void setPropName(String propName) {
		this.propName = propName;
	}


	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getTableHashKey() {
		return null;
	}

	@Override
	public String getIdColumn() {
		return null;
	}

	@Override
	public void setId(Long id) {

	}

	 	@Override
	    public boolean jdbcSave() {
	        try {
	            return this.rawInsert();
	        } catch (Exception e) {
	            log.warn(e.getMessage(), e);
	            return false;
	        }

	    }

	    @Transient
	    static String insertSQL = "insert into "+TABLE_NAME+"(`pid`,`name`,`vid`,`cid`,`prop_name`) values(?,?,?,?,?)";

	    public boolean rawInsert() {

	        long id = dp.insert(false, insertSQL, this.pid, this.name, this.vid, this.cid, this.propName);

	        if (id > 0L) {
	            return true;
	        } else {
	            log.error("Insert skuprops  Fails....."+this);
	            return false;
	        }

	    }

	   
	  

	@Override
	public String getIdName() {
		return null;
	}

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public String toString() {
		return "SkuProps [pid=" + pid + ", name=" + name + ", vid=" + vid
				+ ", cid=" + cid + ", propName=" + propName + "]";
	}
	
	
}