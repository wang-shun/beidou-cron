<?PHP

	/**
		和网络有关的一些工具, by shujip
	*/
	class NetUtil{
		/**
		*　把数据文件上传到服务器上
		*　@params $newFolder  不为空表示要建目录
		*  @exception if any error occurs
		*/
		public function ftpPutData($strRemoteServer,$strRemotePort,
									$strRemoteUser,$strRemotePasswd,$arrFiles,$newFolder=""){


			if(empty($strRemoteServer) || empty($strRemotePort) ||
				empty($strRemoteUser) || empty($strRemotePasswd) || !is_array($arrFiles)){
				throw new Exception('上传文件时参数不对');
				return false;
			}

			$connect_ftp = 0;//链接ftp标示
			for ($times = 0; $times < 3; $times++) {
				if ($ftpd = ftp_connect($strRemoteServer)) {
					$times = 3;
					$connect_ftp = 1;
					if (@ftp_login($ftpd, $strRemoteUser, $strRemotePasswd)) {
						if(!empty($newFolder)){
							@ftp_mkdir($ftpd,$newFolder);

						}
						foreach($arrFiles as $key => $val){
							if(is_array($val))
							{
								$strRemoteFile=$val["remote"];
								$strLocalFile=$val["local"];
								
								if(empty($strRemoteFile) || empty($strLocalFile)){
										continue;
								}

								if(!ftp_put($ftpd, $strRemoteFile, $strLocalFile, FTP_BINARY)) {
										// 上传文件失败
										throw new Exception("上传文件到指定服务器时，传输失败：".$strRemoteFile);
								}
							}else{
										throw new Exception("文件参数错误！");
							}
						}
					} else {
			//			echo $strRemoteUser."<br>".$strRemotePasswd;
						// ftp用户名/密码错误
			//			echo "EEE";

			//			die();
						throw new Exception("上传文件到指定服务器时，ftp用户名/密码有错误");
					}
					ftp_quit($ftpd);
				}
			}
			if ($connect_ftp == 0) {
				// ftp连接失败

				throw new Exception("上传文件到指定服务器时，ftp连接失败，共重试 $n 次");
			}
			return true;
		}


					   public static function get_client_ip()   
                       { 
                                   global  $_SERVER; 
                                   if  (isset($_SERVER["HTTP_X_FORWARDED_FOR"]))   
                                   { 
                                               $realip  =  $_SERVER["HTTP_X_FORWARDED_FOR"]; 
                                   } 
                                   elseif  (isset($_SERVER["HTTP_CLIENT_IP"]))   
                                   { 
                                               $realip  =  $_SERVER["HTTP_CLIENT_IP"]; 
                                   } 
                                   else   
                                   { 
                                               $realip  =  $_SERVER["REMOTE_ADDR"]; 
                                   } 
                                   return  $realip;             
                       } 

	}


?>