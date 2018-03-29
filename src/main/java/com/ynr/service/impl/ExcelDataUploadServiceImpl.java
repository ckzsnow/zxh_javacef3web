package com.ynr.service.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import com.alibaba.fastjson.JSON;
import com.ynr.dao.ITaskDao;
import com.ynr.service.IExcelDataUploadService;
import com.ynr.utils.RedisPool;
import com.ynr.utils.YNRUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Service("excelDataUploadService")
public class ExcelDataUploadServiceImpl implements IExcelDataUploadService {
	
	@Autowired
	private ITaskDao taskDao;
	
	private static final Logger logger = LoggerFactory.getLogger(ExcelDataUploadServiceImpl.class);
	
	@Override
	public void processExcelDataUploadForEVUSAndFrance(DefaultMultipartHttpServletRequest multipartRequest, String userId, int countryId) {
		String filePath = "/data/countries/evus";
		String excelFile = "";
		String name = "";
		String aliaName = "";
		if (multipartRequest != null) {
			Iterator<String> iterator = multipartRequest.getFileNames();
			while (iterator.hasNext()) {
				MultipartFile multifile = multipartRequest.getFile((String) iterator.next());
				String extendName = multifile.getOriginalFilename().substring(
						multifile.getOriginalFilename().lastIndexOf(".") + 1);
				String originalName = multifile.getOriginalFilename().substring(0,
						multifile.getOriginalFilename().lastIndexOf(".") + 1);
				String uuid = UUID.randomUUID().toString().replaceAll("-", "");
				String fileName = uuid + "_evus." + extendName;
				if(("xls").equals(extendName) || ("xlsm").equals(extendName) ||
						("xlsx").equals(extendName)){
					excelFile = fileName;
					name = originalName;
					aliaName = multifile.getOriginalFilename();
				}
				try {
					FileUtils.copyInputStreamToFile(multifile.getInputStream(),
							new File(filePath, fileName));
					long id = taskDao.addApplyRecord(userId, name, countryId, excelFile, aliaName);
					Map<String, String> taskInfoMap = new HashMap<>();
					taskInfoMap.put("task_id", String.valueOf(id));
					taskInfoMap.put("task_type", YNRUtils.countryIdToEngMap.get(countryId));
					taskInfoMap.put("task_excel_file_path", filePath + "/" + fileName);
					Jedis jedis = RedisPool.pool.getResource();
					if(jedis.rpush("PARSE_TASK", JSON.toJSONString(taskInfoMap))>0){
						taskDao.updateApplyRecord(id, "已提交任务,等待解析");
					} else {
						logger.error("FAIL in submiting a task ! info : " + filePath + File.pathSeparator + fileName);
						taskDao.updateApplyRecord(id, "提交任务失败");
					}
					jedis.close();
				} catch (JedisConnectionException e) {
					logger.error(e.toString());
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
		}
	}

	@Override
	public void processExcelDataUploadForAustarlia(DefaultMultipartHttpServletRequest multipartRequest, String userId, int countryId) {
		String filePath = "E:\\data\\countries\\australia";
		String excelFile = "";
		String name = "";
		String aliaName = "";
		Map<String, Map<String, String>> excelMap = new HashMap<>();
		Map<String, Map<String, String>> attachmentMap = new HashMap<>();
		if (multipartRequest != null) {
			Iterator<String> iterator = multipartRequest.getFileNames();
			while (iterator.hasNext()) {
				MultipartFile multifile = multipartRequest.getFile((String) iterator.next());
				String extendName = multifile.getOriginalFilename().substring(
						multifile.getOriginalFilename().lastIndexOf(".") + 1);
				String originalName = multifile.getOriginalFilename().substring(0,
						multifile.getOriginalFilename().lastIndexOf("."));
				String uuid = UUID.randomUUID().toString().replaceAll("-", "");
				String fileName = uuid + "_australia." + extendName;
				if(("xls").equals(extendName) || ("xlsm").equals(extendName) ||
						("xlsx").equals(extendName)){
					excelFile = fileName;
					name = originalName;
					aliaName = multifile.getOriginalFilename();
					Map<String, String> excelSubMap = new HashMap<>();
					excelSubMap.put("task_excel_file_path", filePath + "\\" + fileName);
					excelSubMap.put("excel_file", excelFile);
					excelSubMap.put("excel_file_alia_name", aliaName);
					excelMap.put(name, excelSubMap);
				} else {
					String userName = originalName.substring(0, originalName.indexOf("_"));
					String attachmentIdentify = originalName.substring(originalName.indexOf("_")+1);
					if(!attachmentMap.containsKey(userName)) attachmentMap.put(userName, new HashMap<>());
					attachmentMap.get(userName).put(attachmentIdentify, filePath + "\\" + fileName);
				}
				try {
					FileUtils.copyInputStreamToFile(multifile.getInputStream(),
							new File(filePath, fileName));
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
			try {
				for(Entry<String, Map<String, String>> entry : excelMap.entrySet()){
					Map<String, String> taskInfoMap = new HashMap<>();
					String userName = entry.getKey();
					String taskExcelFilePath = entry.getValue().get("task_excel_file_path");
					String excelFileInfo = entry.getValue().get("excel_file");
					String excelFileAliaName = entry.getValue().get("excel_file_alia_name");
					if(attachmentMap != null && !attachmentMap.isEmpty()) {
						Map<String, String> attcMap = attachmentMap.get(userName);
						for(Entry<String, String> attcEntry : attcMap.entrySet()){
							taskInfoMap.put(attcEntry.getKey(), attcEntry.getValue());
						}
					}
					long id = taskDao.addApplyRecord(userId, userName, countryId, excelFileInfo, excelFileAliaName);
					taskInfoMap.put("task_id", String.valueOf(id));
					taskInfoMap.put("task_type", YNRUtils.countryIdToEngMap.get(countryId));
					taskInfoMap.put("task_excel_file_path", taskExcelFilePath);
					Jedis jedis = RedisPool.pool.getResource();
					if(jedis.rpush("PARSE_TASK", JSON.toJSONString(taskInfoMap))>0){
						taskDao.updateApplyRecord(id, "已提交任务,等待解析");
					} else {
						logger.error("FAIL in submiting a task ! info : " + taskExcelFilePath);
						taskDao.updateApplyRecord(id, "提交任务失败");
					}
					jedis.close();
				}				
			} catch (JedisConnectionException e) {
				logger.error(e.toString());
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
	}

	@Override
	public void processExcelDataUploadForIndia(DefaultMultipartHttpServletRequest multipartRequest, String userId,
			int countryId) {
		String filePath = "E:\\data\\countries\\india";
		String excelFile = "";
		String name = "";
		String aliaName = "";
		Map<String, Map<String, String>> excelMap = new HashMap<>();
		Map<String, Map<String, String>> attachmentMap = new HashMap<>();
		if (multipartRequest != null) {
			Iterator<String> iterator = multipartRequest.getFileNames();
			while (iterator.hasNext()) {
				MultipartFile multifile = multipartRequest.getFile((String) iterator.next());
				String extendName = multifile.getOriginalFilename().substring(
						multifile.getOriginalFilename().lastIndexOf(".") + 1);
				String originalName = multifile.getOriginalFilename().substring(0,
						multifile.getOriginalFilename().lastIndexOf("."));
				String uuid = UUID.randomUUID().toString().replaceAll("-", "");
				String fileName = uuid + "_india." + extendName;
				if(("xls").equals(extendName) || ("xlsm").equals(extendName) ||
						("xlsx").equals(extendName)){
					excelFile = fileName;
					name = originalName;
					aliaName = multifile.getOriginalFilename();
					Map<String, String> excelSubMap = new HashMap<>();
					excelSubMap.put("task_excel_file_path", filePath + "\\" + fileName);
					excelSubMap.put("excel_file", excelFile);
					excelSubMap.put("excel_file_alia_name", aliaName);
					excelMap.put(name, excelSubMap);
				} else {
					String userName = originalName.substring(0, originalName.indexOf("_"));
					String attachmentIdentify = originalName.substring(originalName.indexOf("_")+1);
					if(!attachmentMap.containsKey(userName)) attachmentMap.put(userName, new HashMap<>());
					attachmentMap.get(userName).put(attachmentIdentify, filePath + "\\" + fileName);
				}
				try {
					FileUtils.copyInputStreamToFile(multifile.getInputStream(),
							new File(filePath, fileName));
				} catch (Exception e) {
					logger.error(e.toString());
				}
			}
			try {
				for(Entry<String, Map<String, String>> entry : excelMap.entrySet()){
					Map<String, String> taskInfoMap = new HashMap<>();
					String userName = entry.getKey();
					String taskExcelFilePath = entry.getValue().get("task_excel_file_path");
					String excelFileInfo = entry.getValue().get("excel_file");
					String excelFileAliaName = entry.getValue().get("excel_file_alia_name");
					if(attachmentMap != null && !attachmentMap.isEmpty()) {
						Map<String, String> attcMap = attachmentMap.get(userName);
						for(Entry<String, String> attcEntry : attcMap.entrySet()){
							taskInfoMap.put(attcEntry.getKey(), attcEntry.getValue());
						}
					}
					long id = taskDao.addApplyRecord(userId, userName, countryId, excelFileInfo, excelFileAliaName);
					taskInfoMap.put("task_id", String.valueOf(id));
					taskInfoMap.put("task_type", YNRUtils.countryIdToEngMap.get(countryId));
					taskInfoMap.put("task_excel_file_path", taskExcelFilePath);
					Jedis jedis = RedisPool.pool.getResource();
					if(jedis.rpush("PARSE_TASK", JSON.toJSONString(taskInfoMap))>0){
						taskDao.updateApplyRecord(id, "已提交任务,等待解析");
					} else {
						logger.error("FAIL in submiting a task ! info : " + taskExcelFilePath);
						taskDao.updateApplyRecord(id, "提交任务失败");
					}
					jedis.close();
				}				
			} catch (JedisConnectionException e) {
				logger.error(e.toString());
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
	}	
}