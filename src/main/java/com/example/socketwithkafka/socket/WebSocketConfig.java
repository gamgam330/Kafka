package com.example.socketwithkafka.socket;

import com.example.socketwithkafka.controller.ChatRoomManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private ChatRoomManager chatRoomManager;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chatting")
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String chatRoomName = extractChatRoomName(accessor.getDestination()); // 채팅방 이름 추출
                    String topic = chatRoomManager.getTopicForChatRoom(chatRoomName); // 채팅방 이름으로 토픽 가져오기
                    accessor.setDestination("/topic/" + topic); // 채팅방 토픽으로 변경
                }

                return message;
            }
        });
    }

    private String extractChatRoomName(String destination) {
        // 적절한 로직으로 채팅방 이름 추출
        // 예: "/app/chat/room1" -> "room1"
        // "/app/chat/room2" -> "room2"
        return destination.substring(destination.lastIndexOf('/') + 1);
    }
}
