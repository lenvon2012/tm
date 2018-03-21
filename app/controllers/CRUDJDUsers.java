
package controllers;

import models.jd.JDUser;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(JDUser.class)
public class CRUDJDUsers extends CRUD {

}
