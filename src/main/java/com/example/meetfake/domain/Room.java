package com.example.meetfake.domain;

import org.springframework.web.socket.WebSocketSession;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Room {
	@NotNull
	private final String id;
	private final String room_id;
	private final String host_id;
	private final Map<WebSocketSession, String> clients = new HashMap<>();
	private final Map<WebSocketSession, String> micSocket = new HashMap<>();
	private final Map<WebSocketSession, String> videoSocket = new HashMap<>();
	private final Map<WebSocketSession, Object> info = new HashMap<>();

	private String board;

	public Room(String id, String room_id, String host_id) {
		this.id = id;
		this.room_id = room_id;
		this.host_id = host_id;
	}

	public String getId() {
		return id;
	}

	public String getBoard() {
		return board;
	}

	public String getRoom_id() {
		return room_id;
	}

	public String getHost_id() {
		return host_id;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public Map<WebSocketSession, String> getClients() {
		return clients;
	}

	public Map<WebSocketSession, String> getMicSocket() {
		return micSocket;
	}

	public Map<WebSocketSession, String> getVideoSocket() {
		return videoSocket;
	}

	public Map<WebSocketSession, Object> getInfo() {
		return info;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Room room = (Room) o;
		return Objects.equals(getId(), room.getId()) && Objects.equals(getClients(), room.getClients());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId(), getClients());
	}
}
