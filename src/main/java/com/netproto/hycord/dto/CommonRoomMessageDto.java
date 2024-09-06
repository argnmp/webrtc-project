package com.netproto.hycord.dto;

import lombok.Data;

import java.util.UUID;

// socket message를 정의합니다.
@Data
public class CommonRoomMessageDto {
    private final CommonRoomMessageType type;
    private final UUID id;
    private final String name;
}
