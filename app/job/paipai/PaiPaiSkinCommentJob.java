package job.paipai;

import java.util.List;

import models.comment.CommentConf;
import models.comment.Comments;
import models.paipai.PaiPaiUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import ppapi.PaiPaiOrderFormApi.evaluateDealApi;
import ppapi.PaiPaiOrderFormApi.getUnCommentOrderApi;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;
import controllers.PaiPaiAPIConfig;
import dao.paipai.PaiPaiUserDao;

@Every("4h")
public class PaiPaiSkinCommentJob extends Job{
	
	static final Logger log = LoggerFactory.getLogger(PaiPaiSkinCommentJob.class);
	
    public static String TAG = "PaiPaiSkinCommentJob";
    
    public static Long userCount = 0L;
    
    public void doJob() {
        if (APIConfig.get().getApp() == PaiPaiAPIConfig.paipaidiscount.getApp()
                || APIConfig.get().getApp() == PaiPaiAPIConfig.paipaibiaoti.getApp()) {
            doComment();
        }
    }
    
    public static void doComment(){
    	log.info("Do auto comment start......");
    	List<PaiPaiUser> userList =PaiPaiUserDao.findAutoCommentOn();
    	if(CommonUtils.isEmpty(userList)){
    		log.info("no user for auto comment!!!");
    		return ;
    	}
    	for(PaiPaiUser user: userList){
    		log.info("Do auto comment for "+user.getId());
    		List<Comments> orderList=new getUnCommentOrderApi(user).call();//一个月内的订单
    		if(CommonUtils.isEmpty(orderList)){
    			continue;
    		}
    		for(Comments order :orderList){
    			String comment = getComment(user);
    			String errormessage = new evaluateDealApi(user, order.getResult(), comment).call();//result存的订单号
    			if(errormessage!=null){
    				log.error("errormessage");
    				continue;
    			}
    			order.setContent(comment);
    			order.jdbcSave();
    			log.info("Do auto comment success for dealCode :"+order.getResult());
    		}
    	}
    }
    
    public static String getComment(PaiPaiUser user){
        CommentConf commentConf;
        //commentConf = CommentConf.find("userId = ?", user.getId()).first();
        commentConf = CommentConf.findByUserId(user.getId());
        String comment = "";
        if (commentConf == null)
            comment="欢迎您再次光临！！";
        else
            comment=commentConf.getRandomComment();
        return comment;
    }

}
