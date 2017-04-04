package com.baidu.beidou.cprounit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.auditmanager.vo.DelMaterial;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.util.LogUtils;

/**
 * 从UBMC和本地DB中删除积压数量过多的无用创意数据
 * @author huangjinkun01
 * 
 */
public class DeleteOverFlowMater {

    private static final Log LOG = LogFactory.getLog(DeleteOverFlowMater.class);

    private static UbmcService ubmcService;
    private static UnitDao unitDao;

    // 初始化站点配置
    private static void contextInitialized() {
        String[] fn = new String[] { "applicationContext.xml",
                "classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);

        ubmcService = (UbmcService) ctx.getBean("ubmcService");
        unitDao = (UnitDao) ctx.getBean("unitDao");
    }

    public static void main(String[] args) {
        contextInitialized();

        // 读入文件mcId_mcVersionId_userid_deltime
        String filePath = args[0];
        LOG.info("filePath:" + filePath);

        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(new File(filePath)), 5 * 1024 * 1024); // 每次用5M的缓冲读取文本文件
            String line = null;
            List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
            List<DelMaterial> delMaterials = new ArrayList<DelMaterial>();
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(",");
                Long mcId = Long.parseLong(tmp[0]);
                int mcVersionId = Integer.parseInt(tmp[1]);
                int userId = Integer.parseInt(tmp[2]);
                RequestLite request = new RequestLite(mcId, mcVersionId);
                DelMaterial delMaterial = new DelMaterial();
                delMaterial.setMcId(mcId);
                delMaterial.setMcVersionId(mcVersionId);
                delMaterial.setUserId(userId);
                delMaterials.add(delMaterial);
                units.add(request);
            }
            
            br.close();

            ubmcService.remove(units);
            doAfterUbmc(delMaterials);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtils.fatal(LOG, e.getMessage(), e);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.fatal(LOG, e.getMessage(), e);
        }
    }

    // ubmc删除成功则删除待删除物料表记录，否则打印异常
    private static void doAfterUbmc(List<DelMaterial> delMaterials) {
        if (CollectionUtils.isEmpty(delMaterials)) {
            LOG.error("删除有误，说明某些物料删除失败");
            return;
        }

        for (int index = 0; index < delMaterials.size(); index++) {
            DelMaterial delMaterial = delMaterials.get(index);
            if (delMaterial != null && delMaterial.getMcId() > 0) {
                LOG.info("Delete delMaterial:" + delMaterial.toString());
                unitDao.deleteMater(delMaterial);
            } else {
                LOG.error("delMater[mcId=" + delMaterial.getMcId() 
                        + ", versionId=" + delMaterial.getMcVersionId() + "]删除失败");
            }
        }
    }
   
}
