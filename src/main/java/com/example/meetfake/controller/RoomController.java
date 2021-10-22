package com.example.meetfake.controller;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.example.meetfake.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    RoomMapper romMapper;

    @Autowired
    UserMapper userMapper;

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
        String roomId = getSaltString();
        Room newRoom = new Room();
        newRoom.setRoomid(roomId);
        newRoom.setHost(Long.parseLong(userId));
        romMapper.insert(newRoom);
        return this.mainService.createRoom(userId, roomId);
    }

    @GetMapping("/room/{roomId}")
    public ModelAndView joinRoom(@PathVariable("roomId") final String roomId, Model model, HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("fullname");
        if (username == null || username.isEmpty()) {
            return new ModelAndView("redirect:/sign-in");
        }
        return this.mainService.joinRoom(roomId, username);
    }

    @GetMapping("/join-room")
    public ModelAndView joinExistRoom(@RequestParam() String room, HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("fullname");
        if (username == null || username.isEmpty()) {
            return new ModelAndView("redirect:/sign-in");
        }
        System.out.println(room);
        return this.mainService.joinRoom(room, username);
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


