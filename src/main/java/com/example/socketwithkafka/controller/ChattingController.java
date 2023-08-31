package com.example.socketwithkafka.controller;

import com.example.socketwithkafka.DTO.ChattingHistoryDAO;
import com.example.socketwithkafka.DTO.ChattingMessage;
import com.example.socketwithkafka.kafka.Receiver;
import com.example.socketwithkafka.kafka.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ChattingController {

    @Autowired
    private Sender sender;

    @Autowired
    private Receiver receiver;

    @Autowired
    private ChattingHistoryDAO chattingHistoryDAO;

    @Autowired
    private ChatRoomManager chatRoomManager;


    // 클라이언트에서 "/app/message"로 메시지를 보내면 이 메서드가 호출됨
    // "url/app/message"로 들어오는 메시지를 "/topic/public"을 구독하고있는 사람들에게 송신

    //WebSocket으로 클라이언트에서 /app/message로 메세지를 보낼 때, 이 메소드가 호출됨. 여기서 메세지를 Kafka 토픽으로 보내도록 구현되어있음
    @MessageMapping("/createRoom")
    public void createRoom(String chatRoomName) {
        chatRoomManager.createChatRoom(chatRoomName);
    }

    @MessageMapping("/message/{chatRoomName}") // 각 채팅방별 URL
    public void sendMessage(@DestinationVariable String chatRoomName, ChattingMessage message) throws Exception {
        // 적절한 채팅방 토픽을 가져와서 메시지 전송
        String topic = chatRoomManager.getTopicForChatRoom(chatRoomName);
        message.setTimeStamp(System.currentTimeMillis());
        chattingHistoryDAO.save(message);
        sender.send(topic, message);
    }

    // 클라이언트에서 "/history"로 요청하면 채팅 기록을 반환함
    @RequestMapping("/history")
    public List<ChattingMessage> getChattingHistory() throws Exception {
        System.out.println("history!");
        return chattingHistoryDAO.get();
    }

    // 클라이언트에서 "/app/file"로 파일을 보내면 이 메서드가 호출됨
    @MessageMapping("/file")
    @SendTo("/topic/chatting")
    public ChattingMessage sendFile(ChattingMessage message) throws Exception {
        return new ChattingMessage(message.getFileName(), message.getRawData(), message.getUser());
    }

}