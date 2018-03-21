
package controllers;

import models.op.UserInvite;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(UserInvite.class)
public class CRUDUserInvites extends CRUD {

}
