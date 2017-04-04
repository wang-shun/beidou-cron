<?php

    require_once "include.php";
    require_once "log_util.php";
    
    if(array_key_exists("url", $_POST) && array_key_exists("adid", $_POST)){
	
        try{
			$url=$_POST["url"];
			$adid=$_POST["adid"];
		
			$snapshotFile=$BASE_DIR."/".$adid.".jpg";
			
			$cmd="cd /home/work/google-snapshot/bin && sh google_adx_snapshot_server.sh ".$url." ".$snapshotFile." >> ".$LOGDIR."/snapshot.bin.log";
			system($cmd);
			
			if(file_exists($snapshotFile)){
				$snapshot_data = file_get_contents($snapshotFile);
				print_r($snapshot_data);
				
				// É¾³ý½ØÍ¼ÎÄ¼þ
				unlink($snapshotFile);
			}
			
            log_info("google snapshot success,url=".$url." adid=".$adid);
            die();
            
        }catch(Exception $e){
            log_info("google snapshot failed,url=".$url." adid=".$adid." ".print_r($e, true));
			
            print_r("");
            if(file_exists($snapshotFile)){
                unlink($snapshotFile);
            }            
            die();
        }
      
    }else{
?>
    <title>google snapshot</title>
    <form action="" method="post">
        URL to capture: <input name="url" maxlength="100px" value="<?php echo $_POST["url"]; ?>">
		Adid£º <input name="adid" maxlength="100px" value="<?php echo $_POST["adid"]; ?>">
        <input type="submit" value="submit and wait">
    </form>

<?PHP
    }
?>
