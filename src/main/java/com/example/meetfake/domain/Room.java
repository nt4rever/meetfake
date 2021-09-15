package com.example.meetfake.domain;

import org.springframework.web.socket.WebSocketSession;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Room {
    @NotNull
    private final String id;
    private final Map<WebSocketSession, String> clients = new HashMap<>();
    private final Map<WebSocketSession, String> micSocket = new HashMap<>();
    private final Map<WebSocketSession, String> videoSocket = new HashMap<>();

    public Room(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Room room = (Room) o;
        return Objects.equals(getId(), room.getId()) &&
                Objects.equals(getClients(), room.getClients());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getClients());
    }
}
