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
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class MainServiceImpl implements MainService {
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
	public String createRoom(String userId) {
		String roomCode = getSaltString();
		com.example.meetfake.model.Room newRoom = new com.example.meetfake.model.Room();
		newRoom.setRoomid(roomCode);
		newRoom.setHost(Long.parseLong(userId));
		newRoom.setTitle(roomCode);
		newRoom.setStatus((byte) 0);
		roomMapper.insert(newRoom);
		return REDIRECT + "room/" + roomCode;
	}

	@Override
	public ModelAndView joinRoom(String roomCode, String username, String id, String ip) {
		RoomExample roomExample = new RoomExample();
		roomExample.createCriteria().andRoomidEqualTo(roomCode);
		List<com.example.meetfake.model.Room> listRoom = roomMapper.selectByExample(roomExample);
		if (listRoom.size() == 0)
			return new ModelAndView("redirect:/");
		com.example.meetfake.model.Room rm = listRoom.get(0);
		String host = rm.getHost().toString();
		String title = rm.getTitle();
		if (id.equals(host)) {
			return join(roomCode, title, username, id, rm.getId().toString(), "host", host, ip);
		}
		if (rm.getStatus() == 0) {
			return join(roomCode, title, username, id, rm.getRoomid().toString(), "attendance", host, ip);
		}
		RoomDetailExample rDetailExample = new RoomDetailExample();
		rDetailExample.createCriteria().andRoomIdEqualTo(rm.getId());
		List<RoomDetail> listRoomDetails = roomDetailMapper.selectByExample(rDetailExample);
		for (RoomDetail rd : listRoomDetails) {
			String user_id = rd.getUserId().toString();
			if (id.equals(user_id))
				return join(roomCode, title, username, id, rd.getRoomId().toString(), "attendance", host, ip);
		}
		return new ModelAndView("redirect:/");
	}

	private ModelAndView join(String roomCode, String title, String username, String user_id, String room_id,
			String auth, String host_id, String ip) {
		Optional<Room> room = roomService.findRoomByStringId(roomCode);
		if (room.isEmpty())
			roomService.addRoom(new Room(roomCode, room_id, host_id));
		final ModelAndView modelAndView = new ModelAndView("room");
		modelAndView.addObject("roomCode", roomCode);
		modelAndView.addObject("title", title);
		modelAndView.addObject("userId", user_id);
		modelAndView.addObject("roomId", room_id);
		modelAndView.addObject("auth", auth);
		modelAndView.addObject("ip", ip);
		modelAndView.addObject("username", username);
		return modelAndView;
	}

	private String getSaltString() {
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
