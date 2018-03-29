package com.ynr.dao;

import java.util.Map;

public interface IUserDao {

	public Map<String, Object> getUserByUsername(String username);
	
}
