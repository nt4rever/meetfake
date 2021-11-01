package com.example.meetfake.controller;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.example.meetfake.service.MainService;
import com.example.meetfake.util.HttpUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.meetfake.mapper.RoomDetailMapper;
import com.example.meetfake.mapper.RoomMapper;
import com.example.meetfake.mapper.UserMapper;
import com.example.meetfake.model.Room;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@ControllerAdvice
public class RoomController {

	private final MainService mainService;

	@Autowired
	RoomMapper roomMapper;

	@Autowired
	UserMapper userMapper;

	@Autowired
	RoomDetailMapper roomDetailMapper;

	@Autowired
	public RoomController(final MainService mainService) {
		this.mainService = mainService;
	}

	@GetMapping("/create-room")
	public String createRoom(HttpServletRequest request) {
		String userId = (String) request.getSession().getAttribute("userId");
		if (userId == null || userId.isEmpty()) {
			return "redirect:/sign-in";
		}
		return this.mainService.createRoom(userId);
	}

	@GetMapping("/room/{roomId}")
	public ModelAndView joinRoom(@PathVariable("roomId") final String roomId, HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute("fullname");
		String id = (String) request.getSession().getAttribute("userId");
		if (username == null || username.isEmpty()) {
			return new ModelAndView("redirect:/sign-in");
		}
		String ip = HttpUtils.getRequestIP(request);
		return this.mainService.joinRoom(roomId, username, id, ip);
	}

	@GetMapping("/join-room")
	public ModelAndView joinExistRoom(@RequestParam() String room, HttpServletRequest request) {
		String username = (String) request.getSession().getAttribute("fullname");
		String id = (String) request.getSession().getAttribute("userId");
		if (username == null || username.isEmpty()) {
			return new ModelAndView("redirect:/sign-in");
		}
		String ip = HttpUtils.getRequestIP(request);
		return mainService.joinRoom(room, username, id, ip);
	}

	protected String getSaltString() {
		String SALTCHARS = "abcdefghijklmnopqrstuvwxyz";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		// -- 0 1 2 3 4 5 6 7 8 9 10 11
		while (salt.length() < 11) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		salt.setCharAt(3, '-');
		salt.setCharAt(7, '-');
		String saltStr = salt.toString();
		return saltStr;
	}
}
