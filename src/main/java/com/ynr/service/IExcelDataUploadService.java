package com.ynr.service;

import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

public interface IExcelDataUploadService {
	
	public void processExcelDataUploadForEVUSAndFrance(DefaultMultipartHttpServletRequest multipartRequest, String userId, int countryId);
	
	public void processExcelDataUploadForAustarlia(DefaultMultipartHttpServletRequest multipartRequest, String userId, int countryId);
	
}