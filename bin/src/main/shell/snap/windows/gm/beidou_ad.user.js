// ==UserScript==
// @name           beidou_ad
// @namespace      shujip
// ==/UserScript==
//alert(document);
//alert(document.getelementbtid);
var anchors = document.getElementsByTagName('iframe');

for(ifr in anchors){
//	var myReg = /^http:\/\/cpro\.baidu\.com\/cpro\/ui\/uijs.php\?.*&tn=([^&]+)&.*$/gi; 
	var myReg = /^http:\/\/cpro\.baidu\.com\/cpro\/ui\/uijs.php\?([^\?]*){1}$/gi; 
	if(myReg.test(anchors[ifr].src)){
		var tnPat=/&tn=([^&]+)&/gi;
		
		var matches = anchors[ifr].src.match(tnPat);
		
		if(matches.length>0){
			var tnVPat=/[^=&]+/gi;
			
			var matches2=matches[0].match(tnVPat);
			
			if(matches2.length>1){
				if(readCookie(matches2[1])==null){
					createCookie(matches2[1], 1);
					anchors[ifr].src="http://localhost:8000/windows/api/demodata.php?src="+escape(anchors[ifr].src);
				}
			}
		}
	}
}
/*
find();


function   find(){  
  var WshShell =new ActiveXObject("WScript.Shell");  
  WshShell.SendKeys("^f");  
}
*/

 function createCookie(name,value,days) {
     if (days) {
         var date = new Date();
         date.setTime(date.getTime()+(days*24*60*60*1000));
         var expires = "; expires="+date.toGMTString();
     }
     else var expires = "";
     document.cookie = name+"="+escape(value)+expires+"; path=/";
 }
  
 function readCookie(name) {
     var nameEQ = name + "=";
     var ca = document.cookie.split(';');
     for(var i=0;i < ca.length;i++) {
         var c = ca[i];
         while (c.charAt(0)==' ') c = c.substring(1,c.length);
         if (c.indexOf(nameEQ) == 0) return unescape(c.substring(nameEQ.length,c.length));
     }
     return null;
 }
  
 function eraseCookie(name) {
     createCookie(name,"",-1);
 }
  
 function showCookie(name) {
     alert(readCookie(name));
 }
  
  
 function addCookie(name,value,days) {
     if (readCookie(name) != null) {
         var oldvalue = readCookie(name);
         var newvalue = oldvalue+","+value;
     }
     else var newvalue = value;
     createCookie(name,newvalue,days);
 }
  
 function getHistory(name) {
     var sHistory = readCookie(name);
     if(sHistory) {
         var aHistroy = sHistory.split(",");
         for (x in aHistroy)
         {
             //do something ...
         }
     }
 }