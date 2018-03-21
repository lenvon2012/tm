/* Simplified Chinese translation for the jQuery Timepicker Addon /
/ Written by Will Lu */
((function($) {
	$.timepicker.regional['zh-CN'] = {
		timeOnlyTitle: '选择时间',
		timeText: '时间',
		hourText: '小时',
		minuteText: '分钟',
		secondText: '秒钟',
		millisecText: '微秒',
		microsecText: '微秒',
		timezoneText: '时区',
		currentText: '现在时间',
		closeText: '确定',
		timeFormat: 'hh:mm:ss',
		amNames: ['AM', 'A'],
		pmNames: ['PM', 'P'],
		isRTL: false
	};
	
	$.datepicker.regional['zh-CN'] = {
			closeText: '关闭',
			prevText: '>',
			nextText: '<',
			currentText: '现在时间',
			monthNames: ['一月','二月','三月','四月','五月','六月',
				         	'七月','八月','九月','十月','十一月','十二月'],
			monthNamesShort: ['一月','二月','三月','四月','五月','六月',
					         	'七月','八月','九月','十月','十一月','十二月'],
			dayNames: ['周日','周一','周二','周三','周四','周五','周六'],
			dayNamesShort: ['日','一','二','三','四','五','六'],
			dayNamesMin: ['日','一','二','三','四','五','六'],
			weekHeader: '周',
			dateFormat: 'yy-mm-dd',
			firstDay: 1,
			isRTL: false,
			showMonthAfterYear: false,
			yearSuffix: ''
		};
	$.timepicker.setDefaults($.timepicker.regional['zh-CN']);
	$.datepicker.setDefaults($.datepicker.regional['zh-CN']);
})(jQuery));
