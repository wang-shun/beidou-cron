<?PHP

$LOGDIR=$BASE_DIR."\\logs\\".date("Ymd");

function log_info($msg){
	global $LOGDIR;
	
	
	if(!file_exists($LOGDIR)){
		mkdir($LOGDIR);
	}
	$filename=$LOGDIR."\\snap.log";

	if(!file_exists($filename)){
		$handle=fopen($filename, 'w');
		fclose($handle);
	}
       
	   $handle = fopen($filename, 'a');
	   fwrite($handle, date("Ymd H:i:s")." ".$msg."\n");
	   fclose($handle);
}

?>