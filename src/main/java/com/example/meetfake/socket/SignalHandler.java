package com.example.meetfake.socket;

import com.example.meetfake.domain.Room;
import com.example.meetfake.domain.RoomService;
import com.example.meetfake.domain.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SignalHandler extends TextWebSocketHandler {
    @Autowired
    private RoomService roomService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Room> sessionIdToRoomMap = new HashMap<>();

    // message types, used in signalling:
    // text message
    private static final String MSG_TYPE_TEXT = "text";
    // SDP Offer message
    private static final String MSG_TYPE_OFFER = "offer";
    // SDP Answer message
    private static final String MSG_TYPE_ANSWER = "answer";
    // New ICE Candidate message
    private static final String MSG_TYPE_ICE = "ice";
    // join room data message
    private static final String MSG_TYPE_JOIN = "join";

    private static final String MSG_TYPE_ACTION = "action";
    // leave room data message
    private static final String MSG_TYPE_LEAVE = "leave";

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        // webSocket has been opened, send a message to the client
        // when data field contains 'true' value, the client starts negotiating
        // to establish peer-to-peer connection, otherwise they wait for a counterpart
        //sendMessage(session, new WebSocketMessage("Server", MSG_TYPE_JOIN, Boolean.toString(!sessionIdToRoomMap.isEmpty()), null, null));
    }


    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        logger.debug("[ws] Session has been closed with status {}", status);
        System.out.println("Remove client " + session.getId());
        Room rm = sessionIdToRoomMap.get(session.getId());
        if (rm != null) {
            Map<WebSocketSession, String> clients = roomService.getClients(rm);
            String username = clients.get(session);
            for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                if (!client.getKey().getId().equals(session.getId())) {
                    sendMessage(client.getKey(), new WebSocketMessage(
                            username,
                            "leave",
                            session.getId()
                    ));
                    sendMessage(client.getKey(), new WebSocketMessage(
                            username,
                            "user count",
                            String.valueOf(clients.size() - 1)
                    ));
                }
            }
            roomService.removeClientByKey(rm, session);
        }
        sessionIdToRoomMap.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(final WebSocketSession session, final TextMessage textMessage) throws Exception {
        WebSocketMessage message = objectMapper.readValue(textMessage.getPayload(), WebSocketMessage.class);
        Map<WebSocketSession, String> clients;
        Map<WebSocketSession, String> micSocket;
        Map<WebSocketSession, String> videoSocket;
        Room rm = sessionIdToRoomMap.get(session.getId());
        Room room;
        Object sid;
        switch (message.getType()) {
            case MSG_TYPE_TEXT:
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (!client.getKey().getId().equals(session.getId()))
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    message.getFrom(),
                                    message.getType(),
                                    message.getData()
                            ));
                    }
                }
                break;
            case MSG_TYPE_OFFER:
                System.out.println("offer " + session.getId());
                Object offer = message.getOffer();
                sid = message.getSid();
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    micSocket = roomService.getMicSocket(rm);
                    videoSocket = roomService.getVideoSocket(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().getId().equals(sid)) {
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    null, "offer", null, null, session.getId(), offer, micSocket.get(session), videoSocket.get(session), client.getValue(), null, null
                            ));
                        }
                    }
                }
                break;
            case MSG_TYPE_ANSWER:
                System.out.println("answer " + session.getId());
                Object answer = message.getAnswer();
                sid = message.getSid();
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().getId().equals(sid)) {
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    null, "answer", null, null, session.getId(), null, null, null, null, answer, null
                            ));
                        }
                    }
                }
                break;

            case MSG_TYPE_ICE:
                System.out.println("ice " + session.getId());
                Object candidate = message.getCandidate();
                sid = message.getSid();
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().getId().equals(sid)) {
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    null, "ice", null, candidate, session.getId(), null, null, null, null, null, null
                            ));
                        }
                    }
                }
                break;

            case MSG_TYPE_JOIN:
                room = roomService.findRoomByStringId(message.getData())
                        .orElseThrow(() -> new IOException("Invalid room number received!"));
                roomService.addClient(room, message.getFrom(), session);
                sessionIdToRoomMap.put(session.getId(), room);
                clients = roomService.getClients(room);
                micSocket = roomService.getMicSocket(room);
                videoSocket = roomService.getVideoSocket(room);
                micSocket.put(session, "on");
                videoSocket.put(session, "on");
                ArrayList<String> conc = new ArrayList<String>();
                Map<String, String> socketName = new HashMap<>();
                Map<String, String> micInfo = new HashMap<>();
                Map<String, String> videoInfo = new HashMap<>();
                for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                    if (!client.getKey().getId().equals(session.getId()))
                        conc.add(client.getKey().getId());
                    socketName.put(client.getKey().getId(), client.getValue());
                }
                for (Map.Entry<WebSocketSession, String> mic : micSocket.entrySet()) {
                    micInfo.put(mic.getKey().getId(), mic.getValue());
                }
                for (Map.Entry<WebSocketSession, String> video : videoSocket.entrySet()) {
                    videoInfo.put(video.getKey().getId(), video.getValue());
                }
                sendMessage(session,
                        new WebSocketMessage(
                                message.getFrom(),
                                "join",
                                objectMapper.writeValueAsString(socketName),
                                objectMapper.writeValueAsString(conc),
                                objectMapper.writeValueAsString(micInfo),
                                objectMapper.writeValueAsString(micInfo)));

                for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                    sendMessage(client.getKey(), new WebSocketMessage(
                            "username",
                            "user count",
                            String.valueOf(clients.size())
                    ));
                    sendMessage(client.getKey(), new WebSocketMessage(
                            "Bot",
                            "text",
                            message.getFrom() + " join!"
                    ));
                }
                System.out.println(message.getFrom() + " join room " + room.getId() + " session " + session.getId());
                break;

            case MSG_TYPE_ACTION:
                String msg = message.getData();
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    micSocket = roomService.getMicSocket(rm);
                    videoSocket = roomService.getVideoSocket(rm);
                    if (msg == "mute") {
                        micSocket.put(session, "off");
                    } else if (msg == "unmute") {
                        micSocket.put(session, "on");
                    } else if (msg == "videoon") {
                        videoSocket.put(session, "on");
                    } else if (msg == "videooff") {
                        videoSocket.put(session, "off");
                    }
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().equals(session) == false) {
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    null, "action", msg, null, session.getId(), null, null, null, null, null, null
                            ));
                        }

                    }
                }
                break;

            case MSG_TYPE_LEAVE:
                sessionIdToRoomMap.remove(session.getId());
                break;

            default:
                logger.debug("[ws] Type of the received message {} is undefined!", message.getType());
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            synchronized (session) {
                String json = objectMapper.writeValueAsString(message);
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug("An error occured: {}", e.getMessage());
        }
    }

}
