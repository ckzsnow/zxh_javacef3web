package com.ynr.controller;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RootController {

	private static final Logger logger = LoggerFactory.getLogger(RootController.class);
		
	@RequestMapping("/")
	public String displayHomePage(HttpServletRequest request) {
		logger.debug("request home page");
		return "redirect:views/login.html";
	}
	
}
