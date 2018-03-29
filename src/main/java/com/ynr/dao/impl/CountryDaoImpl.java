package com.ynr.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ynr.dao.ICountryDao;

@Repository("countryDao")
public class CountryDaoImpl implements ICountryDao {
	
	private static final Logger logger = LoggerFactory.getLogger(CountryDaoImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<Map<String, Object>> getAllCountry() {
		List<Map<String, Object>> countryList = new ArrayList<>();
		String sql = "select * from country";
		try{
			countryList = jdbcTemplate.queryForList(sql);
		}catch(Exception e) {
			logger.error("getAllCountry error : {}", e.toString());
		}
		return countryList;
	}	
}
