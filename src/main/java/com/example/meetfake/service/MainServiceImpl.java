package com.example.meetfake.service;

import com.example.meetfake.domain.Room;
import com.example.meetfake.domain.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Service
public class MainServiceImpl implements MainService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String REDIRECT = "redirect:/";

    private final RoomService roomService;

    @Autowired
    public MainServiceImpl(final RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public String createRoom(String userId, String roomId) {
        roomService.addRoom(new Room(roomId));
        return REDIRECT + "room/" + roomId;
    }

    @Override
    public ModelAndView joinRoom(String roomId, String username) {
        Optional<Room> room = roomService.findRoomByStringId(roomId);
        if (room.isEmpty()) {
            return new ModelAndView("redirect:/");
        }
        final ModelAndView modelAndView = new ModelAndView("room");
        modelAndView.addObject("roomId", roomId);
        modelAndView.addObject("rooms", roomService.getRooms());
        modelAndView.addObject("username", username);
        return modelAndView;
    }
}
