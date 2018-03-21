
package controllers;

import models.paipai.PaiPaiUser;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(PaiPaiUser.class)
public class CRUDPaiPaiUsers extends CRUD {

}
