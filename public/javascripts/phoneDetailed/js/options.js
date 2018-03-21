$(function(){
	var startDate = localStorage["start_date"];
	var endDate = localStorage["end_date"];

	if(startDate){
		$("input[name='start-date-text']").val(startDate);
	}
	if(endDate){
		$("input[name='end-date-text']").val(endDate);
	}


	$("#save").click(function(){
		var startDate = $("input[name='start-date-text']").val();
        var endDate = $("input[name='end-date-text']").val();

        localStorage["start_date"] = startDate;
        localStorage["end_date"] = endDate;

		alert("保存成功!");
	})
});