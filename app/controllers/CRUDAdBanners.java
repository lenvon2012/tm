
package controllers;

import models.op.AdBanner;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(AdBanner.class)
public class CRUDAdBanners extends CRUD {

}
