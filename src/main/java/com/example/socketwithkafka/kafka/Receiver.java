package com.example.socketwithkafka.kafka;

import com.example.socketwithkafka.DTO.ChattingMessage;
import com.example.socketwithkafka.controller.ChatRoomManager;
import com.example.socketwithkafka.socket.WebSocketConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class Receiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private ChatRoomManager chatRoomManager; // 채팅방 관리 클래스 추가

    @Autowired
    private WebSocketConfig webSocketConfig;

    @KafkaListener(id = "main-listener", topics = "kafka-chatting")
    public void receive(ChattingMessage message) throws Exception {
        LOGGER.info("message='{}'", message);
        //이름 가져와야함 String chatRoomName = extractChatRoomName(webSocketConfig.extractChatRoomName()); // 채팅방 이름 추출
        String topic = chatRoomManager.getTopicForChatRoom(chatRoomName); // 채팅방 토픽 가져오기
        HashMap<String, String> msg = new HashMap<>();
        msg.put("timestamp", Long.toString(message.getTimeStamp()));
        msg.put("message", message.getMessage());
        msg.put("author", message.getUser());

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(msg);

        this.template.convertAndSend("/topic/" + topic, json); // 해당 채팅방 토픽으로 전송
    }

    private String extractChatRoomName(String topic) {
        // 예: "chatroom-room1" -> "room1"
        return topic.replace("chatroom-", "");
    }
}
