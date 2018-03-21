
package controllers;

import models.op.MoreClickNick;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(MoreClickNick.class)
public class CRUDMoreClickNick extends CRUD {

}
