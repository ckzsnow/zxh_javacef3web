package com.ynr.dao.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ynr.dao.IUserDao;

@Repository("userDao")
public class UserDaoImpl implements IUserDao {
	
	private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public Map<String, Object> getUserByUsername(String username) {
		Map<String, Object> userMap = null;
		String sql = "select * from user where user_email=?";
		try{
			userMap = jdbcTemplate.queryForMap(sql, username);
		}catch(Exception e) {
			logger.error("getUserByUsername error : {}", e.toString());
		}
		return userMap;
	}	
}
