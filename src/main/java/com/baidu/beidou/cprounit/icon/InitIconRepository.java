package com.baidu.beidou.cprounit.icon;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.icon.bo.TempSystemIcon;
import com.baidu.beidou.cprounit.icon.service.IconRepositoryService;
import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.util.MD5;

/**
 * 图标库的初始化构建
 * @author tiejing
 *
 */
public class InitIconRepository {

	private static final Log LOG = LogFactory.getLog(InitIconRepository.class);
	/**系统图标库的图标文件夹路径*/
	private static String  sysIconSrcPath="";
    /**系统图标库各图标对应的图标信息文件路径*/
	private static String sysIconInitInfos="";
	/**图标信息的分隔符*/
	private static String split = ",";
	
	
	public static void main(String[] args){
		  if(args.length < 2){
				System.err.println("the main need 2 parameters, param1 is system icons image path"
						           +", param2 is system icons infomation path ");
				System.exit(-1);  
		  }
		  
		  LOG.debug("system icon image path is :"+args[0]); 
		  LOG.debug("system icon infomation file name is:"+args[1]);
		  
		  
		  /**
		   *图标字节流，临时保存 
		   * 与下面的 tempIconInfoList的内容一一对应的
		   */
		  List<byte[]> tempIconByteList = new ArrayList<byte[]>();
			
		 /**图标字节流对应的图标对应的图标信息，临时保存*/
		  List<TempSystemIcon> tempIconInfoList = new ArrayList<TempSystemIcon>();
		  
		  String[] paths = new String[] { "applicationContext.xml"};
		
		  ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		  UbmcService ubmcService = (UbmcService) ctx.getBean("ubmcService");;
		  IconRepositoryService iconReposService = (IconRepositoryService) ctx.getBean("iconRepositoryService");
		
		  sysIconSrcPath = args[0];
		  sysIconInitInfos = args[1];
		  
		  //从PM给的系统图标文件中读取图标信息
	      BufferedReader reader = null;
	      int line = 1;
	      
	      try {
	    	    reader = new BufferedReader(new InputStreamReader(new FileInputStream(sysIconInitInfos),"UTF-8"));
	            String tempString = null;
	          
	            tempString = reader.readLine();
	            
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	                 // 显示行号
                     //System.out.println("line " + line + ": " + tempString);
		             line++;
		             
		             if("".equals(tempString)){
		            		continue;
		             }
		             //读取一行图标信息 
		             String[] strs = tempString.split(split);
		             
		             if(strs.length < 5){
		            	 LOG.error("the giving icon information is not validate for format, error occur at line:["+line+"]");
                  	     System.exit(-1);
		             }
		            	 
		             
		             String iconName = strs[0];
		             String firstTrade = strs[1];
		             String secondTrade = strs[2];
		             String purpose = strs[3];
		             String tags = strs[4];
		             //System.out.println(iconName+" "+firstTrade+" "+secondTrade+" "+purpose+" "+tags);
                     /////////////////////////////////////////////////////
		             
		             //要求图标名称,一级行业及推广目的不能为空
		             if(null == iconName || "".equals(iconName.trim())){
		            	 LOG.error("icon Name should not null, error occur at line:["+line+"]");
                    	 System.exit(-1);
		             }
		            
		             if(null == firstTrade || "".equals(firstTrade.trim())){
		            	 LOG.error("icon firstTrade should not null, error occur at line:["+line+
		            			   ",iconName:"+iconName+".jpg, firsTrade:"+firstTrade+"]");
                    	 System.exit(-1);
		             }
		             
		             if(null == purpose || "".equals(purpose.trim())){
		            	 LOG.error("icon purpose should not null, error occur at line:["+line+
		            			   ", iconName:"+iconName+".jpg, firstTrade:"+firstTrade+", purpose:"+purpose+"]");
                    	 System.exit(-1);
		             }
		             
		             
                     File iconFile = new File(sysIconSrcPath+"/"+iconName.trim()+".jpg");
                     File iconFile2 = new File(sysIconSrcPath+"/"+iconName.trim()+".JPG");
                     File iconFile3 = new File(sysIconSrcPath+"/"+iconName.trim()+".JPEG");
                    
                     if(!iconFile.exists()&& !iconFile2.exists()&& !iconFile3.exists()){
                    	 LOG.error("icon File is not exist, iconId:["+iconName.trim()+".jpg ]");
                    	 System.exit(-1);
                     }

                     BufferedImage image  = null;
                     byte[] data = null;
                     
                     if(iconFile.exists()){
                    	 image = ImageIO.read(new FileInputStream(iconFile));
                    	 data = IOUtils.toByteArray(new FileInputStream(iconFile));
                     }else if(iconFile2.exists()){
                    	 image = ImageIO.read(new FileInputStream(iconFile2));
                    	 data = IOUtils.toByteArray(new FileInputStream(iconFile2));
                     }else if(iconFile3.exists()){
                    	 image = ImageIO.read(new FileInputStream(iconFile3));
                    	 data = IOUtils.toByteArray(new FileInputStream(iconFile3));
                     }
                     
     				 int hight = image.getHeight(null);
     				 int width = image.getWidth(null);
     				 
     				 //宽度和高度不合法
     				 if(hight != CproUnitConstant.LITERAL_WITH_ICON_DEFAULT_HEIGHT
     						 ||
     					width != CproUnitConstant.LITERAL_WITH_ICON_DEFAULT_WIDTH ){
     					 LOG.error("icon hight or width  is error, [iconName:"+iconName+".jpg,hight:"+hight+",width:"+width+"]");
     					 System.exit(-1);
     				 }
     				 
     				 //图标大于55K
     				 if(data.length > CproUnitConstant.LITERAL_WITH_ICON_DEFAULT_SIZE ){
     					 LOG.error("icon size is error, [iconName:"+iconName+".jpg,size:"+data.length+"]");
     					 System.exit(-1);
     				 }
     				 
     				 TempSystemIcon t_icon =new TempSystemIcon();
 		   	         t_icon.setHight(hight);
 		             t_icon.setWidth(width);
                      
 		             t_icon.setFirstTrade(firstTrade);
 		             t_icon.setSecondTrade(secondTrade);
 		             t_icon.setPurpose(purpose);
 		             t_icon.setTags(tags);
 		             
 		             //保存图标字节流信息
     				 tempIconByteList.add(data);
     				 //保存图标的属性信息
     				 tempIconInfoList.add(t_icon);
     				 
	          }
	          //读取文件结束
	          reader.close();
	      } catch (Exception e) {
	    	  System.out.println("exception in [ line: "+line+" ]");
	    	  System.out.println(e.toString());
	    	  System.exit(-1);
	      }
	    
		    //PM给的图标个数和图标信息个数不一致
		    if(tempIconByteList.size() != tempIconInfoList.size() ){
		    	 LOG.error("the given system icon image numbers is not equal with the given system icon info numbers"
		    			 + ",[image number is:"+tempIconByteList.size()+ ",info number is:"+tempIconInfoList.size()+" ]");
		    	 System.exit(-1);
		    }
		    
			// 调用ubmcService服务，将图片信息插入ubmc物料库中
			List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
			for(int i=0; i<tempIconInfoList.size();i++){
		    	byte[] data = tempIconByteList.get(i);
		    	String fileSrcMd5 = MD5.getMd5(data);
		    	TempSystemIcon tsi = tempIconInfoList.get(i);
				RequestBaseMaterial request = new RequestIconMaterial(null, null, tsi.getWidth(), 
						tsi.getHight(), data, fileSrcMd5);
				requests.add(request);
			}
			List<ResponseBaseMaterial> result = ubmcService.insert(requests);
			
			if(CollectionUtils.isEmpty(result)){
				 LOG.error("ubmc insert material fail,icon size is error");
				 System.exit(-1);
			}
			
			if(result.size()< 1){
				 LOG.error("ubmc insert material fail,icon size is error");
				 System.exit(-1);
			}
			
			//拷贝 wid,fileSrc 信息
			for(int i=0;i<result.size();i++){
				ResponseBaseMaterial material = result.get(i);
				TempSystemIcon tsi =tempIconInfoList.get(i);
				tsi.setMcId(material.getMcId());
			}
		
	//	    for(int i =0 ;i<beanpart.size();i++){
	//	    	List<RequestIconMaterial> bpart  = new ArrayList<RequestIconMaterial>();
	//	    	bpart.add(beanpart.get(i));
	//	    	List<ResponseIconMaterial> bresult = service.insertBatch(bpart);
	//	    	
	//	    	if(CollectionUtils.isEmpty(bresult)){
	//				 LOG.error("DRMC insert material fail,icon size is error");
	//				 System.exit(-1);
	//			}
	//			
	//			if(bresult.size()< 1){
	//				 LOG.error("DRMC insert material fail,icon size is error");
	//			       	return;
	//			}
	//			//拷贝 wid ，fileSrc 信息
	//			ResponseIconMaterial material = null;
	//			material = bresult.get(0);
	//			TempSystemIcon tsi =tempIconInfoList.get(i);
	//			tsi.setWid(material.getMcid());
	//			tsi.setFileSrc(material.getFileSrc());
	//	    }
	    
	    
		    //将系统图标物料信息插入数据库中
		    try {
				iconReposService.setSystemIconRepository(tempIconInfoList);
			} catch (Exception e) {
				  LOG.error(e.getMessage());
		    	  System.exit(-1);
			}
	    
	}
			
}
	


