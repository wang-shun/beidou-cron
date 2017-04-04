<?php

	require_once "include.php";
	require_once "net_util.php";
	require_once "log_util.php";
	
	$util=new NetUtil;
/*
	$ip=$util->get_client_ip();
	
	
	if(!in_array($ip, $ALLOW_IPS)){
		log_info("SNAP-REQUEST forbiden ".$ip);
		echo "FORBIDON";
		die();
	}
	*/
/*	
	$file='E:\beidou\windows\images\20090821\20090821094429.png';
	$fileDir='20090821';
	$fileName='abc2';
	
			$util=new NetUtil;
			
			$util->ftpPutData($IMAGE_SERVER["host"],
								21,
								$IMAGE_SERVER["user"],
								$IMAGE_SERVER["passwd"],
								array(
								array("local"=>$file, "remote"=>$IMAGE_SERVER["path"]."/".$fileDir."/".$fileName),
								),
								$IMAGE_SERVER["path"]."/".$fileDir);

								die();
								*/
	if(array_key_exists("url", $_POST) 
		&& array_key_exists("ad", $_POST)
		&& array_key_exists("tn", $_POST)){
		
		$url=$_POST["url"];
		$ad=$_POST["ad"];
		$tn=$_POST["tn"];
		
		log_info("SNAP-REQUEST url=".$url." orderid=".$ad." tn=".$tn);
		
		$curFile=$BASE_DIR."\\api\\tmp\\cur.data";
		
		try{
			
			if(file_exists($curFile)){
				//如果文件时间超时1分钟，也进行删除
				$line=file_get_contents($curFile);
				
				$arrLine=explode("\t", $line);
				
				if(time()-$arrLine[0]<$REPEAT_TIMEOUT){				
					log_info("SNAP-REQUEST CONFLICT url=".$url." orderid=".$ad." tn=".$tn);
					echo "CONFLICT";
					die();				
				}
			}
		
		//建立正在处理的标示文件
			
			file_put_contents($curFile, time()."\t".$url."\t".$tn."\t".$ad);


		//	$line = "http://bbs.55bbs.com/thread-2693085-1-1.html";
		//	$line=str_replace("&", "^&", $line);
		//	$line=str_replace("=", "^=", $line);
			if(!file_exists($BASE_DIR."\\images\\".date("Ymd"))){
				system("mkdir ".$BASE_DIR."\\images\\".date("Ymd"));
			}
			
			$fileDir=date("Ymd");
			$fileName=date("YmdHis").".png";
			
			$file=$BASE_DIR."\\images\\".$fileDir."\\".$fileName;
			
			$cmd="cd ".$BASE_DIR."\\script && snap_shot.bat \"".$url."\" \"".$file."\" \"".$LOGDIR."\\snap_shot.exe.log\"";
			
			system($cmd);
			
			//推到linux机上去			
			
			if(file_exists($curFile)){
				$line=file_get_contents($curFile);
				$arrLine=explode("\t", $line);
				if(count($arrLine)>=5 && $arrLine[4]==1){
					log_info("SNAP-SUCCESS gen file url=".$url." orderid=".$ad." tn=".$tn." ".$fileDir."/".$fileName);	
					
					$util->ftpPutData($IMAGE_SERVER["host"],
								21,
								$IMAGE_SERVER["user"],
								$IMAGE_SERVER["passwd"],
								array(
								array("local"=>$file, "remote"=>$IMAGE_SERVER["path"]."/".$fileDir."/".$fileName),
								),
								$IMAGE_SERVER["path"]."/".$fileDir);

					echo $fileDir."/".$fileName;
					unlink($curFile);
					die();
				}
				unlink($curFile);
			}
			
			log_info("SNAP-NOT-FOUND url=".$url." orderid=".$ad." tn=".$tn);
			echo "NO_AD";
			die();
			
		}catch(Exception $e){
			log_info("SNAP-EXCEPTION url=".$url." orderid=".$ad." tn=".$tn." ".print_r($e, true));
			print_r($e);
			if(file_exists($curFile)){
				unlink($curFile);
			}			
			die();
		}
	//	print_r($arr);
		//system("runBeidou.bat http://www.5ccc.net/");
	}else{
?>
	<title>Capture</title>
	<form action="" method="post">
		URL to capture: <input name="url" maxlength="100px" value="<?php echo $_POST["url"]; ?>">
		Ad： <input name="ad" maxlength="100px" value="<?php echo $_POST["ad"]; ?>">
		Tn:  <input name="tn" maxlength="100px" value="<?php echo $_POST["tn"]; ?>">
		<input type="submit" value="submit and wait">
	</form>

<?PHP
	
	}
?>