package com.netproto.hycord.controller;

import com.netproto.hycord.domain.RoomChat;
import com.netproto.hycord.dto.RoomRequestDto;
import com.netproto.hycord.service.RoomService;
import com.netproto.hycord.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SignalController {
    private final RoomService roomService;
    private final UserService userService;

    @MessageMapping("/room/{roomId}/offer/{key}")
    @SendTo("/topic/room/{roomId}/offer/{key}")
    public String offer(@Payload String offer, @DestinationVariable(value = "roomId") String roomId,
                                  @DestinationVariable(value = "key") String key) {
        log.info("room {} | {} -> offer {}", roomId, key, offer);
        return offer;
    }

    @MessageMapping("/room/{roomId}/answer/{key}")
    @SendTo("/topic/room/{roomId}/answer/{key}")
    public String answer(@Payload String answer, @DestinationVariable(value = "roomId") String roomId,
                                   @DestinationVariable(value = "key") String key) {
        log.info("room {} | {} -> answer {}", roomId, key, answer);
        return answer;
    }

    @MessageMapping("/room/{roomId}/ice/{key}")
    @SendTo("/topic/room/{roomId}/ice/{key}")
    public String ice(@Payload String ice, @DestinationVariable(value = "roomId") String roomId,
                      @DestinationVariable(value = "key") String key) {
        log.info("room {} | {} -> ice {}", roomId, key, ice);
        return ice;
    }

    // roomId에 입장하려는 client는 signaling server에 /app/req/{roomId}로 key를 요청합니다.
    // signaling server는 roomId에 입장해 있는 client에 /topic/req/{roomId}로 key를 요청합니다.
    @MessageMapping("/room/{roomId}/key/req")
    @SendTo("/topic/room/{roomId}/key/req")
    public String requestKey(@Payload String msg, @DestinationVariable(value = "roomId") String roomId) {
        log.info("room {} | key request", roomId);
        return msg;
    }

    // roomId에 입장해있는 client는 /topic/req/{roomId} 에서 key를 보내라는 요청을 받습니다.
    // 요청을 받은 client는 /app/res/{roomId}로 자신의 key를 전송합니다.
    @MessageMapping("/room/{roomId}/key/res")
    @SendTo("/topic/room/{roomId}/key/res")
    public String respondKey(@Payload String msg, @DestinationVariable(value = "roomId") String roomId) {
        log.info("room {} | key respond: {}", roomId, msg);
        return msg;
    }

    // user가 room에서 연결을 종료할 때 나타나는 이벤트이다.
    @MessageMapping("/room/{roomId}/disconnect")
    @SendTo("/topic/room/{roomId}/disconnect")
    public String disconnect(@Payload String msg, @DestinationVariable(value = "roomId") String roomId) {
        log.info("[disconnect] : {}", msg);
        return msg;
    }

    // user room에서 text message를 교환하기 위한 endpoint
    @MessageMapping("/room/{roomId}/chat")
    @SendTo("/topic/room/{roomId}/chat")
    public Optional<RoomChat> roomChat(@Payload RoomRequestDto.Chat data, @DestinationVariable(value = "roomId") UUID roomId){
        return roomService.addRoomChat(roomId, data);
    }

    // socket 연결 자체를 끊을 때, User를 삭제합니다.
    @MessageMapping("/disconnect")
    public void socketDisconnect(@Payload String uuid){
        userService.deleteUser(uuid);
    }
}
