package controllers;

import models.CDNPIc.UserCDNPic;
import play.mvc.With;


@With(Secure.class)
@CRUD.For(UserCDNPic.class)
public class CRUDUserCDNPic extends CRUD {

}
