package com.ynr.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import com.ynr.dao.ICountryDao;
import com.ynr.dao.ITaskDao;
import com.ynr.service.IExcelDataUploadService;

@Controller
public class DataController {

	private static final Logger logger = LoggerFactory.getLogger(DataController.class);
	
	@Autowired
	private ITaskDao taskDao;
	
	@Autowired
	private ICountryDao countryDao;
	
	@Autowired
	private IExcelDataUploadService excelDataUploadService;
	
	@RequestMapping("/task/getAllTask")
	@ResponseBody
	public List<Map<String, Object>> getAllTask(HttpServletRequest request) {
		List<Map<String, Object>> retList = new ArrayList<>();
		String userId = (String) request.getSession().getAttribute("user_id");
		String offset = request.getParameter("offset");
		String limit = request.getParameter("limit");
		int offset_ = 0;
		int limit_ = 1000000;
		/*try {
			offset_ = Integer.valueOf(offset);
			limit_ = Integer.valueOf(limit);
		} catch(Exception ex) {
			logger.error(ex.toString());
		}*/
		retList = taskDao.getAllApplyRecord(userId, offset_, limit_);
		return retList;
	}
	
	@RequestMapping("/task/delTaskById")
	@ResponseBody
	public Map<String, String> delTaskById(HttpServletRequest request) {
		Map<String, String> retMap = new HashMap<>();
		String ids = request.getParameter("ids");
		if(ids == null || ids.isEmpty()){
			retMap.put("error", "请选择需要删除的任务！");
		} else {
			if(taskDao.delApplyRecord(ids)){
				retMap.put("error", "");
			}else{
				retMap.put("error", "服务器暂时无法处理您的请求，请稍后重试！");
			}
		}
		return retMap;
	}
	
	@RequestMapping("/task/downloadExecelData")
	public ResponseEntity<byte[]> downloadExecelData(HttpServletRequest request) {
		String fileName = request.getParameter("fileName");
		ResponseEntity<byte[]> re = null;
		File file = new File("/data/autofillform/exceldata/" + fileName);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDispositionFormData("attachment", new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			re = new ResponseEntity<byte[]>(
					FileUtils.readFileToByteArray(file), headers,
					HttpStatus.CREATED);
		} catch (IOException e) {
			logger.debug("downloadExecelData exception : {}", e.toString());
		}
		return re;
	}
	
	@RequestMapping("/getAllCountries")
	@ResponseBody
	public List<Map<String, Object>> getAllCountries(HttpServletRequest request) {
		List<Map<String, Object>> retList = new ArrayList<>();
		retList = countryDao.getAllCountry();
		return retList;
	}
	
	@RequestMapping("/task/uploadTaskFiles")
	@ResponseBody
	public Map<String, String> uploadTaskFiles(DefaultMultipartHttpServletRequest multipartRequest,
			HttpServletRequest request) {
		logger.debug("uploadTaskFiles");
		String userId = (String) request.getSession().getAttribute("user_id");
		String selectCountry = request.getParameter("selectCountry");
		int selectCountry_ = 0;
		try {
			selectCountry_ = Integer.valueOf(selectCountry);
		} catch(Exception ex) {
			logger.error(ex.toString());
		}
		if(selectCountry_ == 5 || selectCountry_ ==6){
			excelDataUploadService.processExcelDataUploadForEVUSAndFrance(multipartRequest, userId, selectCountry_);
		} else if(selectCountry_ == 4) {
			excelDataUploadService.processExcelDataUploadForAustarlia(multipartRequest, userId, selectCountry_);
		}
		Map<String, String> retMap = new HashMap<>();
		retMap.put("url", "url");
		return retMap;
	}
}
