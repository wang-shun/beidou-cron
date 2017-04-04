package com.baidu.beidou.tool.dao;

import java.util.List;

import com.baidu.beidou.tool.bo.SnapShotFile;

public interface SnapShotFileDao{

	public List<SnapShotFile> getDeletedFile();
	
	public boolean updateDeleteTime(int id);

	/**
	 * 更新email发送状态
	 * @param id
	 * @return
	 */
	public boolean updateEmailStatus(int id);

	public boolean saveSnapFile(SnapShotFile file);
}
