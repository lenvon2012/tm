
package controllers;

import models.op.RecommendFeedBack;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(RecommendFeedBack.class)
public class CRUDRecommendFeedback extends CRUD {

}
