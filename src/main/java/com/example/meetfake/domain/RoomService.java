package com.example.meetfake.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

@Service
public class RoomService {
    private final Set<Room> rooms = new TreeSet<>(Comparator.comparing(Room::getId));

    @Autowired
    public RoomService() {

    }

    public Boolean addRoom(final Room room) {
        return rooms.add(room);
    }

    public Set<Room> getRooms() {
        final TreeSet<Room> defensiveCopy = new TreeSet<>(Comparator.comparing(Room::getId));
        defensiveCopy.addAll(rooms);
        return defensiveCopy;
    }

    public Optional<Room> findRoomByStringId(final String sid) {
        // simple get() because of parser errors handling
        return rooms.stream().filter(r -> r.getId().equals(sid)).findAny();
    }

    public String getRoomId(Room room) {
        return room.getId();
    }

    public Map<WebSocketSession, String> getClients(final Room room) {
        return Optional.ofNullable(room)
                .map(r -> Collections.unmodifiableMap(r.getClients()))
                .orElse(Collections.emptyMap());
    }

    public Map<WebSocketSession, String> getVideoSocket(final Room room) {
        return room.getVideoSocket();
    }

    public Map<WebSocketSession, String> getMicSocket(final Room room) {
        return room.getMicSocket();
    }

    public String addClient(final Room room, final String name, final WebSocketSession session) {
        return room.getClients().put(session, name);
    }

    public void setBoard(Room room, String url) {
        room.setBoard(url);
    }

    public String getBoard(Room room) {
        return room.getBoard();
    }

    public String removeClientByKey(final Room room, final WebSocketSession name) {
        return room.getClients().remove(name);
    }
}
