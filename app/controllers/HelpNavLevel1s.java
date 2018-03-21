
package controllers;

import models.helpcenter.HelpNavLevel1;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(HelpNavLevel1.class)
public class HelpNavLevel1s extends CRUD {

}
