
package controllers;

import models.op.TMRefundName;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(TMRefundName.class)
public class CRUDTMRefundTrades extends CRUD {
}
