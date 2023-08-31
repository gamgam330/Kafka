package com.example.socketwithkafka.controller;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatRoomManager {
    private Map<String, String> chatRoomTopics = new ConcurrentHashMap<>(); // 채팅방 이름과 토픽 이름을 매핑

    public void createChatRoom(String chatRoomName) {
        String topicName = "chatroom-" + chatRoomName; // 각 채팅방에 고유한 토픽 이름 부여
        chatRoomTopics.put(chatRoomName, topicName);
    }

    public String getTopicForChatRoom(String chatRoomName) {
        return chatRoomTopics.get(chatRoomName);
    }
}
