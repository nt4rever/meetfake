package com.example.meetfake.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.meetfake.mapper.TrackingMapper;
import com.example.meetfake.model.Tracking;

@Service
public class SocketServiceImpl implements SocketService {

	@Autowired
	TrackingMapper trackingMapper;

	@Override
	public void saveTracking(String room_id, String user_id, String start, String end, String ip) {
		Tracking tracking = new Tracking();
		tracking.setRoomId(Long.parseLong(room_id));
		tracking.setUserId(Long.parseLong(user_id));
		tracking.setIp(ip);
		tracking.setStart(start);
		tracking.setEnd(end);
		trackingMapper.insert(tracking);
//		System.out.println(
//				"room_id " + room_id + " user_id " + user_id + " start " + start + " end " + end + " ip " + ip);
	}

}
