package com.ynr.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ynr.dao.ITaskDao;

@Repository("applyRecordDao")
public class TaskDaoImpl implements ITaskDao {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskDaoImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<Map<String, Object>> getAllApplyRecord(String userId, int offset, int limit) {
		List<Map<String, Object>> applyRecordList = new ArrayList<>();
		String sql = "select task.*, CONCAT(country.country_name_chi, '(', country.country_name_eng, ')') as country, DATE_FORMAT(task.create_time, '%Y-%c-%d %h:%i') as readable_time from task left join country on task.country_id=country.id where task.user_id=? order by create_time desc limit ?,?";
		try{
			applyRecordList = jdbcTemplate.queryForList(sql, userId, offset, limit);
		}catch(Exception e) {
			logger.error("getAllApplyRecord error : {}", e.toString());
		}
		return applyRecordList;
	}

	@Override
	public long addApplyRecord(String userId, String name, int countryId, String excelDataFileName, String excelAlias) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try{
			jdbcTemplate.update(new PreparedStatementCreator() {  
		        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {  
		        	String sql = "insert into task(user_id, name, country_id, excel_data_file, status, excel_data_file_alias, create_time) values (?,?,?,?,?,?,?)";
		               PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);  
		               ps.setString(1, userId);  
		               ps.setString(2, name);  
		               ps.setLong(3, countryId);
		               ps.setString(4, excelDataFileName);
		               ps.setString(5, "任务已提交");
		               ps.setString(6, excelAlias);
		               ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
		               return ps;  
		        }  
		    }, keyHolder);
		} catch(Exception e) {
			logger.error("addApplyRecord : {}", e.toString());
		}
		logger.debug("addApplyRecord result : {}", keyHolder.getKey().longValue());
		return keyHolder.getKey().longValue();
	}

	@Override
	public boolean delApplyRecord(String ids) {
		String sql= "delete from task where id=";
		String[] idsStr = ids.split(",");
		for(int index=0; index<idsStr.length; index++){
			if(index == idsStr.length - 1)
				sql = sql + idsStr[index];
			else
				sql = sql + idsStr[index] + " or id=";
		}
		logger.info("sql : " + sql);
		try{
			int num = jdbcTemplate.update(sql);
			return num > 0;
		}catch(Exception e){
			logger.error("delApplyRecord : {}", e.toString());
		}
		return false;
	}

	@Override
	public boolean updateApplyRecord(long id, String status) {
		String sql = "update task set status=? where id=?";
		int affectedRows = 0;
		try {
			affectedRows = jdbcTemplate.update(sql, status, id);
			logger.debug("updateApplyRecord result : {}", affectedRows);
		} catch(Exception e) {
			logger.error(e.toString());
		}
		return affectedRows != 0;
	}	
}
