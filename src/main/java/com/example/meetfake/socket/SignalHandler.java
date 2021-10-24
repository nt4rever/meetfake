package com.example.meetfake.socket;

import com.example.meetfake.domain.Room;
import com.example.meetfake.domain.RoomService;
import com.example.meetfake.domain.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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

    private static final String MSG_TYPE_DRAW = "draw";
    private static final String MSG_TYPE_GET_CANVAS = "getCanvas";
    private static final String MSG_TYPE_CLEAR_BOARD = "clearBoard";
    private static final String MSG_TYPE_STORE_CANVAS = "store canvas";


    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        // webSocket has been opened, send a message to the client
        // when data field contains 'true' value, the client starts negotiating
        // to establish peer-to-peer connection, otherwise they wait for a counterpart
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws JsonProcessingException {
        System.out.println("Remove client " + session.getId());
        Room rm = sessionIdToRoomMap.get(session.getId());
        if (rm != null) {
            Map<WebSocketSession, String> clients = roomService.getClients(rm);
            String username = clients.get(session);
            roomService.removeClientByKey(rm, session);
            Map<String, String> socketName = new HashMap<>();
            for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                socketName.put(client.getKey().getId(), client.getValue());
            }
            for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                JSONObject objectLeave = new JSONObject();
                objectLeave.put("sid", session.getId());
                sendMessage(client.getKey(), new WebSocketMessage(
                        username,
                        "leave",
                        objectLeave.toString()
                ));

                JSONObject objectCount = new JSONObject();
                objectCount.put("userCount", String.valueOf(clients.size()));
                objectCount.put("listAttendies", objectMapper.writeValueAsString(socketName));
                sendMessage(client.getKey(), new WebSocketMessage(
                        username,
                        "user count",
                        objectCount.toString()
                ));
            }
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
        String username = message.getFrom();
        JSONObject dataBody = new JSONObject(objectMapper.writeValueAsString(message.getData()));
        switch (message.getType()) {
            case MSG_TYPE_TEXT:
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    JSONObject objectText = new JSONObject();
                    objectText.put("message", dataBody.get("message"));
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (!client.getKey().getId().equals(session.getId())) {
                            sendMessage(client.getKey(), new WebSocketMessage(username, "text", objectText.toString()));
                        }
                    }
                }
                break;
            case MSG_TYPE_OFFER:
//                System.out.println("offer " + session.getId());
                Object offer = dataBody.get("offer");
                sid = dataBody.get("sid");
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    micSocket = roomService.getMicSocket(rm);
                    videoSocket = roomService.getVideoSocket(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().getId().equals(sid)) {
                            JSONObject objectOffer = new JSONObject();
                            objectOffer.put("sid", session.getId());
                            objectOffer.put("offer", offer);
                            objectOffer.put("micinf", micSocket.get(session));
                            objectOffer.put("vidinf", videoSocket.get(session));
                            objectOffer.put("cname", username);
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    username, "offer", objectOffer.toString()
                            ));
                        }
                    }
                }
                break;
            case MSG_TYPE_ANSWER:
//                System.out.println("answer " + session.getId());
                Object answer = dataBody.get("answer");
                sid = dataBody.get("sid");
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().getId().equals(sid)) {
                            JSONObject objectAns = new JSONObject();
                            objectAns.put("sid", session.getId());
                            objectAns.put("answer", answer);
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    username, "answer", objectAns.toString()
                            ));
                        }
                    }
                }
                break;

            case MSG_TYPE_ICE:
//                System.out.println("ice " + session.getId());
                Object candidate = dataBody.get("candidate");
                sid = dataBody.get("sid");
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().getId().equals(sid)) {
                            JSONObject objectIce = new JSONObject();
                            objectIce.put("candidate", candidate);
                            objectIce.put("sid", session.getId());
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    username, "ice", objectIce.toString()
                            ));
                        }
                    }
                }
                break;

            case MSG_TYPE_JOIN:
                room = roomService.findRoomByStringId((String) dataBody.get("roomId"))
                        .orElseThrow(() -> new IOException("Invalid room number received!"));
                roomService.addClient(room, username, session);
                sessionIdToRoomMap.put(session.getId(), room);
                clients = roomService.getClients(room);
                micSocket = roomService.getMicSocket(room);
                videoSocket = roomService.getVideoSocket(room);
                micSocket.put(session, "on");
                videoSocket.put(session, "on");
                ArrayList<String> conc = new ArrayList<>();
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
                JSONObject objectJoin = new JSONObject();
                objectJoin.put("conc", conc);
                objectJoin.put("socketName", socketName);
                objectJoin.put("micinf", micInfo);
                objectJoin.put("vidinf", videoInfo);
                sendMessage(session, new WebSocketMessage(
                        username,
                        "join",
                        objectJoin.toString()));

                for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                    JSONObject objectCount = new JSONObject();
                    objectCount.put("userCount", String.valueOf(clients.size()));
                    objectCount.put("listAttendies", objectMapper.writeValueAsString(socketName));
                    sendMessage(client.getKey(), new WebSocketMessage(
                            username,
                            "user count",
                            objectCount.toString()
                    ));
                    JSONObject objectMess = new JSONObject();
                    objectMess.put("message", username + " join!");
                    sendMessage(client.getKey(), new WebSocketMessage(
                            "Bot",
                            "text",
                            objectMess.toString()
                    ));
                }
                System.out.println(message.getFrom() + " join room " + room.getId() + " session " + session.getId());
                break;

            case MSG_TYPE_ACTION:
                String msg = (String) dataBody.get("action");
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
                            JSONObject objectAction = new JSONObject();
                            objectAction.put("action", msg);
                            objectAction.put("sid", session.getId());
                            sendMessage(client.getKey(), new WebSocketMessage(
                                    username, "action", objectAction.toString()
                            ));
                        }
                    }
                }
                break;

            case MSG_TYPE_GET_CANVAS:
                if (rm != null) {
                    if (roomService.getBoard(rm) != null) {
                        JSONObject objectBoard = new JSONObject();
                        objectBoard.put("url", roomService.getBoard(rm));
                        sendMessage(session, new WebSocketMessage(
                                username, "getCanvas", objectBoard.toString()
                        ));
                    }
                }
                break;

            case MSG_TYPE_STORE_CANVAS:
                if (rm != null)
                    roomService.setBoard(rm, (String) dataBody.get("url"));
                break;

            case MSG_TYPE_CLEAR_BOARD:
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().equals(session) == false)
                            sendMessage(client.getKey(), new WebSocketMessage(username, "clearBoard", null));
                    }
                }
                break;

            case MSG_TYPE_DRAW:
                if (rm != null) {
                    clients = roomService.getClients(rm);
                    for (Map.Entry<WebSocketSession, String> client : clients.entrySet()) {
                        if (client.getKey().equals(session) == false)
                            sendMessage(client.getKey(), new WebSocketMessage(username, "draw", dataBody.toString()));
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
