package com.ynr.controller;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ynr.dao.IUserDao;
import com.ynr.utils.UserPwdMD5Encrypt;

@Controller
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private IUserDao userDao;
	
	@RequestMapping("/user/userLogin")
	@ResponseBody
	public Map<String, Object> userLogin(HttpServletRequest request) {
		Map<String, Object> retMap = new HashMap<>();
		String useremail = request.getParameter("user_email");
		String password = request.getParameter("user_pwd");
		logger.debug("userLogin useremail : {}, password : {}", useremail, password);
		if(useremail != null && !useremail.isEmpty() && password != null && !password.isEmpty()) {
			Map<String, Object> userMap = userDao.getUserByUsername(useremail);
			if(userMap == null || userMap.isEmpty()){
				retMap.put("error_code", "1");
				retMap.put("error_msg", "您输入的用户名不正确，请检查！");
				
			} else {
				String enPwd = (String)userMap.get("user_pwd");
				if(UserPwdMD5Encrypt.getPasswordByMD5Encrypt(password).equals(enPwd)) {
					retMap.put("error_code", "0");
					request.getSession().setAttribute("user_id", useremail);
				} else {
					retMap.put("error_code", "2");
					retMap.put("error_msg", "您输入的密码不正确，请检查！");
				}
			}
		} else {
			retMap.put("error_code", "3");
			retMap.put("error_msg", "用户名或密码不能为空！");
		}
		return retMap;
	}
}
