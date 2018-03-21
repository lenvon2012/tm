var DS = DS || {};
DS.data = DS.data || {};
((function ($, window) {
	function getdata(){
		$.ajax({
	
		url:"/Diag/shop",
		success:function(data){
			$(".data").html(data);
		}
		});
	}
})(jQuery, window));