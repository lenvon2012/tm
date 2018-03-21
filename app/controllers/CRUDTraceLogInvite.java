
package controllers;

import models.op.TraceLogInvite;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(TraceLogInvite.class)
public class CRUDTraceLogInvite extends CRUD {

}
