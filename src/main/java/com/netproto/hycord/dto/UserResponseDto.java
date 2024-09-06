package com.netproto.hycord.dto;

import lombok.Data;

import java.util.UUID;

public class UserResponseDto {
    @Data
    public static class CREATE {
        private final String key;
    }
}
