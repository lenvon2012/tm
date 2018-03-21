
package controllers;

import models.helpcenter.HelpNavLevel2;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(HelpNavLevel2.class)
public class HelpNavLevel2s extends CRUD {

}
