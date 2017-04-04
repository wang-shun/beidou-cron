<?PHP

	/**
		�������йص�һЩ����, by shujip
	*/
	class NetUtil{
		/**
		*���������ļ��ϴ�����������
		*��@params $newFolder  ��Ϊ�ձ�ʾҪ��Ŀ¼
		*  @exception if any error occurs
		*/
		public function ftpPutData($strRemoteServer,$strRemotePort,
									$strRemoteUser,$strRemotePasswd,$arrFiles,$newFolder=""){


			if(empty($strRemoteServer) || empty($strRemotePort) ||
				empty($strRemoteUser) || empty($strRemotePasswd) || !is_array($arrFiles)){
				throw new Exception('�ϴ��ļ�ʱ��������');
				return false;
			}

			$connect_ftp = 0;//����ftp��ʾ
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
										// �ϴ��ļ�ʧ��
										throw new Exception("�ϴ��ļ���ָ��������ʱ������ʧ�ܣ�".$strRemoteFile);
								}
							}else{
										throw new Exception("�ļ���������");
							}
						}
					} else {
			//			echo $strRemoteUser."<br>".$strRemotePasswd;
						// ftp�û���/�������
			//			echo "EEE";

			//			die();
						throw new Exception("�ϴ��ļ���ָ��������ʱ��ftp�û���/�����д���");
					}
					ftp_quit($ftpd);
				}
			}
			if ($connect_ftp == 0) {
				// ftp����ʧ��

				throw new Exception("�ϴ��ļ���ָ��������ʱ��ftp����ʧ�ܣ������� $n ��");
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