package com.netproto.hycord.dto;

import lombok.Data;

import java.util.UUID;

public class RoomRequestDto {
    @Data
    public static class Create {
        private String name;
        private UUID userId;
    }

    @Data
    public static class Update {
        private String name;
    }

    @Data
    public static class Chat {
        private final UUID userId;
        private final String name;
        private final String message;
    }
}
