var dataUrl="http://localhost:8000/windows/api/demodata.php?src=";

var tnObj={};

function replace_ad(docRoot, deep){

	if(deep>2){
		return;
	}
	
	var anchors = docRoot.getElementsByTagName('iframe');

	for(ifr in anchors){
		var myReg = /^http:\/\/cpro\.baidu\.com\/cpro\/ui\/uijs.php\?([^\?]*){1}$/gi; 
		if(myReg.test(anchors[ifr].src)){
			var tnPat=/&tn=([^&]+)&/gi;

			var matches = anchors[ifr].src.match(tnPat);
			
			if(matches!=null && matches.length>0){
				var tnVPat=/[^=&]+/gi;
				var matches2=matches[0].match(tnVPat);
				
				if(matches2!=null && matches2.length>1){
					if(tnObj[matches2[1]]==null){
						tnObj[matches2[1]]=1;
						anchors[ifr].src=dataUrl+escape(anchors[ifr].src);
					}
				}
			}
		}else{
			if(anchors[ifr].contentWindow){

				try  
				  {  
							if(anchors[ifr].contentWindow.document){
								replace_ad(anchors[ifr].contentWindow.document, deep+1);
							}
				  }  
				catch(err)  
				  {
			//			alert(err.description);
				  }  
			}
			
		}
		
		//replace_ad(ifr.contentWindow.document);
	}
}

replace_ad(document, 0);