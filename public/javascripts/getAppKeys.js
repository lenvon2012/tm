(function(){
    ISV = {};
    ISV.config = {
        'atpanel':'http://toptrace.taobao.com/assets/atpanel.js',
        'sign':'1.0.0.0',//无需�?�
        'appkey':'21255586'//应用Appkey
    };
    ISV.init = function(){
        var self = this, doc = document, config = self.config, url = config.atpanel, head = doc.getElementsByTagName('head')[0], script = doc.createElement('script');
        script.setAttribute('src',url); head.appendChild(script);
    };
    ISV.getAppKey=function(){
        var topLog;
        var jsNotes = document.getElementsByTagName('script');
        if(jsNotes){
            for(var i = 0; i < jsNotes.length; i ++){
                var jsNote = jsNotes[i];
                var appkey = jsNote.getAttribute('topappkey');
                if(appkey){
//				alert(appkey);
                    this.config.appkey = appkey;
                    break;
                }
            }
        }
    }
    ISV.getAppKey();
    ISV.init();
})();