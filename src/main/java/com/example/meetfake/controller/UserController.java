package com.example.meetfake.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meetfake.mapper.UserMapper;
import com.example.meetfake.model.User;
import com.example.meetfake.model.UserExample;


@RestController
public class UserController {
	@Autowired
	UserMapper userMapper;
	
	@GetMapping("/list-user")
	public List<User> listUser(){
		UserExample userExample = new UserExample();
		userExample.createCriteria().getAllCriteria();
		List<User> listUsers = userMapper.selectByExample(userExample);
		return listUsers;
	}
}
