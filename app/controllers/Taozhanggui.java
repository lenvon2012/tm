
package controllers;

public class Taozhanggui extends TMController {

    public static void invite() {
        render("op/tbtinvites.html");
    }
    
    public static void award() {
    	render("Application/award.html");
    }
    
    public static void lottery() {
        render("lottery/lotterytzg.html");
    }
    
    public static void search() {
    	render("Kits/delistsearch.html");
    }
}
