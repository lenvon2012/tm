
package controllers;

import models.user.User;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(User.class)
public class CRUDDevUsers extends CRUD {

}
