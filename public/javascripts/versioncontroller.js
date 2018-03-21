$("#dialog").dialog({
    closeText: "关闭",
    autoOpen: false,
    width: 930,
    resizable: false,
    title:"温馨提示栏",
    modal: true,
    buttons: [
              {
            	  text: "关闭",
    	      	click: function() {
    	    	  $(this).dialog( "close" );
    	      		}
    	      	}
    	      ]
});

    	