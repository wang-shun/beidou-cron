/**
 * ??2009 Baidu
 */
package com.baidu.beidou.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPMessageCollector;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * @author zhuqian
 *
 */
public class FileUtils{
	
	Log logger = LogFactory.getLog(this.getClass());
	
	private String baseDir="";

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public boolean removeFile(String file) {
		String fileDir=baseDir+file;
		return new File(fileDir).delete();
	}
	
	public boolean saveFile(String file, String content){
		
		String path=baseDir+file;
		try{
			File f = new File(path);
			if(!f.exists()){
				f.createNewFile();
			}
			
			BufferedWriter output = new BufferedWriter(new FileWriter(f));
		    output.write(content);
		    output.close();
			
			return true;
		}catch(Exception e){
			logger.error("write file fail "+path+" "+e.getMessage());
			return false;
		}
	}
	
	public boolean appendContent(String file, String content){
		String path=baseDir+file;
		
		try{
			if(!new File(path).exists()){
				return false;
			}

			FileWriter fstream = new FileWriter(path,true);
			BufferedWriter out = new BufferedWriter(fstream);
		    out.write(content);
		    //Close the output stream
		    out.close();
			return true;
		}catch(Exception e){
			logger.error("append file fail "+path+" "+e.getMessage());
			return false;
		}
	}
	
	public List<String> readFileLines(String file){
		String path=baseDir+file;	
		List<String> lines = new ArrayList<String>();
		
		try{
			if(!new File(path).exists()){
				return lines;
			}
			
			FileReader myFileReader=new FileReader(path);
			BufferedReader myBufferedReader=new BufferedReader(myFileReader);
			String line;
	
			while((line=myBufferedReader.readLine())!=null)
			{
				lines.add(line);
			} 

		}catch(Exception e){
			logger.error("read file lines fail "+path+" "+e.getMessage());
		}
			
		return lines;
	}
	
	
	/**
	 * 检查文件，如果不存在，创建
	 * @return
	 */
	public boolean checkFileExist(String path, String file){
		path=baseDir+path;
		file=path+"/"+file;
		try{
			File p=new File(path);
			File f=new File(file);
			if(!p.exists()){
				p.mkdirs();
			}
			if(!f.exists()){
				return f.createNewFile();
			}
			return true;
		}catch(Exception e){
			logger.error("create file fail "+path+" "+file+" "+e.getMessage());	
			return false;
		}
		
	}

	public boolean uploadFile(String server, int port, String user, String passwd, String path, String file, String local){
		FTPClient ftp = new FTPClient(); 
//      FileOutputStream fos = null; 

      boolean success=true;
      try { 
          // set up client
          ftp.setRemoteHost(server);
          ftp.setRemotePort(port);
          ftp.setControlEncoding("GB2312"); //设置可以访问中文路径
          FTPMessageCollector listener = new FTPMessageCollector();
          ftp.setMessageListener(listener);            
          // connect
          ftp.connect();
           // login
          ftp.login(user, passwd);
          // set up passive BINARY transfers 设置ftp传输模式的
          ftp.setConnectMode(FTPConnectMode.PASV); 
          ftp.setType(FTPTransferType.BINARY); 
          
          try { 
        	  ftp.chdir(path); 
          } catch (Exception e) { 
        	  logger.info("mkdir:" + path); 
        	  ftp.mkdir(path);
          }
          
          ftp.put(baseDir+local, file);
          
      } catch (Exception e) {
          logger.error("ftp error put file "+file+" "+e.getMessage());
          success=false;
      } finally {
//      	IOUtils.closeQuietly(fos);
          try { 
          	ftp.quit();
          } catch (Exception e) { 
              logger.error("close ftp error put file "+file+" "+e.getMessage());
          } 
      }
      return success;
		
	}
	
	public boolean downloadFileCheckMd5(String server, int port, String user, String passwd, String file, String md5File, String local, String md5Local){
		FTPClient ftp = new FTPClient(); 
//      FileOutputStream fos = null; 

      boolean success=true;
      try { 
          // set up client
          ftp.setRemoteHost(server);
          ftp.setRemotePort(port);
          ftp.setControlEncoding("GB2312"); //设置可以访问中文路径
          FTPMessageCollector listener = new FTPMessageCollector();
          ftp.setMessageListener(listener);            
          // connect
          ftp.connect();
           // login
          ftp.login(user, passwd);
          // set up passive BINARY transfers 设置ftp传输模式的
          ftp.setConnectMode(FTPConnectMode.PASV); 
          ftp.setType(FTPTransferType.BINARY); 
          
          ftp.get(baseDir+local, file);
          ftp.get(baseDir+md5Local, md5File);
          
          success=this.checkFileMd5(local, md5Local);

      } catch (Exception e) { 
          logger.error("ftp error get file "+file+" "+e.getMessage());
          success=false;
      } finally {
//      	IOUtils.closeQuietly(fos);
          try { 
          	ftp.quit();
          } catch (Exception e) { 
              logger.error("close ftp error get file "+file+" "+e.getMessage());
          }
      }
      return success;
	}
	
	
	public boolean downloadFile(String server, int port, String user, String passwd, String file, String local){
		FTPClient ftp = new FTPClient(); 
//        FileOutputStream fos = null; 

        boolean success=true;
        try { 
            // set up client
            ftp.setRemoteHost(server);
            ftp.setRemotePort(port);
            ftp.setControlEncoding("GB2312"); //设置可以访问中文路径
            FTPMessageCollector listener = new FTPMessageCollector();
            ftp.setMessageListener(listener);            
            // connect
            ftp.connect();
             // login
            ftp.login(user, passwd);
            // set up passive BINARY transfers 设置ftp传输模式的
            ftp.setConnectMode(FTPConnectMode.PASV); 
            ftp.setType(FTPTransferType.BINARY); 
            
            ftp.get(baseDir+local, file);
        /*
            ftpClient.connect(server); 
            ftpClient.login(user, passwd); 

            String remoteFileName = file; 
            fos = new FileOutputStream(); 

            ftpClient.setBufferSize(1024);
            //设置文件类型（二进制） 
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            success=ftpClient.retrieveFile(remoteFileName, fos); 
            ftp.get(filename + ".rm" , "01.rm"); 
            
            fos.close();*/
        } catch (Exception e) { 
            logger.error("ftp error get file "+file+" "+e.getMessage());
            success=false;
        } finally {
//        	IOUtils.closeQuietly(fos);
            try { 
            	ftp.quit();
            } catch (Exception e) { 
                logger.error("close ftp error get file "+file+" "+e.getMessage());
            } 
        }
        return success;
	}
	
    public static String getHash(String fileName, String hashType) throws  
    Exception {  
		InputStream fis;  
		fis = new FileInputStream(fileName);  
		byte[] buffer = new byte[1024];  
		MessageDigest md5 = MessageDigest.getInstance(hashType);  
		int numRead = 0;  
		while ((numRead = fis.read(buffer)) > 0) {  
		    md5.update(buffer, 0, numRead);  
		}  
		fis.close();  
		return toHexString(md5.digest());  
	}  
    
 

    public static char[] hexChar = {'0', '1', '2', '3',  
        '4', '5', '6', '7',  
        '8', '9', 'a', 'b',  
        'c', 'd', 'e', 'f'};  
    
	public static String toHexString(byte[] b) {  
		StringBuilder sb = new StringBuilder(b.length * 2);  
		for (int i = 0; i < b.length; i++) {  
		    sb.append(hexChar[(b[i] & 0xf0) >>> 4]);  
		    sb.append(hexChar[b[i] & 0x0f]);  
		}  
		return sb.toString();  
	}
	
	public String makeFileMd5(String file){
		String path=baseDir+file;
		try{
			return FileUtils.getHash(path, "MD5");
		}catch(Exception e){
			logger.error("get file md5 fail "+path);
			return null;
		}
	}

/*
	public String makeFileMd5(String file){
		String path=baseDir+file;
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			InputStream is = new FileInputStream(path);
			try {
			  is = new DigestInputStream(is, md);
			  // read stream to EOF as normal...
			}
			finally {
			  is.close();
			}
			byte[] digest = md.digest();
			int j = digest.length; 
			char str[] = new char[j * 2]; 
			int k = 0; 
			for (int i = 0; i < j; i++) 
			{ 
			byte byte0 = digest[i]; 
			str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
			str[k++] = hexDigits[byte0 & 0xf]; 
			} 
			return String.valueOf(digest);
			
		}catch(Exception e){
			logger.error("make md5 fail "+path);
			return null;
		}
	}*/
	
	public boolean buildMd5File(String fullName, String fileName,String md5FileName){
		String md5=this.makeFileMd5(fullName);
		return this.saveFile(md5FileName, md5+"  "+fileName);
	}
	
	public boolean checkFileMd5(String file, String md5File){
		String md5=this.makeFileMd5(file);
		
		List<String> lines=this.readFileLines(md5File);
		
		if(lines.size()>0){
			String strMd5=lines.get(0).split("  ")[0];
			return md5.equals(strMd5);
		}
		
		return false;
	}
	
	public int getFileSize(String file){
		String path=baseDir+file;
		 FileInputStream fis = null;
         try{
             fis = new FileInputStream(path);  
             return fis.available();
         }catch(Exception e){
        	 logger.error("get file size fail: "+path+e.getMessage());
        	 return 0;
         }
	}
	/*
    public static void testUpload() {   
        FTPClient ftpClient = new FTPClient();   
        FileInputStream fis = null;   
  
        try {   
            ftpClient.connect("172.20.82.227");   
            ftpClient.login("oracle", "oracle");   
            File srcFile = new File("E:/apache+tomcat.zip");   
            fis = new FileInputStream(srcFile);   
            //设置上传目录   
            ftpClient.changeWorkingDirectory("/home/oracle");   
            ftpClient.setBufferSize(1024);   
            ftpClient.setControlEncoding("GBK");   
            //设置文件类型（二进制）   
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);   
            ftpClient.storeFile("apache+tomcat.zip", fis);   
            System.out.println("成功！");  
        } catch (Exception e) {   
            e.printStackTrace();   
            throw new RuntimeException("FTP客户端出错！", e);   
        } finally {   
            IOUtils.closeQuietly(fis);   
            try {   
                ftpClient.disconnect();   
            } catch (Exception e) {   
                e.printStackTrace();   
                throw new RuntimeException("关闭FTP连接发生异常！", e);   
            }   
        }   
    }   */
}
