
package controllers;

import models.op.MixPlanRecommend;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(MixPlanRecommend.class)
public class CRUDMixPlans extends CRUD {

}
