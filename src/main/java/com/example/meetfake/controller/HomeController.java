package com.example.meetfake.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.meetfake.mapper.UserMapper;
import com.example.meetfake.model.User;
import com.example.meetfake.model.UserExample;

@Controller
public class HomeController {
	@Autowired
	UserMapper userMapper;

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

	@GetMapping("/sign-in")
	public String signIn(HttpServletRequest request) {
		String id = (String) request.getSession().getAttribute("userId");
		if (id != null)
			if (!id.isEmpty())
				return "redirect:/";
		return "sign-in";
	}

	@PostMapping("/sign-in")
	public String doSignIn(HttpServletRequest request, @RequestParam() String password, String email) {
		UserExample userExample = new UserExample();
		userExample.createCriteria().andEmailEqualTo(email);
		List<User> listUser = userMapper.selectByExample(userExample);
		if (listUser.size() > 0) {
			BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
			String ps = listUser.get(0).getPassword();
			if (bc.matches(password, ps)) {
				User user = listUser.get(0);
				request.getSession().setAttribute("userId", user.getId().toString());
				request.getSession().setAttribute("fullname", user.getFullname());
				return "redirect:/";
			}
		}
		return "redirect:/sign-in";
	}

	@GetMapping("/sign-up")
	public String signUp(HttpServletRequest request) {
		String id = (String) request.getSession().getAttribute("userId");
		if (id != null)
			if (!id.isEmpty())
				return "redirect:/";
		return "sign-up";
	}

	@PostMapping("/sign-up")
	public String doSignUp(@RequestParam() String fullname, String password, String email) {
		// check email existence
		UserExample userExample = new UserExample();
		userExample.createCriteria().andEmailEqualTo(email);
		List<User> listUser = userMapper.selectByExample(userExample);
		if (listUser.size() > 0)
			return "redirect:/sign-up";
		User newUser = new User();
		newUser.setEmail(email);
		newUser.setFullname(fullname);
		BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
		newUser.setPassword(bc.encode(password));
		userMapper.insert(newUser);
		return "redirect:/sign-in";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		request.getSession().invalidate();
		return "redirect:/";
	}

}
