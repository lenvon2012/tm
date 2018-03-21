var TM = TM || {};

((function ($, window) {

	TM.Welcome = TM.Welcome || {};
	var Welcome = TM.Welcome;

	Welcome.init = Welcome.init || {};
	Welcome.init = $.extend({
		doInit: function(container) {
			Welcome.container = container;
			
//			var numIid = parent.document.getElementById("#tzg").attr("numIid"); 
//			window.location.href = "http://localhost:9000/Diag/itemPvUv?numIid=" + numIid;
		},
		getContainer: function() {
			return Welcome.container;
		}
	}, Welcome.init);
	
})(jQuery,window));