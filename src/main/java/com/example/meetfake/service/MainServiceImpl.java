package com.example.meetfake.service;

import com.example.meetfake.domain.Room;
import com.example.meetfake.domain.RoomService;
import com.example.meetfake.mapper.RoomDetailMapper;
import com.example.meetfake.mapper.RoomMapper;
import com.example.meetfake.model.RoomDetail;
import com.example.meetfake.model.RoomDetailExample;
import com.example.meetfake.model.RoomExample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Service
public class MainServiceImpl implements MainService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String REDIRECT = "redirect:/";

	private final RoomService roomService;

	@Autowired
	RoomDetailMapper roomDetailMapper;

	@Autowired
	RoomMapper roomMapper;

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
	public ModelAndView joinRoom(String roomId, String username, String id) {
		RoomExample roomExample = new RoomExample();
		roomExample.createCriteria().andRoomidEqualTo(roomId);
		List<com.example.meetfake.model.Room> listRoom = roomMapper.selectByExample(roomExample);
		if (listRoom.size() == 0)
			return new ModelAndView("redirect:/");
		com.example.meetfake.model.Room rm = listRoom.get(0);
		String host = rm.getHost().toString();
		if (id.equals(host)) {
			return join(roomId, username, id, "host");
		}
		RoomDetailExample rDetailExample = new RoomDetailExample();
		rDetailExample.createCriteria().andRoomIdEqualTo(rm.getId());
		List<RoomDetail> listRoomDetails = roomDetailMapper.selectByExample(rDetailExample);
		for (RoomDetail rd : listRoomDetails) {
			String user_id = rd.getUserId().toString();
			if (id.equals(user_id))
				return join(roomId, username, id, "attendance");
		}
		return new ModelAndView("redirect:/");
	}

	private ModelAndView join(String roomId, String username, String id, String auth) {
		Optional<Room> room = roomService.findRoomByStringId(roomId);
		if (room.isEmpty())
			roomService.addRoom(new Room(roomId));
		final ModelAndView modelAndView = new ModelAndView("room");
		modelAndView.addObject("roomId", roomId);
		modelAndView.addObject("userId", id);
		modelAndView.addObject("auth", auth);
		modelAndView.addObject("rooms", roomService.getRooms());
		modelAndView.addObject("username", username);
		return modelAndView;
	}
}
