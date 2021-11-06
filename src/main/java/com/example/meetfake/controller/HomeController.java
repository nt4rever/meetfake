package com.example.meetfake.controller;

import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.meetfake.mapper.UserMapper;
import com.example.meetfake.model.User;
import com.example.meetfake.model.UserExample;

@Controller
public class HomeController {

	@Autowired
	UserMapper mapper;

	@Value("${dashboard.url}")
	private String url;

	@GetMapping("/")
	public String index(HttpServletRequest request, Model model) {
		String id = (String) request.getSession().getAttribute("userId");
		if (id == null || id.isEmpty()) {
			return "redirect:/sign-in";
		}
		model.addAttribute(id);
		String fullname = (String) request.getSession().getAttribute("fullname");
		model.addAttribute("fullname", fullname);
		return "index";
	}

	@GetMapping("/dashboard")
	public String dashboard(HttpServletRequest request) {
		String id = (String) request.getSession().getAttribute("userId");
		if (id == null || id.isEmpty()) {
			return "redirect:/sign-in";
		}
		UserExample userExample = new UserExample();
		userExample.createCriteria().andIdEqualTo(Long.parseLong(id));
		List<User> listUser = mapper.selectByExample(userExample);
		if (listUser.size() > 0) {
			User user = listUser.get(0);
			String token = getToken();
			user.setToken(token);
			mapper.updateByPrimaryKey(user);
			return "redirect:" + url + "/api/login?id=" + user.getId() + "&token=" + token;
		}
		return "redirect:/sign-in";
	}

	private String getToken() {
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 30) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;
	}

}
