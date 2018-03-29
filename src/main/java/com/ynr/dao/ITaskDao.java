package com.ynr.dao;

import java.util.List;
import java.util.Map;

public interface ITaskDao {

	public List<Map<String, Object>> getAllApplyRecord(String userId, int offset, int limit);
	
	public long addApplyRecord(String userId, String name, int countryId, String excelDataFileName, String excelAlias);
	
	public boolean delApplyRecord(String ids);
	
	public boolean updateApplyRecord(long id, String status);
	
}
