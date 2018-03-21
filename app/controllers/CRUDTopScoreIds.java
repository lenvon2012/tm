package controllers;

import models.topscoreid.TopScoreId;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(TopScoreId.class)
public class CRUDTopScoreIds extends CRUD {

}
