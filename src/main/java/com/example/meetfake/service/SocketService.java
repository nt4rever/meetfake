package com.example.meetfake.service;

public interface SocketService {
	void saveTracking(String room_id, String user_id, String start, String end, String ip);
}
