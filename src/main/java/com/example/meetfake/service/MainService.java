package com.example.meetfake.service;

import org.springframework.web.servlet.ModelAndView;

public interface MainService {
    ModelAndView joinRoom(String roomId, String username);
    String createRoom(String userId,String roomId);
}
