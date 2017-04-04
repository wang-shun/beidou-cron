<?php	
	require_once "include.php";
	require_once "net_util.php";	
	
	$util=new NetUtil;
/*
	$ip=$util->get_client_ip();
	
	if(!in_array($ip, $ALLOW_IPS)){
		log_info("SNAP-REQUEST forbiden ".$ip);
		echo "forbiden for ip ".$ip;
		die();
	}
*/	
	$src=$_GET["src"];
	
	if(!empty($src)){

		if(preg_match("/^http:\/\/cpro\.baidu\.com\/cpro\/ui\/uijs.php\?(.*)$/", $src, $matches)){
			$params=$matches[1];
			$arr=explode("&", $params);
			$map=array();
			foreach($arr as $one){
				$r=explode("=", $one);
				$map[$r[0]]=$r[1];
			}
		//	print_r($map);
			$url=urldecode($map["word"]);
			$tn=$map["tn"];
					$curFile=$BASE_DIR."\\api\\tmp\\cur.data";
					if(!file_exists($curFile)){
						header("location: ".$src);
						die();
					}
					$line=file_get_contents($curFile);
				    $arrLine=explode("\t", $line);

					if($arrLine[2]==$tn){//与当前任务相符					
						$orderid=$arrLine[3];			
					}
		
				if(!empty($orderid)){
				
					$orderFile=$BASE_DIR."\\ads\\".$orderid.".ads";
					
/*					if(array_key_exists($url, $_SESSION)){
						header("location: ".$src);
						die();
					}*/

					if(file_exists($orderFile)){
						$f=fopen($orderFile, "r");
						$content=fread($f, filesize($orderFile));
						fclose($f);
						file_put_contents($curFile, $line."\t1");
						echo $content;
					}else{
						header("location: ".$src);
					}
		//			$_SESSION[$url]=1;
					die();
				}else{
					header("location: ".$src);
					die();
				}
			}
		echo $src."blocked";
	}
?>