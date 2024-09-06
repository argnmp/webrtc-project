package com.netproto.hycord.dto;

import com.netproto.hycord.domain.Rooms;
import lombok.Data;

import java.util.UUID;

public class RoomResponseDto {
    @Data
    public static class Info {
        private final UUID id;
        private final String name;
        private final UUID userId;
    }
}
