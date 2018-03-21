
package controllers;

import models.helpcenter.TMHelpArticle;
import play.mvc.With;

@With(Secure.class)
@CRUD.For(TMHelpArticle.class)
public class TMHelpArticles extends CRUD {
    
//    public static void articles() throws Exception {
//        ObjectType type = (ObjectType) Java.invokeStaticOrParent(TMHelpArticles.class, "createObjectType",
//                TMHelpArticle.class);
//        type.name = TMHelpArticles.class.getSimpleName().replace("$", "");
//        type.controllerName = TMHelpArticles.class.getSimpleName().toLowerCase().replace("$", "");
//        type.controllerClass = TMHelpArticles.class;
//        TMHelpArticle object = TMHelpArticle.find("1 = 1").first();
//        render("helpcenter/show.html", type, object);
//    }
}
