package com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.ubmc.cprounit.service.UbmcServiceFactory;
import com.baidu.beidou.accountmove.ubmc.cprounit.service.UbmcServiceImpl;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request.RequestGroup;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.material.response.ResponseGroup;

public class MaterialCopy {

	private static UbmcServiceImpl ubmcService = null;
    private static MaterialCopy instance = new MaterialCopy();

    public MaterialCopy() {
        MaterialCopy.instance = this;
        ubmcService = UbmcServiceFactory.getInstance();
    }

    public MaterialKey copyMaterial(MaterialKey mKey) {

        MaterialKey newKey = null;
        if ((newKey = CopyLog.getExistedKey(mKey.getMcId(), mKey.getMcVersion())) != null) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ":" + mKey + " already exists");
            return newKey;
        }

        // ubmc中拷贝创意，并生成最新的MaterialKey
        List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
        RequestBaseMaterial request = new RequestLite(mKey.getMcId(), mKey.getMcVersion());
        requests.add(request);
        List<ResponseBaseMaterial> newUnitList = ubmcService.copy(requests);
        if (CollectionUtils.isEmpty(newUnitList) || newUnitList.get(0) == null) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ":" + mKey + " copy to ubmc failed");
            return null;
        }
        newKey = new MaterialKey(mKey, newUnitList.get(0).getMcId(), newUnitList.get(0).getVersionId());
        
        return newKey;
    }

    public Map<MaterialKey, MaterialKey> copyMaterial(List<MaterialKey> mKeys) {

        Map<MaterialKey, MaterialKey> keyMap = new HashMap<MaterialKey, MaterialKey>();
        List<MaterialKey> notExistKeys = new ArrayList<MaterialKey>(mKeys.size());
        for (MaterialKey mKey : mKeys) {
            MaterialKey newKey = CopyLog.getExistedKey(mKey.getMcId(), mKey.getMcVersion());
            if (newKey == null) {
                notExistKeys.add(mKey);
            } else {
                keyMap.put(mKey, newKey);
            }
        }

        // ubmc中拷贝创意，并生成最新的MaterialKey
        List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
        for (MaterialKey mKey : notExistKeys) {
            RequestBaseMaterial request = new RequestLite(mKey.getMcId(), mKey.getMcVersion());
            requests.add(request);
        }
        List<ResponseBaseMaterial> newUnitList = ubmcService.copy(requests);
        if (CollectionUtils.isEmpty(newUnitList) || newUnitList.size() != requests.size()) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ": batch copy to ubmc failed");
            return keyMap;
        }
        
        ResponseBaseMaterial responseBaseMaterial = null;
        for (int index = 0; index < notExistKeys.size(); index++) {
            MaterialKey mKey = notExistKeys.get(index);
            responseBaseMaterial = newUnitList.get(0);
            if (responseBaseMaterial == null) {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                        + ":" + mKey + " batch copy to ubmc failed");
                continue;
            }
            MaterialKey newKey = new MaterialKey(mKey, newUnitList.get(0).getMcId(), newUnitList.get(0).getVersionId());
            keyMap.put(mKey, newKey);
        }
        
        return keyMap;
    }

    public MaterialKey copyGroupMaterialKey(MaterialKey mKey, int srcGroupId, int destGroupId) {
        
        // 推广组的是否和创意的数据在一起，目前的考虑是基于在一起的
        // 如果不在一起，则可以考虑将CopyLog.getExistedKey的校验逻辑删除
        MaterialKey newKey = null;
        if ((newKey = CopyLog.getExistedKey(mKey.getMcId(), mKey.getMcVersion())) != null) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ":" + mKey + " already exists");
            return newKey;
        }

        // ubmc中获取mcId，并生成最新的MaterialKey
        List<RequestBaseMaterial> getRequestList = new LinkedList<RequestBaseMaterial>();
        RequestBaseMaterial getRequest = new RequestLite(mKey.getMcId(), mKey.getMcVersion());
        getRequestList.add(getRequest);
        List<ResponseBaseMaterial> oldUnitList = ubmcService.get(getRequestList, false);
        if (CollectionUtils.isEmpty(oldUnitList) || oldUnitList.get(0) == null) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ":" + mKey + " get from ubmc failed");
            return null;
        }
        
        if (!(oldUnitList.get(0) instanceof ResponseGroup)) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ":" + mKey + " info from ubmc is invalid");
            return null;
        }
        
        ResponseGroup preRes = (ResponseGroup)oldUnitList.get(0);
        List<RequestBaseMaterial> insertRequestList = new LinkedList<RequestBaseMaterial>();
        RequestGroup insertRequest = new RequestGroup(0L, 1, destGroupId, preRes.getPhoneId(), preRes.getPhone(), 
                preRes.getMsgPhoneId(), preRes.getMsgPhone(), preRes.getMsgContent(), preRes.getSubUrlParam(),
                preRes.getSubUrlTitle(), preRes.getSubUrlLink());
        insertRequestList.add(insertRequest);
        List<ResponseBaseMaterial> newUnitList = ubmcService.insert(insertRequestList);
        if (CollectionUtils.isEmpty(newUnitList) || newUnitList.get(0) == null) {
            System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) 
                    + ":" + mKey + " insert into ubmc failed");
            return null;
        }
        newKey = new MaterialKey(mKey, newUnitList.get(0).getMcId(), newUnitList.get(0).getVersionId());
        
        return newKey;
    }

    public static void copy(MaterialKey key) {
    	
        MaterialKey newKey = instance.copyMaterial(key);

        if (newKey == null) {
            CopyLog.failCopy(key.getMcId(), key.getMcVersion(), key.getUserId(), key.getPlanId(), key.getGroupId(),
                    key.getUnitId());
        } else {
            CopyLog.succCopy(key.getMcId(), key.getMcVersion(), key.getUserId(), key.getPlanId(), key.getGroupId(),
                    key.getUnitId(), newKey.getMcId(), newKey.getMcVersion());
        }
    }
}
