package actions.ump;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.promotion.TMProActivity;
import models.promotion.TMProActivity.ActivityStatus;
import models.promotion.TMProActivity.ActivityType;
import models.ump.PromotionPlay;
import models.ump.PromotionPlay.ItemPromoteType;
import models.ump.ShopMinDiscountPlay;
import models.ump.PromotionPlay.PromotionParams;
import models.ump.PromotionPlay.TMPromotionStatus;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import tbapi.ump.NewUMPApi.CommonItemActivityDelete;
import tbapi.ump.NewUMPApi.CommonItemDetailAdd;
import tbapi.ump.NewUMPApi.CommonItemDetailDelete;
import tbapi.ump.NewUMPApi.CommonItemDetailListGet;
import tbapi.ump.NewUMPApi.CommonItemDetailUpdate;
import tbapi.ump.UMPApi.UmpRangeAdd;
import tbapi.ump.UMPApi.UmpSingleItemActivityAdd;
import tbapi.ump.UMPApi.UmpSingleItemActivityDelete;
import tbapi.ump.UMPApi.UmpSingleItemActivityUpdate;
import actions.ump.PromotionResult.PromotionActionType;
import actions.ump.PromotionResult.PromotionErrorMsg;
import actions.ump.PromotionResult.PromotionErrorType;
import bustbapi.result.CommonItemDetail;

import com.ciaosir.client.CommonUtils;

import dao.item.ItemDao;
import dao.ump.PromotionDao;

public class UmpPromotionAction {

    private static final Logger log = LoggerFactory.getLogger(UmpPromotionAction.class);
    
    public static final int MaxPromotionNum = 150;
    
    
    //如果是w2权限的错误，那么还要不要执行接下来的宝贝？？？？因为一定是失败的
    
    
    
    public static PromotionResult doAddPromotions(User user, TMProActivity tmActivity,
            List<PromotionParams> paramsList) {
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        String vipNick = "柠檬绿茶运动天下";
        
        Map<Long, PromotionPlay> promotionNumIidMap = PromotionSearchAction.
                findAllOnActivePromotionNumIidMap(user, paramsList);
        
        for (PromotionParams params : paramsList) {
            
            if (params == null) {
                continue;
            }
            
            if (checkIsContinueExecute(errorList) == false) {
                break;
            }
            PromotionPlay existPromotion = promotionNumIidMap.get(params.getNumIid());
            
            if (existPromotion != null && existPromotion.isActive() == true && !vipNick.equalsIgnoreCase(user.getUserNick())) {
                PromotionErrorMsg promotionError = new PromotionErrorMsg(params.getNumIid(), 
                        "该宝贝已经在活动中打过折了！", PromotionErrorType.ItemAlreadyPromotioned);
                
                errorList.add(promotionError);
                
                continue;
            }
            
            // UMP接口改造
            if(tmActivity.getActivityType() == ActivityType.NewDiscount) {
            	Long activityId = tmActivity.getMjsActivityId();
            	Long itemId = params.getNumIid();
            	Long promotionType = 0L;
            	Long promotionValue = 0L;
            	ItemPromoteType type = params.getPromotionType();
            	if (ItemPromoteType.discount.equals(type)) {
            		promotionType = 1L;
            		promotionValue = params.getDiscountRate();
                } else if (ItemPromoteType.decrease.equals(type)) {
                	promotionType = 0L;
                	promotionValue = params.getDecreaseAmount();
                }
            	CommonItemDetailAdd detailAddApi = new CommonItemDetailAdd(user, activityId, itemId, promotionType, promotionValue);
            	Long detailId = detailAddApi.call();
            	if(detailId == null) {
            		// 清理活动中已经被删除的宝贝的活动详情
            		if("优惠详情数量超过限制".equalsIgnoreCase(detailAddApi.getSubErrorMsg())) {
            			List<PromotionPlay> promotionList = PromotionDao.findByTMActivityId(user.getId(), tmActivity.getId());
            			if(!CommonUtils.isEmpty(promotionList)) {
            				for (PromotionPlay promotion : promotionList) {
								Long numIid = promotion.getNumIid();
								ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
								if(item == null) {
									Boolean success = new CommonItemDetailDelete(user, activityId, promotion.getPromotionId()).call();
									if(success) {
										promotion.rawDelete();
									}
								}
							}
            			}
            		}
            		PromotionErrorMsg promotionError = PromotionResult.createPromotionError(detailAddApi, 
                            params.getNumIid());
                    
                    if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                        promotionError.setErrorMsg("系统异常，创建通用单品优惠详情失败，请联系我们！");
                    }
                    
                    errorList.add(promotionError);
            	}
            	//创建成功，先删除数据库
                PromotionDao.deleteByTMActivityIdAndNumIid(user.getId(), tmActivity.getTMActivityId(), 
                        params.getNumIid());
                
                PromotionPlay promotion = new PromotionPlay(user.getId(), detailId, tmActivity, 
                        params, TMPromotionStatus.Active);
                
                boolean isSuccess = promotion.jdbcSave();
                if (isSuccess == false) {
                    PromotionErrorMsg promotionError = new PromotionErrorMsg(params.getNumIid(), 
                            "系统出现异常，数据存储错误，请联系我们！", PromotionErrorType.DataBaseSaveError);
                    errorList.add(promotionError);
                    continue;
                }
                
                successNumIidSet.add(params.getNumIid());
            } else {
            	//不管原来数据库中有没有这个宝贝的promotion，先调用接口，如果接口能调用成功，那么就删除数据库，重新保存
                //创建activity
                UmpSingleItemActivityAdd acvitiyAddApi = new UmpSingleItemActivityAdd(user, 
                        tmActivity, params.getPromotionType(), 
                        params.getDiscountRate(), params.getDecreaseAmount());
                
                Long promotionId = acvitiyAddApi.call();
                if (promotionId == null || promotionId <= 0L || acvitiyAddApi.isApiSuccess() == false) {
                    
                    PromotionErrorMsg promotionError = PromotionResult.createPromotionError(acvitiyAddApi, 
                            params.getNumIid());
                    
                    if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                        promotionError.setErrorMsg("系统异常，创建打折活动失败，请联系我们！");
                    }
                    
                    errorList.add(promotionError);
                    
                    continue;
                } 
                
                //添加range
                UmpRangeAdd rangeAddApi = new UmpRangeAdd(user, promotionId, params.getNumIid());
                TMResult rangeAddRes = rangeAddApi.call();
                
                if (rangeAddRes == null || rangeAddRes.isOk() == false || rangeAddApi.isApiSuccess() == false) {
                    
                    
                    PromotionErrorMsg promotionError = PromotionResult.createPromotionError(rangeAddApi, 
                            params.getNumIid());
                    
                    if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                        promotionError.setErrorMsg("系统异常，添加打折宝贝失败，请联系我们！");
                    }
                    
                    errorList.add(promotionError);
                    
                    //失败，删除新建的activity
                    UmpSingleItemActivityDelete deleteApi = new UmpSingleItemActivityDelete(user, 
                            promotionId);
                    deleteApi.call();
                    
                    continue;
                }
                
                //创建成功，先删除数据库
                PromotionDao.deleteByTMActivityIdAndNumIid(user.getId(), tmActivity.getTMActivityId(), 
                        params.getNumIid());
                
                
                PromotionPlay promotion = new PromotionPlay(user.getId(), promotionId, tmActivity, 
                        params, TMPromotionStatus.Active);
                
                boolean isSuccess = promotion.jdbcSave();
                if (isSuccess == false) {
                    PromotionErrorMsg promotionError = new PromotionErrorMsg(params.getNumIid(), 
                            "系统出现异常，数据存储错误，请联系我们！", PromotionErrorType.DataBaseSaveError);
                    errorList.add(promotionError);
                    continue;
                }
                
                successNumIidSet.add(params.getNumIid());
            }
        }
        if (CommonUtils.isEmpty(successNumIidSet) == false) {
            tmActivity.setStatus(ActivityStatus.ACTIVE);
            tmActivity.jdbcSave();
        }
        
        int notExecuteNum = paramsList.size() - successNumIidSet.size() - errorList.size();
        
        String shopMinDiscountMessage = getShopMinDiscountMessage(user, paramsList);
        
        PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.AddPromotion, 
                successNumIidSet, notExecuteNum, errorList, shopMinDiscountMessage);
        
        return promotionRes;
    }
    
    
    private static String getShopMinDiscountMessage(User user, List<PromotionParams> paramsList) {
        
        if (CommonUtils.isEmpty(paramsList)) {
            return "";
        }
        
        ShopMinDiscountPlay shopDiscount = ShopMinDiscountPlay.findByUserId(user.getId());
        
        if (shopDiscount == null) {
            return "";
        }
        
        int minDiscountRate = shopDiscount.getMinDiscountRate();
        
        if (minDiscountRate <= 0) {
            return "";
        }
        
        int lowDiscountNum = 0;
        for (PromotionParams params : paramsList) {
            long discountRate = params.getDiscountRate();
            if (ItemPromoteType.decrease.equals(params.getPromotionType())) {
                continue;
            }
            if (discountRate < minDiscountRate) {
                lowDiscountNum++;
            }
        }
        
        if (lowDiscountNum <= 0) {
            return "";
        }
        
        String message = "但其中有" + lowDiscountNum + "个宝贝低于了店铺最低折扣" 
                + minDiscountRate * 1.0 / 100 + "折，折扣将无法显示，请修改店铺最低折扣";
        
        return message;
    }
    
    
    //根据错误类型，判断要不要继续执行，比如出现授权错误，那么马上就可以停止
    private static boolean checkIsContinueExecute(List<PromotionErrorMsg> errorList) {
        //当错误个数超过MaxErrorNum个后，就不再执行下去了
        //final int MaxErrorNum = 5;
        
        if (CommonUtils.isEmpty(errorList)) {
            return true;
        }
        
        
        for (PromotionErrorMsg errorObj : errorList) {
            
            int errorType = errorObj.getErrorType();
            
            //数据库存储失败，立即结束
            if (errorType == PromotionErrorType.DataBaseSaveError) {
                return false;
            }
            if (errorType == PromotionErrorType.W2AuthError) {
                return false;
            }
            if (errorType == PromotionErrorType.MinDiscountRateError) {
                return false;
            }
            
        }
        
        return true;
                
                       
    }
    
    
    
    public static PromotionResult updateSomePromotions(User user, TMProActivity tmActivity,
            List<PromotionParams> paramsList) {
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        if (CommonUtils.isEmpty(paramsList)) {
            return new PromotionResult(false, "请先设置要更新的宝贝！");
        }
        
        Map<Long, PromotionPlay> promotionNumIidMap = PromotionSearchAction.findPromotionNumIidMapByParams(user, 
                tmActivity.getId(), 
                paramsList);
        
        for (PromotionParams params : paramsList) {
            
            if (params == null) {
                continue;
            }
            
            if (checkIsContinueExecute(errorList) == false) {
                break;
            }
            
            PromotionPlay existPromotion = promotionNumIidMap.get(params.getNumIid());
            
            if (existPromotion == null) {
                String errorMsg = "找不到相应的宝贝活动，可能已经被删除，" +
                		"请刷新页面后重试，或联系我们！";
                PromotionErrorMsg promotionError = new PromotionErrorMsg(params.getNumIid(), 
                        errorMsg, PromotionErrorType.CannotFindPromotionInDB);
                errorList.add(promotionError);
                
                continue;
            }
            
            if (existPromotion.isActive() == false) {
                log.warn("promotion: " + existPromotion.getPromotionId() + " is not active for user: " 
                        + user.getUserNick() + "---------------");
            }
            
            
            boolean isSuccess = updateOnePromotion(user, tmActivity, existPromotion, 
                    params, errorList, false);
            
            if (isSuccess == true) {
                successNumIidSet.add(params.getNumIid());
            }
            
        }
        
        int notExecuteNum = paramsList.size() - successNumIidSet.size() - errorList.size();
        
        String shopMinDiscountMessage = getShopMinDiscountMessage(user, paramsList);
        
        PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.ModifyPromotion, 
                successNumIidSet, notExecuteNum, errorList, shopMinDiscountMessage);
        
        return promotionRes;
        
        
    }
    
    
    public static PromotionResult updateAllPromotions(User user, TMProActivity tmActivity) {
        
        List<PromotionPlay> promotionList = PromotionDao.findByTMActivityId(user.getId(), 
                tmActivity.getTMActivityId());
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        
        for (PromotionPlay promotion : promotionList) {

            if (promotion.isActive() == false) {
                log.warn("promotion: " + promotion.getPromotionId() + " is not active for user: " 
                        + user.getUserNick() + "---------------");
                continue;
            }
            
            if (checkIsContinueExecute(errorList) == false) {
                break;
            }
            
            PromotionParams params = promotion.genPromotionParams();
            
            boolean isSuccess = updateOnePromotion(user, tmActivity, promotion, 
                    params, errorList, true);
            
            if (isSuccess == true) {
                successNumIidSet.add(promotion.getNumIid());
            }
            
        }
        
        int notExecuteNum = promotionList.size() - successNumIidSet.size() - errorList.size();
        
        PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.ModifyPromotion, 
                successNumIidSet, notExecuteNum, errorList);
        
        return promotionRes;
    }
    
    
    private static boolean updateOnePromotion(User user, TMProActivity tmActivity, 
            PromotionPlay existPromotion, PromotionParams params,
            List<PromotionErrorMsg> errorList, boolean isIgnoreUnActivePromotion) {
        
        boolean isPromotionActive = existPromotion.isActive();
        
        // UMP接口改造
        if(tmActivity.getActivityType() == ActivityType.NewDiscount) {
        	Long activityId = tmActivity.getMjsActivityId();
        	Long itemId = params.getNumIid();
        	Long promotionType = 0L;
        	Long promotionValue = 0L;
        	ItemPromoteType type = params.getPromotionType();
        	if (ItemPromoteType.discount.equals(type)) {
        		promotionType = 1L;
        		promotionValue = params.getDiscountRate();
            } else if (ItemPromoteType.decrease.equals(type)) {
            	promotionType = 0L;
            	promotionValue = params.getDecreaseAmount();
            }
        	Long detailId = existPromotion.getPromotionId();
        	CommonItemDetailUpdate detailUpdateApi = new CommonItemDetailUpdate(user, activityId, detailId, itemId, promotionType, promotionValue); 
        	Boolean success = detailUpdateApi.call();
        	if(!success) {
        		PromotionErrorMsg promotionError = PromotionResult.createPromotionError(detailUpdateApi, 
                        params.getNumIid());
        		errorList.add(promotionError);
        		return false;
        	}
        	existPromotion.updateFromPromotionParams(tmActivity, params);
            boolean isSuccess = existPromotion.jdbcSave();
            if(!isSuccess) {
            	PromotionErrorMsg promotionError = new PromotionErrorMsg(params.getNumIid(), 
                        "系统出现异常，数据更新错误，请联系我们！", PromotionErrorType.DataBaseSaveError);
                errorList.add(promotionError);
                return false;
            }
            return true;
        }
        
        //更新activity
        UmpSingleItemActivityUpdate acvitiyUpdateApi = new UmpSingleItemActivityUpdate(user, 
                tmActivity, existPromotion.getPromotionId(), params.getPromotionType(), 
                params.getDiscountRate(), params.getDecreaseAmount());
        
        Boolean updateRes = acvitiyUpdateApi.call();
        boolean isDeletedInTaobao = false;
        
        if (updateRes == null || updateRes.booleanValue() == false 
                || acvitiyUpdateApi.isApiSuccess() == false) {
            
            PromotionErrorMsg promotionError = PromotionResult.createPromotionError(acvitiyUpdateApi, 
                    params.getNumIid());
            
            if (PromotionErrorType.PromotionDeletedInTaobao == promotionError.getErrorType()) {
                isDeletedInTaobao = true;
            } else {
                isDeletedInTaobao = false;
            }
            
            //isDeletedInTaobao = checkIsPromotionDeletedInTaobao(acvitiyUpdateApi);
            //如果这个活动在淘宝后台被删除了
            if (isDeletedInTaobao == true && isIgnoreUnActivePromotion == true
                    && isPromotionActive == false) {
                
            } else {
                
                if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                    promotionError.setErrorMsg("系统异常，更新打折活动失败，请联系我们！");
                }
                
                errorList.add(promotionError);
                
            }
            
            if (isDeletedInTaobao == false) {
                return false;
            }
            
        } 
        
        boolean isDbSuccess = true;
        
        //如果在淘宝那里被删除了，但数据库中没有被删除的记录，所以是卖家自己在淘宝后台删除的，那么数据库中也要删除
        if (isDeletedInTaobao == true) {
            if (isPromotionActive == true) {
                isDbSuccess = existPromotion.rawDelete();
            } else {
                //这里还是不更新的，不然有疑问：都提示失败了，但为什么折扣还是改了？？？
                isDbSuccess = true;
            }
            
        } else {
            existPromotion.updateFromPromotionParams(tmActivity, params);
            //existPromotion.setTmStatus(TMPromotionStatus.Active);
            isDbSuccess = existPromotion.jdbcSave();
        }
        
        
        if (isDbSuccess == false) {
            PromotionErrorMsg promotionError = new PromotionErrorMsg(params.getNumIid(), 
                    "系统出现异常，数据存储错误，请联系我们！", PromotionErrorType.DataBaseSaveError);
            errorList.add(promotionError);
        }
        
        if (isDbSuccess == false) {
            return false;
        } else {
            if (isDeletedInTaobao == true) {
                return false;
            } else {
                return true;
            }
        }
        
    }
    
    
    /**
     * 彻底删除包括淘宝和数据库中的promotion
     * @param user
     * @param tmActivity
     * @param numIidSet
     * @return
     */
    public static PromotionResult deleteSomePromotionsTotally(User user, TMProActivity tmActivity, 
            Set<Long> numIidSet) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new PromotionResult(false, "请先选择要从活动中删除的宝贝！");
        }
        
        Map<Long, PromotionPlay> promotionNumIidMap = PromotionSearchAction.findActivityPromotionNumIidMap(user, 
                tmActivity.getTMActivityId(), numIidSet);
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        for (Long numIid : numIidSet) {
            
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            if (checkIsContinueExecute(errorList) == false) {
                break;
            }
            
            PromotionPlay existPromotion = promotionNumIidMap.get(numIid);
            
            if (existPromotion == null) {
                String errorMsg = "找不到相应的宝贝活动，可能已经被删除，" +
                        "请刷新页面后重试，或联系我们！";
                PromotionErrorMsg promotionError = new PromotionErrorMsg(numIid, 
                        errorMsg, PromotionErrorType.CannotFindPromotionInDB);
                errorList.add(promotionError);
                
                continue;
            }
            boolean isPromotionActive = existPromotion.isActive();
            
            if (isPromotionActive == false) {
                log.warn("promotion: " + existPromotion.getPromotionId() + " is not active for user: " 
                        + user.getUserNick() + "---------------");
                
            }
            
            boolean isSuccess = deleteOnePromotion(user, tmActivity, existPromotion, 
                    errorList, true);
            
            if (isSuccess == true) {
                successNumIidSet.add(numIid);
            }
        }
        
        int notExecuteNum = numIidSet.size() - successNumIidSet.size() - errorList.size();
        
        PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.DeletePromotion, 
                successNumIidSet, notExecuteNum, errorList);
        
        return promotionRes;
    }
    
    /**
     * 结束活动，但不删除数据库中的记录
     * @param user
     * @param tmActivity
     * @return
     */
    public static PromotionResult cancelDazheActivity(User user, TMProActivity tmActivity) {
        
        if (tmActivity.isDiscountActivity() == false) {
            return new PromotionResult(false, "系统出现异常，这不是打折活动，请联系我们！");
        }
        
        List<PromotionPlay> promotionList = PromotionDao.findByTMActivityId(user.getId(), 
                tmActivity.getTMActivityId());
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<PromotionErrorMsg> errorList = new ArrayList<PromotionErrorMsg>();
        
        
        for (PromotionPlay promotion : promotionList) {
            
            boolean isPromotionActive = promotion.isActive();
            
            if (isPromotionActive == false) {
                log.warn("promotion: " + promotion.getPromotionId() + " is not active for user: " 
                        + user.getUserNick() + "---------------");
                
            }
            
            if (checkIsContinueExecute(errorList) == false) {
                break;
            }
            
            
            boolean isSuccess = deleteOnePromotion(user, tmActivity, promotion, errorList, false);
            
            //if (isSuccess == true && isPromotionActive == true) {
            if (isSuccess == true) {
                successNumIidSet.add(promotion.getNumIid());
            }
            
        }
        
        int notExecuteNum = promotionList.size() - successNumIidSet.size() - errorList.size();
        
        //有错误的宝贝，就先不修改TMProActivity的状态了
        if (CommonUtils.isEmpty(errorList) == false
                || notExecuteNum > 0) {
            
            PromotionResult promotionRes = new PromotionResult(user, PromotionActionType.DeletePromotion, 
                    successNumIidSet, notExecuteNum, errorList);
            
            return promotionRes;
        } else {
            
            //tmActivity.setStatus(ActivityStatus.UNACTIVE);
            tmActivity.setUnActiveStatusAndEndTime();
            
            tmActivity.jdbcSave();
            
            PromotionResult promotionRes = new PromotionResult(true, "打折活动结束成功！");
            
            return promotionRes;
        }
        
        
    }
    
    
    private static boolean deleteOnePromotion(User user, TMProActivity tmActivity, 
            PromotionPlay existPromotion,
            List<PromotionErrorMsg> errorList, boolean isDeleteFromDB) {
        
    	// UMP接口改造
    	if(tmActivity.getActivityType() == ActivityType.NewDiscount) {
    		Long activityId = tmActivity.getMjsActivityId();
    		Long detailId = existPromotion.getPromotionId();
    		CommonItemDetailDelete detailDeleteApi = new CommonItemDetailDelete(user, activityId, detailId);
    		Boolean success = detailDeleteApi.call();
    		if(!success) {
    			if(!"优惠详情不存在".equalsIgnoreCase(detailDeleteApi.getSubErrorMsg())) {
    				PromotionErrorMsg promotionError = PromotionResult.createPromotionError(detailDeleteApi, 
    						existPromotion.getNumIid());
    				errorList.add(promotionError);
    				return false;
    			}
    		}
    		boolean isSuccess = existPromotion.rawDelete();
    		if(!isSuccess) {
    			PromotionErrorMsg promotionError = new PromotionErrorMsg(existPromotion.getNumIid(), 
    					"系统出现异常，数据更新错误，请联系我们！", PromotionErrorType.DataBaseSaveError);
    			errorList.add(promotionError);
    			return false;
    		}
    		return true;
    	}
    	
        UmpSingleItemActivityDelete deleteApi = new UmpSingleItemActivityDelete(user, 
                existPromotion.getPromotionId());
        
        Boolean isSuccess = deleteApi.call();
        boolean isDeletedInTaobao = false;
        
        if (isSuccess == null || isSuccess.booleanValue() == false || deleteApi.isApiSuccess() == false) {
            
            PromotionErrorMsg promotionError = PromotionResult.createPromotionError(deleteApi, 
                    existPromotion.getNumIid());
            
            if (PromotionErrorType.PromotionDeletedInTaobao == promotionError.getErrorType()) {
                isDeletedInTaobao = true;
            } else {
                isDeletedInTaobao = false;
            }
            
            //isDeletedInTaobao = checkIsPromotionDeletedInTaobao(deleteApi);
            //如果这个活动在淘宝后台被删除了
            if (isDeletedInTaobao == true) {
                
            } else {
                
                if (StringUtils.isEmpty(promotionError.getErrorMsg())) {
                    promotionError.setErrorMsg("系统异常，删除打折宝贝失败，请联系我们！");
                }
                errorList.add(promotionError);
                
                return false;
            }
            
        }
        boolean isDbSuccess = true;
        
        if (isDeleteFromDB == true) {
            isDbSuccess = existPromotion.rawDelete();
        } else {
            
            //如果在淘宝那里被删除了
            if (isDeletedInTaobao == true) {
                //但数据库中没有被删除的记录，所以是卖家自己在淘宝后台删除的，那么数据库中也要删除
                if (existPromotion.isActive() == true) {
                    isDbSuccess = existPromotion.rawDelete();
                } else {
                    existPromotion.setTmStatus(TMPromotionStatus.UnActive);
                    isDbSuccess = existPromotion.jdbcSave();
                }
            } else {
                existPromotion.setTmStatus(TMPromotionStatus.UnActive);
                isDbSuccess = existPromotion.jdbcSave();
            }
            
            
        }
        
        
        if (isDbSuccess == false) {
            PromotionErrorMsg promotionError = new PromotionErrorMsg(existPromotion.getNumIid(), 
                    "系统出现异常，数据存储错误，请联系我们！", PromotionErrorType.DataBaseSaveError);
            errorList.add(promotionError);
        }
        
        return isDbSuccess;
    }
    
    /*
    //这个promotion是否是之前就在淘宝后台被删除掉了
    private static boolean checkIsPromotionDeletedInTaobao(TBApi tbApi) {
        
        if (tbApi == null) {
            return false;
        }
        
        //
        boolean isDeletedInTaobao = false;
        
        
        
        return isDeletedInTaobao;
        
        
        
    }
    
    */
    
    /**
     * UMP接口改造 
     * 删除通用单品优惠活动
     */
    public static PromotionResult cancelNewDazheActivity(User user, TMProActivity tmActivity) {
        
        if (!tmActivity.isNewDiscountActivity()) {
            return new PromotionResult(false, "系统出现异常，这不是新版打折活动，请联系我们！");
        }
        
        CommonItemActivityDelete activityDeleteApi = new CommonItemActivityDelete(user, tmActivity.getMjsActivityId());
        Boolean success = activityDeleteApi.call();
        if(!success) {
        	return new PromotionResult(false, "系统出现异常，删除通用单品优惠活动接口调用失败，请联系我们！");
        }
        
        tmActivity.setUnActiveStatusAndEndTime();
        boolean isSuccess = tmActivity.jdbcSave();
        if(!isSuccess) {
        	return new PromotionResult(false, "系统出现异常，数据库更新失败，请联系我们！");
        }
        
        List<PromotionPlay> promotionList = PromotionDao.findByTMActivityId(user.getId(), 
                tmActivity.getTMActivityId());
        for (PromotionPlay promotionPlay : promotionList) {
			promotionPlay.setTmStatus(TMPromotionStatus.UnActive);
			promotionPlay.jdbcSave();
		}
        
        PromotionResult promotionRes = new PromotionResult(true, "打折活动结束成功！");
        
        return promotionRes;
        
    }
    
}
